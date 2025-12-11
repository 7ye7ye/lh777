<template> 
  <view class="schedule-page"> 
    <!-- 顶部医生信息 -->
    <view class="doctor-header">
      <view class="doctor-info">
        <text class="doctor-name">{{ doctorInfo.name || '加载中...' }}</text>
        <text class="doctor-dept">{{ doctorInfo.department || '—' }}</text>
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
              <text class="info-label">排班ID：</text> 
              <text class="info-value">{{ item.scheduleId }}</text> 
            </view> 
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

    <!-- 操作按钮 --> 
    <view class="action-section"> 
      <button class="action-btn refresh-btn" @click="handleRefresh"> 
        🔄 刷新排班 
      </button>
      <button class="action-btn adjust-btn" @click="goToAdjustSchedule"> 
        申请调班 
      </button> 
    </view> 

    <!-- 医生端底部导航 -->
    <DoctorTabBar active="schedule" />
  </view> 
</template> 

<script setup> 
import { ref, onMounted, computed } from 'vue' 
import DoctorTabBar from '@/components/DoctorTabBar.vue'

import { uniNavigateTo, uniShowToast } from '../../../utils/uniHelper' 
import { useUserStore } from '../../../store/user.js'
import { doctorApi as doctorApiMassage } from '../../../api/doctor_massage'
import { doctorApi as doctorApiMain } from '../../../api/doctor'

// 使用有 getProfileByUserId 方法的 API
const doctorApi = doctorApiMassage?.getProfileByUserId 
  ? doctorApiMassage 
  : doctorApiMain

// 用接口数据填充页头信息与 doctorId
const userStore = useUserStore()
const doctorInfo = ref({ 
  name: '', 
  department: '' 
}) 
// 修复：去掉 TS 泛型，使用纯 JS
const doctorIdRef = ref(null)
const toIntId = (v) => {
  const n = Number(v)
  return Number.isFinite(n) && n > 0 ? n : 1
}
const doctorId = computed(() => toIntId(doctorIdRef.value ?? userStore.userInfo?.doctorId ?? 1))

