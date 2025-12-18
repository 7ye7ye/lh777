<template>
  <view class="page-bg">
    <!-- 标题和刷新按钮区域 -->
    <view class="header-section">
      <view class="list-header-wrapper">
        <view class="header-icon">🔄</view>
        <view class="list-header">就诊记录</view>
        <view class="header-badge" v-if="filteredRecords.length > 0">{{ filteredRecords.length }}</view>
      </view>
      <view class="header-actions">
        <button class="refresh-btn small" @click="loadHospitalRecords" :disabled="loading">
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
        :class="{ active: currentFilter.value === tab.value }"
        @click="changeFilter(tab.value)"
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
              v-if="item.canRefer && !item.hasSuccessfulReferral" 
              class="small-referral-btn blue-btn" 
              @click.stop="goToReferralApplication(item)"
            >
              申请转诊
            </button>
            <button 
              v-else-if="item.hasSuccessfulReferral" 
              class="small-referral-btn blue-btn" 
              @click.stop="goToReferralDetail(item)"
            >
              转诊详情
            </button>
            <text v-else-if="item.statusDisplay === '待就诊'" class="cannot-refer-text">未就诊，不能转诊</text>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getRegistrationRecords } from '@/api/registration'
import { ensurePatientCard } from '@/utils/patientHelper'
import { getPatientReferralList } from '@/api/referral'

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
const loading = ref(false)

// 日期范围筛选
const startDate = ref('')
const endDate = ref('')
const showDatePicker = ref(false)

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
// 状态码映射（根据数据库：0-候补；1-已预约；2-已就诊；3-已退号；4-已取消）
const RAW_STATUS_CODE_MAP = new Map([
  ['0', STATUS_DEFINITIONS.pending],   // 候补
  ['1', STATUS_DEFINITIONS.pending],   // 已预约（待就诊）
  ['2', STATUS_DEFINITIONS.completed], // 已就诊（已完成）
  ['3', STATUS_DEFINITIONS.cancelled], // 已退号
  ['4', STATUS_DEFINITIONS.cancelled]  // 已取消
])

const STATUS_KEYWORD_RULES = [
  { regex: /(取消|退号|作废|关闭|失败|拒绝|撤销)/, status: STATUS_DEFINITIONS.cancelled },
  { regex: /(过期|失效)/, status: STATUS_DEFINITIONS.expired },
  { regex: /(完成|成功|已支付|已就诊|诊疗|结束)/, status: STATUS_DEFINITIONS.completed },
  { regex: /(待|未支付|预约|排队|未就诊|确认中)/, status: STATUS_DEFINITIONS.pending }
]

const DAY_IN_MS = 1000 * 60 * 60 * 24

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
		// 只使用visitTimeStr，不使用registerTimeStr作为备选，因为registerTimeStr是挂号时间，不是就诊时间
		const visitDate = parseToDate(visitTimeStr)
		const now = new Date()

		// 优先处理取消状态
		if (rawText && /(取消|退号|作废|关闭|失败|拒绝|撤销)/.test(rawText)) {
			return cloneStatus(STATUS_DEFINITIONS.cancelled)
		}
		
		// 优先处理过期状态
		if (rawText && /(过期|失效)/.test(rawText)) {
			return cloneStatus(STATUS_DEFINITIONS.expired)
		}

		// 如果有就诊时间，优先根据时间判定状态
		if (visitDate) {
			if (now < visitDate) {
				// 未来的就诊，应该是待就诊
				return cloneStatus(STATUS_DEFINITIONS.pending)
			}
			const diffDays = (now.getTime() - visitDate.getTime()) / DAY_IN_MS
			if (diffDays <= 5) {
				return cloneStatus(STATUS_DEFINITIONS.completed)
			}
			return cloneStatus(STATUS_DEFINITIONS.expired)
		}

		// 然后才根据状态码判定
		if (RAW_STATUS_CODE_MAP.has(rawText)) {
			return cloneStatus(RAW_STATUS_CODE_MAP.get(rawText))
		}

		if (rawText && RAW_STATUS_CODE_MAP.has(String(Number(rawText)))) {
			return cloneStatus(RAW_STATUS_CODE_MAP.get(String(Number(rawText))))
		}

		// 最后根据关键字规则判定
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
  if (loading.value) return
  loading.value = true
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
    
    // 获取所有转诊记录，用于检查就诊记录是否已有成功的转诊
    let referralMap = new Map()
    try {
      const referralParams = {
        pageNo: 1,
        pageSize: 1000 // 获取足够多的记录
      }
      const referralRes = await getPatientReferralList(referralParams)
      
      let referralList = []
      if (Array.isArray(referralRes?.result?.records)) {
        referralList = referralRes.result.records
      } else if (Array.isArray(referralRes?.records)) {
        referralList = referralRes.records
      } else if (Array.isArray(referralRes?.data?.records)) {
        referralList = referralRes.data.records
      } else if (Array.isArray(referralRes?.data)) {
        referralList = referralRes.data
      } else if (Array.isArray(referralRes?.result)) {
        referralList = referralRes.result
      } else if (Array.isArray(referralRes)) {
        referralList = referralRes
      }
      
      // 构建转诊记录映射：registrationRecordId -> 转诊记录
      referralList.forEach(ref => {
        const registrationId = ref.registrationRecordId || ref.registration_record_id
        const status = (ref.status || '').toUpperCase()
        // 只记录成功的转诊（已批准）
        if (registrationId && status === 'APPROVED') {
          const recordId = Number(registrationId)
          if (!referralMap.has(recordId)) {
            referralMap.set(recordId, {
              id: ref.id || ref.referralId,
              status: status
            })
          }
        }
      })
    } catch (error) {
      console.warn('获取转诊记录失败:', error)
    }
    
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
      
      // 检查是否已有成功的转诊记录
      const recordId = Number(item.recordId || item.id)
      const referralInfo = referralMap.get(recordId)
      const hasSuccessfulReferral = !!referralInfo
      
      // 如果有成功的转诊记录，强制将状态设置为已完成
      const finalStatusInfo = hasSuccessfulReferral ? STATUS_DEFINITIONS.completed : statusInfo

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
        statusDisplay: finalStatusInfo.label,
        normalizedStatus: finalStatusInfo.label,
        statusCode: finalStatusInfo.code,
        statusKey: finalStatusInfo.key,
        statusDescription: finalStatusInfo.description,
        recordNumber: recordNumber,
        recordNumberDisplay: recordNumber || '无编号',
        canRefer: statusInfo.allowReferral,
        hasSuccessfulReferral: hasSuccessfulReferral,
        referralId: referralInfo?.id || null,
        patientId: item.patientId || item.patient_id || patientId, // 确保使用就诊记录中的patientId
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
    loading.value = false
  }
}

