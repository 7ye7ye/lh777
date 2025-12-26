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
          <!-- 挂号须知 -->
          <view class="section">
            <view class="section-title">【挂号须知】</view>
            <view class="paragraph">1. 本系统实行全预约挂号，请提前在线选择科室、医生与具体时段。</view>
            <view class="paragraph">2. 可预约的日期范围通常为近7天，实际可选日期以界面展示为准。</view>
            <view class="paragraph">3. 挂号前请先创建并绑定电子就诊卡，否则无法完成挂号支付。</view>
            <view class="paragraph">4. 支付时需选择就诊人，并可在支付页选择就诊身份（学生/教师/职工），费用按所选身份计算。</view>
            <view class="paragraph">5. 支付成功后系统会跳转到对应的挂号记录页，可随时在“我的-挂号记录”查看状态。</view>
          </view>
          
          <!-- 退号说明 -->
          <view class="section">
            <view class="section-title">【退号说明】</view>
            <view class="paragraph">1. 如需取消，请在就诊前于挂号记录中发起退号申请，实际可退规则以医院处理为准。</view>
            <view class="paragraph">2. 退号成功后费用按支付渠道原路退回；已就诊或已过期的记录通常不可退。</view>
            <view class="paragraph">3. 退号处理有一定时间，请关注退款到账情况，若异常可联系收费窗口。</view>
          </view>
          
          <!-- 就诊须知 -->
          <view class="section">
            <view class="section-title">【就诊须知】</view>
            <view class="paragraph">1. 请按照预约时间准时到院就诊，建议提前10-15分钟到达。</view>
            <view class="paragraph">2. 就诊时请出示电子就诊码，可通过首页就诊卡查看。</view>
            <view class="paragraph">3. 医院是特殊公共场所，为了您和他人的健康安全，请全程佩戴口罩，建议一名患者一名陪护。</view>
            <view class="paragraph">4. 如病情危急，请直接前往急诊科就诊，无需预约。</view>
          </view>
          
          <!-- 温馨提示 -->
          <view class="section">
            <view class="section-title">【温馨提示】</view>
            <view class="paragraph">1. 支付成功即生成挂号记录，如未自动跳转，可在“我的-挂号记录”查看。</view>
            <view class="paragraph">2. 如遇支付异常或退款问题，请联系医院客服或前往收费窗口咨询处理。</view>
            <view class="paragraph">3. 请妥善保管您的就诊卡信息，避免泄露给他人。</view>
            <view class="paragraph">4. 感谢您的理解与配合，祝您就诊顺利！</view>
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

