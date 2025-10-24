package org.jeecg.modules.hospital.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest;
import org.jeecg.modules.hospital.mapper.DoctorShiftChangeRequestMapper;
import org.jeecg.modules.hospital.service.DoctorShiftChangeRequestService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@DS("hospital")
public class DoctorShiftChangeRequestServiceImpl extends ServiceImpl<DoctorShiftChangeRequestMapper, DoctorShiftChangeRequest>
        implements DoctorShiftChangeRequestService {

    @Override
    public DoctorShiftChangeRequest apply(DoctorShiftChangeRequest req) {
        req.setStatus(1); // 待审批
        req.setApplyTime(java.time.LocalDateTime.now());
        // 审批相关字段初始为空
        req.setAdminId(null);
        req.setApproveTime(null);
        req.setRejectReason(null);
        req.setNewScheduleId(null);

        this.save(req);
        return req;
    }

    @Override
    public List<DoctorShiftChangeRequest> listByDoctor(Long doctorId, Integer status) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DoctorShiftChangeRequest> qw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        qw.eq(doctorId != null, DoctorShiftChangeRequest::getDoctorId, doctorId);
        qw.eq(status != null, DoctorShiftChangeRequest::getStatus, status);
        qw.orderByDesc(DoctorShiftChangeRequest::getApplyTime);
        return this.list(qw);
    }
}