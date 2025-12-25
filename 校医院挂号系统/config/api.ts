/**
 * API 配置
 * 统一管理后端 API 地址，方便修改和部署
 * 
 * 使用方法：
 * 1. 修改下面的 BASE_URL 为你的后端服务器地址
 * 2. 如果是真机调试，使用内网穿透的 HTTPS 地址（如：https://xxxxx.ngrok-free.dev）
 * 3. 如果是生产环境，使用实际的服务器地址（如：https://api.example.com）
 * 
 * ⚠️ 重要：这是项目中唯一需要修改 API 地址的地方！
 */

// @ts-ignore - uni 是 uni-app 的全局对象，运行时存在
declare const uni: any

// ==================== 配置区域 ====================
// 后端服务器地址
// 
// 配置说明：
// 1. 本地开发：'http://localhost:8095'
// 2. 真机调试（使用内网穿透）：'https://xxxxx.natapp1.cc' (使用内网穿透工具的公网地址)
// 3. 真机调试（局域网IP）：'http://192.168.1.100:8095' (仅开发者工具可用，真机需要内网穿透)
// 4. 生产环境：'https://api.example.com'
//
// 内网穿透工具推荐：
// - natapp: https://natapp.cn (国内，推荐)
// - cpolar: https://www.cpolar.com (国内)
// - ngrok: https://ngrok.com (国外)
//
// 详细配置步骤请查看：内网穿透配置指南.md
export const API_CONFIG = {
  // 后端服务器基础地址
  // 使用内网穿透时，修改为内网穿透工具提供的 HTTPS 地址
  // ⚠️ 重要：微信小程序真机环境必须使用 HTTPS！
  // 
  // 如果 natapp 免费版只提供 HTTP，可以：
  // 1. 使用 cpolar（免费版支持 HTTPS）：https://www.cpolar.com
  // 2. 购买 natapp VIP（支持 HTTPS 和固定域名）
  // 3. 使用 ngrok（免费版支持 HTTPS）：https://ngrok.com
  //
  // 例如：'https://abc123.natapp1.cc' 或 'https://xxxxx.cpolar.cn'
//   BASE_URL: 'http://10.60.73.201:8095', // 本地 IPv4 地址，模拟器显示图片正常，真机调试无法显示图片
  // BASE_URL: 'https://470edbfe.r8.cpolar.cn', // 内网穿透生成的HTTPS 地址，真机可用,每次生成的地址不一样需要手动配置
  BASE_URL: 'http://localhost:8095', //不使用真机调试，仅在电脑上运行用这个

  // API 前缀
  API_PREFIX: '/jeecg-boot',
  
  // 请求超时时间（毫秒）
  TIMEOUT: 8000,
}

// ==================== 自动获取本机 IPv4 地址（仅开发环境） ====================
/**
 * 获取本机 IPv4 地址（用于真机调试）
 * 注意：这个方法只在开发环境有效，生产环境请直接配置 BASE_URL
 */
function getLocalIPv4(): string {
  // 在 uni-app 环境中，无法直接获取本机IP
  // 需要手动配置，或者通过其他方式获取
  // 这里提供一个默认值，实际使用时需要手动修改
  return '192.168.1.100' // TODO: 修改为你的本机IP地址
}

/**
 * 检测并返回 BASE_URL
 * 优先级：
 * 1. uni.getStorageSync('BASE_URL') - 运行时动态设置（最高优先级）
 * 2. API_CONFIG.BASE_URL - 配置文件中的值
 * 3. 默认值 - http://localhost:8095
 */
export function getBaseURL(): string {
  // 1. 优先使用运行时存储的值（可以在控制台动态修改）
  // 注意：如果存储的值是旧的 localhost、127.0.0.1 或 IPv4 地址，则使用配置文件的值
  if (typeof uni !== 'undefined') {
    const stored = uni.getStorageSync('BASE_URL')
    if (stored) {
      // 如果存储的值是旧的 IP 地址或 localhost，忽略它，使用配置文件的值
      if (stored.includes('localhost') || 
          stored.includes('127.0.0.1') || 
          /^\d+\.\d+\.\d+\.\d+/.test(stored.replace(/^https?:\/\//, '').split(':')[0])) {
        console.warn('⚠️ 检测到旧的 IP 地址配置，使用配置文件中的值:', stored)
        // 继续使用配置文件的值
      } else {
        // 存储的值看起来是有效的域名，使用它
        return stored
      }
    }
  }
  
  // 2. 使用配置文件中的值（这是主要配置来源）
  if (API_CONFIG.BASE_URL) {
    return API_CONFIG.BASE_URL
  }
  
  // 3. 默认值（不应该到达这里，因为配置文件应该总是有值）
  console.error('❌ API_CONFIG.BASE_URL 未配置，请检查 config/api.ts')
  return 'http://localhost:8095'
}

/**
 * 获取 API 前缀
 */
export function getApiPrefix(): string {
  if (typeof uni !== 'undefined') {
    const stored = uni.getStorageSync('API_PREFIX')
    if (stored) {
      return stored
    }
  }
  return API_CONFIG.API_PREFIX
}

/**
 * 获取完整的 API 基础地址（包含前缀）
 */
export function getFullBaseURL(): string {
  const baseURL = getBaseURL()
  const prefix = getApiPrefix()
  // 确保 baseURL 不以 / 结尾，prefix 以 / 开头
  const cleanBase = baseURL.replace(/\/$/, '')
  const cleanPrefix = prefix.startsWith('/') ? prefix : `/${prefix}`
  return `${cleanBase}${cleanPrefix}`
}

/**
 * 设置 API 地址（运行时动态修改）
 * 可以在微信开发者工具控制台调用：
 *   uni.setStorageSync('BASE_URL', 'http://192.168.1.100:8095')
 */
export function setBaseURL(url: string) {
  if (typeof uni !== 'undefined') {
    uni.setStorageSync('BASE_URL', url)
    console.log('已设置 BASE_URL:', url)
  } else {
    console.warn('uni 未定义，无法设置 BASE_URL')
  }
}

/**
 * 设置 API 前缀（运行时动态修改）
 */
export function setApiPrefix(prefix: string) {
  if (typeof uni !== 'undefined') {
    uni.setStorageSync('API_PREFIX', prefix)
    console.log('已设置 API_PREFIX:', prefix)
  } else {
    console.warn('uni 未定义，无法设置 API_PREFIX')
  }
}

// 导出默认配置
export default API_CONFIG

