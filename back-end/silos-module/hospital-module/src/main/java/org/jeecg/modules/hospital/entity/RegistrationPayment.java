package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("registration_payment")
public class RegistrationPayment implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long paymentId; // 支付ID
    private Long registrationId; // 挂号记录ID
    private String paymentMethod; // 支付方式（微信支付、支付宝、现金）
    private BigDecimal amount; // 支付金额
    private LocalDateTime paymentTime; // 支付时间
    private String status; // 支付状态（已支付、已退款）
    private LocalDateTime createTime; // 创建时间
}