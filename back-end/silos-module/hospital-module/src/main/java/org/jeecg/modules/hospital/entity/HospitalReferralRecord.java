package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 转诊记录实体
 */
@Data
@TableName(value = "hospital_referral_record")
public class HospitalReferralRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联转诊申请ID */
    private Long referralId;

    /** 转诊单编号 */
    private String referralCode;

    /** 患者姓名 */
    private String patientName;

    /** 患者身份证号 */
    private String patientIdCard;

    /** 实际就诊医院ID */
    private Long actualHospitalId;

    /** 实际就诊医院名称 */
    private String actualHospitalName;

    /** 实际就诊科室ID */
    private Long actualDeptId;

    /** 实际就诊科室名称 */
    private String actualDeptName;

    /** 实际就诊医生ID */
    private Long actualDoctorId;

    /** 实际就诊医生名称 */
    private String actualDoctorName;

    /** 转诊日期 */
    private LocalDate referralDate;

    /** 实际就诊日期 */
    private LocalDate visitDate;

    /** 诊断结果 */
    private String diagnosisResult;

    /** 治疗方案 */
    private String treatmentPlan;

    /** 用药情况 */
    private String medications;

    /** 检查项目 */
    private String examinations;

    /** 总费用 */
    private BigDecimal totalCost;

    /** 医保报销金额 */
    private BigDecimal coveredCost;

    /** 患者自付金额 */
    private BigDecimal patientCost;

    /** 转诊状态(PENDING/SENT/RECEIVED/COMPLETED/FOLLOWED_UP) */
    private String referralStatus;

    /** 医疗文档(JSON) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> medicalDocuments;

    /** 随访状态(NONE/PENDING/COMPLETED) */
    private String followUpStatus;

    /** 随访日期 */
    private LocalDate followUpDate;

    /** 随访医生 */
    private String followUpDoctor;

    /** 随访记录 */
    private String followUpNotes;

    /** 恢复状况 */
    private String recoveryStatus;

    /** 满意度评分(1-5) */
    private Integer feedbackRating;

    /** 反馈意见 */
    private String feedbackComments;

    /** 转出时间 */
    private LocalDateTime transferOutTime;

    /** 转入确认时间 */
    private LocalDateTime transferInTime;

    /** 完成时间 */
    private LocalDateTime completionTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}