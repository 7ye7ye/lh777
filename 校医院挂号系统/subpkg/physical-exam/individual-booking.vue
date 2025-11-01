<template>
  <view class="booking-bg">
    <!-- 顶部提示 -->
    <view class="top-tips">
      <text class="tip-icon">💡</text>
      <text>请选择体检套餐和时间，完成预约</text>
    </view>

    <!-- 体检套餐选择 -->
    <view class="section card">
      <view class="section-title">
        <text>选择体检套餐</text>
        <text class="required">*</text>
      </view>
      <view class="packages-list">
        <view 
          v-for="pkg in packages" 
          :key="pkg.id"
          class="package-item"
          :class="{ active: selectedPackage === pkg.id }"
          @click="selectPackage(pkg.id)"
        >
          <view class="package-header">
            <view class="package-name">{{ pkg.name }}</view>
            <view class="package-price">¥{{ pkg.price }}</view>
          </view>
          <view class="package-desc">{{ pkg.desc }}</view>
          <view class="package-items">
            <text class="item-count">包含 {{ pkg.itemCount }} 项检查</text>
            <text class="detail-btn" @click.stop="showPackageDetail(pkg)">详情 ></text>
          </view>
          <view class="package-tag" v-if="pkg.tag">{{ pkg.tag }}</view>
        </view>
      </view>
    </view>

    <!-- 体检日期选择 -->
    <view class="section card">
      <view class="section-title">
        <text>选择体检日期</text>
        <text class="required">*</text>
      </view>
      <picker mode="date" :value="selectedDate" :start="minDateStr" :end="maxDateStr" @change="onDateChange">
        <view class="date-selector">
          <text :class="{ placeholder: !selectedDate }">
            {{ displayDate || '请选择体检日期' }}
          </text>
          <text class="arrow">></text>
        </view>
      </picker>
    </view>

    <!-- 体检时间段选择 -->
    <view class="section card" v-if="selectedDate">
      <view class="section-title">
        <text>选择时间段</text>
        <text class="required">*</text>
      </view>
      <view class="time-slots">
        <view 
          v-for="slot in timeSlots"
          :key="slot.id"
          class="time-slot"
          :class="{ 
            active: selectedTimeSlot === slot.id,
            disabled: slot.full 
          }"
          @click="selectTimeSlot(slot)"
        >
          <view class="slot-time">{{ slot.time }}</view>
          <view class="slot-info">
            <text v-if="!slot.full">剩余{{ slot.available }}人</text>
            <text v-else class="full-text">已满</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 体检人信息 -->
    <view class="section card">
      <view class="section-title">
        <text>体检人信息</text>
        <text class="required">*</text>
      </view>
      <view class="form-item">
        <text class="label">姓名</text>
        <input 
          class="input" 
          v-model="formData.name" 
          placeholder="请输入姓名"
          placeholder-class="input-placeholder"
        />
      </view>
      <view class="form-item">
        <text class="label">性别</text>
        <view class="radio-group">
          <view 
            class="radio-item"
            :class="{ active: formData.gender === '男' }"
            @click="formData.gender = '男'"
          >
            <text class="radio-icon">{{ formData.gender === '男' ? '☑' : '☐' }}</text>
            <text>男</text>
          </view>
          <view 
            class="radio-item"
            :class="{ active: formData.gender === '女' }"
            @click="formData.gender = '女'"
          >
            <text class="radio-icon">{{ formData.gender === '女' ? '☑' : '☐' }}</text>
            <text>女</text>
          </view>
        </view>
      </view>
      <view class="form-item">
        <text class="label">手机号</text>
        <input 
          class="input" 
          v-model="formData.phone" 
          type="number"
          placeholder="请输入手机号"
          placeholder-class="input-placeholder"
          maxlength="11"
        />
      </view>
      <view class="form-item">
        <text class="label">身份证号</text>
        <input 
          class="input" 
          v-model="formData.idCard" 
          placeholder="请输入身份证号"
          placeholder-class="input-placeholder"
          maxlength="18"
        />
      </view>
      <view class="form-item">
        <text class="label">备注</text>
        <textarea 
          class="textarea" 
          v-model="formData.remark" 
          placeholder="如有特殊情况请备注（选填）"
          placeholder-class="input-placeholder"
          maxlength="200"
        />
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <view class="price-info">
        <text class="label">合计：</text>
        <text class="price">¥{{ totalPrice }}</text>
      </view>
      <button class="submit-btn" @click="submitBooking">确认预约</button>
    </view>

    <view class="bottom-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 体检套餐列表
