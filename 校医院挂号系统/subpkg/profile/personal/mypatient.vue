<template>
  <view class="page-bg">
    <!-- 医院信息头部 -->
    <view class="hospital-header">
      <text class="hospital-name">BJTU号号通</text>
      <view class="card-badge">
        <text class="badge-icon">+</text>
        <text class="badge-text">电子就诊卡</text>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-text">加载中...</view>
    </view>

    <!-- 就诊人列表 -->
    <view v-else-if="patientList.length > 0" class="patient-list">
      <view 
        v-for="patient in patientList" 
        :key="patient.patientId" 
        class="patient-card"
        @click="viewPatientDetail(patient)"
      >
        <view class="patient-info">
          <!-- 头像 -->
          <view class="avatar-container">
            <image 
              v-if="patient.avatar" 
              :src="patient.avatar" 
              class="avatar" 
              mode="aspectFill"
            />
            <view v-else class="avatar default-avatar">
              <text class="avatar-text">{{ getFirstChar(patient.patientName) }}</text>
            </view>
          </view>
          
          <!-- 基本信息 -->
          <view class="info-content">
            <view class="name-row">
              <text class="patient-name">{{ patient.patientName }}</text>
              <text v-if="patient.isDefault === 1" class="default-badge">默认</text>
            </view>
            <text class="patient-id">{{ maskIdNumber(patient.idCard) }}</text>
          </view>
        </view>
        
        <!-- 操作按钮 -->
        <view class="action-buttons" @click.stop>
          <view 
            v-if="patient.isDefault !== 1" 
            class="set-default-btn" 
            @click="setAsDefault(patient)"
          >
            <text class="btn-icon">⭐</text>
            <text class="btn-text">设为默认</text>
          </view>
          <view v-else class="default-indicator">
            <text class="indicator-icon">✓</text>
            <text class="indicator-text">默认就诊卡</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-container">
      <image 
        class="empty-bg-image" 
        :src="getStaticImage('/static/images/no_data.png')" 
        mode="scaleToFill"
      />
      <view class="empty-content">
        <view class="empty-text">暂无就诊人</view>
        <view class="empty-tip">请先添加就诊人</view>
        <button class="add-patient-btn-empty" @click="goToAddPatient">添加就诊人</button>
      </view>
    </view>

    <!-- 底部统计和添加按钮（有数据时显示） -->
    <view v-if="patientList.length > 0" class="footer-section">
      <text class="footer-text">
        已添加<text class="highlight-num">{{ patientList.length }}</text>位就诊人，
        还可以添加<text class="highlight-num">{{ maxPatients - patientList.length }}</text>位
      </text>
      <button class="add-patient-btn" @click="goToAddPatient">添加就诊人</button>
      <button class="unbind-link" @click="showUnbindHelp">解绑电子就诊卡</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { patientApi } from '@/api/patient'
import { useUserStore } from '@/store/user'
import { uniShowToast, uniNavigateTo, uniShowModal } from '@/utils/uniHelper'
import { getStaticImage } from '@/utils/imageHelper'

const userStore = useUserStore()
const loading = ref(false)
const patientList = ref([])
const maxPatients = 5 // 最多5位就诊人

// 获取就诊人列表
const getPatientList = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) {
      console.log('未获取到用户ID')
      patientList.value = []
      loading.value = false
      return
    }
    
    const res = await patientApi.getPatientList({ userId })
    console.log('获取就诊人列表响应:', res)
    
    // 处理不同格式的响应
    let listData = []
    if (Array.isArray(res)) {
      listData = res
    } else if (res && Array.isArray(res.list)) {
      listData = res.list
    } else if (res && res.data && Array.isArray(res.data)) {
      listData = res.data
    } else {
      console.log('未识别的响应格式:', res)
      listData = []
    }
    
    console.log('处理后的就诊人列表:', listData)
    
    // 确保数组是全新的引用，以触发响应式更新
    patientList.value = JSON.parse(JSON.stringify(listData))
    console.log('更新后的patientList:', patientList.value)
  } catch (error) {
    console.error('获取就诊人列表失败:', error)
    patientList.value = []
    // 只在非网络错误时提示
    if (error.message && !error.message.includes('Network')) {
      uniShowToast({ 
        title: '获取就诊人列表失败', 
        icon: 'none' 
      })
    }
  } finally {
    loading.value = false
  }
}

// 脱敏身份证号
const maskIdNumber = (idCard) => {
  if (!idCard) return '未填写'
  if (idCard.length < 18) return idCard
  return idCard.substring(0, 3) + '**********' + idCard.substring(14)
}

// 获取姓名首字符
const getFirstChar = (name) => {
  if (!name) return '?'
  return name.charAt(0)
}

// 查看就诊人详情
const viewPatientDetail = (patient) => {
  uniNavigateTo({ 
    url: `/subpkg/profile/personal/mycard?patientId=${patient.patientId}` 
  })
}

