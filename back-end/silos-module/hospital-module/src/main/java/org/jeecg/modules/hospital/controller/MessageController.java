package org.jeecg.modules.hospital.controller; 

import org.jeecg.modules.hospital.entity.Message;
import org.jeecg.modules.hospital.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 消息中心对外接口
 */
@RestController // 声明这是一个返回JSON格式的Controller
@RequestMapping("/api/messages") // 定义这个Controller下所有接口的统一前缀
public class MessageController {

    @Autowired // 自动注入刚刚写的Service
    private MessageService messageService;

    /**
     * 获取当前用户的消息列表
     * 访问地址： GET http://localhost:8080/api/messages/list?userId=wuzhizhu_001
     * @param userId 从请求参数中获取用户ID
     * @return 消息列表
     */
    @GetMapping("/list")
    public List<Message> getMessageList(@RequestParam String userId) {
        // 在实际项目中，userId通常从登录状态中获取，这里为了方便测试，从请求参数中直接传递
        return messageService.listMessagesByUserId(userId);
    }
    /**
     * 根据预约ID获取消息详情列表
     * 访问地址： GET http://localhost:8095/jeecg-boot/api/messages/detail?appointmentId=APPOINTMENT_001
     */
    @GetMapping("/detail")
    public List<Message> getMessageDetail(@RequestParam String appointmentId) {
        return messageService.listMessagesByAppointmentId(appointmentId);
    }
}