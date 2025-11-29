package org.jeecg.modules.hospital.dto.referral;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class ReferralApplyRequest {

    @NotBlank(message = "患者姓名不能为空")
    private String patientName;

    private String gender;

    @NotNull(message = "年龄不能为空")
    private Integer age;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @NotBlank(message = "症状描述不能为空")
    private String symptoms;

    private String medicalHistory;

    @NotBlank(message = "请填写转诊原因")
    private String reason;

    /** 申请来源：DOCTOR_DIRECT / PATIENT_BEFORE / PATIENT_AFTER */
    private String sourceType;

    /** 转诊目标类型：INTERNAL / EXTERNAL */
    @NotBlank(message = "请选择转诊目标类型")
    private String targetType;

    private Long targetDeptId;

    private String targetDeptName;

    private String targetHospitalName;

    /** 期望的就诊日期（可选） */
    private String preferredDate;

    /** 关联的挂号记录ID（就诊后申请转诊时使用） */
    private Long registrationRecordId;

    private List<ReferralAttachmentPayload> attachments;
}


