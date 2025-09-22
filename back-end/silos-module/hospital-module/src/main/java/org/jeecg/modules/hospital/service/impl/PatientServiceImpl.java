package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.service.PatientService;
import org.jeecg.modules.hospital.mapper.PatientMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【patient(患者表)】的数据库操作Service实现
* @createDate 2025-09-22 20:15:21
*/
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient>
    implements PatientService{

}




