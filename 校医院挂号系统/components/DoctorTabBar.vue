<template>
  <view class="doctor-tab-bar">
    <view
      class="tab-item"
      :class="{ active: active === 'patients' }"
      @click="goTo('patients')"
    >
      <text class="tab-icon">👥</text>
      <text class="tab-text">患者列表</text>
    </view>
    <view
      class="tab-item"
      :class="{ active: active === 'schedule' }"
      @click="goTo('schedule')"
    >
      <text class="tab-icon">📅</text>
      <text class="tab-text">排班管理</text>
    </view>
    <view
      class="tab-item"
      :class="{ active: active === 'profile' }"
      @click="goTo('profile')"
    >
      <text class="tab-icon">👤</text>
      <text class="tab-text">我的信息</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  active: {
    type: String,
    default: 'schedule'
  }
})

const goTo = (key) => {
  if (key === props.active) return
  let url = ''
  if (key === 'patients') {
    url = '/subpkg/doctor/patients/list'
  } else if (key === 'schedule') {
    url = '/subpkg/doctor/schedule/main'
  } else if (key === 'profile') {
    url = '/subpkg/doctor/profile/index'
  }
  if (!url) return
  uni.navigateTo({ url })
}
</script>

<style scoped>
.doctor-tab-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100rpx;
  padding: 8rpx 24rpx;
  box-sizing: border-box;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
  border-top: 1rpx solid #e2e8f0;
  box-shadow: 0 -4rpx 12rpx rgba(15, 23, 42, 0.06);
  z-index: 998;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 22rpx;
}

.tab-item.active {
  color: #2563eb;
}

.tab-icon {
  font-size: 32rpx;
}

.tab-text {
  margin-top: 4rpx;
}
</style>
