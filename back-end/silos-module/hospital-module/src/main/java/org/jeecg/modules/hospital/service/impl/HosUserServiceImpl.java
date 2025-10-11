package org.jeecg.modules.hospital.service;

import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.modules.hospital.dto.HosUserLoginResult;
import org.jeecg.modules.hospital.entity.HosUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【hos_user(用户表)】的数据库操作Service
* @createDate 2025-09-22 21:05:09
*/
public interface HosUserService extends IService<HosUser> {
    /**
     * 用户注册
     *
     * @param  username 用户名
     * @param password 用户类
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    int userRegister(String username, String password, String checkPassword,String userType);

    /**
     *
     * @param username
     * @param password
     * @return 脱敏后的用户信息
     */
    HosUserLoginResult userLogin(String username, String password, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser 未脱敏用户
     * @return 安全用户信息
     */
    public HosUser getSaftyUser(HosUser originUser);

    public org.jeecg.common.system.vo.HosUser getHosUserByAccount(String account);
}