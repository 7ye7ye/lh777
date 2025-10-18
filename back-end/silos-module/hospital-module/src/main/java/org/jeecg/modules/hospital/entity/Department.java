package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 科室表
 * @TableName department
 */
@TableName(value ="department")
@Data
public class Department {
    /**
     * 科室唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long deptId;

    /**
     * 科室名称（如“内科”“消化内科”）
     */
    private String deptName;

    /**
     * 父科室ID（顶级科室为0，如“内科”的父科室为0，“消化内科”父科室为内科ID）
     */
    private Long parentDeptId;

    /**
     * 科室级别（1-一级科室；2-二级科室）
     */
    private Integer deptLevel;

    /**
     * 科室简介（如“负责消化系统疾病诊疗”）
     */
    private String deptDesc;

    /**
     * 科室位置（如“门诊楼3层东侧”，用于导航扩展功能）
     */
    private String location;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
