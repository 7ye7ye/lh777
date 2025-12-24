<template>
  <view class="profile-page">
    <!-- 医生信息头部 -->
    <view class="profile-header">
      <view class="avatar-section">
        <image class="avatar" :src="avatarUrl" mode="aspectFill" @click="onChangeAvatar" />

        <view class="doctor-main">
          <view class="doctor-main-top">
            <view class="doctor-basic">
              <text class="doctor-name">{{ doctorInfo.name }}</text>
              <text class="doctor-title">{{ doctorInfo.title }}</text>
            </view>
            <text class="logout-inline" @click="logout">退出登录</text>
          </view>
          <view class="doctor-dept">
            <text class="dept-name">{{ doctorInfo.department }}</text>
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

    <!-- 个人简介 -->
    <view class="info-section">
      <view class="section-title">
        <text class="title-text">个人简介</text>
      </view>
      <view class="specialty-container">
        <text class="specialty-text">{{ doctorInfo.doctorDesc || '暂无简介' }}</text>
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

        <view class="menu-item" @click="goMyRequests">
          <view class="menu-left">
            <text class="menu-icon">📄</text>
            <text class="menu-label">我的申请</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>

        <view class="menu-item" @click="goEditProfile">
          <view class="menu-left">
            <text class="menu-icon">✏️</text>
            <text class="menu-label">编辑资料</text>
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

        <view class="menu-item" @click="goToLeaveApplication">
          <view class="menu-left">
            <text class="menu-icon">📝</text>
            <text class="menu-label">申请请假</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 修改密码弹窗 -->
    <view v-if="passwordDialog.visible" class="dialog-mask" @tap="closePasswordDialog">
      <view class="dialog-box" @tap.stop>
        <view class="dialog-header">
          <text class="dialog-title">修改密码</text>
        </view>
        <input
          class="dialog-input"
          v-model="passwordDialog.oldPassword"
          type="password"
          placeholder="请输入原密码"
          @tap.stop
        />
        <input
          class="dialog-input"
          v-model="passwordDialog.newPassword"
          type="password"
          placeholder="请输入新密码（8-20位，含字母、数字和特殊字符）"
          @tap.stop
        />
        <input
          class="dialog-input"
          v-model="passwordDialog.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          @tap.stop
          @keyup.enter="submitPasswordChange"
        />
        <view class="dialog-actions">
          <button class="dialog-btn cancel" @tap.stop="closePasswordDialog">取消</button>
          <button class="dialog-btn save" @tap.stop="submitPasswordChange">确定</button>
        </view>
      </view>
    </view>

    <!-- 医生端底部导航 -->
    <DoctorTabBar active="profile" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

import { uniNavigateTo, uniShowToast } from '@/utils/uniHelper'
import { useUserStore } from '@/store/user'
import { doctorApi } from '@/api/doctor'
import { userApi } from '@/api/user'
import { getDepartmentDetail } from '@/api/department'
import DoctorTabBar from '@/components/DoctorTabBar.vue'

const doctorInfo = ref({
  id: null,
  name: '',
  title: '',
  department: '',
  departmentId: null,
  // 存服务器上的头像相对路径，如 doctor-avatar/xxx.jpg
  avatar: '',
  specialty: '',
  doctorDesc: ''
})

// 头像完整 URL：优先用服务器相对路径拼接，其次用默认本地占位图
const buildImageUrl = (relativePath) => {
  if (!relativePath) return '/static/doctor.svg'
  const baseURL = uni.getStorageSync('BASE_URL') || 'http://localhost:8095'
  const apiPrefix = uni.getStorageSync('API_PREFIX') || '/jeecg-boot'
  const cleanPrefix = apiPrefix.endsWith('/') ? apiPrefix.slice(0, -1) : apiPrefix
  const cleanPath = relativePath.replace(/^\/+/, '')
  return `${baseURL}${cleanPrefix}/sys/common/static/${encodeURI(cleanPath)}`
}

