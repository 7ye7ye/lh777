<template>
  <view class="profile-bg">
    <view class="profile-header">
      <view class="profile-info">
        <image class="avatar" src="/static/avatar.png" />
        <view class="user-info">
          <text class="user-name">微信用户</text>
          <text class="user-phone">15******068</text>
        </view>
      </view>
      <button class="unbind-btn" size="mini">账户解绑</button>
    </view>

    <view class="profile-section card">
      <view class="profile-row">
        <view class="profile-item">
          <image src="/static/card.png" class="icon" />
          <text>我的就诊卡</text>
        </view>
        <view class="profile-item">
          <image src="/static/patient.png" class="icon" />
          <text>我的就诊人</text>
        </view>
        <view class="profile-item">
          <image src="/static/doctor.png" class="icon" />
          <text>我的医生</text>
        </view>
      </view>
    </view>

    <view class="profile-section card">
      <view class="section-title">就诊记录</view>
      <view class="profile-row">
        <view class="profile-item">
          <image src="/static/register.png" class="icon" />
          <text>挂号记录</text>
        </view>
        <view class="profile-item">
          <image src="/static/outpatient.png" class="icon" />
          <text>门诊缴费记录</text>
        </view>
        <view class="profile-item">
          <image src="/static/hospital.png" class="icon" />
          <text>住院预交记录</text>
        </view>
        <view class="profile-item">
          <image src="/static/consult.png" class="icon" />
          <text>咨询记录</text>
        </view>
      </view>
      <view class="profile-row">
        <view class="profile-item">
          <image src="/static/revisit.png" class="icon" />
          <text>在线复诊记录</text>
        </view>
        <view class="profile-item">
          <image src="/static/check.png" class="icon" />
          <text>检查预约记录</text>
        </view>
      </view>
    </view>

    <view class="profile-section card">
      <view class="section-title">其他</view>
      <view class="profile-row">
        <view class="profile-item">
          <image src="/static/privacy.png" class="icon" />
          <text>隐私协议</text>
        </view>
        <view class="profile-item">
          <image src="/static/help.png" class="icon" />
          <text>帮助反馈</text>
        </view>
        <view class="profile-item">
          <image src="/static/complain.png" class="icon" />
          <text>投诉建议</text>
        </view>
        <view class="profile-item">
          <image src="/static/evaluate.png" class="icon" />
          <text>就诊评价</text>
        </view>
      </view>
    </view>

    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
const login = () => {
  uni.showToast({ title: '准备跳转', icon: 'none' })
  uni.navigateTo({
    url: '/pages/login/login'
  })
}
</script>

<style scoped>
.profile-bg {
  background: #3a9cff;
  min-height: 100vh;
  padding-bottom: 120rpx;
}
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 48rpx 32rpx 32rpx 32rpx;
  background: #3a9cff;
}
.profile-info {
  display: flex;
  align-items: center;
}
.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: #fff;
  margin-right: 24rpx;
}
.user-info {
  display: flex;
  flex-direction: column;
  color: #fff;
}
.user-name {
  font-size: 36rpx;
  font-weight: bold;
}
.user-phone {
  font-size: 26rpx;
  margin-top: 8rpx;
}
.unbind-btn {
  border: 1px solid #fff;
  color: #fff;
  background: transparent;
  font-size: 26rpx;
  border-radius: 8rpx;
  padding: 8rpx 24rpx;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx 24rpx 0 24rpx;
  padding: 24rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}
.section-title {
  font-size: 28rpx;
  font-weight: bold;
  margin: 0 0 16rpx 32rpx;
  color: #333;
}
.profile-row {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  margin: 0 0 16rpx 0;
}
.profile-item {
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
.tabbar-placeholder {
  height: 120rpx;
}
</style>


