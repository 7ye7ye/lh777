package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.mapper.DoctorMapper;
import org.jeecg.modules.hospital.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 医生表服务实现
 * @author Administrator
 * @createDate 2025-09-22 20:15:20
 */
@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor>
        implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public List<Doctor> getDoctorsByDeptId(Long deptId) {
        if (deptId == null) {
            throw new JeecgBootException("科室ID不能为空");
        }
        return doctorMapper.selectByDeptId(deptId);
    }

    @Override
    public List<Doctor> getActiveDoctors() {
        return doctorMapper.selectActiveDoctors();
    }

    @Override
    public List<Doctor> searchDoctorsByName(String doctorName) {
        if (doctorName == null || doctorName.trim().isEmpty()) {
            throw new JeecgBootException("医生姓名不能为空");
        }
        return doctorMapper.selectByDoctorName(doctorName);
    }

    @Override
    public boolean createDoctor(Doctor doctor) {
        // 设置默认值
        if (doctor.getIsActive() == null) {
            doctor.setIsActive(1); // 默认正常出诊
        }
        if (doctor.getUpdateVerify() == null) {
            doctor.setUpdateVerify(0); // 默认未提交修改
        }
        return save(doctor);
    }

    @Override
    public boolean updateDoctor(Doctor doctor) {
        // 校验医生是否存在
        if (getById(doctor.getDoctorId()) == null) {
            throw new JeecgBootException("医生不存在");
        }
        return updateById(doctor);
    }

    @Override
    public boolean deleteDoctor(Long doctorId) {
        // 校验医生是否存在
        if (getById(doctorId) == null) {
            throw new JeecgBootException("医生不存在");
        }
        return removeById(doctorId);
    }

    @Override
    public boolean updateDoctorStatus(Long doctorId, Integer isActive) {
        // 校验医生是否存在
        if (getById(doctorId) == null) {
            throw new JeecgBootException("医生不存在");
        }
        
        UpdateWrapper<Doctor> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("doctor_id", doctorId).set("is_active", isActive);
        return update(updateWrapper);
    }
}




