package org.jeecg.modules.hospital.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.jeecg.modules.hospital.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Administrator
* @description 针对表【department(科室表)】的数据库操作Service实现
* @createDate 2025-09-22 20:15:20
*/
@Slf4j
@Service
@DS("hospital")
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
    implements DepartmentService{
    
    @Resource
    private DepartmentMapper departmentMapper;

    @Override
    @DS("hospital")
    public List<Map<String, Object>> getDepartmentTree() {
        // 手动切换数据源（双重保险）
        try {
            DynamicDataSourceContextHolder.push("hospital");
            log.info("当前数据源: {}", DynamicDataSourceContextHolder.peek());
            
            // 获取所有一级科室
            LambdaQueryWrapper<Department> firstLevelQuery = new LambdaQueryWrapper<>();
            firstLevelQuery.eq(Department::getDeptLevel, 1)
                          .orderByAsc(Department::getDeptId);
            List<Department> firstLevelList = departmentMapper.selectList(firstLevelQuery);

            // 获取所有二级科室
            LambdaQueryWrapper<Department> secondLevelQuery = new LambdaQueryWrapper<>();
            secondLevelQuery.eq(Department::getDeptLevel, 2)
                           .orderByAsc(Department::getDeptId);
            List<Department> secondLevelList = departmentMapper.selectList(secondLevelQuery);

            // 构建树形结构
            List<Map<String, Object>> tree = new ArrayList<>();
            for (Department firstLevel : firstLevelList) {
                Map<String, Object> firstLevelMap = new HashMap<>();
                firstLevelMap.put("deptId", firstLevel.getDeptId());
                firstLevelMap.put("deptName", firstLevel.getDeptName());
                firstLevelMap.put("deptDesc", firstLevel.getDeptDesc());
                firstLevelMap.put("location", firstLevel.getLocation());
                firstLevelMap.put("deptLevel", firstLevel.getDeptLevel());
                firstLevelMap.put("parentDeptId", firstLevel.getParentDeptId());

                // 获取该一级科室下的二级科室
                List<Map<String, Object>> children = new ArrayList<>();
                for (Department secondLevel : secondLevelList) {
                    if (secondLevel.getParentDeptId() != null && 
                        secondLevel.getParentDeptId().equals(firstLevel.getDeptId())) {
                        Map<String, Object> secondLevelMap = new HashMap<>();
                        secondLevelMap.put("deptId", secondLevel.getDeptId());
                        secondLevelMap.put("deptName", secondLevel.getDeptName());
                        secondLevelMap.put("deptDesc", secondLevel.getDeptDesc());
                        secondLevelMap.put("location", secondLevel.getLocation());
                        secondLevelMap.put("deptLevel", secondLevel.getDeptLevel());
                        secondLevelMap.put("parentDeptId", secondLevel.getParentDeptId());
                        children.add(secondLevelMap);
                    }
                }
                firstLevelMap.put("children", children);
                tree.add(firstLevelMap);
            }

            return tree;
        } finally {
            // 恢复数据源
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<Department> getSecondLevelByParentId(Long parentDeptId) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            LambdaQueryWrapper<Department> query = new LambdaQueryWrapper<>();
            query.eq(Department::getParentDeptId, parentDeptId)
                 .eq(Department::getDeptLevel, 2)
                 .orderByAsc(Department::getDeptId);
            return departmentMapper.selectList(query);
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    @DS("hospital")
    public List<Department> searchDepartments(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        try {
            DynamicDataSourceContextHolder.push("hospital");
            LambdaQueryWrapper<Department> query = new LambdaQueryWrapper<>();
            query.and(wrapper -> wrapper
                    .like(Department::getDeptName, keyword)
                    .or()
                    .like(Department::getDeptDesc, keyword)
                    .or()
                    .like(Department::getLocation, keyword))
                 .orderByAsc(Department::getDeptId);
            return departmentMapper.selectList(query);
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }
    
    /**
     * 重写getById方法，确保使用hospital数据源
     */
    @DS("hospital")
    @Override
    public Department getById(java.io.Serializable id) {
        try {
            DynamicDataSourceContextHolder.push("hospital");
            return departmentMapper.selectById(id);
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }
}




