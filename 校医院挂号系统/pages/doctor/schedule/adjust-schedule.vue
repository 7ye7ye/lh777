<template>
  <view class="adjust-page">
    <view class="page-header">
      <view class="header-back" @click="goBackToSchedule">
        <text class="back-icon">‹</text>
        <text class="back-text">返回</text>
      </view>
      <view class="header-content">
        <text class="header-title">申请调班</text>
        <text class="header-subtitle">请填写调班申请信息</text>
      </view>
    </view>

    <view class="form-container">
      <!-- 原排班选择 -->
      <view class="form-item">
        <view class="form-label">
          <text class="label-text">原排班</text>
          <text class="required">*</text>
        </view>
        <picker
          mode="multiSelector"
          :range="originalScheduleRange"
          :value="originalScheduleValue"
          @change="onOriginalScheduleChange"
        >
          <view class="picker-content">
            <text v-if="!form.originalSchedule" class="placeholder">请选择原排班</text>
            <text v-else class="value">{{ form.originalSchedule }}</text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 新日期选择 -->
      <view class="form-item">
        <view class="form-label">
          <text class="label-text">新日期</text>
          <text class="required">*</text>
        </view>
        <picker
          mode="date"
          :value="form.newDate"
          :start="minDate"
          :end="maxDate"
          @change="onNewDateChange"
        >
          <view class="picker-content">
            <text v-if="!form.newDate" class="placeholder">请选择新日期</text>
            <text v-else class="value">{{ form.newDate }}</text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 新时间段选择 -->
      <view class="form-item">
        <view class="form-label">
          <text class="label-text">新时间段</text>
          <text class="required">*</text>
        </view>
        <picker
          mode="selector"
          :range="timePeriods"
          :value="timePeriodIndex"
          @change="onTimePeriodChange"
        >
          <view class="picker-content">
            <text v-if="timePeriodIndex === -1" class="placeholder">请选择新时间段</text>
            <text v-else class="value">{{ timePeriods[timePeriodIndex] }}</text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 调班原因 -->
      <view class="form-item">
        <view class="form-label">
          <text class="label-text">调班原因</text>
          <text class="required">*</text>
        </view>
        <textarea
          class="textarea-input"
          v-model="form.reason"
          placeholder="请输入调班原因（至少10个字）"
          maxlength="200"
          :show-confirm-bar="false"
        />
        <view class="char-count">{{ form.reason.length }}/200</view>
      </view>

      <!-- 联系方式 -->
      <view class="form-item">
        <view class="form-label">
          <text class="label-text">联系方式</text>
          <text class="required">*</text>
        </view>
        <input
          class="text-input"
          v-model="form.contact"
          placeholder="请输入联系电话"
          type="number"
          maxlength="11"
        />
      </view>
    </view>

    <!-- 提示信息 -->
    <view class="tips-box">
      <view class="tips-title">📋 温馨提示：</view>
      <view class="tips-item">1. 调班申请需要提前3天提交</view>
      <view class="tips-item">2. 调班申请提交后需等待审批，请及时关注审批结果</view>
      <view class="tips-item">3. 如有紧急情况，请联系科室主任</view>
    </view>

    <!-- 提交按钮 -->
    <view class="button-group">
      <button class="submit-btn" @click="submitAdjustment">提交申请</button>
      <button class="cancel-btn" @click="goBack">取消</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { uniNavigateTo, uniShowToast, uniSwitchTab } from '@/utils/uniHelper'

// 表单数据
const form = ref({
  originalSchedule: '',
  newDate: '',
  newTimePeriod: '',
  reason: '',
  contact: ''
})

// 原排班选择器范围
const originalScheduleRange = ref([
  ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  ['上午', '下午', '晚上']
])

const originalScheduleValue = ref([0, 0])

// 时间段选项
const timePeriods = ref([
  '上午 08:00-12:00',
  '下午 14:00-17:00',
  '晚上 18:00-20:00'
])

const timePeriodIndex = ref(-1)

// 日期范围
const minDate = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() + 3) // 至少提前3天
  return formatDate(date)
})

const maxDate = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() + 30) // 最多30天内
  return formatDate(date)
})

// 格式化日期
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 原排班选择变化
const onOriginalScheduleChange = (e) => {
  const val = e.detail.value
  originalScheduleValue.value = val
  const weekday = originalScheduleRange.value[0][val[0]]
  const period = originalScheduleRange.value[1][val[1]]
  form.value.originalSchedule = `${weekday} ${period}`
}