// 初始化页头的医生信息
const initDoctorInfo = async () => {
  try {
    userStore.initFromStorage()
    const userId = userStore.userInfo?.userId
    console.log('initDoctorInfo: userId', userId)
    console.log('initDoctorInfo: userStore.userInfo', userStore.userInfo)
    
    if (!userId) {
      console.warn('initDoctorInfo: userId 为空')
      // 如果 userId 为空，尝试使用 doctorId
      const storedDoctorId = userStore.userInfo?.doctorId
      if (storedDoctorId) {
        console.log('initDoctorInfo: 使用存储的 doctorId', storedDoctorId)
        doctorIdRef.value = toIntId(storedDoctorId)
        // 尝试通过 doctorId 获取医生信息
        try {
          const doctorDetail = await doctorApi.getDoctorDetail(storedDoctorId)
          doctorInfo.value = {
            name: doctorDetail?.doctorName || doctorDetail?.name || doctorDetail?.realname || '未命名',
            department: doctorDetail?.deptName || doctorDetail?.departmentName || doctorDetail?.department || '—'
          }
        } catch (e) {
          console.error('initDoctorInfo: 通过 doctorId 获取医生信息失败', e)
        }
      }
      return
    }
    
    console.log('initDoctorInfo: 开始获取医生资料，userId:', userId)
    console.log('initDoctorInfo: doctorApi 对象', doctorApi)
    console.log('initDoctorInfo: doctorApi.getProfileByUserId 是否存在', typeof doctorApi?.getProfileByUserId)
    
    // 尝试使用 getProfileByUserId，如果不存在则使用 getMyProfile 或其他方法
    let profile = null
    if (typeof doctorApi?.getProfileByUserId === 'function') {
      console.log('initDoctorInfo: 使用 getProfileByUserId 方法')
      profile = await doctorApi.getProfileByUserId(userId)
    } else if (typeof doctorApiMain?.getProfileByUserId === 'function') {
      console.log('initDoctorInfo: 使用备用 API (doctor.ts) 的 getProfileByUserId')
      profile = await doctorApiMain.getProfileByUserId(userId)
    } else if (typeof doctorApi?.getMyProfile === 'function') {
      console.log('initDoctorInfo: 使用 getMyProfile 方法')
      profile = await doctorApi.getMyProfile()
    } else if (typeof doctorApiMain?.getMyProfile === 'function') {
      console.log('initDoctorInfo: 使用备用 API (doctor.ts) 的 getMyProfile')
      profile = await doctorApiMain.getMyProfile()
    } else {
      console.error('initDoctorInfo: 无法找到获取医生信息的方法')
      throw new Error('无法找到获取医生信息的方法')
    }
    console.log('initDoctorInfo: 获取到的医生资料', profile)
    
    if (profile) {
      doctorInfo.value = {
        name: profile?.doctorName || profile?.realname || profile?.name || '未命名',
        department: profile?.deptName || profile?.departmentName || profile?.department || '—'
      }
      doctorIdRef.value = toIntId(profile?.doctorId ?? null)
      console.log('initDoctorInfo: 设置医生信息', doctorInfo.value)
      console.log('initDoctorInfo: 设置 doctorId', doctorIdRef.value)
    } else {
      console.warn('initDoctorInfo: 未获取到医生资料')
    }
  } catch (e) {
    console.error('initDoctorInfo: 获取医生信息失败', e)
    // 如果获取失败，至少显示占位信息
    if (!doctorInfo.value.name) {
      doctorInfo.value = {
        name: '医生',
        department: '—'
      }
    }
  }
}

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
    // 检查 doctorId 是否有效
    if (!doctorId.value) {
      console.warn('loadScheduleData: doctorId 为空，等待初始化...')
      uniShowToast({ title: '正在获取医生信息...', icon: 'loading' })
      return
    }

    const sel = dateList.value[selectedDateIndex.value]
    if (!sel || !sel.fullDate) {
      console.warn('loadScheduleData: 日期选择无效')
      return
    }

    const startDate = fmtDate(sel.fullDate)
    const doctorIdNum = Number(doctorId.value)
    
    console.log('loadScheduleData: 查询参数', {
      doctorId: doctorIdNum,
      startDate: startDate,
      days: 1
    })
    
    if (isNaN(doctorIdNum) || doctorIdNum <= 0) {
      console.error('loadScheduleData: doctorId 无效', doctorId.value)
      uniShowToast({ title: '医生ID无效，请重新登录', icon: 'none' })
      return
    }
    
    const resp = await doctorApi.getSchedules(doctorIdNum, startDate, 1)
    console.log('loadScheduleData: API响应', resp)
    console.log('loadScheduleData: 响应类型', typeof resp, '是否为数组', Array.isArray(resp))
    
    if (!resp || (Array.isArray(resp) && resp.length === 0)) {
      console.warn('loadScheduleData: 未获取到排班数据')
      console.warn('loadScheduleData: 可能的原因：')
      console.warn('  1) 数据库中的 status 字段不是 1（后端只查询 status=1 的记录）')
      console.warn('  2) 数据库中的 doctor_id 不匹配')
      console.warn('  3) 数据库中的 schedule_date 格式不匹配')
      console.warn('loadScheduleData: 请检查数据库：')
      console.warn(`  SELECT * FROM doctor_schedule WHERE doctor_id = ${doctorIdNum} AND schedule_date = '${startDate}'`)
      scheduleList.value = []
      uniShowToast({ 
        title: `该日期(${startDate})暂无排班\n请检查数据库status字段是否为1`, 
        icon: 'none', 
        duration: 4000 
      })
      return
    }
    
    const schedules = Array.isArray(resp) ? resp : []
    console.log('loadScheduleData: 解析到', schedules.length, '条排班数据')
    
    scheduleList.value = schedules.map(s => ({
      scheduleId: s.id ?? s.scheduleId ?? 0,
      timePeriod: labelFromRange(s.timeRange || s.time_range || ''),
      roomNumber: s.roomNo || s.room_no || '',
      totalSlots: s.totalSlots || s.total_slots || 0,
      bookedSlots: s.bookedCount || s.booked_count || 0,
      remainingSlots: (s.totalSlots || s.total_slots || 0) - (s.bookedCount || s.booked_count || 0)
    }))
    
    console.log('loadScheduleData: 处理后的排班列表', scheduleList.value)
  } catch (e) {
    console.error('loadScheduleData: 获取排班数据失败', e)
    scheduleList.value = []
    uniShowToast({ title: '排班加载失败: ' + (e.message || '未知错误'), icon: 'none', duration: 3000 })
  }
} 

// 跳转到申请调班（映射到已存在的页面路径）
const goToAdjustSchedule = () => { 
  uniNavigateTo({ url: '/subpkg/doctor/schedule/apply' }) 
} 

// 跳转到患者列表（映射到已存在的页面路径）
const goToPatients = () => { 
  uniNavigateTo({ url: '/subpkg/doctor/patients/list' }) 
} 

// 跳转到个人信息（映射到已存在的页面路径）
const goToProfile = () => { 
  uniNavigateTo({ url: '/subpkg/doctor/profile/index' }) 
}

// 刷新排班数据
const handleRefresh = async () => {
  uniShowToast({ title: '正在刷新...', icon: 'loading' })
  try {
    await loadScheduleData()
    uniShowToast({ title: '刷新成功', icon: 'success' })
  } catch (error) {
    uniShowToast({ title: '刷新失败', icon: 'error' })
  }
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
  initDoctorInfo().finally(loadScheduleData)
}) 
</script> 

