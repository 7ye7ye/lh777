<template>
  <view class="schedule-page">
    <!-- 顶部医生信息 -->
    <view class="doctor-header">
      <view class="doctor-info">
        <text class="doctor-name">{{ doctorInfo.name || '张医生' }}</text>
        <text class="doctor-dept">{{ doctorInfo.department || '内科' }}</text>
      </view>
      <text class="today-date">{{ todayDate }}</text>
    </view>

    <!-- 日期选择器 -->
    <view class="date-selector">
      <scroll-view scroll-x class="date-scroll">
        <view
          v-for="(date, index) in dateList"
          :key="index"
          class="date-item"
          :class="{ active: selectedDateIndex === index }"
          @click="selectDate(index)"
        >
          <text class="date-weekday">{{ date.weekday }}</text>
          <text class="date-day">{{ date.day }}</text>
          <text class="date-month">{{ date.month }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 排班列表 -->
    <view class="schedule-container">
      <view class="section-title">
        <text class="title-text">当日出诊排班</text>
      </view>

      <view v-if="scheduleList.length === 0" class="empty-state">
        <text class="empty-text">暂无排班信息</text>
      </view>

      <view v-else class="schedule-list">
        <view
          v-for="(item, index) in scheduleList"
          :key="index"
          class="schedule-item"
        >
          <view class="schedule-header">
            <view class="time-period">
              <text class="period-icon">⏰</text>
              <text class="period-text">{{ item.timePeriod }}</text>
            </view>
            <view class="status-tag" :class="getStatusClass(item)">
              {{ getStatusText(item) }}
            </view>
          </view>

          <view class="schedule-info">
            <view class="info-item">
              <text class="info-label">诊室号：</text>
              <text class="info-value">{{ item.roomNumber }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">可预约：</text>
              <text class="info-value">{{ item.totalSlots }}人</text>
            </view>
            <view class="info-item">
              <text class="info-label">已挂号：</text>
              <text class="info-value appointment-count">{{ item.bookedSlots }}人</text>
            </view>
            <view class="info-item">
              <text class="info-label">剩余号源：</text>
              <text class="info-value remaining" :class="{ 'low-remaining': item.remainingSlots < 3 }">
                {{ item.remainingSlots }}人
              </text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 申请调班按钮 -->
    <view class="action-section">
      <button class="action-btn adjust-btn" @click="goToAdjustSchedule">
        申请调班
      </button>
    </view>

    <!-- 底部导航 -->
    <view class="bottom-nav">
      <view class="nav-item" @click="goToPatients">
        <text class="nav-icon">👥</text>
        <text class="nav-text">患者详情</text>
      </view>
      <view class="nav-item" @click="goToProfile">
        <text class="nav-icon">👤</text>
        <text class="nav-text">个人信息</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { uniNavigateTo, uniShowToast } from '@/utils/uniHelper'

// 医生信息
const doctorInfo = ref({
  name: '张医生',
  department: '内科'
})

// 当前日期
const todayDate = ref('')

// 日期列表（未来7天）
const dateList = ref([])
const selectedDateIndex = ref(0)

// 排班列表
const scheduleList = ref([])

// 初始化日期列表
const initDateList = () => {
  const dates = []
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  
  for (let i = 0; i < 7; i++) {
    const date = new Date()
    date.setDate(date.getDate() + i)
    
    dates.push({
      weekday: weekdays[date.getDay()],
      day: date.getDate(),
      month: `${date.getMonth() + 1}月`,
      fullDate: date
    })
  }
  
  dateList.value = dates
  
  // 设置今日日期显示
  const today = new Date()
  todayDate.value = `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日`
}

// 选择日期
const selectDate = (index) => {
  selectedDateIndex.value = index
  loadScheduleData()
}

// 加载排班数据（模拟数据）
const loadScheduleData = () => {
  // 模拟数据
  const mockData = [
    {
      timePeriod: '上午 08:00-12:00',
      roomNumber: '101',
      totalSlots: 20,
      bookedSlots: 15,
      remainingSlots: 5
    },
    {
      timePeriod: '下午 14:00-17:00',
      roomNumber: '101',
      totalSlots: 15,
      bookedSlots: 13,
      remainingSlots: 2
    },
    {
      timePeriod: '晚上 18:00-20:00',
      roomNumber: '102',
      totalSlots: 10,
      bookedSlots: 5,
      remainingSlots: 5
    }
  ]
  
  // 根据选择的日期调整数据（这里简化处理）
  if (selectedDateIndex.value === 0) {
    scheduleList.value = mockData
  } else {
    // 未来日期显示预约较少的数据
    scheduleList.value = mockData.map(item => ({
      ...item,
      bookedSlots: Math.floor(item.totalSlots * 0.3),
      remainingSlots: Math.floor(item.totalSlots * 0.7)
    }))
  }
}

// 获取状态样式类
const getStatusClass = (item) => {
  const rate = item.bookedSlots / item.totalSlots
  if (rate >= 0.9) return 'status-full'
  if (rate >= 0.6) return 'status-busy'
  return 'status-available'
}

// 获取状态文本
const getStatusText = (item) => {
  const rate = item.bookedSlots / item.totalSlots
  if (rate >= 0.9) return '号源紧张'
  if (rate >= 0.6) return '预约较多'
  return '可预约'
}

// 跳转到申请调班
const goToAdjustSchedule = () => {
  uniNavigateTo({ url: '/pages/doctor/schedule/adjust-schedule' })
}

// 跳转到患者列表
const goToPatients = () => {
  uniNavigateTo({ url: '/pages/doctor/patients/patient-list' })
}

// 跳转到个人信息
const goToProfile = () => {
  uniNavigateTo({ url: '/pages/doctor/profile/profile' })
}

onMounted(() => {
  initDateList()
  loadScheduleData()
})
</script>

<style scoped>
.schedule-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 120rpx;
}

/* 医生头部信息 */
.doctor-header {
  background: linear-gradient(135deg, #1677ff 0%, #4da3ff 100%);
  padding: 40rpx 32rpx;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.doctor-info {
  display: flex;
  flex-direction: column;
}

.doctor-name {
  font-size: 36rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.doctor-dept {
  font-size: 26rpx;
  opacity: 0.9;
}

.today-date {
  font-size: 24rpx;
  opacity: 0.85;
}

/* 日期选择器 */
.date-selector {
  background: #fff;
  padding: 24rpx 0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.date-scroll {
  white-space: nowrap;
  padding: 0 24rpx;
}

.date-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 24rpx;
  margin-right: 16rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  min-width: 100rpx;
  transition: all 0.3s;
}

.date-item.active {
  background: #1677ff;
  color: #fff;
}

.date-weekday {
  font-size: 22rpx;
  margin-bottom: 4rpx;
  opacity: 0.8;
}

.date-day {
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 2rpx;
}

.date-month {
  font-size: 20rpx;
  opacity: 0.7;
}

/* 排班容器 */
.schedule-container {
  padding: 24rpx 32rpx;
}

.section-title {
  margin-bottom: 24rpx;
}

.title-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 排班列表 */
.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.schedule-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.time-period {
  display: flex;
  align-items: center;
}

.period-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.period-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.status-tag {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.status-available {
  background: #e6f7ff;
  color: #1677ff;
}

.status-busy {
  background: #fff7e6;
  color: #fa8c16;
}

.status-full {
  background: #fff1f0;
  color: #ff4d4f;
}

/* 排班信息 */
.schedule-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-size: 26rpx;
  color: #666;
}

.info-value {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.appointment-count {
  color: #1677ff;
}

.remaining {
  color: #52c41a;
}

.remaining.low-remaining {
  color: #ff4d4f;
}

/* 操作区域 */
.action-section {
  padding: 24rpx 32rpx;
}

.action-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 12rpx;
  font-size: 30rpx;
  border: none;
}

.adjust-btn {
  background: linear-gradient(135deg, #1677ff 0%, #4da3ff 100%);
  color: #fff;
}

/* 底部导航 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: #fff;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08);
  padding: 16rpx 0;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12rpx 0;
}

.nav-icon {
  font-size: 48rpx;
  margin-bottom: 8rpx;
}

.nav-text {
  font-size: 24rpx;
  color: #666;
}
</style>

