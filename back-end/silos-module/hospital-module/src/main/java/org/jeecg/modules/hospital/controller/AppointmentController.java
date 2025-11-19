package org.jeecg.modules.hospital.controller;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hospital.entity.Appointment;
import org.jeecg.modules.hospital.entity.Message;
import org.jeecg.modules.hospital.service.AppointmentService;
import org.jeecg.modules.hospital.service.MessageService;
import org.jeecg.modules.hospital.task.AppointmentReminderTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/appointment")
@DS("hospital")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private AppointmentReminderTask appointmentReminderTask;

    /**
     * 根据ID获取预约详情（回执单）
     */
    @GetMapping("/detail")
    public Appointment getAppointmentDetail(@RequestParam String id) {
        return appointmentService.getById(id);
    }
    
    /**
     * 创建预约（含自动提醒功能）
     * 
     * 业务流程：
     * 1. 创建预约记录
     * 2. 发送预约成功消息
     * 3. ⭐ 自动判断是否需要发送就诊提醒（>=08:00 && 预约明天）
     */
    @PostMapping("/create")
    public String createAppointment(@RequestBody Appointment appointment) {
        try {
            log.info("[createAppointment] 开始创建预约, appointmentId={}", appointment.getId());
            
            // 1. 保存预约记录
            appointmentService.save(appointment);
            log.info("[createAppointment] 预约记录已保存");
            
            // 2. 创建预约成功消息
            Message successMessage = new Message();
            successMessage.setUserId(getUserIdFromAppointment(appointment)); // 需要从预约中获取userId
            successMessage.setAppointmentId(appointment.getId());
            successMessage.setMessageType("APPOINTMENT_SUCCESS");
            successMessage.setTitle("预约挂号成功提醒");
            
            // 组装消息内容JSON
            String contentJson = String.format(
                "{\"patient_card_no\":\"%s\",\"patient_name\":\"%s\",\"doctor_name\":\"%s\",\"department_name\":\"%s\",\"appointment_time\":\"%s %s\",\"hospital_remark\":\"请提前15分钟到达诊室候诊\"}",
                appointment.getQrCodeData() != null ? appointment.getQrCodeData() : "",
                appointment.getPatientName() != null ? appointment.getPatientName() : "",
                appointment.getDoctorName() != null ? appointment.getDoctorName() : "",
                appointment.getDepartmentName() != null ? appointment.getDepartmentName() : "",
                appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "",
                appointment.getAppointmentTime() != null ? appointment.getAppointmentTime().toString() : ""
            );
            successMessage.setContent(contentJson);
            successMessage.setCreatedTime(LocalDateTime.now());
            successMessage.setIsRead(false);
            
            messageService.save(successMessage);
            log.info("[createAppointment] 预约成功消息已发送");
            
            // 3. ⭐ 自动判断是否需要立即发送就诊提醒
            //    条件：预约日期 == 明天 && 当前时间 >= 08:00
            appointmentReminderTask.checkAndCreateImmediateReminder(appointment.getId());
            log.info("[createAppointment] 已检查是否需要立即提醒");
            
            return "success";
            
        } catch (Exception e) {
            log.error("[createAppointment] 创建预约失败", e);
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 从预约信息中获取用户ID
     * 注意：实际项目中可能需要从登录态、token或其他方式获取
     * 这里简化处理，实际使用时需要根据业务调整
     */
    private String getUserIdFromAppointment(Appointment appointment) {
        // TODO: 根据实际业务逻辑获取userId
        // 方案1：从登录态获取
        // 方案2：从患者表查询
        // 方案3：前端传递
        
        // 临时方案：从患者姓名推断（仅示例，生产环境需改进）
        return "262"; // 这里暂时返回固定值，实际应该动态获取
    }
}