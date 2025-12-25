<template>
  <view class="report-bg">
    <!-- 顶部搜索栏 -->
    <view class="search-bar">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          v-model="searchKeyword"
          placeholder="搜索体检报告（姓名/日期）"
          placeholder-class="search-placeholder"
          @input="onSearch"
        />
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-tabs">
      <view 
        v-for="tab in filterTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: currentFilter === tab.value }"
        @click="changeFilter(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 报告列表 -->
    <view class="reports-list" v-if="filteredReports.length > 0">
      <view 
        v-for="report in filteredReports" 
        :key="report.id"
        class="report-card"
        @click="viewReport(report)"
      >
        <view class="report-header">
          <view class="report-info">
            <text class="report-name">{{ report.name }}</text>
            <text class="report-date">{{ report.date }}</text>
          </view>
          <view class="report-status" :class="report.statusClass">
            {{ report.statusText }}
          </view>
        </view>
        <view class="report-detail">
          <view class="detail-item">
            <text class="detail-label">体检类型：</text>
            <text class="detail-value">{{ report.type }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">体检项目：</text>
            <text class="detail-value">{{ report.package }}</text>
          </view>
        </view>
        <view class="report-footer">
          <view class="footer-tag" v-if="report.hasAbnormal">
            <text class="tag-icon">⚠️</text>
            <text>有异常指标</text>
          </view>
          <view class="footer-tag normal" v-else>
            <text class="tag-icon">✅</text>
            <text>未见异常</text>
          </view>
          <view class="footer-actions">
            <text class="action-btn" @click.stop="downloadReport(report)">下载</text>
            <text class="action-divider">|</text>
            <text class="action-btn" @click.stop="shareReport(report)">分享</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <image class="empty-icon" :src="getStaticImage('/static/empty_message.png')" mode="aspectFit" />
      <text class="empty-text">暂无体检报告</text>
      <button class="empty-btn" @click="goToBooking">立即预约体检</button>
    </view>

    <!-- 底部提示 -->
    <view class="bottom-tips card" v-if="filteredReports.length > 0">
      <view class="tips-title">
        <text class="tips-icon">💡</text>
        <text>温馨提示</text>
      </view>
      <view class="tips-content">
        <text>• 体检报告一般在体检后3-5个工作日生成</text>
        <text>• 如有异常指标，建议及时咨询医生</text>
        <text>• 报告保存期为2年，请及时下载</text>
      </view>
    </view>

    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getStaticImage } from '@/utils/imageHelper'

// 搜索关键词
const searchKeyword = ref('')

// 筛选标签
const filterTabs = [
  { label: '全部', value: 'all' },
  { label: '个检', value: 'individual' },
  { label: '团检', value: 'group' },
  { label: '有异常', value: 'abnormal' }
]

const currentFilter = ref('all')

// 模拟报告数据
const reports = ref([
  {
    id: 1,
    name: '张三',
    date: '2025-10-15',
    type: '个人体检',
    package: '教职工套餐',
    statusText: '已出报告',
    statusClass: 'finished',
    hasAbnormal: true
  },
  {
    id: 2,
    name: '李四',
    date: '2025-09-20',
    type: '个人体检',
    package: '基础套餐',
    statusText: '已出报告',
    statusClass: 'finished',
    hasAbnormal: false
  },
  {
    id: 3,
    name: '王五',
    date: '2025-08-10',
    type: '团体体检',
    package: '全面套餐',
    statusText: '已出报告',
    statusClass: 'finished',
    hasAbnormal: true
  },
  {
    id: 4,
    name: '赵六',
    date: '2025-10-16',
    type: '个人体检',
    package: '基础套餐',
    statusText: '检查中',
    statusClass: 'processing',
    hasAbnormal: false
  }
])

// 筛选后的报告列表
const filteredReports = computed(() => {
  let result = reports.value

  // 按筛选条件过滤
  if (currentFilter.value === 'individual') {
    result = result.filter(r => r.type === '个人体检')
  } else if (currentFilter.value === 'group') {
    result = result.filter(r => r.type === '团体体检')
  } else if (currentFilter.value === 'abnormal') {
    result = result.filter(r => r.hasAbnormal)
  }

  // 按搜索关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(r => 
      r.name.toLowerCase().includes(keyword) ||
      r.date.includes(keyword)
    )
  }

  return result
})

