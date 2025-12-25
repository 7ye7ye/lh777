# API 配置说明

## 📍 统一配置文件位置

**配置文件：`config/api.ts`**

这是项目中**唯一**需要修改 API 地址的地方，修改后所有 API 请求都会使用新的地址。

> 💡 **真机调试配置：** 请参考 `真机调试完整指南.md` 或 `README-真机调试.md` 使用 cpolar 进行真机调试配置。

## 🔧 如何修改 API 地址

### 方法一：修改配置文件（推荐）

1. 打开 `校医院挂号系统/config/api.ts`
2. 找到 `API_CONFIG` 对象
3. 修改 `BASE_URL` 的值：

```typescript
export const API_CONFIG = {
  BASE_URL: 'http://10.60.73.201:8095', // 修改这里为你的实际IP地址
  API_PREFIX: '/jeecg-boot',
  TIMEOUT: 8000,
}
```

### 方法二：运行时动态修改（临时）

在微信开发者工具的控制台中执行：

```javascript
// 设置 API 地址
uni.setStorageSync('BASE_URL', 'http://192.168.1.100:8095')
uni.setStorageSync('API_PREFIX', '/jeecg-boot')

// 然后刷新页面
```

## 🌐 获取本机 IPv4 地址

### Windows 系统

1. 打开命令提示符（CMD）或 PowerShell
2. 执行命令：
   ```bash
   ipconfig
   ```
3. 查找 "IPv4 地址"，通常是 `192.168.x.x` 或 `10.x.x.x` 格式

### 或者使用 PowerShell

```powershell
Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notlike "127.*" -and $_.IPAddress -notlike "169.254.*" } | Select-Object IPAddress
```

## 📝 配置优先级

1. **运行时存储** (`uni.getStorageSync('BASE_URL')`) - 最高优先级
   - 如果设置了，且不是 localhost/127.0.0.1，则使用存储的值
   
2. **配置文件** (`API_CONFIG.BASE_URL`) - 默认使用
   - 应用启动时从配置文件读取
   
3. **默认值** (`http://localhost:8095`) - 兜底
   - 如果以上都没有，使用默认值

## 🔍 检查当前配置

在控制台执行：

```javascript
console.log('BASE_URL:', uni.getStorageSync('BASE_URL'))
console.log('API_PREFIX:', uni.getStorageSync('API_PREFIX'))
```

## ✅ 验证配置是否生效

1. 打开浏览器开发者工具
2. 查看 Network 标签
3. 发起一个 API 请求
4. 检查请求 URL 是否使用了正确的 IP 地址

## 🚨 常见问题

### 问题1：修改了配置文件但请求还是使用旧地址

**解决方案：**
1. 清除浏览器/微信开发者工具的缓存
2. 重新编译项目
3. 检查是否有其他地方硬编码了地址

### 问题2：真机调试无法连接

**检查清单：**
- ✅ 确保手机和电脑在同一局域网
- ✅ 确保防火墙允许 8095 端口
- ✅ 确保后端服务正在运行
- ✅ 使用正确的 IPv4 地址（不是 127.0.0.1）

### 问题3：配置文件无法加载

**解决方案：**
- 检查 `config/api.ts` 文件是否存在
- 检查文件语法是否正确
- 查看控制台是否有错误信息

## 📚 相关文件

- `config/api.ts` - 统一配置文件
- `utils/request.ts` - 请求工具（使用配置文件）
- `api/file.ts` - 文件相关 API（使用配置文件）
- `main.js` - 应用入口（初始化配置）

## 💡 提示

- 修改配置文件后，需要**重新编译**项目才能生效
- 真机调试时，确保手机能访问到电脑的 IP 地址
- 生产环境部署时，记得修改为实际的服务器地址

