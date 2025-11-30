package org.jeecg.modules.hospital.controller.response;

public class PatientIdentityAdminItem {
    private Long patientId;
    private String patientName;
    private String phone;
    private Integer patientType;
    private String studentId;
    private String staffId;
    private Integer identityVerify;
    private String identityPhoto;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getPatientType() {
        return patientType;
    }

    public void setPatientType(Integer patientType) {
        this.patientType = patientType;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public Integer getIdentityVerify() {
        return identityVerify;
    }

    public void setIdentityVerify(Integer identityVerify) {
        this.identityVerify = identityVerify;
    }

    public String getIdentityPhoto() {
        return identityPhoto;
    }

    public void setIdentityPhoto(String identityPhoto) {
        this.identityPhoto = identityPhoto;
    }
}
