package org.jeecg.modules.hospital.task;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.hospital.dto.RegistrationDetailDTO;
import org.jeecg.modules.hospital.entity.Message;
import org.jeecg.modules.hospital.mapper.MessageMapper;
import org.jeecg.modules.hospital.mapper.RegistrationMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private final RegistrationMapper registrationMapper;
    private final MessageMapper messageMapper;

    // 每天 08:00 运行一次（CRON: 秒 分 时 日 月 周）
    @Scheduled(cron = "0 0 8 * * ?")
    // @Scheduled(cron = "0 */5 * * * ?")
    public void generateTomorrowReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        log.info("[AppointmentReminderTask] ========== 开始执行提前一天提醒任务 ==========");
        log.info("[AppointmentReminderTask] 当前日期: {}, 目标日期(明天): {}", LocalDate.now(), targetDate);

        List<RegistrationDetailDTO> records = registrationMapper.listRegistrationsByDate(targetDate);
        log.info("[AppointmentReminderTask] 查询到 {} 条预约记录", records != null ? records.size() : 0);

        if (records == null || records.isEmpty()) {
            log.info("[AppointmentReminderTask] 没有找到 {} 的预约记录，任务结束", targetDate);
            return;
        }

        int successCount = 0;
        int skipCount = 0;
        for (RegistrationDetailDTO detail : records) {
            log.info("[AppointmentReminderTask] 处理预约: recordId={}, patientName={}, doctorName={}, date={}, slot={}", 
                detail.getRecordId(), detail.getPatientName(), detail.getDoctorName(), 
                detail.getScheduleDate(), detail.getTimeSlot());
            boolean result = createReminderForRegistration(detail);
            if (result) {
                successCount++;
            } else {
                skipCount++;
            }
        }

        log.info("[AppointmentReminderTask] ========== 任务完成 ==========");
        log.info("[AppointmentReminderTask] 成功创建: {} 条, 跳过: {} 条, 总计: {} 条", 
            successCount, skipCount, records.size());
    }

    /**
     * 每小时运行，创建“提前一小时提醒”
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void generateOneHourReminders() {
        LocalDate today = LocalDate.now();
        int nextHour = LocalTime.now().plusHours(1).getHour();
        Integer timeSlot = hourToSlot(nextHour);
        if (timeSlot == null) {
            return;
        }
        List<RegistrationDetailDTO> records = registrationMapper.listRegistrationsByDateAndSlot(today, timeSlot);
        if (records == null || records.isEmpty()) {
            return;
        }
        for (RegistrationDetailDTO detail : records) {
            createOneHourReminder(detail);
        }
    }
    
    /**
     * 为单个预约创建提醒消息（可被定时任务或预约接口调用）
     * @param detail 挂号详情
     * @return 是否成功创建提醒
     */
    public boolean createReminderForRegistration(RegistrationDetailDTO detail) {
        if (detail == null || detail.getRecordId() == null) {
            log.warn("[createReminder] detail is null or recordId is null");
            return false;
        }
        String appointmentId = String.valueOf(detail.getRecordId());
        log.info("[createReminder] 开始创建提醒: appointmentId={}, patientUserId={}", appointmentId, detail.getPatientUserId());
        
        // 1. 检查是否已存在提醒
        Long exist = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
            .eq(Message::getAppointmentId, appointmentId)
            .eq(Message::getMessageType, "APPOINTMENT_REMINDER")
        );
        if (exist != null && exist > 0) {
            log.info("[createReminder] ⏭️ 提醒已存在，跳过: appointmentId={}", appointmentId);
            return false;
        }

        String userId = resolveReminderUserId(appointmentId, detail);
        log.info("[createReminder] 解析到的userId: {}", userId);
        if (userId == null) {
            log.warn("[createReminder] ❌ 无法解析userId，跳过: appointmentId={}, patientUserId={}", appointmentId, detail.getPatientUserId());
            return false;
        }

        // 3. 创建提醒消息
        Message reminder = new Message();
        reminder.setUserId(userId);
        reminder.setAppointmentId(appointmentId);
        reminder.setMessageType("APPOINTMENT_REMINDER");
        reminder.setTitle("就诊前一天提醒");

        // 4. 组装简易内容 JSON（与前端解析保持兼容：content 为字符串的 JSON）
        String contentJson = String.format("{\n  \"patient_card_no\": \"%s\",\n  \"patient_name\": \"%s\",\n  \"doctor_name\": \"%s\",\n  \"department_name\": \"%s\",\n  \"appointment_time\": \"%s %s\",\n  \"hospital_remark\": \"请提前15分钟到院办理就诊。\"\n}",
            detail.getPatientCardNo() == null ? "" : detail.getPatientCardNo(),
            detail.getPatientName() == null ? "" : detail.getPatientName(),
            detail.getDoctorName() == null ? "" : detail.getDoctorName(),
            detail.getDepartmentName() == null ? "" : detail.getDepartmentName(),
            detail.getScheduleDate() == null ? "" : detail.getScheduleDate().toString(),
            formatTimeSlot(detail.getTimeSlot())
        );
        reminder.setContent(contentJson);
        // ⭐ 定时任务创建：创建时间固定为当天8点
        reminder.setCreatedTime(LocalDate.now().atTime(8, 0));
        log.info("[createReminder] 定时任务创建提醒，创建时间固定为当天8点: {}", reminder.getCreatedTime());
        reminder.setIsRead(false);

        // 5. 插入数据库
        int rows = messageMapper.insert(reminder);
        log.info("[createReminder] created reminder for appointmentId={}, userId={}, createdTime={}, success={}", 
            appointmentId, userId, reminder.getCreatedTime(), rows > 0);
        return rows > 0;
    }
    
    /**
     * 为单个预约创建提醒消息（使用指定的userId）
     * @param detail 挂号详情
     * @param userId 用户ID（直接使用前端传递的userId）
     * @return 是否成功创建提醒
     */
    public boolean createReminderForRegistrationWithUserId(RegistrationDetailDTO detail, String userId) {
        if (detail == null || detail.getRecordId() == null) {
            log.warn("[createReminderWithUserId] detail is null or recordId is null");
            return false;
        }
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("[createReminderWithUserId] userId is null or empty");
            return false;
        }
        
        String appointmentId = String.valueOf(detail.getRecordId());
        log.info("[createReminderWithUserId] 开始创建提醒: appointmentId={}, userId={}", appointmentId, userId);
        
        // 1. 检查是否已存在提醒
        Long exist = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
            .eq(Message::getAppointmentId, appointmentId)
            .eq(Message::getMessageType, "APPOINTMENT_REMINDER")
        );
        if (exist != null && exist > 0) {
            log.info("[createReminderWithUserId] ⏭️ 提醒已存在，跳过: appointmentId={}", appointmentId);
            return false;
        }

        // 2. 创建提醒消息（直接使用传入的userId）
        Message reminder = new Message();
        reminder.setUserId(userId);
        reminder.setAppointmentId(appointmentId);
        reminder.setMessageType("APPOINTMENT_REMINDER");
        reminder.setTitle("就诊前一天提醒");

        // 3. 组装简易内容 JSON
        String contentJson = String.format("{\n  \"patient_card_no\": \"%s\",\n  \"patient_name\": \"%s\",\n  \"doctor_name\": \"%s\",\n  \"department_name\": \"%s\",\n  \"appointment_time\": \"%s %s\",\n  \"hospital_remark\": \"请提前15分钟到院办理就诊。\"\n}",
            detail.getPatientCardNo() == null ? "" : detail.getPatientCardNo(),
            detail.getPatientName() == null ? "" : detail.getPatientName(),
            detail.getDoctorName() == null ? "" : detail.getDoctorName(),
            detail.getDepartmentName() == null ? "" : detail.getDepartmentName(),
            detail.getScheduleDate() == null ? "" : detail.getScheduleDate().toString(),
            formatTimeSlot(detail.getTimeSlot())
        );
        reminder.setContent(contentJson);
        // ⭐ 此方法已废弃，保留逻辑但不使用（由 createReminderForRegistrationWithUserIdAndTime 替代）
        // 创建时间固定为当天8点（用于登录时创建）
        reminder.setCreatedTime(LocalDate.now().atTime(8, 0));
        log.info("[createReminderWithUserId] 登录时创建提醒，创建时间固定为当天8点: {}", reminder.getCreatedTime());
        reminder.setIsRead(false);

        // 4. 插入数据库
        int rows = messageMapper.insert(reminder);
        log.info("[createReminderWithUserId] ✅ 创建提醒成功: appointmentId={}, userId={}, createdTime={}, rows={}", 
            appointmentId, userId, reminder.getCreatedTime(), rows);
        return rows > 0;
    }
    
    /**
     * 为单个预约创建提醒消息（使用指定的userId和创建时间）
     * @param detail 挂号详情
     * @param userId 用户ID（直接使用前端传递的userId）
     * @param createdTime 指定的创建时间
     * @return 是否成功创建提醒
     */
    public boolean createReminderForRegistrationWithUserIdAndTime(RegistrationDetailDTO detail, String userId, LocalDateTime createdTime) {
        if (detail == null || detail.getRecordId() == null) {
            log.warn("[createReminderWithUserIdAndTime] detail is null or recordId is null");
            return false;
        }
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("[createReminderWithUserIdAndTime] userId is null or empty");
            return false;
        }
        if (createdTime == null) {
            log.warn("[createReminderWithUserIdAndTime] createdTime is null");
            return false;
        }
        
        String appointmentId = String.valueOf(detail.getRecordId());
        log.info("[createReminderWithUserIdAndTime] 开始创建提醒: appointmentId={}, userId={}, createdTime={}", 
            appointmentId, userId, createdTime);
        
        // 1. 检查是否已存在提醒
        Long exist = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
            .eq(Message::getAppointmentId, appointmentId)
            .eq(Message::getMessageType, "APPOINTMENT_REMINDER")
        );
        if (exist != null && exist > 0) {
            log.info("[createReminderWithUserIdAndTime] ⏭️ 提醒已存在，跳过: appointmentId={}", appointmentId);
            return false;
        }

        // 2. 创建提醒消息（直接使用传入的userId和创建时间）
        Message reminder = new Message();
        reminder.setUserId(userId);
        reminder.setAppointmentId(appointmentId);
        reminder.setMessageType("APPOINTMENT_REMINDER");
        reminder.setTitle("就诊前一天提醒");

        // 3. 组装简易内容 JSON
        String contentJson = String.format("{\n  \"patient_card_no\": \"%s\",\n  \"patient_name\": \"%s\",\n  \"doctor_name\": \"%s\",\n  \"department_name\": \"%s\",\n  \"appointment_time\": \"%s %s\",\n  \"hospital_remark\": \"请提前15分钟到院办理就诊。\"\n}",
            detail.getPatientCardNo() == null ? "" : detail.getPatientCardNo(),
            detail.getPatientName() == null ? "" : detail.getPatientName(),
            detail.getDoctorName() == null ? "" : detail.getDoctorName(),
            detail.getDepartmentName() == null ? "" : detail.getDepartmentName(),
            detail.getScheduleDate() == null ? "" : detail.getScheduleDate().toString(),
            formatTimeSlot(detail.getTimeSlot())
        );
        reminder.setContent(contentJson);
        reminder.setCreatedTime(createdTime);  // 使用指定的创建时间
        reminder.setIsRead(false);

        // 4. 插入数据库
        int rows = messageMapper.insert(reminder);
        log.info("[createReminderWithUserIdAndTime] ✅ 创建提醒成功: appointmentId={}, userId={}, createdTime={}, rows={}", 
            appointmentId, userId, createdTime, rows);
        return rows > 0;
    }
    
    /**
     * 预约成功时调用：如果是明天的预约，且当前时间>=8点，立即创建提醒
     * 规则：
     * - 只处理明天的预约（scheduleDate == tomorrow）
     * - 如果当前时间 >= 8点：立即创建提醒，创建时间为当前时间
     * - 如果当前时间 < 8点：不创建提醒（只创建挂号成功通知，提醒会在8点后登录时创建）
     * - 如果是后天及更晚的预约：不创建提醒（由定时任务在预约前一天8点创建）
     * @param detail 挂号详情
     */
    public void checkAndCreateImmediateReminder(RegistrationDetailDTO detail) {
        if (detail == null || detail.getScheduleDate() == null) {
            return;
        }
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        // 只处理明天的预约
        if (!tomorrow.equals(detail.getScheduleDate())) {
            log.info("[immediateReminder] not tomorrow's appointment, skip. appointmentDate={}, tomorrow={}", 
                detail.getScheduleDate(), tomorrow);
            return;
        }
        
        // 只处理8点之后的挂号
        LocalTime now = LocalTime.now();
        if (now.getHour() < 8) {
            log.info("[immediateReminder] current time < 8:00, skip immediate reminder. appointmentId={}, currentTime={}", 
                detail.getRecordId(), now);
            log.info("[immediateReminder] 提醒将在8点后登录时创建，创建时间固定为当天8点");
            return;
        }
        
        // 8点之后挂号：立即创建提醒，创建时间为当前时间
        log.info("[immediateReminder] creating immediate reminder for appointmentId={}, currentTime={}", 
            detail.getRecordId(), now);
        
        LocalDateTime createdTime = LocalDateTime.now();
        log.info("[immediateReminder] 当前时间 >= 8点，创建时间设为当前时间: {}", createdTime);
        
        // 创建提醒（需要先解析userId）
        String userId = resolveReminderUserId(String.valueOf(detail.getRecordId()), detail);
        if (userId != null) {
            createReminderForRegistrationWithUserIdAndTime(detail, userId, createdTime);
        } else {
            log.warn("[immediateReminder] 无法解析userId，使用默认方法创建提醒");
            createReminderForRegistration(detail);
        }
    }

    private String resolveReminderUserId(String appointmentId, RegistrationDetailDTO detail) {
        log.debug("[resolveReminderUserId] 开始解析userId: appointmentId={}, patientUserId={}", appointmentId, detail.getPatientUserId());
        
        // 1. 优先从成功消息中获取userId（最可靠）
        Message successMsg = messageMapper.selectOne(new LambdaQueryWrapper<Message>()
                .eq(Message::getAppointmentId, appointmentId)
                .eq(Message::getMessageType, "APPOINTMENT_SUCCESS")
                .last("limit 1"));
        if (successMsg != null && StringUtils.isNotBlank(successMsg.getUserId())) {
            log.debug("[resolveReminderUserId] ✅ 从成功消息中获取userId: {}", successMsg.getUserId());
            return successMsg.getUserId();
        }
        
        // 2. 从预约详情中获取patientUserId
        if (detail.getPatientUserId() != null) {
            log.debug("[resolveReminderUserId] ✅ 从预约详情中获取userId: {}", detail.getPatientUserId());
            return String.valueOf(detail.getPatientUserId());
        }
        
        log.warn("[resolveReminderUserId] ❌ 无法解析userId: appointmentId={}", appointmentId);
        return null;
    }

    private String formatTimeSlot(Integer slot) {
        if (slot == null) {
            return "";
        }
        return switch (slot) {
            case 1 -> "08:00-12:00";
            case 2 -> "14:00-17:00";
            case 3 -> "18:00-20:00";
            default -> "";
        };
    }

    private void createOneHourReminder(RegistrationDetailDTO detail) {
        if (detail == null || detail.getRecordId() == null) {
            return;
        }
        if (!isEligibleForOneHourReminder(detail)) {
            return;
        }
        String appointmentId = String.valueOf(detail.getRecordId());
        boolean exists = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getAppointmentId, appointmentId)
                .eq(Message::getMessageType, "APPOINTMENT_ONE_HOUR")) > 0;
        if (exists) {
            return;
        }
        String userId = resolveReminderUserId(appointmentId, detail);
        if (userId == null) {
            return;
        }
        LocalDateTime reminderTime = calcReminderTime(detail);
        if (reminderTime == null) {
            return;
        }
        createOneHourReminderWithUserIdAndTime(detail, userId, reminderTime);
    }

    /**
     * 创建“就诊前一小时提醒”（指定userId与创建时间，便于补建）
     */
    public boolean createOneHourReminderWithUserIdAndTime(RegistrationDetailDTO detail,
                                                          String userId,
                                                          LocalDateTime createdTime) {
        if (detail == null || detail.getRecordId() == null) {
            log.warn("[createOneHourReminderWithUserIdAndTime] detail or recordId is null");
            return false;
        }
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("[createOneHourReminderWithUserIdAndTime] userId is blank");
            return false;
        }
        if (createdTime == null) {
            createdTime = calcReminderTime(detail);
            if (createdTime == null) {
                return false;
            }
        }

        if (!isEligibleForOneHourReminder(detail)) {
            log.info("[createOneHourReminderWithUserIdAndTime] skip recordId={} due to late registration", detail.getRecordId());
            return false;
        }

        String appointmentId = String.valueOf(detail.getRecordId());
        Long exist = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getAppointmentId, appointmentId)
                .eq(Message::getMessageType, "APPOINTMENT_ONE_HOUR"));
        if (exist != null && exist > 0) {
            log.info("[createOneHourReminderWithUserIdAndTime] reminder exists, skip. appointmentId={}", appointmentId);
            return false;
        }

        Message reminder = new Message();
        reminder.setUserId(userId);
        reminder.setAppointmentId(appointmentId);
        reminder.setMessageType("APPOINTMENT_ONE_HOUR");
        reminder.setTitle("就诊提醒");
        String contentJson = String.format("{\n  \"patient_card_no\": \"%s\",\n  \"patient_name\": \"%s\",\n  \"doctor_name\": \"%s\",\n  \"department_name\": \"%s\",\n  \"appointment_time\": \"%s %s\",\n  \"hospital_remark\": \"您的就诊时间即将到达，请尽快前往诊室。\"\n}",
                detail.getPatientCardNo() == null ? "" : detail.getPatientCardNo(),
                detail.getPatientName() == null ? "" : detail.getPatientName(),
                detail.getDoctorName() == null ? "" : detail.getDoctorName(),
                detail.getDepartmentName() == null ? "" : detail.getDepartmentName(),
                detail.getScheduleDate() == null ? "" : detail.getScheduleDate().toString(),
                formatTimeSlot(detail.getTimeSlot()));
        reminder.setContent(contentJson);
        reminder.setCreatedTime(createdTime);
        reminder.setIsRead(false);
        int rows = messageMapper.insert(reminder);
        log.info("[createOneHourReminderWithUserIdAndTime] created reminder: appointmentId={}, userId={}, createdTime={}, rows={}",
                appointmentId, userId, createdTime, rows);
        return rows > 0;
    }

    private boolean isEligibleForOneHourReminder(RegistrationDetailDTO detail) {
        LocalDateTime reminderTime = calcReminderTime(detail);
        if (reminderTime == null) {
            return false;
        }
        if (detail.getRegisterTime() != null && detail.getRegisterTime().isAfter(reminderTime)) {
            log.info("[isEligibleForOneHourReminder] recordId={} registered at {}, after reminder window {}, skip",
                    detail.getRecordId(), detail.getRegisterTime(), reminderTime);
            return false;
        }
        return true;
    }

    private LocalDateTime calcReminderTime(RegistrationDetailDTO detail) {
        if (detail == null || detail.getScheduleDate() == null) {
            return null;
        }
        LocalTime slotStart = slotStartTime(detail.getTimeSlot());
        if (slotStart == null) {
            return null;
        }
        return LocalDateTime.of(detail.getScheduleDate(), slotStart).minusHours(1);
    }

    private Integer hourToSlot(int hour) {
        if (hour >= 8 && hour < 12) {
            return 1;
        } else if (hour >= 14 && hour < 17) {
            return 2;
        } else if (hour >= 18 && hour < 21) {
            return 3;
        }
        return null;
    }

    private LocalTime slotStartTime(Integer slot) {
        if (slot == null) {
            return null;
        }
        return switch (slot) {
            case 1 -> LocalTime.of(8, 0);
            case 2 -> LocalTime.of(14, 0);
            case 3 -> LocalTime.of(18, 0);
            default -> null;
        };
    }
}


