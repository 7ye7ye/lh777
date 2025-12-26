<template>
  <view class="apply-page">
    <view class="header">
      <view class="title">申请调班</view>
      <view class="subtitle">请填写调班信息并提交</view>
    </view>

    <view class="card">
      <view class="card-title">申请信息</view>

      <view class="form-row">
        <view class="label">医生姓名</view>
        <input
          class="input"
          type="text"
          v-model.trim="form.doctorName"
          placeholder="请输入医生姓名"
        />
      </view>

      <view class="form-row">
        <view class="label">科室</view>
        <input
          class="input"
          type="text"
          v-model.trim="form.deptName"
          placeholder="请输入科室名称"
        />
      </view>

      <!-- 新：原排班记录ID -->
      <view class="form-row">
        <view class="label">原排班记录ID</view>
        <input
          class="input"
          type="number"
          v-model.number="form.originalScheduleId"
          placeholder="请输入原排班记录ID"
        />
      </view>

      <!-- 目标日期 -->
      <view class="form-row">
        <view class="label">目标日期</view>
        <picker mode="date" :value="form.targetDate" @change="onDateChange($event, 'target')">
          <view class="picker">{{ form.targetDate || '请选择日期' }}</view>
        </picker>
      </view>

      <!-- 目标班次 -->
      <view class="form-row">
        <view class="label">目标班次</view>
        <view class="slots">
          <view
            v-for="slot in slots"
            :key="'target-' + slot.value"
            class="slot"
            :class="{ active: form.targetTimeSlot === slot.value }"
            @click="form.targetTimeSlot = slot.value"
          >
            {{ slot.label }}
          </view>
        </view>
      </view>

      <!-- 新：目标出诊科室ID -->
      <view class="form-row" v-if="false">
        <view class="label">目标出诊科室ID</view>
        <input
          class="input"
          type="number"
          v-model.number="form.targetDeptId"
          placeholder="请输入目标出诊科室ID"
        />
      </view>

      <!-- 新：目标出诊科室名称（仅展示/辅助输入） -->
      <view class="form-row" v-if="false">
        <view class="label">目标出诊科室名称</view>
        <input
          class="input"
          type="text"
          v-model.trim="form.targetDeptName"
          placeholder="请输入目标出诊科室名称"
        />
      </view>

      <!-- 申请理由 -->
      <view class="form-row">
        <view class="label">申请理由</view>
        <textarea
          class="textarea"
          v-model.trim="form.reason"
          auto-height
          placeholder="请简要说明调班原因（如就诊安排、个人事务等）"
        />
      </view>

      <button class="btn" type="primary" :disabled="disabled || loading" :loading="loading" @click="onSubmit">
        提交申请
      </button>
    </view>

    <view class="card">
      <view class="card-title row">
        <view>我的申请记录</view>
        <picker :range="statusOptions" @change="onStatusFilter">
          <view class="filter">{{ statusText }}</view>
        </picker>
      </view>

      <view v-if="list.length === 0" class="empty">暂无记录</view>
      <view v-else class="list">
        <view v-for="item in list" :key="item.adjustmentId" class="item">
          <view class="row">
            <view class="tag" :class="statusClass(item.status)">{{ statusMap[item.status] || '未知' }}</view>
            <view class="time">{{ (item.applyTime || '').replace('T',' ') }}</view>
          </view>
          <view class="desc">
            <text>原排班ID：{{ item.originalScheduleId }}</text>
            <text> → 目标：{{ item.targetDate }}（{{ slotLabel(item.targetTimeSlot) }}）</text>
          </view>
          <view class="reason">理由：{{ item.reason }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { uniShowToast } from '@/utils/uniHelper'
import { useUserStore } from '@/store/user'
import { doctorApi } from '@/api/doctor'

const userStore = useUserStore()
const toIntId = (v) => {
  const n = Number(v)
  return Number.isFinite(n) && n > 0 ? n : 1
}
const doctorId = computed(() => toIntId(userStore.userInfo?.doctorId ?? userStore.userInfo?.id ?? 1))

const slots = [
  { value: 1, label: '上午' },
  { value: 2, label: '下午' },
  { value: 3, label: '晚上' },
]

// 表单：与数据库字段对齐
const form = ref({
  originalScheduleId: undefined,
  targetDate: '',
  targetTimeSlot: 1,
  targetDeptId: undefined,
  targetDeptName: '', // 辅助展示
  doctorName: '',
  deptName: '',
  reason: ''
})

const loading = ref(false)
const disabled = computed(() =>
  !form.value.originalScheduleId ||
  !form.value.targetDate ||
  !form.value.targetTimeSlot ||
  !form.value.reason
)

const onDateChange = (e, which) => {
  const val = e?.detail?.value || ''
  if (which === 'target') form.value.targetDate = val
}

const slotLabel = (s) => (slots.find(x => x.value === s)?.label || '-')

const slotFromTimeRange = (range) => {
  const r = String(range || '')
  if (!r) return undefined
  if (r.includes('08:00-12:00')) return 1
  if (r.includes('14:00-17:00')) return 2
  if (r.includes('18:00-20:00')) return 3
  return undefined
}

const isSameAsOriginalSchedule = async () => {
  const originId = Number(form.value.originalScheduleId)
  const targetDate = String(form.value.targetDate || '')
  const targetSlot = Number(form.value.targetTimeSlot)
  if (!Number.isFinite(originId) || originId <= 0) return false
  if (!targetDate || !Number.isFinite(targetSlot)) return false

  const resp = await doctorApi.getSchedules(doctorId.value, targetDate, 1)
  const schedules = Array.isArray(resp) ? resp : (resp?.records ?? [])
  if (!Array.isArray(schedules) || schedules.length === 0) return false

  const origin = schedules.find((s) => {
    const sid = Number(s?.id ?? s?.scheduleId ?? s?.schedule_id)
    return Number.isFinite(sid) && sid === originId
  })
  if (!origin) return false

  const date = String(origin?.date ?? origin?.scheduleDate ?? origin?.schedule_date ?? '').slice(0, 10)
  const slot = Number(origin?.timeSlot ?? origin?.time_slot ?? slotFromTimeRange(origin?.timeRange ?? origin?.time_range ?? origin?.timePeriod ?? origin?.time_period))
  if (!date || !Number.isFinite(slot)) return false

  return date === targetDate && slot === targetSlot
}

const hasTargetScheduleConflict = async () => {
  const targetDate = String(form.value.targetDate || '')
  const targetSlot = Number(form.value.targetTimeSlot)
  if (!targetDate || !Number.isFinite(targetSlot)) return false

  const originId = Number(form.value.originalScheduleId)
  const resp = await doctorApi.getSchedules(doctorId.value, targetDate, 1)
  const schedules = Array.isArray(resp) ? resp : (resp?.records ?? [])
  if (!Array.isArray(schedules) || schedules.length === 0) return false

  return schedules.some((s) => {
    const sid = Number(s?.id ?? s?.scheduleId ?? s?.schedule_id)
    if (Number.isFinite(originId) && originId > 0 && Number.isFinite(sid) && sid === originId) return false

    const date = String(s?.date ?? s?.scheduleDate ?? s?.schedule_date ?? '').slice(0, 10)
    if (!date || date !== targetDate) return false

    const slot = Number(s?.timeSlot ?? s?.time_slot ?? slotFromTimeRange(s?.timeRange ?? s?.time_range ?? s?.timePeriod ?? s?.time_period))
    return Number.isFinite(slot) && slot === targetSlot
  })
}

// 新状态映射：1-待审批，2-已通过，3-已驳回，4-已撤销
const statusMap = { 1: '待审批', 2: '已通过', 3: '已驳回', 4: '已撤销' }
const statusOptions = ['全部', '待审批', '已通过', '已驳回', '已撤销']
const statusText = ref('全部')
const statusFilter = ref(undefined)

const onStatusFilter = (e) => {
  const idx = Number(e?.detail?.value ?? 0)
  statusText.value = statusOptions[idx]
  // 0->undefined, 1->1, 2->2, 3->3, 4->4
  statusFilter.value = idx === 0 ? undefined : idx
  loadList()
}

const list = ref([])

const loadList = async () => {
  try {
    await userStore.initFromStorage()
    const resp = await doctorApi.listShiftChange(doctorId.value, statusFilter.value)
    list.value = Array.isArray(resp) ? resp : (resp?.records ?? [])
  } catch (e) {
    list.value = []
  }
}

const onSubmit = async () => {
  if (disabled.value) {
    await uniShowToast({ title: '请完整填写信息', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await userStore.initFromStorage()

    const today = (() => {
      const d = new Date()
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    })()
    if (String(form.value.targetDate || '') === today) {
      await uniShowToast({ title: '不允许医生申请当天调班', icon: 'none' })
      return
    }

    if (await isSameAsOriginalSchedule()) {
      await uniShowToast({ title: '与原排班相同', icon: 'none' })
      return
    }

    if (await hasTargetScheduleConflict()) {
      await uniShowToast({ title: '目的班次已有排班', icon: 'none' })
      return
    }

    const payload = {
      doctorId: doctorId.value,
      originalScheduleId: form.value.originalScheduleId,
      targetDate: form.value.targetDate,
      targetTimeSlot: form.value.targetTimeSlot,
      reason: form.value.reason
    }
    await doctorApi.applyShiftChange(payload)
    await uniShowToast({ title: '提交成功' })
    form.value.reason = ''
    await loadList()
  } catch (e) {
    await uniShowToast({ title: (e && e.message) || '提交失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const userStore = useUserStore()
  userStore.initFromStorage()
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
        form.value.doctorName = p.doctorName || p.name || p.realName || ''
        form.value.deptName = p.deptName || p.departmentName || p.department || ''
      }
    } catch (_) {}
  })()
  loadList()
})

// 新增：状态对应样式类（兼容旧数据0=待审核）
const statusClass = (s) => {
  if (s === 0 || s === 1) return 'audit'
  if (s === 2) return 'pass'
  if (s === 3) return 'reject'
  if (s === 4) return 'cancel'
  return ''
}
</script>

<style scoped>
.apply-page { min-height: 100vh; background: #f7faff; padding-bottom: 24rpx; }
.header { padding: 24rpx; background: linear-gradient(90deg, #479fff 0%, #2176ff 100%); color: #fff; }
.title { font-size: 36rpx; font-weight: 700; }
.subtitle { font-size: 26rpx; opacity: 0.9; margin-top: 6rpx; }
.card { background: #fff; border-radius: 16rpx; margin: 16rpx; padding: 16rpx; box-shadow: 0 6rpx 16rpx rgba(33,118,255,0.08); }
.card-title { font-size: 30rpx; color: #1e293b; font-weight: 600; margin-bottom: 12rpx; }
.card-title.row { display: flex; justify-content: space-between; align-items: center; }
.filter { color: #2176ff; }

.form-row { margin-bottom: 12rpx; }
.label { font-size: 26rpx; color: #64748b; margin-bottom: 6rpx; }
.picker { background: #f1f5f9; border-radius: 12rpx; padding: 12rpx; font-size: 28rpx; color: #334155; }
.input { background: #f1f5f9; border-radius: 12rpx; padding: 12rpx; font-size: 28rpx; color: #334155; }

.slots { display: flex; gap: 12rpx; }
.slot { padding: 10rpx 16rpx; background: #f1f5f9; border-radius: 12rpx; font-size: 26rpx; color: #334155; }
.slot.active { background: #e0f2fe; color: #0369a1; border: 1rpx solid #2176ff; }

.textarea { width: 100%; min-height: 120rpx; background: #f1f5f9; border-radius: 12rpx; padding: 12rpx; font-size: 28rpx; color: #334155; }

.btn { width: 100%; background: #2176ff; color: #fff; border-radius: 12rpx; padding: 16rpx; font-size: 28rpx; margin-top: 8rpx; }

.empty { color: #64748b; text-align: center; padding: 16rpx; }
.list .item { padding: 12rpx; border-bottom: 1rpx solid #e2e8f0; }
.list .row { display: flex; justify-content: space-between; color: #334155; }
.tag { padding: 4rpx 10rpx; border-radius: 999rpx; font-size: 24rpx; background: #f1f5f9; }
.tag.audit { background: #e0f2fe; color: #0369a1; }
.tag.pass { background: #dcfce7; color: #166534; }
.tag.reject { background: #fee2e2; color: #b91c1c; }
/* 新增：撤销状态样式 */
.tag.cancel { background: #e5e7eb; color: #374151; }
.time { color: #64748b; font-size: 24rpx; }
.desc { color: #0f172a; font-size: 26rpx; margin-top: 6rpx; }
.reason { color: #64748b; font-size: 24rpx; margin-top: 4rpx; }
</style>