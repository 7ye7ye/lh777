<template>
  <view class="patients-page">
    <!-- 顶部栏 -->
    <view class="header">
      <text class="title">患者列表</text>
      <button class="back" @click="goBack">返回</button>
    </view>

    <!-- 筛选区 -->
    <view class="filters">
      <view class="filter-card">
        <view class="filter-item">
          <text class="label">出诊日期</text>
          <picker mode="date" @change="onDateChange">
            <view class="picker">{{ selectedDate || '请选择日期' }}</view>
          </picker>
        </view>
        <view class="filter-item">
          <text class="label">时间段</text>
          <picker mode="selector" :range="timeRanges" @change="onTimeChange">
            <view class="picker">{{ selectedSlot || '全部' }}</view>
          </picker>
        </view>
      </view>
    </view>

    <!-- 列表 -->
    <view class="list">
      <view v-if="filteredPatients.length === 0" class="empty-state">
        <text class="empty-text">暂无患者</text>
      </view>

      <view v-else>
        <view v-for="p in filteredPatients" :key="p.appointmentId" class="patient-card" @click="openDetail(p)">
          <view class="row">
            <text class="name">{{ p.name }}</text>
            <text class="identity">{{ p.identity }}</text>
          </view>
          <view class="row">
            <text class="slot">预约时段：{{ p.appointmentTimeRange }}</text>
            <text class="status" :class="p.statusClass">{{ p.statusText }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 医生端底部导航 -->
    <DoctorTabBar active="patients" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import DoctorTabBar from '@/components/DoctorTabBar.vue'

import { uniNavigateTo, uniShowToast } from '../../../utils/uniHelper'
import { useUserStore } from '../../../store/user.js'
import { doctorApi } from '../../../api/doctor_massage'

const userStore = useUserStore()
const doctorId = computed(() => userStore.userInfo?.doctorId ?? userStore.userInfo?.id ?? 1)

const selectedDate = ref('')
const timeRanges = ref(['全部', '08:00-12:00', '14:00-17:00', '18:00-20:00'])
const selectedSlot = ref('全部')

const patients = ref([])

const goBack = () => uni.navigateBack()

const onDateChange = async (e) => {
  selectedDate.value = e.detail.value
  await loadPatients()
}
const onTimeChange = (e) => {
  selectedSlot.value = timeRanges.value[e.detail.value]
}

const filteredPatients = computed(() => {
  if (selectedSlot.value === '全部') return patients.value
  return patients.value.filter(p => p.appointmentTimeRange === selectedSlot.value)
})

const loadPatients = async () => {
  try {
    if (!selectedDate.value) return
    const list = await doctorApi.getPatientsByDate(doctorId.value, selectedDate.value)
    patients.value = Array.isArray(list) ? list : []
  } catch (e) {
    patients.value = []
  }
}

const openDetail = (p) => {
  const qs = `?id=${p.patientId}&appointmentId=${p.appointmentId}`
  uniNavigateTo({ url: `/subpkg/doctor/patients/detail${qs}` })
}

onMounted(async () => {
  const today = new Date()
  const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
  selectedDate.value = fmt(today)
  await loadPatients()
})

onShow(async () => {
  await loadPatients()
})
</script>

<style scoped>
.patients-page { min-height: 100vh; background: linear-gradient(180deg, #e8f3ff 0%, #f7faff 45%, #ffffff 100%); padding-bottom: 120rpx; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 26rpx 24rpx; background: linear-gradient(120deg, #3a9cff 0%, #6ec6ff 100%); box-shadow: 0 10rpx 28rpx rgba(58,156,255,0.22); }
.title { color: #fff; font-size: 34rpx; font-weight: 800; letter-spacing: 1rpx; }
.back { background: #fff; color: #2176ff; border-radius: 999rpx; padding: 10rpx 20rpx; font-size: 26rpx; box-shadow: 0 8rpx 18rpx rgba(0,0,0,0.06); }

.filters { padding: 24rpx; }
.filter-card { background: #fff; border-radius: 18rpx; padding: 18rpx; box-shadow: 0 10rpx 24rpx rgba(58,156,255,0.12); display: grid; grid-template-columns: 1fr 1fr; gap: 14rpx; }
.filter-item { }
.label { color: #64748b; font-size: 26rpx; }
.picker { margin-top: 8rpx; background: #f6f9ff; border-radius: 14rpx; padding: 14rpx; font-size: 26rpx; color: #0f172a; border: 1rpx solid #e1e9f6; }

.list { padding: 0 24rpx 24rpx; }
.empty-state { background: #fff; border-radius: 18rpx; padding: 36rpx; text-align: center; color: #64748b; box-shadow: 0 10rpx 24rpx rgba(58,156,255,0.1); }
.patient-card { background: #fff; border-radius: 18rpx; padding: 18rpx; margin: 14rpx 0; box-shadow: 0 10rpx 24rpx rgba(58,156,255,0.12); border: 1rpx solid #eef3fb; }
.row { display: flex; justify-content: space-between; align-items: center; margin: 8rpx 0; }
.name { font-size: 32rpx; color: #0f172a; font-weight: 800; letter-spacing: 0.5rpx; }
.identity { font-size: 26rpx; color: #64748b; }
.slot { font-size: 26rpx; color: #334155; }
.status { font-size: 26rpx; font-weight: 700; }
.status-wait { color: #0ea5e9; }
.status-progress { color: #f59e0b; }
.status-done { color: #10b981; }
.status-referral { color: #8b5cf6; }
.status-cancel { color: #94a3b8; }
</style>