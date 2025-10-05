package org.jeecg.common.system.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 医院相关用户信息
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
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
     * 身份证号（敏感信息，加密存储）
     */
    private String idCard;

    /**
     * 手机号（用于接收就诊提醒）
     */
    private String phone;

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
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
}
