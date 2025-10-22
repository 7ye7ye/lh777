package org.jeecg.modules.hospital.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "创建医生排班请求体")
public class DoctorScheduleCreateRequest {

    @Schema(description = "医生ID", example = "1001")
    private Long doctorId;

    @Schema(description = "科室ID（二级科室）", example = "2002")
    private Long deptId;

    @Schema(description = "排班日期，格式：YYYY-MM-DD", example = "2025-10-20")
    private String date;

    @Schema(description = "班次（morning/afternoon/evening）", example = "morning")
    private String shift;

    @Schema(description = "可预约号源数量", example = "20")
    private Integer slots;

    @Schema(description = "备注", example = "专家门诊")
    private String remark;

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public Integer getSlots() { return slots; }
    public void setSlots(Integer slots) { this.slots = slots; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}