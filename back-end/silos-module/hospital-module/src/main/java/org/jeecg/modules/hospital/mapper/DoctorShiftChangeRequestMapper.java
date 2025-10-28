package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest;

/**
 * 医生调班申请Mapper
 */
@DS("hospital")
@Mapper
public interface DoctorShiftChangeRequestMapper extends BaseMapper<DoctorShiftChangeRequest> {
}