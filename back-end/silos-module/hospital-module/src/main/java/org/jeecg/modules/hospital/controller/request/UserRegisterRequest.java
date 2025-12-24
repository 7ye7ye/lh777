package org.jeecg.modules.hospital.controller.request;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private int userType;

    public int getStatus() {
        return status;
    }

    private int status;

    public String getUserAccount() {
        return userAccount;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public String getCheckPassword() {
        return checkPassword;
    }

    public int getUserType() {
        return userType;
    }
}
