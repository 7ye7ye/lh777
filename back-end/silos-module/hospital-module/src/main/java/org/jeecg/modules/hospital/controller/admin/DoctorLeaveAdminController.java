package org.jeecg.modules.hospital.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.hospital.entity.DoctorLeaveRequest;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.entity.RegistrationRecord;
import org.jeecg.modules.hospital.mapper.RegistrationRecordMapper;
import org.jeecg.modules.hospital.service.DoctorLeaveRequestService;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.jeecg.modules.hospital.service.HosUserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员-医生请假审批控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/leave")
@Tag(name = "管理员-医生请假审批")
public class DoctorLeaveAdminController {

    @Resource
    private DoctorLeaveRequestService leaveRequestService;

    @Resource
    private DoctorScheduleService scheduleService;

    @Resource
    private HosUserService hosUserService;

    @Resource
    private RegistrationRecordMapper registrationRecordMapper;

    @Resource
    private org.jeecg.modules.hospital.service.RegistrationService registrationService;

    @Operation(summary = "获取请假申请列表")
    @GetMapping("/list")
    public Result<IPage<DoctorLeaveRequest>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Page<DoctorLeaveRequest> page = new Page<>(current, size);
        LambdaQueryWrapper<DoctorLeaveRequest> queryWrapper = new LambdaQueryWrapper<>();

        // 状态筛选
        if (status != null) {
            queryWrapper.eq(DoctorLeaveRequest::getStatus, status);
        }

        // 医生姓名筛选
        if (doctorName != null && !doctorName.trim().isEmpty()) {
            queryWrapper.like(DoctorLeaveRequest::getDoctorName, doctorName);
        }

        // 科室筛选
        if (deptId != null) {
            queryWrapper.eq(DoctorLeaveRequest::getDeptId, deptId);
        }

        // 申请时间范围筛选
        if (startDate != null) {
            queryWrapper.ge(DoctorLeaveRequest::getApplyTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            queryWrapper.le(DoctorLeaveRequest::getApplyTime, endDate.atTime(23, 59, 59));
        }

        // 按申请时间倒序
        queryWrapper.orderByDesc(DoctorLeaveRequest::getApplyTime);

        IPage<DoctorLeaveRequest> result = leaveRequestService.page(page, queryWrapper);
        return Result.OK(result);
    }

    @Operation(summary = "获取请假申请详情")
    @GetMapping("/{leaveId}")
    public Result<DoctorLeaveRequest> detail(@PathVariable Long leaveId) {
        DoctorLeaveRequest leaveRequest = leaveRequestService.getById(leaveId);
        if (leaveRequest == null) {
            return Result.error("请假申请不存在");
        }
        return Result.OK(leaveRequest);
    }

    @Operation(summary = "审批请假申请")
    @PostMapping("/approve")
    public Result<Boolean> approve(HttpServletRequest httpRequest, @RequestBody ApprovalRequest request) {
        try {
            DoctorLeaveRequest leaveRequest = leaveRequestService.getById(request.getLeaveId());
            if (leaveRequest == null) {
                return Result.error("请假申请不存在");
            }

            if (leaveRequest.getStatus() != 1) {
                return Result.error("该申请已处理，无法重复审批");
            }

            Long adminId = resolveCurrentAdminId(httpRequest);
            if (adminId == null) {
                return Result.error("未登录或权限不足");
            }

            // 更新审批状态
            leaveRequest.setStatus(request.getStatus());
            leaveRequest.setApproveTime(LocalDateTime.now());
            leaveRequest.setAdminId(adminId);

            if (request.getStatus() == 3 && request.getRejectReason() != null) {
                leaveRequest.setRejectReason(request.getRejectReason());
            }

            // 如果同意，需要禁用或删除请假期间的排班
            if (request.getStatus() == 2) {
                LocalDate startDate = leaveRequest.getStartDate();
                LocalDate endDate = leaveRequest.getEndDate();
                Long doctorId = leaveRequest.getDoctorId();

                if (startDate == null || endDate == null || doctorId == null) {
                    return Result.error("请假日期或医生ID缺失");
                }

                // 查询请假日期范围内的所有排班
                List<DoctorSchedule> schedules = scheduleService.listByDoctorAndDateRange(
                        doctorId, startDate, endDate);

                // 禁用这些排班（将状态设为0）并更新相关挂号记录
                for (DoctorSchedule schedule : schedules) {
                    if (schedule.getStatus() == 1) { // 只处理有效状态的排班
                        schedule.setStatus(0); // 0表示已禁用
                        schedule.setUpdateTime(LocalDateTime.now());
                        scheduleService.updateById(schedule);

                        // 更新该排班相关的挂号记录状态为3（已退号）
                        updateRegistrationRecordsForSchedule(schedule.getScheduleId(),
                                "医生临时停诊");
                    }
                }
            }

            boolean success = leaveRequestService.updateById(leaveRequest);
            return Result.OK(success);
        } catch (Exception e) {
            return Result.error("审批失败：" + e.getMessage());
        }
    }

