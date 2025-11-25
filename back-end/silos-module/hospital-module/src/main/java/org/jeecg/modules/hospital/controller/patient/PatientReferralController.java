package org.jeecg.modules.hospital.controller.patient;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.dto.referral.*;
import org.jeecg.modules.hospital.entity.ReferralApplication;
import org.jeecg.modules.hospital.service.IReferralApplicationService;
import org.jeecg.modules.hospital.vo.referral.ReferralOptionsVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patient/referral")
@Tag(name = "患者端-转诊管理")
@Validated
public class PatientReferralController {

    @Resource
    private IReferralApplicationService referralService;

    /**
     * 提交转诊申请
     */
    @Operation(summary = "提交转诊申请")
    @PostMapping("/apply")
    public Result<ReferralApplication> apply(@Valid @RequestBody ReferralApplyRequest request) {
        ReferralApplication application = referralService.createReferral(request);
        return Result.OK(application);
    }

    /**
     * 获取转诊记录列表
     */
    @Operation(summary = "获取转诊记录列表")
    @GetMapping("/list")
    public Result<Page<ReferralApplication>> list(ReferralListQuery query) {
        Page<ReferralApplication> page = referralService.queryPage(query);
        return Result.OK(page);
    }

    /**
     * 获取转诊详情
     */
    @Operation(summary = "获取转诊详情")
    @GetMapping("/{id}")
    public Result<ReferralApplication> detail(@PathVariable Long id) {
        ReferralApplication application = referralService.getDetail(id);
        return Result.OK(application);
    }

    /**
     * 取消转诊申请
     */
    @Operation(summary = "取消转诊申请")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody ReferralCancelRequest request) {
        referralService.cancelReferral(request);
        return Result.OK();
    }

    /**
     * 获取转诊选项（内部科室和外部医院）
     */
    @Operation(summary = "获取转诊选项")
    @GetMapping("/options")
    public Result<ReferralOptionsVO> options() {
        ReferralOptionsVO options = referralService.loadOptions();
        return Result.OK(options);
    }
}