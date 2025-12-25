<template>
  <view class="patient-detail-page">
    <!-- 返回按钮 -->
    <view class="back-section" @click="goBackToPatientList">
      <text class="back-icon">‹</text>
      <text class="back-text">返回患者列表</text>
    </view>

    <!-- 患者基本信息卡片 -->
    <view class="info-card">
      <view class="card-header">
        <text class="card-title">基本信息</text>
      </view>
      <view class="info-grid">
        <view class="info-item">
          <text class="info-label">姓名</text>
          <text class="info-value">{{ patient.name }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">性别</text>
          <text class="info-value">{{ patient.gender }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">年龄</text>
          <text class="info-value">{{ patient.age }}岁</text>
        </view>
        <view class="info-item">
          <text class="info-label">身份</text>
          <text class="info-value">{{ patient.identity }}</text>
        </view>
        <view class="info-item full-width">
          <text class="info-label">联系方式</text>
          <text class="info-value">{{ patient.phone }}</text>
        </view>
      </view>
    </view>

    <!-- 挂号信息卡片 -->
    <view class="info-card">
      <view class="card-header">
        <text class="card-title">挂号信息</text>
        <view class="status-badge" :class="getStatusClass(patient.status)">
          {{ patient.status }}
        </view>
      </view>
      <view class="info-list">
        <view class="list-item">
          <text class="item-label">挂号号码</text>
          <text class="item-value">{{ patient.registrationNumber }}</text>
        </view>
        <view class="list-item">
          <text class="item-label">预约时段</text>
          <text class="item-value">{{ patient.appointmentTime }}</text>
        </view>
        <view class="list-item">
          <text class="item-label">就诊科室</text>
          <text class="item-value">{{ patient.department }}</text>
        </view>
        <view class="list-item">
          <text class="item-label">就诊医生</text>
          <text class="item-value">{{ patient.doctor }}</text>
        </view>
      </view>
    </view>

    <!-- 健康档案卡片（医生端只读，仅体征信息） -->
    <view class="info-card">
      <view class="card-header">
        <text class="card-title">健康档案</text>
      </view>
      <view class="info-list">
        <view class="list-item">
          <text class="item-label">身高</text>
          <text class="item-value">{{ healthProfile.height ? healthProfile.height + ' cm' : '-' }}</text>
        </view>
        <view class="list-item">
          <text class="item-label">体重</text>
          <text class="item-value">{{ healthProfile.weight ? healthProfile.weight + ' kg' : '-' }}</text>
        </view>
        <view class="list-item">
          <text class="item-label">血型</text>
          <text class="item-value">{{ healthProfile.bloodType || '-' }}</text>
        </view>
        <view class="list-item">
          <text class="item-label">婚姻状况</text>
          <text class="item-value">{{ healthProfile.maritalStatus || '-' }}</text>
        </view>
        <view class="list-item">
          <text class="item-label">生育情况</text>
          <text class="item-value">{{ healthProfile.fertilityStatus || '-' }}</text>
        </view>
      </view>
    </view>

    <!-- 既往病史卡片 -->
    <view class="info-card">
      <view class="card-header">
        <text class="card-title">既往病史</text>
      </view>
      <view v-if="patient.medicalHistory && patient.medicalHistory.length > 0" class="history-list">
        <view
          v-for="(history, index) in patient.medicalHistory"
          :key="index"
          class="history-item"
        >
          <view class="history-tag">{{ history.type }}</view>
          <text class="history-text">{{ history.description }}</text>
        </view>
      </view>
      <view v-else class="empty-history">
        <text class="empty-text">暂无既往病史记录</text>
      </view>
    </view>

    <!-- 就诊历史卡片 -->
    <view class="info-card">
      <view class="card-header">
        <text class="card-title">就诊历史</text>
      </view>
      <view v-if="patient.visitHistory && patient.visitHistory.length > 0" class="visit-list">
        <view
          v-for="(visit, index) in patient.visitHistory"
          :key="index"
          class="visit-item"
          @click="viewVisitDetail(visit)"
        >
          <view class="visit-header">
            <view class="visit-date">
              <text class="date-icon">📅</text>
              <text class="date-text">{{ visit.date }}</text>
            </view>
            <text class="visit-arrow">›</text>
          </view>
          <view class="visit-info">
            <text class="visit-dept">{{ visit.department }}</text>
            <text class="visit-doctor">{{ visit.doctor }}</text>
          </view>
          <view v-if="visit.diagnosis" class="visit-diagnosis">
            <text class="diagnosis-label">诊断：</text>
            <text class="diagnosis-text">{{ visit.diagnosis }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-history">
        <text class="empty-text">暂无就诊历史</text>
      </view>
    </view>

    <!-- 操作按钮区域 -->
    <view v-if="patient.status !== '已完成' && patient.status !== '已转诊'" class="action-section">
      <button
        v-if="patient.status === '待接诊'"
        class="action-btn receive-btn"
        @click="receivePatient"
      >
        开始接诊
      </button>
      <view v-else-if="patient.status === '进行中'" class="action-buttons-group">
        <button
          class="action-btn referral-btn"
          @click="showReferralModal"
        >
          转诊
        </button>
        <button
          class="action-btn complete-btn"
          @click="completePatient"
        >
          完成接诊
        </button>
      </view>
    </view>

    <!-- 备注输入区域 -->
    <view v-if="patient.status === '进行中'" class="note-section">
      <view class="note-header">
        <text class="note-title">就诊备注</text>
      </view>
      <textarea
        class="note-textarea"
        v-model="visitNote"
        placeholder="请输入就诊情况、诊断结果等信息..."
        maxlength="500"
      />
      <view class="note-footer">
        <text class="char-count">{{ visitNote.length }}/500</text>
        <button class="save-note-btn" @click="saveNote">保存备注</button>
      </view>
    </view>
    <!-- 转诊意见弹窗 -->
    <view v-if="referralData.showReferral" class="modal-overlay">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">添加转诊意见</text>
          <text class="modal-close" @click="hideReferralModal">×</text>
        </view>
        
        <view class="modal-body">
          <!-- 转诊类型选择 -->
          <view class="form-section">
            <text class="form-label">转诊类型</text>
            <view class="type-selector">
              <view 
                class="type-option" 
                :class="{ active: referralData.targetType === 'internal' }"
                @click="changeReferralType('internal')"
              >
                <text>院内转诊</text>
              </view>
              <view 
                class="type-option" 
                :class="{ active: referralData.targetType === 'external' }"
                @click="changeReferralType('external')"
              >
                <text>院外转诊</text>
              </view>
            </view>
          </view>
          
          <!-- 院内转诊 - 目标科室 -->
          <view v-if="referralData.targetType === 'internal'" class="form-section internal-dept-section">
            <text class="form-label">目标科室</text>
            <view 
              v-if="departmentGroups.length"
              class="dept-groups"
            >
              <view
                v-for="group in departmentGroups"
                :key="group.groupId"
                class="dept-group"
              >
                <view class="dept-group-title">{{ group.groupName }}</view>
                <view class="dept-items">
                  <view
                    v-for="dept in group.children"
                    :key="dept.deptId"
                    class="dept-item"
                    :class="{ 
                      active: referralData.targetDeptId === String(dept.deptId),
                      disabled: isSameDepartment(dept.deptId)
                    }"
                    @click="!isSameDepartment(dept.deptId) && selectInternalDepartment(dept)"
                  >
                    <view class="dept-item-name">{{ dept.deptName }}</view>
                    <view class="dept-item-desc">{{ dept.deptDesc || '暂无介绍' }}</view>
                    <text v-if="isSameDepartment(dept.deptId)" class="disabled-tip">（当前科室）</text>
                  </view>
                </view>
              </view>
            </view>
            <view v-else class="empty-dept">正在加载科室信息...</view>
          </view>
          
          <!-- 院外转诊 - 目标医院 -->
          <view v-else class="form-section">
            <text class="form-label">目标医院</text>
            <picker 
              mode="selector" 
              :range="hospitals" 
              range-key="name"
              @change="handleHospitalChange"
            >
              <view class="picker-text">
                {{ referralData.targetHospital || '请选择医院' }}
              </view>
            </picker>
          </view>
          
          <!-- 转诊原因 -->
          <view class="form-section">
            <text class="form-label">转诊原因</text>
            <textarea 
              v-model="referralData.reason"
              placeholder="请输入详细的转诊原因..."
              class="textarea-input"
              maxlength="500"
            />
            <text class="char-count">{{ referralData.reason.length }}/500</text>
          </view>
        </view>
        
        <view class="modal-footer">
          <button class="modal-btn cancel-btn" @click="hideReferralModal">取消</button>
          <button class="modal-btn confirm-btn" @click="submitReferral">提交</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { uniNavigateTo, uniShowToast } from '@/utils/uniHelper'
import { doctorApi } from '@/api/doctor'
import { doctorGenerateReferralAdvice } from '@/api/referral'
import { getDepartmentTree } from '@/api/department'
import { patientApi } from '@/api/patient'

// 患者详情数据（加载后会用后端数据覆盖）
const patient = ref({
  patientId: '', // 增加患者ID字段
  name: '张**',
  gender: '男',
  age: 22,
  identity: '学生',
  phone: '138****5678',
  registrationNumber: 'R2024101101',
  appointmentTime: '2024-10-11 08:00-08:30',
  department: '内科',
  doctor: '张医生',
  status: '待接诊',
  medicalHistory: [],
  visitHistory: []
})

const visitNote = ref('')

// 健康档案（医生端只读展示）
const healthProfile = ref({
  height: '',
  weight: '',
  bloodType: '',
  maritalStatus: '',
  fertilityStatus: '',
  currentIllness: '',
  pastHistory: '',
  familyHistory: '',
  allergyHistory: ''
})

// 转诊相关数据
const referralData = ref({
  showReferral: false,
  reason: '',
  targetType: 'internal', // internal: 院内, external: 院外
  targetDepartment: '',
  targetHospital: '',
  targetDeptId: ''
})

const departmentGroups = ref([])

// 目标医院列表（模拟数据，实际应从API获取）
const hospitals = ref([
  { id: '1', name: '北京协和医院' },
  { id: '2', name: '北京天坛医院' },
  { id: '3', name: '北京同仁医院' },
  { id: '4', name: '北京大学第一医院' },
  { id: '5', name: '中国医学科学院肿瘤医院' }
])

const getStatusClass = (status) => {
  if (status === '待接诊') return 'status-pending'
  if (status === '进行中') return 'status-progress'
  if (status === '已完成') return 'status-done'
  if (status === '已转诊') return 'status-referral'
  return 'status-pending'
}

function handleHospitalChange(event) {
  const index = Number(event?.detail?.value ?? -1)
  const hospital = hospitals.value[index]
  referralData.value.targetHospital = hospital?.name || ''
}

const loadDepartmentGroups = async () => {
  try {
    const res = await getDepartmentTree()
    let data = res?.data || res?.result || res || []
    if (!Array.isArray(data)) {
      data = data ? [data] : []
    }
    departmentGroups.value = data
      .map(group => ({
        groupId: group.deptId || group.id || group.deptCode || Math.random().toString(36).slice(2),
        groupName: group.deptName || group.name || '科室',
        children: (group.children || group.subDepartments || []).map(child => ({
          deptId: child.deptId || child.id || '',
          deptName: child.deptName || child.name || '未命名科室',
          deptDesc: child.deptDesc || child.description || '',
        })).filter(child => child.deptId)
      }))
      .filter(group => group.children && group.children.length)
  } catch (error) {
    console.error('加载科室数据失败', error)
    departmentGroups.value = []
  }
}

// 当前就诊记录的科室ID
const originalDeptId = ref(null)

const selectInternalDepartment = (dept) => {
  // 检查是否是同一科室
  if (isSameDepartment(dept.deptId)) {
    uniShowToast({
      title: '不能转诊到同一科室',
      icon: 'none'
    })
    return
  }
  referralData.value.targetDeptId = String(dept.deptId || '')
  referralData.value.targetDepartment = dept.deptName || ''
}

// 检查是否是同一科室
const isSameDepartment = (deptId) => {
  if (!originalDeptId.value || !deptId) return false
  return String(originalDeptId.value) === String(deptId)
}

const buildMedicalHistoryText = () => {
  if (!Array.isArray(patient.value.medicalHistory) || !patient.value.medicalHistory.length) {
    return ''
  }
  return patient.value.medicalHistory
    .map(item => {
      const label = item?.type || '病史'
      const desc = item?.description || ''
      return desc ? `${label}：${desc}` : label
    })
    .join('；')
}

const resolveSymptomsText = () => {
  if (patient.value.symptoms) return patient.value.symptoms
  const visit = Array.isArray(patient.value.visitHistory) ? patient.value.visitHistory[0] : null
  if (visit?.diagnosis) return visit.diagnosis
  return ''
}

// 工具：计算年龄
// 移除 TS 泛型与类型注解
const patientIdRef = ref(null)
const appointmentIdRef = ref(null)

// 工具：计算年龄（移除参数类型注解）
const calcAge = (birthDate) => {
  if (!birthDate) return ''
  try {
    const d = new Date(birthDate)
    const now = new Date()
    let age = now.getFullYear() - d.getFullYear()
    const m = now.getMonth() - d.getMonth()
    if (m < 0 || (m === 0 && now.getDate() < d.getDate())) age--
    return age
  } catch { return '' }
}

// 工具：患者身份映射
const mapIdentity = (type) => {
  if (type === 1) return '学生'
  if (type === 2) return '教师'
  if (type === 3) return '职工'
  return '其他'
}

// 加载后端患者详情
async function loadPatientDetail(id) {
  try {
    const detail = await doctorApi.getPatientDetail(id)
    // 兼容多种返回结构：{patient, visits} 或 {data:{patient,visits}} / {result:{...}} / {body:{...}}
    const dto = (detail && (detail.patient || detail.visits))
      ? detail
      : (detail?.data || detail?.result || detail?.body || {})
    const p = dto?.patient || {}
    const visits = Array.isArray(dto?.visits) ? dto.visits : []

    const statusMap = (s) => (s === 2 ? '已完成' : (s === 6 ? '已转诊' : (s === 5 ? '进行中' : '待接诊')))
    const latestStatus = visits.length > 0 ? statusMap(visits[0]?.status) : '待接诊'

    // 为避免某些端/编译器对整个对象替换的追踪问题，使用就地合并
    // 从 patient 表构造病史条目
    const mh = []
    if (p.presentIllness) mh.push({ type: '现病史', description: p.presentIllness })
    if (p.pastIllness) mh.push({ type: '既往史', description: p.pastIllness })
    if (p.familyIllness) mh.push({ type: '家族史', description: p.familyIllness })
    if (p.allergyHistory && p.allergyHistory !== '无') mh.push({ type: '过敏史', description: p.allergyHistory })

    Object.assign(patient.value, {
      patientId: p.patientId || p.id || '',
      name: p.patientName || '',
      gender: p.gender || '',
      age: calcAge(p.birthDate),
      identity: mapIdentity(p.patientType),
      phone: p.phone || '',
      registrationNumber: (visits[0]?.visitNo)
        || p.outpatientNumber
        || (appointmentIdRef.value ? String(appointmentIdRef.value) : ''),
      appointmentTime: visits[0]?.timeSlot || '',
      department: '',
      doctor: '',
      status: latestStatus,
      medicalHistory: mh,
      visitHistory: visits.map(v => ({
        date: v.visitDate,
        department: '',
        doctor: '',
        diagnosis: v.diagnosis || ''
      }))
    })

    // 若带了预约ID，则加载 registration_record 详情，填充预约时段/科室/医生
    if (appointmentIdRef.value) {
      try {
        const ap = await doctorApi.getAppointmentDetail(appointmentIdRef.value)
        if (ap) {
          Object.assign(patient.value, {
            appointmentTime: ap.appointmentTime || patient.value.appointmentTime,
            department: ap.department || patient.value.department,
            doctor: ap.doctor || patient.value.doctor,
          })
          if (ap.status !== undefined && ap.status !== null) {
            patient.value.status = statusMap(ap.status)
          }
          // 获取当前就诊记录的科室ID
          originalDeptId.value = ap.deptId || ap.dept_id || ap.departmentId || ap.department_id || null
          console.log('医生端获取当前科室ID:', originalDeptId.value, 'from appointment:', ap)
        }
      } catch {}
    }
  } catch (e) {
    uniShowToast({ title: '获取患者详情失败', icon: 'none' })
  }
}

// 加载健康档案数据（复用患者端接口）
async function loadHealthProfile(patientId) {
  try {
    if (!patientId) return
    const data = await patientApi.getHealthProfile({ patientId })
    if (data) {
      Object.assign(healthProfile.value, data)
    }
  } catch (e) {
    // 医生端健康档案加载失败不阻塞整体页面
    console.error('加载健康档案失败', e)
  }
}

function goBackToPatientList() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uniNavigateTo({ url: '/subpkg/doctor/patients/list' })
  }
}