    private Long resolveCurrentAdminId(HttpServletRequest httpRequest) {
        HosUser current = null;
        String token = httpRequest.getHeader(CommonConstant.X_ACCESS_TOKEN);
        if (token != null && !token.isEmpty()) {
            try {
                String account = JwtUtil.getUsername(token);
                current = hosUserService.lambdaQuery()
                        .eq(HosUser::getUserAccount, account)
                        .one();
            } catch (Exception ignored) {
            }
        }
        if (current == null) {
            Object userObj = httpRequest.getSession()
                    .getAttribute(org.jeecg.modules.hospital.contant.UserContant.USER_LOGIN_STATE);
            if (userObj instanceof HosUser) {
                current = (HosUser) userObj;
            }
        }
        return current != null ? current.getUserId() : null;
    }

    /**
     * 审批请求参数
     */
    public static class ApprovalRequest {
        private Long leaveId;
        private Integer status; // 2-通过, 3-驳回
        private String rejectReason;

        public Long getLeaveId() {
            return leaveId;
        }

        public void setLeaveId(Long leaveId) {
            this.leaveId = leaveId;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getRejectReason() {
            return rejectReason;
        }

        public void setRejectReason(String rejectReason) {
            this.rejectReason = rejectReason;
        }
    }

    /**
     * 更新指定排班相关的挂号记录状态为3（已退号）
     * 
     * @param scheduleId 排班ID
     * @param reason     退号原因
     */
    private void updateRegistrationRecordsForSchedule(Long scheduleId, String reason) {
        if (scheduleId == null) {
            log.warn("updateRegistrationRecordsForSchedule: scheduleId为null，跳过更新");
            return;
        }

        try {
            // 先查询该排班的所有有效挂号记录，用于日志记录
            LambdaQueryWrapper<RegistrationRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RegistrationRecord::getScheduleId, scheduleId)
                    .in(RegistrationRecord::getStatus, 0, 1, 2); // 只更新有效状态的挂号记录

            List<RegistrationRecord> records = registrationRecordMapper.selectList(queryWrapper);
            log.info("找到排班ID {} 相关的挂号记录 {} 条", scheduleId, records.size());

            // 发送排班取消通知 (因医生请假)
            try {
                if (!records.isEmpty()) {
                    registrationService.sendBatchScheduleCancellationMessages(records, reason);
                }
            } catch (Exception e) {
                log.error("批量发送医生请假退号通知失败", e);
            }

            if (records.isEmpty()) {
                log.info("排班ID {} 没有需要更新的挂号记录", scheduleId);
                return;
            }

            // 使用批量SQL更新，更高效且可靠
            LocalDateTime now = LocalDateTime.now();
            int updateCount = registrationRecordMapper.updateStatusByScheduleId(scheduleId, now, reason);
            log.info("排班ID {} 的挂号记录批量更新完成，成功更新 {} 条记录", scheduleId, updateCount);

            // 验证更新结果
            if (updateCount != records.size()) {
                log.warn("更新数量不匹配：查询到 {} 条记录，但只更新了 {} 条", records.size(), updateCount);
            }
        } catch (Exception e) {
            // 记录错误但不影响审批流程
            log.error("更新挂号记录状态失败，排班ID：{}", scheduleId, e);
            e.printStackTrace();
        }
    }
}
