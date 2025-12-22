package org.jeecg.modules.hospital.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.hospital.entity.ReferralApplication;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;

import java.util.List;
import java.util.Map;

/**
 * 转诊申请Mapper接口
 */
@Mapper
@DS("hospital")
public interface ReferralMapper extends BaseMapper<ReferralApplication> {

    /**
     * 查询转诊申请列表
     * @param params 查询参数
     * @return 转诊申请列表
     */
    List<Map<String, Object>> selectReferralList(Map<String, Object> params);

    /**
     * 查询转诊申请总数
     * @param params 查询参数
     * @return 总数
     */
    long selectReferralCount(Map<String, Object> params);

    /**
     * 查询转诊申请详情
     * @param id 转诊申请ID
     * @return 详情信息
     */
    Map<String, Object> selectReferralDetail(@Param("id") Long id);

    /**
     * 查询患者的转诊记录
     * @param patientId 患者ID
     * @return 转诊记录列表
     */
    List<Map<String, Object>> selectPatientReferrals(@Param("patientId") Long patientId);

    /**
     * 查询医生相关的转诊记录
     * @param doctorId 医生ID
     * @return 转诊记录列表
     */
    List<Map<String, Object>> selectDoctorReferrals(@Param("doctorId") Long doctorId);

    /**
     * 更新自动挂号状态
     * @param id 转诊申请ID
     * @param status 状态
     */
    void updateAutoRegisterStatus(@Param("id") Long id, @Param("status") Integer status);
}