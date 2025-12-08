<template>
  <view class="certificate-container">
    <view class="certificate-header">
      <text class="certificate-title">患者转诊记录单</text>
    </view>
    
    <view class="certificate-content" id="certificate-content">
      <view class="form-section">
        <!-- 第一行：患者姓名、性别、年龄 -->
        <view class="form-row">
          <view class="form-field">
            <text class="field-label">患者姓名：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.name) }}</text>
            </view>
          </view>
          <view class="form-field">
            <text class="field-label">性别：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.gender) }}</text>
            </view>
          </view>
          <view class="form-field">
            <text class="field-label">年龄：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.age) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 第二行：病历号 -->
        <view class="form-row">
          <view class="form-field">
            <text class="field-label">病历号：</text>
            <view class="field-underline medical-record-no">
              <text class="field-value medical-record-value">{{ formatFieldValue(patientInfo.medicalRecordNo) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 第三行：就诊时间、就诊地点 -->
        <view class="form-row">
          <view class="form-field long">
            <text class="field-label">就诊时间：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.consultationTime) }}</text>
            </view>
          </view>
          <view class="form-field long">
            <text class="field-label">就诊地点：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.consultationLocation) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 转诊原因 -->
        <view class="form-row">
          <view class="form-field full">
            <text class="field-label">转诊原因：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.referralReason) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 主要症状 -->
        <view class="form-row">
          <view class="form-field full">
            <text class="field-label">主要症状：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.mainSymptoms) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 诊断结果 -->
        <view class="form-row">
          <view class="form-field full">
            <text class="field-label">诊断结果：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.diagnosisResult) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 转诊建议 -->
        <view class="form-row">
          <view class="form-field full">
            <text class="field-label">转诊建议：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.referralAdvice) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 目标医院 -->
        <view class="form-row">
          <view class="form-field full">
            <text class="field-label">转诊至医院：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.targetHospital) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 注意事项 -->
        <view class="form-row">
          <view class="form-field full">
            <text class="field-label">注意事项：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.notes) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 签名和日期 -->
        <view class="form-row signature-row">
          <view class="form-field signature-field">
            <text class="field-label">签名：</text>
            <view class="field-underline signature-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.signature) }}</text>
            </view>
          </view>
          <view class="form-field date-field">
            <text class="field-label">日期：</text>
            <view class="field-underline date-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.date) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 盖章处 -->
        <view class="form-row seal-row">
          <view class="seal-section">
            <view class="seal-container">
              <view class="seal-circle">
                <text class="seal-text">北京交通大学</text>
                <text class="seal-text">校医院</text>
                <text class="seal-text">转诊专用章</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <view class="instructions">
        <view class="instructions-text">
          <text>转诊单是为了方便患者转诊时，向其他医疗机构提供患者病情和治疗情况的记录。本转诊单用于描述患者的病情、诊断结果、转诊原因以及转诊建议等信息，以便接收患者的医疗机构能够更好地了解患者的病情，并提供更合适的治疗服务。</text>
        </view>
        <view class="instructions-text">
          <text>填写本转诊单时，请详细描述患者的症状和诊断结果，明确转诊原因和转诊建议，并注意填写清晰、准确、规范的信息。同时，在转诊过程中，请确保患者携带本转诊单以及相关的医疗证明文件，以便接收患者的医疗机构能够及时获取患者的相关信息。</text>
        </view>
        <view class="instructions-text">
          <text>本转诊单仅为参考模板，具体内容应根据患者的实际情况进行调整和完善。在使用本转诊单时，请遵循相关法律法规和规定，确保患者的个人隐私和信息安全。</text>
        </view>
      </view>
    </view>
    
    <view class="certificate-actions">
      <button class="action-btn" @click="downloadAsPDF">下载PDF</button>
      <button class="action-btn" @click="downloadCertificate">下载图片</button>
      <button class="action-btn" @click="previewCertificate">预览</button>
    </view>
    
    <!-- 用于生成图片的Canvas，设置为不可见 -->
    <canvas canvas-id="certificate-canvas" style="position: absolute; left: -9999px; width: 600px; height: 1000px;"></canvas>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import http from '@/utils/request'
import { getPatientReferralDetail } from '../../api/referral.js'

// 患者信息
const patientInfo = ref({
  name: '',
  gender: '',
  age: '',
  medicalRecordNo: '',
  consultationTime: '',
  consultationLocation: '北京交通大学校医院',
  referralReason: '',
  mainSymptoms: '',
  diagnosisResult: '',
  referralAdvice: '',
  targetHospital: '',
  targetDepartment: '',
  notes: '',
  signature: '',
  date: ''
})

const loading = ref(false)

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  try {
    // iOS 兼容：将 "yyyy-MM-dd HH:mm:ss" 转换为 "yyyy-MM-ddTHH:mm:ss"
    let normalizedDateStr = String(dateStr).trim()
    
    // 如果包含空格分隔日期和时间，替换为 T（ISO 8601 格式）
    if (normalizedDateStr.includes(' ') && !normalizedDateStr.includes('T')) {
      normalizedDateStr = normalizedDateStr.replace(' ', 'T')
    }
    
    // 如果只有日期部分，直接使用
    const date = new Date(normalizedDateStr)
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      // 如果解析失败，尝试其他格式
      // 尝试将 "yyyy-MM-dd HH:mm:ss" 转换为 "yyyy/MM/dd HH:mm:ss"
      normalizedDateStr = String(dateStr).trim().replace(/-/g, '/')
      const date2 = new Date(normalizedDateStr)
      if (isNaN(date2.getTime())) {
        return dateStr // 如果都失败，返回原字符串
      }
      const year = date2.getFullYear()
      const month = String(date2.getMonth() + 1).padStart(2, '0')
      const day = String(date2.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  } catch (e) {
    console.warn('日期格式化失败:', dateStr, e)
    return dateStr
  }
}

