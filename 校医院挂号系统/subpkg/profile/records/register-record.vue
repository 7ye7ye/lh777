<template>
  <view class="page-bg">
    <!-- 页面头部 -->
    <view class="page-header">
      <text class="header-title">挂号记录</text>
    </view>

    <!-- 患者选择卡片 -->
    <view class="patient-card">
      <view class="patient-label">
        <text class="label-icon">👤</text>
        <text class="label-text">就诊人</text>
      </view>
      <picker mode="selector" :range="patients.map(p => p.patientName)" @change="onPatientChange">
        <view class="picker-wrapper">
          <text class="picker-text">{{ selectedPatient?.patientName || '请选择患者' }}</text>
          <text class="picker-arrow">›</text>
        </view>
      </picker>
    </view>

    <!-- 状态筛选标签 -->
    <view class="status-tabs-wrapper">
      <scroll-view class="status-tabs" scroll-x="true" show-scrollbar="false">
        <view
          v-for="tab in filterTabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeStatus === tab.value }"
          @click="activeStatus = tab.value"
        >
          {{ tab.label }}
        </view>
      </scroll-view>
    </view>

    <!-- 挂号记录列表 -->
    <view class="record-list">
      <view v-for="item in filteredRecords" :key="item.id" class="record-card">
        <!-- 卡片头部：科室和状态 -->
        <view class="card-header">
          <view class="dept-badge">
            <text class="dept-icon">🏥</text>
            <text class="dept-name">{{ item.dept }}</text>
          </view>
          <view :class="['status-badge', statusClassMap[item.status]]">
            {{ statusTextMap[item.status] }}
          </view>
        </view>

        <!-- 医生信息 -->
        <view class="doctor-info">
          <text class="doctor-icon">👨‍⚕️</text>
          <text class="doctor-name">{{ item.doctor }}</text>
        </view>

        <!-- 挂号类型标签 -->
        <view class="type-tag-wrapper">
          <view class="type-tag">{{ item.type }}</view>
        </view>

        <!-- 详细信息 -->
        <view class="info-section">
          <view class="info-item">
            <text class="info-label">📅 挂号时间</text>
            <text class="info-value">{{ item.time }}</text>
          </view>
          <view class="info-item" v-if="item.registrationNo">
            <text class="info-label">🔢 挂号单号</text>
            <text class="info-value">{{ item.registrationNo }}</text>
          </view>
        </view>

        <!-- 价格信息 -->
        <view class="price-section">
          <view class="price-item">
            <text class="price-label">原价</text>
            <text class="price-original">¥{{ item.priceOriginal }}</text>
          </view>
          <view class="price-divider"></view>
          <view class="price-item">
            <text class="price-label">实付</text>
            <text class="price-actual">¥{{ item.actualPrice }}</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="action-section" v-if="item.status === 1">
          <button class="cancel-btn" @click="cancelRecord(item)">
            取消挂号
          </button>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="filteredRecords.length === 0" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无挂号记录</text>
        <text class="empty-hint">选择其他状态筛选或切换就诊人查看</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { patientApi } from "@/api/patient"; // 使用新的接口
import { getRegistrationRecords, getRegistrationTypes } from "@/api/registration"; // 获取挂号记录和挂号类型
import { cancelRegistration } from "@/api/registration";
import { ensurePatientCard } from "@/utils/patientHelper";
import { getDoctorDetail } from "@/api/doctor_massage"; // 获取医生详情
import { getDepartmentDetail } from "@/api/department"; // 获取科室详情

const patients = ref([]);
const selectedPatient = ref(null);
const records = ref([]);
const activeStatus = ref(-1);

// 缓存映射表
const doctorInfoMap = ref(new Map()); // doctorId -> { doctorName, deptId, deptName, title }
const departmentInfoMap = ref(new Map()); // deptId -> { deptName }
const registrationTypeMap = ref(new Map()); // typeId -> { typeName }

const userInfo = uni.getStorageSync('userInfo'); // 例子
const userId = userInfo?.userId;

const statusTextMap = {
  0: "候补",
  1: "已预约",
  2: "已就诊",
  3: "已退号",
  4: "已取消",
};

