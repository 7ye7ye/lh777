<template>
  <view class="home-bg">
    <view class="home-header">
      <text class="title">北京交通大学校医院</text>
      <view class="header-icons">
        <view class="header-icon">⚙️</view>
      </view>
    </view>
    <view class="banner">
      <image src="/static/hospitalpicture.png" mode="aspectFill" style="width: 100%; height: 100%; border-radius: 12rpx;" />
    </view>
    <view class="visit-card card" @click="onVisitCardClick">
      <view class="visit-left">
        <view class="weather">周晴晴  女  20岁</view>
        <view class="ecard">电子就诊卡</view>
        <view class="visit-no">门诊号：M01078965</view>
      </view>
      <view class="visit-right">
        <view class="qrcode">📱</view>
        <view class="enter">出示就诊码</view>
      </view>
    </view>
    <view class="bind-tip card">
      <text class="plus">+</text>
      <text>首次使用，请绑定就诊人</text>
    </view>
    <view class="night-banner">
      <text>“ 晚间门诊 ” 专栏</text>
      <button class="night-btn" size="mini">点击进入</button>
    </view>
    <view class="quick card">
      <view class="quick-grid">
        <view class="quick-item">
          <view class="quick-icon">📝</view>
          <text>按疾病挂号</text>
        </view>
        <view class="quick-item">
          <view class="quick-icon">🏥</view>
          <text>按科室挂号</text>
        </view>
        <view class="quick-item">
          <view class="quick-icon">📊</view>
          <text>报告查询</text>
        </view>
        <view class="quick-item">
          <view class="quick-icon">🌐</view>
          <text>互联网诊疗</text>
        </view>
      </view>
    </view>
    <view class="home-tabs card">
      <view 
        v-for="(tab, idx) in tabs" 
        :key="tab" 
        class="tab" 
        :class="{ active: idx === activeIndex }"
        @click="activeIndex = idx"
      >{{ tab }}</view>
    </view>
    <view class="home-section card">
      <view class="home-grid">
        <view 
          v-for="item in currentItems" 
          :key="item.text" 
          class="home-item" 
          @click="onItemClick(item)"
        >
          <view class="icon">{{ item.icon }}</view>
          <text>{{ item.text }}</text>
        </view>
      </view>
    </view>
    <!-- 未登录内联提示 -->
    <LoginPrompt ref="loginPromptRef" mode="inline" message="登录后可出示电子就诊码" login-text="去登录" />
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import LoginPrompt from '@/components/LoginPrompt.vue'
import { useUserStore } from '@/store/user'

const tabs = ['门诊', '住院', '体检', '其他']
const activeIndex = ref(0)
const loginPromptRef = ref(null)
const userStore = useUserStore()

const itemsMap = {
  门诊: [
    { icon: '🌙', text: '晚间门诊' },
    { icon: '📅', text: '周末门诊' },
    { icon: '📋', text: '门诊签到' },
    { icon: '🧠', text: '心理筛查门诊' },
    { icon: '🗓️', text: '超声签到' },
    { icon: '🧾', text: '看结果K号' },
    { icon: '💴', text: '门诊缴费' },
    { icon: '🔎', text: '检查预约' },
    { icon: '🧾', text: '电子发票' },
    { icon: '📂', text: '电子票夹' },
    { icon: '🧭', text: '院内导航' },
    { icon: '📘', text: '门诊服务指南' },
    { icon: '📝', text: '预约记录' },
    { icon: '💬', text: '护理咨询' },
    { icon: '💳', text: '就诊卡余额退款' },
    { icon: '📚', text: '病史采集' },
    { icon: '🤖', text: '智能导诊' },
  ],
  住院: [
    { icon: '💳', text: '住院预交' },
    { icon: '🧾', text: '在院费用查询' },
    { icon: '🪪', text: '电子陪护证' },
    { icon: '📄', text: '病案复印' },
    { icon: '🧾', text: '住院发票清单' },
    { icon: '📘', text: '住院服务指南' },
    { icon: '🍱', text: '住院订餐' },
    { icon: '🧾', text: '订单清单' },
    { icon: '🍼', text: '出生证预约' },
    { icon: '🧠', text: '心理筛查住院' },
    { icon: '📊', text: '满意度调查' },
  ],
  体检: [
    { icon: '👤', text: '个检预约' },
    { icon: '👥', text: '团检预约' },
    { icon: '🗂️', text: '体检报告' },
    { icon: '🧾', text: '体检订单' },
    { icon: '🏥', text: '体检中心' },
  ],
  其他: [
    { icon: '📚', text: '健康百科' },
    { icon: '📣', text: '科普宣教' },
    { icon: '🆘', text: '帮助与反馈' },
    { icon: '💴', text: '价目公示' },
    { icon: '➕', text: '移动随访' },
    { icon: '🚑', text: '院前急救' },
    { icon: '💉', text: '惠民复诊' },
  ],
}

