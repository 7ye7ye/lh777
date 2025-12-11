package org.jeecg.modules.hospital.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "自动生成排班请求体")
public class GenerateSchedulesRequest {

    @Schema(description = "科室ID列表", example = "[101, 102]")
    private List<Long> deptIds;

    @Schema(description = "排班数量", example = "10")
    private Integer scheduleCount;

    @Schema(description = "时段列表（1-上午，2-下午，3-晚上）", example = "[1, 2]")
    private List<Integer> timeSlots;

    @Schema(description = "最大号源数", example = "50")
    private Integer maxQuota;

    @Schema(description = "起始日期，格式：YYYY-MM-DD", example = "2025-12-01")
    private String startDate;

    public List<Long> getDeptIds() { return deptIds; }
    public void setDeptIds(List<Long> deptIds) { this.deptIds = deptIds; }

    public Integer getScheduleCount() { return scheduleCount; }
    public void setScheduleCount(Integer scheduleCount) { this.scheduleCount = scheduleCount; }

    public List<Integer> getTimeSlots() { return timeSlots; }
    public void setTimeSlots(List<Integer> timeSlots) { this.timeSlots = timeSlots; }

    public Integer getMaxQuota() { return maxQuota; }
    public void setMaxQuota(Integer maxQuota) { this.maxQuota = maxQuota; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
}

