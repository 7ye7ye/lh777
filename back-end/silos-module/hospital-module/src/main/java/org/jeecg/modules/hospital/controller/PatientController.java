package org.jeecg.modules.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.jeecg.modules.hospital.controller.request.PatientListRequest;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.service.PatientService;
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
        patientService.save(patient);
        return ResponseEntity.ok().body(new HashMap<String, Object>() {{
            put("code", 200);
            put("message", "创建就诊卡成功");
        }});
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
        // 1. 根据 userId 查询患者信息（userId 是关联字段，用条件构造器匹配）
        Patient patientInfo = patientService.lambdaQuery()
                .eq(Patient::getUserId, patient.getUserId()) // 匹配 patient 表的 userId 字段
                .one(); // 查询一条（确保 userId 唯一，避免多结果）

        // 2. 封装返回结果
        HashMap<String, Object> result = new HashMap<>();
        if (patientInfo != null) {
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", patientInfo); // 存入查询到的就诊卡信息
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
