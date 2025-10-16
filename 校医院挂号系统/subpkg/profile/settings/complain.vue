<template>
  <view class="page-bg">
    <!-- 头部导航栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="separator">|</text>
        <text class="home-btn" @click="goHome">🏠</text>
      </view>
      <text class="header-title">投诉反馈</text>
      <view class="header-right">
        <text class="more-btn">⋯</text>
        <text class="separator">|</text>
        <text class="minimize-btn">−</text>
        <text class="separator">|</text>
        <text class="focus-btn">◎</text>
      </view>
    </view>

    <!-- 导航标签 -->
    <view class="nav-tabs">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'feedback' }"
        @click="switchTab('feedback')"
      >
        <text class="tab-text">问题反馈</text>
        <view class="tab-underline" v-if="activeTab === 'feedback'"></view>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'myfeedback' }"
        @click="switchTab('myfeedback')"
      >
        <text class="tab-text">我的反馈</text>
        <view class="tab-underline" v-if="activeTab === 'myfeedback'"></view>
      </view>
    </view>

    <!-- 问题反馈内容 -->
    <view class="feedback-content" v-if="activeTab === 'feedback'">
      <!-- 问题分类 -->
      <view class="form-section">
        <view class="section-header">
          <view class="section-bar"></view>
          <text class="section-title">问题分类</text>
        </view>
        <view class="category-buttons">
          <button 
            v-for="category in problemCategories" 
            :key="category.value"
            class="category-btn"
            :class="{ active: selectedCategory === category.value }"
            @click="selectCategory(category.value)"
          >
            {{ category.label }}
          </button>
        </view>
      </view>

      <!-- 业务类型 -->
      <view class="form-section">
        <view class="section-header">
          <view class="section-bar"></view>
          <text class="section-title">业务类型</text>
        </view>
        <view class="business-buttons">
          <button 
            v-for="business in businessTypes" 
            :key="business.value"
            class="business-btn"
            :class="{ active: selectedBusiness === business.value }"
            @click="selectBusiness(business.value)"
          >
            {{ business.label }}
          </button>
        </view>
      </view>

      <!-- 反馈内容 -->
      <view class="form-section">
        <view class="section-header">
          <view class="section-bar"></view>
          <text class="section-title">反馈内容</text>
        </view>
        <view class="content-input-wrapper">
          <textarea 
            class="content-input"
            :placeholder="getPlaceholder()"
            v-model="feedbackContent"
            maxlength="200"
            @input="onContentInput"
          />
          <text class="char-count">{{ feedbackContent.length }}/200</text>
        </view>
      </view>

      <!-- 反馈人 -->
      <view class="form-section">
        <view class="info-row">
          <text class="info-label">反馈人</text>
          <text class="info-value">{{ userInfo.name || '周诗晴' }}</text>
        </view>
      </view>

      <!-- 反馈院区 -->
      <view class="form-section">
        <view class="info-row" @click="selectCampus">
          <text class="info-label">反馈院区</text>
          <view class="info-value-wrapper">
            <text class="info-value">{{ selectedCampus || '请选择院区' }}</text>
            <text class="arrow-icon">›</text>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button 
          class="submit-btn" 
          :class="{ disabled: !canSubmit }"
          @click="submitFeedback"
          :disabled="!canSubmit"
        >
          提交反馈
        </button>
      </view>
    </view>

    <!-- 我的反馈内容 -->
    <view class="my-feedback-content" v-if="activeTab === 'myfeedback'">
      <view class="feedback-list">
        <view 
          class="feedback-item" 
          v-for="item in myFeedbacks" 
          :key="item.id"
        >
          <view class="feedback-header">
            <text class="feedback-category">{{ item.category }}</text>
            <text class="feedback-status" :class="item.status">{{ item.statusText }}</text>
          </view>
          <view class="feedback-content-text">{{ item.content }}</view>
          <view class="feedback-footer">
            <text class="feedback-time">{{ item.time }}</text>
            <text class="feedback-business">{{ item.business }}</text>
          </view>
        </view>
      </view>
      
      <view class="empty-state" v-if="myFeedbacks.length === 0">
        <text class="empty-text">暂无反馈记录</text>
      </view>
    </view>

    <!-- 院区选择弹窗 -->
    <view class="campus-modal" v-if="showCampusModal">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">选择院区</text>
          <text class="close-btn" @click="showCampusModal = false">×</text>
        </view>
        <view class="campus-list">
          <view 
            class="campus-item" 
            v-for="campus in campuses" 
            :key="campus.value"
            @click="selectCampusItem(campus.value)"
          >
            <text class="campus-name">{{ campus.label }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { userApi } from '@/api/user'

const activeTab = ref('feedback')
const selectedCategory = ref('投诉')
const selectedBusiness = ref('门诊')
const feedbackContent = ref('')
const selectedCampus = ref('')
const showCampusModal = ref(false)
const userInfo = ref({})

const problemCategories = [
  { label: '举报', value: '举报' },
  { label: '投诉', value: '投诉' },
  { label: '建议', value: '建议' },
  { label: '反馈', value: '反馈' },
  { label: '咨询', value: '咨询' }
]

const businessTypes = [
  { label: '门诊', value: '门诊' },
  { label: '住院', value: '住院' },
  { label: '收费', value: '收费' },
  { label: '检查', value: '检查' },
  { label: '检验', value: '检验' },
  { label: '药房', value: '药房' },
  { label: '问诊', value: '问诊' },
  { label: '其他', value: '其他' }
]

const campuses = [
  { label: '总院区', value: '总院区' },
  { label: '东院区', value: '东院区' },
  { label: '西院区', value: '西院区' },
  { label: '南院区', value: '南院区' },
  { label: '北院区', value: '北院区' }
]

const myFeedbacks = ref([
  {
    id: 1,
    category: '投诉',
    business: '门诊',
    content: '挂号排队时间过长，希望能优化流程',
    time: '2025-09-20 14:30',
    status: 'processing',
    statusText: '处理中'
  },
  {
    id: 2,
    category: '建议',
    business: '收费',
    content: '建议增加移动支付方式',
    time: '2025-09-19 10:15',
    status: 'completed',
    statusText: '已处理'
  }
])

const canSubmit = computed(() => {
  return selectedCategory.value && 
         selectedBusiness.value && 
         feedbackContent.value.trim().length > 0 &&
         selectedCampus.value
})

const getPlaceholder = () => {
  const placeholders = {
    '举报': '请填写您的举报内容',
    '投诉': '请填写您的投诉内容',
    '建议': '请填写您的建议内容',
    '反馈': '请填写您的反馈内容',
    '咨询': '请填写您的咨询内容'
  }
  return placeholders[selectedCategory.value] || '请填写您的反馈内容'
}

const goBack = () => {
  uni.navigateBack()
}

const goHome = () => {
  uni.reLaunch({ url: '/pages/home/home' })
}

const switchTab = (tab) => {
  activeTab.value = tab
}

const selectCategory = (category) => {
  selectedCategory.value = category
}

const selectBusiness = (business) => {
  selectedBusiness.value = business
}

const onContentInput = () => {
  // 内容输入处理
}

const selectCampus = () => {
  showCampusModal.value = true
}

const selectCampusItem = (campus) => {
  selectedCampus.value = campus
  showCampusModal.value = false
}

const submitFeedback = () => {
  if (!canSubmit.value) {
    uni.showToast({ title: '请完善所有必填信息', icon: 'error' })
    return
  }

  const feedbackData = {
    category: selectedCategory.value,
    business: selectedBusiness.value,
    content: feedbackContent.value,
    campus: selectedCampus.value,
    user: userInfo.value.name || '周诗晴'
  }

  userApi.submitComplain(feedbackData).then(() => {
    uni.showToast({ title: '反馈提交成功', icon: 'success' })
    
    // 重置表单
    feedbackContent.value = ''
    selectedCampus.value = ''
    
    // 切换到我的反馈查看
    setTimeout(() => {
      activeTab.value = 'myfeedback'
    }, 1500)
  }).catch(() => {
    uni.showToast({ title: '提交失败，请重试', icon: 'error' })
  })
}

const getUserInfo = () => {
  userApi.getCurrentUser().then(res => {
    userInfo.value = res.data || {}
  }).catch(() => {
    userInfo.value = { name: '周诗晴' }
  })
}

onMounted(() => {
  getUserInfo()
})
</script>

<style scoped>
.page-bg {
  min-height: 100vh;
  background: #f8faff;
}

/* 头部导航栏 */
.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #3a9cff;
  padding: 20rpx 24rpx;
  color: #fff;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
}

