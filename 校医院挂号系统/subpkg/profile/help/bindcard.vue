<template>
  <view class="page-bg">
    <!-- 头部导航栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="separator">|</text>
        <text class="home-btn" @click="goHome">🏠</text>
      </view>
      <text class="header-title">绑卡</text>
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
          <text class="title-icon">💳</text>
          <text class="title-text">为什么要绑卡</text>
        </view>
        <view class="help-content">
          <text class="content-text">绑定银行卡或校园卡可用于门诊缴费等操作，方便快捷地完成费用支付，提升就医体验。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">🔗</text>
          <text class="title-text">如何绑卡</text>
        </view>
        <view class="help-content">
          <text class="content-text">登录系统后，进入个人中心或账户管理页面，找到绑卡选项。选择您要绑定的卡类型（银行卡或校园卡），按照页面提示填写卡号、持卡人姓名、预留手机号等信息，完成绑定。部分银行卡可能需要通过短信验证码进行验证，请留意手机短信。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">🔓</text>
          <text class="title-text">解绑卡操作</text>
        </view>
        <view class="help-content">
          <text class="content-text">如需解绑已绑定的卡，在绑卡页面找到对应卡片，点击解绑选项。系统会再次向您确认是否解绑，确认后即可完成操作。解绑后，该卡将无法用于本系统的缴费等相关功能。</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-actions">
      <button class="action-btn" @click="goToBindCard">前往绑卡</button>
      <button class="action-btn secondary" @click="goToMyCard">我的卡片</button>
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

const goToBindCard = () => {
  uni.showModal({
    title: '绑卡功能',
    content: '绑卡功能开发中，敬请期待',
    showCancel: false
  })
}

const goToMyCard = () => {
  uni.navigateTo({ url: '/pages/profile/mycard' })
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
