<template>
  <view class="patient-list-page">
    <!-- 返回按钮 -->
    <view class="back-section" @click="goBackToSchedule">
      <text class="back-icon">‹</text>
      <text class="back-text">返回医生主界面</text>
    </view>

    <!-- 顶部筛选区域 -->
    <view class="filter-section">
      <view class="date-filter">
        <picker
          mode="date"
          :value="selectedDate"
          :start="minDate"
          :end="maxDate"
          @change="onDateChange"
        >
          <view class="date-picker">
            <text class="picker-icon">📅</text>
            <text class="picker-text">{{ selectedDate || '选择日期' }}</text>
          </view>
        </picker>
      </view>

      <view class="time-filter">
        <scroll-view scroll-x class="time-scroll">
          <view
            v-for="(period, index) in timePeriods"
            :key="index"
            class="time-item"
            :class="{ active: selectedPeriod === index }"
            @click="selectPeriod(index)"
          >
            {{ period }}
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-section">
      <view class="stat-item">
        <text class="stat-value">{{ totalPatients }}</text>
        <text class="stat-label">总挂号</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value waiting-count">{{ waitingPatients }}</text>
        <text class="stat-label">待接诊</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value completed-count">{{ completedPatients }}</text>
        <text class="stat-label">已完成</text>
      </view>
    </view>

    <!-- 患者列表 -->
    <view class="patient-list-container">
      <view v-if="patientList.length === 0" class="empty-state">
        <text class="empty-icon">🏥</text>
        <text class="empty-text">暂无患者挂号信息</text>
      </view>

      <view v-else class="patient-list">
        <view
          v-for="(patient, index) in patientList"
          :key="index"
          class="patient-item"
          @click="viewPatientDetail(patient)"
        >
          <view class="patient-header">
            <view class="patient-basic">
              <view class="patient-name-row">
                <text class="patient-name">{{ patient.name }}</text>
                <view class="identity-tag" :class="getIdentityClass(patient.identity)">
                  {{ patient.identity }}
                </view>
              </view>
              <text class="patient-info">{{ patient.gender }} · {{ patient.age }}岁</text>
            </view>
            <view class="status-badge" :class="getStatusClass(patient.status)">
              {{ patient.status }}
            </view>
          </view>

          <view class="patient-detail">
            <view class="detail-item">
              <text class="detail-icon">⏰</text>
              <text class="detail-text">预约时段：{{ patient.appointmentTime }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-icon">📋</text>
              <text class="detail-text">挂号号码：{{ patient.registrationNumber }}</text>
            </view>
            <view v-if="patient.previousVisit" class="detail-item">
              <text class="detail-icon">🏥</text>
              <text class="detail-text">曾于{{ patient.previousVisit }}就诊</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view v-if="patient.status === '待接诊'" class="action-buttons">
            <button class="action-btn receive-btn" @click.stop="receivePatient(patient)">
              开始接诊
            </button>
          </view>
          <view v-else-if="patient.status === '接诊中'" class="action-buttons">
            <button class="action-btn complete-btn" @click.stop="completePatient(patient)">
              完成接诊
            </button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { uniNavigateTo, uniShowToast, uniSwitchTab } from '@/utils/uniHelper'

// 选中的日期
const selectedDate = ref('')

// 选中的时间段
const selectedPeriod = ref(0) // 0-全部，1-上午，2-下午，3-晚上

// 时间段选项
const timePeriods = ref(['全部', '上午', '下午', '晚上'])

// 患者列表
const patientList = ref([])

// 日期范围
const minDate = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() - 30)
  return formatDate(date)
})

const maxDate = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() + 7)
  return formatDate(date)
})

// 统计数据
const totalPatients = computed(() => patientList.value.length)
const waitingPatients = computed(() => 
  patientList.value.filter(p => p.status === '待接诊').length
)
const completedPatients = computed(() => 
  patientList.value.filter(p => p.status === '已完成').length
)

// 格式化日期
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 日期选择变化
const onDateChange = (e) => {
  selectedDate.value = e.detail.value
  loadPatientData()
}

// 时间段选择
const selectPeriod = (index) => {
  selectedPeriod.value = index
  filterPatients()
}

// 加载患者数据（模拟数据）
const loadPatientData = () => {
  // 模拟数据
  const mockData = [
    {
      id: 1,
      name: '张**',
      gender: '男',
      age: 22,
      identity: '学生',
      appointmentTime: '08:00-08:30',
      registrationNumber: 'R2024101101',
      status: '待接诊',
      timePeriod: '上午',
      previousVisit: '2024年9月15日 - 内科 - 李医生'
    },
    {
      id: 2,
      name: '李**',
      gender: '女',
      age: 35,
      identity: '教师',
      appointmentTime: '08:30-09:00',
      registrationNumber: 'R2024101102',
      status: '接诊中',
      timePeriod: '上午',
      previousVisit: null
    },
    {
      id: 3,
      name: '王**',
      gender: '男',
      age: 28,
      identity: '教职工',
      appointmentTime: '09:00-09:30',
      registrationNumber: 'R2024101103',
      status: '待接诊',
      timePeriod: '上午',
      previousVisit: '2024年8月20日 - 外科 - 赵医生'
    },
    {
      id: 4,
      name: '赵**',
      gender: '女',
      age: 20,
      identity: '学生',
      appointmentTime: '09:30-10:00',
      registrationNumber: 'R2024101104',
      status: '已完成',
      timePeriod: '上午',
      previousVisit: null
    },
    {
      id: 5,
      name: '刘**',
      gender: '男',
      age: 45,
      identity: '教职工',
      appointmentTime: '14:00-14:30',
      registrationNumber: 'R2024101105',
      status: '待接诊',
      timePeriod: '下午',
      previousVisit: '2024年7月10日 - 内科 - 张医生'
    },
    {
      id: 6,
      name: '陈**',
      gender: '女',
      age: 23,
      identity: '学生',
      appointmentTime: '14:30-15:00',
      registrationNumber: 'R2024101106',
      status: '待接诊',
      timePeriod: '下午',
      previousVisit: null
    }
  ]

  patientList.value = mockData
}

