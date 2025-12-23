<template>
  <view class="page-bg">
    <!-- 标题和刷新按钮区域 -->
    <view class="header-section">
      <view class="list-header-wrapper">
        <view class="header-icon">🔄</view>
        <view class="list-header">转诊记录</view>
        <view class="header-badge" v-if="filteredRecords.length > 0">{{ filteredRecords.length }}</view>
      </view>
      <view class="header-actions">
        <button class="refresh-btn small" @click="refreshRecords" :disabled="loading">
          <text class="refresh-icon">⟳</text>
          <text>刷新</text>
        </button>
      </view>
    </view>

    <!-- 顶部就诊人信息卡片 -->
    <view class="patient-card">
      <view class="patient-info-left">
        <view class="patient-avatar">{{ (currentPatientInfo.name || '访').charAt(0) }}</view>
        <view class="patient-text">
          <view class="patient-name">{{ currentPatientInfo.name || '未填写姓名' }}</view>
          <view class="patient-visit-no" v-if="currentPatientInfo.visitNo">门诊号 {{ currentPatientInfo.visitNo }}</view>
        </view>
      </view>
      <view class="patient-info-right">
        <button class="patient-switch-btn" @click.stop="openPatientSelect">
          <text class="btn-icon">⇄</text>
          <text class="btn-text">切换就诊人</text>
        </button>
      </view>
    </view>

    <!-- 分类目录 -->
    <view class="filter-tabs">
      <view 
        v-for="(tab, index) in filterTabs" 
        :key="tab.value || index"
        class="filter-tab"
        :class="{ active: currentFilter === index }"
        @click="changeFilter(index)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 日期范围筛选 - 紧凑形式 -->
    <view class="date-filter-compact">
      <view class="date-filter-btn" @click="showDatePicker = true">
        <text class="calendar-icon">📅</text>
        <text class="date-range-text" v-if="startDate && endDate">
          {{ formatDateForDisplay(startDate) }} 至 {{ formatDateForDisplay(endDate) }}
        </text>
        <text class="date-range-text placeholder" v-else>选择日期范围</text>
      </view>
      <view v-if="startDate || endDate" class="clear-date-btn" @click.stop="clearDateRange">
        <text class="clear-date-icon">✕</text>
        <text class="clear-date-text">清除</text>
      </view>
    </view>
    
    <!-- 日历选择弹窗 -->
    <view class="date-picker-modal" v-if="showDatePicker" @click="showDatePicker = false">
      <view class="date-picker-content" @click.stop>
        <view class="date-picker-header">
          <text class="date-picker-title">选择日期范围</text>
          <text class="date-picker-close" @click="showDatePicker = false">✕</text>
        </view>
        <view class="date-picker-body">
          <view class="date-picker-row">
            <text class="date-picker-label">开始日期：</text>
            <picker mode="date" :value="startDate" @change="onStartDateChange" :end="endDate || undefined">
              <view class="date-picker-view">
                <text :class="startDate ? 'date-picker-value' : 'date-picker-placeholder'">
                  {{ startDate ? formatDateForDisplay(startDate) : '选择开始日期' }}
                </text>
              </view>
            </picker>
          </view>
          <view class="date-picker-row">
            <text class="date-picker-label">结束日期：</text>
            <picker mode="date" :value="endDate" @change="onEndDateChange" :start="startDate || undefined">
              <view class="date-picker-view">
                <text :class="endDate ? 'date-picker-value' : 'date-picker-placeholder'">
                  {{ endDate ? formatDateForDisplay(endDate) : '选择结束日期' }}
                </text>
              </view>
            </picker>
          </view>
        </view>
        <view class="date-picker-footer">
          <button 
            v-if="startDate || endDate" 
            class="date-picker-reset" 
            @click="handleResetDateRange"
          >
            重置
          </button>
          <button class="date-picker-confirm" @click="showDatePicker = false">确定</button>
        </view>
      </view>
    </view>

    <!-- 记录列表 -->
    <view class="record-container">
      <view
        v-for="(record, index) in filteredRecords"
        :key="record.id || index"
        class="record-card"
        @click="viewRecordDetail(record)"
      >
          <view class="card-header">
            <text class="operation-time">{{ record.applyTimeText }}</text>
            <view class="badge-group">
              <view v-if="record.sourceType === 'DOCTOR_DIRECT'" class="source-badge">医生发起</view>
              <view class="status-badge" :class="getStatusClass(record.status)">
                {{ record.statusLabel }}
              </view>
            </view>
          </view>
          <view class="card-title">
            <text class="hospital-name">{{ record.targetHospital }}</text>
            <text v-if="record.targetDepartment" class="department-name"> · {{ record.targetDepartment }}</text>
          </view>
          <view class="card-body blue-bg">
            <view class="info-row">
              <text class="info-label">患者姓名</text>
              <text class="info-value">{{ record.patientName }}</text>
            </view>
            <view class="info-row" v-if="record.registrationRecordId">
              <text class="info-label">就诊记录编号</text>
              <text class="info-value">{{ record.registrationRecordId }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">症状</text>
              <text class="info-value symptoms">{{ truncateText(record.symptoms, 30) }}</text>
            </view>
            <view class="info-row" v-if="record.status === 'APPROVED' && record.reviewTimeText">
              <text class="info-label">审核时间</text>
              <text class="info-value">{{ record.reviewTimeText }}</text>
            </view>
            <view class="info-row" v-else-if="record.status === 'REJECTED'">
              <text class="info-label">驳回原因</text>
              <text class="info-value reject-reason">{{ record.rejectReason || '未说明' }}</text>
            </view>
            <view class="info-row" v-else-if="record.status === 'CANCELLED'">
              <text class="info-label">取消时间</text>
              <text class="info-value">{{ record.cancelTimeText || record.reviewTimeText || '--' }}</text>
            </view>
            <view class="info-row" v-else-if="record.status === 'PENDING'">
              <text class="info-label">状态</text>
              <text class="info-value waiting-tips">等待管理员审核</text>
            </view>
            <view class="info-row" v-if="record.status === 'APPROVED' && record.targetType === 'INTERNAL' && record.autoRegisterStatus !== null && record.autoRegisterStatus !== undefined">
              <text class="info-label">自动挂号状态</text>
              <text class="info-value" :class="getAutoRegisterStatusClass(record.autoRegisterStatus)">
                {{ getAutoRegisterStatusText(record.autoRegisterStatus) }}
              </text>
            </view>
          </view>
          <view class="card-actions">
            <view class="detail-section">
              <button class="detail-btn" @click.stop="viewRecordDetail(record)">查看详情</button>
              <button 
                v-if="record.status === 'APPROVED' && record.targetType === 'INTERNAL' && record.autoRegisterStatus !== null && record.autoRegisterStatus !== undefined"
                class="detail-btn status-btn"
                @click.stop="viewAutoRegisterStatus(record)"
              >
                查看挂号状态
              </button>
            </view>
            <view class="action-wrapper">
              <button
                v-if="record.status === 'APPROVED' && record.targetType === 'INTERNAL' && (record.autoRegisterStatus === null || record.autoRegisterStatus === undefined || record.autoRegisterStatus === 0)"
                class="small-action-btn blue-btn"
                @click.stop="handleAutoRegister(record)"
              >
                自动挂号
              </button>
              <button
                v-else-if="record.status === 'APPROVED' && record.targetType !== 'INTERNAL'"
                class="small-action-btn blue-btn"
                @click.stop="downloadReferralCertificate(record)"
              >
                下载转诊证明
              </button>
              <button
                v-else-if="record.status === 'PENDING'"
                class="small-cancel-btn"
                @click.stop="cancelReferral(record)"
              >
                取消申请
              </button>
            </view>
          </view>
        </view>

      <!-- 空状态 -->
      <view v-if="!loading && filteredRecords.length === 0" class="empty-container">
        <image src="/static/empty_message.png" mode="widthFix" class="empty-img" />
        <text class="empty-text">暂无转诊记录</text>
        <button class="create-btn" @click="createNewReferral">发起转诊申请</button>
      </view>

      <view v-if="loading && filteredRecords.length === 0" class="loading-state">
        <text class="loading-text">正在加载...</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPatientReferralList, cancelPatientReferral, autoRegisterInternalReferral } from '@/api/referral'