// 格式化字段值显示（处理空值和特殊字符）
const formatFieldValue = (value) => {
  if (!value || value === '' || value === null || value === undefined) {
    return ''
  }
  // 去除首尾空格
  const trimmed = String(value).trim()
  // 如果只是 "-" 或类似占位符，返回空
  if (trimmed === '-' || trimmed === '--' || trimmed === '---') {
    return ''
  }
  return trimmed
}

// 从路由参数获取转诊ID并加载详情
onLoad((options) => {
  const referralId = options?.id || options?.referralId
  if (referralId) {
    loadReferralDetail(referralId)
  } else {
    // 兼容旧版本的record参数
    loadFromRecordParam()
  }
})

// 加载转诊详情
const loadReferralDetail = async (referralId) => {
  if (!referralId) return
  
  try {
    loading.value = true
    const res = await getPatientReferralDetail(referralId)
    
    console.log('API返回数据:', res)
    
    // 响应拦截器已经解包了数据，res 直接就是 result 对象
    // 如果响应拦截器没有解包，则 res 可能是 {success, code, result, ...}
    let data = res
    
    // 如果 res 有 result 字段，说明响应拦截器没有解包，需要手动提取
    if (res && typeof res === 'object' && res.result !== undefined) {
      data = res.result
    }
    // 如果 res 有 data 字段，也尝试提取
    else if (res && typeof res === 'object' && res.data !== undefined && !res.result) {
      data = res.data
    }
    
    console.log('提取后的数据:', data)
    console.log('所有字段名:', Object.keys(data || {}))
    
    // 打印关键字段的值（用于调试）
    if (data) {
      console.log('数据字段详情:', {
        reviewDoctor: data.reviewDoctor,
        review_doctor: data.review_doctor,
        reviewDoctorName: data.reviewDoctorName,
        originalDoctorName: data.originalDoctorName,
        original_doctor_name: data.original_doctor_name,
        registrationNo: data.registrationNo,
        registration_no: data.registration_no,
        outpatientNumber: data.outpatientNumber,
        outpatient_number: data.outpatient_number,
        registrationRecordId: data.registrationRecordId,
        registration_record_id: data.registration_record_id
      })
    }
    
    if (data && (data.id || data.referralId || data.patientName)) {
      // 获取医生签名：优先使用审核医生，如果没有则使用原始医生，最后使用默认值
      // 注意：后端返回的是实体对象，字段是驼峰格式，但可能为null
      const getDoctorName = () => {
        // 先尝试审核医生（可能是null，需要检查）
        if (data.reviewDoctor && String(data.reviewDoctor).trim()) {
          return String(data.reviewDoctor).trim()
        }
        // 尝试审核医生名称字段
        if (data.reviewDoctorName && String(data.reviewDoctorName).trim()) {
          return String(data.reviewDoctorName).trim()
        }
        // 尝试原始医生名称
        if (data.originalDoctorName && String(data.originalDoctorName).trim()) {
          return String(data.originalDoctorName).trim()
        }
        // 尝试下划线格式（兼容性）
        if (data.review_doctor && String(data.review_doctor).trim()) {
          return String(data.review_doctor).trim()
        }
        if (data.original_doctor_name && String(data.original_doctor_name).trim()) {
          return String(data.original_doctor_name).trim()
        }
        // 如果都没有，显示默认值
        return '系统管理员'
      }
      
      // 获取病历号：优先使用门诊号，如果没有则使用挂号单号
      const getMedicalRecordNo = () => {
        // 优先使用门诊号（outpatient_number）
        if (data.outpatientNumber && String(data.outpatientNumber).trim()) {
          return String(data.outpatientNumber).trim()
        }
        // 尝试下划线格式
        if (data.outpatient_number && String(data.outpatient_number).trim()) {
          return String(data.outpatient_number).trim()
        }
        // 尝试其他可能的字段名
        if (data.outpatientNo && String(data.outpatientNo).trim()) {
          return String(data.outpatientNo).trim()
        }
        if (data.outpatient_no && String(data.outpatient_no).trim()) {
          return String(data.outpatient_no).trim()
        }
        // 使用挂号单号作为备选
        if (data.registrationNo && String(data.registrationNo).trim()) {
          return String(data.registrationNo).trim()
        }
        if (data.registration_no && String(data.registration_no).trim()) {
          return String(data.registration_no).trim()
        }
        // 其他可能的字段
        if (data.visitNo && String(data.visitNo).trim()) {
          return String(data.visitNo).trim()
        }
        if (data.visit_no && String(data.visit_no).trim()) {
          return String(data.visit_no).trim()
        }
        if (data.medicalRecordNo && String(data.medicalRecordNo).trim()) {
          return String(data.medicalRecordNo).trim()
        }
        if (data.medical_record_no && String(data.medical_record_no).trim()) {
          return String(data.medical_record_no).trim()
        }
        return ''
      }
      
      // 填充患者信息
      patientInfo.value = {
        name: formatFieldValue(data.patientName || data.patient_name),
        gender: formatFieldValue(data.gender || data.patientGender),
        age: formatFieldValue(data.age || data.patientAge),
        medicalRecordNo: formatFieldValue(getMedicalRecordNo()),
        consultationTime: formatDateTime(data.consultationTime || data.visitTime || data.visit_time || data.applyTime || data.apply_time || data.createTime || data.create_time),
        consultationLocation: formatFieldValue(data.consultationLocation || data.sourceHospitalName || data.originalDeptName || data.original_dept_name || '北京交通大学校医院'),
        referralReason: formatFieldValue(data.reason || data.referralReason),
        mainSymptoms: formatFieldValue(data.symptoms || data.mainSymptoms),
        diagnosisResult: formatFieldValue(data.diagnosis || data.diagnosisResult),
        referralAdvice: formatFieldValue(data.referralAdvice || data.refer_advice || data.reviewComments || data.review_comments),
        targetHospital: formatFieldValue(data.targetHospitalName || data.target_hospital_name || data.targetHospital),
        targetDepartment: formatFieldValue(data.targetDeptName || data.target_dept_name || data.targetDepartment),
        notes: formatFieldValue(data.notes || data.medicalHistory || data.medical_history),
        signature: formatFieldValue(getDoctorName()),
        date: formatDateTime(data.reviewTime || data.review_time || data.createTime || data.create_time || new Date())
      }
      
      console.log('转诊信息加载成功:', patientInfo.value)
      console.log('签名字段值:', patientInfo.value.signature)
      console.log('病历号字段值:', patientInfo.value.medicalRecordNo)
    } else {
      console.warn('转诊数据格式不正确，无法识别数据:', res)
      uni.showToast({
        title: '获取转诊信息失败',
        icon: 'none'
      })
      // 如果API失败，尝试从路由参数读取
      loadFromRecordParam()
    }
  } catch (error) {
    console.error('加载转诊详情失败:', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
    // 如果API失败，尝试从路由参数读取
    loadFromRecordParam()
  } finally {
    loading.value = false
  }
}

// 从路由参数获取转诊记录（兼容旧版本）
const loadFromRecordParam = () => {
  try {
    if (typeof getCurrentPages === 'function') {
      const pages = getCurrentPages()
      if (pages && pages.length > 0) {
        const currentPage = pages[pages.length - 1]
        if (currentPage?.options?.record) {
          try {
            const decodeURIComponentFn = typeof decodeURIComponent === 'function' ? decodeURIComponent : (str) => str
            const JSONParseFn = typeof JSON.parse === 'function' ? JSON.parse : () => {}
            
            const recordStr = decodeURIComponentFn(currentPage.options.record)
            const record = JSONParseFn(recordStr)
            
            if (record) {
              // 如果有ID，尝试加载完整详情
              if (record.id || record.referralId) {
                loadReferralDetail(record.id || record.referralId)
              } else {
                // 否则使用记录中的数据
                patientInfo.value = {
                  name: record.patientName || '',
                  gender: record.gender || record.patientGender || '',
                  age: record.age || record.patientAge || '',
                  // 病历号使用患者的门诊号
                  medicalRecordNo: formatFieldValue(record.outpatientNumber || record.outpatient_number || record.outpatientNo || record.outpatient_no || record.visitNo || record.visit_no || record.registrationNo || record.registration_no || record.cardNumber || record.card_number || record.medicalRecordNo),
                  consultationTime: formatDateTime(record.consultationTime || record.applyTime),
                  consultationLocation: record.consultationLocation || '北京交通大学校医院',
                  referralReason: record.reason || record.referralReason || '',
                  mainSymptoms: record.symptoms || record.mainSymptoms || '',
                  diagnosisResult: record.diagnosis || record.diagnosisResult || '',
                  referralAdvice: record.referralAdvice || '',
                  targetHospital: record.targetHospital || record.targetHospitalName || '',
                  targetDepartment: record.targetDepartment || record.targetDeptName || '',
                  notes: record.notes || record.medicalHistory || '',
                  // 签名使用医生姓名
                  signature: formatFieldValue(record.reviewDoctor || record.reviewDoctorName || record.doctorName || record.doctor_name || record.physicianName),
                  date: formatDateTime(record.reviewTime || record.applyTime || new Date())
                }
              }
            }
          } catch (error) {
            console.error('解析转诊记录失败:', error)
          }
        }
      }
    }
  } catch (error) {
    console.error('获取路由参数失败:', error)
  }
}

onMounted(() => {
  // 如果还没有加载数据，尝试从路由参数加载
  if (!patientInfo.value.name && !loading.value) {
    loadFromRecordParam()
  }
})

// 下载为PDF
const downloadAsPDF = async () => {
  try {
    uni.showLoading({
      title: '正在生成PDF...'
    })
    
    // 首先生成图片
    const imagePath = await generateCertificateImage()
    if (!imagePath) {
      uni.hideLoading()
      return
    }
    
    // 尝试使用后端API转换为PDF
    try {
      const uploadResult = await uploadImageAndConvertToPDF(imagePath)
      if (uploadResult && uploadResult.pdfUrl) {
        // 后端已经返回完整的PDF URL，直接使用
        const pdfUrl = uploadResult.pdfUrl
        
        // 直接打开PDF，不保存到本地（避免存储限制）
        uni.hideLoading()
        uni.showToast({
          title: '正在打开PDF...',
          icon: 'loading',
          duration: 1000
        })
        
        // 下载PDF文件到临时目录
        uni.downloadFile({
          url: pdfUrl,
          success: (res) => {
            if (res.statusCode === 200) {
              // 直接打开PDF，不保存到本地
              uni.openDocument({
                filePath: res.tempFilePath,
                success: () => {
                  uni.hideLoading()
                  uni.showToast({
                    title: 'PDF已打开',
                    icon: 'success',
                    duration: 2000
                  })
                },
                fail: (err) => {
                  uni.hideLoading()
                  console.error('打开PDF失败:', err)
                  // 如果打开失败，尝试保存
                  uni.saveFile({
                    tempFilePath: res.tempFilePath,
                    success: (saveRes) => {
                      uni.showToast({
                        title: 'PDF已保存',
                        icon: 'success',
                        duration: 2000
                      })
                    },
                    fail: (saveErr) => {
                      console.error('保存PDF失败:', saveErr)
                      uni.showToast({
                        title: 'PDF文件过大，请使用浏览器打开',
                        icon: 'none',
                        duration: 3000
                      })
                      // 提供下载链接
                      uni.setClipboardData({
                        data: pdfUrl,
                        success: () => {
                          uni.showModal({
                            title: '提示',
                            content: 'PDF链接已复制到剪贴板，请在浏览器中打开',
                            showCancel: false
                          })
                        }
                      })
                    }
                  })
                }
              })
            } else {
              throw new Error('下载失败')
            }
          },
          fail: (err) => {
            throw err
          }
        })
      } else {
        throw new Error('后端未返回PDF URL')
      }
    } catch (backendError) {
      // 后端API失败，显示错误提示
      uni.hideLoading()
      console.error('PDF生成失败:', backendError)
      
      let errorMessage = 'PDF生成失败，请稍后重试'
      if (backendError && backendError.message) {
        errorMessage = backendError.message
      } else if (backendError && backendError.errMsg) {
        errorMessage = backendError.errMsg
      }
      
      uni.showModal({
        title: 'PDF生成失败',
        content: errorMessage + '\n\n请检查网络连接或联系管理员。',
        showCancel: true,
        cancelText: '取消',
        confirmText: '重试',
        success: (res) => {
          if (res.confirm) {
            // 用户选择重试
            downloadAsPDF()
          }
        }
      })
    }
  } catch (err) {
    uni.hideLoading()
    console.error('生成PDF异常:', err)
    uni.showToast({
      title: '生成PDF失败，请重试',
      icon: 'none',
      duration: 2000
    })
  }
}

// 上传图片并转换为PDF（需要后端支持）
const uploadImageAndConvertToPDF = async (imagePath) => {
  return new Promise((resolve, reject) => {
    // 生成文件名（包含患者姓名和日期）
    const patientName = formatFieldValue(patientInfo.value.name) || '患者'
    const dateStr = formatFieldValue(patientInfo.value.date) || new Date().toISOString().split('T')[0]
    const filename = `转诊记录单_${patientName}_${dateStr}.pdf`
    
    // 自定义存储路径（可以根据需要修改）
    const customPath = 'referral-certificates'
    
    console.log('上传PDF参数:', {
      filename,
      customPath,
      imagePath: imagePath.substring(0, 50) + '...'
    })
    
    // 使用http.upload方法，它会自动处理baseURL和token
    http.upload('/api/referral/convert-to-pdf', imagePath, {
      name: 'image',
      formData: {
        'filename': filename,
        'customPath': customPath
      },
      timeout: 30000
    }).then((response) => {
      console.log('PDF上传响应:', response)
      // 后端返回Result格式：{ success: true, result: { pdfUrl: "...", filename: "..." } }
      if (response && response.success && response.result && response.result.pdfUrl) {
        resolve({
          pdfUrl: response.result.pdfUrl,
          filename: response.result.filename
        })
      } else if (response && response.pdfUrl) {
        // 兼容直接返回pdfUrl的情况
        resolve({
          pdfUrl: response.pdfUrl,
          filename: response.filename
        })
      } else if (response && response.message) {
        // 后端返回错误信息
        reject(new Error(response.message || 'PDF生成失败'))
            } else {
        reject(new Error('服务器未返回PDF URL，请稍后重试'))
      }
    }).catch((err) => {
      // 处理错误，提取错误信息
      let errorMessage = 'PDF生成失败'
      if (err && err.message) {
        errorMessage = err.message
      } else if (err && err.errMsg) {
        errorMessage = err.errMsg
      } else if (err && typeof err === 'string') {
        errorMessage = err
      }
      
      // 如果是网络错误或API不存在
      if (errorMessage.includes('No static resource') || errorMessage.includes('404')) {
        errorMessage = 'PDF生成接口不存在，请联系管理员配置'
      } else if (errorMessage.includes('timeout') || errorMessage.includes('网络')) {
        errorMessage = '网络超时，请检查网络连接后重试'
      }
      
      reject(new Error(errorMessage))
    })
  })
}

// 生成转诊单图片
const generateCertificateImage = () => {
  return new Promise((resolve, reject) => {
    try {
      // 使用uni.canvasToTempFilePath将内容转换为图片
      uni.createSelectorQuery().select('#certificate-content').fields({
        size: true,
        scrollOffset: true
      }).exec((res) => {
        try {
          if (!res || !res[0]) {
            reject(new Error('获取页面内容失败'))
            return
          }
          
          // 使用较小的Canvas尺寸以减小文件大小
          // uni-app中Canvas使用px单位，需要与实际绘制尺寸一致
          const canvasWidth = 600 // 降低宽度以减小文件大小
          // 计算实际需要的字段行数（字体缩小到75%后）：
          // 标题区域：50px（标题+间距）
          // 12行字段：12 * 42 = 504px
          // 签名日期区域：35（间距）+ 42（行高）+ 20（间距）= 97px
          // 盖章区域：75（印章大小）+ 20（底部间距）= 95px
          // 总计：50 + 504 + 97 + 95 = 746px，加上安全边距约100px
          const estimatedHeight = 50 + (12 * 42) + 97 + 95 + 100 // 标题 + 12行字段 + 签名日期 + 盖章 + 安全边距
          const canvasHeight = Math.max(estimatedHeight, 850) // 确保高度足够，至少850px
          
          // 确保createCanvasContext可用
          if (typeof uni.createCanvasContext === 'function') {
            const canvas = uni.createCanvasContext('certificate-canvas')
            
            // 设置画布背景为白色
            canvas.setFillStyle('#ffffff')
            canvas.fillRect(0, 0, canvasWidth, canvasHeight)
            
            // 绘制文本内容
            drawCertificateContent(canvas, canvasWidth, canvasHeight)
            
            canvas.draw(false, () => {
              setTimeout(() => {
                try {
                  // 生成图片，确保完整显示所有内容
                  uni.canvasToTempFilePath({
                    canvasId: 'certificate-canvas',
                    width: canvasWidth,
                    height: canvasHeight,
                    x: 0,
                    y: 0,
                    destWidth: canvasWidth, // 使用原始宽度，确保清晰度
                    destHeight: canvasHeight, // 使用原始高度，确保完整显示
                    fileType: 'jpg', // 使用jpg格式，文件更小
                    quality: 0.85, // 质量85%，平衡清晰度和文件大小
                    success: (tempFilePath) => {
                      resolve(tempFilePath.tempFilePath)
                    },
                    fail: (err) => {
                      console.error('生成图片失败:', err)
                      reject(err)
                    }
                  })
                } catch (canvasToTempErr) {
                  console.error('canvasToTempFilePath调用异常:', canvasToTempErr)
                  reject(canvasToTempErr)
                }
              }, 1000)
            })
          } else {
            reject(new Error('当前环境不支持canvas操作'))
          }
        } catch (canvasErr) {
          console.error('canvas操作异常:', canvasErr)
          reject(canvasErr)
        }
      })
    } catch (err) {
      reject(err)
    }
  })
}

// 下载转诊证明（图片）
const downloadCertificate = async () => {
  try {
    uni.showLoading({
      title: '正在生成转诊证明...'
    })
    
    const imagePath = await generateCertificateImage()
    if (!imagePath) {
      uni.hideLoading()
      return
    }
    
    // 保存图片到相册
    uni.saveImageToPhotosAlbum({
      filePath: imagePath,
      success: () => {
        uni.hideLoading()
        uni.showToast({
          title: '转诊证明已保存到相册',
          icon: 'success'
        })
      },
      fail: (err) => {
        uni.hideLoading()
        console.error('保存图片失败:', err)
        // 如果是用户拒绝授权，提示用户打开设置
        if (err.errMsg && err.errMsg.indexOf('auth deny') !== -1) {
          uni.showModal({
            title: '提示',
            content: '需要获取相册权限才能保存图片，请在设置中允许',
            success: (res) => {
              if (res.confirm && typeof uni.openSetting === 'function') {
                uni.openSetting()
              }
            }
          })
        } else {
          uni.showToast({
            title: '保存失败，请重试',
            icon: 'none'
          })
        }
      }
    })
  } catch (err) {
    uni.hideLoading()
    console.error('下载转诊证明异常:', err)
    uni.showToast({
      title: '下载失败，请重试',
      icon: 'none'
    })
  }
}

// 绘制转诊单内容到Canvas
const drawCertificateContent = (canvas, width, height) => {
  // 确保使用正确的宽度，避免内容超出Canvas范围
  const actualWidth = Math.min(width, 600) // 确保不超过Canvas宽度
  const margin = 40
  const contentWidth = actualWidth - margin * 2
  let yPosition = margin + 30
  
  // 设置字体（缩小到75%，既清晰又能完整显示）
  canvas.setFontSize(22)
  canvas.setFillStyle('#000000')
  
  // 绘制标题
  canvas.setTextAlign('center')
  canvas.setFontSize(27)
  const titleText = '患者转诊记录单'
  canvas.fillText(titleText, actualWidth / 2, yPosition)
  yPosition += 50
  
  // 绘制患者信息
  canvas.setTextAlign('left')
  canvas.setFontSize(21)
  
  const lineHeight = 42
  const fontSize = 21
  const textBaselineOffset = 4 // 文字基线相对于yPosition的偏移
  const underlineOffset = 9 // 下划线相对于文字基线的偏移（下划线在文字下方）
  const underlineLength = 200 // 下划线长度
  
  // 第一行：患者姓名、性别
  let xPos = margin
  canvas.setFillStyle('#000000')
  const label1 = '患者姓名'
  canvas.fillText(label1, xPos, yPosition)
  xPos += 120
  canvas.setStrokeStyle('#000000')
  canvas.setLineWidth(1.5)
  // 下划线位置：文字基线 + 偏移
  const underlineY1 = yPosition + underlineOffset
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY1)
  canvas.lineTo(xPos + underlineLength, underlineY1)
  canvas.stroke()
  const nameText = formatFieldValue(patientInfo.value.name) || ''
  if (nameText) {
    // 确保文本不会超出下划线范围
    const maxNameWidth = underlineLength - 10
    const displayName = nameText.length > 8 ? nameText.substring(0, 8) : nameText
    // 文字绘制在下划线上方，使用相同的基线
    canvas.fillText(displayName, xPos + 5, yPosition)
  }
  
  xPos += 240
  const label2 = '性别'
  canvas.fillText(label2, xPos, yPosition)
  xPos += 80
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY1)
  canvas.lineTo(xPos + 80, underlineY1)
  canvas.stroke()
  const genderText = formatFieldValue(patientInfo.value.gender) || ''
  if (genderText) {
    canvas.fillText(genderText, xPos + 5, yPosition)
  }
  
  yPosition += lineHeight
  
  // 第二行：年龄
  xPos = margin
  const label3 = '年龄'
  canvas.fillText(label3, xPos, yPosition)
  xPos += 80
  const underlineY2 = yPosition + underlineOffset
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY2)
  canvas.lineTo(xPos + 80, underlineY2)
  canvas.stroke()
  const ageText = formatFieldValue(patientInfo.value.age) || ''
  if (ageText) {
    canvas.fillText(ageText, xPos + 5, yPosition)
  }
  
  yPosition += lineHeight
  
  // 第三行：病历号（单独一行）
  xPos = margin
  const label4 = '病历号'
  canvas.fillText(label4, xPos, yPosition)
  xPos += 100
  const underlineY3_record = yPosition + underlineOffset
  const recordNoUnderlineLength = actualWidth - xPos - margin - 10
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY3_record)
  canvas.lineTo(xPos + recordNoUnderlineLength, underlineY3_record)
  canvas.stroke()
  const recordNoText = formatFieldValue(patientInfo.value.medicalRecordNo) || ''
  if (recordNoText) {
    // 病历号使用稍大的字体
    canvas.setFontSize(23)
    canvas.fillText(recordNoText, xPos + 5, yPosition)
    // 恢复原来的字体大小
    canvas.setFontSize(fontSize)
  }
  
  yPosition += lineHeight
  
  // 第四行：就诊时间
  xPos = margin
  const label5 = '就诊时间'
  canvas.fillText(label5, xPos, yPosition)
  xPos += 120
  const underlineY3 = yPosition + underlineOffset
  const timeUnderlineLength = actualWidth - xPos - margin - 10
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY3)
  canvas.lineTo(xPos + timeUnderlineLength, underlineY3)
  canvas.stroke()
  const timeText = formatFieldValue(patientInfo.value.consultationTime) || ''
  if (timeText) {
    // 确保文本不会超出边界
    const maxTimeWidth = timeUnderlineLength - 10
    const displayTime = timeText.length > 20 ? timeText.substring(0, 20) : timeText
    canvas.fillText(displayTime, xPos + 5, yPosition)
  }
  
  yPosition += lineHeight
  
  // 第四行：就诊地点
  xPos = margin
  const label6 = '就诊地点'
  canvas.fillText(label6, xPos, yPosition)
  xPos += 120
  const underlineY4 = yPosition + underlineOffset
  const locationUnderlineLength = actualWidth - xPos - margin - 10
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY4)
  canvas.lineTo(xPos + locationUnderlineLength, underlineY4)
  canvas.stroke()
  const locationText = formatFieldValue(patientInfo.value.consultationLocation) || ''
  if (locationText) {
    // 确保文本不会超出边界
    const maxLocationWidth = locationUnderlineLength - 10
    const displayLocation = locationText.length > 25 ? locationText.substring(0, 25) : locationText
    canvas.fillText(displayLocation, xPos + 5, yPosition)
  }
  
  yPosition += lineHeight
  
  // 转诊原因
  xPos = margin
  const label7 = '转诊原因'
  canvas.fillText(label7, xPos, yPosition)
  xPos += 120
  const underlineY5 = yPosition + underlineOffset
  const fullWidthUnderline = actualWidth - xPos - margin - 10
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY5)
  canvas.lineTo(xPos + fullWidthUnderline, underlineY5)
  canvas.stroke()
  const reasonText = formatFieldValue(patientInfo.value.referralReason) || ''
  if (reasonText) {
    // 处理长文本，确保不超出边界
    const maxReasonWidth = fullWidthUnderline - 10
    const displayReason = reasonText.length > 30 ? reasonText.substring(0, 30) : reasonText
    canvas.fillText(displayReason, xPos + 5, yPosition)
  }
  yPosition += lineHeight
  
  // 主要症状
  xPos = margin
  const label8 = '主要症状'
  canvas.fillText(label8, xPos, yPosition)
  xPos += 120
  const underlineY6 = yPosition + underlineOffset
  const fullWidthUnderline2 = actualWidth - xPos - margin - 10
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY6)
  canvas.lineTo(xPos + fullWidthUnderline2, underlineY6)
  canvas.stroke()
  const symptomsText = formatFieldValue(patientInfo.value.mainSymptoms) || ''
  if (symptomsText) {
    const displaySymptoms = symptomsText.length > 30 ? symptomsText.substring(0, 30) : symptomsText
    canvas.fillText(displaySymptoms, xPos + 5, yPosition)
  }
  yPosition += lineHeight
  
  // 诊断结果
  xPos = margin
  const label9 = '诊断结果'
  canvas.fillText(label9, xPos, yPosition)
  xPos += 120
  const underlineY7 = yPosition + underlineOffset
  const fullWidthUnderline3 = actualWidth - xPos - margin - 10
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY7)
  canvas.lineTo(xPos + fullWidthUnderline3, underlineY7)
  canvas.stroke()
  const diagnosisText = formatFieldValue(patientInfo.value.diagnosisResult) || ''
  if (diagnosisText) {
    const displayDiagnosis = diagnosisText.length > 30 ? diagnosisText.substring(0, 30) : diagnosisText
    canvas.fillText(displayDiagnosis, xPos + 5, yPosition)
  }
  yPosition += lineHeight
  
  // 转诊建议
  xPos = margin
  const label10 = '转诊建议'
  canvas.fillText(label10, xPos, yPosition)
  xPos += 120
  const underlineY8 = yPosition + underlineOffset
  const fullWidthUnderline4 = actualWidth - xPos - margin - 10
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY8)
  canvas.lineTo(xPos + fullWidthUnderline4, underlineY8)
  canvas.stroke()
  const adviceText = formatFieldValue(patientInfo.value.referralAdvice) || ''
  if (adviceText) {
    const displayAdvice = adviceText.length > 30 ? adviceText.substring(0, 30) : adviceText
    canvas.fillText(displayAdvice, xPos + 5, yPosition)
  }
  yPosition += lineHeight
  
  // 转诊至医院
  xPos = margin
  const label11 = '转诊至医院'
  canvas.fillText(label11, xPos, yPosition)
  xPos += 140
  const underlineY9 = yPosition + underlineOffset
  const hospitalUnderlineLength = actualWidth - xPos - margin - 10
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY9)
  canvas.lineTo(xPos + hospitalUnderlineLength, underlineY9)
  canvas.stroke()
  const hospitalText = formatFieldValue(patientInfo.value.targetHospital) || ''
  if (hospitalText) {
    const displayHospital = hospitalText.length > 25 ? hospitalText.substring(0, 25) : hospitalText
    canvas.fillText(displayHospital, xPos + 5, yPosition)
  }
  yPosition += lineHeight
  
  // 注意事项
  xPos = margin
  const label13 = '注意事项'
  canvas.fillText(label13, xPos, yPosition)
  xPos += 120
  const underlineY11 = yPosition + underlineOffset
  const fullWidthUnderline5 = actualWidth - xPos - margin - 10
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY11)
  canvas.lineTo(xPos + fullWidthUnderline5, underlineY11)
  canvas.stroke()
  const notesText = formatFieldValue(patientInfo.value.notes) || ''
  if (notesText) {
    const displayNotes = notesText.length > 30 ? notesText.substring(0, 30) : notesText
    canvas.fillText(displayNotes, xPos + 5, yPosition)
  }
  yPosition += lineHeight + 35
  
  // 签名和日期
  const signatureY = yPosition
  xPos = margin
  const label14 = '签名'
  canvas.fillText(label14, xPos, signatureY)
  xPos += 80
  const underlineY12 = signatureY + underlineOffset
  // 签名占据左侧一半空间
  const signatureUnderlineLength = (actualWidth - margin * 2 - 80 - 20) / 2
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY12)
  canvas.lineTo(xPos + signatureUnderlineLength, underlineY12)
  canvas.stroke()
  const signatureText = formatFieldValue(patientInfo.value.signature) || ''
  // 即使签名为空也显示默认值
  const displaySignature = signatureText || '系统管理员'
  if (displaySignature) {
    const finalSignature = displaySignature.length > 20 ? displaySignature.substring(0, 20) : displaySignature
    canvas.fillText(finalSignature, xPos + 5, signatureY)
  }
  
  xPos += signatureUnderlineLength + 40
  const label15 = '日期'
  canvas.fillText(label15, xPos, signatureY)
  xPos += 80
  // 日期占据右侧剩余空间
  const dateUnderlineLength = actualWidth - xPos - margin - 10
  canvas.beginPath()
  canvas.moveTo(xPos, underlineY12)
  canvas.lineTo(xPos + dateUnderlineLength, underlineY12)
  canvas.stroke()
  const dateText = formatFieldValue(patientInfo.value.date) || ''
  if (dateText) {
    canvas.fillText(dateText, xPos + 5, signatureY)
  }
  
  yPosition += lineHeight + 20
  
  // 绘制盖章处（在签名和日期下方）
  const sealY = yPosition
  const sealX = actualWidth - margin - 90
  const sealSize = 75
  
  // 绘制印章圆形边框（使用多个小线段模拟圆形）
  canvas.setStrokeStyle('#d32f2f')
  canvas.setLineWidth(2.5)
  const centerX = sealX
  const centerY = sealY + sealSize / 2
  const radius = sealSize / 2 - 3
  
  // 绘制圆形边框（使用多个点连接成圆形）
  canvas.beginPath()
  for (let i = 0; i <= 360; i += 5) {
    const angle = (i * Math.PI) / 180
    const x = centerX + radius * Math.cos(angle)
    const y = centerY + radius * Math.sin(angle)
    if (i === 0) {
      canvas.moveTo(x, y)
    } else {
      canvas.lineTo(x, y)
    }
  }
  canvas.stroke()
  
  // 绘制印章文字（缩小到75%）
  canvas.setFontSize(14)
  canvas.setFillStyle('#d32f2f')
  canvas.setTextAlign('center')
  canvas.fillText('北京交通大学', centerX, centerY - 15)
  canvas.fillText('校医院', centerX, centerY)
  canvas.fillText('转诊专用章', centerX, centerY + 15)
  
  yPosition = sealY + sealSize + 20
  
  // 记录最终绘制位置，用于调试
  console.log('Canvas绘制完成，最终yPosition:', yPosition, 'Canvas高度:', height)
  
  // 如果内容超出Canvas高度，输出警告
  if (yPosition > height - 50) {
    console.warn('警告：绘制内容可能超出Canvas高度，建议增加Canvas高度')
  }
  
  // 绘制分隔线（可选，根据实际需求）
  // yPosition += 40
  // canvas.setStrokeStyle('#e5e5e5')
  // canvas.setLineWidth(1)
  // canvas.beginPath()
  // canvas.moveTo(margin, yPosition)
  // canvas.lineTo(width - margin, yPosition)
  // canvas.stroke()
  // yPosition += 40
  
  // 绘制说明文字（可选，根据实际需求）
  // canvas.setFontSize(22)
  // canvas.setFillStyle('#666666')
  // const instructions = [
  //   '转诊单是为了方便患者转诊时，向其他医疗机构提供患者病情和治疗情况的记录。'
  // ]
  // instructions.forEach((text, index) => {
  //   const maxCharsPerLine = Math.floor((width - margin * 2) / 14)
  //   const lines = wrapText(text, maxCharsPerLine)
  //   lines.forEach(line => {
  //     if (yPosition < height - 100) {
  //       canvas.fillText(line, margin, yPosition)
  //       yPosition += 38
  //     }
  //   })
  // })
}

