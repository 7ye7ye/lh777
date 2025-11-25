package org.jeecg.modules.hospital.controller.doctor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.DoctorLeaveRequest;
import org.jeecg.modules.hospital.service.DoctorLeaveRequestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/doctor/leave")
@Tag(name = "医生端-请假申请")
public class DoctorLeaveController {

    @Resource
    private DoctorLeaveRequestService leaveRequestService;

    @Operation(summary = "医生提交请假申请")
    @PostMapping("/apply")
    public Result<Boolean> apply(@RequestBody ApplyRequest req) {
        if (req.getDoctorId() == null || req.getDoctorName() == null || 
            req.getDeptId() == null || req.getDeptName() == null) {
            return Result.error("缺少必填字段：doctorId、doctorName、deptId 或 deptName");
        }
        if (req.getLeaveType() == null || req.getLeaveType().isEmpty()) {
            return Result.error("请选择请假类型");
        }
        if (req.getStartDate() == null || req.getStartDate().isEmpty()) {
            return Result.error("请选择请假开始日期");
        }
        if (req.getEndDate() == null || req.getEndDate().isEmpty()) {
            return Result.error("请选择请假结束日期");
        }
        if (req.getReason() == null || req.getReason().trim().isEmpty()) {
            return Result.error("请填写请假事由");
        }

        // 验证日期
        LocalDate startDate = LocalDate.parse(req.getStartDate());
        LocalDate endDate = LocalDate.parse(req.getEndDate());
        if (endDate.isBefore(startDate)) {
            return Result.error("结束日期不能早于开始日期");
        }

        DoctorLeaveRequest r = new DoctorLeaveRequest();
        r.setDoctorId(req.getDoctorId());
        r.setDoctorName(req.getDoctorName());
        r.setDeptId(req.getDeptId());
        r.setDeptName(req.getDeptName());
        r.setLeaveType(req.getLeaveType());
        r.setStartDate(startDate);
        r.setEndDate(endDate);
        r.setReason(req.getReason());
        r.setApplyTime(LocalDateTime.now());
        r.setStatus(1); // 待审批

        boolean ok = leaveRequestService.submitLeaveRequest(r);
        return ok ? Result.OK(true) : Result.error("提交失败");
    }

    @Operation(summary = "医生查询自己的请假申请列表")
    @GetMapping("/list")
    public Result<IPage<DoctorLeaveRequest>> list(
            @RequestParam Long doctorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Page<DoctorLeaveRequest> page = new Page<>(current, size);
        LambdaQueryWrapper<DoctorLeaveRequest> q = new LambdaQueryWrapper<>();
        q.eq(DoctorLeaveRequest::getDoctorId, doctorId);
        if (status != null) {
            q.eq(DoctorLeaveRequest::getStatus, status);
        }
        if (startDate != null) {
            q.ge(DoctorLeaveRequest::getApplyTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            q.le(DoctorLeaveRequest::getApplyTime, endDate.atTime(23,59,59));
        }
        q.orderByDesc(DoctorLeaveRequest::getApplyTime);
        IPage<DoctorLeaveRequest> result = leaveRequestService.page(page, q);
        return Result.OK(result);
    }

    @Operation(summary = "医生撤销请假申请")
    @PostMapping("/cancel")
    public Result<Boolean> cancel(@RequestParam Long leaveId) {
        DoctorLeaveRequest leaveRequest = leaveRequestService.getById(leaveId);
        if (leaveRequest == null) {
            return Result.error("请假申请不存在");
        }
        if (leaveRequest.getStatus() != 1) {
            return Result.error("只能撤销待审批状态的申请");
        }
        leaveRequest.setStatus(4); // 已撤销
        boolean ok = leaveRequestService.updateById(leaveRequest);
        return ok ? Result.OK(true) : Result.error("撤销失败");
    }

    public static class ApplyRequest {
        private Long doctorId;
        private String doctorName;
        private Long deptId;
        private String deptName;
        private String leaveType;
        private String startDate;
        private String endDate;
        private String reason;

        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

        public String getDoctorName() { return doctorName; }
        public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }

        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }

        public String getLeaveType() { return leaveType; }
        public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}