const currentItems = computed(() => itemsMap[tabs[activeIndex.value]] || [])

const onItemClick = (item) => {
  uni.showToast({ title: item.text, icon: 'none' })
}

const onVisitCardClick = () => {
  if (!userStore.isLoggedIn) {
    loginPromptRef.value && loginPromptRef.value.open('请先登录后查看电子就诊卡')
    return
  }
  // 已登录时进入就诊码/就诊卡页面
  uni.navigateTo({ url: '/subpkg/profile/personal/mycard' })
}
</script>

<style scoped>
.home-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 120rpx;
}
.home-header {
  background: #3a9cff;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}
.title {
  color: #fff;
  font-size: 36rpx;
  font-weight: bold;
}
.header-icons {
  display: flex;
  align-items: center;
}
.header-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
}
.banner {
  width: 100%;
  height: 180rpx;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80rpx;
  background: #f0f0f0;
  border-radius: 12rpx;
}
.bind-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(90deg, #3a9cff 0%, #1de9b6 100%);
  color: #fff;
  font-size: 28rpx;
  border-radius: 16rpx;
  margin: 16rpx 24rpx 0 24rpx;
  padding: 24rpx 0;
  font-weight: bold;
}
.plus {
  font-size: 36rpx;
  margin-right: 16rpx;
}
.special-banner {
  width: 92%;
  margin: 24rpx 4% 0 4%;
  border-radius: 16rpx;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60rpx;
  background: #f0f0f0;
}
.visit-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 16rpx 24rpx 0 24rpx;
  padding: 16rpx 24rpx;
}
.visit-left .weather { font-size: 26rpx; color: #fff; }
.visit-left .ecard { margin-top: 8rpx; background: #fff; color: #3a9cff; display: inline-block; padding: 6rpx 12rpx; border-radius: 8rpx; font-size: 24rpx; }
.visit-left .visit-no { margin-top: 8rpx; color: #fff; font-size: 26rpx; }
.visit-right { display:flex; flex-direction: column; align-items: center; color:#fff; }
.visit-right .qrcode { font-size: 48rpx; }
.visit-right .enter { font-size: 22rpx; margin-top: 6rpx; }

.night-banner {
  margin: 16rpx 24rpx 0 24rpx;
  height: 120rpx;
  border-radius: 16rpx;
  background: linear-gradient(90deg, #6a00ff 0%, #8a2eff 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  color: #fff;
  font-weight: bold;
}
.night-btn { background: #fff; color: #6a00ff; border-radius: 999rpx; padding: 8rpx 16rpx; }

.quick .quick-grid { display: flex; }
.quick-item { width: 25%; display: flex; flex-direction: column; align-items: center; }
.quick-icon { width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; font-size: 32rpx; margin-bottom: 8rpx; }
.card {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx 24rpx 0 24rpx;
  padding: 24rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}
.home-section {
  margin-top: 24rpx;
}
.home-row {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  margin: 0 0 16rpx 0;
}
.home-grid {
  display: flex;
  flex-wrap: wrap;
}
.home-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 16rpx 0;
}
.icon {
  width: 56rpx;
  height: 56rpx;
  margin-bottom: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}
.home-tabs {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx 24rpx 0 24rpx;
  overflow: hidden;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #888;
}
.tab.active {
  color: #3a9cff;
  font-weight: bold;
  border-bottom: 4rpx solid #3a9cff;
  background: #f0f8ff;
}
.tabbar-placeholder {
  height: 120rpx;
}
</style>
