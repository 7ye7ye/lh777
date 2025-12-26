package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.hospital.common.BaseResponse;
import org.jeecg.modules.hospital.common.ErrorCode;
import org.jeecg.modules.hospital.common.ResultUtils;
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
    public BaseResponse<Long> userRegister(String userAccount, String userPassword, String checkPassword, int userType, int status) {
        //一，校验
        //1.非空
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 去除首尾空格
        userAccount = userAccount.trim();
        userPassword = userPassword.trim();
        checkPassword = checkPassword.trim();

        //2.用户账号长度校验：4-20位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号不能少于4位");
        }
        if (userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号不能超过20位，当前长度：" + userAccount.length());
        }

        //3.用户密码长度校验：8-20位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码不能少于8位");
        }
        if (userPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码不能超过20位");
        }
        if (checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "确认密码不能少于8位");
        }
        if (checkPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "确认密码不能超过20位");
        }

        // 密码复杂度校验：必须包含字母、数字和特殊字符
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/]).{8,20}$";
        if (!userPassword.matches(passwordRegex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码必须包含字母、数字和特殊字符，长度8-20位");
        }

        //4.用户名/手机号格式校验
        String phoneRegex = "^1[3-9]\\d{9}$";
        String usernameRegex = "^[\\u4e00-\\u9fa5a-zA-Z0-9_.-]+$";

        if (userAccount.matches(phoneRegex)) {
            // 是手机号，格式正确
        } else if (userAccount.matches(usernameRegex)) {
            // 是用户名，校验特殊字符
            if (userAccount.contains("<") || userAccount.contains(">") || userAccount.contains("'") || userAccount.contains("\"") || userAccount.contains("/")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名不能包含以下字符：< > ' \" /");
            }
        } else {
             throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名格式不正确，只能包含中文、英文字母、数字、下划线(_)、点(.)、连字符(-)或手机号");
        }

        //5.两次输入密码需相同
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

        // 5. 校验用户类型并设置状态
        if (userType == 1) { // 患者
            user.setUserType(1);
            // 患者注册默认激活，但使用传入的状态参数
            user.setStatus(status >= 0 && status <= 2 ? status : ACTIVE);
        } else if (userType == 2) { // 医生
            user.setUserType(2);
            // 医生注册使用传入的状态参数，默认为1(正常)
            user.setStatus(status >= 0 && status <= 2 ? status : 1);
        } else {
            // 不允许注册管理员或其他角色
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法的用户类型");
        }

        // 设置创建时间为当前时间
        user.setCreateTime(LocalDateTime.now());
        // 设置更新时间为当前时间
        user.setUpdateTime(LocalDateTime.now());

        boolean saveResult=this.save(user);//service的方法，userMapper.insert(user)返回Int类型
        if(!saveResult){
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "数据库错误");
        }