// 搜索
const onSearch = () => {
  // 实际项目中可以添加防抖处理
}

// 切换筛选条件
const changeFilter = (value) => {
  currentFilter.value = value
}

// 查看报告详情
const viewReport = (report) => {
  if (report.statusText === '检查中') {
    uni.showToast({
      title: '报告生成中，请稍后查看',
      icon: 'none'
    })
    return
  }

  // 跳转到报告详情页
  uni.showModal({
    title: '体检报告详情',
    content: `姓名：${report.name}\n体检日期：${report.date}\n体检类型：${report.type}\n体检套餐：${report.package}\n\n报告状态：${report.hasAbnormal ? '检出异常指标' : '未见明显异常'}\n\n详细报告内容请在实际页面中展示。`,
    showCancel: false
  })
}

// 下载报告
const downloadReport = (report) => {
  if (report.statusText === '检查中') {
    uni.showToast({
      title: '报告生成中，暂不支持下载',
      icon: 'none'
    })
    return
  }

  uni.showLoading({ title: '准备下载...' })
  
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '报告已保存到相册',
      icon: 'success'
    })
  }, 1500)
}

// 分享报告
const shareReport = (report) => {
  if (report.statusText === '检查中') {
    uni.showToast({
      title: '报告生成中，暂不支持分享',
      icon: 'none'
    })
    return
  }

  uni.showActionSheet({
    itemList: ['分享给医生', '分享给好友', '生成分享码'],
    success: (res) => {
      const actions = ['分享给医生', '分享给好友', '生成分享码']
      uni.showToast({
        title: actions[res.tapIndex],
        icon: 'none'
      })
    }
  })
}

// 去预约
const goToBooking = () => {
  uni.navigateTo({
    url: '/subpkg/physical-exam/individual-booking'
  })
}
</script>

<style scoped>
.report-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.search-bar {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  padding: 32rpx 24rpx;
}

.search-box {
  background: #fff;
  border-radius: 40rpx;
  padding: 16rpx 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.search-icon {
  font-size: 32rpx;
  color: #999;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.search-placeholder {
  color: #ccc;
}

.filter-tabs {
  display: flex;
  padding: 24rpx 24rpx 0;
  gap: 16rpx;
  overflow-x: auto;
}

.filter-tab {
  padding: 16rpx 32rpx;
  background: #fff;
  border-radius: 40rpx;
  font-size: 26rpx;
  color: #666;
  white-space: nowrap;
  box-shadow: 0 2rpx 8rpx rgba(79, 172, 254, 0.1);
}

.filter-tab.active {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
  font-weight: bold;
}

.reports-list {
  padding: 24rpx;
}

.report-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(79, 172, 254, 0.1);
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24rpx;
  padding-bottom: 24rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.report-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.report-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.report-date {
  font-size: 24rpx;
  color: #999;
}

.report-status {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: bold;
}

.report-status.finished {
  background: #e8f5e9;
  color: #4caf50;
}

.report-status.processing {
  background: #fff3e0;
  color: #ff9800;
}

.report-detail {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.detail-item {
  display: flex;
  font-size: 26rpx;
}

.detail-label {
  color: #999;
  margin-right: 8rpx;
}

.detail-value {
  color: #333;
}

.report-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-tag {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #fff3e0;
  color: #ff9800;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.footer-tag.normal {
  background: #e8f5e9;
  color: #4caf50;
}

.tag-icon {
  font-size: 28rpx;
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-size: 26rpx;
  color: #4facfe;
}

.action-btn {
  font-weight: bold;
}

.action-divider {
  color: #ddd;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  width: 300rpx;
  height: 300rpx;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 48rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
  border-radius: 40rpx;
  padding: 20rpx 64rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
}

.card {
  background: #fff;
  border-radius: 20rpx;
  margin: 24rpx 24rpx 0;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(79, 172, 254, 0.08);
}

.bottom-tips {
  margin-top: 24rpx;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.tips-icon {
  font-size: 32rpx;
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  font-size: 24rpx;
  color: #666;
  line-height: 36rpx;
}

.tabbar-placeholder {
  height: 120rpx;
}
</style>

