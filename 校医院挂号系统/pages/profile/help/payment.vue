<template>
  <view class="page-bg">
    <!-- 头部导航栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="separator">|</text>
        <text class="home-btn" @click="goHome">🏠</text>
      </view>
      <text class="header-title">门诊缴费</text>
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
          <text class="title-text">缴费方式</text>
        </view>
        <view class="help-content">
          <text class="content-text">系统支持银行卡、校园卡等多种支付方式。在完成就诊后，系统会生成缴费订单，您可在个人中心或缴费通知中查看。点击缴费按钮，选择您已绑定的支付方式，按照页面提示完成支付操作。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">📊</text>
          <text class="title-text">缴费记录查询</text>
        </view>
        <view class="help-content">
          <text class="content-text">缴费成功后，您可以在个人中心的缴费记录中查看详细的缴费信息，包括缴费时间、金额、就诊项目等。如有疑问，可根据记录信息联系校医院财务部门或客服人员进行核实。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">⚠️</text>
          <text class="title-text">缴费异常处理</text>
        </view>
        <view class="help-content">
          <text class="content-text">若出现缴费失败或支付成功但订单未更新等异常情况，首先检查银行卡或校园卡余额是否充足。若余额充足，可能是网络延迟等原因导致，可稍后在缴费记录中查看订单状态是否更新。如长时间未解决，可通过在线反馈提供订单号、支付时间等信息，以便我们协助处理。</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-actions">
      <button class="action-btn" @click="goToPayment">前往缴费</button>
      <button class="action-btn secondary" @click="goToRecord">缴费记录</button>
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

const goToPayment = () => {
  uni.showModal({
    title: '缴费功能',
    content: '缴费功能开发中，敬请期待',
    showCancel: false
  })
}

const goToRecord = () => {
  uni.navigateTo({ url: '/pages/profile/outpatient-record' })
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
