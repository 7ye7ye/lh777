<template>
  <view class="navigation-container">
    <!-- 中间平面图区域 -->
    <view class="map-container with-panel">
      <scroll-view 
        class="map-scroll" 
        scroll-x 
        scroll-y
        :enable-flex="true"
        :show-scrollbar="false"
        :scroll-left="scrollLeft"
        :scroll-top="scrollTopMap"
      >
        <view 
          class="image-wrapper"
          :style="{ transform: `scale(${scale})`, transformOrigin: 'center center' }"
          @wheel.prevent.stop="onWheel"
          @touchstart="onTouchStart"
          @touchmove="onTouchMove"
          @touchend="onTouchEnd"
        >
          <image 
            :src="currentPlanImage" 
            class="plan-image"
            mode="widthFix"
            :lazy-load="true"
            @load="onImageLoad"
          />
        </view>
      </scroll-view>
    </view>

    <!-- 左侧悬浮楼层选择器 -->
    <view class="floor-selector-float">
      <view class="floor-arrow-up" @tap="scrollFloorUp" v-if="canScrollUp">
        <text class="arrow-icon">▲</text>
      </view>
      <scroll-view 
        class="floor-scroll" 
        scroll-y 
        :scroll-top="scrollTop"
        :scroll-with-animation="true"
        :show-scrollbar="false"
      >
        <view 
          v-for="floor in floors" 
          :key="floor.value"
          class="floor-item"
          :class="{ active: currentFloor === floor.value }"
          @tap="switchFloor(floor.value)"
        >
          <text class="floor-text">{{ floor.label }}</text>
        </view>
      </scroll-view>
      <view class="floor-arrow-down" @tap="scrollFloorDown" v-if="canScrollDown">
        <text class="arrow-icon">▼</text>
      </view>
    </view>

    <!-- 右下角缩放控制按钮 -->
    <view class="zoom-controls" :class="{ 'panel-expanded': panelExpanded }">
      <view class="zoom-btn" @tap="zoomIn">
        <text class="zoom-icon">+</text>
      </view>
      <view class="zoom-btn" @tap="zoomOut">
        <text class="zoom-icon">-</text>
      </view>
      <view class="zoom-btn" @tap="resetZoom">
        <text class="zoom-reset">重置</text>
      </view>
    </view>

    <!-- 底部导航面板 -->
    <view 
      class="bottom-panel" 
      :style="panelTranslateStyle"
      @touchstart="onPanelTouchStart"
      @touchmove="onPanelTouchMove"
      @touchend="onPanelTouchEnd"
    >
      <view class="panel-drag-area" @tap="togglePanel">
        <view class="panel-drag-bar"></view>
      </view>
      <view class="panel-header">
        <text class="panel-title">智能导航</text>
        <view class="panel-area-select">
          <picker :range="campusOptions" :value="currentCampusIndex" @change="onCampusChange">
            <view class="picker-inner">
              {{ campusOptions[currentCampusIndex] }}
              <text class="picker-arrow">▼</text>
            </view>
          </picker>
        </view>
      </view>

      <view class="panel-search" @tap="openSearchPage">
      <view class="panel-search-input">
          <text v-if="!searchText">请输入目的地或服务设施</text>
          <text v-else>{{ searchText }}</text>
        </view>
        <view class="panel-search-action">
          <image class="voice-icon" src="/static/inhos_navi/voice.svg" mode="aspectFit" />
        </view>
      </view>

      <view class="panel-quick-actions">
        <view 
          class="quick-item" 
          v-for="item in quickActions" 
          :key="item.label"
          @tap="handleQuickAction(item.label)"
        >
          <image :src="item.icon" class="quick-icon" mode="aspectFit" />
          <text>{{ item.label }}</text>
        </view>
      </view>

      <view class="panel-section">
        <view class="section-title">
          <text>热门科室</text>
          <text class="section-subtitle">门诊楼</text>
        </view>
        <view class="section-cards">
          <view
            class="section-card"
            v-for="dept in hotDepartments"
            :key="dept.name"
            @tap="handleHotDepartment(dept.name)"
          >
            <text class="card-name">{{ dept.name }}</text>
            <text class="card-loc">{{ dept.location }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { onShow } from '@dcloudio/uni-app'

// 楼层数据
const floors = [
  { label: 'F4', value: 4 },
  { label: 'F3', value: 3 },
  { label: 'F2', value: 2 },
  { label: 'F1', value: 1 }
]

// 当前选中的楼层
const currentFloor = ref(1)

// 滚动位置（楼层选择器）
const scrollTop = ref(0)

// 平面图滚动位置
const scrollLeft = ref(0)
const scrollTopMap = ref(0)

// 缩放相关
const scale = ref(1)
const minScale = 0.5
const maxScale = 3
const scaleStep = 0.2

// 手势缩放相关
const touchStartDistance = ref(0)
const touchStartScale = ref(1)
const isPinching = ref(false)

// 当前平面图路径
const currentPlanImage = computed(() => {
  return `/static/${currentFloor.value}F_plan.png`
})

// 是否可以向上滚动
const canScrollUp = computed(() => {
  return scrollTop.value > 0
})

// 是否可以向下滚动
const canScrollDown = computed(() => {
  const maxScroll = (floors.length - 1) * 80
  return scrollTop.value < maxScroll
})

// 切换楼层
const switchFloor = (floorValue) => {
  if (currentFloor.value === floorValue) {
    return
  }
  currentFloor.value = floorValue
  
  // 更新楼层选择器滚动位置，使当前楼层居中显示
  const index = floors.findIndex(f => f.value === floorValue)
  if (index >= 0) {
    // 每个楼层按钮高度约80rpx，计算滚动位置
    scrollTop.value = index * 80
  }
  
  // 切换楼层时重置平面图滚动位置和缩放
  scrollLeft.value = 0
  scrollTopMap.value = 0
  scale.value = 1
}

// 楼层选择器向上滚动
const scrollFloorUp = () => {
  scrollTop.value = Math.max(0, scrollTop.value - 80)
}

// 楼层选择器向下滚动
const scrollFloorDown = () => {
  const maxScroll = (floors.length - 1) * 80
  scrollTop.value = Math.min(maxScroll, scrollTop.value + 80)
}

// 放大
const zoomIn = () => {
  if (scale.value < maxScale) {
    scale.value = Math.min(maxScale, scale.value + scaleStep)
  }
}

// 缩小
const zoomOut = () => {
  if (scale.value > minScale) {
    scale.value = Math.max(minScale, scale.value - scaleStep)
  }
}

// 重置缩放
const resetZoom = () => {
  scale.value = 1
  scrollLeft.value = 0
  scrollTopMap.value = 0
}

// 计算两点之间的距离
const getDistance = (touch1, touch2) => {
  const dx = touch2.clientX - touch1.clientX
  const dy = touch2.clientY - touch1.clientY
  return Math.sqrt(dx * dx + dy * dy)
}

// 触摸开始
const onTouchStart = (e) => {
  if (e.touches.length === 2) {
    isPinching.value = true
    const touch1 = e.touches[0]
    const touch2 = e.touches[1]
    touchStartDistance.value = getDistance(touch1, touch2)
    touchStartScale.value = scale.value
  }
}

// 触摸移动
const onTouchMove = (e) => {
  if (isPinching.value && e.touches.length === 2) {
    const touch1 = e.touches[0]
    const touch2 = e.touches[1]
    const currentDistance = getDistance(touch1, touch2)
    const scaleRatio = currentDistance / touchStartDistance.value
    const newScale = touchStartScale.value * scaleRatio
    scale.value = Math.max(minScale, Math.min(maxScale, newScale))
  }
}

// 触摸结束
const onTouchEnd = () => {
  isPinching.value = false
  touchStartDistance.value = 0
}

// H5 模拟缩放（触控板捏合会触发 ctrl+wheel）
const onWheel = (event) => {
  // #ifdef H5
  if (!event.ctrlKey) {
    return
  }
  if (typeof event.preventDefault === 'function') {
    event.preventDefault()
  }
  if (event.deltaY < 0) {
    zoomIn()
  } else if (event.deltaY > 0) {
    zoomOut()
  }
  // #endif
}

// #ifdef H5
const wheelListener = (event) => {
  if (!event.ctrlKey) {
    return
  }
  event.preventDefault()
  if (event.deltaY < 0) {
    zoomIn()
  } else if (event.deltaY > 0) {
    zoomOut()
  }
}

onMounted(() => {
  window.addEventListener('wheel', wheelListener, { passive: false })
})

onBeforeUnmount(() => {
  window.removeEventListener('wheel', wheelListener)
})
// #endif

// 图片加载完成
const onImageLoad = (e) => {
  console.log('平面图加载完成', currentFloor.value)
}

// 面板逻辑
const panelExpanded = ref(false)
const panelHeight = 620 // rpx
const collapsedHeight = 220 // rpx
const panelTranslateStyle = computed(() => {
  const translate = panelExpanded.value ? 0 : (panelHeight - collapsedHeight)
  return `transform: translateY(${translate}rpx);`
})

const togglePanel = () => {
  panelExpanded.value = !panelExpanded.value
}

const panelTouchStartY = ref(0)
const panelTouchDelta = ref(0)

const onPanelTouchStart = (e) => {
  if (e.touches.length !== 1) return
  panelTouchStartY.value = e.touches[0].clientY
  panelTouchDelta.value = 0
}

const onPanelTouchMove = (e) => {
  if (e.touches.length !== 1) return
  panelTouchDelta.value = e.touches[0].clientY - panelTouchStartY.value
}

const onPanelTouchEnd = () => {
  if (panelTouchDelta.value < -30) {
    panelExpanded.value = true
  } else if (panelTouchDelta.value > 30) {
    panelExpanded.value = false
  }
  panelTouchStartY.value = 0
  panelTouchDelta.value = 0
}

const campusOptions = ['主院区','威海院区']
const currentCampusIndex = ref(0)
const onCampusChange = (e) => {
  currentCampusIndex.value = Number(e.detail.value || 0)
}
const HISTORY_KEY = 'NAVIGATION_SEARCH_HISTORY'
const searchText = ref('')
const openSearchPage = () => {
  uni.navigateTo({
    url: '/subpkg/hospital/navigation-search'
  })
}

const refreshSearchText = () => {
  const history = uni.getStorageSync(HISTORY_KEY)
  if (Array.isArray(history) && history.length > 0) {
    searchText.value = history[0]
  } else {
    searchText.value = ''
  }
}

onShow(() => {
  refreshSearchText()
})
const quickActions = [
  { label: '服务台', icon: '/static/inhos_navi/服务台.svg' },
  { label: '收费处', icon: '/static/inhos_navi/收费处.svg' },
  { label: '挂号', icon: '/static/inhos_navi/挂号.svg' },
  { label: '取药', icon: '/static/inhos_navi/取药.svg' },
  { label: '电梯', icon: '/static/inhos_navi/电梯.svg' },
  { label: '卫生间', icon: '/static/inhos_navi/卫生间.svg' }
]

const hotDepartments = [
  { name: '口腔科', location: '门诊楼 2F' },
  { name: '外科', location: '门诊楼 1F' }
]

const navigateWithKeyword = (keyword) => {
  const encoded = encodeURIComponent(keyword)
  uni.navigateTo({
    url: `/subpkg/hospital/navigation-search?keyword=${encoded}`
  })
}

const handleQuickAction = (label) => {
  navigateWithKeyword(label)
}

const handleHotDepartment = (deptName) => {
  navigateWithKeyword(deptName)
}
</script>

<style scoped>
.navigation-container {
  position: relative;
  width: 100%;
  height: 100vh;
  background-color: #f5f5f5;
  overflow: hidden;
}

/* 中间平面图区域 */
.map-container {
  width: 100%;
  height: 100%;
  position: relative;
  background-color: #f5f5f5;
}
.map-container.with-panel {
  padding-top: 80rpx;
  padding-bottom: 300rpx;
}

.map-scroll {
  width: 100%;
  height: 100%;
}

.image-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  transition: transform 0.1s ease-out;
  padding-top: 20rpx;
}

