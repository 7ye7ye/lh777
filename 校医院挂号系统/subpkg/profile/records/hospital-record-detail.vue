<template>
  <view class="detail-page">
    <view class="header-section">
      <text class="page-title">就诊详情</text>
    </view>
    
    <!-- 整合基本信息和患者信息 -->
    <view class="info-card">
      <view class="card-title">就诊基本信息</view>
      <view class="info-content">
        <view class="info-row">
          <view class="info-item">
            <text class="info-label">科室</text>
            <text class="info-value">{{ departmentName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">医生</text>
            <text class="info-value">{{ doctorName || '-' }}</text>
          </view>
        </view>
        <view class="info-row">
          <view class="info-item">
            <text class="info-label">挂号时间</text>
            <text class="info-value">{{ formatTime(record?.registerTime || record?.visitTime) || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">就诊时间</text>
            <text class="info-value">{{ getTimeSlotWithDate(record) || '-' }}</text>
          </view>
        </view>
        <view class="info-row">
          <view class="info-item">
            <text class="info-label">挂号编号</text>
            <text class="info-value">{{ record?.id || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">就诊状态</text>
            <text class="info-value">{{ record?.status !== undefined ? (statusTextMap[record.status] || '未知状态') : '-' }}</text>
          </view>
        </view>
        <view class="info-row">
          <view class="info-item">
            <text class="info-label">患者姓名</text>
            <text class="info-value">{{ patientInfo?.name || '' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">就诊卡号</text>
            <text class="info-value">{{ patientInfo?.cardNumber || '' }}</text>
          </view>
        </view>
        <view class="info-row">
          <view class="info-item">
            <text class="info-label">联系方式</text>
            <text class="info-value">{{ patientInfo?.phone || '' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">挂号类型</text>
            <text class="info-value">{{ record?.registerType || '-' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 支付信息卡片 - 简化显示 -->
    <view class="info-card">
      <view class="card-title">支付信息</view>
      <view class="info-content">
        <view class="info-row">
          <view class="info-item">
            <text class="info-label">挂号费用</text>
            <text class="info-value fee">¥{{ paymentInfo?.fee || '' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">支付状态</text>
            <text class="info-value status-paid">{{ paymentInfo?.status || '' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 诊断信息卡片 -->
    <view class="info-card">
      <view class="card-title">诊断信息</view>
      <view class="info-content">
        <view class="diagnosis-item">
          <text class="diagnosis-title">初步诊断</text>
            <text class="diagnosis-content">{{ diagnosisInfo?.primary || '' }}</text>
          </view>
          <view class="diagnosis-item">
            <text class="diagnosis-title">处理意见</text>
            <text class="diagnosis-content">{{ diagnosisInfo?.advice || '' }}</text>
          </view>
      </view>
    </view>

    <!-- 操作按钮区域 -->
    <view class="action-section">
      <button v-if="canRefer" class="action-btn referral-btn" @click="goToReferral">申请转诊</button>
      <text v-else class="action-btn cannot-refer-btn">超过5天，无法转诊</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchPatientCard } from '@/utils/patientHelper'
import { getDepartmentDetail } from '@/api/department'
import { getDoctorDetail } from '@/api/doctor_massage'

// 挂号状态映射表
const statusTextMap = {
  0: '候补',
  1: '已预约',
  2: '已就诊',
  3: '已退号',
  4: '已取消'
}

// 接收从上一个页面传递的数据
const record = ref(null)
const patientInfo = ref({})
const paymentInfo = ref({})
const diagnosisInfo = ref({})
const canRefer = ref(false)
const doctorName = ref('')
const departmentName = ref('-')

// 转换日期字符串为iOS兼容格式
const convertToIOSCompatibleDate = (dateString) => {
  if (!dateString) return dateString;
  const normalized = String(dateString).trim();
  if (!normalized) return normalized;
  if (normalized.includes('T')) {
    return normalized;
  }
  // 将 "yyyy-MM-dd HH:mm" 或 "yyyy-MM-dd HH:mm:ss" 都转换为斜线格式或 ISO 格式
  if (/^\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}(:\d{2})?$/.test(normalized)) {
    return normalized.replace(/-/g, '/');
  }
  return normalized;
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  
  // 转换为iOS兼容格式
  const compatibleTimeStr = convertToIOSCompatibleDate(timeStr);
  const date = new Date(compatibleTimeStr)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 加载患者信息 - 不使用缓存，直接从服务器获取最新数据
const loadPatientInfo = async () => {
  try {
    const info = await fetchPatientCard()
    if (info) {
      patientInfo.value = {
        name: info.patientName || '患者',
        cardNumber: info.cardNumber || '',
        phone: info.phone || '',
        patientId: info.patientId
      }
    }
  } catch (error) {
    console.error('加载患者信息失败:', error)
  }
}

// 检查就诊记录是否在五天内（可申请转诊）
const isWithinFiveDays = (visitTimeStr) => {
  if (!visitTimeStr) return false
  
  try {
    // 转换为iOS兼容格式
    const compatibleVisitTimeStr = convertToIOSCompatibleDate(visitTimeStr);
    const visitTime = new Date(compatibleVisitTimeStr)
    // 确保日期有效
    if (isNaN(visitTime.getTime())) {
      console.warn('无效的就诊时间格式:', visitTimeStr)
      return false
    }
    
    const now = new Date()
    
    // 仅比较日期部分，忽略时分秒差异
    const visitDate = new Date(visitTime.getFullYear(), visitTime.getMonth(), visitTime.getDate())
    const nowDate = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    
    // 计算5天前的日期
    const fiveDaysAgo = new Date(nowDate.getTime() - 5 * 24 * 60 * 60 * 1000)
    
    // 检查就诊时间是否在过去5天内且不是未来时间
    // 使用 > 而不是 >= 来确保5天前的日期不被包含
    const result = visitDate > fiveDaysAgo && visitDate <= nowDate
    console.log('转诊权限检查:', { visitTime, fiveDaysAgo, nowDate, canRefer: result })
    return result
  } catch (error) {
    console.error('检查转诊时间失败:', error)
    return false
  }
}

// 加载就诊相关信息
const loadRecordDetails = async () => {
  try {
    // 在UniApp中获取页面参数
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    const options = currentPage.options || {};
    
    let routeData = null;
    // 尝试从options中获取record参数
    if (options.record) {
      try {
        // 先尝试解码可能已经被URL编码的参数
        let recordStr = options.record;
        // 检查是否包含URL编码特征
        if (recordStr.includes('%22') || recordStr.includes('%7B') || recordStr.includes('%')) {
          try {
            recordStr = decodeURIComponent(recordStr);
            console.log('已解码参数:', recordStr);
          } catch (decodeError) {
            console.warn('参数解码失败，使用原始字符串:', decodeError);
          }
        }
        // 再尝试解析JSON
        routeData = JSON.parse(recordStr);
      } catch (parseError) {
        console.warn('无法解析record参数:', parseError);
        // 检查是否已经是对象
        if (typeof options.record === 'object') {
          routeData = options.record;
        } else {
          // 尝试其他可能的参数名
          if (options.recordData) {
            try {
              let dataStr = options.recordData;
              if (dataStr.includes('%')) {
                dataStr = decodeURIComponent(dataStr);
              }
              routeData = JSON.parse(dataStr);
            } catch (e) {
              console.warn('无法解析recordData参数:', e);
            }
          }
        }
      }
    }
    
    if (routeData) {
      record.value = routeData;
      
      // 检查是否可以转诊
      canRefer.value = isWithinFiveDays(routeData.visitTime);
      
      // 初始化科室和医生信息
      let doctorId = null;
      let departmentId = null;
      
      // 设置医生和科室名称，确保显示的是名称而非ID
      // 从原始记录中获取更完整的信息
      if (routeData.originalRecord) {
        // 从originalRecord中提取患者信息（如果页面数据中有患者姓名）
        if (routeData.patientName) {
          patientInfo.value = {
            ...patientInfo.value,
            name: routeData.patientName
          };
        }
        
        // 获取医生ID
        doctorId = routeData.originalRecord.doctorId || routeData.doctorId;
        
        // 获取科室ID
        departmentId = routeData.originalRecord.departmentId || routeData.departmentId;
        
        // 处理医生信息
        let doctorData = {
          ...routeData,
          doctorId: doctorId,
          // 从routeData中尝试获取医生名称
          doctorName: routeData.doctorName || ''
        };
        doctorName.value = getDoctorName(doctorData);
        
        // 处理科室信息
        let departmentData = {
          ...routeData,
          department: routeData.department || '',
          departmentId: departmentId,
          departmentName: routeData.departmentName || ''
        };
        departmentName.value = getDepartmentName(departmentData);
      } else {
        // 如果没有originalRecord，使用当前routeData
        doctorId = routeData.doctorId;
        departmentId = routeData.departmentId || (typeof routeData.department === 'number' ? routeData.department : null);
        
        doctorName.value = getDoctorName(routeData);
        departmentName.value = getDepartmentName(routeData);
      }
      
      // 使用真实支付信息，从路由数据和originalRecord中获取
      const normalizePaymentStatus = (value) => {
        if (value === undefined || value === null || value === '') {
          return '未支付'
        }
        const normalized = String(value).toLowerCase()
        const paidKeywords = ['paid', 'success', 'completed', '已支付', '已完成', '1', 'true']
        return paidKeywords.some(keyword => normalized.includes(keyword)) ? '已支付' : '未支付'
      }

      const rawStatus = routeData.paymentStatus || routeData.status || routeData.paymentInfo?.status || ''
      paymentInfo.value = {
        fee: routeData.fee || 
             routeData.paymentFee || 
             routeData.paymentInfo?.fee || 
             routeData.cost || 
             routeData.price || 
             (routeData.originalRecord?.priceOriginal || 0) || // 从originalRecord获取原始价格
             (routeData.originalRecord?.actualPrice || 0), // 从originalRecord获取实际价格
        status: normalizePaymentStatus(rawStatus),
        paymentTime: routeData.paymentTime || routeData.visitTime || '',
        method: routeData.paymentMethod || routeData.paymentInfo?.method || ''
      };
      
      // 使用真实诊断信息，从路由数据中获取
      diagnosisInfo.value = {
        primary: routeData.diagnosis || routeData.diagnosisInfo?.primary || routeData.primaryDiagnosis || '',
        advice: routeData.advice || routeData.diagnosisInfo?.advice || routeData.treatmentAdvice || ''
      };
      
      // 特殊处理：增强科室信息提取，从更多可能的来源获取
      if (!departmentName.value || departmentName.value === '-' || departmentName.value === '暂无科室信息') {
        // 尝试从多个可能的字段获取科室信息
        const possibleDeptSources = [
          // 直接字段
          routeData.department,
          routeData.departmentName,
          routeData.deptName,
          routeData.section,
          routeData.sectionName,
          // 嵌套对象
          routeData.department?.name,
          routeData.department?.departmentName,
          // 原始记录
          routeData.originalRecord?.department,
          routeData.originalRecord?.departmentName,
          routeData.originalRecord?.deptName,
          routeData.originalRecord?.section,
          // 原始记录嵌套对象
          routeData.originalRecord?.department?.name,
          // 数据字段
          routeData.data?.department,
          routeData.data?.departmentName
        ];
        
        for (const source of possibleDeptSources) {
          if (source && typeof source === 'string') {
            const trimmedSource = source.trim();
            if (trimmedSource && !trimmedSource.toLowerCase().includes('未知科室') && !trimmedSource.toLowerCase().includes('unknown')) {
              departmentName.value = trimmedSource;
              console.log('从备用字段找到科室信息:', trimmedSource);
              break;
            }
          }
        }
      }
      
      // 特殊处理：如果医生信息为空，尝试从更多可能的字段获取
      if (doctorName.value === '暂无医生信息') {
        // 尝试从多个可能的字段获取医生信息
        const possibleDoctorFields = [
          routeData.doctor,
          routeData.doctorName,
          routeData.name,
          routeData.originalRecord?.doctor,
          routeData.originalRecord?.doctorName,
          routeData.originalRecord?.name
        ];
        
        for (const field of possibleDoctorFields) {
          if (field && typeof field === 'string' && !field.includes('未知医生')) {
            doctorName.value = field;
            break;
          }
        }
      }
      
      // 如果有科室ID，通过API获取科室详情
      if (departmentId) {
        const departmentInfo = await fetchDepartmentInfo(departmentId);
        if (departmentInfo && departmentInfo.deptName) {
          console.log('通过API更新科室名称:', departmentInfo.deptName);
          departmentName.value = departmentInfo.deptName;
        } else {
          // API调用失败或未返回有效数据时，尝试从本地数据获取
          console.log('API未返回科室信息，尝试从本地数据获取');
          
          // 更全面地提取可能的科室ID字段
          const possibleDeptIds = [
            routeData.departmentId,
            routeData.deptId,
            routeData.department,
            routeData.originalRecord?.departmentId,
            routeData.originalRecord?.deptId,
            routeData.originalRecord?.department
          ];
          
          // 尝试其他可能的科室ID
          for (const id of possibleDeptIds) {
            if (id && id !== departmentId) {
              const altDeptInfo = await fetchDepartmentInfo(id);
              if (altDeptInfo && altDeptInfo.deptName) {
                console.log('通过备用科室ID更新科室名称:', altDeptInfo.deptName);
                departmentName.value = altDeptInfo.deptName;
                break;
              }
            }
          }
        }
      }
      
      // 如果有医生ID，通过API获取医生详情
      if (doctorId) {
        const doctorInfo = await fetchDoctorInfo(doctorId);
        if (doctorInfo) {
          if (doctorInfo.doctorName) {
            console.log('通过API更新医生名称:', doctorInfo.doctorName);
            doctorName.value = doctorInfo.doctorName;
          }
          if (!departmentName.value || departmentName.value === '-' || departmentName.value === '暂无科室信息') {
            const deptFromDoctor = doctorInfo.deptName || doctorInfo.department || doctorInfo.section
            if (deptFromDoctor) {
              console.log('通过医生详情补充科室名称:', deptFromDoctor);
              departmentName.value = deptFromDoctor
            }
          }
        } else {
          // API调用失败或未返回有效数据时，尝试从本地数据获取
          console.log('API未返回医生信息，尝试从本地数据获取');
          
          // 更全面地提取可能的医生ID字段
          const possibleDoctorIds = [
            routeData.doctorId,
            routeData.doctor,
            routeData.originalRecord?.doctorId,
            routeData.originalRecord?.doctor
          ];
          
          // 尝试其他可能的医生ID
          for (const id of possibleDoctorIds) {
            if (id && id !== doctorId) {
              const altDoctorInfo = await fetchDoctorInfo(id);
              if (altDoctorInfo && altDoctorInfo.doctorName) {
                console.log('通过备用医生ID更新医生名称:', altDoctorInfo.doctorName);
                doctorName.value = altDoctorInfo.doctorName;
                break;
              }
            }
          }
        }
      }
    }
  } catch (error) {
    console.error('加载就诊详情失败:', error);
    // 只设置空对象，不使用模拟数据
    record.value = {};
    doctorName.value = '暂无医生信息';
    departmentName.value = '暂无科室信息';
    paymentInfo.value = {};
    diagnosisInfo.value = {};
    
    // 向用户显示错误提示
    uni.showToast({
      title: '加载就诊详情失败，请稍后重试',
      icon: 'none'
    });
  }
};

// 通过API获取科室详情
const fetchDepartmentInfo = async (deptId) => {
  if (!deptId) {
    console.warn('科室ID不存在');
    return null;
  }
  
  try {
    const response = await getDepartmentDetail(deptId);
    console.log('科室详情响应:', response);
    
    // 处理不同的响应格式
    let departmentData = response;
    if (response && response.data) {
      departmentData = response.data;
    } else if (response && response.result) {
      departmentData = response.result;
    }
    
    // 更全面地提取科室名称，考虑更多可能的字段名
    if (departmentData) {
      const deptName = departmentData.deptName || 
                      departmentData.name || 
                      departmentData.departmentName || 
                      departmentData.department || '';
      
      if (deptName && !deptName.includes('未知科室')) {
        return {
          deptId: departmentData.deptId || departmentData.id || deptId,
          deptName: deptName
        };
      }
    }
    return null;
  } catch (error) {
    console.error('获取科室详情失败:', error);
    return null;
  }
};

// 通过API获取医生详情
const fetchDoctorInfo = async (doctorId) => {
  if (!doctorId) {
    console.warn('医生ID不存在');
    return null;
  }
  
  try {
    const response = await getDoctorDetail(doctorId);
    console.log('医生详情响应:', response);
    
    // 处理不同的响应格式
    let doctorData = response;
    if (response && response.data) {
      doctorData = response.data;
    } else if (response && response.result) {
      doctorData = response.result;
    }
    
    // 更全面地提取医生信息，考虑更多可能的字段名
    if (doctorData) {
      const doctorName = doctorData.doctorName || 
                        doctorData.name || 
                        doctorData.doctor || '';
      
      if (doctorName && !doctorName.includes('未知医生')) {
        return {
          doctorId: doctorData.doctorId || doctorData.id || doctorId,
          doctorName: doctorName,
          title: doctorData.title || doctorData.position || '',
          specialty: doctorData.specialty || doctorData.department || ''
        };
      }
    }
    return null;
  } catch (error) {
    console.error('获取医生详情失败:', error);
    return null;
  }
};

// 获取科室名称 - 增强版，支持更多字段和更复杂的数据结构
const getDepartmentName = (recordData) => {
  if (!recordData) return '-';
  
  // 更全面地尝试各种可能的科室名字段
  let deptName = null;
  
  // 检查是否是完整记录对象
  if (typeof recordData === 'object') {
    // 尝试从多个可能的字段获取科室名称
    const departmentFields = [
      'departmentName', 'deptName', 'department', 'name', 
      'department_name', 'dept_name', 'section', 'sectionName'
    ];
    
    // 先从顶层字段查找
    for (const field of departmentFields) {
      if (recordData[field] && typeof recordData[field] === 'string' && recordData[field].trim()) {
        deptName = recordData[field];
        break;
      }
    }
    
    // 检查是否有嵌套的department对象
    if (!deptName && recordData.department && typeof recordData.department === 'object') {
      for (const field of departmentFields) {
        if (recordData.department[field] && typeof recordData.department[field] === 'string' && recordData.department[field].trim()) {
          deptName = recordData.department[field];
          break;
        }
      }
    }
    
    // 检查原始记录中是否有科室信息
    if (!deptName && recordData.originalRecord && typeof recordData.originalRecord === 'object') {
      for (const field of departmentFields) {
        if (recordData.originalRecord[field] && typeof recordData.originalRecord[field] === 'string' && recordData.originalRecord[field].trim()) {
          deptName = recordData.originalRecord[field];
          break;
        }
      }
    }
    
    // 检查嵌套的data字段
    if (!deptName && recordData.data && typeof recordData.data === 'object') {
      for (const field of departmentFields) {
        if (recordData.data[field] && typeof recordData.data[field] === 'string' && recordData.data[field].trim()) {
          deptName = recordData.data[field];
          break;
        }
      }
    }
  } else if (typeof recordData === 'string') {
    // 如果传入的是字符串，假设是科室名称
    if (recordData.trim() && !recordData.includes('未知科室')) {
      return recordData.trim();
    }
  }
  
  // 如果已找到名称，返回名称（确保不是'未知科室'）
  if (deptName && typeof deptName === 'string') {
    const trimmedName = deptName.trim();
    if (trimmedName && !trimmedName.toLowerCase().includes('未知科室') && !trimmedName.toLowerCase().includes('unknown')) {
      return trimmedName;
    }
  }
  
  // 特殊情况：如果传入的是数字ID，返回科室ID
  if (typeof recordData === 'number') {
    return `科室${recordData}`;
  }
  
  // 默认返回
  return '-';
};

// 获取医生名称
const getDoctorName = (recordData) => {
  if (!recordData) return '暂无医生信息';
  
  // 更全面地尝试各种可能的医生名字段
  let doctorName = null;
  
  // 检查是否是完整记录对象
  if (typeof recordData === 'object') {
    // 尝试从多个可能的字段获取医生名称
    doctorName = recordData.doctorName || 
                 recordData.name || 
                 recordData.doctor || 
                 null;
    
    // 检查是否有嵌套的doctor对象
    if (recordData.doctor && typeof recordData.doctor === 'object') {
      doctorName = recordData.doctor.name || 
                 recordData.doctor.doctorName || 
                 recordData.doctor.doctor || 
                 doctorName;
    }
    
    // 检查原始记录中是否有医生信息
    if (recordData.originalRecord && typeof recordData.originalRecord === 'object') {
      doctorName = recordData.originalRecord.doctorName || 
                 recordData.originalRecord.name || 
                 recordData.originalRecord.doctor || 
                 doctorName;
    }
  } else if (typeof recordData === 'string') {
    // 如果传入的是字符串，假设是医生名称
    if (!recordData.includes('未知医生')) {
      return recordData;
    }
  }
  
  // 如果已找到名称，返回名称（确保不是'未知医生'）
  if (doctorName && typeof doctorName === 'string' && !doctorName.includes('未知医生')) {
    return doctorName;
  }
  
  // 特殊情况：如果传入的是数字ID，返回医生ID
  if (typeof recordData === 'number') {
    return `医生${recordData}`;
  }
  
  // 默认返回
  return '暂无医生信息';
};

// 获取就诊时段
const getTimeSlot = (record) => {
    if (!record) return '';
    
    // 尝试从各种可能的字段获取时段信息
    if (record.timeSlot) return record.timeSlot;
    if (record.slot) return record.slot;
    if (record.shift) return record.shift;
    
    // 从原始数据中尝试获取
    const originalData = record.originalRecord;
    if (originalData) {
      if (originalData.timeSlot) return originalData.timeSlot;
      if (originalData.slot) return originalData.slot;
      if (originalData.shift) return originalData.shift;
    }
    
    // 尝试从时间推断时段
    const registerTime = record.registerTime || record.visitTime;
    if (registerTime) {
      try {
        // 转换为iOS兼容格式
        const compatibleRegisterTime = convertToIOSCompatibleDate(registerTime);
        const date = new Date(compatibleRegisterTime);
        if (date && !isNaN(date.getTime())) {
          const hour = date.getHours();
          if (hour >= 6 && hour < 12) return '上午';
          if (hour >= 12 && hour < 18) return '下午';
          if (hour >= 18 && hour < 24) return '晚上';
        }
      } catch (error) {
        console.warn('时段推断失败:', error);
      }
    }
    
    return '';
  };

  // 获取带日期的就诊时段
  const getTimeSlotWithDate = (record) => {
    if (!record) return '';
    
    // 获取时段信息
    const timeSlot = getTimeSlot(record);
    
    // 获取日期信息
    const visitTime = record.visitTime || record.registerTime;
    let dateStr = '';
    
    if (visitTime) {
      try {
        // 转换为iOS兼容格式
        const compatibleVisitTime = convertToIOSCompatibleDate(visitTime);
        const date = new Date(compatibleVisitTime);
        if (date && !isNaN(date.getTime())) {
          const year = date.getFullYear();
          const month = (date.getMonth() + 1).toString().padStart(2, '0');
          const day = date.getDate().toString().padStart(2, '0');
          dateStr = `${year}-${month}-${day}`;
        }
      } catch (error) {
        console.warn('日期格式化失败:', error);
      }
    }
    
    // 返回日期和时段
    if (dateStr && timeSlot) {
      return `${dateStr} ${timeSlot}`;
    } else if (dateStr) {
      return dateStr;
    } else {
      return timeSlot;
    }
  };

// 导航到医生详情页面
const navigateToDoctor = () => {
  // 获取医生ID
  let doctorId = null;
  if (record.value && record.value.doctorId) {
    doctorId = record.value.doctorId;
  } else if (record.value && typeof record.value.doctor === 'number') {
    doctorId = record.value.doctor;
  }
  
  if (doctorId) {
    uni.navigateTo({
      url: `/subpkg/hospital/doctor-detail?id=${doctorId}`
    })
  } else {
    uni.showToast({
      title: '暂无医生详情信息',
      icon: 'none'
    })
  }
}

// 导航到科室详情页面
const navigateToDepartment = () => {
  // 获取科室ID
  let departmentId = null;
  if (record.value && record.value.departmentId) {
    departmentId = record.value.departmentId;
  } else if (record.value && typeof record.value.department === 'number') {
    departmentId = record.value.department;
  }
  
  if (departmentId) {
    uni.navigateTo({
      url: `/subpkg/hospital/department-detail?id=${departmentId}`
    })
  } else {
    uni.showToast({
      title: '暂无科室详情信息',
      icon: 'none'
    })
  }
}

// 申请转诊
  const goToReferral = () => {
    try {
      // 验证是否有足够的记录数据
      if (!record.value || !record.value.id) {
        console.warn('缺少必要的就诊记录信息')
        uni.showToast({
          title: '就诊记录信息不完整',
          icon: 'none'
        })
        return
      }
      
      // 准备要传递的记录数据 - 仅使用真实数据
      const referralData = {
        recordId: record.value.id,
        patientName: patientInfo.value?.name || '',
        department: departmentName.value,
        doctor: doctorName.value,
        visitTime: record.value.visitTime || '',
        diagnosis: diagnosisInfo.value?.primary || '',
        // 添加更多可能有用的真实字段
        visitId: record.value.visitId || record.value.id,
        patientId: patientInfo.value?.id || '',
        originalRecord: record.value // 传递完整的原始记录
      }
      
      // 将数据序列化为URL参数
      const encodedData = encodeURIComponent(JSON.stringify(referralData))
      
      // 跳转到转诊申请页面，并传递就诊记录信息
      uni.navigateTo({
        url: `/subpkg/hospital/referral-application?recordData=${encodedData}`,
        success: () => {
          console.log('成功跳转到转诊申请页面')
        },
        fail: (error) => {
          console.error('跳转转诊申请页面失败:', error)
          uni.showToast({
            title: '跳转失败，请重试',
            icon: 'none'
          })
        }
      })
    } catch (error) {
      console.error('准备转诊数据失败:', error)
      uni.showToast({
        title: '操作失败，请重试',
        icon: 'none'
      })
    }
  }

onMounted(async () => {
  await loadPatientInfo()
  loadRecordDetails()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background-color: #e3f2fd;
  padding: 20rpx;
}

.header-section {
  margin-bottom: 24rpx;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1976d2;
}

.info-card {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.card-title {
  background-color: #1976d2;
  color: white;
  font-size: 28rpx;
  font-weight: bold;
  padding: 20rpx 32rpx;
}

.info-content {
  padding: 20rpx 24rpx;
}

.info-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12rpx;
    gap: 16rpx;
  }
  
  .info-row .info-item {
    flex: 1;
    min-width: 0; /* 允许内容收缩 */
  }
  
  .info-item.full-width {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12rpx 0;
    width: 100%;
    margin-right: 0;
    border-top: 1px solid #f0f0f0;
    margin-top: 8rpx;
  }

.info-row:last-child {
  margin-bottom: 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  flex: 1;
  margin-right: 20rpx;
}

.info-item:last-child {
  margin-right: 0;
}

.info-item.full-width {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  width: 100%;
  margin-right: 0;
  border-top: 1px solid #f0f0f0;
  margin-top: 8rpx;
}

.info-label {
  color: #1976d2;
  font-size: 28rpx;
  min-width: 120rpx;
  font-weight: 500;
}

.info-value {
  color: #333;
  font-size: 28rpx;
  flex: 1;
  text-align: right;
}

.info-value.clickable {
  color: #1989fa;
  text-decoration: underline;
}

.info-value.clickable:active {
  opacity: 0.7;
}

.fee {
  color: #ff4d4f;
  font-weight: bold;
}

.status-paid {
  color: #52c41a;
}

.diagnosis-item {
  margin-bottom: 20rpx;
}

.diagnosis-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 12rpx;
  display: block;
}

.diagnosis-content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  padding-left: 16rpx;
  border-left: 4rpx solid #1976d2;
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 32rpx;
  margin-bottom: 40rpx;
}

.action-btn {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 24rpx;
  color: #333;
  font-size: 28rpx;
  padding: 24rpx 0;
  transition: all 0.3s ease;
}

.action-btn:active {
  background: #f0f0f0;
}

.doctor-btn {
  border-color: #4a90e2;
  color: #4a90e2;
}

.dept-btn {
  border-color: #4a90e2;
  color: #4a90e2;
}

.referral-btn {
  background-color: #4a90e2;
  color: white;
  border: none;
}

.referral-btn:active {
  opacity: 0.9;
  background-color: #357abd;
}

.cannot-refer-btn {
  background-color: #f5f5f5;
  color: #999;
  border: 1px solid #e8e8e8;
}

.cannot-refer-btn:active {
  background-color: #f5f5f5;
}
</style>