// 文本换行处理
const wrapText = (text, maxLength) => {
  const lines = []
  let currentLine = ''
  
  for (let i = 0; i < text.length; i++) {
    currentLine += text[i]
    if (currentLine.length >= maxLength) {
      lines.push(currentLine)
      currentLine = ''
    }
  }
  
  if (currentLine) {
    lines.push(currentLine)
  }
  
  return lines
}

// 预览转诊证明
const previewCertificate = async () => {
  try {
    uni.showLoading({
      title: '正在生成预览...'
    })
    
    // 生成转诊证明图片
    const imagePath = await generateCertificateImage()
    if (!imagePath) {
      uni.hideLoading()
      return
    }
    
    // 使用uni.previewImage预览图片
    uni.previewImage({
      urls: [imagePath],
      current: imagePath,
      success: () => {
        uni.hideLoading()
      },
      fail: (err) => {
        uni.hideLoading()
        console.error('预览失败:', err)
        uni.showToast({
          title: '预览失败，请重试',
          icon: 'none'
        })
      }
    })
  } catch (err) {
    uni.hideLoading()
    console.error('预览转诊证明异常:', err)
    uni.showToast({
      title: '预览失败，请重试',
      icon: 'none'
    })
  }
}
</script>

<style scoped>
.certificate-container {
  padding: 40rpx 30rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.certificate-header {
  text-align: center;
  margin-bottom: 30rpx;
  padding: 40rpx 20rpx;
  background-color: #fff;
  border-bottom: 2rpx solid #e0e0e0;
}

.certificate-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  letter-spacing: 2rpx;
}

