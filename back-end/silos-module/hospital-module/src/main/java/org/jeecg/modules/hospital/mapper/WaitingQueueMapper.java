package org.jeecg.modules.hospital.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.*;
import org.jeecg.modules.hospital.entity.RegistrationRecord;
import org.jeecg.modules.hospital.entity.WaitingQueue;

/**
 * 挂号相关的 Mapper，绑定 hospital 数据源
 */
@Mapper
@DS("hospital")  // ✅ 指定使用 hospital 数据源
public interface WaitingQueueMapper {

    // 插入候补记录
    @Insert("INSERT INTO waiting_queue(schedule_id, patient_id, record_id, queue_rank, queue_time, status) " +
            "VALUES(#{scheduleId}, #{patientId}, #{recordId}, #{queueRank}, #{queueTime}, #{status})")
    int insert(WaitingQueue queue);

    // 查询某个排班下最大候补排名
    @Select("SELECT MAX(queue_rank) FROM waiting_queue WHERE schedule_id = #{scheduleId}")
    Integer selectMaxQueueRank(@Param("scheduleId") Long scheduleId);

    // 检查患者是否已在候补队列（等待中状态 status=0）
    @Select("SELECT COUNT(*) FROM waiting_queue " +
            "WHERE schedule_id = #{scheduleId} AND patient_id = #{patientId} AND status = 0")
    int countExistingQueue(@Param("scheduleId") Long scheduleId, @Param("patientId") Long patientId);

    /**
     * 查询某排班下最靠前的候补记录（状态 = 0）
     * @param scheduleId 排班ID
     * @return 排在最前面的候补记录（若无返回 null）
     */
    @Select("SELECT * FROM waiting_queue " +
            "WHERE schedule_id = #{scheduleId} AND status = 0 " +
            "ORDER BY queue_rank ASC " +
            "LIMIT 1")
    WaitingQueue selectFirstWaiting(@Param("scheduleId") Long scheduleId);

    /**
     * 更新候补队列记录（可用于修改状态、排班或其他字段）
     * @param queue 候补记录对象，必须包含 queueId
     * @return 更新行数
     */
    @Update("UPDATE waiting_queue " +
            "SET schedule_id = #{scheduleId}, " +
            "patient_id = #{patientId}, " +
            "record_id = #{recordId}, " +
            "queue_rank = #{queueRank}, " +
            "queue_time = #{queueTime}, " +
            "status = #{status} " +
            "WHERE queue_id = #{queueId}")
    int updateById(WaitingQueue queue);

    /**
     * 查询某排班下最靠前的候补记录（状态 = 0：等待中）
     * @param scheduleId 排班ID
     * @return 排在最前面的候补记录（若无返回 null）
     */
    @Select("SELECT * FROM waiting_queue " +
            "WHERE schedule_id = #{scheduleId} AND status = 0 " +
            "ORDER BY queue_rank ASC " +
            "LIMIT 1")
    WaitingQueue selectFirstByScheduleId(@Param("scheduleId") Long scheduleId);


    /**
     * 查询某排班下最靠前的候补记录（排除指定患者）
     * @param scheduleId 排班ID
     * @param excludePatientId 排除的患者ID（当前退号患者）
     * @return 候补记录对象（若无返回 null）
     */
    @Select("SELECT * FROM waiting_queue " +
            "WHERE schedule_id = #{scheduleId} AND status = 0 AND patient_id != #{excludePatientId} " +
            "ORDER BY queue_rank ASC " +
            "LIMIT 1")
    WaitingQueue selectFirstWaitingExcludingPatient(@Param("scheduleId") Long scheduleId,
                                                    @Param("excludePatientId") Long excludePatientId);

}
