package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.hospital.entity.VerificationAuditLog;

/**
 * 身份验证审核日志Mapper接口
 */
@Mapper
public interface VerificationAuditLogMapper extends BaseMapper<VerificationAuditLog> {
}
