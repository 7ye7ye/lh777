// org.jeecg.modules.hospital.common.service.IWebDoctorScheduleService.java
package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.dto.WebScheduleQueryDTO;
import org.jeecg.modules.hospital.entity.WebDoctorSchedule;
import org.jeecg.modules.hospital.vo.WebTodayScheduleVO;

import java.util.List;

/**
 * 医生排班服务接口
 */
public interface IWebDoctorScheduleService extends IService<WebDoctorSchedule> {

    /**
     * 查询今日排班列表
     */
    List<WebTodayScheduleVO> listTodaySchedules(WebScheduleQueryDTO queryDTO);
}