package org.jeecg.modules.hospital.enums;

/**
 * 转诊名额和排班处理策略
 */
public enum ReferralQuotaAction {
    DIRECT_ASSIGN,   // 直接占用最近的排班号源
    WAITLIST,        // 加入候补队列
    EXTERNAL_TRANSFER; // 外部医院，无需处理号源
}


