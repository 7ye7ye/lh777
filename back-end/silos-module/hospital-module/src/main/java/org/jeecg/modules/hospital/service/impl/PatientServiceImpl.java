package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.service.PatientService;
import org.jeecg.modules.hospital.mapper.PatientMapper;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.hospital.entity.PatientVisit;
import org.jeecg.modules.hospital.mapper.PatientVisitMapper;
import org.jeecg.modules.hospital.vo.PatientBriefVO;
import org.jeecg.modules.hospital.vo.PatientDetailVO;
import java.time.LocalDate;
import java.util.List;

@Service
@DS("hospital")
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient>
    implements PatientService{
    @Override
    public void cardInfo(Patient patient) {
        this.baseMapper.selectById(patient.getUserId());
    }

    // 新增：注入 PatientVisitMapper
    @Resource
    private PatientVisitMapper patientVisitMapper;

    // 新增：医生端患者列表查询
    @Override
    public java.util.List<org.jeecg.modules.hospital.vo.PatientBriefVO> list(
            Long doctorId,
            String keyword,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            Integer status
    ) {
        // 仅从 patient 表查询；医生/状态/就诊日期筛选暂不使用
        return this.baseMapper.selectBriefPatients(keyword, startDate, endDate);
    }

    // 新增：患者详情（基础信息 + 就诊记录）
    @Override
    public org.jeecg.modules.hospital.vo.PatientDetailVO detail(Long patientId) {
        Patient patient = super.getById(patientId);
        org.jeecg.modules.hospital.vo.PatientDetailVO vo = new org.jeecg.modules.hospital.vo.PatientDetailVO();
        vo.setPatient(patient);
        vo.setVisits(null); // 使用 patient 表，不返回就诊记录
        return vo;
    }
}




