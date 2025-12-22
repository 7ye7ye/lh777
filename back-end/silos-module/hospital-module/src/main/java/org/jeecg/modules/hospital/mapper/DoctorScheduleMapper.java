package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import com.baomidou.dynamic.datasource.annotation.DS;

@DS("hospital")
@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {
    // 如需自定义SQL，可在此追加 @Select/@Update 方法
    
    /**
     * 自定义插入方法，确保time_slot字段被包含
     * 注意：方法名不能与BaseMapper的方法冲突
     */
    @Insert({
        "INSERT INTO doctor_schedule",
        "(doctor_id, dept_id, schedule_date, time_slot, used_quota, max_quota, room_number, status, create_time, update_time)",
        "VALUES",
        "(#{doctorId}, #{deptId}, #{scheduleDate}, #{timeSlot}, #{usedQuota}, #{maxQuota}, #{roomNumber}, #{status}, #{createTime}, #{updateTime})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "scheduleId", keyColumn = "schedule_id")
    int insertSchedule(DoctorSchedule schedule);
}