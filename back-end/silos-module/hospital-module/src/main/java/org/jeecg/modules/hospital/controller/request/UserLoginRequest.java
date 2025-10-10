package org.jeecg.modules.hospital.controller.request;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
public class UserLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;

    private String userAccount;
    private String userPassword;
    private String captcha; // 验证码（可选，web端不验证）
    private String checkKey; // 验证码key（可选，web端不验证）

    public String getUsername() {
        return userAccount;
    }

    public String getPassword() {
        return userPassword;
    }
}
