package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

@TableName(value = "registration_record")
public class RegistrationRecord implements Serializable {

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId; // 挂号记录ID

    @TableField("schedule_id")
    private Long scheduleId; // 排班ID

    @TableField("patient_id")
    private Long patientId; // 患者ID

    @TableField("doctor_id")
    private Long doctorId; // 医生ID

    @TableField("type_id")
    private Long typeId; // 号源类型ID

    @TableField("registration_no")
    private String registrationNo; // 挂号单号

    @TableField("register_time")
    private LocalDateTime registerTime; // 挂号时间

    @TableField("status")
    private Integer status; // 状态（0-候补；1-已预约；2-已就诊；3-已退号；4-已取消）

    @TableField("consult_room")
    private String consultRoom; // 诊室号

    @TableField("visit_time")
    private LocalDateTime visitTime; // 实际就诊时间
    
    @TableField("price_original")
    private BigDecimal priceOriginal; // 原价
    
    @TableField("actual_price")
    private BigDecimal actualPrice; // 实付价
    
    @TableField("waiting_rank")
    private Integer waitingRank; // 候补排名
    
    @TableField("cancel_time")
    private LocalDateTime cancelTime; // 取消/退号时间
    
    @TableField("cancel_reason")
    private String cancelReason; // 取消/退号原因
    
    @TableField("is_add")
    private Integer isAdd; // 是否加号（0-正常；1-加号）
    
    @TableField("add_remark")
    private String addRemark; // 加号备注

    // getter and setter methods
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }
    public LocalDateTime getRegisterTime() { return registerTime; }
    public void setRegisterTime(LocalDateTime registerTime) { this.registerTime = registerTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getConsultRoom() { return consultRoom; }
    public void setConsultRoom(String consultRoom) { this.consultRoom = consultRoom; }
    public LocalDateTime getVisitTime() { return visitTime; }
    public void setVisitTime(LocalDateTime visitTime) { this.visitTime = visitTime; }
    public BigDecimal getPriceOriginal() { return priceOriginal; }
    public void setPriceOriginal(BigDecimal priceOriginal) { this.priceOriginal = priceOriginal; }
    public BigDecimal getActualPrice() { return actualPrice; }
    public void setActualPrice(BigDecimal actualPrice) { this.actualPrice = actualPrice; }
    public Integer getWaitingRank() { return waitingRank; }
    public void setWaitingRank(Integer waitingRank) { this.waitingRank = waitingRank; }
    public LocalDateTime getCancelTime() { return cancelTime; }
    public void setCancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Integer getIsAdd() { return isAdd; }
    public void setIsAdd(Integer isAdd) { this.isAdd = isAdd; }
    public String getAddRemark() { return addRemark; }
    public void setAddRemark(String addRemark) { this.addRemark = addRemark; }
    
    // 额外的方法
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