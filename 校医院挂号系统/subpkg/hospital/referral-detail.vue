<template>
  <view class="referral-bg">
    <view class="page-header">
      <text class="page-title">转诊详情</text>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 状态卡片 -->
      <view class="status-card" :class="getStatusClass(referralDetail.status)">
        <view class="status-header">
          <view class="status-icon">
            <image :src="getStatusIcon(referralDetail.status)" mode="aspectFit" class="icon-img"></image>
          </view>
          <view class="status-info">
            <text class="status-text">{{ referralDetail.status }}</text>
            <text class="status-desc">{{ getStatusDescription(referralDetail.status) }}</text>
          </view>
        </view>
        <view class="hospital-info">
          <text class="hospital-name">{{ referralDetail.targetHospital }}</text>
          <text class="department">{{ referralDetail.targetDepartment }}</text>
        </view>
      </view>

      <!-- 申请信息 -->
      <view class="info-section">
        <view class="section-title">申请信息</view>
        <view class="info-grid">
          <view class="info-item">
            <text class="info-label">申请编号</text>
            <text class="info-value">{{ referralDetail.id }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">申请时间</text>
            <text class="info-value">{{ referralDetail.applyTime }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">审核时间</text>
            <text class="info-value">{{ referralDetail.reviewTime || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 患者信息 -->
      <view class="info-section">
        <view class="section-title">患者信息</view>
        <view class="info-grid">
          <view class="info-item">
            <text class="info-label">姓名</text>
            <text class="info-value">{{ referralDetail.patientName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">性别</text>
            <text class="info-value">{{ referralDetail.gender || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">年龄</text>
            <text class="info-value">{{ referralDetail.age || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">联系电话</text>
            <text class="info-value">{{ referralDetail.phone || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 病情描述 -->
      <view class="info-section">
        <view class="section-title">病情描述</view>
        <view class="description-item">
          <text class="desc-label">症状描述</text>
          <text class="desc-content">{{ referralDetail.symptoms || '-' }}</text>
        </view>
        <view class="description-item">
          <text class="desc-label">既往病史</text>
          <text class="desc-content">{{ referralDetail.medicalHistory || '-' }}</text>
        </view>
        <view class="description-item">
          <text class="desc-label">转诊原因</text>
          <text class="desc-content">{{ referralDetail.reason || '-' }}</text>
        </view>
      </view>

      <!-- 审核信息 -->
      <view v-if="referralDetail.status !== '待审核'" class="info-section">
        <view class="section-title">审核信息</view>
        <view class="description-item">
          <text class="desc-label">审核医生</text>
          <text class="desc-content">{{ referralDetail.reviewDoctor || '系统管理员' }}</text>
        </view>
        <view v-if="referralDetail.status === '已拒绝'" class="description-item">
          <text class="desc-label">拒绝原因</text>
          <text class="desc-content reject-reason">{{ referralDetail.rejectReason || '未说明' }}</text>
        </view>
        <view v-if="referralDetail.status === '待审核'" class="description-item">
          <text class="desc-label">审核状态</text>
          <text class="desc-content waiting-tips">请等待医生审核...</text>
        </view>
        <view v-else class="description-item">
          <text class="desc-label">审核意见</text>
          <text class="desc-content">{{ referralDetail.reviewComments || '同意转诊' }}</text>
        </view>
      </view>

      <!-- 上传资料预览 -->
      <view v-if="referralDetail.attachments && referralDetail.attachments.length > 0" class="info-section">
        <view class="section-title">上传资料</view>
        <view class="attachment-list">
          <view 
            v-for="(item, index) in referralDetail.attachments" 
            :key="index"
            class="attachment-item"
            @click="previewImage(item.url, index)"
          >
            <image :src="item.url" mode="aspectFill" class="attachment-img"></image>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-section">
        <button class="primary-btn" @click="goBack">返回列表</button>
        <button v-if="referralDetail.status === '待审核'" 
                class="cancel-btn" 
                @click="cancelReferral"
                :disabled="submitting">
          {{ submitting ? '处理中...' : '取消申请' }}
        </button>
        <!-- 院内转诊自动挂号按钮 -->
        <button v-if="referralDetail.status === '已审核' && referralDetail.targetHospital === '校医院' && referralDetail.type === '院内转诊'" 
                class="register-btn" 
                @click="autoRegister"
                :disabled="submitting">
          {{ submitting ? '处理中...' : '自动挂号' }}
        </button>


      </view>
    </scroll-view>
    <canvas canvas-id="certificateCanvas" class="hidden-canvas"></canvas>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPatientReferralDetail, cancelPatientReferral, autoRegisterInternalReferral } from '../../api/referral'

// 转诊详情数据
const referralDetail = ref({
  id: '',
  patientName: '',
  gender: '',
  age: '',
  phone: '',
  symptoms: '',
  medicalHistory: '',
  reason: '',
  targetHospital: '',
  targetDepartment: '',
  applyTime: '',
  reviewTime: '',
  status: '',
  reviewDoctor: '',
  reviewComments: '',
  rejectReason: '',
  attachments: [],
  type: '院内转诊', // 默认院内转诊，后端返回时会覆盖
  visitRecordId: '', // 关联的就诊记录ID
  rawStatus: 'PENDING'
})
const loading = ref(true)
const submitting = ref(false)

const statusMap = {
  PENDING: '待审核',
  APPROVED: '已审核',
  REJECTED: '已拒绝',
  CANCELLED: '已取消',
  COMPLETED: '已完成'
}

const referralId = ref('')

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 获取状态对应的样式类
const getStatusClass = (status) => {
  switch (status) {
    case '待审核':
      return 'status-pending'
    case '已审核':
      return 'status-approved'
    case '已拒绝':
      return 'status-rejected'
    default:
      return ''
  }
}

// 获取状态图标
const getStatusIcon = (status) => {
  switch (status) {
    case '待审核':
      return '/static/info_message.png'
    case '已审核':
      return '/static/check.svg'
    case '已拒绝':
      return '/static/complain.svg'
    default:
      return '/static/info_message.png'
  }
}

// 获取状态描述
const getStatusDescription = (status) => {
  switch (status) {
    case '待审核':
      return '您的转诊申请正在等待医生审核'
    case '已审核':
      return '您的转诊申请已通过审核，可以前往目标医院就诊'
    case '已拒绝':
      return '您的转诊申请未通过审核，请查看拒绝原因'
    default:
      return ''
  }
}

// 预览图片
const previewImage = (current, index) => {
  const urls = referralDetail.value.attachments.map(item => item.url)
  uni.previewImage({
    current,
    urls
  })
}

// 取消转诊申请
const cancelReferral = async () => {
  if (referralDetail.value.rawStatus !== 'PENDING') {
    uni.showToast({
      title: '只有待审核的申请可以取消',
      icon: 'none'
    })
    return
  }
  
  uni.showModal({
    title: '提示',
    content: '确定要取消该转诊申请吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          submitting.value = true
          await cancelPatientReferral({id: referralId.value, reason: '患者主动取消'})
          uni.showToast({
            title: '取消成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          uni.showToast({
            title: '取消失败',
            icon: 'none'
          })
        } finally {
          submitting.value = false
        }
      }
    }
  })
}

// 前往医院导航
const goToHospital = () => {
  console.log('导航到医院:', referralDetail.value.targetHospital)
  uni.showToast({
    title: '医院导航功能待实现',
    icon: 'none'
  })
}



// 院内转诊自动挂号
const autoRegister = async () => {
  if (referralDetail.value.rawStatus !== 'APPROVED') {
    uni.showToast({
      title: '只有已审核的申请可以进行自动挂号',
      icon: 'none'
    })
    return
  }
  
  uni.showModal({
    title: '提示',
    content: `确定要为患者${referralDetail.value.patientName}在${referralDetail.value.targetDepartment}自动挂号吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          submitting.value = true
          const result = await autoRegisterInternalReferral(referralDetail.value.id)
          
          if (result && result !== false) {
            uni.showToast({
              title: '自动挂号成功！',
              icon: 'success'
            })
            // 更新转诊状态为已挂号并通知就诊记录页面刷新
            setTimeout(() => {
              loadReferralDetail() // 重新加载详情，更新状态
              // 通知就诊记录页面刷新数据
              uni.$emit('refreshHospitalRecords')
            }, 1500)
          } else {
            uni.showToast({
              title: result.message || '自动挂号失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('自动挂号失败:', error)
          uni.showToast({
            title: '自动挂号失败，请稍后重试',
            icon: 'none'
          })
        } finally {
          submitting.value = false
        }
      }
    }
  })
}

const drawFallbackStamp = (ctx, x, y, r) => {
  ctx.save()
  ctx.beginPath()
  ctx.arc(x, y, r, 0, Math.PI * 2)
  ctx.strokeStyle = '#1f5caa'
  ctx.lineWidth = 8
  ctx.stroke()
  ctx.fillStyle = '#1f5caa'
  ctx.font = 'bold 36px sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText('北京交通大学', x, y - 10)
  ctx.font = 'bold 28px sans-serif'
  ctx.fillText('校医院', x, y + 30)
  ctx.restore()
}

const drawFallbackStampMP = (ctx, x, y, r) => {
  ctx.setLineWidth(8)
  ctx.setStrokeStyle('#1f5caa')
  ctx.beginPath()
  ctx.arc(x, y, r, 0, Math.PI * 2)
  ctx.stroke()
  ctx.setFillStyle('#1f5caa')
  ctx.setFontSize(36)
  ctx.fillText('北京交通大学', x - 110, y - 10)
  ctx.setFontSize(28)
  ctx.fillText('校医院', x - 40, y + 30)
}

const downloadExternalReferralCertificate = async () => {
  try {
    submitting.value = true
    const platform = typeof process !== 'undefined' && process.env && process.env.UNI_PLATFORM ? process.env.UNI_PLATFORM : 'h5'
    if (platform === 'h5' && typeof window !== 'undefined' && typeof document !== 'undefined') {
      const w = 1000
      const h = 1400
      const canvas = document.createElement('canvas')
      canvas.width = w
      canvas.height = h
      const ctx = canvas.getContext('2d')
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, w, h)
      ctx.fillStyle = '#0b4ea2'
      ctx.font = 'bold 56px sans-serif'
      ctx.textAlign = 'center'
      ctx.fillText('院外转诊证明', w / 2, 120)
      ctx.strokeStyle = '#0b4ea2'
      ctx.lineWidth = 2
      ctx.beginPath()
      ctx.moveTo(120, 160)
      ctx.lineTo(w - 120, 160)
      ctx.stroke()
      ctx.textAlign = 'left'
      ctx.fillStyle = '#333333'
      ctx.font = '36px sans-serif'
      const left = 140
      let y = 240
      const line = (label, value) => { ctx.fillText(`${label}：${value || '-'}`, left, y); y += 64 }
      line('申请编号', referralDetail.value.id)
      line('患者姓名', referralDetail.value.patientName)
      line('性别/年龄', `${referralDetail.value.gender || '-'} / ${referralDetail.value.age || '-'}`)
      line('联系电话', referralDetail.value.phone || '-')
      line('目标医院', referralDetail.value.targetHospital || '-')
      line('目标科室', referralDetail.value.targetDepartment || '-')
      ctx.font = '34px sans-serif'
      line('症状描述', referralDetail.value.symptoms || '-')
      line('既往病史', referralDetail.value.medicalHistory || '-')
      line('转诊原因', referralDetail.value.reason || '-')
      ctx.font = '36px sans-serif'
      line('申请时间', referralDetail.value.applyTime || '-')
      line('审核时间', referralDetail.value.reviewTime || '-')
      line('审核医生', referralDetail.value.reviewDoctor || '系统管理员')
      ctx.fillStyle = '#666666'
      ctx.font = '28px sans-serif'
      ctx.fillText('本证明用于患者院外就诊使用。', left, y + 20)
      const stampX = w - 260
      const stampY = h - 300
      const stampR = 120
      await new Promise((resolve) => {
        const img = new Image()
        img.crossOrigin = 'anonymous'
        img.onload = () => { ctx.save(); ctx.globalAlpha = 0.96; ctx.drawImage(img, stampX - stampR, stampY - stampR, stampR * 2, stampR * 2); ctx.restore(); resolve(null) }
        img.onerror = () => { drawFallbackStamp(ctx, stampX, stampY, stampR); resolve(null) }
        img.src = '/static/bjtu_logo.png'
      })
      const dataUrl = canvas.toDataURL('image/png')
      try {
        const blob = await (await fetch(dataUrl)).blob()
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `转诊证明_${referralDetail.value.id}.png`
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(url)
        uni.showToast({ title: '已生成并下载', icon: 'success' })
      } catch (err) {
        uni.previewImage({ urls: [dataUrl] })
        uni.showToast({ title: '下载失败，已打开预览', icon: 'none' })
      }
    } else {
      const w = 1000
      const h = 1400
      const ctx = uni.createCanvasContext('certificateCanvas')
      ctx.setFillStyle('#ffffff')
      ctx.fillRect(0, 0, w, h)
      ctx.setFillStyle('#0b4ea2')
      ctx.setFontSize(56)
      ctx.fillText('院外转诊证明', w / 2 - 180, 120)
      ctx.setStrokeStyle('#0b4ea2')
      ctx.setLineWidth(2)
      ctx.beginPath()
      ctx.moveTo(120, 160)
      ctx.lineTo(w - 120, 160)
      ctx.stroke()
      ctx.setFillStyle('#333333')
      ctx.setFontSize(36)
      const left = 140
      let y = 240
      const line = (label, value) => { ctx.fillText(`${label}：${value || '-'}`, left, y); y += 64 }
      line('申请编号', referralDetail.value.id)
      line('患者姓名', referralDetail.value.patientName)
      line('性别/年龄', `${referralDetail.value.gender || '-'} / ${referralDetail.value.age || '-'}`)
      line('联系电话', referralDetail.value.phone || '-')
      line('目标医院', referralDetail.value.targetHospital || '-')
      line('目标科室', referralDetail.value.targetDepartment || '-')
      ctx.setFontSize(34)
      line('症状描述', referralDetail.value.symptoms || '-')
      line('既往病史', referralDetail.value.medicalHistory || '-')
      line('转诊原因', referralDetail.value.reason || '-')
      ctx.setFontSize(36)
      line('申请时间', referralDetail.value.applyTime || '-')
      line('审核时间', referralDetail.value.reviewTime || '-')
      line('审核医生', referralDetail.value.reviewDoctor || '系统管理员')
      ctx.setFillStyle('#666666')
      ctx.setFontSize(28)
      ctx.fillText('本证明用于患者院外就诊使用。', left, y + 20)
      const stampX = w - 260
      const stampY = h - 300
      const stampR = 120
      uni.getImageInfo({
        src: '/static/bjtu_logo.png',
        success: (info) => {
          ctx.drawImage(info.path, stampX - stampR, stampY - stampR, stampR * 2, stampR * 2)
          ctx.draw(false, () => {
            uni.canvasToTempFilePath({
              canvasId: 'certificateCanvas',
              destWidth: w,
              destHeight: h,
              width: w,
              height: h,
              success: (res) => {
                const filePath = res.tempFilePath
                uni.previewImage({ urls: [filePath] })
                uni.showToast({ title: '图片预览中，长按保存', icon: 'none' })
              },
              fail: () => {
                uni.showToast({ title: '下载失败', icon: 'none' })
              }
            })
          })
        },
        fail: () => {
          drawFallbackStampMP(ctx, stampX, stampY, stampR)
          ctx.draw(false, () => {
            uni.canvasToTempFilePath({
              canvasId: 'certificateCanvas',
              destWidth: w,
              destHeight: h,
              width: w,
              height: h,
              success: (res) => {
                const filePath = res.tempFilePath
                uni.previewImage({ urls: [filePath] })
                uni.showToast({ title: '图片预览中，长按保存', icon: 'none' })
              },
              fail: () => {
                uni.showToast({ title: '下载失败', icon: 'none' })
              }
            })
          })
        }
      })
    }
  } catch (e) {
    console.error('生成失败', e)
    uni.showToast({ title: '下载失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

// 加载转诊详情
const loadReferralDetail = async () => {
  if (!referralId.value) return
  try {
    loading.value = true
    const res = await getPatientReferralDetail(referralId.value)
    if (res) {
      referralDetail.value = {
        ...referralDetail.value,
        ...res,
        id: res.id || res.referralId || referralId.value,
        patientName: res.patientName || '',
        gender: res.gender || '',
        age: res.age || '',
        phone: res.phone || '',
        symptoms: res.symptoms || '',
        medicalHistory: res.medicalHistory || '',
        reason: res.reason || '',
        targetHospital: res.targetHospitalName || res.targetHospital || '',
        targetDepartment: res.targetDeptName || res.targetDepartment || '',
        applyTime: res.applyTime || res.createTime || '',
        reviewTime: res.reviewTime || '',
        status: statusMap[res.status] || res.status || '待审核',
        rawStatus: res.status || 'PENDING',
        reviewDoctor: res.reviewDoctor || '',
        reviewComments: res.reviewComments || '',
        rejectReason: res.rejectReason || '',
        attachments: Array.isArray(res.attachments) ? res.attachments : [],
        type: res.targetType === 'EXTERNAL' ? '院外转诊' : '院内转诊'
      }
    } else {
      uni.showToast({ title: '未找到该转诊记录', icon: 'none' })
    }
  } catch (error) {
    console.error('加载转诊详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 页面加载时获取详情数据
onLoad((options) => {
  referralId.value = options?.id || ''
  loadReferralDetail()
})
</script>

<style scoped>
.referral-bg {
  background-color: #f5f5f5;
  min-height: 100vh;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.page-header {
  background-color: #ffffff;
  padding: 32rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: sticky;
  top: 0;
  z-index: 10;
  max-width: 100%;
  box-sizing: border-box;
}

.page-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1f2d3d;
  text-align: center;
}


.content {
  padding: 16px;
  height: calc(100vh - 52px);
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

/* 状态卡片样式 - 缩小一半 */
.status-card {
  border-radius: 8px;
  padding: 10px 16px;
  margin-bottom: 16px;
  margin-left: 0;
  margin-right: 0;
  width: 100%;
  color: #fff;
  box-sizing: border-box;
}

.status-pending {
  background: linear-gradient(135deg, #ff9800, #f57c00);
}

.status-approved {
  background: linear-gradient(135deg, #4caf50, #388e3c);
}

.status-rejected {
  background: linear-gradient(135deg, #f44336, #d32f2f);
}

.status-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.status-icon {
  margin-right: 10px;
  flex-shrink: 0;
}

.icon-img {
  width: 28px;
  height: 28px;
}

.status-info {
  flex: 1;
}

.status-text {
  font-size: 16px;
  font-weight: bold;
  display: block;
  margin-bottom: 4px;
}

.status-desc {
  font-size: 12px;
  opacity: 0.9;
  line-height: 1.4;
}

.hospital-info {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 8px 12px;
  border-radius: 6px;
  margin-top: 8px;
}

.hospital-name {
  font-size: 14px;
  font-weight: bold;
  display: block;
  margin-bottom: 2px;
}

.department {
  font-size: 12px;
  opacity: 0.95;
}

/* 信息区域样式 */
.info-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  margin-left: 0;
  margin-right: 0;
  width: 100%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
  position: relative;
  padding-left: 10px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background-color: #1989fa;
  border-radius: 2px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.description-item {
  margin-bottom: 16px;
}

.description-item:last-child {
  margin-bottom: 0;
}

.desc-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  display: block;
}

.desc-content {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  word-break: break-all;
}

.reject-reason {
  color: #f44336;
}

/* 附件样式 */
.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.attachment-item {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
}

.attachment-img {
  width: 100%;
  height: 100%;
}

/* 操作按钮样式 */
.action-section {
  margin-top: 20px;
  width: 100%;
  display: flex;
  gap: 12px;
  box-sizing: border-box;
}

.primary-btn {
  flex: 1;
  height: 44px;
  background-color: #1989fa;
  color: #fff;
  border: none;
  border-radius: 22px;
  font-size: 16px;
}

.secondary-btn {
    flex: 1;
    height: 44px;
    background-color: #fff;
    color: #1989fa;
    border: 1px solid #1989fa;
    border-radius: 22px;
    font-size: 16px;
  }
  
.register-btn {
  flex: 1;
  height: 44px;
  background: linear-gradient(90deg, #4caf50, #66bb6a);
  color: #fff;
  border: none;
  border-radius: 22px;
  font-size: 16px;
}

.cancel-btn {
  flex: 1;
  height: 44px;
  background-color: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 22px;
  font-size: 16px;
}

.waiting-tips {
  color: #ff9500;
  font-weight: 500;
}

.primary-btn:active {
  background-color: #0e77d0;
}

.secondary-btn:active {
  background-color: #f0f0f0;
}
.hidden-canvas {
  position: absolute;
  left: -9999rpx;
  top: -9999rpx;
  width: 1000px;
  height: 1400px;
}
</style>
