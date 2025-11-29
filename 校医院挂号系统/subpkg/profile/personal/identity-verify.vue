<template>
  <view class="page-bg">
    <view class="card">
      <view class="card-header">
        <text class="title">身份认证</text>
        <text class="sub-title">学生/教职工上传证件照，提交认证申请</text>
      </view>

      <!-- 就诊卡选择器：始终显示，有数据时可切换就诊卡 -->
      <view v-if="patientList.length" class="selector-row">
        <picker
          mode="selector"
          :value="pickerIndex"
          :range="patientList"
          range-key="patientName"
          @change="onPatientChange"
        >
          <view class="selector-display">
            <text>选择就诊卡：</text>
            <text class="selector-text">{{ currentPatientName }}</text>
          </view>
        </picker>
      </view>

      <view class="card-body" v-if="cardInfo">
        <view class="info-row">
          <text class="label">姓名</text>
          <text class="value">{{ cardInfo.patientName }}</text>
        </view>
        <view class="info-row">
          <text class="label">身份</text>
          <text class="value">{{ identityText }}</text>
        </view>
        <view class="info-row" v-if="cardInfo.patientType === 1">
          <text class="label">学号</text>
          <input class="input" v-model="form.studentId" placeholder="请输入学号" />
        </view>
        <view class="info-row" v-if="cardInfo.patientType === 2">
          <text class="label">工号</text>
          <input class="input" v-model="form.staffId" placeholder="请输入工号" />
        </view>
        <!-- 校外人员（patientType=3）无需认证 -->
        <view v-if="isExternal" class="info-row">
          <text class="label">提示</text>
          <text class="value">校外人员无需进行身份认证</text>
        </view>

        <template v-else>
          <view class="status-row">
            <text class="label">认证状态</text>
            <view class="status-tag" :class="statusClass">{{ statusText }}</view>
          </view>

          <view class="photo-section">
            <text class="label">证件照片（正面）</text>
            <view class="photo-box" @click="chooseImage">
              <image v-if="form.identityPhoto" :src="form.identityPhoto" mode="aspectFill" class="photo" />
              <view v-else class="photo-placeholder">
                <text>点击上传证件正面照</text>
              </view>
            </view>
          </view>
        </template>
      </view>

      <view v-else class="empty">
        <text>当前暂无就诊卡信息，请先创建就诊卡</text>
      </view>
    </view>

    <view class="actions" v-if="cardInfo && !isExternal">
      <button
        class="btn primary"
        :class="{ 'btn-disabled': isVerified }"
        :disabled="loading || isVerified"
        @click="submitApply"
      >
        {{ isVerified ? '已完成认证' : (loading ? '提交中...' : '提交认证申请') }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { patientApi } from '@/api/patient'
import { uploadIdentityPhoto } from '@/api/file'
import { uniShowToast } from '@/utils/uniHelper'

const userStore = useUserStore()
const cardInfo = ref(null)
const loading = ref(false)
const routePatientId = ref(null)
const patientList = ref([])
const pickerIndex = ref(0)

const form = ref({
  patientId: null,
  studentId: '',
  staffId: '',
  identityPhoto: ''
})

const identityText = computed(() => {
  if (!cardInfo.value) return ''
  if (cardInfo.value.patientType === 1) return '学生'
  if (cardInfo.value.patientType === 2) return '教职工'
  return '其他'
})

const statusText = computed(() => {
  if (!cardInfo.value || cardInfo.value.identityVerify === null || cardInfo.value.identityVerify === undefined) return '未提交'
  if (cardInfo.value.identityVerify === 0) return '未审核'
  if (cardInfo.value.identityVerify === 1) return '已通过'
  if (cardInfo.value.identityVerify === 2) return '未通过'
  return '未知'
})

const statusClass = computed(() => {
  if (!cardInfo.value) return 'status-pending'
  if (cardInfo.value.identityVerify === 1) return 'status-pass'
  if (cardInfo.value.identityVerify === 2) return 'status-reject'
  return 'status-pending'
})

const isExternal = computed(() => {
  if (!cardInfo.value) return false
  const t = cardInfo.value.patientType
  return t === 3 || t === '3'
})

const isVerified = computed(() => {
  if (!cardInfo.value) return false
  const v = cardInfo.value.identityVerify
  // 兼容后端返回数字 1 或字符串 '1'
  return v === 1 || v === '1'
})

const currentPatientName = computed(() => {
  if (!patientList.value.length) return ''
  return patientList.value[pickerIndex.value]?.patientName || ''
})

const loadCardInfo = async () => {
  try {
    const params = {}
    if (routePatientId.value) {
      params.patientId = routePatientId.value
    } else {
      params.userId = userStore.userInfo.userId
    }
    const res = await patientApi.getCard(params)
    console.log("获得的响应" + res)
    if (res) {
      cardInfo.value = res
      form.value.patientId = res.patientId
      form.value.studentId = res.studentId || ''
      form.value.staffId = res.staffId || ''
      form.value.identityPhoto = res.identityPhoto || ''
    }
  } catch (e) {
    console.error('获取就诊卡信息失败', e)
  }
}

const loadPatientList = async () => {
  try {
    const res = await patientApi.getPatientList({ userId: userStore.userInfo.userId })
    const list = (res && res.data) ? res.data : res || []
    patientList.value = Array.isArray(list) ? list : []
    if (patientList.value.length > 0 && !routePatientId.value) {
      pickerIndex.value = 0
      routePatientId.value = patientList.value[0].patientId
    }
  } catch (e) {
    console.error('获取就诊人列表失败', e)
  }
}

const onPatientChange = (e) => {
  const index = Number(e.detail.value || 0)
  pickerIndex.value = index
  const p = patientList.value[index]
  if (p && p.patientId) {
    routePatientId.value = p.patientId
    loadCardInfo()
  }
}

const buildImageUrl = (relativePath) => {
  if (!relativePath) return ''
  const baseURL = uni.getStorageSync('BASE_URL') || 'http://localhost:8095'
  const apiPrefix = uni.getStorageSync('API_PREFIX') || '/jeecg-boot'
  const cleanPrefix = apiPrefix.endsWith('/') ? apiPrefix.slice(0, -1) : apiPrefix
  const cleanPath = relativePath.replace(/^\/+/, '')
  return `${baseURL}${cleanPrefix}/sys/common/static/${encodeURI(cleanPath)}`
}

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const filePath = res.tempFilePaths[0]
      try {
        const payload = await uploadIdentityPhoto(filePath)
        // responseInterceptor 已经将 {code,message,data:{url}} 解包为 data，即 payload 形如 { url }
        const relative = payload && (payload.url || (payload.data && payload.data.url))
        if (relative) {
          // 表单里存相对路径，提交给后端
          form.value.identityPhoto = relative
          // 预览用完整 URL
          form.value.identityPhoto = buildImageUrl(relative)
        } else {
          uniShowToast({ title: '上传失败，请重试', icon: 'none' })
        }
      } catch (err) {
        console.error(err)
        uniShowToast({ title: '上传失败，请稍后重试', icon: 'none' })
      }
    }
  })
}

