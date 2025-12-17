<template>
  <view class="apply-page">
    <view class="header">
      <view class="title">申请请假</view>
      <view class="subtitle">请填写请假信息并提交</view>
    </view>

    <view class="card">
      <view class="card-title">请假信息</view>

      <view class="form-row">
        <view class="label">医生ID</view>
        <input
          class="input"
          type="text"
          v-model.trim="form.doctorId"
          placeholder="医生ID"
          disabled
        />
      </view>

      <view class="form-row">
        <view class="label">医生姓名</view>
        <input
          class="input"
          type="text"
          v-model.trim="form.doctorName"
          placeholder="医生姓名"
          disabled
        />
      </view>

      <view class="form-row">
        <view class="label">科室ID</view>
        <input
          class="input"
          type="text"
          v-model.trim="form.deptId"
          placeholder="科室ID"
          disabled
        />
      </view>

      <view class="form-row">
        <view class="label">科室名称</view>
        <input
          class="input"
          type="text"
          v-model.trim="form.deptName"
          placeholder="科室名称"
          disabled
        />
      </view>

      <view class="form-row">
        <view class="label">请假类型</view>
        <picker mode="selector" :range="leaveTypes" :range-key="'label'" @change="onLeaveTypeChange">
          <view class="picker">{{ selectedLeaveType || '请选择请假类型' }}</view>
        </picker>
      </view>

      <view class="form-row">
        <view class="label">请假开始时间</view>
        <picker mode="date" :value="form.startDate" @change="onStartDateChange">
          <view class="picker">{{ form.startDate || '请选择开始日期' }}</view>
        </picker>
      </view>

      <view class="form-row">
        <view class="label">请假结束时间</view>
        <picker mode="date" :value="form.endDate" @change="onEndDateChange">
          <view class="picker">{{ form.endDate || '请选择结束日期' }}</view>
        </picker>
      </view>

      <view class="form-row">
        <view class="label">请假事由</view>
        <textarea
          class="textarea"
          v-model.trim="form.reason"
          auto-height
          placeholder="请详细说明请假原因"
        />
      </view>

      <button class="btn" type="primary" :disabled="disabled || loading" :loading="loading" @click="onSubmit">
        提交申请
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { uniShowToast } from '@/utils/uniHelper'
import { useUserStore } from '@/store/user'
import { doctorApi } from '@/api/doctor'
import { leaveApi } from '@/api/leave'

const userStore = useUserStore()

// 请假类型选项
const leaveTypes = [
  { value: 'sick', label: '病假' },
  { value: 'personal', label: '事假' },
  { value: 'annual', label: '年假' },
  { value: 'maternity', label: '产假' },
  { value: 'marriage', label: '婚假' },
  { value: 'bereavement', label: '丧假' },
  { value: 'other', label: '其他' }
]

const selectedLeaveType = ref('')

// 表单数据
const form = ref({
  doctorId: '',
  doctorName: '',
  deptId: '',
  deptName: '',
  leaveType: '',
  startDate: '',
  endDate: '',
  reason: ''
})

const loading = ref(false)
const disabled = computed(() =>
  !form.value.doctorId ||
  !form.value.doctorName ||
  !form.value.deptId ||
  !form.value.deptName ||
  !form.value.leaveType ||
  !form.value.startDate ||
  !form.value.endDate ||
  !form.value.reason.trim()
)

// 请假类型选择
const onLeaveTypeChange = (e) => {
  const index = e.detail.value
  const selected = leaveTypes[index]
  form.value.leaveType = selected.value
  selectedLeaveType.value = selected.label
}

// 日期选择
const onStartDateChange = (e) => {
  form.value.startDate = e.detail.value
  // 如果结束日期早于开始日期，清空结束日期
  if (form.value.endDate && form.value.endDate < form.value.startDate) {
    form.value.endDate = ''
  }
}

const onEndDateChange = (e) => {
  const endDate = e.detail.value
  if (endDate < form.value.startDate) {
    uniShowToast({ title: '结束日期不能早于开始日期', icon: 'none' })
    return
  }
  form.value.endDate = endDate
}

