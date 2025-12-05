<template>
  <view class="certificate-container">
    <view class="certificate-header">
      <text class="certificate-title">患者转诊记录单</text>
    </view>
    
    <view class="certificate-content" id="certificate-content">
      <view class="patient-info">
        <view class="info-row">
          <text class="info-label">患者姓名：</text>
          <text class="info-value">{{ patientInfo.name }}</text>
          
          <text class="info-label">性别：</text>
          <text class="info-value">{{ patientInfo.gender }}</text>
          
          <text class="info-label">年龄：</text>
          <text class="info-value">{{ patientInfo.age }}</text>
          
          <text class="info-label">病历号：</text>
          <text class="info-value">{{ patientInfo.medicalRecordNo }}</text>
        </view>
        
        <view class="info-row">
          <text class="info-label">就 诊 时 间 ：</text>
          <text class="info-value">{{ patientInfo.consultationTime }}</text>
          
          <text class="info-label">就 诊 地 点 ：</text>
          <text class="info-value">{{ patientInfo.consultationLocation }}</text>
        </view>
        
        <view class="info-row">
          <text class="info-label">转诊原因：</text>
          <text class="info-value">{{ patientInfo.referralReason }}</text>
        </view>
      </view>
      
      <view class="medical-info">
        <view class="info-row">
          <text class="info-label">主要症状：</text>
          <text class="info-value">{{ patientInfo.mainSymptoms }}</text>
        </view>
        
        <view class="info-row">
          <text class="info-label">诊断结果：</text>
          <text class="info-value">{{ patientInfo.diagnosisResult }}</text>
        </view>
        
        <view class="info-row">
          <text class="info-label">转诊建议：</text>
          <text class="info-value">{{ patientInfo.referralAdvice }}</text>
        </view>
        
        <view class="info-row">
          <text class="info-label">注意事项：</text>
          <text class="info-value">{{ patientInfo.notes }}</text>
        </view>
      </view>
      
      <view class="signature-info">
        <view class="info-row">
          <text class="info-label">签名：</text>
          <text class="info-value">{{ patientInfo.signature }}</text>
          
          <text class="info-label">日期：</text>
          <text class="info-value">{{ patientInfo.date }}</text>
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
      <button class="action-btn download-btn" @click="downloadCertificate">下载转诊证明</button>
      <button class="action-btn share-btn" @click="shareCertificate">分享</button>
    </view>
    
    <!-- 用于生成图片的Canvas，设置为不可见 -->
    <canvas canvas-id="certificate-canvas" style="position: absolute; left: -9999rpx; width: 750rpx; height: 1000rpx;"></canvas>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 患者信息
const patientInfo = ref({
  name: '张三',
  gender: '男',
  age: '35',
  medicalRecordNo: '123456',
  consultationTime: '2023-12-15',
  consultationLocation: '北京交通大学校医院',
  referralReason: '病情需要转上级医院进一步治疗',
  mainSymptoms: '持续性头痛、恶心、呕吐',
  diagnosisResult: '颅内占位性病变',
  referralAdvice: '建议转至北京协和医院神经外科进一步诊治',
  notes: '1. 避免剧烈运动；2. 注意休息；3. 按时服药',
  signature: '李医生',
  date: '2023-12-15'
})

