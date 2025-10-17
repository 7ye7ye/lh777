<template>
  <view class="patient-detail-page">
    <!-- 返回按钮 -->
    <view class="back-section" @click="goBackToPatientList">
      <text class="back-icon">‹</text>
      <text class="back-text">返回患者列表</text>
    </view>

    <!-- 患者基本信息卡片 -->
    <view class="info-card">
      <view class="card-header">
        <text class="card-title">基本信息</text>
      </view>
      <view class="info-grid">
        <view class="info-item">
          <text class="info-label">姓名</text>
          <text class="info-value">{{ patient.name }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">性别</text>
          <text class="info-value">{{ patient.gender }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">年龄</text>
          <text class="info-value">{{ patient.age }}岁</text>
        </view>
        <view class="info-item">
          <text class="info-label">身份</text>
          <text class="info-value">{{ patient.identity }}</text>
        </view>
        <view class="info-item full-width">
          <text class="info-label">联系方式</text>
          <text class="info-value">{{ patient.phone }}</text>
        </view>
      </view>
    </view>

    <!-- 挂号信息卡片 -->
    <view class="info-card">
      <view class="card-header">
        <text class="card-title">挂号信息</text>
        <view class="status-badge" :class="getStatusClass(patient.status)">
          {{ patient.status }}
        </view>
      </view>
      <view class="info-list">
        <view class="list-item">
          <text class="item-label">挂号号码</text>
          <text class="item-value">{{ patient.registrationNumber }}</text>
        </view>
        <view class="list-item">
          <text class="item-label">预约时段</text>
          <text class="item-value">{{ patient.appointmentTime }}</text>
        </view>
        <view class="list-item">
          <text class="item-label">就诊科室</text>
          <text class="item-value">{{ patient.department }}</text>
        </view>
        <view class="list-item">
          <text class="item-label">就诊医生</text>
          <text class="item-value">{{ patient.doctor }}</text>
        </view>
      </view>
    </view>

    <!-- 既往病史卡片 -->
    <view class="info-card">
      <view class="card-header">
        <text class="card-title">既往病史</text>
        <text class="privacy-tip">（脱敏展示）</text>
      </view>
      <view v-if="patient.medicalHistory && patient.medicalHistory.length > 0" class="history-list">
        <view
          v-for="(history, index) in patient.medicalHistory"
          :key="index"
          class="history-item"
        >
          <view class="history-tag">{{ history.type }}</view>
          <text class="history-text">{{ history.description }}</text>
        </view>
      </view>
      <view v-else class="empty-history">
        <text class="empty-text">暂无既往病史记录</text>
      </view>
    </view>

    <!-- 就诊历史卡片 -->
    <view class="info-card">
      <view class="card-header">
        <text class="card-title">就诊历史</text>
      </view>
      <view v-if="patient.visitHistory && patient.visitHistory.length > 0" class="visit-list">
        <view
          v-for="(visit, index) in patient.visitHistory"
          :key="index"
          class="visit-item"
          @click="viewVisitDetail(visit)"
        >
          <view class="visit-header">
            <view class="visit-date">
              <text class="date-icon">📅</text>
              <text class="date-text">{{ visit.date }}</text>
            </view>
            <text class="visit-arrow">›</text>
          </view>
          <view class="visit-info">
            <text class="visit-dept">{{ visit.department }}</text>
            <text class="visit-doctor">{{ visit.doctor }}</text>
          </view>
          <view v-if="visit.diagnosis" class="visit-diagnosis">
            <text class="diagnosis-label">诊断：</text>
            <text class="diagnosis-text">{{ visit.diagnosis }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-history">
        <text class="empty-text">暂无就诊历史</text>
      </view>
    </view>

    <!-- 操作按钮区域 -->
    <view v-if="patient.status !== '已完成'" class="action-section">
      <button
        v-if="patient.status === '待接诊'"
        class="action-btn receive-btn"
        @click="receivePatient"
      >
        开始接诊
      </button>
      <button
        v-else-if="patient.status === '接诊中'"
        class="action-btn complete-btn"
        @click="completePatient"
      >
        完成接诊
      </button>
    </view>

    <!-- 备注输入区域 -->
    <view v-if="patient.status === '接诊中'" class="note-section">
      <view class="note-header">
        <text class="note-title">就诊备注</text>
      </view>
      <textarea
        class="note-textarea"
        v-model="visitNote"
        placeholder="请输入就诊情况、诊断结果等信息..."
        maxlength="500"
      />
      <view class="note-footer">
        <text class="char-count">{{ visitNote.length }}/500</text>
        <button class="save-note-btn" @click="saveNote">保存备注</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { uniNavigateTo, uniShowToast } from '@/utils/uniHelper'

// 患者详情数据（支持示例数据与外部传入）
const patient = ref({
  name: '张**',
  gender: '男',
  age: 22,
  identity: '学生',
  phone: '138****5678',
  registrationNumber: 'R2024101101',
  appointmentTime: '2024-10-11 08:00-08:30',
  department: '内科',
  doctor: '张医生',
  status: '待接诊',
  medicalHistory: [
    { type: '过敏史', description: '青霉素过敏' },
    { type: '慢性病', description: '轻度高血压（已控制）' }
  ],
  visitHistory: [
    { date: '2024-09-15', department: '内科', doctor: '李医生', diagnosis: '急性上呼吸道感染' },
    { date: '2024-07-20', department: '骨科', doctor: '王医生', diagnosis: '踝关节扭伤' }
  ]
})

const visitNote = ref('')

const getStatusClass = (status) => {
  if (status === '待接诊') return 'status-pending'
  if (status === '接诊中') return 'status-progress'
  if (status === '已完成') return 'status-done'
  return 'status-pending'
}

function goBackToPatientList() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uniNavigateTo('/pages/doctor/patients/list')
  }
}

function receivePatient() {
  patient.value.status = '接诊中'
  uniShowToast('已开始接诊')
}

function completePatient() {
  patient.value.status = '已完成'
  uniShowToast('已完成接诊')
}

function saveNote() {
  if (!visitNote.value) {
    uniShowToast('请输入备注内容')
    return
  }
  uniShowToast('备注已保存')
}

function viewVisitDetail(visit) {
  uniShowToast(`就诊记录：${visit.date} ${visit.department}`)
}

// URL 参数传入（patient=encodeURIComponent(JSON.stringify(obj))）
onLoad((options) => {
  try {
    if (options && options.patient) {
      const incoming = JSON.parse(decodeURIComponent(options.patient))
      patient.value = { ...patient.value, ...incoming }
    }
  } catch (e) {
    // ignore parse error
  }
})

// EventChannel 传入（推荐从列表页使用）
onMounted(() => {
  const pages = getCurrentPages()
  const cur = pages[pages.length - 1]
  if (cur && cur.getOpenerEventChannel) {
    const ec = cur.getOpenerEventChannel()
    if (ec) {
      ec.on('patient', (data) => {
        if (data) patient.value = { ...patient.value, ...data }
      })
      ec.on('sendPatient', (data) => {
        if (data && data.patient) {
          patient.value = { ...patient.value, ...data.patient }
        }
      })
    }
  }
})
</script>

<style scoped>
.patient-detail-page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 24rpx;
  background: linear-gradient(180deg, #e9f2ff 0%, #f7f9fc 100%);
}

/* 返回 */
.back-section {
  display: flex;
  align-items: center;
  color: #2a7bff;
  font-weight: 600;
  margin-bottom: 20rpx;
}
.back-icon {
  font-size: 40rpx;
  margin-right: 8rpx;
}
.back-text {
  font-size: 28rpx;
}

/* 卡片通用 */
.info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(42, 123, 255, 0.08);
  margin-bottom: 24rpx;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}