// 开始接诊：调用后端更新状态
async function receivePatient() {
  if (!appointmentIdRef.value) {
    uniShowToast({ title: '缺少预约ID', icon: 'none' })
    return
  }
  try {
    await doctorApi.updatePatientStatus({ appointmentId: appointmentIdRef.value, action: 'start' })
    patient.value.status = '进行中'
    uniShowToast({ title: '已开始接诊', icon: 'success' })
  } catch {
    uniShowToast({ title: '开始接诊失败', icon: 'none' })
  }
}

// 完成接诊：调用后端更新状态
async function completePatient() {
  if (!appointmentIdRef.value) {
    uniShowToast({ title: '缺少预约ID', icon: 'none' })
    return
  }
  try {
    await doctorApi.updatePatientStatus({ appointmentId: appointmentIdRef.value, action: 'finish' })
    patient.value.status = '已完成'
    uniShowToast({ title: '已完成接诊', icon: 'success' })
  } catch {
    uniShowToast({ title: '完成接诊失败', icon: 'none' })
  }
}

function saveNote() {
  if (!visitNote.value) {
    uniShowToast({ title: '请输入备注内容', icon: 'none' })
    return
  }
  uniShowToast({ title: '备注已保存', icon: 'success' })
}

// 显示转诊意见弹窗
function showReferralModal() {
  referralData.value.showReferral = true
}

