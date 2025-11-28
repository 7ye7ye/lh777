<template>
  <view class="referral-bg">
    <view class="page-header">
      <view class="back-btn" @click="goBack">←</view>
      <text class="page-title">转诊记录</text>
      <view class="header-right">
        <text class="refresh-btn" :class="{ disabled: loading }" @click="refreshRecords">刷新</text>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view
        v-for="(item, index) in filterTabs"
        :key="item.value || index"
        class="filter-item"
        :class="{ active: currentFilter === index }"
        @click="changeFilter(index)"
      >
        {{ item.label }}
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
        v-for="group in groupedRecords"
        :key="group.patientKey"
        class="patient-group"
      >
        <view class="patient-header">
          <text class="patient-name">{{ group.patientName }}</text>
          <text class="record-count">共 {{ group.records.length }} 条转诊记录</text>
        </view>

        <view
          v-for="(record, index) in group.records"
          :key="record.id || index"
          class="record-item"
          @click="viewRecordDetail(record)"
        >
          <view class="record-header">
            <view class="record-title">
              <text class="hospital-name">{{ record.targetHospital }}</text>
              <text v-if="record.targetDepartment" class="department-name"> · {{ record.targetDepartment }}</text>
            </view>
            <view class="badge-group">
              <view v-if="record.sourceType === 'DOCTOR_DIRECT'" class="source-badge">医生发起</view>
              <view class="status-badge" :class="getStatusClass(record.status)">
                {{ record.statusLabel }}
              </view>
            </view>
          </view>
          <text class="apply-time">申请时间：{{ record.applyTimeText }}</text>

          <view class="record-content">
            <view class="info-item">
              <span class="info-label">患者姓名：</span>
              <span class="info-value">{{ record.patientName }}</span>
            </view>
            <view class="info-item">
              <span class="info-label">症状：</span>
              <span class="info-value symptoms">{{ truncateText(record.symptoms, 30) }}</span>
            </view>
          </view>

          <view class="record-footer">
            <view class="footer-info">
              <text v-if="record.status === 'APPROVED' && record.reviewTimeText" class="review-time">
                审核时间：{{ record.reviewTimeText }}
              </text>
              <text v-else-if="record.status === 'REJECTED'" class="reject-reason">
                驳回原因：{{ record.rejectReason || '未说明' }}
              </text>
              <text v-else-if="record.status === 'PENDING'" class="waiting-tips">等待管理员审核</text>
              <text v-else-if="record.status === 'CANCELLED'">
                取消时间：{{ record.cancelTimeText || record.reviewTimeText || '--' }}
              </text>
            </view>
            <view class="footer-actions">
              <button
                v-if="record.status === 'APPROVED'"
                class="action-btn"
                @click.stop="goToHospital(record)"
              >
                前往医院
              </button>
              <button
                v-else-if="record.status === 'PENDING'"
                class="cancel-btn"
                @click.stop="cancelReferral(record)"
              >
                取消申请
              </button>
            </view>
            <view class="arrow"></view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && groupedRecords.length === 0" class="empty-state">
        <image src="/static/empty_message.png" mode="widthFix" class="empty-img" />
        <text class="empty-text">暂无转诊记录</text>
        <button class="create-btn" @click="createNewReferral">发起转诊申请</button>
      </view>

      <view v-if="loading && groupedRecords.length === 0" class="loading-state">
        <text class="loading-text">正在加载...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPatientReferralList, cancelPatientReferral } from '@/api/referral'

// 筛选选项
const filterTabs = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已审核', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已取消', value: 'CANCELLED' }
]
const currentFilter = ref(0)

// 转诊记录数据
const referralRecords = ref([])

// 根据筛选条件过滤记录
const filterStatus = ref(filterTabs[0].value || '')

const filteredRecords = computed(() => {
  if (!filterStatus.value) {
    return referralRecords.value
  }
  return referralRecords.value.filter(record => record.status === filterStatus.value)
})

const groupedRecords = computed(() => {
  const map = new Map()
  filteredRecords.value.forEach(record => {
    const key = record.patientId || record.patientName || 'default'
    if (!map.has(key)) {
      map.set(key, {
        patientKey: key,
        patientName: record.patientName || '未命名就诊人',
        records: []
      })
    }
    map.get(key).records.push(record)
  })
  return Array.from(map.values())
})

// 切换筛选
const changeFilter = (index) => {
  if (currentFilter.value === index && filterStatus.value === filterTabs[index].value) {
    return
  }
  currentFilter.value = index
  filterStatus.value = filterTabs[index].value || ''
  currentPage.value = 1
  referralRecords.value = []
  hasMore.value = true
  loadReferralRecords()
}

// 获取状态对应的样式类
const statusTextMap = {
  PENDING: '待审核',
  APPROVED: '已审核',
  REJECTED: '已拒绝',
  CANCELLED: '已取消'
}

const getStatusClass = (status) => {
  switch (status) {
    case 'PENDING':
      return 'status-pending'
    case 'APPROVED':
      return 'status-approved'
    case 'REJECTED':
      return 'status-rejected'
    case 'CANCELLED':
      return 'status-cancelled'
    default:
      return 'status-pending'
  }
}

const resolveStatusLabel = (status) => {
  return statusTextMap[status] || '待审核'
}

