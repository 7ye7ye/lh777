package org.jeecg.modules.hospital.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import org.jeecg.modules.hospital.common.ErrorCode;
import org.jeecg.modules.hospital.controller.admin.dto.DoctorRegisterRequest;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.exception.BusinessException;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.HosUserService;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@DS("hospital")
@RestController
@RequestMapping("/admin/doctor")
public class AdminDoctorRegisterController {

    @Resource
    private HosUserService userService;

    @Resource
    private DoctorService doctorService;

    @Resource
    private DepartmentService departmentService;

    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class)  // 添加事务注解
    public Map<String, Object> registerDoctor(@RequestBody DoctorRegisterRequest request) {
        // 1. 校验参数
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求体不能为空");
        }

        // 2. 校验必填字段
        if (StrUtil.hasBlank(request.getUserAccount(), request.getDoctorName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号和医生姓名不能为空");
        }

        // 3. 校验医生姓名格式
        String doctorName = request.getDoctorName().trim();
        if (!doctorName.matches("^[\\u4e00-\\u9fa5a-zA-Z]+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生姓名只能包含中文和英文字母");
        }
        if (doctorName.length() < 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生姓名不能少于2位");
        }
        if (doctorName.length() > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生姓名不能超过10位");
        }
        request.setDoctorName(doctorName);

        // 4. 校验科室ID
        if (request.getDeptId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "科室ID不能为空");
        }
        Department department = departmentService.getById(request.getDeptId());
        if (department == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "指定的科室不存在");
        }

        // 5. 校验职称（如果提供）
        if (StrUtil.isNotBlank(request.getTitle())) {
            String title = request.getTitle().trim();
            if (title.length() < 2) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "职称不能少于2位");
            }
            if (title.length() > 10) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "职称不能超过10位");
            }
            request.setTitle(title);
        }

        // 6. 校验职称ID（必填）
        if (request.getTitleId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "职称ID不能为空");
        }

        // 7. 校验擅长领域（如果提供）
        if (StrUtil.isNotBlank(request.getSpecialty())) {
            String specialty = request.getSpecialty().trim();
            if (specialty.length() < 5) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "擅长领域不能少于5位");
            }
            if (specialty.length() > 200) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "擅长领域不能超过200位");
            }
            request.setSpecialty(specialty);
        }

        // 8. 校验医生简介（如果提供）
        if (StrUtil.isNotBlank(request.getDoctorDesc())) {
            String doctorDesc = request.getDoctorDesc().trim();
            if (doctorDesc.length() < 10) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生简介不能少于10位");
            }
            if (doctorDesc.length() > 500) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生简介不能超过500位");
            }
            request.setDoctorDesc(doctorDesc);
        }

        // 9. 校验用户类型必须为医生（2）
        if (request.getUserType() != 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户类型必须为医生");
        }

        // 10. 生成随机密码（8位，包含大小写字母、数字和特殊字符）
        String randomPassword = generateRandomPassword();
        request.setUserPassword(randomPassword);

        // 11. 先注册 hos_user 账号
        Long userId;
        try {
            // 注册医生账号，设置用户类型为医生(2)，并设置状态为1(正常)
            userId = userService.userRegister(
                    request.getUserAccount(),
                    request.getUserPassword(),
                    request.getUserPassword(),
                    request.getUserType(), // user_type = 2 (医生)
                    1 // 设置账号状态为1(正常)
            ).getData();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "用户账号注册失败: " + e.getMessage());
        }

        try {
            // 12. 将 doctor 表需要的信息插入
            Doctor doctor = new Doctor();
            doctor.setUserId(userId);
            doctor.setDoctorName(request.getDoctorName());
            doctor.setDeptId(request.getDeptId());
            doctor.setTitle(request.getTitle());
            doctor.setTitleId(request.getTitleId());  // 设置职称ID
            doctor.setSpecialty(request.getSpecialty());
            doctor.setDoctorDesc(request.getDoctorDesc());
            doctor.setIsActive(Boolean.TRUE.equals(request.getIsActive()) ? 1 : 0);
            doctor.setUpdateVerify(0); // 新注册医生账户未申请修改个人信息

            if (!doctorService.save(doctor)) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败");
            }

            // 返回医生信息和生成的密码
            Map<String, Object> result = new HashMap<>();
            result.put("doctor", doctor);
            result.put("password", randomPassword);
            result.put("message", "医生账户创建成功，请将密码通知医生：" + randomPassword);

            return result;

        } catch (Exception e) {
            // 如果医生信息保存失败，事务会回滚，包括之前创建的用户账号
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败: " + e.getMessage());
        }
    }

    /**
     * 生成随机密码（8位，包含大小写字母、数字和特殊字符）
     * @return 随机密码
     */
    private String generateRandomPassword() {
        // 定义密码字符集
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*";
        String allChars = upperCase + lowerCase + numbers + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(8);

        // 确保至少包含一个大写字母
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));

        // 确保至少包含一个小写字母
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));

        // 确保至少包含一个数字
        password.append(numbers.charAt(random.nextInt(numbers.length())));

        // 确保至少包含一个特殊字符
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // 填充剩余4位
        for (int i = 0; i < 4; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 打乱密码顺序
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }
}
