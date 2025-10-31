package org.jeecg.modules.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户表
 * @TableName hos_user
 */
@TableName(value ="hos_user")
@Data
public class HosUser {
    /**
     * 用户唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * 登录账号（患者：学号/工号/手机号；医生：管理员分配账号；管理员：固定账号）
     */
    private String userAccount;

    /**
     * 加密存储的密码（如MD5+盐值）
     */
    private String userPassword;

    /**
     * 用户类型（1-患者；2-医生；3-管理员）
     */
    private Integer userType;

    /**
     * 邮箱（可选通知渠道）
     */
    private String email;

    /**
     * 账号状态（0-未激活；1-正常；2-禁用）
     */
    private Integer status;

    /**
     * 账号创建时间
     */
    private LocalDateTime createTime;

    /**
     * 账号更新时间
     */
    private LocalDateTime updateTime;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", userId=").append(userId);
        sb.append(", userAccount=").append(userAccount);
        sb.append(", userPassword=").append(userPassword);
        sb.append(", userType=").append(userType);
        sb.append(", email=").append(email);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public org.jeecg.common.system.vo.HosUser convertToVO() {
        org.jeecg.common.system.vo.HosUser hosUser = new org.jeecg.common.system.vo.HosUser();
        hosUser.setUserId(this.getUserId());
        hosUser.setUserAccount(this.getUserAccount());
        hosUser.setUserType(this.getUserType());
        hosUser.setStatus( this.getStatus());
        System.out.println("根据账号从数据库中取出的用户: " + this.toString());
        System.out.println("转换类型后的用户: " + hosUser.toString());
        return hosUser;
    }
}
