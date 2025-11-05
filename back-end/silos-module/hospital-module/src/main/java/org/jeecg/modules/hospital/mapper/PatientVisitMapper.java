package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.hospital.entity.PatientVisit;
import org.jeecg.modules.hospital.vo.PatientBriefVO;

import java.time.LocalDate;
import java.util.List;

// PatientVisitMapper 接口
import com.baomidou.dynamic.datasource.annotation.DS;

@DS("hospital")
@org.apache.ibatis.annotations.Mapper
public interface PatientVisitMapper extends BaseMapper<org.jeecg.modules.hospital.entity.PatientVisit> {

    @org.apache.ibatis.annotations.Select({
            "<script>",
            "SELECT ",
            " p.patient_id AS patientId,",
            " u.user_account AS name,",
            " p.gender AS gender,",
            " p.phone AS phone,",
            " MAX(v.visit_date) AS lastVisitDate,",
            " MAX(v.status) AS lastVisitStatus,",
            " COUNT(v.visit_id) AS visitCount",
            " FROM patient p",
            " JOIN patient_visit v ON v.patient_id = p.patient_id",
            " JOIN hos_user u ON p.user_id = u.user_id",
            " <where>",
            "  <if test='doctorId != null'> AND v.doctor_id = #{doctorId} </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (",
            "      u.user_account LIKE CONCAT('%', #{keyword}, '%')",
            "      OR p.phone LIKE CONCAT('%', #{keyword}, '%')",
            "    )",
            "  </if>",
            "  <if test='startDate != null'> AND v.visit_date &gt;= #{startDate} </if>",
            "  <if test='endDate != null'> AND v.visit_date &lt;= #{endDate} </if>",
            "  <if test='status != null'> AND v.status = #{status} </if>",
            " </where>",
            " GROUP BY p.patient_id, u.user_account, p.gender, p.phone",
            " ORDER BY MAX(v.visit_date) DESC",
            "</script>"
    })
    List<PatientBriefVO> selectPatientsForDoctor(
            @Param("doctorId") Long doctorId,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") Integer status
    );

    @Select("SELECT * FROM patient_visit WHERE schedule_id = #{scheduleId} AND patient_id = #{patientId} LIMIT 1")
    PatientVisit findByScheduleAndPatient(@Param("scheduleId") Long scheduleId, @Param("patientId") Long patientId);

    @org.apache.ibatis.annotations.Update("UPDATE patient_visit SET status = #{status}, update_time = NOW() WHERE visit_id = #{visitId}")
    int updateStatus(@Param("visitId") Long visitId, @Param("status") Integer status);
}