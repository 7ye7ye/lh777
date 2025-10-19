<template>
  <view class="page-bg">
    <!-- 创建就诊卡卡片 -->
    <view class="medical-card">
      <view class="card-header">
        <view class="hospital-logo">🏥</view>
        <view class="hospital-name">创建就诊卡</view>
      </view>
      
      <view class="card-content">
        <view class="form-container">
          <!-- 基本信息 -->
          <view class="form-section">
            <view class="section-title">基本信息</view>
            
            <view class="form-item">
              <text class="form-label">姓名 <text class="required">*</text></text>
              <input 
                v-model="formData.patientName" 
                class="form-input" 
                placeholder="请输入真实姓名"
                maxlength="20"
              />
            </view>
            
            <view class="form-item">
              <text class="form-label">证件类型 <text class="required">*</text></text>
              <picker 
                :value="idTypeIndex" 
                :range="idTypeOptions" 
                @change="onIdTypeChange"
                class="form-picker"
              >
                <view class="picker-text">{{ formData.idType || '请选择证件类型' }}</view>
              </picker>
            </view>
            
            <view class="form-item">
              <text class="form-label">证件号码 <text class="required">*</text></text>
              <input 
                v-model="formData.idCard" 
                class="form-input" 
                placeholder="请输入证件号码"
                maxlength="18"
              />
            </view>
            
            <view class="form-item">
              <text class="form-label">性别 <text class="required">*</text></text>
              <picker 
                :value="genderIndex" 
                :range="genderOptions" 
                @change="onGenderChange"
                class="form-picker"
              >
                <view class="picker-text">{{ formData.gender || '请选择性别' }}</view>
              </picker>
            </view>
            
            <view class="form-item">
              <text class="form-label">出生日期 <text class="required">*</text></text>
              <picker 
                mode="date" 
                :value="formData.birthDate" 
                @change="onBirthDateChange"
                class="form-picker"
              >
                <view class="picker-text">{{ formData.birthDate || '请选择出生日期' }}</view>
              </picker>
            </view>
            
            <view class="form-item">
              <text class="form-label">民族 <text class="required">*</text></text>
              <picker 
                :value="nationIndex" 
                :range="nationOptions" 
                @change="onNationChange"
                class="form-picker"
              >
                <view class="picker-text">{{ formData.nation || '请选择民族' }}</view>
              </picker>
            </view>
            
            <view class="form-item">
              <text class="form-label">国籍</text>
              <picker 
                :value="nationalityIndex" 
                :range="nationalityOptions" 
                @change="onNationalityChange"
                class="form-picker"
              >
                <view class="picker-text">{{ formData.nationality || '请选择国籍' }}</view>
              </picker>
            </view>
          </view>
          
          <!-- 联系信息 -->
          <view class="form-section">
            <view class="section-title">联系信息</view>
            
            <view class="form-item">
              <text class="form-label">所在地区 <text class="required">*</text></text>
              <input 
                v-model="formData.region" 
                class="form-input" 
                placeholder="请输入所在地区"
                maxlength="50"
              />
            </view>
            
            <view class="form-item">
              <text class="form-label">详细住址 <text class="required">*</text></text>
              <input 
                v-model="formData.detailedAddress" 
                class="form-input" 
                placeholder="请输入详细住址"
                maxlength="100"
              />
            </view>
            
            <view class="form-item">
              <text class="form-label">手机号 <text class="required">*</text></text>
              <input 
                v-model="formData.phone" 
                class="form-input" 
                placeholder="请输入手机号"
                type="number"
                maxlength="11"
              />
            </view>
            
    
          </view>
          
          <!-- 身份信息 -->
          <view class="form-section">
            <view class="section-title">身份信息</view>
            
            <view class="form-item">
              <text class="form-label">患者身份 <text class="required">*</text></text>
              <picker 
                :value="patientTypeIndex" 
                :range="patientTypeOptions" 
                @change="onPatientTypeChange"
                class="form-picker"
              >
                <view class="picker-text">{{ formData.patientTypeText || '请选择身份' }}</view>
              </picker>
            </view>
            
            <view class="form-item" v-if="formData.patientType === 1">
              <text class="form-label">学号 <text class="required">*</text></text>
              <input 
                v-model="formData.studentId" 
                class="form-input" 
                placeholder="请输入学号"
                maxlength="20"
              />
            </view>
            
            <view class="form-item" v-if="formData.patientType === 2 || formData.patientType === 3">
              <text class="form-label">工号 <text class="required">*</text></text>
              <input 
                v-model="formData.staffId" 
                class="form-input" 
                placeholder="请输入工号"
                maxlength="20"
              />
            </view>
          </view>
          
          <!-- 紧急联系人 -->
          <view class="form-section">
            <view class="section-title">紧急联系人</view>
            
            <view class="form-item">
              <text class="form-label">紧急联系人姓名</text>
              <input 
                v-model="formData.emergencyContact" 
                class="form-input" 
                placeholder="请输入紧急联系人姓名"
                maxlength="20"
              />
            </view>
            
            <view class="form-item">
              <text class="form-label">紧急联系人电话</text>
              <input 
                v-model="formData.emergencyPhone" 
                class="form-input" 
                placeholder="请输入紧急联系人电话"
                maxlength="20"
              />
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="action-btn create-btn" @click="handleCreate" :disabled="loading">
        {{ loading ? '创建中...' : '立即创建' }}
      </button>
      <button class="action-btn cancel-btn" @click="goBack">取消</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { patientApi } from '@/api/patient'
