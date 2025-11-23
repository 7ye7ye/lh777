package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.DoctorLeaveRequest;
import org.jeecg.modules.hospital.mapper.DoctorLeaveRequestMapper;
import org.jeecg.modules.hospital.service.DoctorLeaveRequestService;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;

import java.time.LocalDateTime;
import jakarta.annotation.PostConstruct;

/**
 * 医生请假申请服务实现
 */
@Service
@DS("hospital")
public class DoctorLeaveRequestServiceImpl 
    extends ServiceImpl<DoctorLeaveRequestMapper, DoctorLeaveRequest>
    implements DoctorLeaveRequestService {

    @PostConstruct
    public void ensureTableExists() {
        try { this.baseMapper.ensureTable(); } catch (Exception ignored) {}
    }

    @Override
    public boolean submitLeaveRequest(DoctorLeaveRequest request) {
        ensureTableExists();
        request.setApplyTime(LocalDateTime.now());
        request.setStatus(1); // 待审批
        return this.save(request);
    }

    @Override
    public boolean approveLeaveRequest(Long leaveId, Integer status, String rejectReason, Long adminId) {
        DoctorLeaveRequest leaveRequest = this.getById(leaveId);
        if (leaveRequest == null || leaveRequest.getStatus() != 1) {
            return false;
        }
        
        leaveRequest.setStatus(status);
        leaveRequest.setApproveTime(LocalDateTime.now());
        leaveRequest.setAdminId(adminId);
        
        if (status == 3 && rejectReason != null) {
            leaveRequest.setRejectReason(rejectReason);
        }
        
        return this.updateById(leaveRequest);
    }
}

