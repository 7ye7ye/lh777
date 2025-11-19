package org.jeecg.modules.hospital.dto.referral;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class ReferralCancelRequest {

    @NotNull
    private Long id;

    private String reason;
}

