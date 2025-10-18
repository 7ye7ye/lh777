package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 医生表
 * @TableName doctor
 */
@TableName(value ="doctor")
@Data
public class Doctor {
    /**
     * 医生唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long doctorId;

    /**
     * 关联用户表
     */
    private Long userId;

    /**
     * 所属科室ID（二级科室，如“消化内科”）
     */
    private Long deptId;

    /**
     * 职称（如“主治医师”“主任医师”）
     */
    private String title;

    /**
     * 擅长领域（如“胃炎、胃溃疡诊疗”）
     */
    private String specialty;

    /**
     * 医生简介
     */
    private String doctorDesc;

    /**
     * 医生头像URL
     */
    private String avatar;

    /**
     * 出诊状态（0-暂停出诊；1-正常出诊）
     */
    private Integer isActive;

    /**
     * 信息修改审核状态（0-未提交修改；1-待审核；2-已通过；3-已驳回）
     */
    private Integer updateVerify;

    /**
     * 医生姓名
     */
    private String doctorName;
}
