// org.jeecg.modules.hospital.common.dto.WebScheduleQueryDTO.java
package org.jeecg.modules.hospital.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 排班查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class WebScheduleQueryDTO {

    /**
     * 查询日期
     */
    private Date date;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 时段(1-上午,2-下午,3-晚上)
     */
    private Integer timeSlot;

    private String keyword;
}