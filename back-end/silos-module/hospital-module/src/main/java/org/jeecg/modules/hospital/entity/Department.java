package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 科室表
 * @TableName department
 */
@TableName(value ="department")
@Data
public class Department {
    /**
     * 科室唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long deptId;

    /**
     * 科室名称（如“内科”“消化内科”）
     */
    private String deptName;

    /**
     * 父科室ID（顶级科室为0，如“内科”的父科室为0，“消化内科”父科室为内科ID）
     */
    private Long parentDeptId;

    /**
     * 科室级别（1-一级科室；2-二级科室）
     */
    private Integer deptLevel;

    /**
     * 科室简介（如“负责消化系统疾病诊疗”）
     */
    private String deptDesc;

    /**
     * 科室位置（如“门诊楼3层东侧”，用于导航扩展功能）
     */
    private String location;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

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
        Department other = (Department) that;
        return (this.getDeptId() == null ? other.getDeptId() == null : this.getDeptId().equals(other.getDeptId()))
            && (this.getDeptName() == null ? other.getDeptName() == null : this.getDeptName().equals(other.getDeptName()))
            && (this.getParentDeptId() == null ? other.getParentDeptId() == null : this.getParentDeptId().equals(other.getParentDeptId()))
            && (this.getDeptLevel() == null ? other.getDeptLevel() == null : this.getDeptLevel().equals(other.getDeptLevel()))
            && (this.getDeptDesc() == null ? other.getDeptDesc() == null : this.getDeptDesc().equals(other.getDeptDesc()))
            && (this.getLocation() == null ? other.getLocation() == null : this.getLocation().equals(other.getLocation()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getDeptId() == null) ? 0 : getDeptId().hashCode());
        result = prime * result + ((getDeptName() == null) ? 0 : getDeptName().hashCode());
        result = prime * result + ((getParentDeptId() == null) ? 0 : getParentDeptId().hashCode());
        result = prime * result + ((getDeptLevel() == null) ? 0 : getDeptLevel().hashCode());
        result = prime * result + ((getDeptDesc() == null) ? 0 : getDeptDesc().hashCode());
        result = prime * result + ((getLocation() == null) ? 0 : getLocation().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", deptId=").append(deptId);
        sb.append(", deptName=").append(deptName);
        sb.append(", parentDeptId=").append(parentDeptId);
        sb.append(", deptLevel=").append(deptLevel);
        sb.append(", deptDesc=").append(deptDesc);
        sb.append(", location=").append(location);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}
