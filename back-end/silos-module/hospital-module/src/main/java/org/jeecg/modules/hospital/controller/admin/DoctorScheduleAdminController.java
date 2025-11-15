package org.jeecg.modules.hospital.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.controller.request.DoctorScheduleCreateRequest;
import org.jeecg.modules.hospital.controller.request.DoctorScheduleUpdateRequest;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-医生排班管理控制器
 */
@RestController
@RequestMapping("/admin/schedule")
@Tag(name = "管理员-医生排班管理")
public class DoctorScheduleAdminController {

    @Resource
    private DoctorScheduleService scheduleService;

    @Operation(summary = "查询排班列表（可按医生/科室/日期筛选）")
    @GetMapping("/list")
    public Result<List<DoctorSchedule>> list(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<DoctorSchedule> list = scheduleService.list(doctorId, deptId, date);
        return Result.OK(list);
    }

    @Operation(summary = "根据排班ID获取详情")
    @GetMapping("/{scheduleId:\\d+}")
    public Result<DoctorSchedule> detail(@PathVariable Long scheduleId) {
        DoctorSchedule s = scheduleService.getById(scheduleId);
        return Result.OK(s);
    }

    @Operation(summary = "创建排班")
    @PostMapping("/create")
    public Result<DoctorSchedule> create(@RequestBody DoctorScheduleCreateRequest req) {
        DoctorSchedule s = new DoctorSchedule();
        s.setDoctorId(req.getDoctorId());
        s.setDeptId(req.getDeptId());
        s.setDate(LocalDate.parse(req.getDate()));
        s.setShift(req.getShift());
        s.setSlots(req.getSlots());
        s.setRemark(req.getRemark());
        DoctorSchedule created = scheduleService.create(s);
        return Result.OK(created);
    }

    @Operation(summary = "更新排班")
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody DoctorScheduleUpdateRequest req) {
        DoctorSchedule s = new DoctorSchedule();
        s.setScheduleId(req.getScheduleId());
        s.setDoctorId(req.getDoctorId());
        s.setDeptId(req.getDeptId());
        if (req.getDate() != null && !req.getDate().isEmpty()) {
            s.setDate(LocalDate.parse(req.getDate()));
        }
        s.setShift(req.getShift());
        s.setSlots(req.getSlots());
        s.setBookedSlots(req.getBookedSlots());
        s.setStatus(req.getStatus());
        s.setRemark(req.getRemark());
        boolean ok = scheduleService.update(s);
        return Result.OK(ok);
    }

    @Operation(summary = "删除排班")
    @DeleteMapping("/{scheduleId:\\d+}")
    public Result<Boolean> delete(@PathVariable Long scheduleId) {
        boolean ok = scheduleService.delete(scheduleId);
        return Result.OK(ok);
    }

    @Operation(summary = "按科室查询指定年月排班映射")
    @GetMapping("/month-by-dept")
    public Result<Map<String, List<Map<String, Integer>>>> monthByDept(
            @RequestParam Long deptId,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<DoctorSchedule> list = scheduleService.lambdaQuery()
                .eq(DoctorSchedule::getDeptId, deptId)
                .ge(DoctorSchedule::getScheduleDate, start)
                .le(DoctorSchedule::getScheduleDate, end)
                .eq(DoctorSchedule::getStatus, 1)
                .list();
        Map<String, List<Map<String, Integer>>> resp = new HashMap<>();
        for (DoctorSchedule ds : list) {
            String key = ds.getScheduleDate().toString();
            List<Map<String, Integer>> arr = resp.computeIfAbsent(key, k -> new ArrayList<>());
            Map<String, Integer> obj = new HashMap<>();
            obj.put("timeSlot", ds.getTimeSlot());
            arr.add(obj);
        }
        return Result.OK(resp);
    }

    @Operation(summary = "按医生查询指定年月排班映射")
    @GetMapping("/month-by-doctor")
    public Result<Map<String, List<Map<String, Integer>>>> monthByDoctor(
            @RequestParam Long doctorId,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<DoctorSchedule> list = scheduleService.lambdaQuery()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .ge(DoctorSchedule::getScheduleDate, start)
                .le(DoctorSchedule::getScheduleDate, end)
                .eq(DoctorSchedule::getStatus, 1)
                .list();
        Map<String, List<Map<String, Integer>>> resp = new HashMap<>();
        for (DoctorSchedule ds : list) {
            String key = ds.getScheduleDate().toString();
            List<Map<String, Integer>> arr = resp.computeIfAbsent(key, k -> new ArrayList<>());
            Map<String, Integer> obj = new HashMap<>();
            obj.put("timeSlot", ds.getTimeSlot());
            arr.add(obj);
        }
        return Result.OK(resp);
    }
}