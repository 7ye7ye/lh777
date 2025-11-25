<template>
  <view class="page-bg">
    <view class="page-header">
      <text class="title">挂号支付</text>
    </view>

    <!-- 挂号信息 -->
    <view class="section card">
      <text class="label">挂号信息</text>
      <view class="info-item"><text>科室：</text>{{ dept }}</view>
      <view class="info-item"><text>医生：</text>{{ doctor }}</view>
      <view class="info-item"><text>时间：</text>{{ time }}</view>
      <view class="info-item"><text>挂号费：</text><text class="price">¥{{ fee }}</text></view>
    </view>

    <!-- 支付方式 -->
    <view class="section card">
      <text class="label">选择支付方式</text>
      <view 
        v-for="method in paymentMethods" 
        :key="method.value" 
        class="pay-item"
        :class="{ selected: selectedMethod === method.value }"
        @click="selectedMethod = method.value"
      >
        <view class="pay-left">
          <image :src="method.icon" class="icon"></image>
          <text>{{ method.name }}</text>
        </view>
        <view v-if="selectedMethod === method.value" class="check">✔️</view>
      </view>
    </view>

    <!-- 支付按钮 -->
    <view class="bottom-btn">
      <button 
        class="pay-btn" 
        :disabled="!selectedMethod" 
        @click="onPay"
      >
        立即支付
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { createRegistration } from '../../api/registration' // 挂号接口
import { ensurePatientCard } from '@/utils/patientHelper'

// ------------------ 挂号信息 ------------------
const dept = ref('')
const doctor = ref('')
const time = ref('')
const fee = ref(20)
const doctorId = ref(null)
const typeId = ref(null)
const scheduleId = ref(null)
const currentPatient = ref(null)

// ------------------ 支付方式 ------------------
const paymentMethods = ref([
  { name: '微信支付', value: 'wechat', icon: '/static/icons/wechat.png' },
  { name: '支付宝支付', value: 'alipay', icon: '/static/icons/alipay.png' },
  { name: '医保卡支付', value: 'medical', icon: '/static/icons/medical.png' }
])
const selectedMethod = ref(null)

// ------------------ 页面加载 ------------------
const loadPatientInfo = async () => {
  const info = await ensurePatientCard()
  if (info && info.patientId) {
    currentPatient.value = info
  } else {
    currentPatient.value = null
  }
}

const ensurePatientId = async () => {
  if (currentPatient.value?.patientId) {
    return currentPatient.value.patientId
  }
  await loadPatientInfo()
  if (currentPatient.value?.patientId) {
    return currentPatient.value.patientId
  }
  uni.showModal({
    title: '未找到就诊卡',
    content: '请先创建并绑定就诊卡后再进行挂号支付',
    confirmText: '去创建',
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({
          url: '/subpkg/profile/personal/create-card'
        })
      }
    }
  })
  return null
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}

  dept.value = decodeURIComponent(options.dept || '未知科室')
  doctor.value = decodeURIComponent(options.doctor || '未知医生')
  time.value = decodeURIComponent(options.time || '未知时间')

  doctorId.value = Number(options.doctorId || 0)
  typeId.value = Number(options.typeId || 0)
  scheduleId.value = Number(options.scheduleId || 0)
  deptId.value = Number(options.deptId || 0)
  
  console.log('支付页接收参数:', { dept: options.dept, doctor: options.doctor, time: options.time, doctorId: options.doctorId, typeId: options.typeId, scheduleId: options.scheduleId, deptId: options.deptId })
  loadPatientInfo()
})

  



// ------------------ 时间格式转换 ------------------
const getLocalDateTime = () => {
  const now = new Date()
  const yyyy = now.getFullYear()
  const MM = String(now.getMonth() + 1).padStart(2,'0')
  const dd = String(now.getDate()).padStart(2,'0')
  const HH = String(now.getHours()).padStart(2,'0')
  const mm = String(now.getMinutes()).padStart(2,'0')
  const ss = String(now.getSeconds()).padStart(2,'0')
  return `${yyyy}-${MM}-${dd} ${HH}:${mm}:${ss}`
}

// ------------------ 支付 + 写入挂号 ------------------
const onPay = async () => {
  if (!selectedMethod.value) {
    uni.showToast({ title: '请选择支付方式', icon: 'none' })
    return
  }

  uni.showLoading({ title: '正在支付...' })

  setTimeout(async () => {
    uni.hideLoading()
    uni.showToast({ title: '支付成功！', icon: 'success', duration: 1500 })

    // 支付成功后写入挂号表
    const patientId = await ensurePatientId()
    if (!patientId) {
      return
    }

    // 构建挂号记录对象
    const record = {
      scheduleId: scheduleId.value,
      patientId,
      doctorId: doctorId.value,
      typeId: typeId.value,
      registrationNo: generateRegistrationNo(), // 前端生成或后端生成都可以
      registerTime: formatLocalDateTime(new Date()), // YYYY-MM-DD HH:mm:ss
      status: 1, // 已预约
      priceOriginal: fee.value,
      actualPrice: fee.value,
      isAdd: 0 // 正常号
    }

    try {
      await createRegistration(record, patientId, true)
      console.log('挂号记录写入成功')
    } catch (error) {
      console.error('挂号写入失败', error)
      uni.showModal({
        title: '挂号失败',
        content: '支付已成功，但挂号信息写入失败，请联系医院或重试。',
        showCancel: false
      })
      return
    }

    // 跳转首页
    setTimeout(() => {
      uni.switchTab({
        url: '/pages/home/home'
      })
    }, 1500)
  }, 2000)
}

// 工具函数：生成挂号单号（前端简单示例）
function generateRegistrationNo() {
  const date = new Date()
  const y = date.getFullYear()
  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  const d = date.getDate().toString().padStart(2, '0')
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
  return `${y}${m}${d}${doctorId.value}${random}`
}

// 工具函数：格式化时间 YYYY-MM-DD HH:mm:ss
function formatLocalDateTime(date) {
  const pad = (n) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

</script>

<style scoped>
.page-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 120rpx;
}
.page-header {
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3a9cff;
}
.page-header .title {
  font-size: 36rpx;
  color: #fff;
  font-weight: bold;
}
.section {
  margin: 24rpx;
  padding: 16rpx;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
  padding: 24rpx;
}
.label {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
  display: block;
}
.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
  font-size: 28rpx;
  color: #333;
}
.price {
  color: #ff5500;
  font-weight: bold;
}
.pay-item {
  padding: 20rpx;
  border-radius: 12rpx;
  background: #f0f8ff;
  margin-bottom: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pay-item.selected {
  background: #3a9cff;
  color: #fff;
}
.pay-left {
  display: flex;
  align-items: center;
}
.icon {
  width: 40rpx;
  height: 40rpx;
  margin-right: 16rpx;
}
.check {
  font-size: 28rpx;
}
.bottom-btn {
  margin: 32rpx 24rpx 0 24rpx;
}
.pay-btn {
  width: 100%;
  padding: 16rpx 0;
  background: #3a9cff;
  color: #fff;
  font-size: 30rpx;
  border-radius: 12rpx;
}
.pay-btn:disabled {
  background: #ccc;
  color: #999;
}
</style>
