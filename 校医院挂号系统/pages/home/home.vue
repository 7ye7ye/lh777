<template>
  <view class="home-bg">
    <view class="home-header">
      <text class="title">北京交通大学校医院</text>
      <view class="header-icons">
        <view class="header-icon">⚙️</view>
      </view>
    </view>
    <view class="banner">
      <image :src="bannerImage || getStaticImage('/static/hospitalpicture.png')" mode="aspectFill" style="width: 100%; height: 100%; border-radius: 12rpx;" />
    </view>
    <view class="visit-card card" @click="onVisitCardClick">
      <view class="visit-card-content">
        <view class="visit-card-left">
          <view class="patient-basic-info" v-if="cardInfo && cardInfo.patientId">
            <text class="patient-name">{{ cardInfo.patientName || '未知' }}</text>
            <text class="patient-gender" v-if="cardInfo.gender">{{ formatGender(cardInfo.gender) }}</text>
            <text class="patient-age" v-if="cardInfo.birthDate || cardInfo.age">{{ formatAge(cardInfo.birthDate, cardInfo.age) }}岁</text>
          </view>
          <view class="patient-basic-info-empty" v-else>
            <text>点击绑定就诊卡</text>
          </view>
          <view class="ecard-label-wrapper">
            <view class="ecard-label">电子就诊卡</view>
          </view>
          <view class="card-number-row" v-if="cardInfo.patientId">
            <text class="card-number-text">证件号: {{ cardInfo.outpatientNumber || cardInfo.cardNumber || cardInfo.idCard || '-' }}</text>
          </view>
        </view>
        <view class="visit-card-right" v-if="cardInfo.patientId">
          <view class="card-status">
            <text class="status-text">已绑定</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 快速功能 -->
    <view class="quick card">
      <view class="quick-grid">
        <view class="quick-item-large quick-item-blue" @click="goToDiseaseGuide">
          <view class="quick-item-content">
            <image class="quick-icon-large" :src="diseaseBookingIcon || getStaticImage('/static/disease-booking.svg')" mode="aspectFit"></image>
            <text class="quick-text-large">按疾病挂号</text>
            <text class="quick-desc">根据症状快速匹配科室</text>
          </view>
        </view>
        <view class="quick-item-large quick-item-green" @click="goToDepartmentBooking">
          <view class="quick-item-content">
            <image class="quick-icon-large" :src="departmentBookingIcon || getStaticImage('/static/department-booking.svg')" mode="aspectFit"></image>
            <text class="quick-text-large">按科室挂号</text>
            <text class="quick-desc">选择科室预约就诊</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 功能标签页 -->
    <view class="home-tabs card">
      <view
        v-for="(tab, idx) in tabs"
        :key="tab"
        class="tab"
        :class="{ active: idx === activeIndex }"
        @click="activeIndex = idx"
      >{{ tab }}</view>
    </view>
    <view class="home-section card" style="margin-top: 0;">
      <view class="home-grid">
        <view
          v-for="item in currentItems"
          :key="item.text"
          class="home-item"
          @click="onItemClick(item)"
        >
          <view class="icon">
            <image v-if="item.image" :src="item.image" class="icon-image" mode="aspectFit" />
            <text v-else>{{ item.icon }}</text>
          </view>
          <text>{{ item.text }}</text>
        </view>
      </view>
    </view>
    <!-- 未登录内联提示 -->
    <LoginPrompt ref="loginPromptRef" mode="inline" message="登录后可出示电子就诊码" login-text="去登录" />
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import LoginPrompt from '@/components/LoginPrompt.vue'
import { AUTH_REQUIRED_FEATURES, createAuthHandler } from '@/utils/auth'
import { userApi } from '@/api/user'
import { useUserStore } from '@/store/user'
import { uniNavigateTo } from '@/utils/uniHelper'
import { patientApi } from '@/api/patient'
import uqrcode from '@/uni_modules/Sansnn-uQRCode/components/uqrcode/uqrcode.vue'
import { getStaticImage } from '@/utils/imageHelper'
import { preloadImage } from '@/utils/imagePreloader'
import { getStaticImage } from '@/utils/imageHelper'
import { preloadImage } from '@/utils/imagePreloader'

