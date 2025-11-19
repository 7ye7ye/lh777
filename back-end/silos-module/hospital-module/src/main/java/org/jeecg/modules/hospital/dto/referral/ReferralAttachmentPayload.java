package org.jeecg.modules.hospital.dto.referral;

import lombok.Data;

@Data
public class ReferralAttachmentPayload {
    private String name;
    private String url;
    private String type;
}

