package org.jeecg.modules.hospital.controller.applet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.RegistrationRecord;
import org.jeecg.modules.hospital.entity.RegistrationType;
import org.jeecg.modules.hospital.service.RegistrationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
