package org.jeecg.config.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.HosUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @Description: 用户登录鉴权和获取用户授权
 * @Author: Scott
 * @Date: 2019-4-23 8:13
 * @Version: 1.1
 */
@Component
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ShiroRealm extends AuthorizingRealm {
	@Lazy
    @Resource
    private CommonAPI commonApi;

    @Lazy
    @Resource
    private RedisUtil redisUtil;

    /**
     * 必须重写此方法，不然Shiro会报错
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 权限信息认证(包括角色以及权限)是用户访问controller的时候才进行验证(redis存储的此处权限信息)
     * 触发检测用户权限时才会调用此方法，例如checkRole,checkPermission
     *
     * @param principals 身份信息
     * @return AuthorizationInfo 权限信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        log.debug("===============Shiro权限认证开始============ [ roles、permissions]==========");
        String userId = null;
        if (principals != null) {
            HosUser sysUser = (HosUser) principals.getPrimaryPrincipal();
            userId = sysUser.getUserId().toString();
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        // 设置用户拥有的角色集合，比如“admin,test”
        Set<String> roleSet = commonApi.queryUserRolesById(userId);
        //System.out.println(roleSet.toString());
        info.setRoles(roleSet);

        // 设置用户拥有的权限集合，比如“sys:role:add,sys:user:add”
        Set<String> permissionSet = commonApi.queryUserAuths(userId);
        info.addStringPermissions(permissionSet);
        //System.out.println(permissionSet);
        log.info("===============Shiro权限认证成功==============");
        return info;
    }

    /**
     * 用户信息认证是在用户进行登录的时候进行验证(不存redis)
     * 也就是说验证用户输入的账号和密码是否正确，错误抛出异常
     *
     * @param auth 用户登录的账号密码信息
     * @return 返回封装了用户信息的 AuthenticationInfo 实例
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        log.debug("===============Shiro身份认证开始============doGetAuthenticationInfo==========");
        String token = (String) auth.getCredentials();
        if (token == null) {
            HttpServletRequest req = SpringContextUtils.getHttpServletRequest();
            log.info("————————身份认证失败——————————IP地址:  "+ oConvertUtils.getIpAddrByRequest(req) +"，URL:"+req.getRequestURI());
            throw new AuthenticationException("token为空!");
        }
        // 校验token有效性（修改：返回值从LoginUser改为HosUser）
        HosUser hosUser = null;
        try {
            hosUser = this.checkUserTokenIsEffect(token);
        } catch (AuthenticationException e) {
            JwtUtil.responseError(SpringContextUtils.getHttpServletResponse(),401,e.getMessage());
            e.printStackTrace();
            return null;
        }
        // 修改：将HosUser作为身份信息传入
        return new SimpleAuthenticationInfo(hosUser, token, getName());
    }

    /**
     * 校验token的有效性（适配HosUser表结构）
     *
     * @param token
     */
    public HosUser checkUserTokenIsEffect(String token) throws AuthenticationException {
        // 解密获得用户名（HosUser中对应userAccount字段）
        String username = JwtUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("token非法无效!");
        }

        // 查询用户信息（修改：从HosUser表查询，方法名调整为getHosUserByAccount）
        log.debug("———校验token是否有效————checkUserTokenIsEffect——————— "+ token);
        HosUser hosUser = TokenUtils.getHosUserByAccount(username, commonApi, redisUtil);
        if (hosUser == null) {
            throw new AuthenticationException("用户不存在!");
        }
        System.out.println("从token中取到的hosuser"+hosUser);
        // 判断用户状态（修改：HosUser中状态字段为status，1=正常）
        if (hosUser.getStatus() != 1) {
            throw new AuthenticationException("账号已被锁定或未激活,请联系管理员!");
        }

        // 校验token是否超时失效（修改：使用固定密钥验证，不依赖用户密码）
        if (!jwtTokenRefresh(token, username)) {
            throw new AuthenticationException(CommonConstant.TOKEN_IS_INVALID_MSG);
        }

        return hosUser;
    }
    /**
     * JWTToken刷新生命周期 （实现： 用户在线操作不掉线功能）
     * 1、登录成功后将用户的JWT生成的Token作为k、v存储到cache缓存里面(这时候k、v值一样)，缓存有效期设置为Jwt有效时间的2倍
     * 2、当该用户再次请求时，通过JWTFilter层层校验之后会进入到doGetAuthenticationInfo进行身份验证
     * 3、当该用户这次请求jwt生成的token值已经超时，但该token对应cache中的k还是存在，则表示该用户一直在操作只是JWT的token失效了，程序会给token对应的k映射的v值重新生成JWTToken并覆盖v值，该缓存生命周期重新计算
     * 4、当该用户这次请求jwt在生成的token值已经超时，并在cache中不存在对应的k，则表示该用户账户空闲超时，返回用户信息已失效，请重新登录。
     * 注意： 前端请求Header中设置Authorization保持不变，校验有效性以缓存中的token为准。
     *       用户过期时间 = Jwt有效时间 * 2。
     *
     * @param userName
     * @param passWord
     * @return
     */
    public boolean jwtTokenRefresh(String token, String userName) {
        String cacheToken = String.valueOf(redisUtil.get(CommonConstant.PREFIX_USER_TOKEN + token));
        if (oConvertUtils.isNotEmpty(cacheToken)) {
            // 校验token有效性（使用固定密钥）
            if (!JwtUtil.verify(cacheToken, userName, getFixedSecret())) {
                String newAuthorization = JwtUtil.sign(userName, getFixedSecret());
                // 设置超时时间
                redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, newAuthorization);
                redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME *2 / 1000);
                log.debug("——————————用户在线操作，更新token保证不掉线—————————jwtTokenRefresh——————— "+ token);
            }
            //update-begin--Author:scott  Date:20191005  for：解决每次请求，都重写redis中 token缓存问题
//			else {
//				// 设置超时时间
//				redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, cacheToken);
//				redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME / 1000);
//			}
            //update-end--Author:scott  Date:20191005   for：解决每次请求，都重写redis中 token缓存问题
            return true;
        }

        //redis中不存在此TOEKN，说明token非法返回false
        return false;
    }

    /**
     * 获取固定的JWT密钥（与登录时使用的密钥保持一致）
     * 这里使用一个固定的密钥，确保JWT验证的一致性
     */
    private String getFixedSecret() {
        // 使用与HosUserServiceImpl中相同的盐值作为固定密钥
        return "caoyue";
    }

    /**
     * 清除当前用户的权限认证缓存
     *
     * @param principals 权限信息
     */
    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
        //update-begin---author:scott ---date::2024-06-18  for：【TV360X-1320】分配权限必须退出重新登录才生效，造成很多用户困扰---
        super.clearCachedAuthorizationInfo(principals);
        //update-end---author:scott ---date::2024-06-18  for：【TV360X-1320】分配权限必须退出重新登录才生效，造成很多用户困扰---
    }
}
