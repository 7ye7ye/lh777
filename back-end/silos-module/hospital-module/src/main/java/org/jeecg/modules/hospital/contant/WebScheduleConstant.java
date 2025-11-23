// org.jeecg.modules.hospital.common.config.constant.WebScheduleConstant.java
package org.jeecg.modules.hospital.contant;

/**
 * 排班相关常量
 */
public interface WebScheduleConstant {

    /**
     * 时段类型
     */
    interface TimeSlot {
        int MORNING = 1;    // 上午
        int AFTERNOON = 2;  // 下午
        int EVENING = 3;    // 晚上
    }

    /**
     * 排班状态
     */
    interface Status {
        int DISABLED = 0;   // 停用
        int ENABLED = 1;    // 有效
    }
}