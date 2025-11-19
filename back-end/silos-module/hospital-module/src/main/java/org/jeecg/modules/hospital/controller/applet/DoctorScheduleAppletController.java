package org.jeecg.modules.hospital.controller.applet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/applet/schedule")
@Tag(name = "小程序-医生排班")
public class DoctorScheduleAppletController {

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    @Operation(summary = "按日期查询医生排班")
    @GetMapping("/by-date")
    public Result<List<DoctorSchedule>> getByDate(@RequestParam Long doctorId,
                                                  @RequestParam String date) {
        List<DoctorSchedule> list = doctorScheduleService.listByDoctorAndDate(doctorId, LocalDate.parse(date));
        return Result.OK(list);
    }

    @Operation(summary = "按日期范围查询医生排班（默认7天）")
    @GetMapping("/weekly")
    public Result<List<DoctorSchedule>> getWeekly(@RequestParam Long doctorId,
                                                  @RequestParam String startDate,
                                                  @RequestParam(required = false) String endDate,
                                                  @RequestParam(required = false, defaultValue = "7") Integer days) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = (endDate != null && !endDate.isEmpty())
                ? LocalDate.parse(endDate)
                : start.plusDays(days - 1);
        List<DoctorSchedule> list = doctorScheduleService.listByDoctorAndDateRange(doctorId, start, end);
        return Result.OK(list);
    }

    @Operation(summary = "新增排班")
    @PostMapping("/create")
    public Result<Boolean> create(@RequestBody DoctorSchedule schedule) {
        boolean ok = doctorScheduleService.save(schedule);
        return Result.OK(ok);
    }

    @Operation(summary = "更新已使用号源")
    @PutMapping("/quota")
    public Result<Boolean> updateQuota(@RequestBody QuotaUpdateRequest body) {
        boolean ok = doctorScheduleService.updateUsedQuota(body.getScheduleId(), body.getUsedQuota());
        return Result.OK(ok);
    }

    public static class QuotaUpdateRequest {
        private Long scheduleId;
        private Integer usedQuota;
        public Long getScheduleId() { return scheduleId; }
        public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
        public Integer getUsedQuota() { return usedQuota; }
        public void setUsedQuota(Integer usedQuota) { this.usedQuota = usedQuota; }
    }
}