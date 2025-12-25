<template>
  <view class="nav-plan-container">
    <!-- 顶部控制面板 -->
    <view class="control-panel">
      <view class="picker-group">
        <view class="picker-item">
          <text class="label">起点：</text>
          <picker 
            mode="selector" 
            :range="poiList" 
            range-key="name" 
            :value="startIndex" 
            @change="onStartChange"
          >
            <view class="picker-view">
              {{ poiList[startIndex] ? poiList[startIndex].name : '请选择起点' }}
              <text class="arrow">▼</text>
            </view>
          </picker>
        </view>
        
        <view class="picker-item">
          <text class="label">终点：</text>
          <picker 
            mode="selector" 
            :range="poiList" 
            range-key="name" 
            :value="endIndex" 
            @change="onEndChange"
          >
            <view class="picker-view">
              {{ poiList[endIndex] ? poiList[endIndex].name : '请选择终点' }}
              <text class="arrow">▼</text>
            </view>
          </picker>
        </view>
      </view>
      
      <button class="nav-btn" @tap="startNavigation" :disabled="isNavigating">
        {{ isNavigating ? '正在规划...' : '开始导航' }}
      </button>
    </view>

    <!-- 地图区域 (movable-view 支持双指缩放/拖拽) -->
    <movable-area class="map-container" :style="{ height: mapHeight + 'px' }">
      <movable-view 
        class="map-content"
        direction="all" 
        :scale="true" 
        :scale-min="1" 
        :scale-max="3"
        :x="mapX"
        :y="mapY"
        :scale-value="mapScale"
        :style="{ width: '100%', height: mapHeight + 'px' }"
      >
        <!-- 底图 -->
        <image 
          :src="getStaticImage('/static/医院地图new.png')" 
          class="map-image" 
          :style="{ width: '100%', height: mapHeight + 'px' }"
          mode="scaleToFill"
          @load="onMapLoad"
        />
        <!-- 路径绘制层：与底图同尺寸叠加 -->
        <canvas 
          canvas-id="navCanvas" 
          class="nav-canvas"
          :style="{ width: '100%', height: mapHeight + 'px' }"
        />
      </movable-view>

      <!-- 缩放按钮 -->
      <view class="zoom-controls">
        <button class="zoom-btn" size="mini" @tap.stop="zoomIn">+</button>
        <button class="zoom-btn" size="mini" @tap.stop="zoomOut">-</button>
      </view>
    </movable-area>

    <!-- 导航提示 -->
    <view class="nav-tips" v-if="pathFound">
      <text class="tip-text">已为您规划最优路线，全程约 {{ pathDistance }} 米</text>
    </view>
  </view>
</template>

<script>
// --- 静态路网数据---
const mapNodes = {
    'NODE_ENTRY': { x: 450, y: 580, name: '入口' }, // 骨架节点
    'NODE_LOBBY_JUNC': { x: 450, y: 560, name: '大厅十字路口' },
    'NODE_SOUTH_WEST': { x: 180, y: 560, name: '西南角' },
    'NODE_SOUTH_EAST': { x: 720, y: 560, name: '东南角' },
    'NODE_WEST_MID': { x: 180, y: 300, name: '西走廊中段' },
    'NODE_WEST_NORTH': { x: 180, y: 120, name: '西北角' },
    'NODE_EAST_MID': { x: 720, y: 300, name: '东走廊中段' },
    'NODE_EAST_NORTH': { x: 720, y: 120, name: '东北角' },
    'NODE_NORTH_MID': { x: 450, y: 120, name: '北走廊中段' },
    'NODE_CENTER_SOUTH': { x: 450, y: 460, name: '中轴南端' },
    'NODE_CENTER_MID': { x: 450, y: 300, name: '中轴中心' },
    'NODE_CENTER_NORTH': { x: 450, y: 220, name: '中轴北端' },
    'N_PHARM': { x: 180, y: 530, name: '西药房' }, 
    'N_REG': { x: 720, y: 530, name: '挂号处' }, 
    'N_NURSE': { x: 450, y: 480, name: '护士站' },
    'N_IM_1': { x: 150, y: 410, name: '内科一' }, 
    'N_IM_2': { x: 150, y: 320, name: '内科二' }, 
    'N_IM_3': { x: 150, y: 230, name: '消化内科' },
    'N_PED_1': { x: 150, y: 110, name: '儿科一' }, 
    'N_PED_2': { x: 230, y: 140, name: '儿科二' },
    'N_SURG_1': { x: 750, y: 410, name: '外科一' }, 
    'N_SURG_2': { x: 750, y: 320, name: '骨科' }, 
    'N_ENT': { x: 750, y: 230, name: '耳鼻喉' },
    'N_EYE': { x: 750, y: 110, name: '眼科' }, 
    'N_DERM': { x: 670, y: 140, name: '皮肤科' },
    'N_US': { x: 440, y: 300, name: 'B超室' }, 
    'N_XRAY': { x: 460, y: 300, name: '放射科' },
    'N_WC_WEST': { x: 440, y: 175, name: '西WC' }, 
    'N_WC_EAST': { x: 460, y: 175, name: '东WC' },
    'N_LAB': { x: 360, y: 140, name: '检验科' }, 
    'N_CT': { x: 540, y: 140, name: 'CT室' }
};

