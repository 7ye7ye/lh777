import App from './App'
// Pinia 统一集成
// #ifdef VUE3
import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
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
  try {
    // 导入统一配置文件（编译后会自动处理 .ts 文件）
    const { API_CONFIG } = require('./config/api.ts')
    if (API_CONFIG) {
      // 强制使用配置文件中的值，覆盖任何旧的存储值
      if (API_CONFIG.BASE_URL) {
        uni.setStorageSync('BASE_URL', API_CONFIG.BASE_URL)
        console.log('✅ API 配置已初始化 - BASE_URL:', API_CONFIG.BASE_URL)
      }
      if (API_CONFIG.API_PREFIX) {
        uni.setStorageSync('API_PREFIX', API_CONFIG.API_PREFIX)
        console.log('✅ API 配置已初始化 - API_PREFIX:', API_CONFIG.API_PREFIX)
      }
    }
  } catch (e) {
    console.error('❌ 无法加载 API 配置文件 config/api.ts:', e)
    console.warn('⚠️ 请检查 config/api.ts 文件是否存在且配置正确')
    // 如果配置文件加载失败，使用配置文件中的默认值（不设置旧的 IP 地址）
    if (!uni.getStorageSync('BASE_URL')) {
      console.warn('⚠️ 使用默认 BASE_URL，请尽快配置 config/api.ts')
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