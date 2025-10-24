package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest;

import java.util.List;

public interface DoctorShiftChangeRequestService extends IService<DoctorShiftChangeRequest> {
    DoctorShiftChangeRequest apply(DoctorShiftChangeRequest req);
    List<DoctorShiftChangeRequest> listByDoctor(Long doctorId, Integer status);
}