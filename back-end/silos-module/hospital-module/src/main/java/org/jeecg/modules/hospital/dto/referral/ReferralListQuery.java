package org.jeecg.modules.hospital.dto.referral;

import lombok.Data;

@Data
public class ReferralListQuery {
    private String status;
    private String phone;
    private String patientName;
    private Long deptId;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}


