package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.entity.DoctorProfileUpdateRequest;

/**
 * 医生资料修改申请 Service
 */
public interface DoctorProfileUpdateRequestService extends IService<DoctorProfileUpdateRequest> {

    /**
     * 创建一条新的资料修改申请
     * @param doctorId   医生ID
     * @param avatar     头像URL（可为空）
     * @param specialty  擅长领域
     * @param doctorDesc 医生简介（可为空）
     */
    void createRequest(Long doctorId, String avatar, String specialty, String doctorDesc);

    /**
     * 分页查询资料修改申请
     */
    Page<DoctorProfileUpdateRequest> pageRequests(long pageNo, long pageSize, Integer status);

    /** 审核通过 */
    void approveRequest(Long requestId, String reason);

    /** 审核驳回 */
    void rejectRequest(Long requestId, String reason);

    /**
     * 医生端：分页查询本人资料修改申请
     */
    Page<DoctorProfileUpdateRequest> pageMyRequests(Long doctorId, long pageNo, long pageSize, Integer status);
}
