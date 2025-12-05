<template>
  <view class="page-bg">
    <!-- 标题和刷新按钮区域 -->
    <view class="header-section">
      <view class="list-header">就诊记录</view>
      <view class="header-actions">
        <button class="refresh-btn small" @click="loadHospitalRecords">
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
        v-for="tab in filterTabs" 
        :key="tab.value"
        class="filter-tab"
        :class="{ active: currentFilter === tab.value }"
        @click="changeFilter(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>
    
    <view class="record-container">
      <view 
        class="record-card" 
        v-for="item in filteredRecords" 
        :key="item.id"
        @click="viewRecordDetail(item)"
      >
        <view class="card-header">
          <text class="operation-time">{{ item.displayRegisterTime }}</text>
          <text class="status-badge" v-if="item.statusDisplay">{{ item.statusDisplay }}</text>
        </view>
        <view class="card-title">
          <text class="department">{{ item.department || '-' }}</text>
        </view>
        <view class="card-body blue-bg">
          <view class="info-row">
            <text class="info-label">医生</text>
            <text class="info-value doctor-name">{{ item.doctor || item.doctorName || '-' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">就诊编号</text>
            <text class="info-value">{{ item.recordNumberDisplay }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">状态</text>
            <text class="info-value status-text">{{ item.statusDisplay }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">挂号时间</text>
            <text class="info-value">{{ item.displayRegisterTime }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">就诊时间</text>
            <text class="info-value">{{ item.displayVisitTime }}</text>
          </view>
        </view>
        <view class="card-actions">
          <view class="detail-section">
            <button class="detail-btn" @click.stop="viewRecordDetail(item)">查看详情</button>
          </view>
          <view class="referral-wrapper">
            <button 
              v-if="item.canRefer" 
              class="small-referral-btn blue-btn" 
              @click.stop="goToReferralApplication(item)"
            >
              申请转诊
            </button>
            <text v-else class="cannot-refer-text">超过5天，无法转诊</text>
          </view>
        </view>
      </view>
      <view v-if="filteredRecords.length === 0 && records.length > 0" class="empty-container">
        <image class="empty-icon" src="/static/no-records.svg" mode="aspectFit"></image>
        <text class="empty-text">暂无该状态的就诊记录</text>
      </view>
      <view v-else-if="records.length === 0" class="empty-container">
        <image class="empty-icon" src="/static/no-records.svg" mode="aspectFit"></image>
        <text class="empty-text">暂无就诊记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getRegistrationRecords } from '@/api/registration'
import { ensurePatientCard } from '@/utils/patientHelper'

import { getDoctorDetail } from '@/api/doctor_massage'
import { getDepartmentDetail } from '@/api/department'
import { patientApi } from '@/api/patient'
import { useUserStore } from '@/store/user'

const records = ref([])
const currentPatientInfo = ref({ name: '', visitNo: '' })
const selectedPatientId = ref(null)
const currentFilter = ref('all')
const doctorDepartmentMap = ref(new Map())
const userStore = useUserStore()

const filterTabs = [
  { label: '全部', value: 'all' },
  { label: '待就诊', value: '待就诊' },
  { label: '已完成', value: '已完成' },
  { label: '已取消', value: '已取消' },
  { label: '已过期', value: '已过期' }
]

const STATUS_DEFINITIONS = {
  pending: {
    key: 'pending',
    code: 1,
    label: '待就诊',
    description: '还没到预约的就诊时间',
    allowReferral: false
  },
  completed: {
    key: 'completed',
    code: 2,
    label: '已完成',
    description: '已过就诊时间但是还没有超过五天，还能转诊',
    allowReferral: true
  },
  cancelled: {
    key: 'cancelled',
    code: 3,
    label: '已取消',
    description: '就诊时间之前已取消',
    allowReferral: false
  },
  expired: {
    key: 'expired',
    code: 4,
    label: '已过期',
    description: '已过就诊时间五天无法转诊',
    allowReferral: false
  }
}

const RAW_STATUS_CODE_MAP = new Map([
  ['0', STATUS_DEFINITIONS.pending],
  ['1', STATUS_DEFINITIONS.completed],
  ['2', STATUS_DEFINITIONS.cancelled],
  ['3', STATUS_DEFINITIONS.expired],
  ['4', STATUS_DEFINITIONS.expired]
])

const STATUS_KEYWORD_RULES = [
  { regex: /(取消|退号|作废|关闭|失败|拒绝|撤销)/, status: STATUS_DEFINITIONS.cancelled },
  { regex: /(过期|失效)/, status: STATUS_DEFINITIONS.expired },
  { regex: /(完成|成功|已支付|已就诊|诊疗|结束)/, status: STATUS_DEFINITIONS.completed },
  { regex: /(待|未支付|预约|排队|未就诊|确认中)/, status: STATUS_DEFINITIONS.pending }
]

const DAY_IN_MS = 1000 * 60 * 60 * 24

const parseToDate = (value) => {
  if (!value) return null
  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value
  }
  if (typeof value === 'number') {
    const numericDate = new Date(value)
    return Number.isNaN(numericDate.getTime()) ? null : numericDate
  }
  const stringValue = String(value).trim()
  if (!stringValue) return null

  const sanitized = stringValue
    .replace('T', ' ')
    .replace(/\.\d+$/, '')
  const normalized = sanitized.includes('-') ? sanitized.replace(/-/g, '/') : sanitized

  const parsed = new Date(normalized)
  if (!Number.isNaN(parsed.getTime())) {
    return parsed
  }

  const fallback = new Date(sanitized)
  return Number.isNaN(fallback.getTime()) ? null : fallback
}

const cloneStatus = (statusDefinition) => ({ ...statusDefinition })

const resolveStatusInfo = (rawStatusValue, visitTimeStr, registerTimeStr) => {
  const rawText = String(rawStatusValue ?? '').trim()

  if (RAW_STATUS_CODE_MAP.has(rawText)) {
    return cloneStatus(RAW_STATUS_CODE_MAP.get(rawText))
  }

  if (rawText && RAW_STATUS_CODE_MAP.has(String(Number(rawText)))) {
    return cloneStatus(RAW_STATUS_CODE_MAP.get(String(Number(rawText))))
  }

  const visitDate = parseToDate(visitTimeStr || registerTimeStr)
  const now = new Date()

  if (rawText) {
    if (/(取消|退号|作废|关闭|失败|拒绝|撤销)/.test(rawText)) {
      return cloneStatus(STATUS_DEFINITIONS.cancelled)
    }
    if (/(过期|失效)/.test(rawText)) {
      return cloneStatus(STATUS_DEFINITIONS.expired)
    }
  }

  if (visitDate) {
    if (now < visitDate) {
      return cloneStatus(STATUS_DEFINITIONS.pending)
    }
    const diffDays = (now.getTime() - visitDate.getTime()) / DAY_IN_MS
    if (diffDays <= 5) {
      return cloneStatus(STATUS_DEFINITIONS.completed)
    }
    return cloneStatus(STATUS_DEFINITIONS.expired)
  }

  if (rawText) {
    for (const rule of STATUS_KEYWORD_RULES) {
      if (rule.regex.test(rawText)) {
        return cloneStatus(rule.status)
      }
    }
  }

  return cloneStatus(STATUS_DEFINITIONS.pending)
}

const pickStringValue = (obj, paths) => {
  for (const path of paths) {
    const keys = path.split('.')
    let value = obj
    for (const key of keys) {
      value = value?.[key]
    }
    if (value && typeof value === 'string') {
      return value
    }
  }
  return ''
}

const resolveRecordNumber = (record) => {
  if (!record) return ''
  const candidateFields = [
    'recordNumber',
    'registrationNo',
    'registration_no',
    'registrationNumber',
    'registration_number',
    'registrationId',
    'registration_id',
    'appointmentNumber',
    'appointment_number',
    'appointmentId',
    'appointment_id',
    'orderNo',
    'order_no',
    'id',
    'recordId',
    'record_id'
  ]

  for (const field of candidateFields) {
    const value = record[field]
    if (value === 0) {
      return '0'
    }
    if (value !== undefined && value !== null) {
      const stringValue = String(value).trim()
      if (stringValue) {
        return stringValue
      }
    }
  }

  return ''
}

const formatDateTimeForDisplay = (raw) => {
  if (!raw) return '-'
  const candidate = String(raw).replace('T', ' ')
  const match = candidate.match(/\d{4}[-\/]\d{2}[-\/]\d{2}\s\d{2}:\d{2}(?::\d{2})?/)
  const extracted = match ? match[0] : candidate
  const normalizedForDate = extracted.replace(/-/g, '/').replace('T', ' ')
  const date = new Date(normalizedForDate)
  if (Number.isNaN(date.getTime())) {
    return extracted.replace(/\//g, '-').split('.')[0]
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const getDepartmentByDoctorId = async (doctorId) => {
  if (doctorDepartmentMap.value.has(doctorId)) {
    return doctorDepartmentMap.value.get(doctorId)
  }
  
  try {
    const doctorRes = await getDoctorDetail(doctorId)
    let doctorData = doctorRes
    
    if (doctorRes?.data) {
      doctorData = doctorRes.data
    } else if (doctorRes?.result) {
      doctorData = doctorRes.result
    }

    let departmentName = ''
    let deptId = doctorData?.deptId
    const resolvedDoctorName = doctorData?.doctorName || doctorData?.name || doctorData?.realname || doctorData?.realName || ''

    if (doctorData?.deptName) {
      departmentName = doctorData.deptName
    }

    if (!departmentName && deptId) {
      try {
        const deptResponse = await getDepartmentDetail(deptId)
        let deptData = deptResponse
        if (deptResponse?.data) {
          deptData = deptResponse.data
        } else if (deptResponse?.result) {
          deptData = deptResponse.result
        }
        const resolvedName = deptData?.deptName || deptData?.name || deptData?.departmentName || deptData?.department
        if (resolvedName) {
          departmentName = resolvedName
        }
      } catch (error) {
        console.warn('通过科室ID查询详情失败:', error)
      }
    }

    if (!departmentName && deptId) {
      departmentName = `科室ID: ${deptId}`
    }

    doctorDepartmentMap.value.set(doctorId, {
      name: departmentName,
      deptId,
      doctorName: resolvedDoctorName
    })
    return {
      name: departmentName,
      deptId,
      doctorName: resolvedDoctorName
    }
  } catch (error) {
    console.error(`获取医生 ${doctorId} 科室信息失败:`, error)
    return { name: '', deptId: null, doctorName: '' }
  }
}

const processRecordsDepartments = async (recordsList) => {
  const doctorIds = new Set()
  recordsList.forEach(record => {
    if (record.doctorId || record.doctor_id) {
      doctorIds.add(record.doctorId || record.doctor_id)
    }
  })
  
  await Promise.all(
    Array.from(doctorIds).map(async (doctorId) => {
      await getDepartmentByDoctorId(doctorId)
    })
  )
  
  return recordsList.map(record => {
    const doctorIdKey = record.doctorId || record.doctor_id
    const deptInfo = doctorIdKey ? doctorDepartmentMap.value.get(doctorIdKey) : null
    return {
      ...record,
      departmentName: deptInfo?.name || '',
      departmentId: deptInfo?.deptId || (record.departmentId ?? record.dept_id ?? null),
      doctorResolvedName: deptInfo?.doctorName || ''
    }
  })
}

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

const ensurePatientId = async () => {
  if (selectedPatientId.value) {
    await updateCurrentPatientInfo(selectedPatientId.value)
    return selectedPatientId.value
  }
  
  const card = await ensurePatientCard()
  if (card?.patientId) {
    selectedPatientId.value = Number(card.patientId)
    currentPatientInfo.value = {
      name: card.patientName || card.name || '',
      visitNo: card.visitNo || card.cardNumber || ''
    }
    return selectedPatientId.value
  }

  uni.showModal({
    title: '未找到就诊卡',
    content: '请先创建就诊卡后再查看就诊记录',
    confirmText: '去创建',
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({ url: '/subpkg/profile/personal/create-card' })
      }
    }
  })

  return null
}

const loadHospitalRecords = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    
    const patientId = await ensurePatientId()
    if (!patientId) {
      uni.hideLoading()
      return
    }
    
    let response
    try {
      response = await getRegistrationRecords(patientId, { timeout: 20000 })
    } catch (error) {
      console.warn('首次请求失败，重试...', error)
      response = await getRegistrationRecords(patientId, { timeout: 20000 })
    }
    
    let list = response?.data || response || []
    if (!Array.isArray(list)) {
      list = []
    }
    
    const recordsWithDepartment = await processRecordsDepartments(list)
    list = recordsWithDepartment
    
    records.value = list.map(item => {
      const deptName = (item.departmentName || '').trim()
      const doctorName = pickStringValue(item, [
        'doctorName',
        'doctor_name',
        'doctor',
        'doctorId',
        'doctor_id'
      ])

      const rawStatus = item.status || item.orderStatus || item.appointmentStatus || item.registrationStatus || '未知'
      const recordNumber = resolveRecordNumber(item)

      const resolvedDoctor = doctorName.trim() || (item.doctorResolvedName || '').trim()
      const rawRegisterTime = item.registerTime || item.registration_time || item.appointmentTime || ''
      const rawVisitTime = item.visitTime || item.visit_time || ''
      const displayRegisterTime = formatDateTimeForDisplay(rawRegisterTime)
      const displayVisitTime = formatDateTimeForDisplay(rawVisitTime)
      const displayTimeSlot = item.timeSlot || item.time_slot || item.appointmentTimeSlot || displayVisitTime || '-'
      const statusInfo = resolveStatusInfo(rawStatus, rawVisitTime, rawRegisterTime)

      return {
        id: item.recordId || item.id,
        department: deptName || '-',
        departmentId: item.departmentId || item.deptId || item.dept_id || null,
        doctor: resolvedDoctor || '-',
        doctorName: resolvedDoctor || '-',
        registerTime: rawRegisterTime,
        visitTime: rawVisitTime,
        displayRegisterTime,
        displayVisitTime,
        timeSlot: displayTimeSlot,
        status: rawStatus,
        statusDisplay: statusInfo.label,
        normalizedStatus: statusInfo.label,
        statusCode: statusInfo.code,
        statusKey: statusInfo.key,
        statusDescription: statusInfo.description,
        recordNumber: recordNumber,
        recordNumberDisplay: recordNumber || '无编号',
        canRefer: statusInfo.allowReferral,
        originalRecord: item
      }
    })

    if (records.value.length === 0) {
      uni.showToast({
        title: '暂无就诊记录',
        icon: 'none'
      })
    } else {
      uni.showToast({
        title: '获取成功',
        icon: 'success'
      })
    }
  } catch (e) {
    console.error('获取就诊记录失败:', e)
    records.value = []
    uni.showToast({
      title: '获取就诊记录失败，请稍后重试',
      icon: 'none',
      duration: 2000
    })
  } finally {
    uni.hideLoading()
  }
}

const changeFilter = (filterValue) => {
  currentFilter.value = filterValue
}

const filteredRecords = computed(() => {
  if (currentFilter.value === 'all') {
    return records.value
  }
  return records.value.filter(record => record.normalizedStatus === currentFilter.value)
})

const viewRecordDetail = (record) => {
  uni.navigateTo({
    url: `/subpkg/profile/records/hospital-record-detail?record=${encodeURIComponent(JSON.stringify(record))}`
  })
}

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
    await loadHospitalRecords()
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
    await loadHospitalRecords()
  }
}

