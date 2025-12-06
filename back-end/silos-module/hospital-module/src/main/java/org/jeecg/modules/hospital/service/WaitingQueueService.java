package org.jeecg.modules.hospital.service;

public interface WaitingQueueService {

    /**
     * 自动补候补队列（候补转正）
     * @param scheduleId 排班ID
     * @param n 候补成功人数
     */
    void autoFillFromQueue(Long scheduleId, int n);

    /**
     * 自动补候补队列（候补转正），支持标识加号场景
     * @param scheduleId 排班ID
     * @param n 候补成功人数
     * @param isAddQuota 是否为管理员加号场景（true-加号成功，false-正常候补成功）
     */
    void autoFillFromQueue(Long scheduleId, int n, boolean isAddQuota);
}
