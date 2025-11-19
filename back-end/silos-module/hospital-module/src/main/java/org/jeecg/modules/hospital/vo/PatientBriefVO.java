package org.jeecg.modules.hospital.vo;
import java.time.LocalDate;
public class PatientBriefVO {
    private Long patientId;
    private String name;
    private String gender; // 改为 String，匹配数据库 char(1)
    private LocalDate birthDate;
    private String phone;
    private java.time.LocalDateTime lastVisitDate;
    private Integer lastVisitStatus;
    private Integer visitCount;
    
    // 新增：前端需要的预约相关字段
    private String identity; // 患者身份：学生/教师/职工
    private String appointmentTimeRange; // 预约时间段
    private String statusText; // 状态文本
    private String statusClass; // 状态样式类
    private Long appointmentId; // 预约ID

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
    public java.time.LocalDateTime getLastVisitDate() { return lastVisitDate; }
    public void setLastVisitDate(java.time.LocalDateTime lastVisitDate) { this.lastVisitDate = lastVisitDate; }
    public Integer getLastVisitStatus() { return lastVisitStatus; }
    public void setLastVisitStatus(Integer lastVisitStatus) { this.lastVisitStatus = lastVisitStatus; }
    public Integer getVisitCount() { return visitCount; }
    public void setVisitCount(Integer visitCount) { this.visitCount = visitCount; }

    // 新增字段的 getter 和 setter 方法
    public String getIdentity() { return identity; }
    public void setIdentity(String identity) { this.identity = identity; }
    public String getAppointmentTimeRange() { return appointmentTimeRange; }
    public void setAppointmentTimeRange(String appointmentTimeRange) { this.appointmentTimeRange = appointmentTimeRange; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public String getStatusClass() { return statusClass; }
    public void setStatusClass(String statusClass) { this.statusClass = statusClass; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
}