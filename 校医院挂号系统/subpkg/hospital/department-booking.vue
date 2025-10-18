<template>
  <view class="department-booking-page">
    <!-- 顶部横幅 -->
    <view class="header-banner">
      <view class="banner-content">
        <text class="banner-title">选择就诊科室</text>
        <text class="banner-subtitle">北京交通大学校医院为您服务</text>
      </view>
      <image class="banner-image" src="/static/logo.png" mode="aspectFit" />
    </view>

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          placeholder="搜索科室名称..." 
          v-model="searchKeyword"
          @input="onSearch"
        />
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-access">
      <view class="quick-item" @click="goToDiseaseGuide">
        <view class="quick-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <text>🔍</text>
        </view>
        <text class="quick-text">按疾病查找</text>
      </view>
      <view class="quick-item" @click="showHotDepts">
        <view class="quick-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <text>🔥</text>
        </view>
        <text class="quick-text">热门科室</text>
      </view>
      <view class="quick-item" @click="callConsult">
        <view class="quick-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
          <text>📞</text>
        </view>
        <text class="quick-text">电话咨询</text>
      </view>
      <view class="quick-item" @click="showAllDepts">
        <view class="quick-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
          <text>📋</text>
        </view>
        <text class="quick-text">全部科室</text>
      </view>
    </view>

    <!-- 科室分类 -->
    <scroll-view class="department-list" scroll-y :show-scrollbar="false">
      <view v-if="!searchKeyword" class="list-content">
        <!-- 热门科室 -->
        <view class="dept-section" v-if="showHot">
          <view class="section-header">
            <text class="section-icon">🔥</text>
            <text class="section-title">热门科室</text>
            <text class="section-tag">就诊量最高</text>
          </view>
          <view class="dept-grid">
            <view 
              v-for="(dept, index) in hotDepartments" 
              :key="index"
              class="dept-card hot"
              @click="selectDept(dept)"
            >
              <view class="card-header">
                <text class="card-icon">{{ dept.icon }}</text>
                <view class="hot-badge">HOT</view>
              </view>
              <text class="card-name">{{ dept.name }}</text>
              <text class="card-desc">{{ dept.desc }}</text>
              <view class="card-footer">
                <text class="waiting-count">{{ dept.waiting }}人等候</text>
                <text class="arrow">›</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 按分类显示科室 -->
        <view 
          v-for="(category, index) in departmentCategories" 
          :key="index"
          class="dept-section"
        >
          <view class="section-header">
            <text class="section-icon">{{ category.icon }}</text>
            <text class="section-title">{{ category.name }}</text>
            <text class="section-count">{{ category.depts.length }}个</text>
          </view>
          <view class="dept-list-items">
            <view 
              v-for="(dept, idx) in category.depts" 
              :key="idx"
              class="dept-item"
              @click="selectDept(dept)"
            >
              <view class="item-left">
                <text class="item-icon">{{ dept.icon }}</text>
                <view class="item-info">
                  <text class="item-name">{{ dept.name }}</text>
                  <text class="item-desc">{{ dept.desc }}</text>
                </view>
              </view>
              <view class="item-right">
                <view class="status-tag" :class="dept.status">
                  {{ dept.statusText }}
                </view>
                <text class="arrow">›</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 搜索结果 -->
      <view v-else class="search-results">
        <view v-if="searchResults.length > 0">
          <view class="result-header">
            <text>找到 {{ searchResults.length }} 个科室</text>
          </view>
          <view class="dept-list-items">
            <view 
              v-for="(dept, index) in searchResults" 
              :key="index"
              class="dept-item"
              @click="selectDept(dept)"
            >
              <view class="item-left">
                <text class="item-icon">{{ dept.icon }}</text>
                <view class="item-info">
                  <text class="item-name">{{ dept.name }}</text>
                  <text class="item-desc">{{ dept.desc }}</text>
                </view>
              </view>
              <view class="item-right">
                <view class="status-tag" :class="dept.status">
                  {{ dept.statusText }}
                </view>
                <text class="arrow">›</text>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="empty-result">
          <text class="empty-icon">🔍</text>
          <text class="empty-text">未找到相关科室</text>
        </view>
      </view>

      <!-- 底部提示 -->
      <view class="bottom-tips">
        <text class="tip-text">💡 不确定挂哪个科室？试试"按疾病查找"功能</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const searchKeyword = ref('')