const tabs = ['门诊', '体检', '其他']
const activeIndex = ref(0)
const loginPromptRef = ref(null)
const userStore = useUserStore()
const cardInfo = ref({})
const qrcodeRef = ref(null)

// 预下载的图片路径
const bannerImage = ref('')
const diseaseBookingIcon = ref('')
const departmentBookingIcon = ref('')

// 监听cardInfo变化，用于调试
watch(() => cardInfo.value, (newVal) => {
  console.log('cardInfo变化:', JSON.parse(JSON.stringify(newVal || {})))
  console.log('patientId:', newVal?.patientId)
  console.log('patientName:', newVal?.patientName)
  console.log('gender:', newVal?.gender)
  console.log('birthDate:', newVal?.birthDate)
}, { deep: true, immediate: true })

const itemsMap = {
  门诊: [
    { icon: 'patient', image: getStaticImage('/static/patient.svg'), text: '我的就诊人', action: 'goToMyPatient' },
    { icon: 'register', image: getStaticImage('/static/register.svg'), text: '挂号记录', action: 'goToRegisterRecord' },
    { icon: 'hospital', image: getStaticImage('/static/hospital.svg'), text: '就诊记录', action: 'goToHospitalRecord' },
    { icon: 'referral', image: getStaticImage('/static/referral-record.svg'), text: '转诊记录', action: 'goToTransferHistory' },
  ],
  体检: [
    { icon: 'personal-exam', image: getStaticImage('/static/presonal-exam.svg'), text: '个检预约' },
    { icon: 'group-exam', image: getStaticImage('/static/group-exam.svg'), text: '团检预约' },
    { icon: 'exam-report', image: getStaticImage('/static/exam-report.svg'), text: '体检报告' },
    { icon: 'exam-order', image: getStaticImage('/static/exam-order.svg'), text: '体检订单' },
    { icon: 'exam-center', image: getStaticImage('/static/exam-center.svg'), text: '体检中心' },
  ],
  其他: [
    { icon: 'department', image: getStaticImage('/static/department-introduce.svg'), text: '科室介绍', action: 'goDepartments' },
    { icon: 'doctor', image: getStaticImage('/static/doctor-introduce.svg'), text: '专家介绍', action: 'goDoctors' },
    { icon: 'navigation', image: getStaticImage('/static/inhospital_navigation.svg'), text: '院内导航', action: 'goNavigation' },
    { icon: 'help', image: getStaticImage('/static/help.svg'), text: '帮助反馈', action: 'goToHelp' },
  ],
}

const currentItems = computed(() => itemsMap[tabs[activeIndex.value]] || [])

const onItemClick = (item) => {
  // 如果有action字段，调用对应的函数
  if (item.action) {
    const actionMap = {
      'goToMyCard': goToMyCard,
      'goToMyPatient': goToMyPatient,
      'goToRegisterRecord': goToRegisterRecord,
      'goToHospitalRecord': goToHospitalRecord,
      'goToOutpatientRecord': goToOutpatientRecord,
      'goToTransferHistory': goToTransferHistory,
      'goToConsultRecord': goToConsultRecord,
      'goToCheckRecord': goToCheckRecord,
      'goDepartments': goDepartments,
      'goDoctors': goDoctors,
      'goNavigation': goNavigation,
      'goToHelp': goToHelp,
    }

    const actionFunc = actionMap[item.action]
    if (actionFunc) {
      actionFunc()
      return
    }
  }

  // 体检功能跳转映射
  const examRoutes = {
    '个检预约': '/subpkg/physical-exam/individual-booking',
    '团检预约': '/subpkg/physical-exam/group-booking',
    '体检报告': '/subpkg/physical-exam/exam-report',
    '体检订单': '/subpkg/physical-exam/exam-orders',
    '体检中心': '/subpkg/physical-exam/exam-center'
  }

  const route = examRoutes[item.text]
  if (route) {
    uni.navigateTo({
      url: route,
      success: () => {
        console.log('跳转成功:', item.text)
      },
      fail: (err) => {
        console.error('跳转失败:', err)
        uni.showToast({
          title: '页面未找到',
          icon: 'none'
        })
      }
    })
  }
  // 如果没有路由映射也没有action，说明该功能未实现，不显示任何提示
}

