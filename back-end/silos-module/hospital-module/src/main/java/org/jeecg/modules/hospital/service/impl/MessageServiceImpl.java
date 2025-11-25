package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hospital.dto.RegistrationDetailDTO;
import org.jeecg.modules.hospital.entity.Message;
import org.jeecg.modules.hospital.mapper.MessageMapper;
import org.jeecg.modules.hospital.mapper.RegistrationMapper;
import org.jeecg.modules.hospital.service.MessageService;
import org.jeecg.modules.hospital.task.AppointmentReminderTask;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息记录业务逻辑实现类
 */
@Slf4j
@Service // 声明这是一个Spring的服务类
@DS("hospital") // 指定使用名为 "hospital" 的数据源
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    
    private final RegistrationMapper registrationMapper;
    private final AppointmentReminderTask appointmentReminderTask;
    
    // 第一次加载的是所有预约信息的列表，所以传的是用户ID
    @Override
    public List<Message> listMessagesByUserId(String userId) {
        // ⭐ 在查询消息前，先检查并创建缺失的提醒
        checkAndCreateRemindersForUser(userId);
        checkAndCreateOneHourRemindersForUser(userId);
        
        // 使用MyBatis-Plus的查询构造器
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        // 查询条件：userId 等于传入的 userId
        queryWrapper.eq(Message::getUserId, userId);
        // 排序条件：按创建时间降序排列 (最新的在最前面)
        queryWrapper.orderByDesc(Message::getCreatedTime);
        
        // baseMapper 是 ServiceImpl 自带的，可以直接用
        return baseMapper.selectList(queryWrapper);
    }
    
    /**
     * 检查并创建用户缺失的提醒消息
     * 规则：
     * - 只处理明天的预约（scheduleDate == tomorrow）
     * - 只有当前时间>=8点才创建提醒
     * - 创建时间固定为当天8点（无论几点登录，看到的提醒时间都是8点）
     * - 如果是后天及更晚的预约，不在此处处理（由定时任务在预约前一天8点创建）
     */
    private void checkAndCreateRemindersForUser(String userId) {
        try {
            log.info("[checkAndCreateReminders] ========== 开始检查提醒 ==========");
            log.info("[checkAndCreateReminders] userId={}, currentTime={}", userId, LocalTime.now());
            
            Long userIdLong = Long.valueOf(userId);
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalTime now = LocalTime.now();
            
            log.info("[checkAndCreateReminders] tomorrow={}, currentHour={}", tomorrow, now.getHour());
            
            // 只有当前时间>=8点才创建提醒
            if (now.getHour() < 8) {
                log.info("[checkAndCreateReminders] ⏰ 当前时间 < 08:00，跳过检查。currentHour={}", now.getHour());
                log.info("[checkAndCreateReminders] 💡 提示：8点后登录才能看到明天的预约提醒");
                return;
            }
            
            // ⭐ 关键修改：通过预约成功消息来查找预约记录，而不是通过 patient 表
            // 因为预约成功消息中的 userId 是创建预约时使用的用户ID，更可靠
            log.info("[checkAndCreateReminders] 🔍 通过预约成功消息查找用户的预约记录: userId={}, date={}", userIdLong, tomorrow);
            
            // 1. 先查询所有明天的预约记录
            List<RegistrationDetailDTO> allRecords = registrationMapper.listRegistrationsByDate(tomorrow);
            if (allRecords == null || allRecords.isEmpty()) {
                log.info("[checkAndCreateReminders] ❌ 未找到任何明天的预约记录: date={}", tomorrow);
                return;
            }
            
            log.info("[checkAndCreateReminders] 找到 {} 条明天的预约记录（所有用户）", allRecords.size());
            
            // 2. 通过预约成功消息来匹配当前用户的预约记录
            List<RegistrationDetailDTO> records = new java.util.ArrayList<>();
            for (RegistrationDetailDTO detail : allRecords) {
                String appointmentId = String.valueOf(detail.getRecordId());
                // 查询该预约的成功消息，看 userId 是否匹配
                Message successMsg = baseMapper.selectOne(new LambdaQueryWrapper<Message>()
                    .eq(Message::getAppointmentId, appointmentId)
                    .eq(Message::getMessageType, "APPOINTMENT_SUCCESS")
                    .last("limit 1"));
                
                if (successMsg != null && userId.equals(successMsg.getUserId())) {
                    log.info("[checkAndCreateReminders] ✅ 找到匹配的预约: recordId={}, 成功消息中的userId={}, patientUserId={}", 
                        detail.getRecordId(), successMsg.getUserId(), detail.getPatientUserId());
                    records.add(detail);
                } else {
                    log.debug("[checkAndCreateReminders] 跳过预约: recordId={}, 成功消息userId={}, 当前userId={}", 
                        detail.getRecordId(), 
                        successMsg != null ? successMsg.getUserId() : "null", 
                        userId);
                }
            }
            
            if (records.isEmpty()) {
                log.warn("[checkAndCreateReminders] ❌ 未找到当前用户的预约记录");
                log.warn("[checkAndCreateReminders] 💡 提示：请检查预约成功消息中的 userId 是否为 {}", userIdLong);
                for (RegistrationDetailDTO detail : allRecords) {
                    Message successMsg = baseMapper.selectOne(new LambdaQueryWrapper<Message>()
                        .eq(Message::getAppointmentId, String.valueOf(detail.getRecordId()))
                        .eq(Message::getMessageType, "APPOINTMENT_SUCCESS")
                        .last("limit 1"));
                    log.warn("[checkAndCreateReminders] 预约记录: recordId={}, patientUserId={}, 成功消息userId={}", 
                        detail.getRecordId(), 
                        detail.getPatientUserId(),
                        successMsg != null ? successMsg.getUserId() : "null");
                }
                return;
            }
            
            log.info("[checkAndCreateReminders] ✅ 找到 {} 条当前用户的预约记录", records.size());
            
            log.info("[checkAndCreateReminders] ✅ 找到 {} 条明天的预约记录", records.size());
            for (RegistrationDetailDTO detail : records) {
                log.info("[checkAndCreateReminders] 📋 预约详情: recordId={}, patientName={}, doctorName={}, date={}, slot={}, patientUserId={}", 
                    detail.getRecordId(), detail.getPatientName(), detail.getDoctorName(), 
                    detail.getScheduleDate(), detail.getTimeSlot(), detail.getPatientUserId());
            }
            
            // 为每条预约记录创建提醒（如果还没有创建）
            // ⭐ 直接使用前端传递的userId，而不是从预约记录中解析
            // ⭐ 查询消息时创建的提醒，创建时间固定为当天8点（因为这是"提前一天提醒"，应该在当天8点发送）
            int createdCount = 0;
            for (RegistrationDetailDTO detail : records) {
                boolean result = appointmentReminderTask.createReminderForRegistrationWithUserIdAndTime(
                    detail, userId, LocalDate.now().atTime(8, 0));
                if (result) {
                    createdCount++;
                    log.info("[checkAndCreateReminders] ✅ 成功创建提醒: recordId={}, userId={}, createdTime=当天8点", 
                        detail.getRecordId(), userId);
                } else {
                    log.info("[checkAndCreateReminders] ⏭️ 跳过创建（已存在）: recordId={}", detail.getRecordId());
                }
            }
            
            log.info("[checkAndCreateReminders] ========== 检查完成 ==========");
            log.info("[checkAndCreateReminders] 总计: {} 条预约，成功创建: {} 条提醒", records.size(), createdCount);
        } catch (NumberFormatException e) {
            log.error("[checkAndCreateReminders] ❌ userId格式错误: userId={}, error={}", userId, e.getMessage());
        } catch (Exception e) {
            log.error("[checkAndCreateReminders] ❌ 检查提醒时发生错误: userId={}", userId, e);
        }
    }

    /**
     * 登录时补建“就诊前一小时提醒”
     * 规则：
     * - 只处理今天的预约
     * - 仅当当前时间 >= (就诊开始时间 - 1小时) 且 < 就诊开始时间 时补建
     * - 创建时间固定为 (就诊开始时间 - 1小时)，与定时任务保持一致
     */
    private void checkAndCreateOneHourRemindersForUser(String userId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();
            log.info("[checkAndCreateOneHourReminders] userId={}, today={}, currentTime={}", userId, today, now);

            List<RegistrationDetailDTO> allRecords = registrationMapper.listRegistrationsByDate(today);
            if (allRecords == null || allRecords.isEmpty()) {
                log.info("[checkAndCreateOneHourReminders] 无今日预约记录");
                return;
            }

            int createdCount = 0;
            for (RegistrationDetailDTO detail : allRecords) {
                if (!isRecordBelongToUser(detail, userId)) {
                    continue;
                }

                LocalTime slotStartTime = slotStartTime(detail.getTimeSlot());
                if (slotStartTime == null || detail.getScheduleDate() == null) {
                    continue;
                }

                LocalDateTime appointmentStart = LocalDateTime.of(detail.getScheduleDate(), slotStartTime);
                LocalDateTime reminderTime = appointmentStart.minusHours(1);

                if (now.isBefore(reminderTime)) {
                    // 时间未到一小时前
                    continue;
                }
                if (!now.isBefore(appointmentStart)) {
                    // 已超过就诊开始时间，不再提醒
                    continue;
                }
                if (detail.getRegisterTime() != null && detail.getRegisterTime().isAfter(reminderTime)) {
                    log.info("[checkAndCreateOneHourReminders] recordId={} registered at {}, after reminder window {}, skip",
                            detail.getRecordId(), detail.getRegisterTime(), reminderTime);
                    continue;
                }

                boolean created = appointmentReminderTask.createOneHourReminderWithUserIdAndTime(detail, userId, reminderTime);
                if (created) {
                    createdCount++;
                }
            }

            log.info("[checkAndCreateOneHourReminders] 为 userId={} 补建 {} 条一小时提醒", userId, createdCount);
        } catch (Exception e) {
            log.error("[checkAndCreateOneHourReminders] 处理失败: userId={}", userId, e);
        }
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

    private boolean isRecordBelongToUser(RegistrationDetailDTO detail, String userId) {
        if (detail == null || detail.getRecordId() == null) {
            return false;
        }
        Message successMsg = baseMapper.selectOne(new LambdaQueryWrapper<Message>()
                .eq(Message::getAppointmentId, String.valueOf(detail.getRecordId()))
                .eq(Message::getMessageType, "APPOINTMENT_SUCCESS")
                .last("limit 1"));
        if (successMsg != null && userId.equals(successMsg.getUserId())) {
            return true;
        }
        if (detail.getPatientUserId() != null) {
            return userId.equals(String.valueOf(detail.getPatientUserId()));
        }
        return false;
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