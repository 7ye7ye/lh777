package org.jeecg.modules.hospital.controller.doctor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.service.PatientService;
import org.jeecg.modules.hospital.vo.PatientBriefVO;
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

/**
 * 医生端-患者按日期查询（匹配前端 /doctor/patients/by-date）
 */
@RestController
@RequestMapping("/doctor")
@Tag(name = "医生端-患者按日期查询")
public class DoctorPatientQueryController {

    @Resource
    private PatientService patientService;

    @Resource
    private DoctorService doctorService;

    @Resource
    private HosUserService hosUserService;

    @Operation(summary = "按日期获取患者列表（医生端）")
    @GetMapping("/patients/by-date")
    public Result<List<PatientBriefVO>> getByDate(
            HttpServletRequest request,
            @RequestParam String date
    ) {
        Long doctorId = resolveCurrentDoctorId(request);
        if (doctorId == null) {
            return Result.error("未登录或未绑定医生信息");
        }
        LocalDate d;
        try {
            d = LocalDate.parse(date.replace('/', '-'));
        } catch (Exception e) {
            return Result.error("日期格式错误");
        }
        List<PatientBriefVO> list = patientService.list(doctorId, null, d, d, null);
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
        // 优化：不再严格依赖 userType == 2，只要有医生绑定记录即可
        if (current == null) {
            return null;
        }
        Doctor doctor = doctorService.lambdaQuery().eq(Doctor::getUserId, current.getUserId()).one();
        return doctor != null ? doctor.getDoctorId() : null;
    }
}