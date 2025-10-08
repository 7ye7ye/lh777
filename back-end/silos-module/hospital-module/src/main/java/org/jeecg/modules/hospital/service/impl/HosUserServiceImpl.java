package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.hospital.common.ErrorCode;
import org.jeecg.modules.hospital.dto.HosUserLoginResult;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.exception.BusinessException;
import org.jeecg.modules.hospital.service.HosUserService;
import org.jeecg.modules.hospital.mapper.HosUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

import static org.jeecg.modules.hospital.contant.UserContant.ACTIVE;
import static org.jeecg.modules.hospital.contant.UserContant.PATIENT;

/**
* @author Administrator
* @description 针对表【hos_user(用户表)】的数据库操作Service实现
* @createDate 2025-09-22 21:05:09
*/
@Service
public class HosUserServiceImpl extends ServiceImpl<HosUserMapper, HosUser>
    implements HosUserService{
    @Autowired
    private HosUserMapper hosUserMapper;
    /**
     * 盐值：混淆密码
     */
    private static final String SALT="caoyue";
    /**
     * 用户登录态键：通过键可以找到唯一的一条数据
     */
    private static final String USER_LOGIN_STATE="user_login_state";

    // 注入原有项目的Redis工具类（用于存储Token）
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public int userRegister(String userAccount, String userPassword, String checkPassword) {
        //一，校验
        //1.非空
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        //2.用户账号需大于3
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        //2.用户密码需大于7
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        //4.用户名不能包含特殊字符
        String regex = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$"; // 只允许中文、英文字母、数字
        if (!userAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名不能包含特殊字符");
        }

        //两次输入密码需相同
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不同");
        }
        //3.用户名不能重复：查询数据库(放最后节约型性能)
        QueryWrapper<HosUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = hosUserMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已注册");
        }

        //二.加密
        String newPassword= DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());

        //插入数据
        HosUser user=new HosUser();
        user.setUserAccount(userAccount);
        user.setUserPassword(newPassword);
        user.setUserType(PATIENT);
        user.setStatus(ACTIVE);
        // 设置创建时间为当前时间
        user.setCreateTime(LocalDateTime.now());
        // 设置更新时间为当前时间
        user.setUpdateTime(LocalDateTime.now());

        boolean saveResult=this.save(user);//service的方法，userMapper.insert(user)返回Int类型
        if(!saveResult){
            return -1;
        }

        return user.getUserId().intValue();

    }

    @Override
    public HosUserLoginResult userLogin(String userAccount, String password, HttpServletRequest request) {
        // 一、校验用户名和密码是否合法
        if(StringUtils.isAnyBlank(userAccount,password)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码为空");
        }
        if(userAccount.length()<2||password.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码过短");
        }
        String regex = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$";
        if (!userAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名不能包含特殊字符");
        }

        // 二、加密处理
        String newPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        // 三、查询用户是否存在
        QueryWrapper<HosUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", newPassword);
        HosUser user = hosUserMapper.selectOne(queryWrapper);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在，请注册");
        }

        // 四、用户脱敏
        HosUser saftyUser = getSaftyUser(user);

        // 五、生成原有系统兼容的Token（核心逻辑）
        String token = generateToken(userAccount, newPassword);

        // 六、存储Token到Redis（与原有系统保持一致）
        storeTokenInRedis(token);

        // 七、记录登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        System.out.println("Backend recorded login user data: " + saftyUser);
        System.out.println("Session ID (save): " + request.getSession().getId());

        // 八、返回包含Token的结果
        HosUserLoginResult result = new HosUserLoginResult();
        result.setUser(saftyUser);
        result.setToken(token);
        return result;
    }

    /**
     * 生成与原有系统兼容的JWT Token
     */
    private String generateToken(String username, String password) {
        // 使用固定密钥生成Token，确保验证时的一致性
        return JwtUtil.sign(username, getFixedSecret());
    }

    /**
     * 获取固定的JWT密钥（与ShiroRealm中的密钥保持一致）
     */
    private String getFixedSecret() {
        return SALT; // 使用盐值作为固定密钥
    }

    /**
     * 将Token存储到Redis（与原有系统保持一致的存储方式）
     */
    private void storeTokenInRedis(String token) {
        // 与原有系统保持相同的Key前缀和过期时间
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        // 过期时间与原有系统保持一致（JwtUtil.EXPIRE_TIME单位是毫秒）
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);
    }


    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    public HosUser getSaftyUser(HosUser originUser){
        if(originUser==null){
            return null;
        }
        HosUser saftyUser=new HosUser();
        saftyUser.setUserId(originUser.getUserId());
        saftyUser.setUserAccount(originUser.getUserAccount());
        saftyUser.setUserType(originUser.getUserType());
        return saftyUser;
    }

    /**
     * 根据账户查找用户
     * 
     * @param account
     * @return
     */
    public org.jeecg.common.system.vo.HosUser getHosUserByAccount(String account){
        HosUser hosUser = hosUserMapper.selectOne(new QueryWrapper<HosUser>().eq("user_account", account));
        if (hosUser == null) {
            return null;
        }
        return hosUser.convertToVO();
    }
}




