package org.jeecg.modules.hospital.controller.doctor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.service.PatientService;
import org.jeecg.modules.hospital.vo.PatientBriefVO;
import org.jeecg.modules.hospital.vo.PatientDetailVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/doctor/patient")
@Tag(name = "医生端-患者管理")
public class PatientDoctorController {

    @Resource
    private PatientService patientService;

    @Operation(summary = "患者列表（医生端）")
    @GetMapping("/list")
    public Result<List<PatientBriefVO>> list(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer status
    ) {
        List<PatientBriefVO> list = patientService.list(doctorId, keyword, startDate, endDate, status);
        return Result.OK(list);
    }

    @Operation(summary = "患者详情（基础信息 + 就诊记录）")
    @GetMapping("/{patientId}")
    public Result<PatientDetailVO> detail(@PathVariable Long patientId) {
        PatientDetailVO vo = patientService.detail(patientId);
        return Result.OK(vo);
    }

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