// org.jeecg.modules.hospital.common.vo.WebTodayScheduleVO.java
package org.jeecg.modules.hospital.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 今日排班VO
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class WebTodayScheduleVO {

    /**
     * 排班ID
     */
    private Long scheduleId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 排班日期
     */
    private String scheduleDate;

    /**
     * 时段(1-上午,2-下午,3-晚上)
     */
    private Integer timeSlot;

    /**
     * 已使用号源
     */
    private Integer usedQuota;

    /**
     * 状态(1-有效,0-停用)
     */
    private Integer status;

    /**
     * 号源总数
     */
    private Integer maxQuota;

    /**
     * 诊室号
     */
    private String roomNumber;

    /**
     * 剩余号源
     */
    private Integer remainingQuota;
}