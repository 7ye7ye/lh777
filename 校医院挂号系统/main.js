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

  // 强制设置后端地址与前缀（开发联调）
  try {
    uni.setStorageSync('BASE_URL', 'http://127.0.0.1:8095');
    uni.setStorageSync('API_PREFIX', '/jeecg-boot');
  } catch (e) {
    console.warn('设置 BASE_URL/API_PREFIX 失败:', e);
  }

  return {
    app
  }
}
// #endif