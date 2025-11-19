<template>
  <view class="referral-bg">
    <view class="page-header">
      <view class="back-btn" @click="goBack">←</view>
      <text class="page-title">转诊详情</text>
      <view class="header-right"></view>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 状态卡片 -->
      <view class="status-card" :class="getStatusClass(referralDetail.status)">
        <view class="status-header">
          <view class="status-icon">
            <image :src="getStatusIcon(referralDetail.status)" mode="aspectFit" class="icon-img"></image>
          </view>
          <view class="status-info">
            <text class="status-text">{{ referralDetail.status }}</text>
            <text class="status-desc">{{ getStatusDescription(referralDetail.status) }}</text>
          </view>
        </view>
        <view class="hospital-info">
          <text class="hospital-name">{{ referralDetail.targetHospital }}</text>
          <text class="department">{{ referralDetail.targetDepartment }}</text>
        </view>
      </view>

      <!-- 申请信息 -->
      <view class="info-section">
        <view class="section-title">申请信息</view>
        <view class="info-grid">
          <view class="info-item">
            <text class="info-label">申请编号</text>
            <text class="info-value">{{ referralDetail.id }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">申请时间</text>
            <text class="info-value">{{ referralDetail.applyTime }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">审核时间</text>
            <text class="info-value">{{ referralDetail.reviewTime || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 患者信息 -->
      <view class="info-section">
        <view class="section-title">患者信息</view>
        <view class="info-grid">
          <view class="info-item">
            <text class="info-label">姓名</text>
            <text class="info-value">{{ referralDetail.patientName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">性别</text>
            <text class="info-value">{{ referralDetail.gender || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">年龄</text>
            <text class="info-value">{{ referralDetail.age || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">联系电话</text>
            <text class="info-value">{{ referralDetail.phone || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 病情描述 -->
      <view class="info-section">
        <view class="section-title">病情描述</view>
        <view class="description-item">
          <text class="desc-label">症状描述</text>
          <text class="desc-content">{{ referralDetail.symptoms || '-' }}</text>
        </view>
        <view class="description-item">
          <text class="desc-label">既往病史</text>
          <text class="desc-content">{{ referralDetail.medicalHistory || '-' }}</text>
        </view>
        <view class="description-item">
          <text class="desc-label">转诊原因</text>
          <text class="desc-content">{{ referralDetail.reason || '-' }}</text>
        </view>
      </view>

      <!-- 审核信息 -->
      <view v-if="referralDetail.status !== '待审核'" class="info-section">
        <view class="section-title">审核信息</view>
        <view class="description-item">
          <text class="desc-label">审核医生</text>
          <text class="desc-content">{{ referralDetail.reviewDoctor || '系统管理员' }}</text>
        </view>
        <view v-if="referralDetail.status === '已拒绝'" class="description-item">
          <text class="desc-label">拒绝原因</text>
          <text class="desc-content reject-reason">{{ referralDetail.rejectReason || '未说明' }}</text>
        </view>
        <view v-if="referralDetail.status === '待审核'" class="description-item">
          <text class="desc-label">审核状态</text>
          <text class="desc-content waiting-tips">请等待医生审核...</text>
        </view>
        <view v-else class="description-item">
          <text class="desc-label">审核意见</text>
          <text class="desc-content">{{ referralDetail.reviewComments || '同意转诊' }}</text>
        </view>
      </view>

      <!-- 上传资料预览 -->
      <view v-if="referralDetail.attachments && referralDetail.attachments.length > 0" class="info-section">
        <view class="section-title">上传资料</view>
        <view class="attachment-list">
          <view 
            v-for="(item, index) in referralDetail.attachments" 
            :key="index"
            class="attachment-item"
            @click="previewImage(item.url, index)"
          >
            <image :src="item.url" mode="aspectFill" class="attachment-img"></image>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-section">
        <button class="primary-btn" @click="goBack">返回列表</button>
        <button v-if="referralDetail.status === '待审核'" 
                class="cancel-btn" 
                @click="cancelReferral"
                :disabled="submitting">
          {{ submitting ? '处理中...' : '取消申请' }}
        </button>
        <button class="secondary-btn" @click="createNewReferral">创建新申请</button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getReferralDetail, cancelReferralApplication } from '../../api/referral'

// 转诊详情数据
const referralDetail = ref({
  id: '',
  patientName: '',
  gender: '',
  age: '',
  phone: '',
  symptoms: '',
  medicalHistory: '',
  reason: '',
  targetHospital: '',
  targetDepartment: '',
  applyTime: '',
  reviewTime: '',
  status: '',
  reviewDoctor: '',
  reviewComments: '',
  rejectReason: '',
  attachments: []
})
const loading = ref(true)
const submitting = ref(false)

// 获取路由参数
const route = useRoute()
const referralId = computed(() => route.params.id)

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 获取状态对应的样式类
const getStatusClass = (status) => {
  switch (status) {
    case '待审核':
      return 'status-pending'
    case '已审核':
      return 'status-approved'
    case '已拒绝':
      return 'status-rejected'
    default:
      return ''
  }
}

// 获取状态图标
const getStatusIcon = (status) => {
  switch (status) {
    case '待审核':
      return '/static/info_message.png'
    case '已审核':
      return '/static/check.svg'
    case '已拒绝':
      return '/static/complain.svg'
    default:
      return '/static/info_message.png'
  }
}

// 获取状态描述
const getStatusDescription = (status) => {
  switch (status) {
    case '待审核':
      return '您的转诊申请正在等待医生审核'
    case '已审核':
      return '您的转诊申请已通过审核，可以前往目标医院就诊'
    case '已拒绝':
      return '您的转诊申请未通过审核，请查看拒绝原因'
    default:
      return ''
  }
}

// 预览图片
const previewImage = (current, index) => {
  const urls = referralDetail.value.attachments.map(item => item.url)
  uni.previewImage({
    current,
    urls
  })
}

// 取消转诊申请
const cancelReferral = async () => {
  if (referralDetail.value.status !== '待审核') {
    uni.showToast({
      title: '只有待审核的申请可以取消',
      icon: 'none'
    })
    return
  }
  
  uni.showModal({
    title: '提示',
    content: '确定要取消该转诊申请吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          submitting.value = true
          await cancelReferralApplication(referralId.value)
          uni.showToast({
            title: '取消成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          uni.showToast({
            title: '取消失败',
            icon: 'none'
          })
        } finally {
          submitting.value = false
        }
      }
    }
  })
}

// 前往医院导航
const goToHospital = () => {
  console.log('导航到医院:', referralDetail.value.targetHospital)
  uni.showToast({
    title: '医院导航功能待实现',
    icon: 'none'
  })
}

// 创建新的转诊申请
const createNewReferral = () => {
  uni.navigateTo({
    url: '/subpkg/hospital/referral-application'
  })
}

// 加载转诊详情
const loadReferralDetail = async () => {
  try {
    loading.value = true
    const res = await getReferralDetail(referralId.value)
    
    if (res.code === 200 && res.data) {
      referralDetail.value = res.data
    }
  } catch (error) {
    console.error('加载转诊详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 页面加载时获取详情数据
onMounted(() => {
  loadReferralDetail()
})
</script>

<style scoped>
.referral-bg {
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  background-color: #1989fa;
  color: #fff;
  padding: 16px;
  display: flex;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-btn {
  font-size: 20px;
  margin-right: 20px;
}

.page-title {
  flex: 1;
  font-size: 18px;
  font-weight: bold;
  text-align: center;
}

.header-right {
  width: 20px;
}

.content {
  padding: 16px;
  height: calc(100vh - 52px);
}

/* 状态卡片样式 */
.status-card {
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  color: #fff;
}

.status-pending {
  background: linear-gradient(135deg, #ff9800, #f57c00);
}

.status-approved {
  background: linear-gradient(135deg, #4caf50, #388e3c);
}

.status-rejected {
  background: linear-gradient(135deg, #f44336, #d32f2f);
}

.status-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.status-icon {
  margin-right: 16px;
}

.icon-img {
  width: 40px;
  height: 40px;
}

.status-text {
  font-size: 20px;
  font-weight: bold;
  display: block;
  margin-bottom: 4px;
}

.status-desc {
  font-size: 14px;
  opacity: 0.9;
}

.hospital-info {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 12px;
  border-radius: 8px;
}

.hospital-name {
  font-size: 16px;
  font-weight: bold;
  display: block;
  margin-bottom: 4px;
}

.department {
  font-size: 14px;
}

/* 信息区域样式 */
.info-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
  position: relative;
  padding-left: 10px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background-color: #1989fa;
  border-radius: 2px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.description-item {
  margin-bottom: 16px;
}

.description-item:last-child {
  margin-bottom: 0;
}

.desc-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  display: block;
}

.desc-content {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  word-break: break-all;
}

.reject-reason {
  color: #f44336;
}

/* 附件样式 */
.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.attachment-item {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
}

.attachment-img {
  width: 100%;
  height: 100%;
}

/* 操作按钮样式 */
.action-section {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.primary-btn {
  flex: 1;
  height: 44px;
  background-color: #1989fa;
  color: #fff;
  border: none;
  border-radius: 22px;
  font-size: 16px;
}

.secondary-btn {
  flex: 1;
  height: 44px;
  background-color: #fff;
  color: #1989fa;
  border: 1px solid #1989fa;
  border-radius: 22px;
  font-size: 16px;
}

.cancel-btn {
  flex: 1;
  height: 44px;
  background-color: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 22px;
  font-size: 16px;
}

.waiting-tips {
  color: #ff9500;
  font-weight: 500;
}

.primary-btn:active {
  background-color: #0e77d0;
}

.secondary-btn:active {
  background-color: #f0f0f0;
}
</style>