package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.HospitalReferralRecord;

import java.util.List;
import java.util.Map;

/**
 * 转诊记录服务接口
 */
public interface HospitalReferralRecordService extends IService<HospitalReferralRecord> {

    /**
     * 创建转诊记录
     * @param record 转诊记录信息
     * @return 创建结果
     */
    Result<String> createReferralRecord(HospitalReferralRecord record);

    /**
     * 根据转诊申请ID查询转诊记录
     * @param referralId 转诊申请ID
     * @return 转诊记录
     */
    Result<HospitalReferralRecord> getByReferralId(Long referralId);

    /**
     * 根据转诊单编号查询转诊记录
     * @param referralCode 转诊单编号
     * @return 转诊记录
     */
    Result<HospitalReferralRecord> getByReferralCode(String referralCode);

    /**
     * 更新转诊状态
     * @param id 转诊记录ID
     * @param status 新状态
     * @return 更新结果
     */
    Result<String> updateReferralStatus(Long id, String status);

    /**
     * 更新随访信息
     * @param id 转诊记录ID
     * @param followUpInfo 随访信息
     * @return 更新结果
     */
    Result<String> updateFollowUpInfo(Long id, Map<String, Object> followUpInfo);

    /**
     * 获取转诊记录列表
     * @param params 查询参数
     * @return 转诊记录列表
     */
    Result<Map<String, Object>> getReferralRecordList(Map<String, Object> params);

    /**
     * 批量更新转诊记录状态
     * @param ids 转诊记录ID列表
     * @param status 新状态
     * @return 更新结果
     */
    Result<String> batchUpdateStatus(List<Long> ids, String status);
}