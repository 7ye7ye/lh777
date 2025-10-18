package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.mapper.DepartmentMapper;
import org.jeecg.modules.hospital.mapper.DoctorMapper;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 科室表服务实现
 * @author Administrator
 * @createDate 2025-09-22 20:15:20
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
        implements DepartmentService {

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
            throw new JeecgBootException("父科室ID不能为空");
        }
        return departmentMapper.selectSecondLevelByParentId(parentDeptId);
    }

    @Override
    public List<Map<String, Object>> getDepartmentTree() {
        // 获取原始数据
        List<Map<String, Object>> rawData = departmentMapper.selectDepartmentWithChildren();
        
        // 转换为树形结构
        Map<Long, Map<String, Object>> treeMap = new HashMap<>();
        
        for (Map<String, Object> row : rawData) {
            Long deptId = ((Number) row.get("deptId")).longValue();
            
            // 获取或创建一级科室节点
            Map<String, Object> firstLevel = treeMap.computeIfAbsent(deptId, k -> {
                Map<String, Object> node = new HashMap<>();
                node.put("deptId", row.get("deptId"));
                node.put("deptName", row.get("deptName"));
                node.put("deptDesc", row.get("deptDesc"));
                node.put("children", new ArrayList<Map<String, Object>>());
                return node;
            });
            
            // 如果有二级科室，添加到children中
            if (row.get("childDeptId") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) firstLevel.get("children");
                Map<String, Object> child = new HashMap<>();
                child.put("deptId", row.get("childDeptId"));
                child.put("deptName", row.get("childDeptName"));
                child.put("deptDesc", row.get("childDeptDesc"));
                child.put("location", row.get("childLocation"));
                children.add(child);
            }
        }
        
        return new ArrayList<>(treeMap.values());
    }

    @Override
    public boolean createDepartment(Department department) {
        // 校验科室名称唯一性
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_name", department.getDeptName());
        if (baseMapper.selectCount(queryWrapper) > 0) {
            throw new JeecgBootException("科室名称已存在");
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
            throw new JeecgBootException("科室不存在");
        }

        // 校验名称唯一性（排除自身）
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_name", department.getDeptName())
                .ne("dept_id", department.getDeptId());
        if (baseMapper.selectCount(queryWrapper) > 0) {
            throw new JeecgBootException("科室名称已存在");
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
            throw new JeecgBootException("该科室下有关联医生，无法删除");
        }

        // 检查是否有子科室（如果是一级科室）
        QueryWrapper<Department> childQueryWrapper = new QueryWrapper<>();
        childQueryWrapper.eq("parent_dept_id", deptId);
        if (baseMapper.selectCount(childQueryWrapper) > 0) {
            throw new JeecgBootException("该科室下有子科室，无法删除");
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