.plan-image {
  width: 100%;
  display: block;
  user-select: none;
}

/* 左侧悬浮楼层选择器 */
.floor-selector-float {
  position: fixed;
  left: 20rpx;
  top: 45%;
  transform: translateY(-50%);
  width: 80rpx;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
  z-index: 100;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8rpx 0;
}

.floor-arrow-up,
.floor-arrow-down {
  width: 100%;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.floor-arrow-up:active,
.floor-arrow-down:active {
  opacity: 0.6;
}

.arrow-icon {
  font-size: 20rpx;
  color: #479fff;
  font-weight: bold;
}

.floor-scroll {
  flex: 1;
  width: 100%;
  max-height: 320rpx;
}

.floor-item {
  width: 100%;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
  transition: all 0.3s;
  border-radius: 8rpx;
  margin: 4rpx 8rpx;
}

.floor-item.active {
  background-color: #479fff;
}

.floor-item.active .floor-text {
  color: #ffffff;
  font-weight: bold;
}

.floor-text {
  font-size: 28rpx;
  color: #333333;
}

/* 右下角缩放控制 */
.zoom-controls {
  position: fixed;
  right: 20rpx;
  top: 45%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  z-index: 100;
}
.zoom-controls.panel-expanded {
  top: 38%;
}

.zoom-btn {
  width: 80rpx;
  height: 80rpx;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.zoom-btn:active {
  opacity: 0.7;
  transform: scale(0.95);
}

.zoom-icon {
  font-size: 36rpx;
  color: #479fff;
  font-weight: bold;
  line-height: 1;
}

.zoom-reset {
  font-size: 24rpx;
  color: #479fff;
  font-weight: bold;
}

/* 底部导航面板 */
.bottom-panel {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 620rpx;
  background: #fff;
  border-top-left-radius: 24rpx;
  border-top-right-radius: 24rpx;
  box-shadow: 0 -8rpx 24rpx rgba(0, 0, 0, 0.1);
  z-index: 200;
  transition: transform 0.35s ease;
}

.panel-drag-area {
  width: 100%;
  padding: 16rpx 0 8rpx;
  display: flex;
  justify-content: center;
}

.panel-drag-bar {
  width: 120rpx;
  height: 10rpx;
  border-radius: 5rpx;
  background: #d8d8d8;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}

.panel-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.panel-area-select {
  background: #f5f7fb;
  border-radius: 999rpx;
  padding: 8rpx 24rpx;
}

.picker-inner {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #333;
}

.picker-arrow {
  font-size: 20rpx;
  margin-left: 8rpx;
}

.panel-search {
  margin: 20rpx 32rpx 0;
  display: flex;
  align-items: center;
  background: #f5f7fb;
  border-radius: 999rpx;
  padding: 8rpx 12rpx;
}

.panel-search-input {
  flex: 1;
  font-size: 26rpx;
  padding: 12rpx 16rpx;
  color: #333;
}

.panel-search-input text {
  color: #999;
}

.panel-search-action {
  padding: 0 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.panel-search-action .voice-icon {
  width: 40rpx;
  height: 40rpx;
}

.panel-quick-actions {
  margin: 24rpx 32rpx 0;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  row-gap: 20rpx;
  column-gap: 10rpx;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 24rpx;
  color: #333;
}

.quick-icon {
  width: 60rpx;
  height: 60rpx;
  margin-bottom: 8rpx;
}

.panel-section {
  margin-top: 24rpx;
  padding: 0 32rpx 32rpx;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.section-subtitle {
  font-size: 24rpx;
  color: #999;
}

.section-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.section-card {
  flex: 1 0 30%;
  min-width: 200rpx;
  background: #f8f9fd;
  border-radius: 16rpx;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.card-name {
  font-size: 26rpx;
  color: #333;
}

.card-loc {
  font-size: 22rpx;
  color: #999;
}
</style>