const showHot = ref(true)

// 热门科室
const hotDepartments = ref([
  { 
    id: 14, 
    name: '呼吸内科', 
    icon: '🫁', 
    desc: '感冒、咳嗽、肺部疾病', 
    waiting: 5,
    status: 'available',
    statusText: '可预约'
  },
  { 
    id: 13, 
    name: '消化内科', 
    icon: '🫄', 
    desc: '胃肠疾病诊疗', 
    waiting: 3,
    status: 'available',
    statusText: '可预约'
  },
  { 
    id: 43, 
    name: '常规体检', 
    icon: '📋', 
    desc: '健康体检服务', 
    waiting: 8,
    status: 'available',
    statusText: '可预约'
  },
  { 
    id: 52, 
    name: '口腔治疗', 
    icon: '🦷', 
    desc: '口腔疾病诊疗', 
    waiting: 6,
    status: 'busy',
    statusText: '较忙'
  }
])

// 科室分类
const departmentCategories = ref([
  {
    name: '内科系统',
    icon: '🏥',
    depts: [
      { id: 11, name: '心内科', icon: '❤️', desc: '心脏疾病、高血压', status: 'available', statusText: '可预约' },
      { id: 12, name: '神经内科', icon: '🧠', desc: '头痛、失眠、神经痛', status: 'available', statusText: '可预约' },
      { id: 13, name: '消化内科', icon: '🫄', desc: '胃痛、腹泻、肠胃炎', status: 'available', statusText: '可预约' },
      { id: 14, name: '呼吸内科', icon: '🫁', desc: '咳嗽、哮喘、肺部疾病', status: 'available', statusText: '可预约' },
      { id: 15, name: '内分泌科', icon: '🩸', desc: '糖尿病、甲状腺疾病', status: 'available', statusText: '可预约' }
    ]
  },
  {
    name: '外科系统',
    icon: '👨‍⚕️',
    depts: [
      { id: 21, name: '骨科', icon: '🦴', desc: '骨折、关节炎、颈腰椎病', status: 'available', statusText: '可预约' },
      { id: 22, name: '皮肤科', icon: '🩹', desc: '皮炎、湿疹、皮肤病', status: 'available', statusText: '可预约' },
      { id: 23, name: '普通外科', icon: '🔪', desc: '痔疮、胆囊炎、阑尾炎', status: 'available', statusText: '可预约' },
      { id: 24, name: '外伤处理', icon: '🩹', desc: '外伤、扭伤、创伤处理', status: 'available', statusText: '可预约' }
    ]
  },
  {
    name: '口腔科室',
    icon: '👁️',
    depts: [
      { id: 51, name: '口腔咨询', icon: '💬', desc: '口腔健康咨询', status: 'available', statusText: '可预约' },
      { id: 52, name: '口腔治疗', icon: '🦷', desc: '牙齿治疗、口腔疾病', status: 'busy', statusText: '较忙' }
    ]
  },
  {
    name: '预防保健',
    icon: '🛡️',
    depts: [
      { id: 43, name: '常规体检', icon: '📋', desc: '健康体检、入职体检', status: 'available', statusText: '可预约' },
      { id: 33, name: '疫苗接种', icon: '💉', desc: '流感疫苗、其他疫苗', status: 'available', statusText: '可预约' },
      { id: 31, name: '老年免费流感疫苗', icon: '💉', desc: '65岁以上免费接种', status: 'available', statusText: '可预约' }
    ]
  }
])

// 搜索结果
const searchResults = computed(() => {
  if (!searchKeyword.value) return []
  
  const keyword = searchKeyword.value.toLowerCase()
  const results = []
  
  departmentCategories.value.forEach(category => {
    category.depts.forEach(dept => {
      if (dept.name.toLowerCase().includes(keyword) || 
          dept.desc.toLowerCase().includes(keyword)) {
        results.push(dept)
      }
    })
  })
  
  return results
})

// 搜索
const onSearch = () => {
  showHot.value = !searchKeyword.value
}

// 选择科室
const selectDept = (dept) => {
  uni.navigateTo({
    url: `/subpkg/hospital/department-detail?deptId=${dept.id}`
  })
}

