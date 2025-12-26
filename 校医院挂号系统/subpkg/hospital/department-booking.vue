<template>
  <view class="department-booking-page">
    <!-- 顶部标题 -->
    <view class="page-header">
      <text class="header-title">预约挂号</text>
      <view class="header-right">
        <button class="refresh-btn small" @click="refreshBookingData">
          <text class="refresh-icon">⟳</text>
          <text class="refresh-text">刷新</text>
        </button>
      </view>
    </view>

    <!-- 日期选择器 - 移到最上方 -->
    <view class="date-selection">
      <scroll-view scroll-x :show-scrollbar="false" class="date-scroll">
        <view 
          v-for="(date, index) in dateList" 
          :key="index"
          class="date-item"
          :class="{ 'active': selectedDate === date.date }"
          @click="changeDate({date: date.date})"
        >
          <text class="date-week">{{ date.week }}</text>
          <text class="date-day">{{ date.day }}</text>
          <text v-if="date.isToday" class="date-tag">今天</text>
        </view>
      </scroll-view>
    </view>

    <!-- 日期下方就诊人选择区域，靠右与科室树并行 -->
    <view class="patient-inline-row">
      <view class="patient-inline-right">
        <view class="patient-selector-small inline">
          <picker 
            mode="selector" 
            :range="patientList" 
            range-key="patientName"
            :value="selectedPatientIndex"
            @change="onPatientChange"
          >
            <view class="picker-small">
              <view class="picker-left">
                <view class="picker-avatar">
                  {{ getPatientInitial(currentPatient?.patientName) }}
                </view>
                <view class="picker-text">
                  <text 
                    class="picker-name" 
                    :class="{ 'placeholder': !currentPatient?.patientName }"
                  >
                    {{ currentPatient?.patientName || '选择就诊人' }}
                  </text>
                </view>
              </view>
              <view class="picker-right">
                <text class="picker-arrow">⌄</text>
              </view>
            </view>
          </picker>
        </view>
      </view>
    </view>

    <!-- 主内容区域 - 双栏布局 -->
    <view class="main-content">
      <!-- 左侧科室列表 -->
        <view class="left-panel">
          <!-- 隐藏标签页 -->
          <view class="tabs" style="display: none;">
            <view class="tab active">全部</view>
            <view class="tab">普通门诊</view>
          </view>
          
          <scroll-view scroll-y :show-scrollbar="false" class="department-tree-scroll">
            <!-- 科室树形结构 -->
            <view class="department-tree">
              <view 
                v-for="(department, index) in departmentTree" 
                :key="department.deptId"
                class="department-item"
              >
                <!-- 一级科室 - 可点击展开/收起 -->
                <view 
                  class="primary-department"
                  :class="{ 'active': department.selected }"
                  @click="toggleDepartment(department)"
                >
                  <text class="department-name">{{ department.deptName }}</text>
                  <text class="toggle-icon" :class="{ 'expanded': department.expanded }">
                    {{ department.expanded ? '▼' : '▶' }}
                  </text>
                </view>
                
                <!-- 二级科室列表 - 默认隐藏 -->
                <view 
                  v-if="department.children && department.children.length > 0" 
                  class="secondary-department-list"
                  :class="{ 'expanded': department.expanded }"
                >
                  <view 
                    v-for="(child, childIndex) in department.children" 
                    :key="child.deptId || child.id"
                    class="secondary-department"
                    :class="{
                      'active': child.selected
                    }"
                    @click="onDeptClick(child)"
                  >
                    <text class="department-name">
                      {{ child.deptName }}
                    </text>
                  </view>
                </view>
              </view>
            </view>
          
          <!-- 搜索结果 -->
          <view v-if="searchKeyword" class="search-results">
            <view v-if="searchResults.length > 0">
              <view class="result-header">
                <text>找到 {{ searchResults.length }} 个科室</text>
              </view>
              <view class="search-result-items">
                <view 
                  v-for="(dept, index) in searchResults" 
                  :key="index"
                  class="search-result-item"
                  @click="onDeptClick(dept)"
                >
                  <text class="result-name">{{ dept.deptName }}</text>
                  <text class="result-desc">{{ dept.deptDesc }}</text>
                </view>
              </view>
            </view>
            <view v-else class="empty-result">
              <text class="empty-icon">🔍</text>
              <text class="empty-text">未找到相关科室</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 右侧医生排班信息 -->
      <view class="right-panel">
        <!-- 科室信息头部 - 隐藏 -->
        <view class="department-header" style="display: none;">
          <view v-if="selectedDepartment" class="department-info">
            <text class="department-address">西直门院区</text>
            <text class="department-location">北京市西城区西直门南大街11号</text>
          </view>
        </view>
        
        <!-- 医生排班列表 -->
        <scroll-view scroll-y class="schedule-container">
            <view v-if="!selectedDepartment" class="empty-state">
              <text class="empty-icon">🏥</text>
              <text class="empty-text">请选择科室</text>
            </view>
            <view v-else-if="doctors.length === 0" class="no-doctors">
              <text class="no-doctors-text">当前科室暂无医生排班</text>
            </view>
            <view v-else>
              <!-- 医生排班列表 -->
              <view v-for="doctor in doctors" :key="doctor.id" class="doctor-item" :class="{ 'doctor-locked': getDoctorLimitState(doctor).locked }">
                <!-- 医生基本信息 - 可点击跳转到详情 -->
                <view class="doctor-info" @click="viewDoctorDetail(doctor)">
                  <view class="doctor-avatar">{{ getDoctorInitial(doctor.name || doctor.doctorName || '未知医生') }}</view>
                  <view class="doctor-details">
                    <view class="doctor-name">
                      {{ doctor.name || doctor.doctorName || '未知医生' }}
                      <text 
                        v-if="getDoctorLimitState(doctor).locked" 
                        class="doctor-limit-text"
                      >
                        {{ getDoctorLimitState(doctor).label }}
                      </text>
                    </view>
                    <view class="doctor-title-row">
                      <text class="doctor-title">{{ doctor.title || '医师' }}</text>
                      <text v-if="getDoctorTypeName(doctor)" class="doctor-type-name">
                        {{ getDoctorTypeName(doctor) }}
                      </text>
                    </view>
                    <view class="doctor-specialty">擅长: {{ doctor.specialty || '内科' }}</view>
                  </view>
                </view>
                <!-- 按日期分组显示排班 -->
                <view class="date-section">
                  <text class="date-title">{{ formatDateTitle(selectedDate) }}</text>
                </view>
                
                <!-- 单个医生的排班信息 -->
                <view v-if="doctorSchedules[doctor.id]">
                  <!-- 时段选择区域 -->
                  <view class="time-slots-container">
                    <!-- 上午时段 -->
                    <view 
                      v-for="slot in [
                        { key: '上午', label: '上午 (08:00-12:00)', timeText: '上午 (08:00-12:00)' },
                        { key: '下午', label: '下午 (14:00-17:00)', timeText: '下午 (14:00-17:00)' },
                        { key: '晚上', label: '晚上 (18:00-20:00)', timeText: '晚上 (18:00-20:00)' }
                      ]"
                      :key="slot.key"
                      class="time-slot-item" 
                      :class="{ 
                        'clickable': (getSlotByTimeRange(doctor.id, slot.key)?.availableSlots > 0 && !getSlotByTimeRange(doctor.id, slot.key)?.expired && !getSlotLimitStateByKey(doctor, slot.key).disabled) || (getSlotByTimeRange(doctor.id, slot.key) && !getSlotByTimeRange(doctor.id, slot.key)?.expired && getSlotByTimeRange(doctor.id, slot.key)?.availableSlots === 0 && !getSlotLimitStateByKey(doctor, slot.key).disabled),
                        'expired': getSlotByTimeRange(doctor.id, slot.key)?.expired,
                        'no-slot': !getSlotByTimeRange(doctor.id, slot.key),
                        'full': getSlotByTimeRange(doctor.id, slot.key) && !getSlotByTimeRange(doctor.id, slot.key)?.expired && getSlotByTimeRange(doctor.id, slot.key)?.availableSlots === 0,
                        'disabled': getSlotLimitStateByKey(doctor, slot.key).disabled
                      }"
                      @click="onSlotClick(doctor, slot)"
                    >
                      <view class="slot-info">
                        <text class="slot-label">{{ slot.label }}</text>
                        <text 
                          v-if="getSlotLimitStateByKey(doctor, slot.key).disabled && getSlotLimitStateByKey(doctor, slot.key).reason" 
                          class="slot-reason"
                        >
                          {{ getSlotLimitStateByKey(doctor, slot.key).reason }}
                        </text>
                      </view>
                      <!-- 状态显示 -->
                      <text v-if="!getSlotByTimeRange(doctor.id, slot.key)" class="slot-status no-slot">
                        无号
                      </text>
                      <text v-else-if="getSlotByTimeRange(doctor.id, slot.key)?.expired" class="slot-status expired">
                        已过预约时间
                      </text>
                      <text v-else-if="getSlotLimitStateByKey(doctor, slot.key).disabled" class="slot-status disabled">
                        {{ getSlotLimitStateByKey(doctor, slot.key).reason || '已禁用' }}
                      </text>
                      <text v-else-if="getSlotByTimeRange(doctor.id, slot.key)?.availableSlots === 0" class="slot-status full">
                        已满
                      </text>
                      <text v-else class="slot-status">
                        余{{ getSlotByTimeRange(doctor.id, slot.key).availableSlots }}
                      </text>
                    </view>
                  </view>
                </view>
                <view v-else class="no-schedule">
                  <text>暂无排班信息</text>
                </view>
              </view>
            </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getDepartmentTree } from '@/api/department'
import { getDoctorsByDeptId, getDoctorDetail } from '@/api/doctor'
import { scheduleApi } from '@/api/schedule'
import { getDoctorSchedules, getRegistrationTypes, getRegistrationRecords, checkDuplicateBySchedule, checkDeptLimitBySchedule, createRegistration } from '@/api/registration'
import { ensurePatientCard } from '@/utils/patientHelper'
import { patientApi } from '@/api/patient'
import { useUserStore } from '@/store/user'

// 生成医生头像初始化字母
const getDoctorInitial = (name) => {
  if (!name || name.length === 0) return '医';
  // 获取姓或名的第一个字符
  return name.charAt(0);
}

// 生成就诊人头像初始化字母
const getPatientInitial = (name) => {
  if (!name || name.length === 0) return '选';
  return name.charAt(0);
}

