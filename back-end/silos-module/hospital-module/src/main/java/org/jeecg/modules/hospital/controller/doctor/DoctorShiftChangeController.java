package org.jeecg.modules.hospital.controller.doctor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.HosUserService;
import org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest;
import org.jeecg.modules.hospital.service.DoctorShiftChangeRequestService;

import java.util.List;

@RestController
@RequestMapping("/doctor/shift-change")
@Tag(name = "医生端-申请调班")
public class DoctorShiftChangeController {

    @Resource
    private DoctorShiftChangeRequestService service;

    @Resource
    private DoctorService doctorService;

    @Resource
    private HosUserService hosUserService;

    @io.swagger.v3.oas.annotations.Operation(summary = "提交排班调整申请")
    @org.springframework.web.bind.annotation.PostMapping("/apply")
    public org.jeecg.common.api.vo.Result<java.lang.Boolean> apply(
            HttpServletRequest request,
            @org.springframework.web.bind.annotation.RequestBody org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest req) {
        Long doctorId = resolveCurrentDoctorId(request);
        if (doctorId == null) {
            return org.jeecg.common.api.vo.Result.error("未登录或未绑定医生信息");
        }
        req.setDoctorId(doctorId);
        boolean ok = service.submitAdjustment(req);
        return ok ? org.jeecg.common.api.vo.Result.OK(true) : org.jeecg.common.api.vo.Result.error("提交失败");
    }

    @Operation(summary = "查询我的调班申请")
    @GetMapping("/list")
    public Result<List<DoctorShiftChangeRequest>> list(
            HttpServletRequest request,
            @RequestParam(required = false) Integer status
    ) {
        Long doctorId = resolveCurrentDoctorId(request);
        if (doctorId == null) {
            return Result.error("未登录或未绑定医生信息");
        }
        List<DoctorShiftChangeRequest> list = service.lambdaQuery()
                .eq(DoctorShiftChangeRequest::getDoctorId, doctorId)
                .eq(status != null, DoctorShiftChangeRequest::getStatus, status)
                .orderByDesc(DoctorShiftChangeRequest::getApplyTime)
                .list();
        return Result.OK(list);
    }

    private Long resolveCurrentDoctorId(HttpServletRequest httpRequest) {
        HosUser current = null;
        String token = httpRequest.getHeader(CommonConstant.X_ACCESS_TOKEN);
        if (token != null && !token.isEmpty()) {
            try {
                String account = JwtUtil.getUsername(token);
                current = hosUserService.lambdaQuery()
                        .eq(HosUser::getUserAccount, account)
                        .one();
            } catch (Exception ignored) {}
        }
        if (current == null) {
            Object userObj = httpRequest.getSession().getAttribute(org.jeecg.modules.hospital.contant.UserContant.USER_LOGIN_STATE);
            if (userObj instanceof HosUser) {
                current = (HosUser) userObj;
            }
        }
        if (current == null || current.getUserType() == null || current.getUserType() != 2) {
            return null;
        }
        Doctor doctor = doctorService.lambdaQuery().eq(Doctor::getUserId, current.getUserId()).one();
        return doctor != null ? doctor.getDoctorId() : null;
    }
}