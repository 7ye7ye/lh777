package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.entity.Doctor;

import java.util.List;

/**
 * 医生表服务接口
 * @author Administrator
 * @createDate 2025-09-22 20:15:21
 */
public interface DoctorService extends IService<Doctor> {

    /**
     * 根据科室ID查询医生列表
     */
    List<Doctor> getDoctorsByDeptId(Long deptId);

    /**
     * 查询所有在职医生
     */
    List<Doctor> getActiveDoctors();

    /**
     * 根据医生姓名模糊查询
     */
    List<Doctor> searchDoctorsByName(String doctorName);

    /**
     * 创建医生
     */
    boolean createDoctor(Doctor doctor);

    /**
     * 更新医生信息
     */
    boolean updateDoctor(Doctor doctor);

    /**
     * 删除医生
     */
    boolean deleteDoctor(Long doctorId);

    /**
     * 更新医生出诊状态
     */
    boolean updateDoctorStatus(Long doctorId, Integer isActive);
}
