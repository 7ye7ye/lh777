<template>
  <view class="home-bg">
    <view class="home-header">
      <text class="title">北京交通大学校医院</text>
      <view class="header-icons">
        <view class="header-icon">⚙️</view>
      </view>
    </view>
    <view class="banner">🏥</view>
    <view class="bind-tip card">
      <text class="plus">+</text>
      <text>首次使用，请绑定就诊人</text>
    </view>
    <view class="special-banner">⭐</view>
    <view class="home-section card">
      <view class="home-row">
        <view class="home-item">
          <view class="icon">🩺</view>
          <text>按疾病挂号</text>
        </view>
        <view class="home-item">
          <view class="icon">🏥</view>
          <text>按科室挂号</text>
        </view>
      </view>
      <view class="home-row">
        <view class="home-item">
          <view class="icon">📊</view>
          <text>报告查询</text>
        </view>
        <view class="home-item">
          <view class="icon">💻</view>
          <text>互联网诊疗</text>
        </view>
      </view>
    </view>
    <view class="home-tabs card">
      <view class="tab active">门诊</view>
      <view class="tab">住院</view>
      <view class="tab">体检</view>
      <view class="tab">其他</view>
    </view>
    <view class="home-row">
      <view class="home-item">
        <view class="icon">🌙</view>
        <text>晚间门诊</text>
      </view>
      <view class="home-item">
        <view class="icon">📅</view>
        <text>周末门诊</text>
      </view>
      <view class="home-item">
        <view class="icon">✅</view>
        <text>门诊签到</text>
      </view>
      <view class="home-item">
        <view class="icon">🧠</view>
        <text>心理筛查门诊</text>
      </view>
    </view>
    <view class="home-row">
      <view class="home-item">
        <view class="icon">📅</view>
        <text>预约门诊</text>
      </view>
      <view class="home-item">
        <view class="icon">👥</view>
        <text>添加就诊人</text>
      </view>
      <view class="home-item">
        <view class="icon">💰</view>
        <text>缴费</text>
      </view>
      <view class="home-item">
        <view class="icon">⏰</view>
        <text>预约提醒</text>
      </view>
    </view>
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
// 可根据需要添加交互逻辑
</script>

<style scoped>
.home-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 120rpx;
}
.home-header {
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
.banner {
  width: 100%;
  height: 180rpx;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80rpx;
  background: #f0f0f0;
  border-radius: 12rpx;
}
.bind-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(90deg, #3a9cff 0%, #1de9b6 100%);
  color: #fff;
  font-size: 28rpx;
  border-radius: 16rpx;
  margin: 16rpx 24rpx 0 24rpx;
  padding: 24rpx 0;
  font-weight: bold;
}
.plus {
  font-size: 36rpx;
  margin-right: 16rpx;
}
.special-banner {
  width: 92%;
  margin: 24rpx 4% 0 4%;
  border-radius: 16rpx;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60rpx;
  background: #f0f0f0;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx 24rpx 0 24rpx;
  padding: 24rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}
.home-section {
  margin-top: 24rpx;
}
.home-row {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  margin: 0 0 16rpx 0;
}
.home-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 16rpx 0;
}
.icon {
  width: 56rpx;
  height: 56rpx;
  margin-bottom: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}
.home-tabs {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx 24rpx 0 24rpx;
  overflow: hidden;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #888;
}
.tab.active {
  color: #3a9cff;
  font-weight: bold;
  border-bottom: 4rpx solid #3a9cff;
  background: #f0f8ff;
}
.tabbar-placeholder {
  height: 120rpx;
}
</style>
