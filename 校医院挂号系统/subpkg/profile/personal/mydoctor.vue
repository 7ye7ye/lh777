<template>
  <view class="page-bg">
    <!-- 空状态 -->
    <view class="empty-container">
      <image 
        class="empty-bg-image" 
        src="/static/images/no_data.png" 
        mode="scaleToFill"
      />
      <view class="empty-content">
        <text class="empty-text">暂无数据</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)

// 获取我的医生列表
const getDoctorList = async () => {
  loading.value = true
  try {
    // TODO: 调用获取我的医生列表API
    // const userId = userStore.userInfo?.userId
    // const data = await doctorApi.getMyDoctors({ userId })
    // 暂时显示空状态
  } catch (error) {
    console.error('获取医生列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getDoctorList()
})

onShow(() => {
  getDoctorList()
})
</script>

<style scoped>
.page-bg {
  min-height: 100vh;
  background: #f5f5f5;
}

.empty-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 40rpx 120rpx;
}

.empty-bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  opacity: 0.6;
}

.empty-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  margin-top: auto;
}

.empty-text {
  font-size: 32rpx;
  color: #999;
  letter-spacing: 2rpx;
  font-weight: 500;
}
</style>
