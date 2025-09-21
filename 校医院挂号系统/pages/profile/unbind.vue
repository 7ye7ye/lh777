<template>
  <view class="page-bg">
    <view class="unbind-header">账户解绑</view>
    
    <!-- 警告提示 -->
    <view class="warning-section">
      <view class="warning-icon">⚠️</view>
      <view class="warning-content">
        <text class="warning-title">解绑风险提示</text>
        <text class="warning-text">解绑后将无法继续使用当前账户，请谨慎操作</text>
      </view>
    </view>

    <!-- 账户信息 -->
    <view class="account-info">
      <view class="info-title">当前账户信息</view>
      <view class="info-item">
        <text class="info-label">用户名：</text>
        <text class="info-value">{{ userInfo.name || '微信用户' }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">手机号：</text>
        <text class="info-value">{{ userInfo.phone || '15******068' }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">绑定时间：</text>
        <text class="info-value">{{ userInfo.bindTime || '2025-01-01' }}</text>
      </view>
    </view>

    <!-- 解绑影响说明 -->
    <view class="impact-section">
      <view class="impact-title">解绑后将影响以下功能：</view>
      <view class="impact-list">
        <view class="impact-item">
          <text class="impact-icon">❌</text>
          <text class="impact-text">无法查看就诊记录</text>
        </view>
        <view class="impact-item">
          <text class="impact-icon">❌</text>
          <text class="impact-text">无法进行在线挂号</text>
        </view>
        <view class="impact-item">
          <text class="impact-icon">❌</text>
          <text class="impact-text">无法查看就诊卡信息</text>
        </view>
        <view class="impact-item">
          <text class="impact-icon">❌</text>
          <text class="impact-text">无法使用在线支付</text>
        </view>
      </view>
    </view>

    <!-- 解绑原因 -->
    <view class="reason-section">
      <view class="reason-title">解绑原因（可选）</view>
      <textarea 
        class="reason-input" 
        placeholder="请告诉我们您解绑的原因，帮助我们改进服务..."
        v-model="unbindReason"
        maxlength="200"
      />
      <text class="char-count">{{ unbindReason.length }}/200</text>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="cancel-btn" @click="goBack">取消</button>
      <button class="unbind-btn" @click="confirmUnbind">确认解绑</button>
    </view>

    <!-- 确认弹窗 -->
    <view class="confirm-modal" v-if="showConfirmModal">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">最后确认</text>
        </view>
        <view class="modal-body">
          <text class="modal-text">解绑后需要重新绑定才能使用服务</text>
          <text class="modal-text">确定要解绑当前账户吗？</text>
        </view>
        <view class="modal-actions">
          <button class="modal-btn cancel" @click="showConfirmModal = false">取消</button>
          <button class="modal-btn confirm" @click="executeUnbind">确认解绑</button>
        </view>
      </view>
    </view>
  </view>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'

const userInfo = ref({})
const unbindReason = ref('')
const showConfirmModal = ref(false)

const getUserInfo = () => {
  userApi.getCurrentUser().then(res => {
    userInfo.value = res.data || {}
  }).catch(() => {
    // 使用默认信息
    userInfo.value = { 
      name: '微信用户', 
      phone: '15******068',
      bindTime: '2025-01-01'
    }
  })
}

const goBack = () => {
  uni.navigateBack()
}

const confirmUnbind = () => {
  showConfirmModal.value = true
}

const executeUnbind = () => {
  const unbindData = {
    reason: unbindReason.value
  }
  
  userApi.unbindAccount(unbindData).then(() => {
    uni.showToast({ title: '解绑成功', icon: 'success' })
    showConfirmModal.value = false
    
    // 延迟跳转到登录页
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/login/login' })
    }, 1500)
  }).catch(() => {
    uni.showToast({ title: '解绑失败，请重试', icon: 'error' })
    showConfirmModal.value = false
  })
}

onMounted(() => {
  getUserInfo()
})
</script>
<style scoped>
.page-bg { 
  min-height: 100vh; 
  background: #f8faff; 
}

.unbind-header { 
  font-size: 36rpx; 
  font-weight: bold; 
  padding: 32rpx; 
}

.warning-section {
  background: #fff2e8;
  border: 1px solid #ffd591;
  border-radius: 16rpx;
  margin: 24rpx;
  padding: 24rpx;
  display: flex;
  align-items: flex-start;
}

.warning-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.warning-content {
  flex: 1;
}

.warning-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #d46b08;
  margin-bottom: 8rpx;
}

.warning-text {
  font-size: 26rpx;
  color: #d46b08;
}

.account-info {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.info-title {
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
  color: #333;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.info-label {
  width: 140rpx;
  font-size: 26rpx;
  color: #666;
}

.info-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
}

.impact-section {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.impact-title {
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
  color: #333;
}

.impact-list {
  display: flex;
  flex-direction: column;
}

.impact-item {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.impact-icon {
  font-size: 24rpx;
  margin-right: 12rpx;
}

.impact-text {
  font-size: 26rpx;
  color: #666;
}

.reason-section {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.reason-title {
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
  color: #333;
}

.reason-input {
  width: 100%;
  min-height: 120rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 16rpx;
  font-size: 26rpx;
  border: 1px solid #e9ecef;
  margin-bottom: 8rpx;
}

.char-count {
  font-size: 22rpx;
  color: #999;
  text-align: right;
}

.action-buttons {
  display: flex;
  gap: 16rpx;
  padding: 0 24rpx 32rpx 24rpx;
}

.cancel-btn {
  flex: 1;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 28rpx;
}

.unbind-btn {
  flex: 1;
  background: #ff4d4f;
  color: #fff;
  border: none;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 28rpx;
}

.confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 16rpx;
  margin: 32rpx;
  overflow: hidden;
}

.modal-header {
  padding: 32rpx 32rpx 16rpx 32rpx;
  text-align: center;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.modal-body {
  padding: 16rpx 32rpx 32rpx 32rpx;
  text-align: center;
}

.modal-text {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
  display: block;
}

.modal-actions {
  display: flex;
  border-top: 1px solid #f0f0f0;
}

.modal-btn {
  flex: 1;
  padding: 24rpx;
  border: none;
  font-size: 28rpx;
  background: transparent;
}

.modal-btn.cancel {
  color: #666;
  border-right: 1px solid #f0f0f0;
}

.modal-btn.confirm {
  color: #ff4d4f;
  font-weight: bold;
}
</style>