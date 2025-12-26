/**
 * 图片工具函数
 * 用于处理静态图片URL，将前端/static/路径转换为后端API路径
 */

import { buildStaticImageUrl } from '../api/file'

/**
 * 获取静态图片的完整URL
 * 将前端的 /static/xxx 路径转换为后端API路径
 * 
 * @param path 图片路径，例如 "/static/card.svg" 或 "card.svg"
 * @returns 完整的图片URL
 */
export function getStaticImage(path: string): string {
  if (!path) return ''
  
  // 移除开头的 /static/ 前缀
  const cleanPath = path.replace(/^\/static\//, '').replace(/^static\//, '')
  
  // 使用buildStaticImageUrl构建完整URL
  return buildStaticImageUrl(cleanPath)
}

/**
 * 批量获取静态图片URL（用于computed或响应式数据）
 * @param paths 图片路径数组
 * @returns 图片URL对象，key为原始路径，value为完整URL
 */
export function getStaticImages(paths: string[]): Record<string, string> {
  const result: Record<string, string> = {}
  paths.forEach(path => {
    result[path] = getStaticImage(path)
  })
  return result
}

