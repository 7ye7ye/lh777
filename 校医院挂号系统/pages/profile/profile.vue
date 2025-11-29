<template>
  <view class="profile-bg">
  <view class="profile-header">
      <view class="profile-info">
        <image class="avatar" src="/static/profile.svg" mode="aspectFill"></image>
        <view class="user-info">
          <text class="user-name">{{ userInfo.name || '微信用户' }}</text>
          <text class="user-phone">{{ userInfo.phone || '*************' }}</text>
        </view>
      </view>
      <template v-if="!isLoggedIn">
        <button class="unbind-btn" size="mini" @click="goLogin">登录</button>
      </template>
      <template v-else>
        <button class="unbind-btn" size="mini" @click="handleLogout">退出登录</button>
      </template>
    </view>

    <view class="profile-section card centered centered-down">
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

    <view class="profile-section card">
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

    <view class="profile-section card">
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
      </view>
    </view>

    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { userApi } from '@/api/user'
import { useUserStore } from '@/store/user'
import { uniShowToast, uniSwitchTab, uniNavigateTo } from '@/utils/uniHelper'
import LoginPrompt from '@/components/LoginPrompt.vue'
import { AUTH_REQUIRED_FEATURES, createAuthHandler } from '@/utils/auth'

const userInfo = ref({})
const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.isLoggedIn)

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

onMounted(() => {
  getUserInfo()
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
  padding: 28rpx 32rpx 20rpx 32rpx;
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
  margin: 16rpx 32rpx 0 32rpx;
  padding: 24rpx 0;
  box-shadow: 0 4rpx 20rpx rgba(58, 156, 255, 0.15);
  flex-shrink: 0;
}
.profile-section {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.profile-section.centered {
  min-height: 180rpx;
  justify-content: center;
}
.profile-section.centered-down {
  padding-top: 12rpx;
}
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin: 0 0 20rpx 32rpx;
  color: #1a4d80;
}
.profile-row {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: flex-start;
  margin: 0;
  padding: 0 24rpx;
}
.profile-row:not(:last-child) {
  margin-bottom: 20rpx;
}
.profile-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16rpx 12rpx;
  border-radius: 16rpx;
  transition: all 0.3s ease;
  min-height: 130rpx;
}

.profile-item:active {
  background-color: rgba(58, 156, 255, 0.1);
  transform: scale(0.96);
}
.icon {
  width: 80rpx;
  height: 80rpx;
  margin-bottom: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-lg {
  width: 88rpx;
  height: 88rpx;
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
  height: 0;
  flex-shrink: 0;
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

