<template>
  <view class="referral-bg">
    <view class="page-header">
      <text class="page-title">转诊申请</text>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 患者信息表单 -->
      <view class="form-section">
        <view class="section-title">患者信息</view>
        
        <view class="form-item">
          <view class="label">姓名</view>
          <input type="text" v-model="formData.patientName" placeholder="请输入患者姓名" class="input" />
        </view>
        
        <view class="form-item">
          <view class="label">性别</view>
          <radio-group @change="handleGenderChange">
            <label class="radio-item" :class="{ active: formData.gender === '男' }">
              <radio value="男" :checked="formData.gender === '男'" />男
            </label>
            <label class="radio-item" :class="{ active: formData.gender === '女' }">
              <radio value="女" :checked="formData.gender === '女'" />女
            </label>
          </radio-group>
        </view>
        
        <view class="form-item">
          <view class="label">年龄</view>
          <input type="number" v-model="formData.age" placeholder="请输入年龄" class="input" />
        </view>
        
        <view class="form-item">
          <view class="label">联系电话</view>
          <input type="number" v-model="formData.phone" placeholder="请输入联系电话" class="input" />
        </view>
      </view>

      <!-- 病情描述 -->
      <view class="form-section">
        <view class="section-title">病情描述</view>
        <view class="form-item">
          <view class="label">症状描述</view>
          <textarea 
            v-model="formData.symptoms" 
            placeholder="请详细描述您的症状和持续时间" 
            class="textarea"
            rows="4"
          ></textarea>
        </view>
        
        <view class="form-item">
          <view class="label">既往病史</view>
          <textarea 
            v-model="formData.medicalHistory" 
            placeholder="请填写您的既往病史、过敏史等" 
            class="textarea"
            rows="3"
          ></textarea>
        </view>
        
        <view class="form-item">
          <view class="label">转诊原因</view>
          <textarea 
            v-model="formData.reason" 
            placeholder="请说明需要转诊的原因" 
            class="textarea"
            rows="3"
          ></textarea>
        </view>
      </view>

      <!-- 目标医院/科室选择（根据转诊类型显示） -->
      <view class="form-section">
        <view class="section-title">{{ isInternalReferral ? '院内科室' : '目标医院' }}</view>
        
        <!-- 院内转诊：显示目标科室 -->
        <template v-if="isInternalReferral">
          <view class="form-item">
            <view class="label">目标科室</view>
            <view class="form-value">{{ formData.targetDepartment }}</view>
            <view class="change-tip" @click="changeReferralType">修改科室</view>
          </view>
          <view class="form-item">
            <view class="label">目标医院</view>
            <view class="form-value">本校医院</view>
          </view>
        </template>
        
        <!-- 院外转诊：显示医院和科室选择 -->
        <template v-else>
          <view class="form-item">
            <view class="label">选择医院</view>
            <picker @change="onHospitalChange" :value="hospitalIndex" :range="hospitals" range-key="name" class="picker" :disabled="loading">
              <view class="picker-text">{{ hospitals[hospitalIndex]?.name || '请选择目标医院' }}</view>
            </picker>
          </view>
          
          <view class="form-item">
            <view class="label">目标科室</view>
            <input type="text" v-model="formData.targetDepartment" placeholder="请输入目标科室" class="input" />
          </view>
        </template>
      </view>

      <!-- 上传资料 -->
      <view class="form-section">
        <view class="section-title">上传资料</view>
        <view class="upload-section">
          <view class="upload-tips">可上传检查报告、病历等相关资料（最多5张）</view>
          <view class="upload-list">
            <view 
              v-for="(item, index) in formData.attachments" 
              :key="index"
              class="upload-item"
            >
              <image :src="item.url" mode="aspectFill" class="upload-img"></image>
              <view class="delete-btn" @click="deleteAttachment(index)">×</view>
            </view>
            <view 
              v-if="formData.attachments.length < 5"
              class="upload-btn"
              @click="chooseImage"
            >
              <view class="upload-icon">+</view>
              <text class="upload-text">添加图片</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button class="submit-btn" @click="submitApplication" :disabled="loading">
          {{ loading ? '提交中...' : (isInternalReferral ? '提交院内转诊申请' : '提交院外转诊申请') }}
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { submitReferralApplication, getReferralOptions } from '../../api/referral'

