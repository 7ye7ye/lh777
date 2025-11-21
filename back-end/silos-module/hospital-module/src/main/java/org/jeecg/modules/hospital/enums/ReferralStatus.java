package org.jeecg.modules.hospital.enums;

/**
 * 转诊申请状态
 */
public enum ReferralStatus {
    PENDING("待审核"),
    APPROVED("已通过"),
    REJECTED("已拒绝"),
    CANCELLED("已取消");

    private final String label;

    ReferralStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}