const packages = ref([
  {
    id: 1,
    name: '基础套餐',
    price: 280,
    desc: '适合学生、青年教职工',
    itemCount: 15,
    tag: '学生免费'
  },
  {
    id: 2,
    name: '教职工套餐',
    price: 480,
    desc: '适合在职教职工，包含职业病筛查',
    itemCount: 25,
    tag: '可报销'
  },
  {
    id: 3,
    name: '全面套餐',
    price: 880,
    desc: '适合50岁以上及有基础疾病者',
    itemCount: 35,
    tag: '深度筛查'
  },
  {
    id: 4,
    name: '高端套餐',
    price: 1580,
    desc: '全面深度检查，含肿瘤标志物',
    itemCount: 45,
    tag: 'VIP'
  }
])

// 时间段列表
const timeSlots = ref([
  { id: 1, time: '07:30-08:30', available: 15, full: false },
  { id: 2, time: '08:30-09:30', available: 8, full: false },
  { id: 3, time: '09:30-10:30', available: 3, full: false },
  { id: 4, time: '10:30-11:00', available: 0, full: true }
])

// 选中的套餐、日期、时间段
const selectedPackage = ref(null)
const selectedDate = ref('')
const selectedTimeSlot = ref(null)

// 日期范围（明天到30天后）
const today = new Date()
const minDate = new Date(today.getTime() + 24 * 60 * 60 * 1000) // 明天
const maxDate = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000) // 30天后