// 新日期选择变化
const onNewDateChange = (e) => {
  form.value.newDate = e.detail.value
}

// 新时间段选择变化
const onTimePeriodChange = (e) => {
  timePeriodIndex.value = e.detail.value
  form.value.newTimePeriod = timePeriods.value[e.detail.value]
}

// 表单验证
const validateForm = () => {
  if (!form.value.originalSchedule) {
    uniShowToast({ title: '请选择原排班', icon: 'none' })
    return false
  }
  if (!form.value.newDate) {
    uniShowToast({ title: '请选择新日期', icon: 'none' })
    return false
  }
  if (!form.value.newTimePeriod) {
    uniShowToast({ title: '请选择新时间段', icon: 'none' })
    return false
  }
  if (form.value.reason.length < 10) {
    uniShowToast({ title: '调班原因至少需要10个字', icon: 'none' })
    return false
  }
  if (!form.value.contact) {
    uniShowToast({ title: '请输入联系方式', icon: 'none' })
    return false
  }
  if (!/^1[3-9]\d{9}$/.test(form.value.contact)) {
    uniShowToast({ title: '请输入正确的手机号码', icon: 'none' })
    return false
  }
  return true
}

// 提交调班申请
const submitAdjustment = async () => {
  if (!validateForm()) return
  
  try {
    const res = await uniShowModal({
      title: '确认提交',
      content: '确定要提交调班申请吗？',
      confirmText: '确定',
      cancelText: '取消'
    })
    
    if (res.confirm) {
      // TODO: 调用后端API提交数据
      // await adjustScheduleApi.submit(form.value)
      
      await uniShowToast({ title: '申请提交成功', icon: 'success' })
      setTimeout(() => {
        uniNavigateBack()
      }, 1500)
    }
  } catch (error) {
    uniShowToast({ title: '提交失败，请重试', icon: 'none' })
  }
}

// 返回医生主界面
const goBackToSchedule = () => {
  uniNavigateTo({ url: '/pages/doctor/schedule/schedule' })
}

// 返回
const goBack = () => {
  goBackToSchedule()
}
</script>

<style scoped>
.adjust-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40rpx;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #1677ff 0%, #4da3ff 100%);
  padding: 48rpx 32rpx;
  color: #fff;
  position: relative;
}

.header-back {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  cursor: pointer;
}

.back-icon {
  font-size: 48rpx;
  font-weight: 300;
  margin-right: 8rpx;
  line-height: 1;
}

.back-text {
  font-size: 28rpx;
  opacity: 0.95;
}

.header-content {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: 40rpx;
  font-weight: 600;
  display: block;
  margin-bottom: 12rpx;
}

.header-subtitle {
  font-size: 26rpx;
  opacity: 0.9;
}

/* 表单容器 */
.form-container {
  margin: 24rpx 32rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.label-text {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.required {
  color: #ff4d4f;
  margin-left: 4rpx;
  font-size: 28rpx;
}

/* 选择器样式 */
.picker-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
}

.placeholder {
  font-size: 28rpx;
  color: #999;
}

.value {
  font-size: 28rpx;
  color: #333;
}

.arrow {
  font-size: 36rpx;
  color: #999;
  font-weight: 300;
}

/* 文本输入框 */
.text-input {
  width: 100%;
  padding: 24rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
  font-size: 28rpx;
}

/* 文本域 */
.textarea-input {
  width: 100%;
  min-height: 200rpx;
  padding: 24rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
  font-size: 28rpx;
  line-height: 1.6;
}

.char-count {
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

/* 提示框 */
.tips-box {
  margin: 24rpx 32rpx;
  background: #e6f7ff;
  border-radius: 12rpx;
  padding: 24rpx;
  border-left: 6rpx solid #1677ff;
}

.tips-title {
  font-size: 28rpx;
  color: #1677ff;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.tips-item {
  font-size: 24rpx;
  color: #666;
  line-height: 1.8;
  margin-bottom: 8rpx;
}

.tips-item:last-child {
  margin-bottom: 0;
}

/* 按钮组 */
.button-group {
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.submit-btn,
.cancel-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
  border: none;
}

.submit-btn {
  background: linear-gradient(135deg, #1677ff 0%, #4da3ff 100%);
  color: #fff;
}

.cancel-btn {
  background: #fff;
  color: #666;
  border: 2rpx solid #e8e8e8;
}
</style>

