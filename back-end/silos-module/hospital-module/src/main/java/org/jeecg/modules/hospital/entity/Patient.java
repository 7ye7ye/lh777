package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 患者表
 * @TableName patient
 */
@TableName(value ="patient")
@Data
public class Patient {
    /**
     * 患者唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long patientId;

    /**
     * 关联用户表
     */
    private Long userId;

    /**
     * 患者身份（1-学生；2-教师；3-职工）
     */
    private Integer patientType;

    /**
     * 学号（学生用户必填）
     */
    private String studentId;

    /**
     * 工号（教师/职工用户必填）
     */
    private String staffId;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 性别（男/女/未知）
     */
    private String gender;

    /**
     * 身高（单位：cm）
     */
    private BigDecimal height;

    /**
     * 体重（单位：kg）
     */
    private BigDecimal weight;

    /**
     * 血型
     */
    private String bloodType;

    /**
     * 婚姻状况
     */
    private String maritalStatus;

    /**
     * 生育情况
     */
    private String fertilityStatus;

    /**
     * 现病史
     */
    private String presentIllness;

    /**
     * 既往史
     */
    private String pastIllness;

    /**
     * 家族史
     */
    private String familyIllness;

    /**
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 证件类型
     */
    private String idType;

    /**
     * 证件号码
     */
    private String idNumber;

    /**
     * 民族
     */
    private String nation;

    /**
     * 国籍
     */
    private String nationality;

    /**
     * 所在地区
     */
    private String region;

    /**
     * 详细住址
     */
    private String detailedAddress;

    /**
     * 电话号码
     */
    private String phoneNumber;

    /**
     * 家庭地址
     */
    private String homeAddress;

    /**
     * 紧急联系人姓名
     */
    private String emergencyContact;

    /**
     * 紧急联系人电话
     */
    private String emergencyPhone;

    /**
     * 既往病史（可修改）
     */
    private String medicalHistory;

    /**
     * 身份认证状态（0-未审核；1-已通过；2-未通过）
     */
    private Integer identityVerify;

    /**
     * 审核通过时间
     */
    private LocalDateTime verifyTime;

    /**
     * 门诊号
     */
    private String outpatientNumber;

    /**
     * 住院号
     */
    private String hospitalizationNumber;

    /**
     * 条形码信息
     */
    private String barcodeInfo;

