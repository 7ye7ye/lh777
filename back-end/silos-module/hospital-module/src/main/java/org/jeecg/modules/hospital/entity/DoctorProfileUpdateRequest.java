package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医生资料修改申请表实体，对应表 doctor_profile_update_request
 */
@Data
@TableName("doctor_profile_update_request")
public class DoctorProfileUpdateRequest {

    /** 申请ID，主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 医生ID，对应 doctor.doctor_id */
    private Long doctorId;

    /** 医生姓名（冗余用于前端展示，不入库） */
    @TableField(exist = false)
    private String doctorName;

    /** 申请修改后的头像URL */
    private String avatar;

    /** 申请修改后的擅长领域 */
    private String specialty;

    /** 申请修改后的医生简介 */
    private String doctorDesc;

    /** 审核状态：1-待审核；2-已通过；3-已驳回 */
    private Integer status;

    /** 审核备注/驳回原因 */
    private String reason;

    /** 提交时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