// 表单数据
const formData = ref({
  patientName: '',
  gender: '男',
  age: '',
  phone: '',
  symptoms: '',
  medicalHistory: '',
  reason: '',
  targetHospital: '',
  targetDepartment: '',
  attachments: [],
  referralType: '', // 'internal' 或 'external'
  visitRecordId: '' // 关联的就诊记录ID
})

// 转诊信息
const referralInfo = ref({})
const selectedVisitRecord = ref({})

// 可选医院列表
const hospitals = ref([])
const hospitalIndex = ref(0)
const loading = ref(false)
const isInternalReferral = ref(false)

// 选择医院
const onHospitalChange = (e) => {
  hospitalIndex.value = e.detail.value
  formData.value.targetHospital = hospitals.value[hospitalIndex.value].name
}

// 获取当前用户信息（实际项目中可能从全局状态获取）
const getCurrentUserInfo = () => {
  // 模拟获取用户信息
  return {
    name: '当前用户',
    phone: '13800138000',
    gender: '男',
    age: '25'
  }
}

// 处理性别选择
const handleGenderChange = (e) => {
  formData.value.gender = e.detail.value
}

// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 5 - formData.value.attachments.length,
    success: (res) => {
      const tempFilePaths = res.tempFilePaths
      tempFilePaths.forEach(path => {
        formData.value.attachments.push({
          url: path,
          name: path.split('/').pop()
        })
      })
    }
  })
}

// 删除图片
const deleteAttachment = (index) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除该图片吗？',
    success: (res) => {
      if (res.confirm) {
        formData.value.attachments.splice(index, 1)
      }
    }
  })
}

// 返回修改转诊类型
const changeReferralType = () => {
  uni.navigateBack()
}