// 将时间文本映射为 slotKey（上午/下午/晚上）
const slotKeyFromTimeText = (timeText) => {
  if (!timeText) return ''
  if (timeText.includes('上午') || timeText.includes('08:')) return '上午'
  if (timeText.includes('下午') || timeText.includes('14:')) return '下午'
  if (timeText.includes('晚上') || timeText.includes('18:')) return '晚上'
  return ''
}

const searchKeyword = ref('')
const departmentTree = ref([])
const selectedDepartment = ref(null)
const selectedDeptId = ref('')
const currentDepartment = ref({
  deptName: '请选择科室',
  deptDesc: ''
})
const doctors = ref([])
const doctorSchedules = ref({})
const selectedDate = ref('')
const dateList = ref([])
const registrationTypeNameMap = ref({})
const patientId = ref(null)
const patientRecords = ref([]) // 患者当天的挂号记录
const DAILY_TOTAL_LIMIT = 3
const DAILY_DOCTOR_LIMIT = 1
const DAILY_DEPT_LIMIT = 2
const loadingRecords = ref(false)
const patientList = ref([]) // 就诊人列表
const currentPatient = ref(null) // 当前选择的就诊人
const selectedPatientIndex = ref(0) // 当前选择的就诊人索引
const userStore = useUserStore()

const loadRegistrationTypeNameMap = async () => {
  if (registrationTypeNameMap.value && Object.keys(registrationTypeNameMap.value).length > 0) return
  try {
    const res = await getRegistrationTypes()
    const types = Array.isArray(res?.result) ? res.result : Array.isArray(res?.data) ? res.data : Array.isArray(res) ? res : []
    const map = {}
    types.forEach((t) => {
      const id = t?.typeId ?? t?.type_id ?? t?.id
      const name = t?.typeName ?? t?.type_name ?? t?.name
      if (id != null && name) map[String(id)] = name
    })
    registrationTypeNameMap.value = map
  } catch (e) {
    registrationTypeNameMap.value = {}
  }
}

// 工具：两位数补零
const pad = (n) => n.toString().padStart(2, '0')

// 初始化日期列表（未来7天）
const initDateList = () => {
  const dates = []
  const today = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    const month = date.getMonth() + 1
    const day = date.getDate()
    const weekDay = weekDays[date.getDay()]
    
    dates.push({
      date: `${date.getFullYear()}-${pad(month)}-${pad(day)}`,
      day: `${month}.${day}`,
      week: weekDay,
      isToday: i === 0
    })
  }
  
  dateList.value = dates
  selectedDate.value = dates[0].date // 默认选择今天
}

// 切换选择的日期
const changeDate = async (date) => {
  selectedDate.value = date.date
  
  // 如果已选择科室且有医生列表，重新加载排班信息
  if (selectedDepartment.value && doctors.value.length > 0) {
    for (const doctor of doctors.value) {
      await fetchDoctorSchedules(doctor.id, date.date)
    }
  }
  
  // 重新加载患者当天的挂号记录
  if (patientId.value) {
    await loadPatientRecords()
  }
}

// 格式化日期为标题形式
const formatDateTitle = (dateString) => {
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekDay = weekDays[date.getDay()]
  return `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')} (${weekDay})`
}

// 格式化日期带星期
const formatDateWithWeek = (dateString) => {
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[date.getDay()]
  return `${year}-${month}.${day} (${weekDay})`
}

  // 预约功能
const makeAppointment = async (doctor, schedule) => {
  try {
    // 这里可以实现实际的预约逻辑
    // const response = await appointmentApi.createAppointment({
    //   doctorId: doctor.id,
    //   scheduleId: schedule.id,
    //   departmentId: selectedDepartment.value.deptId,
    //   date: schedule.date,
    //   timeRange: schedule.timeRange
    // })
    
    // 模拟预约成功
    console.log('预约信息:', { doctor, schedule })
    alert(`预约成功！\n医生：${doctor.name}\n日期：${schedule.date}\n时间：${schedule.timeRange}\n费用：¥${schedule.fee}`)
    
    // 刷新排班信息，更新剩余号数
    await fetchDoctorSchedules(doctor.id, schedule.date)
  } catch (error) {
    console.error('预约失败:', error)
    alert('预约失败，请稍后重试')
  }
}

  // 查看医生详情 - 跳转到详情页面
const viewDoctorDetail = async (doctor) => {
  try {
    // 使用uni.navigateTo跳转到医生详情页面
    // 传递医生ID和基本信息作为参数
    uni.navigateTo({
      url: `/subpkg/hospital/doctor-detail?id=${doctor.id}&name=${encodeURIComponent(doctor.name)}&title=${encodeURIComponent(doctor.title || '')}&specialty=${encodeURIComponent(doctor.specialty || '')}`,
      success: () => {
        console.log('成功跳转到医生详情页面:', doctor.name)
      },
      fail: (err) => {
        console.error('跳转失败:', err)
        // 如果跳转失败，显示一个提示
        uni.showToast({
          title: '跳转失败，请稍后重试',
          icon: 'none'
        })
      }
    })
  } catch (error) {
    console.error('跳转到医生详情失败:', error)
  }
}

// 选择科室的函数已在下方正确实现
  
  // 根据科室获取医生列表
  const fetchDoctorsByDeptId = async (deptId) => {
    try {
      // 显示加载提示
      uni.showLoading({
        title: '加载医生信息中...',
        mask: true
      })
      
      // 调用API获取医生列表
      const response = await getDoctorsByDeptId(deptId)
      console.log('医生列表响应:', response)
      
      // 处理不同的响应格式，使其更加健壮
      let data = response
      if (response && response.data) {
        data = response.data
      } else if (response && response.result) {
        data = response.result
      }
      
      // 尝试从不同结构中获取医生数据
      let doctorList = []
      if (Array.isArray(data)) {
        doctorList = data
      } else if (data && data.doctors && Array.isArray(data.doctors)) {
        doctorList = data.doctors
      }
      
      if (doctorList.length > 0) {
        // 转换医生数据格式，确保必要字段存在
          doctors.value = doctorList.map(doctor => {
            // 确保 doctorId 有值，优先使用 doctorId（后端字段名）
            const doctorId = doctor.doctorId || doctor.id || `temp_${Math.random().toString(36).substr(2, 9)}`;
            
            return {
              id: doctorId,
              doctorId: doctorId, // 同时保留doctorId字段以兼容后端API
              name: doctor.doctorName || doctor.name || '未知医生',
              title: doctor.title || '医师',
              specialty: doctor.specialty || doctor.department || '内科',
              ...doctor
            };
          })
        
        console.log('医生列表加载成功:', doctors.value)
      } else {
        // 没有找到符合格式的医生数据，使用空数组
        doctors.value = []
        console.log('未找到医生数据或数据格式不符合要求')
      }
      
      // 清除之前的排班信息
      doctorSchedules.value = {}
      
      // 如果已选择日期，为每个医生获取排班信息
      if (selectedDate.value && doctors.value.length > 0) {
        for (const doctor of doctors.value) {
          if (doctor.id) {
            // 确保传递的 doctorId 是数字类型
            const doctorId = Number(doctor.id.toString().replace('temp_', ''));
            if (!isNaN(doctorId)) {
              await fetchDoctorSchedules(doctorId, selectedDate.value);
            } else {
              console.error('无效的医生ID格式:', doctor.id);
            }
          } else {
            console.error('医生ID不存在:', doctor);
          }
        }
      }
    } catch (error) {
      console.error('获取医生列表失败:', error)
      // 清空医生列表，确保不使用模拟数据
      doctors.value = []
      
      // 显示错误提示
      uni.showToast({
        title: error.message || '医生数据加载失败',
        icon: 'none'
      })
    } finally {
      // 隐藏加载提示
      uni.hideLoading()
    }
  }
  
  // 根据医生ID和时间段获取排班信息
  const getSlotByTimeRange = (doctorId, timeRange) => {
    const schedules = doctorSchedules.value[doctorId] || [];
    return schedules.find(slot => slot.timeRange === timeRange);
  };

  const extractRecordDoctorId = (record) => {
    return Number(
      record?.doctor_id ??
      record?.doctorId ??
      record?.doctor_id ??
      record?.doctor?.id ??
      record?.doctor?.doctorId
    )
  }

  const extractRecordDeptId = (record) => {
    return Number(
      record?.dept_id ??
      record?.deptId ??
      record?.departmentId ??
      record?.department_id ??
      record?.department?.id ??
      record?.dept?.deptId
    )
  }

  const getDoctorIdFromDoctor = (doctor) => {
    return Number(doctor?.doctorId ?? doctor?.id ?? doctor?.doctor_id)
  }

  const getDeptIdFromContext = (doctor, schedule) => {
    return Number(
      selectedDepartment.value?.deptId ??
      selectedDepartment.value?.id ??
      selectedDeptId.value ??
      doctor?.deptId ??
      doctor?.departmentId ??
      doctor?.dept_id ??
      schedule?.deptId ??
      schedule?.departmentId ??
      schedule?.dept_id
    )
  }

  const getPatientTotalCountToday = () => patientRecords.value.length

  const getPatientDoctorCountToday = (doctorId) => {
    if (!doctorId) return 0
    return patientRecords.value.filter(r => extractRecordDoctorId(r) === Number(doctorId)).length
  }

  const getPatientDeptCountToday = (deptId) => {
    if (!deptId) return 0
    return patientRecords.value.filter(r => extractRecordDeptId(r) === Number(deptId)).length
  }

// 获取科室锁定状态（暂不展示上限提示）
const getDeptLimitState = () => ({
  locked: false,
  type: '',
  label: '',
  toast: '',
  modalTitle: '',
  modalContent: ''
})

// 获取医生锁定状态（暂不展示上限提示）
const getDoctorLimitState = () => ({
  locked: false,
  type: '',
  label: '',
  toast: '',
  modalTitle: '',
  modalContent: ''
})

