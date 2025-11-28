package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

// class DoctorSchedule
@TableName(value = "doctor_schedule")
public class DoctorSchedule {

    @TableId(value = "schedule_id", type = IdType.AUTO)
    private Long scheduleId;

    @TableField("doctor_id")
    private Long doctorId;

    @TableField("dept_id")
    private Long deptId;

    @TableField("type_id")
    private Integer typeId;

    @TableField("schedule_date")
    private LocalDate scheduleDate;

    @TableField("time_slot")
    private Integer timeSlot; // 1-上午, 2-下午, 3-晚上

    @TableField("used_quota")
    private Integer usedQuota;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("status")
    private Integer status;

    // 数据库尚无 room_number 字段，保留属性仅用于兼容展示，标记为非持久化
    @TableField(exist = false)
    private String roomNumber;

    // 兼容旧字段（非持久化别名）
    @TableField(exist = false)
    private LocalDate date;

    @TableField(exist = false)
    private String shift;

    @TableField(exist = false)
    private Integer slots;

    @TableField(exist = false)
    private Integer bookedSlots;

    @TableField(exist = false)
    private String remark;

    // getters/setters（含别名映射）
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

    public Integer getTimeSlot() { return timeSlot; }
    public void setTimeSlot(Integer timeSlot) { this.timeSlot = timeSlot; }

    public Integer getUsedQuota() { return usedQuota; }
    public void setUsedQuota(Integer usedQuota) { this.usedQuota = usedQuota; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    // 兼容旧逻辑的最大号源字段，未在数据库落库
    @TableField(exist = false)
    private Integer maxQuota;

    public Integer getMaxQuota() { return maxQuota; }
    public void setMaxQuota(Integer maxQuota) { this.maxQuota = maxQuota; }

    // 别名：date <-> scheduleDate
    public LocalDate getDate() { return scheduleDate; }
    public void setDate(LocalDate date) { this.scheduleDate = date; }

    // 别名：shift <-> timeSlot
    public String getShift() {
        if (timeSlot == null) return null;
        if (timeSlot == 1) return "morning";
        if (timeSlot == 2) return "afternoon";
        if (timeSlot == 3) return "evening";
        return null;
    }
    public void setShift(String shift) {
        this.shift = shift;
        if (shift == null) return;
        if ("morning".equalsIgnoreCase(shift)) this.timeSlot = 1;
        else if ("afternoon".equalsIgnoreCase(shift)) this.timeSlot = 2;
        else if ("evening".equalsIgnoreCase(shift)) this.timeSlot = 3;
    }

    // 非持久化展示/兼容字段
    public Integer getSlots() { return slots; }
    public void setSlots(Integer slots) { this.slots = slots; }
    public Integer getBookedSlots() { return bookedSlots; }
    public void setBookedSlots(Integer bookedSlots) { this.bookedSlots = bookedSlots; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}