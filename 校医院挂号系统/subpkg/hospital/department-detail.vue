<template>
  <view class="detail-bg">
    <!-- 科室信息 -->
    <view class="dept-info">
      <view class="title">{{ department.deptName }}</view>
      <view class="desc">
        <text>科室介绍：</text>
        <text>{{ department.deptDesc || '暂无介绍' }}</text>
      </view>
      <view class="location" v-if="department.location">
        <text>位置：{{ department.location }}</text>
      </view>
    </view>

    <!-- 体检套餐快捷入口 (仅常规体检显示) -->
    <view class="package-banner" v-if="deptId === '43' || deptId === 43" @click="goToPackages">
      <view class="banner-left">
        <view class="banner-title">💎 精选体检套餐</view>
        <view class="banner-desc">三种套餐可选 · 价格透明 · 专业体检</view>
        <view class="price-tags">
          <text class="tag">基础 ¥280</text>
          <text class="tag">标准 ¥480</text>
          <text class="tag">深度 ¥880</text>
        </view>
      </view>
      <view class="banner-right">
        <view class="enter-btn">查看套餐 ›</view>
      </view>
    </view>

    <!-- 医生列表 -->
    <view class="doctor-list">
      <view class="list-title">出诊医生</view>
      <view 
        v-for="(doctor, index) in doctorList" 
        :key="index" 
        class="doctor-item"
        @click="navigateToDoctorDetail(doctor)"
      >
        <view class="doctor-avatar">
          {{ (doctor.doctorName || doctor.name || '医').charAt(0) }}
        </view>
        <view class="doctor-info">
          <view class="name-title">
            <text class="name">{{ doctor.doctorName }}</text>
            <view class="title-row">
              <text class="title">{{ doctor.title }}</text>
              <text v-if="doctorTypeNames[doctor.doctorId]" class="type-name">
                {{ doctorTypeNames[doctor.doctorId] }}
              </text>
            </view>
          </view>
          <view class="specialty">擅长：{{ doctor.specialty }}</view>
        <!-- 今日排班状态 -->
        <view class="schedule-status-row">
          <view class="schedule-badge" :class="getDoctorScheduleStatusClass(doctor.doctorId)">
            <view class="schedule-badge__dot"></view>
            <text class="schedule-badge__text">
              {{ getDoctorScheduleStatusText(doctor.doctorId) }}
            </text>
          </view>
        </view>
        </view>
      </view>
      <view class="empty" v-if="doctorList.length === 0">
        暂无出诊医生信息
      </view>
    </view>
    
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getDepartmentDetail } from '../../api/department'
import { getDoctorsByDeptId } from '../../api/doctor_massage'
import { getRegistrationTypes, getDoctorSchedules } from '../../api/registration'

const deptId = ref('')
const department = ref({ deptName: '加载中...', deptDesc: '', location: '' })
const doctorList = ref([])
const doctorTypeNames = ref({}) // 存储每个医生的号别类型
const registrationTypeNameMap = ref({})
// 每个医生当天排班状态：{ [doctorId]: { hasSchedule, hasAvailable, slotsText } }
const doctorScheduleStatusMap = ref({})

const loadRegistrationTypeNameMap = async () => {
  if (registrationTypeNameMap.value && Object.keys(registrationTypeNameMap.value).length > 0) return
  try {
    const res = await getRegistrationTypes()
    const types = Array.isArray(res?.result) ? res.result : Array.isArray(res?.data) ? res.data : Array.isArray(res) ? res : []
    const map = {}
    types.forEach((t) => {
      const id = t?.typeId ?? t?.type_id ?? t?.id
      const name = t?.typeName ?? t?.type_name ?? t?.name
      if (id != null && name) map[String(id)] = name
    })
    registrationTypeNameMap.value = map
  } catch (e) {
    registrationTypeNameMap.value = {}
  }
}

