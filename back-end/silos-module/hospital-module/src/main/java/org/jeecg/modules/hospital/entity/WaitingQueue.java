package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("waiting_queue")
public class WaitingQueue implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long queueId; // 队列ID
    private Long scheduleId; // 排班ID
    private Long patientId; // 患者ID
    private Long recordId; // 挂号记录ID
    private Integer queueRank; // 候补排名
    private LocalDateTime queueTime; // 加入队列时间
    private Integer status; // 队列状态（0-等待；1-转正；2-放弃；3-过期）
    private LocalDateTime transferTime; // 转正时间

    public Long getRegistrationId() {
        return recordId;
    }
}