// 前往按疾病查找
const goToDiseaseGuide = () => {
  uni.navigateTo({
    url: '/subpkg/hospital/disease-guide'
  })
}

// 显示热门科室
const showHotDepts = () => {
  showHot.value = true
  searchKeyword.value = ''
  uni.pageScrollTo({
    scrollTop: 0,
    duration: 300
  })
}

// 显示全部科室
const showAllDepts = () => {
  showHot.value = false
  uni.pageScrollTo({
    scrollTop: 400,
    duration: 300
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
</script>

<style scoped>
.department-booking-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.header-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 48rpx 32rpx 80rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

.banner-content {
  flex: 1;
}

.banner-title {
  display: block;
  font-size: 44rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 12rpx;
}

.banner-subtitle {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
}

.banner-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.2);
  padding: 16rpx;
}

.search-section {
  margin-top: -48rpx;
  padding: 0 32rpx 24rpx;
}

.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 48rpx;
  padding: 24rpx 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.15);
}

.search-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
}

.quick-access {
  display: flex;
  justify-content: space-around;
  padding: 32rpx 32rpx 24rpx;
  background: #fff;
  margin: 0 32rpx 24rpx;
  border-radius: 16rpx;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.quick-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  margin-bottom: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.quick-text {
  font-size: 24rpx;
  color: #666;
}

.department-list {
  flex: 1;
  padding: 0 32rpx 24rpx;
}

.list-content {
  min-height: 100%;
}

.dept-section {
  margin-bottom: 24rpx;
}

.section-header {
  display: flex;
  align-items: center;
  padding: 0 4rpx 16rpx;
}

.section-icon {
  font-size: 36rpx;
  margin-right: 12rpx;
}

.section-title {
  flex: 1;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.section-tag {
  font-size: 20rpx;
  color: #f5576c;
  background: rgba(245, 87, 108, 0.1);
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
}

.section-count {
  font-size: 22rpx;
  color: #999;
}

.dept-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10rpx;
  padding: 0 16rpx;
}

.dept-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  min-height: 230rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  width: 100%;
  max-width: 300rpx;
  box-sizing: border-box;
}

.dept-card:active {
  transform: scale(0.98);
}

.dept-card.hot {
  border: 2rpx solid #f5576c;
  background: linear-gradient(180deg, #fff 0%, #fff5f7 100%);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.card-icon {
  font-size: 48rpx;
}

.hot-badge {
  font-size: 18rpx;
  color: #fff;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 4rpx 10rpx;
  border-radius: 8rpx;
  font-weight: bold;
  white-space: nowrap;
}

.card-name {
  display: block;
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-desc {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-bottom: 12rpx;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  max-height: 62rpx;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10rpx;
  border-top: 1rpx solid #f0f0f0;
  margin-top: auto;
}

.waiting-count {
  font-size: 20rpx;
  color: #f5576c;
  white-space: nowrap;
}

.arrow {
  font-size: 40rpx;
  color: #ccc;
  font-weight: 300;
}

.dept-list-items {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.dept-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
  transition: all 0.2s;
}

.dept-item:last-child {
  border-bottom: none;
}

.dept-item:active {
  background: #f8f9fa;
}

.item-left {
  flex: 1;
  display: flex;
  align-items: center;
  margin-right: 16rpx;
}

.item-icon {
  font-size: 56rpx;
  margin-right: 24rpx;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 8rpx;
}

.item-desc {
  font-size: 24rpx;
  color: #999;
}

.item-right {
  display: flex;
  align-items: center;
}

.status-tag {
  font-size: 22rpx;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  margin-right: 12rpx;
}

.status-tag.available {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.1);
}

.status-tag.busy {
  color: #faad14;
  background: rgba(250, 173, 20, 0.1);
}

.status-tag.full {
  color: #f5222d;
  background: rgba(245, 34, 45, 0.1);
}

.search-results {
  background: #fff;
  border-radius: 16rpx;
}

.result-header {
  font-size: 28rpx;
  color: #666;
  padding: 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.empty-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
}

.bottom-tips {
  text-align: center;
  padding: 48rpx 0;
}

.tip-text {
  font-size: 24rpx;
  color: #999;
}
</style>
