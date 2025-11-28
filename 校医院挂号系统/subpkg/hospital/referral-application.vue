<template>
  <view class="referral-bg">
    <view class="page-header">
      <text class="page-title">转诊申请</text>
    </view>

    <scroll-view scroll-y class="content">
      <view class="form-section type-section">
        <view class="section-title">转诊类型</view>
        <view class="type-options">
          <view
            class="type-option"
            :class="{ active: formData.referralType === 'internal' }"
            @click="selectReferralType('internal')"
          >
            <view class="option-header">
              <text class="option-title">院内转诊</text>
              <view v-if="formData.referralType === 'internal'" class="option-badge">当前</view>
            </view>
            <view class="option-desc">由校医院协调院内科室继续治疗，流程更便捷</view>
          </view>
          <view
            class="type-option"
            :class="{ active: formData.referralType === 'external' }"
            @click="selectReferralType('external')"
          >
            <view class="option-header">
              <text class="option-title">院外转诊</text>
              <view v-if="formData.referralType === 'external'" class="option-badge">当前</view>
            </view>
            <view class="option-desc">转往合作医院获取更多诊疗资源</view>
          </view>
        </view>
        <view class="type-tip">
          <text>{{ referralTypeDesc }}</text>
        </view>
      </view>

      <view class="form-section status-section">
        <view class="status-row">
          <view class="status-chip">{{ referralTypeLabel }}</view>
          <view class="status-hint">目标范围：{{ targetScopeLabel }}</view>
        </view>
        <view class="status-summary">当前目标：{{ currentTargetDisplay }}</view>
      </view>

      <view class="form-section target-section">
        <view class="section-title">转诊目标</view>

        <view class="target-card" v-if="isInternalReferral">
          <view class="target-line">
            <text class="target-label">目标医院</text>
            <text class="target-value">本校医院</text>
          </view>
          <view class="target-line target-select" @click="openDepartmentTree">
            <text class="target-label">目标科室</text>
            <text class="target-value target-action">
              {{ selectedDepartment?.deptName || formData.targetDepartment || '请选择院内科室' }}
              <text class="target-arrow">›</text>
            </text>
          </view>
        </view>

        <view class="target-card" v-else>
          <view class="target-line">
            <text class="target-label">合作医院</text>
            <picker
              class="target-picker"
              @change="onHospitalChange"
              :value="hospitalIndex"
              :range="hospitals"
              range-key="name"
              :disabled="hospitalLoading"
            >
              <view class="target-value picker-value">
                {{ hospitals[hospitalIndex]?.name || '请选择合作医院' }}
              </view>
            </picker>
          </view>
        </view>
      </view>

      <view class="form-section">
        <view class="section-title">患者信息</view>
        <view class="form-item">
          <view class="label">姓名</view>
          <input type="text" v-model.trim="formData.patientName" placeholder="请输入患者姓名" class="input" />
        </view>
        <view class="form-item">
          <view class="label">性别</view>
          <radio-group @change="handleGenderChange">
            <label class="radio-item" :class="{ active: formData.gender === '男' }">
              <radio value="男" :checked="formData.gender === '男'" />男
            </label>
            <label class="radio-item" :class="{ active: formData.gender === '女' }">
              <radio value="女" :checked="formData.gender === '女'" />女
            </label>
          </radio-group>
        </view>
        <view class="form-item">
          <view class="label">年龄</view>
          <input type="number" v-model.trim="formData.age" placeholder="请输入年龄" class="input" />
        </view>
        <view class="form-item">
          <view class="label">联系电话</view>
          <input type="number" v-model.trim="formData.phone" placeholder="请输入联系电话" class="input" />
        </view>
      </view>

      <view class="form-section">
        <view class="section-title">病情描述</view>
        <view class="form-item">
          <view class="label">症状描述</view>
          <textarea
            v-model.trim="formData.symptoms"
            placeholder="请详细描述您的症状和持续时间"
            class="textarea"
            rows="4"
          ></textarea>
        </view>
        <view class="form-item">
          <view class="label">既往病史</view>
          <textarea
            v-model.trim="formData.medicalHistory"
            placeholder="请填写既往病史、过敏史等"
            class="textarea"
            rows="3"
          ></textarea>
        </view>
        <view class="form-item">
          <view class="label">转诊原因</view>
          <textarea
            v-model.trim="formData.reason"
            placeholder="请说明需要转诊的原因"
            class="textarea"
            rows="3"
          ></textarea>
        </view>
      </view>

      <view class="form-section">
        <view class="section-title">上传资料</view>
        <view class="upload-section">
          <view class="upload-tips">可上传检查报告、病历等资料（最多 {{ MAX_ATTACHMENTS }} 张，单张≤2MB）</view>
          <view class="upload-list">
            <view v-for="(item, index) in formData.attachments" :key="index" class="upload-item">
              <image :src="item.url" mode="aspectFill" class="upload-img" />
              <view class="delete-btn" @click="deleteAttachment(index)">×</view>
            </view>
            <view v-if="formData.attachments.length < MAX_ATTACHMENTS" class="upload-btn" @click="chooseImage">
              <view class="upload-icon">+</view>
              <text class="upload-text">添加图片</text>
            </view>
          </view>
        </view>
      </view>

      <view class="submit-section">
        <button class="submit-btn" @click="submitApplication" :disabled="submitting">
          {{ submitting ? '提交中...' : '提交转诊申请' }}
        </button>
      </view>
    </scroll-view>

    <view class="department-tree-modal" v-if="showDepartmentTree">
      <view class="modal-overlay" @click="closeDepartmentTree"></view>
      <!-- 防止文字溢出到模态框外部 -->
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择院内科室</text>
          <view class="close-btn" @click="closeDepartmentTree">×</view>
        </view>

        <view class="department-tree-container">
          <view v-if="loadingDepartment" class="loading">加载科室信息中...</view>
          <scroll-view v-else scroll-y class="department-tree-scroll">
            <view class="department-tree">
              <view v-for="department in departmentTree" :key="department.deptId" class="department-item">
                <view class="primary-department" @click="toggleDepartment(department)">
                  <text class="department-name">{{ department.deptName }}</text>
                  <text class="toggle-icon">{{ department.expanded ? '▼' : '▶' }}</text>
                </view>
                <view
                  v-if="department.children && department.children.length"
                  class="secondary-department-list"
                  :class="{ expanded: department.expanded }"
                >
                  <view
                    v-for="child in department.children"
                    :key="child.deptId"
                    class="secondary-department"
                    :class="{ selected: selectedDeptId === child.deptId }"
                    @click="selectDepartment(child)"
                  >
                    <text class="department-name">{{ child.deptName }}</text>
                  </view>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>

        <view class="modal-footer">
          <button class="cancel-btn" @click="closeDepartmentTree">取消</button>
          <button class="confirm-btn" @click="confirmDepartmentSelection" :disabled="!selectedDepartment">确认选择</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { useUserStore } from '@/store/user'

