package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("doctor_schedule_adjustment")
public class DoctorShiftChangeRequest {

    @TableId(value = "adjustment_id", type = IdType.AUTO)
    private Long adjustmentId;

    @TableField("doctor_id")
    private Long doctorId;

    @TableField("original_schedule_id")
    private Long originalScheduleId;

    @TableField("target_date")
    private LocalDate targetDate;

    @TableField("target_time_slot")
    private Integer targetTimeSlot;

    @TableField("target_dept_id")
    private Long targetDeptId;

    @TableField("reason")
    private String reason;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("status")
    private Integer status; // 1-待审批,2-已通过,3-已驳回,4-已撤销

    @TableField("admin_id")
    private Long adminId;

    @TableField("approve_time")
    private LocalDateTime approveTime;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("new_schedule_id")
    private Long newScheduleId;

    // getters & setters
    public Long getAdjustmentId() { return adjustmentId; }
    public void setAdjustmentId(Long adjustmentId) { this.adjustmentId = adjustmentId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getOriginalScheduleId() { return originalScheduleId; }
    public void setOriginalScheduleId(Long originalScheduleId) { this.originalScheduleId = originalScheduleId; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public Integer getTargetTimeSlot() { return targetTimeSlot; }
    public void setTargetTimeSlot(Integer targetTimeSlot) { this.targetTimeSlot = targetTimeSlot; }

    public Long getTargetDeptId() { return targetDeptId; }
    public void setTargetDeptId(Long targetDeptId) { this.targetDeptId = targetDeptId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getApplyTime() { return applyTime; }
    public void setApplyTime(LocalDateTime applyTime) { this.applyTime = applyTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public LocalDateTime getApproveTime() { return approveTime; }
    public void setApproveTime(LocalDateTime approveTime) { this.approveTime = approveTime; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public Long getNewScheduleId() { return newScheduleId; }
    public void setNewScheduleId(Long newScheduleId) { this.newScheduleId = newScheduleId; }
}