import { getRegistrationRecords } from '@/api/registration'
import { patientApi } from '@/api/patient'
import { useUserStore } from '@/store/user'

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

// 就诊人相关
const currentPatientInfo = ref({ name: '', visitNo: '' })
const selectedPatientId = ref(null)
const userStore = useUserStore()

// 日期范围筛选
const startDate = ref('')
const endDate = ref('')
const showDatePicker = ref(false)

// 根据筛选条件过滤记录
const filterStatus = ref(filterTabs[0].value || '')

// 格式化日期用于显示
const formatDateForDisplay = (dateStr) => {
  if (!dateStr) return ''
  // 如果是 YYYY-MM-DD 格式，转换为 YYYY年MM月DD日
  const match = dateStr.match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (match) {
    return `${match[1]}年${match[2]}月${match[3]}日`
  }
  return dateStr
}

// 解析日期字符串为Date对象（用于比较）
const parseDate = (dateStr) => {
  if (!dateStr) return null
  // 处理各种日期格式
  const str = String(dateStr).replace('T', ' ').split(' ')[0] // 取日期部分
  const date = new Date(str)
  if (isNaN(date.getTime())) return null
  // 设置为当天的0点0分0秒
  date.setHours(0, 0, 0, 0)
  return date
}

// 开始日期变化
const onStartDateChange = (e) => {
  const selectedDate = e.detail.value
  // 验证开始日期不能晚于结束日期
  if (endDate.value) {
    const start = parseDate(selectedDate)
    const end = parseDate(endDate.value)
    if (start && end && start > end) {
      uni.showToast({
        title: '开始日期不能晚于结束日期',
        icon: 'none'
      })
      return
    }
  }
  startDate.value = selectedDate
}

