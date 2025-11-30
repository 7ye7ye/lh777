package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.service.PatientService;
import org.jeecg.modules.hospital.mapper.PatientMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.hospital.entity.PatientVisit;
import org.jeecg.modules.hospital.mapper.PatientVisitMapper;
import org.jeecg.modules.hospital.vo.PatientBriefVO;
import org.jeecg.modules.hospital.vo.PatientDetailVO;
import org.jeecg.modules.hospital.vo.AppointmentDetailVO;
import java.time.LocalDate;
import java.util.List;

import java.util.HashMap;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Administrator
* @description 针对表【patient(患者表)】的数据库操作Service实现
* @createDate 2025-09-22 20:15:21
*/
@Service
@DS("hospital")
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient>
    implements PatientService{
    @Override
    public Patient cardInfo(Patient patient) {
        // 使用条件查询避免触发包含 patient_name 的默认查询
        return this.lambdaQuery()
                .eq(Patient::getUserId, patient.getUserId())
                .one();
    }

    @Resource
    private PatientVisitMapper patientVisitMapper;

    @Resource
    private org.jeecg.modules.hospital.mapper.DoctorScheduleMapper doctorScheduleMapper;
    @Resource
    private org.jeecg.modules.hospital.mapper.RegistrationRecordMapper registrationRecordMapper;

    @Override
    public java.util.List<org.jeecg.modules.hospital.vo.PatientBriefVO> listBasic(
            String keyword,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    ) {
        // 直接查询 patient 表（已配置 @DS("hospital") 使用云库）
        return this.baseMapper.selectBriefPatients(keyword, startDate, endDate);
    }

    @Override
    public AppointmentDetailVO appointmentDetail(Long appointmentId) {
        if (appointmentId == null) return null;
        return this.baseMapper.selectAppointmentDetail(appointmentId);
    }
    @Override
    public java.util.List<org.jeecg.modules.hospital.vo.PatientBriefVO> list(
            Long doctorId,
            String keyword,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            Integer status
    ) {
        // 使用挂号记录 + 排班进行医生+日期过滤（按单日，控制器传 start=end）
        return this.baseMapper.selectPatientsByRegistration(doctorId, startDate, status, keyword);
    }

    // 新增：患者详情（基础信息 + 就诊记录）
    @Override
    public org.jeecg.modules.hospital.vo.PatientDetailVO detail(Long patientId) {
        // 使用联表查询，别名填充 patient_name，避免 patient 表缺列导致的 SQL 错误
        Patient patient = this.baseMapper.selectDetailById(patientId);
        org.jeecg.modules.hospital.vo.PatientDetailVO vo = new org.jeecg.modules.hospital.vo.PatientDetailVO();
        vo.setPatient(patient);
        vo.setVisits(java.util.Collections.emptyList());
        return vo;
    }

    @Override
    @Transactional
    public boolean updateVisitStatus(Long appointmentId, String registrationNo, String action) {
        if ((appointmentId == null) && (registrationNo == null || registrationNo.trim().isEmpty())) return false;
        if (action == null || action.trim().isEmpty()) return false;

        String normalized = action.trim().toLowerCase();
        boolean isStart = "start".equals(normalized) || "开始接诊".equals(action) || "开始".equals(action.trim());
        boolean isFinish = "finish".equals(normalized) || "完成接诊".equals(action) || "完成".equals(action.trim());
        if (!isStart && !isFinish) return false;

        org.jeecg.modules.hospital.entity.RegistrationRecord rr = null;
        if (appointmentId != null) {
            rr = registrationRecordMapper.selectById(appointmentId);
        }
        if (rr == null && registrationNo != null && !registrationNo.trim().isEmpty()) {
            rr = registrationRecordMapper.selectByRegistrationNo(registrationNo.trim());
        }
        if (rr == null) return false;

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (isStart) {
            Integer currentStatus = rr.getStatus() != null ? rr.getStatus() : 1; // 默认已预约=1
            int newStatus = (currentStatus != null && currentStatus == 2) ? 2 : currentStatus; // 已完成不降级
            registrationRecordMapper.updateStatusAndVisitTime(rr.getRecordId(), newStatus, now);
        }
        if (isFinish) {
            registrationRecordMapper.updateStatusAndVisitTime(rr.getRecordId(), 2, now); // 已就诊=2
        }
        return true;
    }

    @Override
    @Transactional
    public boolean unbindPatientCard(Long userId, Long patientId) {
        if (userId == null || patientId == null) {
            return false;
        }

        // 软删除：将指定 userId 名下、指定 patientId 的记录 isDeleted 置为 1
        return this.lambdaUpdate()
                .eq(Patient::getUserId, userId)
                .eq(Patient::getPatientId, patientId)
                .set(Patient::getIsDeleted, 1)
                .update();
    }
}