// 提交申请
const onSubmit = async () => {
  if (disabled.value) {
    await uniShowToast({ title: '请完整填写信息', icon: 'none' })
    return
  }

  // 验证日期
  if (form.value.endDate < form.value.startDate) {
    await uniShowToast({ title: '结束日期不能早于开始日期', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await userStore.initFromStorage()

    const payload = {
      doctorId: Number(form.value.doctorId),
      doctorName: form.value.doctorName,
      deptId: Number(form.value.deptId),
      deptName: form.value.deptName,
      leaveType: form.value.leaveType,
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      reason: form.value.reason
    }

    await leaveApi.applyLeave(payload)
    await uniShowToast({ title: '提交成功' })
    
    // 清空表单（保留基本信息）
    form.value.leaveType = ''
    form.value.startDate = ''
    form.value.endDate = ''
    form.value.reason = ''
    selectedLeaveType.value = ''
  } catch (e) {
    await uniShowToast({ title: (e && e.message) || '提交失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  userStore.initFromStorage()
  
  // 从URL参数获取医生信息
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}
  
  if (options.doctorId) {
    form.value.doctorId = options.doctorId
  }
  if (options.doctorName) {
    form.value.doctorName = decodeURIComponent(options.doctorName)
  }
  if (options.deptId) {
    form.value.deptId = options.deptId
  }
  if (options.deptName) {
    form.value.deptName = decodeURIComponent(options.deptName)
  }

  // 如果URL参数中没有，尝试从API获取
  if (!form.value.doctorId || !form.value.doctorName) {
    ;(async () => {
      try {
        let p = await doctorApi.getMyProfile()
        if (!p || (typeof p === 'object' && Object.keys(p).length === 0)) {
          const uid = userStore.userInfo?.userId || userStore.userInfo?.id
          if (uid) {
            p = await doctorApi.getProfileByUserId(Number(uid))
          }
        }
        if (p) {
          if (!form.value.doctorId) {
            form.value.doctorId = String(p.doctorId || p.id || '')
          }
          if (!form.value.doctorName) {
            form.value.doctorName = p.doctorName || p.name || p.realName || ''
          }
          if (!form.value.deptId) {
            form.value.deptId = String(p.deptId || p.departmentId || '')
          }
          if (!form.value.deptName) {
            form.value.deptName = p.deptName || p.departmentName || p.department || ''
          }
        }
      } catch (_) {
        console.error('获取医生信息失败:', _)
      }
    })()
  }
})
</script>

<style scoped>
.apply-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e8f3ff 0%, #f7faff 45%, #ffffff 100%);
  padding-bottom: 24rpx;
}

.header {
  padding: 26rpx 24rpx;
  background: linear-gradient(120deg, #3a9cff 0%, #6ec6ff 100%);
  color: #fff;
  box-shadow: 0 12rpx 28rpx rgba(58,156,255,0.25);
}

.title {
  font-size: 36rpx;
  font-weight: 800;
  letter-spacing: 1rpx;
}

.subtitle {
  font-size: 26rpx;
  opacity: 0.9;
  margin-top: 6rpx;
}

.card {
  background: #fff;
  border-radius: 18rpx;
  margin: 18rpx;
  padding: 24rpx;
  box-shadow: 0 10rpx 24rpx rgba(58, 156, 255, 0.12);
  border: 1rpx solid #eef3fb;
}

.card-title {
  font-size: 30rpx;
  color: #1e293b;
  font-weight: 600;
  margin-bottom: 20rpx;
}

.form-row {
  margin-bottom: 20rpx;
}

.label {
  font-size: 26rpx;
  color: #475569;
  margin-bottom: 8rpx;
  font-weight: 600;
}

.picker {
  background: #f6f9ff;
  border-radius: 14rpx;
  padding: 16rpx;
  font-size: 28rpx;
  color: #334155;
  min-height: 48rpx;
  display: flex;
  align-items: center;
  border: 1rpx solid #e1e9f6;
}

.input {
  background: #f6f9ff;
  border-radius: 14rpx;
  padding: 16rpx;
  font-size: 28rpx;
  color: #334155;
  width: 100%;
  box-sizing: border-box;
  border: 1rpx solid #e1e9f6;
}

.input[disabled] {
  color: #94a3b8;
  background: #f8fafc;
}

.textarea {
  width: 100%;
  min-height: 160rpx;
  background: #f6f9ff;
  border-radius: 14rpx;
  padding: 16rpx;
  font-size: 28rpx;
  color: #334155;
  box-sizing: border-box;
  border: 1rpx solid #e1e9f6;
}

.btn {
  width: 100%;
  background: linear-gradient(135deg, #3a9cff 0%, #6ec6ff 100%);
  color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 30rpx;
  font-weight: 700;
  margin-top: 24rpx;
  border: none;
  box-shadow: 0 10rpx 24rpx rgba(58,156,255,0.2);
}

.btn[disabled] {
  background: #cbd5e1;
  color: #94a3b8;
}
</style>