const showDuplicateAppointmentModal = (recordId = null) => {
  // 确保 recordId 是有效的数字或字符串
  const validRecordId = recordId && (Number(recordId) || String(recordId).trim()) ? recordId : null
  
  const content = validRecordId 
    ? '您已预约过此号，不可重复预约。如需查看已预约的详情，请点击"查看详情"按钮。'
    : '您已预约过此号，不可重复预约。'
  
  console.log('🔔 显示预约上限弹窗 - recordId:', validRecordId, '原始recordId:', recordId)
  
  uni.showModal({
    title: '预约已达上限',
    content: content,
    showCancel: true,
    cancelText: '确定',
    confirmText: validRecordId ? '查看详情' : undefined,
    success: (res) => {
      if (res.confirm && validRecordId) {
        // 跳转到就诊详情页面
        const targetUrl = `/subpkg/profile/records/hospital-record-detail?id=${validRecordId}`
        console.log('🔗 跳转到就诊详情页面:', targetUrl)
        uni.navigateTo({
          url: targetUrl,
          success: () => {
            console.log('✅ 成功跳转到就诊详情页面')
          },
          fail: (err) => {
            console.error('❌ 跳转到就诊详情页面失败:', err)
            uni.showToast({
              title: '跳转失败，请稍后重试',
              icon: 'none'
            })
          }
        })
      } else if (res.confirm && !validRecordId) {
        console.warn('⚠️ 未找到预约记录ID，无法跳转')
        uni.showToast({
          title: '未找到预约记录',
          icon: 'none'
        })
      }
    }
  })
}

// 获取当前时段是否受限及原因（仅保留重复时段校验）
const getSlotLimitState = (doctor, schedule, slotKeyOverride) => {
  const doctorId = getDoctorIdFromDoctor(doctor)
  const slotKey = slotKeyOverride || schedule?.timeRangeKey || schedule?.slotKey
  if (slotKey && doctorId && hasBookedSameSlot(doctorId, selectedDate.value, slotKey)) {
    return { disabled: true, reason: '已预约该时段', type: 'slot' }
  }
  return { disabled: false, reason: '', type: '' }
}

