<template>
  <view class="page-bg">
    <view class="hero-card">
      <view class="hero-left">
        <view class="hero-name">{{ formData.name || '未填写姓名' }}</view>
        <view class="hero-meta">
          <text class="hero-tag">{{ formData.gender || '性别未填' }}</text>
          <text class="hero-dot">•</text>
          <text class="hero-tag">{{ formData.birthDate || '生日未填' }}</text>
        </view>
        <view class="hero-id">证件号：{{ formData.idNumber || '未填写' }}</view>
      </view>
      <view class="hero-right">
        <view class="hero-pill">
          <text class="pill-label">联系电话</text>
          <text class="pill-value">{{ formData.phone || '未填写' }}</text>
        </view>
        <button class="pill-btn" size="mini" @click="goToHealthProfile">
          健康档案
        </button>
      </view>
    </view>

    <view class="card form-container">
      <view class="card-title">基本信息</view>
      <view class="form-item">
        <text class="form-label">姓名</text>
        <input class="form-input" v-model="formData.name" placeholder="请输入姓名" />
      </view>
      
      <view class="form-item">
        <text class="form-label">证件类型</text>
        <picker @change="onIdTypeChange" :value="idTypeIndex" :range="idTypeOptions">
          <view class="form-picker">
            {{ formData.idType || '请选择证件类型' }}
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="form-label">证件号码</text>
        <input class="form-input" v-model="formData.idNumber" placeholder="请输入证件号码" />
      </view>
      
      <view class="form-item">
        <text class="form-label">性别</text>
        <picker @change="onGenderChange" :value="genderIndex" :range="genderOptions">
          <view class="form-picker">
            {{ formData.gender || '请选择性别' }}
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="form-label">出生日期</text>
        <picker mode="date" @change="onBirthDateChange" :value="formData.birthDate">
          <view class="form-picker">
            {{ formData.birthDate || '请选择出生日期' }}
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
    </view>

    <view class="card form-container">
      <view class="card-title">身份信息</view>
      <view class="form-item">
        <text class="form-label">民族</text>
        <picker @change="onNationChange" :value="nationIndex" :range="nationOptions">
          <view class="form-picker">
            {{ formData.nation || '请选择民族' }}
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="form-label">国籍</text>
        <picker @change="onNationalityChange" :value="nationalityIndex" :range="nationalityOptions">
          <view class="form-picker">
            {{ formData.nationality || '请选择国籍' }}
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="form-label">所在地区</text>
        <picker mode="region" @change="onRegionChange" :value="regionValue">
          <view class="form-picker">
            {{ formData.region || '请选择所在地区' }}
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="form-label">详细住址</text>
        <input class="form-input" v-model="formData.address" placeholder="请输入详细住址" />
      </view>
    </view>

    <view class="card form-container">
      <view class="card-title">联系与验证</view>
      <view class="form-item">
        <text class="form-label">电话号码</text>
        <input class="form-input" v-model="formData.phone" placeholder="请输入电话号码" />
      </view>
      
      <view class="form-item">
        <text class="form-label">短信验证码</text>
        <view class="verification-group">
          <input class="form-input verification-input" v-model="formData.verificationCode" placeholder="请填写验证码" />
          <button class="verification-btn" @click="getVerificationCode" :disabled="countdown > 0">
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </button>
        </view>
      </view>
      
      <view class="form-item archive-row" @click="goToHealthProfile">
        <view class="health-archive">
          <view class="archive-icon">📋</view>
          <view class="archive-info">
            <text class="archive-label">健康档案</text>
            <text class="archive-optional">可选填，补充血型/婚育等信息</text>
          </view>
        </view>
        <text class="picker-arrow">›</text>
      </view>
    </view>
    
    <button class="submit-btn" @click="submitForm" :loading="loading">确认修改</button>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { patientApi } from '@/api/patient'
import { useUserStore } from '@/store/user'
import { uniShowToast } from '@/utils/uniHelper'

const userStore = useUserStore()
const loading = ref(false)
const countdown = ref(0)
const patientId = ref(null)

// 表单数据
const formData = reactive({
  name: '',
  idType: '身份证',
  idNumber: '',
  gender: '女',
  birthDate: '2004-09-29',
  nation: '汉族',
  nationality: '中国',
  region: '北京市北京市海淀区',
  address: '北京交通大学',
  phone: '',
  verificationCode: ''
})

