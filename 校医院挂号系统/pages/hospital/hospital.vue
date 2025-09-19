<template>
  <view class="hospital-bg">
    <view class="hospital-header">
      <image class="hospital-banner" src="/static/hospital-banner.png" />
      <view class="hospital-info">
        <image class="hospital-logo" src="/static/hospital-logo.png" />
        <view class="hospital-title">
          <text class="name">北京交通大学校医院</text>
          <view class="tags">
            <text class="tag">三级甲等</text>
            <text class="tag">综合</text>
          </view>
        </view>
      </view>
    </view>
    <view class="hospital-tabs">
      <view class="tab active">主区院区</view>
      <view class="tab">东区院区</view>
      <view class="tab">威海院区</view>
    </view>
    <view class="hospital-address">
      北京市海淀区西直门外上园村5号
      <image class="location-icon" src="/static/location.png" />
    </view>
    <view class="hospital-section card">
      <view class="hospital-row">
        <view class="hospital-item">
          <image src="/static/ksjj.png" class="icon" />
          <text>科室介绍</text>
        </view>
        <view class="hospital-item">
          <image src="/static/zjjs.png" class="icon" />
          <text>专家介绍</text>
        </view>
        <view class="hospital-item">
          <image src="/static/ydnh.png" class="icon" />
          <text>院内导航</text>
        </view>
        <view class="hospital-item">
          <image src="/static/jyzn.png" class="icon" />
          <text>就医指南</text>
        </view>
      </view>
    </view>
    <view class="hospital-intro-title">医院介绍</view>
    <view class="hospital-intro card">
      北京交通大学校医院作为学校师生健康的坚实后盾，在校园生活中扮演着不可或缺的角色。它致力于为全校师生提供全面、优质且便捷的医疗保健服务，为校园的正常运转和师生的学习、工作生活保驾护航。
    </view>
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
// 可根据需要添加交互逻辑
</script>

<style scoped>
.hospital-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 120rpx;
}
.hospital-header {
  background: #3a9cff;
  padding-bottom: 24rpx;
}
.hospital-banner {
  width: 100%;
  height: 180rpx;
  object-fit: cover;
}
.hospital-info {
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  margin-top: -60rpx;
}
.hospital-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  background: #fff;
  border: 4rpx solid #fff;
  margin-right: 24rpx;
}
.hospital-title {
  display: flex;
  flex-direction: column;
}
.name {
  color: #222;
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}
.tags {
  display: flex;
  gap: 16rpx;
}
.tag {
  background: #e6f2ff;
  color: #3a9cff;
  font-size: 22rpx;
  border-radius: 8rpx;
  padding: 4rpx 16rpx;
  margin-right: 8rpx;
}
.hospital-tabs {
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
.hospital-address {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #333;
  margin: 16rpx 32rpx;
}
.location-icon {
  width: 32rpx;
  height: 32rpx;
  margin-left: 8rpx;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx 24rpx 0 24rpx;
  padding: 24rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}
.hospital-row {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  margin: 0 0 16rpx 0;
}
.hospital-item {
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
.hospital-intro-title {
  font-size: 28rpx;
  font-weight: bold;
  margin: 32rpx 0 16rpx 32rpx;
  color: #3a9cff;
}
.hospital-intro {
  font-size: 26rpx;
  color: #444;
  margin: 0 24rpx 0 24rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}
.tabbar-placeholder {
  height: 120rpx;
}
</style>


