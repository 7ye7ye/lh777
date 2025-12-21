<template>
  <view class="page-bg">
    <!-- 患者选择 -->
    <view class="patient-select">
      <picker mode="selector" :range="patients.map(p => p.patientName)" @change="onPatientChange">
        <view class="picker-text">
          当前患者：{{ selectedPatient?.patientName || '请选择患者' }}
        </view>
      </picker>
    </view>

    <!-- 状态筛选 -->
    <view class="status-tabs">
      <view
        v-for="tab in filterTabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: activeStatus === tab.value }"
        @click="activeStatus = tab.value"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 挂号记录列表 -->
    <view class="record-list">
      <view v-for="item in filteredRecords" :key="item.id" class="record-item">
        
        <!-- 左上角：科室 + 医生 -->
        <view class="top-row">
          <view class="dept">{{ item.dept }}</view>
          <view class="doctor">{{ item.doctor }}</view>
        </view>

        <!-- 挂号类型 + 状态 -->
        <view class="row">
          <view class="tag">{{ item.type }}</view>
          <view :class="['status', statusClassMap[item.status]]">{{ statusTextMap[item.status] }}</view>
        </view>

        <!-- 挂号时间 -->
        <view class="row">
          <text class="label">挂号时间：</text>
          <text>{{ item.time }}</text>
        </view>

        <!-- 价格信息 -->
        <view class="row price-row">
          <text class="price-original">原价: {{ item.priceOriginal }}</text>
          <text class="price-actual">实付: {{ item.actualPrice }}</text>
        </view>

        <!-- 右下角：取消挂号按钮 -->
        <view class="row cancel-row" v-if="item.status === 1">
          <button class="cancel-btn" @click="cancelRecord(item)">取消</button>
        </view>
      </view>

      <view v-if="records.length === 0" class="empty-text">暂无挂号记录</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { patientApi } from "@/api/patient"; // 使用新的接口
import { getRegistrationRecords } from "@/api/registration"; // 获取挂号记录
import { cancelRegistration } from "@/api/registration";
import { ensurePatientCard } from "@/utils/patientHelper";

const patients = ref([]);
const selectedPatient = ref(null);
const records = ref([]);
const activeStatus = ref(-1);

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

/** 医生映射 */
const doctorMap = {
  1: "张仁泽", 2: "李修远", 3: "王景明",
  7: "刘承宇", 8: "陈博彦", 9: "杨明哲",
  10: "黄俊诚", 11: "周泽谦", 12: "周泽谦", 13: "周泽谦",
  14: "马嘉树", 15: "朱彦霖", 16: "胡泽远", 17: "胡泽远",
  18: "高子谦", 19: "梁景琛", 20: "谢君浩",
  21: "宋宇航", 22: "宋宇航", 23: "唐承泽", 24: "许博文",
  25: "邓彦辰", 26: "韩明远", 27: "冯俊逸", 28: "曹泽楷",
  29: "彭思远", 30: "曾明哲", 31: "吕嘉恒", 32: "苏景瑜",
  33: "卢子轩", 34: "蒋彦博",
  119: "蔡泽安", 120: "魏明宇", 121: "丁嘉树", 122: "薛子谦",
  123: "阎景然", 124: "潘承宇", 125: "杜彦辰", 126: "夏思远",
  127: "汪星朗", 128: "田亦辰", 129: "李慧宁", 130: "王芷然",
  131: "张语桐", 132: "刘若溪", 133: "陈欣悦", 134: "杨思妍",
  135: "黄雨馨", 136: "周芷柔", 137: "吴诗涵", 138: "吴诗涵",
  139: "马若涵", 140: "朱芷妍", 141: "胡雨然", 142: "罗诗琪",
  143: "高语馨", 144: "梁若菲", 145: "谢欣冉", 146: "宋芷瑜",
  147: "唐雨桐", 148: "唐雨桐", 149: "邓语茉", 150: "韩若曦",
  151: "冯静姝", 152: "曹雨馨", 153: "彭诗涵", 154: "曾语桐",
  155: "吕若涵", 156: "苏芷妍", 157: "卢雨然", 158: "蒋诗琪",
  159: "蔡语馨", 160: "魏若菲", 161: "丁欣冉", 162: "薛芷瑜",
  165: "潘思悦", 166: "杜语茉", 167: "戴若曦", 168: "夏静妍",
  169: "汪雨馨", 202: "侯明昊", 204: "田诗涵"
};

