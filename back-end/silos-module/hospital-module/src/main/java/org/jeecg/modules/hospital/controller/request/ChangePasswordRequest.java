package org.jeecg.modules.hospital.controller.request;

import lombok.Data;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Data
@Getter
public class ChangePasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认新密码
     */
    private String confirmPassword;
}