// 跳转到按疾病挂号
const goToDiseaseGuide = createAuthHandler(
  AUTH_REQUIRED_FEATURES.HOME.DISEASE_BOOKING,
  '/subpkg/hospital/disease-guide'
)

// 跳转到按科室挂号
const goToDepartmentBooking = createAuthHandler(
  AUTH_REQUIRED_FEATURES.HOME.DEPARTMENT_BOOKING,
  '/subpkg/hospital/department-booking'
)

// 使用统一的权限控制创建导航函数
const goToMyCard = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.MY_CARD,
  '/subpkg/profile/personal/mycard',
  { requireCard: true }
)

const goToMyPatient = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.MY_PATIENT,
  '/subpkg/profile/personal/mypatient'
)

const goToRegisterRecord = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
  '/subpkg/profile/records/register-record'
)

const goToOutpatientRecord = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
  '/subpkg/profile/records/outpatient-record'
)

const goToHospitalRecord = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
  '/subpkg/profile/records/hospital-record'
)

const goToConsultRecord = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
  '/subpkg/profile/records/consult-record'
)

const goToCheckRecord = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
  '/subpkg/profile/records/check-record'
)

const goToTransferHistory = createAuthHandler(
  AUTH_REQUIRED_FEATURES.HOME.REFERRAL_RECORDS,
  '/subpkg/hospital/referral-records'
)

const goToHelp = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS,
  '/subpkg/profile/help/help'
)

// 医院信息相关功能
const goDepartments = createAuthHandler(
  AUTH_REQUIRED_FEATURES.HOME.DEPARTMENT_INTRODUCTION,
  '/subpkg/hospital/departments'
)

const goDoctors = createAuthHandler(
  AUTH_REQUIRED_FEATURES.HOME.DOCTOR_INTRODUCTION,
  '/subpkg/hospital/doctors'
)

const goNavigation = () => {
  uni.navigateTo({ url: '/subpkg/hospital/navigation' })
}


