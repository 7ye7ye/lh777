package org.jeecg.modules.hospital.service;

import org.jeecg.modules.hospital.entity.Doctor;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【doctor(医生表)】的数据库操作Service
* @createDate 2025-09-22 20:15:21
*/
public interface DoctorService extends IService<Doctor> {

    /**
     * 获取所有医生列表（仅返回正常出诊的医生）
     * @return 医生列表
     */
    List<Doctor> getAllDoctors();

    /**
     * 搜索医生
     * @param keyword 关键词（姓名或专长）
     * @return 医生列表
     */
    List<Doctor> searchDoctors(String keyword);

    /**
     * 获取医生详情
     * @param doctorId 医生ID
     * @return 医生信息
     */
    Doctor getDoctorDetail(Long doctorId);

    /**
     * 根据科室ID获取医生列表
     * @param deptId 科室ID
     * @return 医生列表
     */
    List<Doctor> getDoctorsByDeptId(Long deptId);
}
