package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.exception.BusinessException;
import org.jeecg.modules.hospital.common.ErrorCode;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.mapper.DepartmentMapper;
import org.jeecg.modules.hospital.mapper.DoctorMapper;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 科室表服务实现
 * @author Administrator
 * @description 针对表【department(科室表)】的数据库操作Service实现
 * @createDate 2025-09-22 20:15:20
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public List<Department> getFirstLevelDepartments() {
        return departmentMapper.selectFirstLevelDepartments();
    }

    @Override
    public List<Department> getSecondLevelByParentId(Long parentDeptId) {
        if (parentDeptId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "父科室ID不能为空");
        }
        return departmentMapper.selectSecondLevelByParentId(parentDeptId);
    }

    @Override
    public List<Map<String, Object>> getDepartmentTree() {
        return departmentMapper.selectDepartmentWithChildren();
    }

    @Override
    public boolean createDepartment(Department department) {
        // 校验科室名称唯一性
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_name", department.getDeptName());
        if (baseMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "科室名称已存在");
        }

        // 设置时间
        department.setCreateTime(LocalDateTime.now());
        department.setUpdateTime(LocalDateTime.now());
        return save(department);
    }

    @Override
    public boolean updateDepartment(Department department) {
        // 校验科室存在性
        if (getById(department.getDeptId()) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "科室不存在");
        }

        // 校验名称唯一性（排除自身）
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_name", department.getDeptName())
                .ne("dept_id", department.getDeptId());
        if (baseMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "科室名称已存在");
        }

        department.setUpdateTime(LocalDateTime.now());
        return updateById(department);
    }

    @Override
    public boolean deleteDepartment(Long deptId) {
        // 检查是否有医生关联
        QueryWrapper<Doctor> doctorQueryWrapper = new QueryWrapper<>();
        doctorQueryWrapper.eq("dept_id", deptId);
        if (doctorMapper.selectCount(doctorQueryWrapper) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该科室下有关联医生，无法删除");
        }

        // 检查是否有子科室（如果是一级科室）
        QueryWrapper<Department> childQueryWrapper = new QueryWrapper<>();
        childQueryWrapper.eq("parent_dept_id", deptId);
        if (baseMapper.selectCount(childQueryWrapper) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该科室下有子科室，无法删除");
        }

        return removeById(deptId);
    }

    @Override
    public List<Department> searchDepartments(String keyword) {
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("dept_name", keyword)
                .or().like("dept_desc", keyword);
        return baseMapper.selectList(queryWrapper);
    }
}




