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
  const cleanPrefix = apiPrefix.endsWith('/') ? apiPrefix.slice(0, -1) : apiPrefix
  const relativePath = `static-resources/${cleanFilename}`
  const fullUrl = `${baseURL}${cleanPrefix}/sys/common/static/${relativePath}`
  
  // 开发环境输出URL用于调试（包括模拟器和真机）
  if (process.env.NODE_ENV === 'development') {
    console.log('🖼️ 构建图片URL:', {
      filename,
      cleanFilename,
      baseURL,
      apiPrefix,
      fullUrl
    })
    // 提取域名用于检查 downloadFile合法域名
    try {
      const urlObj = new URL(fullUrl)
      console.log('📋 图片域名:', urlObj.origin)
      console.log('⚠️ 请确保此域名已配置在微信公众平台的 downloadFile合法域名 中！')
    } catch (e) {
      console.warn('无法解析URL:', fullUrl)
    }
  }
  
  return fullUrl
}