<template>
  <view class="referral-bg">
    <view class="page-header">
      <text class="page-title">转诊记录</text>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view 
        v-for="(item, index) in filterOptions" 
        :key="index"
        class="filter-item"
        :class="{ active: currentFilter === index }"
        @click="changeFilter(index)"
      >
        {{ item }}
      </view>
    </view>

    <!-- 记录列表 -->
      <scroll-view 
        scroll-y 
        class="records-list"
        @refresh="onRefresh"
        @scrolltolower="onLoadMore"
        refresher-enabled
        :refresher-triggered="loading"
      >
      <view 
        v-for="(record, index) in filteredRecords" 
        :key="index"
        class="record-item"
        @click="viewRecordDetail(record)"
      >
        <view class="record-header">
          <view class="record-title">
            <text class="hospital-name">{{ record.targetHospital }}</text>
            <view class="status-badge" :class="getStatusClass(record.status)">
              {{ record.status }}
            </view>
          </view>
          <text class="apply-time">申请时间：{{ record.applyTime }}</text>
        </view>
        
        <view class="record-content">
          <view class="info-item">
            <span class="info-label">患者姓名：</span>
            <span class="info-value">{{ record.patientName }}</span>
          </view>
          <view class="info-item">
            <span class="info-label">目标科室：</span>
            <span class="info-value">{{ record.targetDepartment }}</span>
          </view>
          <view class="info-item">
            <span class="info-label">症状：</span>
            <span class="info-value symptoms">{{ truncateText(record.symptoms, 30) }}</span>
          </view>
        </view>
        
        <view class="record-footer">
          <view v-if="record.status === '已审核'">
            <text class="review-time">审核时间：{{ record.reviewTime }}</text>
            <button class="action-btn" @click.stop="goToHospital(record)">前往医院</button>
          </view>
          <view v-else-if="record.status === '已拒绝'">
            <text class="reject-reason">拒绝原因：{{ record.rejectReason || '未说明' }}</text>
          </view>
          <view v-else-if="record.status === '待审核'">
            <text class="waiting-tips">请等待医生审核...</text>
            <button class="cancel-btn" @click.stop="cancelReferral(record)">取消申请</button>
          </view>
          <view class="arrow"></view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="filteredRecords.length === 0" class="empty-state">
        <image src="/static/empty_message.png" mode="widthFix" class="empty-img"></image>
        <text class="empty-text">暂无转诊记录</text>
        <button class="create-btn" @click="createNewReferral">发起转诊申请</button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 筛选选项
const filterOptions = ['全部', '待审核', '已审核', '已拒绝']
const currentFilter = ref(0)

// 转诊记录数据
const referralRecords = ref([])

// 根据筛选条件过滤记录
const filteredRecords = computed(() => {
  if (currentFilter.value === 0) {
    return referralRecords.value
  }
  return referralRecords.value.filter(record => {
    return record.status === filterOptions[currentFilter.value]
  })
})

// 切换筛选
const changeFilter = (index) => {
  currentFilter.value = index
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

// 截断文本
const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

// 查看记录详情
const viewRecordDetail = (record) => {
  // 导航到详情页面
  console.log('查看转诊详情:', record)
  uni.navigateTo({
    url: `/subpkg/hospital/referral-detail?id=${record.id}`
  })
}

// 前往医院
const goToHospital = (record) => {
  console.log('前往医院:', record.targetHospital)
  uni.showToast({
    title: '前往医院导航功能待实现',
    icon: 'none'
  })
}

// 创建新转诊申请
const createNewReferral = () => {
  uni.navigateTo({
    url: '/subpkg/hospital/referral-application'
  })
}

// 加载模拟数据
const loadMockData = () => {
  referralRecords.value = [
    {
      id: '1',
      patientName: '张三',
      targetHospital: '北京协和医院',
      targetDepartment: '心内科',
      symptoms: '胸闷气短，活动后加重，伴有咳嗽',
      applyTime: '2024-01-15 09:30',
      status: '已审核',
      reviewTime: '2024-01-15 11:20'
    },
    {
      id: '2', 
      patientName: '李四',
      targetHospital: '北京大学第一医院',
      targetDepartment: '骨科',
      symptoms: '膝关节疼痛，行走困难，上下楼梯加剧',
      applyTime: '2024-01-14 14:20',
      status: '已拒绝',
      rejectReason: '建议先在本院进行保守治疗观察'
    },
    {
      id: '3',
      patientName: '王五',
      targetHospital: '北京301医院',
      targetDepartment: '神经内科',
      symptoms: '头痛反复发作，伴有恶心呕吐，最近加重',
      applyTime: '2024-01-15 16:45',
      status: '待审核'
    },
    {
      id: '4',
      patientName: '赵六',
      targetHospital: '北京同仁医院',
      targetDepartment: '眼科',
      symptoms: '视力下降，眼睛干涩，有异物感',
      applyTime: '2024-01-13 10:15',
      status: '已审核',
      reviewTime: '2024-01-13 14:30'
    }
  ]
}

// 页面加载时初始化数据
onMounted(() => {
  loadMockData()
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
  text-align: center;
  position: sticky;
  top: 0;
  z-index: 10;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.filter-bar {
  display: flex;
  background-color: #fff;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 52px;
  z-index: 5;
}

.filter-item {
  flex: 1;
  padding: 14px 0;
  text-align: center;
  font-size: 14px;
  color: #666;
  position: relative;
}

.filter-item.active {
  color: #1989fa;
}

.filter-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background-color: #1989fa;
  border-radius: 1.5px;
}

.records-list {
  padding: 12px;
  height: calc(100vh - 110px);
}

.record-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.record-header {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.record-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.hospital-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  color: #fff;
}

.status-pending {
  background-color: #ff9800;
}

.status-approved {
  background-color: #4caf50;
}

.status-rejected {
  background-color: #f44336;
}

.apply-time {
  font-size: 12px;
  color: #999;
}

.record-content {
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-label {
  color: #666;
  width: 70px;
}

.info-value {
  color: #333;
  flex: 1;
}

.symptoms {
  word-break: break-all;
}

.record-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.review-time,
.reject-reason,
.waiting-tips {
  font-size: 12px;
  color: #999;
}

.action-btn {
  padding: 4px 12px;
  background-color: #1989fa;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 12px;
}

.arrow {
  color: #ccc;
  font-size: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-img {
  width: 120px;
  height: 120px;
  margin-bottom: 16px;
}

.empty-text {
  color: #999;
  font-size: 14px;
  margin-bottom: 20px;
}

.create-btn {
  padding: 8px 24px;
  background-color: #1989fa;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
}
</style>