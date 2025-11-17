package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("registration_type")
public class RegistrationType implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long typeId; // 号源类型ID
    private String typeName; // 类型名称（普通号、专家号等）
    private BigDecimal priceOriginal; // 原价
    private BigDecimal studentPrice; // 学生价
    private BigDecimal staffPrice; // 教师/职工价
    private Integer dailyQuota; // 每日号源上限
    private Integer isActive; // 是否启用（0-禁用；1-启用）
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}