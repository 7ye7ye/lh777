<template>
  <view class="detail-bg">
    <!-- 医生基本信息 -->
    <view class="doctor-card">
      <view class="doctor-avatar">
        {{ (doctor.doctorName || doctor.name || '医').charAt(0) }}
      </view>
      <view class="doctor-info">
        <view class="name-title">
          <text class="name">{{ doctor.doctorName }}</text>
          <view class="title-row">
            <text class="title">{{ doctor.title }}</text>
            <text v-if="doctorTypeNames" class="type-name">
              {{ doctorTypeNames }}
            </text>
          </view>
        </view>
        <view class="specialty">擅长：{{ doctor.specialty }}</view>
        <view class="status" v-if="doctor.isActive === 1">
          <text class="status-dot"></text>
          <text>正常出诊</text>
        </view>
      </view>
    </view>

    <!-- 医生简介 -->
    <view class="section-card">
      <view class="section-title">医生简介</view>
      <view class="section-content">
        {{ doctor.doctorDesc || '暂无医生简介' }}
      </view>
    </view>

    <!-- 所属科室 -->
    <view class="section-card" v-if="departmentInfo">
      <view class="section-title">所属科室</view>
      <view class="dept-info" @click="navigateToDepartment">
        <text class="dept-name">{{ departmentInfo.deptName }}</text>
        <text class="arrow">></text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <view class="action-btn primary" @click="navigateToRegister">
        <image src="/static/register.svg" mode="widthFix" class="btn-icon"></image>
        <text>预约挂号</text>
      </view>
      <view class="action-btn" @click="handleConsult">
        <image src="/static/consult.svg" mode="widthFix" class="btn-icon"></image>
        <text>在线咨询</text>
      </view>
    </view>
    
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { doctorApi } from '../../api/doctor_massage'
import { getDepartmentDetail } from '../../api/department'
import { getDoctorSchedules } from '../../api/registration'

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
.detail-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 180rpx;
}

.doctor-card {
  background: linear-gradient(135deg, #3a9cff 0%, #1de9b6 100%);
  padding: 40rpx 24rpx;
  display: flex;
  align-items: center;
}

.doctor-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  margin-right: 24rpx;
  border: 4rpx solid rgba(255,255,255,0.3);
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  color: #ffffff;
}

.doctor-info {
  flex: 1;
}

.name-title {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
  flex-wrap: wrap;
  gap: 8rpx;
}

.name {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-right: 16rpx;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
}

.title {
  font-size: 24rpx;
  color: #fff;
  background: rgba(255,255,255,0.3);
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
}

.type-name {
  font-size: 22rpx;
  color: #fff;
  background: rgba(255, 107, 107, 0.4);
  padding: 4rpx 10rpx;
  border-radius: 4rpx;
  font-weight: 500;
}

.specialty {
  font-size: 28rpx;
  color: rgba(255,255,255,0.9);
  margin-bottom: 12rpx;
  line-height: 1.4;
}

.status {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: rgba(255,255,255,0.9);
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  background: #4caf50;
  border-radius: 50%;
  margin-right: 8rpx;
  display: inline-block;
}

.section-card {
  background: #fff;
  border-radius: 16rpx;
  margin: 16rpx 16rpx 0 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
  padding-bottom: 12rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.section-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.dept-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
}

.dept-info:active {
  background-color: #f5f5f5;
  border-radius: 8rpx;
}

.dept-name {
  font-size: 30rpx;
  color: #3a9cff;
}

.arrow {
  font-size: 32rpx;
  color: #ccc;
}

.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  padding: 16rpx;
  background: #fff;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05);
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
  border-radius: 12rpx;
  background: #f0f0f0;
  margin: 0 8rpx;
  font-size: 28rpx;
  color: #666;
}

.action-btn.primary {
  background: linear-gradient(135deg, #3a9cff 0%, #1de9b6 100%);
  color: #fff;
}

.action-btn:active {
  opacity: 0.8;
}

.btn-icon {
  width: 48rpx;
  margin-bottom: 8rpx;
}

.tabbar-placeholder { 
  height: 120rpx; 
}
</style>






