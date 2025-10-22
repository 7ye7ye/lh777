package org.jeecg.modules.hospital.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.jeecg.modules.hospital.entity.Patient;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Administrator
* @description 针对表【patient(患者表)】的数据库操作Mapper
* @createDate 2025-09-22 20:15:21
* @Entity org.jeecg.modules.hospital.entity.Patient
*/
@DS("hospital")
public interface PatientMapper extends BaseMapper<Patient> {

}




