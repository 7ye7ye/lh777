package org.jeecg.modules.hospital.controller.doctor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.service.DoctorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/doctor")
@Tag(name = "医生-公开接口")
public class DoctorController {

    @Resource
    private DoctorService doctorService;

    @Operation(summary = "获取所有医生列表")
    @GetMapping("/list")
    public Result<List<Doctor>> list() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return Result.OK(doctors);
    }

    @Operation(summary = "按姓名或专长搜索医生")
    @GetMapping("/search")
    public Result<List<Doctor>> search(@RequestParam String keyword,
                                       @RequestParam(required = false) Long departmentId) {
        QueryWrapper<Doctor> q = new QueryWrapper<>();
        q.and(w -> w.like("doctor_name", keyword).or().like("specialty", keyword))
         .eq("is_active", 1)
         .orderByDesc("doctor_id");
        if (departmentId != null) {
            q.eq("dept_id", departmentId);
        }
        List<Doctor> doctors = doctorService.list(q);
        return Result.OK(doctors);
    }

    @Operation(summary = "按科室查询医生")
    @GetMapping("/list/by-department")
    public Result<List<Doctor>> listByDepartment(@RequestParam("departmentId") Long departmentId) {
        List<Doctor> doctors = doctorService.getDoctorsByDeptId(departmentId);
        return Result.OK(doctors);
    }

    @Operation(summary = "医生详情")
    @GetMapping("/detail/{doctorId}")
    public Result<Doctor> detail(@PathVariable Long doctorId) {
        Doctor doctor = doctorService.getDoctorDetail(doctorId);
        return Result.OK(doctor);
    }
}