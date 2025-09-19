<template>
  <view class="home-bg">
    <view class="home-header">
      <text class="title">北京交通大学校医院</text>
      <view class="header-icons">
        <image src="/static/top-right.png" class="header-icon" />
      </view>
    </view>
    <image class="banner" src="/static/hospital-banner.png" />
    <view class="bind-tip card">
      <text class="plus">+</text>
      <text>首次使用，请绑定就诊人</text>
    </view>
    <image class="special-banner" src="/static/special-banner.png" />
    <view class="home-section card">
      <view class="home-row">
        <view class="home-item">
          <image src="/static/register-disease.png" class="icon" />
          <text>按疾病挂号</text>
        </view>
        <view class="home-item">
          <image src="/static/register-dept.png" class="icon" />
          <text>按科室挂号</text>
        </view>
      </view>
      <view class="home-row">
        <view class="home-item">
          <image src="/static/report.png" class="icon" />
          <text>报告查询</text>
        </view>
        <view class="home-item">
          <image src="/static/online.png" class="icon" />
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
        <image src="/static/night.png" class="icon" />
        <text>晚间门诊</text>
      </view>
      <view class="home-item">
        <image src="/static/weekend.png" class="icon" />
        <text>周末门诊</text>
      </view>
      <view class="home-item">
        <image src="/static/sign.png" class="icon" />
        <text>门诊签到</text>
      </view>
      <view class="home-item">
        <image src="/static/psychology.png" class="icon" />
        <text>心理筛查门诊</text>
      </view>
    </view>
    <view class="home-row">
      <view class="home-item">
        <image src="/static/appoint.png" class="icon" />
        <text>预约门诊</text>
      </view>
      <view class="home-item">
        <image src="/static/add-patient.png" class="icon" />
        <text>添加就诊人</text>
      </view>
      <view class="home-item">
        <image src="/static/pay.png" class="icon" />
        <text>缴费</text>
      </view>
      <view class="home-item">
        <image src="/static/clock.png" class="icon" />
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
}
.banner {
  width: 100%;
  height: 180rpx;
  object-fit: cover;
  margin-bottom: 16rpx;
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
