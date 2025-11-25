package org.jeecg.modules.hospital.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 挂号记录视图对象，用于科室/疾病筛选结果展示
 */
@Data
public class RegistrationVO {
    private Long recordId;
    private String registrationNo;
    private String patientName;
    private String doctorName;
    private String departmentName;
    private String typeName;
    private BigDecimal actualPrice;
    private LocalDate appointmentDate;
    private Integer timeSlot;
    private String consultRoom;
    private Integer status;
}

