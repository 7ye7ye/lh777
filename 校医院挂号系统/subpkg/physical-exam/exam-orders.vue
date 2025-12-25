<template>
  <view class="orders-bg">
    <!-- 顶部标签页 -->
    <view class="tabs-bar">
      <view 
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="changeTab(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <view class="tab-badge" v-if="tab.count > 0">{{ tab.count }}</view>
      </view>
    </view>

    <!-- 订单列表 -->
    <view class="orders-list" v-if="filteredOrders.length > 0">
      <view 
        v-for="order in filteredOrders" 
        :key="order.id"
        class="order-card"
      >
        <!-- 订单头部 -->
        <view class="order-header">
          <view class="order-number">
            <text class="label">订单号：</text>
            <text class="value">{{ order.orderNo }}</text>
          </view>
          <view class="order-status" :class="order.statusClass">
            {{ order.statusText }}
          </view>
        </view>

        <!-- 订单内容 -->
        <view class="order-content">
          <view class="content-row">
            <text class="content-label">体检套餐</text>
            <text class="content-value">{{ order.package }}</text>
          </view>
          <view class="content-row">
            <text class="content-label">体检人</text>
            <text class="content-value">{{ order.name }}</text>
          </view>
          <view class="content-row">
            <text class="content-label">体检日期</text>
            <text class="content-value">{{ order.date }}</text>
          </view>
          <view class="content-row">
            <text class="content-label">体检时间</text>
            <text class="content-value">{{ order.time }}</text>
          </view>
        </view>

        <!-- 订单金额 -->
        <view class="order-price">
          <text class="price-label">订单金额：</text>
          <text class="price-value">¥{{ order.price }}</text>
        </view>

        <!-- 订单操作 -->
        <view class="order-actions">
          <!-- 待支付 -->
          <template v-if="order.status === 'pending'">
            <button class="action-btn secondary" @click="cancelOrder(order)">取消订单</button>
            <button class="action-btn primary" @click="payOrder(order)">立即支付</button>
          </template>
          
          <!-- 待体检 -->
          <template v-else-if="order.status === 'paid'">
            <button class="action-btn secondary" @click="viewDetail(order)">查看详情</button>
            <button class="action-btn primary" @click="changeDate(order)">改约</button>
          </template>
          
          <!-- 已完成 -->
          <template v-else-if="order.status === 'completed'">
            <button class="action-btn secondary" @click="viewReport(order)">查看报告</button>
            <button class="action-btn primary" @click="bookAgain(order)">再次预约</button>
          </template>
          
          <!-- 已取消 -->
          <template v-else-if="order.status === 'cancelled'">
            <button class="action-btn secondary" @click="deleteOrder(order)">删除订单</button>
            <button class="action-btn primary" @click="bookAgain(order)">再次预约</button>
          </template>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <image class="empty-icon" :src="getStaticImage('/static/empty_message.png')" mode="aspectFit" />
      <text class="empty-text">{{ emptyText }}</text>
      <button class="empty-btn" @click="goToBooking">立即预约体检</button>
    </view>

    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getStaticImage } from '@/utils/imageHelper'

// 标签页
const tabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '待支付', value: 'pending', count: 1 },
  { label: '待体检', value: 'paid', count: 1 },
  { label: '已完成', value: 'completed', count: 2 },
  { label: '已取消', value: 'cancelled', count: 0 }
])

const currentTab = ref('all')

// 模拟订单数据
const orders = ref([
  {
    id: 1,
    orderNo: '202510180001',
    package: '教职工套餐',
    name: '张三',
    date: '2025-10-20',
    time: '08:30-09:30',
    price: 480,
    status: 'pending',
    statusText: '待支付',
    statusClass: 'pending'
  },
  {
    id: 2,
    orderNo: '202510150002',
    package: '基础套餐',
    name: '李四',
    date: '2025-10-22',
    time: '07:30-08:30',
    price: 280,
    status: 'paid',
    statusText: '待体检',
    statusClass: 'paid'
  },
  {
    id: 3,
    orderNo: '202509200003',
    package: '全面套餐',
    name: '王五',
    date: '2025-09-25',
    time: '09:30-10:30',
    price: 880,
    status: 'completed',
    statusText: '已完成',
    statusClass: 'completed'
  },
  {
    id: 4,
    orderNo: '202508100004',
    package: '基础套餐',
    name: '赵六',
    date: '2025-08-15',
    time: '08:30-09:30',
    price: 280,
    status: 'completed',
    statusText: '已完成',
    statusClass: 'completed'
  }
])

// 筛选后的订单
const filteredOrders = computed(() => {
  if (currentTab.value === 'all') {
    return orders.value
  }
  return orders.value.filter(order => order.status === currentTab.value)
})