// 获取就诊卡信息
const loadCardInfo = async () => {
  try {
    userStore.initFromStorage() // 确保从本地存储加载用户信息
    const userId = userStore.userInfo?.userId
    if (!userId) {
      console.log('未获取到用户ID，无法加载就诊卡信息')
      cardInfo.value = {}
      return
    }

    console.log('开始获取就诊卡信息，userId:', userId)
    const res = await patientApi.getCard({ userId })
    console.log('主页获取到的就诊卡API响应:', res)

    // 响应拦截器可能已经处理了响应，直接使用res
    // 但如果响应拦截器返回的是{code, data}结构，需要提取data
    let cardData = null
    if (res && res.patientId) {
      // 如果返回的直接是就诊卡数据对象（响应拦截器已处理）
      cardData = res
    } else if (res && res.code === 200 && res.data) {
      // 如果返回的是 {code: 200, data: {...}} 格式（响应拦截器未处理）
      cardData = res.data
    } else if (res && res.data && res.data.patientId) {
      // 如果返回的是 {data: {...}} 格式
      cardData = res.data
    }

    if (cardData && cardData.patientId) {
      // 先保留所有原始数据
      const mappedData = { ...cardData }

      // 然后统一字段映射，确保覆盖所有可能的字段名（包括下划线和驼峰命名）
      // 基础标识（优先使用驼峰命名）
      mappedData.patientId = cardData.patientId || cardData.patient_id || mappedData.patientId || null

      // 姓名（多种可能的字段名，优先使用驼峰命名）
      mappedData.patientName = cardData.patientName || cardData.patient_name || cardData.name || mappedData.patientName || ''

      // 性别
      mappedData.gender = cardData.gender || mappedData.gender || ''

      // 出生日期（多种可能的字段名）
      mappedData.birthDate = cardData.birthDate || cardData.birth_date || cardData.birthday || cardData.birthDay || mappedData.birthDate || ''

      // 年龄
      mappedData.age = cardData.age || mappedData.age || null

      // 门诊号（多种可能的字段名）
      mappedData.outpatientNumber = cardData.outpatientNumber || cardData.outpatient_number || cardData.outpatientNo || cardData.outpatient_no || mappedData.outpatientNumber || ''

      // 卡号
      mappedData.cardNumber = cardData.cardNumber || cardData.card_number || cardData.cardNo || cardData.card_no || mappedData.cardNumber || ''

      // 身份证号
      mappedData.idCard = cardData.idCard || cardData.id_card || cardData.idCardNumber || cardData.id_card_number || mappedData.idCard || ''

      // 如果门诊号为空，尝试使用身份证号作为显示
      if (!mappedData.outpatientNumber && !mappedData.cardNumber && mappedData.idCard) {
        mappedData.outpatientNumber = mappedData.idCard
      }

      // 确保 patientName 有值（如果所有字段都为空，使用默认值）
      if (!mappedData.patientName || mappedData.patientName === '') {
        mappedData.patientName = '未知'
      }

      // 直接赋值，确保响应式更新（Vue 3 的 ref 需要直接赋值整个对象）
      cardInfo.value = { ...mappedData }

      console.log('就诊卡信息已设置:', JSON.parse(JSON.stringify(cardInfo.value)))
      console.log('cardInfo.patientId:', cardInfo.value.patientId)
      console.log('患者姓名:', cardInfo.value.patientName)
      console.log('性别:', cardData.gender, '格式化后:', formatGender(cardData.gender))
      console.log('年龄:', cardInfo.value.age, '出生日期:', cardInfo.value.birthDate)
      console.log('门诊号:', cardInfo.value.outpatientNumber || cardInfo.value.cardNumber || cardInfo.value.idCard)

      // 强制触发响应式更新
      await nextTick()
      console.log('DOM更新后，cardInfo.value:', JSON.parse(JSON.stringify(cardInfo.value)))
      console.log('cardInfo.value.patientId 检查:', cardInfo.value?.patientId)
      console.log('cardInfo.value.patientName 检查:', cardInfo.value?.patientName)
    } else {
      console.log('未获取到有效就诊卡数据，API响应:', res)
      cardInfo.value = {}
    }
  } catch (error) {
    console.error('获取就诊卡信息失败:', error)
    cardInfo.value = {}
  }
}

// 格式化性别
const formatGender = (gender) => {
  if (!gender) return ''
  const str = String(gender).trim()
  if (str === '1' || str === '男' || str.toLowerCase() === 'male') return '男'
  if (str === '2' || str === '女' || str.toLowerCase() === 'female') return '女'
  return str === '男' || str === '女' ? str : ''
}

// 格式化年龄
const formatAge = (birthDate, age) => {
  if (age) return String(age)
  if (!birthDate) return ''
  try {
    const birth = new Date(birthDate)
    if (Number.isNaN(birth.getTime())) return ''
    const now = new Date()
    let calculatedAge = now.getFullYear() - birth.getFullYear()
    const monthDiff = now.getMonth() - birth.getMonth()
    if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < birth.getDate())) {
      calculatedAge -= 1
    }
    return calculatedAge >= 0 ? String(calculatedAge) : ''
  } catch (error) {
    console.warn('计算年龄失败:', error)
    return ''
  }
}

