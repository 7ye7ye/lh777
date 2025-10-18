package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.jeecg.modules.hospital.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【department(科室表)】的数据库操作Service实现
* @createDate 2025-09-22 20:15:20
*/
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
    implements DepartmentService{

}




