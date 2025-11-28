package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.hospital.entity.HospitalReferralRecord;

import java.util.List;
import java.util.Map;

@Mapper
@DS("hospital")
public interface HospitalReferralRecordMapper extends BaseMapper<HospitalReferralRecord> {
    
    /**
     * 根据转诊申请ID查询转诊记录
     */
    HospitalReferralRecord selectByReferralId(Long referralId);
    
    /**
     * 根据转诊单编号查询转诊记录
     */
    HospitalReferralRecord selectByReferralCode(String referralCode);

    /**
     * 查询转诊记录列表（支持分页/条件）
     */
    List<Map<String, Object>> selectReferralRecordList(Map<String, Object> params);

    /**
     * 统计转诊记录数量
     */
    long selectReferralRecordCount(Map<String, Object> params);

    /**
     * 批量更新转诊记录状态
     */
    int batchUpdateStatus(List<Long> ids, String status);
}