package org.jeecg.modules.hospital.service;

import org.jeecg.modules.hospital.entity.Department;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author Administrator
* @description 针对表【department(科室表)】的数据库操作Service
* @createDate 2025-09-22 20:15:20
*/
public interface DepartmentService extends IService<Department> {

    /**
     * 获取科室树形结构（一级+二级）
     * @return 树形结构列表
     */
    List<Map<String, Object>> getDepartmentTree();

    /**
     * 根据父科室ID获取二级科室
     * @param parentDeptId 父科室ID
     * @return 二级科室列表
     */
    List<Department> getSecondLevelByParentId(Long parentDeptId);

    /**
     * 搜索科室
     * @param keyword 关键词
     * @return 科室列表
     */
    List<Department> searchDepartments(String keyword);
}
