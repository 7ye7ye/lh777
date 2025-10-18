package org.jeecg.modules.hospital.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.hospital.entity.Department;

import java.util.List;
import java.util.Map;

/**
 * 科室表数据库操作Mapper
 * @author Administrator
 * @createDate 2025-09-22 20:15:20
 */
@Mapper
@DS("hospital")
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 查询所有一级科室
     */
    List<Department> selectFirstLevelDepartments();

    /**
     * 根据父科室ID查询二级科室
     */
    List<Department> selectSecondLevelByParentId(Long parentDeptId);

    /**
     * 查询科室及下属二级科室（树形结构）
     */
    List<Map<String, Object>> selectDepartmentWithChildren();
}