// 设置为默认就诊卡
const setAsDefault = async (patient) => {
  // 如果已经是默认，直接返回
  if (patient.isDefault === 1) return
  
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) {
      uniShowToast({ title: '未获取到用户ID', icon: 'none' })
      return
    }

    // 显示加载中
    uni.showLoading({ title: '设置中...', mask: true })
    
    // 调用设置默认接口
    await patientApi.setDefaultPatient({ userId, patientId: patient.patientId })
    
    // 强制刷新列表 - 先清空再获取新数据
    patientList.value = []
    await new Promise(resolve => setTimeout(resolve, 50)) // 确保UI有机会更新
    
    // 获取更新后的列表
    await getPatientList()
    
    // 显示成功提示
    uni.hideLoading()
    
    // 使用setTimeout确保UI更新完成再显示提示
    setTimeout(() => {
      uniShowToast({ 
        title: `已设置${patient.patientName}为默认就诊人`,
        icon: 'success',
        duration: 2000
      })
    }, 300)
    
  } catch (error) {
    console.error('设置默认就诊卡失败:', error)
    uni.hideLoading()
    const errorMsg = error?.response?.data?.description || error?.message || '设置失败，请稍后重试'
    uniShowToast({ 
      title: errorMsg, 
      icon: 'none',
      duration: 2000
    })
  }
}

// 跳转到添加就诊人页面
const goToAddPatient = () => {
  if (patientList.value.length >= maxPatients) {
    uniShowToast({ 
      title: `最多只能添加${maxPatients}位就诊人`, 
      icon: 'none' 
    })
    return
  }
  uniNavigateTo({ url: '/subpkg/profile/personal/create-card' })
}

// 解绑帮助
const showUnbindHelp = () => {
  uniShowModal({
    title: '解绑说明',
    content: '如需解绑就诊卡，请在每个就诊人详情页进行操作',
    confirmText: '我知道了',
    showCancel: false
  })
}

// 页面挂载时获取数据
onMounted(() => {
  getPatientList()
})

// 页面显示时刷新数据
onShow(() => {
  console.log('mypatient页面onShow，刷新数据')
  getPatientList()
})
</script>

<style scoped>
.page-bg {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 医院信息头部 */
.hospital-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 24rpx;
  background: #fff;
  margin-bottom: 16rpx;
}

.hospital-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.card-badge {
  display: flex;
  align-items: center;
  background: #ff4d4f;
  color: #fff;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.badge-icon {
  font-size: 20rpx;
  font-weight: bold;
  margin-right: 4rpx;
}

.badge-text {
  font-size: 22rpx;
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 80rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

/* 就诊人列表 */
.patient-list {
  padding: 0 24rpx;
}

.patient-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  position: relative;
  transition: all 0.3s ease;
}

.patient-card:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.patient-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.avatar-container {
  margin-right: 24rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
}

.default-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 40rpx;
  color: #fff;
  font-weight: bold;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.patient-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300rpx;
}

.default-badge {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  color: #fff;
  font-size: 20rpx;
  padding: 6rpx 14rpx;
  border-radius: 12rpx;
  font-weight: 500;
  box-shadow: 0 2rpx 6rpx rgba(82, 196, 26, 0.3);
}

.patient-id {
  font-size: 26rpx;
  color: #999;
}

.action-buttons {
  flex-shrink: 0;
  margin-left: 24rpx;
}

.set-default-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: linear-gradient(135deg, #3a9cff 0%, #2980e6 100%);
  color: #fff;
  font-size: 26rpx;
  padding: 16rpx 28rpx;
  border-radius: 24rpx;
  white-space: nowrap;
  box-shadow: 0 4rpx 12rpx rgba(58, 156, 255, 0.3);
  transition: all 0.3s ease;
}

.set-default-btn:active {
  background: linear-gradient(135deg, #2980e6 0%, #1e6bb8 100%);
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(58, 156, 255, 0.4);
}

.btn-icon {
  font-size: 28rpx;
  line-height: 1;
}

.btn-text {
  font-weight: 500;
  letter-spacing: 0.5rpx;
}

.default-indicator {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  color: #fff;
  font-size: 24rpx;
  padding: 16rpx 28rpx;
  border-radius: 24rpx;
  white-space: nowrap;
  box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.3);
}

.indicator-icon {
  font-size: 28rpx;
  line-height: 1;
  font-weight: bold;
}

.indicator-text {
  font-weight: 500;
  letter-spacing: 0.5rpx;
}

/* 底部统计和添加按钮 */
.footer-section {
  padding: 40rpx 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.footer-text {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 32rpx;
  text-align: center;
}

.highlight-num {
  color: #3a9cff;
  font-weight: 600;
  padding: 0 4rpx;
}

.add-patient-btn {
  width: 100%;
  height: 88rpx;
  background: #3a9cff;
  color: #fff;
  border: none;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.add-patient-btn:active {
  background: #2980e6;
  transform: scale(0.98);
}

.unbind-link {
  background: transparent;
  color: #3a9cff;
  font-size: 28rpx;
  border: none;
  padding: 16rpx;
}

.unbind-link::after {
  border: none;
}

.unbind-link:active {
  opacity: 0.7;
}

/* 空状态样式 */
.empty-container {
  position: relative;
  min-height: calc(100vh - 200rpx);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 40rpx 120rpx;
}

.empty-bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  opacity: 0.6;
}

.empty-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  margin-top: auto;
}

.empty-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.empty-tip {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 48rpx;
}

.add-patient-btn-empty {
  background: #3a9cff;
  color: #fff;
  border: none;
  border-radius: 44rpx;
  padding: 24rpx 80rpx;
  font-size: 30rpx;
  font-weight: 500;
  box-shadow: 0 8rpx 24rpx rgba(58, 156, 255, 0.3);
}

.add-patient-btn-empty:active {
  background: #2980e6;
  transform: scale(0.98);
}
</style>
