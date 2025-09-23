package org.jeecg.modules.hospital.contant;

/**
 * 用户常量
 *
 * @author caoyue
 */
public interface UserContant {
    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login_state";

    //------------权限
    //用户身份
    int PATIENT = 1;
    int DOCTOR = 2;
    int ADMIN = 3;

    //用户状态
    int INACTIVE = 0;
    int ACTIVE = 1;
    int DISABLED = 2;
}
