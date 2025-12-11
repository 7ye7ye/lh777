<template>
  <view class="page-bg">
    <!-- 顶部插图背景 -->
    <view class="header-illustration">
      <image 
        class="illustration-bg-image"
        src="../../../static/images/health-bg.png"
        mode="scaleToFill"
      />
      <view class="header-title">— 关爱健康 · 感恩生活 —</view>
    </view>

    <!-- 基本信息卡片 -->
    <view class="info-card">
      <view class="form-row">
        <text class="form-label">身高(cm)</text>
        <input 
          v-model="formData.height" 
          class="form-input" 
          placeholder="请填写身高"
          type="number"
        />
      </view>

      <view class="form-row">
        <text class="form-label">体重(kg)</text>
        <input 
          v-model="formData.weight" 
          class="form-input" 
          placeholder="请填写体重"
          type="number"
        />
      </view>

      <view class="form-row">
        <text class="form-label">血型</text>
        <picker 
          :value="bloodTypeIndex" 
          :range="bloodTypeOptions" 
          @change="onBloodTypeChange"
        >
          <view class="picker-input">
            {{ formData.bloodType || '请选择' }}
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-row">
        <text class="form-label">婚姻状况</text>
        <picker 
          :value="maritalStatusIndex" 
          :range="maritalStatusOptions" 
          @change="onMaritalStatusChange"
        >
          <view class="picker-input">
            {{ formData.maritalStatus || '未婚' }}
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-row">
        <text class="form-label">生育情况</text>
        <picker 
          :value="fertilityStatusIndex" 
          :range="fertilityStatusOptions" 
          @change="onFertilityStatusChange"
        >
          <view class="picker-input">
            {{ formData.fertilityStatus || '请选择' }}
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 健康信息卡片 -->
    <view class="info-card">
      <view class="textarea-row">
        <text class="form-label">现病史</text>
        <textarea 
          v-model="formData.currentIllness" 
          class="form-textarea" 
          placeholder="您目前的疾病信息"
          maxlength="200"
        />
      </view>

      <view class="textarea-row">
        <text class="form-label">既往史</text>
        <textarea 
          v-model="formData.pastHistory" 
          class="form-textarea" 
          placeholder="您历史的疾病信息"
          maxlength="200"
        />
      </view>

      <view class="textarea-row">
        <text class="form-label">家族史</text>
        <textarea 
          v-model="formData.familyHistory" 
          class="form-textarea" 
          placeholder="您的家族遗传疾病信息"
          maxlength="200"
        />
      </view>

      <view class="textarea-row">
        <text class="form-label">过敏史</text>
        <textarea 
          v-model="formData.allergyHistory" 
          class="form-textarea" 
          placeholder="请填写过敏史，如无可填“无”"
          maxlength="200"
        />
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="button-container">
      <button class="save-btn" @click="handleSave" :loading="saving">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { patientApi } from '@/api/patient'
import { useUserStore } from '@/store/user'
import { uniShowToast, uniNavigateBack } from '@/utils/uniHelper'

const userStore = useUserStore()
const saving = ref(false)
const patientId = ref(null)

// 表单数据
const formData = reactive({
  height: '',
  weight: '',
  bloodType: '',
  maritalStatus: '未婚',
  fertilityStatus: '',
  currentIllness: '',
  pastHistory: '',
  familyHistory: '',
  allergyHistory: '无'
})

// 选择器选项
const bloodTypeOptions = ['请选择', 'A型', 'B型', 'AB型', 'O型', 'RH阴性', '其他']
const maritalStatusOptions = ['未婚', '已婚', '离异', '丧偶']
const fertilityStatusOptions = ['请选择', '未育', '已育', '不详']

// 选择器索引
const bloodTypeIndex = ref(0)
const maritalStatusIndex = ref(0)
const fertilityStatusIndex = ref(0)

// 选择器事件
const onBloodTypeChange = (e) => {
  bloodTypeIndex.value = e.detail.value
  formData.bloodType = bloodTypeOptions[e.detail.value]
}

const onMaritalStatusChange = (e) => {
  maritalStatusIndex.value = e.detail.value
  formData.maritalStatus = maritalStatusOptions[e.detail.value]
}

