<template>
  <view class="page-bg">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-text">加载中...</view>
    </view>
    
    <!-- 就诊卡卡片（后端返回patientId即视为有卡） -->
    <view v-else-if="cardInfo.patientId" class="medical-card">
      <view class="card-header">
        <view class="hospital-logo">🏥</view>
        <view class="hospital-name">校医院就诊卡</view>
      </view>
      
      <view class="card-content">
        <!-- 患者姓名：对应后端patientName -->
        <view class="patient-name">{{ cardInfo.patientName }}</view>
        
        <view class="card-details">
          <!-- 门诊号：对应后端outpatientNumber，无数据显示“未分配” -->
          <view class="detail-item">
            <text class="detail-label">门诊号：</text>
            <text class="detail-value">{{ cardInfo.outpatientNumber || '未分配' }}</text>
          </view>
          <!-- 证件类型：对应后端idType，默认显示“身份证” -->
          <view class="detail-item">
            <text class="detail-label">证件类型：</text>
            <text class="detail-value">{{ cardInfo.idType || '身份证' }}</text>
          </view>
          <!-- 证件号码：对应后端idCard，调用脱敏方法 -->
          <view class="detail-item">
            <text class="detail-label">证件号码：</text>
            <text class="detail-value">{{ maskIdNumber(cardInfo.idCard) }}</text>
          </view>
          <!-- 电话：对应后端phone，调用脱敏方法 -->
          <view class="detail-item">
            <text class="detail-label">电话：</text>
            <text class="detail-value">{{ maskPhone(cardInfo.phone) }}</text>
          </view>
          <!-- 患者类型：对应后端patientType（1-学生/2-教师/3-职工） -->
          <view class="detail-item">
            <text class="detail-label">身份类型：</text>
            <text class="detail-value">{{ getPatientTypeName(cardInfo.patientType) }}</text>
          </view>
          <!-- 认证状态：对应后端identityVerify（0-未审核/1-已通过/2-未通过） -->
          <view class="detail-item">
            <text class="detail-label">认证状态：</text>
            <text class="detail-value">{{ getVerifyStatusName(cardInfo.identityVerify) }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 无就诊卡状态（无patientId即视为无卡） -->
    <view v-else class="no-card-container">
      <view class="no-card-icon">💳</view>
      <view class="no-card-text">您还未创建就诊卡</view>
      <button class="create-card-btn" @click="goToCreateCard">立即创建</button>
    </view>

    <!-- 操作按钮（有就诊卡时显示） -->
    <view v-if="cardInfo.patientId" class="action-buttons">
      <button class="action-btn modify-btn" @click="goToModifyInfo">修改个人信息</button>
      <button class="action-btn replace-btn" @click="goToReplaceCard">更换新就诊卡</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { patientApi } from '@/api/patient'
import { uniShowToast, uniShowModal, uniShowLoading, uniHideLoading, uniNavigateBack, uniNavigateTo } from '@/utils/uniHelper'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const cardInfo = ref({}) // 存储后端返回的就诊卡数据
const loading = ref(false)

const getCardInfo = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) throw new Error('未获取到用户ID，请重新登录')
    
    // 调用接口，直接接收后端返回的“纯数据”
    const cardData = await patientApi.getCard({ userId })
    console.log('后端返回的就诊卡数据：', cardData)
    
    // 后端直接返回数据，所以只要拿到数据就视为成功
    if (cardData?.patientId) { // 用patientId判断是否有卡
      cardInfo.value = cardData
    } else {
      cardInfo.value = {}
      showCreateCardPrompt()
    }
  } catch (error) {
    console.log('获取就诊卡失败原因：', error.message)
    uniShowToast({ title: error.message || '获取就诊卡信息失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 显示创建就诊卡提示
const showCreateCardPrompt = () => {
  uniShowModal({
    title: '温馨提示',
    content: '您还未创建就诊卡，是否立即创建？',
    confirmText: '立即创建',
    cancelText: '暂不创建',
    success: (res) => {
      if (res.confirm) {
        uniNavigateTo({ url: '/subpkg/profile/personal/create-card' })
      } else {
        uniNavigateBack()
      }
    }
  })
}

// 脱敏身份证号（适配后端idCard字段）
const maskIdNumber = (idCard) => {
  if (!idCard) return '未填写'
  if (idCard.length < 18) return idCard // 非标准身份证号不脱敏
  return idCard.substring(0, 6) + '********' + idCard.substring(14) // 标准脱敏：保留前6后4
}

// 脱敏手机号（适配后端phone字段）
const maskPhone = (phone) => {
  if (!phone) return '未填写'
  if (phone.length !== 11) return phone // 非11位手机号不脱敏
  return phone.substring(0, 3) + '****' + phone.substring(7) // 标准脱敏：保留前3后4
}

// 转换患者类型名称（后端patientType：1-学生/2-教师/3-职工）
const getPatientTypeName = (type) => {
  const typeMap = { 1: '学生', 2: '教师', 3: '职工' }
  return typeMap[type] || '未知身份'
}

// 转换认证状态名称（后端identityVerify：0-未审核/1-已通过/2-未通过）
const getVerifyStatusName = (status) => {
  const statusMap = { 0: '未审核', 1: '已通过', 2: '未通过' }
  return statusMap[status] || '未设置'
}

// 跳转到创建就诊卡页面
const goToCreateCard = () => {
  uniNavigateTo({ url: '/subpkg/profile/personal/create-card' })
}

// 跳转到修改个人信息页面（可携带当前cardInfo数据）
const goToModifyInfo = () => {
  uniNavigateTo({ 
    url: `/subpkg/profile/personal/modify-info?cardInfo=${encodeURIComponent(JSON.stringify(cardInfo.value))}` 
  })
}

// 更换新就诊卡（可根据实际业务补充逻辑）
const goToReplaceCard = () => {
  uniShowModal({
    title: '提示',
    content: '确定要更换新的就诊卡吗？旧卡信息将失效',
    confirmText: '确定',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        // 此处可补充调用“更换就诊卡”接口的逻辑
        uniShowToast({ title: '更换成功，新卡已生成', icon: 'success' })
        getCardInfo() // 重新获取最新就诊卡信息
      }
    }
  })
}

// 页面挂载时获取就诊卡信息
onMounted(() => {
  getCardInfo()
})
</script>

<style scoped>
.page-bg { 
  min-height: 100vh; 
  background: #f8faff; 
  padding: 24rpx;
}

/* 就诊卡样式 */
.medical-card {
  background: linear-gradient(135deg, #3a9cff 0%, #5db7ff 100%);
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(58, 156, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.medical-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  pointer-events: none;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  position: relative;
  z-index: 1;
}

.hospital-logo {
  font-size: 48rpx;
  margin-right: 16rpx;
}

.hospital-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.card-content {
  position: relative;
  z-index: 1;
}

.patient-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  text-align: center;
  margin-bottom: 30rpx;
}

.card-details {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16rpx;
  padding: 24rpx;
  backdrop-filter: blur(10rpx);
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 8rpx 0;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  width: 140rpx;
  flex-shrink: 0;
}

.detail-value {
  font-size: 26rpx;
  color: #fff;
  flex: 1;
  font-weight: 500;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.action-btn {
  width: 100%;
  height: 88rpx;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 500;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.modify-btn {
  background: #3a9cff;
  color: #fff;
}

.modify-btn:active {
  background: #2980e6;
  transform: scale(0.98);
}

.replace-btn {
  background: #52c41a;
  color: #fff;
}

.replace-btn:active {
  background: #389e0d;
  transform: scale(0.98);
}

/* 加载状态样式 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #666;
}

/* 无就诊卡状态样式 */
.no-card-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 500rpx;
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.no-card-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
  opacity: 0.6;
}

.no-card-text {
  font-size: 32rpx;
  color: #666;
  margin-bottom: 40rpx;
}

.create-card-btn {
  background: #3a9cff;
  color: #fff;
  border: none;
  border-radius: 12rpx;
  padding: 20rpx 40rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.create-card-btn:active {
  background: #2980e6;
  transform: scale(0.98);
}
</style>