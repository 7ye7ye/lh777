package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.*;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.entity.RegistrationRecord;
import org.jeecg.modules.hospital.entity.RegistrationType;

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
            "  WHEN 1 THEN '上午' " +
            "  WHEN 2 THEN '下午' " +
            "  WHEN 3 THEN '晚上' " +
            "  ELSE '未知' " +
            "END AS time_range, " +
            "s.type_id, " +
            "s.used_quota, " +
            "COALESCE(rt.daily_quota, 0) AS daily_quota, " +
            "COALESCE(rt.daily_quota, 0) - s.used_quota AS available_quota, " +
            "s.status " +
            "FROM doctor_schedule s " +
            "LEFT JOIN registration_type rt ON s.type_id = rt.type_id " +
            "WHERE s.doctor_id = #{doctorId} " +
            "AND s.schedule_date >= #{startDate} " +
            "AND s.schedule_date <= DATE_ADD(#{startDate}, INTERVAL #{days} DAY) " +
            "AND s.status = 1 " +
            "ORDER BY s.schedule_date ASC, s.time_slot ASC")
    List<Map<String, Object>> selectSchedulesByDoctor(@Param("doctorId") Long doctorId,
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





}
