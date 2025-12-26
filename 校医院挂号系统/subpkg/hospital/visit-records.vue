<template>
  <view class="visit-records-bg">
    <view class="page-header">
      <view class="back-btn" @click="goBack">←</view>
      <text class="page-title">选择就诊记录</text>
      <view class="header-right"></view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view 
        v-for="(item, index) in filterOptions" 
        :key="index"
        class="filter-item"
        :class="{ active: currentFilter === index }"
        @click="changeFilter(index)"
      >
        {{ item }}
      </view>
    </view>

    <!-- 记录列表 -->
    <scroll-view 
      scroll-y 
      class="records-list"
      @refresh="onRefresh"
      @scrolltolower="onLoadMore"
      refresher-enabled
      :refresher-triggered="loading"
    >
      <view 
        v-for="(record, index) in filteredRecords" 
        :key="index"
        class="record-item"
        @click="selectRecord(record)"
      >
        <view class="record-header">
          <view class="record-title">
            <text class="department-name">{{ record.departmentName }}</text>
            <text class="doctor-name">{{ record.doctorName }}</text>
          </view>
          <text class="visit-time">就诊时间：{{ record.registrationTime }}</text>
        </view>
        
        <view class="record-content">
          <view class="info-item">
            <span class="info-label">就诊编号：</span>
            <span class="info-value">{{ record.registrationNo }}</span>
          </view>
          <view class="info-item">
            <span class="info-label">就诊状态：</span>
            <span class="info-value status-done">已就诊</span>
          </view>
          <view class="info-item" v-if="record.diagnosis">
            <span class="info-label">诊断：</span>
            <span class="info-value diagnosis">{{ truncateText(record.diagnosis, 30) }}</span>
          </view>
        </view>
        
        <view class="record-footer">
          <text class="select-tips">点击选择此就诊记录进行转诊</text>
          <view class="arrow"></view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="filteredRecords.length === 0 && !loading" class="empty-state">
        <image :src="getStaticImage('/static/empty_message.png')" mode="widthFix" class="empty-img"></image>
        <text class="empty-text">暂无已就诊的记录</text>
      </view>
      
      <!-- 加载中 -->
      <view v-if="loading" class="loading-state">
        <text class="loading-text">加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPatientVisitRecords } from '../../api/referral'
import { useUserStore } from '../../store/user'
import { getStaticImage } from '@/utils/imageHelper'

// 筛选选项
const filterOptions = ['全部', '近一个月', '近三个月', '近半年']
const currentFilter = ref(0)

// 就诊记录数据
const visitRecords = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

// 根据筛选条件过滤记录
const filteredRecords = computed(() => {
  if (currentFilter.value === 0) {
    return visitRecords.value
  }
  
  // 这里可以根据选择的时间范围过滤记录
  // 为简化，暂时返回所有记录
  return visitRecords.value
})

// 切换筛选
const changeFilter = (index) => {
  currentFilter.value = index
  // 重新加载数据
  currentPage.value = 1
  hasMore.value = true
  visitRecords.value = []
  loadVisitRecords()
}

// 截断文本
const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const userStore = useUserStore()
userStore.initFromStorage?.()

const resolveGender = (value) => {
  if (value === null || value === undefined) return ''
  const str = String(value).trim().toLowerCase()
  if (!str) return ''
  if (str === '男' || str === 'male' || str === '1') return '男'
  if (str === '女' || str === 'female' || str === '2') return '女'
  return ''
}

const calculateAgeFromBirth = (birthDate) => {
  if (!birthDate) return ''
  const date = new Date(birthDate)
  if (Number.isNaN(date.getTime())) return ''
  const now = new Date()
  let age = now.getFullYear() - date.getFullYear()
  const monthDiff = now.getMonth() - date.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < date.getDate())) {
    age -= 1
  }
  return age >= 0 ? String(age) : ''
}

const buildPatientSnapshot = (record) => {
  const storedUser = userStore?.userInfo || uni.getStorageSync('userInfo') || {}
  const patientName = record.patientName || storedUser.name || storedUser.realname || storedUser.nickName || ''
  const patientPhone = record.patientPhone || storedUser.phone || storedUser.mobile || storedUser.tel || ''
  const patientGender = resolveGender(record.patientGender || record.gender || storedUser.gender)
  const patientAge =
    record.patientAge ||
    record.age ||
    storedUser.age ||
    calculateAgeFromBirth(storedUser.birthDate)

  return {
    name: patientName,
    phone: patientPhone,
    gender: patientGender,
    age: patientAge ? String(patientAge) : '',
  }
}

