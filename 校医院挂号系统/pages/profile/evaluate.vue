<template>
  <view class="page-bg">
    <view class="evaluate-header">就诊评价</view>
    
    <!-- 就诊人信息 -->
    <view class="patient-info">
        <view class="avatar">👤</view>
      <view class="info">
        <text class="name">{{ currentPatient.name || '周诗晴' }}</text>
        <view class="tags">
          <text class="tag">电子就诊卡</text>
          <text class="tag">就诊码</text>
        </view>
        <text class="no">门诊号：{{ currentPatient.visitNo || 'M017087965' }}</text>
      </view>
      <button class="switch-btn" @click="showPatientList = true">切换就诊人</button>
    </view>

    <!-- 评价类型 -->
    <view class="evaluate-type">
      <view class="type-title">评价类型</view>
      <radio-group @change="onTypeChange">
        <label class="radio-label">
          <radio value="门诊" :checked="evaluateType === '门诊'" />
          <text>门诊评价</text>
        </label>
        <label class="radio-label">
          <radio value="住院" :checked="evaluateType === '住院'" />
          <text>住院评价</text>
        </label>
      </radio-group>
    </view>

    <!-- 评价内容 -->
    <view class="evaluate-content" v-if="showEvaluateForm">
      <view class="content-title">请对本次就诊进行评价</view>
      
      <!-- 医生评价 -->
      <view class="evaluate-section">
        <view class="section-title">医生服务</view>
        <view class="rating-item">
          <text class="rating-label">服务态度</text>
          <view class="rating-stars">
            <text 
              v-for="i in 5" 
              :key="i" 
              class="star" 
              :class="{ active: i <= doctorRating.attitude }"
              @click="setDoctorRating('attitude', i)"
            >★</text>
          </view>
        </view>
        <view class="rating-item">
          <text class="rating-label">专业水平</text>
          <view class="rating-stars">
            <text 
              v-for="i in 5" 
              :key="i" 
              class="star" 
              :class="{ active: i <= doctorRating.professional }"
              @click="setDoctorRating('professional', i)"
            >★</text>
          </view>
        </view>
        <view class="rating-item">
          <text class="rating-label">沟通能力</text>
          <view class="rating-stars">
            <text 
              v-for="i in 5" 
              :key="i" 
              class="star" 
              :class="{ active: i <= doctorRating.communication }"
              @click="setDoctorRating('communication', i)"
            >★</text>
          </view>
        </view>
      </view>

      <!-- 医院环境评价 -->
      <view class="evaluate-section">
        <view class="section-title">医院环境</view>
        <view class="rating-item">
          <text class="rating-label">环境卫生</text>
          <view class="rating-stars">
            <text 
              v-for="i in 5" 
              :key="i" 
              class="star" 
              :class="{ active: i <= hospitalRating.environment }"
              @click="setHospitalRating('environment', i)"
            >★</text>
          </view>
        </view>
        <view class="rating-item">
          <text class="rating-label">排队等候</text>
          <view class="rating-stars">
            <text 
              v-for="i in 5" 
              :key="i" 
              class="star" 
              :class="{ active: i <= hospitalRating.waiting }"
              @click="setHospitalRating('waiting', i)"
            >★</text>
          </view>
        </view>
      </view>

      <!-- 文字评价 -->
      <view class="evaluate-section">
        <view class="section-title">详细评价</view>
        <textarea 
          class="evaluate-textarea" 
          placeholder="请详细描述您的就诊体验和建议..."
          v-model="evaluateText"
          maxlength="500"
        />
        <text class="char-count">{{ evaluateText.length }}/500</text>
      </view>
    </view>

    <view class="tip">注：住院号是住院收据，右上角000开头的10位数字。</view>
    
    <view class="action-buttons">
      <button 
        class="main-btn" 
        :class="{ disabled: !canSubmit }"
        @click="submitEvaluate"
        :disabled="!canSubmit"
      >
        {{ showEvaluateForm ? '提交评价' : '去评价' }}
      </button>
    </view>

    <!-- 就诊人选择弹窗 -->
    <view class="patient-modal" v-if="showPatientList">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">选择就诊人</text>
          <text class="close-btn" @click="showPatientList = false">×</text>
        </view>
        <view class="patient-list">
          <view 
            class="patient-item" 
            v-for="patient in patients" 
            :key="patient.id"
            @click="selectPatient(patient)"
          >
            <text class="patient-name">{{ patient.name }}</text>
            <text class="patient-info">{{ patient.gender }} | {{ patient.age }}岁</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { userApi } from '@/api/user'

const evaluateType = ref('门诊')
const showEvaluateForm = ref(false)
const showPatientList = ref(false)
const evaluateText = ref('')

const currentPatient = ref({
  name: '周诗晴',
  visitNo: 'M017087965'
})

const patients = ref([
  { id: 1, name: '周诗晴', gender: '女', age: 28, visitNo: 'M017087965' },
  { id: 2, name: '张三', gender: '男', age: 35, visitNo: 'M017087966' }
])

