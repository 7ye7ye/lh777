package org.jeecg.modules.hospital.enums;

/**
 * 转诊来源场景
 */
public enum ReferralSourceType {
    DOCTOR_DIRECT,       // 医生诊断后直接开具
    PATIENT_BEFORE,      // 挂号前自助申请
    PATIENT_AFTER        // 就诊后提出
}

