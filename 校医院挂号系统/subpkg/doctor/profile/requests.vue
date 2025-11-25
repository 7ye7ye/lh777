<template>
  <view class="page">
    <view class="header">
      <text class="title">我的资料修改申请</text>
    </view>

    <view class="filter-bar">
      <picker :range="statusOptions" range-key="label" @change="onStatusChange">
        <view class="picker">
          <text>{{ currentStatusLabel }}</text>
        </view>
      </picker>
    </view>

    <scroll-view scroll-y class="list">
      <view v-if="loading" class="empty">加载中...</view>
      <view v-else-if="requests.length === 0" class="empty">暂无申请记录</view>
      <view v-else v-for="item in requests" :key="item.id" class="card">
        <view class="row">
          <text class="label">提交时间</text>
          <text class="value">{{ item.createTime || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">擅长领域</text>
          <text class="value">{{ item.specialty || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">医生简介</text>
          <text class="value desc">{{ item.doctorDesc || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">状态</text>
          <text class="value" :class="'status-' + item.status">{{ statusText(item.status) }}</text>
        </view>
        <view v-if="item.reason" class="row">
          <text class="label">审批备注</text>
          <text class="value desc">{{ item.reason }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { doctorApi } from '@/api/doctor'
import { useUserStore } from '@/store/user'
import { uniShowToast } from '@/utils/uniHelper'

const userStore = useUserStore()

const loading = ref(false)
const requests = ref([])

const statusOptions = [
  { value: 0, label: '全部状态' },
  { value: 1, label: '待审核' },
  { value: 2, label: '已通过' },
  { value: 3, label: '已驳回' }
]
const currentStatus = ref(0)
const currentStatusLabel = ref('全部状态')

function statusText(status) {
  if (status === 1) return '待审核'
  if (status === 2) return '已通过'
  if (status === 3) return '已驳回'
  return '-'
}

function onStatusChange(e) {
  const idx = Number(e.detail.value || 0)
  const opt = statusOptions[idx]
  currentStatus.value = opt.value
  currentStatusLabel.value = opt.label
  fetchData()
}

async function fetchData() {
  let doctorId = null
  try {
    // 优先从接口获取当前医生资料
    let profile = await doctorApi.getMyProfile()
    if (!profile || (typeof profile === 'object' && Object.keys(profile).length === 0)) {
      const stored = userStore?.userInfo || uni.getStorageSync('userInfo') || {}
      const uid = stored.userId || stored.id
      if (uid) {
        profile = await doctorApi.getProfileByUserId(Number(uid))
      }
    }
    if (profile && (profile.doctorId || profile.id)) {
      doctorId = Number(profile.doctorId || profile.id)
    }
  } catch (e) {
    console.error('获取医生资料失败', e)
  }

  if (!doctorId) {
    await uniShowToast({ title: '未找到医生ID', icon: 'none' })
    return
  }

  loading.value = true
  try {
    const params = { doctorId, pageNo: 1, pageSize: 50 }
    if (currentStatus.value) {
      params.status = currentStatus.value
    }
    const res = await doctorApi.listProfileUpdateRequests(params)
    requests.value = (res?.records || res || []).map((r) => ({
      id: r.id,
      specialty: r.specialty,
      doctorDesc: r.doctorDesc,
      status: r.status,
      reason: r.reason,
      createTime: r.createTime
    }))
  } catch (e) {
    console.error('获取资料修改申请失败', e)
    await uniShowToast({ title: '获取申请记录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onLoad(() => {
  fetchData()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24rpx;
  box-sizing: border-box;
  background: #f5f7fb;
}
.header {
  margin-bottom: 16rpx;
}
.title {
  font-size: 32rpx;
  font-weight: 700;
}
.filter-bar {
  margin-bottom: 16rpx;
}
.picker {
  padding: 16rpx 20rpx;
  background: #fff;
  border-radius: 12rpx;
}
.list {
  max-height: calc(100vh - 160rpx);
}
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.03);
}
.row {
  display: flex;
  margin-bottom: 8rpx;
}
.label {
  width: 160rpx;
  font-size: 26rpx;
  color: #7a8aa0;
}
.value {
  flex: 1;
  font-size: 26rpx;
  color: #2f3b52;
}
.value.desc {
  white-space: pre-wrap;
}
.status-1 {
  color: #2a7bff;
}
.status-2 {
  color: #52c41a;
}
.status-3 {
  color: #ff4d4f;
}
.empty {
  margin-top: 40rpx;
  text-align: center;
  color: #999;
}
</style>
