package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.jeecg.modules.hospital.entity.VerificationAuditLog;
import org.jeecg.modules.hospital.mapper.VerificationAuditLogMapper;
import org.jeecg.modules.hospital.service.VerificationAuditLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 身份验证审核日志服务实现类
 */
@Service
@DS("hospital")
public class VerificationAuditLogServiceImpl extends ServiceImpl<VerificationAuditLogMapper, VerificationAuditLog> 
        implements VerificationAuditLogService {
    
    @Override
    public List<VerificationAuditLog> listByPatientId(Long patientId) {
        LambdaQueryWrapper<VerificationAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerificationAuditLog::getPatientId, patientId)
               .orderByDesc(VerificationAuditLog::getCreateTime);
        return this.list(wrapper);
    }
}