const statusClassMap = {
  0: "pending",
  1: "success",
  2: "done",
  3: "refund",
  4: "cancelled",
};

const filterTabs = [
  { label: "全部", value: -1 },
  { label: "候补", value: 0 },
  { label: "已预约", value: 1 },
  { label: "已就诊", value: 2 },
  { label: "已退号", value: 3 },
  { label: "已取消", value: 4 },
];

const filteredRecords = computed(() => {
  if (activeStatus.value === -1) return records.value;
  return records.value.filter(r => r.status === activeStatus.value);
});

/** 获取当前用户 ID */
const getCurrentUserId = async () => {
  const info = await ensurePatientCard();
  return info?.userId || null;
};

/** 加载患者列表 */
const loadPatients = async () => {
  const userId = await getCurrentUserId();
  if (!userId) return;

  try {
    const res = await patientApi.getPatientList({ userId });
    patients.value = Array.isArray(res) ? res : [];
    if (patients.value.length) selectedPatient.value = patients.value[0];
    loadRecords();
  } catch (err) {
    console.error("获取患者列表失败:", err);
  }
};

/** 选中患者 */
const onPatientChange = (e) => {
  const index = e.detail.value;
  selectedPatient.value = patients.value[index];
  loadRecords();
};


/** 获取医生信息（包含科室信息） */
const getDoctorInfo = async (doctorId) => {
  if (!doctorId) return null;
  
  // 检查缓存
  if (doctorInfoMap.value.has(doctorId)) {
    return doctorInfoMap.value.get(doctorId);
  }
  
  try {
    const doctorRes = await getDoctorDetail(doctorId);
    let doctorData = doctorRes;
    
    // 处理不同的响应格式
    if (doctorRes?.data) {
      doctorData = doctorRes.data;
    } else if (doctorRes?.result) {
      doctorData = doctorRes.result;
    }
    
    const doctorName = doctorData?.doctorName || doctorData?.name || '';
    const title = doctorData?.title || '';
    const deptId = doctorData?.deptId || doctorData?.dept_id || null;
    let deptName = doctorData?.deptName || doctorData?.department || '';
    
    // 如果医生信息中没有科室名称，通过科室ID获取
    if (!deptName && deptId) {
      const deptInfo = await getDepartmentInfo(deptId);
      if (deptInfo) {
        deptName = deptInfo.deptName;
      }
    }
    
    const doctorInfo = {
      doctorName: doctorName || `医生ID: ${doctorId}`,
      title: title,
      deptId: deptId,
      deptName: deptName || (deptId ? `科室ID: ${deptId}` : '')
    };
    
    // 缓存结果
    doctorInfoMap.value.set(doctorId, doctorInfo);
    return doctorInfo;
  } catch (error) {
    console.error(`获取医生 ${doctorId} 信息失败:`, error);
    return {
      doctorName: `医生ID: ${doctorId}`,
      title: '',
      deptId: null,
      deptName: ''
    };
  }
};

/** 获取科室信息 */
const getDepartmentInfo = async (deptId) => {
  if (!deptId) return null;
  
  // 检查缓存
  if (departmentInfoMap.value.has(deptId)) {
    return departmentInfoMap.value.get(deptId);
  }
  
  try {
    const deptRes = await getDepartmentDetail(deptId);
    let deptData = deptRes;
    
    // 处理不同的响应格式
    if (deptRes?.data) {
      deptData = deptRes.data;
    } else if (deptRes?.result) {
      deptData = deptRes.result;
    }
    
    const deptName = deptData?.deptName || deptData?.name || deptData?.departmentName || '';
    
    const deptInfo = {
      deptId: deptId,
      deptName: deptName || `科室ID: ${deptId}`
    };
    
    // 缓存结果
    departmentInfoMap.value.set(deptId, deptInfo);
    return deptInfo;
  } catch (error) {
    console.error(`获取科室 ${deptId} 信息失败:`, error);
    return {
      deptId: deptId,
      deptName: `科室ID: ${deptId}`
    };
  }
};