// 日期字符串格式（YYYY-MM-DD）
const minDateStr = computed(() => {
  const year = minDate.getFullYear()
  const month = String(minDate.getMonth() + 1).padStart(2, '0')
  const day = String(minDate.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

const maxDateStr = computed(() => {
  const year = maxDate.getFullYear()
  const month = String(maxDate.getMonth() + 1).padStart(2, '0')
  const day = String(maxDate.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

// 格式化显示日期（YYYY-MM-DD 周X）
const displayDate = computed(() => {
  if (!selectedDate.value) return ''
  const date = new Date(selectedDate.value)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
  return `${year}-${month}-${day} ${weekDay}`
})

// 表单数据
const formData = ref({
  name: '',
  gender: '男',
  phone: '',
  idCard: '',
  remark: ''
})

// 计算总价
const totalPrice = computed(() => {
  if (!selectedPackage.value) return 0
  const pkg = packages.value.find(p => p.id === selectedPackage.value)
  return pkg ? pkg.price : 0
})

// 选择套餐
const selectPackage = (id) => {
  selectedPackage.value = id
}

// 显示套餐详情
const showPackageDetail = (pkg) => {
  const items = pkg.itemCount === 15 ? 
    '血常规、尿常规、肝功能、肾功能、血糖、血脂、胸部X光、心电图、B超（肝胆脾胰肾）、身高体重、血压、视力、内科、外科、眼科' :
    pkg.itemCount === 25 ?
    '在基础套餐基础上增加：甲状腺功能、肿瘤标志物初筛、骨密度、肺功能、动脉硬化检测、幽门螺杆菌检测、妇科/泌尿系统检查、心理健康评估等' :
    pkg.itemCount === 35 ?
    '在教职工套餐基础上增加：CT检查、胃镜、肠镜、心脏超声、颈动脉超声、全身体成分分析、睡眠监测、营养评估等' :
    '全面深度检查套餐，包含所有常规项目及高端检查项目'

  uni.showModal({
    title: pkg.name,
    content: `价格：¥${pkg.price}\n\n包含项目：\n${items}`,
    showCancel: true,
    cancelText: '关闭',
    confirmText: '选择此套餐',
    success: (res) => {
      if (res.confirm) {
        selectPackage(pkg.id)
      }
    }
  })
}

// 日期选择器变化事件
const onDateChange = (e) => {
  const selectedDateObj = new Date(e.detail.value)
  
  // 检查是否为周日
  if (selectedDateObj.getDay() === 0) {
    uni.showToast({
      title: '周日不开展体检，请选择其他日期',
      icon: 'none'
    })
    return
  }
  
  selectedDate.value = e.detail.value
  selectedTimeSlot.value = null // 重置时间段选择
}

// 选择时间段
const selectTimeSlot = (slot) => {
  if (slot.full) {
    uni.showToast({ title: '该时间段已满', icon: 'none' })
    return
  }
  selectedTimeSlot.value = slot.id
}

// 提交预约
const submitBooking = () => {
  // 验证
  if (!selectedPackage.value) {
    uni.showToast({ title: '请选择体检套餐', icon: 'none' })
    return
  }
  if (!selectedDate.value) {
    uni.showToast({ title: '请选择体检日期', icon: 'none' })
    return
  }
  if (!selectedTimeSlot.value) {
    uni.showToast({ title: '请选择时间段', icon: 'none' })
    return
  }
  if (!formData.value.name) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  if (!formData.value.phone) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  if (!/^1[3-9]\d{9}$/.test(formData.value.phone)) {
    uni.showToast({ title: '手机号格式不正确', icon: 'none' })
    return
  }
  if (!formData.value.idCard) {
    uni.showToast({ title: '请输入身份证号', icon: 'none' })
    return
  }
  if (!/^\d{17}[\dXx]$/.test(formData.value.idCard)) {
    uni.showToast({ title: '身份证号格式不正确', icon: 'none' })
    return
  }

  // 显示确认对话框
  const pkg = packages.value.find(p => p.id === selectedPackage.value)
  const slot = timeSlots.value.find(s => s.id === selectedTimeSlot.value)
  
  uni.showModal({
    title: '确认预约信息',
    content: `套餐：${pkg.name}\n日期：${selectedDate.value}\n时间：${slot.time}\n姓名：${formData.value.name}\n金额：¥${totalPrice.value}`,
    success: (res) => {
      if (res.confirm) {
        // 这里应该调用后端API
        uni.showLoading({ title: '预约中...' })
        
        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({ 
            title: '预约成功！', 
            icon: 'success',
            duration: 2000
          })
          
          setTimeout(() => {
            uni.navigateBack()
          }, 2000)
        }, 1500)
      }
    }
  })
}
</script>

<style scoped>
.booking-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 200rpx;
}

.top-tips {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-size: 26rpx;
}

.tip-icon {
  font-size: 32rpx;
}

.card {
  background: #fff;
  border-radius: 20rpx;
  margin: 24rpx 24rpx 0;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.08);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
}

.required {
  color: #ff4757;
  margin-left: 8rpx;
}

.packages-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.package-item {
  border: 2rpx solid #e8e8e8;
  border-radius: 16rpx;
  padding: 24rpx;
  position: relative;
  transition: all 0.3s;
}

.package-item.active {
  border-color: #667eea;
  background: #f0f4ff;
}

.package-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.package-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.package-price {
  font-size: 36rpx;
  font-weight: bold;
  color: #667eea;
}

.package-desc {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
}

.package-items {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-count {
  font-size: 24rpx;
  color: #999;
}

.detail-btn {
  font-size: 24rpx;
  color: #667eea;
}

.package-tag {
  position: absolute;
  top: 0;
  right: 24rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 22rpx;
  padding: 8rpx 16rpx;
  border-radius: 0 16rpx 0 16rpx;
}

.date-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #f8faff;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
}

.placeholder {
  color: #999;
}

.arrow {
  color: #999;
}

.time-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.time-slot {
  width: calc((100% - 16rpx) / 2);
  padding: 24rpx;
  border: 2rpx solid #e8e8e8;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.time-slot.active {
  border-color: #667eea;
  background: #f0f4ff;
}

.time-slot.disabled {
  opacity: 0.5;
  background: #f5f5f5;
}

.slot-time {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.slot-info {
  font-size: 24rpx;
  color: #999;
}

.full-text {
  color: #ff4757;
}

.form-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 32rpx;
  gap: 24rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.label {
  width: 140rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 60rpx;
}

.input {
  flex: 1;
  padding: 16rpx 24rpx;
  background: #f8faff;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
  font-size: 28rpx;
}

.textarea {
  flex: 1;
  padding: 16rpx 24rpx;
  background: #f8faff;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
  font-size: 28rpx;
  min-height: 150rpx;
}

.input-placeholder {
  color: #ccc;
}

.radio-group {
  flex: 1;
  display: flex;
  gap: 32rpx;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
  color: #666;
}

.radio-item.active {
  color: #667eea;
}

.radio-icon {
  font-size: 32rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 24rpx 32rpx;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 999;
}

.price-info {
  display: flex;
  align-items: baseline;
}

.price-info .label {
  font-size: 28rpx;
  color: #666;
}

.price {
  font-size: 40rpx;
  font-weight: bold;
  color: #ff4757;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 40rpx;
  padding: 24rpx 64rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
}

.bottom-placeholder {
  height: 160rpx;
}
</style>

