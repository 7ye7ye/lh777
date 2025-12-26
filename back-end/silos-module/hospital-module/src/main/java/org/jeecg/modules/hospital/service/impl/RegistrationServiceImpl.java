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
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 挂号业务实现类
 */
@Slf4j
@Service
@DS("hospital")
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

    // 【Redis优化新增】引入 RedisTemplate
    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Result<String> createRegistration(RegistrationRecord record, Long patientId, boolean joinWaitingQueue) {
        // Redis Key 定义 (用于高并发预扣减)
        String redisKey = null;
        boolean redisDecremented = false;

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

            // 【Redis优化核心逻辑】
            // 只有当用户不是为了加入候补时，才进行 Redis 库存预检查
            // 如果是候补，不需要扣减 Redis 缓存，直接走后续逻辑
            if (!joinWaitingQueue) {
                redisKey = "doctor_schedule:quota:" + record.getScheduleId();
                // 预扣减
                Long stock = stringRedisTemplate.opsForValue().decrement(redisKey);
                if (stock != null && stock < 0) {
                    // 扣减后小于0，说明库存不足
                    // 立即回补
                    stringRedisTemplate.opsForValue().increment(redisKey);

                    // 询问用户是否加入候补
                    // 注意：这里提前返回，避免了后面查数据库，起到了“Redis挡板”的作用
                    return Result.error("号源已满，是否加入候补？");
                }
                // 标记已扣减，用于后续如果发生异常回滚
                redisDecremented = true;
            }

            // ------------------------------
            // 2. 校验 patientId 是否属于当前用户
            // ------------------------------
            String currentUserId = resolveCurrentUserId();
            Long actualPatientId = patientId; // 默认为传入的ID

            if (currentUserId != null) {
                Patient patient = patientMapper.selectById(patientId);
                if (patient == null) {
                    throw new RuntimeException("患者信息不存在"); // 使用 Exception 触发回滚
                }

                // 如果当前登录用户和患者绑定的userId不一致
                if (!patient.getUserId().equals(Long.valueOf(currentUserId))) {
                    throw new RuntimeException("非法患者信息，请重新选择就诊人");
                }
            }

            // ✅ 只使用前端传来的 patientId
            record.setPatientId(actualPatientId);
            log.info("本次挂号使用 patientId={}", actualPatientId);

            // ------------------------------
            // 3. 获取排班信息 + 预约截止时间校验
            // ------------------------------
            DoctorSchedule schedule = registrationMapper.selectScheduleById(record.getScheduleId());
            if (schedule == null) {
                throw new RuntimeException("未找到对应排班信息");
            }
            record.setDoctorId(schedule.getDoctorId());

            // 3.1 就诊前2小时内不允许再预约该时段（含已过期）
            if (schedule.getScheduleDate() != null && schedule.getTimeSlot() != null) {
                LocalTime baseTime;
                switch (schedule.getTimeSlot()) {
                    case 1 -> baseTime = LocalTime.of(8, 0); // 上午
                    case 2 -> baseTime = LocalTime.of(14, 0); // 下午
                    case 3 -> baseTime = LocalTime.of(18, 0); // 晚上
                    default -> baseTime = null;
                }
                if (baseTime != null) {
                    LocalDateTime slotStart = LocalDateTime.of(schedule.getScheduleDate(), baseTime);
                    // 当前时间晚于「开始时间前2小时」则视为已截止预约
                    if (LocalDateTime.now().isAfter(slotStart.minusHours(2))) {
                        throw new RuntimeException("该时段预约已截止，请选择其他时间段");
                    }
                }
            }

            // ------------------------------
            // 4. 防止同一患者重复预约同一排班（关键）
            // ------------------------------
            if (checkDuplicateBySchedule(actualPatientId, schedule.getScheduleId())) {
                throw new RuntimeException("您已预约过该时段，请勿重复挂号");
            }

            // ------------------------------
            // 5. 获取挂号类型
            // ------------------------------
            RegistrationType type = registrationMapper.selectTypeById(record.getTypeId());
            if (type == null) {
                throw new RuntimeException("未找到对应挂号类型");
            }

            // ☆☆☆━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 核心并发控制：尝试原子更新库存
            // 尝试 +1，数据库会保证只有 quota < max 时才成功，且返回 1
            // ☆☆☆━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 【注意】如果是 joinWaitingQueue，则不需要扣减数据库库存
            int affectedRows = 0;
            if (!joinWaitingQueue) {
                affectedRows = registrationMapper.incrementUsedQuota(schedule.getScheduleId());
            }

            // 如果更新行数为 0 且不是候补，说明数据库层面也没号了
            if (!joinWaitingQueue && affectedRows == 0) {
                // 理论上 Redis 应该已经拦截了，但如果 Redis 数据不一致漏进来了，数据库这里是最后一道防线
                // 此时应该让用户去候补
                // 但前面 Redis 扣减成功了，所以这里要回滚 Redis（虽然后面 catch 会处理，但为了逻辑清晰）
                throw new RuntimeException("号源已满(DB check failed)，是否加入候补？");
            }

            // 如果是主动加入候补，或者上面逻辑跳转过来的（这里实际不会跳过来，因为上面 throw 了）
            // 但为了保留原逻辑的 structure
            if (joinWaitingQueue) {
                // ------------------------------
                // 处理候补逻辑 (原样保留)
                // ------------------------------
                log.info("用户选择加入候补，写入候补挂号记录…");

                // 写入“候补挂号记录”
                record.setRegisterTime(LocalDateTime.now());
                record.setStatus(0); // 候补
                record.setVisitTime(null);
                record.setPriceOriginal(type.getPriceOriginal());
                record.setActualPrice(type.getPriceOriginal());
                record.setIsAdd(0);

                if (record.getRegistrationNo() == null) {
                    record.setRegistrationNo(generateRegistrationNo(actualPatientId));
                }

                registrationMapper.insertRegistration(record);

                if (record.getRecordId() == null) {
                    throw new RuntimeException("候补挂号记录创建失败");
                }

                // 加入候补队列
                Result<String> waitRes = addToWaitingQueue(schedule.getScheduleId(), actualPatientId,
                        record.getRecordId());

                if (waitRes.isSuccess()) {
                    return Result.OK("当前号源已满，但您已成功加入候补队列", waitRes.getResult());
                } else {
                    throw new RuntimeException("加入候补队列失败：" + waitRes.getMessage());
                }
            }

            // ☆☆☆━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 更新成功（拿到号了），继续写入挂号记录
            // ☆☆☆━━━━━━━━━━━━━━━━━━━━━━━━━━
            try {
                Patient patient = patientMapper.selectById(actualPatientId);
                if (patient == null) {
                    throw new RuntimeException("患者信息未找到");
                }

                record.setRegisterTime(LocalDateTime.now());
                record.setStatus(1); // 正常挂号
                record.setVisitTime(null);
                record.setPriceOriginal(type.getPriceOriginal());
                record.setActualPrice(
                        switch (patient.getPatientType()) {
                            case 1 -> type.getStudentPrice();
                            case 2, 3 -> type.getStaffPrice();
                            default -> type.getPriceOriginal(); // 恢复了原价/折扣逻辑
                        });

                if (record.getRegistrationNo() == null) {
                    record.setRegistrationNo(generateRegistrationNo(actualPatientId));
                }
                if (record.getIsAdd() == null) {
                    record.setIsAdd(0);
                }

                registrationMapper.insertRegistration(record);

                // 成功后的消息与提醒
                if (record.getRecordId() != null) {
                    RegistrationDetailDTO detail = registrationMapper.selectRegistrationDetail(record.getRecordId());
                    if (detail != null) {
                        createSuccessMessage(detail);
                        appointmentReminderTask.checkAndCreateImmediateReminder(detail);
                    }
                }

                // 【Redis优化】一切顺利，不需要回滚 Redis
                return Result.OK("挂号成功");

            } catch (Exception e) {
                // 如果后续步骤失败（比如写记录报错），事务会回滚
                log.error("Create registration record failed", e);
                throw e; // 抛出异常触发事务回滚
            }

        } catch (Exception e) {
            log.error("createRegistration error", e);
            // 手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            // 【Redis优化】发生任何异常（数据库回滚了），Redis 也要回滚（补库存）
            if (redisKey != null && redisDecremented) {
                stringRedisTemplate.opsForValue().increment(redisKey);
                log.info("Rollback Redis Quota for key: {}", redisKey);
            }

            // 构造友好的错误提示
            String msg = e.getMessage();
            if (msg == null)
                msg = "挂号失败，请稍后重试";
            // 如果是特定的业务异常（如号源已满），可以特殊处理，这里直接返回错误
            return Result.error(msg);
        }
    }

    private Result<String> addToWaitingQueue(Long scheduleId, Long patientId, Long recordId) {
        WaitingQueue queue = new WaitingQueue();
        queue.setScheduleId(scheduleId);
        queue.setRecordId(recordId);
        queue.setPatientId(patientId);
        // 记录对应的候补挂号记录 ID，供自动补位时查找 RegistrationRecord 使用
        queue.setRecordId(recordId);
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

    /**
     * 检查同一患者在当前排班对应的科室、同一天是否已有其他挂号记录
     * <p>
     * 核心规则：
     * - 通过 scheduleId 反查出该排班所在的科室（dept_id）和排班日期（schedule_date）；
     * - 统计该患者在同一科室、同一天、所有未退号(status != 3) 的挂号记录数量；
     * - 若数量 > 0，则认为已经在该科室预约过一次，当天再次预约需要前端给出确认提示。
     * </p>
     */
    @Override
    public boolean checkDeptLimitForSchedule(Long patientId, Long scheduleId) {
        if (patientId == null || scheduleId == null) {
            return false;
        }
        Integer count = registrationMapper.countDeptRegistrationsForDay(patientId, scheduleId);
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
        waitingQueueService.autoFillFromQueue(record.getScheduleId(), 1);

        // // 发送候补成功通知
        // RegistrationDetailDTO candidateDetail =
        // registrationMapper.selectRegistrationDetail(candidate.getId());
        // createQueueSuccessMessage(candidateDetail);

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
        System.out.println("查询患者 patientId = " + patientId);
        Patient patient = registrationMapper.selectPatientById(patientId);
        System.out.println("查询结果 = " + patient);
        if (patient == null) {
            throw new RuntimeException("患者不存在，patientId=" + patientId);
        }
        return patient;
    }

    @Override
    public int getPatientTypeById(Long patientId) {
        System.out.println("查询患者类型 patientId = " + patientId);
        Integer patientType = registrationMapper.selectPatientTypeById(patientId);
        System.out.println("查询结果 patientType = " + patientType);
        if (patientType == null) {
            throw new RuntimeException("患者不存在，patientId=" + patientId);
        }
        return patientType;
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

    private void createWaitingJoinMessage(Long scheduleId, Long patientId, WaitingQueue queue) {
        try {
            DoctorSchedule schedule = registrationMapper.selectScheduleById(scheduleId);
            if (schedule == null) {
                return;
            }
            Patient patient = patientMapper.selectById(patientId);
            Doctor doctor = schedule.getDoctorId() != null ? doctorMapper.selectById(schedule.getDoctorId()) : null;
            Department department = schedule.getDeptId() != null ? departmentMapper.selectById(schedule.getDeptId())
                    : null;

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
            message.setAppointmentId(
                    "WAITING_" + (queue != null && queue.getQueueId() != null ? queue.getQueueId() : scheduleId));
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
            payload.put("waiting_join_time",
                    queue != null && queue.getQueueTime() != null ? DATE_TIME_FORMATTER.format(queue.getQueueTime())
                            : null);
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

    @Override
    public void sendBatchScheduleCancellationMessages(List<RegistrationRecord> records, String reason) {
        if (records == null || records.isEmpty()) {
            return;
        }
        log.info("开始批量发送排班调整通知，共 {} 条记录，原因：{}", records.size(), reason);
        for (RegistrationRecord record : records) {
            try {
                // 跳过非正常预约状态（只通知候补(0)和已预约(1)的用户）
                // if (record.getStatus() != 1 && record.getStatus() != 0) continue;
                // 但外面传进来的可能已经是筛选过的，这里再保险起见查一次详情

                RegistrationDetailDTO detail = registrationMapper.selectRegistrationDetail(record.getRecordId());
                if (detail == null)
                    continue;

                Message message = new Message();
                String userId = resolveUserIdForDetail(detail, null);

                if (StringUtils.isBlank(userId)) {
                    log.warn("无法获取用户ID，跳过通知 sendBatchScheduleCancellationMessages recordId={}", record.getRecordId());
                    continue;
                }

                message.setUserId(userId);
                message.setAppointmentId(String.valueOf(detail.getRecordId()));
                // 复用 APPOINTMENT_CANCEL 类型以保证前端卡片显示正常
                message.setMessageType(MESSAGE_TYPE_CANCEL);
                message.setTitle("排班调整提醒");
                message.setCreatedTime(LocalDateTime.now());
                message.setIsRead(false);

                Map<String, Object> payload = new HashMap<>();
                payload.put("patient_card_no", detail.getPatientCardNo());
                payload.put("patient_name", detail.getPatientName());
                payload.put("doctor_name", detail.getDoctorName());
                payload.put("department_name", detail.getDepartmentName());
                payload.put("appointment_time", buildAppointmentTime(detail.getScheduleDate(), detail.getTimeSlot()));
                payload.put("cancel_time", DATE_TIME_FORMATTER.format(LocalDateTime.now()));
                payload.put("cancel_reason", StringUtils.defaultIfBlank(reason, "医生排班调整"));
                payload.put("hospital_remark", "因" + StringUtils.defaultIfBlank(reason, "医生排班调整") + "，您的预约已被取消，请重新预约");

                try {
                    message.setContent(objectMapper.writeValueAsString(payload));
                } catch (JsonProcessingException e) {
                    log.error("serialize message content failed", e);
                    message.setContent("{}");
                }

                boolean saved = messageService.save(message);
                log.info("排班调整通知已发送 saved={}, userId={}, recordId={}", saved, userId, detail.getRecordId());

            } catch (Exception e) {
                log.error("发送排班调整通知失败 recordId={}", record.getRecordId(), e);
            }
        }
    }
}
