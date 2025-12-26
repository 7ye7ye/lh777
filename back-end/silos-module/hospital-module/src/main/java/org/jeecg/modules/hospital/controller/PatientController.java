package org.jeecg.modules.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.hospital.common.ErrorCode;
import org.jeecg.modules.hospital.controller.request.PatientListRequest;
import org.jeecg.modules.hospital.controller.response.PatientIdentityAdminItem;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.exception.BusinessException;
import org.jeecg.modules.hospital.service.PatientService;
import org.jeecg.modules.hospital.service.PatientIdentityVerifyService;
import org.jeecg.modules.hospital.service.VerificationAuditLogService;
import org.jeecg.modules.hospital.entity.VerificationAuditLog;
import org.jeecg.modules.hospital.entity.PatientIdentityVerify;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;
import cn.hutool.core.util.IdcardUtil;

@RestController
@RequestMapping("/patient")
public class PatientController {
    @Resource
    private PatientService patientService;

    @Resource
    private PatientIdentityVerifyService patientIdentityVerifyService;
    
    @Resource
    private VerificationAuditLogService verificationAuditLogService;

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
            // 1. 参数校验 - 姓名
            if (StringUtils.isBlank(patient.getPatientName())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "患者姓名不能为空");
            }
            
            // 姓名格式验证：只能包含中文和英文字母
            String patientName = patient.getPatientName().trim();
            if (!patientName.matches("^[\\u4e00-\\u9fa5a-zA-Z]+$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "患者姓名只能包含中文和英文字母");
            }
            
            // 姓名长度验证：2-10位
            if (patientName.length() < 2) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "患者姓名不能少于2位");
            }
            if (patientName.length() > 10) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "患者姓名不能超过10位");
            }
            patient.setPatientName(patientName);
            
            // 2. 参数校验 - 身份证号
            if (StringUtils.isBlank(patient.getIdCard())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "证件号码不能为空");
            }
            // 身份证校验
            if (!IdcardUtil.isValidCard(patient.getIdCard())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "身份证号格式不正确");
            }
            
            // 3. 参数校验 - 手机号
            if (StringUtils.isBlank(patient.getPhone())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不能为空");
            }
            // 手机号校验
            String phone = patient.getPhone().trim();
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
            }
            patient.setPhone(phone);
            
            // 4. 患者类型验证
            Integer patientType = patient.getPatientType();
            if (patientType != null) {
                if (patientType == 1) {
                    // 学生类型：必须填写学号
                    if (StringUtils.isBlank(patient.getStudentId())) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "学生身份必须填写学号");
                    }
                    // 学号格式验证：10位固定长度，格式：年份(4位)+学院代码(2位)+流水号(4位)
                    String studentId = patient.getStudentId().trim();
                    if (!studentId.matches("^[12]\\d{3}[0-9]{2}\\d{4}$")) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                            "学号格式不正确，应为10位数字，格式：年份(4位)+学院代码(2位)+流水号(4位)，如：2024010001");
                    }
                    patient.setStudentId(studentId);
                } else if (patientType == 2) {
                    // 教职工类型：必须填写教职工号
                    if (StringUtils.isBlank(patient.getStaffId())) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "教职工身份必须填写教职工号");
                    }
                    // 教职工号格式验证：8位固定长度，格式：类型(1位)+部门(2位)+流水号(5位)
                    String staffId = patient.getStaffId().trim();
                    if (!staffId.matches("^[123][0-9]{2}\\d{5}$")) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                            "教职工号格式不正确，应为8位数字，格式：类型标识(1位，1-教师/2-行政/3-后勤)+部门代码(2位)+流水号(5位)，如：10100001");
                    }
                    patient.setStaffId(staffId);
                }
            }
            
            // 5. 检查该用户绑定的就诊人数量是否超过限制（最多5个）
            long count = patientService.lambdaQuery()
                    .eq(Patient::getUserId, patient.getUserId())
                    .eq(Patient::getIsDeleted, 0)
                    .count();
            if (count >= 5) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "一个账户最多只能绑定5名就诊人");
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
    public ResponseEntity<HashMap<String, Object>> update(@RequestBody Map<String, Object> requestBody) {
        try {
            // 将 Map 转换为 Patient 对象，兼容前端可能传递的不同字段名
            Patient patient = new Patient();
            
            // 1. 验证主键是否存在
            Object patientIdObj = requestBody.get("patientId");
            if (patientIdObj == null) {
                return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                    put("code", 40000);
                    put("message", "请求参数错误");
                    put("description", "患者ID不能为空");
                }});
            }
            Long patientId = Long.valueOf(patientIdObj.toString());
            patient.setPatientId(patientId);

            // 2. 检查患者是否存在
            Patient existingPatient = patientService.getById(patientId);
            if (existingPatient == null) {
                return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                    put("code", 40400);
                    put("message", "未找到该患者");
                    put("description", "患者ID " + patientId + " 不存在");
                }});
            }
            
            // 3. 映射其他字段（兼容前端可能传递的不同字段名）
            if (requestBody.containsKey("patientName")) {
                patient.setPatientName(requestBody.get("patientName") != null ? requestBody.get("patientName").toString() : null);
            }
            if (requestBody.containsKey("phone")) {
                patient.setPhone(requestBody.get("phone") != null ? requestBody.get("phone").toString() : null);
            }
            // 兼容 idNumber 和 idCard 两种字段名
            if (requestBody.containsKey("idNumber")) {
                patient.setIdCard(requestBody.get("idNumber") != null ? requestBody.get("idNumber").toString() : null);
            } else if (requestBody.containsKey("idCard")) {
                patient.setIdCard(requestBody.get("idCard") != null ? requestBody.get("idCard").toString() : null);
            }
            if (requestBody.containsKey("gender")) {
                patient.setGender(requestBody.get("gender") != null ? requestBody.get("gender").toString() : null);
            }
            if (requestBody.containsKey("birthDate")) {
                Object birthDateObj = requestBody.get("birthDate");
                if (birthDateObj != null) {
                    try {
                        patient.setBirthDate(java.time.LocalDate.parse(birthDateObj.toString()));
                    } catch (Exception e) {
                        // 日期解析失败，忽略
                    }
                }
            }
            if (requestBody.containsKey("idType")) {
                patient.setIdType(requestBody.get("idType") != null ? requestBody.get("idType").toString() : null);
            }
            if (requestBody.containsKey("nation")) {
                patient.setNation(requestBody.get("nation") != null ? requestBody.get("nation").toString() : null);
            }
            if (requestBody.containsKey("nationality")) {
                patient.setNationality(requestBody.get("nationality") != null ? requestBody.get("nationality").toString() : null);
            }
            if (requestBody.containsKey("region")) {
                patient.setRegion(requestBody.get("region") != null ? requestBody.get("region").toString() : null);
            }
            if (requestBody.containsKey("detailedAddress")) {
                patient.setDetailedAddress(requestBody.get("detailedAddress") != null ? requestBody.get("detailedAddress").toString() : null);
            }

            // 4. 校验必填字段
            if (patient.getPatientName() != null && !patient.getPatientName().trim().isEmpty()) {
                String patientName = patient.getPatientName().trim();
                if (!patientName.matches("^[\\u4e00-\\u9fa5a-zA-Z\\s]+$")) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "患者姓名只能包含中文和英文字母");
                    }});
                }
                if (patientName.length() < 2) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "患者姓名不能少于2位");
                    }});
                }
                if (patientName.length() > 10) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "患者姓名不能超过10位");
                    }});
                }
                patient.setPatientName(patientName);
            }

            // 5. 校验手机号格式（如果提供）
            if (patient.getPhone() != null && !patient.getPhone().trim().isEmpty()) {
                String phone = patient.getPhone().trim();
                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "手机号格式不正确，请输入11位有效手机号");
                    }});
                }
                // 检查手机号是否被其他患者使用（排除当前患者）
                boolean phoneExists = patientService.lambdaQuery()
                        .eq(Patient::getPhone, phone)
                        .ne(Patient::getPatientId, patientId)
                        .exists();
                if (phoneExists) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "该手机号已被其他患者使用");
                    }});
                }
            }

            // 6. 校验证件号码（如果提供）
            // 兼容前端可能传递的 idNumber 或 idCard 字段
            String idCardValue = null;
            if (patient.getIdCard() != null && !patient.getIdCard().trim().isEmpty()) {
                idCardValue = patient.getIdCard().trim();
            }
            
            // 如果提供了身份证号，必须进行验证
            if (idCardValue != null && !idCardValue.isEmpty()) {
                // 6.1 身份证号格式校验
                if (!IdcardUtil.isValidCard(idCardValue)) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "身份证号格式不正确，请输入18位有效身份证号");
                    }});
                }
                
                // 6.2 检查证件号码是否被其他患者使用（排除当前患者）
                boolean idCardExists = patientService.lambdaQuery()
                        .eq(Patient::getIdCard, idCardValue)
                        .ne(Patient::getPatientId, patientId)
                        .exists();
                if (idCardExists) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "该证件号码已被其他患者使用");
                    }});
                }
                
                // 6.3 设置到 patient 对象中，确保更新时使用正确的值
                patient.setIdCard(idCardValue);
            }

            // 7. 使用 MyBatis-Plus 的 updateById 方法执行更新
            boolean isUpdated = patientService.updateById(patient);

            // 8. 根据更新结果返回不同响应
            if (isUpdated) {
                return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                    put("code", 20000);
                    put("message", "更新成功");
                    put("data", patientId); // 返回更新的患者ID
                }});
            } else {
                return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                    put("code", 30000);
                    put("message", "更新失败");
                    put("description", "未找到该患者或数据未变更");
                }});
            }
        } catch (BusinessException e) {
            // 业务异常
            return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                put("code", e.getCode());
                put("message", e.getMessage());
                put("description", e.getDescription());
            }});
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 唯一约束违反
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("phone")) {
                return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                    put("code", 50001);
                    put("message", "数据库错误");
                    put("description", "该手机号已被其他患者使用");
                }});
            } else if (errorMsg != null && errorMsg.contains("id_card")) {
                return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                    put("code", 50001);
                    put("message", "数据库错误");
                    put("description", "该证件号码已被其他患者使用");
                }});
            } else {
                return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                    put("code", 50001);
                    put("message", "数据库错误");
                    put("description", "数据唯一性约束冲突，请检查输入信息");
                }});
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 数据完整性约束违反
            Throwable rootCause = e.getRootCause();
            String rootCauseMsg = rootCause != null ? rootCause.getMessage() : null;
            
            if (rootCauseMsg != null) {
                if (rootCauseMsg.contains("foreign key constraint") || rootCauseMsg.contains("FOREIGN KEY")) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 50001);
                        put("message", "数据库错误");
                        put("description", "外键约束违反，请检查关联数据");
                    }});
                } else if (rootCauseMsg.contains("cannot be null") || rootCauseMsg.contains("NOT NULL")) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 50001);
                        put("message", "数据库错误");
                        put("description", "必填字段不能为空");
                    }});
                }
            }
            return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                put("code", 50001);
                put("message", "数据库错误");
                put("description", "数据完整性约束违反，" + (rootCauseMsg != null ? rootCauseMsg : e.getMessage()));
            }});
        } catch (Exception e) {
            // 其他类型的异常
            String errorMsg = e.getMessage();
            String className = e.getClass().getSimpleName();
            
            // 检查是否是数据库相关异常
            if (errorMsg != null) {
                if (errorMsg.contains("Duplicate entry") || errorMsg.contains("duplicate key")) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 50001);
                        put("message", "数据库错误");
                        put("description", "数据重复，请检查输入信息");
                    }});
                } else if (errorMsg.contains("foreign key") || errorMsg.contains("FOREIGN KEY")) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 50001);
                        put("message", "数据库错误");
                        put("description", "外键约束违反，请检查关联数据");
                    }});
                } else if (errorMsg.contains("cannot be null") || errorMsg.contains("NOT NULL")) {
                    return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                        put("code", 50001);
                        put("message", "数据库错误");
                        put("description", "必填字段不能为空");
                    }});
                }
            }
            
            // 系统异常
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("code", 50000);
                put("message", "系统内部异常");
                put("description", className + " - " + (errorMsg != null ? errorMsg : "未知错误"));
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
            // 根据userId查询，优先返回默认就诊卡，如果没有默认就诊卡则返回第一条
            List<Patient> patientList = patientService.lambdaQuery()
                    .eq(Patient::getUserId, patient.getUserId())
                    .eq(Patient::getIsDeleted, 0)
                    .orderByDesc(Patient::getIsDefault) // 默认就诊卡优先
                    .orderByAsc(Patient::getPatientId) // 然后按ID排序
                    .list();
            
            // 优先返回默认就诊卡（isDefault=1），如果没有则返回第一条
            patientInfo = patientList.stream()
                    .filter(p -> p.getIsDefault() != null && p.getIsDefault() == 1)
                    .findFirst()
                    .orElse(patientList.isEmpty() ? null : patientList.get(0));
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

        // 执行查询：只返回当前 userId 下符合条件的患者，优先返回默认就诊卡
        queryWrapper.orderByDesc(Patient::getIsDefault) // 默认就诊卡优先
                    .orderByAsc(Patient::getPatientId); // 然后按ID排序
        List<Patient> patientList = patientService.list(queryWrapper);

        // 封装结果
        HashMap<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", patientList.isEmpty() ? "未找到该用户关联的患者" : "查询成功");
        result.put("data", patientList);

        return ResponseEntity.ok(result);
    }

    /**
     * 设置默认就诊卡
     */
    @PostMapping("/setDefault")
    public ResponseEntity<HashMap<String, Object>> setDefaultPatient(@RequestBody Map<String, Object> body) {
        HashMap<String, Object> result = new HashMap<>();
        
        try {
            if (body == null) {
                result.put("code", 40000);
                result.put("message", "请求参数错误");
                result.put("description", "请求体不能为空");
                return ResponseEntity.ok(result);
            }

            Object userIdObj = body.get("userId");
            Object patientIdObj = body.get("patientId");
            
            if (userIdObj == null || patientIdObj == null) {
                result.put("code", 40000);
                result.put("message", "请求参数错误");
                result.put("description", "userId 和 patientId 不能为空");
                return ResponseEntity.ok(result);
            }

            Long userId = Long.valueOf(userIdObj.toString());
            Long patientId = Long.valueOf(patientIdObj.toString());

            // 验证就诊卡是否属于该用户
            Patient targetPatient = patientService.lambdaQuery()
                    .eq(Patient::getPatientId, patientId)
                    .eq(Patient::getUserId, userId)
                    .eq(Patient::getIsDeleted, 0)
                    .one();
            
            if (targetPatient == null) {
                result.put("code", 40400);
                result.put("message", "未找到该就诊卡");
                result.put("description", "就诊卡不存在或不属于当前用户");
                return ResponseEntity.ok(result);
            }

            // 将该用户下所有就诊卡的 isDefault 设置为 0
            List<Patient> allPatients = patientService.lambdaQuery()
                    .eq(Patient::getUserId, userId)
                    .eq(Patient::getIsDeleted, 0)
                    .list();
            
            for (Patient p : allPatients) {
                if (p.getIsDefault() != null && p.getIsDefault() == 1) {
                    p.setIsDefault(0);
                    patientService.updateById(p);
                }
            }

            // 设置目标就诊卡为默认
            targetPatient.setIsDefault(1);
            boolean updated = patientService.updateById(targetPatient);
            
            if (updated) {
                result.put("code", 20000);
                result.put("message", "设置成功");
                result.put("description", "已设置为默认就诊卡");
            } else {
                result.put("code", 50001);
                result.put("message", "数据库错误");
                result.put("description", "设置默认就诊卡失败，请稍后重试");
            }
            
            return ResponseEntity.ok(result);
        } catch (NumberFormatException e) {
            result.put("code", 40000);
            result.put("message", "请求参数错误");
            result.put("description", "userId 或 patientId 格式不正确，必须为数字");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 50000);
            result.put("message", "系统内部异常");
            result.put("description", "设置默认就诊卡失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
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
            // 1. 基础参数校验
            if (request == null || request.getPatientId() == null) {
                result.put("code", 400);
                result.put("message", "patientId 不能为空");
                return ResponseEntity.ok(result);
            }

            // 2. 检查患者是否存在
            Patient dbPatient = patientService.getById(request.getPatientId());
            if (dbPatient == null) {
                result.put("code", 404);
                result.put("message", "未找到对应就诊卡");
                return ResponseEntity.ok(result);
            }
            
            // 3. 校验身份证号格式
            String idCard = dbPatient.getIdCard();
            if (idCard == null || !IdcardUtil.isValidCard(idCard)) {
                result.put("code", 400);
                result.put("message", "身份证号格式不正确，请先更新个人信息");
                return ResponseEntity.ok(result);
            }
            
            // 4. 校验姓名格式
            String patientName = dbPatient.getPatientName();
            if (patientName == null || patientName.trim().isEmpty() || 
                    !patientName.matches("^[\\u4e00-\\u9fa5a-zA-Z\\s]+$")) {
                result.put("code", 400);
                result.put("message", "姓名格式不正确，请先更新个人信息");
                return ResponseEntity.ok(result);
            }

            // 5. 校验学生/教职工信息
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

            // 6. 校验图片是否上传
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
            
            // 7. 校验图片格式
            String[] allowedFormats = {"jpg", "jpeg", "png"};
            boolean validIdentityFormat = false;
            boolean validHandheldFormat = false;
            
            String identityPhoto = request.getIdentityPhoto().toLowerCase();
            String handheldPhoto = request.getHandheldIdentityPhoto().toLowerCase();
            
            for (String format : allowedFormats) {
                if (identityPhoto.endsWith("." + format)) {
                    validIdentityFormat = true;
                }
                if (handheldPhoto.endsWith("." + format)) {
                    validHandheldFormat = true;
                }
            }
            
            if (!validIdentityFormat) {
                result.put("code", 400);
                result.put("message", "证件照片格式不正确，请上传JPG或PNG格式的图片");
                return ResponseEntity.ok(result);
            }
            
            if (!validHandheldFormat) {
                result.put("code", 400);
                result.put("message", "手持证件照片格式不正确，请上传JPG或PNG格式的图片");
                return ResponseEntity.ok(result);
            }

            // 8. 提交认证申请
            try {
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
                
                // 9. 返回成功响应
                result.put("code", 200);
                result.put("message", "身份认证申请已提交，请等待管理员审核");
                result.put("data", new HashMap<String, Object>() {{
                    put("patientId", dbPatient.getPatientId());
                    put("status", 0); // 待审核
                    put("submitTime", LocalDateTime.now());
                }});
                return ResponseEntity.ok(result);
            } catch (BusinessException e) {
                // 业务异常
                result.put("code", e.getCode());
                result.put("message", e.getMessage());
                result.put("description", e.getDescription());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (BusinessException e) {
            // 业务异常
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<String, Object>() {{
                put("code", e.getCode());
                put("message", e.getMessage());
                put("description", e.getDescription());
            }});
        } catch (Exception e) {
            // 系统异常
            e.printStackTrace(); // 开发环境输出异常堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("code", 500);
                put("message", "系统异常，请稍后重试");
            }});
        }
    }

    /**
     * 获取患者身份认证驳回原因
     */
    @GetMapping("/identity/rejectReason/{patientId}")
    public ResponseEntity<HashMap<String, Object>> getIdentityRejectReason(@PathVariable Long patientId) {
        HashMap<String, Object> result = new HashMap<>();

        try {
            // 1. 检查患者是否存在
            Patient dbPatient = patientService.getById(patientId);
            if (dbPatient == null) {
                result.put("code", 404);
                result.put("message", "未找到对应就诊卡");
                return ResponseEntity.ok(result);
            }
            
            // 2. 获取认证记录
            LambdaQueryWrapper<PatientIdentityVerify> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PatientIdentityVerify::getPatientId, patientId);
            PatientIdentityVerify record = patientIdentityVerifyService.getOne(wrapper);
            
            // 3. 返回驳回原因
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", new HashMap<String, Object>() {{
                put("patientId", patientId);
                put("rejectReason", record != null ? record.getRejectReason() : null);
            }});
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("code", 500);
                put("message", "系统异常，请稍后重试");
            }});
        }
    }
    
    /**
     * 获取患者身份认证审核历史
     */
    @GetMapping("/identity/auditHistory/{patientId}")
    public ResponseEntity<HashMap<String, Object>> getIdentityAuditHistory(@PathVariable Long patientId) {
        HashMap<String, Object> result = new HashMap<>();

        try {
            // 1. 检查患者是否存在
            Patient dbPatient = patientService.getById(patientId);
            if (dbPatient == null) {
                result.put("code", 404);
                result.put("message", "未找到对应就诊卡");
                return ResponseEntity.ok(result);
            }
            
            // 2. 获取审核历史记录
            List<VerificationAuditLog> auditLogs = verificationAuditLogService.listByPatientId(patientId);
            
            // 3. 返回审核历史
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", new HashMap<String, Object>() {{
                put("patientId", patientId);
                put("auditLogs", auditLogs);
                put("total", auditLogs.size());
            }});
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
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
    /**
     * 管理员审核身份认证
     */
    @PostMapping("/identity/approve")
    public ResponseEntity<HashMap<String, Object>> approveIdentity(@RequestBody HashMap<String, Object> body) {
        HashMap<String, Object> result = new HashMap<>();

        try {
            // 1. 参数校验
            Object patientIdObj = body.get("patientId");
            Object approveObj = body.get("approve");
            Object rejectReasonObj = body.get("rejectReason");
            Object operatorIdObj = body.get("operatorId");
            Object operatorNameObj = body.get("operatorName");
            
            if (patientIdObj == null || approveObj == null) {
                result.put("code", 400);
                result.put("message", "patientId 和 approve 参数不能为空");
                return ResponseEntity.ok(result);
            }

            Long patientId = Long.valueOf(patientIdObj.toString());
            boolean approve = Boolean.parseBoolean(approveObj.toString());
            String rejectReason = rejectReasonObj == null ? null : rejectReasonObj.toString();
            
            // 如果驳回，必须提供驳回原因
            if (!approve && (rejectReason == null || rejectReason.trim().isEmpty())) {
                result.put("code", 400);
                result.put("message", "驳回时必须提供驳回原因");
                return ResponseEntity.ok(result);
            }
            
            // 获取操作人信息，如果没有提供，使用默认值
            Long operatorId = operatorIdObj == null ? 0L : Long.valueOf(operatorIdObj.toString());
            String operatorName = operatorNameObj == null ? "admin" : operatorNameObj.toString();

            // 2. 检查患者是否存在
            Patient dbPatient = patientService.getById(patientId);
            if (dbPatient == null) {
                result.put("code", 404);
                result.put("message", "未找到对应就诊卡");
                return ResponseEntity.ok(result);
            }
            
            // 3. 检查当前认证状态，避免重复审核
            if (dbPatient.getIdentityVerify() != null) {
                if (approve && dbPatient.getIdentityVerify() == 1) {
                    result.put("code", 400);
                    result.put("message", "该身份认证已经通过，无需重复审核");
                    return ResponseEntity.ok(result);
                }
            }

            // 4. 执行审核操作
            boolean success;
            if (approve) {
                success = patientIdentityVerifyService.approveIdentity(patientId, operatorId, operatorName);
                if (success) {
                    result.put("code", 200);
                    result.put("message", "已通过该身份认证申请");
                    result.put("data", new HashMap<String, Object>() {{
                        put("patientId", patientId);
                        put("status", 1); // 已通过
                        put("verifyTime", LocalDateTime.now());
                    }});
                } else {
                    result.put("code", 500);
                    result.put("message", "审核失败，请稍后重试");
                }
            } else {
                success = patientIdentityVerifyService.rejectIdentity(patientId, rejectReason, operatorId, operatorName);
                if (success) {
                    result.put("code", 200);
                    result.put("message", "已驳回该身份认证申请");
                    result.put("data", new HashMap<String, Object>() {{
                        put("patientId", patientId);
                        put("status", 2); // 已驳回
                        put("rejectReason", rejectReason);
                    }});
                } else {
                    result.put("code", 500);
                    result.put("message", "驳回失败，请稍后重试");
                }
            }

            return ResponseEntity.ok(result);
        } catch (BusinessException e) {
            // 业务异常
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<String, Object>() {{
                put("code", e.getCode());
                put("message", e.getMessage());
                put("description", e.getDescription());
            }});
        } catch (Exception e) {
            // 系统异常
            e.printStackTrace(); // 开发环境输出异常堆栈
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

        try {
            // 1. 参数校验
            if (body == null) {
                return ResponseEntity.ok(new HashMap<String, Object>() {{
                    put("code", 40000);
                    put("message", "请求参数错误");
                    put("description", "请求体不能为空");
                }});
            }

            Object patientIdObj = body.get("patientId");
            if (patientIdObj == null) {
                return ResponseEntity.ok(new HashMap<String, Object>() {{
                    put("code", 40000);
                    put("message", "请求参数错误");
                    put("description", "患者ID不能为空");
                }});
            }

            Long patientId;
            try {
                patientId = Long.valueOf(patientIdObj.toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.ok(new HashMap<String, Object>() {{
                    put("code", 40000);
                    put("message", "请求参数错误");
                    put("description", "患者ID格式不正确，必须为数字");
                }});
            }

            // 2. 检查患者是否存在
            Patient patient = patientService.getById(patientId);
            if (patient == null) {
                return ResponseEntity.ok(new HashMap<String, Object>() {{
                    put("code", 40400);
                    put("message", "未找到该患者");
                    put("description", "患者ID " + patientId + " 不存在");
                }});
            }

            // 3. 验证并设置身高
            if (body.get("height") != null && !body.get("height").toString().trim().isEmpty()) {
                String heightStr = body.get("height").toString().trim();
                try {
                    BigDecimal height = new BigDecimal(heightStr);
                    // 身高范围验证：50-250cm
                    if (height.compareTo(new BigDecimal("50")) < 0 || height.compareTo(new BigDecimal("250")) > 0) {
                        return ResponseEntity.ok(new HashMap<String, Object>() {{
                            put("code", 40000);
                            put("message", "请求参数错误");
                            put("description", "身高必须在50-250cm之间");
                        }});
                    }
                    patient.setHeight(height);
                } catch (NumberFormatException e) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "身高格式不正确，请输入有效的数字（如：175.5）");
                    }});
                }
            }

            // 4. 验证并设置体重
            if (body.get("weight") != null && !body.get("weight").toString().trim().isEmpty()) {
                String weightStr = body.get("weight").toString().trim();
                try {
                    BigDecimal weight = new BigDecimal(weightStr);
                    // 体重范围验证：5-500kg
                    if (weight.compareTo(new BigDecimal("5")) < 0 || weight.compareTo(new BigDecimal("500")) > 0) {
                        return ResponseEntity.ok(new HashMap<String, Object>() {{
                            put("code", 40000);
                            put("message", "请求参数错误");
                            put("description", "体重必须在5-500kg之间");
                        }});
                    }
                    patient.setWeight(weight);
                } catch (NumberFormatException e) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "体重格式不正确，请输入有效的数字（如：70.5）");
                    }});
                }
            }

            // 5. 设置其他字段（字符串类型，进行长度限制）
            if (body.get("bloodType") != null) {
                String bloodType = body.get("bloodType").toString().trim();
                if (bloodType.length() > 10) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "血型长度不能超过10个字符");
                    }});
                }
                patient.setBloodType(bloodType);
            }
            if (body.get("maritalStatus") != null) {
                String maritalStatus = body.get("maritalStatus").toString().trim();
                if (maritalStatus.length() > 20) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "婚姻状况长度不能超过20个字符");
                    }});
                }
                patient.setMaritalStatus(maritalStatus);
            }
            if (body.get("fertilityStatus") != null) {
                String fertilityStatus = body.get("fertilityStatus").toString().trim();
                if (fertilityStatus.length() > 20) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "生育情况长度不能超过20个字符");
                    }});
                }
                patient.setFertilityStatus(fertilityStatus);
            }
            if (body.get("currentIllness") != null) {
                String currentIllness = body.get("currentIllness").toString().trim();
                if (currentIllness.length() > 500) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "现病史长度不能超过500个字符");
                    }});
                }
                patient.setPresentIllness(currentIllness);
            }
            if (body.get("pastHistory") != null) {
                String pastHistory = body.get("pastHistory").toString().trim();
                if (pastHistory.length() > 500) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "既往史长度不能超过500个字符");
                    }});
                }
                patient.setPastIllness(pastHistory);
            }
            if (body.get("familyHistory") != null) {
                String familyHistory = body.get("familyHistory").toString().trim();
                if (familyHistory.length() > 500) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "家族史长度不能超过500个字符");
                    }});
                }
                patient.setFamilyIllness(familyHistory);
            }
            if (body.get("allergyHistory") != null) {
                String allergyHistory = body.get("allergyHistory").toString().trim();
                if (allergyHistory.length() > 500) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 40000);
                        put("message", "请求参数错误");
                        put("description", "过敏史长度不能超过500个字符");
                    }});
                }
                patient.setAllergyHistory(allergyHistory);
            }

            // 6. 执行更新
            boolean updated = patientService.updateById(patient);
            if (!updated) {
                return ResponseEntity.ok(new HashMap<String, Object>() {{
                    put("code", 50001);
                    put("message", "数据库错误");
                    put("description", "健康档案保存失败，请稍后重试");
                }});
            }

            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("code", 20000);
                put("message", "保存成功");
            }});
        } catch (BusinessException e) {
            // 业务异常
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("code", e.getCode());
                put("message", e.getMessage());
                put("description", e.getDescription());
            }});
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 唯一约束违反
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("code", 50001);
                put("message", "数据库错误");
                put("description", "健康档案保存失败：数据唯一性约束冲突");
            }});
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 数据完整性约束违反
            Throwable rootCause = e.getRootCause();
            String rootCauseMsg = rootCause != null ? rootCause.getMessage() : null;
            
            if (rootCauseMsg != null) {
                if (rootCauseMsg.contains("cannot be null") || rootCauseMsg.contains("NOT NULL")) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 50001);
                        put("message", "数据库错误");
                        put("description", "健康档案保存失败：必填字段不能为空");
                    }});
                }
            }
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("code", 50001);
                put("message", "数据库错误");
                put("description", "健康档案保存失败：数据完整性约束违反，" + (rootCauseMsg != null ? rootCauseMsg : e.getMessage()));
            }});
        } catch (NumberFormatException e) {
            // 数字格式异常
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("code", 40000);
                put("message", "请求参数错误");
                put("description", "数字格式不正确，请检查身高、体重等数值字段");
            }});
        } catch (Exception e) {
            // 其他异常
            String errorMsg = e.getMessage();
            String className = e.getClass().getSimpleName();
            
            // 检查是否是数据库相关异常
            if (errorMsg != null) {
                if (errorMsg.contains("Duplicate entry") || errorMsg.contains("duplicate key")) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 50001);
                        put("message", "数据库错误");
                        put("description", "健康档案保存失败：数据重复");
                    }});
                } else if (errorMsg.contains("foreign key") || errorMsg.contains("FOREIGN KEY")) {
                    return ResponseEntity.ok(new HashMap<String, Object>() {{
                        put("code", 50001);
                        put("message", "数据库错误");
                        put("description", "健康档案保存失败：外键约束违反，请检查关联数据");
                    }});
                }
            }
            
            // 系统异常
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("code", 50000);
                put("message", "系统内部异常");
                put("description", className + " - " + (errorMsg != null ? errorMsg : "未知错误"));
            }});
        }
    }
}
