package org.jeecg.modules.hospital.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.entity.DoctorProfileUpdateRequest;
import org.jeecg.modules.hospital.mapper.DoctorProfileUpdateRequestMapper;
import org.jeecg.modules.hospital.service.DoctorProfileUpdateRequestService;
import org.jeecg.modules.hospital.service.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * 医生资料修改申请 Service 实现
 */
@Service
@DS("hospital")
public class DoctorProfileUpdateRequestServiceImpl
        extends ServiceImpl<DoctorProfileUpdateRequestMapper, DoctorProfileUpdateRequest>
        implements DoctorProfileUpdateRequestService {

    @Resource
    private DoctorService doctorService;

    @Override
    public void createRequest(Long doctorId, String avatar, String specialty, String doctorDesc) {
        DoctorProfileUpdateRequest req = new DoctorProfileUpdateRequest();
        req.setDoctorId(doctorId);
        req.setAvatar(avatar);
        req.setSpecialty(specialty);
        req.setDoctorDesc(doctorDesc);
        // 1-待审核
        req.setStatus(1);
        req.setCreateTime(LocalDateTime.now());
        req.setUpdateTime(LocalDateTime.now());
        this.save(req);
    }

    @Override
    public Page<DoctorProfileUpdateRequest> pageRequests(long pageNo, long pageSize, Integer status) {
        LambdaQueryWrapper<DoctorProfileUpdateRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(DoctorProfileUpdateRequest::getStatus, status);
        }
        wrapper.orderByDesc(DoctorProfileUpdateRequest::getCreateTime);

        Page<DoctorProfileUpdateRequest> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        // 为每条记录补充医生姓名、头像，方便前端展示
        if (page != null && page.getRecords() != null) {
            for (DoctorProfileUpdateRequest req : page.getRecords()) {
                if (req != null && req.getDoctorId() != null) {
                    Doctor doctor = doctorService.getById(req.getDoctorId());
                    if (doctor != null) {
                        req.setDoctorName(doctor.getDoctorName());
                        if (req.getAvatar() == null) {
                            req.setAvatar(doctor.getAvatar());
                        }
                    }
                }
            }
        }
        return page;
    }

    @Override
    public Page<DoctorProfileUpdateRequest> pageMyRequests(Long doctorId, long pageNo, long pageSize, Integer status) {
        LambdaQueryWrapper<DoctorProfileUpdateRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorProfileUpdateRequest::getDoctorId, doctorId);
        if (status != null) {
            wrapper.eq(DoctorProfileUpdateRequest::getStatus, status);
        }
        wrapper.orderByDesc(DoctorProfileUpdateRequest::getCreateTime);

        Page<DoctorProfileUpdateRequest> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        if (page != null && page.getRecords() != null) {
            for (DoctorProfileUpdateRequest req : page.getRecords()) {
                if (req != null && req.getDoctorId() != null) {
                    Doctor doctor = doctorService.getById(req.getDoctorId());
                    if (doctor != null) {
                        req.setDoctorName(doctor.getDoctorName());
                        if (req.getAvatar() == null) {
                            req.setAvatar(doctor.getAvatar());
                        }
                    }
                }
            }
        }
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRequest(Long requestId, String reason) {
        DoctorProfileUpdateRequest req = this.getById(requestId);
        if (req == null) {
            return;
        }
        Doctor doctor = doctorService.getById(req.getDoctorId());
        if (doctor != null) {
            if (req.getAvatar() != null) doctor.setAvatar(req.getAvatar());
            if (req.getSpecialty() != null) doctor.setSpecialty(req.getSpecialty());
            if (req.getDoctorDesc() != null) doctor.setDoctorDesc(req.getDoctorDesc());
            // 2-已通过
            doctor.setUpdateVerify(2);
            doctorService.updateById(doctor);
        }

        req.setStatus(2);
        req.setReason(reason);
        req.setUpdateTime(LocalDateTime.now());
        this.updateById(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRequest(Long requestId, String reason) {
        DoctorProfileUpdateRequest req = this.getById(requestId);
        if (req == null) {
            return;
        }
        req.setStatus(3);
        req.setReason(reason);
        req.setUpdateTime(LocalDateTime.now());
        this.updateById(req);

        // 同步 doctor 表审核状态为驳回（3）
        Doctor doctor = doctorService.getById(req.getDoctorId());
        if (doctor != null) {
            doctor.setUpdateVerify(3);
            doctorService.updateById(doctor);
        }
    }
}
