<template>
  <view class="department-booking-page">
    <!-- 顶部标题 -->
    <view class="page-header">
      <text class="header-title">预约挂号</text>
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
                    :class="{ 'active': child.selected }"
                    @click="selectDepartment(child)"
                  >
                    <text class="department-name">{{ child.deptName }}</text>
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
                  @click="selectDepartment(dept)"
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
              <view v-for="doctor in doctors" :key="doctor.id" class="doctor-item">
                <!-- 医生基本信息 - 可点击跳转到详情 -->
                <view class="doctor-info" @click="viewDoctorDetail(doctor)">
                  <view class="doctor-avatar">{{ getDoctorInitial(doctor.name || doctor.doctorName || '未知医生') }}</view>
                  <view class="doctor-details">
                    <view class="doctor-name">{{ doctor.name || doctor.doctorName || '未知医生' }}</view>
                    <view class="doctor-title">{{ doctor.title || '医师' }}</view>
                    <view class="doctor-specialty">擅长: {{ doctor.specialty || '内科' }}</view>
                  </view>
                  <view class="arrow-icon">></view>
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
                      class="time-slot-item" 
                      :class="{ 'clickable': getSlotByTimeRange(doctor.id, '上午')?.availableSlots }"
                      @click="navigateToPayment(doctor, getSlotByTimeRange(doctor.id, '上午'), '上午 (08:00-12:00)')"
                      v-if="getSlotByTimeRange(doctor.id, '上午')?.availableSlots"
                    >
                      <view class="slot-info">
                        <text class="slot-label">上午 (08:00-12:00)</text>
                      </view>
                      <text class="slot-status">
                        余{{ getSlotByTimeRange(doctor.id, '上午').availableSlots }}
                      </text>
                    </view>
                    <view 
                      class="time-slot-item" 
                      v-else
                    >
                      <view class="slot-info">
                        <text class="slot-label">上午 (08:00-12:00)</text>
                      </view>
                      <text class="slot-status no-slot">
                        无号
                      </text>
                    </view>
                    
                    <!-- 下午时段 -->
                    <view 
                      class="time-slot-item" 
                      :class="{ 'clickable': getSlotByTimeRange(doctor.id, '下午')?.availableSlots }"
                      @click="navigateToPayment(doctor, getSlotByTimeRange(doctor.id, '下午'), '下午 (14:00-17:00)')"
                      v-if="getSlotByTimeRange(doctor.id, '下午')?.availableSlots"
                    >
                      <view class="slot-info">
                        <text class="slot-label">下午 (14:00-17:00)</text>
                      </view>
                      <text class="slot-status">
                        余{{ getSlotByTimeRange(doctor.id, '下午').availableSlots }}
                      </text>
                    </view>
                    <view 
                      class="time-slot-item" 
                      v-else
                    >
                      <view class="slot-info">
                        <text class="slot-label">下午 (14:00-17:00)</text>
                      </view>
                      <text class="slot-status no-slot">
                        无号
                      </text>
                    </view>
                    
                    <!-- 晚上时段 -->
                    <view 
                      class="time-slot-item" 
                      :class="{ 'clickable': getSlotByTimeRange(doctor.id, '晚上')?.availableSlots }"
                      @click="navigateToPayment(doctor, getSlotByTimeRange(doctor.id, '晚上'), '晚上 (18:00-20:00)')"
                      v-if="getSlotByTimeRange(doctor.id, '晚上')?.availableSlots"
                    >
                      <view class="slot-info">
                        <text class="slot-label">晚上 (18:00-20:00)</text>
                      </view>
                      <text class="slot-status">
                        余{{ getSlotByTimeRange(doctor.id, '晚上').availableSlots }}
                      </text>
                    </view>
                    <view 
                      class="time-slot-item" 
                      v-else
                    >
                      <view class="slot-info">
                        <text class="slot-label">晚上 (18:00-20:00)</text>
                      </view>
                      <text class="slot-status no-slot">
                        无号
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
import { getDepartmentTree } from '@/api/department'
import { getDoctorsByDeptId, getDoctorDetail } from '@/api/doctor'
import { scheduleApi } from '@/api/schedule'
import { getDoctorSchedules } from '@/api/registration'

