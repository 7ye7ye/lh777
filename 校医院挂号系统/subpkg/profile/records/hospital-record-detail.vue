<template>
  <view class="detail-page">
    <view class="header-section">
      <text class="page-title">就诊详情</text>
    </view>
    
    <!-- 整合基本信息和患者信息 -->
    <view class="info-card">
      <view class="card-title">就诊基本信息</view>
      <view class="info-content">
        <view class="info-row-full">
          <text class="info-label">科室</text>
          <text class="info-value">{{ departmentName }}</text>
        </view>
        <view class="info-row-full">
          <text class="info-label">医生</text>
          <text class="info-value">{{ doctorName || '-' }}</text>
        </view>
        <view class="info-row-full" v-if="appointmentTimeSlot">
          <text class="info-label">就诊时间段</text>
          <text class="info-value">{{ appointmentTimeSlot }}</text>
        </view>
        <view class="info-row-full" v-if="appointmentTime">
          <text class="info-label">就诊时间</text>
          <text class="info-value">{{ appointmentTime }}</text>
        </view>
        <view class="info-row-full" v-if="record?.visitTime && formatTime(record.visitTime) !== '-'">
          <text class="info-label">实际就诊时间</text>
          <text class="info-value">{{ formatTime(record.visitTime) }}</text>
        </view>
        <view class="info-row-full">
          <text class="info-label">挂号编号</text>
          <text class="info-value">{{ record?.id || '-' }}</text>
        </view>
        <view class="info-row-full">
          <text class="info-label">就诊状态</text>
          <text class="info-value">{{ statusDisplay || '-' }}</text>
        </view>
        <view class="info-row-full">
          <text class="info-label">患者姓名</text>
          <text class="info-value">{{ patientInfo?.name || '' }}</text>
        </view>
        <view class="info-row-full">
          <text class="info-label">就诊卡号</text>
          <text class="info-value">{{ patientInfo?.cardNumber || '' }}</text>
        </view>
        <view class="info-row-full">
          <text class="info-label">联系方式</text>
          <text class="info-value">{{ patientInfo?.phone || '' }}</text>
        </view>
        <view class="info-row-full">
          <text class="info-label">挂号类型</text>
          <text class="info-value">{{ record?.registerType || '-' }}</text>
        </view>
      </view>
    </view>

    <!-- 支付信息卡片 - 简化显示 -->
    <view class="info-card">
      <view class="card-title">支付信息</view>
      <view class="info-content">
        <view class="info-row">
          <view class="info-item">
            <text class="info-label">挂号定价</text>
            <text class="info-value fee">¥{{ paymentInfo?.fee ?? '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">实付金额</text>
            <text class="info-value fee real">¥{{ paymentInfo?.actualPaid ?? paymentInfo?.paidAmount ?? paymentInfo?.fee ?? '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">支付状态</text>
            <text class="info-value status-paid">{{ paymentInfo?.status || '' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 诊断信息卡片 -->
    <view class="info-card">
      <view class="card-title">诊断信息</view>
      <view class="info-content">
        <view class="diagnosis-item">
          <text class="diagnosis-title">初步诊断</text>
          <text class="diagnosis-content">{{ diagnosisInfo?.primary || '' }}</text>
        </view>
        <view class="diagnosis-item">
          <text class="diagnosis-title">处理意见</text>
          <text class="diagnosis-content">{{ diagnosisInfo?.advice || '' }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮区域 -->
    <view class="action-section">
      <button v-if="canRefer && !hasReferral" class="action-btn referral-btn" @click="goToReferral">申请转诊</button>
      <button v-else-if="referralStatus === 'PENDING'" class="action-btn referral-btn" @click="goToReferralStatus">申请中，可查看进度</button>
      <button v-else-if="hasReferral" class="action-btn referral-btn" @click="goToReferralStatus">查看转诊情况</button>
      <text v-else-if="statusDisplay === '已取消'" class="action-btn cannot-refer-btn">无效记录，无法转诊</text>
      <text v-else-if="statusDisplay === '待就诊'" class="action-btn cannot-refer-btn">未就诊，不能转诊</text>
      <text v-else class="action-btn cannot-refer-btn">超过5天，无法转诊</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchPatientCard } from '@/utils/patientHelper'
import { getDepartmentDetail } from '@/api/department'
import { getDoctorDetail } from '@/api/doctor_massage'
import { patientApi } from '@/api/patient'
import { getScheduleDetailById, getRegistrationRecords } from '@/api/registration'
import { getPatientReferralList, getPatientVisitRecords } from '@/api/referral'

// 状态定义（与就诊记录列表页面一致）
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
// 注意：状态码3和4需要结合cancel_time和cancel_reason判断，不能仅凭状态码判定为已取消
const RAW_STATUS_CODE_MAP = new Map([
  ['0', STATUS_DEFINITIONS.pending],   // 候补
  ['1', STATUS_DEFINITIONS.pending],   // 已预约（待就诊）
  ['2', STATUS_DEFINITIONS.completed], // 已就诊（已完成）
  // '3' 和 '4' 不在这里映射，需要检查 cancel_time 和 cancel_reason
])

const STATUS_KEYWORD_RULES = [
  // 注意：取消状态不再通过关键字匹配，只能通过 cancel_time 和 cancel_reason 判断
  { regex: /(过期|失效)/, status: STATUS_DEFINITIONS.expired },
  { regex: /(完成|成功|已支付|已就诊|诊疗|结束)/, status: STATUS_DEFINITIONS.completed },
  { regex: /(待|未支付|预约|排队|未就诊|确认中)/, status: STATUS_DEFINITIONS.pending }
]

const cloneStatus = (statusDefinition) => ({ ...statusDefinition })

// 接收从上一个页面传递的数据
const record = ref(null)
const patientInfo = ref({})
const paymentInfo = ref({})
const diagnosisInfo = ref({})
const canRefer = ref(false)
const hasReferral = ref(false)
const referralStatus = ref(null) // 转诊状态（PENDING、APPROVED等）
const doctorName = ref('')
const departmentName = ref('-')
const appointmentTimeSlot = ref('') // 就诊时间段
const appointmentTime = ref('') // 就诊时间（具体时间点）
const statusDisplay = ref('') // 解析后的状态显示文本

// 转换日期字符串为iOS兼容格式
const convertToIOSCompatibleDate = (dateString) => {
  if (!dateString) return dateString;
  const normalized = String(dateString).trim();
  if (!normalized) return normalized;
  if (normalized.includes('T')) {
    return normalized;
  }
  // 将 "yyyy-MM-dd HH:mm" 或 "yyyy-MM-dd HH:mm:ss" 都转换为斜线格式或 ISO 格式
  if (/^\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}(:\d{2})?$/.test(normalized)) {
    return normalized.replace(/-/g, '/');
  }
  return normalized;
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  
  // 转换为iOS兼容格式
  const compatibleTimeStr = convertToIOSCompatibleDate(timeStr);
  const date = new Date(compatibleTimeStr)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 加载患者信息 - 不使用缓存，直接从服务器获取最新数据
const loadPatientInfo = async () => {
  try {
    const info = await fetchPatientCard()
    if (info) {
      patientInfo.value = {
        name: info.patientName || '患者',
        cardNumber: info.cardNumber || '',
        phone: info.phone || '',
        patientId: info.patientId
      }
    }
  } catch (error) {
    console.error('加载患者信息失败:', error)
  }
}

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

// 根据时间段获取就诊开始时间（小时）
const getTimeSlotStartHour = (timeSlot) => {
  const slotNum = Number(timeSlot)
  if (slotNum === 1) return 8   // 上午 08:00
  if (slotNum === 2) return 14  // 下午 14:00
  if (slotNum === 3) return 18  // 晚上 18:00
  return null
}

// 根据时间段获取完整的时间范围文本
const getTimeSlotRange = (timeSlot) => {
  const slotNum = Number(timeSlot)
  if (slotNum === 1) return '08:00-12:00'  // 上午
  if (slotNum === 2) return '14:00-17:00'  // 下午
  if (slotNum === 3) return '18:00-20:00'  // 晚上
  return ''
}

// 根据时间段获取时段标签
const getTimeSlotLabel = (timeSlot) => {
  const slotNum = Number(timeSlot)
  if (slotNum === 1) return '上午'
  if (slotNum === 2) return '下午'
  if (slotNum === 3) return '晚上'
  return ''
}

// 构建完整的就诊时间段显示（包含时间范围）
const buildAppointmentTimeSlot = (scheduleDateStr, timeSlot) => {
  if (!scheduleDateStr || !timeSlot) return ''
  
  const timeSlotLabel = getTimeSlotLabel(timeSlot)
  const timeSlotRange = getTimeSlotRange(timeSlot)
  
  if (!timeSlotLabel || !timeSlotRange) return ''
  
  // 格式化日期
  const dateStr = String(scheduleDateStr).substring(0, 10)
  const dateParts = dateStr.split('-')
  
  let formattedDate = dateStr
  if (dateParts.length === 3) {
    formattedDate = `${dateParts[0]}年${dateParts[1]}月${dateParts[2]}日`
  }
  
  // 返回完整格式：日期 + 时段 + 时间范围
  return `${formattedDate} ${timeSlotLabel} (${timeSlotRange})`
}

// 根据排班日期和时段构建就诊时间（返回Date对象）
const buildAppointmentDateTime = (scheduleDateStr, timeSlot) => {
  if (!scheduleDateStr || !timeSlot) return null
  
  const scheduleDate = parseToDate(scheduleDateStr)
  if (!scheduleDate) return null
  
  const startHour = getTimeSlotStartHour(timeSlot)
  if (startHour === null) return null
  
  const appointmentTime = new Date(scheduleDate)
  appointmentTime.setHours(startHour, 0, 0, 0)
  return appointmentTime
}

// 构建就诊时间显示文本（具体时间点：日期 + 时段开始时间）
const buildAppointmentTimeDisplay = (scheduleDateStr, timeSlot) => {
  if (!scheduleDateStr || !timeSlot) return ''
  
  const appointmentDate = buildAppointmentDateTime(scheduleDateStr, timeSlot)
  if (!appointmentDate) return ''
  
  // 格式化日期时间
  const year = appointmentDate.getFullYear()
  const month = (appointmentDate.getMonth() + 1).toString().padStart(2, '0')
  const day = appointmentDate.getDate().toString().padStart(2, '0')
  const hours = appointmentDate.getHours().toString().padStart(2, '0')
  const minutes = appointmentDate.getMinutes().toString().padStart(2, '0')
  
  // 返回格式：YYYY年MM月DD日 HH:mm
  return `${year}年${month}月${day}日 ${hours}:${minutes}`
}

// 解析状态信息（与就诊记录列表页面一致）
const resolveStatusInfo = (rawStatusValue, visitTimeStr, registerTimeStr, scheduleDateStr = null, timeSlot = null, cancelTime = null, cancelReason = null) => {
  const rawText = String(rawStatusValue ?? '').trim()
  const now = new Date()

  // 优先检查是否取消（取消状态优先级最高）
  // 只有 cancel_time 和 cancel_reason 都有合理数值时，才判定为已取消
  const hasCancelTime = cancelTime && String(cancelTime).trim() !== ''
  const hasCancelReason = cancelReason && String(cancelReason).trim() !== ''
  
  if (hasCancelTime && hasCancelReason) {
    return cloneStatus(STATUS_DEFINITIONS.cancelled)
  }

  // 优先检查是否有实际就诊时间
    // 这个检查要在状态码映射之前，确保有实际就诊时间的记录不会被错误判定
    const visitDate = parseToDate(visitTimeStr)
    if (visitDate) {
      // 检查就诊时间是否在未来
      if (now < visitDate) {
        // 未来的就诊时间，应判定为待就诊
        return cloneStatus(STATUS_DEFINITIONS.pending)
      }
      
      // 过去的就诊时间，才判定为已完成或已过期
      // 判断是否超过5天
      const diffDays = (now.getTime() - visitDate.getTime()) / DAY_IN_MS
      if (diffDays <= 5) {
        return cloneStatus(STATUS_DEFINITIONS.completed)
      }
      // 超过5天 → 已过期
      return cloneStatus(STATUS_DEFINITIONS.expired)
    }

  // 如果状态码是3或4，但没有取消时间和原因，不判定为已取消
  // 状态码3（已退号）和4（已取消）需要同时有cancel_time和cancel_reason才能判定为已取消
  if (rawText === '3' || rawText === '4' || rawText === 3 || rawText === 4) {
    // 如果没有取消时间和原因，根据其他信息判断状态
    // 继续后续判断逻辑
  } else {
    // 对于其他状态码，使用状态码映射
    if (RAW_STATUS_CODE_MAP.has(rawText)) {
      return cloneStatus(RAW_STATUS_CODE_MAP.get(rawText))
    }

    if (rawText && RAW_STATUS_CODE_MAP.has(String(Number(rawText)))) {
      return cloneStatus(RAW_STATUS_CODE_MAP.get(String(Number(rawText))))
    }
  }

  // 如果没有取消时间和原因，再检查状态文本关键字（但不包括取消相关的关键字）
  if (rawText) {
    if (/(过期|失效)/.test(rawText)) {
      return cloneStatus(STATUS_DEFINITIONS.expired)
    }
  }

  // 如果没有实际就诊时间，使用排班时间判断状态（根据用户要求：超过预约挂号的排班时间的自动认定为已就诊）
  if (scheduleDateStr && timeSlot) {
    const appointmentTime = buildAppointmentDateTime(scheduleDateStr, timeSlot)
    if (appointmentTime) {
      // 如果当前时间还没到排班时间 → 待就诊
      if (now < appointmentTime) {
        return cloneStatus(STATUS_DEFINITIONS.pending)
      }
      // 如果当前时间已经超过排班时间 → 已就诊（已完成）
      // 判断是否超过5天
      const diffDays = (now.getTime() - appointmentTime.getTime()) / DAY_IN_MS
      if (diffDays <= 5) {
        return cloneStatus(STATUS_DEFINITIONS.completed)
      }
      // 超过5天 → 已过期
      return cloneStatus(STATUS_DEFINITIONS.expired)
    }
  }

  // 根据关键字匹配状态
  if (rawText) {
    for (const rule of STATUS_KEYWORD_RULES) {
      if (rule.regex.test(rawText)) {
        return cloneStatus(rule.status)
      }
    }
  }

  // 默认：如果没有就诊时间和排班信息，默认视为待就诊
  return cloneStatus(STATUS_DEFINITIONS.pending)
}

// 检查是否可以转诊（统一判断逻辑，与就诊记录页面一致）
const checkCanRefer = (rawStatusValue, visitTimeStr, registerTimeStr, scheduleDateStr = null, timeSlot = null, cancelTime = null, cancelReason = null) => {
  const now = new Date()
  
  // 优先检查是否取消（取消状态不能转诊）
  const hasCancelTime = cancelTime && String(cancelTime).trim() !== ''
  const hasCancelReason = cancelReason && String(cancelReason).trim() !== ''
  if (hasCancelTime && hasCancelReason) {
    return false // 取消的记录不能转诊
  }
  
  // 优先使用排班时间判断（与状态判断逻辑一致）
  if (scheduleDateStr && timeSlot) {
    const appointmentTime = buildAppointmentDateTime(scheduleDateStr, timeSlot)
    if (appointmentTime) {
      // 如果还没到排班时间，不能转诊（待就诊状态）
      if (now < appointmentTime) {
        return false
      }
      // 如果已过排班时间，判断是否在5天内
      const diffDays = (now.getTime() - appointmentTime.getTime()) / DAY_IN_MS
      return diffDays <= 5
    }
  }
  
      // 如果没有排班信息，使用实际就诊时间判断（作为备选）
  const visitDate = parseToDate(visitTimeStr)
  if (visitDate) {
    // 如果当前时间还没到就诊时间，不能转诊（待就诊状态）
    if (now < visitDate) {
      return false
    }
    // 如果已过就诊时间，判断是否在5天内
    const diffDays = (now.getTime() - visitDate.getTime()) / DAY_IN_MS
    return diffDays <= 5
  }
  
  // 默认不能转诊
  return false
}

const getNumberSafe = (val) => {
  if (val === null || val === undefined || val === '') return null
  const num = Number(val)
  return isNaN(num) ? null : num
}

const pickAmount = (item) => {
  if (!item) return { listPrice: null, paidAmount: null }
  const candidatesList = [
    item.priceOriginal,
    item.price,
    item.fee,
    item.cost,
    item.registerFee,
    item.totalFee,
  ]
  const candidatesPaid = [
    item.actualPayAmount,
    item.actualPayment,
    item.payAmount,
    item.paymentAmount,
    item.paidAmount,
    item.actualPrice,
    item.pricePaid,
    item.paidFee,
    item.realFee,
    item.realPayment,
  ]
  const listPrice = candidatesList.map(getNumberSafe).find((v) => v !== null) ?? null
  const paidAmount = candidatesPaid.map(getNumberSafe).find((v) => v !== null) ?? null
  return { listPrice, paidAmount }
}

const fetchBackendPayment = async (recordId) => {
  const pid = patientInfo.value?.patientId
  if (!pid || !recordId) return

  const mergePayment = (match) => {
    if (!match) return
    const { listPrice, paidAmount } = pickAmount(match)
    const resolvedPaid =
      paidAmount ??
      paymentInfo.value.actualPaid ??
      paymentInfo.value.paidAmount ??
      ((paymentInfo.value.status || match.paymentStatus || match.status || '').includes('支付') ? (listPrice ?? paymentInfo.value.fee ?? null) : null)

    paymentInfo.value = {
      ...paymentInfo.value,
      fee: listPrice ?? paymentInfo.value.fee,
      actualPaid: resolvedPaid,
      paidAmount: resolvedPaid,
      status: paymentInfo.value.status || match.paymentStatus || match.status || '',
      paymentTime: paymentInfo.value.paymentTime || match.paymentTime || match.payTime || match.visitTime || '',
      method: paymentInfo.value.method || match.paymentMethod || match.payMethod || ''
    }
  }

  const matchById = (list) => {
    if (!Array.isArray(list)) return null
    return list.find((item) => {
      const ids = [
        item.id,
        item.registrationId,
        item.registration_id,
        item.recordId,
        item.registrationRecordId,
        item.registration_record_id,
      ].map((v) => Number(v))
      return ids.includes(Number(recordId))
    })
  }

  // 优先走 /applet/registration/records
  try {
    const regRes = await getRegistrationRecords(pid)
    const regList = Array.isArray(regRes?.result) ? regRes.result
      : Array.isArray(regRes?.data) ? regRes.data
      : Array.isArray(regRes) ? regRes
      : []
    const regMatch = matchById(regList)
    if (regMatch) {
      mergePayment(regMatch)
      return
    }
  } catch (err) {
    console.warn('获取挂号记录支付信息失败:', err)
  }

  // 兼容旧接口 /patient/registration/history，若不存在则忽略
  try {
    const res = await getPatientVisitRecords({
      patientId: pid,
      pageNo: 1,
      pageSize: 200
    })
    const list = Array.isArray(res?.records) ? res.records
      : Array.isArray(res?.result?.records) ? res.result.records
      : Array.isArray(res?.data?.records) ? res.data.records
      : Array.isArray(res) ? res
      : []

    const match = matchById(list)
    mergePayment(match)
  } catch (error) {
    if (String(error?.message || '').includes('No static resource')) {
      console.info('后台不支持 patient/registration/history，已跳过')
      return
    }
    console.warn('获取后台支付信息失败:', error)
  }
}

// 加载就诊相关信息
const loadRecordDetails = async () => {
  try {
    // 在UniApp中获取页面参数
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    const options = currentPage.options || {};
    
    let routeData = null;
    // 尝试从options中获取record参数
    if (options.record) {
      try {
        // 先尝试解码可能已经被URL编码的参数
        let recordStr = options.record;
        // 检查是否包含URL编码特征
        if (recordStr.includes('%22') || recordStr.includes('%7B') || recordStr.includes('%')) {
          try {
            recordStr = decodeURIComponent(recordStr);
            console.log('已解码参数:', recordStr);
          } catch (decodeError) {
            console.warn('参数解码失败，使用原始字符串:', decodeError);
          }
        }
        // 再尝试解析JSON
        routeData = JSON.parse(recordStr);
      } catch (parseError) {
        console.warn('无法解析record参数:', parseError);
        // 检查是否已经是对象
        if (typeof options.record === 'object') {
          routeData = options.record;
        } else {
          // 尝试其他可能的参数名
          if (options.recordData) {
            try {
              let dataStr = options.recordData;
              if (dataStr.includes('%')) {
                dataStr = decodeURIComponent(dataStr);
              }
              routeData = JSON.parse(dataStr);
            } catch (e) {
              console.warn('无法解析recordData参数:', e);
            }
          }
        }
      }
    }
    
    if (routeData) {
      record.value = routeData;
      
      // 优先使用传递过来的状态显示（从列表页已经计算好的状态）
      if (routeData.statusDisplay || routeData.normalizedStatus) {
        statusDisplay.value = routeData.statusDisplay || routeData.normalizedStatus || '待就诊'
      }
      
      // 优先使用传递过来的就诊时间段（如果已计算好）
      if (routeData.appointmentTimeSlot) {
        appointmentTimeSlot.value = routeData.appointmentTimeSlot
      }
      
      // 优先使用传递过来的就诊时间（如果已计算好）
      if (routeData.appointmentTime) {
        appointmentTime.value = routeData.appointmentTime
      }
      
      // 优先使用传递过来的转诊状态
      if (routeData.canRefer !== undefined && routeData.canRefer !== null) {
        canRefer.value = routeData.canRefer
      }
      
      // 获取排班信息用于构建就诊时间段和判断转诊状态
      const scheduleId = routeData.scheduleId || routeData.schedule_id || routeData.originalRecord?.scheduleId || routeData.originalRecord?.schedule_id || null
      const scheduleDateStr = routeData.scheduleDateStr || routeData.schedule_date || routeData.scheduleDate || routeData.originalRecord?.scheduleDate || routeData.originalRecord?.schedule_date || null
      const timeSlot = routeData.timeSlotValue || routeData.timeSlot || routeData.time_slot || routeData.originalRecord?.timeSlot || routeData.originalRecord?.time_slot || null
      
      // 获取取消相关字段
      const cancelTime = routeData.cancelTime || routeData.cancel_time || routeData.originalRecord?.cancelTime || routeData.originalRecord?.cancel_time || null
      const cancelReason = routeData.cancelReason || routeData.cancel_reason || routeData.originalRecord?.cancelReason || routeData.originalRecord?.cancel_reason || null
      
      // 如果有 scheduleId，查询排班详情以获取就诊时间
      if (scheduleId) {
        try {
          console.log('查询排班详情，scheduleId:', scheduleId)
          const scheduleRes = await getScheduleDetailById(scheduleId)
          console.log('排班详情原始响应:', scheduleRes)
          
          // 处理不同的响应格式
          let schedule = null
          if (scheduleRes) {
            // 如果 scheduleRes 本身就是排班对象（有 schedule_date 字段）
            if (scheduleRes.schedule_date || scheduleRes.scheduleDate) {
              schedule = scheduleRes
            }
            // 如果 scheduleRes 有 result 字段
            else if (scheduleRes.result) {
              schedule = scheduleRes.result
            }
            // 如果 scheduleRes 有 data 字段
            else if (scheduleRes.data) {
              schedule = scheduleRes.data
            }
            // 否则直接使用 scheduleRes
            else {
              schedule = scheduleRes
            }
          }
          
          console.log('排班详情提取后:', schedule)
          
          if (schedule) {
            const fetchedScheduleDateStr = schedule.schedule_date || schedule.scheduleDate || schedule.scheduleDateStr || scheduleDateStr
            const fetchedTimeSlot = schedule.time_slot || schedule.timeSlot || schedule.timeSlotValue || timeSlot
            
            console.log('提取的排班信息:', { fetchedScheduleDateStr, fetchedTimeSlot })
            
            // 构建就诊时间段显示（如果还没有）- 使用完整的时间范围
            if (!appointmentTimeSlot.value && fetchedScheduleDateStr && fetchedTimeSlot) {
              appointmentTimeSlot.value = buildAppointmentTimeSlot(fetchedScheduleDateStr, fetchedTimeSlot)
              console.log('设置就诊时间段:', appointmentTimeSlot.value)
            }
            
            // 构建就诊时间（具体时间点）- 优先使用查询到的排班信息
            if (!appointmentTime.value && fetchedScheduleDateStr && fetchedTimeSlot) {
              appointmentTime.value = buildAppointmentTimeDisplay(fetchedScheduleDateStr, fetchedTimeSlot)
              console.log('设置就诊时间:', appointmentTime.value)
            }
            
            // 解析状态信息（如果没有从列表页传递过来）
            if (!statusDisplay.value) {
              const finalScheduleDateStr = fetchedScheduleDateStr || scheduleDateStr
              const finalTimeSlot = fetchedTimeSlot || timeSlot
              
              const statusInfo = resolveStatusInfo(
                routeData.status,
                routeData.visitTime,
                routeData.registerTime,
                finalScheduleDateStr,
                finalTimeSlot,
                cancelTime,
                cancelReason
              )
              statusDisplay.value = statusInfo.label || '待就诊'
              
              // 判断转诊状态（如果没有从路由数据传递）
              if (routeData.canRefer === undefined || routeData.canRefer === null) {
                canRefer.value = checkCanRefer(
                  routeData.status,
                  routeData.visitTime,
                  routeData.registerTime,
                  finalScheduleDateStr,
                  finalTimeSlot,
                  cancelTime,
                  cancelReason
                )
              }
            }
          } else {
            console.warn('排班详情为空或缺少schedule_date字段')
          }
        } catch (error) {
          console.warn('查询排班详情失败:', error)
          // 如果查询失败且还没有状态，使用现有信息判断
          if (!statusDisplay.value) {
            const statusInfo = resolveStatusInfo(
              routeData.status,
              routeData.visitTime,
              routeData.registerTime,
              scheduleDateStr,
              timeSlot,
              cancelTime,
              cancelReason
            )
            statusDisplay.value = statusInfo.label || '待就诊'
            
            if (routeData.canRefer === undefined || routeData.canRefer === null) {
              canRefer.value = checkCanRefer(routeData.status, routeData.visitTime, routeData.registerTime, scheduleDateStr, timeSlot, cancelTime, cancelReason)
            }
          }
        }
      }
      
      // 如果没有查询到时间段，使用现有排班信息构建 - 使用完整的时间范围
      if (!appointmentTimeSlot.value && scheduleDateStr && timeSlot) {
        appointmentTimeSlot.value = buildAppointmentTimeSlot(scheduleDateStr, timeSlot)
      }
      
      // 如果没有查询到就诊时间，使用现有排班信息构建
      if (!appointmentTime.value && scheduleDateStr && timeSlot) {
        appointmentTime.value = buildAppointmentTimeDisplay(scheduleDateStr, timeSlot)
      }
      
      // 如果还没有解析状态（说明前面都没有设置），使用默认逻辑解析
      if (!statusDisplay.value) {
        const finalScheduleDateStr = scheduleDateStr || routeData.scheduleDateStr || routeData.originalRecord?.scheduleDate
        const finalTimeSlot = timeSlot || routeData.timeSlotValue || routeData.originalRecord?.timeSlot
        const statusInfo = resolveStatusInfo(
          routeData.status,
          routeData.visitTime,
          routeData.registerTime,
          finalScheduleDateStr,
          finalTimeSlot,
          cancelTime,
          cancelReason
        )
        statusDisplay.value = statusInfo.label || '待就诊'
      }
      
      // 检查是否可以转诊（统一判断逻辑，优先使用传递过来的canRefer状态，否则重新计算）
      if (routeData.canRefer === undefined || routeData.canRefer === null) {
        if (canRefer.value === undefined || canRefer.value === null) {
          const finalScheduleDateStr = scheduleDateStr || routeData.scheduleDateStr || routeData.originalRecord?.scheduleDate
          const finalTimeSlot = timeSlot || routeData.timeSlotValue || routeData.originalRecord?.timeSlot
          canRefer.value = checkCanRefer(routeData.status, routeData.visitTime, routeData.registerTime, finalScheduleDateStr, finalTimeSlot, cancelTime, cancelReason)
        }
      }
      
      // 优先使用传递过来的转诊状态
      if (routeData.hasReferral !== undefined && routeData.hasReferral !== null) {
        hasReferral.value = routeData.hasReferral
      }
      if (routeData.referralStatus) {
        referralStatus.value = (routeData.referralStatus || '').toUpperCase()
      }
      if (routeData.referralId) {
        record.value.referralId = routeData.referralId
      }
      
      // 如果已经传递了转诊状态，就不需要再查询了
      // 否则检查是否已申请过转诊
      if (!hasReferral.value) {
        await checkReferralStatus()
      }
      
      // 初始化科室和医生信息
      let doctorId = null;
      let departmentId = null;
      
      // 设置医生和科室名称，确保显示的是名称而非ID
      // 从原始记录中获取更完整的信息
      if (routeData.originalRecord) {
        // 从originalRecord中获取patientId，根据patientId查询正确的患者信息
        const recordPatientId = routeData.originalRecord.patientId || routeData.originalRecord.patient_id
        if (recordPatientId) {
          // 根据就诊记录的patientId查询该就诊人的就诊卡信息
          try {
            const cardData = await patientApi.getCard({ patientId: recordPatientId })
            if (cardData && cardData.patientId) {
              patientInfo.value = {
                name: cardData.patientName || cardData.name || '',
                cardNumber: cardData.cardNumber || cardData.outpatientNumber || '',
                phone: cardData.phone || '',
                patientId: cardData.patientId,
                id: cardData.patientId // 同时设置id字段，以便兼容
              }
            }
          } catch (error) {
            console.warn('根据patientId获取就诊卡信息失败:', error)
            // 如果获取失败，使用传递的patientName作为备选
            if (routeData.patientName) {
              patientInfo.value = {
                ...patientInfo.value,
                name: routeData.patientName
              }
            }
          }
        } else if (routeData.patientName) {
          // 如果没有patientId，使用传递的patientName
          patientInfo.value = {
            ...patientInfo.value,
            name: routeData.patientName
          }
        }
        
        // 获取医生ID
        doctorId = routeData.originalRecord.doctorId || routeData.doctorId;
        
        // 获取科室ID
        departmentId = routeData.originalRecord.departmentId || routeData.departmentId;
        
        // 处理医生信息
        let doctorData = {
          ...routeData,
          doctorId: doctorId,
          // 从routeData中尝试获取医生名称
          doctorName: routeData.doctorName || ''
        };
        doctorName.value = getDoctorName(doctorData);
        
        // 处理科室信息
        let departmentData = {
          ...routeData,
          department: routeData.department || '',
          departmentId: departmentId,
          departmentName: routeData.departmentName || ''
        };
        departmentName.value = getDepartmentName(departmentData);
      } else {
        // 如果没有originalRecord，使用当前routeData
        doctorId = routeData.doctorId;
        departmentId = routeData.departmentId || (typeof routeData.department === 'number' ? routeData.department : null);
        
        doctorName.value = getDoctorName(routeData);
        departmentName.value = getDepartmentName(routeData);
      }
      
      // 使用真实支付信息，从路由数据和originalRecord中获取
      const normalizePaymentStatus = (value) => {
        if (value === undefined || value === null || value === '') {
          return '未支付'
        }
        const normalized = String(value).toLowerCase()
        const paidKeywords = ['paid', 'success', 'completed', '已支付', '已完成', '1', 'true']
        return paidKeywords.some(keyword => normalized.includes(keyword)) ? '已支付' : '未支付'
      }

      const rawStatus = routeData.paymentStatus || routeData.status || routeData.paymentInfo?.status || ''
      paymentInfo.value = {
        fee: routeData.fee || 
             routeData.paymentFee || 
             routeData.paymentInfo?.fee || 
             routeData.cost || 
             routeData.price || 
             (routeData.originalRecord?.priceOriginal || 0) || // 从originalRecord获取原始价格
             (routeData.originalRecord?.actualPrice || 0), // 从originalRecord获取实际价格
        status: normalizePaymentStatus(rawStatus),
        paymentTime: routeData.paymentTime || routeData.visitTime || '',
        method: routeData.paymentMethod || routeData.paymentInfo?.method || ''
      };
      if (routeData.id) {
        await fetchBackendPayment(routeData.id)
      }
      
      // 使用真实诊断信息，从路由数据中获取
      diagnosisInfo.value = {
        primary: routeData.diagnosis || routeData.diagnosisInfo?.primary || routeData.primaryDiagnosis || '',
        advice: routeData.advice || routeData.diagnosisInfo?.advice || routeData.treatmentAdvice || ''
      };
      
      // 特殊处理：增强科室信息提取，从更多可能的来源获取
      if (!departmentName.value || departmentName.value === '-' || departmentName.value === '暂无科室信息') {
        // 尝试从多个可能的字段获取科室信息
        const possibleDeptSources = [
          // 直接字段
          routeData.department,
          routeData.departmentName,
          routeData.deptName,
          routeData.section,
          routeData.sectionName,
          // 嵌套对象
          routeData.department?.name,
          routeData.department?.departmentName,
          // 原始记录
          routeData.originalRecord?.department,
          routeData.originalRecord?.departmentName,
          routeData.originalRecord?.deptName,
          routeData.originalRecord?.section,
          // 原始记录嵌套对象
          routeData.originalRecord?.department?.name,
          // 数据字段
          routeData.data?.department,
          routeData.data?.departmentName
        ];
        
        for (const source of possibleDeptSources) {
          if (source && typeof source === 'string') {
            const trimmedSource = source.trim();
            if (trimmedSource && !trimmedSource.toLowerCase().includes('未知科室') && !trimmedSource.toLowerCase().includes('unknown')) {
              departmentName.value = trimmedSource;
              console.log('从备用字段找到科室信息:', trimmedSource);
              break;
            }
          }
        }
      }
      
      // 特殊处理：如果医生信息为空，尝试从更多可能的字段获取
      if (doctorName.value === '暂无医生信息') {
        // 尝试从多个可能的字段获取医生信息
        const possibleDoctorFields = [
          routeData.doctor,
          routeData.doctorName,
          routeData.name,
          routeData.originalRecord?.doctor,
          routeData.originalRecord?.doctorName,
          routeData.originalRecord?.name
        ];
        
        for (const field of possibleDoctorFields) {
          if (field && typeof field === 'string' && !field.includes('未知医生')) {
            doctorName.value = field;
            break;
          }
        }
      }
      
      // 如果有科室ID，通过API获取科室详情
      if (departmentId) {
        const departmentInfo = await fetchDepartmentInfo(departmentId);
        if (departmentInfo && departmentInfo.deptName) {
          console.log('通过API更新科室名称:', departmentInfo.deptName);
          departmentName.value = departmentInfo.deptName;
        } else {
          // API调用失败或未返回有效数据时，尝试从本地数据获取
          console.log('API未返回科室信息，尝试从本地数据获取');
          
          // 更全面地提取可能的科室ID字段
          const possibleDeptIds = [
            routeData.departmentId,
            routeData.deptId,
            routeData.department,
            routeData.originalRecord?.departmentId,
            routeData.originalRecord?.deptId,
            routeData.originalRecord?.department
          ];
          
          // 尝试其他可能的科室ID
          for (const id of possibleDeptIds) {
            if (id && id !== departmentId) {
              const altDeptInfo = await fetchDepartmentInfo(id);
              if (altDeptInfo && altDeptInfo.deptName) {
                console.log('通过备用科室ID更新科室名称:', altDeptInfo.deptName);
                departmentName.value = altDeptInfo.deptName;
                break;
              }
            }
          }
        }
      }
      
      // 如果有医生ID，通过API获取医生详情
      if (doctorId) {
        const doctorInfo = await fetchDoctorInfo(doctorId);
        if (doctorInfo) {
          if (doctorInfo.doctorName) {
            console.log('通过API更新医生名称:', doctorInfo.doctorName);
            doctorName.value = doctorInfo.doctorName;
          }
          if (!departmentName.value || departmentName.value === '-' || departmentName.value === '暂无科室信息') {
            const deptFromDoctor = doctorInfo.deptName || doctorInfo.department || doctorInfo.section
            if (deptFromDoctor) {
              console.log('通过医生详情补充科室名称:', deptFromDoctor);
              departmentName.value = deptFromDoctor
            }
          }
        } else {
          // API调用失败或未返回有效数据时，尝试从本地数据获取
          console.log('API未返回医生信息，尝试从本地数据获取');
          
          // 更全面地提取可能的医生ID字段
          const possibleDoctorIds = [
            routeData.doctorId,
            routeData.doctor,
            routeData.originalRecord?.doctorId,
            routeData.originalRecord?.doctor
          ];
          
          // 尝试其他可能的医生ID
          for (const id of possibleDoctorIds) {
            if (id && id !== doctorId) {
              const altDoctorInfo = await fetchDoctorInfo(id);
              if (altDoctorInfo && altDoctorInfo.doctorName) {
                console.log('通过备用医生ID更新医生名称:', altDoctorInfo.doctorName);
                doctorName.value = altDoctorInfo.doctorName;
                break;
              }
            }
          }
        }
      }
    }
  } catch (error) {
    console.error('加载就诊详情失败:', error);
    // 只设置空对象，不使用模拟数据
    record.value = {};
    doctorName.value = '暂无医生信息';
    departmentName.value = '暂无科室信息';
    paymentInfo.value = {};
    diagnosisInfo.value = {};
    
    // 确保状态显示有默认值
    if (!statusDisplay.value) {
      statusDisplay.value = '待就诊'
    }
    
    // 向用户显示错误提示
    uni.showToast({
      title: '加载就诊详情失败，请稍后重试',
      icon: 'none'
    });
  }
};

// 通过API获取科室详情
const fetchDepartmentInfo = async (deptId) => {
  if (!deptId) {
    console.warn('科室ID不存在');
    return null;
  }
  
  try {
    const response = await getDepartmentDetail(deptId);
    console.log('科室详情响应:', response);
    
    // 处理不同的响应格式
    let departmentData = response;
    if (response && response.data) {
      departmentData = response.data;
    } else if (response && response.result) {
      departmentData = response.result;
    }
    
    // 更全面地提取科室名称，考虑更多可能的字段名
    if (departmentData) {
      const deptName = departmentData.deptName || 
                      departmentData.name || 
                      departmentData.departmentName || 
                      departmentData.department || '';
      
      if (deptName && !deptName.includes('未知科室')) {
        return {
          deptId: departmentData.deptId || departmentData.id || deptId,
          deptName: deptName
        };
      }
    }
    return null;
  } catch (error) {
    console.error('获取科室详情失败:', error);
    return null;
  }
};

// 通过API获取医生详情
const fetchDoctorInfo = async (doctorId) => {
  if (!doctorId) {
    console.warn('医生ID不存在');
    return null;
  }
  
  try {
    const response = await getDoctorDetail(doctorId);
    console.log('医生详情响应:', response);
    
    // 处理不同的响应格式
    let doctorData = response;
    if (response && response.data) {
      doctorData = response.data;
    } else if (response && response.result) {
      doctorData = response.result;
    }
    
    // 更全面地提取医生信息，考虑更多可能的字段名
    if (doctorData) {
      const doctorName = doctorData.doctorName || 
                        doctorData.name || 
                        doctorData.doctor || '';
      
      if (doctorName && !doctorName.includes('未知医生')) {
        return {
          doctorId: doctorData.doctorId || doctorData.id || doctorId,
          doctorName: doctorName,
          title: doctorData.title || doctorData.position || '',
          specialty: doctorData.specialty || doctorData.department || ''
        };
      }
    }
    return null;
  } catch (error) {
    console.error('获取医生详情失败:', error);
    return null;
  }
};

// 获取科室名称 - 增强版，支持更多字段和更复杂的数据结构
const getDepartmentName = (recordData) => {
  if (!recordData) return '-';
  
  // 更全面地尝试各种可能的科室名字段
  let deptName = null;
  
  // 检查是否是完整记录对象
  if (typeof recordData === 'object') {
    // 尝试从多个可能的字段获取科室名称
    const departmentFields = [
      'departmentName', 'deptName', 'department', 'name', 
      'department_name', 'dept_name', 'section', 'sectionName'
    ];
    
    // 先从顶层字段查找
    for (const field of departmentFields) {
      if (recordData[field] && typeof recordData[field] === 'string' && recordData[field].trim()) {
        deptName = recordData[field];
        break;
      }
    }
    
    // 检查是否有嵌套的department对象
    if (!deptName && recordData.department && typeof recordData.department === 'object') {
      for (const field of departmentFields) {
        if (recordData.department[field] && typeof recordData.department[field] === 'string' && recordData.department[field].trim()) {
          deptName = recordData.department[field];
          break;
        }
      }
    }
    
    // 检查原始记录中是否有科室信息
    if (!deptName && recordData.originalRecord && typeof recordData.originalRecord === 'object') {
      for (const field of departmentFields) {
        if (recordData.originalRecord[field] && typeof recordData.originalRecord[field] === 'string' && recordData.originalRecord[field].trim()) {
          deptName = recordData.originalRecord[field];
          break;
        }
      }
    }
    
    // 检查嵌套的data字段
    if (!deptName && recordData.data && typeof recordData.data === 'object') {
      for (const field of departmentFields) {
        if (recordData.data[field] && typeof recordData.data[field] === 'string' && recordData.data[field].trim()) {
          deptName = recordData.data[field];
          break;
        }
      }
    }
  } else if (typeof recordData === 'string') {
    // 如果传入的是字符串，假设是科室名称
    if (recordData.trim() && !recordData.includes('未知科室')) {
      return recordData.trim();
    }
  }
  
  // 如果已找到名称，返回名称（确保不是'未知科室'）
  if (deptName && typeof deptName === 'string') {
    const trimmedName = deptName.trim();
    if (trimmedName && !trimmedName.toLowerCase().includes('未知科室') && !trimmedName.toLowerCase().includes('unknown')) {
      return trimmedName;
    }
  }
  
  // 特殊情况：如果传入的是数字ID，返回科室ID
  if (typeof recordData === 'number') {
    return `科室${recordData}`;
  }
  
  // 默认返回
  return '-';
};

// 获取医生名称
const getDoctorName = (recordData) => {
  if (!recordData) return '暂无医生信息';
  
  // 更全面地尝试各种可能的医生名字段
  let doctorName = null;
  
  // 检查是否是完整记录对象
  if (typeof recordData === 'object') {
    // 尝试从多个可能的字段获取医生名称
    doctorName = recordData.doctorName || 
                 recordData.name || 
                 recordData.doctor || 
                 null;
    
    // 检查是否有嵌套的doctor对象
    if (recordData.doctor && typeof recordData.doctor === 'object') {
      doctorName = recordData.doctor.name || 
                 recordData.doctor.doctorName || 
                 recordData.doctor.doctor || 
                 doctorName;
    }
    
    // 检查原始记录中是否有医生信息
    if (recordData.originalRecord && typeof recordData.originalRecord === 'object') {
      doctorName = recordData.originalRecord.doctorName || 
                 recordData.originalRecord.name || 
                 recordData.originalRecord.doctor || 
                 doctorName;
    }
  } else if (typeof recordData === 'string') {
    // 如果传入的是字符串，假设是医生名称
    if (!recordData.includes('未知医生')) {
      return recordData;
    }
  }
  
  // 如果已找到名称，返回名称（确保不是'未知医生'）
  if (doctorName && typeof doctorName === 'string' && !doctorName.includes('未知医生')) {
    return doctorName;
  }
  
  // 特殊情况：如果传入的是数字ID，返回医生ID
  if (typeof recordData === 'number') {
    return `医生${recordData}`;
  }
  
  // 默认返回
  return '暂无医生信息';
};

// 获取就诊时段
const getTimeSlot = (record) => {
    if (!record) return '';
    
    // 尝试从各种可能的字段获取时段信息
    if (record.timeSlot) return record.timeSlot;
    if (record.slot) return record.slot;
    if (record.shift) return record.shift;
    
    // 从原始数据中尝试获取
    const originalData = record.originalRecord;
    if (originalData) {
      if (originalData.timeSlot) return originalData.timeSlot;
      if (originalData.slot) return originalData.slot;
      if (originalData.shift) return originalData.shift;
    }
    
    // 尝试从时间推断时段
    const registerTime = record.registerTime || record.visitTime;
    if (registerTime) {
      try {
        // 转换为iOS兼容格式
        const compatibleRegisterTime = convertToIOSCompatibleDate(registerTime);
        const date = new Date(compatibleRegisterTime);
        if (date && !isNaN(date.getTime())) {
          const hour = date.getHours();
          if (hour >= 6 && hour < 12) return '上午';
          if (hour >= 12 && hour < 18) return '下午';
          if (hour >= 18 && hour < 24) return '晚上';
        }
      } catch (error) {
        console.warn('时段推断失败:', error);
      }
    }
    
    return '';
  };

  // 获取带日期的就诊时段
  const getTimeSlotWithDate = (record) => {
    if (!record) return '';
    
    // 获取时段信息
    const timeSlot = getTimeSlot(record);
    
    // 获取日期信息
    const visitTime = record.visitTime || record.registerTime;
    let dateStr = '';
    
    if (visitTime) {
      try {
        // 转换为iOS兼容格式
        const compatibleVisitTime = convertToIOSCompatibleDate(visitTime);
        const date = new Date(compatibleVisitTime);
        if (date && !isNaN(date.getTime())) {
          const year = date.getFullYear();
          const month = (date.getMonth() + 1).toString().padStart(2, '0');
          const day = date.getDate().toString().padStart(2, '0');
          dateStr = `${year}-${month}-${day}`;
        }
      } catch (error) {
        console.warn('日期格式化失败:', error);
      }
    }
    
    // 返回日期和时段
    if (dateStr && timeSlot) {
      return `${dateStr} ${timeSlot}`;
    } else if (dateStr) {
      return dateStr;
    } else {
      return timeSlot;
    }
  };

// 导航到医生详情页面
const navigateToDoctor = () => {
  // 获取医生ID
  let doctorId = null;
  if (record.value && record.value.doctorId) {
    doctorId = record.value.doctorId;
  } else if (record.value && typeof record.value.doctor === 'number') {
    doctorId = record.value.doctor;
  }
  
  if (doctorId) {
    uni.navigateTo({
      url: `/subpkg/hospital/doctor-detail?id=${doctorId}`
    })
  } else {
    uni.showToast({
      title: '暂无医生详情信息',
      icon: 'none'
    })
  }
}

// 导航到科室详情页面
const navigateToDepartment = () => {
  // 获取科室ID
  let departmentId = null;
  if (record.value && record.value.departmentId) {
    departmentId = record.value.departmentId;
  } else if (record.value && typeof record.value.department === 'number') {
    departmentId = record.value.department;
  }
  
  if (departmentId) {
    uni.navigateTo({
      url: `/subpkg/hospital/department-detail?id=${departmentId}`
    })
  } else {
    uni.showToast({
      title: '暂无科室详情信息',
      icon: 'none'
    })
  }
}

// 检查是否已申请过转诊
const checkReferralStatus = async () => {
  if (!record.value || !record.value.id) {
    return
  }
  
  try {
    // 查询所有转诊记录
    const referralParams = {
      pageNo: 1,
      pageSize: 100 // 获取足够多的记录
    }
    const referralRes = await getPatientReferralList(referralParams)
    
    let referralList = []
    if (Array.isArray(referralRes?.records)) {
      referralList = referralRes.records
    } else if (Array.isArray(referralRes?.result?.records)) {
      referralList = referralRes.result.records
    } else if (Array.isArray(referralRes?.data?.records)) {
      referralList = referralRes.data.records
    } else if (Array.isArray(referralRes)) {
      referralList = referralRes
    }
    
    // 查找当前就诊记录对应的转诊记录（排除已取消和已拒绝的）
    const recordId = Number(record.value.id)
    const referral = referralList.find(ref => {
      const registrationId = ref.registrationRecordId || ref.registration_record_id
      const status = (ref.status || '').toUpperCase()
      return Number(registrationId) === recordId && status !== 'CANCELLED' && status !== 'REJECTED'
    })
    
    if (referral) {
      hasReferral.value = true
      canRefer.value = false // 已申请过转诊，不能再次申请
      // 保存关联的转诊记录ID和状态，供"查看转诊情况"使用
      record.value.referralId = referral.id || referral.referralId || null
      referralStatus.value = (referral.status || '').toUpperCase()
      // 如果有转诊记录，根据状态处理状态显示
      if (referralStatus.value !== 'PENDING') {
        statusDisplay.value = '已完成'
      }
    }
  } catch (error) {
    console.warn('检查转诊状态失败:', error)
    // 检查失败不影响显示，允许用户尝试申请
  }
}

// 查看转诊情况
const goToReferralStatus = () => {
  try {
    const referralId = record.value?.referralId
    if (!referralId) {
      uni.showToast({
        title: '未找到对应的转诊记录',
        icon: 'none'
      })
      return
    }
    
    uni.navigateTo({
      url: `/subpkg/hospital/referral-detail?id=${referralId}`,
      fail: (error) => {
        console.error('跳转转诊详情页面失败:', error)
        uni.showToast({
          title: '跳转失败，请重试',
          icon: 'none'
        })
      }
    })
  } catch (error) {
    console.error('查看转诊情况失败:', error)
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    })
  }
}

// 申请转诊
  const goToReferral = () => {
    try {
      // 验证是否有足够的记录数据
      if (!record.value || !record.value.id) {
        console.warn('缺少必要的就诊记录信息')
        uni.showToast({
          title: '就诊记录信息不完整',
          icon: 'none'
        })
        return
      }
      
      // 准备要传递的记录数据 - 仅使用真实数据
      const referralData = {
        recordId: record.value.id,
        patientName: patientInfo.value?.name || '',
        department: departmentName.value,
        doctor: doctorName.value,
        visitTime: record.value.visitTime || '',
        diagnosis: diagnosisInfo.value?.primary || '',
        // 添加更多可能有用的真实字段
        visitId: record.value.visitId || record.value.id,
        patientId: patientInfo.value?.patientId || patientInfo.value?.id || '',
        originalRecord: record.value // 传递完整的原始记录
      }
      
      // 将数据序列化为URL参数
      const encodedData = encodeURIComponent(JSON.stringify(referralData))
      
      // 跳转到转诊申请页面，并传递就诊记录信息
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
      console.error('准备转诊数据失败:', error)
      uni.showToast({
        title: '操作失败，请重试',
        icon: 'none'
      })
    }
  }

onMounted(async () => {
  await loadPatientInfo()
  loadRecordDetails()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background-color: #e3f2fd;
  padding: 20rpx;
}

.header-section {
  margin-bottom: 24rpx;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1976d2;
}

.info-card {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.card-title {
  background-color: #1976d2;
  color: white;
  font-size: 28rpx;
  font-weight: bold;
  padding: 20rpx 32rpx;
}

.info-content {
  padding: 20rpx 24rpx;
}

.info-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12rpx;
    gap: 16rpx;
  }
  
  .info-row .info-item {
    flex: 1;
    min-width: 0; /* 允许内容收缩 */
  }
  
  .info-row-full {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1px solid #f5f5f5;
  }
  
  .info-row-full:last-child {
    border-bottom: none;
  }
  
  .info-item.full-width {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12rpx 0;
    width: 100%;
    margin-right: 0;
    border-top: 1px solid #f0f0f0;
    margin-top: 8rpx;
  }

.info-row:last-child {
  margin-bottom: 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  flex: 1;
  margin-right: 20rpx;
}

.info-item:last-child {
  margin-right: 0;
}

.info-item.full-width {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  width: 100%;
  margin-right: 0;
  border-top: 1px solid #f0f0f0;
  margin-top: 8rpx;
}

.info-label {
  color: #1976d2;
  font-size: 28rpx;
  min-width: 120rpx;
  font-weight: 500;
}

.info-value {
  color: #333;
  font-size: 28rpx;
  flex: 1;
  text-align: right;
}

.info-value.clickable {
  color: #1989fa;
  text-decoration: underline;
}

.info-value.clickable:active {
  opacity: 0.7;
}

.fee {
  color: #ff4d4f;
  font-weight: bold;
}
.fee.real {
  color: #fa8c16;
}

.status-paid {
  color: #52c41a;
  white-space: nowrap;
}

.diagnosis-item {
  margin-bottom: 20rpx;
}

.diagnosis-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 12rpx;
  display: block;
}

.diagnosis-content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  padding-left: 16rpx;
  border-left: 4rpx solid #1976d2;
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-top: 40rpx;
  margin-bottom: 40rpx;
  padding: 0 20rpx;
}

.action-btn {
  width: 100%;
  border-radius: 24rpx;
  padding: 28rpx 40rpx;
  font-size: 32rpx;
  font-weight: 600;
  text-align: center;
  transition: all 0.3s ease;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 88rpx;
}

.doctor-btn {
  border-color: #4a90e2;
  color: #4a90e2;
}

.dept-btn {
  border-color: #4a90e2;
  color: #4a90e2;
}

.referral-btn {
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  color: #ffffff;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(74, 144, 226, 0.3);
  position: relative;
  overflow: hidden;
  padding: 16rpx 32rpx;
  font-size: 28rpx;
  min-height: 64rpx;
}

.referral-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.referral-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.25);
}

.referral-btn:active::before {
  left: 100%;
}

.cannot-refer-btn {
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  color: #999999;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 88rpx;
}

.cannot-refer-btn:active {
  background: linear-gradient(135deg, #f0f0f0 0%, #e5e5e5 100%);
}
</style>