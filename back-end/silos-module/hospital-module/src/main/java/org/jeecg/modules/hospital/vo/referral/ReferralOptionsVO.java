package org.jeecg.modules.hospital.vo.referral;

import lombok.Data;

import java.util.List;

@Data
public class ReferralOptionsVO {
    private List<DeptOption> internalDepartments;
    private List<HospitalOption> externalHospitals;

    @Data
    public static class DeptOption {
        private Long deptId;
        private String deptName;
        private String parentDeptName;
    }

    @Data
    public static class HospitalOption {
        private String code;
        private String name;
        private String level;
        private String address;
    }
}

