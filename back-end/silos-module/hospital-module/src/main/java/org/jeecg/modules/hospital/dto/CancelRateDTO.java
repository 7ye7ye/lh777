package org.jeecg.modules.hospital.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 退号率统计DTO
 */
@Data
public class CancelRateDTO {
    /**
     * 科室ID（按科室统计时）
     */
    private Long deptId;
    
    /**
     * 科室名称
     */
    private String deptName;
    
    /**
     * 医生ID（按医生统计时）
     */
    private Long doctorId;
    
    /**
     * 医生姓名
     */
    private String doctorName;
    
    /**
     * 号别ID（按号别统计时）
     */
    private Long typeId;
    
    /**
     * 号别名称
     */
    private String typeName;
    
    /**
     * 总挂号量
     */
    private Integer totalRegistration;
    
    /**
     * 退号数量
     */
    private Integer cancelCount;
    
    /**
     * 退号率（百分比）
     */
    private BigDecimal cancelRate;
}