const getSlotLimitStateByKey = (doctor, slotKey) => {
  const schedule = getSlotByTimeRange(doctor.id, slotKey)
  return getSlotLimitState(doctor, schedule, slotKey)
}

  // 点击科室时不再做锁定拦截，直接切换科室查看
  const onDeptClick = (dept) => {
    selectDepartment(dept)
  }

  // 点击号源时的统一处理（包含锁定判断）
  const onSlotClick = async (doctor, slot) => {
    // 点击前先刷新当日记录，确保限制判断使用最新数据
    await loadPatientRecords()
    const schedule = getSlotByTimeRange(doctor.id, slot.key)
    const limitState = getSlotLimitStateByKey(doctor, slot.key)

    // 规则锁定优先处理（仅保留重复时段提醒）
    if (limitState.disabled) {
      // 查找对应的已预约记录
      const slotKeyForSearch = slot.key
      const doctorIdNumForSearch = Number(doctor.doctorId || doctor.id)
      const duplicateRecord = patientRecords.value.find(r => {
        const recordDoctorId = Number(r.doctor_id || r.doctorId)
        const recordDateRaw = r.schedule_date || r.scheduleDate || r.register_time || r.registerTime || r.schedule_time || r.scheduleTime || ''
        const recordDateStr = normalizeDateOnly(recordDateRaw)
        const recordSlotKey = slotKeyFromTimeText(r.time_range || r.timeRange || r.time_slot || r.timeSlot || '')
        return recordDoctorId === doctorIdNumForSearch && 
               recordDateStr === normalizeDateOnly(selectedDate.value) &&
               recordSlotKey === slotKeyForSearch
      })
      const recordId = duplicateRecord?.record_id || duplicateRecord?.recordId || duplicateRecord?.id || duplicateRecord?.registration_id || duplicateRecord?.registrationId || duplicateRecord?.registrationRecordId
      
      console.log('🔍 时段重复检查 - doctorId:', doctorIdNumForSearch, 'slotKey:', slotKeyForSearch, '找到记录:', duplicateRecord, 'recordId:', recordId)
      
      showDuplicateAppointmentModal(recordId)
      return
    }

    // 已过预约时间，不可选
    if (schedule && schedule.expired) {
      uni.showToast({
        title: '该时段预约已截止',
        icon: 'none'
      })
      return
    }

    // 无排班不可选
    if (!schedule) {
      uni.showToast({
        title: '该时段暂无排班',
        icon: 'none'
      })
      return
    }

    // 有排班但已满 → 可以候补
    if (schedule.availableSlots === 0) {
      // 获取就诊人ID
      let currentPatientId = patientId.value
      if (!currentPatientId) {
        try {
          const patientInfo = await ensurePatientCard()
          if (patientInfo && patientInfo.patientId) {
            currentPatientId = Number(patientInfo.patientId)
            patientId.value = currentPatientId
          } else {
            uni.showModal({
              title: '就诊人信息缺失',
              content: '未找到就诊人信息，请先创建并绑定就诊卡',
              showCancel: false,
              success: () => {
                uni.navigateTo({
                  url: '/subpkg/profile/personal/create-card'
                })
              }
            })
            return
          }
        } catch (error) {
          console.error('获取就诊人信息失败:', error)
          uni.showModal({
            title: '就诊人信息缺失',
            content: '获取就诊人信息失败，请先创建并绑定就诊卡',
            showCancel: false,
            success: () => {
              uni.navigateTo({
                url: '/subpkg/profile/personal/create-card'
              })
            }
          })
          return
        }
      }

      // 确保挂号记录最新（防止跨页面后数据滞后）
      await loadPatientRecords()

      const scheduleIdForCheck = Number(schedule?.scheduleId ?? schedule?.id ?? schedule?.schedule_id)

      // 1️⃣ 优先检查：是否已预约过该号（同一排班）- 重复预约显示详细提示
      if (scheduleIdForCheck) {
        try {
          const duplicate = await checkDuplicateBySchedule(currentPatientId, scheduleIdForCheck)
          if (duplicate) {
            // 重新加载记录以确保数据最新
            await loadPatientRecords()
            
            // 查找对应的已预约记录（优先从当天记录中查找）
            let duplicateRecord = patientRecords.value.find(r => {
              const recordScheduleId = Number(r.schedule_id || r.scheduleId || r.schedule_id)
              return recordScheduleId === scheduleIdForCheck
            })
            
            // 如果当天记录中找不到，尝试从所有记录中查找（不限制日期）
            if (!duplicateRecord && currentPatientId) {
              try {
                const allRecords = await getRegistrationRecords(currentPatientId)
                if (Array.isArray(allRecords)) {
                  duplicateRecord = allRecords.find(r => {
                    const recordScheduleId = Number(r.schedule_id || r.scheduleId || r.schedule_id)
                    return recordScheduleId === scheduleIdForCheck
                  })
                }
              } catch (err) {
                console.warn('获取所有记录失败:', err)
              }
            }
            
            const recordId = duplicateRecord?.record_id || duplicateRecord?.recordId || duplicateRecord?.id || duplicateRecord?.registration_id || duplicateRecord?.registrationId || duplicateRecord?.registrationRecordId
            
            console.log('🔍 候补-重复预约检查 - scheduleId:', scheduleIdForCheck, '找到记录:', duplicateRecord, 'recordId:', recordId)
            
            uni.showModal({
              title: '预约已达上限',
              content: '您已预约过此号，不可重复预约。如需查看已预约的详情，请点击"查看详情"按钮。',
              showCancel: true,
              cancelText: '确定',
              confirmText: recordId ? '查看详情' : undefined,
              success: (res) => {
                if (res.confirm && recordId) {
                  // 跳转到就诊详情页面
                  uni.navigateTo({
                    url: `/subpkg/profile/records/hospital-record-detail?id=${recordId}`
                  })
                } else if (res.confirm && !recordId) {
                  uni.showToast({
                    title: '未找到预约记录',
                    icon: 'none'
                  })
                }
              }
            })
            return
          }
        } catch (error) {
          console.error('候补-检查重复挂号失败:', error)
          // 即使检查失败，也继续候补流程，但记录错误
        }
      }

      // 2️⃣ 检查是否达到当日总次数限制（一天预约三个号）
      const totalCountToday = getPatientTotalCountToday()
      console.log('📊 候补-当日总预约次数:', totalCountToday, '上限:', DAILY_TOTAL_LIMIT)
      if (totalCountToday >= DAILY_TOTAL_LIMIT) {
        uni.showToast({
          title: '超过挂号限制',
          icon: 'none'
        })
        return
      }

      // 3️⃣ 检查是否达到科室单日限制（同一科室一天预约两次）
      const deptIdForCount = getDeptIdFromContext(doctor, schedule)
      const deptCountToday = deptIdForCount ? getPatientDeptCountToday(deptIdForCount) : 0
      console.log('📊 候补-当日科室预约次数:', deptCountToday, '上限:', DAILY_DEPT_LIMIT, '科室ID:', deptIdForCount)
      
      if (deptIdForCount && deptCountToday >= DAILY_DEPT_LIMIT) {
        uni.showToast({
          title: '超过挂号限制',
          icon: 'none'
        })
        return
      }

      uni.showModal({
        title: '号源已满',
        content: '该时段已无可用号源，您可以选择加入候补队列',
        cancelText: '取消',
        confirmText: '加入候补',
        async success(res) {
          if (res.confirm) {
            // 检查是否有选中排班
            if (!schedule) {
              console.error("schedule 为空，无法获取排班 ID");
              uni.showToast({
                title: '候补失败：未找到排班',
                icon: 'none'
              });
              return;
            }

            try {
              const scheduleId = schedule.scheduleId ?? schedule.id ?? schedule.schedule_id
              if (!scheduleId) {
                uni.showToast({
                  title: '候补失败：未找到排班ID',
                  icon: 'none'
                });
                return;
              }

              const fee = 20; // 候补费用
              const typeId = schedule.typeId ?? schedule.type_id ?? 1

              // 构建挂号记录对象
              const record = {
                scheduleId,
                patientId: currentPatientId,
                doctorId: doctor.doctorId ?? doctor.id,
                typeId: typeId,
                registrationNo: generateRegistrationNo(doctor), // 前端生成或后端生成都可以
                registerTime: formatLocalDateTime(new Date()), // YYYY-MM-DD HH:mm:ss
                status: 0, // 候补
                priceOriginal: fee,
                actualPrice: fee,
                isAdd: 0 // 正常号
              };

              // 写入挂号记录
              const regRes = await createRegistration(record, currentPatientId, true);
              console.log('createRegistration返回值', regRes);

              // 判断接口返回值
              if ((typeof regRes === 'string' && regRes.includes('已加入候补队列')) || regRes?.success) {
                uni.showToast({
                  title: '已加入候补队列',
                  icon: 'success'
                });
                console.log('候补挂号写入成功', {
                  regRes
                });
                // 刷新排班信息
                await fetchDoctorSchedules(doctor.id, selectedDate.value)
                // 刷新患者记录
                await loadPatientRecords()
              } else {
                // 优先显示具体错误原因
                let errorMsg = '加入候补失败';
                if (typeof regRes === 'string') errorMsg = regRes;
                else if (regRes?.message) errorMsg = `挂号记录失败：${regRes.message}`;

                uni.showToast({
                  title: errorMsg,
                  icon: 'none'
                });
                console.warn('候补写入部分失败', {
                  regRes
                });
              }

            } catch (e) {
              console.error('加入候补异常', e);
              uni.showToast({
                title: e?.message || '加入候补失败，请稍后重试',
                icon: 'none'
              });
            }
          }
        }
      });

      return
    }

    navigateToPayment(doctor, schedule, slot.timeText)
  }

  // 加载患者当天的挂号记录
  const normalizeDateOnly = (val) => {
    if (!val) return ''
    const s = String(val).trim()
    // 如果已经是 yyyy-MM-dd 格式，直接返回
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
      return s
    }
    // 取前 10 位 yyyy-MM-dd
    if (s.length >= 10) {
      const datePart = s.substring(0, 10)
      // 验证格式是否正确
      if (/^\d{4}-\d{2}-\d{2}$/.test(datePart)) {
        return datePart
      }
    }
    // 尝试解析日期
    try {
      const date = new Date(s.replace(' ', 'T'))
      if (!isNaN(date.getTime())) {
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        return `${year}-${month}-${day}`
      }
    } catch (e) {
      console.warn('日期格式化失败:', s, e)
    }
    return s.substring(0, 10) || s
  }

  const loadPatientRecords = async () => {
    if (!patientId.value || !selectedDate.value) {
      patientRecords.value = []
      return
    }

    try {
      loadingRecords.value = true
      const response = await getRegistrationRecords(patientId.value)
      
      // 处理不同的响应格式
      let records = []
      if (Array.isArray(response)) {
        records = response
      } else if (Array.isArray(response?.result)) {
        records = response.result
      } else if (Array.isArray(response?.data)) {
        records = response.data
      } else {
        console.warn('获取挂号记录返回格式异常:', response)
        records = []
      }
      
      // 过滤出当天的记录（状态保持与后端一致：0/1/2/5/6 视为有效，过滤3/4取消类）
      const dateStr = normalizeDateOnly(selectedDate.value)
      const VALID_STATUS = [0, 1, 2, 5, 6]
      
      console.log('📋 开始过滤记录 - 日期:', dateStr, '总记录数:', records.length)
      
      patientRecords.value = records.filter(r => {
        const recordDate = r.schedule_date || r.scheduleDate || r.register_time || r.registerTime || r.schedule_time || r.scheduleTime
        if (!recordDate) {
          console.log('⚠️ 记录缺少日期字段:', r)
          return false
        }
        const recordDateStr = normalizeDateOnly(recordDate)
        const status = Number(r.status)
        const validStatus = isNaN(status) ? true : VALID_STATUS.includes(status)
        const matches = recordDateStr === dateStr && validStatus
        
        if (!matches && recordDateStr === dateStr) {
          console.log('⚠️ 记录日期匹配但状态无效:', { recordDateStr, status, validStatus, record: r })
        }
        
        return matches
      })
      
      console.log('📋 加载患者记录完成 - 日期:', dateStr, '过滤后记录数:', patientRecords.value.length, '总记录数:', records.length)
      
      // 如果过滤后记录数为 0，但总记录数不为 0，输出调试信息
      if (patientRecords.value.length === 0 && records.length > 0) {
        console.warn('⚠️ 过滤后记录数为 0，但总记录数不为 0，可能过滤条件过严')
        // 输出前几条记录的日期和状态，便于调试
        records.slice(0, 5).forEach((r, index) => {
          const recordDate = r.schedule_date || r.scheduleDate || r.register_time || r.registerTime || r.schedule_time || r.scheduleTime
          const recordDateStr = normalizeDateOnly(recordDate)
          const status = Number(r.status)
          console.log(`  记录 ${index + 1}: 日期=${recordDateStr}, 状态=${status}, 匹配日期=${recordDateStr === dateStr}`)
        })
      }
    } catch (error) {
      console.error('加载患者挂号记录失败:', error)
      patientRecords.value = []
      // 即使加载失败，也显示错误提示
      uni.showToast({
        title: '加载预约记录失败',
        icon: 'none',
        duration: 2000
      })
    } finally {
      loadingRecords.value = false
    }
  }

  // 检查是否已预约过该医生当天的号
  const hasBookedSameDoctorToday = (doctorId) => {
    if (!patientRecords.value.length) return false
    
    const doctorIdNum = Number(doctorId)
    return patientRecords.value.some(r => {
      const recordDoctorId = Number(r.doctor_id || r.doctorId)
      return recordDoctorId === doctorIdNum
    })
  }

  // 是否已预约相同医生同一天同时间段
  const hasBookedSameSlot = (doctorId, dateStr, slotKey) => {
    if (!patientRecords.value.length) return false
    const targetDoctorId = Number(doctorId)
    const dateOnly = normalizeDateOnly(dateStr)
    return patientRecords.value.some(r => {
      const recordDoctorId = Number(r.doctor_id || r.doctorId)
      const recordDateRaw = r.schedule_date || r.scheduleDate || r.register_time || r.registerTime || r.schedule_time || r.scheduleTime || ''
      const recordDate = normalizeDateOnly(recordDateRaw)
      const timeText = (r.timeRange || r.time_range || r.schedule_time_range || r.scheduleTimeRange || r.register_time || r.registerTime || r.schedule_time || r.scheduleTime || '').toString()
      const matchSlot =
        slotKey === '上午'
          ? timeText.includes('08:') || timeText.includes('上午')
          : slotKey === '下午'
          ? timeText.includes('14:') || timeText.includes('下午')
          : slotKey === '晚上'
          ? timeText.includes('18:') || timeText.includes('晚上')
          : false
      return recordDoctorId === targetDoctorId && recordDate === dateOnly && matchSlot
    })
  }

  // 获取医生的号别类型名称（从排班数据中提取）
  const getDoctorTypeName = (doctorOrId) => {
    const doctor = typeof doctorOrId === 'object' ? doctorOrId : (doctors.value || []).find(d => d.id === doctorOrId || d.doctorId === doctorOrId)
    const doctorId = typeof doctorOrId === 'object' ? (doctorOrId.id || doctorOrId.doctorId) : doctorOrId
    const schedules = doctorSchedules.value[doctorId] || [];
    if (schedules.length === 0) {
      const titleId = doctor?.titleId ?? doctor?.title_id ?? doctor?.doctorTitleTypeId ?? doctor?.doctor_title_type_id
      return titleId != null ? (registrationTypeNameMap.value[String(titleId)] || '') : '';
    }
    
    // 从排班数据中提取所有不同的号别类型（严格使用职称映射字段）
    const typeNames = schedules
      .map(schedule => schedule.doctorTitleTypeName || schedule.doctor_title_type_name)
      .filter(typeName => typeName && typeName.trim() !== '')
      .filter((value, index, self) => self.indexOf(value) === index); // 去重
    
    // 如果有多个号别类型，用斜杠分隔显示；如果只有一个，直接显示
    if (typeNames.length > 0) return typeNames.join(' / ');
    const titleId = doctor?.titleId ?? doctor?.title_id ?? doctor?.doctorTitleTypeId ?? doctor?.doctor_title_type_id
    return titleId != null ? (registrationTypeNameMap.value[String(titleId)] || '') : '';
  };

  // 跳转到支付页面
  const navigateToPayment = async (doctor, schedule, timeText) => {
    if (!doctor || !schedule) {
      return;
    }
    
    // 检查是否已过预约时间
    if (schedule.expired) {
      uni.showToast({
        title: '该时段预约已截止',
        icon: 'none'
      })
      return
    }
    
    // 检查是否有可用号源
    if (!schedule.availableSlots || schedule.availableSlots <= 0) {
      uni.showToast({
        title: '该时段号源已满',
        icon: 'none'
      })
      return
    }

    // 获取就诊人ID
    let currentPatientId = patientId.value
    if (!currentPatientId) {
      try {
        const patientInfo = await ensurePatientCard()
        if (patientInfo && patientInfo.patientId) {
          currentPatientId = Number(patientInfo.patientId)
          patientId.value = currentPatientId
          console.log('获取到就诊人ID:', currentPatientId)
          // 加载患者记录
          await loadPatientRecords()
        } else {
          uni.showModal({
            title: '就诊人信息缺失',
            content: '未找到就诊人信息，请先创建并绑定就诊卡',
            showCancel: false,
            success: () => {
              uni.navigateTo({
                url: '/subpkg/profile/personal/create-card'
              })
            }
          })
          return
        }
      } catch (error) {
        console.error('获取就诊人信息失败:', error)
        uni.showModal({
          title: '就诊人信息缺失',
          content: '获取就诊人信息失败，请先创建并绑定就诊卡',
          showCancel: false,
          success: () => {
            uni.navigateTo({
              url: '/subpkg/profile/personal/create-card'
            })
          }
        })
        return
      }
    }

    // 确保挂号记录最新（防止跨页面后数据滞后）
    try {
      await loadPatientRecords()
    } catch (error) {
      console.error('加载患者记录失败:', error)
      // 即使加载失败，也继续检查，但记录错误
    }

    const scheduleIdForCheck = Number(schedule?.scheduleId ?? schedule?.id ?? schedule?.schedule_id)
    
    // 1️⃣ 检查是否已预约过该号（同一排班）- 重复预约显示详细提示
    if (scheduleIdForCheck) {
      try {
        const duplicate = await checkDuplicateBySchedule(currentPatientId, scheduleIdForCheck)
        if (duplicate) {
          // 重新加载记录以确保数据最新
          try {
            await loadPatientRecords()
          } catch (error) {
            console.error('重新加载患者记录失败:', error)
          }
          
          // 查找对应的已预约记录（优先从当天记录中查找）
          let duplicateRecord = patientRecords.value.find(r => {
            const recordScheduleId = Number(r.schedule_id || r.scheduleId || r.schedule_id)
            return recordScheduleId === scheduleIdForCheck
          })
          
          // 如果当天记录中找不到，尝试从所有记录中查找（不限制日期）
          if (!duplicateRecord && currentPatientId) {
            try {
              const allRecords = await getRegistrationRecords(currentPatientId)
              if (Array.isArray(allRecords)) {
                duplicateRecord = allRecords.find(r => {
                  const recordScheduleId = Number(r.schedule_id || r.scheduleId || r.schedule_id)
                  return recordScheduleId === scheduleIdForCheck
                })
              }
            } catch (err) {
              console.warn('获取所有记录失败:', err)
            }
          }
          
          const recordId = duplicateRecord?.record_id || duplicateRecord?.recordId || duplicateRecord?.id || duplicateRecord?.registration_id || duplicateRecord?.registrationId || duplicateRecord?.registrationRecordId
          
          console.log('🔍 重复预约检查 - scheduleId:', scheduleIdForCheck, '找到记录:', duplicateRecord, 'recordId:', recordId)
          
          uni.showModal({
            title: '预约已达上限',
            content: '您已预约过此号，不可重复预约。如需查看已预约的详情，请点击"查看详情"按钮。',
            showCancel: true,
            cancelText: '确定',
            confirmText: recordId ? '查看详情' : undefined,
            success: (res) => {
              if (res.confirm && recordId) {
                // 跳转到就诊详情页面
                uni.navigateTo({
                  url: `/subpkg/profile/records/hospital-record-detail?id=${recordId}`
                })
              } else if (res.confirm && !recordId) {
                uni.showToast({
                  title: '未找到预约记录',
                  icon: 'none'
                })
              }
            }
          })
          return
        }
      } catch (error) {
        console.error('检查重复挂号失败:', error)
        uni.showToast({
          title: '超过挂号限制',
          icon: 'none'
        })
        return
      }
    }

    // 2️⃣ 检查是否达到当日总次数限制（一天预约三个号）
    try {
      await loadPatientRecords()
    } catch (error) {
      console.error('检查前加载患者记录失败:', error)
    }
    
    const totalCountToday = getPatientTotalCountToday()
    console.log('📊 当日总预约次数:', totalCountToday, '上限:', DAILY_TOTAL_LIMIT)
    
    if (totalCountToday >= DAILY_TOTAL_LIMIT) {
      uni.showToast({
        title: '超过挂号限制',
        icon: 'none'
      })
      return
    }

    // 3️⃣ 检查是否达到科室单日限制（同一科室一天预约两次）
    const deptIdForCount = getDeptIdFromContext(doctor, schedule)
    const deptCountToday = deptIdForCount ? getPatientDeptCountToday(deptIdForCount) : 0
    console.log('📊 当日科室预约次数:', deptCountToday, '上限:', DAILY_DEPT_LIMIT, '科室ID:', deptIdForCount)
    
    if (deptIdForCount && deptCountToday >= DAILY_DEPT_LIMIT) {
      uni.showToast({
        title: '超过挂号限制',
        icon: 'none'
      })
      return
    }
    
    // 4️⃣ 后端校验科室限制（更准确）
    if (deptIdForCount && scheduleIdForCheck) {
      try {
        const reachedDeptLimit = await checkDeptLimitBySchedule(currentPatientId, scheduleIdForCheck)
        if (reachedDeptLimit) {
          uni.showToast({
            title: '超过挂号限制',
            icon: 'none'
          })
          return
        }
      } catch (error) {
        console.warn('检查科室单日限约失败', error)
      }
    }

    // 4️⃣ 强化重复预约校验（同患者/同医生/同日/同时间段不可重复）- 重复预约显示详细提示
    const slotKeyCandidate =
      schedule?.timeRangeKey ||
      schedule?.slotKey ||
      slotKeyFromTimeText(timeText) ||
      schedule?.timeRange
    const doctorIdNumForRepeat = Number(doctor.doctorId || doctor.id)
    if (slotKeyCandidate && doctorIdNumForRepeat && hasBookedSameSlot(doctorIdNumForRepeat, selectedDate.value, slotKeyCandidate)) {
      // 重新加载记录以确保数据最新
      await loadPatientRecords()
      
      // 查找对应的已预约记录（优先从当天记录中查找）
      let duplicateRecord = patientRecords.value.find(r => {
        const recordDoctorId = Number(r.doctor_id || r.doctorId)
        const recordDateRaw = r.schedule_date || r.scheduleDate || r.register_time || r.registerTime || r.schedule_time || r.scheduleTime || ''
        const recordDateStr = normalizeDateOnly(recordDateRaw)
        const recordSlotKey = slotKeyFromTimeText(r.time_range || r.timeRange || r.time_slot || r.timeSlot || '')
        return recordDoctorId === doctorIdNumForRepeat && 
               recordDateStr === normalizeDateOnly(selectedDate.value) &&
               recordSlotKey === slotKeyCandidate
      })
      
      // 如果当天记录中找不到，尝试从所有记录中查找（不限制日期）
      if (!duplicateRecord && currentPatientId) {
        try {
          const allRecords = await getRegistrationRecords(currentPatientId)
          if (Array.isArray(allRecords)) {
            duplicateRecord = allRecords.find(r => {
              const recordDoctorId = Number(r.doctor_id || r.doctorId)
              const recordDateRaw = r.schedule_date || r.scheduleDate || r.register_time || r.registerTime || r.schedule_time || r.scheduleTime || ''
              const recordDateStr = normalizeDateOnly(recordDateRaw)
              const recordSlotKey = slotKeyFromTimeText(r.time_range || r.timeRange || r.time_slot || r.timeSlot || '')
              return recordDoctorId === doctorIdNumForRepeat && 
                     recordDateStr === normalizeDateOnly(selectedDate.value) &&
                     recordSlotKey === slotKeyCandidate
            })
          }
        } catch (err) {
          console.warn('获取所有记录失败:', err)
        }
      }
      
      const recordId = duplicateRecord?.record_id || duplicateRecord?.recordId || duplicateRecord?.id || duplicateRecord?.registration_id || duplicateRecord?.registrationId || duplicateRecord?.registrationRecordId
      
      console.log('🔍 时间段重复检查 - doctorId:', doctorIdNumForRepeat, 'slotKey:', slotKeyCandidate, '找到记录:', duplicateRecord, 'recordId:', recordId)
      
      uni.showModal({
        title: '预约已达上限',
        content: '您已预约过此号，不可重复预约。如需查看已预约的详情，请点击"查看详情"按钮。',
        showCancel: true,
        cancelText: '确定',
        confirmText: recordId ? '查看详情' : undefined,
        success: (res) => {
          if (res.confirm && recordId) {
            // 跳转到就诊详情页面
            uni.navigateTo({
              url: `/subpkg/profile/records/hospital-record-detail?id=${recordId}`
            })
          } else if (res.confirm && !recordId) {
            uni.showToast({
              title: '未找到预约记录',
              icon: 'none'
            })
          }
        }
      })
      return
    }

    // 5️⃣ 二次校验：按钮级拦截 - 重复预约显示详细提示
    const latestLimit = getSlotLimitStateByKey(doctor, slotKeyFromTimeText(timeText))
    if (latestLimit.disabled) {
      // 重新加载记录以确保数据最新
      await loadPatientRecords()
      
      // 查找对应的已预约记录（优先从当天记录中查找）
      const slotKeyForSearch = slotKeyFromTimeText(timeText)
      const doctorIdNumForSearch = Number(doctor.doctorId || doctor.id)
      let duplicateRecord = patientRecords.value.find(r => {
        const recordDoctorId = Number(r.doctor_id || r.doctorId)
        const recordDateRaw = r.schedule_date || r.scheduleDate || r.register_time || r.registerTime || r.schedule_time || r.scheduleTime || ''
        const recordDateStr = normalizeDateOnly(recordDateRaw)
        const recordSlotKey = slotKeyFromTimeText(r.time_range || r.timeRange || r.time_slot || r.timeSlot || '')
        return recordDoctorId === doctorIdNumForSearch && 
               recordDateStr === normalizeDateOnly(selectedDate.value) &&
               recordSlotKey === slotKeyForSearch
      })
      
      // 如果当天记录中找不到，尝试从所有记录中查找（不限制日期）
      if (!duplicateRecord && currentPatientId) {
        try {
          const allRecords = await getRegistrationRecords(currentPatientId)
          if (Array.isArray(allRecords)) {
            duplicateRecord = allRecords.find(r => {
              const recordDoctorId = Number(r.doctor_id || r.doctorId)
              const recordDateRaw = r.schedule_date || r.scheduleDate || r.register_time || r.registerTime || r.schedule_time || r.scheduleTime || ''
              const recordDateStr = normalizeDateOnly(recordDateRaw)
              const recordSlotKey = slotKeyFromTimeText(r.time_range || r.timeRange || r.time_slot || r.timeSlot || '')
              return recordDoctorId === doctorIdNumForSearch && 
                     recordDateStr === normalizeDateOnly(selectedDate.value) &&
                     recordSlotKey === slotKeyForSearch
            })
          }
        } catch (err) {
          console.warn('获取所有记录失败:', err)
        }
      }
      
      const recordId = duplicateRecord?.record_id || duplicateRecord?.recordId || duplicateRecord?.id || duplicateRecord?.registration_id || duplicateRecord?.registrationId || duplicateRecord?.registrationRecordId
      
      console.log('🔍 按钮级拦截检查 - doctorId:', doctorIdNumForSearch, 'slotKey:', slotKeyForSearch, '找到记录:', duplicateRecord, 'recordId:', recordId)
      
      uni.showModal({
        title: '预约已达上限',
        content: '您已预约过此号，不可重复预约。如需查看已预约的详情，请点击"查看详情"按钮。',
        showCancel: true,
        cancelText: '确定',
        confirmText: recordId ? '查看详情' : undefined,
        success: (res) => {
          if (res.confirm && recordId) {
            // 跳转到就诊详情页面
            uni.navigateTo({
              url: `/subpkg/profile/records/hospital-record-detail?id=${recordId}`
            })
          } else if (res.confirm && !recordId) {
            uni.showToast({
              title: '未找到预约记录',
              icon: 'none'
            })
          }
        }
      })
      return
    }

    // 6️⃣ 检查是否已预约过该医生当天的号
    const doctorIdNum = Number(doctor.doctorId || doctor.id)
    if (getPatientDoctorCountToday(doctorIdNum) >= DAILY_DOCTOR_LIMIT) {
      uni.showToast({
        title: '超过挂号限制',
        icon: 'none'
      })
      return
    }

    // 正常情况：直接跳转支付页
    await proceedToPayment(doctor, schedule, timeText, currentPatientId)
  }

  // 执行跳转到支付页面的逻辑
  const proceedToPayment = async (doctor, schedule, timeText, currentPatientId) => {
    
    // 构建跳转参数
    const scheduleId = schedule?.scheduleId ?? schedule?.id ?? schedule?.schedule_id ?? 0
    const typeId = schedule?.typeId ?? schedule?.type_id ?? schedule?.type ?? 1

    // 获取科室名称，优先使用 selectedDepartment，其次使用 currentDepartment
    const deptName = selectedDepartment.value?.deptName || currentDepartment.value?.deptName || '未知科室'
    const deptIdValue = selectedDepartment.value?.deptId || selectedDepartment.value?.id || selectedDeptId.value || 0

    console.log('跳转支付页 - 科室信息:', {
      selectedDepartment: selectedDepartment.value,
      currentDepartment: currentDepartment.value,
      deptName,
      deptIdValue,
      patientId: currentPatientId
    })

    const params = {
      dept: encodeURIComponent(deptName),
      doctor: encodeURIComponent(doctor.name || doctor.doctorName || '未知医生'),
      time: encodeURIComponent(`${selectedDate.value} ${timeText}`),
      doctorId: doctor.doctorId || doctor.id || 0,
      scheduleId,
      typeId,
      deptId: deptIdValue,
      patientId: currentPatientId
    };
    
    // 跳转到支付页面
    uni.navigateTo({
      url: `/subpkg/hospital/payment?${Object.keys(params)
        .map(key => `${key}=${params[key]}`)
        .join('&')}`
    });
  };

  // 获取医生排班信息
  const fetchDoctorSchedules = async (doctorId, date) => {
    // 确保doctorId为有效数字，且必须存在
    const validDoctorId = doctorId ? Number(doctorId) : null;
    
    try {
      if (!validDoctorId || isNaN(validDoctorId)) {
        console.error('无效的医生ID:', doctorId);
        // 避免在doctorId无效时访问doctorSchedules
        return;
      }
      
      // 显示加载提示
      uni.showLoading({
        title: '加载排班信息中...',
        mask: true
      });
      
      // 初始化当前医生的排班数据
      doctorSchedules.value[validDoctorId] = [];
      
      console.log('正在获取医生排班，医生ID:', validDoctorId, '日期:', date);
      
      // 使用registration API中的getDoctorSchedules方法获取排班信息
      const response = await getDoctorSchedules(validDoctorId, date, 1);
      console.log('排班API响应:', response);
      
      // 提取数组数据
      let schedules = [];
      if (Array.isArray(response?.result)) schedules = response.result;
      else if (Array.isArray(response?.data)) schedules = response.data;
      else if (Array.isArray(response)) schedules = response;
      
      // 转换排班数据格式，根据数据库表结构映射字段
      doctorSchedules.value[validDoctorId] = schedules
        .filter(schedule => {
          // 只保留当前日期的数据
          const scheduleDate = schedule.schedule_date || schedule.scheduleDate;
          return scheduleDate?.substring(0, 10) === date;
        })
        .map(schedule => {
          // 重新获取scheduleDate，避免作用域问题
          const scheduleDate = schedule.schedule_date || schedule.scheduleDate;

          // 解析排班与类型编号
          const resolvedScheduleId = schedule.schedule_id ?? schedule.scheduleId ?? schedule.id ?? `${validDoctorId}-${date}`;
          const resolvedTypeId = schedule.type_id ?? schedule.typeId ?? schedule.registration_type_id ?? schedule.registrationTypeId ?? schedule.type?.id ?? null;

          // 将time_slot转换为文本形式
          let timeRangeText = '';
          switch (Number(schedule.time_slot ?? schedule.timeSlot)) {
            case 1:
              timeRangeText = '上午';
              break;
            case 2:
              timeRangeText = '下午';
              break;
            case 3:
              timeRangeText = '晚上';
              break;
            default:
              timeRangeText = '全天';
          }

          // 计算剩余号源
          const maxQuota = Number(schedule.max_quota || schedule.maxQuota || schedule.totalQuota || 0);
          const usedQuota = Number(schedule.used_quota || schedule.usedQuota || 0);
          const availableSlots = maxQuota - usedQuota;

          // 判断是否已过预约时间（就诊前2小时截止）
          let expired = false
          if (scheduleDate) {
            const scheduleDateObj = new Date(scheduleDate)
            const now = new Date()
            const timeSlot = Number(schedule.time_slot ?? schedule.timeSlot)
            
            // 确定时段的开始时间
            let slotStartHour = 8 // 默认上午
            if (timeSlot === 2) slotStartHour = 14 // 下午
            else if (timeSlot === 3) slotStartHour = 18 // 晚上
            
            // 构建时段开始时间
            const slotStartTime = new Date(scheduleDateObj)
            slotStartTime.setHours(slotStartHour, 0, 0, 0)
            
            // 如果当前时间 >= 时段开始时间 - 2小时，则已过期
            const cutoffTime = new Date(slotStartTime.getTime() - 2 * 60 * 60 * 1000)
            expired = now >= cutoffTime
          }

          return {
            id: resolvedScheduleId,
            scheduleId: resolvedScheduleId,
            date: scheduleDate || date,
            timeRange: timeRangeText,
            typeId: resolvedTypeId != null ? Number(resolvedTypeId) : null,
            typeName: schedule.type_name || schedule.typeName || schedule.type?.name || null,
            doctorTitleTypeName: schedule.doctor_title_type_name || schedule.doctorTitleTypeName || null,
            fee: Number(schedule.price || schedule.fee || 50),
            availableSlots,
            expired, // 是否已过预约时间
            roomNo: schedule.room_number || schedule.roomNumber || '诊室1',
            totalSlots: maxQuota
          };
        });
      
      if (!doctorSchedules.value[validDoctorId].length) {
        console.log('该医生当日暂无排班');
      }
    } catch (error) {
      console.error('获取医生排班失败:', error);
      // 清空排班信息，确保不使用模拟数据
      if (validDoctorId) {
        doctorSchedules.value[validDoctorId] = [];
      }
    } finally {
      uni.hideLoading();
    }
  }
  
  // 搜索结果
