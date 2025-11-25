package org.jeecg.modules.hospital.controller.request;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * Excel导入排班数据DTO
 */
@Data
public class DoctorScheduleExcelImportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 医生姓名
     */
    @Excel(name = "医生姓名", width = 20, orderNum = "0")
    private String doctorName;

    /**
     * 科室名称
     */
    @Excel(name = "科室名称", width = 20, orderNum = "1")
    private String deptName;

    /**
     * 排班日期
     */
    @Excel(name = "排班日期", width = 20, format = "yyyy-MM-dd", orderNum = "2")
    private String scheduleDate;

    /**
     * 时段（上午/下午/晚上）
     */
    @Excel(name = "时段", width = 15, orderNum = "3")
    private String timeSlotStr;

    /**
     * 号源数量（可选，如果不填则根据规则计算）
     */
    @Excel(name = "号源数量", width = 15, orderNum = "4")
    private Integer quota;
}

