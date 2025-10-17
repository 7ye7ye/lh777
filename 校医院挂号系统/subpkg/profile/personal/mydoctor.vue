<template>
  <view class="page-bg">
    <view class="doctor-header">我的医生</view>
    
    <!-- 搜索栏 -->
    <view class="search-section">
      <input 
        class="search-input" 
        placeholder="搜索医生姓名或科室" 
        v-model="searchKeyword"
        @input="onSearch"
      />
    </view>

    <!-- 医生列表 -->
    <view class="doctor-list">
      <view
        class="doctor-item"
        v-for="item in filteredDoctors"
        :key="item.id"
        @click="goToDoctorDetail(item.id)"
      >
        <view class="avatar">👨‍⚕️</view>
        <view class="info">
          <view class="doctor-basic">
            <text class="name">{{ item.name }}</text>
            <text class="title">{{ item.title || '主治医师' }}</text>
          </view>
          <text class="dept">{{ item.dept }}</text>
          <text class="introduction" v-if="item.introduction">{{ item.introduction }}</text>
          <view class="tags">
            <text class="tag" v-for="tag in item.tags" :key="tag">{{ tag }}</text>
          </view>
        </view>
        <view class="action">
          <button class="contact-btn" @click.stop="contactDoctor(item.id)">联系</button>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="filteredDoctors.length === 0">
      <text class="empty-text">暂无医生信息</text>
    </view>

    <button class="main-btn" @click="getDoctorList">刷新医生列表</button>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { userApi } from '@/api/user'

const doctors = ref([
  { 
    id: 1, 
    name: '李医生', 
    dept: '内科', 
    avatar: '/static/avatar.png',
    title: '主任医师',
    introduction: '擅长心血管疾病的诊断和治疗',
    tags: ['心血管', '高血压', '糖尿病']
  },
  { 
    id: 2, 
    name: '王医生', 
    dept: '外科', 
    avatar: '/static/avatar.png',
    title: '副主任医师',
    introduction: '专业从事微创外科手术',
    tags: ['微创手术', '腹腔镜', '甲状腺']
  },
  { 
    id: 3, 
    name: '张医生', 
    dept: '儿科', 
    avatar: '/static/avatar.png',
    title: '主治医师',
    introduction: '儿童常见病多发病的诊治',
    tags: ['儿童保健', '呼吸道感染', '消化系统']
  }
])

const searchKeyword = ref('')

const filteredDoctors = computed(() => {
  if (!searchKeyword.value) {
    return doctors.value
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  return doctors.value.filter(doctor => 
    doctor.name.toLowerCase().includes(keyword) ||
    doctor.dept.toLowerCase().includes(keyword) ||
    doctor.introduction.toLowerCase().includes(keyword) ||
    doctor.tags.some(tag => tag.toLowerCase().includes(keyword))
  )
})

const getDoctorList = () => {
  userApi.getDoctorList().then(res => {
    uni.showToast({ title: '获取成功', icon: 'success' })
    // doctors.value = res.data
  }).catch(() => {
    uni.showToast({ title: '获取失败', icon: 'error' })
  })
}

const onSearch = () => {
  // 搜索逻辑已在computed中处理
}

// 跳转到医生详情页面
const goToDoctorDetail = (id) => {
  uni.navigateTo({
    url: `/pages/profile/doctor-detail?id=${id}`
  })
}

const contactDoctor = (id) => {
  uni.showModal({
    title: '联系医生',
    content: '确定要联系这位医生吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '联系功能开发中', icon: 'none' })
      }
    }
  })
}

onMounted(() => {
  getDoctorList()
})
</script>

<style scoped>
.page-bg { 
  min-height: 100vh; 
  background: #f8faff; 
}

.doctor-header { 
  font-size: 36rpx; 
  font-weight: bold; 
  padding: 32rpx; 
}

.search-section {
  padding: 0 24rpx 24rpx 24rpx;
}

.search-input {
  width: 100%;
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  border: 1px solid #e9ecef;
}

.doctor-list { 
  background: #fff; 
  margin: 0 24rpx 24rpx 24rpx; 
  border-radius: 16rpx; 
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.doctor-item { 
  display: flex; 
  align-items: flex-start; 
  margin-bottom: 24rpx; 
  padding: 20rpx;
  border-radius: 12rpx;
  background: #f8f9fa;
}

.doctor-item:last-child {
  margin-bottom: 0;
}

.avatar { 
  width: 80rpx; 
  height: 80rpx; 
  border-radius: 50%; 
  margin-right: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  background: #f0f0f0;
}

.info { 
  flex: 1;
}

.doctor-basic {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.name { 
  font-size: 30rpx; 
  font-weight: bold; 
  margin-right: 12rpx;
}

.title {
  font-size: 24rpx;
  color: #3a9cff;
  background: #e6f2ff;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.dept { 
  font-size: 26rpx; 
  color: #888; 
  margin-bottom: 8rpx;
  display: block;
}

.introduction {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 12rpx;
  display: block;
  line-height: 1.4;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.tag {
  font-size: 22rpx;
  color: #666;
  background: #f0f0f0;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.action {
  display: flex;
  align-items: center;
}

.contact-btn {
  background: #3a9cff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 12rpx 24rpx;
  font-size: 24rpx;
}

.empty-state {
  text-align: center;
  padding: 80rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.main-btn { 
  width: 100%; 
  margin: 32rpx 24rpx 0 24rpx; 
  background: #3a9cff; 
  color: #fff; 
  border: none;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 28rpx;
}
</style>