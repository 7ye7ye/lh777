package org.jeecg.modules.hospital.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.ReferralApplication;

import java.util.List;
import java.util.Map;

/**
 * 转诊服务接口
 */
public interface ReferralService {

    /**
     * 获取患者已就诊的挂号记录
     * @param patientId 患者ID
     * @return 挂号记录列表
     */
    Result<List<Map<String, Object>>> getPatientVisitedRecords(Long patientId);

    /**
     * 患者申请转诊
     * @param application 转诊申请信息
     * @return 申请结果
     */
    Result<String> applyReferralByPatient(ReferralApplication application);

    /**
     * 医生直接生成转诊意见
     * @param application 转诊申请信息
     * @return 操作结果
     */
    Result<String> createReferralByDoctor(ReferralApplication application);

    /**
     * 获取转诊申请列表
     * @param params 查询参数
     * @return 转诊申请列表和分页信息
     */
    Result<Map<String, Object>> getReferralList(Map<String, Object> params);

    /**
     * 获取转诊申请详情
     * @param id 转诊申请ID
     * @return 转诊申请详情
     */
    Result<Map<String, Object>> getReferralDetail(Long id);

    /**
     * 处理院内转诊自动挂号
     * @param referralId 转诊申请ID
     * @return 处理结果
     */
    Result<String> processAutoRegister(Long referralId);

    /**
     * 更新转诊状态
     * @param id 转诊申请ID
     * @param status 新状态
     * @param comments 审核意见
     * @return 操作结果
     */
    Result<String> updateReferralStatus(Long id, String status, String comments);

    /**
     * 取消转诊申请
     * @param id 转诊申请ID
     * @param reason 取消原因
     * @return 操作结果
     */
    Result<String> cancelReferral(Long id, String reason);

    /**
     * 获取可转诊科室列表
     * @return 科室列表
     */
    Result<List<Map<String, Object>>> getTargetDepartments();
}