package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
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
     * 用户姓名（映射 patient_name）
     */
    @TableField("patient_name")
    private String patientName;

    /**
     * 患者身份（1-学生；2-教师；3-职工）
     */
    private Integer patientType;

    /**
     * 身份证号（映射 id_number）
     */
    @TableField("id_number")
    private String idCard;

    /**
     * 手机号（映射 phone_number）
     */
    @TableField("phone_number")
    private String phone;

    private String studentId;

    private String staffId;

    private LocalDate birthDate;

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
     * 身份认证状态（0-未审核；1-已通过；2-未通过，默认0）
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

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
