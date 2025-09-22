package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 医生表
 * @TableName doctor
 */
@TableName(value ="doctor")
@Data
public class Doctor {
    /**
     * 医生唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long doctorId;

    /**
     * 关联用户表
     */
    private Long userId;

    /**
     * 所属科室ID（二级科室，如“消化内科”）
     */
    private Long deptId;

    /**
     * 职称（如“主治医师”“主任医师”）
     */
    private String title;

    /**
     * 擅长领域（如“胃炎、胃溃疡诊疗”）
     */
    private String specialty;

    /**
     * 医生简介
     */
    private String doctorDesc;

    /**
     * 医生头像URL
     */
    private String avatar;

    /**
     * 出诊状态（0-暂停出诊；1-正常出诊）
     */
    private Integer isActive;

    /**
     * 信息修改审核状态（0-未提交修改；1-待审核；2-已通过；3-已驳回）
     */
    private Integer updateVerify;

    /**
     * 医生姓名
     */
    private String doctorName;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Doctor other = (Doctor) that;
        return (this.getDoctorId() == null ? other.getDoctorId() == null : this.getDoctorId().equals(other.getDoctorId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getDeptId() == null ? other.getDeptId() == null : this.getDeptId().equals(other.getDeptId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getSpecialty() == null ? other.getSpecialty() == null : this.getSpecialty().equals(other.getSpecialty()))
            && (this.getDoctorDesc() == null ? other.getDoctorDesc() == null : this.getDoctorDesc().equals(other.getDoctorDesc()))
            && (this.getAvatar() == null ? other.getAvatar() == null : this.getAvatar().equals(other.getAvatar()))
            && (this.getIsActive() == null ? other.getIsActive() == null : this.getIsActive().equals(other.getIsActive()))
            && (this.getUpdateVerify() == null ? other.getUpdateVerify() == null : this.getUpdateVerify().equals(other.getUpdateVerify()))
            && (this.getDoctorName() == null ? other.getDoctorName() == null : this.getDoctorName().equals(other.getDoctorName()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getDoctorId() == null) ? 0 : getDoctorId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getDeptId() == null) ? 0 : getDeptId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getSpecialty() == null) ? 0 : getSpecialty().hashCode());
        result = prime * result + ((getDoctorDesc() == null) ? 0 : getDoctorDesc().hashCode());
        result = prime * result + ((getAvatar() == null) ? 0 : getAvatar().hashCode());
        result = prime * result + ((getIsActive() == null) ? 0 : getIsActive().hashCode());
        result = prime * result + ((getUpdateVerify() == null) ? 0 : getUpdateVerify().hashCode());
        result = prime * result + ((getDoctorName() == null) ? 0 : getDoctorName().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", doctorId=").append(doctorId);
        sb.append(", userId=").append(userId);
        sb.append(", deptId=").append(deptId);
        sb.append(", title=").append(title);
        sb.append(", specialty=").append(specialty);
        sb.append(", doctorDesc=").append(doctorDesc);
        sb.append(", avatar=").append(avatar);
        sb.append(", isActive=").append(isActive);
        sb.append(", updateVerify=").append(updateVerify);
        sb.append(", doctorName=").append(doctorName);
        sb.append("]");
        return sb.toString();
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getDoctorDesc() {
        return doctorDesc;
    }

    public void setDoctorDesc(String doctorDesc) {
        this.doctorDesc = doctorDesc;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getUpdateVerify() {
        return updateVerify;
    }

    public void setUpdateVerify(Integer updateVerify) {
        this.updateVerify = updateVerify;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
}