// 生成医生头像初始化字母
const getDoctorInitial = (name) => {
  if (!name || name.length === 0) return '医';
  // 获取姓或名的第一个字符
  return name.charAt(0);
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
      date: date.toISOString().split('T')[0],
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

  // 跳转到支付页面
  const navigateToPayment = (doctor, schedule, timeText) => {
    if (!doctor || !schedule || !schedule.availableSlots) {
      return;
    }
    
    // 构建跳转参数
    const scheduleId = schedule?.scheduleId ?? schedule?.id ?? schedule?.schedule_id ?? 0
    const typeId = schedule?.typeId ?? schedule?.type_id ?? schedule?.type ?? 1

    const params = {
      dept: encodeURIComponent(selectedDepartment?.deptName || '未知科室'),
      doctor: encodeURIComponent(doctor.name || doctor.doctorName || '未知医生'),
      time: encodeURIComponent(`${selectedDate.value} ${timeText}`),
      doctorId: doctor.doctorId || doctor.id || 0,
      scheduleId,
      typeId,
      deptId: selectedDepartment?.deptId || 0
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

          return {
            id: resolvedScheduleId,
            scheduleId: resolvedScheduleId,
            date: scheduleDate || date,
            timeRange: timeRangeText,
            typeId: resolvedTypeId != null ? Number(resolvedTypeId) : null,
            typeName: schedule.type_name || schedule.typeName || schedule.type?.name || '普通门诊',
            fee: Number(schedule.price || schedule.fee || 50),
            availableSlots,
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

// 在组件挂载后设置默认状态
onMounted(() => {
  initDateList()
  initDepartmentTree()
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

// 页面加载时初始化
onMounted(() => {
  initDateList()
  fetchDepartmentTree()
})
</script>

<style scoped>
/* 全局样式重置和统一 */
.department-booking-page {
  min-height: 100vh;
  background: #f5f5f5;
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
  background-color: #ffffff;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  border-bottom: 1rpx solid #eaeaea;
  z-index: 100;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

/* 日期选择器容器 - 移至顶部 */
.date-selection {
  margin-top: 88rpx;
  padding: 0;
  background-color: #fff;
  position: sticky;
  top: 88rpx;
  z-index: 99;
  border-bottom: 1rpx solid #e0e0e0;
  overflow-x: auto;
  white-space: nowrap;
}

.date-scroll {
  padding: 0;
  display: flex;
  width: 100%;
}

.date-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 0;
  text-align: center;
  border-bottom: 4rpx solid transparent;
  flex: 1;
  min-width: 120rpx;
  transition: all 0.3s ease;
}

.date-item.active {
  color: #197afc;
  border-bottom-color: #197afc;
  transform: scale(1.05);
}

.date-item:active {
  transform: scale(1.05);
  color: #197afc;
}

.date-week {
  font-size: 26rpx;
  margin-bottom: 5rpx;
  text-align: center;
  transition: font-size 0.3s ease;
}

.date-day {
  font-size: 30rpx;
  font-weight: 500;
  text-align: center;
  transition: font-size 0.3s ease;
}

.date-item.active .date-day,
.date-item:active .date-day {
  font-size: 32rpx;
}

.date-item.active .date-week,
.date-item:active .date-week {
  font-size: 28rpx;
}

/* 主内容区域 - 双栏布局 */
.main-content {
  flex: 1;
  display: flex;
  height: calc(100vh - 300rpx);
}

/* 左侧科室列表 */
.left-panel {
  width: 220rpx;
  background-color: #f5f5f5;
  height: calc(100vh - 240rpx);
  overflow-y: auto;
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
  padding: 25rpx 0;
  text-align: center;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  background-color: #f5f5f5;
  border-bottom: 1rpx solid #e0e0e0;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  user-select: none;
  -webkit-user-select: none;
}

.primary-department:hover {
  background-color: #f0f0f0;
  transform: translateY(-1rpx);
}

.primary-department.active {
  background-color: #e8f0fe;
  color: #197afc;
  box-shadow: 0 2rpx 8rpx rgba(25, 122, 252, 0.1);
}

.primary-department:active {
  transform: scale(0.98);
  transition: transform 0.1s ease;
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
  padding: 30rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #666;
  transition: all 0.2s ease;
  background-color: #f5f5f5;
  border-bottom: 1rpx solid #e0e0e0;
  user-select: none;
  -webkit-user-select: none;
  transform-origin: center;
}

.secondary-department:hover {
  background-color: #f0f0f0;
  color: #197afc;
}

.secondary-department.active {
  background-color: white;
  color: #197afc;
  font-weight: bold;
  padding: 30rpx 0;
  box-shadow: inset 0 0 0 1rpx #197afc;
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
}

.empty-state .empty-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-state .empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 医生排班列表 */
.doctor-schedule-list {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* 日期分组标题 */
.date-section {
  padding: 15rpx 20rpx;
  background-color: #f5f5f5;
}

.date-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

/* 医生信息卡片 */
.doctor-item {
  margin-bottom: 20rpx;
}

/* 医生基本信息 */
.doctor-info {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: white;
  border-bottom: 1rpx solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.doctor-info:active {
  background-color: #f5f5f5;
}

.arrow-icon {
  font-size: 28rpx;
  color: #999;
  margin-left: 10rpx;
}

.doctor-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  margin-right: 20rpx;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  color: #197afc;
}

.doctor-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.doctor-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 5rpx;
}

.doctor-title {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 5rpx;
}

.doctor-specialty {
  font-size: 24rpx;
  color: #999;
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
  padding: 30rpx;
  margin-bottom: 15rpx;
  background-color: #fff;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.time-slot-item:active {
  transform: scale(0.98);
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.05);
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
  font-weight: 500;
}

/* 时段状态 */
.slot-status {
  font-size: 30rpx;
  font-weight: bold;
  color: #197afc;
}

/* 无号状态 */
.slot-status.no-slot {
  color: #999;
  font-weight: normal;
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
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}

.no-doctors-text {
  font-size: 28rpx;
  color: #999;
}
</style>
