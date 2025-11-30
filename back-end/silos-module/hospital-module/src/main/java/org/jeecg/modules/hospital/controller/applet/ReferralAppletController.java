package org.jeecg.modules.hospital.controller.applet;

import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.dto.referral.ReferralApplyRequest;
import org.jeecg.modules.hospital.dto.referral.ReferralCancelRequest;
import org.jeecg.modules.hospital.dto.referral.ReferralListQuery;
import org.jeecg.modules.hospital.entity.ReferralApplication;
import org.jeecg.modules.hospital.service.IReferralApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/applet/referral")
@RequiredArgsConstructor
@Validated
public class ReferralAppletController {

    private final IReferralApplicationService referralService;

    @PostMapping("/apply")
    public Result<ReferralApplication> apply(@Valid @RequestBody ReferralApplyRequest request) {
        return Result.OK(referralService.createReferral(request));
    }

    @GetMapping("/records")
    public Result<?> records(ReferralListQuery query) {
        if (query.getPhone() == null || query.getPhone().isEmpty()) {
            return Result.error("缺少联系电话");
        }
        return Result.OK(referralService.queryPage(query));
    }

    @GetMapping("/{id}")
    public Result<ReferralApplication> detail(@PathVariable Long id) {
        return Result.OK(referralService.getDetail(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id, @RequestBody(required = false) ReferralCancelRequest req) {
        ReferralCancelRequest request = req == null ? new ReferralCancelRequest() : req;
        request.setId(id);
        referralService.cancelReferral(request);
        return Result.OK("取消成功");
    }

    @GetMapping("/options")
    public Result<?> options() {
        return Result.OK(referralService.loadOptions());
    }

    /**
     * 院内转诊自动挂号（申请加号）
     */
    @PostMapping("/autoRegister/{referralId}")
    public Result<String> autoRegister(@PathVariable Long referralId) {
        try {
            String result = referralService.processAutoRegister(referralId);
            return Result.OK(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("申请加号失败：" + e.getMessage());
        }
    }
}


