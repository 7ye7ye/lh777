<template>
  <view class="page-bg">
    <!-- 头部导航栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="separator">|</text>
        <text class="home-btn" @click="goHome">🏠</text>
      </view>
      <text class="header-title">登录注册</text>
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
          <text class="title-icon">📝</text>
          <text class="title-text">如何注册账号</text>
        </view>
        <view class="help-content">
          <text class="content-text">打开北京交通大学校医院挂号系统，点击注册按钮。按照页面提示填写个人信息，包括姓名、学号/教职工号、身份证号、联系电话等必填项。填写完成后，设置登录密码，点击提交完成注册。请确保所填信息真实有效，以便顺利使用系统功能。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">🔑</text>
          <text class="title-text">忘记登录密码怎么办</text>
        </view>
        <view class="help-content">
          <text class="content-text">在登录页面点击"忘记密码"选项。按照系统提示，通过注册时预留的手机号或邮箱进行身份验证。验证通过后，设置新的登录密码即可重新登录系统。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">⚠️</text>
          <text class="title-text">登录异常处理</text>
        </view>
        <view class="help-content">
          <text class="content-text">若出现无法登录的情况，首先检查输入的账号和密码是否正确，注意区分大小写。若确认无误仍无法登录，可能是网络问题，请检查网络连接后重试。如问题依旧，可通过在线反馈或联系校医院客服人员，提供您的账号信息及详细问题描述，以便我们协助解决。</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-actions">
      <button class="action-btn" @click="goToLogin">前往登录</button>
      <button class="action-btn secondary" @click="goToRegister">前往注册</button>
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

const goToLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' })
}

const goToRegister = () => {
  uni.navigateTo({ url: '/pages/login/register' })
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
