package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("registration_record")
public class RegistrationRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long recordId; // 挂号记录ID
    private Long scheduleId; // 排班ID
    private Long patientId; // 患者ID
    private Long doctorId; // 医生ID
    private Long typeId; // 号源类型ID
    private String registrationNo; // 挂号单号
    private LocalDateTime registerTime; // 挂号时间
    private Integer status; // 状态（0-候补；1-已预约；2-已就诊；3-已退号；4-已取消）
    private BigDecimal priceOriginal; // 原价
    private BigDecimal actualPrice; // 实付价
    private Integer waitingRank; // 候补排名
    private String consultRoom; // 诊室号
    private LocalDateTime visitTime; // 实际就诊时间
    private LocalDateTime cancelTime; // 取消/退号时间
    private String cancelReason; // 取消/退号原因
    private Integer isAdd; // 是否加号（0-正常；1-加号）
    private String addRemark; // 加号备注

    public Integer getDailyQuota() {
        // TODO: 这里可以通过注入 RegistrationTypeMapper 或 service 查询
        // 暂时返回示例值
        if (typeId == null) {
            return 0;
        }
        return switch (typeId.intValue()) {
            case 1 -> // 普通号
                    50;
            case 2 -> // 专家号
                    20;
            case 3 -> // 特需号
                    10;
            default -> 0;
        };
    }

}