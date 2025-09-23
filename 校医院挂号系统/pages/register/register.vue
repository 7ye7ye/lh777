<template>
  <view class="register-page">
    <view class="header">
      <view class="title">注册账号</view>
      <view class="subtitle">校医院挂号系统</view>
    </view>

    <view class="card">
      <view class="card-title">信息填写</view>

      <view class="input-group">
        <view class="input-label">账号</view>
        <input
          class="input"
          v-model.trim="form.userAccount"
          placeholder="请输入学号/工号/手机号"
          type="text"
          confirm-type="next"
        />
      </view>

      <view class="input-group">
        <view class="input-label">密码</view>
        <input
          class="input"
          v-model.trim="form.userPassword"
          placeholder="请输入密码"
          password
          type="text"
          confirm-type="next"
        />
      </view>

      <view class="input-group">
        <view class="input-label">确认密码</view>
        <input
          class="input"
          v-model.trim="form.checkPassword"
          placeholder="请确认密码"
          password
          type="text"
          confirm-type="done"
        />
      </view>

      <button
        class="btn"
        type="primary"
        :loading="loading"
        :disabled="disabled"
        @click="onSubmit"
      >
        注册
      </button>

      <view class="tips alt" @click="goLogin">已有账号？去登录</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { userApi } from '@/api/user'
 import { uniShowToast, uniNavigateTo } from '@/utils/uniHelper'

const form = ref({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const loading = ref(false)
const disabled = computed(() => !form.value.userAccount || !form.value.userPassword || !form.value.checkPassword || loading.value)

const onSubmit = async () => {
  if (disabled.value) return
  loading.value = true
  try {
    if (form.value.userPassword !== form.value.checkPassword) {
      await uniShowToast({ title: '两次密码不一致', icon: 'none' })
      return
    }
    // 后端期望字段：{ userAccount, userPassword, checkPassword }
    const res=await userApi.register({ userAccount: form.value.userAccount, userPassword: form.value.userPassword, checkPassword: form.value.checkPassword })
    console.log(res)
	await uniShowToast({ title: '注册成功' })
    await uniNavigateTo({ url: '/pages/login/login' })
  } catch (e) {
    await uniShowToast({ title: (e && e.message) || '注册失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const goLogin = () => {
  uniNavigateTo({ url: '/pages/login/login' })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #4da3ff 0%, #5db7ff 40%, #f6f7fb 40%, #f6f7fb 100%);
}
.header {
  padding: 80rpx 40rpx 60rpx 40rpx;
  color: #fff;
}
.title {
  font-size: 40rpx;
  font-weight: 700;
}
.subtitle {
  margin-top: 10rpx;
  opacity: 0.9;
  font-size: 26rpx;
}
.card {
  margin: -60rpx 32rpx 0 32rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.06);
  padding: 40rpx 32rpx 50rpx 32rpx;
}
.card-title {
  font-size: 34rpx;
  font-weight: 600;
  margin-bottom: 30rpx;
}
.input-group { 
  margin-bottom: 28rpx; 
}
.input-label {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
}
.input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  background: #fafafa;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  font-size: 28rpx;
}
.btn {
  width: 100%;
  height: 92rpx;
  line-height: 92rpx;
  font-size: 32rpx;
  color: #fff;
  background: #1677ff;
  border: none;
  border-radius: 14rpx;
}
.btn:disabled {
  background: #a5c8ff;
}
.tips {
  text-align: center;
  color: #999;
  font-size: 24rpx;
  margin-top: 24rpx;
}
.tips.alt { color: #1677ff; }
</style>


