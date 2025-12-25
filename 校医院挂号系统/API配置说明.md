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
  // 根据使用场景选择：
  // BASE_URL: 'http://localhost:8095', // 仅模拟器
  // BASE_URL: 'http://10.61.168.113:8095', // 本机内网IP，仅模拟器
  // BASE_URL: 'http://183.242.199.186:8095', // 公网IP（需要服务器配置）
  BASE_URL: 'https://xxxxx.cpolar.cn', // 内网穿透HTTPS地址，真机可用（推荐）
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

## 🌐 获取本机 IP 地址

### 当前网络信息

根据你的网络配置：
- **本机内网 IPv4 地址**：`10.61.168.113`（网络：phone.wlan.bjtu）
- **公网 IP 地址**：`183.242.199.186`
- **默认网关**：`10.61.0.1`

### ⚠️ 重要说明

1. **内网 IP（10.61.168.113）**：
   - ✅ 可用于微信开发者工具模拟器
   - ❌ **不能用于真机调试**（真机无法访问内网 IP）
   - 需要确保后端服务绑定到 `0.0.0.0:8095` 而不是 `127.0.0.1:8095`

2. **公网 IP（183.242.199.186）**：
   - 这是学校的公网出口 IP
   - ❌ **不能直接使用**（需要端口转发和防火墙配置）
   - 仅当有公网服务器且配置了端口转发时可用

3. **真机调试推荐方案**：
   - ✅ 使用 **cpolar** 等内网穿透工具（推荐）
   - ✅ 使用 HTTPS 地址（微信小程序要求）

### Windows 系统获取 IP 地址

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

**重要：真机调试必须使用内网穿透工具（如 cpolar），不能直接使用内网 IP！**

**检查清单：**
- ✅ 使用 cpolar 等内网穿透工具（推荐）
- ✅ 使用 HTTPS 地址（微信小程序要求）
- ✅ 确保后端服务运行在 `0.0.0.0:8095`（不是 `127.0.0.1:8095`）
- ✅ 确保防火墙允许 8095 端口
- ✅ 确保后端服务正在运行
- ✅ 更新微信小程序合法域名（request、uploadFile、downloadFile）

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

