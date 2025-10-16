<template>
  <view class="page-bg">
    <view class="list-header">住院预交记录</view>
    <view class="record-list">
      <view class="record-item" v-for="item in records" :key="item.id">
        <view class="row">
          <text class="label">住院号：</text>
          <text>{{ item.hospitalNo }}</text>
        </view>
        <view class="row">
          <text class="label">金额：</text>
          <text>{{ item.amount }}</text>
        </view>
        <view class="row">
          <text class="label">时间：</text>
          <text>{{ item.time }}</text>
        </view>
      </view>
    </view>
    <button class="main-btn" @click="getHospitalRecord">刷新</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { userApi } from '@/api/user'
const records = ref([
  { id: 1, hospitalNo: '0001234567', amount: '1000元', time: '2025-09-21 09:00' }
])
const getHospitalRecord = () => {
  userApi.getHospitalRecord().then(res => {
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