/** 科室映射 */
const scheduleMap = {
  1: "内科", 2: "外科", 3: "预防保健科", 4: "体检科", 5: "口腔科", 6: "B超室",
  7: "护理科", 8: "公疗报销", 11: "呼吸内科", 12: "心内科", 13: "消化内科", 14: "神经内科",
  15: "内分泌科", 21: "骨科", 22: "皮肤科", 23: "普通外科", 24: "外伤处理",
  31: "儿童保健", 32: "妇女保健", 33: "老年人保健",
  41: "常规体检", 42: "入职体检", 43: "专项体检",
  51: "口腔内科", 52: "口腔外科", 53: "口腔修复",
  61: "腹部B超", 62: "妇科B超", 63: "心脏彩超",
  71: "门诊护理", 72: "住院护理", 73: "社区护理",
  81: "门诊报销", 82: "住院报销", 83: "慢病报销",
  300: "肿瘤科", 302: "测试", 303: "测试2", 304: "康复", 305: "测试1"
};

const registrationTypeMap = {
  1: "普通号",
  2: "专家号",
  3: "特需号",
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


// 获取挂号记录
const loadRecords = async () => {
  if (!selectedPatient.value) return;
  try {
    const res = await getRegistrationRecords(selectedPatient.value.patientId);
    const list = Array.isArray(res) ? res : [];
    records.value = list.map(item => {
      const deptName = scheduleMap[item.scheduleId] || `科室ID: ${item.scheduleId}`;
      const doctorName = doctorMap[item.doctorId] || `医生ID: ${item.doctorId}`;
      const typeName = registrationTypeMap[item.typeId] || `挂号类型ID: ${item.typeId}`;
      return {
        id: item.recordId,
        patientId: selectedPatient.value.patientId,
        patientName: selectedPatient.value.patientName,
        dept: deptName,
        doctor: `${doctorName} ${item.doctorTitle || ""}`,
        type: typeName,
        time: item.registerTime || "",
        registrationNo: item.registrationNo || "",
        status: item.status,
        priceOriginal: item.priceOriginal,
        actualPrice: item.actualPrice,
      };
    });
  } catch (e) {
    console.error("获取挂号记录失败:", e);
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


onMounted(loadPatients);
</script>


<style scoped>
.page-bg { min-height: 100vh; background: #f0f2f5; padding-bottom: 50rpx; }
.patient-select { margin: 24rpx; }
.picker-text { font-size: 28rpx; color: #333; }
.record-list { background: #fff; margin: 0 24rpx; border-radius: 16rpx; padding: 24rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05); }
.record-item { border-bottom: 1rpx solid #eee; padding: 24rpx 0; position: relative; }
.record-item:last-child { border-bottom: none; }

/* 左上角科室 + 医生 */
.top-row { display: flex; justify-content: flex-start; align-items: center; font-weight: bold; font-size: 32rpx; margin-bottom: 12rpx; color: #222; }
.dept { color: #3a9cff; margin-right: 20rpx; }
.doctor { color: #666; font-size: 28rpx; }

/* 行 */
.row { display: flex; align-items: center; margin-bottom: 8rpx; }
.label { color: #888; width: 120rpx; font-size: 28rpx; }
.tag { padding: 4rpx 12rpx; border-radius: 12rpx; background: #e6f7ff; color: #1890ff; font-size: 26rpx; margin-right: 16rpx; }
.status { font-size: 26rpx; color: #52c41a; }

/* 价格 */
.price-row { margin-top: 8rpx; }
.price-original { color: #888; margin-right: 20rpx; }
.price-actual { color: #fa541c; font-weight: bold; }

/* 取消按钮放右下 */
.cancel-row { 
  display: flex; 
  justify-content: flex-end; 
  margin-top: 16rpx; 
}

.cancel-btn {
  background: #fa541c;
  color: #fff;
  font-size: 28rpx;
  padding: 6rpx 12rpx;
  border-radius: 20rpx; /* 圆角更大，看起来更流线型 */
  min-width: 140rpx;    /* 按钮长度增加 */
  text-align: center;
  border: none;
}

.empty-text { text-align: center; color: #999; padding: 48rpx 0; font-size: 28rpx; }

/* 状态标签颜色 */
.status.pending { color: #faad14; }
.status.success { color: #52c41a; }
.status.done { color: #13c2c2; }
.status.refund { color: #fa541c; }
.status.cancelled { color: #f5222d; }

/* 状态标签 tabs */
.status-tabs { display: flex; background: #fff; margin: 0 24rpx 20rpx; padding: 16rpx 0; border-radius: 12rpx; overflow-x: auto; }
.tab-item { padding: 12rpx 24rpx; font-size: 28rpx; color: #666; margin-right: 20rpx; white-space: nowrap; }
.tab-item.active { color: #3a9cff; font-weight: bold; border-bottom: 4rpx solid #3a9cff; }
</style>