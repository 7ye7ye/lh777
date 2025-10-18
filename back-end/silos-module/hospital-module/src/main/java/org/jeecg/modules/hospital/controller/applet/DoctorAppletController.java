package org.jeecg.modules.hospital.controller.applet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端-医生查询控制器
 */
@RestController
@RequestMapping("/applet/doctor")
@Tag(name = "小程序-医生查询")
public class DoctorAppletController {

    @Autowired
    private DoctorService doctorService;

    @Operation(summary = "获取所有在职医生列表")
    @GetMapping("/list")
    public Result<List<Doctor>> getAllActiveDoctors() {
        List<Doctor> list = doctorService.getActiveDoctors();
        return Result.OK(list);
    }

    @Operation(summary = "根据科室ID获取医生列表")
    @GetMapping("/by-dept/{deptId}")
    public Result<List<Doctor>> getDoctorsByDeptId(@PathVariable Long deptId) {
        List<Doctor> doctors = doctorService.getDoctorsByDeptId(deptId);
        return Result.OK(doctors);
    }

    @Operation(summary = "获取医生详情")
    @GetMapping("/{doctorId}")
    public Result<Doctor> getDoctorDetail(@PathVariable Long doctorId) {
        Doctor doctor = doctorService.getById(doctorId);
        return Result.OK(doctor);
    }

    @Operation(summary = "搜索医生")
    @GetMapping("/search")
    public Result<List<Doctor>> searchDoctors(@RequestParam String keyword) {
        List<Doctor> doctors = doctorService.searchDoctorsByName(keyword);
        return Result.OK(doctors);
    }
}

