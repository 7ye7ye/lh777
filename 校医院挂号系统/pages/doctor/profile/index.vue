<template>
  <view class="profile-page">
    <!-- 返回按钮 -->
    <view class="back-section" @click="goBackToSchedule">
      <text class="back-icon">‹</text>
      <text class="back-text">返回医生主界面</text>
    </view>

    <!-- 医生信息头部 -->
    <view class="profile-header">
      <view class="avatar-section">
        <image class="avatar" :src="doctorInfo.avatar" mode="aspectFill" />
        <view class="doctor-basic">
          <text class="doctor-name">{{ doctorInfo.name }}</text>
          <text class="doctor-title">{{ doctorInfo.title }}</text>
        </view>
      </view>
      <view class="doctor-dept">
        <text class="dept-name">{{ doctorInfo.department }}</text>
      </view>
    </view>

    <!-- 统计数据 -->
    <view class="stats-section">
      <view class="stat-item">
        <text class="stat-value">{{ stats.totalPatients }}</text>
        <text class="stat-label">累计接诊</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.todayPatients }}</text>
        <text class="stat-label">今日接诊</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.rating }}</text>
        <text class="stat-label">患者评分</text>
      </view>
    </view>

    <!-- 个人信息 -->
    <view class="info-section">
      <view class="section-title">
        <text class="title-text">个人信息</text>
      </view>
      <view class="info-list">
        <view class="info-item" @click="showEditDialog('name')">
          <view class="item-left">
            <text class="item-icon">👤</text>
            <text class="item-label">姓名</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ doctorInfo.name }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>

        <view class="info-item">
          <view class="item-left">
            <text class="item-icon">🏥</text>
            <text class="item-label">科室</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ doctorInfo.department }}</text>
          </view>
        </view>

        <view class="info-item">
          <view class="item-left">
            <text class="item-icon">🎓</text>
            <text class="item-label">职称</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ doctorInfo.title }}</text>
          </view>
        </view>

        <view class="info-item" @click="showEditDialog('phone')">
          <view class="item-left">
            <text class="item-icon">📱</text>
            <text class="item-label">联系电话</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ doctorInfo.phone }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>

        <view class="info-item" @click="showEditDialog('email')">
          <view class="item-left">
            <text class="item-icon">📧</text>
            <text class="item-label">邮箱</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ doctorInfo.email }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 专业信息 -->
    <view class="info-section">
      <view class="section-title">
        <text class="title-text">专业信息</text>
      </view>
      <view class="info-list">
        <view class="info-item">
          <view class="item-left">
            <text class="item-icon">🎖️</text>
            <text class="item-label">执业证号</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ doctorInfo.licenseNumber }}</text>
          </view>
        </view>

        <view class="info-item">
          <view class="item-left">
            <text class="item-icon">⏱️</text>
            <text class="item-label">从业年限</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ doctorInfo.yearsOfPractice }}年</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 擅长领域 -->
    <view class="info-section">
      <view class="section-title">
        <text class="title-text">擅长领域</text>
      </view>
      <view class="specialty-container">
        <text class="specialty-text">{{ doctorInfo.specialty }}</text>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="section-title">
        <text class="title-text">功能设置</text>
      </view>
      <view class="menu-list">
        <view class="menu-item" @click="goToScheduleManagement">
          <view class="menu-left">
            <text class="menu-icon">📅</text>
            <text class="menu-label">我的排班</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>

        <view class="menu-item" @click="goToStatistics">
          <view class="menu-left">
            <text class="menu-icon">📊</text>
            <text class="menu-label">接诊统计</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>

        <view class="menu-item" @click="goToSettings">
          <view class="menu-left">
            <text class="menu-icon">⚙️</text>
            <text class="menu-label">系统设置</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>

        <view class="menu-item" @click="changePassword">
          <view class="menu-left">
            <text class="menu-icon">🔒</text>
            <text class="menu-label">修改密码</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 退出登录按钮 -->
    <view class="logout-section">
      <button class="logout-btn" @click="logout">退出登录</button>
    </view>

    <!-- 编辑弹窗 -->
    <view v-if="editDialog.visible" class="dialog-mask" @click.self="closeEditDialog">
      <view class="dialog-box">
        <text class="dialog-title">编辑{{ editDialog.label }}</text>
        <input
          class="dialog-input"
          v-model="editDialog.value"
          :placeholder="`请输入${editDialog.label}`"
        />
        <view class="dialog-actions">
          <button class="dialog-btn cancel" @click="closeEditDialog">取消</button>
          <button class="dialog-btn save" @click="saveEdit">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { uniNavigateTo, uniShowToast } from '@/utils/uniHelper'
