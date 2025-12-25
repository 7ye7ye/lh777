package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.entity.VerificationAuditLog;

import java.util.List;

/**
 * 身份验证审核日志服务接口
 */
public interface VerificationAuditLogService extends IService<VerificationAuditLog> {
    
    /**
     * 根据患者ID获取审核日志列表
     * 
     * @param patientId 患者ID
     * @return 审核日志列表
     */
    List<VerificationAuditLog> listByPatientId(Long patientId);
}