// 选择器选项
const idTypeOptions = ['身份证', '护照', '军官证', '其他']
const genderOptions = ['男', '女']
const nationOptions = ['汉族', '蒙古族', '回族', '藏族', '维吾尔族', '苗族', '彝族', '壮族', '布依族', '朝鲜族', '满族', '侗族', '瑶族', '白族', '土家族', '哈尼族', '哈萨克族', '傣族', '黎族', '傈僳族', '佤族', '畲族', '高山族', '拉祜族', '水族', '东乡族', '纳西族', '景颇族', '柯尔克孜族', '土族', '达斡尔族', '仫佬族', '羌族', '布朗族', '撒拉族', '毛南族', '仡佬族', '锡伯族', '阿昌族', '普米族', '塔吉克族', '怒族', '乌孜别克族', '俄罗斯族', '鄂温克族', '德昂族', '保安族', '裕固族', '京族', '塔塔尔族', '独龙族', '鄂伦春族', '赫哲族', '门巴族', '珞巴族', '基诺族']
const nationalityOptions = ['中国', '美国', '英国', '日本', '韩国', '其他']

// 选择器索引
const idTypeIndex = ref(0)
const genderIndex = ref(1)
const nationIndex = ref(0)
const nationalityIndex = ref(0)
const regionValue = ref(['北京市', '北京市', '海淀区'])

// 选择器事件处理
const onIdTypeChange = (e) => {
  idTypeIndex.value = e.detail.value
  formData.idType = idTypeOptions[e.detail.value]
}

const onGenderChange = (e) => {
  genderIndex.value = e.detail.value
  formData.gender = genderOptions[e.detail.value]
}

const onBirthDateChange = (e) => {
  formData.birthDate = e.detail.value
}

const onNationChange = (e) => {
  nationIndex.value = e.detail.value
  formData.nation = nationOptions[e.detail.value]
}

const onNationalityChange = (e) => {
  nationalityIndex.value = e.detail.value
  formData.nationality = nationalityOptions[e.detail.value]
}

const onRegionChange = (e) => {
  regionValue.value = e.detail.value
  formData.region = e.detail.value.join('')
}

// 跳转到健康档案页面
const goToHealthProfile = () => {
  // 直接跳转，如果没有patientId则传空值，健康档案页面会处理
  uni.navigateTo({
    url: `/subpkg/profile/personal/health-profile?patientId=${patientId.value || ''}`
  })
}

// 获取验证码
const getVerificationCode = () => {
  if (!formData.phone) {
    uniShowToast({ title: '请先输入手机号', icon: 'none' })
    return
  }
  
  // 模拟发送验证码
  uniShowToast({ title: '验证码已发送', icon: 'success' })
  
  // 开始倒计时
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 提交表单
const submitForm = async () => {
  // 验证表单
  if (!formData.name) {
    uniShowToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  
  if (!formData.phone) {
    uniShowToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
    uniShowToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }

  //暂未接通短信验证码接口，暂时注释
  // if (!formData.verificationCode) {
  //   uniShowToast({ title: '请输入验证码', icon: 'none' })
  //   return
  // }
  
  // 开始加载
  loading.value = true
  
  try {
    // 调用修改就诊卡信息的API
    const requestData = {
      patientId: patientId.value,
      patientName: formData.name,
      idType: formData.idType,
      idNumber: formData.idNumber,
      gender: formData.gender,
      birthDate: formData.birthDate,
      nation: formData.nation,
      nationality: formData.nationality,
      region: formData.region,
      detailedAddress: formData.address,
      phone: formData.phone
    }
    
    console.log('提交修改请求:', requestData)
    
    const response = await patientApi.updateCard(requestData)
    console.log('修改成功响应:', response)
    
    // 关闭loading
    loading.value = false
    
    uniShowToast({ title: '修改成功', icon: 'success' })
    
    // 延迟返回上一页
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
    
  } catch (error) {
    // 关闭loading
    loading.value = false
    
    console.error('修改失败:', error)
    uniShowToast({ 
      title: error.message || error.msg || '修改失败，请重试', 
      icon: 'none',
      duration: 2000
    })
  }
}

// 初始化数据
onMounted(() => {
  // 从页面参数获取就诊卡信息
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}
  
  if (options.cardInfo) {
    try {
      const cardInfo = JSON.parse(decodeURIComponent(options.cardInfo))
      console.log('接收到的就诊卡信息:', cardInfo)
      
      // 填充表单数据
      patientId.value = cardInfo.patientId
      formData.name = cardInfo.patientName || ''
      formData.idType = cardInfo.idType || '身份证'
      formData.idNumber = cardInfo.idCard || ''
      formData.gender = cardInfo.gender || '女'
      formData.birthDate = cardInfo.birthDate || '2004-09-29'
      formData.nation = cardInfo.nation || '汉族'
      formData.nationality = cardInfo.nationality || '中国'
      formData.region = cardInfo.region || '北京市北京市海淀区'
      formData.address = cardInfo.detailedAddress || '北京交通大学'
      formData.phone = cardInfo.phone || ''
      
      // 设置选择器索引
      idTypeIndex.value = idTypeOptions.indexOf(formData.idType)
      genderIndex.value = genderOptions.indexOf(formData.gender)
      nationIndex.value = nationOptions.indexOf(formData.nation)
      nationalityIndex.value = nationalityOptions.indexOf(formData.nationality)
      
    } catch (error) {
      console.error('解析就诊卡信息失败:', error)
    }
  }
})
</script>

<style scoped>
.page-bg {
  min-height: 100vh;
  background: linear-gradient(180deg, #e7f3ff 0%, #f7faff 45%, #ffffff 100%);
  padding: 24rpx;
  box-sizing: border-box;
}

.hero-card {
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  border-radius: 24rpx;
  padding: 28rpx;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 10rpx 30rpx rgba(58, 156, 255, 0.28);
  margin-bottom: 24rpx;
}
.hero-left {
  flex: 1;
}
.hero-name {
  font-size: 36rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
}
.hero-meta {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 12rpx;
  font-size: 26rpx;
  opacity: 0.92;
}
.hero-tag {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.18);
}
.hero-dot {
  opacity: 0.7;
}
.hero-id {
  margin-top: 10rpx;
  font-size: 24rpx;
  opacity: 0.86;
}
.hero-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12rpx;
  flex-shrink: 0;
  margin-left: 24rpx;
}
.hero-pill {
  background: rgba(255, 255, 255, 0.15);
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  padding: 10rpx 16rpx;
  border-radius: 18rpx;
  min-width: 220rpx;
}
.pill-label {
  font-size: 24rpx;
  opacity: 0.8;
}
.pill-value {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  margin-top: 6rpx;
}
.pill-btn {
  background: #fff;
  color: #3a9cff;
  border-radius: 18rpx;
  padding: 10rpx 18rpx;
  font-size: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.card {
  background: #fff;
  border-radius: 18rpx;
  padding: 12rpx 24rpx 12rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 6rpx 18rpx rgba(58, 156, 255, 0.08);
}
.card-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1a1a;
  padding: 18rpx 0 8rpx;
}