const searchResults = computed(() => {
  if (!searchKeyword.value) return []
  
  const keyword = searchKeyword.value.toLowerCase()
  const results = []
  
  // 搜索科室树形结构
  departmentTree.value.forEach(dept => {
    // 搜索一级科室
    if (dept.deptName.toLowerCase().includes(keyword) || 
        (dept.deptDesc && dept.deptDesc.toLowerCase().includes(keyword))) {
      results.push(dept)
    }
    
    // 搜索二级科室
    if (dept.children) {
      dept.children.forEach(child => {
        if (child.deptName.toLowerCase().includes(keyword) || 
            (child.deptDesc && child.deptDesc.toLowerCase().includes(keyword))) {
          results.push(child)
        }
      })
    }
  })
  
  return results
})

// 搜索
const onSearch = () => {
  // 搜索逻辑已在computed中实现
}

// 切换科室展开/收起状态
const toggleDepartment = (department) => {
  // 只有有子科室的科室才能展开/收起
  if (department.children && department.children.length > 0) {
    // 切换展开/收起状态
    department.expanded = !department.expanded
    
    // 如果展开，取消其他一级科室的选中状态
    if (department.expanded) {
      departmentTree.value.forEach(dept => {
        if (dept !== department && dept.expanded) {
          dept.expanded = false
        }
      })
    }
  } else if (department.deptLevel === 2) {
    // 选择二级科室时，直接选择该科室
    selectDepartment(department)
  }
}

