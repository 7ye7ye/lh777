package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@TableName("appointments") // 假设你的预约主表名为 appointments
public class Appointment implements Serializable {
    private String id; // 预约ID，对应 appointment_id
    private String qrCodeData; // 用于生成二维码的数据
    private String serialNumber; // 预约流水
    private String patientName; // 就诊人
    private String hospitalAddress; // 医院地址
    private String departmentName; // 就诊科室
    private String visitLocation; // 就诊地点
    private String doctorName; // 预约医生
    private LocalDate appointmentDate; // 预约日期
    private LocalTime appointmentTime; // 预约时间
    private BigDecimal consultationFee; // 诊查费
    private String status; // 业务状态 (例如：预约成功, 已取消)
    private String orderNumber; // 商户订单号
    @TableField(exist = false)
    private Boolean cancelled; // 非持久化字段，前端用于区分退号态
}