package org.jeecg.modules.hospital.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 挂号量统计DTO
 */
@Data
public class RegistrationStatisticsDTO {
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 总挂号量
     */
    private Integer totalRegistration;
    
    /**
     * 号别ID
     */
    private Long typeId;
    
    /**
     * 号别名称
     */
    private String typeName;
    
    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 一级/父科室ID（便于前端展示二级科室）
     */
    private Long parentDeptId;

    /**
     * 一级/父科室名称（便于前端展示二级科室）
     */
    private String parentDeptName;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 医生名称
     */
    private String doctorName;

    /**
     * 该号别挂号量
     */
    private Integer typeRegistration;
    
    /**
     * 对比历史数据（如果有）
     */
    private Integer compareRegistration;
    
    /**
     * 增长率（百分比）
     */
    private BigDecimal growthRate;
}