// 结束日期变化
const onEndDateChange = (e) => {
  const selectedDate = e.detail.value
  // 验证结束日期不能早于开始日期
  if (startDate.value) {
    const start = parseDate(startDate.value)
    const end = parseDate(selectedDate)
    if (start && end && end < start) {
      uni.showToast({
        title: '结束日期不能早于开始日期',
        icon: 'none'
      })
      return
    }
  }
  endDate.value = selectedDate
}

// 清除日期范围
const clearDateRange = () => {
  startDate.value = ''
  endDate.value = ''
  uni.showToast({
    title: '已清除日期筛选',
    icon: 'success',
    duration: 1500
  })
}

// 在弹窗中重置日期范围
const handleResetDateRange = () => {
  startDate.value = ''
  endDate.value = ''
  showDatePicker.value = false
  uni.showToast({
    title: '已重置日期筛选',
    icon: 'success',
    duration: 1500
  })
}

// 检查转诊记录是否在日期范围内
const isRecordInDateRange = (record) => {
  // 如果没有设置日期范围，显示所有记录
  if (!startDate.value && !endDate.value) {
    return true
  }
  
  // 使用申请时间作为比较基准
  const recordDateStr = record.applyTime || record.applyTimeText || record.createTime
  if (!recordDateStr) {
    // 如果没有日期信息，在设置了日期筛选的情况下不显示
    // 但如果是在"全部"状态下，应该显示（这里已经通过 startDate/endDate 判断了）
    return false
  }
  
  const recordDate = parseDate(recordDateStr)
  // 如果日期解析失败，在设置了日期筛选的情况下不显示
  if (!recordDate) {
    console.warn('日期解析失败:', recordDateStr, '记录ID:', record.id)
    return false
  }
  
  const start = parseDate(startDate.value)
  const end = parseDate(endDate.value)
  
  // 如果只有开始日期，只要记录日期 >= 开始日期即可
  if (startDate.value && !endDate.value) {
    return start ? recordDate >= start : true
  }
  
  // 如果只有结束日期，只要记录日期 <= 结束日期即可
  if (!startDate.value && endDate.value) {
    return end ? recordDate <= end : true
  }
  
  // 如果两个日期都有，记录日期必须在范围内
  if (start && end) {
    return recordDate >= start && recordDate <= end
  }
  
  // 如果日期解析失败，默认显示（避免因为日期格式问题导致记录不显示）
  return true
}