const mapGraph = {
    'NODE_ENTRY': { 'NODE_LOBBY_JUNC': 20 },
    'NODE_LOBBY_JUNC': { 'NODE_ENTRY': 20, 'NODE_SOUTH_WEST': 270, 'NODE_SOUTH_EAST': 270, 'NODE_CENTER_SOUTH': 100 },
    'NODE_SOUTH_WEST': { 'NODE_LOBBY_JUNC': 270, 'NODE_WEST_MID': 260, 'N_PHARM': 30 },
    'NODE_SOUTH_EAST': { 'NODE_LOBBY_JUNC': 270, 'NODE_EAST_MID': 260, 'N_REG': 30 },
    'NODE_WEST_MID': { 'NODE_SOUTH_WEST': 260, 'NODE_WEST_NORTH': 180, 'N_IM_1': 110, 'N_IM_2': 20, 'N_IM_3': 70 },
    'NODE_WEST_NORTH': { 'NODE_WEST_MID': 180, 'NODE_NORTH_MID': 270, 'N_PED_1': 30, 'N_PED_2': 50 },
    'NODE_EAST_MID': { 'NODE_SOUTH_EAST': 260, 'NODE_EAST_NORTH': 180, 'N_SURG_1': 110, 'N_SURG_2': 20, 'N_ENT': 70 },
    'NODE_EAST_NORTH': { 'NODE_EAST_MID': 180, 'NODE_NORTH_MID': 270, 'N_EYE': 30, 'N_DERM': 50 },
    'NODE_NORTH_MID': { 'NODE_WEST_NORTH': 270, 'NODE_EAST_NORTH': 270, 'NODE_CENTER_NORTH': 100, 'N_LAB': 90, 'N_CT': 90 },
    'NODE_CENTER_SOUTH': { 'NODE_LOBBY_JUNC': 100, 'NODE_CENTER_MID': 160, 'N_NURSE': 20 },
    'NODE_CENTER_MID': { 'NODE_CENTER_SOUTH': 160, 'NODE_CENTER_NORTH': 80, 'N_US': 10, 'N_XRAY': 10 },
    'NODE_CENTER_NORTH': { 'NODE_CENTER_MID': 80, 'NODE_NORTH_MID': 100, 'N_WC_WEST': 45, 'N_WC_EAST': 45 },
    'N_PHARM': { 'NODE_SOUTH_WEST': 30 }, 
    'N_REG': { 'NODE_SOUTH_EAST': 30 }, 
    'N_NURSE': { 'NODE_CENTER_SOUTH': 20 },
    'N_IM_1': { 'NODE_WEST_MID': 110 }, 
    'N_IM_2': { 'NODE_WEST_MID': 20 }, 
    'N_IM_3': { 'NODE_WEST_MID': 70 },
    'N_PED_1': { 'NODE_WEST_NORTH': 30 }, 
    'N_PED_2': { 'NODE_WEST_NORTH': 50 },
    'N_SURG_1': { 'NODE_EAST_MID': 110 }, 
    'N_SURG_2': { 'NODE_EAST_MID': 20 }, 
    'N_ENT': { 'NODE_EAST_MID': 70 },
    'N_EYE': { 'NODE_EAST_NORTH': 30 }, 
    'N_DERM': { 'NODE_EAST_NORTH': 50 },
    'N_LAB': { 'NODE_NORTH_MID': 90 }, 
    'N_CT': { 'NODE_NORTH_MID': 90 },
    'N_US': { 'NODE_CENTER_MID': 10 }, 
    'N_XRAY': { 'NODE_CENTER_MID': 10 },
    'N_WC_WEST': { 'NODE_CENTER_NORTH': 45 }, 
    'N_WC_EAST': { 'NODE_CENTER_NORTH': 45 }
};

