package org.jeecg.modules.hospital.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 汇总 registration_record 相关信息，用于消息推送与回执展示
 */
@Data
public class RegistrationDetailDTO {
    private Long recordId;
    private String registrationNo;
    private Long patientId;
    private String patientName;
    private String patientCardNo;
    private Long patientUserId;

    private Long doctorId;
    private String doctorName;
    private String doctorTitle;

    private Long deptId;
    private String departmentName;
    private String deptLocation;

    private LocalDate scheduleDate;
    private Integer timeSlot;
    private LocalDateTime registerTime;

    private String typeName;
    private BigDecimal priceOriginal;
    private BigDecimal actualPrice;
    private Integer status;
    private String roomNumber;
}