// 切换就诊人
const onSwitchPatient = () => {
  uni.navigateTo({
    url: '/subpkg/profile/personal/mypatient',
    fail: (err) => {
      console.error('跳转失败:', err)
    }
  })
}

// 使用统一的权限控制（需要就诊卡）
const onVisitCardClick = createAuthHandler(
  AUTH_REQUIRED_FEATURES.HOME.VISIT_CARD,
  '/subpkg/profile/personal/mycard',
  { requireCard: true }
)

// 预加载主要图片
const preloadImagePaths = async () => {
  try {
    console.log('🚀 开始预下载主要图片...')
    // 预下载主要图片（并行下载）
    const [banner, diseaseIcon, departmentIcon] = await Promise.all([
      preloadImage('/static/hospitalpicture.png'),
      preloadImage('/static/disease-booking.svg'),
      preloadImage('/static/department-booking.svg')
    ])

    console.log('📦 预下载结果:', {
      banner: banner,
      diseaseIcon: diseaseIcon,
      departmentIcon: departmentIcon
    })

    bannerImage.value = banner
    diseaseBookingIcon.value = diseaseIcon
    departmentBookingIcon.value = departmentIcon

    console.log('✅ 主要图片预下载完成，已更新响应式变量')
    console.log('🔍 当前图片路径:', {
      bannerImage: bannerImage.value,
      diseaseBookingIcon: diseaseBookingIcon.value,
      departmentBookingIcon: departmentBookingIcon.value
    })
  } catch (error) {
    console.error('❌ 图片预下载失败:', error)
  }
}

// 初始化用户信息
onMounted(() => {
  // 确保用户信息已从存储中恢复
  userStore.initFromStorage()
  // 预加载图片（使用 uni.downloadFile 绕过 ngrok 警告页面）
  preloadImagePaths()
  // 延迟加载，确保用户信息已初始化
  setTimeout(() => {
    loadCardInfo()
  }, 500)
})

onShow(() => {
  // 每次显示页面时重新加载就诊卡信息
  userStore.initFromStorage()
  setTimeout(() => {
    loadCardInfo()
  }, 200)
})
</script>

<style scoped>
.home-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 0;
}
.home-header {
  background: #3a9cff;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}
.title {
  color: #fff;
  font-size: 36rpx;
  font-weight: bold;
}
.header-icons {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.header-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  cursor: pointer;
}
.banner {
  width: calc(100% - 48rpx);
  height: 180rpx;
  margin: 16rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80rpx;
  background: #f0f0f0;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}
.visit-card {
  margin: 16rpx 24rpx 0 24rpx;
  padding: 32rpx 24rpx;
  background: linear-gradient(135deg, #e6f4ff 0%, #cce7ff 100%);
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(58, 156, 255, 0.15);
  position: relative;
  overflow: hidden;
}
.visit-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 70%);
  pointer-events: none;
}
.visit-card-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}
.visit-card-left {
  flex: 1;
  color: #333;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  padding-left: 40rpx;
  padding-right: 20rpx;
}
.patient-basic-info {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  gap: 12rpx;
  width: 100%;
  min-height: 50rpx;
}
.patient-name {
  font-size: 44rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
  color: #1a4d80 !important;
  flex-shrink: 0;
  line-height: 1.3;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
.patient-gender {
  font-size: 28rpx;
  color: #4a7ba7 !important;
  font-weight: 500;
  line-height: 1.3;
  margin-left: 12rpx;
}
.patient-age {
  font-size: 28rpx;
  color: #4a7ba7 !important;
  font-weight: 500;
  line-height: 1.3;
  margin-left: 8rpx;
}
.patient-basic-info-empty {
  font-size: 26rpx;
  color: #4a7ba7;
  opacity: 0.8;
  margin-bottom: 16rpx;
}
.ecard-label-wrapper {
  margin-bottom: 16rpx;
}
.ecard-label {
  display: inline-block;
  background: #fff;
  color: #3a9cff;
  font-size: 24rpx;
  font-weight: 500;
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}
.card-number-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 0;
}
.switch-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  color: #3a9cff;
  border-radius: 32rpx;
  padding: 10rpx 24rpx;
  border: 2rpx solid #3a9cff;
  flex-shrink: 0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}
