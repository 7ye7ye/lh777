package org.jeecg.modules.hospital.service.impl;

import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.mapper.DoctorMapper;
import org.jeecg.modules.hospital.service.DoctorService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 医生服务实现类
 */
@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    /**
     * 获取所有医生列表（仅返回正常出诊的医生）
     * @return 医生列表
     */
    @Override
    public List<Doctor> getAllDoctors() {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        // 假设 status 字段为 1 表示正常出诊的医生
        queryWrapper.eq("status", 1)
                .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    /**
     * 搜索医生
     * @param keyword 关键词（姓名或专长）
     * @return 医生列表
     */
    @Override
    public List<Doctor> searchDoctors(String keyword) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                        .like("doctor_name", keyword)
                        .or()
                        .like("specialty", keyword)
                )
                .eq("status", 1)  // 只搜索正常出诊的医生
                .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    /**
     * 获取医生详情
     * @param doctorId 医生ID
     * @return 医生信息
     */
    @Override
    public Doctor getDoctorDetail(Long doctorId) {
        return this.getById(doctorId);
    }

    /**
     * 根据科室ID获取医生列表
     * @param deptId 科室ID
     * @return 医生列表
     */
    @Override
    public List<Doctor> getDoctorsByDeptId(Long deptId) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_id", deptId)
                .eq("status", 1)  // 只返回正常出诊的医生
                .orderByDesc("create_time");
        return this.list(queryWrapper);
    }
}