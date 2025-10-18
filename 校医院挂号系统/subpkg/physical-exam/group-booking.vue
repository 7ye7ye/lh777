<template>
  <view class="group-bg">
    <!-- 顶部说明 -->
    <view class="top-banner">
      <view class="banner-icon">👥</view>
      <view class="banner-content">
        <text class="banner-title">团体体检预约</text>
        <text class="banner-desc">适用于10人以上的团体预约，享受优惠价格</text>
      </view>
    </view>

    <!-- 团检信息 -->
    <view class="section card">
      <view class="section-title">
        <text>团检信息</text>
        <text class="required">*</text>
      </view>
      <view class="form-item">
        <text class="label">单位名称</text>
        <input 
          class="input" 
          v-model="formData.unitName" 
          placeholder="请输入单位名称"
          placeholder-class="input-placeholder"
        />
      </view>
      <view class="form-item">
        <text class="label">体检人数</text>
        <input 
          class="input" 
          v-model="formData.peopleCount" 
          type="number"
          placeholder="请输入体检人数（最少10人）"
          placeholder-class="input-placeholder"
        />
      </view>
      <view class="form-item">
        <text class="label">期望日期</text>
        <view class="date-picker" @click="showDatePicker">
          <text :class="{ placeholder: !formData.expectDate }">
            {{ formData.expectDate || '请选择期望体检日期' }}
          </text>
          <text class="arrow">></text>
        </view>
      </view>
    </view>

    <!-- 联系人信息 -->
    <view class="section card">
      <view class="section-title">
        <text>联系人信息</text>
        <text class="required">*</text>
      </view>
      <view class="form-item">
        <text class="label">联系人</text>
        <input 
          class="input" 
          v-model="formData.contactName" 
          placeholder="请输入联系人姓名"
          placeholder-class="input-placeholder"
        />
      </view>
      <view class="form-item">
        <text class="label">联系电话</text>
        <input 
          class="input" 
          v-model="formData.contactPhone" 
          type="number"
          placeholder="请输入联系电话"
          placeholder-class="input-placeholder"
          maxlength="11"
        />
      </view>
      <view class="form-item">
        <text class="label">电子邮箱</text>
        <input 
          class="input" 
          v-model="formData.email" 
          placeholder="请输入电子邮箱（选填）"
          placeholder-class="input-placeholder"
        />
      </view>
    </view>

    <!-- 套餐选择 -->
    <view class="section card">
      <view class="section-title">
        <text>选择体检套餐</text>
        <text class="required">*</text>
      </view>
      <view class="tip-box">
        <text class="tip-icon">💰</text>
        <text>团检享9折优惠，50人以上享8.5折优惠</text>
      </view>
      <view class="packages-list">
        <view 
          v-for="pkg in packages" 
          :key="pkg.id"
          class="package-card"
          :class="{ active: selectedPackage === pkg.id }"
          @click="selectPackage(pkg.id)"
        >
          <view class="package-check">
            <text>{{ selectedPackage === pkg.id ? '☑' : '☐' }}</text>
          </view>
          <view class="package-info">
            <view class="package-name">{{ pkg.name }}</view>
            <view class="package-desc">{{ pkg.desc }}</view>
            <view class="package-price">
              <text class="original-price" v-if="pkg.discount">¥{{ pkg.price }}</text>
              <text class="current-price">¥{{ getDiscountPrice(pkg) }}</text>
              <text class="discount-tag" v-if="pkg.discount">{{ pkg.discount }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 特殊需求 -->
    <view class="section card">
      <view class="section-title">
        <text>特殊需求</text>
      </view>
      <textarea 
        class="remark-textarea" 
        v-model="formData.remark" 
        placeholder="请填写特殊需求或备注信息（如：需要加项、特殊时间安排等）"
        placeholder-class="input-placeholder"
        maxlength="500"
      />
      <view class="char-count">{{ formData.remark.length }}/500</view>
    </view>

    <!-- 团检流程 -->
    <view class="section card">
      <view class="section-title">
        <text>团检流程</text>
      </view>
      <view class="process-list">
        <view class="process-item" v-for="(item, index) in processList" :key="index">
          <view class="process-number">{{ index + 1 }}</view>
          <view class="process-content">
            <text class="process-title">{{ item.title }}</text>
            <text class="process-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 温馨提示 -->
    <view class="section card">
      <view class="section-title">
        <text>温馨提示</text>
      </view>
      <view class="tips-list">
        <view class="tip-item" v-for="(tip, index) in tips" :key="index">
          <text class="tip-dot">•</text>
          <text class="tip-text">{{ tip }}</text>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <button class="consult-btn" @click="makePhoneCall">
        <text class="btn-icon">📞</text>
        <text>电话咨询</text>
      </button>
      <button class="submit-btn" @click="submitBooking">提交申请</button>
    </view>

    <view class="bottom-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 表单数据
const formData = ref({
  unitName: '',
  peopleCount: '',
  expectDate: '',
  contactName: '',
  contactPhone: '',
  email: '',
  remark: ''
})

// 套餐列表
const packages = ref([
  {
    id: 1,
    name: '基础团检套餐',
    price: 280,
    desc: '15项常规检查',
    discount: '9折'
  },
  {
    id: 2,
    name: '标准团检套餐',
    price: 480,
    desc: '25项全面检查',
    discount: '9折'
  },
  {
    id: 3,
    name: '全面团检套餐',
    price: 880,
    desc: '35项深度检查',
    discount: '9折'
  }
])

// 流程列表
const processList = [
  {
    title: '提交申请',
    desc: '填写团检申请表，提交预约信息'
  },
  {
    title: '客服联系',
    desc: '工作人员1个工作日内电话联系确认'
  },
  {
    title: '方案确认',
    desc: '确定体检套餐、时间、人数等详情'
  },
  {
    title: '提交名单',
    desc: '提供体检人员名单及个人信息'
  },
  {
    title: '进行体检',
    desc: '按约定时间到体检中心进行体检'
  },
  {
    title: '报告发放',
    desc: '3-5个工作日后统一发放体检报告'
  }
]

// 温馨提示
const tips = [
  '团检预约需提前至少5个工作日申请',
  '10人以上享9折优惠，50人以上享8.5折优惠',
  '可根据单位需求定制个性化体检套餐',
  '提供上门采血、现场体检等特色服务',
  '体检报告可选择电子版或纸质版',
  '如有疑问请致电：010-51682525'
]

const selectedPackage = ref(null)

// 选择套餐
const selectPackage = (id) => {
  selectedPackage.value = id
}

// 计算折扣价格
const getDiscountPrice = (pkg) => {
  const peopleCount = parseInt(formData.value.peopleCount) || 0
  let discount = 1
  
  if (peopleCount >= 50) {
    discount = 0.85
  } else if (peopleCount >= 10) {
    discount = 0.9
  }
  
  return Math.floor(pkg.price * discount)
}

// 显示日期选择器
const showDatePicker = () => {
  const today = new Date()
  const minDate = new Date(today.getTime() + 5 * 24 * 60 * 60 * 1000) // 5天后
  const maxDate = new Date(today.getTime() + 60 * 24 * 60 * 60 * 1000) // 60天后
  
  uni.showActionSheet({
    itemList: generateDateList(minDate, maxDate),
    success: (res) => {
      const dates = generateDateList(minDate, maxDate)
      formData.value.expectDate = dates[res.tapIndex]
    }
  })
}

// 生成日期列表
const generateDateList = (start, end) => {
  const dates = []
  const current = new Date(start)
  
  while (current <= end && dates.length < 20) {
    // 跳过周日
    if (current.getDay() !== 0) {
      const year = current.getFullYear()
      const month = String(current.getMonth() + 1).padStart(2, '0')
      const day = String(current.getDate()).padStart(2, '0')
      const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][current.getDay()]
      dates.push(`${year}-${month}-${day} ${weekDay}`)
    }
    current.setDate(current.getDate() + 1)
  }
  
  return dates
}

