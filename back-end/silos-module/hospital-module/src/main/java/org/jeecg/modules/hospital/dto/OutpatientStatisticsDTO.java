package org.jeecg.modules.hospital.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 门诊量统计DTO
 */
@Data
public class OutpatientStatisticsDTO {
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 科室ID
     */
    private Long deptId;
    
    /**
     * 科室名称
     */
    private String deptName;
    
    /**
     * 父科室ID（用于显示二级科室）
     */
    private Long parentDeptId;
    
    /**
     * 父科室名称（用于显示二级科室）
     */
    private String parentDeptName;
    
    /**
     * 门诊量
     */
    private Integer visitCount;
    
    /**
     * 总门诊量
     */
    private Integer totalVisitCount;
    
    /**
     * 对比历史数据（如果有）
     */
    private Integer compareVisitCount;
    
    /**
     * 增长率（百分比）
     */
    private BigDecimal growthRate;
}

