<template>
  <view class="page-bg">
    <!-- 头部导航栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="separator">|</text>
        <text class="home-btn" @click="goHome">🏠</text>
      </view>
      <text class="header-title">其他</text>
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
          <text class="title-icon">⚙️</text>
          <text class="title-text">系统功能介绍</text>
        </view>
        <view class="help-content">
          <text class="content-text">除上述主要功能外，系统还提供一些辅助功能，如医院科室介绍、医生信息查询、健康科普知识等。您可以在系统首页或相关栏目中浏览查看，了解更多校医院的服务信息和健康知识。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">💬</text>
          <text class="title-text">意见反馈</text>
        </view>
        <view class="help-content">
          <text class="content-text">如果您在使用系统过程中有任何意见、建议或遇到问题，欢迎通过在线反馈功能进行提交。您可以详细描述问题情况、提出改进建议等，我们会及时查看并处理您的反馈，努力提升系统的服务质量和用户体验。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">🕒</text>
          <text class="title-text">服务时间</text>
        </view>
        <view class="help-content">
          <text class="content-text">校医院客服人员的在线服务时间为工作日 8:00-17:00，您可以在此期间通过在线反馈或联系电话进行咨询和问题反馈。非服务时间内提交的反馈，我们会在工作日上班后及时处理。</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-actions">
      <button class="action-btn" @click="goToFeedback">意见反馈</button>
      <button class="action-btn secondary" @click="goToContact">联系我们</button>
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

const goToFeedback = () => {
  uni.navigateTo({ url: '/pages/profile/complain' })
}

const goToContact = () => {
  uni.showModal({
    title: '联系我们',
    content: '客服电话：010-51688120\n服务时间：工作日 8:00-17:00\n邮箱：hospital@bjtu.edu.cn',
    showCancel: false
  })
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
