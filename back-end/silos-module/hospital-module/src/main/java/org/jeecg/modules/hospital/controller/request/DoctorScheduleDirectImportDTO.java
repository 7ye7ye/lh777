package org.jeecg.modules.hospital.controller.request;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * Excel直接导入排班数据DTO（使用数据库字段格式）
 */
@Data
public class DoctorScheduleDirectImportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 排班ID（可选，用于更新）
     */
    @Excel(name = "schedule_id", width = 15, orderNum = "0")
    private Long scheduleId;

    /**
     * 医生ID
     */
    @Excel(name = "doctor_id", width = 15, orderNum = "1")
    private Long doctorId;

    /**
     * 科室ID
     */
    @Excel(name = "dept_id", width = 15, orderNum = "2")
    private Long deptId;

    /**
     * 类型ID
     */
    @Excel(name = "type_id", width = 15, orderNum = "3")
    private Integer typeId;

    /**
     * 排班日期
     */
    @Excel(name = "schedule_date", width = 20, format = "yyyy/MM/dd", orderNum = "4")
    private String scheduleDate;

    /**
     * 时段（1-上午, 2-下午, 3-晚上）
     */
    @Excel(name = "time_slot", width = 15, orderNum = "5")
    private Integer timeSlot;

    /**
     * 已使用号源
     */
    @Excel(name = "used_quota", width = 15, orderNum = "6")
    private Integer usedQuota;

    /**
     * 状态
     */
    @Excel(name = "status", width = 15, orderNum = "7")
    private Integer status;

    /**
     * 创建时间（可选）
     */
    @Excel(name = "create_time", width = 20, orderNum = "8")
    private String createTime;

    /**
     * 更新时间（可选）
     */
    @Excel(name = "update_time", width = 20, orderNum = "9")
    private String updateTime;

    /**
     * 最大号源
     */
    @Excel(name = "max_quota", width = 15, orderNum = "10")
    private Integer maxQuota;

    /**
     * 诊室号
     */
    @Excel(name = "room_number", width = 20, orderNum = "11")
    private String roomNumber;
}

