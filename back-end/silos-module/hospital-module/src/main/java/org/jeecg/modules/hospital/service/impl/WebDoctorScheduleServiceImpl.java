// org.jeecg.modules.hospital.common.service.impl.WebDoctorScheduleServiceImpl.java
package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hospital.dto.WebScheduleQueryDTO;
import org.jeecg.modules.hospital.dto.WebTodayScheduleDTO;
import org.jeecg.modules.hospital.entity.WebDoctorSchedule;
import org.jeecg.modules.hospital.mapper.WebDoctorScheduleMapper;
import org.jeecg.modules.hospital.service.IWebDoctorScheduleService;
import org.jeecg.modules.hospital.vo.WebTodayScheduleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生排班服务实现类
 */
@Slf4j
@Service
@DS("hospital")
public class WebDoctorScheduleServiceImpl extends ServiceImpl<WebDoctorScheduleMapper, WebDoctorSchedule>
        implements IWebDoctorScheduleService {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<WebTodayScheduleVO> listTodaySchedules(WebScheduleQueryDTO queryDTO) {
        try {
            // 设置默认查询日期为今天
            Date queryDate = queryDTO.getDate() != null ? queryDTO.getDate() : new Date();
            java.sql.Date sqlDate = new java.sql.Date(queryDate.getTime());

            // 查询排班数据
            log.info("查询今日排班参数: date={}, deptId={}, doctorId={}, timeSlot={}, keyword={}",
                    dateFormat.format(sqlDate),
                    queryDTO.getDeptId(),
                    queryDTO.getDoctorId(),
                    queryDTO.getTimeSlot(),
                    queryDTO.getKeyword());
            List<WebTodayScheduleDTO> scheduleDTOs = baseMapper.selectTodaySchedules(
                    sqlDate,
                    queryDTO.getDeptId(),
                    queryDTO.getDoctorId(),
                    queryDTO.getTimeSlot(),
                    queryDTO.getKeyword()
            );

            // 转换为VO对象
            return scheduleDTOs.stream().map(this::convertToVO).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("查询今日排班失败", e);
            throw new RuntimeException("查询今日排班失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将DTO转换为VO
     */
    private WebTodayScheduleVO convertToVO(WebTodayScheduleDTO dto) {
        WebTodayScheduleVO vo = new WebTodayScheduleVO();
        BeanUtils.copyProperties(dto, vo, "scheduleDate", "remainingQuota");

        if (dto.getScheduleDate() != null) {
            vo.setScheduleDate(dateFormat.format(dto.getScheduleDate()));
        }

        Integer max = dto.getMaxQuota();
        Integer used = dto.getUsedQuota();
        vo.setRemainingQuota((max != null && used != null) ? (max - used) : 0);

        return vo;
    }
}