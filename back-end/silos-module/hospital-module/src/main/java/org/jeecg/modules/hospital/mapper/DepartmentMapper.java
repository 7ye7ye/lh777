package org.jeecg.modules.hospital.mapper;

import org.jeecg.modules.hospital.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Administrator
* @description 针对表【department(科室表)】的数据库操作Mapper
* @createDate 2025-09-22 20:15:20
* @Entity org.jeecg.modules.hospital.entity.Department
*/
@Mapper
@DS("hospital")
public interface DepartmentMapper extends BaseMapper<Department> {

}