.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a1a;
}

/* 状态徽标 */
.status-badge {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 600;
}
.status-pending {
  color: #8a6d3b;
  background: #fff3cd;
}
.status-progress {
  color: #0f5132;
  background: #d1e7dd;
}
.status-done {
  color: #084298;
  background: #cfe2ff;
}

/* 基本信息网格 */
.info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}
.info-item {
  width: calc(50% - 8rpx);
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 16rpx;
}
.info-item.full-width {
  width: 100%;
}
.info-label {
  font-size: 24rpx;
  color: #7a8aa0;
}
.info-value {
  margin-top: 6rpx;
  font-size: 28rpx;
  color: #2f3b52;
  font-weight: 600;
}

/* 列表信息 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.list-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx;
  background: #f8fafc;
  border-radius: 12rpx;
}
.item-label {
  color: #7a8aa0;
  font-size: 24rpx;
}
.item-value {
  color: #2f3b52;
  font-size: 28rpx;
  font-weight: 600;
}

/* 病史 */
.privacy-tip {
  color: #7a8aa0;
  font-size: 24rpx;
  margin-left: 8rpx;
}
.history-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: #f8fafc;
  padding: 16rpx;
  border-radius: 12rpx;
}
.history-tag {
  background: #e9f2ff;
  color: #2a7bff;
  font-size: 22rpx;
  border-radius: 999rpx;
  padding: 6rpx 12rpx;
}
.history-text {
  font-size: 26rpx;
  color: #2f3b52;
}
.empty-history {
  padding: 24rpx;
  text-align: center;
  color: #7a8aa0;
}
.empty-text {
  font-size: 26rpx;
}

/* 就诊历史 */
.visit-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.visit-item {
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 16rpx;
}
.visit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}
.visit-date {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #2f3b52;
  font-weight: 600;
}
.date-icon {
  font-size: 28rpx;
}
.date-text {
  font-size: 28rpx;
}
.visit-arrow {
  font-size: 40rpx;
  color: #a0b7e0;
}
.visit-info {
  display: flex;
  justify-content: space-between;
  color: #7a8aa0;
  font-size: 24rpx;
}
.visit-diagnosis {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #7a8aa0;
}
.diagnosis-label {
  font-weight: 600;
}
.diagnosis-text {
  color: #2f3b52;
}

/* 操作区 */
.action-section {
  margin: 24rpx 0;
  display: flex;
  gap: 16rpx;
}
.action-btn {
  flex: 1;
  border: none;
  border-radius: 12rpx;
  padding: 24rpx 0;
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
}
.receive-btn {
  background: linear-gradient(90deg, #2a7bff, #6aa9ff);
  box-shadow: 0 12rpx 20rpx rgba(42, 123, 255, 0.24);
}
.complete-btn {
  background: linear-gradient(90deg, #00c853, #52e176);
  box-shadow: 0 12rpx 20rpx rgba(0, 200, 83, 0.24);
}

/* 备注区 */
.note-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(42, 123, 255, 0.08);
}
.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}
.note-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a1a;
}
.note-textarea {
  width: 100%;
  min-height: 160rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 16rpx;
  font-size: 26rpx;
  color: #2f3b52;
  box-sizing: border-box;
}
.note-footer {
  margin-top: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.char-count {
  font-size: 24rpx;
  color: #7a8aa0;
}
.save-note-btn {
  background: linear-gradient(90deg, #2a7bff, #6aa9ff);
  color: #fff;
  border: none;
  border-radius: 12rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  font-weight: 700;
}
</style>