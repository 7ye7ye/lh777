package org.jeecg.modules.hospital.controller;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hospital.dto.RegistrationDetailDTO;
import org.jeecg.modules.hospital.entity.Appointment;
import org.jeecg.modules.hospital.entity.Message;
import org.jeecg.modules.hospital.service.AppointmentService;
import org.jeecg.modules.hospital.service.MessageService;
import org.jeecg.modules.hospital.service.RegistrationService;
import org.jeecg.modules.hospital.task.AppointmentReminderTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @Autowired
    private RegistrationService registrationService;

    /**
     * 根据ID获取预约详情（回执单）
     */
    @GetMapping("/detail")
    public Appointment getAppointmentDetail(@RequestParam String id) {
        // 优先尝试从挂号记录中查询 (RegistrationService)
        try {
            Long recordId = Long.valueOf(id);
            RegistrationDetailDTO detail = registrationService.getRegistrationDetail(recordId);
            if (detail != null) {
                return convertToAppointment(detail);
            }
        } catch (NumberFormatException ignored) {
        }

        // 如果挂号记录中没找到，再查询预约表 (AppointmentService)
        Appointment appointment = appointmentService.getById(id);
        if (appointment != null) {
            return appointment;
        }
        return null;
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
                    appointment.getAppointmentTime() != null ? appointment.getAppointmentTime().toString() : "");
            successMessage.setContent(contentJson);
            successMessage.setCreatedTime(LocalDateTime.now());
            successMessage.setIsRead(false);

            messageService.save(successMessage);
            log.info("[createAppointment] 预约成功消息已发送");

            // 3. ⭐ 自动判断是否需要立即发送就诊提醒
            // 条件：预约日期 == 明天 && 当前时间 >= 08:00
            appointmentReminderTask.checkAndCreateImmediateReminder(convertToDetail(appointment));
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

    private Appointment convertToAppointment(RegistrationDetailDTO detail) {
        Appointment appointment = new Appointment();
        appointment.setId(String.valueOf(detail.getRecordId()));
        appointment.setQrCodeData(detail.getRegistrationNo());
        appointment.setSerialNumber(detail.getRegistrationNo());
        appointment.setPatientName(detail.getPatientName());
        appointment.setHospitalAddress("北京市西直门外上园村3号");
        appointment.setDepartmentName(detail.getDepartmentName());
        appointment.setVisitLocation(detail.getRoomNumber() != null ? detail.getRoomNumber()
                : (detail.getDeptLocation() != null ? detail.getDeptLocation() : "门诊楼一层"));
        appointment.setDoctorName(detail.getDoctorName());
        appointment.setAppointmentDate(detail.getScheduleDate());
        appointment.setAppointmentTime(slotToTime(detail.getTimeSlot()));
        appointment.setConsultationFee(detail.getActualPrice());
        boolean cancelled = detail.getStatus() != null && detail.getStatus() == 3;
        appointment.setStatus(cancelled ? "退号成功" : "预约成功");
        appointment.setOrderNumber(detail.getRegistrationNo());
        appointment.setCancelled(cancelled);
        return appointment;
    }

    private LocalTime slotToTime(Integer slot) {
        return switch (slot != null ? slot : 0) {
            case 1 -> LocalTime.of(8, 0);
            case 2 -> LocalTime.of(14, 0);
            case 3 -> LocalTime.of(18, 0);
            default -> LocalTime.of(9, 0);
        };
    }

    private RegistrationDetailDTO convertToDetail(Appointment appointment) {
        RegistrationDetailDTO detail = new RegistrationDetailDTO();
        if (appointment == null) {
            return detail;
        }
        try {
            if (appointment.getId() != null) {
                detail.setRecordId(Long.valueOf(appointment.getId()));
            }
        } catch (NumberFormatException ignored) {
        }
        detail.setRegistrationNo(appointment.getSerialNumber());
        detail.setPatientName(appointment.getPatientName());
        detail.setDoctorName(appointment.getDoctorName());
        detail.setDepartmentName(appointment.getDepartmentName());
        detail.setScheduleDate(appointment.getAppointmentDate());
        detail.setTimeSlot(timeToSlot(appointment.getAppointmentTime()));
        detail.setPatientCardNo(appointment.getQrCodeData());
        detail.setActualPrice(appointment.getConsultationFee());
        return detail;
    }

    private Integer timeToSlot(LocalTime time) {
        if (time == null)
            return null;
        int hour = time.getHour();
        if (hour < 12)
            return 1;
        if (hour < 18)
            return 2;
        return 3;
    }

    /**
     * 手动触发"提前一天提醒"任务（用于测试）
     * 访问地址：GET http://localhost:8080/api/appointment/trigger-reminder
     */
    @GetMapping("/trigger-reminder")
    public String triggerReminder() {
        try {
            log.info("[triggerReminder] 手动触发提前一天提醒任务");
            appointmentReminderTask.generateTomorrowReminders();
            return "success: 提醒任务已执行，请查看日志";
        } catch (Exception e) {
            log.error("[triggerReminder] 执行失败", e);
            return "error: " + e.getMessage();
        }
    }
}