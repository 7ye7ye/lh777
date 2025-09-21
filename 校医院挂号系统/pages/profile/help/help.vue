<template>
  <view class="page-bg">
    <!-- 头部导航栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="separator">|</text>
        <text class="home-btn" @click="goHome">🏠</text>
      </view>
      <text class="header-title">在线反馈</text>
      <view class="header-right">
        <text class="more-btn">⋯</text>
        <text class="separator">|</text>
        <text class="minimize-btn">−</text>
        <text class="separator">|</text>
        <text class="focus-btn">◎</text>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          placeholder="搜索关键字" 
          v-model="searchKeyword"
          @input="onSearch"
        />
        <button class="search-btn" @click="performSearch">搜索</button>
      </view>
    </view>

    <!-- 常见问题 -->
    <view class="common-issues-section">
      <view class="section-header">
        <text class="section-icon">?</text>
        <text class="section-title">常见问题</text>
      </view>
      
      <view class="issues-list">
        <view 
          class="issue-item" 
          v-for="issue in filteredIssues" 
          :key="issue.id"
          @click="goToIssueDetail(issue.id)"
        >
          <text class="issue-text">{{ issue.title }}</text>
          <text class="arrow-icon">›</text>
        </view>
      </view>
    </view>

    <!-- 问题反馈按钮 -->
    <view class="feedback-section">
      <button class="feedback-btn" @click="goToFeedback">问题反馈</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const searchKeyword = ref('')

const commonIssues = ref([
  { id: 1, title: '登录注册', category: 'account' },
  { id: 2, title: '绑卡', category: 'card' },
  { id: 3, title: '挂号问题', category: 'registration' },
  { id: 4, title: '门诊缴费', category: 'payment' },
  { id: 5, title: '住院', category: 'hospitalization' },
  { id: 6, title: '报告查询', category: 'report' },
  { id: 7, title: '其他', category: 'other' },
  { id: 8, title: '预约挂号', category: 'appointment' }
])

const filteredIssues = computed(() => {
  if (!searchKeyword.value.trim()) {
    return commonIssues.value
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  return commonIssues.value.filter(issue => 
    issue.title.toLowerCase().includes(keyword)
  )
})

const goBack = () => {
  uni.navigateBack()
}

const goHome = () => {
  uni.reLaunch({ url: '/pages/home/home' })
}

const onSearch = () => {
  // 搜索逻辑已在computed中处理
}

const performSearch = () => {
  if (!searchKeyword.value.trim()) {
    uni.showToast({ title: '请输入搜索关键字', icon: 'none' })
    return
  }
  
  uni.showToast({ title: `搜索"${searchKeyword.value}"`, icon: 'none' })
}

const goToIssueDetail = (issueId) => {
  const issue = commonIssues.value.find(item => item.id === issueId)
  
  // 根据问题类型跳转到对应的帮助页面
  const routeMap = {
    1: '/pages/profile/help/login',      // 登录注册
    2: '/pages/profile/help/bindcard',   // 绑卡
    3: '/pages/profile/help/register',   // 挂号问题
    4: '/pages/profile/help/payment',    // 门诊缴费
    5: '/pages/profile/help/hospitalization', // 住院
    6: '/pages/profile/help/report',      // 报告查询
    7: '/pages/profile/help/other',      // 其他
    8: '/pages/profile/help/appointment' // 预约挂号
  }
  
  const route = routeMap[issueId]
  if (route) {
    uni.navigateTo({ url: route })
  } else {
    uni.showModal({
      title: issue.title,
      content: '这是关于' + issue.title + '的详细解答...',
      showCancel: false
    })
  }
}

const goToFeedback = () => {
  uni.navigateTo({ url: '/pages/profile/settings/complain' })
}

onMounted(() => {
  // 初始化数据
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

/* 搜索栏 */
.search-section {
  padding: 24rpx;
  background: #fff;
}

.search-bar {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
}

.search-icon {
  font-size: 28rpx;
  margin-right: 16rpx;
  color: #999;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  background: transparent;
  border: none;
}

.search-btn {
  background: #3a9cff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 12rpx 24rpx;
  font-size: 26rpx;
  margin-left: 16rpx;
}

/* 常见问题 */
.common-issues-section {
  background: #fff;
  margin: 24rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.section-header {
  display: flex;
  align-items: center;
  padding: 24rpx 24rpx 16rpx 24rpx;
  background: #f8f9fa;
}

.section-icon {
  width: 40rpx;
  height: 40rpx;
  background: #3a9cff;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  margin-right: 12rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.issues-list {
  padding: 0 24rpx;
}

.issue-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1px solid #f0f0f0;
}

.issue-item:last-child {
  border-bottom: none;
}

.issue-item:active {
  background: #f8f9fa;
}

.issue-text {
  font-size: 28rpx;
  color: #333;
}

.arrow-icon {
  font-size: 24rpx;
  color: #999;
}

/* 问题反馈按钮 */
.feedback-section {
  padding: 24rpx;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}

.feedback-btn {
  width: 100%;
  background: #3a9cff;
  color: #fff;
  border: none;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 32rpx;
  font-weight: bold;
}
</style>