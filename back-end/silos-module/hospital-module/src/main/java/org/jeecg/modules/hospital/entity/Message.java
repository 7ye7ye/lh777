package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户消息记录实体类
 */
@Data // Lombok注解，自动生成getter/setter等方法
@TableName("message_log") // 指定该实体类对应数据库中的 message_log 表
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "message_id", type = IdType.AUTO) // 声明这是主键，并且是自增的
    private Integer messageId;

    private String userId;

    private String appointmentId;

    private String messageType;

    private String title;

    private String content; // 数据库是JSON类型，这里可以用String接收，后续再处理

    private LocalDateTime createdTime;

    private Boolean isRead;
}