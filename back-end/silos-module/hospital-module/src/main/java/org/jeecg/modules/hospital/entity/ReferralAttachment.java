package org.jeecg.modules.hospital.entity;

import lombok.Data;

/**
 * 转诊上传资料对象
 */
@Data
public class ReferralAttachment {
    private String name;
    private String url;
    private String type;
}

