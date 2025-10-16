package org.jeecg.modules.hospital.controller;

import org.jeecg.modules.hospital.entity.Appointment;
import org.jeecg.modules.hospital.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 根据ID获取预约详情（回执单）
     */
    @GetMapping("/detail")
    public Appointment getAppointmentDetail(@RequestParam String id) {
        return appointmentService.getById(id);
    }
}