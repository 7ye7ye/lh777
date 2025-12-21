package org.jeecg.modules.hospital.controller.applet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.entity.RegistrationRecord;
import org.jeecg.modules.hospital.entity.RegistrationType;
import org.jeecg.modules.hospital.entity.WaitingQueue;
import org.jeecg.modules.hospital.service.RegistrationService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序端-挂号控制器
 */
@RestController
@RequestMapping("/applet/registration")
@Tag(name = "小程序-挂号相关接口")
public class RegistrationAppletController {

    @Resource
    private RegistrationService registrationService;

    @Operation(summary = "获取所有挂号类型")
    @GetMapping("/types")
    public Result<List<RegistrationType>> getRegistrationTypes() {
        List<RegistrationType> types = registrationService.getAllRegistrationTypes();
        return Result.OK(types);
    }

    @Operation(summary = "根据医生ID获取排班信息")
    @GetMapping("/schedules")
    public Result<?> getSchedules(
            @RequestParam Long doctorId,
            @RequestParam String startDate,
            @RequestParam(defaultValue = "7") Integer days
    ) {
        return Result.OK(registrationService.getDoctorSchedules(doctorId, startDate, days));
    }

    @Operation(summary = "创建挂号预约记录")
    @PostMapping("/create")
    public Result<String> createRegistration(@RequestBody RegistrationRecord record,
                                             @RequestParam Long patientId,
                                             @RequestParam(defaultValue = "false") boolean joinWaitingQueue) {
        // 调用 Service
        Result<String> res = registrationService.createRegistration(record, patientId, joinWaitingQueue);
        return res;
    }


    @Operation(summary = "根据患者ID获取挂号记录")
    @GetMapping("/records")
    public Result<List<RegistrationRecord>> getRegistrationRecords(@RequestParam Long patientId) {
        List<RegistrationRecord> records = registrationService.getRecordsByPatientId(patientId);
        return Result.OK(records);
    }

    @Operation(summary = "检查患者是否已对该排班挂号（防止重复挂号）")
    @GetMapping("/checkDuplicateBySchedule")
    public Result<Boolean> checkDuplicateBySchedule(@RequestParam Long patientId,
                                                    @RequestParam Long scheduleId) {
        boolean isDuplicate = registrationService.checkDuplicateBySchedule(patientId, scheduleId);
        if (isDuplicate) {
            // 消息在前，数据在后
            return Result.OK("您已挂过该排班的号", true);
        } else {
            return Result.OK("未发现重复挂号", false);
        }
    }

    @Operation(summary = "将患者加入候补队列")
    @PostMapping("/addWaitingQueue")
    public Result<String> addWaitingQueue(@RequestBody WaitingQueue queue) {
        try {
            // 调用服务层直接返回 Result<String>
            return registrationService.addWaitingQueue(queue);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("加入候补队列失败：" + e.getMessage());
        }
    }

    @Operation(summary = "取消挂号记录")
    @PostMapping("/cancel")
    public Result<String> cancelRegistration(
            @RequestParam Long recordId,
            @RequestParam(required = false, defaultValue = "患者主动取消") String cancelReason
    ) {
        try {
            boolean ok = registrationService.cancelRegistration(recordId, cancelReason);
            if (ok) {
                return Result.OK("取消成功");
            } else {
                return Result.error("取消失败：记录不存在或已被取消");
            }
        } catch (Exception e) {
            return Result.error("取消挂号异常：" + e.getMessage());
        }
    }

    @Operation(summary = "根据排班ID获取科室ID")
    @GetMapping("/schedule/department")
    public Result<?> getDepartmentIdBySchedule(@RequestParam Long scheduleId) {
        try {
            Long departmentId = registrationService.getDepartmentIdBySchedule(scheduleId);
            return Result.OK(departmentId);
        } catch (Exception e) {
            return Result.error("获取科室ID失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据排班ID获取排班详情")
    @GetMapping("/schedule/detail")
    public Result<?> getScheduleDetailById(@RequestParam Long scheduleId) {
        try {
            DoctorSchedule schedule = registrationService.getScheduleDetailById(scheduleId);

            if (schedule == null) {
                return Result.error("排班不存在");
            }

            String timeSlotText;
            switch (schedule.getTimeSlot()) {
                case 1: timeSlotText = "上午"; break;
                case 2: timeSlotText = "下午"; break;
                case 3: timeSlotText = "晚上"; break;
                default: timeSlotText = "未知";
            }

            Map<String, Object> result = new HashMap<>();
            result.put("scheduleId", schedule.getScheduleId());
            result.put("doctorId", schedule.getDoctorId());
            result.put("deptId", schedule.getDeptId());
            result.put("typeId", schedule.getTypeId());
            result.put("scheduleDate", schedule.getScheduleDate());
            result.put("timeSlot", schedule.getTimeSlot());
            result.put("timeSlotText", timeSlotText);
            result.put("roomNumber", schedule.getRoomNumber());

            return Result.OK(result);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取排班详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据患者ID获取患者详情")
    @GetMapping("/patient/detail")
    public Result<?> getPatientDetail(@RequestParam Long patientId) {
        try {
            // 假设你的 RegistrationService 中已经有对应方法
            Object patient = registrationService.getPatientDetailById(patientId);

            if (patient == null) {
                return Result.error("未找到该患者信息");
            }

            return Result.OK(patient);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取患者详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据患者ID获取患者类型")
    @GetMapping("/patient/type")
    public Result<Integer> getPatientType(@RequestParam Long patientId) {
        try {
            Integer patientType = registrationService.getPatientTypeById(patientId);
            if (patientType == null) {
                // 返回统一的 Result 对象
                return Result.error("未找到患者类型");
            }
            // 包装成 Result 对象返回
            return Result.OK(patientType);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回统一的 Result 对象
            return Result.error("获取患者类型失败：" + e.getMessage());
        }
    }



}
