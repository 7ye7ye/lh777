package org.jeecg.modules.hospital.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "更新医生排班请求体")
public class DoctorScheduleUpdateRequest {

    @Schema(description = "排班ID", example = "1")
    private Long scheduleId;

    @Schema(description = "医生ID", example = "1001")
    private Long doctorId;

    @Schema(description = "科室ID（二级科室）", example = "2002")
    private Long deptId;

    @Schema(description = "排班日期，格式：YYYY-MM-DD", example = "2025-10-20")
    private String date;

    @Schema(description = "班次（morning/afternoon/evening）", example = "afternoon")
    private String shift;

    @Schema(description = "可预约号源数量", example = "30")
    private Integer slots;

    @Schema(description = "已预约数量", example = "5")
    private Integer bookedSlots;

    @Schema(description = "状态（1-启用；0-停用）", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "调班")
    private String remark;

    @Schema(description = "诊室号", example = "A101")
    private String roomNumber;

    @Schema(description = "最大号源数量", example = "50")
    private Integer maxQuota;

    @Schema(description = "时段（1-上午，2-下午，3-晚上）", example = "1")
    private Integer timeSlot;

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
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
    public Integer getBookedSlots() { return bookedSlots; }
    public void setBookedSlots(Integer bookedSlots) { this.bookedSlots = bookedSlots; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getMaxQuota() { return maxQuota; }
    public void setMaxQuota(Integer maxQuota) { this.maxQuota = maxQuota; }

    public Integer getTimeSlot() { return timeSlot; }
    public void setTimeSlot(Integer timeSlot) { this.timeSlot = timeSlot; }
}