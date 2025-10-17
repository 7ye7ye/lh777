package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.entity.Department;

import java.util.List;
import java.util.Map;

/**
 * 科室表服务接口
 * @author Administrator
 * @description 针对表【department(科室表)】的数据库操作Service
 * @createDate 2025-09-22 20:15:20
 */
public interface DepartmentService extends IService<Department> {

    /**
     * 获取所有一级科室列表
     */
    List<Department> getFirstLevelDepartments();

    /**
     * 根据父科室ID获取二级科室列表
     */
    List<Department> getSecondLevelByParentId(Long parentDeptId);

    /**
     * 获取科室树形结构（一级+二级）
     */
    List<Map<String, Object>> getDepartmentTree();

    /**
     * 创建科室
     */
    boolean createDepartment(Department department);

    /**
     * 更新科室信息
     */
    boolean updateDepartment(Department department);

    /**
     * 删除科室（检查是否有医生关联）
     */
    boolean deleteDepartment(Long deptId);

    /**
     * 搜索科室（模糊查询）
     */
    List<Department> searchDepartments(String keyword);
}
