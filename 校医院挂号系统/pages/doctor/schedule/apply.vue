<template>
  <view class="apply-page">
    <!-- 顶部栏 -->
    <view class="header">
      <text class="title">申请调班</text>
      <button class="back" @click="goBack">返回</button>
    </view>

    <!-- 表单区 -->
    <view class="form">
      <view class="section-title"><text class="title-text">调班信息</text></view>

      <view class="form-card">
        <view class="form-item">
          <text class="label">原排班</text>
          <picker mode="selector" :range="scheduleOptions" range-key="label" @change="onScheduleChange">
            <view class="picker">{{ selectedScheduleLabel || '请选择原排班' }}</view>
          </picker>
        </view>

        <view class="form-item">
          <text class="label">新日期</text>
          <picker mode="date" @change="onDateChange">
            <view class="picker">{{ newDate || '请选择新日期' }}</view>
          </picker>
        </view>

        <view class="form-item">
          <text class="label">新时间段</text>
          <picker mode="selector" :range="timeRanges" @change="onTimeChange">
            <view class="picker">{{ newTimeRange || '请选择新时间段' }}</view>
          </picker>
        </view>

        <view class="form-item">
          <text class="label">申请原因</text>
          <textarea class="textarea" v-model="reason" placeholder="请输入申请原因（最多200字）" maxlength="200" />
        </view>
      </view>

      <button class="submit" @click="submit">提交申请</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { uniNavigateTo, uniShowToast } from '../../../utils/uniHelper'
import { useUserStore } from '../../../store/user.js'
import { doctorApi } from '../../../api/doctor_massage'

const userStore = useUserStore()
const doctorId = computed(() => userStore.userInfo?.id || 1)

const scheduleOptions = ref([])
const selectedScheduleId = ref(null)
const selectedScheduleLabel = ref('')

const newDate = ref('')
const timeRanges = ref(['08:00-12:00', '14:00-17:00', '18:00-20:00'])
const newTimeRange = ref('')
const reason = ref('')

const goBack = () => uni.navigateBack()

const onScheduleChange = (e) => {
  const idx = e.detail.value
  const opt = scheduleOptions.value[idx]
  selectedScheduleId.value = opt.id
  selectedScheduleLabel.value = opt.label
}

const onDateChange = (e) => { newDate.value = e.detail.value }
const onTimeChange = (e) => { newTimeRange.value = timeRanges.value[e.detail.value] }

const submit = async () => {
  if (!selectedScheduleId.value || !newDate.value || !newTimeRange.value || !reason.value) {
    return uniShowToast({ title: '请完整填写申请信息' })
  }
  try {
    await doctorApi.applyShiftChange({
      scheduleId: selectedScheduleId.value,
      newDate: newDate.value,
      newTimeRange: newTimeRange.value,
      reason: reason.value,
    })
    uniShowToast({ title: '提交成功' })
    setTimeout(() => goBack(), 500)
  } catch (e) {
    uniShowToast({ title: '提交失败，请稍后重试' })
  }
}

const loadSchedules = async () => {
  try {
    const d = await doctorApi.getTodaySchedule(doctorId.value)
    scheduleOptions.value = d.map(s => ({
      id: s.id,
      label: `${s.date} ${s.timeRange} | 诊室 ${s.roomNo}`,
    }))
  } catch (e) {
    const today = new Date()
    const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    scheduleOptions.value = [
      { id: 1, label: `${fmt(today)} 08:00-12:00 | 诊室 A-101` },
      { id: 2, label: `${fmt(today)} 14:00-17:00 | 诊室 A-102` },
    ]
  }
}

onMounted(loadSchedules)
</script>

<style scoped>
.apply-page { min-height: 100vh; background: #f7faff; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 24rpx; background: linear-gradient(90deg, #479fff 0%, #2176ff 100%); }
.title { color: #fff; font-size: 32rpx; font-weight: 700; }
.back { background: #fff; color: #2176ff; border-radius: 999rpx; padding: 8rpx 18rpx; }

.form { padding: 24rpx; }
.section-title .title-text { font-size: 30rpx; color: #1e293b; font-weight: 600; }
.form-card { background: #fff; border-radius: 16rpx; padding: 16rpx; margin-top: 12rpx; box-shadow: 0 6rpx 16rpx rgba(33,118,255,0.08); }
.form-item { margin-bottom: 16rpx; }
.label { color: #64748b; font-size: 26rpx; }
.picker { margin-top: 8rpx; background: #f1f5f9; border-radius: 12rpx; padding: 12rpx; font-size: 26rpx; color: #0f172a; }
.textarea { margin-top: 8rpx; background: #f1f5f9; border-radius: 12rpx; padding: 12rpx; font-size: 26rpx; min-height: 180rpx; }

.submit { width: 100%; background: #2176ff; color: #fff; border-radius: 12rpx; padding: 16rpx; font-size: 28rpx; margin-top: 16rpx; box-shadow: 0 6rpx 16rpx rgba(33,118,255,0.18); }
</style>