import { submitReferralApplication, getReferralHospitals } from '../../api/referral'
import { getDepartmentTree } from '../../api/department'

const MAX_ATTACHMENTS = 5
const MAX_ATTACHMENT_SIZE = 2 * 1024 * 1024

const normalizeDeptId = (dept) => {
  if (!dept) return ''
  const candidate = dept.deptId ?? dept.id ?? dept.departmentId ?? ''
  return candidate === null || candidate === undefined ? '' : String(candidate)
}

const resolveGender = (value) => {
  if (value === null || value === undefined) {
    return ''
  }

  if (typeof value === 'number') {
    if (value === 1) return '男'
    if (value === 2) return '女'
  }

  const text = String(value).trim().toLowerCase()
  if (!text) return ''

  if (text === '男' || text === 'male') return '男'
  if (text === '女' || text === 'female') return '女'
  if (text === '1') return '男'
  if (text === '2') return '女'

  return ''
}

const calculateAgeFromBirth = (birthDate) => {
  if (!birthDate) return ''
  try {
    const birth = new Date(birthDate)
    if (Number.isNaN(birth.getTime())) return ''

    const now = new Date()
    let age = now.getFullYear() - birth.getFullYear()
    const monthDiff = now.getMonth() - birth.getMonth()
    if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < birth.getDate())) {
      age -= 1
    }
    return age < 0 ? '' : age
  } catch (error) {
    console.warn('计算年龄失败:', error)
    return ''
  }
}

