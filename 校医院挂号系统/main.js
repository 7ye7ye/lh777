import App from './App'
// Pinia 统一集成
// #ifdef VUE3
import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
// 导入 API 配置文件（使用别名路径，与 utils/request.ts 保持一致）
// 注意：必须使用 import 而不是 require，因为 uni-app 小程序环境不支持 require .ts 文件
import { API_CONFIG } from '@/config/api'
// #endif

// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)

  // 初始化用户状态（从本地存储恢复）
  const { useUserStore } = require('./store/user.js')
  const userStore = useUserStore()
  userStore.initFromStorage()

  // 初始化 API 配置（从统一配置文件读取）
  // 统一配置文件位置：config/api.ts（唯一配置入口）
  // 如果需要动态修改，可以在控制台调用：
  //   uni.setStorageSync('BASE_URL', 'https://your-domain.com')
  // API_CONFIG 已在文件顶部通过 import 导入（使用别名路径 @/config/api）
  if (typeof API_CONFIG !== 'undefined' && API_CONFIG) {
    // 强制使用配置文件中的值，覆盖任何旧的存储值
    if (API_CONFIG.BASE_URL) {
      uni.setStorageSync('BASE_URL', API_CONFIG.BASE_URL)
      console.log('✅ API 配置已初始化 - BASE_URL:', API_CONFIG.BASE_URL)
    }
    if (API_CONFIG.API_PREFIX) {
      uni.setStorageSync('API_PREFIX', API_CONFIG.API_PREFIX)
      console.log('✅ API 配置已初始化 - API_PREFIX:', API_CONFIG.API_PREFIX)
    }
  } else {
    console.warn('⚠️ API_CONFIG 未定义，使用默认配置')
    // 如果配置文件加载失败，使用默认值
    if (!uni.getStorageSync('BASE_URL')) {
      // 使用本地开发地址（仅模拟器可用）
      // 注意：如果需要真机调试，请使用内网穿透工具（如 cpolar），并更新 config/api.ts 中的 BASE_URL
      uni.setStorageSync('BASE_URL', 'http://localhost:8095')
      console.warn('⚠️ 使用默认 BASE_URL，请检查 config/api.ts 是否正确导入')
    }
    if (!uni.getStorageSync('API_PREFIX')) {
      uni.setStorageSync('API_PREFIX', '/jeecg-boot')
    }
  }

  return {
    app
  }
}
// #endif
