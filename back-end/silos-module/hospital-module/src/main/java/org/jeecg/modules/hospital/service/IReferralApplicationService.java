package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.hospital.dto.referral.ReferralApplyRequest;
import org.jeecg.modules.hospital.dto.referral.ReferralCancelRequest;
import org.jeecg.modules.hospital.dto.referral.ReferralListQuery;
import org.jeecg.modules.hospital.dto.referral.ReferralReviewRequest;
import org.jeecg.modules.hospital.entity.ReferralApplication;
import org.jeecg.modules.hospital.vo.referral.ReferralOptionsVO;

public interface IReferralApplicationService extends IService<ReferralApplication> {

    ReferralApplication createReferral(ReferralApplyRequest request);

    Page<ReferralApplication> queryPage(ReferralListQuery query);

    ReferralApplication getDetail(Long id);

    ReferralApplication review(ReferralReviewRequest request);

    void cancelReferral(ReferralCancelRequest request);

    ReferralOptionsVO loadOptions();
}


