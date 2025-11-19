package org.jeecg.modules.hospital.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.RegistrationRecord;
import org.jeecg.modules.hospital.entity.RegistrationType;
import org.jeecg.modules.hospital.entity.WaitingQueue;

import java.util.List;
import java.util.Map;

/**
 * 挂号业务接口
 */
public interface RegistrationService {

    /**
     * 获取所有挂号类型
     */
    List<RegistrationType> getAllRegistrationTypes();

    /**
     * 根据医生ID获取排班信息
     * @param doctorId 医生ID
     * @param startDate 开始日期
     * @param days 天数
     */
    List<Map<String, Object>> getDoctorSchedules(Long doctorId, String startDate, Integer days);


    /**
     * 创建挂号记录
     * @param record 挂号信息（医生ID、类型ID、排班ID）
     * @param patientId 患者ID（从登录信息获取）
     * @param joinWaitingQueue 如果号源已满，是否加入候补队列
     * @return Result<String> 返回挂号结果或候补提示
     */
    Result<String> createRegistration(RegistrationRecord record, Long patientId, boolean joinWaitingQueue);
    /**
     * 根据患者ID获取挂号记录
     */
    List<RegistrationRecord> getRecordsByPatientId(Long patientId);

    /**
     * 检查患者是否对同一排班重复挂号
     * @param patientId 患者ID
     * @param scheduleId 排班ID
     * @return true 表示已存在重复挂号，false 表示未重复
     */
    boolean checkDuplicateBySchedule(Long patientId, Long scheduleId);

    /**
     * 将患者加入候补队列
     * @param queue 候补信息对象
     * @return true 表示加入成功，false 表示失败
     */
    Result<String> addWaitingQueue(WaitingQueue queue);
}
