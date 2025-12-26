package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import org.jeecg.modules.hospital.common.ErrorCode;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.entity.PatientIdentityVerify;
import org.jeecg.modules.hospital.entity.VerificationAuditLog;
import org.jeecg.modules.hospital.exception.BusinessException;
import org.jeecg.modules.hospital.mapper.PatientIdentityVerifyMapper;
import org.jeecg.modules.hospital.mapper.VerificationAuditLogMapper;
import org.jeecg.modules.hospital.service.PatientIdentityVerifyService;
import org.jeecg.modules.hospital.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.IdcardUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@DS("hospital")
public class PatientIdentityVerifyServiceImpl extends ServiceImpl<PatientIdentityVerifyMapper, PatientIdentityVerify>
        implements PatientIdentityVerifyService {

    @Resource
    private PatientService patientService;
    
    @Resource
    private VerificationAuditLogMapper auditLogMapper;
    
    // 允许的图片格式
    private static final Set<String> ALLOWED_IMAGE_FORMATS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png")
    );
    
    // 最大文件大小 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyIdentity(Long userId,
                                 Long patientId,
                                 String identityPhoto,
                                 String handheldIdentityPhoto,
                                 String realName,
                                 String idCardNo) {
        // 1. 基础校验
        Patient dbPatient = patientService.getById(patientId);
        if (dbPatient == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未找到对应就诊卡");
        }
        
        // 2. 权限校验：确保只有就诊卡所属用户可以提交认证
        if (dbPatient.getUserId() != null && userId != null && !dbPatient.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无权操作此就诊卡");
        }
        
        // 3. 身份证号格式校验
        if (idCardNo == null || !IdcardUtil.isValidCard(idCardNo)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "身份证号格式不正确");
        }
        
        // 4. 姓名格式校验
        if (realName == null || realName.trim().isEmpty() || !realName.matches("^[\\u4e00-\\u9fa5a-zA-Z\\s]+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "姓名格式不正确");
        }
        
        // 5. 图片格式校验
        validateImageFormat(identityPhoto);
        validateImageFormat(handheldIdentityPhoto);

        // 6. 查找或创建认证记录
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
            entity.setStatus(0); // 待审核
            entity.setRejectReason(null);
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            this.save(entity);
            
            // 记录审核日志
            createAuditLog(patientId, userId, "SUBMIT", "提交身份认证申请", null);
        } else {
            exist.setIdentityPhoto(identityPhoto);
            exist.setHandheldIdentityPhoto(handheldIdentityPhoto);
            exist.setRealName(realName);
            exist.setIdCardNo(idCardNo);
            exist.setStatus(0); // 重置为待审核
            exist.setRejectReason(null);
            exist.setUpdateTime(now);
            this.updateById(exist);
            
            // 记录审核日志
            createAuditLog(patientId, userId, "RESUBMIT", "重新提交身份认证申请", null);
        }

        // 7. 更新患者表的认证状态
        Patient update = new Patient();
        update.setPatientId(patientId);
        update.setIdentityVerify(0); // 待审核
        update.setVerifyTime(null);
        patientService.updateById(update);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveIdentity(Long patientId, Long operatorId, String operatorName) {
        // 1. 查找认证记录
        LambdaQueryWrapper<PatientIdentityVerify> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PatientIdentityVerify::getPatientId, patientId);
        PatientIdentityVerify record = this.getOne(wrapper);
        
        if (record == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未找到认证申请记录");
        }
        
        // 2. 更新认证记录状态
        LocalDateTime now = LocalDateTime.now();
        record.setStatus(1); // 已通过
        record.setUpdateTime(now);
        this.updateById(record);
        
        // 3. 更新患者表认证状态
        Patient patient = new Patient();
        patient.setPatientId(patientId);
        patient.setIdentityVerify(1); // 已认证
        patient.setVerifyTime(now);
        patientService.updateById(patient);
        
        // 4. 记录审核日志
        createAuditLog(patientId, operatorId, "APPROVE", "审核通过", operatorName);
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectIdentity(Long patientId, String reason, Long operatorId, String operatorName) {
        // 1. 查找认证记录
        LambdaQueryWrapper<PatientIdentityVerify> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PatientIdentityVerify::getPatientId, patientId);
        PatientIdentityVerify record = this.getOne(wrapper);
        
        if (record == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未找到认证申请记录");
        }
        
        // 2. 更新认证记录状态
        LocalDateTime now = LocalDateTime.now();
        record.setStatus(2); // 已驳回
        record.setRejectReason(reason);
        record.setUpdateTime(now);
        this.updateById(record);
        
        // 3. 更新患者表认证状态
        Patient patient = new Patient();
        patient.setPatientId(patientId);
        patient.setIdentityVerify(2); // 已驳回
        patient.setVerifyTime(now);
        patientService.updateById(patient);
        
        // 4. 记录审核日志
        createAuditLog(patientId, operatorId, "REJECT", "审核驳回：" + reason, operatorName);
        
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
    
    /**
     * 验证图片格式和大小
     * @param imageUrl 图片URL或路径
     */
    private void validateImageFormat(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不能为空");
        }
        
        // 检查文件格式
        String lowerCaseUrl = imageUrl.toLowerCase();
        boolean validFormat = false;
        for (String format : ALLOWED_IMAGE_FORMATS) {
            if (lowerCaseUrl.endsWith("." + format)) {
                validFormat = true;
                break;
            }
        }
        
        if (!validFormat) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                "请上传JPG或PNG格式的图片");
        }
        
        // 注意：实际文件大小检查应在文件上传时进行，这里只是示例
        // 实际项目中，文件大小检查通常在文件上传控制器中完成
    }
    
    /**
     * 创建审核日志记录
     */
    private void createAuditLog(Long patientId, Long operatorId, String action, String description, String operatorName) {
        VerificationAuditLog log = new VerificationAuditLog();
        log.setPatientId(patientId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setAction(action);
        log.setDescription(description);
        log.setCreateTime(LocalDateTime.now());
        auditLogMapper.insert(log);
    }
}
