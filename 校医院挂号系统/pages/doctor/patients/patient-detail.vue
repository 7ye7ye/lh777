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
import { uniNavigateTo, uniShowToast, uniSwitchTab } from '@/utils/uniHelper'

// 患者详情数据
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
    {
      type: '过敏史',
      description: '青霉素过敏'
    },
    {
      type: '慢性病',
      description: '轻度高血压（已控制）'
    }
  ],
  visitHistory: [
    {
      date: '2024-09-15',
      department: '内科',
      doctor: '李医生',
      diagnosis: '急性上呼吸道感染'
    },
    {
      date: '2024-07-20',
      department: '外科',
      doctor: '赵医生',
      diagnosis: '软组织挫伤'
    },
    {
      date: '2024-05-10',
      department: '内科',
      doctor: '王医生',
      diagnosis: '胃炎'
    }
  ]
})

// 就诊备注
const visitNote = ref('')

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    '待接诊': 'status-waiting',
    '接诊中': 'status-processing',
    '已完成': 'status-completed'
  }
  return classMap[status] || ''
}

// 查看就诊详情
const viewVisitDetail = (visit) => {
  uniShowToast({ 
    title: '就诊详情功能开发中', 
    icon: 'none' 
  })
}

// 开始接诊
const receivePatient = async () => {
  try {
    const res = await uniShowModal({
      title: '确认接诊',
      content: `确定开始接诊患者 ${patient.value.name} 吗？`,
      confirmText: '确定',
      cancelText: '取消'
    })
    
    if (res.confirm) {
      // TODO: 调用后端API更新状态
      patient.value.status = '接诊中'
      await uniShowToast({ title: '已开始接诊', icon: 'success' })
    }
  } catch (error) {
    uniShowToast({ title: '操作失败', icon: 'none' })
  }
}

// 完成接诊
const completePatient = async () => {
  if (!visitNote.value.trim()) {
    uniShowToast({ title: '请填写就诊备注', icon: 'none' })
    return
  }

  try {
    const res = await uniShowModal({
      title: '确认完成',
      content: '确定完成本次接诊吗？信息将同步至患者端。',
      confirmText: '确定',
      cancelText: '取消'
    })
    
    if (res.confirm) {
      // TODO: 调用后端API更新状态和备注
      patient.value.status = '已完成'
      await uniShowToast({ title: '接诊已完成', icon: 'success' })
      setTimeout(() => {
        uniNavigateBack()
      }, 1500)
    }
  } catch (error) {
    uniShowToast({ title: '操作失败', icon: 'none' })
  }
}

// 保存备注
const saveNote = async () => {
  if (!visitNote.value.trim()) {
    uniShowToast({ title: '请输入备注内容', icon: 'none' })
    return
  }

  try {
    // TODO: 调用后端API保存备注
    await uniShowToast({ title: '备注保存成功', icon: 'success' })
  } catch (error) {
    uniShowToast({ title: '保存失败', icon: 'none' })
  }
}

// 返回患者信息列表
const goBackToPatientList = () => {
  uniNavigateBack()
}

onMounted(() => {
  // TODO: 从路由参数获取患者ID，加载详细数据
  // const patientId = getCurrentPages()[getCurrentPages().length - 1].options.id
  // loadPatientDetail(patientId)
})
</script>

<style scoped>
.patient-detail-page {
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

/* 信息卡片 */
.info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin: 24rpx 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
  padding-bottom: 20rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.privacy-tip {
  font-size: 24rpx;
  color: #999;
  margin-left: 8rpx;
}

.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
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

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24rpx;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.info-value {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

/* 信息列表 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-label {
  font-size: 28rpx;
  color: #666;
}

.item-value {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

/* 既往病史 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.history-item {
  display: flex;
  align-items: flex-start;
  padding: 20rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
}

.history-tag {
  padding: 4rpx 12rpx;
  background: #1677ff;
  color: #fff;
  border-radius: 8rpx;
  font-size: 22rpx;
  white-space: nowrap;
  margin-right: 16rpx;
}

.history-text {
  flex: 1;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

/* 就诊历史 */
.visit-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.visit-item {
  padding: 24rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
  transition: all 0.3s;
}

.visit-item:active {
  background: #e6f7ff;
  border-color: #1677ff;
}

.visit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.visit-date {
  display: flex;
  align-items: center;
}

.date-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.date-text {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.visit-arrow {
  font-size: 36rpx;
  color: #999;
}

.visit-info {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 8rpx;
}

.visit-dept,
.visit-doctor {
  font-size: 26rpx;
  color: #666;
}

.visit-diagnosis {
  display: flex;
  margin-top: 12rpx;
  padding-top: 12rpx;
  border-top: 2rpx solid #e8e8e8;
}

.diagnosis-label {
  font-size: 24rpx;
  color: #999;
  margin-right: 8rpx;
}

.diagnosis-text {
  flex: 1;
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
}

/* 空状态 */
.empty-history {
  text-align: center;
  padding: 60rpx 0;
}

.empty-text {
  font-size: 26rpx;
  color: #999;
}

/* 操作按钮区域 */
.action-section {
  margin: 0 32rpx 24rpx;
}

.action-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
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

/* 备注区域 */
.note-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin: 0 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.note-header {
  margin-bottom: 20rpx;
}

.note-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.note-textarea {
  width: 100%;
  min-height: 240rpx;
  padding: 24rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  font-size: 28rpx;
  line-height: 1.6;
  border: 2rpx solid #e8e8e8;
}

.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.char-count {
  font-size: 24rpx;
  color: #999;
}

.save-note-btn {
  padding: 12rpx 32rpx;
  background: #1677ff;
  color: #fff;
  border-radius: 8rpx;
  font-size: 26rpx;
  border: none;
  height: auto;
  line-height: normal;
}
</style>

