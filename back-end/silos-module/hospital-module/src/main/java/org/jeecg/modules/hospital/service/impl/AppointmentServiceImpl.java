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
}