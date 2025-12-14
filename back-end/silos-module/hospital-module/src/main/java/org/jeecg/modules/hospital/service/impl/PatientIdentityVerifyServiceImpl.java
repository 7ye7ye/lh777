package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.entity.PatientIdentityVerify;
import org.jeecg.modules.hospital.mapper.PatientIdentityVerifyMapper;
import org.jeecg.modules.hospital.service.PatientIdentityVerifyService;
import org.jeecg.modules.hospital.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@DS("hospital")
public class PatientIdentityVerifyServiceImpl extends ServiceImpl<PatientIdentityVerifyMapper, PatientIdentityVerify>
        implements PatientIdentityVerifyService {

    @Resource
    private PatientService patientService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyIdentity(Long userId,
                                 Long patientId,
                                 String identityPhoto,
                                 String handheldIdentityPhoto,
                                 String realName,
                                 String idCardNo) {
        Patient dbPatient = patientService.getById(patientId);
        if (dbPatient == null) {
            return false;
        }
        if (dbPatient.getUserId() != null && userId != null && !dbPatient.getUserId().equals(userId)) {
            return false;
        }

        LambdaQueryWrapper<PatientIdentityVerify> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PatientIdentityVerify::getPatientId, patientId);
        PatientIdentityVerify exist = this.getOne(wrapper);

        LocalDateTime now = LocalDateTime.now();
        if (exist == null) {
            PatientIdentityVerify entity = new PatientIdentityVerify();
            entity.setPatientId(patientId);
            entity.setUserId(userId);
            entity.setIdentityPhoto(identityPhoto);
            entity.setHandheldIdentityPhoto(handheldIdentityPhoto);
            entity.setRealName(realName);
            entity.setIdCardNo(idCardNo);
            entity.setStatus(0);
            entity.setRejectReason(null);
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            this.save(entity);
        } else {
            exist.setIdentityPhoto(identityPhoto);
            exist.setHandheldIdentityPhoto(handheldIdentityPhoto);
            exist.setRealName(realName);
            exist.setIdCardNo(idCardNo);
            exist.setStatus(0);
            exist.setRejectReason(null);
            exist.setUpdateTime(now);
            this.updateById(exist);
        }

        Patient update = new Patient();
        update.setPatientId(patientId);
        update.setIdentityVerify(0);
        update.setVerifyTime(null);
        patientService.updateById(update);
        return true;
    }

    @Override
    public List<PatientIdentityVerify> listByStatus(Integer status) {
        if (status == null) {
            return this.list();
        }
        LambdaQueryWrapper<PatientIdentityVerify> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PatientIdentityVerify::getStatus, status);
        return this.list(wrapper);
    }
}
