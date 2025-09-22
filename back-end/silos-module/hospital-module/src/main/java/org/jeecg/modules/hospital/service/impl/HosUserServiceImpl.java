package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.service.HosUserService;
import org.jeecg.modules.hospital.mapper.HosUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

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

    @Override
    public int userRegister(String userAccount, String userPassword, String checkPassword) {
        //一，校验
        //1.非空
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            //todo:修改为自定义异常
            return -1;
        }
        //2.用户名不小于2和密码不小于4
        if(userAccount.length()<2||userPassword.length()<4||checkPassword.length()<4){
            return -1;
        }
        //4.用户名不能包含特殊字符
        String regex = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$"; // 只允许中文、英文字母、数字
        if (!userAccount.matches(regex)) {
            return -1;
        }

        //两次输入密码相同
        if(!userPassword.equals(checkPassword)){
            return -1;
        }
        //3.用户名不能重复：查询数据库(放最后节约型性能)
        QueryWrapper<HosUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = hosUserMapper.selectCount(queryWrapper);
        if(count>0){
            return -1;
        }

        //二.加密
        String newPassword= DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());

        //插入数据
        HosUser user=new HosUser();
        user.setUserAccount(userAccount);
        user.setUserPassword(newPassword);
        boolean saveResult=this.save(user);//service的方法，userMapper.insert(user)返回Int类型
        if(!saveResult){
            return -1;
        }

        return user.getUserId().intValue();

    }

    @Override
    public HosUser userLogin(String userAccount, String password, HttpServletRequest request) {
        //一，校验用户名和密码是否合法
        //一，校验
        //1.非空
        if(StringUtils.isAnyBlank(userAccount,password)){
            return null;
        }
        //2.用户名不小于2和密码不小于4
        if(userAccount.length()<2||password.length()<4){
            return null;
        }
        //4.用户名不能包含特殊字符
        // 校验用户名，只允许中文、英文字母、数字
        String regex = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$"; // 只允许中文、英文字母、数字
        if (!userAccount.matches(regex)) {
            return null;
        }

        //二，加密
        String newPassword=DigestUtils.md5DigestAsHex((SALT+password).getBytes());

        //三，查询用户是否存在
        QueryWrapper<HosUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", newPassword);
        HosUser user=hosUserMapper.selectOne(queryWrapper);
        //用户不存在
        if(user==null){
            return null;
        }

        //四，用户脱敏:只返回需要显示的数据，防止数据库的数据暴露给前端
        HosUser saftyUser=getSaftyUser(user);
        //五，记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        System.out.println("Backend recorded login user data: " + saftyUser);

        System.out.println("Session ID (save): " + request.getSession().getId());

        return saftyUser;
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
}




