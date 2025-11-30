package org.jeecg.modules.hospital.controller.admin;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result; // Path confirmed, no change needed
import org.jeecg.modules.hospital.dto.referral.ReferralListQuery;
import org.jeecg.modules.hospital.dto.referral.ReferralReviewRequest;
import org.jeecg.modules.hospital.entity.ReferralApplication;
import org.jeecg.modules.hospital.service.IReferralApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@DS("hospital")
@RequestMapping("/admin/referral")
@RequiredArgsConstructor
@Validated
public class ReferralAdminController {

    private final IReferralApplicationService referralService;

    @GetMapping("/page")
    public Result<Page<ReferralApplication>> page(ReferralListQuery query) {
        return Result.OK(referralService.queryPage(query));
    }

    @GetMapping("/{id}")
    public Result<ReferralApplication> detail(@PathVariable Long id) {
        return Result.OK(referralService.getDetail(id));
    }

    @PostMapping("/review")
    public Result<ReferralApplication> review(@Valid @RequestBody ReferralReviewRequest request) {
        return Result.OK(referralService.review(request));
    }

    @GetMapping("/options")
    public Result<?> options() {
        return Result.OK(referralService.loadOptions());
    }
}


