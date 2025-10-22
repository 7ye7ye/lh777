package org.jeecg.modules.hospital.controller.doctor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.service.PatientService;
import org.jeecg.modules.hospital.vo.PatientBriefVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生端-患者按日期查询（匹配前端 /doctor/patients/by-date）
 */
@RestController
@RequestMapping("/doctor")
@Tag(name = "医生端-患者按日期查询")
public class DoctorPatientQueryController {

    @Resource
    private PatientService patientService;

    @Operation(summary = "按日期获取患者列表（医生端）")
    @GetMapping("/patients/by-date")
    public Result<List<PatientBriefVO>> getByDate(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<PatientBriefVO> list = patientService.list(doctorId, null, date, date, null);
        return Result.OK(list);
    }
}