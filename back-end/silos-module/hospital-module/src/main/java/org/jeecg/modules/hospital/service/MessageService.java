package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.entity.Message;
import java.util.List;

/**
 * 消息记录业务逻辑接口
 */
public interface MessageService extends IService<Message> {
    
    /**
     * 根据用户ID获取消息列表
     * @param userId 用户ID
     * @return 消息列表
     */
    List<Message> listMessagesByUserId(String userId);
    
    /**
     * 根据预约ID获取消息列表
     * @param appointmentId 预约ID
     * @return 消息列表
     */
    List<Message> listMessagesByAppointmentId(String appointmentId);
    
    /**
     * 根据消息ID获取单条消息详情
     * @param messageId 消息ID
     * @return 单条消息
     */
    Message getMessageById(Integer messageId);
}