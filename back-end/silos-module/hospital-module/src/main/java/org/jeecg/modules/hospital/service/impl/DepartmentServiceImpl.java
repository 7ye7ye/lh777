package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.jeecg.modules.hospital.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
@DS("hospital")
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
    implements DepartmentService{

}




