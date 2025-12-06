package org.jeecg.modules.hospital.controller.doctor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新医生个人信息请求体")
public class DoctorProfileUpdateRequest {
    private Long doctorId;
    // Doctor
    private String doctorName;
    private Long deptId;
    private String title;
    private String specialty;
    private String doctorDesc;
    private String avatar;
    private Integer isActive;
    // HosUser
    private String userAccount;
    private String email;
    // 新增字段
    private Integer maxQuota;


}