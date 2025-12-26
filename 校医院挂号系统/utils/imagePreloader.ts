/**
 * 图片预下载工具
 * 使用 uni.downloadFile 下载图片，解决 ngrok 警告页面问题
 */

import { getStaticImage } from './imageHelper'

// @ts-ignore - uni 是 uni-app 的全局对象
declare const uni: any

// 缓存已下载的图片
const imageCache: Record<string, string> = {}

/**
 * 预下载图片（使用 uni.downloadFile）
 * 解决 ngrok 警告页面阻止图片请求的问题
 * 
 * @param path 图片路径，例如 "/static/card.svg"
 * @returns Promise<string> 返回本地临时文件路径，失败时返回原始URL
 */
export function preloadImage(path: string): Promise<string> {
  return new Promise((resolve) => {
    if (!path) {
      resolve('')
      return
    }

    // 检查缓存
    if (imageCache[path]) {
      resolve(imageCache[path])
      return
    }

    const imageUrl = getStaticImage(path)
    if (!imageUrl) {
      resolve('')
      return
    }

    console.log('📥 开始预下载图片:', imageUrl)

    uni.downloadFile({
      url: imageUrl,
      success: (res: any) => {
        console.log('📥 downloadFile 响应:', {
          statusCode: res.statusCode,
          tempFilePath: res.tempFilePath,
          filePath: res.filePath
        })
        
        if (res.statusCode === 200) {
          // 优先使用 tempFilePath，如果没有则使用 filePath
          const localPath = res.tempFilePath || res.filePath
          if (localPath) {
            console.log('✅ 图片预下载成功，本地路径:', localPath)
            
            // 验证文件是否是图片（检查文件大小和类型）
            uni.getFileInfo({
              filePath: localPath,
              success: (fileInfo: any) => {
                console.log('📄 文件信息:', {
                  size: fileInfo.size,
                  digest: fileInfo.digest
                })
                
                // 如果文件大小太小（可能是 HTML 警告页面），使用原始 URL
                if (fileInfo.size < 100) {
                  console.warn('⚠️ 文件大小异常，可能是 HTML 警告页面，使用原始URL')
                  resolve(imageUrl)
                } else {
                  // 缓存结果
                  imageCache[path] = localPath
                  resolve(localPath)
                }
              },
              fail: (err: any) => {
                console.warn('⚠️ 无法获取文件信息，使用原始URL:', err)
                resolve(imageUrl)
              }
            })
          } else {
            console.warn('⚠️ 预下载成功但没有返回文件路径，使用原始URL')
            resolve(imageUrl)
          }
        } else {
          console.warn('⚠️ 图片预下载失败，状态码:', res.statusCode, '使用原始URL')
          // 失败时返回原始URL
          resolve(imageUrl)
        }
      },
      fail: (err: any) => {
        console.error('❌ 图片预下载失败:', err)
        console.log('错误详情:', JSON.stringify(err))
        console.log('使用原始URL:', imageUrl)
        // 失败时返回原始URL，让浏览器/小程序自己处理
        resolve(imageUrl)
      }
    })
  })
}

/**
 * 批量预下载图片
 * @param paths 图片路径数组
 * @returns Promise<Record<string, string>> 返回路径到本地文件路径的映射
 */
export async function preloadImages(paths: string[]): Promise<Record<string, string>> {
  const result: Record<string, string> = {}
  
  // 并行下载所有图片
  const promises = paths.map(async (path) => {
    const localPath = await preloadImage(path)
    result[path] = localPath
  })
  
  await Promise.all(promises)
  return result
}

/**
 * 清除图片缓存
 */
export function clearImageCache() {
  Object.keys(imageCache).forEach(key => {
    delete imageCache[key]
  })
}