const goToReferralApplication = async (record) => {
  try {
    if (!record || !record.id) {
      console.warn('缺少必要的就诊记录信息')
      uni.showToast({
        title: '就诊记录信息不完整',
        icon: 'none'
      })
      return
    }
    
    let patientName = ''
    try {
      const patientInfo = await ensurePatientCard()
      patientName = patientInfo?.patientName || ''
    } catch (error) {
      console.warn('获取患者信息失败:', error)
    }
    
    const referralData = {
      recordId: record.id,
      patientName: patientName,
      department: record.department,
      doctor: record.doctor,
      visitTime: record.visitTime,
      visitId: record.id,
      originalRecord: record.originalRecord || record
    }
    
    const encodedData = encodeURIComponent(JSON.stringify(referralData))
    
    uni.navigateTo({
      url: `/subpkg/hospital/referral-application?recordData=${encodedData}`,
      success: () => {
        console.log('成功跳转到转诊申请页面')
      },
      fail: (error) => {
        console.error('跳转转诊申请页面失败:', error)
        uni.showToast({
          title: '跳转失败，请重试',
          icon: 'none'
        })
      }
    })
  } catch (error) {
    console.error('处理转诊申请失败:', error)
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    })
  }
}

onMounted(() => {
  loadHospitalRecords()
})
</script>

