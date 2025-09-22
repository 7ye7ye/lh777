package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.Admin;
import org.jeecg.modules.hospital.service.AdminService;
import org.jeecg.modules.hospital.mapper.AdminMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【admin(管理员表)】的数据库操作Service实现
* @createDate 2025-09-22 20:15:20
*/
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService{

}




