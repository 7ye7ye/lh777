<template>
  <view class="login-page">
    <!-- 顶部装饰区域 -->
    <view class="header">
      <view class="logo-container">
        <view class="logo-circle">
          <text class="logo-icon">🏥</text>
        </view>
      </view>
      <view class="title">校医院挂号系统</view>
      <view class="subtitle">安全便捷的医疗服务</view>
    </view>

    <!-- 登录卡片 -->
    <view class="card">
      <view class="card-title">欢迎登录</view>
      <view class="card-desc">请输入您的账号信息</view>

      <!-- 账号输入框 -->
      <view class="input-group" :class="{ 'input-focus': accountFocus }">
        <view class="input-icon">👤</view>
        <view class="input-wrapper">
          <view class="input-label">账号</view>
          <input
            class="input"
            v-model.trim="form.userAccount"
            placeholder="请输入学号/工号/手机号"
            type="text"
            confirm-type="next"
            @focus="accountFocus = true"
            @blur="accountFocus = false"
            @confirm="onSubmit"
          />
        </view>
      </view>

      <!-- 密码输入框 -->
      <view class="input-group" :class="{ 'input-focus': passwordFocus }">
        <view class="input-icon">🔒</view>
        <view class="input-wrapper">
          <view class="input-label">密码</view>
          <input
            class="input"
            v-model.trim="form.userPassword"
            placeholder="请输入密码"
            :password="!showPassword"
            type="text"
            confirm-type="go"
            @focus="passwordFocus = true"
            @blur="passwordFocus = false"
            @confirm="onSubmit"
          />
        </view>
        <view class="password-toggle" @click="togglePassword">
          <text class="toggle-icon">{{ showPassword ? '👁️' : '👁️‍🗨️' }}</text>
        </view>
      </view>

      <!-- 登录按钮 -->
      <button
        class="btn"
        :class="{ 'btn-disabled': disabled, 'btn-loading': loading }"
        :loading="loading"
        :disabled="disabled"
        @click="onSubmit"
      >
        <text v-if="!loading">立即登录</text>
        <text v-else>登录中...</text>
      </button>

      <!-- 底部链接 -->
      <view class="footer-links">
        <view class="link-item" @click="goRegister">
          <text class="link-text">没有账号？</text>
          <text class="link-highlight">立即注册</text>
        </view>
        <view class="divider">|</view>
        <view class="link-item" @click="showForgotPassword">
          <text class="link-text">忘记密码</text>
        </view>
      </view>

      <!-- 提示信息 -->
      <view class="tips">
        <text class="tips-icon">💡</text>
        <text class="tips-text">忘记密码请联系管理员重置</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { userApi } from '@/api/user'
import { uniShowToast, uniSwitchTab, uniNavigateTo } from '@/utils/uniHelper'

const form = ref({
  userAccount: '',
  userPassword: ''
})

const loading = ref(false)
const disabled = computed(() => !form.value.userAccount || !form.value.userPassword || loading.value)
const accountFocus = ref(false)
const passwordFocus = ref(false)
const showPassword = ref(false)

const userStore = useUserStore()

// 切换密码显示/隐藏
const togglePassword = () => {
  showPassword.value = !showPassword.value
}

// 显示忘记密码提示
const showForgotPassword = () => {
  uniShowToast({
    title: '请联系管理员重置密码',
    icon: 'none',
    duration: 2000
  })
}

