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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理员-医生调班审批控制器
 */
@RestController
@RequestMapping("/admin/adjustment")
@Tag(name = "管理员-医生调班审批")
public class DoctorAdjustmentAdminController {

    @Resource
    private DoctorShiftChangeRequestService adjustmentService;

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
        return Result.OK(result);
    }

    @Operation(summary = "获取调班申请详情")
    @GetMapping("/{adjustmentId}")
    public Result<DoctorShiftChangeRequest> detail(@PathVariable Long adjustmentId) {
        DoctorShiftChangeRequest adjustment = adjustmentService.getById(adjustmentId);
        if (adjustment == null) {
            return Result.error("调班申请不存在");
        }
        return Result.OK(adjustment);
    }

    @Operation(summary = "审批调班申请")
    @PostMapping("/approve")
    public Result<Boolean> approve(@RequestBody ApprovalRequest request) {
        try {
            DoctorShiftChangeRequest adjustment = adjustmentService.getById(request.getAdjustmentId());
            if (adjustment == null) {
                return Result.error("调班申请不存在");
            }
            
            if (adjustment.getStatus() != 1) {
                return Result.error("该申请已处理，无法重复审批");
            }
            
            // 更新审批状态
            adjustment.setStatus(request.getStatus());
            adjustment.setApproveTime(LocalDateTime.now());
            adjustment.setAdminId(1L); // 这里应该从当前登录用户获取
            
            if (request.getStatus() == 3 && request.getRejectReason() != null) {
                adjustment.setRejectReason(request.getRejectReason());
            }
            
            // 如果通过，这里可以添加创建新排班记录的逻辑
            if (request.getStatus() == 2) {
                // TODO: 创建新的排班记录
                // adjustment.setNewScheduleId(newScheduleId);
            }
            
            boolean success = adjustmentService.updateById(adjustment);
            return Result.OK(success);
        } catch (Exception e) {
            return Result.error("审批失败：" + e.getMessage());
        }
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