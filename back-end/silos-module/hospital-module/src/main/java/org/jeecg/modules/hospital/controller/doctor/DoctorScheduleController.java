package org.jeecg.modules.hospital.controller.doctor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result; // 使用 Jeecg 统一返回类
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.HosUserService;
import org.jeecg.modules.hospital.service.RegistrationService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.entity.Doctor;
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
@Slf4j
public class DoctorScheduleController {

    @Resource
    private DoctorScheduleService scheduleService;
    @Resource
    private DoctorService doctorService;

    @Resource
    private HosUserService hosUserService;

    @Resource
    private RegistrationService registrationService;

    // 新增：用于实时统计挂号人数
    @Resource
    private org.jeecg.modules.hospital.mapper.RegistrationRecordMapper registrationRecordMapper;

    // ------------------- API 接口 -------------------



    @Operation(summary = "获取今日排班")
    @GetMapping("/schedule/today")
    public Result<List<ScheduleDTO>> getTodaySchedule(
            HttpServletRequest request,
            @RequestParam(required = false) Long doctorId // 新增：允许联调时显式传入
    ) {
        Long resolvedDoctorId = (doctorId != null ? doctorId : resolveCurrentDoctorId(request));
        if (resolvedDoctorId == null) {
            return Result.error("未登录或未绑定医生信息");
        }
        LocalDate today = LocalDate.now();
        // 更明确的单日查询
        List<DoctorSchedule> list = scheduleService.listByDoctorAndDate(resolvedDoctorId, today);
        try { log.info("[DoctorSchedule] today doctorId={}, size={}", resolvedDoctorId, (list == null ? 0 : list.size())); } catch (Exception ignored) {}
        return Result.ok(toDTOList(list));
    }