// 选择记录
const selectRecord = (record) => {
  console.log('选择的就诊记录:', record)
  const patientSnapshot = buildPatientSnapshot(record)
  const recordWithPatient = {
    ...record,
    patientName: patientSnapshot.name,
    patientPhone: patientSnapshot.phone,
    patientGender: patientSnapshot.gender,
    patientAge: patientSnapshot.age,
  }
  uni.setStorageSync('selectedVisitRecord', recordWithPatient)
  uni.setStorageSync('referralPatientSnapshot', patientSnapshot)
  uni.navigateTo({
    url: '/subpkg/hospital/referral-type-selection'
  })
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 刷新数据
const onRefresh = () => {
  currentPage.value = 1
  hasMore.value = true
  visitRecords.value = []
  loadVisitRecords()
}

// 加载更多
const onLoadMore = () => {
  if (hasMore.value && !loading.value) {
    currentPage.value++
    loadVisitRecords()
  }
}

// 加载就诊记录
const loadVisitRecords = async () => {
  if (loading.value || (!hasMore.value && currentPage.value > 1)) {
    return
  }
  
  try {
    loading.value = true
    // 构建API请求参数
    const timeRangeMap = ['all', 'recent', 'month3', 'month6']
    const params = {
      status: '已就诊',
      page: currentPage.value,
      pageSize: pageSize.value,
      timeRange: timeRangeMap[currentFilter.value]
    }
    
    const res = await getPatientVisitRecords(params)
    
    if (res.code === 200 && res.data) {
      // 格式化记录，确保数据结构一致
      let records = res.data.records || res.data.list || []
      records = records.map(record => ({
        id: record.id || '',
        departmentName: record.departmentName || record.department || '',
        doctorName: record.doctorName || record.doctor || '',
        registrationTime: record.registrationTime || record.visitDate || '',
        registrationNo: record.registrationNo || record.visitNo || '',
        diagnosis: record.diagnosis || record.symptoms || '',
        status: record.status || '已就诊',
        patientName: record.patientName || record.realname || record.name || record.patient?.name || '',
        patientPhone: record.patientPhone || record.phone || record.mobile || record.patient?.phone || '',
        patientGender: record.patientGender || record.gender || record.patient?.gender || '',
        patientAge: record.patientAge || record.age || record.patient?.age || '',
      }))
      
      if (currentPage.value === 1) {
        visitRecords.value = records
      } else {
        visitRecords.value = [...visitRecords.value, ...records]
      }
      
      // 判断是否还有更多数据
      hasMore.value = res.data.hasMore || visitRecords.value.length < (res.data.total || res.data.totalCount || 0)
    } else {
      uni.showToast({
        title: res.message || '加载失败，请重试',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('加载就诊记录失败:', error)
    uni.showToast({
      title: '网络错误，请稍后重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 页面加载时初始化数据
onMounted(() => {
  loadVisitRecords()
})
</script>

<style scoped>
.visit-records-bg {
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  background-color: #1989fa;
  color: #fff;
  padding: 16px;
  display: flex;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-btn {
  font-size: 20px;
  margin-right: 20px;
}

.page-title {
  flex: 1;
  font-size: 18px;
  font-weight: bold;
  text-align: center;
}

.header-right {
  width: 20px;
}

.filter-bar {
  display: flex;
  background-color: #fff;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 52px;
  z-index: 5;
}

.filter-item {
  flex: 1;
  padding: 14px 0;
  text-align: center;
  font-size: 14px;
  color: #666;
  position: relative;
}

.filter-item.active {
  color: #1989fa;
}

.filter-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background-color: #1989fa;
  border-radius: 1.5px;
}

.records-list {
  padding: 12px;
  height: calc(100vh - 110px);
}

.record-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.record-header {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.record-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.department-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.doctor-name {
  font-size: 14px;
  color: #666;
}

.visit-time {
  font-size: 12px;
  color: #999;
}

.record-content {
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-label {
  color: #666;
  width: 70px;
}

.info-value {
  color: #333;
  flex: 1;
}

.status-done {
  color: #4caf50;
}

.diagnosis {
  word-break: break-all;
}

.record-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.select-tips {
  font-size: 12px;
  color: #1989fa;
}

.arrow {
  color: #ccc;
  font-size: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-img {
  width: 120px;
  height: 120px;
  margin-bottom: 16px;
}

.empty-text {
  color: #999;
  font-size: 14px;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.loading-text {
  color: #999;
  font-size: 14px;
}
</style>
