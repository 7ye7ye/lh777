<template>
  <!-- 内联提示模式：未登录时才显示 -->
  <view v-if="mode === 'inline' && !isLoggedIn" class="login-inline">
    <view class="login-left">
      <image class="login-icon" src="/static/profile.svg" mode="aspectFit" />
      <view class="login-text-wrap">
        <text class="login-title">未登录</text>
        <text class="login-msg">{{ message }}</text>
      </view>
    </view>
    <view class="login-right">
      <button class="login-btn" @click="goLogin">{{ loginText }}</button>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const props = defineProps({
  mode: { type: String, default: 'inline' }, // inline | modal
  message: { type: String, default: '当前功能需要登录后使用' },
  loginText: { type: String, default: '去登录' }
})

const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.isLoggedIn)

const goLogin = () => {
  uni.navigateTo({ url: '/subpkg/auth/login' })
}

// 供外部在需要时以弹窗方式触发
const open = (customMessage) => {
  if (isLoggedIn.value) return
  const content = customMessage || props.message
  uni.showModal({
    title: '温馨提示',
    content,
    confirmText: props.loginText,
    success: (res) => {
      if (res.confirm) {
        goLogin()
      }
    }
  })
}

defineExpose({ open })
</script>

<style scoped>
.login-inline {
  margin: 16rpx 24rpx 0 24rpx;
  padding: 20rpx 24rpx;
  background: #f0f7ff;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 8rpx 24rpx rgba(58, 156, 255, 0.12);
}

.login-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.login-icon {
  width: 56rpx;
  height: 56rpx;
  margin-right: 16rpx;
}

.login-text-wrap {
  display: flex;
  flex-direction: column;
}

.login-title {
  font-size: 26rpx;
  color: #1d39c4;
  font-weight: 600;
  margin-bottom: 4rpx;
}

.login-msg {
  color: #5972a7;
  font-size: 24rpx;
}

.login-right {
  margin-left: 16rpx;
}

.login-btn {
  padding: 0 32rpx;
  height: 64rpx;
  line-height: 64rpx;
  font-size: 26rpx;
  color: #fff;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #3a9cff 0%, #1de9b6 100%);
  box-shadow: 0 6rpx 16rpx rgba(58, 156, 255, 0.35);
}

.login-btn::after {
  border: none;
}
</style>


