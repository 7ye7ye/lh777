<template>
  <view class="detail-container">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="navbar-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="navbar-title">医生详情</view>
      <view class="navbar-right">
        <text class="navbar-icon">⋯</text>
        <text class="navbar-icon">○</text>
      </view>
    </view>
    
    <!-- 内容区域 - 不可滚动 -->
    <view class="content-area">
      <view class="detail-bg">
        <!-- 医生基本信息 -->
        <view class="doctor-card">
          <view class="card-background"></view>
          <view class="doctor-content">
            <view class="doctor-avatar-wrapper">
              <view class="doctor-avatar">
                {{ (doctor.doctorName || doctor.name || '医').charAt(0) }}
              </view>
              <view class="avatar-ring"></view>
            </view>
            <view class="doctor-info">
              <view class="name-title">
                <text class="name">{{ doctor.doctorName || '未知医生' }}</text>
              </view>
              <view class="title-row">
                <text class="title">{{ doctor.title || '医师' }}</text>
                <text v-if="doctorTypeNames" class="type-name">
                  {{ doctorTypeNames }}
                </text>
              </view>
              <view class="specialty">
                <text class="specialty-label">擅长</text>
                <text class="specialty-text">{{ doctor.specialty || '暂无' }}</text>
              </view>
              <view class="status" v-if="doctor.isActive === 1">
                <view class="status-badge">
                  <text class="status-dot"></text>
                  <text class="status-text">正常出诊</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 医生简介 -->
        <view class="section-card intro-card">
          <view class="section-header">
            <view class="section-icon">📋</view>
            <view class="section-title">医生简介</view>
          </view>
          <view class="section-content">
            {{ doctor.doctorDesc || '暂无医生简介' }}
          </view>
        </view>

        <!-- 所属科室 -->
        <view class="section-card dept-card" v-if="departmentInfo" @click="navigateToDepartment">
          <view class="section-header">
            <view class="section-icon">🏥</view>
            <view class="section-title">所属科室</view>
          </view>
          <view class="dept-info">
            <view class="dept-content">
              <text class="dept-name">{{ departmentInfo.deptName }}</text>
              <text class="dept-desc" v-if="departmentInfo.deptDesc">{{ departmentInfo.deptDesc }}</text>
            </view>
            <view class="arrow-wrapper">
              <text class="arrow">›</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 - 固定在底部 -->
    <view class="action-buttons">
      <view class="action-btn primary" @click="navigateToRegister">
        <view class="btn-content">
          <view class="btn-icon-wrapper primary-icon">
            <text class="btn-icon-text">📅</text>
          </view>
          <text class="btn-text">预约挂号</text>
        </view>
      </view>
      <view class="action-btn secondary" @click="handleConsult">
        <view class="btn-content">
          <view class="btn-icon-wrapper secondary-icon">
            <text class="btn-icon-text">💬</text>
          </view>
          <text class="btn-text">在线咨询</text>
        </view>
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
  background: linear-gradient(180deg, #f0f7ff 0%, #f8faff 100%);
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
  background: linear-gradient(180deg, #f0f7ff 0%, #f8faff 100%);
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20rpx;
}

/* 导航栏 */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #3a9cff 0%, #1de9b6 100%);
  padding: 12rpx 20rpx;
  padding-top: calc(12rpx + env(safe-area-inset-top));
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2rpx 12rpx rgba(58, 156, 255, 0.2);
  flex-shrink: 0;
}

.navbar-left {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.navbar-left:active {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(0.95);
}

.back-icon {
  font-size: 36rpx;
  color: #fff;
  font-weight: 300;
  line-height: 1;
}

.navbar-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #fff;
  flex: 1;
  text-align: center;
}

.navbar-right {
  display: flex;
  gap: 12rpx;
  width: 48rpx;
  justify-content: flex-end;
}

.navbar-icon {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 医生卡片 */
.doctor-card {
  position: relative;
  background: linear-gradient(135deg, #3a9cff 0%, #1de9b6 100%);
  padding: 30rpx 20rpx 24rpx;
  margin-bottom: 16rpx;
  overflow: hidden;
}

.card-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.15) 0%, transparent 50%);
  pointer-events: none;
}

.doctor-content {
  position: relative;
  display: flex;
  align-items: flex-start;
  z-index: 1;
}

.doctor-avatar-wrapper {
  position: relative;
  margin-right: 20rpx;
}

.doctor-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.4);
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  color: #ffffff;
  font-weight: bold;
  box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 2;
}

.avatar-ring {
  position: absolute;
  top: -8rpx;
  left: -8rpx;
  right: -8rpx;
  bottom: -8rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.7;
  }
}

.doctor-info {
  flex: 1;
  padding-top: 4rpx;
}

.name-title {
  margin-bottom: 10rpx;
}

.name {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  letter-spacing: 0.5rpx;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
  margin-bottom: 12rpx;
}

.title {
  font-size: 22rpx;
  color: #fff;
  background: rgba(255, 255, 255, 0.25);
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  backdrop-filter: blur(10rpx);
  font-weight: 500;
}

