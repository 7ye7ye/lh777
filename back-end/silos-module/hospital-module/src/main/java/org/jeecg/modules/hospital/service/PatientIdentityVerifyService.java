package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.entity.PatientIdentityVerify;

import java.util.List;

public interface PatientIdentityVerifyService extends IService<PatientIdentityVerify> {

    boolean applyIdentity(Long userId,
                          Long patientId,
                          String identityPhoto,
                          String realName,
                          String idCardNo);

    List<PatientIdentityVerify> listByStatus(Integer status);
}
