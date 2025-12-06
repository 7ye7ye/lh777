<template>
  <view class="certificate-container">
    <view class="certificate-header">
      <text class="certificate-title">患者转诊记录单</text>
    </view>
    
    <view class="certificate-content" id="certificate-content">
      <view class="form-section">
        <!-- 第一行：患者姓名、性别、年龄、病历号 -->
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
          <view class="form-field">
            <text class="field-label">病历号：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.medicalRecordNo) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 第二行：就诊时间、就诊地点 -->
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
        
        <!-- 目标医院和科室 -->
        <view class="form-row">
          <view class="form-field long">
            <text class="field-label">转诊至医院：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.targetHospital) }}</text>
            </view>
          </view>
          <view class="form-field long">
            <text class="field-label">转诊至科室：</text>
            <view class="field-underline">
              <text class="field-value">{{ formatFieldValue(patientInfo.targetDepartment) }}</text>
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
          <view class="signature-section">
            <view class="form-field">
              <text class="field-label">签名：</text>
              <view class="field-underline">
                <text class="field-value">{{ formatFieldValue(patientInfo.signature) }}</text>
              </view>
            </view>
            <view class="form-field">
              <text class="field-label">日期：</text>
              <view class="field-underline">
                <text class="field-value">{{ formatFieldValue(patientInfo.date) }}</text>
              </view>
            </view>
          </view>
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
      <button class="action-btn download-pdf-btn" @click="downloadAsPDF">下载为PDF</button>
      <button class="action-btn download-img-btn" @click="downloadCertificate">下载为图片</button>
      <button class="action-btn share-btn" @click="shareCertificate">分享</button>
    </view>
    
    <!-- 用于生成图片的Canvas，设置为不可见 -->
    <canvas canvas-id="certificate-canvas" style="position: absolute; left: -9999rpx; width: 750rpx; height: 2000rpx;"></canvas>
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
    
    if (data && (data.id || data.referralId || data.patientName)) {
      // 填充患者信息
      patientInfo.value = {
        name: formatFieldValue(data.patientName || data.patient_name),
        gender: formatFieldValue(data.gender || data.patientGender),
        age: formatFieldValue(data.age || data.patientAge),
        // 病历号使用患者的就诊号
        medicalRecordNo: formatFieldValue(data.visitNo || data.registrationNo || data.medicalRecordNo || data.medical_record_no || data.visitNumber),
        consultationTime: formatDateTime(data.consultationTime || data.visitTime || data.applyTime || data.createTime),
        consultationLocation: formatFieldValue(data.consultationLocation || data.sourceHospitalName || '北京交通大学校医院'),
        referralReason: formatFieldValue(data.reason || data.referralReason),
        mainSymptoms: formatFieldValue(data.symptoms || data.mainSymptoms),
        diagnosisResult: formatFieldValue(data.diagnosis || data.diagnosisResult),
        referralAdvice: formatFieldValue(data.referralAdvice || data.reviewComments),
        targetHospital: formatFieldValue(data.targetHospitalName || data.targetHospital),
        targetDepartment: formatFieldValue(data.targetDeptName || data.targetDepartment),
        notes: formatFieldValue(data.notes || data.medicalHistory),
        // 签名使用医生姓名
        signature: formatFieldValue(data.reviewDoctor || data.doctorName || data.doctor_name),
        date: formatDateTime(data.reviewTime || data.createTime || new Date())
      }
      
      console.log('转诊信息加载成功:', patientInfo.value)
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
                  medicalRecordNo: record.medicalRecordNo || '',
                  consultationTime: formatDateTime(record.consultationTime || record.applyTime),
                  consultationLocation: record.consultationLocation || '北京交通大学校医院',
                  referralReason: record.reason || record.referralReason || '',
                  mainSymptoms: record.symptoms || record.mainSymptoms || '',
                  diagnosisResult: record.diagnosis || record.diagnosisResult || '',
                  referralAdvice: record.referralAdvice || '',
                  targetHospital: record.targetHospital || record.targetHospitalName || '',
                  targetDepartment: record.targetDepartment || record.targetDeptName || '',
                  notes: record.notes || record.medicalHistory || '',
                  signature: record.reviewDoctor || record.doctorName || '',
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
        
        // 下载PDF文件
        uni.downloadFile({
          url: pdfUrl,
          success: (res) => {
            if (res.statusCode === 200) {
              // 保存文件
              uni.saveFile({
                tempFilePath: res.tempFilePath,
                success: (saveRes) => {
                  uni.hideLoading()
                  uni.showToast({
                    title: 'PDF已保存',
                    icon: 'success',
                    duration: 2000
                  })
                  // 打开文件
                  setTimeout(() => {
                    uni.openDocument({
                      filePath: saveRes.savedFilePath,
                      success: () => {
                        console.log('打开PDF成功')
                      },
                      fail: (err) => {
                        console.log('打开PDF失败，文件已保存:', err)
                      }
                    })
                  }, 500)
                },
                fail: (err) => {
                  uni.hideLoading()
                  console.error('保存PDF失败:', err)
                  uni.showToast({
                    title: '保存PDF失败',
                    icon: 'none'
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
    // 使用http.upload方法，它会自动处理baseURL和token
    http.upload('/api/referral/convert-to-pdf', imagePath, {
      name: 'image',
      formData: {
        'filename': `转诊记录单_${new Date().getTime()}.pdf`
      },
      timeout: 30000
    }).then((response) => {
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
          
          const { width, height } = res[0]
          const canvasWidth = 750
          // 根据内容计算合适的高度，确保所有内容都能显示
          const canvasHeight = Math.max(2800, height * 2.5) // 增加高度以确保印章和所有内容都能显示
          
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
                  uni.canvasToTempFilePath({
                    canvasId: 'certificate-canvas',
                    width: canvasWidth,
                    height: canvasHeight,
                    x: 0,
                    y: 0,
                    destWidth: canvasWidth * 2, // 提高分辨率
                    destHeight: canvasHeight * 2,
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
  const margin = 80
  const contentWidth = width - margin * 2
  let yPosition = margin + 60
  
  // 设置字体
  canvas.setFontSize(36)
  canvas.setFillStyle('#1a1a1a')
  
  // 绘制标题
  canvas.setTextAlign('center')
  canvas.setFontSize(48)
  // 注意：uni-app canvas不支持setFontWeight，使用更大字体来突出标题
  canvas.fillText('患者转诊记录单', width / 2, yPosition)
  yPosition += 120
  
  // 绘制患者信息
  canvas.setTextAlign('left')
  canvas.setFontSize(32)
  
  const lineHeight = 80
  const fieldSpacing = 30
  
  // 第一行：患者姓名、性别、年龄、病历号
  let xPos = margin
  canvas.setFillStyle('#1a1a1a')
  canvas.fillText('患者姓名：', xPos, yPosition)
  xPos += 160
  canvas.setStrokeStyle('#1a1a1a')
  canvas.setLineWidth(2)
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(xPos + 140, yPosition - 25)
  canvas.stroke()
  const nameText = formatFieldValue(patientInfo.value.name) || ''
  if (nameText) {
    canvas.fillText(nameText, xPos + 5, yPosition - 8)
  }
  
  xPos += 180
  canvas.fillText('性别：', xPos, yPosition)
  xPos += 100
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(xPos + 80, yPosition - 25)
  canvas.stroke()
  const genderText = formatFieldValue(patientInfo.value.gender) || ''
  if (genderText) {
    canvas.fillText(genderText, xPos + 5, yPosition - 8)
  }
  
  xPos += 120
  canvas.fillText('年龄：', xPos, yPosition)
  xPos += 100
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(xPos + 80, yPosition - 25)
  canvas.stroke()
  const ageText = formatFieldValue(patientInfo.value.age) || ''
  if (ageText) {
    canvas.fillText(ageText, xPos + 5, yPosition - 8)
  }
  
  xPos += 120
  canvas.fillText('病历号：', xPos, yPosition)
  xPos += 120
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(xPos + 160, yPosition - 25)
  canvas.stroke()
  const recordNoText = formatFieldValue(patientInfo.value.medicalRecordNo) || ''
  if (recordNoText) {
    canvas.fillText(recordNoText, xPos + 5, yPosition - 8)
  }
  
  yPosition += lineHeight
  
  // 第二行：就诊时间、就诊地点
  xPos = margin
  canvas.fillText('就诊时间：', xPos, yPosition)
  xPos += 160
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(xPos + 240, yPosition - 25)
  canvas.stroke()
  const timeText = formatFieldValue(patientInfo.value.consultationTime) || ''
  if (timeText) {
    canvas.fillText(timeText, xPos + 5, yPosition - 8)
  }
  
  xPos += 280
  canvas.fillText('就诊地点：', xPos, yPosition)
  xPos += 160
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(xPos + 240, yPosition - 25)
  canvas.stroke()
  const locationText = formatFieldValue(patientInfo.value.consultationLocation) || ''
  if (locationText) {
    canvas.fillText(locationText, xPos + 5, yPosition - 8)
  }
  
  yPosition += lineHeight
  
  // 转诊原因
  xPos = margin
  canvas.fillText('转诊原因：', xPos, yPosition)
  xPos += 160
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(width - margin - 20, yPosition - 25)
  canvas.stroke()
  const reasonText = formatFieldValue(patientInfo.value.referralReason) || ''
  if (reasonText) {
    canvas.fillText(reasonText, xPos + 5, yPosition - 8)
  }
  yPosition += lineHeight
  
  // 主要症状
  xPos = margin
  canvas.fillText('主要症状：', xPos, yPosition)
  xPos += 160
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(width - margin - 20, yPosition - 25)
  canvas.stroke()
  const symptomsText = formatFieldValue(patientInfo.value.mainSymptoms) || ''
  if (symptomsText) {
    canvas.fillText(symptomsText, xPos + 5, yPosition - 8)
  }
  yPosition += lineHeight
  
  // 诊断结果
  xPos = margin
  canvas.fillText('诊断结果：', xPos, yPosition)
  xPos += 160
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(width - margin - 20, yPosition - 25)
  canvas.stroke()
  const diagnosisText = formatFieldValue(patientInfo.value.diagnosisResult) || ''
  if (diagnosisText) {
    canvas.fillText(diagnosisText, xPos + 5, yPosition - 8)
  }
  yPosition += lineHeight
  
  // 转诊建议
  xPos = margin
  canvas.fillText('转诊建议：', xPos, yPosition)
  xPos += 160
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(width - margin - 20, yPosition - 25)
  canvas.stroke()
  const adviceText = formatFieldValue(patientInfo.value.referralAdvice) || ''
  if (adviceText) {
    canvas.fillText(adviceText, xPos + 5, yPosition - 8)
  }
  yPosition += lineHeight
  
  // 目标医院和科室
  xPos = margin
  canvas.fillText('转诊至医院：', xPos, yPosition)
  xPos += 180
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(xPos + 260, yPosition - 25)
  canvas.stroke()
  const hospitalText = formatFieldValue(patientInfo.value.targetHospital) || ''
  if (hospitalText) {
    canvas.fillText(hospitalText, xPos + 5, yPosition - 8)
  }
  
  xPos += 300
  canvas.fillText('转诊至科室：', xPos, yPosition)
  xPos += 180
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(width - margin - 20, yPosition - 25)
  canvas.stroke()
  const deptText = formatFieldValue(patientInfo.value.targetDepartment) || ''
  if (deptText) {
    canvas.fillText(deptText, xPos + 5, yPosition - 8)
  }
  yPosition += lineHeight
  
  // 注意事项
  xPos = margin
  canvas.fillText('注意事项：', xPos, yPosition)
  xPos += 160
  canvas.beginPath()
  canvas.moveTo(xPos, yPosition - 25)
  canvas.lineTo(width - margin - 20, yPosition - 25)
  canvas.stroke()
  const notesText = formatFieldValue(patientInfo.value.notes) || ''
  if (notesText) {
    canvas.fillText(notesText, xPos + 5, yPosition - 8)
  }
  yPosition += lineHeight + 40
  
  // 签名和日期（右对齐）
  const signatureY = yPosition
  xPos = width - margin - 400
  canvas.fillText('签名：', xPos, signatureY)
  xPos += 120
  canvas.beginPath()
  canvas.moveTo(xPos, signatureY - 25)
  canvas.lineTo(xPos + 180, signatureY - 25)
  canvas.stroke()
  const signatureText = formatFieldValue(patientInfo.value.signature) || ''
  if (signatureText) {
    canvas.fillText(signatureText, xPos + 5, signatureY - 8)
  }
  
  xPos += 220
  canvas.fillText('日期：', xPos, signatureY)
  xPos += 120
  canvas.beginPath()
  canvas.moveTo(xPos, signatureY - 25)
  canvas.lineTo(xPos + 180, signatureY - 25)
  canvas.stroke()
  const dateText = formatFieldValue(patientInfo.value.date) || ''
  if (dateText) {
    canvas.fillText(dateText, xPos + 5, signatureY - 8)
  }
  
  // 绘制印章（在签名和日期右侧）
  const sealY = signatureY - 60
  const sealX = width - margin - 180
  const sealSize = 150
  
  // 绘制印章圆形边框（使用多个小线段模拟圆形）
  canvas.setStrokeStyle('#d32f2f')
  canvas.setLineWidth(4)
  const centerX = sealX
  const centerY = sealY + sealSize / 2
  const radius = sealSize / 2 - 5
  
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
  
  // 绘制印章文字
  canvas.setFontSize(26)
  canvas.setFillStyle('#d32f2f')
  canvas.setTextAlign('center')
  canvas.fillText('北京交通大学', centerX, centerY - 30)
  canvas.fillText('校医院', centerX, centerY)
  canvas.fillText('转诊专用章', centerX, centerY + 30)
  
  // 尝试绘制印章图片（如果图片存在）
  try {
    // uni-app canvas drawImage需要图片路径
    // 注意：这里需要确保图片路径正确，drawImage需要在draw之前调用
    const sealImagePath = '/static/bjtu.jpg'
    // 由于uni-app canvas的限制，drawImage可能需要在特定时机调用
    // 如果图片加载有问题，使用上面的文字印章
    // canvas.drawImage(sealImagePath, sealX - sealSize/2, sealY, sealSize, sealSize)
  } catch (e) {
    console.warn('绘制印章图片失败，使用文字印章:', e)
  }
  
  yPosition = sealY + sealSize + 40
  
  // 绘制分隔线
  yPosition += 60
  canvas.setStrokeStyle('#e5e5e5')
  canvas.setLineWidth(2)
  canvas.beginPath()
  canvas.moveTo(margin, yPosition)
  canvas.lineTo(width - margin, yPosition)
  canvas.stroke()
  yPosition += 60
  
  // 绘制说明文字
  canvas.setFontSize(24)
  canvas.setFillStyle('#666666')
  
  const instructions = [
    '转诊单是为了方便患者转诊时，向其他医疗机构提供患者病情和治疗情况的记录。本转诊单用于描述患者的病情、诊断结果、转诊原因以及转诊建议等信息，以便接收患者的医疗机构能够更好地了解患者的病情，并提供更合适的治疗服务。',
    '填写本转诊单时，请详细描述患者的症状和诊断结果，明确转诊原因和转诊建议，并注意填写清晰、准确、规范的信息。同时，在转诊过程中，请确保患者携带本转诊单以及相关的医疗证明文件，以便接收患者的医疗机构能够及时获取患者的相关信息。',
    '本转诊单仅为参考模板，具体内容应根据患者的实际情况进行调整和完善。在使用本转诊单时，请遵循相关法律法规和规定，确保患者的个人隐私和信息安全。'
  ]
  
  instructions.forEach((text, index) => {
    // 简单的文本换行处理，根据内容宽度调整
    const maxCharsPerLine = Math.floor((width - margin * 2) / 14) // 根据字体大小估算字符数
    const lines = wrapText(text, maxCharsPerLine)
    lines.forEach(line => {
      if (yPosition < height - 100) { // 确保不超出画布
        canvas.fillText(line, margin, yPosition)
        yPosition += 42
      }
    })
    if (index < instructions.length - 1) {
      yPosition += 30
    }
  })
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

// 分享转诊证明
const shareCertificate = () => {
  uni.showToast({
    title: '分享功能待实现',
    icon: 'none'
  })
}
</script>

<style scoped>
.certificate-container {
  padding: 30rpx 20rpx;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.certificate-header {
  text-align: center;
  margin-bottom: 30rpx;
  padding: 30rpx 20rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12rpx;
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);
}

.certificate-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
  letter-spacing: 4rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
}

.certificate-content {
  background-color: #fff;
  padding: 60rpx 50rpx;
  border-radius: 12rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.08);
  line-height: 1.8;
  max-width: 100%;
  border: 2rpx solid #e9ecef;
}

.form-section {
  width: 100%;
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 40rpx;
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
  margin-top: 40rpx;
  justify-content: space-between;
  align-items: flex-end;
}

.signature-section {
  display: flex;
  gap: 40rpx;
  flex: 1;
}

.seal-section {
  margin-left: 40rpx;
}

.seal-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.seal-circle {
  width: 200rpx;
  height: 200rpx;
  border: 4rpx solid #d32f2f;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20rpx;
}

.seal-text {
  font-size: 24rpx;
  color: #d32f2f;
  font-weight: 600;
  line-height: 1.4;
  text-align: center;
}

.field-label {
  font-size: 30rpx;
  color: #2c3e50;
  font-weight: 600;
  white-space: nowrap;
  margin-right: 16rpx;
  min-width: fit-content;
}

.field-underline {
  position: relative;
  min-width: 120rpx;
  border-bottom: 2rpx solid #495057;
  padding-bottom: 8rpx;
  flex: 1;
  display: flex;
  align-items: flex-end;
  transition: border-color 0.3s;
}

.field-underline:hover {
  border-bottom-color: #667eea;
}

.form-field.full .field-underline {
  width: 100%;
}

.field-value {
  font-size: 30rpx;
  color: #212529;
  position: absolute;
  left: 6rpx;
  bottom: 8rpx;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: calc(100% - 12rpx);
  line-height: 1.4;
  font-weight: 500;
  min-height: 1.4em;
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
  border-top: 2rpx solid #e5e5e5;
  padding-top: 40rpx;
  margin-top: 40rpx;
}

.instructions-text {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 24rpx;
  text-indent: 0;
  line-height: 1.8;
  text-align: justify;
}

.certificate-actions {
  display: flex;
  justify-content: center;
  gap: 40rpx;
  margin-top: 40rpx;
}

.action-btn {
  padding: 20rpx 60rpx;
  border-radius: 8rpx;
  font-size: 30rpx;
  font-weight: 500;
  border: none;
}

.download-pdf-btn {
  background-color: #4a90e2;
  color: #fff;
}

.download-img-btn {
  background-color: #52c41a;
  color: #fff;
}

.share-btn {
  background-color: #5cb85c;
  color: #fff;
}
</style>