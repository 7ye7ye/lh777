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
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { uniNavigateTo, uniShowToast, uniSwitchTab } from '@/utils/uniHelper'

// 医生信息
const doctorInfo = ref({
  name: '张医生',
  avatar: '/static/doctor.png',
  title: '主治医师',
  department: '内科',
  phone: '138****5678',
  email: 'zhangdoc@hospital.com',
  licenseNumber: '110********1234',
  yearsOfPractice: 10,
  specialty: '擅长内科常见病、多发病的诊治，对呼吸系统疾病、消化系统疾病有丰富的临床经验。特别是在慢性咳嗽、慢性胃炎、高血压、糖尿病等疾病的诊疗方面具有独到见解。'
})

// 统计数据
const stats = ref({
  totalPatients: 1856,
  todayPatients: 12,
  rating: 4.8
})

// 显示编辑对话框
const showEditDialog = (field) => {
  uniShowToast({ 
    title: '编辑功能开发中', 
    icon: 'none' 
  })
}

// 跳转到排班管理
const goToScheduleManagement = () => {
  uniNavigateTo({ url: '/pages/doctor/schedule/schedule' })
}

// 跳转到统计页面
const goToStatistics = () => {
  uniShowToast({ 
    title: '统计功能开发中', 
    icon: 'none' 
  })
}

// 跳转到设置页面
const goToSettings = () => {
  uniShowToast({ 
    title: '设置功能开发中', 
    icon: 'none' 
  })
}

// 修改密码
const changePassword = async () => {
  uniShowToast({ 
    title: '修改密码功能开发中', 
    icon: 'none' 
  })
}

// 退出登录
const logout = async () => {
  try {
    const res = await uniShowModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      confirmText: '确定',
      cancelText: '取消'
    })
    
    if (res.confirm) {
      // TODO: 清除登录信息
      await uniShowToast({ title: '已退出登录', icon: 'success' })
      setTimeout(() => {
        uniRedirectTo({ url: '/pages/login/login' })
      }, 1500)
    }
  } catch (error) {
    uniShowToast({ title: '操作失败', icon: 'none' })
  }
}

// 返回医生主界面
const goBackToSchedule = () => {
  uniNavigateTo({ url: '/pages/doctor/schedule/schedule' })
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40rpx;
}

/* 返回按钮 */
.back-section {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #fff;
  cursor: pointer;
  border-bottom: 2rpx solid #f0f0f0;
}

.back-icon {
  font-size: 48rpx;
  color: #1677ff;
  font-weight: 300;
  margin-right: 8rpx;
  line-height: 1;
}

.back-text {
  font-size: 28rpx;
  color: #1677ff;
}

/* 个人信息头部 */
.profile-header {
  background: linear-gradient(135deg, #1677ff 0%, #4da3ff 100%);
  padding: 48rpx 32rpx;
  color: #fff;
}

.avatar-section {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background: #fff;
  margin-right: 24rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.doctor-basic {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.doctor-name {
  font-size: 40rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.doctor-title {
  font-size: 26rpx;
  opacity: 0.9;
}

.doctor-dept {
  text-align: center;
  padding: 12rpx 32rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20rpx;
  display: inline-block;
}

.dept-name {
  font-size: 26rpx;
}

/* 统计区域 */
.stats-section {
  display: flex;
  background: #fff;
  margin: -40rpx 32rpx 24rpx;
  padding: 32rpx 24rpx;
  border-radius: 16rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 48rpx;
  font-weight: 600;
  color: #1677ff;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

.stat-divider {
  width: 2rpx;
  background: #f0f0f0;
  margin: 0 24rpx;
}

/* 信息区域 */
.info-section {
  margin: 0 32rpx 24rpx;
}

.section-title {
  margin-bottom: 16rpx;
}

.title-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.info-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 28rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.item-left {
  display: flex;
  align-items: center;
}

.item-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.item-label {
  font-size: 28rpx;
  color: #333;
}

.item-right {
  display: flex;
  align-items: center;
}

.item-value {
  font-size: 28rpx;
  color: #666;
  margin-right: 8rpx;
}

.item-arrow {
  font-size: 36rpx;
  color: #999;
  font-weight: 300;
}

/* 擅长领域 */
.specialty-container {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
}

.specialty-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
  display: block;
}

/* 功能菜单 */
.menu-section {
  margin: 0 32rpx 24rpx;
}

.menu-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 28rpx;
  border-bottom: 2rpx solid #f0f0f0;
  transition: all 0.3s;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: #f5f7fa;
}

.menu-left {
  display: flex;
  align-items: center;
}

.menu-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.menu-label {
  font-size: 28rpx;
  color: #333;
}

.menu-arrow {
  font-size: 36rpx;
  color: #999;
  font-weight: 300;
}

/* 退出登录区域 */
.logout-section {
  padding: 0 32rpx;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #fff;
  color: #ff4d4f;
  border: 2rpx solid #ff4d4f;
  border-radius: 12rpx;
  font-size: 32rpx;
}

.logout-btn:active {
  background: #fff1f0;
}
</style>

