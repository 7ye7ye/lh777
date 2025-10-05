package org.jeecg.modules.hospital.controller;

import jakarta.annotation.Resource;
import org.jeecg.modules.hospital.entity.Patient;
import org.jeecg.modules.hospital.service.PatientService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patient")
public class PatientController {
    @Resource
    private PatientService patirntService;

    @PostMapping("/add")
    public String add(@RequestBody Patient patient) {
         patirntService.save(patient);
        return "success";
    }

}
