package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.*;
import org.jeecg.modules.hospital.dto.RegistrationDetailDTO;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.entity.RegistrationRecord;
import org.jeecg.modules.hospital.entity.RegistrationType;
import org.jeecg.modules.hospital.vo.RegistrationVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 挂号相关的 Mapper，绑定 hospital 数据源
 */
@Mapper
@DS("hospital")  // ✅ 指定使用 hospital 数据源
public interface RegistrationMapper extends BaseMapper<RegistrationRecord> {

    /**
     * 查询所有挂号类型
     */
    @Select("SELECT type_id, type_name, price_original, student_price, staff_price, daily_quota, is_active " +
            "FROM registration_type " +
            "WHERE is_active = 1")
    List<RegistrationType> selectAllTypes();

    /**
     * 查询医生未来 N 天排班
     */
    @Select("SELECT s.schedule_id, " +
            "s.schedule_date, " +
            "s.time_slot, " +
            "CASE s.time_slot " +
            "    WHEN 1 THEN '上午' " +
            "    WHEN 2 THEN '下午' " +
            "    WHEN 3 THEN '晚上' " +
            "    ELSE '未知' " +
            "END AS time_range, " +
            "s.type_id, " +
            "rt.type_name, " +
            "d.title_id AS doctor_title_type_id, " +
            "rt_title.type_name AS doctor_title_type_name, " +
            "s.used_quota, " +
            "s.max_quota," +
            "s.max_quota - s.used_quota AS available_quota, " +
            "s.status " +
            "FROM doctor_schedule s " +
            "LEFT JOIN registration_type rt ON s.type_id = rt.type_id " +
            "LEFT JOIN doctor d ON s.doctor_id = d.doctor_id " +
            "LEFT JOIN registration_type rt_title ON d.title_id = rt_title.type_id " +
            "WHERE s.doctor_id = #{doctorId} " +
            "AND s.schedule_date >= #{startDate} " +
            "AND s.schedule_date <= DATE_ADD(#{startDate}, INTERVAL #{days} DAY) " +
            "AND s.status = 1 " +
            "ORDER BY s.schedule_date ASC, s.time_slot ASC")
    List<Map<String, Object>> selectSchedulesByDoctor(
            @Param("doctorId") Long doctorId,
            @Param("startDate") String startDate,
            @Param("days") Integer days);

    /**
     * 统计医生某天某挂号类型已预约数量
     */
    @Select("SELECT COUNT(*) FROM registration_record r " +
            "WHERE r.doctor_id = #{doctorId} " +
            "AND r.type_id = #{typeId} " +
            "AND DATE(r.visit_time) = DATE(#{visitTime})")
    Integer countByDoctorAndType(@Param("doctorId") Long doctorId,
                                 @Param("typeId") Long typeId,
                                 @Param("visitTime") String visitTime);

    /**
     * 根据 schedule_id 查询单个排班信息
     */
    @Select("SELECT schedule_id, doctor_id, dept_id, type_id, schedule_date, time_slot, used_quota, status, create_time, update_time " +
            "FROM doctor_schedule " +
            "WHERE schedule_id = #{scheduleId}")
    DoctorSchedule selectScheduleById(@Param("scheduleId") Long scheduleId);

    /**
     * 根据 type_id 查询挂号类型信息
     */
    @Select("SELECT type_id, type_name, price_original, student_price, staff_price, daily_quota, is_active " +
            "FROM registration_type " +
            "WHERE type_id = #{typeId}")
    RegistrationType selectTypeById(@Param("typeId") Long typeId);

    /**
     * 获取某排班最大候补排名
     */
    @Select("SELECT MAX(queue_rank) FROM waiting_queue WHERE schedule_id = #{scheduleId}")
    Integer selectMaxQueueRank(@Param("scheduleId") Long scheduleId);

