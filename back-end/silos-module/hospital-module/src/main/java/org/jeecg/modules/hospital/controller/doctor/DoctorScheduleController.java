package org.jeecg.modules.hospital.controller.doctor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result; // 使用 Jeecg 统一返回类
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 医生排班管理 Controller
 * 对应请求路径: /doctor
 */
@RestController
@RequestMapping("/doctor")
@Tag(name = "医生端-排班")
// 建议类名遵循 Jeecg 规范，改为 ScheduleController
public class DoctorScheduleController {

    @Resource
    private DoctorScheduleService scheduleService;

    // ------------------- API 接口 -------------------

    @Operation(summary = "获取今日排班")
    @GetMapping("/schedule/today")
    public Result<List<ScheduleDTO>> getTodaySchedule(@RequestParam Long doctorId) {
        LocalDate today = LocalDate.now();
        List<DoctorSchedule> list = scheduleService.list(doctorId, null, today);
        return Result.ok(toDTOList(list));
    }

    /**
     * 【重点修正】这个方法对应请求路径 /doctor/schedules
     * 如果前端未传 doctorId 或 startDate，Jeecg 的 Result.error 会处理
     */
    @Operation(summary = "获取未来N天排班")
    @GetMapping("/schedules")
    public Result<List<ScheduleDTO>> getSchedules(
            @RequestParam Long doctorId,
            @RequestParam String startDate,
            @RequestParam(defaultValue = "7") Integer days
    ) {
        try {
            // 确保日期格式正确
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            LocalDate end = start.plusDays(Math.max(1, days) - 1);

            // 注意：你原始的过滤逻辑是在 Java 内存中进行的（list(doctorId, null, null)）。
            // 生产环境中，强烈建议将日期过滤逻辑放到 service/mapper 层，使用 SQL 查询来提高性能。
            List<DoctorSchedule> all = scheduleService.list(doctorId, null, null);

            List<DoctorSchedule> filtered = new ArrayList<>();
            for (DoctorSchedule s : all) {
                if (s.getScheduleDate() != null && !s.getScheduleDate().isBefore(start) && !s.getScheduleDate().isAfter(end)) {
                    filtered.add(s);
                }
            }
            return Result.ok(toDTOList(filtered));

        } catch (Exception e) {
            // 捕获日期解析错误或其他异常
            return Result.error("查询排班失败：" + e.getMessage());
        }
    }

    @Operation(summary = "申请调班")
    @PostMapping("/schedule/shift/apply")
    public Result<?> applyShiftChange(@RequestBody ShiftApplyRequest req) {
        DoctorSchedule origin = scheduleService.getById(req.getScheduleId());
        if (origin == null) {
            return Result.error("原排班记录不存在！");
        }

        // 更新逻辑
        if (req.getNewDate() != null && !req.getNewDate().isEmpty()) {
            origin.setScheduleDate(LocalDate.parse(req.getNewDate(), DateTimeFormatter.ISO_DATE));
        }
        if (req.getNewTimeRange() != null) {
            origin.setTimeSlot(mapTimeRangeToSlot(req.getNewTimeRange()));
        }

        // 可扩展独立申请记录表，此处暂直接修改原记录
        boolean ok = scheduleService.updateById(origin); // 建议使用 updateById

        if (ok) {
            return Result.ok("调班申请成功！");
        } else {
            return Result.error("调班申请失败，请联系管理员！");
        }
    }

    // ------------------- 辅助方法 -------------------

    private List<ScheduleDTO> toDTOList(List<DoctorSchedule> list) {
        List<ScheduleDTO> res = new ArrayList<>();
        // ... (保持不变)
        for (DoctorSchedule s : list) {
            ScheduleDTO dto = new ScheduleDTO();
            dto.setId(s.getScheduleId() == null ? 0L : s.getScheduleId());
            dto.setDate(s.getScheduleDate() == null ? "" : s.getScheduleDate().toString());
            dto.setTimeRange(mapSlotToTimeRange(s.getTimeSlot()));
            dto.setRoomNo(mapSlotToRoomNo(s.getTimeSlot()));
            int total = defaultTotalSlots(s.getTimeSlot());
            int booked = s.getUsedQuota() == null ? 0 : s.getUsedQuota();
            dto.setTotalSlots(total);
            dto.setBookedCount(booked);
            res.add(dto);
        }
        return res;
    }

    private String mapSlotToTimeRange(Integer slot) {
        if (slot == null) return "08:00-12:00";
        if (slot == 1) return "08:00-12:00";
        if (slot == 2) return "14:00-17:00";
        if (slot == 3) return "18:00-20:00";
        return "08:00-12:00";
    }

    private String mapSlotToRoomNo(Integer slot) {
        if (slot == null || slot == 1) return "A-101";
        if (slot == 2) return "A-102";
        if (slot == 3) return "A-103";
        return "A-101";
    }

    private int defaultTotalSlots(Integer slot) {
        if (slot == null || slot == 1) return 20;
        if (slot == 2) return 15;
        if (slot == 3) return 10;
        return 20;
    }

    private Integer mapTimeRangeToSlot(String range) {
        if ("08:00-12:00".equals(range)) return 1;
        if ("14:00-17:00".equals(range)) return 2;
        if ("18:00-20:00".equals(range)) return 3;
        return 1;
    }

    // ------------------- DTO/Request 类 -------------------

    // 注意：在 Jeecg-Boot 环境中，你可能需要将这些内部类定义为公共的独立类，
    // 或者在外部使用 @Data 注解（Lombok）简化代码。

    public static class ScheduleDTO {
        private Long id;
        private String date;
        private String timeRange;
        private String roomNo;
        private Integer totalSlots;
        private Integer bookedCount;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getTimeRange() { return timeRange; }
        public void setTimeRange(String timeRange) { this.timeRange = timeRange; }
        public String getRoomNo() { return roomNo; }
        public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
        public Integer getTotalSlots() { return totalSlots; }
        public void setTotalSlots(Integer totalSlots) { this.totalSlots = totalSlots; }
        public Integer getBookedCount() { return bookedCount; }
        public void setBookedCount(Integer bookedCount) { this.bookedCount = bookedCount; }
    }

    public static class ShiftApplyRequest {
        private Long scheduleId;
        private String newDate;
        private String newTimeRange;
        private String reason;
        public Long getScheduleId() { return scheduleId; }
        public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
        public String getNewDate() { return newDate; }
        public void setNewDate(String newDate) { this.newDate = newDate; }
        public String getNewTimeRange() { return newTimeRange; }
        public void setNewTimeRange(String newTimeRange) { this.newTimeRange = newTimeRange; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}