const submitApply = async () => {
  if (!form.value.patientId) {
    uniShowToast({ title: '未找到就诊卡信息', icon: 'none' })
    return
  }

  if (cardInfo.value.patientType === 1 && !form.value.studentId) {
    uniShowToast({ title: '请填写学号', icon: 'none' })
    return
  }
  if (cardInfo.value.patientType === 2 && !form.value.staffId) {
    uniShowToast({ title: '请填写工号', icon: 'none' })
    return
  }
  if (!form.value.identityPhoto) {
    uniShowToast({ title: '请先上传证件照片', icon: 'none' })
    return
  }

  loading.value = true
  try {
    // 提交身份认证申请：生成/覆盖一条认证记录
    await patientApi.applyIdentity({
      patientId: form.value.patientId,
      studentId: form.value.studentId,
      staffId: form.value.staffId,
      identityPhoto: form.value.identityPhoto,
    })
    await uniShowToast({ title: '提交成功，待管理员审核，如重新上传会覆盖当前申请', icon: 'none' })
    await loadCardInfo()
  } catch (e) {
    console.error(e)
    uniShowToast({ title: '提交失败，请稍后重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onLoad((query) => {
  if (query && query.patientId) {
    const id = Number(query.patientId)
    routePatientId.value = Number.isNaN(id) ? null : id
  }
  // 如果没有通过路由指定 patientId，则先加载就诊人列表供选择
  if (!routePatientId.value) {
    loadPatientList().then(() => {
      loadCardInfo()
    })
  } else {
    loadCardInfo()
  }
})
</script>

<style scoped>
.page-bg {
  min-height: 100vh;
  background: #f8faff;
  padding: 24rpx;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.card-header {
  margin-bottom: 16rpx;
}

.title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.sub-title {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: #999;
}

.card-body {
  margin-top: 12rpx;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.label {
  width: 160rpx;
  font-size: 26rpx;
  color: #666;
}

.value {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.input {
  flex: 1;
  height: 72rpx;
  border-radius: 12rpx;
  border: 1rpx solid #ddd;
  padding: 0 20rpx;
  font-size: 28rpx;
}

.status-row {
  display: flex;
  align-items: center;
  margin: 24rpx 0;
}

.status-tag {
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
}

.status-pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-pass {
  background: #f6ffed;
  color: #52c41a;
}

.status-reject {
  background: #fff1f0;
  color: #f5222d;
}

.photo-section {
  margin-top: 8rpx;
}

.photo-box {
  margin-top: 12rpx;
  width: 100%;
  height: 320rpx;
  border-radius: 16rpx;
  border: 1rpx dashed #ccc;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.photo {
  width: 100%;
  height: 100%;
}

.photo-placeholder {
  font-size: 26rpx;
  color: #999;
}

.empty {
  padding: 40rpx 0;
  text-align: center;
  color: #999;
}

.actions {
  margin-top: 24rpx;
}

.btn-disabled {
  background: #d9d9d9 !important;
  color: #ffffff !important;
  border-color: #d9d9d9 !important;
}

.btn {
  width: 100%;
  height: 88rpx;
  border-radius: 16rpx;
  font-size: 32rpx;
}

.primary {
  background: #3a9cff;
  color: #fff;
}
</style>