const formatDateTime = (value) => {
  if (!value) return ''
  if (typeof value === 'string') {
    return value.replace('T', ' ').slice(0, 19)
  }
  try {
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return ''
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    const hh = String(date.getHours()).padStart(2, '0')
    const mm = String(date.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${d} ${hh}:${mm}`
  } catch (e) {
    return ''
  }
}

// 截断文本
const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const goBack = () => {
  if (getCurrentPages().length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({
      url: '/pages/home/home'
    })
  }
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
  console.log('Attempting to navigate to: /subpkg/hospital/referral-application')
  uni.navigateTo({
    url: '/subpkg/hospital/referral-application',
    success: (res) => {
      console.log('Navigation successful:', res)
    },
    fail: (err) => {
      console.error('Navigation failed:', err)
      uni.showToast({
        title: '跳转失败: ' + JSON.stringify(err),
        icon: 'none',
        duration: 3000
      })
    }
  })
}

// 分页相关状态
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

// 加载转诊记录数据
const loadReferralRecords = async () => {
  if (loading.value || (!hasMore.value && currentPage.value !== 1)) return

  loading.value = true
  try {
    const params = {
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      status: filterStatus.value || undefined
    }

    const res = await getPatientReferralList(params)
    const rawRecords = Array.isArray(res?.records) ? res.records : []
    const normalizedRecords = rawRecords.map(record => {
      const status = (record.status || '').toUpperCase()
      const applyTime = record.applyTime || record.createTime || ''
      const reviewTime = record.reviewTime || ''
      const cancelTime = record.cancelTime || ''
      return {
        id: record.id || record.referralId || '',
        patientId: record.patientId || record.patient_id || '',
        patientName: record.patientName || '',
        targetHospital: record.targetHospitalName || record.targetHospital || '校医院',
        targetDepartment: record.targetDeptName || record.targetDepartment || '',
        symptoms: record.symptoms || record.reason || '',
        applyTime,
        applyTimeText: formatDateTime(applyTime),
        status,
        statusLabel: resolveStatusLabel(status),
        reviewTime,
        reviewTimeText: formatDateTime(reviewTime),
        cancelTime,
        cancelTimeText: formatDateTime(cancelTime),
        rejectReason: record.rejectReason || '',
        sourceType: record.sourceType || ''
      }
    })

    if (currentPage.value === 1) {
      referralRecords.value = normalizedRecords
    } else {
      referralRecords.value = [...referralRecords.value, ...normalizedRecords]
    }

    const total = res?.total || 0
    hasMore.value = currentPage.value * pageSize.value < total
  } catch (error) {
    console.error('加载转诊记录失败:', error)
    uni.showToast({
      title: '加载失败，请稍后重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const cancelReferral = (record) => {
  if (!record?.id) return
  uni.showModal({
    title: '取消转诊',
    content: '确定要取消该转诊申请吗？',
    confirmText: '确认取消',
    cancelText: '保留申请',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await cancelPatientReferral({
          id: record.id,
          reason: '患者主动取消'
        })
        uni.showToast({
          title: '申请已取消',
          icon: 'success'
        })
        onRefresh()
      } catch (error) {
        console.error('取消转诊失败:', error)
        uni.showToast({
          title: '取消失败，请稍后重试',
          icon: 'none'
        })
      }
    }
  })
}

// 刷新数据
const onRefresh = () => {
  currentPage.value = 1
  referralRecords.value = []
  hasMore.value = true
  loadReferralRecords()
}

const refreshRecords = () => {
  if (loading.value) return
  onRefresh()
}

// 加载更多
const onLoadMore = () => {
  if (!loading.value && hasMore.value) {
    currentPage.value++
    loadReferralRecords()
  }
}

// 页面加载时初始化数据
onMounted(() => {
  loadReferralRecords()
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
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-btn {
  font-size: 20px;
  font-weight: bold;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.header-right {
  min-width: 60px;
  text-align: right;
}

.refresh-btn {
  font-size: 14px;
  color: #fff;
}

.refresh-btn.disabled {
  opacity: 0.5;
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

.patient-group {
  margin-bottom: 16px;
}

.patient-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px 8px 4px;
  color: #555;
}

.patient-name {
  font-size: 16px;
  font-weight: bold;
  color: #1a1a1a;
}

.record-count {
  font-size: 12px;
  color: #999;
}

.record-header {
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.record-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.hospital-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.department-name {
  font-size: 14px;
  color: #666;
}

.badge-group {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-badge {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: #fff;
  white-space: nowrap;
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

.status-cancelled {
  background-color: #9e9e9e;
}

.source-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  background-color: #fff;
  color: #1989fa;
}

.apply-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
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
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  gap: 10px;
}

.footer-info {
  flex: 1;
  font-size: 12px;
  color: #666;
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.review-time,
.reject-reason,
.waiting-tips {
  font-size: 12px;
  color: #999;
}

.action-btn {
  padding: 6px 16px;
  background-color: #1989fa;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
}

.cancel-btn {
  padding: 6px 16px;
  background-color: transparent;
  color: #f44336;
  border: 1px solid #f44336;
  border-radius: 20px;
  font-size: 14px;
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

.loading-state {
  text-align: center;
  padding: 24px 0;
  color: #999;
}

.loading-text {
  font-size: 14px;
}
</style>