.switch-icon {
  font-size: 24rpx;
  margin-right: 6rpx;
}
.switch-text {
  font-size: 24rpx;
  font-weight: 500;
}
.card-number-badge {
  display: inline-flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  padding: 6rpx 14rpx;
  border-radius: 12rpx;
  flex: 1;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}
.card-number-text {
  font-size: 26rpx;
  flex: 1;
  font-weight: 500;
  color: #4a7ba7 !important;
}
.visit-card-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #333;
  margin-left: 20rpx;
  padding-left: 20rpx;
  flex-shrink: 0;
}
.card-status {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border-radius: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.3);
}

.status-text {
  font-size: 26rpx;
  color: #fff;
  font-weight: 500;
}

.quick .quick-grid {
  display: flex;
  gap: 20rpx;
  padding: 0 24rpx;
}
.quick-item-large {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-radius: 20rpx;
  padding: 32rpx 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(74, 144, 226, 0.25);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}
.quick-item-blue {
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  box-shadow: 0 8rpx 24rpx rgba(74, 144, 226, 0.25);
}
.quick-item-blue:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.2);
}
.quick-item-green {
  background: linear-gradient(135deg, #1de9b6 0%, #4caf50 100%);
  box-shadow: 0 8rpx 24rpx rgba(29, 233, 182, 0.25);
}
.quick-item-green:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 16rpx rgba(29, 233, 182, 0.2);
}
.quick-item-large::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s;
}
.quick-item-large:active::before {
  opacity: 1;
}
.quick-item-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 1;
}
.quick-icon-large {
  width: 80rpx;
  height: 80rpx;
  margin-bottom: 16rpx;
  filter: drop-shadow(0 4rpx 8rpx rgba(0,0,0,0.1));
}
.quick-text-large {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 8rpx;
  text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.1);
}
.quick-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
  line-height: 1.4;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  margin: 16rpx 24rpx 0 24rpx;
  padding: 16rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}
.icon {
  width: 64rpx;
  height: 64rpx;
  margin-bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
}
.icon-image {
  width: 64rpx;
  height: 64rpx;
  display: block;
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
}
.home-section {
  margin-top: 0;
  padding-top: 0;
  padding-bottom: 0;
}
.home-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 8rpx 16rpx;
}
.home-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 10rpx 0;
  padding: 10rpx 8rpx;
  border-radius: 16rpx;
  transition: all 0.3s ease;
  box-sizing: border-box;
}
.home-item:active {
  background-color: rgba(58, 156, 255, 0.08);
  transform: scale(0.95);
}
.home-item text {
  font-size: 24rpx;
  color: #333;
  margin-top: 10rpx;
  text-align: center;
  line-height: 1.4;
  font-weight: 500;
  word-break: break-all;
  padding: 0 8rpx;
  box-sizing: border-box;
}
.home-tabs {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  margin: 16rpx 24rpx 0 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(58, 156, 255, 0.08);
  margin-bottom: 0;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  font-size: 30rpx;
  color: #666;
  position: relative;
  transition: all 0.3s ease;
}
.tab.active {
  color: #3a9cff;
  font-weight: 600;
  background: linear-gradient(to bottom, #f0f8ff, #ffffff);
}
.tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: linear-gradient(90deg, #3a9cff, #6ec6ff);
  border-radius: 2rpx;
}
.tabbar-placeholder {
  height: 0;
}
</style>
