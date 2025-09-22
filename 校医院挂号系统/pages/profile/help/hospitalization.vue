<template>
  <view class="page-bg">
    <!-- 头部导航栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="separator">|</text>
        <text class="home-btn" @click="goHome">🏠</text>
      </view>
      <text class="header-title">住院</text>
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
          <text class="title-icon">🏥</text>
          <text class="title-text">住院登记</text>
        </view>
        <view class="help-content">
          <text class="content-text">如需住院治疗，在医生开具住院单后，持相关证件和单据前往校医院住院处办理住院登记手续。也可在系统中找到住院登记选项，按照提示填写个人信息、住院科室、入院日期等信息，提交后等待医院审核确认。审核通过后，您会收到系统通知，按照通知要求前往住院处办理后续手续。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">💰</text>
          <text class="title-text">住院费用查询</text>
        </view>
        <view class="help-content">
          <text class="content-text">住院期间，您可以在系统个人中心的住院费用记录中查看每日费用明细，包括药品费用、检查费用、床位费用等。如有疑问，可向住院处或科室护士咨询。</text>
        </view>
      </view>

      <view class="help-item">
        <view class="help-title">
          <text class="title-icon">📋</text>
          <text class="title-text">出院结算</text>
        </view>
        <view class="help-content">
          <text class="content-text">出院时，前往住院处办理出院结算手续。系统会自动计算住院期间的总费用，您可选择已绑定的支付方式进行结算。结算完成后，医院会提供结算清单，您也可在系统中查看出院结算记录。</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-actions">
      <button class="action-btn" @click="goToHospitalization">住院登记</button>
      <button class="action-btn secondary" @click="goToRecord">住院记录</button>
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

const goToHospitalization = () => {
  uni.showModal({
    title: '住院登记',
    content: '住院登记功能开发中，敬请期待',
    showCancel: false
  })
}

const goToRecord = () => {
  uni.navigateTo({ url: '/pages/profile/hospital-record' })
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
