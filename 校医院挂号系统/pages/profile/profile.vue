<template>
  <view class="profile-bg">
  <view class="profile-header">
      <view class="profile-info">
        <image 
          class="avatar" 
          :src="avatarError || !cardInfo.identityPhoto ? '/static/profile.svg' : cardInfo.identityPhoto" 
          mode="aspectFill"
          @error="onAvatarError"
        ></image>
        <view class="user-info">
          <text class="user-name">{{ cardInfo.patientName || userInfo.name || '微信用户' }}</text>
          <text class="user-phone">{{ cardInfo.phone || userInfo.phone || '*************' }}</text>
        </view>
      </view>
      <template v-if="!isLoggedIn">
        <button class="unbind-btn" size="mini" @click="goLogin">登录</button>
      </template>
      <template v-else>
        <button class="unbind-btn" size="mini" @click="handleLogout">退出登录</button>
      </template>
    </view>

    <view class="profile-section card centered centered-down first-card">
      <view class="profile-row">
        <view class="profile-item" @click="goToMyCard">
          <image class="icon icon-lg" src="/static/card.svg" />
          <text>我的就诊卡</text>
        </view>
        <view class="profile-item" @click="goToMyPatient">
          <image class="icon icon-lg" src="/static/patient.svg" />
          <text>我的就诊人</text>
        </view>
        <view class="profile-item" @click="goToMyDoctor">
          <image class="icon icon-lg" src="/static/doctor.svg" />
          <text>医生端</text>
        </view>
      </view>
    </view>

    <view class="profile-section card record-section">
      <view class="section-title">就诊记录</view>
      <view class="profile-row">
        <view class="profile-item" @click="goToRegisterRecord">
          <image class="icon" src="/static/register.svg" />
          <text>挂号记录</text>
        </view>
        <view class="profile-item" @click="goToHospitalRecord">
          <image class="icon" src="/static/hospital.svg" />
          <text>就诊记录</text>
        </view>
        <view class="profile-item" @click="goToTransferHistory">
          <image class="icon" src="/static/referral-record.svg" />
          <text>转诊记录</text>
        </view>
        <view class="profile-item" @click="goToOutpatientRecord">
          <image class="icon" src="/static/outpatient.svg" />
          <text>缴费记录</text>
        </view>
      </view>
      <view class="profile-row">
        <view class="profile-item" @click="goToRevisitRecord">
          <image class="icon" src="/static/record.svg" />
          <text>复诊记录</text>
        </view>
        <view class="profile-item" @click="goToCheckRecord">
          <image class="icon" src="/static/check.svg" />
          <text>检查预约</text>
        </view>
        <view class="profile-item" @click="goToConsultRecord">
          <image class="icon" src="/static/consult.svg" />
          <text>咨询记录</text>
        </view>
      </view>
    </view>

    <view class="profile-section card other-section">
      <view class="section-title">其他</view>
      <view class="profile-row">
        <view class="profile-item" @click="goToPrivacy">
          <image class="icon" src="/static/privacy.svg" />
          <text>隐私协议</text>
        </view>
        <view class="profile-item" @click="goToHelp">
          <image class="icon" src="/static/help.svg" />
          <text>帮助反馈</text>
        </view>
        <view class="profile-item" @click="goToComplain">
          <image class="icon" src="/static/complain.svg" />
          <text>投诉建议</text>
        </view>
        <view class="profile-item" @click="goToEvaluate">
          <image class="icon" src="/static/evaluate.svg" />
          <text>就诊评价</text>
        </view>
        <view class="profile-item" @click="goToIdentityVerify">
          <image class="icon" src="/static/privacy.svg" />
          <text>身份认证</text>
        </view>
      </view>
    </view>

    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { userApi } from '@/api/user'
import { useUserStore } from '@/store/user'
import { uniShowToast, uniSwitchTab, uniNavigateTo } from '@/utils/uniHelper'
import LoginPrompt from '@/components/LoginPrompt.vue'
import { AUTH_REQUIRED_FEATURES, createAuthHandler } from '@/utils/auth'
import { patientApi } from '@/api/patient'

const userInfo = ref({})
const cardInfo = ref({})
const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.isLoggedIn)
const avatarError = ref(false)

// 获取用户信息
const getUserInfo = () => {
  // 优先从状态管理获取用户信息
  if (userStore.userInfo) {
    userInfo.value = userStore.userInfo
  } else {
    // 如果状态管理中没有，则从API获取
    userApi.getCurrentUser().then(res => {
      userInfo.value = res.data
    }).catch(() => {
      // 如果获取失败，使用默认信息
      userInfo.value = { name: '微信用户', phone: '***********' }
    })
  }
}

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

