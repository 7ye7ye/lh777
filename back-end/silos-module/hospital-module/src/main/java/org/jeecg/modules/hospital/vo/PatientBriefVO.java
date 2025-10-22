package org.jeecg.modules.hospital.vo;

import java.time.LocalDate;

public class PatientBriefVO {
    private Long patientId;
    private String name;
    private String gender; // 改为 String，匹配数据库 char(1)
    private LocalDate birthDate;
    private String phone;
    private LocalDate lastVisitDate;
    private Integer lastVisitStatus;
    private Integer visitCount;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getLastVisitDate() { return lastVisitDate; }
    public void setLastVisitDate(LocalDate lastVisitDate) { this.lastVisitDate = lastVisitDate; }
    public Integer getLastVisitStatus() { return lastVisitStatus; }
    public void setLastVisitStatus(Integer lastVisitStatus) { this.lastVisitStatus = lastVisitStatus; }
    public Integer getVisitCount() { return visitCount; }
    public void setVisitCount(Integer visitCount) { this.visitCount = visitCount; }
}