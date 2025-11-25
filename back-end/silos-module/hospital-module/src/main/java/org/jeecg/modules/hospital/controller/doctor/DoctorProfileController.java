// DoctorProfileController 类（修正 HttpServletRequest 包）
package org.jeecg.modules.hospital.controller.doctor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jeecg.common.api.vo.Result;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.hospital.controller.doctor.dto.DoctorProfileDTO;
import org.jeecg.modules.hospital.entity.DoctorProfileUpdateRequest;

import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.jeecg.modules.hospital.service.DoctorProfileUpdateRequestService;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.HosUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 医生端-个人信息
 */
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

    @Resource
    private DoctorProfileUpdateRequestService doctorProfileUpdateRequestService;

    /**
     * 医生资料修改申请 DTO
     *
     * 前端传入字段示例：
     * - id:          医生ID（doctor_id）
     * - avatar:      头像URL（建议是已上传至服务器后的完整URL）
     * - specialty:   擅长领域
     * - doctorDesc:  医生简介
     */
    public static class DoctorProfileUpdateApplyDTO {

        /** 医生ID，对应 doctor.doctor_id */
        @NotNull(message = "医生ID不能为空")
        private Long id;

        /** 申请后的头像URL，可为空 */
        private String avatar;
 
        /** 申请后的擅长领域 */
        @NotNull(message = "擅长领域不能为空")
        @Size(max = 100, message = "擅长领域长度不能超过100字符")
        private String specialty;

        /** 申请后的医生简介 */
        @Size(max = 500, message = "医生简介长度不能超过500字符")
        private String doctorDesc;

        // 预留：冗余记录当前提交时间（也可以直接在表里用 create_time 默认值）
        private LocalDateTime applyTime = LocalDateTime.now();

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getSpecialty() {
            return specialty;
        }

        public void setSpecialty(String specialty) {
            this.specialty = specialty;
        }

        public String getDoctorDesc() {
            return doctorDesc;
        }

        public void setDoctorDesc(String doctorDesc) {
            this.doctorDesc = doctorDesc;
        }

        public LocalDateTime getApplyTime() {
            return applyTime;
        }

        public void setApplyTime(LocalDateTime applyTime) {
            this.applyTime = applyTime;
        }
    }

    /**
     * 管理员审批资料修改申请 DTO
     */
    public static class UpdateRequestAuditDTO {

        @NotNull(message = "申请ID不能为空")
        private Long requestId;

        private String reason;

        public Long getRequestId() {
            return requestId;
        }

        public void setRequestId(Long requestId) {
            this.requestId = requestId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

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

    /**
     * 提交医生资料修改申请
     *
     * 对应前端：POST /doctor/profile/update-request
     *
     * 建议在 Service 层中：
     * 1. 根据 doctor_id 校验医生是否存在；
     * 2. 向 doctor_profile_update_request 表插入一条 status=1(待审核) 的记录；
     * 3. 管理员审核通过后，再把 avatar/specialty/doctor_desc 同步更新到 doctor 表；
     * 4. 同时更新 doctor.update_verify 字段表示审核状态。
     */
    @PostMapping("/update-request")
    @Operation(summary = "提交医生资料修改申请（头像、擅长领域、简介）")
    public Result<Boolean> applyProfileUpdate(@RequestBody @Valid DoctorProfileUpdateApplyDTO dto) {
        // 校验医生是否存在
        Doctor doctor = doctorService.getById(dto.getId());
        if (doctor == null) {
            return Result.error("医生不存在");
        }

        // 创建一条资料修改申请，状态默认为待审核
        doctorProfileUpdateRequestService.createRequest(
                dto.getId(),
                dto.getAvatar(),
                dto.getSpecialty(),
                dto.getDoctorDesc()
        );

        return Result.ok(true);
    }

    // ========== 管理员端：资料修改申请审批 ==========

    @GetMapping("/update-request/list")
    @Operation(summary = "分页查询医生资料修改申请")
    public Result<Page<DoctorProfileUpdateRequest>> listUpdateRequests(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        Page<DoctorProfileUpdateRequest> page = doctorProfileUpdateRequestService.pageRequests(pageNo, pageSize, status);
        return Result.ok(page);
    }

    @PostMapping("/update-request/approve")
    @Operation(summary = "审核通过医生资料修改申请")
    public Result<Boolean> approveUpdateRequest(@RequestBody @Valid UpdateRequestAuditDTO dto) {
        doctorProfileUpdateRequestService.approveRequest(dto.getRequestId(), dto.getReason());
        return Result.ok(true);
    }

    @PostMapping("/update-request/reject")
    @Operation(summary = "驳回医生资料修改申请")
    public Result<Boolean> rejectUpdateRequest(@RequestBody @Valid UpdateRequestAuditDTO dto) {
        doctorProfileUpdateRequestService.rejectRequest(dto.getRequestId(), dto.getReason());
        return Result.ok(true);
    }

    // ========== 医生端：查看本人资料修改申请记录 ==========

    @GetMapping("/update-request/my")
    @Operation(summary = "医生端-查看本人资料修改申请记录")
    public Result<Page<DoctorProfileUpdateRequest>> listMyUpdateRequests(
            @RequestParam Long doctorId,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) Integer status) {
        Page<DoctorProfileUpdateRequest> page = doctorProfileUpdateRequestService.pageMyRequests(doctorId, pageNo, pageSize, status);
        return Result.ok(page);
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