.certificate-content {
  background-color: #fff;
  padding: 50rpx 40rpx;
  line-height: 1.8;
  max-width: 100%;
  border: 1rpx solid #e0e0e0;
}

.form-section {
  width: 100%;
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 30rpx;
  align-items: flex-end;
}

.form-field {
  display: flex;
  align-items: center;
  margin-right: 20rpx;
  margin-bottom: 20rpx;
}

.form-field.long {
  flex: 1;
  min-width: 300rpx;
}

.form-field.full {
  width: 100%;
  margin-right: 0;
}

.signature-row {
  margin-top: 30rpx;
  justify-content: space-between;
  align-items: flex-end;
  gap: 30rpx;
}

.signature-field {
  flex: 1;
  min-width: 200rpx;
}

.signature-underline {
  min-width: 200rpx;
}

.date-field {
  flex: 1;
  min-width: 200rpx;
}

.date-underline {
  min-width: 200rpx;
}

.seal-row {
  margin-top: 20rpx;
  justify-content: flex-end;
  align-items: center;
  padding-right: 0;
}

.seal-section {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.seal-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.seal-circle {
  width: 180rpx;
  height: 180rpx;
  border: 2rpx solid #d32f2f;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20rpx;
}

.seal-text {
  font-size: 17rpx;
  color: #d32f2f;
  font-weight: 500;
  line-height: 1.4;
  text-align: center;
}

.field-label {
  font-size: 23rpx;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  margin-right: 12rpx;
  min-width: fit-content;
}

.field-underline {
  position: relative;
  min-width: 120rpx;
  border-bottom: 1rpx solid #333;
  padding-bottom: 8rpx;
  flex: 1;
  display: flex;
  align-items: flex-end;
}

.form-field.full .field-underline {
  width: 100%;
}

.field-value {
  font-size: 23rpx;
  color: #333;
  position: absolute;
  left: 5rpx;
  bottom: 6rpx;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: calc(100% - 10rpx);
  line-height: 1.4;
  font-weight: 400;
  min-height: 1.4em;
}

/* 病历号特殊样式 - 增大字体 */
.medical-record-no {
  min-width: 400rpx;
  flex: 1;
}

.medical-record-value {
  font-size: 24rpx !important;
  font-weight: 600 !important;
  letter-spacing: 1rpx;
}

/* 空值时的样式 - 确保下划线可见 */
.field-underline {
  min-height: 40rpx;
}

.field-value:empty {
  min-height: 1.4em;
  display: block;
}

.instructions {
  border-top: 1rpx solid #e0e0e0;
  padding-top: 30rpx;
  margin-top: 30rpx;
}

.instructions-text {
  font-size: 18rpx;
  color: #666;
  margin-bottom: 15rpx;
  text-indent: 0;
  line-height: 1.6;
  text-align: justify;
}

.certificate-actions {
  display: flex;
  justify-content: space-around;
  gap: 20rpx;
  margin-top: 40rpx;
  margin-bottom: 30rpx;
  padding: 0 30rpx;
  flex-wrap: wrap;
}

.action-btn {
  flex: 1;
  padding: 16rpx 30rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
  background-color: #1890ff;
  color: #fff;
  text-align: center;
  height: auto;
  line-height: 1.4;
  min-width: 200rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:active {
  background-color: #40a9ff;
  border-color: #40a9ff;
}
</style>