<template>
  <view class="page-bg">
    <!-- 就诊卡卡片 -->
    <view class="medical-card">
      <view class="card-header">
        <view class="hospital-logo">🏥</view>
        <view class="hospital-name">校医院就诊卡</view>
      </view>
      
      <view class="card-content">
        <view class="patient-name">{{ cardInfo.name || '张三' }}</view>
        
        <view class="card-details">
          <view class="detail-item">
            <text class="detail-label">门诊号：</text>
            <text class="detail-value">{{ cardInfo.cardNumber || 'M017080045' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">证件类型：</text>
            <text class="detail-value">{{ cardInfo.idType || '身份证' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">证件号码：</text>
            <text class="detail-value">{{ maskIdNumber(cardInfo.idNumber) || '420**********961' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">电话：</text>
            <text class="detail-value">{{ maskPhone(cardInfo.phone) || '15******467' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">地址：</text>
            <text class="detail-value">{{ cardInfo.address || '北京市北京市海淀区北京交通大学' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="action-btn modify-btn" @click="goToModifyInfo">修改个人信息</button>
      <button class="action-btn replace-btn" @click="goToReplaceCard">更换新就诊卡</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import { useUserStore } from '@/store/user'

const cardInfo = ref({})
const userStore = useUserStore()

// 获取就诊卡信息
const getCardInfo = () => {
  userApi.getCard().then(res => {
    cardInfo.value = res.data || {}
  }).catch(() => {
    // 如果获取失败，使用默认信息
    cardInfo.value = {
      name: '张三',
      cardNumber: 'M017080045',
      idType: '身份证',
      idNumber: '42012319900101961',
      phone: '15812345678',
      address: '北京市北京市海淀区北京交通大学'
    }
  })
}

// 脱敏身份证号
const maskIdNumber = (idNumber) => {
  if (!idNumber) return ''
  if (idNumber.length < 8) return idNumber
  return idNumber.substring(0, 3) + '**********' + idNumber.substring(idNumber.length - 3)
}

// 脱敏手机号
const maskPhone = (phone) => {
  if (!phone) return ''
  if (phone.length < 7) return phone
  return phone.substring(0, 2) + '******' + phone.substring(phone.length - 3)
}

// 跳转到修改个人信息页面
const goToModifyInfo = () => {
  uni.navigateTo({ url: '/pages/profile/personal/modify-info' })
}

// 更换新就诊卡
const goToReplaceCard = () => {
  uni.showModal({
    title: '更换就诊卡',
    content: '更换新就诊卡功能开发中，敬请期待',
    showCancel: false
  })
}

onMounted(() => {
  getCardInfo()
})
</script>

<style scoped>
.page-bg { 
  min-height: 100vh; 
  background: #f8faff; 
  padding: 24rpx;
}

/* 就诊卡样式 */
.medical-card {
  background: linear-gradient(135deg, #3a9cff 0%, #5db7ff 100%);
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(58, 156, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.medical-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  pointer-events: none;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  position: relative;
  z-index: 1;
}

.hospital-logo {
  font-size: 48rpx;
  margin-right: 16rpx;
}

.hospital-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.card-content {
  position: relative;
  z-index: 1;
}

.patient-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  text-align: center;
  margin-bottom: 30rpx;
}

.card-details {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16rpx;
  padding: 24rpx;
  backdrop-filter: blur(10rpx);
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 8rpx 0;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  width: 140rpx;
  flex-shrink: 0;
}

.detail-value {
  font-size: 26rpx;
  color: #fff;
  flex: 1;
  font-weight: 500;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.action-btn {
  width: 100%;
  height: 88rpx;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 500;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.modify-btn {
  background: #3a9cff;
  color: #fff;
}

.modify-btn:active {
  background: #2980e6;
  transform: scale(0.98);
}

.replace-btn {
  background: #52c41a;
  color: #fff;
}

.replace-btn:active {
  background: #389e0d;
  transform: scale(0.98);
}
</style>