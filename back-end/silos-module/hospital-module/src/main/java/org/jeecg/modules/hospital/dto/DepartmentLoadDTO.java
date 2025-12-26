package org.jeecg.modules.hospital.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Duration;

/**
 * 科室负荷统计DTO
 */
@Data
public class DepartmentLoadDTO {
    /**
     * 科室ID
     */
    private Long deptId;
    
    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 父科室ID（用于区分一级科室）
     */
    private Long parentDeptId;

    /**
     * 父科室名称（用于展示一级科室名称）
     */
    private String parentDeptName;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 医生姓名
     */
    private String doctorName;
    
    /**
     * 出诊时长（小时）
     */
    private Double visitDurationHours;
    
    /**
     * 号源总数
     */
    private Integer totalQuota;
    
    /**
     * 已使用号源
     */
    private Integer usedQuota;
    
    /**
     * 号源使用率（百分比）
     */
    private BigDecimal quotaUsageRate;
}

