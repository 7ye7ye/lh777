package org.jeecg.modules.hospital.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "批量创建排班请求体")
public class BatchCreateSchedulesRequest {

    @Schema(description = "排班列表")
    private List<ScheduleItem> schedules;

    public List<ScheduleItem> getSchedules() { return schedules; }
    public void setSchedules(List<ScheduleItem> schedules) { this.schedules = schedules; }

    @Schema(description = "排班项")
    public static class ScheduleItem {
        @Schema(description = "医生ID")
        private Long doctorId;

        @Schema(description = "科室ID")
        private Long deptId;

        @Schema(description = "排班日期，格式：YYYY-MM-DD")
        private String scheduleDate;

        @Schema(description = "时段（1-上午，2-下午，3-晚上）")
        private Integer timeSlot;

        @Schema(description = "最大号源数")
        private Integer maxQuota;

        @Schema(description = "诊室号")
        private String roomNumber;

        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }

        public String getScheduleDate() { return scheduleDate; }
        public void setScheduleDate(String scheduleDate) { this.scheduleDate = scheduleDate; }

        public Integer getTimeSlot() { return timeSlot; }
        public void setTimeSlot(Integer timeSlot) { this.timeSlot = timeSlot; }

        public Integer getMaxQuota() { return maxQuota; }
        public void setMaxQuota(Integer maxQuota) { this.maxQuota = maxQuota; }

        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    }
}

