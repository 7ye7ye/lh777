<template>
  <view class="page-bg">
    <view class="list-header">挂号记录</view>
    <view class="record-list">
      <view class="record-item" v-for="item in records" :key="item.id">
        <view class="row">
          <text class="label">科室：</text>
          <text>{{ item.dept }}</text>
        </view>
        <view class="row">
          <text class="label">医生：</text>
          <text>{{ item.doctor }}</text>
        </view>
        <view class="row">
          <text class="label">时间：</text>
          <text>{{ item.time }}</text>
        </view>
      </view>
    </view>
    <button class="main-btn" @click="getRegisterRecord">刷新</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { userApi } from '@/api/user'
const records = ref([
  { id: 1, dept: '内科', doctor: '李医生', time: '2025-09-21 09:00' }
])
const getRegisterRecord = () => {
  userApi.getRegisterRecord().then(res => {
    uni.showToast({ title: '获取成功', icon: 'success' })
    // records.value = res.data
  })
}
</script>

<style scoped>
.page-bg { min-height: 100vh; background: #f8faff; }
.list-header { font-size: 36rpx; font-weight: bold; padding: 32rpx; }
.record-list { background: #fff; margin: 24rpx; border-radius: 16rpx; padding: 32rpx; }
.record-item { border-bottom: 1px solid #eee; padding: 16rpx 0; }
.row { display: flex; align-items: center; margin-bottom: 8rpx; }
.label { color: #888; width: 100rpx; }
.main-btn { width: 100%; margin-top: 32rpx; background: #3a9cff; color: #fff; }
</style>