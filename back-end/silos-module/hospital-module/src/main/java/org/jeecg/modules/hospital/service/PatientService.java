package org.jeecg.modules.hospital.service;

import org.jeecg.modules.hospital.entity.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;
import org.jeecg.modules.hospital.vo.PatientBriefVO;
import org.jeecg.modules.hospital.vo.PatientDetailVO;

/**
* @author Administrator
* @description 针对表【patient(患者表)】的数据库操作Service
* @createDate 2025-09-22 20:15:21
*/
public interface PatientService extends IService<Patient> {
    void cardInfo(Patient patient);

    // 医生端患者列表查询（支持关键词、日期范围、状态）
    List<PatientBriefVO> list(Long doctorId, String keyword, LocalDate startDate, LocalDate endDate, Integer status);

    // 患者详情（基础信息 + 就诊记录）
    PatientDetailVO detail(Long patientId);
}
