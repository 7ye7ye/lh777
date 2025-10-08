package org.jeecg.modules.hospital.contant;

public enum UserTypeEnum {
    // 枚举实例：患者、医生、管理员
    PATIENT("patient", 1, "患者"),
    DOCTOR("doctor", 2, "医生"),
    ADMIN("admin", 3, "管理员");

    // 成员变量
    private final String code;   // 前端传递的标识（如"doctor"）
    private final Integer value; // 数据库存储的数值（如2）
    private final String desc;   // 角色描述

    // 构造方法（私有，枚举类构造器必须为private）
    UserTypeEnum(String code, Integer value, String desc) {
        this.code = code;
        this.value = value;
        this.desc = desc;
    }

    // Getter方法
    public String getCode() {
        return code;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    // 工具方法：根据code获取枚举实例
    public static UserTypeEnum getByCode(String code) {
        for (UserTypeEnum type : UserTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null; // 无匹配时返回null，也可抛出异常
    }

    // 工具方法：根据value获取枚举实例
    public static UserTypeEnum getByValue(Integer value) {
        for (UserTypeEnum type : UserTypeEnum.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null; // 无匹配时返回null，也可抛出异常
    }

    // 工具方法：判断code是否为有效枚举值
    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }

    // 工具方法：判断value是否为有效枚举值
    public static boolean isValidValue(Integer value) {
        return getByValue(value) != null;
    }
}
