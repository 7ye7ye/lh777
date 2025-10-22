<template>
  <view class="page-bg">
    <view class="list-header">门诊缴费记录</view>
    
    <!-- 统计信息 -->
    <view class="stats-section">
      <view class="stats-item">
        <text class="stats-label">本月缴费</text>
        <text class="stats-value">¥{{ monthlyTotal }}</text>
      </view>
      <view class="stats-item">
        <text class="stats-label">总缴费次数</text>
        <text class="stats-value">{{ totalCount }}次</text>
      </view>
    </view>

    <!-- 记录列表 -->
    <view class="record-list">
      <view class="record-item" v-for="item in records" :key="item.id">
        <view class="record-header">
          <text class="record-time">{{ item.time }}</text>
          <text class="record-status" :class="item.status === '已支付' ? 'paid' : 'unpaid'">
            {{ item.status }}
          </text>
        </view>
        <view class="record-content">
          <view class="record-info">
            <text class="record-project">{{ item.project }}</text>
            <text class="record-dept">{{ item.dept }}</text>
          </view>
          <view class="record-amount">
            <text class="amount">¥{{ item.amount }}</text>
          </view>
        </view>
        <view class="record-footer" v-if="item.status === '未支付'">
          <button class="pay-btn" @click="payRecord(item.id)">立即支付</button>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="records.length === 0">
      <text class="empty-text">暂无缴费记录</text>
    </view>

    <button class="main-btn" @click="getOutpatientRecord">刷新记录</button>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { userApi } from '@/api/user'

const records = ref([
  { 
    id: 1, 
    project: '挂号费', 
    amount: '15.00', 
    time: '2025-09-21 09:00',
    dept: '内科',
    status: '已支付'
  },
  { 
    id: 2, 
    project: '药品费用', 
    amount: '128.50', 
    time: '2025-09-20 14:30',
    dept: '药房',
    status: '已支付'
  },
  { 
    id: 3, 
    project: '检查费', 
    amount: '200.00', 
    time: '2025-09-19 10:15',
    dept: '检验科',
    status: '未支付'
  }
])

const monthlyTotal = computed(() => {
  const currentMonth = new Date().getMonth()
  const currentYear = new Date().getFullYear()
  
  return records.value
    .filter(record => {
      const recordDate = new Date(record.time)
      return recordDate.getMonth() === currentMonth && 
             recordDate.getFullYear() === currentYear &&
             record.status === '已支付'
    })
    .reduce((total, record) => total + parseFloat(record.amount), 0)
    .toFixed(2)
})

const totalCount = computed(() => {
  return records.value.filter(record => record.status === '已支付').length
})

const getOutpatientRecord = () => {
  userApi.getOutpatientRecord().then(res => {
    uni.showToast({ title: '获取成功', icon: 'success' })
    // records.value = res.data
  }).catch(() => {
    uni.showToast({ title: '获取失败', icon: 'error' })
  })
}

const payRecord = (id) => {
  uni.showModal({
    title: '确认支付',
    content: '确定要支付这笔费用吗？',
    success: (res) => {
      if (res.confirm) {
        const record = records.value.find(r => r.id === id)
        if (record) {
          record.status = '已支付'
          uni.showToast({ title: '支付成功', icon: 'success' })
        }
      }
    }
  })
}

onMounted(() => {
  getOutpatientRecord()
})
</script>

<style scoped>
.page-bg { 
  min-height: 100vh; 
  background: #f8faff; 
}

.list-header { 
  font-size: 36rpx; 
  font-weight: bold; 
  padding: 32rpx; 
}

.stats-section {
  display: flex;
  background: #fff;
  margin: 0 24rpx 24rpx 24rpx;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.stats-item {
  flex: 1;
  text-align: center;
}

.stats-label {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 8rpx;
}

.stats-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #3a9cff;
}

.record-list { 
  background: #fff; 
  margin: 0 24rpx 24rpx 24rpx; 
  border-radius: 16rpx; 
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.record-item { 
  border-bottom: 1px solid #f0f0f0; 
  padding: 20rpx 0; 
}

.record-item:last-child {
  border-bottom: none;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.record-time {
  font-size: 26rpx;
  color: #666;
}

.record-status {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.record-status.paid {
  background: #f6ffed;
  color: #52c41a;
}

.record-status.unpaid {
  background: #fff2e8;
  color: #fa8c16;
}

.record-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.record-info {
  flex: 1;
}

.record-project {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 4rpx;
  display: block;
}

.record-dept {
  font-size: 24rpx;
  color: #999;
}

.record-amount {
  text-align: right;
}

.amount {
  font-size: 30rpx;
  font-weight: bold;
  color: #ff4d4f;
}

.record-footer {
  text-align: right;
}

.pay-btn {
  background: #3a9cff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 8rpx 24rpx;
  font-size: 24rpx;
}

.empty-state {
  text-align: center;
  padding: 80rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.main-btn { 
  width: 100%; 
  margin: 32rpx 24rpx 0 24rpx; 
  background: #3a9cff; 
  color: #fff; 
  border: none;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 28rpx;
}
</style>