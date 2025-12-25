package org.jeecg.modules.hospital.controller.admin.dto;

import lombok.Data;

@Data
public class DoctorRegisterRequest {
    private String userAccount;
    private String userPassword;
    private int userType;
    private String doctorName;
    private Long deptId;
    private String title;
    private Long titleId;
    private String specialty;
    private String doctorDesc;
    private String email;
    private Boolean isActive;
}