import { useUserStore } from '@/store/user'
import { uniShowToast, uniNavigateBack } from '@/utils/uniHelper'

const userStore = useUserStore()
const loading = ref(false)

// 表单数据
const formData = reactive({
  patientName: '',
  idType: '',
  idCard: '',
  gender: '',
  birthDate: '',
  nation: '',
  nationality: '中国',
  region: '',
  detailedAddress: '',
  phone: '',
  patientType: null,
  patientTypeText: '',
  studentId: '',
  staffId: '',
  emergencyContact: '',
  emergencyPhone: ''
})

// 选择器选项
const idTypeOptions = ['身份证', '护照', '港澳通行证', '台胞证', '其他']
const genderOptions = ['男', '女', '未知']
const nationOptions = ['汉族', '蒙古族', '回族', '藏族', '维吾尔族', '苗族', '彝族', '壮族', '布依族', '朝鲜族', '满族', '侗族', '瑶族', '白族', '土家族', '哈尼族', '哈萨克族', '傣族', '黎族', '傈僳族', '佤族', '畲族', '高山族', '拉祜族', '水族', '东乡族', '纳西族', '景颇族', '柯尔克孜族', '土族', '达斡尔族', '仫佬族', '羌族', '布朗族', '撒拉族', '毛南族', '仡佬族', '锡伯族', '阿昌族', '普米族', '塔吉克族', '怒族', '乌孜别克族', '俄罗斯族', '鄂温克族', '德昂族', '保安族', '裕固族', '京族', '塔塔尔族', '独龙族', '鄂伦春族', '赫哲族', '门巴族', '珞巴族', '基诺族', '其他']
const nationalityOptions = ['中国', '美国', '英国', '法国', '德国', '日本', '韩国', '俄罗斯', '加拿大', '澳大利亚', '其他']
const patientTypeOptions = ['学生', '教师', '职工']

// 选择器索引
const idTypeIndex = ref(0)
const genderIndex = ref(0)
const nationIndex = ref(0)
const nationalityIndex = ref(0)
const patientTypeIndex = ref(0)

// 选择器事件
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

const onPatientTypeChange = (e) => {
  patientTypeIndex.value = e.detail.value
  formData.patientType = parseInt(e.detail.value) + 1 // 1-学生；2-教师；3-职工
  formData.patientTypeText = patientTypeOptions[e.detail.value]
}

