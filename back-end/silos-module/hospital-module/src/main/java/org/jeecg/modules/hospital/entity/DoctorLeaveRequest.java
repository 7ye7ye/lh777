package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("doctor_leave_request")
public class DoctorLeaveRequest {

    @TableId(value = "leave_id", type = IdType.AUTO)
    private Long leaveId;

    @TableField("doctor_id")
    private Long doctorId;

    @TableField("doctor_name")
    private String doctorName;

    @TableField("dept_id")
    private Long deptId;

    @TableField("dept_name")
    private String deptName;

    @TableField("leave_type")
    private String leaveType;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

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

    // ------------- 扩展展示字段（非持久化） -------------
    @TableField(exist = false)
    private String title;

    // getters & setters
    public Long getLeaveId() { return leaveId; }
    public void setLeaveId(Long leaveId) { this.leaveId = leaveId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}

