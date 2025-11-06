package org.jeecg.modules.hospital.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.mapper.DoctorMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
* @author Administrator
* @description 针对表【doctor(医生表)】的数据库操作Service实现
* @createDate 2025-09-22 20:15:20
*/
@Slf4j
@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor>
    implements DoctorService {
    
    @Resource
    private DoctorMapper doctorMapper;

    @Override
    @DS("hospital")
    public List<Doctor> getAllDoctors() {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            LambdaQueryWrapper<Doctor> query = new LambdaQueryWrapper<>();
            // 只返回正常出诊的医生
            query.eq(Doctor::getIsActive, 1)
                 .orderByAsc(Doctor::getDoctorId);
            return doctorMapper.selectList(query);
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<Doctor> searchDoctors(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        try {
            DynamicDataSourceContextHolder.push("hospital");
            LambdaQueryWrapper<Doctor> query = new LambdaQueryWrapper<>();
            query.and(wrapper -> wrapper
                    .like(Doctor::getDoctorName, keyword)
                    .or()
                    .like(Doctor::getSpecialty, keyword)
                    .or()
                    .like(Doctor::getDoctorDesc, keyword))
                 .eq(Doctor::getIsActive, 1) // 只返回正常出诊的医生
                 .orderByAsc(Doctor::getDoctorId);
            return doctorMapper.selectList(query);
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public Doctor getDoctorDetail(Long doctorId) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            return doctorMapper.selectById(doctorId);
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<Doctor> getDoctorsByDeptId(Long deptId) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            LambdaQueryWrapper<Doctor> query = new LambdaQueryWrapper<>();
            query.eq(Doctor::getDeptId, deptId)
                 .eq(Doctor::getIsActive, 1) // 只返回正常出诊的医生
                 .orderByAsc(Doctor::getDoctorId);
            return doctorMapper.selectList(query);
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }
    
    /**
     * 重写getById方法，确保使用hospital数据源
     */
    @DS("hospital")
    @Override
    public Doctor getById(java.io.Serializable id) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            return doctorMapper.selectById(id);
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }
}




