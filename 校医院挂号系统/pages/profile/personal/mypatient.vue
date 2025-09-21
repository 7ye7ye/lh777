<template>
  <view class="page-bg">
    <view class="patient-header">
      <text class="header-title">我的就诊人</text>
      <button class="add-btn" @click="showAddForm = true">+ 添加就诊人</button>
    </view>

    <!-- 就诊人列表 -->
    <view class="patient-list">
      <view class="patient-item" v-for="patient in patients" :key="patient.id">
        <view class="patient-info">
          <view class="patient-basic">
            <text class="patient-name">{{ patient.name }}</text>
            <text class="patient-gender">{{ patient.gender }}</text>
            <text class="patient-age">{{ patient.age }}岁</text>
          </view>
          <view class="patient-detail">
            <text class="patient-id">{{ patient.idType }}：{{ patient.idNumber }}</text>
            <text class="patient-phone">手机：{{ patient.phone }}</text>
          </view>
        </view>
        <view class="patient-actions">
          <button class="action-btn edit" @click="editPatient(patient)">编辑</button>
          <button class="action-btn delete" @click="deletePatient(patient.id)">删除</button>
        </view>
      </view>
    </view>

    <!-- 添加/编辑表单 -->
    <view class="form-modal" v-if="showAddForm">
      <view class="form-content">
        <view class="form-header">
          <text class="form-title">{{ isEdit ? '编辑就诊人' : '添加就诊人' }}</text>
          <text class="close-btn" @click="closeForm">×</text>
        </view>
        
        <view class="form-tip">实名制就诊，请如实填写就诊信息</view>
        
        <view class="form-body">
          <view class="form-item">
            <text class="form-label">姓名*</text>
            <input class="form-input" placeholder="请填写就诊人姓名" v-model="formData.name" />
          </view>
          <view class="form-item">
            <text class="form-label">证件类型*</text>
            <picker :range="idTypes" @change="onTypeChange">
              <view class="form-input">{{ formData.idType }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">证件号码*</text>
            <input class="form-input" placeholder="请填写证件号码" v-model="formData.idNumber" />
          </view>
          <view class="form-item">
            <text class="form-label">性别*</text>
            <picker :range="genders" @change="onGenderChange">
              <view class="form-input">{{ formData.gender }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">出生日期*</text>
            <picker mode="date" @change="onDateChange">
              <view class="form-input">{{ formData.birthday }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">手机号*</text>
            <input class="form-input" placeholder="请填写手机号" v-model="formData.phone" />
          </view>
        </view>
        
        <view class="form-actions">
          <button class="form-btn cancel" @click="closeForm">取消</button>
          <button class="form-btn submit" @click="submitForm">提交</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'

const patients = ref([
  { 
    id: 1, 
    name: '张三', 
    gender: '男', 
    age: 35, 
    idType: '身份证', 
    idNumber: '110101199001011234', 
    phone: '138****5678',
    birthday: '1990-01-01'
  },
  { 
    id: 2, 
    name: '李四', 
    gender: '女', 
    age: 28, 
    idType: '身份证', 
    idNumber: '110101199501011234', 
    phone: '139****5678',
    birthday: '1995-01-01'
  }
])

const showAddForm = ref(false)
const isEdit = ref(false)
const idTypes = ['身份证', '护照']
const genders = ['男', '女']

const formData = ref({
  name: '',
  idType: '身份证',
  idNumber: '',
  gender: '男',
  birthday: '请选择',
  phone: ''
})

const onTypeChange = (e) => { 
  formData.value.idType = idTypes[e.detail.value] 
}

const onGenderChange = (e) => { 
  formData.value.gender = genders[e.detail.value] 
}

const onDateChange = (e) => { 
  formData.value.birthday = e.detail.value 
}

const editPatient = (patient) => {
  isEdit.value = true
  formData.value = { ...patient }
  showAddForm.value = true
}

const deletePatient = (id) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个就诊人吗？',
    success: (res) => {
      if (res.confirm) {
        patients.value = patients.value.filter(p => p.id !== id)
        uni.showToast({ title: '删除成功', icon: 'success' })
      }
    }
  })
}

const closeForm = () => {
  showAddForm.value = false
  isEdit.value = false
  resetForm()
}

const resetForm = () => {
  formData.value = {
    name: '',
    idType: '身份证',
    idNumber: '',
    gender: '男',
    birthday: '请选择',
    phone: ''
  }
}

const submitForm = () => {
  if (!formData.value.name || !formData.value.idNumber || !formData.value.phone) {
    uni.showToast({ title: '请填写完整信息', icon: 'error' })
    return
  }
  
  if (isEdit.value) {
    // 编辑模式
    const index = patients.value.findIndex(p => p.id === formData.value.id)
    if (index !== -1) {
      patients.value[index] = { ...formData.value }
    }
    uni.showToast({ title: '编辑成功', icon: 'success' })
  } else {
    // 添加模式
    const newPatient = {
      ...formData.value,
      id: Date.now(),
      age: new Date().getFullYear() - new Date(formData.value.birthday).getFullYear()
    }
    patients.value.push(newPatient)
    uni.showToast({ title: '添加成功', icon: 'success' })
  }
  
  closeForm()
}

const getPatientList = () => {
  userApi.getPatientList().then(res => {
    // patients.value = res.data
  }).catch(() => {
    // 使用默认数据
  })
}

onMounted(() => {
  getPatientList()
})
</script>

<style scoped>
.page-bg { 
  min-height: 100vh; 
  background: #f8faff; 
}

.patient-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
}

.add-btn {
  background: #3a9cff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 12rpx 24rpx;
  font-size: 26rpx;
}

.patient-list {
  padding: 0 24rpx;
}

.patient-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.patient-info {
  margin-bottom: 16rpx;
}

.patient-basic {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.patient-name {
  font-size: 30rpx;
  font-weight: bold;
  margin-right: 16rpx;
}

.patient-gender, .patient-age {
  font-size: 26rpx;
  color: #666;
  margin-right: 16rpx;
}

.patient-detail {
  display: flex;
  flex-direction: column;
}

.patient-id, .patient-phone {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 4rpx;
}

.patient-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  padding: 12rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
  border: none;
}

.action-btn.edit {
  background: #3a9cff;
  color: #fff;
}

.action-btn.delete {
  background: #ff4d4f;
  color: #fff;
}

.form-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.form-content {
  background: #fff;
  border-radius: 16rpx;
  margin: 32rpx;
  max-height: 80vh;
  overflow-y: auto;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  border-bottom: 1px solid #f0f0f0;
}

.form-title {
  font-size: 32rpx;
  font-weight: bold;
}

.close-btn {
  font-size: 40rpx;
  color: #999;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-tip { 
  color: #e69a2a; 
  font-size: 26rpx; 
  margin: 16rpx 32rpx; 
}

.form-body {
  padding: 32rpx;
}

.form-item { 
  display: flex; 
  align-items: center; 
  margin-bottom: 24rpx; 
}

.form-label { 
  width: 180rpx; 
  font-size: 28rpx; 
}

.form-input { 
  flex: 1; 
  border-bottom: 1px solid #eee; 
  font-size: 28rpx; 
  color: #333; 
  padding: 8rpx 0; 
}

.form-actions {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  border-top: 1px solid #f0f0f0;
}

.form-btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

.form-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.form-btn.submit {
  background: #3a9cff;
  color: #fff;
}
</style>