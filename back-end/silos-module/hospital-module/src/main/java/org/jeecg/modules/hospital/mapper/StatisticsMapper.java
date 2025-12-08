package org.jeecg.modules.hospital.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.hospital.dto.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计Mapper接口
 */
@DS("hospital")
@Mapper
public interface StatisticsMapper {
    
    /**
     * 统计门诊量（按日期和科室）
     */
    List<OutpatientStatisticsDTO> getOutpatientStatistics(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("deptId") Long deptId,
        @Param("periodType") String periodType
    );
    
    /**
     * 统计科室负荷（医生出诊时长、号源使用率）
     */
    List<DepartmentLoadDTO> getDepartmentLoadStatistics(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("deptId") Long deptId,
        @Param("doctorId") Long doctorId
    );
    
    /**
     * 统计退号率（按科室）
     */
    List<CancelRateDTO> getCancelRateByDepartment(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("deptId") Long deptId
    );
    
    /**
     * 统计退号率（按医生）
     */
    List<CancelRateDTO> getCancelRateByDoctor(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("doctorId") Long doctorId
    );
    
    /**
     * 统计退号率（按号别）
     */
    List<CancelRateDTO> getCancelRateByType(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("typeId") Long typeId
    );
    
    /**
     * 统计挂号量（按日期和号别）
     */
    List<RegistrationStatisticsDTO> getRegistrationStatistics(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("typeId") Long typeId,
        @Param("periodType") String periodType
    );
    
    /**
     * 获取历史对比数据（门诊量）
     */
    List<OutpatientStatisticsDTO> getOutpatientStatisticsForCompare(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("deptId") Long deptId,
        @Param("periodType") String periodType,
        @Param("comparePeriods") Integer comparePeriods
    );
    
    /**
     * 获取历史对比数据（挂号量）
     */
    List<RegistrationStatisticsDTO> getRegistrationStatisticsForCompare(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("typeId") Long typeId,
        @Param("periodType") String periodType,
        @Param("comparePeriods") Integer comparePeriods
    );
    
    /**
     * 统计转诊情况（按日期、科室、转诊类型）
     */
    List<ReferralStatisticsDTO> getReferralStatistics(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("deptId") Long deptId,
        @Param("periodType") String periodType
    );
    
    /**
     * 统计总收入（挂号费收入）
     */
    Double getTotalIncome(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("deptId") Long deptId,
        @Param("doctorId") Long doctorId
    );
}