import { useUserStore } from '../../../store/user.js'
import { doctorApi } from '../../../api/doctor'

// 医生信息（从后端填充）
const doctorInfo = ref({
  name: '',
  title: '',
  department: '',
  avatar: '/static/doctor.svg',
  phone: '',
  email: '',
  licenseNumber: '',
  yearsOfPractice: 0,
  specialty: ''
})

const stats = ref({
  totalPatients: 0,
  todayPatients: 0,
  rating: 0
})

// 加载后端医生资料（优先用当前登录用户的 userId）
const userStore = useUserStore()
const loadProfile = async () => {
  try {
    userStore.initFromStorage()
    const userId = userStore.userInfo?.userId
    let profile = null
    if (userId) {
      profile = await doctorApi.getProfileByUserId(userId)
    } else {
      profile = await doctorApi.getMyProfile()
    }
    if (!profile || !profile.doctorId) {
      uniShowToast('未绑定医生资料或接口返回空数据')
      return
    }
    doctorInfo.value = {
      name: profile.doctorName || profile.realname || '',
      title: profile.title || '',
      department: profile.deptName || '',
      avatar: profile.avatar || '/static/doctor.svg',
      phone: profile.phone || '',
      email: profile.email || '',
      licenseNumber: profile.licenseNumber || '',
      yearsOfPractice: profile.yearsOfPractice || 0,
      specialty: profile.specialty || ''
    }
    // 可选：统计项从其它接口获取，这里占位
    stats.value = {
      totalPatients: 0,
      todayPatients: 0,
      rating: 0
    }
  } catch (e) {
    uniShowToast('获取医生资料失败')
    console.warn('loadProfile错误：', e)
  }
}

onMounted(loadProfile)

// 返回医生主界面
function goBackToSchedule() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uniNavigateTo('/pages/doctor/schedule/main')
  }
}

// 功能菜单
function goToScheduleManagement() {
  uniNavigateTo('/pages/doctor/schedule/main')
}
function goToStatistics() {
  uniShowToast('接诊统计功能开发中')
}
function goToSettings() {
  // 项目中存在 /pages/profile/profile.vue
  uniNavigateTo('/pages/profile/profile')
}
function changePassword() {
  uniShowToast('请前往系统设置中修改密码')
}

// 编辑弹窗
const editDialog = ref({
  visible: false,
  field: '',
  label: '',
  value: ''
})

function showEditDialog(field) {
  const labelMap = { name: '姓名', phone: '联系电话', email: '邮箱' }
  editDialog.value.visible = true
  editDialog.value.field = field
  editDialog.value.label = labelMap[field] || field
  editDialog.value.value = doctorInfo.value[field] || ''
}
function closeEditDialog() {
  editDialog.value.visible = false
}
function saveEdit() {
  const { field, value } = editDialog.value
  // 简单校验
  if (field === 'name' && !value.trim()) {
    uniShowToast('姓名不能为空')
    return
  }
  if (field === 'phone' && !/^1[3-9]\d{9}$/.test(value)) {
    uniShowToast('请输入有效手机号')
    return
  }
  if (field === 'email' && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    uniShowToast('请输入有效邮箱')
    return
  }
  doctorInfo.value[field] = value
  closeEditDialog()
  uniShowToast('已保存')
}

