package org.jeecg.modules.hospital.controller.doctor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest;
import org.jeecg.modules.hospital.service.DoctorShiftChangeRequestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor/shift-change")
@Tag(name = "医生端-申请调班")
public class DoctorShiftChangeController {

    @Resource
    private DoctorShiftChangeRequestService service;

    @io.swagger.v3.oas.annotations.Operation(summary = "提交排班调整申请")
    @org.springframework.web.bind.annotation.PostMapping("/apply")
    public org.jeecg.common.api.vo.Result<org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest> apply(
            @org.springframework.web.bind.annotation.RequestBody org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest req) {
        // 这里可从登录态解析 doctorId（如果已接入登录），当前版本接受 doctorId 作为入参
        org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest saved = service.apply(req);
        return org.jeecg.common.api.vo.Result.OK(saved);
    }

    @Operation(summary = "查询我的调班申请")
    @GetMapping("/list")
    public Result<List<DoctorShiftChangeRequest>> list(
            @RequestParam Long doctorId,
            @RequestParam(required = false) Integer status
    ) {
        List<DoctorShiftChangeRequest> list = service.listByDoctor(doctorId, status);
        return Result.OK(list);
    }
}