// 加载科室详情
const loadDepartmentDetail = async () => {
  try {
    // 重置department状态为初始值
    department.value = { deptName: '加载中...', deptDesc: '', location: '' }
    
    // 验证deptId的有效性
    if (!deptId.value || (typeof deptId.value === 'string' && deptId.value.trim() === '')) {
      console.error('无效的科室ID:', deptId.value)
      department.value = { 
        deptName: '科室ID无效', 
        deptDesc: '请检查科室ID是否正确', 
        location: '' 
      }
      uni.showToast({
        title: '科室ID无效',
        icon: 'none'
      })
      return
    }
    
    console.log(`正在获取科室详情，deptId: ${deptId.value}`)
    const res = await getDepartmentDetail(deptId.value)
    console.log('科室详情API响应:', res)
    
    // 增强的空值检查和错误处理
    if (res === null) {
      console.warn('API返回null数据，科室可能不存在或未设置详细信息')
      department.value = { 
        deptName: '科室信息未设置', 
        deptDesc: '该科室尚未完善详细信息', 
        location: '' 
      }
      uni.showToast({
        title: '科室信息未完善',
        icon: 'none'
      })
      return
    }
    
    if (res === undefined) {
      console.error('API返回undefined，可能是请求失败或无响应')
      department.value = { 
        deptName: '加载异常', 
        deptDesc: '获取科室信息时出现未知异常', 
        location: '' 
      }
      uni.showToast({
        title: '加载异常，请重试',
        icon: 'none'
      })
      return
    }
    
    // 处理不同的响应格式
    let data = res
    if (res && res.data) {
      data = res.data
    } else if (res && res.result) {
      data = res.result
    }
    
    // 检查data是否为有效对象
    if (!data || typeof data !== 'object') {
      console.warn('科室详情数据不是有效对象:', res)
      department.value = { 
        deptName: '数据格式异常', 
        deptDesc: '返回的数据格式不符合预期', 
        location: '' 
      }
      uni.showToast({
        title: '数据格式异常',
        icon: 'none'
      })
      return
    }
    
    // 最终数据处理，确保即使缺少某些字段也能正常显示
    department.value = {
      deptName: data.deptName || '科室名称未知',
      deptDesc: data.deptDesc || '暂无科室介绍',
      location: data.location || '位置信息未提供',
      ...data
    }
    
  } catch (error) {
    console.error('加载科室详情失败:', error)
    // 更详细的错误提示
    let errorMessage = '获取科室信息时出错'
    if (error.message && error.message.includes('404')) {
      errorMessage = '未找到该科室信息'
    } else if (error.message && error.message.includes('网络')) {
      errorMessage = '网络连接异常，请检查网络'
    }
    
    department.value = { 
      deptName: '加载失败', 
      deptDesc: errorMessage,
      location: '' 
    }
    
    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 2000
    })
  }
}

// 加载科室医生列表
const loadDoctorsByDeptId = async () => {
  try {
    // 重置医生列表为空数组
    doctorList.value = []
    
    // 验证deptId的有效性
    if (!deptId.value || (typeof deptId.value === 'string' && deptId.value.trim() === '')) {
      console.error('无效的科室ID，无法加载医生列表:', deptId.value)
      return
    }
    
    console.log(`正在获取科室医生列表，deptId: ${deptId.value}`)
    const res = await getDoctorsByDeptId(deptId.value)
    console.log('医生列表API响应:', res)
    
    // 增强的空值检查和错误处理
    if (res === null || res === undefined) {
      console.warn(`API返回${res === null ? 'null' : 'undefined'}数据，医生列表可能为空或获取失败`)
      doctorList.value = []
      return
    }
    
    // 处理不同的响应格式
    let data = res
    if (res && res.data) {
      data = res.data
    } else if (res && res.result) {
      data = res.result
    }
    
    // 验证数据是否为有效数组
    if (!data || !Array.isArray(data)) {
      console.warn('医生列表数据不是有效数组:', res)
      doctorList.value = []
      return
    }
    
    // 确保即使是空数组也能正常显示
    doctorList.value = data
    // 异步加载每个医生的号别类型
    loadDoctorTypeNamesForList(data)
    // 异步检查每个医生当天的排班状态
    loadDoctorScheduleStatusForList(data)
  } catch (error) {
    console.error('加载科室医生列表失败:', error)
    doctorList.value = []
    // 可以根据需要添加错误提示
  }
}

// 跳转到医生详情
const navigateToDoctorDetail = (doctor) => {
  uni.navigateTo({
    url: `/subpkg/hospital/doctor-detail?doctorId=${doctor.doctorId}`
  })
}

// 加载医生列表的号别类型
const loadDoctorTypeNamesForList = async (doctors) => {
  if (!Array.isArray(doctors)) return

  await loadRegistrationTypeNameMap()
  const map = registrationTypeNameMap.value || {}
  const result = {}
  doctors.forEach((doctor) => {
    const doctorId = doctor?.doctorId ?? doctor?.id
    if (!doctorId) return
    const titleId = doctor?.titleId ?? doctor?.title_id ?? doctor?.doctorTitleTypeId ?? doctor?.doctor_title_type_id
    const typeName = titleId != null ? (map[String(titleId)] || '') : ''
    if (typeName) result[String(doctorId)] = typeName
  })
  doctorTypeNames.value = result
}