    /**
     * 二维码信息
     */
    private String qrCodeInfo;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Patient other = (Patient) that;
        return (this.getPatientId() == null ? other.getPatientId() == null : this.getPatientId().equals(other.getPatientId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getPatientType() == null ? other.getPatientType() == null : this.getPatientType().equals(other.getPatientType()))
            && (this.getStudentId() == null ? other.getStudentId() == null : this.getStudentId().equals(other.getStudentId()))
            && (this.getStaffId() == null ? other.getStaffId() == null : this.getStaffId().equals(other.getStaffId()))
            && (this.getBirthDate() == null ? other.getBirthDate() == null : this.getBirthDate().equals(other.getBirthDate()))
            && (this.getGender() == null ? other.getGender() == null : this.getGender().equals(other.getGender()))
            && (this.getHeight() == null ? other.getHeight() == null : this.getHeight().equals(other.getHeight()))
            && (this.getWeight() == null ? other.getWeight() == null : this.getWeight().equals(other.getWeight()))
            && (this.getBloodType() == null ? other.getBloodType() == null : this.getBloodType().equals(other.getBloodType()))
            && (this.getMaritalStatus() == null ? other.getMaritalStatus() == null : this.getMaritalStatus().equals(other.getMaritalStatus()))
            && (this.getFertilityStatus() == null ? other.getFertilityStatus() == null : this.getFertilityStatus().equals(other.getFertilityStatus()))
            && (this.getPresentIllness() == null ? other.getPresentIllness() == null : this.getPresentIllness().equals(other.getPresentIllness()))
            && (this.getPastIllness() == null ? other.getPastIllness() == null : this.getPastIllness().equals(other.getPastIllness()))
            && (this.getFamilyIllness() == null ? other.getFamilyIllness() == null : this.getFamilyIllness().equals(other.getFamilyIllness()))
            && (this.getAllergyHistory() == null ? other.getAllergyHistory() == null : this.getAllergyHistory().equals(other.getAllergyHistory()))
            && (this.getIdType() == null ? other.getIdType() == null : this.getIdType().equals(other.getIdType()))
            && (this.getIdNumber() == null ? other.getIdNumber() == null : this.getIdNumber().equals(other.getIdNumber()))
            && (this.getNation() == null ? other.getNation() == null : this.getNation().equals(other.getNation()))
            && (this.getNationality() == null ? other.getNationality() == null : this.getNationality().equals(other.getNationality()))
            && (this.getRegion() == null ? other.getRegion() == null : this.getRegion().equals(other.getRegion()))
            && (this.getDetailedAddress() == null ? other.getDetailedAddress() == null : this.getDetailedAddress().equals(other.getDetailedAddress()))
            && (this.getPhoneNumber() == null ? other.getPhoneNumber() == null : this.getPhoneNumber().equals(other.getPhoneNumber()))
            && (this.getHomeAddress() == null ? other.getHomeAddress() == null : this.getHomeAddress().equals(other.getHomeAddress()))
            && (this.getEmergencyContact() == null ? other.getEmergencyContact() == null : this.getEmergencyContact().equals(other.getEmergencyContact()))
            && (this.getEmergencyPhone() == null ? other.getEmergencyPhone() == null : this.getEmergencyPhone().equals(other.getEmergencyPhone()))
            && (this.getMedicalHistory() == null ? other.getMedicalHistory() == null : this.getMedicalHistory().equals(other.getMedicalHistory()))
            && (this.getIdentityVerify() == null ? other.getIdentityVerify() == null : this.getIdentityVerify().equals(other.getIdentityVerify()))
            && (this.getVerifyTime() == null ? other.getVerifyTime() == null : this.getVerifyTime().equals(other.getVerifyTime()))
            && (this.getOutpatientNumber() == null ? other.getOutpatientNumber() == null : this.getOutpatientNumber().equals(other.getOutpatientNumber()))
            && (this.getHospitalizationNumber() == null ? other.getHospitalizationNumber() == null : this.getHospitalizationNumber().equals(other.getHospitalizationNumber()))
            && (this.getBarcodeInfo() == null ? other.getBarcodeInfo() == null : this.getBarcodeInfo().equals(other.getBarcodeInfo()))
            && (this.getQrCodeInfo() == null ? other.getQrCodeInfo() == null : this.getQrCodeInfo().equals(other.getQrCodeInfo()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPatientId() == null) ? 0 : getPatientId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getPatientType() == null) ? 0 : getPatientType().hashCode());
        result = prime * result + ((getStudentId() == null) ? 0 : getStudentId().hashCode());
        result = prime * result + ((getStaffId() == null) ? 0 : getStaffId().hashCode());
        result = prime * result + ((getBirthDate() == null) ? 0 : getBirthDate().hashCode());
        result = prime * result + ((getGender() == null) ? 0 : getGender().hashCode());
        result = prime * result + ((getHeight() == null) ? 0 : getHeight().hashCode());
        result = prime * result + ((getWeight() == null) ? 0 : getWeight().hashCode());
        result = prime * result + ((getBloodType() == null) ? 0 : getBloodType().hashCode());
        result = prime * result + ((getMaritalStatus() == null) ? 0 : getMaritalStatus().hashCode());
        result = prime * result + ((getFertilityStatus() == null) ? 0 : getFertilityStatus().hashCode());
        result = prime * result + ((getPresentIllness() == null) ? 0 : getPresentIllness().hashCode());
        result = prime * result + ((getPastIllness() == null) ? 0 : getPastIllness().hashCode());
        result = prime * result + ((getFamilyIllness() == null) ? 0 : getFamilyIllness().hashCode());
        result = prime * result + ((getAllergyHistory() == null) ? 0 : getAllergyHistory().hashCode());
        result = prime * result + ((getIdType() == null) ? 0 : getIdType().hashCode());
        result = prime * result + ((getIdNumber() == null) ? 0 : getIdNumber().hashCode());
        result = prime * result + ((getNation() == null) ? 0 : getNation().hashCode());
        result = prime * result + ((getNationality() == null) ? 0 : getNationality().hashCode());
        result = prime * result + ((getRegion() == null) ? 0 : getRegion().hashCode());
        result = prime * result + ((getDetailedAddress() == null) ? 0 : getDetailedAddress().hashCode());
        result = prime * result + ((getPhoneNumber() == null) ? 0 : getPhoneNumber().hashCode());
        result = prime * result + ((getHomeAddress() == null) ? 0 : getHomeAddress().hashCode());
        result = prime * result + ((getEmergencyContact() == null) ? 0 : getEmergencyContact().hashCode());
        result = prime * result + ((getEmergencyPhone() == null) ? 0 : getEmergencyPhone().hashCode());
        result = prime * result + ((getMedicalHistory() == null) ? 0 : getMedicalHistory().hashCode());
        result = prime * result + ((getIdentityVerify() == null) ? 0 : getIdentityVerify().hashCode());
        result = prime * result + ((getVerifyTime() == null) ? 0 : getVerifyTime().hashCode());
        result = prime * result + ((getOutpatientNumber() == null) ? 0 : getOutpatientNumber().hashCode());
        result = prime * result + ((getHospitalizationNumber() == null) ? 0 : getHospitalizationNumber().hashCode());
        result = prime * result + ((getBarcodeInfo() == null) ? 0 : getBarcodeInfo().hashCode());
        result = prime * result + ((getQrCodeInfo() == null) ? 0 : getQrCodeInfo().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", patientId=").append(patientId);
        sb.append(", userId=").append(userId);
        sb.append(", patientType=").append(patientType);
        sb.append(", studentId=").append(studentId);
        sb.append(", staffId=").append(staffId);
        sb.append(", birthDate=").append(birthDate);
        sb.append(", gender=").append(gender);
        sb.append(", height=").append(height);
        sb.append(", weight=").append(weight);
        sb.append(", bloodType=").append(bloodType);
        sb.append(", maritalStatus=").append(maritalStatus);
        sb.append(", fertilityStatus=").append(fertilityStatus);
        sb.append(", presentIllness=").append(presentIllness);
        sb.append(", pastIllness=").append(pastIllness);
        sb.append(", familyIllness=").append(familyIllness);
        sb.append(", allergyHistory=").append(allergyHistory);
        sb.append(", idType=").append(idType);
        sb.append(", idNumber=").append(idNumber);
        sb.append(", nation=").append(nation);
        sb.append(", nationality=").append(nationality);
        sb.append(", region=").append(region);
        sb.append(", detailedAddress=").append(detailedAddress);
        sb.append(", phoneNumber=").append(phoneNumber);
        sb.append(", homeAddress=").append(homeAddress);
        sb.append(", emergencyContact=").append(emergencyContact);
        sb.append(", emergencyPhone=").append(emergencyPhone);
        sb.append(", medicalHistory=").append(medicalHistory);
        sb.append(", identityVerify=").append(identityVerify);
        sb.append(", verifyTime=").append(verifyTime);
        sb.append(", outpatientNumber=").append(outpatientNumber);
        sb.append(", hospitalizationNumber=").append(hospitalizationNumber);
        sb.append(", barcodeInfo=").append(barcodeInfo);
        sb.append(", qrCodeInfo=").append(qrCodeInfo);
        sb.append("]");
        return sb.toString();
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getFertilityStatus() {
        return fertilityStatus;
    }