// 从路由参数获取转诊记录
onMounted(() => {
  try {
    // 确保getCurrentPages在当前环境中可用
    if (typeof getCurrentPages === 'function') {
      const pages = getCurrentPages()
      if (pages && pages.length > 0) {
        const currentPage = pages[pages.length - 1]
        if (currentPage?.options?.record) {
          try {
            // 确保decodeURIComponent和JSON.parse可用
            const decodeURIComponentFn = typeof decodeURIComponent === 'function' ? decodeURIComponent : (str) => str
            const JSONParseFn = typeof JSON.parse === 'function' ? JSON.parse : () => {}
            
            const recordStr = decodeURIComponentFn(currentPage.options.record)
            const record = JSONParseFn(recordStr)
            
            // 根据实际记录数据填充患者信息
            if (record) {
              patientInfo.value = {
                name: record.patientName || '未填写',
                gender: record.patientGender || '未填写',
                age: record.patientAge || '未填写',
                medicalRecordNo: record.medicalRecordNo || '未填写',
                consultationTime: record.consultationTime || '未填写',
                consultationLocation: record.consultationLocation || '北京交通大学校医院',
                referralReason: record.referralReason || '未填写',
                mainSymptoms: record.mainSymptoms || '未填写',
                diagnosisResult: record.diagnosisResult || '未填写',
                referralAdvice: record.referralAdvice || '未填写',
                notes: record.notes || '未填写',
                signature: record.doctorName || '未填写',
                date: new Date().toLocaleDateString()
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
})

// 下载转诊证明
const downloadCertificate = () => {
  try {
    uni.showLoading({
      title: '正在生成转诊证明...'
    })
    
    // 使用uni.canvasToTempFilePath将内容转换为图片
    uni.createSelectorQuery().select('#certificate-content').fields({
      size: true
    }).exec((res) => {
      try {
        if (!res || !res[0]) {
          uni.hideLoading()
          uni.showToast({
            title: '获取页面内容失败',
            icon: 'none'
          })
          return
        }
        
        const { width, height } = res[0]
        
        // 确保createCanvasContext可用
        if (typeof uni.createCanvasContext === 'function') {
          const canvas = uni.createCanvasContext('certificate-canvas')
          
          // 设置画布背景为白色
          canvas.setFillStyle('#ffffff')
          canvas.fillRect(0, 0, width, height)
          
          // 绘制文本内容
          drawCertificateContent(canvas, width, height)
          
          canvas.draw(false, () => {
            setTimeout(() => {
              try {
                uni.canvasToTempFilePath({
                  canvasId: 'certificate-canvas',
                  width: width,
                  height: height,
                  x: 0,
                  y: 0,
                  success: (tempFilePath) => {
                    try {
                      // 保存图片到相册
                      uni.saveImageToPhotosAlbum({
                        filePath: tempFilePath.tempFilePath,
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
                    } catch (saveErr) {
                      uni.hideLoading()
                      console.error('保存图片异常:', saveErr)
                      uni.showToast({
                        title: '保存失败，请重试',
                        icon: 'none'
                      })
                    }
                  },
                  fail: (err) => {
                    uni.hideLoading()
                    console.error('生成图片失败:', err)
                    uni.showToast({
                      title: '生成图片失败，请重试',
                      icon: 'none'
                    })
                  }
                })
              } catch (canvasToTempErr) {
                uni.hideLoading()
                console.error('canvasToTempFilePath调用异常:', canvasToTempErr)
                uni.showToast({
                  title: '生成图片失败，请重试',
                  icon: 'none'
                })
              }
            }, 1000)
          })
        } else {
          uni.hideLoading()
          uni.showToast({
            title: '当前环境不支持canvas操作',
            icon: 'none'
          })
        }
      } catch (canvasErr) {
        uni.hideLoading()
        console.error('canvas操作异常:', canvasErr)
        uni.showToast({
          title: '生成图片失败，请重试',
          icon: 'none'
        })
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
  const margin = 20
  let yPosition = margin
  
  // 设置字体
  canvas.setFontSize(20)
  canvas.setFillStyle('#000000')
  
  // 绘制标题
  canvas.setTextAlign('center')
  canvas.fillText('患者转诊记录单', width / 2, yPosition + 30)
  yPosition += 60
  
  // 绘制患者信息
  canvas.setTextAlign('left')
  canvas.setFontSize(14)
  
  canvas.fillText(`患者姓名：${patientInfo.name}`, margin, yPosition)
  canvas.fillText(`性别：${patientInfo.gender}`, margin + 150, yPosition)
  canvas.fillText(`年龄：${patientInfo.age}`, margin + 250, yPosition)
  canvas.fillText(`病历号：${patientInfo.medicalRecordNo}`, margin + 350, yPosition)
  yPosition += 30
  
  canvas.fillText(`就 诊 时 间 ：${patientInfo.consultationTime}`, margin, yPosition)
  canvas.fillText(`就 诊 地 点 ：${patientInfo.consultationLocation}`, margin + 300, yPosition)
  yPosition += 30
  
  canvas.fillText(`转诊原因：${patientInfo.referralReason}`, margin, yPosition)
  yPosition += 30
  
  canvas.fillText(`主要症状：${patientInfo.mainSymptoms}`, margin, yPosition)
  yPosition += 30
  
  canvas.fillText(`诊断结果：${patientInfo.diagnosisResult}`, margin, yPosition)
  yPosition += 30
  
  canvas.fillText(`转诊建议：${patientInfo.referralAdvice}`, margin, yPosition)
  yPosition += 30
  
  canvas.fillText(`注意事项：${patientInfo.notes}`, margin, yPosition)
  yPosition += 30
  
  canvas.fillText(`签名：${patientInfo.signature}`, margin, yPosition)
  canvas.fillText(`日期：${patientInfo.date}`, margin + 200, yPosition)
  yPosition += 50
  
  // 绘制说明文字
  canvas.setFontSize(12)
  canvas.setFillStyle('#666666')
  
  const instructions = [
    '转诊单是为了方便患者转诊时，向其他医疗机构提供患者病情和治疗情况的记录。本转诊单用于描述患者的病情、诊断结果、转诊原因以及转诊建议等信息，以便接收患者的医疗机构能够更好地了解患者的病情，并提供更合适的治疗服务。',
    '填写本转诊单时，请详细描述患者的症状和诊断结果，明确转诊原因和转诊建议，并注意填写清晰、准确、规范的信息。同时，在转诊过程中，请确保患者携带本转诊单以及相关的医疗证明文件，以便接收患者的医疗机构能够及时获取患者的相关信息。',
    '本转诊单仅为参考模板，具体内容应根据患者的实际情况进行调整和完善。在使用本转诊单时，请遵循相关法律法规和规定，确保患者的个人隐私和信息安全。'
  ]
  
  instructions.forEach(text => {
    // 简单的文本换行处理
    const lines = wrapText(text, 35)
    lines.forEach(line => {
      canvas.fillText(line, margin, yPosition)
      yPosition += 20
    })
    yPosition += 10
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
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.certificate-header {
  text-align: center;
  margin-bottom: 30rpx;
}

.certificate-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
}

.certificate-content {
  background-color: #fff;
  padding: 40rpx;
  border-radius: 10rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  line-height: 1.8;
}

.certificate-info {
  margin-bottom: 30rpx;
}

.patient-info {
  margin-bottom: 30rpx;
}

.medical-info {
  margin-bottom: 30rpx;
}

.signature-info {
  margin-bottom: 40rpx;
}

.info-row {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 20rpx;
}

.info-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-right: 10rpx;
}

.info-value {
  font-size: 28rpx;
  color: #666;
  margin-right: 30rpx;
  flex: 1;
}

.instructions {
  border-top: 1rpx solid #eee;
  padding-top: 30rpx;
}

.instructions-text {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 20rpx;
  text-indent: 52rpx;
  line-height: 1.6;
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

.download-btn {
  background-color: #4a90e2;
  color: #fff;
}

.share-btn {
  background-color: #5cb85c;
  color: #fff;
}
</style>