# 权限控制工具使用说明

## 概述
`utils/auth.js` 提供了统一的权限控制工具，避免在每个页面重复写登录判断逻辑。

## 功能配置

### 1. 功能标识定义
```javascript
import { AUTH_REQUIRED_FEATURES } from '@/utils/auth'

// 使用预定义的功能标识
AUTH_REQUIRED_FEATURES.HOME.VISIT_CARD      // 电子就诊卡
AUTH_REQUIRED_FEATURES.PROFILE.MY_CARD      // 我的就诊卡
AUTH_REQUIRED_FEATURES.PROFILE.RECORDS      // 就诊记录
// ... 更多功能标识
```

### 2. 自动提示文案
每个功能都有对应的提示文案，无需手动编写：
```javascript
// 自动获取提示文案
getAuthMessage(AUTH_REQUIRED_FEATURES.HOME.VISIT_CARD)
// 返回: "请先登录后查看电子就诊卡"
```

## 使用方法

### 方法一：createAuthHandler（推荐）
创建带权限检查的点击处理函数：

```javascript
import { AUTH_REQUIRED_FEATURES, createAuthHandler } from '@/utils/auth'

// 创建权限检查函数
const goToMyCard = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.MY_CARD,
  '/subpkg/profile/personal/mycard'
)

// 在模板中直接使用
<view @click="goToMyCard">我的就诊卡</view>
```

### 方法二：checkAuth
手动检查权限并处理：

```javascript
import { AUTH_REQUIRED_FEATURES, checkAuth } from '@/utils/auth'

const handleClick = () => {
  checkAuth(AUTH_REQUIRED_FEATURES.HOME.VISIT_CARD, {
    onSuccess: () => {
      // 已登录，执行正常逻辑
      uni.navigateTo({ url: '/subpkg/profile/personal/mycard' })
    },
    onFail: () => {
      // 未登录，已自动显示登录提示
      console.log('用户未登录')
    }
  })
}
```

### 方法三：navigateWithAuth
带权限检查的页面跳转：

```javascript
import { AUTH_REQUIRED_FEATURES, navigateWithAuth } from '@/utils/auth'

const goToPage = () => {
  navigateWithAuth(
    AUTH_REQUIRED_FEATURES.PROFILE.MY_CARD,
    '/subpkg/profile/personal/mycard'
  )
}
```

## 自定义配置

### 添加新功能
在 `utils/auth.js` 中添加新的功能标识：

```javascript
// 在 AUTH_REQUIRED_FEATURES 中添加
OTHER: {
  NEW_FEATURE: 'new_feature',
}

// 在 AUTH_MESSAGES 中添加提示文案
[AUTH_REQUIRED_FEATURES.OTHER.NEW_FEATURE]: '请先登录后使用新功能',

// 在 AUTH_REDIRECTS 中添加跳转路径（可选）
[AUTH_REQUIRED_FEATURES.OTHER.NEW_FEATURE]: '/subpkg/other/new-feature',
```

### 自定义提示文案
```javascript
const goToPage = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.MY_CARD,
  '/subpkg/profile/personal/mycard',
  { customMessage: '自定义提示文案' }
)
```

## 实际应用示例

### 首页电子就诊卡
```javascript
// pages/home/home.vue
const onVisitCardClick = createAuthHandler(
  AUTH_REQUIRED_FEATURES.HOME.VISIT_CARD,
  '/subpkg/profile/personal/mycard'
)
```

### 个人中心功能
```javascript
// pages/profile/profile.vue
const goToMyCard = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.MY_CARD,
  '/subpkg/profile/personal/mycard'
)

const goToRecords = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
  '/subpkg/profile/records/register-record'
)
```

## 优势

1. **统一管理**：所有需要登录的功能都在一个文件中配置
2. **减少重复**：不需要在每个页面写相同的登录判断逻辑
3. **易于维护**：修改提示文案或跳转逻辑只需在一个地方修改
4. **类型安全**：使用预定义的功能标识，避免拼写错误
5. **灵活扩展**：可以轻松添加新功能或自定义行为

## 注意事项

- 确保 `useUserStore` 正确初始化用户状态
- 登录页路径统一为 `/subpkg/auth/login`
- 所有权限检查都会自动显示登录提示弹窗
- 可以通过 `showModal: false` 选项禁用弹窗提示