// 根据科室ID获取医生列表的函数已移至下方定义

// 获取医生排班信息的函数已移至上方定义

// 选择科室
const selectDepartment = async (dept) => {
  // 取消之前的选中状态
  clearSelection(departmentTree.value)
  
  // 设置当前科室为选中状态
  dept.selected = true
  
  // 记录选中的科室信息
  selectedDepartment.value = dept
  selectedDeptId.value = dept.deptId || dept.id
  
  // 显示科室信息
  currentDepartment.value = {
    deptName: dept.deptName,
    deptDesc: dept.deptDesc || '暂无描述'
  }
  
  // 重置搜索关键词
  searchKeyword.value = ''
  
  console.log('选中科室:', dept)
  
  // 确保有选中的日期
  if (!selectedDate.value && dateList.value.length > 0) {
    selectedDate.value = dateList.value[0].date
  }
  
  // 获取该科室的医生列表
  await fetchDoctorsByDeptId(dept.deptId || dept.id)
  
  // 确保医生数据加载完成后再刷新UI
  await nextTick()
}

// 清除所有选中状态
const clearSelection = (departments) => {
  if (!departments || !Array.isArray(departments)) return
  
  departments.forEach(dept => {
    dept.selected = false
    
    if (dept.children) {
      clearSelection(dept.children)
    }
  })
}

// 加载就诊人列表
const loadPatientList = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) {
    return
  }

  try {
    const data = await patientApi.getPatientList({ userId })
    let list = []
    
    if (Array.isArray(data)) {
      list = data
    } else if (data && Array.isArray(data.list)) {
      list = data.list
    } else if (data && Array.isArray(data.data)) {
      list = data.data
    }
    
    patientList.value = list
    
    // 如果有就诊人列表，设置默认选择第一个
    if (list.length > 0) {
      currentPatient.value = list[0]
      patientId.value = Number(list[0].patientId)
      selectedPatientIndex.value = 0
      await loadPatientRecords()
    } else {
      // 如果没有就诊人，尝试从ensurePatientCard获取
      const patientInfo = await ensurePatientCard()
      if (patientInfo && patientInfo.patientId) {
        patientId.value = Number(patientInfo.patientId)
        currentPatient.value = patientInfo
        await loadPatientRecords()
      }
    }
  } catch (error) {
    console.warn('获取就诊人列表失败:', error)
    // 如果获取失败，尝试从ensurePatientCard获取
    try {
      const patientInfo = await ensurePatientCard()
      if (patientInfo && patientInfo.patientId) {
        patientId.value = Number(patientInfo.patientId)
        currentPatient.value = patientInfo
        await loadPatientRecords()
      }
    } catch (e) {
      console.warn('从ensurePatientCard获取患者信息失败:', e)
    }
  }
}

