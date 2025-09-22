package org.jeecg.modules.hospital.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.jeecg.modules.hospital.entity.HosUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Administrator
* @description 针对表【hos_user(用户表)】的数据库操作Mapper
* @createDate 2025-09-22 21:05:09
* @Entity org.jeecg.modules.hospital.entity.HosUser
*/
@DS("hospital")
public interface HosUserMapper extends BaseMapper<HosUser> {

}




