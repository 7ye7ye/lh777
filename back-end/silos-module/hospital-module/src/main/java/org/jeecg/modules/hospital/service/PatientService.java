package org.jeecg.modules.hospital.service;

import org.jeecg.modules.hospital.entity.Patient;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

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
    Patient cardInfo(Patient patient);

    // 医生端患者列表查询（支持关键词、日期范围、状态）
    List<PatientBriefVO> list(Long doctorId, String keyword, LocalDate startDate, LocalDate endDate, Integer status);

    // 患者详情（基础信息 + 就诊记录）
    PatientDetailVO detail(Long patientId);

    /**
     * 更新就诊状态：
     * action= "start" 或 "开始接诊" -> patient_visit 置为进行中
     * action= "finish" 或 "完成接诊" -> patient_visit 置为已完成 + registration_record 置为已就诊并记录时间
     */
    // 更新就诊状态：支持 record_id 或 registration_no 二选一
    boolean updateVisitStatus(Long appointmentId, String registrationNo, String action);
}