const avatarUrl = computed(() => buildImageUrl(doctorInfo.value.avatar))

const loadingProfile = ref(false)
const userStore = useUserStore()

// 修改密码弹窗
const passwordDialog = ref({
  visible: false,
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

async function loadDoctorProfile() {
  if (loadingProfile.value) return
  loadingProfile.value = true
  try {
    let p = await doctorApi.getMyProfile()
    if (!p || (typeof p === 'object' && Object.keys(p).length === 0)) {
      const uid =
        userStore?.userInfo?.userId ??
        userStore?.userInfo?.id ??
        uni.getStorageSync('userInfo')?.userId ??
        uni.getStorageSync('userInfo')?.id
      if (uid) {
        p = await doctorApi.getProfileByUserId(Number(uid))
      }
    }
    if (!p || (typeof p === 'object' && Object.keys(p).length === 0)) {
      await uniShowToast({ title: '未获取到医生资料', icon: 'none' })
      return
    }

    doctorInfo.value = {
      id: p.doctorId || p.id || null,
      name: p.doctorName || p.name || p.realName || '未知姓名',
      title: p.title || p.professionalTitle || '医师',
      department: p.deptName || p.departmentName || p.department || '未知科室',
      departmentId: p.deptId || p.departmentId || null,
      // 这里期望后端返回的是相对路径，如 doctor-avatar/xxx.jpg
      avatar: p.avatar || p.avatarUrl || p.photo || '',
      specialty: p.specialty || '',
      doctorDesc: p.doctorDesc || p.doctor_desc || p.description || ''
    }
  } catch (e) {
    console.error('加载医生资料失败:', e)
    await uniShowToast({ title: '加载医生资料失败', icon: 'none' })
  } finally {
    loadingProfile.value = false
  }
}

onMounted(() => {
  try {
    if (!userStore.userInfo || !userStore.token) {
      userStore.initFromStorage?.()
    }
  } catch (_) {}
  loadDoctorProfile()
})

// 返回医生主界面
function goBackToSchedule() {
  // const pages = getCurrentPages()
  // if (pages.length > 1) {
  //   uni.navigateBack()
  // } else {
    uniNavigateTo({ url: '/subpkg/doctor/schedule/main' })
  // }
}

// 功能菜单
function goToScheduleManagement() {
  uniNavigateTo({ url: '/subpkg/doctor/schedule/main' })
}
function goMyRequests() {
  uniNavigateTo({ url: '/subpkg/doctor/profile/requests' })
}
function goToStatistics() {
  uniShowToast('接诊统计功能开发中')
}
function goToSettings() {
  // 项目中存在 /pages/profile/profile.vue
  uniNavigateTo({ url: '/pages/profile/profile' })
}

// 修改密码相关函数
const changePassword = () => {
  // 重置表单并显示弹窗
  passwordDialog.value = {
    visible: true,
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

const closePasswordDialog = () => {
  // 使用 nextTick 确保状态更新
  passwordDialog.value = {
    visible: false,
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

// 点击遮罩层时关闭弹窗
const handleMaskClick = () => {
  closePasswordDialog()
}

const submitPasswordChange = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordDialog.value
  
  // 基础验证
  if (!oldPassword || !oldPassword.trim()) {
    uniShowToast({ title: '请输入原密码', icon: 'none' })
    return false
  }
  
  if (!newPassword || !newPassword.trim()) {
    uniShowToast({ title: '请输入新密码', icon: 'none' })
    return false
  }
  
  // 密码长度验证
  if (newPassword.length < 8 || newPassword.length > 20) {
    uniShowToast({ title: '新密码长度应为8-20位', icon: 'none' })
    return false
  }
  
  // 密码复杂度验证
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[~!@#$%^&*()_+=\-{}\[\]:;"'<>,.?/]).{8,20}$/
  if (!passwordRegex.test(newPassword)) {
    uniShowToast({ 
      title: '密码必须包含字母、数字和特殊字符', 
      icon: 'none', 
      duration: 2500 
    })
    return false
  }
  
  // 确认密码验证
  if (!confirmPassword) {
    uniShowToast({ title: '请确认新密码', icon: 'none' })
    return false
  }
  
  if (newPassword !== confirmPassword) {
    uniShowToast({ title: '两次输入的新密码不一致', icon: 'none' })
    return false
  }
  
  // 新旧密码不能相同
  if (oldPassword === newPassword) {
    uniShowToast({ title: '新密码不能与原密码相同', icon: 'none' })
    return false
  }

  // 显示加载提示
  uni.showLoading({ title: '提交中...' })
  
// In the submitPasswordChange function, I'll add the missing catch block
    try {
      // 显示加载中
      uni.showLoading({ title: '正在修改密码...', mask: true })
      // ... existing code ...
    } catch (error) {
      console.error('修改密码出错:', error)
      uni.hideLoading()
      uni.showToast({
        title: '修改密码失败，请重试',
        icon: 'none'
      })
    }
    
    try {
      // 调用后端修改密码接口
      const response = await userApi.changePassword({
        oldPassword: oldPassword.trim(),
        newPassword: newPassword.trim(),
        confirmPassword: confirmPassword.trim()
      })
      
      // 关闭加载中
      uni.hideLoading()
      
      // 清空表单
      passwordDialog.value = {
        visible: false,
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      
      // 显示成功提示
      uni.showToast({
        title: '密码修改成功',
        icon: 'success',
        duration: 1500
      })
      
      // 延迟显示重新登录提示
      setTimeout(() => {
        uni.showModal({
          title: '提示',
          content: '密码修改成功，请重新登录',
          showCancel: false,
          success: () => {
            // 清除用户信息
            userStore.logout()
            // 跳转到登录页
            uni.reLaunch({ url: '/subpkg/auth/login' })
          }
        })
      }, 1500)
    
  } catch (error) {
    // 关闭加载中
    uni.hideLoading()
    
    console.error('修改密码失败:', error)
    
    // 根据错误类型显示不同的提示
    let errorMsg = '修改密码失败，请稍后重试'
    
    // 处理不同格式的错误响应
    if (error?.response?.data?.message) {
      errorMsg = error.response.data.message
    } else if (error?.data?.message) {
      errorMsg = error.data.message
    } else if (error?.message) {
      errorMsg = error.message
    } else if (error?.errMsg) {
      errorMsg = error.errMsg
    }
    
    // 显示错误提示
    uni.showToast({
      title: errorMsg,
      icon: 'none',
      duration: 2500
    })
  }
}

// 统一编辑资料页
function goEditProfile() {
  uniNavigateTo({ url: '/subpkg/doctor/profile/edit' })
}

// 跳转到请假申请页面
function goToLeaveApplication() {
  // 传递医生信息到请假页面
  const params = {
    doctorId: doctorInfo.value.id,
    doctorName: doctorInfo.value.name,
    deptId: doctorInfo.value.departmentId,
    deptName: doctorInfo.value.department
  }
  const query = Object.keys(params)
    .filter(key => params[key] !== null && params[key] !== undefined)
    .map(key => `${key}=${encodeURIComponent(params[key])}`)
    .join('&')
  uniNavigateTo({ url: `/subpkg/doctor/leave/apply${query ? '?' + query : ''}` })
}

// 编辑弹窗
const editDialog = ref({
  visible: false,
  field: '',
  label: '',
  value: ''
})

function showEditDialog(field) {
  const labelMap = { name: '姓名', phone: '联系电话', email: '邮箱', specialty: '擅长领域' }
  editDialog.value.visible = true
  editDialog.value.field = field
  editDialog.value.label = labelMap[field] || field
  editDialog.value.value = doctorInfo.value[field] || ''
}

function closeEditDialog() {
  editDialog.value.visible = false
}

async function saveEdit() {
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
  if (field === 'specialty' && !value.trim()) {
    uniShowToast('擅长领域不能为空')
    return
  }
  // 本地更新
  doctorInfo.value[field] = value
  closeEditDialog()

  // 若后端已提供更新接口，则尝试提交修改
  try {
    if (typeof doctorApi?.updateProfile === 'function') {
      await doctorApi.updateProfile(doctorInfo.value)
      uniShowToast('已提交并保存')
    } else {
      uniShowToast('已保存（待接入后端同步接口）')
    }
  } catch (e) {
    console.error('提交个人信息失败:', e)
    uniShowToast('本地已保存，但提交服务器失败')
  }
}

// 点击头像预览大图
function onChangeAvatar() {
  // 如果有头像，则预览大图
  if (avatarUrl.value && avatarUrl.value !== '/static/doctor.svg') {
    uni.previewImage({
      urls: [avatarUrl.value],
      current: 0
    })
  } else {
    // 如果没有头像，提示去编辑资料页面上传
    uni.showModal({
      title: '提示',
      content: '暂无头像，是否前往编辑资料页面上传头像？',
      confirmText: '去上传',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          goEditProfile()
        }
      }
    })
  }
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
  padding-bottom: 140rpx;
  box-sizing: border-box;
  background: linear-gradient(180deg, #e8f3ff 0%, #f7faff 45%, #ffffff 100%);
}

/* 头部信息 */
.profile-header {
  background: linear-gradient(120deg, #3a9cff 0%, #6ec6ff 100%);
  border-radius: 20rpx;
  padding: 26rpx;
  box-shadow: 0 12rpx 28rpx rgba(58, 156, 255, 0.25);
  margin-bottom: 24rpx;
  color: #fff;
}
.avatar-section {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 999rpx;
  background: rgba(255,255,255,0.18);
  box-shadow: 0 8rpx 18rpx rgba(0,0,0,0.08);
}
.doctor-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.doctor-main-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.doctor-basic {
  display: flex;
  flex-direction: column;
}
.doctor-name {
  font-size: 32rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.5rpx;
}
.doctor-title {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: rgba(255,255,255,0.9);
}
.doctor-dept {
  margin-top: 8rpx;
}
.dept-name {
  display: inline-block;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(255,255,255,0.18);
  color: #fff;
  font-size: 24rpx;
  font-weight: 600;
}

.logout-inline {
  font-size: 24rpx;
  color: #ffd7d7;
}

/* 统计 */
.stats-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 18rpx;
  padding: 24rpx;
  box-shadow: 0 10rpx 24rpx rgba(58, 156, 255, 0.1);
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
  border-radius: 18rpx;
  padding: 24rpx;
  box-shadow: 0 10rpx 24rpx rgba(58, 156, 255, 0.1);
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
  background: #f6f9ff;
  border-radius: 14rpx;
  padding: 18rpx;
  border: 1rpx solid #e8efff;
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
  background: #f6f9ff;
  border-radius: 14rpx;
  padding: 18rpx;
  border: 1rpx solid #e8efff;
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
  background: #f6f9ff;
  border-radius: 14rpx;
  padding: 18rpx;
  border: 1rpx solid #e8efff;
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
  color: #9ab4e5;
}

/* 编辑弹窗 */
.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.dialog-box {
  width: 80%;
  max-width: 500px;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  position: relative;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dialog-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.dialog-close {
  font-size: 28px;
  color: #999;
  padding: 0 10px;
  cursor: pointer;
  line-height: 1;
}

.dialog-input {
  width: 100%;
  height: 44px;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 0 12px;
  margin-bottom: 16px;
  font-size: 14px;
  box-sizing: border-box;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-btn {
  margin-left: 12px;
  padding: 8px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  outline: none;
}

.dialog-btn.cancel {
  background-color: #f5f5f5;
  color: #666;
  border: 1px solid #e0e0e0;
}

.dialog-btn.save {
  background-color: #1890ff;
  color: #fff;
  border: 1px solid #1890ff;
}
</style>