// 拨打电话
const makePhoneCall = () => {
  uni.makePhoneCall({
    phoneNumber: '010-51682525',
    success: () => {
      console.log('拨打电话成功')
    }
  })
}

// 提交预约
const submitBooking = () => {
  // 验证
  if (!formData.value.unitName) {
    uni.showToast({ title: '请输入单位名称', icon: 'none' })
    return
  }
  
  const peopleCount = parseInt(formData.value.peopleCount)
  if (!peopleCount || peopleCount < 10) {
    uni.showToast({ title: '团检人数至少10人', icon: 'none' })
    return
  }
  
  if (!formData.value.expectDate) {
    uni.showToast({ title: '请选择期望日期', icon: 'none' })
    return
  }
  
  if (!formData.value.contactName) {
    uni.showToast({ title: '请输入联系人姓名', icon: 'none' })
    return
  }
  
  if (!formData.value.contactPhone) {
    uni.showToast({ title: '请输入联系电话', icon: 'none' })
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(formData.value.contactPhone)) {
    uni.showToast({ title: '联系电话格式不正确', icon: 'none' })
    return
  }
  
  if (!selectedPackage.value) {
    uni.showToast({ title: '请选择体检套餐', icon: 'none' })
    return
  }

  // 显示确认对话框
  const pkg = packages.value.find(p => p.id === selectedPackage.value)
  const totalPrice = getDiscountPrice(pkg) * peopleCount
  
  uni.showModal({
    title: '确认提交',
    content: `单位：${formData.value.unitName}\n人数：${peopleCount}人\n套餐：${pkg.name}\n预估费用：¥${totalPrice}\n\n提交后工作人员将在1个工作日内联系您确认详情`,
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '提交中...' })
        
        setTimeout(() => {
          uni.hideLoading()
          uni.showModal({
            title: '申请提交成功',
            content: '您的团检申请已提交，工作人员将在1个工作日内与您联系确认详情。\n\n咨询电话：010-51682525',
            showCancel: false,
            success: () => {
              uni.navigateBack()
            }
          })
        }, 1500)
      }
    }
  })
}
</script>

