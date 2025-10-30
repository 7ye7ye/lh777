package org.jeecg.modules.hospital.controller;

import jakarta.annotation.Resource;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/patient")
public class PatientController {
    @Resource
    private PatientService patirntService;

    @PostMapping("/create")
    public ResponseEntity<HashMap<String, Object>> create(@RequestBody Patient patient) {
        patirntService.save(patient);
        return ResponseEntity.ok().body(new HashMap<String, Object>() {{
            put("code", 200);
            put("message", "创建就诊卡成功");
        }});
    }

    @PostMapping("/cardInfo")
    public ResponseEntity<HashMap<String, Object>> cardInfo(@RequestBody Patient patient ) {
        // 使用service层的自定义查询方法，避免触发patient_name查询错误
        Patient patientInfo = patirntService.cardInfo(patient);

        // 封装返回结果
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
}