// 进入医生端
const goToMyDoctor = () => {
  const stored = uni.getStorageSync('userInfo') || {}
  const userType = stored.userType
  // 2 表示医生账号，其它类型无权限
  if (userType !== 2) {
    uni.showModal({
      title: '无权限',
      content: '当前账号无权限进入医生端，请使用医生账号登录',
      confirmText: '去登录',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          uni.reLaunch({ url: '/subpkg/auth/login' })
        }
      }
    })
    return
  }
  // 医生账号：重启到医生端排班首页
  uni.reLaunch({ url: '/subpkg/doctor/schedule/main' })
}

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

const goToRevisitRecord = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
  '/subpkg/profile/records/revisit-record'
)

const goToCheckRecord = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
  '/subpkg/profile/records/check-record'
)

const goToPrivacy = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS,
  '/subpkg/profile/settings/privacy'
)

const goToHelp = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS,
  '/subpkg/profile/help/help'
)

const goToComplain = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS,
  '/subpkg/profile/settings/complain'
)

const goToEvaluate = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS,
  '/subpkg/profile/settings/evaluate'
)

const goToIdentityVerify = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS,
  '/subpkg/profile/personal/identity-verify'
)

const goToUnbind = createAuthHandler(
  AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS,
  '/subpkg/profile/settings/unbind'
)

const goToTransferHistory = () => {
   console.log('直接导航到: /subpkg/hospital/referral-records')
   uniNavigateTo({ url: '/subpkg/hospital/referral-records' })
  }

const goLogin = () => {
  uni.navigateTo({ url: '/subpkg/auth/login' })
}

// 退出登录
const handleLogout = async () => {
  try {
    // 显示确认对话框
    const res = await new Promise((resolve) => {
      uni.showModal({
        title: '确认退出',
        content: '确定要退出登录吗？',
        success: (result) => resolve(result.confirm)
      })
    })

    if (res) {
      // 调用后端退出接口（可选）
      try {
        await userApi.logout()
      } catch (e) {
        // 即使后端退出失败，也要清除本地状态
        console.log('后端退出失败，但继续清除本地状态')
      }

      // 清除本地状态
      userStore.logout()

      // 显示退出成功提示
      await uniShowToast({ title: '已退出登录' })

      // 跳转到登录页
      await uniSwitchTab({ url: '/pages/profile/profile' })
    }
  } catch (e) {
    console.log(e)
    await uniShowToast({ title: '退出失败', icon: 'none' })
  }
}

// 获取就诊卡信息
const loadCardInfo = async () => {
  try {
    userStore.initFromStorage()
    const userId = userStore.userInfo?.userId
    if (!userId) {
      console.log('未获取到用户ID，无法加载就诊卡信息')
      cardInfo.value = {}
      return
    }
    
    // 重置头像错误状态
    resetAvatarError()
    
    console.log('开始获取就诊卡信息，userId:', userId)
    const res = await patientApi.getCard({ userId })
    console.log('个人主页获取到的就诊卡API响应:', res)
    
    // 处理不同的响应格式
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
      
      // 统一字段映射，确保覆盖所有可能的字段名（包括下划线和驼峰命名）
      // 基础标识
      mappedData.patientId = cardData.patientId || cardData.patient_id || mappedData.patientId || null
      
      // 姓名（多种可能的字段名）
      mappedData.patientName = cardData.patientName || cardData.patient_name || cardData.name || mappedData.patientName || ''
      
      // 电话号码（多种可能的字段名）
      mappedData.phone = cardData.phone || cardData.phoneNumber || cardData.phone_number || mappedData.phone || ''
      
      // 性别
      mappedData.gender = cardData.gender || mappedData.gender || ''
      
      // 出生日期
      mappedData.birthDate = cardData.birthDate || cardData.birth_date || cardData.birthday || cardData.birthDay || mappedData.birthDate || ''
      
      // 年龄
      mappedData.age = cardData.age || mappedData.age || null
      
      // 身份证号
      mappedData.idCard = cardData.idCard || cardData.id_card || cardData.idCardNumber || cardData.id_card_number || mappedData.idCard || ''
      
      // 处理认证照片URL
      if (cardData.identityPhoto || cardData.identity_photo) {
        const photoPath = cardData.identityPhoto || cardData.identity_photo
        // 构建完整图片URL
        if (!photoPath.startsWith('http://') && !photoPath.startsWith('https://')) {
          // 相对路径，需要构建完整URL
          mappedData.identityPhoto = buildImageUrl(photoPath)
        } else {
          mappedData.identityPhoto = photoPath
        }
      }
      
      // 确保 patientName 有值
      if (!mappedData.patientName || mappedData.patientName === '') {
        mappedData.patientName = '未知'
      }
      
      // 直接赋值，确保响应式更新
      cardInfo.value = { ...mappedData }
      
      console.log('就诊卡信息已设置:', JSON.parse(JSON.stringify(cardInfo.value)))
      console.log('患者姓名:', cardInfo.value.patientName)
      console.log('电话号码:', cardInfo.value.phone)
      console.log('认证照片:', cardInfo.value.identityPhoto)
    } else {
      console.log('未获取到有效就诊卡数据，API响应:', res)
      cardInfo.value = {}
    }
  } catch (error) {
    console.error('获取就诊卡信息失败:', error)
    cardInfo.value = {}
  }
}