<style scoped>
.page-bg { 
  min-height: 100vh; 
  background: #f5f7fa; 
  padding: 20rpx;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  visibility: visible;
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

.record-number {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  font-weight: bold;
  font-size: 28rpx;
  color: #333;
}

.record-container {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.record-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.record-card:active {
  background: #f8f9fa;
  transform: scale(0.98);
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
  justify-content: space-between;
  align-items: center;
}

.department {
  font-size: 30rpx;
  font-weight: 600;
  color: #23324b;
}

.status-badge {
  background: #fff3e6;
  color: #ff7a45;
  padding: 4rpx 20rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  font-weight: 600;
}

.register-type {
  font-size: 24rpx;
  color: #666;
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
  min-width: 100rpx;
}

.info-value {
  color: #333;
  font-size: 28rpx;
  flex: 1;
  text-align: right;
}

.info-value.doctor-name {
  font-weight: 600;
  color: #1f2d3d;
}

.info-value.status-text {
  color: #ff7a45;
  font-weight: 600;
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

.referral-wrapper {
  display: flex;
  justify-content: flex-end;
}

.small-referral-btn {
  font-size: 24rpx;
  padding: 8rpx 32rpx;
  border-radius: 20rpx;
  border: none;
  min-width: 160rpx;
  line-height: 1.5;
}

.blue-btn {
  background-color: #4a90e2;
  color: white;
}

.blue-btn:active {
  background-color: #357abd;
}

.cannot-refer-text {
  padding: 8rpx 32rpx;
  font-size: 24rpx;
  color: #999;
  background-color: #f5f5f5;
  border-radius: 20rpx;
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

.empty-icon {
  width: 300rpx;
  height: 200rpx;
  margin-bottom: 30rpx;
}

.empty-text {
  color: #999;
  font-size: 28rpx;
}

.refresh-btn.small {
  padding: 2rpx 32rpx;
  height: 48rpx;
  line-height: 44rpx;

  background-color: white;
  border: 1rpx solid #4a90e2;
  border-radius: 12rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8rpx; /* 保持与原来相同的间距 */
  font-size: 28rpx; /* 字体大小保持不变 */
  color: #4a90e2; /* 文字颜色改为蓝色，与边框一致 */
  width: auto;
  margin: 0;
  transition: all 0.3s ease;
}

.refresh-btn.small:active {
  background-color: rgba(74, 144, 226, 0.1); /* 白色背景下的点击效果 */
  opacity: 0.9;
}

.refresh-btn .refresh-icon {
  display: inline-block;
  transition: transform 0.3s ease;
  font-size: 32rpx;
}

.refresh-btn:active .refresh-icon {
  animation: rotate 0.5s linear;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 分类目录样式 */
.filter-tabs {
  display: flex;
  overflow-x: auto;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  margin-bottom: 10rpx;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none; /* Firefox */
  background-color: #ffffff;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.filter-tabs::-webkit-scrollbar {
  display: none; /* Chrome, Safari */
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

.filter-tab::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 0;
  height: 0;
  background-color: rgba(74, 144, 226, 0.1);
  border-radius: 50%;
  transition: all 0.3s ease;
}

.filter-tab:active::before {
  width: 100%;
  height: 100%;
}

/* 优化记录卡片样式 */
.record-card {
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.record-card:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}
</style>