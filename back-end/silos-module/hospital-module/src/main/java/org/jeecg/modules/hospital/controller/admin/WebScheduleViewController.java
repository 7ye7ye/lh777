package org.jeecg.modules.hospital.controller.admin;

// org.jeecg.modules.hospital.common.controller.WebScheduleViewController.java

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.dto.WebScheduleQueryDTO;
import org.jeecg.modules.hospital.service.IWebDoctorScheduleService;
import org.jeecg.modules.hospital.vo.WebTodayScheduleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

        import java.util.List;
        import java.time.LocalDate;
        import java.sql.Date;

/**
 * 排班查看控制器
 */
@Slf4j
@Tag(name = "排班查看")
@RestController
@RequestMapping("/hospital/scheduleView")
public class WebScheduleViewController {

    @Autowired
    private IWebDoctorScheduleService webDoctorScheduleService;

    /**
     * 查询今日排班列表
     */
    @Operation(summary = "查询今日排班列表")
    @PostMapping("/listByDate")
    public Result<List<WebTodayScheduleVO>> listSchedulesByDate(@RequestBody WebScheduleQueryDTO queryDTO) {
        try {
            log.info("查询今日排班，参数: {}", queryDTO);
            List<WebTodayScheduleVO> schedules = webDoctorScheduleService.listTodaySchedules(queryDTO);
            return Result.OK(schedules);
        } catch (Exception e) {
            log.error("查询今日排班失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询今日排班列表 - GET方式
     */
    @Operation(summary = "查询今日排班列表(GET)")
    @GetMapping("/listByDate")
    public Result<List<WebTodayScheduleVO>> listSchedulesByDateGet(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Integer timeSlot,
            @RequestParam(required = false) String keyword) {

        try {
            WebScheduleQueryDTO queryDTO = new WebScheduleQueryDTO();
            if (date != null && !date.isEmpty()) {
                LocalDate ld = LocalDate.parse(date);
                queryDTO.setDate(Date.valueOf(ld));
            }
            queryDTO.setDeptId(deptId);
            queryDTO.setDoctorId(doctorId);
            queryDTO.setTimeSlot(timeSlot);
            queryDTO.setKeyword(keyword);

            log.info("查询今日排班(GET)，参数: {}", queryDTO);
            List<WebTodayScheduleVO> schedules = webDoctorScheduleService.listTodaySchedules(queryDTO);
            return Result.OK(schedules);
        } catch (Exception e) {
            log.error("查询今日排班失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}