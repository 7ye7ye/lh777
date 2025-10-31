package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.hospital.entity.DoctorSchedule;

@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {
    // 如需自定义SQL，可在此追加 @Select/@Update 方法
}