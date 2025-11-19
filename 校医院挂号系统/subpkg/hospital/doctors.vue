<template>
  <view class="doctors-bg">
    <!-- 搜索框 -->
    <view class="search-bar">
      <input 
        type="text" 
        placeholder="搜索医生姓名或专长..." 
        v-model="keyword"
        @input="handleSearch"
        class="search-input"
      />
    </view>

    <!-- 医生列表 -->
    <view class="doctors-list">
      <view 
        v-for="(doctor, index) in doctorList" 
        :key="index" 
        class="doctor-item"
        @click="navigateToDetail(doctor)"
      >
        <image 
          :src="doctor.avatar || '/static/doctor.svg'" 
          mode="aspectFill" 
          class="avatar"
        ></image>
        <view class="doctor-info">
          <view class="name-title">
            <text class="name">{{ doctor.doctorName }}</text>
            <text class="title">{{ doctor.title }}</text>
          </view>
          <view class="specialty">擅长：{{ doctor.specialty }}</view>
          <view class="desc" v-if="doctor.doctorDesc">{{ doctor.doctorDesc }}</view>
        </view>
        <view class="arrow">
          <text>></text>
        </view>
      </view>
      
      <view class="empty" v-if="doctorList.length === 0">
        <image src="/static/empty_message.png" mode="widthFix" class="empty-img"></image>
        <text>暂无医生信息</text>
      </view>
    </view>
    
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllDoctors, searchDoctors } from '../../api/doctor_massage'

const keyword = ref('')
const doctorList = ref([])
const originalList = ref([]) // 用于搜索备份

// 加载医生列表
const loadDoctors = async () => {
  try {
    const res = await getAllDoctors()
    console.log('医生列表数据:', res)
    
    // 处理不同的响应格式
    let data = res
    if (res && res.data) {
      data = res.data
    } else if (res && res.result) {
      data = res.result
    }
    
    if (data && Array.isArray(data)) {
      doctorList.value = data
      originalList.value = [...data]
    } else if (Array.isArray(res)) {
      doctorList.value = res
      originalList.value = [...res]
    } else {
      console.warn('医生列表数据格式异常:', res)
      uni.showToast({
        title: '数据格式异常',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('加载医生列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

// 搜索医生
const handleSearch = async () => {
  if (!keyword.value.trim()) {
    // 清空搜索，恢复原始列表
    doctorList.value = [...originalList.value]
    return
  }
  
  try {
    const res = await searchDoctors(keyword.value)
    console.log('搜索结果:', res)
    
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
      console.warn('搜索结果格式异常:', res)
      uni.showToast({
        title: '搜索数据格式异常',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({
      title: '搜索失败',
      icon: 'none'
    })
  }
}

// 跳转到医生详情
const navigateToDetail = (doctor) => {
  uni.navigateTo({
    url: `/subpkg/hospital/doctor-detail?doctorId=${doctor.doctorId}`
  })
}

onMounted(() => {
  loadDoctors()
})
</script>

<style scoped>
.doctors-bg {
  background: #f8faff;
  min-height: 100vh;
  padding: 16rpx 0 120rpx 0;
}

.search-bar {
  background: #fff;
  padding: 16rpx;
  border-radius: 8rpx;
  margin: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.search-input {
  height: 60rpx;
  background-color: #f0f0f0;
  padding: 0 20rpx;
  border-radius: 30rpx;
  font-size: 28rpx;
  width: 100%;
  box-sizing: border-box;
}

.doctors-list {
  padding: 0 16rpx;
}

.doctor-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.doctor-item:active {
  background-color: #f5f5f5;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  margin-right: 20rpx;
  background: #e0e0e0;
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
  font-size: 32rpx;
  font-weight: bold;
  margin-right: 16rpx;
  color: #222;
}

.title {
  font-size: 24rpx;
  color: #666;
  background-color: #e6f2ff;
  color: #3a9cff;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
}

.specialty {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
  line-height: 1.4;
}

.desc {
  font-size: 24rpx;
  color: #999;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.arrow {
  font-size: 32rpx;
  color: #ccc;
  margin-left: 16rpx;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}

.empty-img {
  width: 200rpx;
  margin-bottom: 24rpx;
  opacity: 0.6;
}

.tabbar-placeholder { 
  height: 120rpx; 
}
</style>