// 根据时间段筛选患者
const filterPatients = () => {
  loadPatientData() // 重新加载数据
  
  if (selectedPeriod.value !== 0) {
    const periodName = timePeriods.value[selectedPeriod.value]
    patientList.value = patientList.value.filter(p => p.timePeriod === periodName)
  }
}

// 获取身份标签样式
const getIdentityClass = (identity) => {
  const classMap = {
    '学生': 'identity-student',
    '教师': 'identity-teacher',
    '教职工': 'identity-staff'
  }
  return classMap[identity] || ''
}

// 获取状态样式
const getStatusClass = (status) => {
  const classMap = {
    '待接诊': 'status-waiting',
    '接诊中': 'status-processing',
    '已完成': 'status-completed'
  }
  return classMap[status] || ''
}

// 查看患者详情
const viewPatientDetail = (patient) => {
  uniNavigateTo({ 
    url: `/pages/doctor/patients/patient-detail?id=${patient.id}` 
  })
}

// 开始接诊
const receivePatient = async (patient) => {
  try {
    const res = await uniShowModal({
      title: '确认接诊',
      content: `确定开始接诊患者 ${patient.name} 吗？`,
      confirmText: '确定',
      cancelText: '取消'
    })
    
    if (res.confirm) {
      // TODO: 调用后端API更新状态
      patient.status = '接诊中'
      await uniShowToast({ title: '已开始接诊', icon: 'success' })
    }
  } catch (error) {
    uniShowToast({ title: '操作失败', icon: 'none' })
  }
}

// 完成接诊
const completePatient = async (patient) => {
  try {
    const res = await uniShowModal({
      title: '确认完成',
      content: `确定完成患者 ${patient.name} 的接诊吗？`,
      confirmText: '确定',
      cancelText: '取消'
    })
    
    if (res.confirm) {
      // TODO: 调用后端API更新状态
      patient.status = '已完成'
      await uniShowToast({ title: '接诊已完成', icon: 'success' })
    }
  } catch (error) {
    uniShowToast({ title: '操作失败', icon: 'none' })
  }
}

// 返回医生主界面
const goBackToSchedule = () => {
  uniNavigateTo({ url: '/pages/doctor/schedule/schedule' })
}

onMounted(() => {
  // 默认显示今日数据
  selectedDate.value = formatDate(new Date())
  loadPatientData()
})
</script>

<style scoped>
.patient-list-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40rpx;
}

/* 返回按钮 */
.back-section {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #fff;
  cursor: pointer;
  border-bottom: 2rpx solid #f0f0f0;
}

.back-icon {
  font-size: 48rpx;
  color: #1677ff;
  font-weight: 300;
  margin-right: 8rpx;
  line-height: 1;
}

.back-text {
  font-size: 28rpx;
  color: #1677ff;
}

/* 筛选区域 */
.filter-section {
  background: #fff;
  padding: 24rpx 32rpx;
}

.date-filter {
  margin-bottom: 24rpx;
}

.date-picker {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  background: linear-gradient(135deg, #1677ff 0%, #4da3ff 100%);
  border-radius: 12rpx;
  color: #fff;
}

.picker-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.picker-text {
  font-size: 28rpx;
  font-weight: 500;
}

.time-filter {
  margin-top: 16rpx;
}

.time-scroll {
  white-space: nowrap;
}

.time-item {
  display: inline-block;
  padding: 16rpx 32rpx;
  margin-right: 16rpx;
  background: #f5f7fa;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: #666;
  transition: all 0.3s;
}

.time-item.active {
  background: #1677ff;
  color: #fff;
}

/* 统计区域 */
.stats-section {
  display: flex;
  background: #fff;
  margin: 24rpx 32rpx;
  padding: 32rpx 24rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 48rpx;
  font-weight: 600;
  color: #1677ff;
  margin-bottom: 8rpx;
}

.waiting-count {
  color: #fa8c16;
}

.completed-count {
  color: #52c41a;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

.stat-divider {
  width: 2rpx;
  background: #f0f0f0;
  margin: 0 24rpx;
}

/* 患者列表 */
.patient-list-container {
  padding: 0 32rpx;
}

.empty-state {
  text-align: center;
  padding: 120rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.patient-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.patient-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.patient-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.patient-basic {
  flex: 1;
}

.patient-name-row {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.patient-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-right: 12rpx;
}

.identity-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}

.identity-student {
  background: #e6f7ff;
  color: #1677ff;
}

.identity-teacher {
  background: #fff7e6;
  color: #fa8c16;
}

.identity-staff {
  background: #f6ffed;
  color: #52c41a;
}

.patient-info {
  font-size: 24rpx;
  color: #999;
}

.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  white-space: nowrap;
}

.status-waiting {
  background: #fff7e6;
  color: #fa8c16;
}

.status-processing {
  background: #e6f7ff;
  color: #1677ff;
}

.status-completed {
  background: #f6ffed;
  color: #52c41a;
}

/* 患者详情 */
.patient-detail {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.detail-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

/* 操作按钮 */
.action-buttons {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid #f0f0f0;
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 10rpx;
  font-size: 28rpx;
  border: none;
}

.receive-btn {
  background: linear-gradient(135deg, #1677ff 0%, #4da3ff 100%);
  color: #fff;
}

.complete-btn {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: #fff;
}
</style>

