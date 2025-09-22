<template>
  <view class="page-bg">
    <view class="card-header">我的就诊卡</view>
    
    <!-- 卡片信息 -->
    <view class="card-info">
      <view class="card-item">
        <text class="label">姓名：</text>
        <text class="value">{{ cardInfo.name || '张三' }}</text>
      </view>
      <view class="card-item">
        <text class="label">卡号：</text>
        <text class="value">{{ cardInfo.cardNumber || '1234567890' }}</text>
      </view>
      <view class="card-item">
        <text class="label">余额：</text>
        <text class="value balance">¥{{ cardInfo.balance || '0.00' }}</text>
      </view>
      <view class="card-item">
        <text class="label">状态：</text>
        <text class="value status" :class="cardInfo.status === '正常' ? 'active' : 'inactive'">
          {{ cardInfo.status || '正常' }}
        </text>
      </view>
    </view>

    <!-- 功能按钮 -->
    <view class="card-actions">
      <button class="action-btn primary" @click="getCardInfo">刷新卡信息</button>
      <button class="action-btn secondary" @click="goToRecharge">充值</button>
      <button class="action-btn secondary" @click="goToHistory">消费记录</button>
    </view>

    <!-- 最近消费记录 -->
    <view class="recent-section">
      <view class="section-title">最近消费</view>
      <view class="record-list">
        <view class="record-item" v-for="item in recentRecords" :key="item.id">
          <view class="record-info">
            <text class="record-desc">{{ item.description }}</text>
            <text class="record-time">{{ item.time }}</text>
          </view>
          <text class="record-amount" :class="item.type === 'income' ? 'income' : 'expense'">
            {{ item.type === 'income' ? '+' : '-' }}¥{{ item.amount }}
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'

const cardInfo = ref({})
const recentRecords = ref([
  { id: 1, description: '门诊挂号费', time: '2025-09-20 14:30', amount: '15.00', type: 'expense' },
  { id: 2, description: '药品费用', time: '2025-09-19 10:15', amount: '128.50', type: 'expense' },
  { id: 3, description: '卡充值', time: '2025-09-18 16:20', amount: '200.00', type: 'income' }
])

const getCardInfo = () => {
  userApi.getCard().then(res => {
    uni.showToast({ title: '获取成功', icon: 'success' })
    cardInfo.value = res.data || {}
  }).catch(() => {
    uni.showToast({ title: '获取失败', icon: 'error' })
  })
}

const goToRecharge = () => {
  uni.showModal({
    title: '充值功能',
    content: '充值功能开发中，敬请期待',
    showCancel: false
  })
}

const goToHistory = () => {
  uni.showModal({
    title: '消费记录',
    content: '消费记录功能开发中，敬请期待',
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
}

.card-header { 
  font-size: 36rpx; 
  font-weight: bold; 
  padding: 32rpx 32rpx 0 32rpx; 
}

.card-info { 
  background: #fff; 
  margin: 24rpx; 
  border-radius: 16rpx; 
  padding: 32rpx; 
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.card-item { 
  display: flex;
  align-items: center;
  margin-bottom: 24rpx; 
}

.label {
  width: 120rpx;
  font-size: 28rpx;
  color: #666;
}

.value {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.balance {
  color: #3a9cff;
  font-weight: bold;
}

.status.active {
  color: #52c41a;
}

.status.inactive {
  color: #ff4d4f;
}

.card-actions {
  display: flex;
  gap: 16rpx;
  margin: 0 24rpx 24rpx 24rpx;
}

.action-btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

.action-btn.primary {
  background: #3a9cff;
  color: #fff;
}

.action-btn.secondary {
  background: #fff;
  color: #3a9cff;
  border: 1px solid #3a9cff;
}

.recent-section {
  margin: 0 24rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
  color: #333;
}

.record-list {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1px solid #f0f0f0;
}

.record-item:last-child {
  border-bottom: none;
}

.record-info {
  display: flex;
  flex-direction: column;
}

.record-desc {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 4rpx;
}

.record-time {
  font-size: 24rpx;
  color: #999;
}

.record-amount {
  font-size: 28rpx;
  font-weight: bold;
}

.record-amount.income {
  color: #52c41a;
}

.record-amount.expense {
  color: #ff4d4f;
}
</style>