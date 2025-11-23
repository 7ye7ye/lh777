package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.entity.DoctorLeaveRequest;

/**
 * 医生请假申请服务接口
 */
public interface DoctorLeaveRequestService extends IService<DoctorLeaveRequest> {
    
    /**
     * 提交请假申请
     */
    boolean submitLeaveRequest(DoctorLeaveRequest request);
    
    /**
     * 审批请假申请
     */
    boolean approveLeaveRequest(Long leaveId, Integer status, String rejectReason, Long adminId);
}

