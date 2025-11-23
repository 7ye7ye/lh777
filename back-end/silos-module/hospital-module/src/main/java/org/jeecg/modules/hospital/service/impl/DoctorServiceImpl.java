package org.jeecg.modules.hospital.service.impl;

import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.mapper.DoctorMapper;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.DepartmentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 医生服务实现类
 */
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
@DS("hospital")
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 获取所有医生列表（仅返回正常出诊的医生）
     * @return 医生列表
     */
    @Override
    public List<Doctor> getAllDoctors() {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        // 假设 status 字段为 1 表示正常出诊的医生
        queryWrapper.eq("is_active", 1);
        queryWrapper.orderByDesc("doctor_id");
        List<Doctor> doctors = this.list(queryWrapper);
        // 关联科室信息
        return associateDepartmentInfo(doctors);
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
                .eq("is_active", 1)  // 只搜索正常出诊的医生
                .orderByDesc("doctor_id");  // 修复：使用 doctor_id 排序
        List<Doctor> doctors = this.list(queryWrapper);
        // 关联科室信息
        return associateDepartmentInfo(doctors);
    }

    /**
     * 获取医生详情
     * @param doctorId 医生ID
     * @return 医生信息
     */
    @Override
    public Doctor getDoctorDetail(Long doctorId) {
        Doctor doctor = this.getById(doctorId);
        if (doctor != null && doctor.getDeptId() != null) {
            // 关联科室信息
            Department department = departmentService.getById(doctor.getDeptId());
            if (department != null) {
                // 使用反射或Map来存储科室名称
                // 由于Doctor实体没有deptName字段，我们需要通过其他方式传递科室名称
                // 这里我们可以使用ThreadLocal或其他方式，但为了简单起见，我们返回医生对象
                // 前端会通过deptId去查找对应的科室名称
            }
        }
        return doctor;
    }

    /**
     * 根据科室ID获取医生列表
     * @param deptId 科室ID
     * @return 医生列表
     */
    @Override
    public List<Doctor> getDoctorsByDeptId(Long deptId) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        
        // 查询科室信息，判断是否为一级科室
        Department department = departmentService.getById(deptId);
        if (department != null && department.getDeptLevel() == 1) {
            // 如果是一级科室，查询该科室下所有二级科室
            List<Department> subDepartments = departmentService.getSecondLevelByParentId(deptId);
            if (subDepartments != null && !subDepartments.isEmpty()) {
                // 提取所有二级科室ID
                List<Long> subDeptIds = new ArrayList<>();
                for (Department subDept : subDepartments) {
                    subDeptIds.add(subDept.getDeptId());
                }
                // 查询所有二级科室的医生
                queryWrapper.in("dept_id", subDeptIds);
            }
        } else {
            // 如果是二级科室或找不到科室信息，使用原有的精确查询
            queryWrapper.eq("dept_id", deptId);
        }
        
        // 只返回正常出诊的医生并排序
        queryWrapper.eq("is_active", 1)
                .orderByDesc("doctor_id");
        
        List<Doctor> doctors = this.list(queryWrapper);
        // 关联科室信息
        return associateDepartmentInfo(doctors);
    }
    
    /**
     * 关联科室信息到医生列表
     * 由于Doctor实体没有deptName字段，这里我们通过其他方式传递科室信息
     * 在实际使用中，前端会通过deptId去查找对应的科室名称
     */
    private List<Doctor> associateDepartmentInfo(List<Doctor> doctors) {
        // 在实际项目中，这里可以使用更高效的方式，比如一次性查询所有相关科室
        // 然后缓存科室信息，避免多次查询数据库
        
        // 由于Doctor实体没有deptName字段，我们无法直接设置科室名称
        // 但我们可以确保deptId正确，这样前端就可以通过deptId去查找对应的科室名称
        // 或者我们可以创建一个DoctorDTO类，包含deptName字段
        
        return doctors;
    }
}