// 使用成功状态码构建返回结果
        return ResultUtils.success(user.getUserId()); // 返回BaseResponse<Integer>
    }

    @Override
    public HosUserLoginResult userLogin(String userAccount, String password, HttpServletRequest request) {
        // 0. 登录失败次数校验
        String loginFailKey = CommonConstant.LOGIN_FAIL + userAccount;
        Object failCount = redisUtil.get(loginFailKey);
        if (failCount != null) {
            Integer count = Integer.parseInt(failCount.toString());
            if (count > 5) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录失败次数过多，请15分钟后再试");
            }
        }

        // 一、校验用户名和密码是否合法
        if(StringUtils.isAnyBlank(userAccount,password)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码为空");
        }
        // 去除首尾空格，避免格式校验被空白字符影响
        userAccount = userAccount.trim();
        password = password.trim();

        // 用户名长度校验：4-20位（与注册保持一致）
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名不能少于4位");
        }
        if(userAccount.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名不能超过20位");
        }

        // 密码长度校验：4-20位（登录时略宽松，允许旧密码）
        if(password.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能少于4位");
        }
        if(password.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能超过20位");
        }

        // 用户名格式校验（支持手机号和普通用户名）
        String phoneRegex = "^1[3-9]\\d{9}$";
        String usernameRegex = "^[\\u4e00-\\u9fa5a-zA-Z0-9_.-]+$";
        if (!userAccount.matches(phoneRegex) && !userAccount.matches(usernameRegex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名格式不正确，只能包含中文、英文字母、数字、下划线(_)、点(.)、连字符(-)或手机号");
        }

        // 二、加密处理
        String newPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        // 三、查询用户是否存在
        QueryWrapper<HosUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", newPassword);
        HosUser user = hosUserMapper.selectOne(queryWrapper);
        if(user == null){
            // 记录登录失败次数
            Integer count = 0;
            if (failCount != null) {
                count = Integer.parseInt(failCount.toString());
            }
            redisUtil.set(loginFailKey, ++count, 900); // 15分钟
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在，请检查用户名或密码！");
        }

        // 登录成功，清除失败记录
        redisUtil.del(loginFailKey);

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
        try {
            // 使用固定密钥生成Token，确保验证时的一致性
            String token = JwtUtil.sign(username, getFixedSecret());
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Token生成失败: 返回的Token为空");
            }
            return token;
        } catch (Exception e) {
            System.err.println("Token生成异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Token生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取固定的JWT密钥（与ShiroRealm中的密钥保持一致）
     */
    private String getFixedSecret() {
        return SALT; // 使用盐值作为固定密钥
    }

    /**
     * 将Token存储到Redis（与原有系统保持一致的存储方式）
     * 如果Redis不可用，记录日志但不影响登录流程
     */
    private void storeTokenInRedis(String token) {
        try {
            // 检查redisUtil是否可用
            if (redisUtil == null) {
                System.out.println("警告: RedisUtil未注入，跳过Token存储到Redis");
                return;
            }
            // 与原有系统保持相同的Key前缀和过期时间
            redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
            // 过期时间与原有系统保持一致（JwtUtil.EXPIRE_TIME单位是毫秒）
            redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);
            System.out.println("Token已存储到Redis: " + CommonConstant.PREFIX_USER_TOKEN + token);
        } catch (Exception e) {
            // Redis存储失败不影响登录流程，只记录日志
            System.err.println("警告: Token存储到Redis失败，但登录继续: " + e.getMessage());
            e.printStackTrace();
        }
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
        return hosUserMapper.selectOne(new QueryWrapper<HosUser>().eq("user_account", account)).convertToVO();
    }

    @Override
    public BaseResponse<Boolean> changePassword(Long userId, String oldPassword, String newPassword, String confirmPassword) {
        // 1. 参数校验
        if (userId == null || StringUtils.isAnyBlank(oldPassword, newPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }

        // 去除首尾空格
        oldPassword = oldPassword.trim();
        newPassword = newPassword.trim();
        confirmPassword = confirmPassword.trim();

        // 2. 验证新密码长度
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码长度不能少于8位");
        }

        if (newPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码长度不能超过20位");
        }

        // 3. 验证新密码复杂度：必须包含字母、数字和特殊字符
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/]).{8,20}$";
        if (!newPassword.matches(passwordRegex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码必须包含字母、数字和特殊字符，长度8-20位");
        }

        // 4. 验证两次密码输入是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的新密码不一致");
        }

        // 5. 验证新密码不能与旧密码相同
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码不能与旧密码相同");
        }

        // 6. 查询用户
        HosUser user = hosUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        // 7. 验证旧密码是否正确
        String encryptedOldPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        if (!user.getUserPassword().equals(encryptedOldPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
        }

        // 8. 加密新密码
        String encryptedNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());

        // 9. 更新密码
        user.setUserPassword(encryptedNewPassword);
        user.setUpdateTime(LocalDateTime.now());
        int updateResult = hosUserMapper.updateById(user);

        if (updateResult <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "修改密码失败");
        }

        return ResultUtils.success(true);
    }
}