export default {
  data() {
    const userStore = useUserStore()
    try {
      if (!userStore.userInfo || !userStore.token) {
        userStore.initFromStorage?.()
      }
    } catch (error) {
      console.warn('初始化用户信息失败:', error)
    }

    return {
      MAX_ATTACHMENTS,
      MAX_ATTACHMENT_SIZE,
      userStore,
      formData: {
        patientName: '',
        gender: '',
        age: '',
        phone: '',
        symptoms: '',
        medicalHistory: '',
        reason: '',
        targetHospital: '本校医院',
        targetDepartment: '',
        targetDeptId: null,
        attachments: [],
        referralType: 'internal',
        visitRecordId: '',
        targetType: 'INTERNAL',
      },
      hospitals: [],
      hospitalIndex: 0,
      hospitalLoading: false,
      submitting: false,
      showDepartmentTree: false,
      departmentTree: [],
      loadingDepartment: false,
      selectedDeptId: '',
      selectedDepartment: null,
      referralInfo: null,
      selectedVisitRecord: null,
    }
  },
  computed: {
    isInternalReferral() {
      return this.formData.referralType === 'internal'
    },
    referralTypeLabel() {
      return this.isInternalReferral ? '院内转诊' : '院外转诊'
    },
    referralTypeDesc() {
      return this.isInternalReferral
        ? '院内转诊：由校医院协调院内科室继续治疗，流程更顺畅。'
        : '院外转诊：转往合作医院，享受更多诊疗资源。'
    },
    targetScopeLabel() {
      return this.isInternalReferral ? '院内科室' : '合作医院'
    },
    currentTargetDisplay() {
      if (this.isInternalReferral) {
        return this.formData.targetDepartment
          ? `本校医院 · ${this.formData.targetDepartment}`
          : '尚未选择院内科室'
      }
      return this.formData.targetHospital || '未选择合作医院'
    },
  },
  watch: {
    'userStore.userInfo': {
      handler() {
        this.fillPatientInfo()
      },
      deep: true,
      immediate: true,
    },
    'formData.referralType': {
      handler: async function (newType) {
        if (newType === 'external') {
          await this.loadHospitals()
          if (this.hospitals.length > 0) {
            this.formData.targetHospital = this.hospitals[this.hospitalIndex]?.name || ''
          }
        } else {
          this.formData.targetHospital = '本校医院'
        }
      },
    },
  },
  methods: {
    fillPatientFromVisitRecord(record) {
      if (!record) return
      const name = record.patientName || record.name || record.realname || ''
      if (name && !this.formData.patientName) {
        this.formData.patientName = name
      }

      const phone = record.patientPhone || record.phone || record.mobile || ''
      if (phone && !this.formData.phone) {
        this.formData.phone = phone
      }

      const gender = resolveGender(record.gender || record.patientGender || record.sex)
      if (gender) {
        this.formData.gender = gender
      }

      const age = record.patientAge || record.age
      if ((age || age === 0) && !this.formData.age) {
        this.formData.age = String(age)
      }

      if (!this.formData.symptoms && record.symptoms) {
        this.formData.symptoms = record.symptoms
      }

      if (!this.formData.medicalHistory && (record.diagnosis || record.medicalHistory)) {
        this.formData.medicalHistory = record.diagnosis || record.medicalHistory
      }

      if (!this.formData.reason && record.referralReason) {
        this.formData.reason = record.referralReason
      }
    },
    fillPatientFromSnapshot(snapshot) {
      if (!snapshot) return
      if (snapshot.name && !this.formData.patientName) this.formData.patientName = snapshot.name
      if (snapshot.phone && !this.formData.phone) this.formData.phone = snapshot.phone
      const gender = resolveGender(snapshot.gender)
      if (gender) this.formData.gender = gender
      if (snapshot.age && !this.formData.age) this.formData.age = String(snapshot.age)
    },
    closeDepartmentTree() {
      this.showDepartmentTree = false
    },
    async openDepartmentTree() {
      try {
        if (this.departmentTree.length === 0) {
          await this.loadDepartmentTree()
        }
        this.showDepartmentTree = true
      } catch (error) {
        console.error('打开科室选择失败:', error)
        uni.showToast({ title: '无法打开科室选择', icon: 'none' })
      }
    },
    toggleDepartment(department) {
      if (!department || !department.children || department.children.length === 0) {
        return
      }
      department.expanded = !department.expanded
    },
    async loadDepartmentTree() {
      try {
        this.loadingDepartment = true
        const response = await getDepartmentTree()
        let data = response?.data ?? response?.result ?? response
        if (!Array.isArray(data)) data = []
        this.departmentTree = data.map((dept) => ({
          deptId: normalizeDeptId(dept),
          deptName: dept.deptName || dept.name || '未知科室',
          children: (dept.children || dept.subDepartments || []).map((child) => ({
            deptId: normalizeDeptId(child),
            deptName: child.deptName || child.name || '未知科室',
          })),
          expanded: false,
        }))
      } catch (error) {
        console.error('加载科室树失败:', error)
        uni.showToast({ title: '加载科室失败', icon: 'none' })
      } finally {
        this.loadingDepartment = false
      }
    },
    selectDepartment(dept) {
      const id = normalizeDeptId(dept)
      this.selectedDeptId = id
      this.selectedDepartment = {
        deptId: id,
        deptName: dept.deptName || dept.name || '未知科室',
      }
    },
    confirmDepartmentSelection() {
      if (this.selectedDepartment) {
        this.formData.targetDepartment = this.selectedDepartment.deptName
        this.formData.targetDeptId = this.selectedDepartment.deptId || null
        this.formData.targetHospital = '本校医院'
      }
      this.closeDepartmentTree()
    },
    async loadHospitals(force = false) {
      if (this.hospitalLoading) return
      if (!force && this.hospitals.length > 0) {
        const matchedIndex = this.hospitals.findIndex((item) => item.name === this.formData.targetHospital)
        if (matchedIndex >= 0) {
          this.hospitalIndex = matchedIndex
        }
        return
      }

      try {
        this.hospitalLoading = true
        const response = await getReferralHospitals()
        let list = response?.data ?? response?.result ?? response
        if (!Array.isArray(list)) {
          list = []
        }
        this.hospitals = list.map((item) => ({
          id: item.id ?? item.hospitalId ?? item.value ?? null,
          name: item.name || item.label || item.hospitalName || '未知医院',
          level: item.level || item.hospitalLevel || '',
          address: item.address || item.hospitalAddress || '',
        }))

        if (this.hospitals.length > 0) {
          const matchedIndex = this.hospitals.findIndex((item) => item.name === this.formData.targetHospital)
          this.hospitalIndex = matchedIndex >= 0 ? matchedIndex : 0
          this.formData.targetHospital = this.hospitals[this.hospitalIndex]?.name || ''
        } else {
          this.hospitalIndex = 0
          this.formData.targetHospital = ''
        }
      } catch (error) {
        console.error('加载合作医院失败:', error)
        uni.showToast({ title: '合作医院加载失败', icon: 'none' })
      } finally {
        this.hospitalLoading = false
      }
    },
    onHospitalChange(event) {
      const index = Number(event?.detail?.value ?? this.hospitalIndex ?? 0)
      this.hospitalIndex = Number.isNaN(index) ? 0 : index
      const target = this.hospitals[this.hospitalIndex]
      this.formData.targetHospital = target?.name || ''
    },
    handleReferralTypeChange(type) {
      if (!type || this.formData.referralType === type) {
        return
      }

      this.formData.referralType = type
      this.formData.targetType = type === 'internal' ? 'INTERNAL' : 'EXTERNAL'

      if (type === 'internal') {
        this.formData.targetHospital = '本校医院'
        if (!this.selectedDepartment) {
          this.formData.targetDepartment = ''
          this.formData.targetDeptId = null
        }
      } else {
        this.selectedDeptId = ''
        this.selectedDepartment = null
        this.formData.targetDepartment = ''
        this.formData.targetDeptId = null
        this.loadHospitals(true).then(() => {
          if (this.hospitals.length > 0) {
            this.formData.targetHospital = this.hospitals[this.hospitalIndex]?.name || ''
          }
        })
      }
    },
    selectReferralType(type) {
      this.handleReferralTypeChange(type)
    },
    handleGenderChange(event) {
      const gender = resolveGender(event?.detail?.value)
      if (gender) {
        this.formData.gender = gender
      }
    },
    chooseImage() {
      const remaining = this.MAX_ATTACHMENTS - this.formData.attachments.length
      if (remaining <= 0) {
        uni.showToast({ title: `最多上传${this.MAX_ATTACHMENTS}张图片`, icon: 'none' })
        return
      }

      uni.chooseImage({
        count: remaining,
        sizeType: ['compressed', 'original'],
        success: (res) => {
          const tempFiles = res?.tempFiles || []
          const tempPaths = res?.tempFilePaths || []
          const attachmentsToAdd = []
          let hasOversize = false

          const sources = tempFiles.length > 0 ? tempFiles : tempPaths.map((path) => ({ path }))

          sources.forEach((file, index) => {
            const size = file.size ?? tempFiles[index]?.size ?? 0
            if (size && size > this.MAX_ATTACHMENT_SIZE) {
              hasOversize = true
              return
            }

            const url = file.path || tempPaths[index]
            if (!url) return

            attachmentsToAdd.push({
              url,
              size,
              name: file.name || `附件${this.formData.attachments.length + attachmentsToAdd.length + 1}`,
            })
          })

          if (attachmentsToAdd.length) {
            this.formData.attachments.push(...attachmentsToAdd)
          }

          if (hasOversize) {
            uni.showToast({ title: '单张图片需小于2MB', icon: 'none' })
          }
        },
        fail: (error) => {
          console.error('选择图片失败:', error)
          uni.showToast({ title: '选择图片失败', icon: 'none' })
        },
      })
    },
    deleteAttachment(index) {
      if (index < 0 || index >= this.formData.attachments.length) return
      this.formData.attachments.splice(index, 1)
    },
    fillPatientInfo() {
      try {
        const userInfo = this.userStore?.userInfo
        if (!userInfo) {
          return
        }

        if (userInfo.name && !this.formData.patientName) {
          this.formData.patientName = userInfo.name
        }
        if (userInfo.phone && !this.formData.phone) {
          this.formData.phone = userInfo.phone
        }
        const genderFromUser = resolveGender(userInfo.gender)
        if (genderFromUser) {
          this.formData.gender = genderFromUser
        }
        if (userInfo.age && !this.formData.age) {
          this.formData.age = String(userInfo.age)
        }

        if (userInfo.patient) {
          const patient = userInfo.patient
          if (patient.name && !this.formData.patientName) {
            this.formData.patientName = patient.name
          }
          if (patient.phone && !this.formData.phone) {
            this.formData.phone = patient.phone
          }
          const genderFromPatient = resolveGender(patient.gender)
          if (genderFromPatient) {
            this.formData.gender = genderFromPatient
          }
          if (patient.age && !this.formData.age) {
            this.formData.age = String(patient.age)
          }
        }

        if (!this.formData.age && userInfo.birthDate) {
          const calculatedAge = calculateAgeFromBirth(userInfo.birthDate)
          if (calculatedAge !== '') {
            this.formData.age = String(calculatedAge)
          }
        }
      } catch (error) {
        console.warn('填充患者信息失败:', error)
      }
    },
    async prefillFormData() {
      let stored = this.referralInfo
      if (!stored) {
        const cached = uni.getStorageSync('referralInfo')
        if (cached) {
          try {
            stored = typeof cached === 'string' ? JSON.parse(cached) : cached
            this.referralInfo = stored
          } catch (error) {
            console.warn('解析缓存转诊信息失败:', error)
          }
        }
      }

      if (stored && stored.patient) {
        this.fillPatientFromSnapshot(stored.patient)
      } else {
        const snapshot = uni.getStorageSync('referralPatientSnapshot')
        if (snapshot) {
          this.fillPatientFromSnapshot(snapshot)
        }
      }

      if (stored && stored.visitRecord) {
        this.selectedVisitRecord = stored.visitRecord
        this.formData.visitRecordId = stored.visitRecord.id || stored.visitRecord.recordId || ''
        this.fillPatientFromVisitRecord(this.selectedVisitRecord)
      }

      if (stored && stored.type) this.formData.referralType = stored.type
      if (stored && stored.targetHospital) this.formData.targetHospital = stored.targetHospital
      if (stored && stored.targetDepartment) this.formData.targetDepartment = stored.targetDepartment
      if (stored && stored.targetDeptId) {
        const normalized = String(stored.targetDeptId)
        this.formData.targetDeptId = normalized
        this.selectedDeptId = normalized
        this.selectedDepartment = { deptId: normalized, deptName: stored.targetDepartment }
      }

      this.formData.targetType = this.formData.referralType === 'internal' ? 'INTERNAL' : 'EXTERNAL'

      if (!this.isInternalReferral) {
        await this.loadHospitals()

        if (this.formData.targetHospital) {
          const idx = this.hospitals.findIndex((item) => item.name === this.formData.targetHospital)
          if (idx >= 0) this.hospitalIndex = idx
        }
      }
    },
    async submitApplication() {
      if (this.submitting) return

      const errors = []
      if (!this.formData.patientName) errors.push('请填写患者姓名')
      if (!this.formData.gender) errors.push('请选择患者性别')
      if (!this.formData.age) errors.push('请填写患者年龄')
      if (!this.formData.phone) errors.push('请填写联系电话')
      if (!this.formData.symptoms) errors.push('请描述症状信息')
      if (!this.formData.reason) errors.push('请填写转诊原因')

      if (this.isInternalReferral) {
        if (!this.formData.targetDeptId) {
          errors.push('请选择院内科室')
        }
      } else if (!this.formData.targetHospital) {
        errors.push('请选择合作医院')
      }

      if (errors.length) {
        uni.showToast({ title: errors[0], icon: 'none' })
        return
      }

      try {
        this.submitting = true
        const isInternal = this.isInternalReferral
        const payload = {
          patientName: this.formData.patientName,
          gender: this.formData.gender,
          age: this.formData.age ? Number(this.formData.age) : null,
          phone: this.formData.phone,
          symptoms: this.formData.symptoms,
          medicalHistory: this.formData.medicalHistory,
          reason: this.formData.reason,
          sourceType: this.selectedVisitRecord ? 'PATIENT_AFTER' : 'PATIENT_BEFORE',
          targetType: isInternal ? 'INTERNAL' : 'EXTERNAL',
          targetDeptId: isInternal && this.formData.targetDeptId ? Number(this.formData.targetDeptId) : null,
          targetDeptName: isInternal ? this.formData.targetDepartment : null,
          targetHospitalName: isInternal ? '校医院' : this.formData.targetHospital,
          attachments: this.formData.attachments.map((item, index) => ({
            name: item.name || `附件${index + 1}`,
            url: item.url,
            type: item.type || 'image'
          }))
        }

        const response = await submitReferralApplication(payload)
        if (response && response.id) {
          uni.showToast({ title: '提交成功', icon: 'success' })
          uni.removeStorageSync('referralInfo')
          setTimeout(() => {
            uni.redirectTo({
              url: '/subpkg/hospital/referral-records'
            })
          }, 600)
        } else {
          uni.showToast({ title: '提交失败，请稍后重试', icon: 'none' })
        }
      } catch (error) {
        console.error('提交转诊申请失败:', error)
        uni.showToast({ title: '提交失败，请检查网络', icon: 'none' })
      } finally {
        this.submitting = false
      }
    },
  },
  async mounted() {
    try {
      const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
      const currentPage = pages.length ? pages[pages.length - 1] : null
      const options = currentPage?.options || {}

      if (options.recordData) {
        try {
          const parsed = JSON.parse(decodeURIComponent(options.recordData))
          this.selectedVisitRecord = parsed
        } catch (error) {
          console.warn('解析就诊记录失败:', error)
        }
      } else if (!this.selectedVisitRecord) {
        const cachedRecord = uni.getStorageSync('selectedVisitRecord')
        if (cachedRecord) {
          try {
            this.selectedVisitRecord = typeof cachedRecord === 'string' ? JSON.parse(cachedRecord) : cachedRecord
          } catch (error) {
            this.selectedVisitRecord = cachedRecord
          }
        }
      }

      if (this.selectedVisitRecord) {
        this.formData.visitRecordId =
          this.selectedVisitRecord.id || this.selectedVisitRecord.recordId || this.selectedVisitRecord.registrationNo || ''
        this.fillPatientFromVisitRecord(this.selectedVisitRecord)
      }

      const cachedSnapshot = uni.getStorageSync('referralPatientSnapshot')
      if (cachedSnapshot) {
        this.fillPatientFromSnapshot(cachedSnapshot)
      }

      await this.prefillFormData()
      this.fillPatientInfo()

      if (!this.isInternalReferral) {
        await this.loadHospitals()
      } else {
        this.formData.targetHospital = '本校医院'
      }
    } catch (error) {
      console.error('页面初始化失败:', error)
    }
  },
  onShow() {
    try {
      this.userStore?.initFromStorage?.()
      this.fillPatientInfo()
      if (this.selectedVisitRecord) {
        this.fillPatientFromVisitRecord(this.selectedVisitRecord)
      }
      const snapshot = uni.getStorageSync('referralPatientSnapshot')
      if (snapshot) {
        this.fillPatientFromSnapshot(snapshot)
      }
    } catch (error) {
      console.warn('onShow 初始化失败:', error)
    }
  },
}
</script>

