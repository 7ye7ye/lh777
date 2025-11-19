<template>
  <view class="center-bg">
    <!-- 头部Banner -->
    <view class="header-banner">
      <image class="banner-bg" src="/static/hospitalpicture.png" mode="aspectFill" />
      <view class="banner-overlay">
        <text class="banner-title">北京交通大学体检中心</text>
        <text class="banner-subtitle">专业体检·精准服务·健康管理</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-item" @click="navigateTo('individual')">
        <view class="action-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">👤</view>
        <text class="action-text">个检预约</text>
      </view>
      <view class="action-item" @click="navigateTo('group')">
        <view class="action-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">👥</view>
        <text class="action-text">团检预约</text>
      </view>
      <view class="action-item" @click="navigateTo('report')">
        <view class="action-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">📊</view>
        <text class="action-text">查看报告</text>
      </view>
      <view class="action-item" @click="makePhoneCall">
        <view class="action-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">📞</view>
        <text class="action-text">电话咨询</text>
      </view>
    </view>

    <!-- 中心简介 -->
    <view class="section card">
      <view class="section-title">
        <text class="title-icon">🏥</text>
        <text>中心简介</text>
      </view>
      <view class="intro-content">
        <text class="intro-text">
          北京交通大学体检中心隶属于北京交通大学社区卫生服务中心，是一家集健康体检、健康管理、健康咨询为一体的专业体检机构。中心拥有先进的医疗设备和专业的医护团队，为全校师生及周边社区居民提供优质的体检服务。
        </text>
        <view class="intro-stats">
          <view class="stat-item">
            <text class="stat-number">15+</text>
            <text class="stat-label">年服务经验</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-number">50000+</text>
            <text class="stat-label">年体检人次</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-number">98%</text>
            <text class="stat-label">满意度</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 服务特色 -->
    <view class="section card">
      <view class="section-title">
        <text class="title-icon">✨</text>
        <text>服务特色</text>
      </view>
      <view class="features-list">
        <view class="feature-item" v-for="(item, index) in features" :key="index">
          <view class="feature-icon">{{ item.icon }}</view>
          <view class="feature-content">
            <text class="feature-title">{{ item.title }}</text>
            <text class="feature-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 体检项目 -->
    <view class="section card">
      <view class="section-title">
        <text class="title-icon">📋</text>
        <text>体检项目</text>
      </view>
      <view class="projects-grid">
        <view class="project-item" v-for="item in projects" :key="item.id">
          <view class="project-icon">{{ item.icon }}</view>
          <text class="project-name">{{ item.name }}</text>
        </view>
      </view>
    </view>

    <!-- 联系我们 -->
    <view class="section card">
      <view class="section-title">
        <text class="title-icon">📍</text>
        <text>联系我们</text>
      </view>
      <view class="contact-list">
        <view class="contact-item" @click="makePhoneCall">
          <view class="contact-icon">📞</view>
          <view class="contact-content">
            <text class="contact-label">咨询电话</text>
            <text class="contact-value">010-51682525</text>
          </view>
          <text class="contact-arrow">></text>
        </view>
        <view class="contact-item" @click="copyText('xyy2525@126.com')">
          <view class="contact-icon">✉️</view>
          <view class="contact-content">
            <text class="contact-label">电子邮箱</text>
            <text class="contact-value">xyy2525@126.com</text>
          </view>
          <text class="contact-arrow">></text>
        </view>
        <view class="contact-item" @click="showLocation">
          <view class="contact-icon">📍</view>
          <view class="contact-content">
            <text class="contact-label">地址</text>
            <text class="contact-value">北京市西直门外上园村3号</text>
          </view>
          <text class="contact-arrow">></text>
        </view>
        <view class="contact-item">
          <view class="contact-icon">⏰</view>
          <view class="contact-content">
            <text class="contact-label">工作时间</text>
            <text class="contact-value">周一至周五 7:30-11:00</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 温馨提示 -->
    <view class="section card">
      <view class="section-title">
        <text class="title-icon">💡</text>
        <text>温馨提示</text>
      </view>
      <view class="tips-list">
        <view class="tip-item" v-for="(tip, index) in tips" :key="index">
          <text class="tip-dot">•</text>
          <text class="tip-text">{{ tip }}</text>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <button class="consult-btn" @click="makePhoneCall">
        <text class="btn-icon">📞</text>
        <text>电话咨询</text>
      </button>
      <button class="booking-btn" @click="navigateTo('individual')">
        <text>立即预约</text>
      </button>
    </view>

    <view class="bottom-placeholder"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

// 服务特色
const features = [
  {
    icon: '🔬',
    title: '先进设备',
    desc: '配备彩超、数字化X光机、全自动生化分析仪等先进设备'
  },
  {
    icon: '👨‍⚕️',
    title: '专业团队',
    desc: '由副高级以上职称医师组成的专业体检团队'
  },
  {
    icon: '⚡',
    title: '高效便捷',
    desc: '优化体检流程，平均体检时间缩短30%'
  },
  {
    icon: '🎯',
    title: '精准报告',
    desc: '3-5个工作日出具详细体检报告，异常指标专业解读'
  },
  {
    icon: '💝',
    title: '贴心服务',
    desc: '提供免费早餐、专人导检、一对一咨询等服务'
  },
  {
    icon: '🔒',
    title: '隐私保护',
    desc: '严格保护个人隐私，体检信息加密存储'
  }
]

