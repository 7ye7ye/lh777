<template>
  <view class="register-page">
    <!-- 顶部装饰区域 -->
    <view class="header">
      <view class="logo-container">
        <view class="logo-circle">
          <text class="logo-icon">📝</text>
        </view>
      </view>
      <view class="title">注册账号</view>
      <view class="subtitle">加入校医院挂号系统</view>
    </view>

    <!-- 注册卡片 -->
    <view class="card">
      <view class="card-title">创建账号</view>
      <view class="card-desc">请填写以下信息完成注册</view>

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
          />
        </view>
      </view>

      <!-- 密码输入框 -->
      <view class="input-group" :class="{ 'input-focus': passwordFocus, 'input-error': passwordTooShort && form.userPassword }">
        <view class="input-icon">🔒</view>
        <view class="input-wrapper">
          <view class="input-label">密码</view>
          <input
            class="input"
            v-model.trim="form.userPassword"
            placeholder="请输入密码（至少8位）"
            :password="!showPassword"
            type="text"
            confirm-type="next"
            @focus="passwordFocus = true"
            @blur="passwordFocus = false"
          />
        </view>
        <view class="password-toggle" @click="togglePassword">
          <text class="toggle-icon">{{ showPassword ? '👁️' : '👁️‍🗨️' }}</text>
        </view>
      </view>

      <!-- 密码长度提示 -->
      <view v-if="passwordTooShort && form.userPassword" class="error-tip">
        <text class="error-icon">⚠️</text>
        <text class="error-text">密码长度至少8位</text>
      </view>

      <!-- 确认密码输入框 -->
      <view class="input-group" :class="{ 'input-focus': confirmPasswordFocus, 'input-error': passwordMismatch && form.checkPassword }">
        <view class="input-icon">🔐</view>
        <view class="input-wrapper">
          <view class="input-label">确认密码</view>
          <input
            class="input"
            v-model.trim="form.checkPassword"
            placeholder="请再次输入密码"
            :password="!showConfirmPassword"
            type="text"
            confirm-type="done"
            @focus="confirmPasswordFocus = true"
            @blur="confirmPasswordFocus = false"
            @confirm="onSubmit"
          />
        </view>
        <view class="password-toggle" @click="toggleConfirmPassword">
          <text class="toggle-icon">{{ showConfirmPassword ? '👁️' : '👁️‍🗨️' }}</text>
        </view>
      </view>

      <!-- 密码提示 -->
      <view v-if="passwordMismatch && form.checkPassword" class="error-tip">
        <text class="error-icon">⚠️</text>
        <text class="error-text">两次输入的密码不一致</text>
      </view>

      <!-- 注册按钮 -->
      <button
        class="btn"
        :class="{ 'btn-disabled': disabled, 'btn-loading': loading }"
        :loading="loading"
        :disabled="disabled"
        @click="onSubmit"
      >
        <text v-if="!loading">立即注册</text>
        <text v-else>注册中...</text>
      </button>

      <!-- 底部链接 -->
      <view class="footer-links">
        <view class="link-item" @click="goLogin">
          <text class="link-text">已有账号？</text>
          <text class="link-highlight">立即登录</text>
        </view>
      </view>

      <!-- 提示信息 -->
      <view class="tips">
        <text class="tips-icon">💡</text>
        <text class="tips-text">注册即表示您同意使用本系统服务</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { userApi } from '@/api/user'
import { uniShowToast, uniNavigateTo } from '@/utils/uniHelper'

const form = ref({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const loading = ref(false)
const accountFocus = ref(false)
const passwordFocus = ref(false)
const confirmPasswordFocus = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

// 密码长度检查
const passwordTooShort = computed(() => {
  return form.value.userPassword && form.value.userPassword.length < 8
})

// 密码不匹配检查
const passwordMismatch = computed(() => {
  return form.value.checkPassword && form.value.userPassword !== form.value.checkPassword
})

const disabled = computed(() => {
  return !form.value.userAccount || 
         !form.value.userPassword || 
         !form.value.checkPassword || 
         passwordTooShort.value ||
         passwordMismatch.value ||
         loading.value
})

// 切换密码显示/隐藏
const togglePassword = () => {
  showPassword.value = !showPassword.value
}

// 切换确认密码显示/隐藏
const toggleConfirmPassword = () => {
  showConfirmPassword.value = !showConfirmPassword.value
}

const onSubmit = async () => {
  if (disabled.value) return
  
  // 验证密码长度
  if (form.value.userPassword.length < 8) {
    await uniShowToast({ 
      title: '密码长度至少8位', 
      icon: 'none',
      duration: 2000
    })
    return
  }
  
  // 验证密码是否一致
  if (form.value.userPassword !== form.value.checkPassword) {
    await uniShowToast({ 
      title: '两次输入的密码不一致', 
      icon: 'none',
      duration: 2000
    })
    return
  }
  
  loading.value = true
  try {
    // 后端期望字段：{ userAccount, userPassword, checkPassword, userType }
    // userType为1映射为患者用户
    const res = await userApi.register({ 
      userAccount: form.value.userAccount, 
      userPassword: form.value.userPassword, 
      checkPassword: form.value.checkPassword, 
      userType: 1
    })
    console.log('注册成功:', res)
    
    await uniShowToast({ 
      title: '注册成功', 
      icon: 'success', 
      duration: 1500 
    })
    
    // 使用setTimeout确保toast显示完成后再跳转
    setTimeout(() => {
      uniNavigateTo({ url: '/subpkg/auth/login' })
    }, 1500)
  } catch (e) {
    console.error('注册失败:', e)
    await uniShowToast({ 
      title: (e && e.message) || '注册失败，请重试', 
      icon: 'none',
      duration: 2000
    })
  } finally {
    loading.value = false
  }
}

const goLogin = () => {
  uniNavigateTo({ url: '/subpkg/auth/login' })
}
</script>

<style scoped>
.register-page {
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

/* 注册卡片 */
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

.input-group.input-error {
  border-color: #ff4d4f;
  background: #fff;
  box-shadow: 0 0 0 4rpx rgba(255, 77, 79, 0.1);
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

/* 错误提示 */
.error-tip {
  display: flex;
  align-items: center;
  margin-top: -24rpx;
  margin-bottom: 16rpx;
  padding: 8rpx 16rpx;
  background: #fff2f0;
  border-radius: 8rpx;
  border: 1rpx solid #ffccc7;
}

.error-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.error-text {
  font-size: 24rpx;
  color: #ff4d4f;
  line-height: 1.5;
}

/* 注册按钮 */
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


