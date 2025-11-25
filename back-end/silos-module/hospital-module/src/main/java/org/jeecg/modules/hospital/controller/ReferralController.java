package org.jeecg.modules.hospital.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.ReferralApplication;
import org.jeecg.modules.hospital.service.ReferralService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 转诊功能控制器
 */
@Tag(name="转诊管理")
@RestController
@RequestMapping("/api/referral")
public class ReferralController {

    @Autowired
    private ReferralService referralService;

    /**
     * 获取患者已就诊的挂号记录（用于选择转诊）
     * @param patientId 患者ID
     */
    @Operation(summary = "获取患者已就诊挂号记录")
    @GetMapping("/patient-records")
    public Result<List<Map<String, Object>>> getPatientVisitedRecords(@RequestParam Long patientId) {
        return referralService.getPatientVisitedRecords(patientId);
    }

    /**
     * 患者申请转诊
     * @param application 转诊申请信息
     */
    @Operation(summary = "患者申请转诊")
    @PostMapping("/patient/apply")
    public Result<String> applyReferral(@RequestBody ReferralApplication application) {
        return referralService.applyReferralByPatient(application);
    }

    /**
     * 医生直接生成转诊意见
     * @param application 转诊申请信息
     */
    @Operation(summary = "医生生成转诊意见")
    @PostMapping("/doctor/create")
    public Result<String> createReferralByDoctor(@RequestBody ReferralApplication application) {
        return referralService.createReferralByDoctor(application);
    }

    /**
     * 获取转诊申请列表
     * @param params 查询参数
     */
    @Operation(summary = "获取转诊申请列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> getReferralList(@RequestParam Map<String, Object> params) {
        return referralService.getReferralList(params);
    }

    /**
     * 获取转诊申请详情
     * @param id 转诊申请ID
     */
    @Operation(summary = "获取转诊申请详情")
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getReferralDetail(@PathVariable Long id) {
        return referralService.getReferralDetail(id);
    }

    /**
     * 处理院内转诊自动挂号
     * @param referralId 转诊申请ID
     */
    @Operation(summary = "处理院内转诊自动挂号")
    @PostMapping("/auto-register/{referralId}")
    public Result<String> processAutoRegister(@PathVariable Long referralId) {
        return referralService.processAutoRegister(referralId);
    }

    /**
     * 更新转诊状态
     * @param id 转诊申请ID
     * @param status 新状态
     * @param comments 审核意见
     */
    @Operation(summary = "更新转诊状态")
    @PutMapping("/status/{id}")
    public Result<String> updateReferralStatus(@PathVariable Long id,
                                             @RequestParam String status,
                                             @RequestParam(required = false) String comments) {
        return referralService.updateReferralStatus(id, status, comments);
    }

    /**
     * 取消转诊申请
     * @param id 转诊申请ID
     * @param reason 取消原因
     */
    @Operation(summary = "取消转诊申请")
    @PostMapping("/cancel/{id}")
    public Result<String> cancelReferral(@PathVariable Long id, @RequestParam String reason) {
        return referralService.cancelReferral(id, reason);
    }

    /**
     * 获取目标科室列表（用于转诊选择）
     */
    @Operation(summary = "获取可转诊科室列表")
    @GetMapping("/target-departments")
    public Result<List<Map<String, Object>>> getTargetDepartments() {
        return referralService.getTargetDepartments();
    }
}