package org.jeecg.modules.hospital.service;

public interface WaitingQueueService {

    /**
     * 自动补候补队列（候补转正）
     * @param scheduleId 排班ID
     * @param n 候补成功人数
     */
    void autoFillFromQueue(Long scheduleId, int n);
}