    /**
     * 更新排班已用数量
     */
    @Update("UPDATE doctor_schedule " +
            "SET used_quota = #{usedQuota}, update_time = NOW() " +
            "WHERE schedule_id = #{scheduleId}")
    void updateScheduleUsedQuota(DoctorSchedule appointmentSchedule);
    /**
     * 插入挂号记录
     */
    @Insert("INSERT INTO registration_record (" +
            "schedule_id, patient_id, doctor_id, type_id, registration_no, register_time, status, price_original, actual_price, is_add" +
            ") VALUES (" +
            "#{scheduleId}, #{patientId}, #{doctorId}, #{typeId}, #{registrationNo}, #{registerTime}, #{status}, #{priceOriginal}, #{actualPrice}, #{isAdd}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "recordId", keyColumn = "record_id")
    void insertRegistration(RegistrationRecord record);
    /**
     * 检查患者是否对同一排班重复挂号（防止重复挂号）
     * @param patientId 患者ID
     * @param scheduleId 排班ID
     * @return 重复记录数量（>0 表示重复）
     */
    @Select("SELECT COUNT(*) " +
            "FROM registration_record r " +
            "WHERE r.patient_id = #{patientId} " +
            "AND r.schedule_id = #{scheduleId} " +
            "AND r.status != 3")  // 状态 3 代表取消
    Integer checkDuplicateBySchedule(@Param("patientId") Long patientId,
                                                 @Param("scheduleId") Long scheduleId);


    @Select("SELECT * FROM patient WHERE patient_id = #{patientId}")
    Patient selectPatientById(@Param("patientId") Long patientId);


    /**
     * 查询挂号详情（用于消息 / 回执）
     */
    @Select("""
            SELECT
                rr.record_id,
                rr.registration_no,
                rr.patient_id,
                p.patient_name,
                p.outpatient_number AS patient_card_no,
                p.user_id AS patient_user_id,
                rr.doctor_id,
                d.doctor_name,
                d.title AS doctor_title,
                ds.dept_id,
                dep.dept_name AS department_name,
                dep.location AS dept_location,
                ds.schedule_date,
                ds.time_slot,
                rr.register_time AS register_time,
                rt.type_name,
                COALESCE(rr.price_original, rt.price_original) AS price_original,
                COALESCE(rr.actual_price, rt.price_original) AS actual_price,
                rr.status
            FROM registration_record rr
            LEFT JOIN patient p ON rr.patient_id = p.patient_id
            LEFT JOIN doctor_schedule ds ON rr.schedule_id = ds.schedule_id
            LEFT JOIN department dep ON ds.dept_id = dep.dept_id
            LEFT JOIN doctor d ON rr.doctor_id = d.doctor_id
            LEFT JOIN registration_type rt ON rr.type_id = rt.type_id
            WHERE rr.record_id = #{recordId}
            """)
    RegistrationDetailDTO selectRegistrationDetail(@Param("recordId") Long recordId);

    /**
     * 取消挂号（患者主动取消）
     * @param recordId 挂号记录ID
     * @param reason 取消原因
     */
    @Update("UPDATE registration_record " +
            "SET status = 3, cancel_time = NOW(), cancel_reason = #{reason} " +
            "WHERE record_id = #{recordId}")
    int cancelRegistration(@Param("recordId") Long recordId,
                           @Param("reason") String reason);



    @Select("""
            SELECT rr.record_id,
                   rr.registration_no,
                   p.patient_name,
                   d.doctor_name,
                   dep.dept_name AS department_name,
                   rt.type_name,
                   rr.actual_price,
                   ds.schedule_date AS appointment_date,
                   ds.time_slot,
                   rr.consult_room,
                   rr.status
            FROM registration_record rr
            LEFT JOIN patient p ON rr.patient_id = p.patient_id
            LEFT JOIN doctor_schedule ds ON rr.schedule_id = ds.schedule_id
            LEFT JOIN department dep ON ds.dept_id = dep.dept_id
            LEFT JOIN doctor d ON rr.doctor_id = d.doctor_id
            LEFT JOIN registration_type rt ON rr.type_id = rt.type_id
            WHERE ds.dept_id = #{deptId}
            ORDER BY ds.schedule_date DESC, rr.record_id DESC
            """)
    List<RegistrationVO> getByDepartment(@Param("deptId") Long deptId);

    @Select("""
            SELECT rr.record_id,
                   rr.registration_no,
                   p.patient_name,
                   d.doctor_name,
                   dep.dept_name AS department_name,
                   rt.type_name,
                   rr.actual_price,
                   ds.schedule_date AS appointment_date,
                   ds.time_slot,
                   rr.consult_room,
                   rr.status
            FROM registration_record rr
            LEFT JOIN patient p ON rr.patient_id = p.patient_id
            LEFT JOIN doctor_schedule ds ON rr.schedule_id = ds.schedule_id
            LEFT JOIN department dep ON ds.dept_id = dep.dept_id
            LEFT JOIN doctor d ON rr.doctor_id = d.doctor_id
            LEFT JOIN registration_type rt ON rr.type_id = rt.type_id
            WHERE d.specialty LIKE CONCAT('%', #{disease}, '%')
               OR rt.type_name LIKE CONCAT('%', #{disease}, '%')
            ORDER BY ds.schedule_date DESC, rr.record_id DESC
            """)
    List<RegistrationVO> getByDisease(@Param("disease") String disease);

    /**
     * 查询指定日期的挂号记录
     */
    @Select("""
            SELECT
                rr.record_id,
                rr.registration_no,
                rr.patient_id,
                p.patient_name,
                p.outpatient_number AS patient_card_no,
                p.user_id AS patient_user_id,
                rr.doctor_id,
                d.doctor_name,
                d.title AS doctor_title,
                ds.dept_id,
                dep.dept_name AS department_name,
                dep.location AS dept_location,
                ds.schedule_date,
                ds.time_slot,
                rr.register_time AS register_time,
                rt.type_name,
                COALESCE(rr.price_original, rt.price_original) AS price_original,
                COALESCE(rr.actual_price, rt.price_original) AS actual_price,
                rr.status
            FROM registration_record rr
            LEFT JOIN patient p ON rr.patient_id = p.patient_id
            LEFT JOIN doctor_schedule ds ON rr.schedule_id = ds.schedule_id
            LEFT JOIN department dep ON ds.dept_id = dep.dept_id
            LEFT JOIN doctor d ON rr.doctor_id = d.doctor_id
            LEFT JOIN registration_type rt ON rr.type_id = rt.type_id
            WHERE ds.schedule_date = #{targetDate}
              AND rr.status = 1
            """)
    List<RegistrationDetailDTO> listRegistrationsByDate(@Param("targetDate") LocalDate targetDate);

    /**
     * 查询下一小时内即将就诊的挂号记录
     */
    @Select("""
            SELECT
                rr.record_id,
                rr.registration_no,
                rr.patient_id,
                p.patient_name,
                p.outpatient_number AS patient_card_no,
                p.user_id AS patient_user_id,
                rr.doctor_id,
                d.doctor_name,
                d.title AS doctor_title,
                ds.dept_id,
                dep.dept_name AS department_name,
                dep.location AS dept_location,
                ds.schedule_date,
                ds.time_slot,
                rr.register_time AS register_time,
                rt.type_name,
                COALESCE(rr.price_original, rt.price_original) AS price_original,
                COALESCE(rr.actual_price, rt.price_original) AS actual_price,
                rr.status
            FROM registration_record rr
            LEFT JOIN patient p ON rr.patient_id = p.patient_id
            LEFT JOIN doctor_schedule ds ON rr.schedule_id = ds.schedule_id
            LEFT JOIN department dep ON ds.dept_id = dep.dept_id
            LEFT JOIN doctor d ON rr.doctor_id = d.doctor_id
            LEFT JOIN registration_type rt ON rr.type_id = rt.type_id
            WHERE ds.schedule_date = #{targetDate}
              AND rr.status = 1
              AND ds.time_slot = #{timeSlot}
            """)
    List<RegistrationDetailDTO> listRegistrationsByDateAndSlot(@Param("targetDate") LocalDate targetDate,
                                                               @Param("timeSlot") Integer timeSlot);

    /**
     * 根据用户ID和预约日期查询挂号记录
     */
    @Select("""
            SELECT
                rr.record_id,
                rr.registration_no,
                rr.patient_id,
                p.patient_name,
                p.outpatient_number AS patient_card_no,
                p.user_id AS patient_user_id,
                rr.doctor_id,
                d.doctor_name,
                d.title AS doctor_title,
                ds.dept_id,
                dep.dept_name AS department_name,
                dep.location AS dept_location,
                ds.schedule_date,
                ds.time_slot,
                rr.register_time AS register_time,
                rt.type_name,
                COALESCE(rr.price_original, rt.price_original) AS price_original,
                COALESCE(rr.actual_price, rt.price_original) AS actual_price,
                rr.status
            FROM registration_record rr
            LEFT JOIN patient p ON rr.patient_id = p.patient_id
            LEFT JOIN doctor_schedule ds ON rr.schedule_id = ds.schedule_id
            LEFT JOIN department dep ON ds.dept_id = dep.dept_id
            LEFT JOIN doctor d ON rr.doctor_id = d.doctor_id
            LEFT JOIN registration_type rt ON rr.type_id = rt.type_id
            WHERE p.user_id = #{userId}
              AND ds.schedule_date = #{targetDate}
              AND rr.status = 1
            """)
    List<RegistrationDetailDTO> listRegistrationsByUserIdAndDate(@Param("userId") Long userId, 
                                                                 @Param("targetDate") LocalDate targetDate);
}
