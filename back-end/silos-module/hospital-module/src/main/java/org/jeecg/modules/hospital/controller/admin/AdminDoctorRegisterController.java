package org.jeecg.modules.hospital.controller.admin;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.jeecg.modules.hospital.common.ErrorCode;
import org.jeecg.modules.hospital.controller.admin.dto.DoctorRegisterRequest;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.exception.BusinessException;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.HosUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/doctor")
public class AdminDoctorRegisterController {

    @Resource
    private HosUserService userService;

    @Resource
    private DoctorService doctorService;

    @PostMapping("/register")
    public Doctor registerDoctor(@RequestBody DoctorRegisterRequest request) {
        // 1. 校验参数
        if (request == null || StrUtil.hasBlank(request.getUserAccount(), request.getDoctorName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必要信息缺失");
        }

        // 2. 先注册 hos_user 账号
        Long userId = userService.userRegister(
                request.getUserAccount(),
                request.getUserPassword(),
                request.getUserPassword(),
                request.getUserType()// user_type = 2 (医生)
        ).getData();

        // 3. 将 doctor 表需要的信息插入
        Doctor doctor = new Doctor();
        doctor.setUserId(userId);
        doctor.setDoctorName(request.getDoctorName());
        doctor.setDeptId(request.getDeptId());
        doctor.setTitle(request.getTitle());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setDoctorDesc(request.getDoctorDesc());
        doctor.setIsActive(Boolean.TRUE.equals(request.getIsActive()) ? 1 : 0);
        doctor.setUpdateVerify(0);//新注册医生账户未申请修改个人信息

        doctorService.save(doctor);
        // 使用成功状态码构建返回结果

        return doctor;
    }
}
