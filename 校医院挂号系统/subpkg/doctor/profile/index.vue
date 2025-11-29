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

    <!-- 个人信息 -->
    <view class="info-section">
      <view class="section-title">
        <text class="title-text">个人信息</text>
      </view>
      <view class="info-list">
        <view class="info-item">
          <view class="item-left">
            <text class="item-icon">👤</text>
            <text class="item-label">姓名</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ doctorInfo.name }}</text>
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
      </view>
    </view>

    <!-- 修改密码弹窗 -->
    <view v-if="passwordDialog.visible" class="dialog-mask" @click.self="closePasswordDialog">
      <view class="dialog-box">
        <text class="dialog-title">修改密码</text>
        <input
          class="dialog-input"
          v-model="passwordDialog.oldPassword"
          password
          placeholder="请输入原密码"
        />
        <input
          class="dialog-input"
          v-model="passwordDialog.newPassword"
          password
          placeholder="请输入新密码（不少于6位）"
        />
        <input
          class="dialog-input"
          v-model="passwordDialog.confirmPassword"
          password
          placeholder="请再次输入新密码"
        />
        <view class="dialog-actions">
          <button class="dialog-btn cancel" @click="closePasswordDialog">取消</button>
          <button class="dialog-btn save" @click="submitPasswordChange">确定</button>
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
function changePassword() {
  passwordDialog.value.visible = true
  passwordDialog.value.oldPassword = ''
  passwordDialog.value.newPassword = ''
  passwordDialog.value.confirmPassword = ''
}

function closePasswordDialog() {
  passwordDialog.value.visible = false
}

async function submitPasswordChange() {
  const { oldPassword, newPassword, confirmPassword } = passwordDialog.value
  if (!oldPassword) {
    uniShowToast('请输入原密码')
    return
  }
  if (!newPassword || newPassword.length < 6) {
    uniShowToast('新密码不少于6位')
    return
  }
  if (newPassword !== confirmPassword) {
    uniShowToast('两次输入的新密码不一致')
    return
  }

  try {
    // TODO: 调用后端修改密码接口，如 userApi.changePassword({ oldPassword, newPassword })
    uniShowToast('已提交修改密码请求')
    closePasswordDialog()
  } catch (e) {
    console.error('修改密码失败:', e)
    uniShowToast('修改失败，请稍后重试')
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

// 更换头像（前端本地预览，预留后端上传接口）
function onChangeAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      const tempPath = res.tempFilePaths && res.tempFilePaths[0]
      if (!tempPath) return
      // 本地预览仍然可用：直接覆盖 avatarUrl 的显示即可
      // 这里不修改服务器相对路径字段，真正的上传与保存通过资料变更申请页完成
      // 为保持简单，直接用 tempPath 作为 <image> 的 src
      // 小程序本地路径本身就是一个可访问的完整 URL
      // 若需要在此处直接上传，可复用 uploadIdentityPhoto 逻辑
      doctorInfo.value.avatar = ''
      // 临时预览：直接用 uni 原生本地路径渲染
      // 注意：computed avatarUrl 使用的是服务器路径，这里不再改
      // 如需更复杂的本地+远程混合逻辑，可单独扩展
      uni.previewImage({
        urls: [tempPath]
      })
    }
  })
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
  background: linear-gradient(180deg, #e9f2ff 0%, #f7f9fc 100%);
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
  gap: 20rpx;
}
.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 999rpx;
  background: #e9f2ff;
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
  font-weight: 700;
  color: #1a1a1a;
}
.doctor-title {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: #7a8aa0;
}
.doctor-dept {
  margin-top: 8rpx;
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

.logout-inline {
  font-size: 24rpx;
  color: #ff4b4b;
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