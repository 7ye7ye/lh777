<template>
  <!-- 内联提示模式：未登录时才显示 -->
  <view v-if="mode === 'inline' && !isLoggedIn" class="login-inline">
    <text class="login-msg">{{ message }}</text>
    <button class="login-btn" size="mini" @click="goLogin">{{ loginText }}</button>
  </view>
</template>

<script setup>
import { computed, defineExpose } from 'vue'
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
  background: #fff7e6;
  border: 1rpx solid #ffe7ba;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.login-msg { color: #ad6800; font-size: 26rpx; }
.login-btn { background: #3a9cff; color: #fff; border-radius: 999rpx; padding: 0 24rpx; }
</style>


