<template>
  <view class="page">
    <view class="header">
      <text class="title">消息中心</text>
      <button class="doctor-entry" @click="goDoctorMain">进入医生端</button>
    </view>

    <view class="list">
      <view class="msg-card" v-for="item in messages" :key="item.id" @click="openDetail(item)">
        <view class="msg-title">{{ item.title }}</view>
        <view class="msg-time">{{ item.time }}</view>
        <view class="msg-content">{{ item.content }}</view>
      </view>
      <view v-if="messages.length === 0" class="empty">暂无消息</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const messages = ref([])

onMounted(() => {
  // 可替换为真实接口：/api/messages/list
  messages.value = [
    { id: 1, title: '挂号成功通知', time: '2025-10-17 09:00', content: '请及时前往诊室' },
    { id: 2, title: '排班变更提醒', time: '2025-10-18 12:00', content: '您有新的排班变更' },
  ]
})

const goDoctorMain = () => {
  uni.navigateTo({ url: '/subpkg/doctor/schedule/main' })
}

const openDetail = (item) => {
  // 示例：跳转到现有消息详情页（subpkg/messages/detail.vue）
  uni.navigateTo({ url: `/subpkg/messages/detail?id=${item.id}` })
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f7faff; }
.header { display: flex; align-items: center; justify-content: space-between; padding: 24rpx; background: #479fff; }
.title { color: #fff; font-size: 34rpx; font-weight: bold; }
.doctor-entry { background: #fff; color: #2176ff; border-radius: 999rpx; padding: 10rpx 24rpx; font-size: 26rpx; }
.list { padding: 24rpx; }
.msg-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; box-shadow: 0 4rpx 16rpx rgba(33,118,255,0.08); }
.msg-title { font-size: 30rpx; color: #1e293b; font-weight: 600; }
.msg-time { font-size: 24rpx; color: #64748b; margin-top: 6rpx; }
.msg-content { font-size: 26rpx; color: #334155; margin-top: 12rpx; }
.empty { text-align: center; color: #64748b; font-size: 28rpx; margin-top: 48rpx; }
</style>