<template>
  <view class="page-bg">
    <!-- 头部导航栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="separator">|</text>
        <text class="home-btn" @click="goHome">🏠</text>
      </view>
      <text class="header-title">预约挂号</text>
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
          <text class="title-icon">📅</text>
          <text class="title-text">预约规则</text>
        </view>
        <view class="help-content">
          <text class="content-text">您可以提前7天在系统中预约挂号，不同科室和医生的可预约时间段可能有所不同，请以系统显示为准。每个账号在一定时间内预约同一科室或医生的次数可能有限制，具体限制规则可在系统中查看或咨询客服人员。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">📱</text>
          <text class="title-text">预约成功通知</text>
        </view>
        <view class="help-content">
          <text class="content-text">预约成功后，系统会通过短信、系统消息等方式通知您。通知内容包括就诊科室、医生姓名、就诊日期和时间段等重要信息，请您留意查收并按时前往就诊。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">❌</text>
          <text class="title-text">爽约处理</text>
        </view>
        <view class="help-content">
          <text class="content-text">如果您未能按时就诊且未在规定时间内取消预约挂号，将被视为爽约。累计爽约达到一定次数，可能会影响您后续在本系统的预约挂号功能，请合理安排就诊计划，如不能按时就诊，请及时取消预约。</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-actions">
      <button class="action-btn" @click="goToAppointment">预约挂号</button>
      <button class="action-btn secondary" @click="goToRecord">预约记录</button>
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

const goToAppointment = () => {
  uni.showModal({
    title: '预约挂号',
    content: '预约挂号功能开发中，敬请期待',
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