// 将 time_slot 数字转换为文字
const getTimeSlotLabel = (slot) => {
  const n = Number(slot)
  if (n === 1) return '上午'
  if (n === 2) return '下午'
  if (n === 3) return '晚上'
  return ''
}

// 加载每个医生当天的排班状态
const loadDoctorScheduleStatusForList = async (doctors) => {
  if (!Array.isArray(doctors) || doctors.length === 0) {
    doctorScheduleStatusMap.value = {}
    return
  }

  const today = new Date().toISOString().split('T')[0]
  const statusMap = { ...doctorScheduleStatusMap.value }
  const now = new Date()

  const promises = doctors.map(async (doctor) => {
    const doctorId = doctor?.doctorId ?? doctor?.id
    if (!doctorId) return

    try {
      const res = await getDoctorSchedules(doctorId, today, 1)
      let schedules = []
      if (Array.isArray(res?.result)) schedules = res.result
      else if (Array.isArray(res?.data)) schedules = res.data
      else if (Array.isArray(res)) schedules = res

      const todaySchedules = (schedules || []).filter((s) => {
        const dateStr = (s.schedule_date || s.scheduleDate || '').toString().substring(0, 10)
        return dateStr === today
      })

      if (!todaySchedules.length) {
        statusMap[String(doctorId)] = {
          hasSchedule: false,
          hasAvailable: false,
          slotsText: '今日无排班'
        }
        return
      }

      let hasAvailable = false
      const slotSet = new Set()
      let hasFutureSchedule = false

      todaySchedules.forEach((s) => {
        const remain = Number(
          (s.available_quota ?? s.availableQuota ?? s.remaining_quota ?? s.remainingQuota ?? 0)
        )
        const total = Number(
          (s.max_quota ?? s.maxQuota ?? s.totalQuota ?? 0)
        )

        const slot = s.time_slot ?? s.timeSlot
        const label = getTimeSlotLabel(slot)

        // 与挂号页一致：按“就诊前2小时截止”计算是否过期
        const scheduleDateStr = (s.schedule_date || s.scheduleDate || today).toString().substring(0, 10)
        const scheduleDateObj = new Date(scheduleDateStr.replace(/-/g, '/'))
        const slotNum = Number(slot)

        // 默认上午 8 点；下午 14 点；晚上 18 点
        let slotStartHour = 8
        if (slotNum === 2) slotStartHour = 14
        else if (slotNum === 3) slotStartHour = 18

        const slotStartTime = new Date(scheduleDateObj)
        slotStartTime.setHours(slotStartHour, 0, 0, 0)
        const cutoffTime = new Date(slotStartTime.getTime() - 2 * 60 * 60 * 1000)
        const isExpired = now >= cutoffTime

        // 只统计“尚未截止”的排班时段
        if (!isExpired) {
          hasFutureSchedule = true

          if (!Number.isNaN(remain) && remain > 0) {
            hasAvailable = true
          } else if (Number.isNaN(remain) && !Number.isNaN(total) && total > 0) {
            hasAvailable = true
          }

          if (label) slotSet.add(label)
        }
      })

      let slotsText = ''
      if (hasFutureSchedule && slotSet.size > 0) {
        slotsText = Array.from(slotSet).join(' / ')
      } else if (!hasFutureSchedule) {
        // 有今天的排班记录，但全部已截止
        slotsText = '今日排班已截止'
      } else {
        slotsText = '今日有排班'
      }

      statusMap[String(doctorId)] = {
        hasSchedule: true,
        hasAvailable,
        slotsText
      }
    } catch (e) {
      console.error(`加载医生${doctorId}当天排班失败:`, e)
      statusMap[String(doctorId)] = {
        hasSchedule: false,
        hasAvailable: false,
        slotsText: '排班信息获取失败'
      }
    }
  })

  await Promise.all(promises)
  doctorScheduleStatusMap.value = statusMap
}

// 获取某个医生的排班状态文案
const getDoctorScheduleStatusText = (doctorId) => {
  if (!doctorId) return '今日排班信息未知'
  const status = doctorScheduleStatusMap.value[String(doctorId)]
  if (!status) return '今日排班信息加载中...'

  if (!status.hasSchedule) return status.slotsText || '今日无排班'

  if (status.hasAvailable) {
    return status.slotsText ? `今日可预约 · ${status.slotsText}` : '今日可预约'
  }

  return status.slotsText ? `今日排班已满 · ${status.slotsText}` : '今日排班已满'
}

