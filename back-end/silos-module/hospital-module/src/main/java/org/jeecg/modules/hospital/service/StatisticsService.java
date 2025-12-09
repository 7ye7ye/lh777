package org.jeecg.modules.hospital.service;

import org.jeecg.modules.hospital.dto.*;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {
    
    /**
     * 统计门诊量（按日/周/月，按科室）
     * @param query 查询参数
     * @return 门诊量统计列表
     */
    List<OutpatientStatisticsDTO> getOutpatientStatistics(StatisticsQueryDTO query);
    
    /**
     * 统计科室负荷（医生出诊时长、号源使用率）
     * @param query 查询参数
     * @return 科室负荷统计列表
     */
    List<DepartmentLoadDTO> getDepartmentLoadStatistics(StatisticsQueryDTO query);
    
    /**
     * 统计退号率（按科室/医生/号别）
     * @param query 查询参数
     * @return 退号率统计列表
     */
    List<CancelRateDTO> getCancelRateStatistics(StatisticsQueryDTO query);
    
    /**
     * 统计挂号量（总挂号量及各号别挂号量，支持对比历史）
     * @param query 查询参数
     * @return 挂号量统计列表
     */
    List<RegistrationStatisticsDTO> getRegistrationStatistics(StatisticsQueryDTO query);
    
    /**
     * 统计转诊情况（按日/周/月，按科室、转诊类型）
     * @param query 查询参数
     * @return 转诊情况统计列表
     */
    List<ReferralStatisticsDTO> getReferralStatistics(StatisticsQueryDTO query);
    
    /**
     * 获取统计数据汇总（用于报表导出）
     * @param query 查询参数
     * @return 统计数据汇总
     */
    Map<String, Object> getStatisticsSummary(StatisticsQueryDTO query);

    /**
     * 按时段统计就诊/挂号量
     * @param query 查询参数
     * @return 时段分布列表
     */
    List<Map<String, Object>> getTimeSlotDistribution(StatisticsQueryDTO query);

    /**
     * 收入趋势
     */
    List<Map<String, Object>> getIncomeTrend(StatisticsQueryDTO query);

    /**
     * 科室明细
     */
    List<Map<String, Object>> getDeptDetail(StatisticsQueryDTO query);

    /**
     * 医生明细
     */
    List<Map<String, Object>> getDoctorDetail(StatisticsQueryDTO query);
}

