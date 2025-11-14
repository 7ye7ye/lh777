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
import { ref, onMounted, computed } from 'vue' 
import { uniNavigateTo, uniShowToast } from '../../../utils/uniHelper' 
import { useUserStore } from '../../../store/user.js'
import { doctorApi } from '../../../api/doctor_massage'

// 医生信息（如需从用户状态中取，可替换为 pinia 的 userStore）
const userStore = useUserStore()
const doctorInfo = ref({ 
  name: '张医生', 
  department: '内科' 
}) 
const doctorId = computed(() => userStore.userInfo?.id || 1)

// 当前日期
const todayDate = ref('') 

// 日期列表（未来7天）
const dateList = ref([]) 
const selectedDateIndex = ref(0) 

// 排班列表
const scheduleList = ref([]) 

const fmtDate = (d) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`

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
  const today = new Date() 
  todayDate.value = `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日` 
} 

// 选择日期
const selectDate = (index) => { 
  selectedDateIndex.value = index 
  loadScheduleData() 
} 

// 加载后端排班数据
const labelFromRange = (range) => {
  if (range === '08:00-12:00') return `上午 ${range}`
  if (range === '14:00-17:00') return `下午 ${range}`
  if (range === '18:00-20:00') return `晚上 ${range}`
  return range
}

const loadScheduleData = async () => { 
  try {
    const sel = dateList.value[selectedDateIndex.value]
    const startDate = fmtDate(sel.fullDate)
    const resp = await doctorApi.getSchedules(doctorId.value, startDate, 1)
    scheduleList.value = (resp || []).map(s => ({
      timePeriod: labelFromRange(s.timeRange),
      roomNumber: s.roomNo || 'A-101',
      totalSlots: s.totalSlots || 0,
      bookedSlots: s.bookedCount || 0,
      remainingSlots: (s.totalSlots || 0) - (s.bookedCount || 0)
    }))
  } catch (e) {
    // 回退到本地模拟
    const mockData = [ 
      { timePeriod: '上午 08:00-12:00', roomNumber: '101', totalSlots: 20, bookedSlots: 15, remainingSlots: 5 }, 
      { timePeriod: '下午 14:00-17:00', roomNumber: '101', totalSlots: 15, bookedSlots: 13, remainingSlots: 2 }, 
      { timePeriod: '晚上 18:00-20:00', roomNumber: '102', totalSlots: 10, bookedSlots: 5, remainingSlots: 5 } 
    ] 
    scheduleList.value = mockData 
  }
} 

// 跳转到申请调班（映射到已存在的页面路径）
const goToAdjustSchedule = () => { 
  uniNavigateTo({ url: '/pages/doctor/schedule/apply' }) 
} 

// 跳转到患者列表（映射到已存在的页面路径）
const goToPatients = () => { 
  uniNavigateTo({ url: '/pages/doctor/patients/list' }) 
} 

// 跳转到个人信息（映射到已存在的页面路径）
const goToProfile = () => { 
  uniNavigateTo({ url: '/pages/doctor/profile/index' }) 
} 

// 根据剩余号源与占用比，返回状态的样式类
const getStatusClass = (item) => {
  const total = item?.totalSlots ?? 0
  const booked = item?.bookedSlots ?? 0
  const remaining = item?.remainingSlots ?? (total - booked)
  if (remaining <= 0) return 'status-full'
  const ratio = total > 0 ? booked / total : 0
  if (remaining < 3 || ratio >= 0.7) return 'status-busy'
  return 'status-available'
}

// 根据样式类返回显示文案
const getStatusText = (item) => {
  const cls = getStatusClass(item)
  if (cls === 'status-full') return '已满'
  if (cls === 'status-busy') return '紧张'
  return '可预约'
}

onMounted(() => { 
  userStore.initFromStorage()
  initDateList()
  loadScheduleData()
}) 
</script> 

<style scoped> 
.schedule-page { 
  min-height: 100vh; 
  background: #f7faff; 
  padding-bottom: 140rpx; 
} 

/* 顶部医生信息 */ 
.doctor-header { 
  background: linear-gradient(90deg, #479fff 0%, #2176ff 100%); 
  padding: 24rpx; 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
} 
.doctor-info { color: #fff; } 
.doctor-name { font-size: 36rpx; font-weight: 700; } 
.doctor-dept { font-size: 26rpx; opacity: 0.9; margin-top: 6rpx; display: block; } 
.today-date { color: #fff; font-size: 26rpx; } 

/* 日期选择器 */ 
.date-selector { margin-top: 12rpx; background: #fff; } 
.date-scroll { white-space: nowrap; padding: 12rpx; } 
.date-item { 
  display: inline-flex; 
  flex-direction: column; 
  align-items: center; 
  justify-content: center; 
  width: 120rpx; 
  height: 120rpx; 
  margin-right: 12rpx; 
  background: #f1f5f9; 
  border-radius: 16rpx; 
  box-shadow: 0 4rpx 12rpx rgba(33,118,255,0.06); 
  color: #334155; 
} 
.date-item.active { 
  background: linear-gradient(180deg, #e6f1ff 0%, #ffffff 100%); 
  border: 2rpx solid #479fff; 
  color: #2176ff; 
} 
.date-weekday { font-size: 24rpx; } 
.date-day { font-size: 36rpx; font-weight: 700; } 
.date-month { font-size: 24rpx; } 

/* 排班列表 */ 
.schedule-container { padding: 24rpx; } 
.section-title .title-text { font-size: 30rpx; color: #1e293b; font-weight: 600; } 
.empty-state { 
  background: #fff; 
  border-radius: 16rpx; 
  padding: 32rpx; 
  text-align: center; 
  color: #64748b; 
  margin-top: 16rpx; 
} 
.schedule-list { margin-top: 16rpx; } 
.schedule-item { 
  background: #fff; 
  border-radius: 16rpx; 
  padding: 16rpx; 
  margin-bottom: 12rpx; 
  box-shadow: 0 6rpx 16rpx rgba(33,118,255,0.08); 
} 
.schedule-header { display: flex; justify-content: space-between; align-items: center; } 
.time-period { display: flex; align-items: center; gap: 8rpx; } 
.period-icon { font-size: 26rpx; } 
.period-text { font-size: 28rpx; font-weight: 600; color: #0f172a; } 
.status-tag { 
  padding: 6rpx 12rpx; 
  border-radius: 999rpx; 
  font-size: 24rpx; 
} 
.status-available { background: #e0f2fe; color: #0369a1; } 
.status-busy { background: #fef3c7; color: #b45309; } 
.status-full { background: #fee2e2; color: #b91c1c; } 

.schedule-info { 
  display: grid; 
  grid-template-columns: 1fr 1fr; 
  gap: 8rpx 12rpx; 
  margin-top: 12rpx; 
} 
.info-item { display: flex; align-items: center; } 
.info-label { color: #64748b; font-size: 26rpx; } 
.info-value { color: #0f172a; font-size: 26rpx; font-weight: 600; } 
.appointment-count { color: #0ea5e9; } 
.remaining { color: #10b981; } 
.low-remaining { color: #ef4444; } 

/* 申请调班按钮 */ 
.action-section { padding: 0 24rpx 24rpx; } 
.action-btn { 
  width: 100%; 
  background: #2176ff; 
  color: #fff; 
  border-radius: 12rpx; 
  padding: 16rpx; 
  font-size: 28rpx; 
} 
.adjust-btn { box-shadow: 0 6rpx 16rpx rgba(33,118,255,0.18); } 

/* 底部导航 */ 
.bottom-nav { 
  position: fixed; 
  left: 0; right: 0; bottom: 0; 
  background: #fff; 
  border-top: 1rpx solid #e2e8f0; 
  display: flex; 
  padding: 12rpx 24rpx; 
  gap: 12rpx; 
} 
.nav-item { 
  flex: 1; 
  background: linear-gradient(90deg, #479fff 0%, #2176ff 100%); 
  color: #fff; 
  border-radius: 12rpx; 
  padding: 16rpx 0; 
  text-align: center; 
  box-shadow: 0 6rpx 16rpx rgba(33,118,255,0.18); 
} 
.nav-icon { display: block; font-size: 28rpx; } 
.nav-text { display: block; font-size: 26rpx; font-weight: 600; margin-top: 6rpx; } 
</style>