import { getStaticImage } from '@/utils/imageHelper'

export default {
  methods: {
    getStaticImage
  },
  data() {
    return {
      ORIGIN_WIDTH: 900,
      ORIGIN_HEIGHT: 650,
      screenWidth: 375,
      scaleRatio: 1,
      mapHeight: 0,
      
      poiList: [],
      startIndex: 0,
      endIndex: -1,
      isNavigating: false,
      pathFound: false,
      pathDistance: 0,
      ctx: null,

      // 地图缩放控制
      mapX: 0,
      mapY: 0,
      mapScale: 1
    }
  },
  
  onLoad() {
    this.initMapConfig()
    this.initPoiList()
  },
  
  onReady() {
    this.ctx = uni.createCanvasContext('navCanvas', this)
  },
  
  methods: {
    initMapConfig() {
      const sysInfo = uni.getSystemInfoSync()
      this.screenWidth = sysInfo.windowWidth
      this.scaleRatio = this.screenWidth / this.ORIGIN_WIDTH
      this.mapHeight = this.ORIGIN_HEIGHT * this.scaleRatio
    },
    
    initPoiList() {
      const list = []
      for (let key in mapNodes) {
        if (key.startsWith('N_') || key === 'NODE_ENTRY') {
          list.push({
            id: key,
            name: mapNodes[key].name,
            x: mapNodes[key].x,
            y: mapNodes[key].y
          })
        }
      }
      this.poiList = list
      const entryIdx = list.findIndex(item => item.id === 'NODE_ENTRY')
      if (entryIdx >= 0) this.startIndex = entryIdx
    },
    
    onStartChange(e) {
      this.startIndex = Number(e.detail.value)
    },
    
    onEndChange(e) {
      this.endIndex = Number(e.detail.value)
    },
    
    startNavigation() {
      if (this.startIndex < 0 || this.endIndex < 0) {
        uni.showToast({ title: '请选择起点和终点', icon: 'none' })
        return
      }
      if (this.startIndex === this.endIndex) {
        uni.showToast({ title: '起点和终点不能相同', icon: 'none' })
        return
      }
      
      const startNodeId = this.poiList[this.startIndex].id
      const endNodeId = this.poiList[this.endIndex].id
      
      // 重置缩放
      this.mapX = 0
      this.mapY = 0
      this.mapScale = 1

      this.$nextTick(() => {
        this.isNavigating = true
        const result = this.dijkstra(startNodeId, endNodeId)
        
        if (result.path.length > 0) {
          this.pathFound = true
          this.pathDistance = result.distance
          this.drawPath(result.path)
        } else {
          uni.showToast({ title: '未找到路径', icon: 'none' })
        }
        
        this.isNavigating = false
      })
    },
    
    // 缩放控制：更新 movable-view 的 scale-value，canvas 会随容器一起缩放
    zoomIn() {
      const step = 0.2
      const max = 3
      const next = Math.min(max, this.mapScale + step)
      this.mapScale = Number(next.toFixed(2))
    },
    zoomOut() {
      const step = 0.2
      const min = 1
      const next = Math.max(min, this.mapScale - step)
      this.mapScale = Number(next.toFixed(2))
    },
    
    // Dijkstra 最短路径：返回 { path: [nodeId...], distance: number }
    dijkstra(startId, endId) {
      const distances = {}
      const previous = {}
      const pq = new PriorityQueue()
      
      for (let node in mapNodes) {
        if (node === startId) {
          distances[node] = 0
          pq.enqueue(node, 0)
        } else {
          distances[node] = Infinity
          pq.enqueue(node, Infinity)
        }
        previous[node] = null
      }
      
      while (!pq.isEmpty()) {
        const currentId = pq.dequeue().element
        if (currentId === endId) break
        if (distances[currentId] === Infinity) break
        
        const neighbors = mapGraph[currentId] || {}
        for (let neighborId in neighbors) {
          const weight = neighbors[neighborId]
          const alt = distances[currentId] + weight
          if (alt < distances[neighborId]) {
            distances[neighborId] = alt
            previous[neighborId] = currentId
            pq.enqueue(neighborId, alt)
          }
        }
      }
      
      const path = []
      let current = endId
      if (distances[endId] === Infinity) return { path: [], distance: 0 }
      
      while (current !== null) {
        path.unshift(current)
        current = previous[current]
      }
      
      return { path, distance: distances[endId] }
    },
    
    // 按当前缩放比把节点坐标映射到屏幕像素，绘制路径+起终点
    drawPath(nodeIdList) {
      if (!this.ctx) return
      this.ctx.clearRect(0, 0, this.screenWidth, this.mapHeight)
      if (nodeIdList.length < 2) return
      
      const ratio = this.scaleRatio
      
      this.ctx.beginPath()
      this.ctx.setStrokeStyle('#FF3333')
      this.ctx.setLineWidth(4)
      this.ctx.setLineCap('round')
      this.ctx.setLineJoin('round')
      
      const startNode = mapNodes[nodeIdList[0]]
      this.ctx.moveTo(startNode.x * ratio, startNode.y * ratio)
      
      for (let i = 1; i < nodeIdList.length; i++) {
        const node = mapNodes[nodeIdList[i]]
        this.ctx.lineTo(node.x * ratio, node.y * ratio)
      }
      this.ctx.stroke()
      
      const start = mapNodes[nodeIdList[0]]
      this.drawPoint(start.x * ratio, start.y * ratio, '#09BB07')
      
      const end = mapNodes[nodeIdList[nodeIdList.length - 1]]
      this.drawPoint(end.x * ratio, end.y * ratio, '#FF3333')
      
      this.ctx.draw()
    },
    
    drawPoint(x, y, color) {
      this.ctx.beginPath()
      this.ctx.arc(x, y, 6, 0, 2 * Math.PI)
      this.ctx.setFillStyle(color)
      this.ctx.fill()
      this.ctx.setStrokeStyle('#FFFFFF')
      this.ctx.setLineWidth(2)
      this.ctx.stroke()
    },
    
    onMapLoad() {
      console.log('底图加载完成')
    }
  }
}

