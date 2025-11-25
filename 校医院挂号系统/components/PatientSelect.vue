<template>
  <view class="patient-select-wrapper">
    <view class="patient-select-card">
      <view class="card-header">
        <text class="card-icon">💳</text>
        <view class="card-title-wrapper">
          <text class="card-title">选择就诊卡</text>
          <text class="card-subtitle">挂号前需确认本次使用的就诊人</text>
        </view>
      </view>
      <view class="card-content">
        <view v-if="loading" class="loading-text">正在获取就诊人信息，请稍候...</view>
        <view v-else class="tip-text">
          <text class="tip-line">· 若当前账号暂无就诊卡，系统会引导您前往创建。</text>
          <text class="tip-line">· 若只有一张就诊卡，将自动使用该就诊人进行挂号。</text>
          <text class="tip-line">· 若有多张就诊卡，将弹出列表供您选择本次就诊人。</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { patientApi } from '@/api/patient'
import { useUserStore } from '@/store/user'

const loading = ref(false)

// 确保选择一个就诊人：
// - 无就诊卡：提示去创建就诊卡，返回 null
// - 单卡：直接返回该 patientId
// - 多卡：弹出 ActionSheet 选择就诊人，返回选中的 patientId
const ensurePatientSelected = async () => {
  const userStore = useUserStore()
  const userId = userStore.userInfo?.userId

  if (!userId) {
    uni.showModal({
      title: '提示',
      content: '请先登录后再进行挂号操作。',
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/subpkg/auth/login' })
        }
      }
    })
    return null
  }

  loading.value = true
  try {
    const data = await patientApi.getPatientList({ userId })

    let list = []
    if (Array.isArray(data)) {
      list = data
    } else if (data && Array.isArray(data.list)) {
      list = data.list
    }

    if (!list.length) {
      uni.showModal({
        title: '提示',
        content: '当前账号还没有就诊卡，无法挂号，请先创建就诊卡。',
        confirmText: '去创建',
        cancelText: '稍后再说',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: '/subpkg/profile/personal/create-card' })
          }
        }
      })
      return null
    }

    if (list.length === 1) {
      const patient = list[0]
      if (!patient || !patient.patientId) return null
      return { patientId: patient.patientId }
    }

    // 多个就诊人，弹出选择框
    return await new Promise((resolve) => {
      uni.showActionSheet({
        itemList: list.map(item => {
          const name = item.patientName || '未命名就诊人'
          const typeMap = { 1: '学生', 2: '教师', 3: '职工' }
          const typeText = typeMap[item.patientType] || ''
          return typeText ? `${name}（${typeText}）` : name
        }),
        success: (res) => {
          const index = res.tapIndex
          const selected = list[index]
          if (selected && selected.patientId) {
            resolve({ patientId: selected.patientId })
          } else {
            resolve(null)
          }
        },
        fail: () => {
          resolve(null)
        }
      })
    })
  } catch (error) {
    console.error('获取就诊人列表失败：', error)
    uni.showToast({
      title: '获取就诊人信息失败，请稍后重试',
      icon: 'none'
    })
    return null
  } finally {
    loading.value = false
  }
}

defineExpose({ ensurePatientSelected })
</script>

<style scoped>
.patient-select-wrapper {
	padding: 12rpx 16rpx 0 16rpx;
}

.patient-select-card {
	background: #ffffff;
	border-radius: 16rpx;
	padding: 20rpx 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(58, 156, 255, 0.08);
	display: flex;
	flex-direction: column;
}

.card-header {
	display: flex;
	flex-direction: row;
	align-items: center;
	margin-bottom: 12rpx;
}

.card-icon {
	font-size: 40rpx;
	margin-right: 16rpx;
}

.card-title-wrapper {
	display: flex;
	flex-direction: column;
}

.card-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333333;
}

.card-subtitle {
	margin-top: 4rpx;
	font-size: 24rpx;
	color: #999999;
}

.card-content {
	margin-top: 4rpx;
}

.loading-text {
	font-size: 26rpx;
	color: #3a9cff;
}

.tip-text {
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.tip-line {
	font-size: 24rpx;
	color: #666666;
}
</style>
