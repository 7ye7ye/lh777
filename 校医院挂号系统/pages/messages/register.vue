<template>
  <view class="page">
    <view class="card">
      <view class="title">注册入口</view>
      <view class="desc">正在跳转到注册页，如未自动跳转请点击下方按钮。</view>
      <button class="btn" @click="goRegister">前往注册</button>
    </view>
  </view>
</template>

<script setup>
import { onMounted } from 'vue'
import { uniNavigateTo } from '@/utils/uniHelper'

const goRegister = () => {
  uniNavigateTo({ url: '/subpkg/auth/register' })
}

onMounted(() => {
  // 自动跳转
  goRegister()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f7faff; display: flex; align-items: center; justify-content: center; }
.card { width: 86%; background: #fff; border-radius: 16rpx; padding: 32rpx; box-shadow: 0 6rpx 16rpx rgba(0,0,0,0.08); }
.title { font-size: 32rpx; font-weight: 700; margin-bottom: 12rpx; color: #1e293b; }
.desc { font-size: 26rpx; color: #64748b; margin-bottom: 20rpx; }
.btn { width: 100%; background: #2176ff; color: #fff; border-radius: 12rpx; padding: 16rpx; font-size: 28rpx; }
</style>