<style scoped> 
.schedule-page { 
  min-height: 100vh; 
  background: linear-gradient(180deg, #e8f3ff 0%, #f7faff 45%, #ffffff 100%); 
  padding-bottom: 140rpx; 
} 

/* 顶部医生信息 */ 
.doctor-header { 
  background: linear-gradient(120deg, #3a9cff 0%, #6ec6ff 100%); 
  padding: 28rpx 24rpx; 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  box-shadow: 0 10rpx 30rpx rgba(58,156,255,0.22);
} 
.doctor-info { color: #fff; } 
.doctor-name { font-size: 36rpx; font-weight: 800; letter-spacing: 1rpx; } 
.doctor-dept { font-size: 26rpx; opacity: 0.9; margin-top: 6rpx; display: block; } 
.today-date { color: #fff; font-size: 26rpx; opacity: 0.92; } 

/* 日期选择器 */ 
.date-selector { margin-top: 12rpx; background: transparent; } 
.date-scroll { white-space: nowrap; padding: 16rpx 24rpx; } 
.date-item { 
  display: inline-flex; 
  flex-direction: column; 
  align-items: center; 
  justify-content: center; 
  width: 124rpx; 
  height: 124rpx; 
  margin-right: 14rpx; 
  background: #fff; 
  border-radius: 18rpx; 
  box-shadow: 0 10rpx 24rpx rgba(58,156,255,0.12); 
  color: #334155; 
  border: 2rpx solid transparent;
  transition: all 0.2s ease;
} 
.date-item.active { 
  background: linear-gradient(180deg, #e6f1ff 0%, #ffffff 100%); 
  border-color: #3a9cff; 
  color: #2176ff; 
  transform: translateY(-4rpx);
} 
.date-weekday { font-size: 24rpx; } 
.date-day { font-size: 38rpx; font-weight: 700; } 
.date-month { font-size: 24rpx; color: #64748b; } 

/* 排班列表 */ 
.schedule-container { padding: 12rpx 24rpx 24rpx; } 
.section-title .title-text { font-size: 30rpx; color: #1e293b; font-weight: 700; } 
.empty-state { 
  background: #fff; 
  border-radius: 18rpx; 
  padding: 36rpx; 
  text-align: center; 
  color: #64748b; 
  margin-top: 16rpx; 
  box-shadow: 0 10rpx 24rpx rgba(58,156,255,0.08);
} 
.schedule-list { margin-top: 16rpx; } 
.schedule-item { 
  background: #fff; 
  border-radius: 18rpx; 
  padding: 18rpx; 
  margin-bottom: 14rpx; 
  box-shadow: 0 10rpx 24rpx rgba(58,156,255,0.12); 
  border: 1rpx solid #eef3fb;
} 
.schedule-header { display: flex; justify-content: space-between; align-items: center; } 
.time-period { display: flex; align-items: center; gap: 8rpx; } 
.period-icon { font-size: 26rpx; } 
.period-text { font-size: 28rpx; font-weight: 700; color: #0f172a; } 
.status-tag { 
  padding: 8rpx 16rpx; 
  border-radius: 999rpx; 
  font-size: 24rpx; 
  font-weight: 600;
} 
.status-available { background: #e6f4ff; color: #0b6cc9; } 
.status-busy { background: #fff7e6; color: #b45309; } 
.status-full { background: #ffecec; color: #b91c1c; } 

.schedule-info { 
  display: grid; 
  grid-template-columns: 1fr 1fr; 
  gap: 10rpx 14rpx; 
  margin-top: 14rpx; 
} 
.info-item { display: flex; align-items: center; } 
.info-label { color: #64748b; font-size: 26rpx; } 
.info-value { color: #0f172a; font-size: 26rpx; font-weight: 700; } 
.appointment-count { color: #0ea5e9; } 
.remaining { color: #10b981; } 
.low-remaining { color: #ef4444; } 

/* 操作按钮 */ 
.action-section { 
  padding: 4rpx 24rpx 24rpx; 
  display: flex; 
  gap: 18rpx; 
} 
.action-btn { 
  flex: 1; 
  color: #fff; 
  border-radius: 16rpx; 
  padding: 18rpx; 
  font-size: 28rpx; 
  text-align: center;
  box-shadow: 0 10rpx 24rpx rgba(58,156,255,0.18);
} 
.refresh-btn { 
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%); 
  box-shadow: 0 10rpx 24rpx rgba(16,185,129,0.18); 
}
.adjust-btn { 
  background: linear-gradient(135deg, #3a9cff 0%, #6ec6ff 100%); 
  box-shadow: 0 10rpx 24rpx rgba(58,156,255,0.22); 
} 

/* 底部导航 */ 
.bottom-nav { 
  position: fixed; 
  left: 0; right: 0; bottom: 0; 
  background: #fff; 
  border-top: 1rpx solid #e2e8f0; 
  display: flex; 
  padding: 12rpx 24rpx; 
  gap: 12rpx; 
  box-shadow: 0 -6rpx 18rpx rgba(0,0,0,0.04);
} 
.nav-item { 
  flex: 1; 
  background: linear-gradient(135deg, #3a9cff 0%, #6ec6ff 100%); 
  color: #fff; 
  border-radius: 14rpx; 
  padding: 16rpx 0; 
  text-align: center; 
  box-shadow: 0 8rpx 18rpx rgba(58,156,255,0.2); 
} 
.nav-icon { display: block; font-size: 28rpx; } 
.nav-text { display: block; font-size: 26rpx; font-weight: 600; margin-top: 6rpx; } 
</style>