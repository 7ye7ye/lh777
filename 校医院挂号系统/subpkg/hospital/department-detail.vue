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

// 加载科室医生（这里需要创建医生API，暂时用模拟数据）
const loadDoctorsByDeptId = async () => {
  // 模拟医生数据，实际项目中需要调用医生API
  doctorList.value = [
    {
      doctorId: 1,
      doctorName: '张医生',
      title: '主任医师',
      specialty: '心血管疾病诊疗',
      avatar: '/static/doctor.png'
    },
    {
      doctorId: 2,
      doctorName: '李医生',
      title: '副主任医师',
      specialty: '呼吸系统疾病',
      avatar: '/static/doctor.png'
    }
  ]
}

// 跳转到医生详情
const navigateToDoctorDetail = (doctor) => {
  uni.navigateTo({
    url: `/subpkg/doctor/detail?doctorId=${doctor.doctorId}`
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