const changeFilter = (filterValue) => {
  currentFilter.value = filterValue
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

// 检查就诊记录是否在日期范围内
const isRecordInDateRange = (record) => {
  if (!startDate.value && !endDate.value) {
    return true // 没有设置日期范围，显示所有记录，包括没有日期信息的记录
  }
  
  // 使用就诊时间作为比较基准
  const recordDateStr = record.registerTime || record.consultationTime || record.createTime || ''
  if (!recordDateStr) {
    return false // 有日期筛选但记录没有日期信息，不显示
  }
  
  const recordDate = parseDate(recordDateStr)
  if (!recordDate) return false
  
  const start = parseDate(startDate.value)
  const end = parseDate(endDate.value)
  
  // 如果只有开始日期，只要记录日期 >= 开始日期即可
  if (startDate.value && !endDate.value) {
    return recordDate >= start
  }
  
  // 如果只有结束日期，只要记录日期 <= 结束日期即可
  if (!startDate.value && endDate.value) {
    return recordDate <= end
  }
  
  // 如果两个日期都有，记录日期必须在范围内
  if (start && end) {
    return recordDate >= start && recordDate <= end
  }
  
  return true
}

// 过滤就诊记录
const filteredRecords = computed(() => {
  return records.value.filter(record => {
    // 状态筛选
    if (currentFilter.value !== 'all' && record.statusDisplay !== currentFilter.value) {
      return false
    }
    
    // 日期范围筛选
    return isRecordInDateRange(record)
  })
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
    
    // 确保使用就诊记录中的patientId，而不是当前登录用户的patientId
    const recordPatientId = record.patientId || record.originalRecord?.patientId || record.originalRecord?.patient_id
    
    let patientName = ''
    let patientInfo = null
    
    // 如果就诊记录中有patientId，使用该patientId获取患者信息
    if (recordPatientId) {
      try {
        patientInfo = await patientApi.getPatientDetail({ patientId: recordPatientId })
        patientName = patientInfo?.patientName || patientInfo?.name || ''
        console.log('使用就诊记录中的patientId获取患者信息:', recordPatientId, patientName)
      } catch (error) {
        console.warn('根据就诊记录patientId获取患者信息失败:', error)
      }
    }
    
    // 如果获取失败，尝试使用当前登录用户的就诊卡
    if (!patientName) {
      try {
        const card = await ensurePatientCard()
        patientName = card?.patientName || card?.name || ''
        console.log('使用当前登录用户的就诊卡信息:', patientName)
      } catch (error) {
        console.warn('获取患者信息失败:', error)
      }
    }
    
    const referralData = {
      recordId: record.id,
      patientName: patientName,
      patientId: recordPatientId, // 传递就诊记录中的patientId
      department: record.department,
      doctor: record.doctor,
      visitTime: record.visitTime,
      visitId: record.id,
      originalRecord: {
        ...(record.originalRecord || record),
        patientId: recordPatientId, // 确保originalRecord中包含正确的patientId
        patient_id: recordPatientId
      }
    }
    
    console.log('转诊申请数据:', referralData)
    
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

// 跳转到转诊详情
const goToReferralDetail = (record) => {
  if (!record || !record.referralId) {
    uni.showToast({
      title: '转诊记录信息不完整',
      icon: 'none'
    })
    return
  }
  
  uni.navigateTo({
    url: `/subpkg/hospital/referral-detail?id=${record.referralId}`,
    fail: (error) => {
      console.error('跳转转诊详情失败:', error)
      uni.showToast({
        title: '跳转失败，请重试',
        icon: 'none'
      })
    }
  })
}

// 事件监听回调函数
const handleRefreshRecords = () => {
  console.log('收到刷新就诊记录事件')
  loadHospitalRecords()
}

onMounted(() => {
  loadHospitalRecords()
  // 监听自动挂号成功事件，刷新就诊记录
  uni.$on('refreshHospitalRecords', handleRefreshRecords)
})

onUnmounted(() => {
  // 移除事件监听，避免内存泄漏
  uni.$off('refreshHospitalRecords', handleRefreshRecords)
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

.refresh-btn.small:active {
  background-color: rgba(255, 255, 255, 0.35);
}

.refresh-btn.small:disabled {
  opacity: 0.5;
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