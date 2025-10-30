package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest;
import org.jeecg.modules.hospital.mapper.DoctorShiftChangeRequestMapper;
import org.jeecg.modules.hospital.service.DoctorShiftChangeRequestService;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;

import java.time.LocalDateTime;

/**
 * 医生调班申请服务实现
 */
@Service
@DS("hospital")
public class DoctorShiftChangeRequestServiceImpl 
    extends ServiceImpl<DoctorShiftChangeRequestMapper, DoctorShiftChangeRequest>
    implements DoctorShiftChangeRequestService {

    @Override
    public boolean submitAdjustment(DoctorShiftChangeRequest request) {
        request.setApplyTime(LocalDateTime.now());
        request.setStatus(1); // 待审批
        return this.save(request);
    }

    @Override
    public boolean approveAdjustment(Long adjustmentId, Integer status, String rejectReason, Long adminId) {
        DoctorShiftChangeRequest adjustment = this.getById(adjustmentId);
        if (adjustment == null || adjustment.getStatus() != 1) {
            return false;
        }
        
        adjustment.setStatus(status);
        adjustment.setApproveTime(LocalDateTime.now());
        adjustment.setAdminId(adminId);
        
        if (status == 3 && rejectReason != null) {
            adjustment.setRejectReason(rejectReason);
        }
        
        return this.updateById(adjustment);
    }
}