/** 加载挂号类型映射 */
const loadRegistrationTypeMap = async () => {
  try {
    const res = await getRegistrationTypes();
    let types = [];
    
    // 处理不同的响应格式
    if (Array.isArray(res)) {
      types = res;
    } else if (res?.data && Array.isArray(res.data)) {
      types = res.data;
    } else if (res?.result && Array.isArray(res.result)) {
      types = res.result;
    }
    
    // 构建映射表
    types.forEach(type => {
      const typeId = type.typeId || type.id;
      const typeName = type.typeName || type.name || '';
      if (typeId) {
        registrationTypeMap.value.set(typeId, typeName);
      }
    });
  } catch (error) {
    console.error('获取挂号类型失败:', error);
  }
};

/** 批量处理挂号记录，获取医生和科室信息 */
const processRecords = async (recordsList) => {
  // 收集所有需要查询的医生ID
  const doctorIds = new Set();
  recordsList.forEach(record => {
    if (record.doctorId) {
      doctorIds.add(record.doctorId);
    }
  });
  
  // 并行获取所有医生信息
  await Promise.all(
    Array.from(doctorIds).map(async (doctorId) => {
      await getDoctorInfo(doctorId);
    })
  );
  
  // 处理记录
  return recordsList.map(item => {
    const doctorId = item.doctorId;
    const doctorInfo = doctorId ? doctorInfoMap.value.get(doctorId) : null;
    const typeId = item.typeId;
    const typeName = registrationTypeMap.value.get(typeId) || `挂号类型ID: ${typeId}`;
    
    return {
      id: item.recordId,
      patientId: selectedPatient.value.patientId,
      patientName: selectedPatient.value.patientName,
      dept: doctorInfo?.deptName || '未知科室',
      doctor: `${doctorInfo?.doctorName || '未知医生'} ${doctorInfo?.title || item.doctorTitle || ''}`.trim(),
      type: typeName,
      time: item.registerTime || "",
      registrationNo: item.registrationNo || "",
      status: item.status,
      priceOriginal: item.priceOriginal,
      actualPrice: item.actualPrice,
    };
  });
};

// 获取挂号记录
const loadRecords = async () => {
  if (!selectedPatient.value) return;
  try {
    uni.showLoading({ title: '加载中...' });
    
    // 先加载挂号类型映射
    if (registrationTypeMap.value.size === 0) {
      await loadRegistrationTypeMap();
    }
    
    const res = await getRegistrationRecords(selectedPatient.value.patientId);
    const list = Array.isArray(res) ? res : [];
    
    // 处理记录，获取医生和科室信息
    records.value = await processRecords(list);
    
    uni.hideLoading();
  } catch (e) {
    console.error("获取挂号记录失败:", e);
    uni.hideLoading();
    uni.showToast({
      title: '获取挂号记录失败',
      icon: 'none'
    });
  }
};