// 构建图片URL
const buildImageUrl = (relativePath) => {
  if (!relativePath) return ''
  const baseURL = uni.getStorageSync('BASE_URL') || 'http://localhost:8095'
  const apiPrefix = uni.getStorageSync('API_PREFIX') || '/jeecg-boot'
  const cleanPrefix = apiPrefix.endsWith('/') ? apiPrefix.slice(0, -1) : apiPrefix
  const cleanPath = relativePath.replace(/^\/+/, '')
  return `${baseURL}${cleanPrefix}/sys/common/static/${encodeURI(cleanPath)}`
}

// 头像加载错误处理
const onAvatarError = (e) => {
  console.error('头像加载失败:', e)
  avatarError.value = true
}

// 在加载卡片信息时重置头像错误状态
const resetAvatarError = () => {
  avatarError.value = false
}

onMounted(() => {
  getUserInfo()
  // 延迟加载就诊卡信息，确保用户信息已初始化
  setTimeout(() => {
    loadCardInfo()
  }, 300)
})

onShow(() => {
  // 每次页面显示时重新加载用户信息和就诊卡信息
  getUserInfo()
  setTimeout(() => {
    loadCardInfo()
  }, 200)
})
</script>

<style scoped>
.profile-bg {
  background: linear-gradient(180deg, #e6f4ff 0%, #cce7ff 100%);
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding-bottom: 0;
}
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18rpx 32rpx 4rpx 32rpx;
  flex-shrink: 0;
  background: linear-gradient(180deg, #e6f4ff 0%, #cce7ff 100%);
}
.profile-info {
  display: flex;
  align-items: center;
}
.avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background: #fff;
  margin-right: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 52rpx;
  box-shadow: 0 4rpx 16rpx rgba(58, 156, 255, 0.2);
  border: 3rpx solid rgba(255, 255, 255, 0.8);
}
.user-info {
  display: flex;
  flex-direction: column;
  color: #fff;
}
.user-name {
  font-size: 38rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
  color: #1a4d80;
}
.user-phone {
  font-size: 26rpx;
  opacity: 0.85;
  color: #4a7ba7;
}
.unbind-btn {
  border: 2rpx solid #3a9cff;
  color: #3a9cff;
  background: #fff;
  font-size: 24rpx;
  border-radius: 28rpx;
  padding: 8rpx 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(58, 156, 255, 0.2);
  font-weight: 500;
}
.card {
  background: #fff;
  border-radius: 20rpx;
  margin: 0 32rpx;
  padding: 14rpx 0;
  box-shadow: 0 4rpx 20rpx rgba(58, 156, 255, 0.15);
  flex-shrink: 0;
}

.card.first-card {
  margin-top: 60rpx;
  padding-top: 16rpx;
  padding-bottom: 14rpx;
}

.card.record-section {
  margin-top: 48rpx;
  padding: 14rpx 0;
}

.card.other-section {
  margin-top: 48rpx;
  padding: 14rpx 0;
}
.profile-section {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.profile-section.centered {
  min-height: 160rpx;
  justify-content: center;
}
.profile-section.centered-down {
  padding-top: 0;
}
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin: 0 0 16rpx 32rpx;
  color: #1a4d80;
}
.profile-row {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: flex-start;
  margin: 0;
  padding: 0 32rpx;
}
.profile-row:not(:last-child) {
  margin-bottom: 18rpx;
}
.profile-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 14rpx 12rpx;
  border-radius: 16rpx;
  transition: all 0.3s ease;
  min-height: 112rpx;
}

.profile-item:active {
  background-color: rgba(58, 156, 255, 0.1);
  transform: scale(0.96);
}
.icon {
  width: 88rpx;
  height: 88rpx;
  margin-bottom: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-lg {
  width: 96rpx;
  height: 96rpx;
  margin-bottom: 14rpx;
}
.profile-item text {
  display: block;
  text-align: center;
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
  word-break: keep-all;
  white-space: nowrap;
  font-weight: 500;
}
.tabbar-placeholder {
  height: 16rpx;
  flex-shrink: 0;
  margin: 0;
  padding: 0;
}

/* 退出登录按钮样式 */
.logout-section {
  margin: 20rpx 14rpx 0 14rpx;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  background: #ff4757;
  color: #fff;
  border: none;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s;
}

.logout-btn:active {
  background: #ff3742;
}
</style>

