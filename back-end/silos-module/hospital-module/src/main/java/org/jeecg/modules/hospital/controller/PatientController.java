package org.jeecg.modules.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.hospital.common.ErrorCode;
import org.jeecg.modules.hospital.controller.request.PatientListRequest;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.exception.BusinessException;
import org.jeecg.modules.hospital.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/patient")
public class PatientController {
    @Resource
    private PatientService patientService;

    @PostMapping("/create")
    public ResponseEntity<HashMap<String, Object>> create(@RequestBody Patient patient) {
        try {
            // 1. 参数校验
            if (StringUtils.isBlank(patient.getPatientName())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "患者姓名不能为空");
            }
            if (StringUtils.isBlank(patient.getIdCard())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "证件号码不能为空");
            }
            if (StringUtils.isBlank(patient.getPhone())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不能为空");
            }

            // 2. 检查是否已存在相同的身份证号
            boolean exists_idcard= patientService.lambdaQuery()
                .eq(Patient::getIdCard, patient.getIdCard())
                .exists();
            if (exists_idcard) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "该证件号码已注册");
            }

            if(!StringUtils.isBlank(patient.getStudentId())){
                // 3. 检查是否已存在相同的学号
                boolean exists_stuId = patientService.lambdaQuery()
                        .eq(Patient::getStudentId, patient.getStudentId())
                        .exists();
                if (exists_stuId) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "该学号已注册");
                }
            }

            if(!StringUtils.isBlank(patient.getStaffId())){
                // 4. 检查是否已存在相同的教职工号
                boolean exists_teaId = patientService.lambdaQuery()
                        .eq(Patient::getStaffId, patient.getStaffId())
                        .exists();
                if (exists_teaId) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "该教职工号已注册");
                }
            }

            // 5. 检查是否已存在相同的手机号
            boolean exists_phone = patientService.lambdaQuery()
                    .eq(Patient::getPhone, patient.getPhone())
                    .exists();
            if (exists_phone) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "该手机号已注册");
            }

            // 6. 保存就诊卡信息
            boolean saved = patientService.save(patient);
            if (!saved) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "创建就诊卡失败，请稍后重试");
            }

            // 6. 返回成功响应
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("code", 200);
                put("message", "创建就诊卡成功");
                put("data", patient.getPatientId());
            }});
        } catch (BusinessException e) {
            // 业务异常
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<String, Object>() {{
                put("code", e.getCode());
                put("message", e.getMessage());
                put("description", e.getDescription());
            }});
        } catch (Exception e) {
            // 系统异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("code", 500);
                put("message", "系统异常，请稍后重试");
            }});
        }
    }

    @PostMapping("/update")
    public ResponseEntity<HashMap<String, Object>> update(@RequestBody Patient patient) {
        // 1. 验证主键是否存在
        if (patient.getPatientId() == null) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                put("code", 400);
                put("message", "患者ID不能为空");
            }});
        }

        // 2. 使用 MyBatis-Plus 的 updateById 方法执行更新
        boolean isUpdated = patientService.updateById(patient);

        // 3. 根据更新结果返回不同响应
        if (isUpdated) {
            return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                put("code", 200);
                put("message", "更新成功");
                put("data", patient.getPatientId()); // 返回更新的患者ID
            }});
        } else {
            return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                put("code", 300);
                put("message", "更新失败，未找到该患者或数据未变更");
            }});
        }
    }

    @PostMapping("/cardInfo")
    public ResponseEntity<HashMap<String, Object>> cardInfo(@RequestBody Patient patient ) {
        // 根据userId查询第一条患者信息，查询所有字段
        Patient patientInfo = patientService.lambdaQuery()
                .eq(Patient::getUserId, patient.getUserId())
                .list() // 查询列表
                .stream()
                .findFirst()
                .orElse(null); // 取第一条或 null

        HashMap<String, Object> result = new HashMap<>();
        if (patientInfo != null) {
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", patientInfo);
        } else {
            result.put("code", 404);
            result.put("message", "未找到该用户的就诊卡");
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/list")
    public ResponseEntity<HashMap<String, Object>> getPatientList(@RequestBody(required = false) PatientListRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Patient> queryWrapper = new LambdaQueryWrapper<>();

        // 核心：只查询与传入 userId 匹配的患者（必须添加，确保数据归属正确）
        if (request != null && request.getUserId() != null) {
            queryWrapper.eq(Patient::getUserId, request.getUserId());
        } else {
            // 可选：若未传 userId，返回无数据或提示，避免查询全表（根据业务需求调整）
            HashMap<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("code", 400);
            emptyResult.put("message", "请传入用户唯一标识 userId");
            emptyResult.put("data", Collections.emptyList());
            return ResponseEntity.ok(emptyResult);
        }

        // 保留原有筛选逻辑：按患者姓名模糊查询（可选）
        if (request != null && request.getPatientName() != null && !request.getPatientName().trim().isEmpty()) {
            queryWrapper.like(Patient::getPatientName, request.getPatientName().trim());
        }

        // 可扩展：保留其他筛选条件（如患者类型、认证状态等，按需启用）
        if (request != null && request.getPatientType() != null) {
            queryWrapper.eq(Patient::getPatientType, request.getPatientType());
        }
        if (request != null && request.getIdentityVerify() != null) {
            queryWrapper.eq(Patient::getIdentityVerify, request.getIdentityVerify());
        }

        // 执行查询：只返回当前 userId 下符合条件的患者
        List<Patient> patientList = patientService.list(queryWrapper);

        // 封装结果
        HashMap<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", patientList.isEmpty() ? "未找到该用户关联的患者" : "查询成功");
        result.put("data", patientList);

        return ResponseEntity.ok(result);
    }
}