// 退出登录
function logout() {
  uni.showModal({
    title: '退出登录',
    content: '确认退出登录？',
    success: (res) => {
      if (res.confirm) {
        try {
          uni.clearStorage()
        } catch (e) {}
        // 项目中登录页为 /pages/login/login.vue
        uni.reLaunch({ url: '/subpkg/auth/login' })
      }
    }
  })
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  padding: 24rpx;
  box-sizing: border-box;
  background: linear-gradient(180deg, #e9f2ff 0%, #f7f9fc 100%);
}

/* 返回按钮 */
.back-section {
  display: flex;
  align-items: center;
  color: #2a7bff;
  font-weight: 600;
  margin-bottom: 20rpx;
}
.back-icon {
  font-size: 40rpx;
  margin-right: 8rpx;
}
.back-text {
  font-size: 28rpx;
}

/* 头部信息 */
.profile-header {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(42, 123, 255, 0.08);
  margin-bottom: 24rpx;
}
.avatar-section {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 999rpx;
  background: #e9f2ff;
}
.doctor-basic {
  display: flex;
  flex-direction: column;
}
.doctor-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a1a;
}
.doctor-title {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: #7a8aa0;
}
.doctor-dept {
  margin-top: 12rpx;
}
.dept-name {
  display: inline-block;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #e9f2ff;
  color: #2a7bff;
  font-size: 24rpx;
  font-weight: 600;
}

/* 统计 */
.stats-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(42, 123, 255, 0.08);
  margin-bottom: 24rpx;
}
.stat-item {
  flex: 1;
  text-align: center;
}
.stat-value {
  font-size: 36rpx;
  font-weight: 800;
  color: #2f3b52;
}
.stat-label {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #7a8aa0;
}
.stat-divider {
  width: 2rpx;
  height: 48rpx;
  background: #f0f5ff;
}

/* 通用区块 */
.info-section,
.menu-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(42, 123, 255, 0.08);
  margin-bottom: 24rpx;
}
.section-title {
  margin-bottom: 12rpx;
}
.title-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a1a;
}

/* 信息列表 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 16rpx;
}
.item-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.item-icon {
  font-size: 28rpx;
}
.item-label {
  font-size: 26rpx;
  color: #7a8aa0;
}
.item-right {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.item-value {
  font-size: 28rpx;
  color: #2f3b52;
  font-weight: 600;
}
.item-arrow {
  font-size: 40rpx;
  color: #a0b7e0;
}

/* 擅长领域 */
.specialty-container {
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 16rpx;
}
.specialty-text {
  font-size: 26rpx;
  color: #2f3b52;
}

/* 菜单 */
.menu-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 16rpx;
}
.menu-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.menu-icon {
  font-size: 28rpx;
}
.menu-label {
  font-size: 26rpx;
  color: #2f3b52;
}
.menu-arrow {
  font-size: 40rpx;
  color: #a0b7e0;
}

/* 退出登录 */
.logout-section {
  margin-top: 12rpx;
}
.logout-btn {
  width: 100%;
  border: none;
  border-radius: 12rpx;
  padding: 24rpx 0;
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
  background: linear-gradient(90deg, #ff3b30, #ff6b6b);
  box-shadow: 0 12rpx 20rpx rgba(255, 59, 48, 0.24);
}

/* 编辑弹窗 */
.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.dialog-box {
  width: 80%;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 12rpx 20rpx rgba(42, 123, 255, 0.16);
}
.dialog-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1a1a;
}
.dialog-input {
  margin-top: 16rpx;
  width: 100%;
  height: 72rpx;
  border-radius: 12rpx;
  background: #f8fafc;
  padding: 0 16rpx;
  box-sizing: border-box;
  font-size: 26rpx;
  color: #2f3b52;
}
.dialog-actions {
  margin-top: 16rpx;
  display: flex;
  justify-content: flex-end;
  gap: 12rpx;
}
.dialog-btn {
  border: none;
  border-radius: 12rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  font-weight: 700;
}
.dialog-btn.cancel {
  background: #e9f2ff;
  color: #2a7bff;
}
.dialog-btn.save {
  background: linear-gradient(90deg, #2a7bff, #6aa9ff);
  color: #fff;
}
</style>