.form-container {
  padding: 0 4rpx 12rpx;
}
.form-item {
  display: flex;
  align-items: center;
  padding: 22rpx 0;
  border-bottom: 1px solid #f0f2f5;
}
.form-item:last-child {
  border-bottom: none;
}
.form-label {
  width: 180rpx;
  font-size: 28rpx;
  color: #2f3b52;
  flex-shrink: 0;
  font-weight: 600;
}
.form-input {
  flex: 1;
  font-size: 28rpx;
  color: #2f3b52;
  padding: 10rpx 0;
  border: none;
  outline: none;
}
.form-picker {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 28rpx;
  color: #2f3b52;
}
.picker-arrow {
  color: #9aa7c0;
  font-size: 28rpx;
  margin-left: 12rpx;
}

.verification-group {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.verification-input {
  flex: 1;
}
.verification-btn {
  padding: 16rpx 24rpx;
  background: linear-gradient(135deg, #3a9cff 0%, #6ec6ff 100%);
  color: #fff;
  border: none;
  border-radius: 12rpx;
  font-size: 24rpx;
  white-space: nowrap;
  box-shadow: 0 4rpx 12rpx rgba(58, 156, 255, 0.25);
}
.verification-btn:disabled {
  background: #c8d7f2;
  box-shadow: none;
}

.archive-row {
  align-items: center;
  justify-content: space-between;
}
.health-archive {
  display: flex;
  align-items: center;
  gap: 14rpx;
}
.archive-icon {
  width: 56rpx;
  height: 56rpx;
  background: rgba(58, 156, 255, 0.12);
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
}
.archive-info {
  display: flex;
  flex-direction: column;
}
.archive-label {
  font-size: 28rpx;
  color: #2f3b52;
  font-weight: 600;
}
.archive-optional {
  font-size: 24rpx;
  color: #7a8aa0;
  margin-top: 4rpx;
}

.submit-btn {
  width: calc(100% - 48rpx);
  margin: 12rpx 24rpx 32rpx;
  height: 92rpx;
  background: linear-gradient(135deg, #3a9cff 0%, #57b7ff 50%, #6ec6ff 100%);
  color: #fff;
  border: none;
  border-radius: 46rpx;
  font-size: 32rpx;
  font-weight: 700;
  box-shadow: 0 10rpx 28rpx rgba(58, 156, 255, 0.32);
}
.submit-btn:active {
  transform: scale(0.98);
}
</style>
