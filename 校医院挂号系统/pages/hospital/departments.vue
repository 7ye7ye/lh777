<template>
  <view class="dept-bg">
    <view class="dept-card">
      <view 
        v-for="dept in departments" 
        :key="dept.key" 
        class="dept-row" 
        @click="goDetail(dept.key)"
      >
        <view class="dot"></view>
        <view class="name">{{ dept.name }}</view>
        <view class="date">{{ dept.date }}</view>
      </view>
    </view>
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const departments = ref([
  { key: 'neike', name: '内科', date: '2014-05-09' },
  { key: 'waike', name: '外科', date: '2014-05-09' },
  { key: 'zhongyi', name: '中医理疗科', date: '2014-05-09' },
  { key: 'kouqiang', name: '口腔科', date: '2024-12-11' },
  { key: 'huli', name: '护理部', date: '2014-05-09' },
  { key: 'jiankang', name: '健康管理中心', date: '2014-05-09' },
  { key: 'yiji', name: '保健医技科', date: '2024-12-11' },
  { key: 'yaoji', name: '药剂科', date: '2014-05-09' },
  { key: 'jingshen', name: '精神卫生防治科', date: '2024-12-11' },
  { key: 'dongqu', name: '东校区门诊部', date: '2024-12-11' },
  { key: 'caiwu', name: '院办财务科', date: '2024-12-11' },
])

const goDetail = (key) => {
  uni.navigateTo({ url: `/pages/hospital/department-detail?key=${key}` })
}
</script>

<style scoped>
.dept-bg {
  background: #f8faff;
  min-height: 100vh;
  padding: 16rpx 0 120rpx 0;
}
.dept-card {
  background: #fff;
  border-radius: 16rpx;
  margin: 16rpx 16rpx 0 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}
.dept-row {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #eee;
}
.dept-row:last-child {
  border-bottom: none;
}
.dot {
  width: 8rpx;
  height: 8rpx;
  background: #d0021b;
  border-radius: 50%;
  margin-right: 16rpx;
}
.name {
  flex: 1;
  font-size: 30rpx;
  color: #222;
}
.date {
  font-size: 26rpx;
  color: #999;
}
.tabbar-placeholder { height: 120rpx; }
</style>