// 隐藏转诊意见弹窗
function hideReferralModal() {
  referralData.value.showReferral = false
}

// 切换转诊类型
function changeReferralType(type) {
  referralData.value.targetType = type
  // 切换类型时清空目标选择
  if (type === 'internal') {
    referralData.value.targetHospital = ''
  } else {
    referralData.value.targetDepartment = ''
    referralData.value.targetDeptId = ''
  }
}

// 提交转诊意见
async function submitReferral() {
  // 表单验证
  if (!referralData.value.reason) {
    uniShowToast({ title: '请输入转诊原因', icon: 'none' })
    return
  }
  
  if (referralData.value.targetType === 'internal') {
    if (!referralData.value.targetDeptId) {
      uniShowToast({ title: '请选择目标科室', icon: 'none' })
      return
    }
  } else {
    referralData.value.targetDeptId = ''
    referralData.value.targetDepartment = ''
    if (!referralData.value.targetHospital) {
      uniShowToast({ title: '请选择目标医院', icon: 'none' })
      return
    }
  }
  
  try {
    const isInternal = referralData.value.targetType === 'internal'
    const historyText = buildMedicalHistoryText()
    const symptomsText = resolveSymptomsText()
    const payload = {
      patientId: patient.value.patientId || null,
      patientName: patient.value.name || '',
      gender: patient.value.gender || '',
      age: patient.value.age ? Number(patient.value.age) : null,
      phone: patient.value.phone || '',
      symptoms: symptomsText || referralData.value.reason,
      medicalHistory: historyText,
      reason: referralData.value.reason,
      sourceType: 'DOCTOR_DIRECT',
      targetType: isInternal ? 'INTERNAL' : 'EXTERNAL',
      targetDeptId: isInternal && referralData.value.targetDeptId ? Number(referralData.value.targetDeptId) : null,
      targetDeptName: referralData.value.targetDepartment,
      targetHospitalName: isInternal ? '校医院' : referralData.value.targetHospital,
      registrationRecordId: appointmentIdRef.value || null,
      doctorGenerated: true,
      attachments: []
    }
    
    await doctorGenerateReferralAdvice(payload)

    if (appointmentIdRef.value) {
      try {
        await doctorApi.updatePatientStatus({ appointmentId: appointmentIdRef.value, action: 'referral' })
        patient.value.status = '已转诊'
      } catch {}
    }
    
    uniShowToast({ title: '转诊意见已提交', icon: 'success' })
    hideReferralModal()
    
    // 重置转诊表单
    referralData.value = {
      showReferral: false,
      reason: '',
      targetType: 'internal',
      targetDepartment: '',
      targetHospital: ''
    }
  } catch (error) {
    uniShowToast({ title: '提交失败，请重试', icon: 'none' })
  }
}

