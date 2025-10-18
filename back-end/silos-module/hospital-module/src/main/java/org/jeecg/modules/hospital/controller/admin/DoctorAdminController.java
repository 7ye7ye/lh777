package org.jeecg.modules.hospital.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Web端-管理员医生管理控制器
 */
@RestController
@RequestMapping("/admin/doctor")
@Tag(name = "管理员-医生管理")
public class DoctorAdminController {

    @Autowired
    private DoctorService doctorService;

    @Operation(summary = "获取所有医生列表")
    @GetMapping("/list")
    public Result<List<Doctor>> getAllDoctors() {
        List<Doctor> list = doctorService.list();
        return Result.OK(list);
    }

    @Operation(summary = "创建医生")
    @PostMapping("/create")
    public Result<Boolean> createDoctor(@RequestBody Doctor doctor) {
        boolean result = doctorService.createDoctor(doctor);
        return Result.OK(result);
    }

    @Operation(summary = "更新医生信息")
    @PutMapping("/update")
    public Result<Boolean> updateDoctor(@RequestBody Doctor doctor) {
        boolean result = doctorService.updateDoctor(doctor);
        return Result.OK(result);
    }

    @Operation(summary = "删除医生")
    @DeleteMapping("/{doctorId}")
    public Result<Boolean> deleteDoctor(@PathVariable Long doctorId) {
        boolean result = doctorService.deleteDoctor(doctorId);
        return Result.OK(result);
    }

    @Operation(summary = "更新医生出诊状态")
    @PutMapping("/status/{doctorId}")
    public Result<Boolean> updateDoctorStatus(@PathVariable Long doctorId, @RequestParam Integer isActive) {
        boolean result = doctorService.updateDoctorStatus(doctorId, isActive);
        return Result.OK(result);
    }
}

