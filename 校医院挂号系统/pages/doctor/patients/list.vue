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
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { uniNavigateTo, uniShowToast } from '../../../utils/uniHelper'
import { useUserStore } from '../../../store/user.js'
import { doctorApi } from '../../../api/doctor'

const userStore = useUserStore()
const doctorId = computed(() => userStore.userInfo?.id || 1)

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
    patients.value = await doctorApi.getPatientsByDate(doctorId.value, selectedDate.value)
    if (!Array.isArray(patients.value)) patients.value = []
  } catch (e) {
    patients.value = [
      { appointmentId: 101, patientId: 1001, name: '张三', identity: '学生', appointmentTimeRange: '08:00-12:00', statusText: '已预约', statusClass: 'status-wait' },
      { appointmentId: 102, patientId: 1002, name: '李四', identity: '教职工', appointmentTimeRange: '14:00-17:00', statusText: '已预约', statusClass: 'status-wait' },
    ]
  }
}

const openDetail = (p) => {
  const qs = `?id=${p.patientId}&appointmentId=${p.appointmentId}`
  uniNavigateTo({ url: `/pages/doctor/patients/detail${qs}` })
}

onMounted(async () => {
  const today = new Date()
  const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
  selectedDate.value = fmt(today)
  await loadPatients()
})
</script>

<style scoped>
.patients-page { min-height: 100vh; background: #f7faff; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 24rpx; background: linear-gradient(90deg, #479fff 0%, #2176ff 100%); }
.title { color: #fff; font-size: 32rpx; font-weight: 700; }
.back { background: #fff; color: #2176ff; border-radius: 999rpx; padding: 8rpx 18rpx; }

.filters { padding: 24rpx; }
.filter-card { background: #fff; border-radius: 16rpx; padding: 16rpx; box-shadow: 0 6rpx 16rpx rgba(33,118,255,0.08); display: grid; grid-template-columns: 1fr 1fr; gap: 12rpx; }
.filter-item { }
.label { color: #64748b; font-size: 26rpx; }
.picker { margin-top: 8rpx; background: #f1f5f9; border-radius: 12rpx; padding: 12rpx; font-size: 26rpx; color: #0f172a; }

.list { padding: 24rpx; }
.empty-state { background: #fff; border-radius: 16rpx; padding: 32rpx; text-align: center; color: #64748b; }
.patient-card { background: #fff; border-radius: 16rpx; padding: 16rpx; margin-bottom: 12rpx; box-shadow: 0 6rpx 16rpx rgba(33,118,255,0.08); }
.row { display: flex; justify-content: space-between; align-items: center; margin: 6rpx 0; }
.name { font-size: 30rpx; color: #0f172a; font-weight: 700; }
.identity { font-size: 26rpx; color: #64748b; }
.slot { font-size: 26rpx; color: #334155; }
.status { font-size: 26rpx; }
.status-wait { color: #0ea5e9; }
.status-done { color: #10b981; }
</style>