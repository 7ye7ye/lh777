<template>
  <view class="exam-bg">
    <view class="exam-header">
      <text class="title">体检服务</text>
      <text class="subtitle">守护健康 从体检开始</text>
    </view>
    
    <!-- 顶部Banner -->
    <view class="banner-card">
      <view class="banner-content">
        <view class="banner-icon">🏥</view>
        <view class="banner-text">
          <text class="banner-title">北京交通大学体检中心</text>
          <text class="banner-desc">专业体检 · 精准服务 · 健康管理</text>
        </view>
      </view>
    </view>

    <!-- 主要功能区 -->
    <view class="main-grid">
      <view class="grid-item large" @click="navigateTo('individual')">
        <view class="item-bg" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <view class="item-icon">👤</view>
          <view class="item-content">
            <text class="item-title">个检预约</text>
            <text class="item-desc">个人体检预约</text>
          </view>
          <view class="item-arrow">→</view>
        </view>
      </view>

      <view class="grid-item large" @click="navigateTo('group')">
        <view class="item-bg" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <view class="item-icon">👥</view>
          <view class="item-content">
            <text class="item-title">团检预约</text>
            <text class="item-desc">团体体检预约</text>
          </view>
          <view class="item-arrow">→</view>
        </view>
      </view>

      <view class="grid-item small" @click="navigateTo('report')">
        <view class="item-bg small-bg" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
          <view class="item-icon small-icon">📊</view>
          <text class="item-title small-title">体检报告</text>
          <view class="item-badge" v-if="reportCount > 0">{{ reportCount }}</view>
        </view>
      </view>

      <view class="grid-item small" @click="navigateTo('orders')">
        <view class="item-bg small-bg" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
          <view class="item-icon small-icon">🧾</view>
          <text class="item-title small-title">体检订单</text>
          <view class="item-badge" v-if="orderCount > 0">{{ orderCount }}</view>
        </view>
      </view>

      <view class="grid-item small" @click="navigateTo('center')">
        <view class="item-bg small-bg" style="background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);">
          <view class="item-icon small-icon">🏥</view>
          <text class="item-title small-title">体检中心</text>
        </view>
      </view>
    </view>

    <!-- 快捷服务 -->
    <view class="quick-service card">
      <view class="section-title">
        <text>快捷服务</text>
      </view>
      <view class="service-list">
        <view class="service-item" @click="showConsultDialog('packages')">
          <view class="service-icon" style="background: #e3f2fd;">📦</view>
          <text>套餐选择</text>
        </view>
        <view class="service-item" @click="showConsultDialog('time')">
          <view class="service-icon" style="background: #f3e5f5;">⏰</view>
          <text>体检时间</text>
        </view>
        <view class="service-item" @click="showConsultDialog('notice')">
          <view class="service-icon" style="background: #fff3e0;">⚠️</view>
          <text>注意事项</text>
        </view>
        <view class="service-item" @click="makePhoneCall">
          <view class="service-icon" style="background: #e8f5e9;">📞</view>
          <text>电话咨询</text>
        </view>
      </view>
    </view>

    <!-- 温馨提示 -->
    <view class="tips-card card">
      <view class="section-title">
        <text>温馨提示</text>
      </view>
      <view class="tips-content">
        <view class="tip-item">
          <text class="tip-icon">💡</text>
          <text class="tip-text">体检前一天晚8点后禁食，保持空腹</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">💡</text>
          <text class="tip-text">请携带有效身份证件，提前15分钟到达</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">💡</text>
          <text class="tip-text">报告一般3-5个工作日可在线查询</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">💡</text>
          <text class="tip-text">女性请避开生理期，怀孕请提前告知</text>
        </view>
      </view>
    </view>

    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

// 报告和订单数量（示例数据）
const reportCount = ref(2)
const orderCount = ref(1)

// 页面跳转
const navigateTo = (type) => {
  const routes = {
    individual: '/subpkg/physical-exam/individual-booking',
    group: '/subpkg/physical-exam/group-booking',
    report: '/subpkg/physical-exam/exam-report',
    orders: '/subpkg/physical-exam/exam-orders',
    center: '/subpkg/physical-exam/exam-center'
  }
  
  if (routes[type]) {
    uni.navigateTo({
      url: routes[type],
      fail: (err) => {
        console.error('页面跳转失败:', err)
        uni.showToast({ title: '页面跳转失败', icon: 'none' })
      }
    })
  }
}