const filteredRecords = computed(() => {
  let records = referralRecords.value || []
  
  // 按状态筛选 - 如果选择的是"全部"（空字符串），不筛选
  if (filterStatus.value && filterStatus.value !== '') {
    records = records.filter(record => {
      const recordStatus = (record.status || '').toUpperCase()
      const filterStatusUpper = (filterStatus.value || '').toUpperCase()
      return recordStatus === filterStatusUpper
    })
  }
  
  // 按就诊人筛选 - 使用患者ID筛选（更可靠）
  // 只有当选择了就诊人时才筛选，如果没有选择就诊人（selectedPatientId为null），显示所有记录
  if (selectedPatientId.value !== null && selectedPatientId.value !== undefined) {
    const currentPatientName = (currentPatientInfo.value.name || '').trim()
    const selectedPatientIdNum = Number(selectedPatientId.value)
    
    records = records.filter(record => {
      const recordPatientId = record.patientId || record.patient_id || null
      const recordPatientName = (record.patientName || '').trim()
      const registrationRecordId = record.registrationRecordId || record.registration_record_id || null
      
      // 优先使用 patientId 匹配
      if (recordPatientId !== null && recordPatientId !== undefined) {
        const recordPatientIdNum = Number(recordPatientId)
        if (!isNaN(recordPatientIdNum) && recordPatientIdNum === selectedPatientIdNum) {
          return true
        }
      }
      
      // 如果 patientId 不匹配或不存在，使用姓名匹配作为备选（兼容旧数据）
      if (currentPatientName && recordPatientName) {
        const nameMatch = recordPatientName === currentPatientName
        if (nameMatch) {
          return true
        }
      }
      
      // 如果 patientId 为 null 但有 registrationRecordId，说明后端没有返回 patientId
      // 这种情况下，转诊记录是通过挂号记录关联的，应该属于当前登录用户
      // 为了不丢失数据，显示所有有 registrationRecordId 但 patientId 为 null 的记录
      // 因为转诊记录是通过挂号记录关联的，而挂号记录应该属于当前用户
      if (recordPatientId === null && registrationRecordId) {
        return true
      }
      
      // 如果既没有 patientId 也没有姓名匹配，或者都不匹配，则不显示
      return false
    })
  }
  
  // 按日期范围筛选
  records = records.filter(record => isRecordInDateRange(record))
  
  return records
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
  uni.navigateTo({
    url: `/subpkg/hospital/referral-detail?id=${record.id}`
  })
}

// 获取自动挂号状态文本
const getAutoRegisterStatusText = (status) => {
  if (status === null || status === undefined) return ''
  if (status === 0) return '未处理'
  if (status === 1) return '挂号成功'
  if (status === 2) return '挂号失败'
  return '未知状态'
}

// 获取自动挂号状态样式类
const getAutoRegisterStatusClass = (status) => {
  if (status === 1) return 'status-success'
  if (status === 2) return 'status-failed'
  return ''
}

// 查看自动挂号状态
const viewAutoRegisterStatus = async (record) => {
  const status = record.autoRegisterStatus
  
  if (status === 1) {
    // 挂号成功，跳转到挂号记录详情
    try {
      // 获取患者ID（优先从转诊记录中获取，如果没有则使用当前选择的就诊人ID）
      let patientId = record.patientId || record.patient_id
      
      // 如果转诊记录中没有 patientId，尝试从当前选择的就诊人获取
      if (!patientId && selectedPatientId.value) {
        patientId = selectedPatientId.value
      }
      
      // 如果还是没有，尝试从关联的原始挂号记录中获取（需要查询）
      if (!patientId && record.registrationRecordId) {
        // 这里可以尝试通过 registrationRecordId 查询原始挂号记录获取 patientId
        // 但为了简化，先使用当前选择的就诊人ID
        patientId = selectedPatientId.value
      }
      
      if (!patientId) {
        uni.showToast({
          title: '无法获取患者信息，请先选择就诊人',
          icon: 'none'
        })
        return
      }
      
      // 获取该患者的所有挂号记录
      uni.showLoading({ title: '加载中...' })
      const records = await getRegistrationRecords(patientId)
      const recordsList = Array.isArray(records) ? records : []
      
      // 查找匹配的挂号记录：
      // 1. scheduleId 等于 assignedScheduleId
      // 2. isAdd === 1（加号标记）
      // 3. addRemark 包含"转诊"或"院内转诊"
      const assignedScheduleId = record.assignedScheduleId || record.assigned_schedule_id
      const matchedRecord = recordsList.find(r => {
        const scheduleIdMatch = (r.scheduleId || r.schedule_id) === assignedScheduleId
        const isAddMatch = (r.isAdd || r.is_add) === 1
        const addRemark = (r.addRemark || r.add_remark || '').toString()
        const remarkMatch = addRemark.includes('转诊') || addRemark.includes('院内转诊')
        return scheduleIdMatch && isAddMatch && remarkMatch
      })
      
      uni.hideLoading()
      
      if (matchedRecord) {
        // 构建挂号记录对象，用于跳转到详情页
        const recordData = {
          id: matchedRecord.recordId || matchedRecord.record_id || matchedRecord.id,
          recordId: matchedRecord.recordId || matchedRecord.record_id || matchedRecord.id,
          patientId: matchedRecord.patientId || matchedRecord.patient_id || patientId,
          scheduleId: matchedRecord.scheduleId || matchedRecord.schedule_id,
          doctorId: matchedRecord.doctorId || matchedRecord.doctor_id,
          typeId: matchedRecord.typeId || matchedRecord.type_id,
          registrationNo: matchedRecord.registrationNo || matchedRecord.registration_no,
          registerTime: matchedRecord.registerTime || matchedRecord.register_time,
          status: matchedRecord.status,
          priceOriginal: matchedRecord.priceOriginal || matchedRecord.price_original,
          actualPrice: matchedRecord.actualPrice || matchedRecord.actual_price,
          isAdd: matchedRecord.isAdd || matchedRecord.is_add,
          addRemark: matchedRecord.addRemark || matchedRecord.add_remark
        }
        
        // 跳转到挂号记录详情页面
        uni.navigateTo({
          url: `/subpkg/profile/records/hospital-record-detail?record=${encodeURIComponent(JSON.stringify(recordData))}`,
          fail: (err) => {
            console.error('跳转挂号记录详情失败:', err)
            uni.showToast({
              title: '跳转失败，请重试',
              icon: 'none'
            })
          }
        })
      } else {
        // 如果找不到匹配的记录，显示状态信息
        let content = `自动挂号状态：已成功\n`
        if (record.assignedDate) {
          content += `预约日期：${record.assignedDate}\n`
        }
        if (record.assignedTimeSlot) {
          const timeSlotMap = { 1: '上午', 2: '下午', 3: '晚上' }
          content += `预约时段：${timeSlotMap[record.assignedTimeSlot] || record.assignedTimeSlot}\n`
        }
        content += '未找到对应的挂号记录，请稍后查看'
        
        uni.showModal({
          title: '自动挂号状态',
          content: content,
          showCancel: false,
          confirmText: '知道了'
        })
      }
    } catch (error) {
      uni.hideLoading()
      console.error('查询挂号记录失败:', error)
      uni.showToast({
        title: '查询挂号记录失败',
        icon: 'none'
      })
    }
  } else if (status === 2) {
    // 挂号失败
    let content = '自动挂号状态：挂号失败\n'
    if (record.quotaAction === 'WAITLIST' && record.waitNumber) {
      content += `已加入候补队列，候补号：${record.waitNumber}`
    } else {
      content += '当前无可用排班，请稍后重试或联系医院'
    }
    
    uni.showModal({
      title: '自动挂号状态',
      content: content,
      showCancel: false,
      confirmText: '知道了'
    })
  } else {
    uni.showModal({
      title: '自动挂号状态',
      content: '自动挂号状态：未处理',
      showCancel: false,
      confirmText: '知道了'
    })
  }
}

// 自动挂号（院内转诊）
const handleAutoRegister = async (record) => {
  if (!record || !record.id) {
    uni.showToast({
      title: '转诊记录信息不完整',
      icon: 'none'
    })
    return
  }
  
  // 检查是否已经自动挂号过
  if (record.autoRegisterStatus === 1) {
    uni.showToast({
      title: '该转诊申请已经成功自动挂号，无需重复申请',
      icon: 'none',
      duration: 3000
    })
    return
  }
  
  uni.showModal({
    title: '提示',
    content: `确定要为患者${record.patientName}在${record.targetDepartment}自动挂号吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '申请中...' })
          const result = await autoRegisterInternalReferral(record.id)
          
          if (result) {
              uni.showToast({
                title: '自动挂号成功！',
                icon: 'success'
              })
              // 重新加载转诊记录和就诊记录
              setTimeout(() => {
                currentPage.value = 1
                loadReferralRecords()
                // 通知就诊记录页面刷新数据
                uni.$emit('refreshHospitalRecords')
              }, 1500)
            } else {
              uni.showToast({
                title: '自动挂号失败',
                icon: 'none'
              })
            }
        } catch (error) {
          console.error('自动挂号失败:', error)
          let errorMsg = '自动挂号失败，请稍后重试'
          if (error.message) {
            if (error.message.includes('已经成功')) {
              errorMsg = '该转诊申请已经成功自动挂号，无需重复申请'
            } else if (error.message.includes('不满足')) {
              errorMsg = '不满足自动挂号条件'
            } else {
              errorMsg = error.message
            }
          }
          uni.showToast({
            title: errorMsg,
            icon: 'none',
            duration: 3000
          })
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

// 下载转诊证明
const downloadReferralCertificate = (record) => {
  // 跳转到转诊单页面
  uni.navigateTo({
    url: `/subpkg/hospital/referral-certificate?id=${record.id || record.referralId}`
  })
}

// 创建新转诊申请
const createNewReferral = () => {
  uni.navigateTo({
    url: '/subpkg/hospital/referral-application',
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

// 获取就诊人列表
const fetchPatientList = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) {
    return []
  }

  try {
    const data = await patientApi.getPatientList({ userId })
    if (Array.isArray(data)) {
      return data
    }
    if (data && Array.isArray(data.list)) {
      return data.list
    }
    return []
  } catch (error) {
    console.warn('获取就诊人列表失败:', error)
    uni.showToast({
      title: '获取就诊人列表失败',
      icon: 'none'
    })
    return []
  }
}

// 更新当前就诊人信息
const updateCurrentPatientInfo = async (patientId) => {
  try {
    const list = await fetchPatientList()
    const target = list.find(item => Number(item.patientId) === Number(patientId))
    if (target) {
      selectedPatientId.value = Number(target.patientId)
      currentPatientInfo.value = {
        name: target.patientName || target.name || '',
        visitNo: target.visitNo || target.cardNumber || ''
      }
    }
  } catch (error) {
    console.warn('更新就诊人信息失败:', error)
  }
}

// 打开就诊人选择
const openPatientSelect = async () => {
  const list = await fetchPatientList()
  if (!list.length) {
    uni.showModal({
      title: '提示',
      content: '当前账号还没有就诊卡，无法切换，请先创建就诊卡。',
      confirmText: '去创建',
      cancelText: '稍后再说',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/subpkg/profile/personal/create-card' })
        }
      }
    })
    return
  }

  if (list.length === 1) {
    await updateCurrentPatientInfo(list[0].patientId)
    await loadReferralRecords()
    return
  }

  const selected = await new Promise((resolve) => {
    uni.showActionSheet({
      itemList: list.map(item => {
        const name = item.patientName || '未命名就诊人'
        const typeMap = { 1: '学生', 2: '教师', 3: '职工' }
        const typeText = typeMap[item.patientType] || ''
        return typeText ? `${name}（${typeText}）` : name
      }),
      success: (res) => {
        resolve(list[res.tapIndex] || null)
      },
      fail: () => resolve(null)
    })
  })

  if (selected?.patientId) {
    await updateCurrentPatientInfo(selected.patientId)
    await loadReferralRecords()
  }
}

// 初始化就诊人信息
const initPatientInfo = async () => {
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) {
      return
    }
    
    const list = await fetchPatientList()
    if (list.length > 0) {
      const firstPatient = list[0]
      selectedPatientId.value = Number(firstPatient.patientId)
      currentPatientInfo.value = {
        name: firstPatient.patientName || firstPatient.name || '',
        visitNo: firstPatient.visitNo || firstPatient.cardNumber || ''
      }
    } else {
      // 如果没有就诊人，设置为null，显示所有记录
      selectedPatientId.value = null
      currentPatientInfo.value = {
        name: '',
        visitNo: ''
      }
    }
  } catch (error) {
    console.warn('初始化就诊人信息失败:', error)
    // 出错时也设置为null，显示所有记录
    selectedPatientId.value = null
  }
}

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
    
    // 不在这里添加patientId筛选，让后端返回所有记录，然后在前端筛选
    // 这样可以确保即使没有选择就诊人，也能看到所有记录

    const res = await getPatientReferralList(params)
    // 支持多种数据格式 - 优先检查result字段
    let rawRecords = []
    if (Array.isArray(res?.result?.records)) {
      rawRecords = res.result.records
    } else if (Array.isArray(res?.records)) {
      rawRecords = res.records
    } else if (Array.isArray(res?.data?.records)) {
      rawRecords = res.data.records
    } else if (Array.isArray(res?.data)) {
      rawRecords = res.data
    } else if (Array.isArray(res?.result)) {
      rawRecords = res.result
    } else if (Array.isArray(res)) {
      rawRecords = res
    } else {
      console.warn('未找到有效的记录数组，响应结构:', res)
    }
    const normalizedRecords = rawRecords.map(record => {
      const status = (record.status || '').toUpperCase()
      const applyTime = record.applyTime || record.createTime || ''
      const reviewTime = record.reviewTime || ''
      const cancelTime = record.cancelTime || ''
      
      // 从registrationRecordId关联中获取patientId
      // 后端通过 LEFT JOIN registration_record 返回了 rr.patient_id
      // 优先使用后端返回的 patient_id（来自关联的挂号记录）
      const patientId = record.patient_id || record.patientId || null
      
      return {
        id: record.id || record.referralId || '',
        patientId: patientId,
        patientName: record.patientName || '',
        targetHospital: record.targetHospitalName || record.targetHospital || '校医院',
        targetDepartment: record.targetDeptName || record.targetDepartment || '',
        targetType: record.targetType || record.target_type || 'INTERNAL', // 转诊类型：INTERNAL/EXTERNAL
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
        sourceType: record.sourceType || '',
        registrationRecordId: record.registrationRecordId || record.registration_record_id || null,
        autoRegisterStatus: record.autoRegisterStatus || record.auto_register_status || null,
        assignedScheduleId: record.assignedScheduleId || record.assigned_schedule_id || null,
        assignedDate: record.assignedDate || record.assigned_date || null,
        assignedTimeSlot: record.assignedTimeSlot || record.assigned_time_slot || null,
        quotaAction: record.quotaAction || record.quota_action || null,
        waitNumber: record.waitNumber || record.wait_number || null
      }
    })

    if (currentPage.value === 1) {
      referralRecords.value = normalizedRecords
    } else {
      referralRecords.value = [...referralRecords.value, ...normalizedRecords]
    }

    const total = res?.result?.total || res?.total || res?.data?.total || rawRecords.length || 0
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
onMounted(async () => {
  await initPatientInfo()
  loadReferralRecords()
})
</script>

<style scoped>
.page-bg {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding: 20rpx;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 30rpx 20rpx;
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  border-radius: 0 0 32rpx 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(74, 144, 226, 0.2);
  margin-bottom: 20rpx;
}

.list-header-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.header-icon {
  font-size: 40rpx;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6rpx);
  }
}

.list-header {
  font-size: 42rpx;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 2rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
  position: relative;
}

.list-header::after {
  content: '';
  position: absolute;
  bottom: -4rpx;
  left: 0;
  right: 0;
  height: 4rpx;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
  border-radius: 2rpx;
}

.header-badge {
  background-color: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10rpx);
  color: #ffffff;
  font-size: 22rpx;
  font-weight: 600;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  min-width: 40rpx;
  text-align: center;
  line-height: 1.4;
}

.header-actions {
  display: flex;
  gap: 20rpx;
}

.refresh-btn.small {
  padding: 8rpx 24rpx;
  height: 52rpx;
  line-height: 36rpx;
  background-color: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10rpx);
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  border-radius: 26rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: #ffffff;
  width: auto;
  margin: 0;
  transition: all 0.3s ease;
}

.refresh-icon {
  font-size: 24rpx;
}

.record-container {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
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

.patient-card {
  margin: 24rpx 0;
  padding: 24rpx 28rpx;
  background: linear-gradient(135deg, #f0f6ff, #ffffff);
  border-radius: 24rpx;
  box-shadow: 0 6rpx 16rpx rgba(74, 144, 226, 0.12);
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1rpx solid rgba(74, 144, 226, 0.2);
}

.patient-info-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.patient-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #6ea8ff, #4a90e2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 18rpx rgba(74, 144, 226, 0.25);
}

.patient-text {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.patient-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.patient-visit-no {
  font-size: 24rpx;
  color: #5c6b7a;
}

.patient-info-right {
  display: flex;
  align-items: center;
}

.patient-switch-btn {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #4a90e2, #6ec6ff);
  color: #ffffff;
  border: none;
  border-radius: 999rpx;
  font-size: 26rpx;
  font-weight: 600;
  box-shadow: 0 6rpx 16rpx rgba(74, 144, 226, 0.25);
  transition: all 0.3s ease;
}

.patient-switch-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 3rpx 10rpx rgba(74, 144, 226, 0.25);
}

.patient-switch-btn .btn-icon {
  font-size: 32rpx;
  line-height: 1;
}

.patient-switch-btn .btn-text {
  font-size: 26rpx;
  line-height: 1;
}

.filter-tabs {
  display: flex;
  overflow-x: auto;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  margin-bottom: 10rpx;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  background-color: #ffffff;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.filter-tabs::-webkit-scrollbar {
  display: none;
}

.filter-tab {
  flex-shrink: 0;
  padding: 16rpx 40rpx;
  background-color: #f5f5f5;
  border-radius: 36rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: #666;
  transition: all 0.3s ease;
  white-space: nowrap;
  position: relative;
}

.filter-tab.active {
  background-color: #4a90e2;
  color: white;
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.3);
}

.filter-tab:active {
  transform: scale(0.95);
}

/* 日期筛选区域样式 - 紧凑形式 */
.date-filter-compact {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 12rpx 20rpx;
  margin-bottom: 12rpx;
  gap: 12rpx;
}

.date-filter-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background-color: #ffffff;
  border: 1rpx solid #e4e7ed;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.calendar-icon {
  font-size: 28rpx;
  color: #4a90e2;
}

.date-range-text {
  font-size: 24rpx;
  color: #333;
  max-width: 400rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.date-range-text.placeholder {
  color: #999;
}

.clear-date-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background-color: #fff5f5;
  border: 1rpx solid #ffcccc;
  border-radius: 20rpx;
  transition: all 0.3s ease;
}

.clear-date-btn:active {
  background-color: #ffe0e0;
  border-color: #ffaaaa;
}

.clear-date-icon {
  font-size: 24rpx;
  color: #ff4d4f;
  line-height: 1;
}

.clear-date-text {
  font-size: 24rpx;
  color: #ff4d4f;
  line-height: 1;
}

/* 日历选择弹窗样式 */
.date-picker-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.date-picker-content {
  width: 85%;
  max-width: 600rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(50%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.date-picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.date-picker-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.date-picker-close {
  font-size: 36rpx;
  color: #999;
  padding: 4rpx;
  line-height: 1;
}

.date-picker-body {
  padding: 30rpx;
}

.date-picker-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.date-picker-row:last-child {
  margin-bottom: 0;
}

.date-picker-label {
  font-size: 28rpx;
  color: #666;
  min-width: 120rpx;
}

.date-picker-view {
  flex: 1;
  padding: 16rpx 24rpx;
  background-color: #f5f7fa;
  border-radius: 12rpx;
  border: 1rpx solid #e4e7ed;
}

.date-picker-value {
  font-size: 28rpx;
  color: #333;
}

.date-picker-placeholder {
  font-size: 28rpx;
  color: #999;
}

.date-picker-footer {
  padding: 24rpx 30rpx;
  border-top: 1rpx solid #f0f0f0;
  display: flex;
  gap: 16rpx;
  align-items: center;
}

.date-picker-confirm {
  flex: 1;
  padding: 24rpx;
  background-color: #4a90e2;
  color: #ffffff;
  border: none;
  border-radius: 12rpx;
  font-size: 30rpx;
  text-align: center;
}

.date-picker-confirm:active {
  background-color: #357abd;
}

.date-picker-reset {
  flex: 1;
  padding: 24rpx;
  background-color: #ffffff;
  color: #666666;
  border: 1rpx solid #e4e7ed;
  border-radius: 12rpx;
  font-size: 30rpx;
  text-align: center;
}

.date-picker-reset:active {
  background-color: #f5f7fa;
  border-color: #d0d7de;
}

.records-list {
  padding: 0;
  height: calc(100vh - 300px);
}

.record-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.record-card:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  margin-bottom: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
}

.operation-time {
  font-size: 26rpx;
  color: #666;
  font-weight: 500;
}

.card-title {
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
}

.hospital-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #23324b;
}

.department-name {
  font-size: 28rpx;
  color: #666;
}

.badge-group {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.status-badge {
  padding: 4rpx 20rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  font-weight: 600;
  white-space: nowrap;
}

.status-pending {
  background: #fff3e6;
  color: #ff7a45;
}

.status-approved {
  background: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background: #fff1f0;
  color: #ff4d4f;
}

.status-cancelled {
  background: #f5f5f5;
  color: #9e9e9e;
}

.source-badge {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  background-color: #e6f7ff;
  color: #1989fa;
  font-weight: 500;
}

.card-body {
  margin-bottom: 24rpx;
  padding: 20rpx;
  border-radius: 8rpx;
}

.blue-bg {
  background-color: #e3f2fd;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 8rpx 0;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  color: #1976d2;
  font-size: 28rpx;
  min-width: 140rpx;
}

.info-value {
  color: #333;
  font-size: 28rpx;
  flex: 1;
  text-align: right;
  word-break: break-all;
}

.info-value.symptoms {
  color: #666;
}

.info-value.reject-reason {
  color: #ff4d4f;
}

.info-value.waiting-tips {
  color: #ff7a45;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-section {
  display: flex;
}

.detail-btn {
  font-size: 24rpx;
  color: #4a90e2;
  background: none;
  padding: 8rpx 20rpx;
  border: none;
}
.status-btn {
  margin-left: 8rpx;
}
.info-value.status-success {
  color: #52c41a;
  font-weight: 600;
}
.info-value.status-failed {
  color: #ff4d4f;
  font-weight: 600;
}

.action-wrapper {
  display: flex;
  justify-content: flex-end;
}

.small-action-btn,
.small-cancel-btn {
  font-size: 24rpx;
  padding: 8rpx 32rpx;
  border-radius: 20rpx;
  border: none;
  min-width: 140rpx;
  line-height: 1.5;
}

.small-action-btn.blue-btn {
  background-color: #4a90e2;
  color: white;
}

.small-action-btn.blue-btn:active {
  background-color: #357abd;
}

.small-cancel-btn {
  background-color: transparent;
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
}

.small-cancel-btn:active {
  background-color: #fff1f0;
}

.empty-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 500rpx;
  background: #fff;
  border-radius: 20rpx;
  margin-top: 40rpx;
  padding: 40rpx;
}

.empty-img {
  width: 300rpx;
  height: 200rpx;
  margin-bottom: 30rpx;
}

.empty-text {
  color: #999;
  font-size: 28rpx;
  margin-bottom: 20rpx;
}

.create-btn {
  padding: 16rpx 48rpx;
  background-color: #4a90e2;
  color: #fff;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
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