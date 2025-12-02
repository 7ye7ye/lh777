package org.jeecg.modules.hospital.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 统计查询DTO
 */
@Data
public class StatisticsQueryDTO {
    /**
     * 统计类型: day/week/month
     */
    private String periodType;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 科室ID（可选，为空则统计所有科室）
     */
    private Long deptId;
    
    /**
     * 医生ID（可选，为空则统计所有医生）
     */
    private Long doctorId;
    
    /**
     * 号别ID（可选，为空则统计所有号别）
     */
    private Long typeId;
    
    /**
     * 是否对比历史数据
     */
    private Boolean compareHistory;
    
    /**
     * 对比的历史周期数（如对比上个月则传1）
     */
    private Integer comparePeriods;
}

