package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.hospital.entity.RegistrationRecord;

import java.time.LocalDateTime;

@Mapper
public interface RegistrationRecordMapper extends BaseMapper<RegistrationRecord> {

    @Update("UPDATE registration_record SET status = #{status}, visit_time = #{visitTime} WHERE record_id = #{recordId}")
    int updateStatusAndVisitTime(@Param("recordId") Long recordId,
                                 @Param("status") Integer status,
                                 @Param("visitTime") java.time.LocalDateTime visitTime);
    @org.apache.ibatis.annotations.Select("SELECT * FROM registration_record WHERE registration_no = #{registrationNo} LIMIT 1")
    org.jeecg.modules.hospital.entity.RegistrationRecord selectByRegistrationNo(@Param("registrationNo") String registrationNo);
}