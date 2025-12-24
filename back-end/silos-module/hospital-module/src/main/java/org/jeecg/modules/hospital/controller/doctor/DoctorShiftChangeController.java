package org.jeecg.modules.hospital.controller.doctor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.service.DoctorShiftChangeRequestService;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/doctor/shift-change")
@Tag(name = "医生端-调班申请")
public class DoctorShiftChangeController {

    @Resource
    private DoctorShiftChangeRequestService adjustmentService;

    @Resource
    private DoctorScheduleService scheduleService;

    @Operation(summary = "医生提交调班申请")
    @PostMapping("/apply")
    public Result<Boolean> apply(@RequestBody ApplyRequest req) {
        if (req.getDoctorId() == null || req.getOriginalScheduleId() == null) {
            return Result.error("缺少必填字段：doctorId 或 originalScheduleId");
        }
        DoctorSchedule origin = scheduleService.getById(req.getOriginalScheduleId());
        if (origin == null) {
            return Result.error("原排班记录不存在");
        }

        // 限制：不允许医生申请当天调班（原排班为今天 or 目标日期为今天）
        LocalDate today = LocalDate.now();
        if (origin.getScheduleDate() != null && origin.getScheduleDate().isEqual(today)) {
            return Result.error("不允许医生申请当天调班");
        }

        // 限制：医生不能申请其他科室的排班，目标科室强制与原排班科室一致
        Long originDeptId = origin.getDeptId();
        if (originDeptId != null && req.getTargetDeptId() != null && !originDeptId.equals(req.getTargetDeptId())) {
            return Result.error("不能申请其他科室的排班");
        }

        LocalDate targetDate = null;
        if (req.getTargetDate() != null && !req.getTargetDate().isEmpty()) {
            targetDate = LocalDate.parse(req.getTargetDate());
        }
        if (targetDate != null && targetDate.isEqual(today)) {
            return Result.error("不允许医生申请当天调班");
        }
        Integer targetTimeSlot = req.getTargetTimeSlot();
        if (targetDate != null && targetTimeSlot != null) {
            DoctorSchedule conflict = scheduleService.lambdaQuery()
                    .eq(DoctorSchedule::getDoctorId, req.getDoctorId())
                    .eq(DoctorSchedule::getScheduleDate, targetDate)
                    .eq(DoctorSchedule::getTimeSlot, targetTimeSlot)
                    .eq(DoctorSchedule::getStatus, 1)
                    .ne(DoctorSchedule::getScheduleId, req.getOriginalScheduleId())
                    .one();
            if (conflict != null) {
                return Result.error("目的班次已有排班");
            }
        }

        DoctorShiftChangeRequest r = new DoctorShiftChangeRequest();
        r.setDoctorId(req.getDoctorId());
        r.setOriginalScheduleId(req.getOriginalScheduleId());
        if (targetDate != null) {
            r.setTargetDate(targetDate);
        }
        r.setTargetTimeSlot(req.getTargetTimeSlot());
        r.setTargetDeptId(originDeptId);
        r.setReason(req.getReason());
        r.setApplyTime(LocalDateTime.now());
        r.setStatus(1);

        boolean ok = adjustmentService.submitAdjustment(r);
        return ok ? Result.OK(true) : Result.error("提交失败");
    }

    @Operation(summary = "医生查询自己的调班申请列表")
    @GetMapping("/list")
    public Result<IPage<DoctorShiftChangeRequest>> list(
            @RequestParam Long doctorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Page<DoctorShiftChangeRequest> page = new Page<>(current, size);
        LambdaQueryWrapper<DoctorShiftChangeRequest> q = new LambdaQueryWrapper<>();
        q.eq(DoctorShiftChangeRequest::getDoctorId, doctorId);
        if (status != null) {
            q.eq(DoctorShiftChangeRequest::getStatus, status);
        }
        if (startDate != null) {
            q.ge(DoctorShiftChangeRequest::getApplyTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            q.le(DoctorShiftChangeRequest::getApplyTime, endDate.atTime(23,59,59));
        }
        q.orderByDesc(DoctorShiftChangeRequest::getApplyTime);
        IPage<DoctorShiftChangeRequest> result = adjustmentService.page(page, q);
        return Result.OK(result);
    }

    public static class ApplyRequest {
        private Long doctorId;
        private Long originalScheduleId;
        private String targetDate;
        private Integer targetTimeSlot;
        private Long targetDeptId;
        private String reason;

        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
        public Long getOriginalScheduleId() { return originalScheduleId; }
        public void setOriginalScheduleId(Long originalScheduleId) { this.originalScheduleId = originalScheduleId; }
        public String getTargetDate() { return targetDate; }
        public void setTargetDate(String targetDate) { this.targetDate = targetDate; }
        public Integer getTargetTimeSlot() { return targetTimeSlot; }
        public void setTargetTimeSlot(Integer targetTimeSlot) { this.targetTimeSlot = targetTimeSlot; }
        public Long getTargetDeptId() { return targetDeptId; }
        public void setTargetDeptId(Long targetDeptId) { this.targetDeptId = targetDeptId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}