const doctorRating = ref({
  attitude: 0,
  professional: 0,
  communication: 0
})

const hospitalRating = ref({
  environment: 0,
  waiting: 0
})

const canSubmit = computed(() => {
  return showEvaluateForm.value && 
         (doctorRating.value.attitude > 0 || 
          doctorRating.value.professional > 0 || 
          doctorRating.value.communication > 0 ||
          hospitalRating.value.environment > 0 ||
          hospitalRating.value.waiting > 0 ||
          evaluateText.value.trim().length > 0)
})

const onTypeChange = (e) => {
  evaluateType.value = e.detail.value
  showEvaluateForm.value = true
}

const setDoctorRating = (type, rating) => {
  doctorRating.value[type] = rating
}

const setHospitalRating = (type, rating) => {
  hospitalRating.value[type] = rating
}

const selectPatient = (patient) => {
  currentPatient.value = patient
  showPatientList.value = false
}

const submitEvaluate = () => {
  if (!showEvaluateForm.value) {
    showEvaluateForm.value = true
    return
  }

  if (!canSubmit.value) {
    uni.showToast({ title: '请至少完成一项评价', icon: 'error' })
    return
  }

  const evaluateData = {
    type: evaluateType.value,
    patientId: currentPatient.value.id,
    doctorRating: doctorRating.value,
    hospitalRating: hospitalRating.value,
    text: evaluateText.value
  }

  userApi.submitEvaluate(evaluateData).then(() => {
    uni.showToast({ title: '评价提交成功', icon: 'success' })
    // 重置表单
    resetForm()
  }).catch(() => {
    uni.showToast({ title: '提交失败，请重试', icon: 'error' })
  })
}

const resetForm = () => {
  showEvaluateForm.value = false
  evaluateText.value = ''
  doctorRating.value = { attitude: 0, professional: 0, communication: 0 }
  hospitalRating.value = { environment: 0, waiting: 0 }
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.page-bg { 
  min-height: 100vh; 
  background: #f8faff; 
  padding-bottom: 120rpx; 
}

.evaluate-header { 
  font-size: 36rpx; 
  font-weight: bold; 
  padding: 32rpx; 
}

.patient-info { 
  display: flex; 
  align-items: center; 
  background: #fff; 
  margin: 24rpx; 
  border-radius: 16rpx; 
  padding: 24rpx; 
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
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

.name { 
  font-size: 30rpx; 
  font-weight: bold; 
  margin-bottom: 8rpx;
}

.tags { 
  margin: 8rpx 0; 
}

.tag { 
  background: #e6f2ff; 
  color: #3a9cff; 
  font-size: 22rpx; 
  border-radius: 8rpx; 
  padding: 4rpx 16rpx; 
  margin-right: 8rpx; 
}

.no { 
  font-size: 26rpx; 
  color: #888; 
}

.switch-btn { 
  font-size: 26rpx; 
  color: #3a9cff; 
  background: none; 
  border: none; 
}

.evaluate-type { 
  background: #fff; 
  margin: 24rpx; 
  border-radius: 16rpx; 
  padding: 32rpx; 
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.type-title { 
  font-size: 28rpx; 
  font-weight: bold; 
  margin-bottom: 16rpx; 
}

.radio-label { 
  display: flex; 
  align-items: center; 
  font-size: 28rpx; 
  margin-bottom: 16rpx; 
}

.evaluate-content {
  background: #fff;
  margin: 24rpx;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.content-title {
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 24rpx;
  color: #333;
}

.evaluate-section {
  margin-bottom: 32rpx;
}

.section-title {
  font-size: 26rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
  color: #666;
}

.rating-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.rating-label {
  font-size: 26rpx;
  color: #333;
}

.rating-stars {
  display: flex;
}

.star {
  font-size: 32rpx;
  color: #ddd;
  margin-right: 8rpx;
  cursor: pointer;
}

.star.active {
  color: #ffa500;
}

.evaluate-textarea {
  width: 100%;
  min-height: 120rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 16rpx;
  font-size: 26rpx;
  border: 1px solid #e9ecef;
  margin-bottom: 8rpx;
}

.char-count {
  font-size: 22rpx;
  color: #999;
  text-align: right;
}

.tip { 
  color: #e69a2a; 
  font-size: 26rpx; 
  margin: 16rpx 32rpx; 
}

.action-buttons {
  padding: 0 24rpx;
}

.main-btn { 
  width: 100%; 
  background: #3a9cff; 
  color: #fff; 
  border: none;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 28rpx;
  margin-top: 32rpx;
}

.main-btn.disabled {
  background: #a4caff;
}

.patient-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 16rpx;
  margin: 32rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
}

.close-btn {
  font-size: 40rpx;
  color: #999;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.patient-list {
  padding: 16rpx 0;
}

.patient-item {
  padding: 16rpx 32rpx;
  border-bottom: 1px solid #f0f0f0;
}

.patient-item:last-child {
  border-bottom: none;
}

.patient-name {
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 4rpx;
}

.patient-info {
  font-size: 24rpx;
  color: #999;
}
</style>