// 加载医院列表
const loadHospitals = async () => {
  try {
    loading.value = true
    const res = await getReferralOptions()
    if (res.code === 200 && res.data) {
      hospitals.value = res.data.hospitals || res.data
    }
  } catch (error) {
    console.error('加载医院列表失败:', error)
    uni.showToast({
      title: '加载医院列表失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 预填表单数据
const prefillFormData = () => {
  // 获取用户信息
  const userInfo = getCurrentUserInfo()
  
  // 预填患者信息
  formData.value.patientName = userInfo.name
  formData.value.gender = userInfo.gender
  formData.value.age = userInfo.age
  formData.value.phone = userInfo.phone
  
  // 如果有选择的就诊记录，预填相关信息
  if (selectedVisitRecord.value) {
    // 可以从就诊记录中提取症状、诊断等信息
    if (selectedVisitRecord.value.symptoms) {
      formData.value.symptoms = selectedVisitRecord.value.symptoms
    }
    if (selectedVisitRecord.value.diagnosis) {
      formData.value.medicalHistory = selectedVisitRecord.value.diagnosis
    }
    formData.value.visitRecordId = selectedVisitRecord.value.id || selectedVisitRecord.value.registrationNo
  }
  
  // 如果有转诊信息，设置目标科室或医院
  if (referralInfo.value) {
    formData.value.referralType = referralInfo.value.type
    isInternalReferral.value = referralInfo.value.type === 'internal'
    
    if (referralInfo.value.type === 'internal') {
      // 院内转诊：设置目标科室
      formData.value.targetDepartment = referralInfo.value.targetDepartment || ''
      formData.value.targetHospital = '本校医院' // 院内转诊的目标医院固定为本院
    }
  }
}

// 提交申请
const submitApplication = async () => {
  // 表单验证
  if (!formData.value.patientName) {
    uni.showToast({ title: '请输入患者姓名', icon: 'none' })
    return
  }
  if (!formData.value.age) {
    uni.showToast({ title: '请输入年龄', icon: 'none' })
    return
  }
  if (!formData.value.phone) {
    uni.showToast({ title: '请输入联系电话', icon: 'none' })
    return
  }
  if (!formData.value.symptoms) {
    uni.showToast({ title: '请描述症状', icon: 'none' })
    return
  }
  if (!formData.value.reason) {
    uni.showToast({ title: '请说明转诊原因', icon: 'none' })
    return
  }
  
  // 根据转诊类型进行不同的验证
  if (isInternalReferral.value) {
    // 院内转诊：验证目标科室
    if (!formData.value.targetDepartment) {
      uni.showToast({ title: '请选择目标科室', icon: 'none' })
      return
    }
  } else {
    // 院外转诊：验证目标医院
    if (!formData.value.targetHospital) {
      uni.showToast({ title: '请选择目标医院', icon: 'none' })
      return
    }
    if (!formData.value.targetDepartment) {
      uni.showToast({ title: '请输入目标科室', icon: 'none' })
      return
    }
  }
  
  try {
    loading.value = true
    
    // 准备提交数据
    const submitData = {
      ...formData.value,
      referralType: formData.value.referralType || (isInternalReferral.value ? 'internal' : 'external')
    }
    
    const res = await submitReferralApplication(submitData)
    if (res.code === 200) {
      uni.showToast({
        title: '提交成功',
        icon: 'success',
        duration: 2000,
        success: () => {
          // 重置表单
          formData.value = {
            patientName: '',
            gender: '男',
            age: '',
            phone: '',
            symptoms: '',
            medicalHistory: '',
            reason: '',
            targetHospital: '',
            targetDepartment: '',
            attachments: []
          }
          hospitalIndex.value = 0
          // 跳转到转诊记录页面
          setTimeout(() => {
            uni.navigateTo({
              url: '/subpkg/hospital/referral-records'
            })
          }, 2000)
        }
      })
    } else {
      uni.showToast({
        title: res.message || '提交失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('提交转诊申请失败:', error)
    uni.showToast({
      title: '提交失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 页面加载时初始化数据
onMounted(() => {
  // 获取转诊信息
  const info = uni.getStorageSync('referralInfo')
  if (info) {
    referralInfo.value = info
    selectedVisitRecord.value = info.visitRecord || {}
  }
  
  // 预填表单数据
  prefillFormData()
  
  // 加载医院列表（仅在院外转诊时需要）
  if (!isInternalReferral.value) {
    loadHospitals()
  }
})
</script>

<style scoped>
.referral-bg {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 20px;
}

.page-header {
  background-color: #1989fa;
  color: #fff;
  padding: 16px;
  text-align: center;
  position: sticky;
  top: 0;
  z-index: 10;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.content {
  padding: 16px;
  height: calc(100vh - 120px);
}

.form-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
  position: relative;
  padding-left: 10px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background-color: #1989fa;
  border-radius: 2px;
}

.form-item {
  margin-bottom: 16px;
}

.label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  display: block;
}

.input {
  width: 100%;
  height: 40px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 0 12px;
  font-size: 14px;
  box-sizing: border-box;
}

.textarea {
  width: 100%;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 12px;
  font-size: 14px;
  box-sizing: border-box;
  min-height: 100px;
}

.radio-group {
  display: flex;
  gap: 20px;
}

.radio-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
}

.radio-item.active {
  color: #1989fa;
  border-color: #1989fa;
}

.picker {
  width: 100%;
  height: 40px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
}

.picker-text {
  font-size: 14px;
  color: #333;
}

.upload-section {
  margin-top: 8px;
}

.upload-tips {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.upload-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.upload-item {
  position: relative;
  width: 80px;
  height: 80px;
}

.upload-img {
  width: 100%;
  height: 100%;
  border-radius: 4px;
}

.delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 20px;
  height: 20px;
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
}

.upload-btn {
  width: 80px;
  height: 80px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
}

.upload-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.upload-text {
  font-size: 12px;
}

.submit-section {
  padding: 20px 0;
}

.submit-btn {
  width: 100%;
  height: 44px;
  background-color: #1989fa;
  color: #fff;
  border: none;
  border-radius: 22px;
  font-size: 16px;
  font-weight: bold;
}

.submit-btn:active {
  background-color: #0e77d0;
}
</style>