// 空状态文本
const emptyText = computed(() => {
  const textMap = {
    all: '暂无订单',
    pending: '暂无待支付订单',
    paid: '暂无待体检订单',
    completed: '暂无已完成订单',
    cancelled: '暂无已取消订单'
  }
  return textMap[currentTab.value] || '暂无订单'
})

// 切换标签
const changeTab = (value) => {
  currentTab.value = value
}

// 取消订单
const cancelOrder = (order) => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该订单吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '订单已取消',
          icon: 'success'
        })
        // 更新订单状态
        order.status = 'cancelled'
        order.statusText = '已取消'
        order.statusClass = 'cancelled'
      }
    }
  })
}

// 支付订单
const payOrder = (order) => {
  uni.showModal({
    title: '确认支付',
    content: `订单金额：¥${order.price}\n\n请选择支付方式`,
    confirmText: '微信支付',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '支付中...' })
        
        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '支付成功',
            icon: 'success'
          })
          // 更新订单状态
          order.status = 'paid'
          order.statusText = '待体检'
          order.statusClass = 'paid'
        }, 1500)
      }
    }
  })
}

// 查看详情
const viewDetail = (order) => {
  uni.showModal({
    title: '订单详情',
    content: `订单号：${order.orderNo}\n体检套餐：${order.package}\n体检人：${order.name}\n体检日期：${order.date}\n体检时间：${order.time}\n订单金额：¥${order.price}`,
    showCancel: false
  })
}

// 改约
const changeDate = (order) => {
  uni.showModal({
    title: '改约提示',
    content: '如需改约，请联系客服：010-51682525',
    showCancel: false
  })
}

// 查看报告
const viewReport = (order) => {
  uni.navigateTo({
    url: '/subpkg/physical-exam/exam-report'
  })
}

// 再次预约
const bookAgain = (order) => {
  uni.navigateTo({
    url: '/subpkg/physical-exam/individual-booking'
  })
}

// 删除订单
const deleteOrder = (order) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该订单吗？',
    success: (res) => {
      if (res.confirm) {
        const index = orders.value.findIndex(o => o.id === order.id)
        if (index > -1) {
          orders.value.splice(index, 1)
          uni.showToast({
            title: '订单已删除',
            icon: 'success'
          })
        }
      }
    }
  })
}

// 去预约
const goToBooking = () => {
  uni.navigateTo({
    url: '/subpkg/physical-exam/individual-booking'
  })
}
</script>

<style scoped>
.orders-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.tabs-bar {
  background: #fff;
  display: flex;
  padding: 0 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.tab-item {
  flex: 1;
  padding: 28rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.tab-item.active {
  color: #fa709a;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 6rpx;
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  border-radius: 3rpx;
}

.tab-text {
  font-size: 28rpx;
  font-weight: bold;
}

.tab-badge {
  position: absolute;
  top: 16rpx;
  right: 20rpx;
  background: #ff4757;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 8rpx;
  border-radius: 20rpx;
  min-width: 32rpx;
  text-align: center;
}

.orders-list {
  padding: 24rpx;
}

.order-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(250, 112, 154, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 24rpx;
  border-bottom: 2rpx solid #f5f5f5;
  margin-bottom: 24rpx;
}

.order-number {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
}

.order-number .label {
  color: #999;
}

.order-number .value {
  color: #333;
  font-weight: bold;
}

.order-status {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: bold;
}

.order-status.pending {
  background: #fff3e0;
  color: #ff9800;
}

.order-status.paid {
  background: #e3f2fd;
  color: #2196f3;
}

.order-status.completed {
  background: #e8f5e9;
  color: #4caf50;
}

.order-status.cancelled {
  background: #f5f5f5;
  color: #999;
}

.order-content {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.content-row {
  display: flex;
  justify-content: space-between;
  font-size: 26rpx;
}

.content-label {
  color: #999;
}

.content-value {
  color: #333;
  font-weight: bold;
}

.order-price {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 8rpx;
  padding: 24rpx 0;
  border-top: 2rpx solid #f5f5f5;
  border-bottom: 2rpx solid #f5f5f5;
  margin-bottom: 24rpx;
}

.price-label {
  font-size: 26rpx;
  color: #666;
}

.price-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff4757;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
}

.action-btn {
  padding: 16rpx 32rpx;
  border-radius: 40rpx;
  font-size: 26rpx;
  font-weight: bold;
  border: none;
}

.action-btn.secondary {
  background: #fff;
  color: #666;
  border: 2rpx solid #e8e8e8;
}

.action-btn.primary {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #fff;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  width: 300rpx;
  height: 300rpx;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 48rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #fff;
  border-radius: 40rpx;
  padding: 20rpx 64rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
}

.tabbar-placeholder {
  height: 120rpx;
}
</style>