// 就诊人选择变化
const onPatientChange = async (e) => {
  const index = e.detail.value
  selectedPatientIndex.value = index
  currentPatient.value = patientList.value[index]
  patientId.value = Number(currentPatient.value.patientId)
  
  // 重新加载患者当天的挂号记录
  await loadPatientRecords()
  
  // 如果已选择科室，重新检查排班可用性
  if (selectedDepartment.value && doctors.value.length > 0) {
    for (const doctor of doctors.value) {
      await fetchDoctorSchedules(doctor.id, selectedDate.value)
    }
  }
}

// 初始化患者信息
const initPatientInfo = async () => {
  await loadPatientList()
}

// 刷新预约数据（参考转诊/就诊记录刷新按钮风格与体验）
const refreshBookingData = async () => {
  try {
    uni.showLoading({ title: '刷新中...', mask: true })
    await Promise.all([initPatientInfo(), loadPatientRecords()])
    await initDepartmentTree()

    // 如果此前选中过科室，刷新后尝试保持选中并加载医生/排班
    if (selectedDeptId.value) {
      const target = findDepartment(departmentTree.value, selectedDeptId.value)
      if (target) {
        await selectDepartment(target)
        if (selectedDate.value && doctors.value.length > 0) {
          for (const doctor of doctors.value) {
            await fetchDoctorSchedules(doctor.id, selectedDate.value)
          }
        }
      }
    }
  } catch (error) {
    uni.showToast({
      title: error?.message || '刷新失败，请稍后重试',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 在组件挂载后设置默认状态
onMounted(async () => {
  initDateList()
  initDepartmentTree()
  loadRegistrationTypeNameMap()
  await initPatientInfo()
})

// 页面重新展示时，刷新当日记录与排班可用性，避免已有预约未锁定
onShow(async () => {
  if (patientId.value && selectedDate.value) {
    await loadPatientRecords()
  }
  if (selectedDepartment.value && doctors.value.length > 0 && selectedDate.value) {
    for (const doctor of doctors.value) {
      await fetchDoctorSchedules(doctor.id, selectedDate.value)
    }
  }
})

// 递归查找科室
const findDepartment = (departments, deptId) => {
  for (let dept of departments) {
    if (dept.deptId === deptId || dept.id === deptId) {
      return dept
    }
    if (dept.children && dept.children.length) {
      const found = findDepartment(dept.children, deptId)
      if (found) return found
    }
  }
  return null
}

// 修复重复定义，保留原始的fetchDoctorSchedules函数，删除冲突的函数

// 初始化科室树形结构
const initDepartmentTree = async () => {
  try {
    // 显示加载提示
    uni.showLoading({
      title: '加载科室信息中...',
      mask: true
    })
    
    // 调用API获取科室树形结构
    const response = await getDepartmentTree()
    console.log('科室树形结构响应:', response)
    
    // 处理不同的响应格式，使其更加健壮
    let data = response
    if (response && response.data) {
      data = response.data
    } else if (response && response.result) {
      data = response.result
    }
    
    // 确保数据是数组格式
    const treeData = Array.isArray(data) ? data : [data]
    
    // 转换科室数据格式，确保必要字段存在
    // 默认收起所有科室
    departmentTree.value = treeData.map(dept => ({
      deptId: dept.deptId || dept.id || '',
      deptName: dept.deptName || dept.name || '未知科室',
      deptDesc: dept.deptDesc || '',
      deptLevel: dept.deptLevel || 1,
      children: dept.children || dept.subDepartments || [],
      selected: false,
      expanded: false // 默认收起所有科室
    }))
    
    console.log('科室树形结构加载成功:', departmentTree.value)
  } catch (error) {
    console.error('获取科室树形结构失败:', error)
    // 清空科室树形结构，确保不使用模拟数据
    departmentTree.value = []
    
    // 显示错误提示
    uni.showToast({
      title: error.message || '科室数据加载失败',
      icon: 'none'
    })
  } finally {
    // 隐藏加载提示
    uni.hideLoading()
  }
}

// 前往按疾病查找
const goToDiseaseGuide = () => {
  uni.navigateTo({
    url: '/subpkg/hospital/disease-guide'
  })
}

// 电话咨询
const callConsult = () => {
  uni.showModal({
    title: '导诊咨询',
    content: '如不确定挂哪个科室，可拨打导诊电话咨询',
    confirmText: '拨打电话',
    success: (res) => {
      if (res.confirm) {
        uni.makePhoneCall({
          phoneNumber: '010-51682525'
        })
      }
    }
  })
}

// 工具函数：生成挂号单号（前端简单示例）
function generateRegistrationNo(doctor) {
  const date = new Date()
  const y = date.getFullYear()
  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  const d = date.getDate().toString().padStart(2, '0')
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
  const doctorId = doctor?.doctorId ?? doctor?.id ?? 0
  return `${y}${m}${d}${doctorId}${random}`
}

// 工具函数：格式化时间 YYYY-MM-DD HH:mm:ss
function formatLocalDateTime(date) {
  const pad = (n) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

</script>

<style scoped>
/* 全局样式重置和统一 */
.department-booking-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #f0f3f6 100%);
  display: flex;
  flex-direction: column;
  font-size: 28rpx;
  color: #333333;
}

/* 顶部标题栏 */
.page-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.2);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 24rpx;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
  flex: 1;
  text-align: center;
}

.header-right {
  position: absolute;
  right: 20rpx;
  top: 0;
  height: 100%;
  display: flex;
  align-items: center;
}

.refresh-btn.small {
  padding: 8rpx 24rpx;
  height: 52rpx;
  line-height: 36rpx;
  background-color: rgba(255, 255, 255, 0.25);
  color: #fff;
  border: 1rpx solid rgba(255, 255, 255, 0.4);
  border-radius: 26rpx;
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.08);
  margin: 0;
  transition: all 0.3s ease;
}

.refresh-btn.small:active {
  background-color: rgba(255, 255, 255, 0.35);
}

.refresh-btn.small:disabled {
  opacity: 0.5;
}

.refresh-btn .refresh-icon {
  display: inline-block;
  transition: transform 0.3s ease;
  font-size: 32rpx;
}

.refresh-btn:active .refresh-icon {
  animation: rotate 0.5s linear;
}

.refresh-text {
  font-size: 26rpx;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 小的就诊人选择器 */
.patient-selector-small {
  z-index: 101;
}

.patient-selector-small.inline {
  justify-self: flex-start;
  width: 100%;
}

.picker-small {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 12rpx 20rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f3f6fb 100%);
  border-radius: 22rpx;
  backdrop-filter: blur(14rpx);
  border: 1rpx solid rgba(0, 0, 0, 0.04);
  transition: all 0.24s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 0;
  width: 100%;
  max-width: 100%;
  min-height: 76rpx;
  box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.05), inset 0 1rpx 0 rgba(255, 255, 255, 0.20);
  position: relative;
  overflow: hidden;
}

.picker-small::before {
  content: '';
  position: absolute;
  top: 0;
  left: -120%;
  width: 120%;
  height: 100%;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.45) 50%, transparent 100%);
  transition: left 0.6s ease;
}

.picker-small:active {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.45), rgba(255, 255, 255, 0.26));
  transform: scale(0.985);
  box-shadow: 0 6rpx 14rpx rgba(0, 0, 0, 0.18);
}

.picker-small:active::before {
  left: 100%;
}

.picker-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex: 1;
  min-width: 0;
}

.picker-avatar {
  width: 52rpx;
  height: 52rpx;
  border-radius: 26rpx;
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22rpx;
  font-weight: 700;
  box-shadow: 0 3rpx 6rpx rgba(74, 144, 226, 0.18);
}

.picker-text {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
  flex: 1;
}

.picker-label {
  font-size: 15rpx;
  color: rgba(255, 255, 255, 0.78);
  letter-spacing: 0.6rpx;
  margin-bottom: 2rpx;
}

.picker-name {
  font-size: 24rpx;
  color: #222222;
  font-weight: 700;
  white-space: normal; /* 允许换行显示完整姓名 */
  overflow: visible;
  text-overflow: unset;
  max-width: none;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.08);
  letter-spacing: 0.5rpx;
  line-height: 1.35;
}

.picker-name.placeholder {
  color: rgba(0, 0, 0, 0.45);
  font-weight: 600;
}

.picker-right {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.picker-arrow {
  font-size: 18rpx;
  color: #222222;
  padding-left: 4rpx;
}

/* 日期选择器容器 - 移至顶部 */
.date-selection {
  margin-top: 88rpx;
  padding: 0;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafb 100%);
  position: sticky;
  top: 88rpx;
  z-index: 99;
  border-bottom: 1rpx solid #e0e6ed;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  overflow-x: auto;
  white-space: nowrap;
}

.date-scroll {
  padding: 8rpx 0;
  display: flex;
  width: 100%;
}

.date-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 20rpx;
  margin: 0 4rpx;
  text-align: center;
  border-radius: 16rpx;
  flex: 1;
  min-width: 120rpx;
  transition: all 0.3s ease;
  background: transparent;
  position: relative;
}

.date-item.active {
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  color: #ffffff;
  transform: scale(1.05);
  box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.3);
}

.date-item:active {
  transform: scale(1.02);
}

.date-week {
  font-size: 24rpx;
  margin-bottom: 4rpx;
  text-align: center;
  transition: all 0.3s ease;
  opacity: 0.8;
}

.date-item.active .date-week {
  opacity: 1;
  font-weight: 600;
}

.date-day {
  font-size: 30rpx;
  font-weight: 600;
  text-align: center;
  transition: all 0.3s ease;
}

.date-item.active .date-day,
.date-item:active .date-day {
  font-size: 32rpx;
  font-weight: 700;
}

.date-tag {
  font-size: 20rpx;
  margin-top: 4rpx;
  padding: 2rpx 8rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 8rpx;
}

.patient-inline-row {
  display: flex;
  align-items: center;
  padding: 12rpx 18rpx;
  background: linear-gradient(180deg, #ffffff 0%, #f4f7fb 100%);
  position: sticky;
  top: 146rpx; /* 靠近日期区域，随滚动吸顶但不遮挡内容 */
  margin-left: 220rpx; /* 预留左侧科室树宽度 */
  margin-right: 16rpx;
  width: calc(100% - 220rpx - 32rpx);
  z-index: 95;
  box-shadow: 0 3rpx 10rpx rgba(0, 0, 0, 0.05);
  border-radius: 18rpx;
  border: 1rpx solid rgba(0, 0, 0, 0.04);
}

.patient-inline-right {
  flex: 1;
  display: flex;
  justify-content: flex-start;
  padding-left: 0;
}

/* 主内容区域 - 双栏布局 */
.main-content {
  flex: 1;
  display: flex;
  height: calc(100vh - 400rpx);
  padding-left: 220rpx; /* 预留左侧固定科室树宽度，避免遮挡 */
}

/* 左侧科室列表 */
.left-panel {
  width: 220rpx;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafb 100%);
  /* 完全固定在视窗，独立滚动，不随右侧滚动 */
  position: fixed;
  top: 248rpx; /* 下移以完全避开日期与就诊人条，防止遮挡顶部科室 */
  left: 0;
  bottom: 0;
  height: auto;
  overflow-y: auto;
  border-right: 1rpx solid #e0e6ed;
  box-shadow: 2rpx 0 8rpx rgba(0, 0, 0, 0.02);
  z-index: 90;
}

