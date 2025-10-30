// DoctorProfileController 类（修正 HttpServletRequest 包）
package org.jeecg.modules.hospital.controller.doctor;

import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.jeecg.modules.hospital.service.HosUserService;
import org.springframework.web.bind.annotation.*;
import org.jeecg.modules.hospital.controller.doctor.dto.DoctorProfileDTO;
import org.jeecg.modules.hospital.controller.doctor.request.DoctorProfileUpdateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;

@RestController
@RequestMapping("/doctor/profile")
@Tag(name = "医生端-个人信息")
public class DoctorProfileController {

    @Resource
    private DoctorService doctorService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private HosUserService hosUserService;

    @Operation(summary = "获取医生个人信息")
    @GetMapping
    public Result<DoctorProfileDTO> getProfile(@RequestParam Long doctorId) {
        Doctor doctor = doctorService.getById(doctorId);
        if (doctor == null) {
            return Result.error("医生不存在");
        }
        DoctorProfileDTO dto = new DoctorProfileDTO();
        dto.setDoctorId(doctor.getDoctorId());
        dto.setDoctorName(doctor.getDoctorName());
        dto.setUserId(doctor.getUserId());
        dto.setDeptId(doctor.getDeptId());
        dto.setTitle(doctor.getTitle());
        dto.setSpecialty(doctor.getSpecialty());
        dto.setDoctorDesc(doctor.getDoctorDesc());
        dto.setAvatar(doctor.getAvatar());
        dto.setIsActive(doctor.getIsActive());
        dto.setUpdateVerify(doctor.getUpdateVerify());

        if (doctor.getDeptId() != null) {
            Department dept = departmentService.getById(doctor.getDeptId());
            if (dept != null) {
                dto.setDeptName(dept.getDeptName());
            }
        }
        // 追加：读取 hos_user 的账号与邮箱
        if (doctor.getUserId() != null) {
            org.jeecg.modules.hospital.entity.HosUser user = hosUserService.getById(doctor.getUserId());
            if (user != null) {
                dto.setUserAccount(user.getUserAccount());
                dto.setEmail(user.getEmail());
            }
        }
        return Result.ok(dto);
    }

