package org.jeecg.modules.hospital.vo;

public class AppointmentDetailVO {
    private Long appointmentId;
    private String appointmentTime; // 例如：2025-10-22 08:00-12:00
    private String department;      // 科室名称
    private String doctor;          // 医生姓名

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }
}


