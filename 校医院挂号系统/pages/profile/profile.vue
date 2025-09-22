<template>
  <view class="profile-bg">
    <view class="profile-header">
      <view class="profile-info">
        <view class="avatar">👤</view>
        <view class="user-info">
          <text class="user-name">{{ userInfo.name || '微信用户' }}</text>
          <text class="user-phone">{{ userInfo.phone || '*************' }}</text>
        </view>
      </view>
      <button class="unbind-btn" size="mini" @click="goToUnbind">账户解绑</button>
    </view>

    <view class="profile-section card centered centered-down">
      <view class="profile-row">
        <view class="profile-item" @click="goToMyCard">
          <image class="icon icon-lg" src="/static/card.png" />
          <text>我的就诊卡</text>
        </view>
        <view class="profile-item" @click="goToMyPatient">
          <image class="icon icon-lg" src="/static/patient.png" />
          <text>我的就诊人</text>
        </view>
        <view class="profile-item" @click="goToMyDoctor">
          <image class="icon icon-lg" src="/static/doctor.png" />
          <text>我的医生</text>
        </view>
      </view>
    </view>

    <view class="profile-section card centered centered-down-small">
      <view class="section-title">就诊记录</view>
      <view class="profile-row">
        <view class="profile-item" @click="goToRegisterRecord">
          <image class="icon" src="/static/register.png" />
          <text>挂号记录</text>
        </view>
        <view class="profile-item" @click="goToOutpatientRecord">
          <image class="icon" src="/static/outpatient.png" />
          <text>门诊缴费记录</text>
        </view>
        <view class="profile-item" @click="goToHospitalRecord">
          <image class="icon" src="/static/hospital.png" />
          <text>住院预交记录</text>
        </view>
        <view class="profile-item" @click="goToConsultRecord">
          <image class="icon" src="/static/consult.png" />
          <text>咨询记录</text>
        </view>
      </view>
      <view class="profile-row">
        <view class="profile-item" @click="goToRevisitRecord">
          <image class="icon" src="/static/record.png" />
          <text>在线复诊记录</text>
        </view>
        <view class="profile-item" @click="goToCheckRecord">
          <image class="icon" src="/static/check.png" />
          <text>检查预约记录</text>
        </view>
      </view>
    </view>

    <view class="profile-section card">
      <view class="section-title">其他</view>
      <view class="profile-row">
        <view class="profile-item" @click="goToPrivacy">
          <image class="icon" src="/static/privacy.png" />
          <text>隐私协议</text>
        </view>
        <view class="profile-item" @click="goToHelp">
          <image class="icon" src="/static/help.png" />
          <text>帮助反馈</text>
        </view>
        <view class="profile-item" @click="goToComplain">
          <image class="icon" src="/static/complain.png" />
          <text>投诉建议</text>
        </view>
        <view class="profile-item" @click="goToEvaluate">
          <image class="icon" src="/static/evaluate.png" />
          <text>就诊评价</text>
        </view>
      </view>
    </view>

    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'

const userInfo = ref({})

// 获取用户信息
const getUserInfo = () => {
  userApi.getCurrentUser().then(res => {
    userInfo.value = res.data
  }).catch(() => {
    // 如果获取失败，使用默认信息
    userInfo.value = { name: '微信用户', phone: '15******068' }
  })
}

// 导航函数
const goToMyCard = () => {
  uni.navigateTo({ url: '/pages/profile/personal/mycard' })
}

const goToMyPatient = () => {
  uni.navigateTo({ url: '/pages/profile/personal/mypatient' })
}

const goToMyDoctor = () => {
  uni.navigateTo({ url: '/pages/profile/personal/mydoctor' })
}

const goToRegisterRecord = () => {
  uni.navigateTo({ url: '/pages/profile/records/register-record' })
}

const goToOutpatientRecord = () => {
  uni.navigateTo({ url: '/pages/profile/records/outpatient-record' })
}

const goToHospitalRecord = () => {
  uni.navigateTo({ url: '/pages/profile/records/hospital-record' })
}

const goToConsultRecord = () => {
  uni.navigateTo({ url: '/pages/profile/records/consult-record' })
}

const goToRevisitRecord = () => {
  uni.navigateTo({ url: '/pages/profile/records/revisit-record' })
}

const goToCheckRecord = () => {
  uni.navigateTo({ url: '/pages/profile/records/check-record' })
}

const goToPrivacy = () => {
  uni.navigateTo({ url: '/pages/profile/settings/privacy' })
}

const goToHelp = () => {
  uni.navigateTo({ url: '/pages/profile/help/help' })
}

const goToComplain = () => {
  uni.navigateTo({ url: '/pages/profile/settings/complain' })
}

const goToEvaluate = () => {
  uni.navigateTo({ url: '/pages/profile/settings/evaluate' })
}

const goToUnbind = () => {
  uni.navigateTo({ url: '/pages/profile/settings/unbind' })
}

onMounted(() => {
  getUserInfo()
})
</script>

<style scoped>
.profile-bg {
  background: #3a9cff;
  min-height: 100vh;
  padding-bottom: 72rpx;
}
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 24rpx;
  min-height: 180rpx; /* 个人信息模块在区域内垂直居中 */
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
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
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
  margin: 14rpx 14rpx 0 14rpx; /* 略放松，避免过紧 */
  padding: 14rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}
.profile-section {
  display: flex;
  flex-direction: column;
  justify-content: flex-start; /* 区块内容靠上且整体居中感 */
}
.profile-section.centered {
  min-height: 180rpx;
  justify-content: center; /* 该块内竖直居中 */
}
.profile-section.centered-down {
  padding-top: 18rpx; /* 让第一块稍微下移 */
}
.profile-section.centered-down-small {
  min-height: 200rpx;
  justify-content: center;
  padding-top: 10rpx; /* 就诊记录整体略向下 */
}
.section-title {
  font-size: 28rpx;
  font-weight: bold;
  margin: 0 0 6rpx 32rpx;
  color: #333;
}
.profile-row {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  margin: 0 0 6rpx 0; /* 适当放松行距 */
}
.profile-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center; /* 新增这一行 */
  margin: 8rpx 0;
  padding: 12rpx;
  border-radius: 12rpx;
  transition: background-color 0.3s;
}

.profile-item:active {
  background-color: rgba(58, 156, 255, 0.1);
}
.icon {
  width: 72rpx;
  height: 72rpx;
  margin-bottom: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-lg {
  width: 88rpx;
  height: 88rpx;
  margin-bottom: 12rpx;
}
.profile-item text {
  display: block;
  text-align: center;
  line-height: 32rpx;
  min-height: 64rpx; /* 固定两行高度，保证同一行标题对齐 */
}
.tabbar-placeholder {
  height: 72rpx;
}
</style>