// 体检项目
const projects = [
  { id: 1, icon: '🩸', name: '血液检查' },
  { id: 2, icon: '💉', name: '尿液检查' },
  { id: 3, icon: '🫀', name: '心电图' },
  { id: 4, icon: '🫁', name: '胸部X光' },
  { id: 5, icon: '🔬', name: '肝功能' },
  { id: 6, icon: '🧬', name: '肾功能' },
  { id: 7, icon: '💊', name: '血糖血脂' },
  { id: 8, icon: '🩺', name: 'B超检查' },
  { id: 9, icon: '👁️', name: '眼科检查' },
  { id: 10, icon: '👂', name: '耳鼻喉科' },
  { id: 11, icon: '🦷', name: '口腔检查' },
  { id: 12, icon: '🧠', name: '心理评估' }
]

// 温馨提示
const tips = [
  '体检前一天晚8点后禁食，保持空腹8-12小时',
  '体检当天可少量饮水，不影响检查结果',
  '女性请避开生理期，怀孕或备孕请提前告知',
  '慢性病患者可携带常用药物，高血压糖尿病患者可少量饮水服药',
  '请携带有效身份证件，提前15分钟到达',
  '建议穿宽松衣服，不要佩戴金属饰品',
  '体检报告一般3-5个工作日可在线查询或现场领取'
]

// 页面跳转
const navigateTo = (type) => {
  const routes = {
    individual: '/subpkg/physical-exam/individual-booking',
    group: '/subpkg/physical-exam/group-booking',
    report: '/subpkg/physical-exam/exam-report'
  }
  
  if (routes[type]) {
    uni.navigateTo({
      url: routes[type]
    })
  }
}

// 拨打电话
const makePhoneCall = () => {
  uni.makePhoneCall({
    phoneNumber: '010-51682525',
    success: () => {
      console.log('拨打电话成功')
    }
  })
}

// 复制文本
const copyText = (text) => {
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({
        title: '已复制到剪贴板',
        icon: 'success'
      })
    }
  })
}

// 显示位置
const showLocation = () => {
  uni.showModal({
    title: '体检中心地址',
    content: '北京市西直门外上园村3号\n北京交通大学校医院\n\n点击"打开地图"可导航前往',
    confirmText: '打开地图',
    success: (res) => {
      if (res.confirm) {
        // 实际项目中可以调用地图API
        uni.showToast({
          title: '地图导航功能开发中',
          icon: 'none'
        })
      }
    }
  })
}
</script>

<style scoped>
.center-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 200rpx;
}

.header-banner {
  position: relative;
  height: 400rpx;
  overflow: hidden;
}

.banner-bg {
  width: 100%;
  height: 100%;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(102, 126, 234, 0.7) 0%, rgba(118, 75, 162, 0.8) 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 16rpx;
}

.banner-title {
  color: #fff;
  font-size: 48rpx;
  font-weight: bold;
}

.banner-subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 28rpx;
}

.quick-actions {
  display: flex;
  justify-content: space-around;
  padding: 32rpx 24rpx;
  background: #fff;
  margin: -60rpx 24rpx 0;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.15);
  position: relative;
  z-index: 10;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.action-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.action-text {
  font-size: 24rpx;
  color: #666;
}

.card {
  background: #fff;
  border-radius: 20rpx;
  margin: 24rpx 24rpx 0;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.08);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.title-icon {
  font-size: 36rpx;
}

.intro-content {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.intro-text {
  font-size: 28rpx;
  color: #666;
  line-height: 44rpx;
  text-align: justify;
}

.intro-stats {
  display: flex;
  justify-content: space-around;
  padding: 32rpx 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-number {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
}

.stat-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
}

.stat-divider {
  width: 2rpx;
  background: rgba(255, 255, 255, 0.3);
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 24rpx;
  background: #f8faff;
  border-radius: 16rpx;
}

.feature-icon {
  font-size: 48rpx;
  flex-shrink: 0;
}

.feature-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.feature-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.feature-desc {
  font-size: 24rpx;
  color: #999;
  line-height: 36rpx;
}

.projects-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.project-item {
  width: calc((100% - 48rpx) / 4);
  padding: 24rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  background: #f8faff;
  border-radius: 16rpx;
}

.project-icon {
  font-size: 48rpx;
}

.project-name {
  font-size: 22rpx;
  color: #666;
  text-align: center;
}

.contact-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 0;
  border-bottom: 2rpx solid #f5f5f5;
}

.contact-item:last-child {
  border-bottom: none;
}

.contact-icon {
  font-size: 48rpx;
  flex-shrink: 0;
}

.contact-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.contact-label {
  font-size: 24rpx;
  color: #999;
}

.contact-value {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.contact-arrow {
  color: #ccc;
  font-size: 28rpx;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
}

.tip-dot {
  color: #667eea;
  font-size: 28rpx;
  line-height: 40rpx;
  flex-shrink: 0;
}

.tip-text {
  flex: 1;
  font-size: 26rpx;
  color: #666;
  line-height: 40rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 24rpx 32rpx;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 16rpx;
  z-index: 999;
}

.consult-btn {
  flex: 1;
  background: #fff;
  color: #667eea;
  border: 2rpx solid #667eea;
  border-radius: 40rpx;
  padding: 24rpx;
  font-size: 28rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.btn-icon {
  font-size: 32rpx;
}

.booking-btn {
  flex: 2;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 40rpx;
  padding: 24rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
}

.bottom-placeholder {
  height: 160rpx;
}
</style>