// 咨询内容
const consultContent = {
  packages: {
    title: '体检套餐推荐',
    content: `【基础套餐】280元\n适合：学生、青年教职工\n包含：15项常规检查\n\n【教职工套餐】480元 ★推荐\n适合：在职教职工\n特色：学校报销、职业病筛查\n包含：25项全面检查\n\n【全面套餐】880元\n适合：50岁以上、有基础疾病\n特色：深度筛查、跟踪服务\n包含：35项全面检查\n\n提示：学生体检免费，教职工基础套餐学校报销`
  },
  time: {
    title: '体检时间安排',
    content: `【常规体检时间】\n周一至周五：7:30-11:00\n周六：8:00-11:00（需预约）\n采血时间：7:30-10:00\n\n【特殊时间】\n新生入学体检：8月25日-9月5日\n学生年度体检：9-11月\n教职工体检：3-6月\n\n建议：尽量选择工作日早晨，避免月初、周一高峰`
  },
  notice: {
    title: '体检注意事项',
    content: `【体检前一天】\n• 晚餐清淡，避免油腻\n• 晚上8点后禁食\n• 不要饮酒，保证睡眠\n\n【体检当天】\n• 空腹（禁食8-12小时）\n• 可少量饮水\n• 穿宽松衣服\n• 携带有效证件\n\n【女性注意】\n• 避开生理期\n• 怀孕或备孕请提前告知\n\n【慢性病患者】\n• 高血压、糖尿病患者可少量饮水服药\n• 携带近期病历和处方`
  }
}

// 显示咨询对话框
const showConsultDialog = (type) => {
  const content = consultContent[type]
  if (content) {
    uni.showModal({
      title: content.title,
      content: content.content,
      showCancel: true,
      cancelText: '关闭',
      confirmText: '电话咨询',
      success: (res) => {
        if (res.confirm) {
          makePhoneCall()
        }
      }
    })
  }
}

// 拨打电话
const makePhoneCall = () => {
  uni.makePhoneCall({
    phoneNumber: '010-51682525',
    success: () => {
      console.log('拨打电话成功')
    },
    fail: (err) => {
      console.error('拨打电话失败:', err)
      uni.showToast({ title: '拨打失败', icon: 'none' })
    }
  })
}
</script>

<style scoped>
.exam-bg {
  background: linear-gradient(180deg, #667eea 0%, #f8faff 30%);
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.exam-header {
  padding: 60rpx 32rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title {
  color: #fff;
  font-size: 48rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
}

.subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 28rpx;
}

.banner-card {
  margin: 0 24rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.15);
}

.banner-content {
  display: flex;
  align-items: center;
}

.banner-icon {
  font-size: 80rpx;
  margin-right: 24rpx;
}

.banner-text {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.banner-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.banner-desc {
  font-size: 24rpx;
  color: #999;
}

.main-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 0 24rpx;
  gap: 16rpx;
}

.grid-item {
  position: relative;
}

.grid-item.large {
  width: calc((100% - 16rpx) / 2);
  height: 220rpx;
}

.grid-item.small {
  width: calc((100% - 32rpx) / 3);
  height: 160rpx;
}

.item-bg {
  width: 100%;
  height: 100%;
  border-radius: 20rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.1);
}

.item-bg.small-bg {
  justify-content: center;
  align-items: center;
  padding: 20rpx;
}

.item-icon {
  font-size: 56rpx;
  margin-bottom: 16rpx;
}

.item-icon.small-icon {
  font-size: 64rpx;
  margin-bottom: 12rpx;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.item-title {
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.item-title.small-title {
  font-size: 26rpx;
  text-align: center;
}

.item-desc {
  color: rgba(255, 255, 255, 0.9);
  font-size: 24rpx;
}

.item-arrow {
  position: absolute;
  right: 24rpx;
  top: 24rpx;
  color: #fff;
  font-size: 32rpx;
  opacity: 0.7;
}

.item-badge {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  background: #ff4757;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  min-width: 32rpx;
  text-align: center;
}

.card {
  background: #fff;
  border-radius: 20rpx;
  margin: 32rpx 24rpx 0;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.08);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
  padding-left: 16rpx;
  border-left: 6rpx solid #667eea;
}

.service-list {
  display: flex;
  justify-content: space-around;
}

.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.service-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
}

.service-item text {
  font-size: 24rpx;
  color: #666;
}

.tips-card {
  margin-top: 24rpx;
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
}

.tip-icon {
  font-size: 28rpx;
  line-height: 40rpx;
}

.tip-text {
  flex: 1;
  font-size: 26rpx;
  color: #666;
  line-height: 40rpx;
}

.tabbar-placeholder {
  height: 120rpx;
}
</style>

