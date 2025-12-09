package org.jeecg.modules.hospital.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hospital.dto.*;
import org.jeecg.modules.hospital.mapper.StatisticsMapper;
import org.jeecg.modules.hospital.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现类
 */
@Slf4j
@Service
@DS("hospital")
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private StatisticsMapper statisticsMapper;

    @Override
    @DS("hospital")
    public List<OutpatientStatisticsDTO> getOutpatientStatistics(StatisticsQueryDTO query) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            LocalDate startDate = query.getStartDate();
            LocalDate endDate = query.getEndDate();
            Long deptId = query.getDeptId();
            String periodType = query.getPeriodType();
            
            if (startDate == null || endDate == null) {
                log.warn("开始日期或结束日期为空，返回空列表");
                return new ArrayList<>();
            }
            
            List<OutpatientStatisticsDTO> result = statisticsMapper.getOutpatientStatistics(
                startDate, endDate, deptId, periodType
            );
            
            // 如果需要历史对比
            if (Boolean.TRUE.equals(query.getCompareHistory()) && query.getComparePeriods() != null) {
                List<OutpatientStatisticsDTO> compareData = statisticsMapper.getOutpatientStatisticsForCompare(
                    startDate, endDate, deptId, periodType, query.getComparePeriods()
                );
                // 合并对比数据（这里简化处理，实际可能需要更复杂的逻辑）
                Map<String, OutpatientStatisticsDTO> resultMap = new HashMap<>();
                for (OutpatientStatisticsDTO item : result) {
                    String key = item.getDate() + "_" + (item.getDeptId() != null ? item.getDeptId() : "");
                    resultMap.put(key, item);
                }
                for (OutpatientStatisticsDTO item : compareData) {
                    String key = item.getDate() + "_" + (item.getDeptId() != null ? item.getDeptId() : "");
                    OutpatientStatisticsDTO current = resultMap.get(key);
                    if (current != null) {
                        current.setCompareVisitCount(item.getVisitCount());
                        if (item.getVisitCount() != null && current.getVisitCount() != null && item.getVisitCount() > 0) {
                            BigDecimal growth = BigDecimal.valueOf(current.getVisitCount() - item.getVisitCount())
                                .divide(BigDecimal.valueOf(item.getVisitCount()), 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                            current.setGrowthRate(growth);
                        }
                    }
                }
                result = new ArrayList<>(resultMap.values());
            }
            
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取门诊量统计失败", e);
            return new ArrayList<>();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<DepartmentLoadDTO> getDepartmentLoadStatistics(StatisticsQueryDTO query) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            LocalDate startDate = query.getStartDate();
            LocalDate endDate = query.getEndDate();
            Long deptId = query.getDeptId();
            Long doctorId = query.getDoctorId();
            
            if (startDate == null || endDate == null) {
                log.warn("开始日期或结束日期为空，返回空列表");
                return new ArrayList<>();
            }
            
            List<DepartmentLoadDTO> result = statisticsMapper.getDepartmentLoadStatistics(
                startDate, endDate, deptId, doctorId
            );
            
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取科室负荷统计失败", e);
            return new ArrayList<>();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<CancelRateDTO> getCancelRateStatistics(StatisticsQueryDTO query) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            LocalDate startDate = query.getStartDate();
            LocalDate endDate = query.getEndDate();
            Long deptId = query.getDeptId();
            Long doctorId = query.getDoctorId();
            Long typeId = query.getTypeId();
            
            if (startDate == null || endDate == null) {
                log.warn("开始日期或结束日期为空，返回空列表");
                return new ArrayList<>();
            }
            
            List<CancelRateDTO> result = new ArrayList<>();
            
            // 根据不同的筛选条件调用不同的查询方法
            if (deptId != null) {
                result = statisticsMapper.getCancelRateByDepartment(startDate, endDate, deptId);
            } else if (doctorId != null) {
                result = statisticsMapper.getCancelRateByDoctor(startDate, endDate, doctorId);
            } else if (typeId != null) {
                result = statisticsMapper.getCancelRateByType(startDate, endDate, typeId);
            } else {
                // 如果没有指定筛选条件，可以返回所有或者按科室统计
                result = statisticsMapper.getCancelRateByDepartment(startDate, endDate, null);
            }
            
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取退号率统计失败", e);
            return new ArrayList<>();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<RegistrationStatisticsDTO> getRegistrationStatistics(StatisticsQueryDTO query) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            LocalDate startDate = query.getStartDate();
            LocalDate endDate = query.getEndDate();
            Long typeId = query.getTypeId();
            String periodType = query.getPeriodType();
            
            if (startDate == null || endDate == null) {
                log.warn("开始日期或结束日期为空，返回空列表");
                return new ArrayList<>();
            }
            
            List<RegistrationStatisticsDTO> result = statisticsMapper.getRegistrationStatistics(
                startDate, endDate, typeId, periodType
            );
            
            // 如果需要历史对比
            if (Boolean.TRUE.equals(query.getCompareHistory()) && query.getComparePeriods() != null) {
                List<RegistrationStatisticsDTO> compareData = statisticsMapper.getRegistrationStatisticsForCompare(
                    startDate, endDate, typeId, periodType, query.getComparePeriods()
                );
                // 合并对比数据
                Map<String, RegistrationStatisticsDTO> resultMap = new HashMap<>();
                for (RegistrationStatisticsDTO item : result) {
                    String key = item.getDate() + "_" + (item.getTypeId() != null ? item.getTypeId() : "");
                    resultMap.put(key, item);
                }
                for (RegistrationStatisticsDTO item : compareData) {
                    String key = item.getDate() + "_" + (item.getTypeId() != null ? item.getTypeId() : "");
                    RegistrationStatisticsDTO current = resultMap.get(key);
                    if (current != null) {
                        current.setCompareRegistration(item.getTypeRegistration());
                        if (item.getTypeRegistration() != null && current.getTypeRegistration() != null && item.getTypeRegistration() > 0) {
                            BigDecimal growth = BigDecimal.valueOf(current.getTypeRegistration() - item.getTypeRegistration())
                                .divide(BigDecimal.valueOf(item.getTypeRegistration()), 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                            current.setGrowthRate(growth);
                        }
                    }
                }
                result = new ArrayList<>(resultMap.values());
            }
            
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取挂号量统计失败", e);
            return new ArrayList<>();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<ReferralStatisticsDTO> getReferralStatistics(StatisticsQueryDTO query) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            LocalDate startDate = query.getStartDate();
            LocalDate endDate = query.getEndDate();
            Long deptId = query.getDeptId();
            String periodType = query.getPeriodType();
            
            if (startDate == null || endDate == null) {
                log.warn("开始日期或结束日期为空，返回空列表");
                return new ArrayList<>();
            }
            
            List<ReferralStatisticsDTO> result = statisticsMapper.getReferralStatistics(
                startDate, endDate, deptId, periodType
            );
            
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取转诊情况统计失败", e);
            return new ArrayList<>();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public Map<String, Object> getStatisticsSummary(StatisticsQueryDTO query) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            Map<String, Object> summary = new HashMap<>();
            
            // 获取就诊量统计（状态 1/2，按挂号日期）
            Integer totalVisitCount = statisticsMapper.getVisitCountByStatus12(
                query.getStartDate(),
                query.getEndDate(),
                query.getDeptId()
            );
            summary.put("totalVisitCount", totalVisitCount != null ? totalVisitCount : 0);
            
            // 获取科室负荷统计
            List<DepartmentLoadDTO> deptLoadStats = getDepartmentLoadStatistics(query);
            double avgDeptLoad = deptLoadStats.stream()
                .mapToDouble(item -> item.getQuotaUsageRate() != null ? item.getQuotaUsageRate().doubleValue() : 0.0)
                .average()
                .orElse(0.0);
            summary.put("avgDeptLoad", avgDeptLoad);
            
            // 获取退号率统计
            List<CancelRateDTO> cancelRateStats = getCancelRateStatistics(query);
            double avgCancelRate = cancelRateStats.stream()
                .mapToDouble(item -> item.getCancelRate() != null ? item.getCancelRate().doubleValue() : 0.0)
                .average()
                .orElse(0.0);
            summary.put("avgCancelRate", avgCancelRate);
            
            // 获取挂号量统计
            List<RegistrationStatisticsDTO> registrationStats = getRegistrationStatistics(query);
            int totalRegistration = registrationStats.stream()
                .mapToInt(item -> item.getTotalRegistration() != null ? item.getTotalRegistration() : 0)
                .sum();
            summary.put("totalRegistration", totalRegistration);
            
            // 获取总收入统计（挂号费收入）
            Double totalIncome = statisticsMapper.getTotalIncome(
                query.getStartDate(),
                query.getEndDate(),
                query.getDeptId(),
                query.getDoctorId()
            );
            summary.put("totalIncome", totalIncome != null ? totalIncome : 0.0);

            // 在岗医护人员（按排班 status=1，去重医生数）
            Integer activeStaff = statisticsMapper.getActiveStaffCount(
                query.getStartDate(),
                query.getEndDate(),
                query.getDeptId()
            );
            summary.put("activeStaffCount", activeStaff != null ? activeStaff : 0);
            
            return summary;
        } catch (Exception e) {
            log.error("获取统计数据汇总失败", e);
            return new HashMap<>();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<Map<String, Object>> getTimeSlotDistribution(StatisticsQueryDTO query) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            return statisticsMapper.getTimeSlotDistribution(
                query.getStartDate(),
                query.getEndDate()
            );
        } catch (Exception e) {
            log.error("获取就诊时段分布失败", e);
            return new ArrayList<>();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<Map<String, Object>> getIncomeTrend(StatisticsQueryDTO query) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            return statisticsMapper.getIncomeTrend(
                query.getStartDate(),
                query.getEndDate(),
                query.getPeriodType()
            );
        } catch (Exception e) {
            log.error("获取收入趋势失败", e);
            return new ArrayList<>();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<Map<String, Object>> getDeptDetail(StatisticsQueryDTO query) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            return statisticsMapper.getDeptDetail(
                query.getStartDate(),
                query.getEndDate()
            );
        } catch (Exception e) {
            log.error("获取科室明细失败", e);
            return new ArrayList<>();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<Map<String, Object>> getDoctorDetail(StatisticsQueryDTO query) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            return statisticsMapper.getDoctorDetail(
                query.getStartDate(),
                query.getEndDate()
            );
        } catch (Exception e) {
            log.error("获取医生明细失败", e);
            return new ArrayList<>();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }
}

