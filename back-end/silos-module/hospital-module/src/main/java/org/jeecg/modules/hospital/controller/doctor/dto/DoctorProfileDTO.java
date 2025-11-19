package org.jeecg.modules.hospital.controller.doctor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "医生个人信息DTO")
public class DoctorProfileDTO {
    private Long doctorId;
    private String doctorName;
    private Long userId;
    private Long deptId;
    private String deptName;
    private String title;
    private String specialty;
    private String doctorDesc;
    private String avatar;
    private Integer isActive;
    private Integer updateVerify;
    // 追加：HosUser 字段
    private String userAccount;
    private String email;
    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}