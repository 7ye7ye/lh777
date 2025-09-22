<template>
  <view class="messages-bg">
    <view class="messages-header">
      <text class="title">消息中心</text>
      <view class="header-icons">
        <view class="header-icon">⚙️</view>
      </view>
    </view>
    <view class="content">
      <view class="empty-img">📭</view>
      <view class="empty-tip">
        请
        <text class="login-link" @click="goLogin">登录</text>
        后，查看消息通知
      </view>
    </view>
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' })
}
</script>

<style scoped>
.messages-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 120rpx;
}
.messages-header {
  background: #3a9cff;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}
.title {
  color: #fff;
  font-size: 36rpx;
  font-weight: bold;
}
.header-icons {
  display: flex;
  align-items: center;
}
.header-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
}
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 120rpx;
}
.empty-img {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 120rpx;
  background: #f0f0f0;
  border-radius: 50%;
}
.empty-tip {
  color: #666;
  font-size: 28rpx;
}
.login-link {
  color: #3a9cff;
  margin: 0 8rpx;
}
.tabbar-placeholder {
  height: 120rpx;
}
</style>


