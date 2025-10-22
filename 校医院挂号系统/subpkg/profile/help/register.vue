<template>
  <view class="page-bg">
    <!-- 头部导航栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="separator">|</text>
        <text class="home-btn" @click="goHome">🏠</text>
      </view>
      <text class="header-title">挂号问题</text>
      <view class="header-right">
        <text class="more-btn">⋯</text>
        <text class="separator">|</text>
        <text class="minimize-btn">−</text>
        <text class="separator">|</text>
        <text class="focus-btn">◎</text>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="content-section">
      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">📋</text>
          <text class="title-text">挂号流程</text>
        </view>
        <view class="help-content">
          <text class="content-text">登录系统后，进入挂号页面。选择就诊科室、医生、就诊日期和时间段，确认信息无误后点击挂号按钮。系统会提示挂号是否成功，若成功，您可在个人中心的挂号记录中查看详细信息。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">🔍</text>
          <text class="title-text">号源查询</text>
        </view>
        <view class="help-content">
          <text class="content-text">在挂号页面可查看各科室的号源情况，包括剩余号数、可预约时间段等。您可以根据自己的时间和需求选择合适的号源进行预约。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">❌</text>
          <text class="title-text">取消挂号</text>
        </view>
        <view class="help-content">
          <text class="content-text">如需取消已预约的挂号，在个人中心的挂号记录中找到对应挂号信息，点击取消挂号按钮。注意，部分挂号可能有取消时限要求，超过规定时间可能无法取消，请合理安排就诊计划。</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-actions">
      <button class="action-btn" @click="goToRegister">前往挂号</button>
      <button class="action-btn secondary" @click="goToRecord">挂号记录</button>
    </view>
  </view>
</template>

<script setup>
const goBack = () => {
  uni.navigateBack()
}

const goHome = () => {
  uni.reLaunch({ url: '/pages/home/home' })
}

const goToRegister = () => {
  uni.showModal({
    title: '挂号功能',
    content: '挂号功能开发中，敬请期待',
    showCancel: false
  })
}

const goToRecord = () => {
  uni.navigateTo({ url: '/pages/profile/register-record' })
}
</script>

<style scoped>
.page-bg {
  min-height: 100vh;
  background: #f8faff;
}

/* 头部导航栏 */
.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #3a9cff;
  padding: 20rpx 24rpx;
  color: #fff;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
}

.back-btn, .home-btn, .more-btn, .minimize-btn, .focus-btn {
  font-size: 32rpx;
  padding: 8rpx;
}

.separator {
  font-size: 24rpx;
  margin: 0 8rpx;
  opacity: 0.7;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
}

/* 内容区域 */
.content-section {
  padding: 24rpx;
}

.help-item {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.help-title {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.title-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.title-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.help-content {
  padding-left: 44rpx;
}

.content-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

/* 底部操作 */
.bottom-actions {
  padding: 24rpx;
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  background: #3a9cff;
  color: #fff;
  border: none;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 28rpx;
  font-weight: bold;
}

.action-btn.secondary {
  background: #fff;
  color: #3a9cff;
  border: 1px solid #3a9cff;
}
</style>
