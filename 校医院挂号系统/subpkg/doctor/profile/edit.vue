<template>
  <view class="edit-page">
    <view class="edit-card">
      <view class="edit-header">
        <text class="edit-title">资料变更申请</text>
        <text class="edit-subtitle">提交后将由管理员审核，审核通过后才会生效</text>
      </view>

      <view class="form-group avatar-group">
        <text class="label">医生头像</text>
        <view class="avatar-row">
          <image class="avatar" :src="avatarPreview" mode="aspectFill" />
          <button class="avatar-btn" size="mini" @click="chooseAvatar">更换头像</button>
        </view>
        <text class="avatar-tip">请上传清晰的职业形象照片，头像变更同样需要管理员审核</text>
      </view>

      <view class="form-group">
        <text class="label">擅长领域</text>
        <textarea
          class="textarea"
          v-model.trim="form.specialty"
          placeholder="例如：心律失常、心力衰竭、冠心病等"
          auto-height
        />
      </view>

      <view class="form-group">
        <text class="label">医生简介</text>
        <textarea
          class="textarea"
          v-model.trim="form.doctorDesc"
          placeholder="可简要介绍教育背景、工作经历、主要研究方向等"
          auto-height
        />
      </view>
    </view>

    <view class="btn-bar">
      <button class="btn cancel" @click="goBack">取消</button>
      <button class="btn save" :loading="saving" @click="handleSave">提交申请</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { doctorApi } from '@/api/doctor'

const form = ref({
  id: null,
  avatar: '',     // avatar
  specialty: '',  // specialty
  doctorDesc: ''  // doctor_desc
})

const loading = ref(false)
const saving = ref(false)

// 头像本地预览 & 临时路径
const avatarPreview = ref('/static/doctor.svg')
const avatarTempPath = ref('')

const loadProfile = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const p = await doctorApi.getMyProfile()
    if (!p || (typeof p === 'object' && Object.keys(p).length === 0)) {
      uni.showToast({ title: '未获取到医生资料', icon: 'none' })
      return
    }
    form.value = {
      id: p.doctorId || p.id || null,
      avatar: p.avatar || p.avatarUrl || p.photo || '',
      specialty: p.specialty || '',
      doctorDesc: p.doctor_desc || p.description || ''
    }
    avatarPreview.value = form.value.avatar || '/static/doctor.svg'
  } catch (e) {
    console.error('加载医生资料失败:', e)
    uni.showToast({ title: '加载医生资料失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onMounted(loadProfile)

const goBack = () => {
  uni.navigateBack()
}

// 选择头像（前端预览，提交时一并作为变更内容，后端需提供上传接口）
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      const path = res.tempFilePaths && res.tempFilePaths[0]
      if (!path) return
      avatarTempPath.value = path
      avatarPreview.value = path
      // 实际项目中建议：这里先调用 uni.uploadFile 上传到服务器，拿到正式 URL 后赋值给 form.value.avatar
      // TODO: uni.uploadFile 上传头像，成功后将返回的 URL 写入 form.value.avatar
    }
  })
}

const handleSave = async () => {
  if (saving.value) return
  const payload = { ...form.value }

  if (!payload.specialty?.trim()) {
    uni.showToast({ title: '擅长领域不能为空', icon: 'none' })
    return
  }
  if (!payload.specialty?.trim()) {
    uni.showToast({ title: '擅长领域不能为空', icon: 'none' })
    return
  }

  saving.value = true
  try {
    // 此处 payload.avatar 建议填服务器上的头像 URL
    if (typeof doctorApi.applyProfileUpdate === 'function') {
      await doctorApi.applyProfileUpdate(payload)
      uni.showToast({ title: '申请已提交，待管理员审核', icon: 'success' })
    } else {
      uni.showToast({ title: '后端未提供资料变更申请接口', icon: 'none' })
    }
    setTimeout(() => {
      uni.navigateBack()
    }, 300)
  } catch (e) {
    console.error('保存个人信息失败:', e)
    uni.showToast({ title: '保存失败，请稍后重试', icon: 'none' })
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.edit-page {
  min-height: 100vh;
  padding: 24rpx;
  box-sizing: border-box;
  background: #f5f7fb;
}

.edit-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.edit-header {
  margin-bottom: 16rpx;
}

.edit-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.edit-subtitle {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: #6b7280;
}

.form-group {
  margin-top: 16rpx;
}

.label {
  font-size: 26rpx;
  color: #4b5563;
}

.input {
  margin-top: 8rpx;
  width: 100%;
  height: 72rpx;
  padding: 0 16rpx;
  box-sizing: border-box;
  border-radius: 12rpx;
  background: #f9fafb;
  font-size: 26rpx;
}

.textarea {
  margin-top: 8rpx;
  width: 100%;
  min-height: 120rpx;
  padding: 12rpx 16rpx;
  box-sizing: border-box;
  border-radius: 12rpx;
  background: #f9fafb;
  font-size: 26rpx;
}

.btn-bar {
  margin-top: 24rpx;
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.btn {
  flex: 1;
  border: none;
  border-radius: 999rpx;
  padding: 22rpx 0;
  font-size: 28rpx;
}

.btn.cancel {
  background: #e5e7eb;
  color: #374151;
}

.btn.save {
  background: #2563eb;
  color: #fff;
}
</style>
