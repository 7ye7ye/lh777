# Profile 目录结构说明

## 目录组织

本目录已按功能模块重新组织，提高代码的可维护性和可读性。

### 目录结构

```
profile/
├── profile.vue              # 主页面入口
├── help/                    # 帮助系统
│   ├── help.vue            # 帮助反馈主页面
│   ├── appointment.vue      # 预约帮助
│   ├── bindcard.vue         # 绑卡帮助
│   ├── hospitalization.vue  # 住院帮助
│   ├── login.vue           # 登录帮助
│   ├── other.vue           # 其他帮助
│   ├── payment.vue         # 支付帮助
│   ├── register.vue        # 挂号帮助
│   └── report.vue          # 报告帮助
├── personal/                # 个人信息相关
│   ├── mycard.vue          # 我的就诊卡
│   ├── mydoctor.vue        # 我的医生
│   └── mypatient.vue       # 我的就诊人
├── records/                 # 就诊记录相关
│   ├── check-record.vue    # 检查预约记录
│   ├── consult-record.vue  # 咨询记录
│   ├── hospital-record.vue # 住院预交记录
│   ├── outpatient-record.vue # 门诊缴费记录
│   ├── register-record.vue # 挂号记录
│   └── revisit-record.vue  # 在线复诊记录
└── settings/                # 设置相关
    ├── complain.vue        # 投诉建议
    ├── evaluate.vue        # 就诊评价
    ├── privacy.vue         # 隐私协议
    └── unbind.vue          # 账户解绑
```

### 功能分类

1. **personal/** - 个人信息管理
   - 就诊卡管理
   - 医生管理
   - 就诊人管理

2. **records/** - 就诊记录查看
   - 各种类型的就诊记录
   - 缴费记录
   - 预约记录

3. **settings/** - 系统设置
   - 隐私设置
   - 账户管理
   - 反馈功能

4. **help/** - 帮助系统
   - 各种功能的帮助说明
   - 使用指南

### 优势

- **模块化**: 按功能分类，便于维护
- **可扩展**: 新功能可以轻松添加到对应分类
- **清晰性**: 目录结构一目了然
- **团队协作**: 不同开发者可以专注于不同模块
