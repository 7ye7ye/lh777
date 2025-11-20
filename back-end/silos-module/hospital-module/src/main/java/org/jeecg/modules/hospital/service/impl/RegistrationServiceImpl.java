package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.modules.hospital.dto.RegistrationDetailDTO;
import org.jeecg.modules.hospital.entity.*;
import org.jeecg.modules.hospital.mapper.HosUserMapper;
import org.jeecg.modules.hospital.mapper.PatientMapper;
import org.jeecg.modules.hospital.mapper.RegistrationMapper;
import org.jeecg.modules.hospital.mapper.WaitingQueueMapper;
import org.jeecg.modules.hospital.service.MessageService;
import org.jeecg.modules.hospital.service.RegistrationService;
import org.jeecg.modules.hospital.task.AppointmentReminderTask;
import org.jeecg.modules.hospital.vo.RegistrationVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 挂号业务实现类
 */
@Slf4j
@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final String DEFAULT_HOSPITAL_ADDRESS = "北京市西直门外上园村3号 · 北京交通大学社区卫生服务中心";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private RegistrationMapper registrationMapper;
    @Resource
    private WaitingQueueMapper waitingQueueMapper;
    @Resource
    private PatientMapper patientMapper;
    @Resource
    private HosUserMapper hosUserMapper;
    @Resource
    private MessageService messageService;
    @Resource
    private AppointmentReminderTask appointmentReminderTask;

    @Override
    public List<RegistrationType> getAllRegistrationTypes() {
        return registrationMapper.selectAllTypes();
    }

    @Override
    public List<Map<String, Object>> getDoctorSchedules(Long doctorId, String startDate, Integer days) {
        return registrationMapper.selectSchedulesByDoctor(doctorId, startDate, days);
    }

    @Override
    public Result<String> createRegistration(RegistrationRecord record, Long patientId, boolean joinWaitingQueue) {
        try {
            if (record.getScheduleId() == null || record.getTypeId() == null) {
                return Result.error("缺少必要的挂号信息");
            }
            
            // ⭐ 关键修改：优先根据Token解析的userId查找patientId，确保数据一致性
            Long actualPatientId = patientId;
            String currentUserId = resolveCurrentUserId();
            if (currentUserId != null) {
                // 根据当前登录用户的userId查找对应的patientId
                Patient patientByUserId = patientMapper.selectOne(
                    new LambdaQueryWrapper<Patient>().eq(Patient::getUserId, Long.valueOf(currentUserId)));
                if (patientByUserId != null && patientByUserId.getPatientId() != null) {
                    actualPatientId = patientByUserId.getPatientId();
                    log.info("根据Token解析的userId={}找到对应的patientId={}", currentUserId, actualPatientId);
                } else {
                    log.warn("根据Token解析的userId={}未找到对应的patient记录，使用前端传递的patientId={}", currentUserId, patientId);
                }
            } else {
                log.warn("无法从Token解析userId，使用前端传递的patientId={}", patientId);
            }
            
            if (actualPatientId == null) {
                return Result.error("未获取到患者ID，请登录后再挂号");
            }
            record.setPatientId(actualPatientId);

            DoctorSchedule schedule = registrationMapper.selectScheduleById(record.getScheduleId());
            if (schedule == null) {
                return Result.error("未找到对应排班信息");
            }
            record.setDoctorId(schedule.getDoctorId());

            if (checkDuplicateBySchedule(patientId, schedule.getScheduleId())) {
                return Result.error("您已预约过该时段，请勿重复挂号");
            }

            RegistrationType type = registrationMapper.selectTypeById(record.getTypeId());
            if (type == null) {
                return Result.error("未找到对应挂号类型");
            }

            int usedQuota = schedule.getUsedQuota() != null ? schedule.getUsedQuota() : 0;
            if (usedQuota >= type.getDailyQuota()) {
                if (!joinWaitingQueue) {
                    return Result.error("该号源已满，是否加入候补？");
                }
                return addToWaitingQueue(schedule.getScheduleId(), patientId);
            }

            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) {
                return Result.error("患者信息未找到");
            }

            record.setRegisterTime(LocalDateTime.now());
            record.setStatus(1);
            record.setVisitTime(null);
            record.setPriceOriginal(type.getPriceOriginal());
            record.setActualPrice(switch (patient.getPatientType()) {
                case 1 -> type.getStudentPrice();
                case 2, 3 -> type.getStaffPrice();
                default -> type.getPriceOriginal();
            });
            if (record.getRegistrationNo() == null) {
                record.setRegistrationNo(generateRegistrationNo(patientId));
            }
            if (record.getIsAdd() == null) {
                record.setIsAdd(0);
            }

            registrationMapper.insertRegistration(record);

            schedule.setUsedQuota(usedQuota + 1);
            registrationMapper.updateScheduleUsedQuota(schedule);

            if (record.getRecordId() != null) {
                RegistrationDetailDTO detail = registrationMapper.selectRegistrationDetail(record.getRecordId());
                if (detail != null) {
                    createSuccessMessage(detail);
                    appointmentReminderTask.checkAndCreateImmediateReminder(detail);
                } else {
                    log.warn("Registration detail not found for recordId={}, skip message/reminder", record.getRecordId());
                }
            } else {
                log.warn("Registration saved but recordId is null, skip message creation");
            }
            return Result.OK("挂号成功");
        } catch (Exception e) {
            log.error("createRegistration error", e);
            return Result.error("挂号失败：" + e.getMessage());
        }
    }

    private Result<String> addToWaitingQueue(Long scheduleId, Long patientId) {
        WaitingQueue queue = new WaitingQueue();
        queue.setScheduleId(scheduleId);
        queue.setPatientId(patientId);
        queue.setQueueTime(LocalDateTime.now());
        Integer maxRank = registrationMapper.selectMaxQueueRank(scheduleId);
        queue.setQueueRank(maxRank != null ? maxRank + 1 : 1);
        queue.setStatus(0);
        waitingQueueMapper.insert(queue);
        return Result.OK("已加入候补队列");
    }

    @Override
    public List<RegistrationRecord> getRecordsByPatientId(Long patientId) {
        QueryWrapper<RegistrationRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("patient_id", patientId);
        wrapper.orderByDesc("register_time");
        return registrationMapper.selectList(wrapper);
    }

    @Override
    public boolean checkDuplicateBySchedule(Long patientId, Long scheduleId) {
        Integer count = registrationMapper.checkDuplicateBySchedule(patientId, scheduleId);
        return count != null && count > 0;
    }

    @Override
    public Result<String> addWaitingQueue(WaitingQueue queue) {
        if (queue == null || queue.getPatientId() == null || queue.getScheduleId() == null) {
            return Result.error("缺少必要信息");
        }
        try {
            int existing = waitingQueueMapper.countExistingQueue(queue.getScheduleId(), queue.getPatientId());
            if (existing > 0) {
                return Result.error("您已在该排班的候补队列中");
            }
            Integer maxRank = waitingQueueMapper.selectMaxQueueRank(queue.getScheduleId());
            queue.setQueueRank(maxRank != null ? maxRank + 1 : 1);
            queue.setQueueTime(LocalDateTime.now());
            queue.setStatus(0);
            waitingQueueMapper.insert(queue);
            return Result.OK("已加入候补队列");
        } catch (Exception e) {
            log.error("addWaitingQueue error", e);
            return Result.error("加入候补异常：" + e.getMessage());
        }
    }

    @Override
    public List<RegistrationVO> listByDepartment(Long deptId) {
        return registrationMapper.getByDepartment(deptId);
    }

    @Override
    public List<RegistrationVO> listByDisease(String disease) {
        return registrationMapper.getByDisease(disease);
    }

    @Override
    public RegistrationDetailDTO getRegistrationDetail(Long recordId) {
        return registrationMapper.selectRegistrationDetail(recordId);
    }

    private void createSuccessMessage(RegistrationDetailDTO detail) {
        try {
            if (detail == null || detail.getRecordId() == null) {
                return;
            }
            log.info("createSuccessMessage detail={}", detail);

            Message message = new Message();
            String currentUserId = resolveCurrentUserId();
            String userId = currentUserId != null ? currentUserId :
                    (detail.getPatientUserId() != null ? String.valueOf(detail.getPatientUserId()) : "262");
            message.setUserId(userId);
            message.setAppointmentId(String.valueOf(detail.getRecordId()));
            message.setMessageType("APPOINTMENT_SUCCESS");
            message.setTitle("预约挂号成功提醒");
            message.setCreatedTime(LocalDateTime.now());
            message.setIsRead(false);

            Map<String, Object> payload = new HashMap<>();
            payload.put("patient_card_no", detail.getPatientCardNo());
            payload.put("patient_name", detail.getPatientName());
            payload.put("doctor_name", detail.getDoctorName());
            payload.put("department_name", detail.getDepartmentName());
            payload.put("appointment_time", buildAppointmentTime(detail.getScheduleDate(), detail.getTimeSlot()));
            payload.put("hospital_remark", "请提前15分钟到达诊室候诊");

            try {
                message.setContent(objectMapper.writeValueAsString(payload));
            } catch (JsonProcessingException e) {
                log.error("serialize message content failed", e);
                message.setContent("{}");
            }

            boolean saved = messageService.save(message);
            log.info("message saved={}, userId={}, appointmentId={}", saved, userId, detail.getRecordId());
        } catch (Exception e) {
            log.error("createSuccessMessage error, recordId={}", detail != null ? detail.getRecordId() : null, e);
        }
    }

    private String resolveCurrentUserId() {
        try {
            HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
            if (request == null) {
                return null;
            }
            String token = TokenUtils.getTokenByRequest(request);
            if (StringUtils.isBlank(token)) {
                return null;
            }
            String username = JwtUtil.getUsername(token);
            if (StringUtils.isBlank(username)) {
                return null;
            }
            HosUser hosUser = hosUserMapper.selectOne(
                    new LambdaQueryWrapper<HosUser>().eq(HosUser::getUserAccount, username));
            if (hosUser != null && hosUser.getUserId() != null) {
                return String.valueOf(hosUser.getUserId());
            }
        } catch (Exception e) {
            log.warn("resolveCurrentUserId error", e);
        }
        return null;
    }

    private String generateRegistrationNo(Long patientId) {
        return "REG" + (patientId != null ? patientId : 0) + System.currentTimeMillis();
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
