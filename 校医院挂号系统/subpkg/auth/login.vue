<template>
  <view class="login-page">
    <view class="header">
      <view class="title">个人中心</view>
      <view class="subtitle">校医院挂号系统</view>
    </view>

    <view class="card">
      <view class="card-title">账号登录</view>

      <view class="input-group">
        <view class="input-label">账号</view>
        <input
          class="input"
          v-model.trim="form.userAccount"
          placeholder="请输入学号/工号/手机号"
          type="text"
          confirm-type="done"
          @confirm="onSubmit"
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
          confirm-type="go"
          @confirm="onSubmit"
        />
      </view>

      <button
        class="btn"
        type="primary"
        :loading="loading"
        :disabled="disabled"
        @click="onSubmit"
      >
        登录
      </button>

      <view class="tips link" @click="goRegister">没有账号？去注册</view>

      <view class="tips">
        忘记密码请联系管理员重置
      </view>
    </view>
  </view>
  
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { userApi } from '@/api/user'
import { uniShowToast, uniSwitchTab, uniNavigateTo } from '@/utils/uniHelper'

const form = ref({
  userAccount: '',
  userPassword: ''
})

const loading = ref(false)
const disabled = computed(() => !form.value.userAccount || !form.value.userPassword || loading.value)

const userStore = useUserStore()

const onSubmit = async () => {
  if (disabled.value) return
  loading.value = true
  try {
    const res = await userApi.login({ userAccount: form.value.userAccount, userPassword: form.value.userPassword })
    console.log('登录接口返回数据:', res)
    
    // 根据后端返回结构提取数据
    // 后端返回格式: { code: 0, data: { user: {...}, token: "..." } }
    // 响应拦截器会解包 data 字段，所以 res 就是 { user: {...}, token: "..." }
    const token = res?.token || ''
    const userInfo = res?.user || null
    
    if (!userInfo) {
      await uniShowToast({ title: '登录失败：未获取到用户信息', icon: 'none' })
      return
    }
    
    // 检查用户类型
    if (userInfo.userType == 3) {
      await uniShowToast({ title: '登录失败，请使用患者或医生账号登录', icon: 'none' })
      return
    }
    
    if (!token) {
      await uniShowToast({ title: '登录失败：未获取到token', icon: 'none' })
      return
    }
    
    // 保存 token 和用户信息到状态管理
    userStore.setToken(token)
    userStore.setUserInfo(userInfo)
    
    await uniShowToast({ title: '登录成功' })
    
    // 根据用户类型跳转
    if (userInfo.userType == 1) {
      await uniSwitchTab({ url: '/pages/home/home' })
    } else if (userInfo.userType == 2) {
      // 医生端直接重启到排班首页，避免出现返回上一页按钮
      await uni.reLaunch({ url: '/subpkg/doctor/schedule/main' })
    } else {
      // 默认跳转到首页
      await uniSwitchTab({ url: '/pages/home/home' })
    }
  } catch (e) {
    console.error('登录失败:', e)
    // 错误信息已经在响应拦截器中显示过了，这里不需要再次显示
    // 但如果响应拦截器没有处理，这里作为兜底
    if (e && e.message) {
      // 如果错误信息还没有显示过，才显示
      // 由于响应拦截器已经显示了 toast，这里可以不再显示
    }
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  uniNavigateTo({ url: '/subpkg/auth/register' })
}

</script>

<style scoped>
.login-page {
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
.tips.link { color: #1677ff; }
</style>