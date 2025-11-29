import http from '../utils/request'

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