    public void setFertilityStatus(String fertilityStatus) {
        this.fertilityStatus = fertilityStatus;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getPresentIllness() {
        return presentIllness;
    }

    public void setPresentIllness(String presentIllness) {
        this.presentIllness = presentIllness;
    }

    public String getPastIllness() {
        return pastIllness;
    }

    public void setPastIllness(String pastIllness) {
        this.pastIllness = pastIllness;
    }

    public String getFamilyIllness() {
        return familyIllness;
    }

    public void setFamilyIllness(String familyIllness) {
        this.familyIllness = familyIllness;
    }

    public String getAllergyHistory() {
        return allergyHistory;
    }

    public void setAllergyHistory(String allergyHistory) {
        this.allergyHistory = allergyHistory;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getDetailedAddress() {
        return detailedAddress;
    }

    public void setDetailedAddress(String detailedAddress) {
        this.detailedAddress = detailedAddress;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public Integer getIdentityVerify() {
        return identityVerify;
    }

    public void setIdentityVerify(Integer identityVerify) {
        this.identityVerify = identityVerify;
    }

    public String getOutpatientNumber() {
        return outpatientNumber;
    }

    public void setOutpatientNumber(String outpatientNumber) {
        this.outpatientNumber = outpatientNumber;
    }

    public LocalDateTime getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(LocalDateTime verifyTime) {
        this.verifyTime = verifyTime;
    }

    public String getQrCodeInfo() {
        return qrCodeInfo;
    }

    public void setQrCodeInfo(String qrCodeInfo) {
        this.qrCodeInfo = qrCodeInfo;
    }

    public String getBarcodeInfo() {
        return barcodeInfo;
    }

    public void setBarcodeInfo(String barcodeInfo) {
        this.barcodeInfo = barcodeInfo;
    }

    public String getHospitalizationNumber() {
        return hospitalizationNumber;
    }

    public void setHospitalizationNumber(String hospitalizationNumber) {
        this.hospitalizationNumber = hospitalizationNumber;
    }
}
