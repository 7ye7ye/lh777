package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import org.jeecg.modules.hospital.enums.ReferralQuotaAction;
import org.jeecg.modules.hospital.enums.ReferralSourceType;
import org.jeecg.modules.hospital.enums.ReferralStatus;
import org.jeecg.modules.hospital.enums.ReferralTargetType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 转诊申请实体
 */
@Data
@TableName(value = "hospital_referral", autoResultMap = true)
public class ReferralApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 转诊单编号 */
    private String referralCode;

    private String patientName;
    private String gender;
    private Integer age;
    private String phone;

    private String symptoms;
    private String medicalHistory;
    private String reason;

    /** 申请来源 */
    private String sourceType;

    /** 转诊目标类型 */
    private String targetType;

    private Long targetDeptId;
    private String targetDeptName;
    private String targetHospitalName;
    
    /** 关联的挂号记录ID */
    private Long registrationRecordId;
    
    /** 自动挂号状态：0-未处理，1-成功，2-失败 */
    private Integer autoRegisterStatus;

    /** 号源处理策略 */
    private String quotaAction;

    private Long assignedScheduleId;
    private LocalDate assignedDate;
    private Integer assignedTimeSlot;
    private Integer waitNumber;

    private String status;
    private String reviewDoctor;
    private String reviewComments;
    private String rejectReason;
    private String cancelReason;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ReferralAttachment> attachments;

    private LocalDateTime applyTime;
    private LocalDateTime reviewTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public ReferralSourceType sourceTypeEnum() {
        return sourceType == null ? ReferralSourceType.PATIENT_AFTER : ReferralSourceType.valueOf(sourceType);
    }

    public ReferralTargetType targetTypeEnum() {
        return targetType == null ? ReferralTargetType.INTERNAL : ReferralTargetType.valueOf(targetType);
    }

    public ReferralQuotaAction quotaActionEnum() {
        return quotaAction == null ? null : ReferralQuotaAction.valueOf(quotaAction);
    }

    public ReferralStatus statusEnum() {
        return status == null ? ReferralStatus.PENDING : ReferralStatus.valueOf(status);
    }
}


