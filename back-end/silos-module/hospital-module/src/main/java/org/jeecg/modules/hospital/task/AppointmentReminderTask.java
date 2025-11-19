package org.jeecg.modules.hospital.task;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hospital.entity.Appointment;
import org.jeecg.modules.hospital.entity.Message;
import org.jeecg.modules.hospital.mapper.AppointmentMapper;
import org.jeecg.modules.hospital.mapper.MessageMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 每日生成“就诊前一天提醒”的任务。
 * 规则：
 * - 查找明日(appointment_date = today+1)的预约记录
 * - 若 message_log 中不存在该预约的 APPOINTMENT_REMINDER，则写入一条提醒消息
 * - userId 取自该预约在 message_log 中已有的任意消息（若无，跳过，避免错误关联）
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DS("hospital")
public class AppointmentReminderTask {

    private final AppointmentMapper appointmentMapper;
    private final MessageMapper messageMapper;

    // 每天 08:00 运行一次（CRON: 秒 分 时 日 月 周）
    @Scheduled(cron = "0 0 8 * * ?")
    public void generateTomorrowReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        log.info("[AppointmentReminderTask] start, targetDate={}", targetDate);

        List<Appointment> appointments = appointmentMapper.selectList(
            new LambdaQueryWrapper<Appointment>().eq(Appointment::getAppointmentDate, targetDate)
        );

        if (appointments == null || appointments.isEmpty()) {
            log.info("[AppointmentReminderTask] no appointments for {}", targetDate);
            return;
        }

        for (Appointment appt : appointments) {
            createReminderForAppointment(appt);
        }

        log.info("[AppointmentReminderTask] finished for {}", targetDate);
    }
    
    /**
     * 为单个预约创建提醒消息（可被定时任务或预约接口调用）
     * @param appt 预约信息
     * @return 是否成功创建提醒
     */
    public boolean createReminderForAppointment(Appointment appt) {
        // 1. 检查是否已存在提醒
        Long exist = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
            .eq(Message::getAppointmentId, appt.getId())
            .eq(Message::getMessageType, "APPOINTMENT_REMINDER")
        );
        if (exist != null && exist > 0) {
            log.info("[createReminder] skip, reminder already exists for appointmentId={}", appt.getId());
            return false;
        }

        // 2. 尝试复用该预约已存在的消息来获取 userId
        Message anyMsg = messageMapper.selectOne(new LambdaQueryWrapper<Message>()
            .eq(Message::getAppointmentId, appt.getId())
            .last("limit 1")
        );
        if (anyMsg == null) {
            // 找不到用户归属，跳过，避免写入脏数据
            log.warn("[createReminder] skip, no user message found for appointmentId={}", appt.getId());
            return false;
        }

        // 3. 创建提醒消息
        Message reminder = new Message();
        reminder.setUserId(anyMsg.getUserId());
        reminder.setAppointmentId(appt.getId());
        reminder.setMessageType("APPOINTMENT_REMINDER");
        reminder.setTitle("就诊前一天提醒");

        // 4. 组装简易内容 JSON（与前端解析保持兼容：content 为字符串的 JSON）
        String contentJson = String.format("{\n  \"patient_card_no\": \"%s\",\n  \"patient_name\": \"%s\",\n  \"doctor_name\": \"%s\",\n  \"department_name\": \"%s\",\n  \"appointment_time\": \"%s %s\",\n  \"hospital_remark\": \"请提前15分钟到院办理就诊。\"\n}",
            appt.getQrCodeData() == null ? "" : appt.getQrCodeData(),
            appt.getPatientName() == null ? "" : appt.getPatientName(),
            appt.getDoctorName() == null ? "" : appt.getDoctorName(),
            appt.getDepartmentName() == null ? "" : appt.getDepartmentName(),
            appt.getAppointmentDate() == null ? "" : appt.getAppointmentDate().toString(),
            appt.getAppointmentTime() == null ? "" : appt.getAppointmentTime().toString()
        );
        reminder.setContent(contentJson);
        reminder.setCreatedTime(LocalDateTime.now());
        reminder.setIsRead(false);

        // 5. 插入数据库
        int rows = messageMapper.insert(reminder);
        log.info("[createReminder] created reminder for appointmentId={}, success={}", appt.getId(), rows > 0);
        return rows > 0;
    }
    
    /**
     * 预约成功时调用：如果是明天的预约且当前时间>=08:00，立即创建提醒
     * @param appointmentId 预约ID
     */
    public void checkAndCreateImmediateReminder(String appointmentId) {
        // 1. 查询预约信息
        Appointment appt = appointmentMapper.selectById(appointmentId);
        if (appt == null) {
            log.warn("[immediateReminder] appointment not found, id={}", appointmentId);
            return;
        }
        
        // 2. 判断是否是明天的预约
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (!tomorrow.equals(appt.getAppointmentDate())) {
            log.info("[immediateReminder] not tomorrow's appointment, skip. appointmentDate={}", appt.getAppointmentDate());
            return;
        }
        
        // 3. 判断当前时间是否>=08:00
        int currentHour = java.time.LocalTime.now().getHour();
        if (currentHour < 8) {
            log.info("[immediateReminder] current time < 08:00, let scheduled task handle it");
            return;
        }
        
        // 4. 创建提醒消息
        log.info("[immediateReminder] creating immediate reminder for appointmentId={}", appointmentId);
        createReminderForAppointment(appt);
    }
}


