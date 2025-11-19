package org.jeecg.modules.hospital.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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
}
