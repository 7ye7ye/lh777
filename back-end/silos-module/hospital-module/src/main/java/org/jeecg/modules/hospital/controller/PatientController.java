package org.jeecg.modules.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.hospital.common.ErrorCode;
import org.jeecg.modules.hospital.controller.request.PatientListRequest;
import org.jeecg.modules.hospital.controller.response.PatientIdentityAdminItem;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.exception.BusinessException;
import org.jeecg.modules.hospital.service.PatientService;
import org.jeecg.modules.hospital.service.PatientIdentityVerifyService;
import org.jeecg.modules.hospital.entity.PatientIdentityVerify;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/patient")
public class PatientController {
    @Resource
    private PatientService patientService;

    @Resource
    private PatientIdentityVerifyService patientIdentityVerifyService;

    @Value("${jeecg.domainUrl:http://127.0.0.1:8095}")
    private String domainUrl;

    /**
     * 构建完整的图片 URL
     */
    private String buildFullImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        String cleanPath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return domainUrl + "/jeecg-boot/sys/common/static/" + cleanPath;
    }

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

    @PostMapping("/identity/adminList")
    public ResponseEntity<Map<String, Object>> adminIdentityList(@RequestBody(required = false) Map<String, Object> body) {
        Integer status = null;
        if (body != null && body.get("status") != null) {
            status = Integer.valueOf(body.get("status").toString());
        }

        LambdaQueryWrapper<PatientIdentityVerify> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(PatientIdentityVerify::getStatus, status);
        }
        List<PatientIdentityVerify> verifyRecords = patientIdentityVerifyService.list(wrapper);

        List<PatientIdentityAdminItem> resultList = new ArrayList<>();
        if (verifyRecords != null && !verifyRecords.isEmpty()) {
            for (PatientIdentityVerify item : verifyRecords) {
                Patient patient = patientService.getById(item.getPatientId());
                if (patient == null) {
                    continue;
                }
                PatientIdentityAdminItem vo = new PatientIdentityAdminItem();
                vo.setPatientId(patient.getPatientId());
                vo.setPatientName(patient.getPatientName());
                vo.setPhone(patient.getPhone());
                vo.setPatientType(patient.getPatientType());
                vo.setStudentId(patient.getStudentId());
                vo.setStaffId(patient.getStaffId());
                vo.setIdentityVerify(patient.getIdentityVerify());
                // 将相对路径转换为完整 URL
                vo.setIdentityPhoto(buildFullImageUrl(item.getIdentityPhoto()));
                vo.setHandheldIdentityPhoto(buildFullImageUrl(item.getHandheldIdentityPhoto()));
                resultList.add(vo);
            }
        }

        Map<String, Object> page = new HashMap<>();
        page.put("records", resultList);
        page.put("total", resultList.size());
        page.put("size", resultList.size());
        page.put("current", 1);
        page.put("pages", 1);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "");
        resp.put("code", 200);
        resp.put("result", page);
        resp.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(resp);
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
        // 优先根据 patientId 查询指定就诊人的就诊卡，如未传则根据 userId 查询第一条患者信息
        if (patient == null || (patient.getPatientId() == null && patient.getUserId() == null)) {
            HashMap<String, Object> result = new HashMap<>();
            result.put("code", 400);
            result.put("message", "请至少提供 patientId 或 userId");
            return ResponseEntity.ok(result);
        }

        Patient patientInfo = null;
        if (patient.getPatientId() != null) {
            // 按 patientId 精确查询
            patientInfo = patientService.getById(patient.getPatientId());
        } else if (patient.getUserId() != null) {
            // 根据userId查询第一条患者信息，查询所有字段
            patientInfo = patientService.lambdaQuery()
                    .eq(Patient::getUserId, patient.getUserId())
                    .list() // 查询列表
                    .stream()
                    .findFirst()
                    .orElse(null); // 取第一条或 null
        }

        HashMap<String, Object> result = new HashMap<>();
        if (patientInfo != null) {
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", patientInfo);
        } else {
            result.put("code", 404);
            result.put("message", "未找到就诊卡信息");
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/list")
    public ResponseEntity<HashMap<String, Object>> getPatientList(@RequestBody(required = false) PatientListRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Patient> queryWrapper = new LambdaQueryWrapper<>();

        // 核心：只查询与传入 userId 匹配且未被软删除的患者（isDeleted = 0）
        if (request != null && request.getUserId() != null) {
            queryWrapper.eq(Patient::getUserId, request.getUserId())
                        .eq(Patient::getIsDeleted, 0);
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

    @PostMapping("/unbind")
    public ResponseEntity<HashMap<String, Object>> unbind(@RequestBody Patient patient) {
        HashMap<String, Object> result = new HashMap<>();

        try {
            if (patient == null || patient.getUserId() == null || patient.getPatientId() == null) {
                result.put("code", 400);
                result.put("message", "userId 和 patientId 不能为空");
                return ResponseEntity.ok(result);
            }

            boolean success = patientService.unbindPatientCard(patient.getUserId(), patient.getPatientId());
            if (success) {
                result.put("code", 200);
                result.put("message", "解绑成功");
            } else {
                result.put("code", 404);
                result.put("message", "未找到需要解绑的就诊人或已解绑");
            }

            return ResponseEntity.ok(result);
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<String, Object>() {{
                put("code", e.getCode());
                put("message", e.getMessage());
                put("description", e.getDescription());
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("code", 500);
                put("message", "解绑失败，系统异常，请稍后重试");
            }});
        }
    }

    /**
     * 用户提交/更新身份认证申请：
     * - 更新学号/工号、证件照片
     * - 将 identityVerify 置为 0（未审核）、清空 verifyTime
     */
    @PostMapping("/identity/apply")
    public ResponseEntity<HashMap<String, Object>> applyIdentity(@RequestBody Patient request) {
        HashMap<String, Object> result = new HashMap<>();

        try {
            if (request == null || request.getPatientId() == null) {
                result.put("code", 400);
                result.put("message", "patientId 不能为空");
                return ResponseEntity.ok(result);
            }

            Patient dbPatient = patientService.getById(request.getPatientId());
            if (dbPatient == null) {
                result.put("code", 404);
                result.put("message", "未找到对应就诊卡");
                return ResponseEntity.ok(result);
            }

            Integer patientType = dbPatient.getPatientType();
            if (patientType != null) {
                if (patientType == 1 && (request.getStudentId() == null || request.getStudentId().trim().isEmpty())) {
                    result.put("code", 400);
                    result.put("message", "学生身份必须填写学号");
                    return ResponseEntity.ok(result);
                }
                if (patientType == 2 && (request.getStaffId() == null || request.getStaffId().trim().isEmpty())) {
                    result.put("code", 400);
                    result.put("message", "教职工身份必须填写工号");
                    return ResponseEntity.ok(result);
                }
            }

            if (request.getIdentityPhoto() == null || request.getIdentityPhoto().trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "请先上传证件照片");
                return ResponseEntity.ok(result);
            }
            if (request.getHandheldIdentityPhoto() == null || request.getHandheldIdentityPhoto().trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "请先上传手持证件照片");
                return ResponseEntity.ok(result);
            }

            boolean success = patientIdentityVerifyService.applyIdentity(
                    dbPatient.getUserId(),
                    dbPatient.getPatientId(),
                    request.getIdentityPhoto(),
                    request.getHandheldIdentityPhoto(),
                    dbPatient.getPatientName(),
                    dbPatient.getIdCard()
            );
            if (!success) {
                result.put("code", 500);
                result.put("message", "提交认证申请失败，请稍后重试");
                return ResponseEntity.ok(result);
            }

            result.put("code", 200);
            result.put("message", "身份认证申请已提交，请等待管理员审核");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("code", 500);
                put("message", "系统异常，请稍后重试");
            }});
        }
    }

    /**
     * 管理员审核身份认证
     * approve=true: 通过 -> identityVerify=1, verifyTime=now
     * approve=false: 驳回 -> identityVerify=2
     */
    @PostMapping("/identity/approve")
    public ResponseEntity<HashMap<String, Object>> approveIdentity(@RequestBody HashMap<String, Object> body) {
        HashMap<String, Object> result = new HashMap<>();

        try {
            Object patientIdObj = body.get("patientId");
            Object approveObj = body.get("approve");
            Object rejectReasonObj = body.get("rejectReason");
            if (patientIdObj == null || approveObj == null) {
                result.put("code", 400);
                result.put("message", "patientId 和 approve 参数不能为空");
                return ResponseEntity.ok(result);
            }

            Long patientId = Long.valueOf(patientIdObj.toString());
            boolean approve = Boolean.parseBoolean(approveObj.toString());
            String rejectReason = rejectReasonObj == null ? null : rejectReasonObj.toString();

            Patient dbPatient = patientService.getById(patientId);
            if (dbPatient == null) {
                result.put("code", 404);
                result.put("message", "未找到对应就诊卡");
                return ResponseEntity.ok(result);
            }

            // 先查询认证记录，获取照片路径
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PatientIdentityVerify> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(PatientIdentityVerify::getPatientId, patientId);
            PatientIdentityVerify record = patientIdentityVerifyService.getOne(wrapper);

            Patient update = new Patient();
            update.setPatientId(patientId);
            if (approve) {
                update.setIdentityVerify(1);
                update.setVerifyTime(LocalDateTime.now());
                // 审核通过时，将照片路径同步到 patient 表
                if (record != null) {
                    if (record.getIdentityPhoto() != null) {
                        update.setIdentityPhoto(record.getIdentityPhoto());
                    }
                    if (record.getHandheldIdentityPhoto() != null) {
                        update.setHandheldIdentityPhoto(record.getHandheldIdentityPhoto());
                    }
                }
            } else {
                update.setIdentityVerify(2);
                update.setVerifyTime(null);
            }

            boolean success = patientService.updateById(update);
            if (!success) {
                result.put("code", 500);
                result.put("message", "审核失败，请稍后重试");
                return ResponseEntity.ok(result);
            }

            // 更新认证记录状态
            if (record != null) {
                record.setStatus(approve ? 1 : 2);
                record.setRejectReason(approve ? null : rejectReason);
                record.setUpdateTime(LocalDateTime.now());
                patientIdentityVerifyService.updateById(record);
            }

            result.put("code", 200);
            result.put("message", approve ? "已通过该身份认证申请" : "已驳回该身份认证申请");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("code", 500);
                put("message", "系统异常，请稍后重试");
            }});
        }
    }

    /**
     * 健康档案 - 获取
     * 对应前端 patientApi.getHealthProfile({ patientId })
     */
    @PostMapping("/health/get")
    public ResponseEntity<HashMap<String, Object>> getHealthProfile(@RequestBody Map<String, Object> body) {
        HashMap<String, Object> result = new HashMap<>();

        Object patientIdObj = body == null ? null : body.get("patientId");
        if (patientIdObj == null) {
            result.put("code", 400);
            result.put("message", "patientId 不能为空");
            return ResponseEntity.ok(result);
        }

        Long patientId = Long.valueOf(patientIdObj.toString());
        Patient patient = patientService.getById(patientId);
        if (patient == null) {
            result.put("code", 404);
            result.put("message", "未找到对应就诊卡");
            return ResponseEntity.ok(result);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("height", patient.getHeight());
        data.put("weight", patient.getWeight());
        data.put("bloodType", patient.getBloodType());
        data.put("maritalStatus", patient.getMaritalStatus());
        data.put("fertilityStatus", patient.getFertilityStatus());
        data.put("currentIllness", patient.getPresentIllness());
        data.put("pastHistory", patient.getPastIllness());
        data.put("familyHistory", patient.getFamilyIllness());
        data.put("allergyHistory", patient.getAllergyHistory());

        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    /**
     * 健康档案 - 更新
     * 对应前端 patientApi.updateHealthProfile
     */
    @PostMapping("/health/update")
    public ResponseEntity<HashMap<String, Object>> updateHealthProfile(@RequestBody Map<String, Object> body) {
        HashMap<String, Object> result = new HashMap<>();

        Object patientIdObj = body == null ? null : body.get("patientId");
        if (patientIdObj == null) {
            result.put("code", 400);
            result.put("message", "patientId 不能为空");
            return ResponseEntity.ok(result);
        }

        Long patientId = Long.valueOf(patientIdObj.toString());
        Patient patient = patientService.getById(patientId);
        if (patient == null) {
            result.put("code", 404);
            result.put("message", "未找到对应就诊卡");
            return ResponseEntity.ok(result);
        }

        try {
            if (body.get("height") != null && !body.get("height").toString().trim().isEmpty()) {
                patient.setHeight(new java.math.BigDecimal(body.get("height").toString()));
            }
            if (body.get("weight") != null && !body.get("weight").toString().trim().isEmpty()) {
                patient.setWeight(new java.math.BigDecimal(body.get("weight").toString()));
            }
            if (body.get("bloodType") != null) {
                patient.setBloodType(body.get("bloodType").toString());
            }
            if (body.get("maritalStatus") != null) {
                patient.setMaritalStatus(body.get("maritalStatus").toString());
            }
            if (body.get("fertilityStatus") != null) {
                patient.setFertilityStatus(body.get("fertilityStatus").toString());
            }
            if (body.get("currentIllness") != null) {
                patient.setPresentIllness(body.get("currentIllness").toString());
            }
            if (body.get("pastHistory") != null) {
                patient.setPastIllness(body.get("pastHistory").toString());
            }
            if (body.get("familyHistory") != null) {
                patient.setFamilyIllness(body.get("familyHistory").toString());
            }
            if (body.get("allergyHistory") != null) {
                patient.setAllergyHistory(body.get("allergyHistory").toString());
            }

            boolean updated = patientService.updateById(patient);
            if (!updated) {
                result.put("code", 500);
                result.put("message", "保存失败，请稍后重试");
                return ResponseEntity.ok(result);
            }

            result.put("code", 200);
            result.put("message", "保存成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("code", 500);
                put("message", "保存失败，系统异常，请稍后重试");
            }});
        }
    }
}