const onSubmit = async () => {
  if (disabled.value) return
  loading.value = true
  try {
    const res = await userApi.login({ userAccount: form.value.userAccount, userPassword: form.value.userPassword })
    console.log('登录接口返回数据:', res)
    
    // 根据后端返回结构提取数据
    // 后端返回格式: { code: 0, data: { user: {...}, token: "..." } }
    // 响应拦截器会解包 data 字段，所以 res 就是 { user: {...}, token: "..." }
    const token = res?.token || ''
    const userInfo = res?.user || null
    
    if (!userInfo) {
      await uniShowToast({ title: '登录失败：未获取到用户信息', icon: 'none' })
      return
    }
    
    // 检查用户类型
    if (userInfo.userType == 3) {
      await uniShowToast({ title: '登录失败，请使用患者或医生账号登录', icon: 'none' })
      return
    }
    
    if (!token) {
      await uniShowToast({ title: '登录失败：未获取到token', icon: 'none' })
      return
    }
    
    // 保存 token 和用户信息到状态管理
    userStore.setToken(token)
    userStore.setUserInfo(userInfo)
    
    await uniShowToast({ title: '登录成功' })
    
    // 根据用户类型跳转
    if (userInfo.userType == 1) {
      await uniSwitchTab({ url: '/pages/home/home' })
    } else if (userInfo.userType == 2) {
      // 医生端直接重启到排班首页，避免出现返回上一页按钮
      await uni.reLaunch({ url: '/subpkg/doctor/schedule/main' })
    } else {
      // 默认跳转到首页
      await uniSwitchTab({ url: '/pages/home/home' })
    }
  } catch (e) {
    console.error('登录失败:', e)
    // 错误信息已经在响应拦截器中显示过了，这里不需要再次显示
    // 但如果响应拦截器没有处理，这里作为兜底
    if (e && e.message) {
      // 如果错误信息还没有显示过，才显示
      // 由于响应拦截器已经显示了 toast，这里可以不再显示
    }
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  uniNavigateTo({ url: '/subpkg/auth/register' })
}

</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #479fff 0%, #5db7ff 35%, #f5f7fa 35%, #f5f7fa 100%);
  position: relative;
  overflow: hidden;
}

/* 顶部装饰区域 */
.header {
  padding: 120rpx 40rpx 80rpx 40rpx;
  color: #fff;
  text-align: center;
  position: relative;
  z-index: 1;
}

.logo-container {
  margin-bottom: 30rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-circle {
  width: 120rpx;
  height: 120rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10rpx);
  }
}

.logo-icon {
  font-size: 64rpx;
  line-height: 1;
}

.title {
  font-size: 44rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.subtitle {
  opacity: 0.95;
  font-size: 28rpx;
  font-weight: 400;
  letter-spacing: 0.5rpx;
}

/* 登录卡片 */
.card {
  margin: -60rpx 32rpx 40rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 12rpx 40rpx rgba(71, 159, 255, 0.15);
  padding: 48rpx 32rpx 40rpx 32rpx;
  position: relative;
  z-index: 2;
}

.card-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8rpx;
  text-align: center;
}

.card-desc {
  font-size: 26rpx;
  color: #999;
  text-align: center;
  margin-bottom: 40rpx;
}

/* 输入框组 */
.input-group {
  margin-bottom: 32rpx;
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border: 2rpx solid #e9ecef;
  border-radius: 16rpx;
  padding: 0 24rpx;
  transition: all 0.3s ease;
  position: relative;
}

.input-group.input-focus {
  border-color: #479fff;
  background: #fff;
  box-shadow: 0 0 0 4rpx rgba(71, 159, 255, 0.1);
}

.input-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
  opacity: 0.6;
}

.input-wrapper {
  flex: 1;
  padding: 20rpx 0;
}

.input-label {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 8rpx;
  font-weight: 500;
}

.input {
  width: 100%;
  height: 48rpx;
  font-size: 28rpx;
  color: #1a1a1a;
  background: transparent;
  border: none;
  padding: 0;
  line-height: 48rpx;
}

.input::placeholder {
  color: #bbb;
  font-size: 26rpx;
}

.password-toggle {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 12rpx;
  flex-shrink: 0;
  cursor: pointer;
}

.toggle-icon {
  font-size: 36rpx;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.password-toggle:active .toggle-icon {
  opacity: 1;
}

/* 登录按钮 */
.btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #479fff 0%, #5db7ff 100%);
  border: none;
  border-radius: 16rpx;
  margin-top: 20rpx;
  box-shadow: 0 8rpx 20rpx rgba(71, 159, 255, 0.3);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.btn:active::before {
  left: 100%;
}

.btn:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(71, 159, 255, 0.25);
}

.btn-disabled {
  background: #d0d7de;
  box-shadow: none;
  opacity: 0.6;
}

.btn-disabled:active {
  transform: none;
}

.btn-loading {
  opacity: 0.8;
}

/* 底部链接 */
.footer-links {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 32rpx;
  gap: 16rpx;
}

.link-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: opacity 0.2s;
}

.link-item:active {
  opacity: 0.7;
}

.link-text {
  font-size: 26rpx;
  color: #666;
}

.link-highlight {
  font-size: 26rpx;
  color: #479fff;
  font-weight: 500;
  margin-left: 4rpx;
}

.divider {
  font-size: 24rpx;
  color: #ddd;
  margin: 0 8rpx;
}

/* 提示信息 */
.tips {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 32rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.tips-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
  opacity: 0.6;
}

.tips-text {
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
}
</style>