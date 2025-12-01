package org.jeecg.modules.hospital.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 转诊情况统计DTO
 */
@Data
public class ReferralStatisticsDTO {
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
     * 转诊目标类型（INTERNAL-院内转诊，EXTERNAL-院外转诊）
     */
    private String targetType;
    
    /**
     * 转诊目标类型名称
     */
    private String targetTypeName;
    
    /**
     * 转诊申请数量
     */
    private Integer applicationCount;
    
    /**
     * 已批准数量
     */
    private Integer approvedCount;
    
    /**
     * 已拒绝数量
     */
    private Integer rejectedCount;
    
    /**
     * 已取消数量
     */
    private Integer cancelledCount;
    
    /**
     * 已完成数量
     */
    private Integer completedCount;
    
    /**
     * 总转诊数量
     */
    private Integer totalCount;
    
    /**
     * 批准率（百分比）
     */
    private BigDecimal approvalRate;
    
    /**
     * 完成率（百分比）
     */
    private BigDecimal completionRate;
}