/** 取消挂号 */
const cancelRecord = (item) => {
  uni.showModal({
    title: "取消挂号",
    content: `是否取消【${item.dept} - ${item.doctor}】的挂号？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await cancelRegistration(item.id); // 只传挂号记录id
          console.log("取消挂号返回：", result);

          uni.showToast({
            title: "取消成功",
            icon: "success",
          });

          loadRecords(); // 重新加载挂号记录
        } catch (err) {
          console.error("取消挂号失败：", err);
          uni.showToast({
            title: "取消失败",
            icon: "none",
          });
        }
      }
    },
  });
};


onMounted(async () => {
  // 先加载挂号类型映射
  await loadRegistrationTypeMap();
  // 然后加载患者列表
  loadPatients();
});
</script>


<style scoped>
/* 页面背景 */
.page-bg {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f7ff 0%, #f8faff 100%);
  padding-bottom: 50rpx;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #3a9cff 0%, #1de9b6 100%);
  padding: 40rpx 0 60rpx;
  text-align: center;
  box-shadow: 0 4rpx 20rpx rgba(58, 156, 255, 0.2);
}

.header-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  letter-spacing: 2rpx;
}

/* 患者选择卡片 */
.patient-card {
  background: #fff;
  margin: -30rpx 24rpx 24rpx;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(58, 156, 255, 0.12);
}

.patient-label {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.label-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.label-text {
  font-size: 28rpx;
  color: #666;
  font-weight: 500;
}

.picker-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #f8faff;
  border-radius: 16rpx;
  border: 2rpx solid #e6f7ff;
  transition: all 0.3s;
}

.picker-wrapper:active {
  background: #f0f7ff;
  border-color: #3a9cff;
}

.picker-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
}

.picker-arrow {
  font-size: 40rpx;
  color: #999;
  font-weight: 300;
}

/* 状态筛选标签 */
.status-tabs-wrapper {
  margin: 0 24rpx 24rpx;
}

.status-tabs {
  white-space: nowrap;
  background: #fff;
  border-radius: 20rpx;
  padding: 16rpx 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.tab-item {
  display: inline-block;
  padding: 16rpx 32rpx;
  margin-right: 16rpx;
  font-size: 28rpx;
  color: #666;
  border-radius: 24rpx;
  background: #f8faff;
  transition: all 0.3s;
  white-space: nowrap;
}

.tab-item:last-child {
  margin-right: 0;
}

.tab-item.active {
  background: linear-gradient(135deg, #3a9cff 0%, #1de9b6 100%);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(58, 156, 255, 0.3);
}

/* 挂号记录列表 */
.record-list {
  padding: 0 24rpx;
}

.record-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.record-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6rpx;
  background: linear-gradient(90deg, #3a9cff 0%, #1de9b6 100%);
}

.record-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.12);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.dept-badge {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  padding: 12rpx 20rpx;
  border-radius: 20rpx;
  border: 2rpx solid #bae7ff;
}

.dept-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.dept-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1890ff;
}

.status-badge {
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
}

.status-badge.pending {
  background: #fff7e6;
  color: #faad14;
  border: 2rpx solid #ffe58f;
}

.status-badge.success {
  background: #f6ffed;
  color: #52c41a;
  border: 2rpx solid #b7eb8f;
}

.status-badge.done {
  background: #e6fffb;
  color: #13c2c2;
  border: 2rpx solid #87e8de;
}

.status-badge.refund {
  background: #fff2e8;
  color: #fa541c;
  border: 2rpx solid #ffbb96;
}

.status-badge.cancelled {
  background: #fff1f0;
  color: #f5222d;
  border: 2rpx solid #ffa39e;
}

/* 医生信息 */
.doctor-info {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  padding: 16rpx 0;
}

.doctor-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.doctor-name {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}

/* 挂号类型标签 */
.type-tag-wrapper {
  margin-bottom: 24rpx;
}

.type-tag {
  display: inline-block;
  padding: 8rpx 20rpx;
  background: linear-gradient(135deg, #3a9cff 0%, #1de9b6 100%);
  color: #fff;
  font-size: 24rpx;
  border-radius: 16rpx;
  font-weight: 500;
  box-shadow: 0 2rpx 8rpx rgba(58, 156, 255, 0.3);
}

/* 详细信息区域 */
.info-section {
  background: #f8faff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
}

.info-item:not(:last-child) {
  border-bottom: 1rpx solid #e6f7ff;
}

.info-label {
  font-size: 26rpx;
  color: #666;
}

.info-value {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

/* 价格信息 */
.price-section {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: linear-gradient(135deg, #f0f7ff 0%, #f8faff 100%);
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  margin-bottom: 16rpx;
}

.price-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.price-label {
  font-size: 22rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.price-original {
  font-size: 24rpx;
  color: #999;
  text-decoration: line-through;
}

.price-actual {
  font-size: 28rpx;
  color: #ff5500;
  font-weight: 600;
}

.price-divider {
  width: 2rpx;
  height: 60rpx;
  background: #e6f7ff;
  margin: 0 20rpx;
}

/* 操作按钮 */
.action-section {
  margin-top: 12rpx;
  padding-top: 16rpx;
  border-top: 2rpx dashed #e6f7ff;
  display: flex;
  justify-content: flex-end;
}

.cancel-btn {
  font-size: 24rpx;
  padding: 8rpx 32rpx;
  border-radius: 20rpx;
  border: none;
  min-width: 160rpx;
  line-height: 1.5;
  background-color: #ff4d4f;
  color: white;
  text-align: center;
}

.cancel-btn:active {
  background-color: #d9363e;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
  font-weight: 500;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #999;
  text-align: center;
  line-height: 1.6;
}
</style>