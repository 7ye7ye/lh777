package org.jeecg.modules.hospital.service.impl;

import org.jeecg.modules.hospital.dto.RegistrationDetailDTO;
import org.jeecg.modules.hospital.entity.*;
import org.jeecg.modules.hospital.mapper.*;
import org.jeecg.modules.hospital.service.MessageService;
import org.jeecg.modules.hospital.task.AppointmentReminderTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.jeecg.modules.hospital.service.WaitingQueueService;
import jakarta.annotation.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class WaitingQueueServiceImpl implements WaitingQueueService {

    @Resource
    private WaitingQueueMapper waitingQueueMapper;
    @Resource
    private RegistrationMapper registrationMapper;
    @Resource
    private DoctorScheduleMapper doctorScheduleMapper;
    @Resource
    private PatientMapper patientMapper;
    @Resource
    private MessageService messageService;
    @Resource
    private AppointmentReminderTask appointmentReminderTask;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Logger log = LoggerFactory.getLogger(WaitingQueueServiceImpl.class);

    @Override
    public void autoFillFromQueue(Long scheduleId, int n) {
        // 默认不是加号场景
        autoFillFromQueue(scheduleId, n, false);
    }

    @Override
    public void autoFillFromQueue(Long scheduleId, int n, boolean isAddQuota) {
        // 1. 读取候补队列中排前 n 的人
        for (int i = 0; i < n; i++) {
            WaitingQueue first = waitingQueueMapper.selectFirstWaiting(scheduleId);
            if (first == null) {
                break;  // 如果队列中没有人候补了，退出循环
            }

            // 2. 获取挂号记录对应的候补记录
            RegistrationRecord candidate = registrationMapper.selectById(first.getRegistrationId());
            if (candidate != null) {
                // 3. 更新挂号记录状态为已预约
                candidate.setStatus(1);  // 状态设为已预约
                registrationMapper.updateById(candidate);

                // 4. 更新候补队列状态为已处理
                first.setStatus(1);  // 状态设为已处理
                first.setTransferTime(LocalDateTime.now());  // 设置转正时间
                waitingQueueMapper.updateById(first);

                // 5. 发送候补转正成功通知（区分加号场景）
                sendWaitingSuccessMessage(candidate, first, isAddQuota);

                // 6. 更新排班号源（已用号源增加）
                DoctorSchedule schedule = doctorScheduleMapper.selectById(scheduleId);
                if (schedule != null) {
                    schedule.setUsedQuota(schedule.getUsedQuota() + 1);
                    doctorScheduleMapper.updateById(schedule);  // 更新排班号源
                }
            }
        }
    }

    private void sendWaitingSuccessMessage(RegistrationRecord candidate, WaitingQueue queue) {
        // 默认不是加号场景
        sendWaitingSuccessMessage(candidate, queue, false);
    }

    private void sendWaitingSuccessMessage(RegistrationRecord candidate, WaitingQueue queue, boolean isAddQuota) {
        try {
            RegistrationDetailDTO detail = registrationMapper.selectRegistrationDetail(candidate.getRecordId());
            if (detail == null) {
                return;
            }

            // 创建候补成功的消息
            Message message = new Message();
            String userId = resolveUserIdForMessages(detail);
            if (userId == null) {
                return;
            }

            message.setUserId(userId);
            message.setAppointmentId(String.valueOf(candidate.getRecordId()));
            message.setMessageType("APPOINTMENT_WAITING_SUCCESS");
            
            // 根据是否为加号场景，设置不同的标题和备注
            if (isAddQuota) {
                message.setTitle("加号成功提醒");
            } else {
                message.setTitle("候补挂号成功提醒");
            }
            
            message.setCreatedTime(LocalDateTime.now());
            message.setIsRead(false);

            Map<String, Object> payload = new HashMap<>();
            payload.put("patient_card_no", detail.getPatientCardNo());
            payload.put("patient_name", detail.getPatientName());
            payload.put("doctor_name", detail.getDoctorName());
            payload.put("department_name", detail.getDepartmentName());
            payload.put("appointment_time", buildAppointmentTime(detail.getScheduleDate(), detail.getTimeSlot()));
            payload.put("waiting_rank", queue.getQueueRank());
            payload.put("waiting_join_time", queue.getQueueTime() != null ? DATE_TIME_FORMATTER.format(queue.getQueueTime()) : null);
            payload.put("promote_time", DATE_TIME_FORMATTER.format(LocalDateTime.now()));
            
            // 添加来源标识，便于前端区分显示
            payload.put("source_type", isAddQuota ? "add_quota" : "normal_waiting");
            
            // 根据是否为加号场景，设置不同的备注信息
            if (isAddQuota) {
                payload.put("hospital_remark", "管理员已为您增加号源，候补已转为正式号，请按时就诊");
            } else {
                payload.put("hospital_remark", "候补已转为正式号，请按时就诊");
            }

            message.setContent(objectMapper.writeValueAsString(payload));

            boolean saved = messageService.save(message);
            log.info("候补成功通知已发送，userId={}, appointmentId={}, isAddQuota={}", userId, candidate.getRecordId(), isAddQuota);

            // 候补转正后同样需要创建提醒（就诊前一天和就诊前一小时），沿用正常挂号的判断逻辑
            appointmentReminderTask.checkAndCreateImmediateReminder(detail);
        } catch (Exception e) {
            log.error("发送候补成功通知失败", e);
        }
    }

    private String resolveUserIdForMessages(RegistrationDetailDTO detail) {
        if (detail != null && detail.getPatientUserId() != null) {
            return String.valueOf(detail.getPatientUserId());
        }
        if (detail != null && detail.getPatientId() != null) {
            Patient patient = patientMapper.selectById(detail.getPatientId());
            if (patient != null && patient.getUserId() != null) {
                return String.valueOf(patient.getUserId());
            }
        }
        return null;
    }

    private String buildAppointmentTime(LocalDate date, Integer slot) {
        if (date == null) {
            return "";
        }
        String slotText = switch (slot != null ? slot : 0) {
            case 1 -> "上午(08:00-12:00)";
            case 2 -> "下午(14:00-17:00)";
            case 3 -> "晚上(18:00-20:00)";
            default -> "未指定";
        };
        return date.toString() + " " + slotText;
    }
}
