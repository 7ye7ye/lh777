package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.Message;
import org.jeecg.modules.hospital.mapper.MessageMapper;
import org.jeecg.modules.hospital.service.MessageService;
import org.springframework.stereotype.Service;
import java.util.List;
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 消息记录业务逻辑实现类
 */
@Service // 声明这是一个Spring的服务类
@DS("hospital") // 指定使用名为 "hospital" 的数据源
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    // 第一次加载的是所有预约信息的列表，所以传的是用户ID
    @Override
    public List<Message> listMessagesByUserId(String userId) {
        // 使用MyBatis-Plus的查询构造器
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        // 查询条件：userId 等于传入的 userId
        queryWrapper.eq(Message::getUserId, userId);
        // 排序条件：按创建时间降序排列 (最新的在最前面)
        queryWrapper.orderByDesc(Message::getCreatedTime);
        
        // baseMapper 是 ServiceImpl 自带的，可以直接用
        return baseMapper.selectList(queryWrapper);
    }
    // 第二次加载的是某个特定预约的情况信息
    @Override
    public List<Message> listMessagesByAppointmentId(String appointmentId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        // 查询条件变更为按 appointment_id 查询
        queryWrapper.eq(Message::getAppointmentId, appointmentId);
        // 仍然按时间降序
        queryWrapper.orderByDesc(Message::getCreatedTime);
        return baseMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据消息ID获取单条消息详情
     */
    @Override
    public Message getMessageById(Integer messageId) {
        // 直接使用MyBatis-Plus提供的getById方法
        return baseMapper.selectById(messageId);
    }
}