<template>
  <view class="page-bg">
    <!-- 选项卡切换 -->
    <view class="tabs" v-if="cardInfo.patientId">
      <view 
        :class="['tab-item', activeTab === 'card' ? 'active' : '']" 
        @click="activeTab = 'card'"
      >
        电子就诊卡
      </view>
      <view 
        :class="['tab-item', activeTab === 'inpatient' ? 'active' : '']" 
        @click="activeTab = 'inpatient'"
      >
        住院号
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-text">加载中...</view>
    </view>
    
    <!-- 就诊卡卡片（电子就诊卡） -->
    <view v-else-if="cardInfo.patientId && activeTab === 'card'" class="card-container">
      <view class="medical-card">
        <!-- 门诊号 -->
        <view class="outpatient-number">
          门诊号：{{ cardInfo.outpatientNumber || 'M017080045' }}
        </view>
        
        <!-- 二维码 -->
        <view class="qrcode-container">
          <uqrcode 
            ref="qrcodeRef"
            canvas-id="qrcode"
            :value="cardInfo.outpatientNumber || 'M017080045'" 
            :size="200"
            :margin="5"
            background-color="#FFFFFF"
            foreground-color="#000000"
            file-type="png"
          ></uqrcode>
        </view>
        
        <!-- 分隔线 -->
        <view class="divider"></view>
        
        <!-- 患者姓名 -->
        <view class="patient-name">{{ cardInfo.patientName }}</view>
        
        <!-- 详细信息 -->
        <view class="card-details">
          <view class="detail-row">
            <text class="detail-label">门 诊 号：</text>
            <text class="detail-value">{{ cardInfo.outpatientNumber || 'M017080045' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">证件类型：</text>
            <text class="detail-value">{{ cardInfo.idType || '身份证' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">证件号码：</text>
            <text class="detail-value">{{ maskIdNumber(cardInfo.idCard) }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">电    话：</text>
            <text class="detail-value">{{ maskPhone(cardInfo.phone) }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">地    址：</text>
            <text class="detail-value">{{ cardInfo.detailedAddress || cardInfo.region || '未填写' }}</text>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <button class="action-btn modify-btn" @click="goToModifyInfo">修改个人信息</button>
        <button class="action-btn replace-btn" @click="goToReplaceCard">更换新就诊卡</button>
        <button class="action-btn unbind-btn" @click="handleUnbind">解绑电子就诊卡</button>
      </view>
    </view>
    
    <!-- 无就诊卡状态 -->
    <view v-else-if="!loading && !cardInfo.patientId" class="no-card-container">
      <image 
        class="no-card-bg-image" 
        src="/static/images/no_data.png" 
        mode="scaleToFill"
      />
      <view class="no-card-content">
        <view class="no-card-text">暂无就诊卡</view>
        <view class="no-card-tip">请先创建就诊卡</view>
        <button class="create-card-btn" @click="goToCreateCard">立即创建</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'

import uqrcode from '@/uni_modules/Sansnn-uQRCode/components/uqrcode/uqrcode.vue'
import { patientApi } from '@/api/patient'
import { uniShowToast, uniShowModal, uniNavigateBack, uniNavigateTo } from '@/utils/uniHelper'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const cardInfo = ref({}) // 存储后端返回的就诊卡数据
const loading = ref(false)
const activeTab = ref('card') // 当前选项卡：card-电子就诊卡，inpatient-住院号
const routePatientId = ref(null) // 路由参数携带的patientId

// 读取路由参数中的 patientId
onLoad((query) => {
  if (query && query.patientId) {
    const id = Number(query.patientId)
    routePatientId.value = Number.isNaN(id) ? null : id
  }
})

const getCardInfo = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.userId
    if (!userId && !routePatientId.value) {
      console.log('未获取到用户ID和patientId')
      cardInfo.value = {}
      loading.value = false
      return
    }

    // 优先根据 patientId 查询指定就诊人的就诊卡，若无则按 userId 查询默认就诊卡
    const params = {}

    if (routePatientId.value) {
      params.patientId = routePatientId.value
    } else if (userId) {
      params.userId = userId
    }

    // 调用接口，直接接收后端返回的"纯数据"
    const cardData = await patientApi.getCard(params)

    console.log('后端返回的就诊卡数据：', cardData)
    
    // 后端直接返回数据，所以只要拿到数据就视为成功
    if (cardData && cardData.patientId) { // 用patientId判断是否有卡
      cardInfo.value = cardData
    } else {
      // 没有就诊卡数据
      cardInfo.value = {}
      console.log('暂无就诊卡数据')
    }
  } catch (error) {
    console.log('获取就诊卡失败:', error)
    cardInfo.value = {}
    // 只在非网络错误时提示
    if (error.message && !error.message.includes('Network')) {
      uniShowToast({ title: '获取就诊卡信息失败', icon: 'none' })
    }
  } finally {
    loading.value = false
  }
}

// 跳转到创建就诊卡页面
const goToCreateCard = () => {
  uniNavigateTo({ url: '/subpkg/profile/personal/create-card' })
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

// 解绑就诊卡
const handleUnbind = () => {
  uniShowModal({
    title: '确认解绑',
    content: '解绑后您将无法使用该就诊卡，确定要解绑吗？',
    confirmText: '确定解绑',
    cancelText: '取消',
    success: async (res) => {
      if (res.confirm) {
        try {
          const userId = userStore.userInfo?.userId
          if (!userId) throw new Error('未获取到用户ID')
          if (!cardInfo.value || !cardInfo.value.patientId) throw new Error('未获取到就诊人ID')

          await patientApi.unbindCard({ userId, patientId: cardInfo.value.patientId })

          uniShowToast({ title: '解绑成功', icon: 'success' })
          
          // 清空就诊卡信息
          cardInfo.value = {}
          
          // 1.5秒后返回上一页
          setTimeout(() => {
            uniNavigateBack()
          }, 1500)
        } catch (error) {
          console.error('解绑失败:', error)
          uniShowToast({ 
            title: error.message || '解绑失败', 
            icon: 'none' 
          })
        }
      }
    }
  })
}

// 页面挂载时获取就诊卡信息
onMounted(() => {
  getCardInfo()
})

// 页面显示时刷新就诊卡信息（从创建页面返回时会触发）
onShow(() => {
  console.log('mycard页面onShow，刷新数据')
  getCardInfo()
})
</script>

<style scoped>
.page-bg { 
  min-height: 100vh; 
  background: #f5f5f5; 
}

/* 选项卡样式 */
.tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 28rpx 0;
  font-size: 30rpx;
  color: #666;
  position: relative;
}

.tab-item.active {
  color: #3a9cff;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: #3a9cff;
  border-radius: 2rpx;
}

/* 卡片容器 */
.card-container {
  padding: 24rpx;
}

/* 就诊卡样式 */
.medical-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

/* 门诊号 */
.outpatient-number {
  text-align: center;
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 32rpx;
  margin-top: 20rpx;
  letter-spacing: 2rpx;
}

/* 二维码容器 */
.qrcode-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 30rpx 0;
  margin-bottom: 40rpx;
}

/* 分隔线 */
.divider {
  height: 1px;
  background: #e5e5e5;
  margin: 30rpx 0 40rpx;
}

/* 患者姓名 */
.patient-name {
  font-size: 44rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 40rpx;
}

/* 详细信息 */
.card-details {
  padding: 0 20rpx;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 24rpx;
  line-height: 1.6;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 28rpx;
  color: #666;
  width: 160rpx;
  flex-shrink: 0;
}

.detail-value {
  font-size: 28rpx;
  color: #333;
  flex: 1;
  word-break: break-all;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding: 0 24rpx 24rpx;
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

.unbind-btn {
  background: #fff;
  color: #3a9cff;
  border: 2rpx solid #3a9cff;
}

.unbind-btn:active {
  background: #f0f8ff;
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
  position: relative;
  min-height: calc(100vh - 100rpx);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 40rpx 120rpx;
}

.no-card-bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  opacity: 0.6;
}

.no-card-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  margin-top: auto;
}

.no-card-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.no-card-tip {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 48rpx;
}

.create-card-btn {
  background: #3a9cff;
  color: #fff;
  border: none;
  border-radius: 44rpx;
  padding: 24rpx 80rpx;
  font-size: 30rpx;
  font-weight: 500;
  box-shadow: 0 8rpx 24rpx rgba(58, 156, 255, 0.3);
}

.create-card-btn:active {
  background: #2980e6;
  transform: scale(0.98);
}
</style>