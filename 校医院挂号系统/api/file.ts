import http from '../utils/request'
import { getBaseURL, getApiPrefix } from '@/config/api'

/**
 * 上传身份认证照片
 * @param filePath 微信/uniApp 返回的本地文件路径
 * @param biz 业务标识，例如 identity/student-card/staff-card
 */
export function uploadIdentityPhoto(filePath: string, biz: string = 'identity'): Promise<any> {
  return http.upload('/file/upload', filePath, {
    name: 'file',
    formData: {
      biz,
    },
  })
}

/**
 * 获取静态图片的完整URL
 * @param filename 文件名，例如 "card.svg" 或 "images/no_data.png"
 * @returns 完整的图片URL
 */
export async function getStaticImageUrl(filename: string): Promise<string> {
  try {
    // 移除开头的 /static/ 前缀（如果有）
    const cleanFilename = filename.replace(/^\/static\//, '').replace(/^static\//, '')
    const res = await http.get(`/file/static/${cleanFilename}`)
    if (res && res.url) {
      return res.url
    }
    // 如果API调用失败，返回默认的构建URL
    return buildStaticImageUrl(cleanFilename)
  } catch (error) {
    console.error('获取静态图片URL失败:', error)
    // 失败时返回默认构建的URL
    return buildStaticImageUrl(filename.replace(/^\/static\//, '').replace(/^static\//, ''))
  }
}

/**
 * 构建静态图片的完整URL（不通过API，直接构建）
 * @param filename 文件名，例如 "card.svg" 或 "images/no_data.png"
 * @returns 完整的图片URL
 */
export function buildStaticImageUrl(filename: string): string {
  if (!filename) return ''
  
  // 移除开头的 /static/ 前缀（如果有）
  const cleanFilename = filename.replace(/^\/static\//, '').replace(/^static\//, '')
  const baseURL = getBaseURL()
  const apiPrefix = getApiPrefix()
  
  // 清理 baseURL 和 apiPrefix，确保没有多余的斜杠
  const cleanBaseURL = baseURL.replace(/\/$/, '') // 移除末尾斜杠
  const cleanPrefix = apiPrefix.startsWith('/') ? apiPrefix : `/${apiPrefix}`
  const cleanPrefix2 = cleanPrefix.replace(/\/$/, '') // 移除末尾斜杠
  
  // 构建相对路径：static-resources/文件名
  const relativePath = `static-resources/${cleanFilename}`
  
  // 构建完整URL：baseURL + apiPrefix + /sys/common/static/ + static-resources/文件名
  const fullUrl = `${cleanBaseURL}${cleanPrefix2}/sys/common/static/${relativePath}`
  
  // 开发环境输出URL用于调试
  if (process.env.NODE_ENV === 'development') {
    console.log('🖼️ 构建图片URL:', {
      原始文件名: filename,
      清理后文件名: cleanFilename,
      基础URL: baseURL,
      API前缀: apiPrefix,
      完整URL: fullUrl
    })
    
    // 提取域名用于检查（使用正则，避免 uni-app 兼容性问题）
    const urlMatch = fullUrl.match(/^(https?:\/\/[^\/]+)/)
    if (urlMatch) {
      const origin = urlMatch[1]
      console.log('📋 图片域名:', origin)
      
      // 检查是否是 HTTP（仅提示，不影响功能）
      if (origin.startsWith('http://')) {
        console.log('💡 提示：模拟器可以使用 HTTP，但请确保在微信开发者工具中开启了"不校验合法域名"选项')
      }
    }
  }
  
  return fullUrl
}