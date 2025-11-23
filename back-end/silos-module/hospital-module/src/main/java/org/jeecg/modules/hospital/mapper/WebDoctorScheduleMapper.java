// org.jeecg.modules.hospital.common.mapper.WebDoctorScheduleMapper.java
package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.hospital.dto.WebTodayScheduleDTO;
import org.jeecg.modules.hospital.entity.WebDoctorSchedule;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
 * 医生排班Mapper接口
 */
@DS("hospital")
@Mapper
public interface WebDoctorScheduleMapper extends BaseMapper<WebDoctorSchedule> {

    /**
     * 查询今日排班列表
     */
    List<WebTodayScheduleDTO> selectTodaySchedules(@Param("queryDate") Date queryDate,
                                                   @Param("deptId") Long deptId,
                                                   @Param("doctorId") Long doctorId,
                                                   @Param("timeSlot") Integer timeSlot,
                                                   @Param("keyword") String keyword);
}