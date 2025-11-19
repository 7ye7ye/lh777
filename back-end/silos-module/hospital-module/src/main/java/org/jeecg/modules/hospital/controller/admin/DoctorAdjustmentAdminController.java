package org.jeecg.modules.hospital.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest;
import org.jeecg.modules.hospital.service.DoctorShiftChangeRequestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.service.HosUserService;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.entity.Department;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 管理员-医生调班审批控制器
 */
@RestController
@RequestMapping("/admin/adjustment")
@Tag(name = "管理员-医生调班审批")
public class DoctorAdjustmentAdminController {

    @Resource
    private DoctorShiftChangeRequestService adjustmentService;

    @Resource
    private HosUserService hosUserService;

    @Resource
    private DoctorScheduleService scheduleService;

    @Resource
    private DoctorService doctorService;

    @Resource
    private DepartmentService departmentService;

    @Operation(summary = "获取调班申请列表")
    @GetMapping("/list")
    public Result<IPage<DoctorShiftChangeRequest>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Page<DoctorShiftChangeRequest> page = new Page<>(current, size);
        LambdaQueryWrapper<DoctorShiftChangeRequest> queryWrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (status != null) {
            queryWrapper.eq(DoctorShiftChangeRequest::getStatus, status);
        }
        
        // 日期范围筛选
        if (startDate != null) {
            queryWrapper.ge(DoctorShiftChangeRequest::getApplyTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            queryWrapper.le(DoctorShiftChangeRequest::getApplyTime, endDate.atTime(23, 59, 59));
        }
        
        // 按申请时间倒序
        queryWrapper.orderByDesc(DoctorShiftChangeRequest::getApplyTime);
        
        IPage<DoctorShiftChangeRequest> result = adjustmentService.page(page, queryWrapper);
        for (DoctorShiftChangeRequest item : result.getRecords()) {
            if (item.getDoctorId() != null) {
                Doctor doc = doctorService.getById(item.getDoctorId());
                if (doc != null) {
                    item.setDoctorName(doc.getDoctorName());
                }
            }
            Long deptIdToShow = item.getTargetDeptId() != null ? item.getTargetDeptId() : null;
            if (deptIdToShow == null && item.getOriginalScheduleId() != null) {
                DoctorSchedule origin = scheduleService.getById(item.getOriginalScheduleId());
                if (origin != null) {
                    deptIdToShow = origin.getDeptId();
                }
            }
            if (deptIdToShow != null) {
                Department dept = departmentService.getById(deptIdToShow);
                if (dept != null) {
                    item.setDeptName(dept.getDeptName());
                }
            }
            if (item.getOriginalScheduleId() != null) {
                DoctorSchedule origin = scheduleService.getById(item.getOriginalScheduleId());
                if (origin != null) {
                    item.setOriginalDate(origin.getScheduleDate());
                    item.setOriginalTimeSlot(origin.getTimeSlot());
                }
            }
        }
        return Result.OK(result);
    }

    @Operation(summary = "获取调班申请详情")
    @GetMapping("/{adjustmentId}")
    public Result<DoctorShiftChangeRequest> detail(@PathVariable Long adjustmentId) {
        DoctorShiftChangeRequest adjustment = adjustmentService.getById(adjustmentId);
        if (adjustment == null) {
            return Result.error("调班申请不存在");
        }
        if (adjustment.getDoctorId() != null) {
            Doctor doc = doctorService.getById(adjustment.getDoctorId());
            if (doc != null) {
                adjustment.setDoctorName(doc.getDoctorName());
            }
        }
        Long deptIdToShow = adjustment.getTargetDeptId() != null ? adjustment.getTargetDeptId() : null;
        if (deptIdToShow == null && adjustment.getOriginalScheduleId() != null) {
            DoctorSchedule origin = scheduleService.getById(adjustment.getOriginalScheduleId());
            if (origin != null) {
                deptIdToShow = origin.getDeptId();
            }
        }
        if (deptIdToShow != null) {
            Department dept = departmentService.getById(deptIdToShow);
            if (dept != null) {
                adjustment.setDeptName(dept.getDeptName());
            }
        }
        if (adjustment.getOriginalScheduleId() != null) {
            DoctorSchedule origin = scheduleService.getById(adjustment.getOriginalScheduleId());
            if (origin != null) {
                adjustment.setOriginalDate(origin.getScheduleDate());
                adjustment.setOriginalTimeSlot(origin.getTimeSlot());
            }
        }
        return Result.OK(adjustment);
    }