// 获取某个医生排班状态对应的样式类
const getDoctorScheduleStatusClass = (doctorId) => {
  if (!doctorId) return 'schedule-badge--unknown'
  const status = doctorScheduleStatusMap.value[String(doctorId)]
  if (!status) return 'schedule-badge--loading'

  if (!status.hasSchedule) return 'schedule-badge--none'
  if (status.hasAvailable) return 'schedule-badge--available'
  return 'schedule-badge--full'
}

// 跳转到体检套餐页面
const goToPackages = () => {
  uni.navigateTo({
    url: '/subpkg/hospital/physical-exam-packages'
  })
}

onLoad((query) => {
  // 确保deptId有值且是有效的
  deptId.value = query?.deptId || ''
  console.log('加载科室详情，deptId:', deptId.value)
  
  if (deptId.value) {
    loadDepartmentDetail()
    loadDoctorsByDeptId()
  } else {
    console.warn('无效的科室ID')
    department.value = { 
      deptName: '科室ID无效', 
      deptDesc: '请检查科室ID是否正确', 
      location: '' 
    }
    uni.showToast({
      title: '科室ID无效',
      icon: 'none'
    })
  }
})
</script>

<style scoped>
.detail-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.dept-info {
  background: #fff;
  border-radius: 16rpx;
  margin: 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
  color: #333;
}

.desc {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 16rpx;
}

.location {
  font-size: 26rpx;
  color: #999;
}

/* 体检套餐横幅 */
.package-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  margin: 16rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.25);
  transition: all 0.3s;
}

.package-banner:active {
  transform: scale(0.98);
}

.banner-left {
  flex: 1;
}

.banner-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 12rpx;
}

.banner-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 16rpx;
}

.price-tags {
  display: flex;
  gap: 12rpx;
}

.price-tags .tag {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  backdrop-filter: blur(10rpx);
}

.banner-right {
  display: flex;
  align-items: center;
}

.enter-btn {
  background: #fff;
  color: #667eea;
  font-size: 26rpx;
  font-weight: bold;
  padding: 16rpx 32rpx;
  border-radius: 999rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.doctor-list {
  background: #fff;
  border-radius: 16rpx;
  margin: 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.list-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  color: #333;
}

.doctor-item {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #eee;
}

.doctor-item:last-child {
  border-bottom: none;
}

.doctor-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 44rpx;
  margin-right: 20rpx;
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38rpx;
  color: #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.3);
}

.doctor-info {
  flex: 1;
}

.name-title {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
  flex-wrap: wrap;
  gap: 8rpx;
}

.name {
  font-size: 30rpx;
  font-weight: bold;
  margin-right: 16rpx;
  color: #222;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
}

.title {
  font-size: 24rpx;
  color: #666;
  background-color: #f0f0f0;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
}

.type-name {
  font-size: 22rpx;
  color: #ff6b6b;
  background: rgba(255, 107, 107, 0.1);
  padding: 4rpx 10rpx;
  border-radius: 4rpx;
  font-weight: 500;
}

.specialty {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

/* 医生排班状态徽标 */
.schedule-status-row {
  margin-top: 10rpx;
  display: flex;
  align-items: center;
}

.schedule-badge {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  line-height: 1;
  border: 1rpx solid transparent;
}

.schedule-badge__dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  margin-right: 8rpx;
}

.schedule-badge__text {
  white-space: nowrap;
}

.schedule-badge--loading {
  background: rgba(58, 156, 255, 0.06);
  border-color: rgba(58, 156, 255, 0.18);
  color: #3a9cff;
}

.schedule-badge--loading .schedule-badge__dot {
  background: #3a9cff;
}

.schedule-badge--available {
  background: rgba(52, 199, 89, 0.08);
  border-color: rgba(52, 199, 89, 0.25);
  color: #34c759;
}

.schedule-badge--available .schedule-badge__dot {
  background: #34c759;
}

.schedule-badge--full {
  background: rgba(255, 149, 0, 0.08);
  border-color: rgba(255, 149, 0, 0.25);
  color: #ff9500;
}

.schedule-badge--full .schedule-badge__dot {
  background: #ff9500;
}

.schedule-badge--none {
  background: rgba(142, 142, 147, 0.06);
  border-color: rgba(142, 142, 147, 0.18);
  color: #8e8e93;
}

.schedule-badge--none .schedule-badge__dot {
  background: #8e8e93;
}

.schedule-badge--unknown {
  background: rgba(60, 60, 67, 0.06);
  border-color: rgba(60, 60, 67, 0.18);
  color: #3c3c43;
}

.schedule-badge--unknown .schedule-badge__dot {
  background: #3c3c43;
}

.empty {
  text-align: center;
  padding: 40rpx 0;
  color: #999;
  font-size: 28rpx;
}

.tabbar-placeholder { 
  height: 120rpx; 
}
</style>


