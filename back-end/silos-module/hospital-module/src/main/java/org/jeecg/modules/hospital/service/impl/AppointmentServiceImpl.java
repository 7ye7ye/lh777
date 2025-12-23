package org.jeecg.modules.hospital.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.Appointment;
import org.jeecg.modules.hospital.mapper.AppointmentMapper;
import org.jeecg.modules.hospital.service.AppointmentService;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;

@Service
@DS("hospital") // 同样指定使用 hospital 数据源
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {
    
    @Override
    public boolean save(Appointment appointment) {
        // 1. 检查科室预约次数限制：一个科室不能挂不同医生三次
        if (appointment.getPatientId() != null && appointment.getDepartmentName() != null) {
            int appointmentCount = baseMapper.countByPatientAndDepartment(appointment.getPatientId(), appointment.getDepartmentName());
            if (appointmentCount >= 3) {
                throw new RuntimeException("同一个科室最多只能预约三次不同的医生");
            }
        }
        
        // 2. 检查预约时间限制：预约时段截止前两个小时不能预约
        if (appointment.getAppointmentDate() != null && appointment.getAppointmentTime() != null) {
            java.time.LocalDateTime appointmentDateTime = java.time.LocalDateTime.of(appointment.getAppointmentDate(), appointment.getAppointmentTime());
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime twoHoursBefore = appointmentDateTime.minusHours(2);
            
            if (now.isAfter(twoHoursBefore)) {
                throw new RuntimeException("预约时段截止前两个小时不能预约");
            }
        }
        
        // 执行保存操作
        return super.save(appointment);
    }
}