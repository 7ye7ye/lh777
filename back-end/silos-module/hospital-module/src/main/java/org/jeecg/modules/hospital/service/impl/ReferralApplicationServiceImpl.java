package org.jeecg.modules.hospital.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import org.jeecg.modules.hospital.dto.referral.ReferralApplyRequest;
import org.jeecg.modules.hospital.dto.referral.ReferralAttachmentPayload;
import org.jeecg.modules.hospital.dto.referral.ReferralCancelRequest;
import org.jeecg.modules.hospital.dto.referral.ReferralListQuery;
import org.jeecg.modules.hospital.dto.referral.ReferralReviewRequest;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.entity.ReferralApplication;
import org.jeecg.modules.hospital.entity.ReferralAttachment;
import org.jeecg.modules.hospital.enums.ReferralQuotaAction;
import org.jeecg.modules.hospital.enums.ReferralSourceType;
import org.jeecg.modules.hospital.enums.ReferralStatus;
import org.jeecg.modules.hospital.enums.ReferralTargetType;
import org.jeecg.modules.hospital.entity.RegistrationRecord;
import org.jeecg.modules.hospital.mapper.DepartmentMapper;
import org.jeecg.modules.hospital.mapper.DoctorScheduleMapper;
import org.jeecg.modules.hospital.mapper.ReferralApplicationMapper;
import org.jeecg.modules.hospital.mapper.RegistrationRecordMapper;
import org.jeecg.modules.hospital.service.IReferralApplicationService;
import org.jeecg.modules.hospital.vo.referral.ReferralOptionsVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@DS("hospital")
@RequiredArgsConstructor
public class ReferralApplicationServiceImpl extends ServiceImpl<ReferralApplicationMapper, ReferralApplication>
    implements IReferralApplicationService {

    /** 默认内部转诊可占用的最大候诊号源，避免依赖 DB 字段 */
    private static final int INTERNAL_SCHEDULE_CAPACITY = 20;

    private final DoctorScheduleMapper doctorScheduleMapper;
    private final DepartmentMapper departmentMapper;
    private final RegistrationRecordMapper registrationRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReferralApplication createReferral(ReferralApplyRequest request) {
        // 如果有关联的挂号记录ID，检查是否已有转诊申请
        Long registrationRecordId = request.getRegistrationRecordId();
        Long requestPatientId = request.getPatientId();
        
        if (registrationRecordId != null) {
            // 验证就诊记录是否存在，以及是否属于正确的患者
            RegistrationRecord registrationRecord = registrationRecordMapper.selectById(registrationRecordId);
            if (registrationRecord == null) {
                throw new IllegalStateException("就诊记录不存在");
            }
            
            // 如果传递了patientId，验证就诊记录是否属于该患者
            if (requestPatientId != null) {
                Long recordPatientId = registrationRecord.getPatientId();
                if (!requestPatientId.equals(recordPatientId)) {
                    throw new IllegalStateException("就诊记录不属于指定的患者，无法申请转诊");
                }
            }
            
            // 检查是否已有转诊申请
            LambdaQueryWrapper<ReferralApplication> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(ReferralApplication::getRegistrationRecordId, registrationRecordId)
                    .and(wrapper -> wrapper
                            .ne(ReferralApplication::getStatus, ReferralStatus.CANCELLED.name())
                            .ne(ReferralApplication::getStatus, ReferralStatus.REJECTED.name())
                    );
            long existingCount = count(checkWrapper);
            if (existingCount > 0) {
                throw new IllegalStateException("该就诊记录已经申请过转诊，一次就诊记录只能申请一次转诊");
            }
        }
        
        ReferralApplication entity = new ReferralApplication();
        entity.setPatientName(request.getPatientName());
        entity.setGender(request.getGender());
        entity.setAge(request.getAge());
        entity.setPhone(request.getPhone());
        entity.setSymptoms(request.getSymptoms());
        entity.setMedicalHistory(request.getMedicalHistory());
        entity.setReason(request.getReason());
        entity.setSourceType(determineSourceType(request.getSourceType()).name());
        entity.setTargetType(determineTargetType(request.getTargetType()).name());
        entity.setApplyTime(LocalDateTime.now());
        entity.setStatus(ReferralStatus.PENDING.name());
        entity.setReferralCode(generateReferralCode());
        
        // 设置关联的挂号记录ID
        if (registrationRecordId != null) {
            entity.setRegistrationRecordId(registrationRecordId);
        }

        if (CollUtil.isNotEmpty(request.getAttachments())) {
            entity.setAttachments(convertAttachments(request.getAttachments()));
        }

        if (ReferralTargetType.INTERNAL.name().equals(entity.getTargetType())) {
            entity.setTargetDeptId(request.getTargetDeptId());
            String deptName = request.getTargetDeptName();
            if (entity.getTargetDeptId() != null) {
                Department dept = departmentMapper.selectById(entity.getTargetDeptId());
                if (dept != null) {
                    deptName = dept.getDeptName();
                }
            }
            entity.setTargetDeptName(deptName);
            applyInternalRouting(entity);
        } else {
            entity.setTargetHospitalName(request.getTargetHospitalName());
            entity.setQuotaAction(ReferralQuotaAction.EXTERNAL_TRANSFER.name());
        }

        save(entity);
        return entity;
    }

    @Override
    public Page<ReferralApplication> queryPage(ReferralListQuery query) {
        Page<ReferralApplication> page = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<ReferralApplication> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getStatus()) && !"全部".equals(query.getStatus())) {
            wrapper.eq(ReferralApplication::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getPhone())) {
            wrapper.eq(ReferralApplication::getPhone, query.getPhone());
        }
        if (StringUtils.hasText(query.getPatientName())) {
            wrapper.like(ReferralApplication::getPatientName, query.getPatientName());
        }
        if (query.getDeptId() != null) {
            wrapper.eq(ReferralApplication::getTargetDeptId, query.getDeptId());
        }
        wrapper.orderByDesc(ReferralApplication::getApplyTime);
        return page(page, wrapper);
    }

    @Override
    public ReferralApplication getDetail(Long id) {
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReferralApplication review(ReferralReviewRequest request) {
        ReferralApplication referral = getById(request.getId());
        if (referral == null) {
            throw new IllegalArgumentException("转诊申请不存在");
        }
        if (ReferralStatus.CANCELLED.name().equals(referral.getStatus())) {
            return referral;
        }

        LocalDateTime now = LocalDateTime.now();
        referral.setReviewDoctor(request.getReviewDoctor());
        referral.setReviewTime(now);
        if ("APPROVE".equalsIgnoreCase(request.getDecision())) {
            referral.setStatus(ReferralStatus.APPROVED.name());
            referral.setReviewComments(request.getReviewComments());
        } else {
            referral.setStatus(ReferralStatus.REJECTED.name());
            referral.setRejectReason(request.getRejectReason());
        }
        updateById(referral);
        return referral;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReferral(ReferralCancelRequest request) {
        ReferralApplication referral = getById(request.getId());
        if (referral == null) {
            throw new IllegalArgumentException("转诊申请不存在");
        }
        if (!ReferralStatus.PENDING.name().equals(referral.getStatus())) {
            throw new IllegalStateException("仅待审核申请可取消");
        }
        referral.setStatus(ReferralStatus.CANCELLED.name());
        referral.setCancelReason(request.getReason());
        referral.setCancelTime(LocalDateTime.now());
        updateById(referral);
    }

    @Override
    public ReferralOptionsVO loadOptions() {
        ReferralOptionsVO vo = new ReferralOptionsVO();
        List<Department> secondLevel = departmentMapper.selectList(
            new LambdaQueryWrapper<Department>().eq(Department::getDeptLevel, 2));
        Map<Long, String> parentMap = departmentMapper.selectList(null).stream()
            .collect(Collectors.toMap(Department::getDeptId, Department::getDeptName, (a, b) -> a));

        List<ReferralOptionsVO.DeptOption> deptOptions = secondLevel.stream().map(dept -> {
            ReferralOptionsVO.DeptOption option = new ReferralOptionsVO.DeptOption();
            option.setDeptId(dept.getDeptId());
            option.setDeptName(dept.getDeptName());
            option.setParentDeptName(parentMap.getOrDefault(dept.getParentDeptId(), "校医院"));
            return option;
        }).collect(Collectors.toList());
        vo.setInternalDepartments(deptOptions);

        List<ReferralOptionsVO.HospitalOption> hospitals = new ArrayList<>();
        hospitals.add(buildHospital("PEKING_UNION", "北京协和医院", "三级甲等", "北京市东城区帅府园1号"));
        hospitals.add(buildHospital("PKU_FIRST", "北京大学第一医院", "三级甲等", "北京市西城区西什库大街8号"));
        hospitals.add(buildHospital("PLA_301", "解放军总医院(301医院)", "三级甲等", "北京市海淀区复兴路28号"));
        hospitals.add(buildHospital("TONGREN", "北京同仁医院", "三级甲等", "北京市东城区东交民巷1号"));
        hospitals.add(buildHospital("ANZHEN", "首都医科大学附属北京安贞医院", "三级甲等", "北京市朝阳区安贞路2号"));
        vo.setExternalHospitals(hospitals);
        return vo;
    }

    private ReferralOptionsVO.HospitalOption buildHospital(String code, String name, String level, String address) {
        ReferralOptionsVO.HospitalOption option = new ReferralOptionsVO.HospitalOption();
        option.setCode(code);
        option.setName(name);
        option.setLevel(level);
        option.setAddress(address);
        return option;
    }

    private void applyInternalRouting(ReferralApplication entity) {
        if (entity.getTargetDeptId() == null) {
            entity.setQuotaAction(ReferralQuotaAction.WAITLIST.name());
            entity.setWaitNumber(calculateNextWaitNumber(null));
            return;
        }
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getDeptId, entity.getTargetDeptId())
            .ge(DoctorSchedule::getScheduleDate, LocalDate.now())
            .apply(String.format("IFNULL(used_quota,0) < %d", INTERNAL_SCHEDULE_CAPACITY))
            .orderByAsc(DoctorSchedule::getScheduleDate)
            .last("limit 1");
        DoctorSchedule schedule = doctorScheduleMapper.selectOne(wrapper);
        if (schedule != null) {
            entity.setQuotaAction(ReferralQuotaAction.DIRECT_ASSIGN.name());
            entity.setAssignedScheduleId(schedule.getScheduleId());
            entity.setAssignedDate(schedule.getScheduleDate());
            entity.setAssignedTimeSlot(schedule.getTimeSlot());

            Integer used = schedule.getUsedQuota() == null ? 0 : schedule.getUsedQuota();
            schedule.setUsedQuota(used + 1);
            doctorScheduleMapper.updateById(schedule);
        } else {
            entity.setQuotaAction(ReferralQuotaAction.WAITLIST.name());
            entity.setWaitNumber(calculateNextWaitNumber(entity.getTargetDeptId()));
        }
    }

    private Integer calculateNextWaitNumber(Long deptId) {
        if (deptId == null) {
            return 1;
        }
        LambdaQueryWrapper<ReferralApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReferralApplication::getTargetDeptId, deptId)
            .eq(ReferralApplication::getTargetType, ReferralTargetType.INTERNAL.name())
            .orderByDesc(ReferralApplication::getWaitNumber)
            .last("limit 1");
        ReferralApplication last = getOne(wrapper);
        if (last == null || last.getWaitNumber() == null) {
            return 1;
        }
        return last.getWaitNumber() + 1;
    }

    private String generateReferralCode() {
        return "REF" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss")
            + (int) (Math.random() * 90 + 10);
    }

    private List<ReferralAttachment> convertAttachments(List<ReferralAttachmentPayload> payloads) {
        return payloads.stream().map(item -> {
            ReferralAttachment attachment = new ReferralAttachment();
            attachment.setName(item.getName());
            attachment.setUrl(item.getUrl());
            attachment.setType(item.getType());
            return attachment;
        }).collect(Collectors.toList());
    }

    private ReferralSourceType determineSourceType(String sourceType) {
        if (!StringUtils.hasText(sourceType)) {
            return ReferralSourceType.PATIENT_AFTER;
        }
        return ReferralSourceType.valueOf(sourceType);
    }

    private ReferralTargetType determineTargetType(String targetType) {
        if (!StringUtils.hasText(targetType)) {
            return ReferralTargetType.INTERNAL;
        }
        return ReferralTargetType.valueOf(targetType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String processAutoRegister(Long referralId) {
        ReferralApplication referral = getById(referralId);
        if (referral == null) {
            throw new IllegalArgumentException("转诊记录不存在");
        }

        // 验证状态：必须是已审核通过
        if (!ReferralStatus.APPROVED.name().equals(referral.getStatus())) {
            throw new IllegalStateException("只有已审核通过的转诊申请才能申请加号");
        }

        // 验证类型：必须是院内转诊
        if (!ReferralTargetType.INTERNAL.name().equals(referral.getTargetType())) {
            throw new IllegalStateException("只有院内转诊才能申请加号");
        }

        // 如果已经成功自动挂号过，不允许重复申请
        if (referral.getAutoRegisterStatus() != null && referral.getAutoRegisterStatus() == 1) {
            throw new IllegalStateException("该转诊申请已经成功自动挂号，无需重复申请");
        }

        // 重置自动挂号状态为未处理
        referral.setAutoRegisterStatus(0);
        
        // 重新执行自动挂号逻辑
        try {
            applyInternalRouting(referral);
            updateById(referral);
            
            // 根据结果返回消息
            if (ReferralQuotaAction.DIRECT_ASSIGN.name().equals(referral.getQuotaAction())) {
                referral.setAutoRegisterStatus(1); // 成功
                updateById(referral);
                return "自动挂号成功，已为您安排号源";
            } else if (ReferralQuotaAction.WAITLIST.name().equals(referral.getQuotaAction())) {
                referral.setAutoRegisterStatus(2); // 加入候补
                updateById(referral);
                return String.format("当前无可用排班，已加入候补队列，候补号：%d", referral.getWaitNumber());
            } else {
                referral.setAutoRegisterStatus(2); // 失败
                updateById(referral);
                return "自动挂号失败，请稍后重试";
            }
        } catch (Exception e) {
            referral.setAutoRegisterStatus(2); // 失败
            updateById(referral);
            throw new RuntimeException("自动挂号失败：" + e.getMessage(), e);
        }
    }
}