    @Operation(summary = "更新医生个人信息")
    @PutMapping
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateProfile(@RequestBody DoctorProfileUpdateRequest req) {
        if (req.getDoctorId() == null) {
            return Result.error("doctorId 不能为空");
        }
        Doctor doctor = doctorService.getById(req.getDoctorId());
        if (doctor == null) {
            return Result.error("医生不存在");
        }

        // 允许更新的 Doctor 字段
        if (req.getDoctorName() != null) doctor.setDoctorName(req.getDoctorName());
        if (req.getDeptId() != null) doctor.setDeptId(req.getDeptId());
        if (req.getTitle() != null) doctor.setTitle(req.getTitle());
        if (req.getSpecialty() != null) doctor.setSpecialty(req.getSpecialty());
        if (req.getDoctorDesc() != null) doctor.setDoctorDesc(req.getDoctorDesc());
        if (req.getAvatar() != null) doctor.setAvatar(req.getAvatar());
        if (req.getIsActive() != null) doctor.setIsActive(req.getIsActive());

        boolean ok = doctorService.updateById(doctor);
        if (!ok) {
            return Result.error("更新 doctor 失败");
        }

        // 允许更新的 HosUser 字段（如账号、邮箱）
        if (doctor.getUserId() != null) {
            org.jeecg.modules.hospital.entity.HosUser user = hosUserService.getById(doctor.getUserId());
            if (user != null) {
                if (req.getUserAccount() != null) user.setUserAccount(req.getUserAccount());
                if (req.getEmail() != null) user.setEmail(req.getEmail());
                user.setUpdateTime(java.time.LocalDateTime.now());
                boolean uok = hosUserService.updateById(user);
                if (!uok) {
                    return Result.error("更新 hos_user 失败");
                }
            }
        }

        return Result.ok(true);
    }
    @GetMapping("/me")
    @Operation(summary = "获取当前登录医生个人信息")
    public Result<DoctorProfileDTO> getMyProfile(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader(CommonConstant.X_ACCESS_TOKEN);
        org.jeecg.modules.hospital.entity.HosUser current = null;
        if (token != null && !token.isEmpty()) {
            try {
                String account = JwtUtil.getUsername(token);
                current = hosUserService.lambdaQuery()
                        .eq(org.jeecg.modules.hospital.entity.HosUser::getUserAccount, account)
                        .one();
            } catch (Exception ignored) {}
        }
        // 兜底：会话方式（保留原逻辑）
        if (current == null) {
            Object userObj = httpRequest.getSession().getAttribute(org.jeecg.modules.hospital.contant.UserContant.USER_LOGIN_STATE);
            if (userObj instanceof org.jeecg.modules.hospital.entity.HosUser) {
                current = (org.jeecg.modules.hospital.entity.HosUser) userObj;
            }
        }
        if (current == null) {
            return Result.error("未登录");
        }
        if (current.getUserType() == null || current.getUserType() != 2) {
            return Result.error("仅医生账号可访问该接口");
        }

        Doctor doctor = doctorService.lambdaQuery().eq(Doctor::getUserId, current.getUserId()).one();
        if (doctor == null) {
            return Result.error("未找到绑定的医生信息");
        }

        DoctorProfileDTO dto = new DoctorProfileDTO();
        dto.setDoctorId(doctor.getDoctorId());
        dto.setDoctorName(doctor.getDoctorName());
        dto.setUserId(doctor.getUserId());
        dto.setDeptId(doctor.getDeptId());
        dto.setTitle(doctor.getTitle());
        dto.setSpecialty(doctor.getSpecialty());
        dto.setDoctorDesc(doctor.getDoctorDesc());
        dto.setAvatar(doctor.getAvatar());
        dto.setIsActive(doctor.getIsActive());
        dto.setUpdateVerify(doctor.getUpdateVerify());

        if (doctor.getDeptId() != null) {
            Department dept = departmentService.getById(doctor.getDeptId());
            if (dept != null) {
                dto.setDeptName(dept.getDeptName());
            }
        }
        if (doctor.getUserId() != null) {
            org.jeecg.modules.hospital.entity.HosUser user = hosUserService.getById(doctor.getUserId());
            if (user != null) {
                dto.setUserAccount(user.getUserAccount());
                dto.setEmail(user.getEmail());
            }
        }
        return Result.ok(dto);
    }
    @GetMapping("/byAccount")
    @Operation(summary = "按账号获取医生个人信息")
    public Result<DoctorProfileDTO> getByAccount(@RequestParam String account) {
        org.jeecg.modules.hospital.entity.HosUser user = hosUserService.lambdaQuery()
                .eq(org.jeecg.modules.hospital.entity.HosUser::getUserAccount, account)
                .one();
        if (user == null) {
            return Result.error("未找到该账号对应的用户");
        }
        Doctor doctor = doctorService.lambdaQuery().eq(Doctor::getUserId, user.getUserId()).one();
        if (doctor == null) {
            return Result.error("未找到绑定的医生信息");
        }
        DoctorProfileDTO dto = new DoctorProfileDTO();
        dto.setDoctorId(doctor.getDoctorId());
        dto.setDoctorName(doctor.getDoctorName());
        dto.setUserId(doctor.getUserId());
        dto.setDeptId(doctor.getDeptId());
        dto.setTitle(doctor.getTitle());
        dto.setSpecialty(doctor.getSpecialty());
        dto.setDoctorDesc(doctor.getDoctorDesc());
        dto.setAvatar(doctor.getAvatar());
        dto.setIsActive(doctor.getIsActive());
        dto.setUpdateVerify(doctor.getUpdateVerify());
        if (doctor.getDeptId() != null) {
            Department dept = departmentService.getById(doctor.getDeptId());
            if (dept != null) {
                dto.setDeptName(dept.getDeptName());
            }
        }
        if (doctor.getUserId() != null) {
            org.jeecg.modules.hospital.entity.HosUser u = hosUserService.getById(doctor.getUserId());
            if (u != null) {
                dto.setUserAccount(u.getUserAccount());
                dto.setEmail(u.getEmail());
            }
        }
        return Result.ok(dto);
    }
    @GetMapping("/byUserId")
    public Result<DoctorProfileDTO> getByUserId(@RequestParam Long userId) {
        Doctor doctor = doctorService.lambdaQuery().eq(Doctor::getUserId, userId).one();
        if (doctor == null) {
            return Result.error("未找到绑定的医生信息");
        }
        DoctorProfileDTO dto = new DoctorProfileDTO();
        dto.setDoctorId(doctor.getDoctorId());
        dto.setDoctorName(doctor.getDoctorName());
        dto.setUserId(doctor.getUserId());
        dto.setDeptId(doctor.getDeptId());
        dto.setTitle(doctor.getTitle());
        dto.setSpecialty(doctor.getSpecialty());
        dto.setDoctorDesc(doctor.getDoctorDesc());
        dto.setAvatar(doctor.getAvatar());
        dto.setIsActive(doctor.getIsActive());
        dto.setUpdateVerify(doctor.getUpdateVerify());
        if (doctor.getDeptId() != null) {
            Department dept = departmentService.getById(doctor.getDeptId());
            if (dept != null) {
                dto.setDeptName(dept.getDeptName());
            }
        }
        if (doctor.getUserId() != null) {
            org.jeecg.modules.hospital.entity.HosUser user = hosUserService.getById(doctor.getUserId());
            if (user != null) {
                dto.setUserAccount(user.getUserAccount());
                dto.setEmail(user.getEmail());
            }
        }
        return Result.ok(dto);
    }
}