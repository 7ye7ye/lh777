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
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.HosUserService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/doctor/patient")
@Tag(name = "医生端-患者管理")
public class PatientDoctorController {

    @Resource
    private PatientService patientService;

    @Resource
    private DoctorService doctorService;

    @Resource
    private HosUserService hosUserService;

    @Operation(summary = "患者列表（医生端）")
    @GetMapping("/list")
    public Result<List<PatientBriefVO>> list(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer status
    ) {
        Long doctorId = resolveCurrentDoctorId(request);
        if (doctorId == null) {
            return Result.error("未登录或未绑定医生信息");
        }
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
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long doctorId = resolveCurrentDoctorId(request);
        if (doctorId == null) {
            return Result.error("未登录或未绑定医生信息");
        }
        List<PatientBriefVO> list = patientService.list(doctorId, null, date, date, null);
        return Result.OK(list);
    }

    @Operation(summary = "更新就诊状态（开始接诊/完成接诊）")
    @PostMapping("/status")
    public Result<Boolean> updateStatus(@RequestBody UpdateVisitStatusRequest req) {
        boolean ok = patientService.updateVisitStatus(req.getAppointmentId(), req.getRegistrationNo(), req.getAction());
        return ok ? Result.OK(true) : Result.error("更新失败，请检查参数或数据状态");
    }

    public static class UpdateVisitStatusRequest {
        private Long appointmentId;
        private String registrationNo; // 新增：挂号单号
        private String action; // "start"/"finish" 或 "开始接诊"/"完成接诊" 或 “开始/完成”
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
        public String getRegistrationNo() { return registrationNo; }
        public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
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