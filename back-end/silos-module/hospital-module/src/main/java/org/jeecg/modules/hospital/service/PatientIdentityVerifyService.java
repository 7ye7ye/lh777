package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.entity.PatientIdentityVerify;

import java.util.List;

public interface PatientIdentityVerifyService extends IService<PatientIdentityVerify> {

    /**
     * 提交身份认证申请
     * 
     * @param userId 用户ID
     * @param patientId 患者ID
     * @param identityPhoto 身份证照片
     * @param handheldIdentityPhoto 手持身份证照片
     * @param realName 真实姓名
     * @param idCardNo 身份证号
     * @return 是否提交成功
     */
    boolean applyIdentity(Long userId,
                          Long patientId,
                          String identityPhoto,
                          String handheldIdentityPhoto,
                          String realName,
                          String idCardNo);
    
    /**
     * 审核通过身份认证
     * 
     * @param patientId 患者ID
     * @param operatorId 操作员ID
     * @param operatorName 操作员姓名
     * @return 是否操作成功
     */
    boolean approveIdentity(Long patientId, Long operatorId, String operatorName);
    
    /**
     * 驳回身份认证
     * 
     * @param patientId 患者ID
     * @param reason 驳回原因
     * @param operatorId 操作员ID
     * @param operatorName 操作员姓名
     * @return 是否操作成功
     */
    boolean rejectIdentity(Long patientId, String reason, Long operatorId, String operatorName);

    /**
     * 根据状态查询身份认证记录
     * 
     * @param status 状态码：0-待审核，1-已通过，2-已驳回
     * @return 认证记录列表
     */
    List<PatientIdentityVerify> listByStatus(Integer status);
}