    /**
     * 获取未来N天排班（起始日期 + 天数）
     */
    @Operation(summary = "获取未来N天排班")
    @GetMapping("/schedules")
    public Result<List<ScheduleDTO>> getSchedules(
            HttpServletRequest request,
            @RequestParam String startDate,
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(required = false) Long doctorId // 新增：允许联调时显式传入
    ) {
        Long resolvedDoctorId = (doctorId != null ? doctorId : resolveCurrentDoctorId(request));
        if (resolvedDoctorId == null) {
            return Result.error("未登录或未绑定医生信息");
        }
        try {
            LocalDate start = LocalDate.parse(startDate, java.time.format.DateTimeFormatter.ISO_DATE);
            LocalDate end = start.plusDays(Math.max(1, days) - 1);
            // 使用区间查询，避免内存过滤
            List<DoctorSchedule> list = scheduleService.listByDoctorAndDateRange(resolvedDoctorId, start, end);
            try { log.info("[DoctorSchedule] range doctorId={}, start={}, end={}, size={}", resolvedDoctorId, start, end, (list == null ? 0 : list.size())); } catch (Exception ignored) {}
            return Result.ok(toDTOList(list));
        } catch (Exception e) {
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

        LocalDate newDate = (req.getNewDate() != null && !req.getNewDate().isEmpty())
                ? LocalDate.parse(req.getNewDate(), DateTimeFormatter.ISO_DATE)
                : null;
        Integer newSlot = (req.getNewTimeRange() != null) ? mapTimeRangeToSlot(req.getNewTimeRange()) : null;

        // 限制：目的排班日期和时段不允许与原排班完全相同
        if (newDate != null
                && newSlot != null
                && origin.getScheduleDate() != null
                && origin.getTimeSlot() != null
                && newDate.isEqual(origin.getScheduleDate())
                && newSlot.equals(origin.getTimeSlot())) {
            return Result.error("与原排班相同");
        }

        // 更新逻辑
        if (newDate != null) {
            origin.setScheduleDate(newDate);
        }
        if (newSlot != null) {
            origin.setTimeSlot(newSlot);
        }

        // 可扩展独立申请记录表，此处暂直接修改原记录
        boolean ok = scheduleService.updateById(origin); // 建议使用 updateById

        if (ok) {
            return Result.ok("调班申请成功！");
        } else {
            return Result.error("调班申请失败，请联系管理员！");
        }
    }

    @Operation(summary = "初始化/补充指定时间段的排班数据（测试/演示用）")
    @PostMapping("/schedule/seed")
    public Result<Integer> seedSchedules(
            @RequestParam Long doctorId,
            @RequestParam Long deptId,
            @RequestParam String startDate,
            @RequestParam(defaultValue = "1") Integer days,
            @RequestParam(defaultValue = "1") Integer typeId
    ) {
        int created = 0;
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            LocalDate end = start.plusDays(Math.max(1, days) - 1);
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                for (int slot = 1; slot <= 3; slot++) {
                    boolean exists = scheduleService.lambdaQuery()
                            .eq(DoctorSchedule::getDoctorId, doctorId)
                            .eq(DoctorSchedule::getDeptId, deptId)
                            .eq(DoctorSchedule::getScheduleDate, d)
                            .eq(DoctorSchedule::getTimeSlot, slot)
                            .one() != null;
                    if (exists) continue;

                    DoctorSchedule s = new DoctorSchedule();
                    s.setDoctorId(doctorId);
                    s.setDeptId(deptId);
                    s.setTypeId(typeId);
                    s.setScheduleDate(d);
                    s.setTimeSlot(slot);
                    s.setUsedQuota(0);
                    s.setStatus(1);
                    s.setRoomNumber(mapSlotToRoomNo(slot));
                    s.setMaxQuota(defaultTotalSlots(slot));
                    s.setCreateTime(java.time.LocalDateTime.now());
                    s.setUpdateTime(java.time.LocalDateTime.now());
                    boolean ok = scheduleService.save(s);
                    if (ok) created++;
                }
            }
            return Result.OK(created);
        } catch (Exception e) {
            return Result.error("排班种子初始化失败：" + e.getMessage());
        }
    }

    // ------------------- 辅助方法 -------------------

    // DoctorScheduleController.toDTOList 辅助方法
    // 优先使用数据库doctor_schedule表中的实际数据
    private List<ScheduleDTO> toDTOList(List<DoctorSchedule> list) {
        List<ScheduleDTO> res = new ArrayList<>();
        for (DoctorSchedule s : list) {
            ScheduleDTO dto = new ScheduleDTO();
            dto.setId(s.getScheduleId() == null ? 0L : s.getScheduleId());
            dto.setDate(s.getScheduleDate() == null ? "" : s.getScheduleDate().toString());
            dto.setTimeRange(mapSlotToTimeRange(s.getTimeSlot()));

            // 优先使用数据库doctor_schedule表中的room_number字段
            // 如果数据库值为空，才使用默认值
            String roomNo = s.getRoomNumber() != null && !s.getRoomNumber().isEmpty()
                ? s.getRoomNumber()
                : mapSlotToRoomNo(s.getTimeSlot());
            dto.setRoomNo(roomNo);
            log.debug("[toDTOList] scheduleId={}, roomNumber from DB={}, final roomNo={}",
                s.getScheduleId(), s.getRoomNumber(), roomNo);

            // 优先使用数据库doctor_schedule表中的max_quota字段
            // 如果数据库值为空，才使用默认值
            int total = (s.getMaxQuota() != null && s.getMaxQuota() > 0)
                ? s.getMaxQuota()
                : defaultTotalSlots(s.getTimeSlot());
            dto.setTotalSlots(total);
            log.debug("[toDTOList] scheduleId={}, maxQuota from DB={}, final totalSlots={}",
                s.getScheduleId(), s.getMaxQuota(), total);

            // 优先使用数据库doctor_schedule表中的used_quota字段
            // 如果数据库值为空，才尝试从registration_record表统计
            int booked;
            if (s.getUsedQuota() != null) {
                booked = s.getUsedQuota();
                log.debug("[toDTOList] scheduleId={}, usedQuota from DB={}", s.getScheduleId(), booked);
            } else {
                try {
                    booked = (s.getScheduleId() == null)
                        ? 0
                        : registrationRecordMapper.countActiveByScheduleId(s.getScheduleId());
                    log.debug("[toDTOList] scheduleId={}, usedQuota from registration_record={}", s.getScheduleId(), booked);
                } catch (Exception ex) {
                    booked = 0;
                    log.warn("[toDTOList] scheduleId={}, failed to count from registration_record, using 0", s.getScheduleId());
                }
            }

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

    // ------------------- 当前登录医生解析 -------------------
    private Long resolveCurrentDoctorId(HttpServletRequest httpRequest) {
        HosUser current = null;
        String token = httpRequest.getHeader(CommonConstant.X_ACCESS_TOKEN);
        if (token != null && !token.isEmpty()) {
            try {
                String account = JwtUtil.getUsername(token);
                current = hosUserService.lambdaQuery()
                        .eq(HosUser::getUserAccount, account)
                        .one();
            } catch (Exception ignored) {}
        }
        if (current == null) {
            Object userObj = httpRequest.getSession().getAttribute(org.jeecg.modules.hospital.contant.UserContant.USER_LOGIN_STATE);
            if (userObj instanceof HosUser) {
                current = (HosUser) userObj;
            }
        }
        // 优化：不再严格依赖 userType == 2，只要有医生绑定记录即可
        if (current == null) {
            return null;
        }
        Doctor doctor = doctorService.lambdaQuery().eq(Doctor::getUserId, current.getUserId()).one();
        return doctor != null ? doctor.getDoctorId() : null;
    }

    // ------------------- DTO/Request 类 -------------------
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