<style scoped>
.group-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 200rpx;
}

.top-banner {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 40rpx 32rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.banner-icon {
  font-size: 80rpx;
}

.banner-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.banner-title {
  color: #fff;
  font-size: 36rpx;
  font-weight: bold;
}

.banner-desc {
  color: rgba(255, 255, 255, 0.9);
  font-size: 24rpx;
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

.form-item {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  gap: 24rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.label {
  width: 140rpx;
  font-size: 28rpx;
  color: #333;
}

.input {
  flex: 1;
  padding: 16rpx 24rpx;
  background: #f8faff;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
  font-size: 28rpx;
}

.input-placeholder {
  color: #ccc;
}

.date-picker {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 24rpx;
  background: #f8faff;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
}

.placeholder {
  color: #ccc;
}

.arrow {
  color: #999;
}

.tip-box {
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: #fff8e1;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
  font-size: 26rpx;
  color: #f57c00;
}

.tip-icon {
  font-size: 32rpx;
}

.packages-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.package-card {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx;
  border: 2rpx solid #e8e8e8;
  border-radius: 16rpx;
  transition: all 0.3s;
}

.package-card.active {
  border-color: #f5576c;
  background: #fff5f7;
}

.package-check {
  font-size: 40rpx;
  color: #f5576c;
}

.package-info {
  flex: 1;
}

.package-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.package-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.package-price {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.original-price {
  font-size: 24rpx;
  color: #999;
  text-decoration: line-through;
}

.current-price {
  font-size: 32rpx;
  font-weight: bold;
  color: #f5576c;
}

.discount-tag {
  background: #ff4757;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.remark-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  background: #f8faff;
  border-radius: 12rpx;
  border: 2rpx solid #e8e8e8;
  font-size: 28rpx;
  box-sizing: border-box;
}

.char-count {
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
}

.process-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.process-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
}

.process-number {
  width: 56rpx;
  height: 56rpx;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: bold;
  flex-shrink: 0;
}

.process-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  padding-top: 8rpx;
}

.process-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.process-desc {
  font-size: 24rpx;
  color: #999;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
}

.tip-dot {
  color: #f5576c;
  font-size: 28rpx;
  line-height: 40rpx;
}

.tip-text {
  flex: 1;
  font-size: 26rpx;
  color: #666;
  line-height: 40rpx;
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
  gap: 16rpx;
  z-index: 999;
}

.consult-btn {
  flex: 1;
  background: #fff;
  color: #f5576c;
  border: 2rpx solid #f5576c;
  border-radius: 40rpx;
  padding: 24rpx;
  font-size: 28rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.btn-icon {
  font-size: 32rpx;
}

.submit-btn {
  flex: 2;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
  border-radius: 40rpx;
  padding: 24rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
}

.bottom-placeholder {
  height: 160rpx;
}
</style>