function viewVisitDetail(visit) {
  uniShowToast({ title: `就诊记录：${visit.date}`, icon: 'none' })
}

// URL 参数传入（patient=encodeURIComponent(JSON.stringify(obj))）
onLoad((options) => {
  try {
    if (options && options.patient) {
      const incoming = JSON.parse(decodeURIComponent(options.patient))
      patient.value = { ...patient.value, ...incoming }
    }
  } catch (e) {
    // ignore parse error
  }
  // 兼容 id 或 patientId 作为参数名
  if (options?.id) {
    patientIdRef.value = Number(options.id)
  } else if (options?.patientId) {
    patientIdRef.value = Number(options.patientId)
  }
  if (options?.appointmentId) {
    appointmentIdRef.value = Number(options.appointmentId)
  }
  // 直接在 onLoad 阶段触发加载，避免 onMounted 时机差异导致未请求
  if (patientIdRef.value) {
    loadPatientDetail(patientIdRef.value)
    loadHealthProfile(patientIdRef.value)
  }
})

// EventChannel 传入（保持兼容）
onMounted(async () => {
  await loadDepartmentGroups()
  const pages = getCurrentPages()
  const cur = pages[pages.length - 1]
  if (cur && cur.getOpenerEventChannel) {
    const ec = cur.getOpenerEventChannel()
    if (ec) {
      ec.on('patient', (data) => {
        if (data) patient.value = { ...patient.value, ...data }
      })
      ec.on('sendPatient', (data) => {
        if (data?.patient) {
          patient.value = { ...patient.value, ...data.patient }
        }
      })
    }
  }
  // 若 onLoad 尚未触发或未成功加载，再兜底加载一次
  if (patientIdRef.value && (!patient.value || !patient.value.name || patient.value.name === '张**')) {
    await loadPatientDetail(patientIdRef.value)
  }
})
</script>

