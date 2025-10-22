<template>
  <view class="page-bg">
    <!-- 头部导航栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="separator">|</text>
        <text class="home-btn" @click="goHome">🏠</text>
      </view>
      <text class="header-title">报告查询</text>
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
          <text class="title-icon">🔍</text>
          <text class="title-text">查询方式</text>
        </view>
        <view class="help-content">
          <text class="content-text">检查或检验完成后，当报告结果出来时，系统会发送消息通知您。您可以登录系统，在报告查询页面查看检验报告、检查报告等结果。报告以列表形式展示，点击对应报告即可查看详细内容。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">📖</text>
          <text class="title-text">报告解读</text>
        </view>
        <view class="help-content">
          <text class="content-text">若您对报告内容有疑问，可咨询就诊医生，医生会根据您的具体情况进行详细解读和诊断建议。系统也可能提供部分常见指标的简要说明，帮助您初步了解报告情况。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">📄</text>
          <text class="title-text">报告下载打印</text>
        </view>
        <view class="help-content">
          <text class="content-text">部分报告支持下载和打印功能，在报告详情页面找到下载或打印按钮，按照提示操作即可。您可以将报告下载保存至本地设备，或在附近的打印店进行打印，以便携带和复诊时使用。</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-actions">
      <button class="action-btn" @click="goToReport">查看报告</button>
      <button class="action-btn secondary" @click="goToCheckRecord">检查记录</button>
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

const goToReport = () => {
  uni.showModal({
    title: '报告查询',
    content: '报告查询功能开发中，敬请期待',
    showCancel: false
  })
}

const goToCheckRecord = () => {
  uni.navigateTo({ url: '/pages/profile/check-record' })
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