.type-name {
  font-size: 20rpx;
  color: #fff;
  background: rgba(255, 107, 107, 0.5);
  padding: 4rpx 10rpx;
  border-radius: 16rpx;
  font-weight: 500;
  backdrop-filter: blur(10rpx);
}

.specialty {
  display: flex;
  align-items: flex-start;
  gap: 8rpx;
  margin-bottom: 12rpx;
  padding: 10rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10rpx;
  backdrop-filter: blur(10rpx);
}

.specialty-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
  white-space: nowrap;
}

.specialty-text {
  font-size: 24rpx;
  color: #fff;
  line-height: 1.4;
  flex: 1;
}

.status {
  margin-top: 6rpx;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 12rpx;
  background: rgba(76, 175, 80, 0.25);
  border-radius: 16rpx;
  backdrop-filter: blur(10rpx);
  border: 1rpx solid rgba(255, 255, 255, 0.3);
}

.status-dot {
  width: 10rpx;
  height: 10rpx;
  background: #4caf50;
  border-radius: 50%;
  margin-right: 8rpx;
  display: inline-block;
  box-shadow: 0 0 6rpx rgba(76, 175, 80, 0.6);
  animation: blink 2s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

.status-text {
  font-size: 22rpx;
  color: #fff;
  font-weight: 500;
}

/* 卡片样式 */
.section-card {
  background: #fff;
  border-radius: 16rpx;
  margin: 0 16rpx 12rpx;
  padding: 20rpx;
  box-shadow: 0 3rpx 16rpx rgba(58, 156, 255, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.section-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4rpx;
  background: linear-gradient(90deg, #3a9cff 0%, #1de9b6 100%);
}

.section-card:active {
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 24rpx rgba(58, 156, 255, 0.15);
}

.intro-card {
  margin-top: 12rpx;
}

.dept-card {
  cursor: pointer;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 12rpx;
  padding-bottom: 12rpx;
  border-bottom: 2rpx solid #f0f4f8;
}

.section-icon {
  font-size: 28rpx;
}

.section-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  flex: 1;
}

.section-content {
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
  text-align: justify;
  letter-spacing: 0.3rpx;
}

.dept-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4rpx 0;
}

.dept-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.dept-name {
  font-size: 26rpx;
  color: #3a9cff;
  font-weight: 600;
}

.dept-desc {
  font-size: 22rpx;
  color: #999;
  line-height: 1.3;
}

.arrow-wrapper {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f0f7ff;
  transition: all 0.3s ease;
}

.dept-card:active .arrow-wrapper {
  background: #e0f0ff;
  transform: translateX(4rpx);
}

.arrow {
  font-size: 28rpx;
  color: #3a9cff;
  font-weight: 300;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  padding: 16rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, #fff 100%);
  backdrop-filter: blur(20rpx);
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.08);
  z-index: 100;
  gap: 12rpx;
  flex-shrink: 0;
}

.action-btn {
  flex: 1;
  border-radius: 16rpx;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.action-btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.action-btn:active::after {
  width: 400rpx;
  height: 400rpx;
}

.btn-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20rpx 16rpx;
  min-height: 90rpx;
  position: relative;
  z-index: 1;
}

.btn-icon-wrapper {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10rpx;
  transition: all 0.3s ease;
}

.primary-icon {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0.2) 100%);
  backdrop-filter: blur(10rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.secondary-icon {
  background: linear-gradient(135deg, rgba(58, 156, 255, 0.15) 0%, rgba(29, 233, 182, 0.15) 100%);
  backdrop-filter: blur(10rpx);
  box-shadow: 0 4rpx 12rpx rgba(58, 156, 255, 0.2);
}

.btn-icon-text {
  font-size: 32rpx;
  line-height: 1;
}

.btn-text {
  font-size: 24rpx;
  font-weight: 600;
  letter-spacing: 0.5rpx;
}

.action-btn.primary {
  background: linear-gradient(135deg, #3a9cff 0%, #1de9b6 100%);
  box-shadow: 0 8rpx 24rpx rgba(58, 156, 255, 0.4);
}

.action-btn.primary .btn-text {
  color: #fff;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

.action-btn.secondary {
  background: linear-gradient(135deg, #ffffff 0%, #f8faff 100%);
  border: 2rpx solid #e0f0ff;
  box-shadow: 0 4rpx 16rpx rgba(58, 156, 255, 0.15);
}

.action-btn.secondary .btn-text {
  color: #3a9cff;
}

.action-btn:active {
  transform: scale(0.96);
}

.action-btn.primary:active {
  box-shadow: 0 6rpx 20rpx rgba(58, 156, 255, 0.5);
}

.action-btn.secondary:active {
  box-shadow: 0 2rpx 12rpx rgba(58, 156, 255, 0.25);
  background: linear-gradient(135deg, #f8faff 0%, #f0f7ff 100%);
}

.action-btn.primary::after {
  background: rgba(255, 255, 255, 0.2);
}

.action-btn.secondary::after {
  background: rgba(58, 156, 255, 0.1);
}
</style>