<style scoped>
.patient-detail-page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 24rpx;
  background: linear-gradient(180deg, #e8f3ff 0%, #f7faff 45%, #ffffff 100%);
}

/* 返回 */
.back-section {
  display: flex;
  align-items: center;
  color: #2a7bff;
  font-weight: 700;
  margin-bottom: 20rpx;
}
.back-icon {
  font-size: 40rpx;
  margin-right: 8rpx;
}
.back-text {
  font-size: 28rpx;
}

/* 卡片通用 */
.info-card {
  background: #fff;
  border-radius: 18rpx;
  padding: 24rpx;
  box-shadow: 0 10rpx 24rpx rgba(58, 156, 255, 0.12);
  margin-bottom: 24rpx;
  border: 1rpx solid #eef3fb;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}
.card-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #1a1a1a;
}

/* 状态徽标 */
.status-badge {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 700;
}
.status-pending {
  color: #8a6d3b;
  background: #fff3cd;
}
.status-progress {
  color: #0f5132;
  background: #d1e7dd;
}
.status-done {
  color: #084298;
  background: #cfe2ff;
}
.status-referral {
  color: #5b21b6;
  background: #ede9fe;
}

/* 基本信息网格 */
.info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}
.info-item {
  width: calc(50% - 8rpx);
  background: #f6f9ff;
  border-radius: 14rpx;
  padding: 16rpx;
  border: 1rpx solid #e8efff;
}
.info-item.full-width {
  width: 100%;
}
.info-label {
  font-size: 24rpx;
  color: #7a8aa0;
}
.info-value {
  margin-top: 6rpx;
  font-size: 28rpx;
  color: #2f3b52;
  font-weight: 600;
}

