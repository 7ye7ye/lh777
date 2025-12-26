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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.sql.SQLException;
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
        } catch (BusinessException e) {
            // 如果是业务异常，提取精准的错误描述
            String errorDetail = e.getDescription();
            if (errorDetail != null && !errorDetail.isEmpty()) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "用户账号注册失败：" + errorDetail);
            } else {
                // 如果没有详细描述，使用异常消息
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "用户账号注册失败：" + e.getMessage());
            }
        } catch (Exception e) {
            // 其他类型的异常，提供更详细的错误信息
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Duplicate entry")) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "用户账号注册失败：该账号已被注册，请使用其他账号");
            } else if (errorMsg != null && errorMsg.contains("SQL")) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "用户账号注册失败：数据库操作异常，请稍后重试");
            } else {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "用户账号注册失败：" + (errorMsg != null ? errorMsg : "未知错误"));
            }
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

        } catch (BusinessException e) {
            // 如果是业务异常，提取精准的错误描述
            String errorDetail = e.getDescription();
            if (errorDetail != null && !errorDetail.isEmpty()) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：" + errorDetail);
            } else {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：" + e.getMessage());
            }
        } catch (DuplicateKeyException e) {
            // 唯一约束违反（如主键、唯一索引重复）
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("user_id")) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：该用户已关联其他医生账号");
            } else if (errorMsg != null && errorMsg.contains("doctor_id")) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：医生ID已存在");
            } else {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：数据唯一性约束冲突，请检查输入信息");
            }
        } catch (DataIntegrityViolationException e) {
            // 数据完整性约束违反（外键、非空、检查约束等）
            String errorMsg = e.getMessage();
            Throwable rootCause = e.getRootCause();
            String rootCauseMsg = rootCause != null ? rootCause.getMessage() : null;
            
            if (rootCauseMsg != null) {
                if (rootCauseMsg.contains("foreign key constraint") || rootCauseMsg.contains("FOREIGN KEY")) {
                    if (rootCauseMsg.contains("dept_id") || rootCauseMsg.contains("deptId")) {
                        throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：指定的科室不存在或已被删除");
                    } else if (rootCauseMsg.contains("user_id") || rootCauseMsg.contains("userId")) {
                        throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：关联的用户账号不存在");
                    } else {
                        throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：外键约束违反，请检查关联数据");
                    }
                } else if (rootCauseMsg.contains("cannot be null") || rootCauseMsg.contains("NOT NULL")) {
                    // 提取字段名
                    String fieldName = extractFieldName(rootCauseMsg);
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：必填字段" + fieldName + "不能为空");
                } else if (rootCauseMsg.contains("Duplicate entry")) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：数据重复，请检查输入信息");
                }
            }
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：数据完整性约束违反，" + (rootCauseMsg != null ? rootCauseMsg : errorMsg));
        } catch (Exception e) {
            // 检查是否是SQLException（可能被包装在其他异常中）
            Throwable cause = e.getCause();
            if (cause instanceof SQLException) {
                SQLException sqlEx = (SQLException) cause;
                int errorCode = sqlEx.getErrorCode();
                String errorMsg = sqlEx.getMessage();
                
                // MySQL错误码
                if (errorCode == 1062) { // Duplicate entry
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：数据重复，请检查输入信息");
                } else if (errorCode == 1452) { // Foreign key constraint fails
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：外键约束违反，请检查关联数据");
                } else if (errorCode == 1048) { // Column cannot be null
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：必填字段不能为空");
                } else {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：SQL执行异常（错误码：" + errorCode + "），" + errorMsg);
                }
            }
            // 其他类型的异常，提供更详细的错误信息
            String errorMsg = e.getMessage();
            String className = e.getClass().getSimpleName();
            
            // 检查是否是数据库相关异常
            if (errorMsg != null) {
                if (errorMsg.contains("Duplicate entry") || errorMsg.contains("duplicate key")) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：数据重复，请检查输入信息");
                } else if (errorMsg.contains("foreign key") || errorMsg.contains("FOREIGN KEY")) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：外键约束违反，请检查关联数据");
                } else if (errorMsg.contains("cannot be null") || errorMsg.contains("NOT NULL")) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：必填字段不能为空");
                } else if (errorMsg.contains("SQL") || errorMsg.contains("sql")) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：数据库操作异常（" + className + "），" + errorMsg);
                }
            }
            
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "医生信息保存失败：" + className + " - " + (errorMsg != null ? errorMsg : "未知错误"));
        }
    }

    /**
     * 从错误消息中提取字段名
     * @param errorMsg 错误消息
     * @return 字段名（中文描述）
     */
    private String extractFieldName(String errorMsg) {
        if (errorMsg == null) {
            return "";
        }
        
        // 尝试提取字段名
        if (errorMsg.contains("doctor_name") || errorMsg.contains("doctorName")) {
            return "医生姓名";
        } else if (errorMsg.contains("dept_id") || errorMsg.contains("deptId")) {
            return "科室";
        } else if (errorMsg.contains("user_id") || errorMsg.contains("userId")) {
            return "用户ID";
        } else if (errorMsg.contains("title")) {
            return "职称";
        } else if (errorMsg.contains("specialty")) {
            return "擅长领域";
        } else if (errorMsg.contains("is_active") || errorMsg.contains("isActive")) {
            return "出诊状态";
        }
        
        // 如果无法识别，返回空字符串
        return "";
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
