package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.hospital.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
    /**
     * 统计患者在科室的预约次数
     * @param patientId 患者ID
     * @param departmentName 科室名称
     * @return 预约次数
     */
    int countByPatientAndDepartment(@Param("patientId") String patientId, @Param("departmentName") String departmentName);
}