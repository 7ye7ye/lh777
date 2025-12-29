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

    /**
     * 管理员端查询转诊列表（不按当前登录用户过滤）
     */
    Page<ReferralApplication> adminQueryPage(ReferralListQuery query);

    ReferralApplication getDetail(Long id);

    ReferralApplication review(ReferralReviewRequest request);

    void cancelReferral(ReferralCancelRequest request);

    ReferralOptionsVO loadOptions();

    /**
     * 院内转诊自动挂号（申请加号）
     * @param referralId 转诊申请ID
     * @return 操作结果
     */
    String processAutoRegister(Long referralId);
}