.back-btn, .home-btn, .more-btn, .minimize-btn, .focus-btn {
  font-size: 32rpx;
  padding: 8rpx;
}

.separator {
  font-size: 24rpx;
  margin: 0 8rpx;
  opacity: 0.7;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
}

/* 导航标签 */
.nav-tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  position: relative;
}

.tab-text {
  font-size: 28rpx;
  color: #666;
}

.tab-item.active .tab-text {
  color: #3a9cff;
  font-weight: bold;
}

.tab-underline {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: #3a9cff;
  border-radius: 2rpx;
}

/* 表单内容 */
.feedback-content {
  padding: 24rpx;
}

.form-section {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-bar {
  width: 6rpx;
  height: 32rpx;
  background: #3a9cff;
  border-radius: 3rpx;
  margin-right: 12rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

/* 问题分类按钮 */
.category-buttons {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
}

.category-btn {
  background: #fff;
  color: #666;
  border: 1px solid #e9ecef;
  border-radius: 8rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
}

.category-btn.active {
  background: #3a9cff;
  color: #fff;
  border-color: #3a9cff;
}

/* 业务类型按钮 */
.business-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.business-btn {
  background: #fff;
  color: #666;
  border: 1px solid #e9ecef;
  border-radius: 8rpx;
  padding: 16rpx 12rpx;
  font-size: 24rpx;
  text-align: center;
}

.business-btn.active {
  background: #3a9cff;
  color: #fff;
  border-color: #3a9cff;
}

/* 反馈内容输入 */
.content-input-wrapper {
  position: relative;
}

.content-input {
  width: 100%;
  min-height: 200rpx;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 26rpx;
  color: #333;
}

.char-count {
  position: absolute;
  bottom: 12rpx;
  right: 16rpx;
  font-size: 22rpx;
  color: #999;
}

/* 信息行 */
.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;
}

.info-label {
  font-size: 28rpx;
  color: #333;
}

.info-value {
  font-size: 28rpx;
  color: #666;
}

.info-value-wrapper {
  display: flex;
  align-items: center;
}

.arrow-icon {
  font-size: 24rpx;
  color: #999;
  margin-left: 8rpx;
}

/* 提交按钮 */
.submit-section {
  padding: 24rpx 0 40rpx 0;
}

.submit-btn {
  width: 100%;
  background: #3a9cff;
  color: #fff;
  border: none;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 32rpx;
  font-weight: bold;
}

.submit-btn.disabled {
  background: #a4caff;
}

/* 我的反馈 */
.my-feedback-content {
  padding: 24rpx;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.feedback-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.feedback-category {
  font-size: 24rpx;
  color: #3a9cff;
  background: #e6f2ff;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
}

.feedback-status {
  font-size: 24rpx;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
}

.feedback-status.processing {
  background: #fff2e8;
  color: #fa8c16;
}

.feedback-status.completed {
  background: #f6ffed;
  color: #52c41a;
}

.feedback-content-text {
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
  margin-bottom: 12rpx;
}

.feedback-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.feedback-time {
  font-size: 22rpx;
  color: #999;
}

.feedback-business {
  font-size: 22rpx;
  color: #666;
  background: #f0f0f0;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.empty-state {
  text-align: center;
  padding: 80rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 院区选择弹窗 */
.campus-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 16rpx;
  margin: 32rpx;
  max-height: 60vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
}

.close-btn {
  font-size: 40rpx;
  color: #999;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.campus-list {
  padding: 16rpx 0;
}

.campus-item {
  padding: 20rpx 32rpx;
  border-bottom: 1px solid #f0f0f0;
}

.campus-item:last-child {
  border-bottom: none;
}

.campus-name {
  font-size: 28rpx;
  color: #333;
}
</style>