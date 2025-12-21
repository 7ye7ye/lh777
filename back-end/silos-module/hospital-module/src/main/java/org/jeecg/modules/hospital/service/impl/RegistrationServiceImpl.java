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
import org.jeecg.modules.hospital.mapper.*;
import org.jeecg.modules.hospital.service.MessageService;
import org.jeecg.modules.hospital.service.RegistrationService;
import org.jeecg.modules.hospital.service.WaitingQueueService;
import org.jeecg.modules.hospital.task.AppointmentReminderTask;
import org.jeecg.modules.hospital.vo.RegistrationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String MESSAGE_TYPE_WAITING_JOIN = "APPOINTMENT_WAITING_JOIN";
    private static final String MESSAGE_TYPE_WAITING_SUCCESS = "APPOINTMENT_WAITING_SUCCESS";
    private static final String MESSAGE_TYPE_CANCEL = "APPOINTMENT_CANCEL";
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
    private DoctorMapper doctorMapper;
    @Resource
    private DepartmentMapper departmentMapper;
    @Resource
    private AppointmentReminderTask appointmentReminderTask;
    @Resource
    private DoctorScheduleMapper doctorScheduleMapper;

    @Autowired
    private WaitingQueueService waitingQueueService;

    @Override
    public List<RegistrationType> getAllRegistrationTypes() {
        return registrationMapper.selectAllTypes();
    }

    @Override
    public List<Map<String, Object>> getDoctorSchedules(Long doctorId, String startDate, Integer days) {
        return registrationMapper.selectSchedulesByDoctor(doctorId, startDate, days);
    }

    @Override
    public Result<String> createRegistration(
            RegistrationRecord record,
            Long patientId,
            boolean joinWaitingQueue) {

        try {
            // ------------------------------
            // 1. 基础校验
            // ------------------------------
            if (record == null) {
                return Result.error("挂号信息不能为空");
            }
            if (record.getScheduleId() == null || record.getTypeId() == null) {
                return Result.error("缺少必要的挂号信息");
            }
            if (patientId == null) {
                return Result.error("未获取到患者ID");
            }

            // ------------------------------
            // 2. 校验 patientId 是否属于当前用户（不覆盖）
            // ------------------------------
            String currentUserId = resolveCurrentUserId();
            if (currentUserId != null) {
                Patient patient = patientMapper.selectById(patientId);
                if (patient == null) {
                    return Result.error("患者信息不存在");
                }
                if (!patient.getUserId().equals(Long.valueOf(currentUserId))) {
                    return Result.error("非法患者信息，请重新选择就诊人");
                }
            }

            // ✅ 只使用前端传来的 patientId
            record.setPatientId(patientId);
            log.info("本次挂号使用 patientId={}", patientId);

            // ------------------------------
            // 3. 获取排班信息
            // ------------------------------
            DoctorSchedule schedule =
                    registrationMapper.selectScheduleById(record.getScheduleId());
            if (schedule == null) {
                return Result.error("未找到对应排班信息");
            }
            record.setDoctorId(schedule.getDoctorId());

            // ------------------------------
            // 4. 防止同一患者重复预约同一排班（关键）
            // ------------------------------
            if (checkDuplicateBySchedule(patientId, schedule.getScheduleId())) {
                return Result.error("您已预约过该时段，请勿重复挂号");
            }

            // ------------------------------
            // 5. 获取挂号类型
            // ------------------------------
            RegistrationType type =
                    registrationMapper.selectTypeById(record.getTypeId());
            if (type == null) {
                return Result.error("未找到对应挂号类型");
            }

            int usedQuota = schedule.getUsedQuota() == null ? 0 : schedule.getUsedQuota();

            // ------------------------------
            // 6. 号源已满 → 候补流程
            // ------------------------------
            if (usedQuota >= type.getDailyQuota()) {

                if (!joinWaitingQueue) {
                    return Result.error("该号源已满，是否加入候补？");
                }

                record.setRegisterTime(LocalDateTime.now());
                record.setStatus(0); // 候补
                record.setVisitTime(null);
                record.setPriceOriginal(type.getPriceOriginal());
                record.setActualPrice(type.getPriceOriginal());
                record.setIsAdd(0);

                if (record.getRegistrationNo() == null) {
                    record.setRegistrationNo(generateRegistrationNo(patientId));
                }

                registrationMapper.insertRegistration(record);

                if (record.getRecordId() == null) {
                    return Result.error("候补挂号创建失败");
                }

                Result<String> waitRes = addToWaitingQueue(
                        schedule.getScheduleId(),
                        patientId,
                        record.getRecordId()
                );

                return waitRes.isSuccess()
                        ? Result.OK("当前号源已满，已加入候补队列", waitRes.getResult())
                        : Result.error("加入候补队列失败：" + waitRes.getMessage());
            }

            // ------------------------------
            // 7. 正常挂号流程
            // ------------------------------
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) {
                return Result.error("患者信息不存在");
            }

            record.setRegisterTime(LocalDateTime.now());
            record.setStatus(1); // 正常挂号
            record.setVisitTime(null);
            record.setPriceOriginal(type.getPriceOriginal());
            record.setActualPrice(
                    switch (patient.getPatientType()) {
                        case 1 -> type.getStudentPrice();
                        case 2, 3 -> type.getStaffPrice();
                        default -> type.getPriceOriginal();
                    }
            );
            record.setIsAdd(0);

            if (record.getRegistrationNo() == null) {
                record.setRegistrationNo(generateRegistrationNo(patientId));
            }

            registrationMapper.insertRegistration(record);

            // ------------------------------
            // 8. 更新排班已用号源
            // ------------------------------
            schedule.setUsedQuota(usedQuota + 1);
            registrationMapper.updateScheduleUsedQuota(schedule);

            // ------------------------------
            // 9. 成功后的通知
            // ------------------------------
            if (record.getRecordId() != null) {
                RegistrationDetailDTO detail =
                        registrationMapper.selectRegistrationDetail(record.getRecordId());
                if (detail != null) {
                    createSuccessMessage(detail);
                    appointmentReminderTask.checkAndCreateImmediateReminder(detail);
                }
            }

            return Result.OK("挂号成功");

        } catch (Exception e) {
            log.error("createRegistration error", e);
            return Result.error("挂号失败：" + e.getMessage());
        }
    }


    private Result<String> addToWaitingQueue(Long scheduleId, Long patientId,Long recordId) {
        WaitingQueue queue = new WaitingQueue();
        queue.setScheduleId(scheduleId);
        queue.setRecordId(recordId);
        queue.setPatientId(patientId);
        queue.setQueueTime(LocalDateTime.now());
        Integer maxRank = registrationMapper.selectMaxQueueRank(scheduleId);
        queue.setQueueRank(maxRank != null ? maxRank + 1 : 1);
        queue.setStatus(0);
        waitingQueueMapper.insert(queue);
        createWaitingJoinMessage(scheduleId, patientId, queue);

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
            Long resolvedPatientId = queue.getPatientId();
            String currentUserId = resolveCurrentUserId();
            if (currentUserId != null) {
                Patient patientByUserId = patientMapper.selectOne(
                        new LambdaQueryWrapper<Patient>().eq(Patient::getUserId, Long.valueOf(currentUserId)));
                if (patientByUserId != null && patientByUserId.getPatientId() != null) {
                    resolvedPatientId = patientByUserId.getPatientId();
                }
            }
            if (resolvedPatientId == null) {
                return Result.error("未找到对应患者信息，请重新登录后再试");
            }
            queue.setPatientId(resolvedPatientId);
            int existing = waitingQueueMapper.countExistingQueue(queue.getScheduleId(), queue.getPatientId());
            if (existing > 0) {
                return Result.error("您已在该排班的候补队列中");
            }
            Integer maxRank = waitingQueueMapper.selectMaxQueueRank(queue.getScheduleId());
            queue.setQueueRank(maxRank != null ? maxRank + 1 : 1);
            queue.setQueueTime(LocalDateTime.now());
            queue.setStatus(0);
            waitingQueueMapper.insert(queue);
            createWaitingJoinMessage(queue.getScheduleId(), queue.getPatientId(), queue);
            return Result.OK("已加入候补队列");
        } catch (Exception e) {
            log.error("addWaitingQueue error", e);
            return Result.error("加入候补异常：" + e.getMessage());
        }
    }

    @Override
    public boolean cancelRegistration(Long recordId, String cancelReason) {

        // 1. 校验记录是否存在
        RegistrationRecord record = registrationMapper.selectById(recordId);
        if (record == null) {
            return false;
        }

        // 若状态不是已预约(1)，不允许重复取消
        if (record.getStatus() != 1) {
            return false;
        }

        // 额外获取一次详情用于后续消息推送
        RegistrationDetailDTO detail = registrationMapper.selectRegistrationDetail(recordId);

        // 2. 设置取消状态与信息（status = 3）
        record.setStatus(3); // 3 = 已退号
        record.setCancelTime(LocalDateTime.now());
        record.setCancelReason(cancelReason);

        // 3. 更新挂号记录
        int updated = registrationMapper.updateById(record);
        if (updated <= 0) {
            return false;
        }

        // 4. 退号成功 → 排班号源 +1（即 usedQuota -1）
        DoctorSchedule schedule = registrationMapper.selectScheduleById(record.getScheduleId());
        if (schedule != null) {
            Integer used = schedule.getUsedQuota() != null ? schedule.getUsedQuota() : 0;

            // 防止出现负数
            schedule.setUsedQuota(Math.max(0, used - 1));
            registrationMapper.updateScheduleUsedQuota(schedule);
        }
        // ⭐⭐⭐ 自动候补补位
        waitingQueueService.autoFillFromQueue(record.getScheduleId(),1);




//            // 发送候补成功通知
//            RegistrationDetailDTO candidateDetail = registrationMapper.selectRegistrationDetail(candidate.getId());
//            createQueueSuccessMessage(candidateDetail);


        // 7. 发送退号成功通知
        String cancelUserId = resolveCurrentUserId();
        if (detail == null) {
            detail = registrationMapper.selectRegistrationDetail(recordId);
        }
        createCancelMessage(detail, cancelReason, cancelUserId);

        return true;
    }

    @Override
    public DoctorSchedule getScheduleDetailById(Long scheduleId) {
        if (scheduleId == null) {
            // scheduleId 为空，直接抛出异常
            throw new IllegalArgumentException("排班ID不能为空");
        }
        try {
            DoctorSchedule schedule = doctorScheduleMapper.selectById(scheduleId);
            if (schedule == null) {
                // 查询为空，抛出异常或返回 null，由调用方处理
                throw new RuntimeException("排班ID：" + scheduleId + " 对应的排班不存在");
            }
            return schedule;
        } catch (Exception e) {
            // 捕获 Mapper 查询异常
            e.printStackTrace();
            throw new RuntimeException("查询排班详情失败：" + e.getMessage(), e);
        }
    }


    @Override
    public Long getDepartmentIdBySchedule(Long scheduleId) {
        if (scheduleId == null) {
            log.warn("getDepartmentIdBySchedule called with null scheduleId");
            return null;
        }

        DoctorSchedule schedule = registrationMapper.selectScheduleById(scheduleId);
        if (schedule == null) {
            log.warn("No schedule found for scheduleId={}", scheduleId);
            return null;
        }

        Long deptId = schedule.getDeptId();
        if (deptId == null) {
            log.warn("scheduleId={} exists but deptId is null", scheduleId);
        }

        return deptId;
    }

    @Override
    public Patient getPatientDetailById(Long patientId) {
        Patient patient = registrationMapper.selectPatientById(patientId);
        if (patient == null) {
            throw new RuntimeException("患者不存在，patientId=" + patientId);
        }
        return patient;
    }



    @Override
    public List<RegistrationVO> listByDisease(String disease) {
        return registrationMapper.getByDisease(disease);
    }

    @Override
    public RegistrationDetailDTO getRegistrationDetail(Long recordId) {
        if (recordId == null) {
            return null;
        }
        return registrationMapper.selectRegistrationDetail(recordId);
    }

    private void createSuccessMessage(RegistrationDetailDTO detail) {
        createSuccessMessage(detail, null);
    }

    private void createSuccessMessage(RegistrationDetailDTO detail, String overrideUserId) {
        if (detail == null) {
            return;
        }
        try {
            Message message = new Message();
            String userId = resolveUserIdForMessages(detail, overrideUserId);
            if (StringUtils.isBlank(userId)) {
                log.warn("createSuccessMessage skip due to empty userId, recordId={}", detail.getRecordId());
                return;
            }
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
            log.error("createSuccessMessage error, recordId={}", detail.getRecordId(), e);
        }
    }

    private void createWaitingSuccessMessage(RegistrationDetailDTO detail, WaitingQueue queue, String overrideUserId) {
        try {
            Message message = new Message();
            String userId = resolveUserIdForMessages(detail, overrideUserId);
            if (StringUtils.isBlank(userId)) {
                log.warn("createWaitingSuccessMessage skip due to empty userId, recordId={}", detail != null ? detail.getRecordId() : null);
                return;
            }
            message.setUserId(userId);
            message.setAppointmentId(String.valueOf(detail.getRecordId()));
            message.setMessageType(MESSAGE_TYPE_WAITING_SUCCESS);
            message.setTitle("候补挂号成功提醒");
            message.setCreatedTime(LocalDateTime.now());
            message.setIsRead(false);

            Map<String, Object> payload = new HashMap<>();
            payload.put("patient_card_no", detail.getPatientCardNo());
            payload.put("patient_name", detail.getPatientName());
            payload.put("doctor_name", detail.getDoctorName());
            payload.put("department_name", detail.getDepartmentName());
            payload.put("appointment_time", buildAppointmentTime(detail.getScheduleDate(), detail.getTimeSlot()));
            payload.put("waiting_rank", queue != null ? queue.getQueueRank() : null);
            payload.put("waiting_join_time", queue != null && queue.getQueueTime() != null ? DATE_TIME_FORMATTER.format(queue.getQueueTime()) : null);
            payload.put("promote_time", DATE_TIME_FORMATTER.format(LocalDateTime.now()));
            payload.put("hospital_remark", "候补已转为正式号，请按时就诊");

            message.setContent(objectMapper.writeValueAsString(payload));

            boolean saved = messageService.save(message);
            log.info("waiting message saved={}, userId={}, appointmentId={}", saved, userId, detail.getRecordId());
        } catch (Exception e) {
            log.error("createWaitingSuccessMessage error, recordId={}", detail != null ? detail.getRecordId() : null, e);
        }
    }

    private void createWaitingJoinMessage(Long scheduleId, Long patientId, WaitingQueue queue) {
        try {
            DoctorSchedule schedule = registrationMapper.selectScheduleById(scheduleId);
            if (schedule == null) {
                return;
            }
            Patient patient = patientMapper.selectById(patientId);
            Doctor doctor = schedule.getDoctorId() != null ? doctorMapper.selectById(schedule.getDoctorId()) : null;
            Department department = schedule.getDeptId() != null ? departmentMapper.selectById(schedule.getDeptId()) : null;

            Message message = new Message();
            String currentUserId = resolveCurrentUserId();
            String userId = currentUserId;
            if (StringUtils.isBlank(userId) && patient != null && patient.getUserId() != null) {
                userId = String.valueOf(patient.getUserId());
            }
            if (StringUtils.isBlank(userId)) {
                userId = "262";
            }
            message.setUserId(userId);
            message.setAppointmentId("WAITING_" + (queue != null && queue.getQueueId() != null ? queue.getQueueId() : scheduleId));
            message.setMessageType(MESSAGE_TYPE_WAITING_JOIN);
            message.setTitle("候补排队成功提醒");
            message.setCreatedTime(LocalDateTime.now());
            message.setIsRead(false);

            Map<String, Object> payload = new HashMap<>();
            payload.put("patient_card_no", patient != null ? patient.getOutpatientNumber() : null);
            payload.put("patient_name", patient != null ? patient.getPatientName() : null);
            payload.put("doctor_name", doctor != null ? doctor.getDoctorName() : null);
            payload.put("department_name", department != null ? department.getDeptName() : null);
            payload.put("appointment_time", buildAppointmentTime(schedule.getScheduleDate(), schedule.getTimeSlot()));
            payload.put("waiting_rank", queue != null ? queue.getQueueRank() : null);
            payload.put("waiting_join_time", queue != null && queue.getQueueTime() != null ? DATE_TIME_FORMATTER.format(queue.getQueueTime()) : null);
            payload.put("hospital_remark", "您已成功加入候补队列，系统将在有号源时自动通知");

            message.setContent(objectMapper.writeValueAsString(payload));
            boolean saved = messageService.save(message);
            log.info("waiting join message saved={}, userId={}, scheduleId={}", saved, userId, scheduleId);
        } catch (Exception e) {
            log.error("createWaitingJoinMessage error, scheduleId={}, patientId={}", scheduleId, patientId, e);
        }
    }

    private void createCancelMessage(RegistrationDetailDTO detail, String cancelReason, String overrideUserId) {
        if (detail == null) {
            log.warn("createCancelMessage skip because detail is null");
            return;
        }
        try {
            Message message = new Message();
            String userId = StringUtils.isNotBlank(overrideUserId)
                    ? overrideUserId
                    : resolveUserIdForDetail(detail, null);
            if (StringUtils.isBlank(userId)) {
                log.warn("createCancelMessage skip due to empty userId, recordId={}", detail.getRecordId());
                return;
            }
            message.setUserId(userId);
            message.setAppointmentId(String.valueOf(detail.getRecordId()));
            message.setMessageType(MESSAGE_TYPE_CANCEL);
            message.setTitle("退号成功提醒");
            message.setCreatedTime(LocalDateTime.now());
            message.setIsRead(false);

            Map<String, Object> payload = new HashMap<>();
            payload.put("patient_card_no", detail.getPatientCardNo());
            payload.put("patient_name", detail.getPatientName());
            payload.put("doctor_name", detail.getDoctorName());
            payload.put("department_name", detail.getDepartmentName());
            payload.put("appointment_time", buildAppointmentTime(detail.getScheduleDate(), detail.getTimeSlot()));
            payload.put("cancel_time", DATE_TIME_FORMATTER.format(LocalDateTime.now()));
            payload.put("cancel_reason", StringUtils.defaultIfBlank(cancelReason, "患者主动取消"));
            payload.put("hospital_remark", "您的号源已释放，如需就诊请重新预约");

            message.setContent(objectMapper.writeValueAsString(payload));

            boolean saved = messageService.save(message);
            log.info("cancel message saved={}, userId={}, appointmentId={}", saved, userId, detail.getRecordId());
        } catch (Exception e) {
            log.error("createCancelMessage error, recordId={}", detail.getRecordId(), e);
        }
    }

    private String resolveUserIdForDetail(RegistrationDetailDTO detail, Patient fallbackPatient) {
        if (detail != null && detail.getPatientUserId() != null) {
            return String.valueOf(detail.getPatientUserId());
        }
        if (detail != null && detail.getPatientId() != null) {
            Patient patient = patientMapper.selectById(detail.getPatientId());
            if (patient != null && patient.getUserId() != null) {
                return String.valueOf(patient.getUserId());
            }
        }
        if (fallbackPatient != null && fallbackPatient.getUserId() != null) {
            return String.valueOf(fallbackPatient.getUserId());
        }
        return null;
    }

    private String resolveUserIdForMessages(RegistrationDetailDTO detail, String overrideUserId) {
        if (StringUtils.isNotBlank(overrideUserId)) {
            return overrideUserId;
        }
        String currentUserId = resolveCurrentUserId();
        if (StringUtils.isNotBlank(currentUserId)) {
            return currentUserId;
        }
        return resolveUserIdForDetail(detail, null);
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