// 验证表单
const validateForm = () => {
  if (!formData.patientName || !formData.patientName.trim()) {
    uniShowToast({ title: '请输入姓名', icon: 'none' })
    return false
  }
  
  if (!formData.idType) {
    uniShowToast({ title: '请选择证件类型', icon: 'none' })
    return false
  }
  
  if (!formData.idCard || !formData.idCard.trim()) {
    uniShowToast({ title: '请输入证件号码', icon: 'none' })
    return false
  }
  
  if (!formData.gender) {
    uniShowToast({ title: '请选择性别', icon: 'none' })
    return false
  }
  
  if (!formData.birthDate) {
    uniShowToast({ title: '请选择出生日期', icon: 'none' })
    return false
  }
  
  if (!formData.nation) {
    uniShowToast({ title: '请选择民族', icon: 'none' })
    return false
  }
  
  if (!formData.region || !formData.region.trim()) {
    uniShowToast({ title: '请输入所在地区', icon: 'none' })
    return false
  }
  
  if (!formData.detailedAddress || !formData.detailedAddress.trim()) {
    uniShowToast({ title: '请输入详细住址', icon: 'none' })
    return false
  }
  
  if (!formData.phone || !formData.phone.trim()) {
    uniShowToast({ title: '请输入手机号', icon: 'none' })
    return false
  }
  
  if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
    uniShowToast({ title: '请输入正确的手机号', icon: 'none' })
    return false
  }
  
  if (!formData.patientType) {
    uniShowToast({ title: '请选择患者身份', icon: 'none' })
    return false
  }
  
  if (formData.patientType === 1 && (!formData.studentId || !formData.studentId.trim())) {
    uniShowToast({ title: '请输入学号', icon: 'none' })
    return false
  }
  
  if ((formData.patientType === 2 || formData.patientType === 3) && (!formData.staffId || !formData.staffId.trim())) {
    uniShowToast({ title: '请输入工号', icon: 'none' })
    return false
  }
  
  return true
}

// 创建就诊卡
const handleCreate = async () => {
  if (!validateForm()) return
  
  loading.value = true
  try {
    console.log("创建就诊卡传入的用户ID：",userStore.userInfo.userId)
    // 构建请求数据，字段名与后端实体类对齐
    const requestData = {
      userId: userStore.userInfo.userId, // 传入已登录的userID
      patientName: formData.patientName,
      idType: formData.idType,
      idCard: formData.idCard,
      gender: formData.gender,
      birthDate: formData.birthDate,
      nation: formData.nation,
      nationality: formData.nationality,
      region: formData.region,
      detailedAddress: formData.detailedAddress,
      phone: formData.phone,
      phoneNumber: formData.phoneNumber,
      patientType: formData.patientType,
      studentId: formData.studentId,
      staffId: formData.staffId,
      emergencyContact: formData.emergencyContact,
      emergencyPhone: formData.emergencyPhone
    }
    
    const response = await patientApi.createCard(requestData)
    
    if (response.code === 200) {
      uniShowToast({ title: '就诊卡创建成功' })
      // 延迟一下再返回，让用户看到成功提示
      setTimeout(() => {
        uniNavigateBack()
      }, 1500)
    } else {
      throw new Error(response.message || '创建就诊卡失败')
    }
  } catch (error) {
    console.error('创建就诊卡失败:', error)
    uniShowToast({ 
      title: error.message || '创建失败', 
      icon: 'none' 
    })
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  uniNavigateBack()
}
</script>

<style scoped>
.page-bg { 
  min-height: 100vh; 
  background: #f8faff; 
  padding: 24rpx;
}

/* 就诊卡样式 */
.medical-card {
  background: linear-gradient(135deg, #3a9cff 0%, #5db7ff 100%);
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(58, 156, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.medical-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  pointer-events: none;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  position: relative;
  z-index: 1;
}

.hospital-logo {
  font-size: 48rpx;
  margin-right: 16rpx;
}

.hospital-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.card-content {
  position: relative;
  z-index: 1;
}

.form-container {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16rpx;
  padding: 24rpx;
  backdrop-filter: blur(10rpx);
}

.form-section {
  margin-bottom: 32rpx;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 20rpx;
  padding-bottom: 8rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.2);
}

.form-item {
  margin-bottom: 24rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 12rpx;
  font-weight: 500;
}

.required {
  color: #ff6b6b;
  font-weight: bold;
}

.form-input {
  width: 100%;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #fff;
  box-sizing: border-box;
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.form-input:focus {
  border-color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.25);
}

.form-picker {
  width: 100%;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  box-sizing: border-box;
}

.picker-text {
  font-size: 28rpx;
  color: #fff;
  flex: 1;
}

.picker-text:empty::before {
  content: attr(placeholder);
  color: rgba(255, 255, 255, 0.6);
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.action-btn {
  width: 100%;
  height: 88rpx;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 500;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.create-btn {
  background: #3a9cff;
  color: #fff;
}

.create-btn:active {
  background: #2980e6;
  transform: scale(0.98);
}

.create-btn:disabled {
  background: #ccc;
  color: #999;
  transform: none;
}

.cancel-btn {
  background: #fff;
  color: #666;
  border: 1rpx solid #ddd;
}

.cancel-btn:active {
  background: #f5f5f5;
  transform: scale(0.98);
}
</style>