/* 移除标签页显示 */
.tabs {
  display: none;
}

/* 科室列表 */
.department-tree {
  padding: 0;
}

.department-item {
  margin-bottom: 0;
}

/* 一级科室样式 */
.primary-department {
  padding: 28rpx 16rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  background: transparent;
  border-bottom: 1rpx solid #f0f3f6;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  user-select: none;
  -webkit-user-select: none;
  position: relative;
  overflow: hidden;
}

.primary-department:hover {
  background: linear-gradient(90deg, rgba(74, 144, 226, 0.05) 0%, transparent 100%);
}

.primary-department.active {
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.1) 0%, rgba(110, 198, 255, 0.1) 100%);
  color: #4a90e2;
  font-weight: 700;
  border-left: 4rpx solid #4a90e2;
  box-shadow: inset 0 0 20rpx rgba(74, 144, 226, 0.08);
}

.primary-department:active {
  transform: scale(0.98);
  transition: transform 0.1s ease;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.12) 0%, rgba(110, 198, 255, 0.12) 100%);
  box-shadow: 0 6rpx 14rpx rgba(74, 144, 226, 0.2);
}

.primary-department::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(74, 144, 226, 0.18) 0%, transparent 55%);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.primary-department:active::after {
  opacity: 1;
}

/* 箭头图标 */
.arrow-icon {
  display: inline-block;
  width: 24rpx;
  height: 24rpx;
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  transition: transform 0.3s ease;
}

.primary-department.active .arrow-icon {
  transform: translateY(-50%) rotate(90deg);
}

/* 二级科室列表容器 */
.secondary-department-list {
  transition: all 0.3s ease;
  overflow: hidden;
  max-height: 0;
}

.secondary-department-list.expanded {
  max-height: 1000rpx;
}

/* 二级科室 */
.secondary-department {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 16rpx;
  text-align: center;
  font-size: 26rpx;
  color: #666;
  transition: all 0.2s ease;
  background: transparent;
  border-bottom: 1rpx solid #f0f3f6;
  user-select: none;
  -webkit-user-select: none;
  transform-origin: center;
}

.secondary-department:hover {
  background: linear-gradient(90deg, rgba(74, 144, 226, 0.08) 0%, transparent 100%);
  color: #4a90e2;
}

.secondary-department.active {
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  color: #ffffff;
  font-weight: 700;
  padding: 24rpx 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.3);
  border-radius: 12rpx;
  margin: 8rpx 12rpx;
}

.secondary-department:active {
  transform: scale(0.97);
  transition: transform 0.1s ease;
}

/* 隐藏滚动条但保留功能 */
.left-panel::-webkit-scrollbar {
  display: none;
}

.doctor-schedule-list::-webkit-scrollbar {
  display: none;
}

/* 搜索结果样式 */
.search-results {
  background: #fff;
  padding: 16rpx;
}

.result-header {
  font-size: 26rpx;
  color: #666;
  padding: 16rpx 0;
  margin-bottom: 8rpx;
}

.search-result-items {
  max-height: 600rpx;
  overflow-y: auto;
}

.search-result-item {
  padding: 20rpx 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.search-result-item:last-child {
  border-bottom: none;
}

.result-name {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 4rpx;
}

.result-desc {
  display: block;
  font-size: 24rpx;
  color: #999;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
}

.empty-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 16rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 26rpx;
  color: #999;
}

/* 右侧医生排班信息 */
.right-panel {
  flex: 1;
  background-color: white;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 隐藏科室信息头部 */
.department-header {
  display: none;
}

/* 未选择科室提示 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 40rpx;
}

.empty-state .empty-icon {
  font-size: 140rpx;
  margin-bottom: 32rpx;
  opacity: 0.3;
  filter: grayscale(100%);
}

.empty-state .empty-text {
  font-size: 30rpx;
  color: #999;
  font-weight: 500;
}

/* 医生排班列表 */
.doctor-schedule-list {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* 日期分组标题 */
.date-section {
  padding: 20rpx 24rpx;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.05) 0%, rgba(110, 198, 255, 0.05) 100%);
  border-left: 4rpx solid #4a90e2;
}

.date-title {
  font-size: 28rpx;
  color: #4a90e2;
  font-weight: 600;
}

/* 医生信息卡片 */
.doctor-item {
  margin-bottom: 24rpx;
  background: #ffffff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.doctor-item:hover {
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.1);
  transform: translateY(-2rpx);
}

/* 医生基本信息 */
.doctor-info {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafb 100%);
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1rpx solid #f0f3f6;
}

.doctor-info:active {
  background: linear-gradient(135deg, #f0f3f6 0%, #e8ecf0 100%);
}

.doctor-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 44rpx;
  margin-right: 20rpx;
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38rpx;
  color: #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.3);
}

.doctor-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.doctor-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2d3d;
  margin-bottom: 6rpx;
}

.doctor-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 6rpx;
  flex-wrap: wrap;
}

.doctor-title {
  font-size: 26rpx;
  color: #4a90e2;
  font-weight: 500;
}

.doctor-type-name {
  font-size: 24rpx;
  color: #ff6b6b;
  background: rgba(255, 107, 107, 0.1);
  padding: 2rpx 10rpx;
  border-radius: 4rpx;
  font-weight: 500;
}

.doctor-specialty {
  font-size: 24rpx;
  color: #666;
  line-height: 32rpx;
}

/* 排班卡片 */
/* 时段选择容器 */
.time-slots-container {
  margin: 0 20rpx 20rpx 20rpx;
  padding: 0;
}

/* 单个时段项 */
.time-slot-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 32rpx;
  margin-bottom: 16rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafb 100%);
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
}

.time-slot-item.clickable {
  cursor: pointer;
  background: linear-gradient(135deg, #ffffff 0%, #f0f6ff 100%);
  border-color: rgba(74, 144, 226, 0.2);
}

.time-slot-item.clickable:active {
  transform: scale(0.97);
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.25);
  background: linear-gradient(135deg, #4a90e2 0%, #6ec6ff 100%);
  border-color: transparent;
}

.time-slot-item.clickable:active .slot-label,
.time-slot-item.clickable:active .slot-status {
  color: #ffffff;
}

/* 时段信息 */
.slot-info {
  display: flex;
  flex-direction: column;
}

/* 时段标签 */
.slot-label {
  font-size: 30rpx;
  color: #333;
  font-weight: 600;
  transition: color 0.3s ease;
}

/* 时段状态 */
.slot-status {
  font-size: 30rpx;
  font-weight: 700;
  color: #4a90e2;
  padding: 8rpx 20rpx;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.1) 0%, rgba(110, 198, 255, 0.1) 100%);
  border-radius: 20rpx;
  transition: all 0.3s ease;
}

/* 无号状态 */
.slot-status.no-slot {
  color: #999;
  font-weight: 500;
  background: #f5f5f5;
}

/* 已过预约时间状态 */
.slot-status.expired {
  color: #ff9500;
  font-weight: 500;
  background: rgba(255, 149, 0, 0.1);
}

/* 已满状态 */
.slot-status.full {
  color: #ff4d4f;
  font-weight: 500;
  background: rgba(255, 77, 79, 0.1);
}

/* 已过预约时间的时段项 */
.time-slot-item.expired {
  opacity: 0.6;
  cursor: not-allowed;
}

.time-slot-item.expired .slot-label {
  color: #999;
}

/* 无号的时段项 */
.time-slot-item.no-slot {
  opacity: 0.6;
  cursor: not-allowed;
}

.time-slot-item.no-slot .slot-label {
  color: #999;
}

/* 已满的时段项 */
.time-slot-item.full {
  opacity: 0.8;
  cursor: pointer;
}

.time-slot-item.full .slot-label {
  color: #666;
}

/* 已预约（禁用）的时段项 */
.time-slot-item.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  border-color: #d9d9d9;
}

.time-slot-item.disabled .slot-label {
  color: #999;
}

.time-slot-item.disabled .slot-status.disabled {
  color: #999;
  font-weight: 500;
  background: #e8e8e8;
}

/* 禁用理由 */
.slot-reason {
  font-size: 24rpx;
  color: #ff4d4f;
  margin-top: 8rpx;
}

.schedule-card {
  margin: 0 20rpx 20rpx 20rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  background-color: #fff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  border: 1rpx solid #e8e8e8;
}

.schedule-card .schedule-header {
  padding: 0 0 15rpx 0;
  margin-bottom: 15rpx;
  background-color: transparent;
  border: none;
}

.schedule-date {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.schedule-content {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.schedule-time-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.time-range {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}

.schedule-type {
  font-size: 26rpx;
  color: #666;
}

.schedule-price {
  font-size: 28rpx;
  color: #ff6b6b;
  font-weight: 500;
}

.department-info {
  font-size: 26rpx;
  color: #666;
  padding: 5rpx 0;
}

.quota-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10rpx;
}

.quota-text {
  font-size: 26rpx;
  color: #197afc;
}

.quota-text.no-quota {
  color: #999;
}

.book-button {
  background-color: #197afc;
  color: white;
  border-radius: 25rpx;
  padding: 10rpx 30rpx;
  font-size: 26rpx;
  border: none;
  font-weight: 500;
}

.book-button:disabled {
  background-color: #cccccc;
  color: #ffffff;
}

.no-schedule {
  text-align: center;
  padding: 32rpx 0;
  color: #999;
  font-size: 24rpx;
}

.no-doctors {
  text-align: center;
  padding: 120rpx 40rpx;
  color: #999;
  font-size: 28rpx;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(248, 250, 251, 0.8) 100%);
  border-radius: 20rpx;
  margin: 20rpx;
}

.no-doctors-text {
  font-size: 28rpx;
  color: #999;
  font-weight: 500;
}
</style>