/* 列表信息 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.list-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx;
  background: #f6f9ff;
  border-radius: 14rpx;
  border: 1rpx solid #e8efff;
}
.item-label {
  color: #7a8aa0;
  font-size: 24rpx;
}
.item-value {
  color: #2f3b52;
  font-size: 28rpx;
  font-weight: 600;
}

/* 病史 */
.privacy-tip {
  color: #7a8aa0;
  font-size: 24rpx;
  margin-left: 8rpx;
}
.history-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: #f6f9ff;
  padding: 16rpx;
  border-radius: 14rpx;
  border: 1rpx solid #e8efff;
}
.history-tag {
  background: #e6f2ff;
  color: #2a7bff;
  font-size: 22rpx;
  border-radius: 999rpx;
  padding: 6rpx 12rpx;
}
.history-text {
  font-size: 26rpx;
  color: #2f3b52;
}
.empty-history {
  padding: 24rpx;
  text-align: center;
  color: #7a8aa0;
}
.empty-text {
  font-size: 26rpx;
}

/* 就诊历史 */
.visit-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.visit-item {
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 16rpx;
}
.visit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}
.visit-date {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #2f3b52;
  font-weight: 600;
}
.date-icon {
  font-size: 28rpx;
}
.date-text {
  font-size: 28rpx;
}
.visit-arrow {
  font-size: 40rpx;
  color: #a0b7e0;
}
.visit-info {
  display: flex;
  justify-content: space-between;
  color: #7a8aa0;
  font-size: 24rpx;
}
.visit-diagnosis {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #7a8aa0;
}
.diagnosis-label {
  font-weight: 600;
}
.diagnosis-text {
  color: #2f3b52;
}

