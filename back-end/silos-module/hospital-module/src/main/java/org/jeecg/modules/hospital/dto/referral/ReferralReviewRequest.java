package org.jeecg.modules.hospital.dto.referral;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ReferralReviewRequest {

    @NotNull
    private Long id;

    /** APPROVE / REJECT */
    @NotBlank
    private String decision;

    private String reviewDoctor;

    private String reviewComments;

    private String rejectReason;
}


