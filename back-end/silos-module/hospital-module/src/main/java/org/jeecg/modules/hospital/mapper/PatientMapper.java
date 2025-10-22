package org.jeecg.modules.hospital.mapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.hospital.entity.Patient;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author Administrator
 * @description 针对表【patient(患者表)】的数据库操作Mapper
 * @createDate 2025-09-22 20:15:21
 * @Entity org.jeecg.modules.hospital.entity.Patient
 */
@DS("hospital")
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

    @org.apache.ibatis.annotations.Select({
            "<script>",
            "SELECT ",
            "   p.patient_id AS patientId,",
            "   u.user_account AS name,",
            "   p.gender AS gender,",
            "   p.birth_date AS birthDate,",
            "   u.phone AS phone",
            "FROM patient p",
            "JOIN hos_user u ON p.user_id = u.user_id",
            "<where>",
            "   <if test='keyword != null and keyword != \"\"'>",
            "      AND (",
            "         u.user_account LIKE CONCAT('%', #{keyword}, '%')",
            "         OR u.phone LIKE CONCAT('%', #{keyword}, '%')",
            "      )",
            "   </if>",
            "   <if test='startDate != null'> AND p.birth_date &gt;= #{startDate} </if>",
            "   <if test='endDate != null'> AND p.birth_date &lt;= #{endDate} </if>",
            "</where>",
            "ORDER BY p.patient_id DESC",
            "</script>"
    })
    java.util.List<org.jeecg.modules.hospital.vo.PatientBriefVO> selectBriefPatients(
            @org.apache.ibatis.annotations.Param("keyword") String keyword,
            @org.apache.ibatis.annotations.Param("startDate") java.time.LocalDate startDate,
            @org.apache.ibatis.annotations.Param("endDate") java.time.LocalDate endDate
    );
}