/* 操作区 */
.action-section {
  margin: 24rpx 0;
  display: flex;
  gap: 16rpx;
}
.action-buttons-group {
  display: flex;
  gap: 16rpx;
  width: 100%;
}
.action-btn {
  flex: 1;
  border: none;
  border-radius: 12rpx;
  padding: 24rpx 0;
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
}
.receive-btn {
  background: linear-gradient(90deg, #2a7bff, #6aa9ff);
  box-shadow: 0 12rpx 20rpx rgba(42, 123, 255, 0.24);
}
.complete-btn {
  background: linear-gradient(90deg, #00c853, #52e176);
  box-shadow: 0 12rpx 20rpx rgba(0, 200, 83, 0.24);
}
.referral-btn {
    background: linear-gradient(90deg, #ff9800, #ffb74d);
    box-shadow: 0 12rpx 20rpx rgba(255, 152, 0, 0.24);
  }
  
  /* 弹窗样式 */
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 999;
  }
  
  .modal-content {
    background: #fff;
    border-radius: 20rpx;
    width: 90%;
    max-width: 700rpx;
    max-height: 80vh;
    overflow: hidden;
  }
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 32rpx;
    border-bottom: 2rpx solid #e8e8e8;
  }
  
  .modal-title {
    font-size: 32rpx;
    font-weight: 700;
    color: #1a1a1a;
  }
  
  .modal-close {
    font-size: 48rpx;
    color: #7a8aa0;
    padding: 0 16rpx;
  }
  
  .modal-body {
    padding: 32rpx;
    max-height: 60vh;
    overflow-y: auto;
  }
  
  .form-section {
    margin-bottom: 32rpx;
  }
  
  .form-label {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    color: #2f3b52;
    margin-bottom: 16rpx;
  }

  .internal-dept-section .dept-groups {
    display: flex;
    flex-direction: column;
    gap: 24rpx;
    max-height: 360rpx;
    overflow-y: auto;
  }

  .dept-group {
    padding: 20rpx;
    border-radius: 16rpx;
    background: #f9fbff;
    border: 2rpx solid #eef2ff;
  }

  .dept-group-title {
    font-size: 26rpx;
    font-weight: 600;
    color: #1f2a44;
    margin-bottom: 16rpx;
  }

  .dept-items {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
  }

  .dept-item {
    width: calc(50% - 6rpx);
    border: 2rpx solid #e0e7ff;
    border-radius: 12rpx;
    padding: 16rpx;
    background: #fff;
  }

  .dept-item.active {
    border-color: #ff9800;
    background: #fff7e6;
  }
  
  .dept-item.disabled {
    opacity: 0.5;
    cursor: not-allowed;
    background: #f9fafb;
    border-color: #e5e7eb;
  }
  
  .dept-item.disabled .dept-item-name,
  .dept-item.disabled .dept-item-desc {
    color: #9ca3af;
  }
  
  .dept-item.disabled:active {
    background: #f9fafb;
  }

  .dept-item-name {
    font-size: 26rpx;
    font-weight: 600;
    color: #2f3b52;
    margin-bottom: 6rpx;
  }

  .dept-item-desc {
    font-size: 22rpx;
    color: #7a8aa0;
  }
  
  .disabled-tip {
    font-size: 24rpx;
    color: #9ca3af;
    margin-top: 8rpx;
  }

  .empty-dept {
    font-size: 24rpx;
    color: #7a8aa0;
    background: #f8fafc;
    padding: 20rpx;
    border-radius: 12rpx;
    text-align: center;
  }
  
  .type-selector {
    display: flex;
    gap: 16rpx;
  }
  
  .type-option {
    flex: 1;
    padding: 20rpx;
    text-align: center;
    border: 2rpx solid #e8e8e8;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #7a8aa0;
  }
  
  .type-option.active {
    border-color: #ff9800;
    color: #ff9800;
    background-color: #fff8e1;
  }
  
  .picker-text {
    padding: 20rpx;
    background: #f8fafc;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #2f3b52;
  }
  
  .text-input {
    width: 100%;
    padding: 20rpx;
    background: #f8fafc;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #2f3b52;
    box-sizing: border-box;
  }
  
  .textarea-input {
    width: 100%;
    min-height: 200rpx;
    padding: 20rpx;
    background: #f8fafc;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #2f3b52;
    box-sizing: border-box;
  }
  
  .modal-footer {
    display: flex;
    padding: 24rpx 32rpx;
    border-top: 2rpx solid #e8e8e8;
    gap: 16rpx;
  }
  
  .modal-btn {
    flex: 1;
    padding: 20rpx;
    border-radius: 12rpx;
    font-size: 28rpx;
    font-weight: 600;
  }
  
  .cancel-btn {
    background: #f8fafc;
    color: #7a8aa0;
    border: 2rpx solid #e8e8e8;
  }
  
  .confirm-btn {
    background: linear-gradient(90deg, #ff9800, #ffb74d);
    color: #fff;
    border: none;
  }
  
  /* 备注区 */
.note-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(42, 123, 255, 0.08);
}
.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}
.note-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a1a;
}
.note-textarea {
  width: 100%;
  min-height: 160rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 16rpx;
  font-size: 26rpx;
  color: #2f3b52;
  box-sizing: border-box;
}
.note-footer {
  margin-top: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.char-count {
  font-size: 24rpx;
  color: #7a8aa0;
}
.save-note-btn {
  background: linear-gradient(90deg, #2a7bff, #6aa9ff);
  color: #fff;
  border: none;
  border-radius: 12rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  font-weight: 700;
}
</style>