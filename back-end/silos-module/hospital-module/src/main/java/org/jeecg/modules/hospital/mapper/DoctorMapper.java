package org.jeecg.modules.hospital.mapper;

import org.jeecg.modules.hospital.entity.Doctor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
/**
 * @author Administrator
 * @description 针对表【doctor(医生表)】的数据库操作Mapper
 * @createDate 2025-09-22 20:15:21
 * @Entity org.jeecg.modules.hospital.entity.Doctor
 */
@DS("hospital")
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

}




