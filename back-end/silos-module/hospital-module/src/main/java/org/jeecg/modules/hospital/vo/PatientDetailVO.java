package org.jeecg.modules.hospital.vo;

import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.entity.PatientVisit;

import java.util.List;

public class PatientDetailVO {
    private Patient patient;
    private List<PatientVisit> visits;

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public List<PatientVisit> getVisits() { return visits; }
    public void setVisits(List<PatientVisit> visits) { this.visits = visits; }
}