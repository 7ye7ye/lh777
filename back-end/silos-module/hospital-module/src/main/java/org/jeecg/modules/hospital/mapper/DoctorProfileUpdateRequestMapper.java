package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.hospital.entity.DoctorProfileUpdateRequest;

/**
 * 医生资料修改申请表 Mapper
 */
@Mapper
public interface DoctorProfileUpdateRequestMapper extends BaseMapper<DoctorProfileUpdateRequest> {
}
