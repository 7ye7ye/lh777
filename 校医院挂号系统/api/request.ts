// 统一请求工具
import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { showToast } from 'uni-app'

// 创建axios实例
const request = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || '/api',
  timeout: 15000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    // 添加token到请求头
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    
    // 添加医院ID等公共参数
    if (config.method?.toUpperCase() === 'GET') {
      config.params = {
        ...config.params,
        timestamp: Date.now()
      }
    } else {
      config.data = {
        ...config.data
      }
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data } = response
    
    // 统一处理业务逻辑错误
    if (data.code !== 200) {
      // 401未授权，跳转到登录页
      if (data.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        uni.redirectTo({ url: '/pages/login/index' })
      }
      
      // 显示错误信息
      showToast({
        title: data.message || '请求失败',
        icon: 'none',
        duration: 3000
      })
      
      return Promise.reject(data)
    }
    
    return data
  },
  (error) => {
    // 处理网络错误
    let errorMessage = '网络请求失败'
    
    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 400:
          errorMessage = '请求参数错误'
          break
        case 401:
          errorMessage = '未授权，请重新登录'
          const userStore = useUserStore()
          userStore.logout()
          uni.redirectTo({ url: '/pages/login/index' })
          break
        case 403:
          errorMessage = '拒绝访问'
          break
        case 404:
          errorMessage = '请求的资源不存在'
          break
        case 500:
          errorMessage = '服务器内部错误'
          break
        default:
          errorMessage = `请求失败(${status})`
      }
    } else if (error.request) {
      errorMessage = '网络连接失败，请检查网络设置'
    }
    
    showToast({
      title: errorMessage,
      icon: 'none',
      duration: 3000
    })
    
    return Promise.reject(error)
  }
)

export default request