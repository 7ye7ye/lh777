package org.jeecg.modules.hospital.mapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
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
            "   p.patient_name AS name,",
            "   p.gender AS gender,",
            "   p.birth_date AS birthDate,",
            "   p.phone AS phone,",
            "   CASE ",
            "       WHEN p.patient_type = 1 THEN '学生'",
            "       WHEN p.patient_type = 2 THEN '教师'", 
            "       WHEN p.patient_type = 3 THEN '职工'",
            "       ELSE '其他'",
            "   END AS identity,",
            "   '08:00-12:00' AS appointmentTimeRange,",
            "   '已预约' AS statusText,",
            "   'status-wait' AS statusClass,",
            "   (p.patient_id * 100 + 1) AS appointmentId",
            "FROM patient p",
            "JOIN hos_user u ON p.user_id = u.user_id",
            "<where>",
            "   <if test='keyword != null and keyword != \"\"'>",
            "      AND (",
            "         p.patient_name LIKE CONCAT('%', #{keyword}, '%')",
            "         OR p.phone LIKE CONCAT('%', #{keyword}, '%')",
            "      )",
            "   </if>",
            "   <!-- 移除日期过滤：startDate和endDate应该用于预约日期而不是出生日期 -->",
            "   <!-- TODO: 当有预约表时，应该根据预约日期进行过滤 -->",
            "</where>",
            "ORDER BY p.patient_id DESC",
            "</script>"
    })
    java.util.List<org.jeecg.modules.hospital.vo.PatientBriefVO> selectBriefPatients(
            @org.apache.ibatis.annotations.Param("keyword") String keyword,
            @org.apache.ibatis.annotations.Param("startDate") java.time.LocalDate startDate,
            @org.apache.ibatis.annotations.Param("endDate") java.time.LocalDate endDate
    );

    // 新增：基于 registration_record + doctor_schedule 的按医生与日期查询
    @org.apache.ibatis.annotations.Select({
            "<script>",
            "SELECT ",
            " rr.record_id AS appointmentId,",
            " p.patient_id AS patientId,",
            " p.patient_name AS name,",
            " p.gender AS gender,",
            " p.birth_date AS birthDate,",
            " p.phone AS phone,",
            " CASE ",
            "   WHEN p.patient_type = 1 THEN '学生'",
            "   WHEN p.patient_type = 2 THEN '教师'",
            "   WHEN p.patient_type = 3 THEN '职工'",
            "   ELSE '其他'",
            " END AS identity,",
            " CASE ds.time_slot",
            "   WHEN 1 THEN '08:00-12:00'",
            "   WHEN 2 THEN '14:00-17:00'",
            "   WHEN 3 THEN '18:00-20:00'",
            "   ELSE '全部'",
            " END AS appointmentTimeRange,",
            " CASE rr.status",
            "   WHEN 0 THEN '候补'",
            "   WHEN 1 THEN '待接诊'",
            "   WHEN 5 THEN '进行中'",
            "   WHEN 2 THEN '已完成'",
            "   WHEN 6 THEN '已转诊'",
            "   WHEN 3 THEN '已退号'",
            "   WHEN 4 THEN '已取消'",
            "   ELSE '未知'",
            " END AS statusText,",
            " CASE rr.status",
            "   WHEN 5 THEN 'status-progress'",
            "   WHEN 2 THEN 'status-done'",
            "   WHEN 6 THEN 'status-referral'",
            "   WHEN 3 THEN 'status-cancel'",
            "   WHEN 4 THEN 'status-cancel'",
            "   ELSE 'status-wait'",
            " END AS statusClass",
            "FROM registration_record rr",
            "JOIN patient p ON rr.patient_id = p.patient_id",
            "JOIN hos_user u ON p.user_id = u.user_id",
            "JOIN doctor_schedule ds ON rr.schedule_id = ds.schedule_id",
            "<where>",
            "  rr.doctor_id = #{doctorId}",
            "  AND ds.schedule_date = #{date}",
            "  <if test='status != null'> AND rr.status = #{status} </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (",
            "      p.patient_name LIKE CONCAT('%', #{keyword}, '%')",
            "      OR p.phone LIKE CONCAT('%', #{keyword}, '%')",
            "    )",
            "  </if>",
            "</where>",
            "ORDER BY rr.register_time ASC",
            "</script>"
    })
    java.util.List<org.jeecg.modules.hospital.vo.PatientBriefVO> selectPatientsByRegistration(
            @org.apache.ibatis.annotations.Param("doctorId") Long doctorId,
            @org.apache.ibatis.annotations.Param("date") java.time.LocalDate date,
            @org.apache.ibatis.annotations.Param("status") Integer status,
            @org.apache.ibatis.annotations.Param("keyword") String keyword
    );

    @org.apache.ibatis.annotations.Select({
        "SELECT ",
        "  p.patient_id AS patientId,",
        "  p.user_id AS userId,",
        "  p.patient_type AS patientType,",
        "  p.student_id AS studentId,",
        "  p.staff_id AS staffId,",
        "  p.birth_date AS birthDate,",
        "  p.gender AS gender,",
        "  p.height AS height,",
        "  p.weight AS weight,",
        "  p.blood_type AS bloodType,",
        "  p.marital_status AS maritalStatus,",
        "  p.fertility_status AS fertilityStatus,",
        "  p.present_illness AS presentIllness,",
        "  p.past_illness AS pastIllness,",
        "  p.family_illness AS familyIllness,",
        "  p.allergy_history AS allergyHistory,",
        "  p.id_type AS idType,",
        "  p.id_card AS idCard,",
        "  p.nation AS nation,",
        "  p.nationality AS nationality,",
        "  p.region AS region,",
        "  p.detailed_address AS detailedAddress,",
        "  p.phone AS phone,",
        "  p.home_address AS homeAddress,",
        "  p.emergency_contact AS emergencyContact,",
        "  p.emergency_phone AS emergencyPhone,",
        "  p.medical_history AS medicalHistory,",
        "  p.identity_verify AS identityVerify,",
        "  p.verify_time AS verifyTime,",
        "  p.outpatient_number AS outpatientNumber,",
        "  p.hospitalization_number AS hospitalizationNumber,",
        "  p.barcode_info AS barcodeInfo,",
        "  p.qr_code_info AS qrCodeInfo,",
        "  p.patient_name AS patientName",  // 关键：映射成驼峰属性
        "FROM patient p",
        "WHERE p.patient_id = #{patientId}"
    })
    org.jeecg.modules.hospital.entity.Patient selectDetailById(
        @org.apache.ibatis.annotations.Param("patientId") Long patientId
    );

    // 根据预约记录ID查询预约详情：时段/科室/医生
    @org.apache.ibatis.annotations.Select({
        "SELECT",
        " rr.record_id AS appointmentId,",
        " rr.status AS status,",
        " CONCAT(DATE_FORMAT(ds.schedule_date, '%Y-%m-%d'), ' ',",
        "   CASE ds.time_slot",
        "     WHEN 1 THEN '08:00-12:00'",
        "     WHEN 2 THEN '14:00-17:00'",
        "     WHEN 3 THEN '18:00-20:00'",
        "     ELSE '全部'",
        "   END",
        " ) AS appointmentTime,",
        " d.dept_name AS department,",
        " doc.doctor_name AS doctor",
        " FROM registration_record rr",
        " JOIN doctor_schedule ds ON rr.schedule_id = ds.schedule_id",
        " JOIN department d ON ds.dept_id = d.dept_id",
        " JOIN doctor doc ON rr.doctor_id = doc.doctor_id",
        " WHERE rr.record_id = #{appointmentId}"
    })
    org.jeecg.modules.hospital.vo.AppointmentDetailVO selectAppointmentDetail(
        @org.apache.ibatis.annotations.Param("appointmentId") Long appointmentId
    );
}




