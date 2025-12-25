<template>
  <view class="selection-bg">
    <view class="page-header">
      <view class="back-btn" @click="goBack">←</view>
      <text class="page-title">选择转诊类型</text>
      <view class="header-right"></view>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 已选择的就诊记录信息 -->
      <view class="selected-record-card">
        <view class="section-title">已选择的就诊记录</view>
        <view class="record-info">
          <view class="info-item">
            <span class="info-label">就诊科室：</span>
            <span class="info-value">{{ selectedRecord?.departmentName || '未知' }}</span>
          </view>
          <view class="info-item">
            <span class="info-label">就诊医生：</span>
            <span class="info-value">{{ selectedRecord?.doctorName || '未知' }}</span>
          </view>
          <view class="info-item">
            <span class="info-label">就诊时间：</span>
            <span class="info-value">{{ selectedRecord?.registrationTime || '未知' }}</span>
          </view>
          <view class="info-item" v-if="selectedRecord?.diagnosis">
            <span class="info-label">诊断结果：</span>
            <span class="info-value diagnosis">{{ selectedRecord.diagnosis }}</span>
          </view>
        </view>
      </view>

      <!-- 转诊类型选择 -->
      <view class="selection-section">
        <view class="section-title">请选择转诊类型</view>
        <view class="options-grid">
          <view class="option-item internal"
                @click="selectReferralType('internal')"
                :class="{ selected: selectedType === 'internal' }">
            <view class="option-icon">
              <image :src="getStaticImage('/static/doctor.svg')" mode="aspectFit" class="icon-img"></image>
            </view>
            <view class="option-content">
              <text class="option-title">院内科室转诊</text>
              <text class="option-desc">转至本院其他科室就诊，可自动挂号</text>
            </view>
            <view class="option-select"
                    :class="{ active: selectedType === 'internal' }">
              <text v-if="selectedType === 'internal'" class="check-icon">✓</text>
            </view>
          </view>

          <view class="option-item external"
                @click="selectReferralType('external')"
                :class="{ selected: selectedType === 'external' }">
            <view class="option-icon">
              <image :src="getStaticImage('/static/hospital.svg')" mode="aspectFit" class="icon-img"></image>
            </view>
            <view class="option-content">
              <text class="option-title">院外转诊</text>
              <text class="option-desc">转至其他医院就诊，需填写详细信息</text>
            </view>
            <view class="option-select"
                    :class="{ active: selectedType === 'external' }">
              <text v-if="selectedType === 'external'" class="check-icon">✓</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 院内科室选择（仅院内转诊时显示） --</
      <view v-if="selectedType === 'internal'" class="department-section">
        <view class="section-title">选择目标科室</view>
        <picker @change="onDepartmentChange" :value="departmentIndex" :range="departments" range-key="name" class="picker">
          <view class="picker-text">{{ departments[departmentIndex]?.name || '请选择目标科室' }}</view>
        </picker>
      </view>

      <!-- 下一步按钮 -->
      <view class="action-section">
        <button class="next-btn" 
                @click="goToNextStep"
                :disabled="!selectedType || (selectedType === 'internal' && !selectedDepartment)">
          下一步
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getReferralOptions } from '../../api/referral'
import { getStaticImage } from '@/utils/imageHelper'

// 已选择的就诊记录
const selectedRecord = ref({})

// 转诊类型选择
const selectedType = ref('')

// 科室列表
const departments = ref([])
const departmentIndex = ref(0)
const selectedDepartment = computed(() => departments.value[departmentIndex.value])

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 选择转诊类型
const selectReferralType = (type) => {
  selectedType.value = type
}

// 处理科室选择
const onDepartmentChange = (e) => {
  departmentIndex.value = e.detail.value
}

const resolveGender = (value) => {
  if (value === null || value === undefined) return ''
  const str = String(value).trim().toLowerCase()
  if (!str) return ''
  if (str === '男' || str === 'male' || str === '1') return '男'
  if (str === '女' || str === 'female' || str === '2') return '女'
  return ''
}

const buildPatientSnapshot = () => {
  const storedSnapshot = uni.getStorageSync('referralPatientSnapshot')
  if (storedSnapshot) return storedSnapshot
  const record = selectedRecord.value || uni.getStorageSync('selectedVisitRecord') || {}
  const userInfo = uni.getStorageSync('userInfo') || {}
  const snapshot = {
    name: record.patientName || userInfo.name || userInfo.realname || '',
    phone: record.patientPhone || userInfo.phone || userInfo.mobile || '',
    gender: resolveGender(record.patientGender || record.gender || userInfo.gender),
    age: String(record.patientAge || record.age || userInfo.age || '') || '',
  }
  return snapshot
}

// 进入下一步
const goToNextStep = () => {
  if (!selectedType.value) {
    uni.showToast({
      title: '请选择转诊类型',
      icon: 'none'
    })
    return
  }

  // 保存选择的转诊信息
  const patientSnapshot = buildPatientSnapshot()
  const referralInfo = {
    visitRecord: selectedRecord.value,
    patient: patientSnapshot,
    type: selectedType.value,
    targetDepartment: selectedDepartment.value?.name || ''
  }
  
  uni.setStorageSync('referralInfo', referralInfo)
  uni.setStorageSync('referralPatientSnapshot', patientSnapshot)

  // 跳转到转诊申请页面
  uni.navigateTo({
    url: '/subpkg/hospital/referral-application'
  })
}

// 加载科室列表
const loadDepartments = async () => {
  try {
    const res = await getReferralOptions()
    if (res.code === 200 && res.data && res.data.departments) {
      departments.value = res.data.departments
    }
  } catch (error) {
    console.error('加载科室列表失败:', error)
  }
}

// 页面加载时初始化数据
onMounted(() => {
  // 获取已选择的就诊记录
  const record = uni.getStorageSync('selectedVisitRecord')
  if (record) {
    selectedRecord.value = record
  } else {
    // 如果没有选择记录，返回上一页
    uni.showToast({
      title: '请先选择就诊记录',
      icon: 'none',
      success: () => {
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    })
  }
  
  // 加载科室列表
  loadDepartments()
})
</script>

<style scoped>
.selection-bg {
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

.content {
  padding: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
}

/* 已选择的就诊记录卡片 */
.selected-record-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.record-info .info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.record-info .info-label {
  color: #666;
  width: 80px;
}

.record-info .info-value {
  color: #333;
  flex: 1;
}

.diagnosis {
  word-break: break-all;
}

/* 转诊类型选择 */
.selection-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.options-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.option-item.selected {
  border-color: #1989fa;
  background-color: #e8f4fd;
}

.option-icon {
  width: 48px;
  height: 48px;
  margin-right: 16px;
}

.icon-img {
  width: 100%;
  height: 100%;
}

.option-content {
  flex: 1;
}

.option-title {
  display: block;
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.option-desc {
  display: block;
  font-size: 14px;
  color: #666;
}

.option-select {
  width: 24px;
  height: 24px;
  border: 1px solid #e0e0e0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.option-select.active {
  background-color: #1989fa;
  border-color: #1989fa;
}

.check-icon {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
}

/* 科室选择 */
.department-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.picker {
  width: 100%;
  padding: 12px;
  background-color: #f5f5f5;
  border-radius: 6px;
  font-size: 14px;
}

.picker-text {
  color: #333;
}

/* 操作按钮 */
.action-section {
  padding: 16px;
  margin-top: 20px;
}

.next-btn {
  width: 100%;
  padding: 14px;
  background-color: #1989fa;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
}

.next-btn:disabled {
  background-color: #ccc;
}
</style>
