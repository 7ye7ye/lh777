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
        <image 
          :src="doctor.avatar || '/static/images/default-avatar.png'" 
          mode="widthFix" 
          class="avatar"
        ></image>
        <view class="doctor-info">
          <view class="name-title">
            <text class="name">{{ doctor.doctorName }}</text>
            <text class="title">{{ doctor.title }}</text>
          </view>
          <view class="specialty">擅长：{{ doctor.specialty }}</view>
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
import { getDoctorsByDeptId } from '../../api/doctor'

const deptId = ref('')
const department = ref({})
const doctorList = ref([])

// 加载科室详情
const loadDepartmentDetail = async () => {
  try {
    const res = await getDepartmentDetail(deptId.value)
    console.log('科室详情数据:', res)
    
    // 处理不同的响应格式
    let data = res
    if (res && res.data) {
      data = res.data
    } else if (res && res.result) {
      data = res.result
    }
    
    if (data) {
      department.value = data
    } else {
      console.warn('科室详情数据格式异常:', res)
      uni.showToast({
        title: '数据格式异常',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('加载科室详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

// 加载科室医生
const loadDoctorsByDeptId = async () => {
  try {
    // 使用医生API获取科室医生列表
    const res = await getDoctorsByDeptId(deptId.value)
    
    // 处理不同的响应格式
    let data = res
    if (res && res.data) {
      data = res.data
    } else if (res && res.result) {
      data = res.result
    }
    
    if (data && Array.isArray(data)) {
      doctorList.value = data
    } else if (Array.isArray(res)) {
      doctorList.value = res
    } else {
      console.warn('医生列表数据格式异常:', res)
    }
  } catch (error) {
    console.error('加载科室医生列表失败:', error)
  }
}

// 跳转到医生详情
const navigateToDoctorDetail = (doctor) => {
  uni.navigateTo({
    url: `/subpkg/hospital/doctor-detail?doctorId=${doctor.doctorId}`
  })
}

// 跳转到体检套餐页面
const goToPackages = () => {
  uni.navigateTo({
    url: '/subpkg/hospital/physical-exam-packages'
  })
}

onLoad((query) => {
  deptId.value = query?.deptId || ''
  if (deptId.value) {
    loadDepartmentDetail()
    loadDoctorsByDeptId()
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

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  margin-right: 20rpx;
}

.doctor-info {
  flex: 1;
}

.name-title {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.name {
  font-size: 30rpx;
  font-weight: bold;
  margin-right: 16rpx;
  color: #222;
}

.title {
  font-size: 24rpx;
  color: #666;
  background-color: #f0f0f0;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
}

.specialty {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
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