    @Operation(summary = "审批调班申请")
    @PostMapping("/approve")
    public Result<Boolean> approve(HttpServletRequest httpRequest, @RequestBody ApprovalRequest request) {
        try {
            DoctorShiftChangeRequest adjustment = adjustmentService.getById(request.getAdjustmentId());
            if (adjustment == null) {
                return Result.error("调班申请不存在");
            }
            
            if (adjustment.getStatus() != 1) {
                return Result.error("该申请已处理，无法重复审批");
            }
            
            Long adminId = resolveCurrentAdminId(httpRequest);
            if (adminId == null) {
                return Result.error("未登录或权限不足");
            }

            // 更新审批状态
            adjustment.setStatus(request.getStatus());
            adjustment.setApproveTime(LocalDateTime.now());
            adjustment.setAdminId(adminId);
            
            if (request.getStatus() == 3 && request.getRejectReason() != null) {
                adjustment.setRejectReason(request.getRejectReason());
            }
            
            if (request.getStatus() == 2) {
                if (adjustment.getOriginalScheduleId() == null) {
                    return Result.error("缺少原排班ID");
                }
                DoctorSchedule origin = scheduleService.getById(adjustment.getOriginalScheduleId());
                if (origin == null) {
                    return Result.error("原排班记录不存在");
                }
                LocalDate targetDate = adjustment.getTargetDate();
                Integer targetSlot = adjustment.getTargetTimeSlot();
                Long targetDeptId = adjustment.getTargetDeptId() != null ? adjustment.getTargetDeptId() : origin.getDeptId();

                List<String> pool = roomPool();
                Set<String> used = new HashSet<>();
                List<DoctorSchedule> sameTime = scheduleService.lambdaQuery()
                        .eq(DoctorSchedule::getScheduleDate, targetDate)
                        .eq(DoctorSchedule::getTimeSlot, targetSlot)
                        .eq(DoctorSchedule::getStatus, 1)
                        .list();
                for (DoctorSchedule ds : sameTime) {
                    if (ds.getRoomNumber() != null) used.add(ds.getRoomNumber());
                }
                List<String> available = new ArrayList<>();
                for (String r : pool) {
                    if (!used.contains(r)) available.add(r);
                }
                if (available.isEmpty()) {
                    return Result.error("该日期时段无可用诊室");
                }
                String room = available.get(ThreadLocalRandom.current().nextInt(available.size()));

                DoctorSchedule newSchedule = new DoctorSchedule();
                newSchedule.setDoctorId(origin.getDoctorId());
                newSchedule.setDeptId(targetDeptId);
                newSchedule.setScheduleDate(targetDate);
                newSchedule.setTimeSlot(targetSlot);
                newSchedule.setRoomNumber(room);
                newSchedule.setMaxQuota(origin.getMaxQuota());
                newSchedule.setUsedQuota(0);
                newSchedule.setStatus(1);
                newSchedule.setCreateTime(LocalDateTime.now());
                newSchedule.setUpdateTime(LocalDateTime.now());
                boolean created = scheduleService.save(newSchedule);
                if (!created) {
                    return Result.error("创建新排班失败");
                }
                adjustment.setNewScheduleId(newSchedule.getScheduleId());

                origin.setStatus(0);
                origin.setUpdateTime(LocalDateTime.now());
                boolean disabled = scheduleService.updateById(origin);
                if (!disabled) {
                    return Result.error("更新原排班状态失败");
                }
            }
            
            boolean success = adjustmentService.updateById(adjustment);
            return Result.OK(success);
        } catch (Exception e) {
            return Result.error("审批失败：" + e.getMessage());
        }
    }

    private List<String> roomPool() {
        List<String> list = new ArrayList<>();
        list.add("A-101");
        list.add("A-102");
        list.add("A-103");
        list.add("A-104");
        list.add("A-105");
        list.add("B-201");
        list.add("B-202");
        return list;
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
            } catch (Exception ignored) {}
        }
        if (current == null) {
            Object userObj = httpRequest.getSession().getAttribute(org.jeecg.modules.hospital.contant.UserContant.USER_LOGIN_STATE);
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
        private Long adjustmentId;
        private Integer status; // 2-通过, 3-驳回
        private String rejectReason;

        public Long getAdjustmentId() { return adjustmentId; }
        public void setAdjustmentId(Long adjustmentId) { this.adjustmentId = adjustmentId; }

        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }

        public String getRejectReason() { return rejectReason; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    }
}