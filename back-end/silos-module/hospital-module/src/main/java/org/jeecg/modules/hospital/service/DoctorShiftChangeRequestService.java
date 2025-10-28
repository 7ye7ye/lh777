package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest;

/**
 * 医生调班申请服务接口
 */
public interface DoctorShiftChangeRequestService extends IService<DoctorShiftChangeRequest> {
    
    /**
     * 提交调班申请
     */
    boolean submitAdjustment(DoctorShiftChangeRequest request);
    
    /**
     * 审批调班申请
     */
    boolean approveAdjustment(Long adjustmentId, Integer status, String rejectReason, Long adminId);
}