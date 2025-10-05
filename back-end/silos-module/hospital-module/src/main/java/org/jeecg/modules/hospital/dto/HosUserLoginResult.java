package org.jeecg.modules.hospital.dto; // 包路径必须与存放位置一致

import org.jeecg.modules.hospital.entity.HosUser; // 导入你的HosUser实体类

/**
 * 医院用户登录结果DTO（封装用户信息和Token）
 */
public class HosUserLoginResult {

    // 1. 你的医院用户脱敏信息（HosUser）
    private HosUser user;

    // 2. 复用原有系统生成的Token（与原有登录接口返回的Token格式一致）
    private String token;

    // 3. 可选：可添加其他需要返回给前端的字段（如过期时间、用户角色等）
    private Long tokenExpireTime; // Token过期时间（毫秒数，可选）

    // 无参构造器（JSON序列化/反序列化需要）
    public HosUserLoginResult() {
    }

    // 有参构造器（方便快速创建对象）
    public HosUserLoginResult(HosUser user, String token) {
        this.user = user;
        this.token = token;
    }

    // 4. Getter 和 Setter 方法（必须，否则前端无法获取字段值）
    public HosUser getUser() {
        return user;
    }

    public void setUser(HosUser user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(Long tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
    }
}