<style scoped>
/* 根容器设置，确保整个页面正确布局 */
:deep(#app) {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

.referral-bg {
  background-color: #f5f7fb;
  min-height: 100vh;
  padding-bottom: 20px;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.page-header {
  background: linear-gradient(90deg, #4fa2ff, #1989fa);
  color: #fff;
  padding: 18px 16px;
  text-align: center;
  position: sticky;
  top: 0;
  z-index: 10;
  width: 100%;
  box-sizing: border-box;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.content {
  padding: 16px;
  flex: 1;
  box-sizing: border-box;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  overflow: hidden;
}

.form-section {
  background-color: #fff;
  border-radius: 14px;
  padding: 18px;
  margin-bottom: 16px;
  box-shadow: 0 10px 28px rgba(79, 162, 255, 0.08);
}

.type-section {
  padding-bottom: 14px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  position: relative;
  padding-left: 10px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background-image: linear-gradient(180deg, #54a8ff, #1989fa);
}

.type-options {
  display: flex;
  gap: 12px;
}

.type-option {
  flex: 1;
  background: #f6f8fd;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid transparent;
  transition: all 0.25s ease;
}

.type-option.active {
  background: linear-gradient(180deg, rgba(25, 137, 250, 0.12), rgba(25, 137, 250, 0.05));
  border-color: rgba(25, 137, 250, 0.45);
  box-shadow: 0 8px 20px rgba(25, 137, 250, 0.16);
}

.option-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.option-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.option-badge {
  font-size: 12px;
  color: #fff;
  background: #1989fa;
  border-radius: 10px;
  padding: 2px 8px;
}

.option-desc {
  margin-top: 8px;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
}

.type-tip {
  margin-top: 14px;
  padding: 10px 12px;
  background: rgba(25, 137, 250, 0.1);
  border-radius: 10px;
  color: #2563eb;
  font-size: 13px;
}

.status-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-chip {
  padding: 4px 12px;
  border-radius: 12px;
  background: rgba(25, 137, 250, 0.18);
  color: #155ab6;
  font-size: 13px;
  font-weight: 500;
}

.status-hint {
  font-size: 13px;
  color: #6b7280;
}

.status-summary {
  font-size: 14px;
  color: #2563eb;
  font-weight: 500;
}

.selection-mode {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}

.mode-option {
  flex: 1;
  padding: 14px;
  border-radius: 12px;
  background: #f7f9fd;
  border: 1px solid transparent;
  transition: all 0.25s ease;
}

.mode-option.active {
  border-color: rgba(25, 137, 250, 0.45);
  background: linear-gradient(180deg, rgba(25, 137, 250, 0.12), rgba(25, 137, 250, 0.05));
  box-shadow: 0 8px 20px rgba(25, 137, 250, 0.14);
}

.mode-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mode-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.mode-badge {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  color: #1d4ed8;
  background: rgba(37, 99, 235, 0.12);
}

.mode-desc {
  margin-top: 6px;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
}

.target-card {
  background: #f9fbff;
  border-radius: 12px;
  border: 1px solid rgba(37, 99, 235, 0.12);
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.target-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.target-label {
  font-size: 14px;
  color: #4b5563;
}

.target-value {
  font-size: 14px;
  color: #1f2937;
  flex: 1;
  text-align: right;
}

.target-select {
  cursor: pointer;
}

.target-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #2563eb;
}

.target-arrow {
  font-size: 18px;
  line-height: 1;
}

.target-picker {
  flex: 1;
}

.picker-value {
  text-align: right;
  color: #1f2937;
}

.target-input .inline-input {
  flex: 1;
  margin-left: 12px;
}

.form-item {
  margin-bottom: 16px;
}

.label {
  font-size: 14px;
  color: #4b5563;
  margin-bottom: 6px;
  display: block;
}

.input {
  width: 100%;
  height: 40px;
  border: 1px solid #e0e7ff;
  border-radius: 10px;
  padding: 0 12px;
  font-size: 14px;
  box-sizing: border-box;
  background: #fff;
}

.inline-input {
  height: 38px;
}

.textarea {
  width: 100%;
  border: 1px solid #e0e7ff;
  border-radius: 10px;
  padding: 12px;
  font-size: 14px;
  box-sizing: border-box;
  min-height: 110px;
}

.radio-group {
  display: flex;
  gap: 16px;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #4b5563;
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
}

.radio-item.active {
  color: #2563eb;
  border-color: rgba(37, 99, 235, 0.4);
  background: rgba(37, 99, 235, 0.08);
}

.upload-section {
  margin-top: 8px;
}

.upload-tips {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 12px;
}

.upload-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.upload-item {
  position: relative;
  width: 92px;
  height: 92px;
}

.upload-img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 20px;
  height: 20px;
  background-color: rgba(17, 24, 39, 0.75);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.upload-btn {
  width: 92px;
  height: 92px;
  border: 1px dashed #d1d5db;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  gap: 4px;
}

.upload-icon {
  font-size: 22px;
}

.upload-text {
  font-size: 12px;
}

.submit-section {
  padding: 20px 0 10px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  background-color: #1989fa;
  color: #fff;
  border: none;
  border-radius: 22px;
  font-size: 16px;
  font-weight: bold;
}

.submit-btn:active {
  background-color: #0e77d0;
}

/* 科室选择模态框样式 */
.department-tree-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-content {
  position: relative;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  background-color: #fff;
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
  z-index: 1001;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  font-size: 24px;
  color: #6b7280;
  cursor: pointer;
}

.department-tree-container {
  flex: 1;
  overflow: hidden;
}

.loading {
  padding: 40px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.department-tree-scroll {
  height: 360px;
}

.department-tree {
  padding: 10px 0;
}

.department-item {
  margin-bottom: 10px;
}

.primary-department {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}

.primary-department:active {
  background-color: #f5f7fa;
}

.secondary-department-list {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.secondary-department-list.expanded {
  max-height: 500px;
}

.secondary-department {
  padding: 10px 20px 10px 40px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.secondary-department:active,
.secondary-department.selected {
  background-color: rgba(25, 137, 250, 0.1);
  color: #1989fa;
}

.department-name {
  font-size: 15px;
  color: #1f2937;
}

.secondary-department.selected .department-name {
  color: #1989fa;
}

.toggle-icon {
  font-size: 12px;
  color: #6b7280;
  transition: transform 0.2s ease;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
}

.cancel-btn,
.confirm-btn {
  flex: 1;
  height: 40px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.cancel-btn {
  background-color: #f3f4f6;
  color: #4b5563;
  border: none;
}

.confirm-btn {
  background-color: #1989fa;
  color: #fff;
  border: none;
}

.confirm-btn:disabled {
  background-color: #93c5fd;
  opacity: 0.6;
}

.cancel-btn:active {
  background-color: #e5e7eb;
}

.confirm-btn:active:not(:disabled) {
  background-color: #0e77d0;
}
</style>