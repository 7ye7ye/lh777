<template>
  <view class="agreement-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 顶部深色背景 -->
    <view class="header-bg">
      <view class="header-content">
        <view class="header-text">校医院挂号系统</view>
        <view class="header-icons">
          <view class="header-icon-dot"></view>
          <view class="header-icon-dot"></view>
          <view class="header-icon-dot"></view>
          <view class="header-icon-circle"></view>
        </view>
      </view>
    </view>
    
    <!-- 主内容卡片 -->
    <view class="notice-card">
      <view class="card-title">挂号须知</view>
      
      <scroll-view class="card-content" scroll-y>
        <view class="content-wrapper">
          <!-- 温馨提示 -->
          <view class="section">
            <view class="section-title">【温馨提示】</view>
            <view class="paragraph">选择自费挂号的患者，不享受北京市医保统筹待遇。</view>
            <view class="paragraph">选择医保挂号的患者：</view>
            <view class="paragraph paragraph-indent">1. 支付宝北京交通大学校医院挂号小程序，选择北京市医保线上支付。</view>
            <view class="paragraph paragraph-indent">2. 微信小程序，手机挂医保号需要现场缴费，等待时间较长，就诊当日至少提前1小时到收费窗口缴费取号（北京市医保患者可到医院各楼层自助机缴费取号，特慢病、产科孕妇、计划生育、特药除外）。</view>
          </view>
          
          <!-- 医院政策 -->
          <view class="section">
            <view class="paragraph">我院门诊实行全预约挂号，医院是特殊公共场所，为了您和他人的安全，建议全程佩戴口罩一名患者一名陪护。如病情危急，请您到急诊就诊，感谢您的理解和配合！</view>
          </view>
          
          <!-- 挂号确认和退款 -->
          <view class="section">
            <view class="paragraph">挂号成功以在挂号单中查询到挂号成功的消息为准。如您未收到挂号成功的消息，并已付款，可等待半小时后系统会自动退款，长时间未退款可拨打电话96615给予解决。</view>
          </view>
        </view>
      </scroll-view>
      
      <!-- 同意选项和按钮 -->
      <view class="action-section">
        <view class="checkbox-row">
          <checkbox-group @change="handleCheckChange">
            <label class="checkbox-label">
              <checkbox value="agree" :checked="agreeChecked" />
              <text class="checkbox-text">我已阅读并同意</text>
            </label>
          </checkbox-group>
        </view>
        
        <button
          class="agree-button"
          type="default"
          :disabled="!agreeChecked"
          :class="{ disabled: !agreeChecked }"
          @tap="handleAgree"
        >
          我已阅读并同意
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const agreeChecked = ref(false);
const statusBarHeight = ref(0);

onMounted(() => {
  console.log('协议页面已加载');
  try {
    const systemInfo = uni.getSystemInfoSync();
    statusBarHeight.value = systemInfo.statusBarHeight || 0;
    console.log('状态栏高度:', statusBarHeight.value);
  } catch (e) {
    console.warn('获取系统信息失败:', e);
    statusBarHeight.value = 0;
  }
});

function handleCheckChange(event) {
  const { value } = event.detail;
  agreeChecked.value = value.includes('agree');
}

function handleAgree() {
  if (!agreeChecked.value) {
    uni.showToast({
      title: '请先勾选同意',
      icon: 'none'
    });
    return;
  }
  
  // 保存同意状态到本地存储（仅本次会话有效）
  try {
    uni.setStorageSync('hasAgreedNotice', 'true');
    const timestamp = new Date().getTime();
    uni.setStorageSync('agreeTimestamp', timestamp.toString());
  } catch (e) {
    console.error('保存同意状态失败:', e);
  }
  
  // 更新全局状态
  const app = getApp();
  if (app && app.globalData) {
    app.globalData.hasAgreedNotice = true;
  }
  
  // 跳转到首页
  uni.switchTab({
    url: '/pages/home/home'
  });
}
</script>

<style scoped>
.agreement-page {
  width: 100%;
  min-height: 100vh;
  background: #f4f6fb;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.status-bar {
  width: 100%;
  background: transparent;
}

.header-bg {
  width: 100%;
  background: linear-gradient(180deg, #0a1f3f 0%, #173767 100%);
  padding: 20rpx 32rpx 24rpx;
  box-sizing: border-box;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 2rpx;
}

.header-icons {
  display: flex;
  align-items: center;
}

.header-icon-dot {
  width: 8rpx;
  height: 8rpx;
  background: #ffffff;
  border-radius: 50%;
  opacity: 0.9;
  margin-right: 12rpx;
}

.header-icon-dot:last-of-type {
  margin-right: 0;
}

.header-icon-circle {
  width: 12rpx;
  height: 12rpx;
  border: 2rpx solid #ffffff;
  border-radius: 50%;
  opacity: 0.9;
  margin-left: 12rpx;
}

.notice-card {
  flex: 1;
  margin: 32rpx 24rpx 24rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx 36rpx 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 42rpx;
  color: #1a1a1a;
  font-weight: 700;
  text-align: center;
  margin-bottom: 40rpx;
  letter-spacing: 1rpx;
}

.card-content {
  width: 100%;
  height: 600rpx;
  margin-bottom: 20rpx;
}

.content-wrapper {
  padding-right: 16rpx;
  box-sizing: border-box;
}

.section {
  margin-bottom: 36rpx;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 28rpx;
  color: #1d2c4c;
  font-weight: 600;
  margin-bottom: 20rpx;
  line-height: 1.6;
}

.paragraph {
  font-size: 28rpx;
  color: #333333;
  line-height: 2;
  margin-bottom: 16rpx;
  text-align: justify;
  letter-spacing: 0.5rpx;
}

.paragraph-indent {
  padding-left: 32rpx;
  position: relative;
}

.paragraph-indent::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.8em;
  width: 8rpx;
  height: 8rpx;
  background: #333;
  border-radius: 50%;
}

.action-section {
  margin-top: 32rpx;
  padding-top: 32rpx;
  border-top: 1rpx solid #e5e5e5;
}

.checkbox-row {
  margin-bottom: 32rpx;
}

.checkbox-label {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: #479fff;
}

.checkbox-label checkbox {
  margin-right: 12rpx;
}

.checkbox-text {
  color: #479fff;
  font-weight: 500;
}

.agree-button {
  width: 100%;
  background: linear-gradient(90deg, #479fff 0%, #3a9cff 100%);
  color: #ffffff;
  border-radius: 50rpx;
  font-size: 32rpx;
  font-weight: 600;
  height: 88rpx;
  line-height: 88rpx;
  border: none;
  box-shadow: 0 4rpx 16rpx rgba(71, 159, 255, 0.3);
  transition: all 0.3s;
}

.agree-button.disabled {
  background: #d0d0d0;
  color: #ffffff;
  box-shadow: none;
  opacity: 0.6;
}

.agree-button:not(.disabled):active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(71, 159, 255, 0.2);
}

</style>