const onFertilityStatusChange = (e) => {
  fertilityStatusIndex.value = e.detail.value
  formData.fertilityStatus = fertilityStatusOptions[e.detail.value]
}

// 获取健康档案数据
const getHealthProfile = async () => {
  try {
    if (!patientId.value) return
    
    const data = await patientApi.getHealthProfile({ patientId: patientId.value })
    console.log('健康档案数据:', data)
    
    if (data) {
      Object.assign(formData, data)
      
      // 设置选择器索引
      if (data.bloodType) {
        bloodTypeIndex.value = bloodTypeOptions.indexOf(data.bloodType)
      }
      if (data.maritalStatus) {
        maritalStatusIndex.value = maritalStatusOptions.indexOf(data.maritalStatus)
      }
      if (data.fertilityStatus) {
        fertilityStatusIndex.value = fertilityStatusOptions.indexOf(data.fertilityStatus)
      }
    }
  } catch (error) {
    console.error('获取健康档案失败:', error)
  }
}

// 保存健康档案
const handleSave = async () => {
  if (!patientId.value) {
    uniShowToast({ title: '缺少患者信息', icon: 'none' })
    return
  }

  saving.value = true
  try {
    const requestData = {
      patientId: patientId.value,
      ...formData
    }
    
    await patientApi.updateHealthProfile(requestData)
    uniShowToast({ title: '保存成功', icon: 'success' })
    
    setTimeout(() => {
      uniNavigateBack()
    }, 1500)
  } catch (error) {
    console.error('保存健康档案失败:', error)
    uniShowToast({ 
      title: error.message || '保存失败', 
      icon: 'none' 
    })
  } finally {
    saving.value = false
  }
}

// 页面加载
onMounted(() => {
  // 从页面参数获取 patientId
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}
  
  if (options.patientId) {
    patientId.value = parseInt(options.patientId)
    getHealthProfile()
  } else {
    // 如果没有 patientId，尝试从 userStore 获取
    const userId = userStore.userInfo?.userId
    if (userId) {
      // 这里可以调用 API 获取当前用户的默认患者信息
    }
  }
})
</script>

<style scoped>
.page-bg {
  min-height: 100vh;
  background: linear-gradient(180deg, #e8f4ff 0%, #f5f5f5 40%);
  padding-bottom: 40rpx;
}

/* 顶部插图 */
.header-illustration {
  position: relative;
  width: 100%;
  height: 400rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  padding: 40rpx 0 20rpx;
  overflow: hidden;
}

.illustration-bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.header-title {
  position: relative;
  z-index: 1;
  font-size: 30rpx;
  color: #666;
  letter-spacing: 2rpx;
  text-shadow: 0 2rpx 4rpx rgba(255, 255, 255, 0.8);
}

/* 信息卡片 */
.info-card {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

/* 表单行 */
.form-row {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1px solid #f0f0f0;
}

.form-row:last-child {
  border-bottom: none;
}

.form-label {
  width: 180rpx;
  font-size: 28rpx;
  color: #333;
  flex-shrink: 0;
}

.form-input {
  flex: 1;
  font-size: 28rpx;
  color: #666;
  text-align: right;
}

.picker-input {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 28rpx;
  color: #666;
}

.picker-arrow {
  margin-left: 8rpx;
  color: #999;
  font-size: 24rpx;
}

/* 多行文本区域 */
.textarea-row {
  padding: 24rpx 0;
  border-bottom: 1px solid #f0f0f0;
}

.textarea-row:last-child {
  border-bottom: none;
}

.form-textarea {
  width: 100%;
  min-height: 120rpx;
  font-size: 28rpx;
  color: #333;
  padding: 16rpx;
  background: #f8f8f8;
  border-radius: 8rpx;
  margin-top: 16rpx;
  box-sizing: border-box;
}

.allergy-value {
  font-size: 28rpx;
  color: #666;
  padding: 16rpx;
  background: #f8f8f8;
  border-radius: 8rpx;
  margin-top: 16rpx;
  text-align: center;
}

/* 按钮容器 */
.button-container {
  padding: 0 24rpx;
  margin-top: 40rpx;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #ffa726 0%, #ff9800 100%);
  color: #fff;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 152, 0, 0.4);
}

.save-btn:active {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  transform: scale(0.98);
}
</style>

