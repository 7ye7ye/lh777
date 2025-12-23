package org.jeecg.modules.hospital.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.dto.RegistrationDetailDTO;
import org.jeecg.modules.hospital.entity.*;
import org.jeecg.modules.hospital.vo.RegistrationVO;

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
     * 检查同一患者在当前排班对应的科室、同一天是否已有其他挂号记录
     * <p>
     * 业务含义：用于“小程序挂号限制：单个就诊人单个科室单日最多1次预约”，
     * 其中“单个科室、单日”的范围通过当前排班ID反查得到。
     * </p>
     *
     * @param patientId 患者ID
     * @param scheduleId 当前要预约的排班ID
     * @return true 表示已达到限制（已存在至少1条记录），false 表示未达到限制
     */
    boolean checkDeptLimitForSchedule(Long patientId, Long scheduleId);

    /**
     * 将患者加入候补队列
     * @param queue 候补信息对象
     * @return true 表示加入成功，false 表示失败
     */
    Result<String> addWaitingQueue(WaitingQueue queue);

    /**
     * 取消挂号（退号）
     * @param recordId 挂号记录ID
     * @param cancelReason 取消原因
     */
    boolean cancelRegistration(Long recordId, String cancelReason);

    /**
     * 按疾病名称查询挂号信息
     * @param disease 疾病名称
     * @return 挂号视图列表
     */
    List<RegistrationVO> listByDisease(String disease);

    /**
     * 根据挂号记录ID获取详情
     * @param recordId 挂号记录ID
     * @return 挂号详情
     */
    RegistrationDetailDTO getRegistrationDetail(Long recordId);


    /**
     * 根据排班ID获取科室ID
     * @param scheduleId 排班ID
     * @return 科室ID
     */
    Long getDepartmentIdBySchedule(Long scheduleId);

    /**
     * 根据排班ID获取排班详情
     */
    DoctorSchedule getScheduleDetailById(Long scheduleId);

    /**
     * 根据患者ID获取患者详情
     * @param patientId 患者ID
     * @return 患者详情（包含基本信息、账户信息等）
     */
    Patient getPatientDetailById(Long patientId);

    /**
     * 根据患者ID获取患者类型
     * @param patientId 患者ID
     * @return 患者类型（1-学生，2-教师，3-职工）
     */
    int getPatientTypeById(Long patientId);

}
