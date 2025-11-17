package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.hospital.entity.RegistrationRecord;

import java.time.LocalDateTime;

// RegistrationRecordMapper 接口
import com.baomidou.dynamic.datasource.annotation.DS;

// 该文件的接口声明
@DS("hospital")
@Mapper
public interface RegistrationRecordMapper extends BaseMapper<RegistrationRecord> {

    @Update("UPDATE registration_record SET status = #{status}, visit_time = #{visitTime} WHERE record_id = #{recordId}")
    int updateStatusAndVisitTime(@Param("recordId") Long recordId,
                                 @Param("status") Integer status,
                                 @Param("visitTime") java.time.LocalDateTime visitTime);

    @org.apache.ibatis.annotations.Select("SELECT * FROM registration_record WHERE registration_no = #{registrationNo} LIMIT 1")
    org.jeecg.modules.hospital.entity.RegistrationRecord selectByRegistrationNo(@Param("registrationNo") String registrationNo);

    // 新增：按排班统计有效挂号人数（候补/已预约/已就诊）
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM registration_record WHERE schedule_id = #{scheduleId} AND status IN (0,1,2)")
    int countActiveByScheduleId(@Param("scheduleId") Long scheduleId);
}