class PriorityQueue {
  // 简单双向插入的优先队列（小顶堆逻辑用数组插入保持有序）
  constructor() { this.items = [] }
  enqueue(element, priority) {
    const queueElement = { element, priority }
    let added = false
    for (let i = 0; i < this.items.length; i++) {
      if (queueElement.priority < this.items[i].priority) {
        this.items.splice(i, 0, queueElement)
        added = true
        break
      }
    }
    if (!added) this.items.push(queueElement)
  }
  dequeue() { return this.items.shift() }
  isEmpty() { return this.items.length === 0 }
}
</script>

<style scoped>
.nav-plan-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.control-panel {
  background: #ffffff;
  padding: 30rpx;
  border-bottom: 2rpx solid #eeeeee;
}

.picker-group { margin-bottom: 30rpx; }
.picker-item { display: flex; align-items: center; margin-bottom: 20rpx; font-size: 28rpx; }
.label { width: 100rpx; color: #333; font-weight: bold; }
.picker-view { flex: 1; height: 80rpx; background: #f5f7fb; border-radius: 12rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 24rpx; color: #333; }
.arrow { color: #999; font-size: 24rpx; }

.nav-btn { background: #479fff; color: #ffffff; font-size: 32rpx; border-radius: 44rpx; height: 88rpx; line-height: 88rpx; margin-top: 20rpx; }

/* 地图区域样式更新 */
.map-container {
  width: 100%;
  background-color: #f0f0f0;
  overflow: hidden;
}

.map-content {
  width: 100%;
  position: relative;
  transform-origin: center center;
}

.map-image {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
  display: block;
}

.nav-canvas {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  pointer-events: none; /* 让触摸事件穿透到 movable-view */
}

.zoom-controls {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  z-index: 3;
  display: flex;
  flex-direction: row;
  gap: 16rpx;
}

.zoom-btn {
  min-width: 72rpx;
  height: 56rpx;
  line-height: 56rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: #479fff;
  color: #fff;
  font-size: 30rpx;
  border: none;
}

.zoom-btn:active {
  background: #227aff;
}

.nav-tips { padding: 24rpx 30rpx; background: #e6f3ff; color: #479fff; font-size: 26rpx; text-align: center; }
</style>