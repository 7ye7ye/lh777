<template>
  <view class="detail-container">
    <!-- 内容区域 - 不可滚动 -->
    <view class="content-area">
      <view class="detail-bg">
        <!-- 医生基本信息 -->
        <view class="doctor-card card">
          <view class="doctor-content">
            <view class="doctor-avatar-wrapper">
              <view class="doctor-avatar">
                {{ (doctor.doctorName || doctor.name || '医').charAt(0) }}
              </view>
            </view>
            <view class="doctor-info">
              <view class="name-title-row">
                <text class="name">{{ doctor.doctorName || doctor.name || '未知医生' }}</text>
                <text class="title-badge">{{ doctor.title || '医师' }}</text>
              </view>
              <view class="specialty-row">
                <text class="specialty-label">擅长:</text>
                <text class="specialty-text">{{ doctor.specialty || '暂无' }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 所属科室 -->
        <view class="section-card dept-card card" v-if="departmentInfo" @click="navigateToDepartment">
          <view class="section-title">所属科室</view>
          <view class="dept-info">
            <text class="dept-name">{{ departmentInfo.deptName }}</text>
            <text class="arrow">›</text>
          </view>
        </view>

        <!-- 医生简介 -->
        <view class="section-card intro-card card">
          <view class="section-title">医生简介</view>
          <view class="section-content">
            {{ doctor.doctorDesc || '暂无医生简介' }}
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 - 固定在底部 -->
    <view class="action-buttons">
      <view class="action-btn primary" @click="navigateToRegister">
        <view class="btn-icon primary-icon">📅</view>
        <text class="btn-text">预约挂号</text>
      </view>
      <view class="action-btn secondary" @click="handleConsult">
        <view class="btn-icon secondary-icon">?</view>
        <text class="btn-text">在线咨询</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { doctorApi } from '../../api/doctor_massage'
import { getDepartmentDetail } from '../../api/department'
import { getDoctorSchedules } from '../../api/registration'
import { getStaticImage } from '@/utils/imageHelper'

const doctorId = ref('')
const doctor = ref({})
const departmentInfo = ref(null)
const doctorTypeNames = ref('')

// 加载医生详情
const loadDoctorDetail = async () => {
  try {
    const res = await doctorApi.getDoctorDetail(doctorId.value)
    console.log('医生详情数据:', res)

    // 处理不同的响应格式
    let data = res
    if (res && res.data) {
      data = res.data
    } else if (res && res.result) {
      data = res.result
    }

    if (data) {
      doctor.value = data
      // 加载所属科室信息
      if (data.deptId) {
        loadDepartmentInfo(data.deptId)
      }
      // 加载号别类型信息
      if (data.doctorId) {
        loadDoctorTypeNames(data.doctorId)
      }
    } else {
      console.warn('医生详情数据格式异常:', res)
      uni.showToast({
        title: '数据格式异常',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('加载医生详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

// 加载科室信息
const loadDepartmentInfo = async (deptId) => {
  if (!deptId) {
    console.warn('科室ID为空，跳过加载')
    return
  }

  try {
    const res = await getDepartmentDetail(deptId)
    // 响应拦截器已经解包了 result，所以 res 直接是数据
    let data = res
    if (res && typeof res === 'object' && !Array.isArray(res)) {
      // 如果还是包装格式，尝试解包
      if (res.result) {
        data = res.result
      } else if (res.data) {
        data = res.data
      }
    }
    if (data) {
      departmentInfo.value = data
    }
  } catch (error) {
    console.error('加载科室信息失败:', error)
    // 网络请求失败时不显示错误提示，避免影响用户体验
    // 科室信息不是关键信息，可以继续使用
    // 如果需要显示，可以取消下面的注释
    // uni.showToast({
    //   title: '加载科室信息失败，请检查网络连接',
    //   icon: 'none',
    //   duration: 2000
    // })
  }
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 跳转到科室详情
const navigateToDepartment = () => {
  if (departmentInfo.value && departmentInfo.value.deptId) {
    uni.navigateTo({
      url: `/subpkg/hospital/department-detail?deptId=${departmentInfo.value.deptId}`
    })
  }
}

// 预约挂号
const navigateToRegister = () => {
  if (!doctor.value.doctorId) {
    uni.showToast({
      title: '医生信息不完整',
      icon: 'none'
    })
    return
  }

  // 优先使用 departmentInfo，如果没有则使用 doctor 中的 deptId
  const deptId = departmentInfo.value?.deptId || doctor.value.deptId
  const deptName = departmentInfo.value?.deptName || doctor.value.deptName || ''

  // 构建 URL 查询参数传给 appointment 页面
  const query = `doctorId=${doctor.value.doctorId}` +
    `&doctorName=${encodeURIComponent(doctor.value.doctorName || '')}` +
    `&title=${encodeURIComponent(doctor.value.title || '')}` +
    `&specialty=${encodeURIComponent(doctor.value.specialty || '')}` +
    `&deptId=${deptId || ''}` +
    `&deptName=${encodeURIComponent(deptName)}` +
    `&avatar=${encodeURIComponent(doctor.value.avatar || '')}`

  uni.navigateTo({
    url: `/subpkg/hospital/appointment?${query}`
  })
}


// 在线咨询
const handleConsult = () => {
  uni.showToast({
    title: '在线咨询功能开发中',
    icon: 'none'
  })
}

// 加载医生的号别类型
const loadDoctorTypeNames = async (doctorId) => {
  try {
    // 获取未来7天的排班信息
    const today = new Date().toISOString().split('T')[0]
    const res = await getDoctorSchedules(doctorId, today, 7)

    let schedules = []
    if (Array.isArray(res?.result)) schedules = res.result
    else if (Array.isArray(res?.data)) schedules = res.data
    else if (Array.isArray(res)) schedules = res

    // 提取所有不同的号别类型
    const typeNames = schedules
      .map(schedule => schedule.doctor_title_type_name || schedule.doctorTitleTypeName)
      .filter(typeName => typeName && typeName.trim() !== '')
      .filter((value, index, self) => self.indexOf(value) === index) // 去重

    doctorTypeNames.value = typeNames.length > 0 ? typeNames.join(' / ') : ''
  } catch (error) {
    console.error('加载医生号别类型失败:', error)
    // 失败时不显示号别类型，不影响其他功能
    doctorTypeNames.value = ''
  }
}

onLoad((query) => {
  // 优先使用id参数（从department-booking页面传递），其次使用doctorId
  doctorId.value = query?.id || query?.doctorId || ''

  // 如果直接传递了医生信息，先显示基本信息
  if (query?.name || query?.title || query?.specialty) {
    doctor.value = {
      doctorId: doctorId.value,
      doctorName: query?.name || '未知医生',
      title: query?.title || '',
      specialty: query?.specialty || '',
      doctorDesc: '医生详情加载中...'
    }
  }

  // 无论是否有初始信息，都尝试加载完整详情
  if (doctorId.value) {
    loadDoctorDetail()
  }
})
</script>

<style scoped>
.detail-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f8faff;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.content-area {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.detail-bg {
  background: #f8faff;
  flex: 1;
  overflow-y: auto;
  padding-top: calc(env(safe-area-inset-top) + 32rpx);
  padding-bottom: 32rpx;
}

/* 卡片基础样式 - 按照home.vue样式 */
.card {
  background: #fff;
  border-radius: 16rpx;
  margin: 16rpx 24rpx 0 24rpx;
  padding: 16rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

/* 医生卡片 - 按照home.vue就诊卡样式 */
.doctor-card {
  position: relative;
  background: linear-gradient(135deg, #e6f4ff 0%, #cce7ff 50%, #b3d9ff 100%);
  border-radius: 24rpx;
  padding: 40rpx 24rpx;
  margin: 0 24rpx 32rpx 24rpx;
  box-shadow: 0 6rpx 24rpx rgba(58, 156, 255, 0.2);
  overflow: hidden;
  border: 2rpx solid rgba(255, 255, 255, 0.5);
}

.doctor-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 70%);
  pointer-events: none;
}

.doctor-content {
  position: relative;
  display: flex;
  align-items: center;
  z-index: 1;
}

.doctor-avatar-wrapper {
  position: relative;
  margin-right: 24rpx;
}

.doctor-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background: linear-gradient(135deg, #1a73e8 0%, #4a90e2 50%, #6ec6ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  color: #ffffff;
  font-weight: bold;
  box-shadow: 0 6rpx 16rpx rgba(26, 115, 232, 0.3);
  border: 3rpx solid rgba(255, 255, 255, 0.8);
}

.doctor-info {
  flex: 1;
}

.name-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
  flex-wrap: wrap;
}

.name {
  font-size: 36rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
  color: #052a4a !important;
  line-height: 1.3;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.title-badge {
  font-size: 24rpx;
  color: #1a73e8;
  background: linear-gradient(135deg, #ffffff 0%, #f0f8ff 100%);
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  font-weight: 600;
  box-shadow: 0 2rpx 8rpx rgba(58, 156, 255, 0.2);
  border: 1rpx solid rgba(58, 156, 255, 0.3);
}

.specialty-row {
  display: flex;
  align-items: flex-start;
  gap: 8rpx;
}

.specialty-label {
  font-size: 28rpx;
  color: #1a73e8 !important;
  font-weight: 600;
  white-space: nowrap;
}

.specialty-text {
  font-size: 28rpx;
  color: #2c5aa0 !important;
  line-height: 1.6;
  flex: 1;
  font-weight: 500;
}

/* 卡片样式 - 按照home.vue样式 */
.section-card {
  background: #fff;
  border-radius: 16rpx;
  margin: 0 24rpx 32rpx 24rpx;
  padding: 32rpx 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
  border: 2rpx solid rgba(58, 156, 255, 0.15);
  transition: all 0.3s ease;
}

.section-card:active {
  background-color: rgba(58, 156, 255, 0.08);
  transform: scale(0.98);
}

.intro-card {
  margin-top: 0;
}

.dept-card {
  margin-top: 0;
  cursor: pointer;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.section-content {
  font-size: 28rpx;
  color: #555;
  line-height: 1.8;
  text-align: justify;
  letter-spacing: 0.3rpx;
}

.dept-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dept-name {
  font-size: 28rpx;
  color: #3a9cff;
  font-weight: 500;
}

.arrow {
  font-size: 28rpx;
  color: #999;
  font-weight: 300;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
  gap: 16rpx;
  flex-shrink: 0;
}

.action-btn {
  flex: 1;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 48rpx 20rpx;
  transition: all 0.3s ease;
  position: relative;
}

.btn-icon {
  font-size: 32rpx;
  line-height: 1;
}

.btn-text {
  font-size: 30rpx;
  font-weight: 600;
}

.action-btn.primary {
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  box-shadow: 0 8rpx 24rpx rgba(74, 144, 226, 0.25);
}

.action-btn.primary:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.2);
}

.action-btn.primary .btn-icon {
  color: #fff;
}

.action-btn.primary .btn-text {
  color: #fff;
}

.action-btn.secondary {
  background: #fff;
  border: 2rpx solid #e0e0e0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.action-btn.secondary:active {
  background: #f5f5f5;
  border-color: #d0d0d0;
  transform: scale(0.98);
}

.action-btn.secondary .btn-icon {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #3a9cff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: bold;
}

.action-btn.secondary .btn-text {
  color: #333;
}
</style>







