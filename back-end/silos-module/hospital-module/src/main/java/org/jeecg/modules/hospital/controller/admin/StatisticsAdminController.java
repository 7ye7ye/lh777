package org.jeecg.modules.hospital.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.dto.*;
import org.jeecg.modules.hospital.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Web端-管理员统计控制器
 */
@RestController
@RequestMapping("/admin/statistics")
@Tag(name = "管理员-数据统计")
public class StatisticsAdminController {

    @Autowired
    private StatisticsService statisticsService;

    @Operation(summary = "获取门诊量统计")
    @GetMapping("/outpatient")
    public Result<List<OutpatientStatisticsDTO>> getOutpatientStatistics(
            @RequestParam(required = false) String periodType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Boolean compareHistory,
            @RequestParam(required = false) Integer comparePeriods) {
        
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setPeriodType(periodType);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setDeptId(deptId);
        query.setCompareHistory(compareHistory);
        query.setComparePeriods(comparePeriods);
        
        List<OutpatientStatisticsDTO> result = statisticsService.getOutpatientStatistics(query);
        return Result.ok(result);
    }

    @Operation(summary = "获取科室负荷统计")
    @GetMapping("/department-load")
    public Result<List<DepartmentLoadDTO>> getDepartmentLoadStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long doctorId) {
        
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setDeptId(deptId);
        query.setDoctorId(doctorId);
        
        List<DepartmentLoadDTO> result = statisticsService.getDepartmentLoadStatistics(query);
        return Result.ok(result);
    }

    @Operation(summary = "获取退号率统计")
    @GetMapping("/cancel-rate")
    public Result<List<CancelRateDTO>> getCancelRateStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long typeId) {
        
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setDeptId(deptId);
        query.setDoctorId(doctorId);
        query.setTypeId(typeId);
        
        List<CancelRateDTO> result = statisticsService.getCancelRateStatistics(query);
        return Result.ok(result);
    }

    @Operation(summary = "获取挂号量统计")
    @GetMapping("/registration")
    public Result<List<RegistrationStatisticsDTO>> getRegistrationStatistics(
            @RequestParam(required = false) String periodType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Boolean compareHistory,
            @RequestParam(required = false) Integer comparePeriods) {
        
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setPeriodType(periodType);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setTypeId(typeId);
        query.setCompareHistory(compareHistory);
        query.setComparePeriods(comparePeriods);
        
        List<RegistrationStatisticsDTO> result = statisticsService.getRegistrationStatistics(query);
        return Result.ok(result);
    }

    @Operation(summary = "获取转诊情况统计")
    @GetMapping("/referral")
    public Result<List<ReferralStatisticsDTO>> getReferralStatistics(
            @RequestParam(required = false) String periodType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long deptId) {
        
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setPeriodType(periodType);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setDeptId(deptId);
        
        List<ReferralStatisticsDTO> result = statisticsService.getReferralStatistics(query);
        return Result.ok(result);
    }

    @Operation(summary = "获取统计数据汇总")
    @GetMapping("/summary")
    public Result<Map<String, Object>> getStatisticsSummary(
            @RequestParam(required = false) String periodType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long typeId) {
        
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setPeriodType(periodType);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setDeptId(deptId);
        query.setDoctorId(doctorId);
        query.setTypeId(typeId);
        
        Map<String, Object> result = statisticsService.getStatisticsSummary(query);
        return Result.ok(result);
    }
}

