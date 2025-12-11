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

    <!-- 地图区域 -->
    <view class="map-wrapper" :style="{ height: mapHeight + 'px' }">
      <!-- 底图 -->
      <image 
        src="/static/医院地图new.png" 
        class="map-image" 
        :style="{ width: '100%', height: mapHeight + 'px' }"
        mode="scaleToFill"
        @load="onMapLoad"
      />
      <!-- 路径绘制层 -->
      <canvas 
        canvas-id="navCanvas" 
        class="nav-canvas"
        :style="{ width: '100%', height: mapHeight + 'px' }"
      />
    </view>

    <!-- 导航提示 -->
    <view class="nav-tips" v-if="pathFound">
      <text class="tip-text">已为您规划最优路线，全程约 {{ pathDistance }} 米</text>
    </view>
  </view>
</template>

<script>
/**
 * 院内导航路径规划组件
 * 包含Dijkstra最短路径算法和Canvas绘图逻辑
 */

// --- 静态路网数据定义 ---
// 节点定义：x, y 为基于 900x650 图纸的像素坐标
const mapNodes = {
    // 路网骨架节点
    'NODE_ENTRY': { x: 450, y: 580, name: '入口' },
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

    // 具体房间/功能区
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

// 邻接表（权重为大致距离）
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

export default {
  data() {
    return {
      // 原始设计尺寸
      ORIGIN_WIDTH: 900,
      ORIGIN_HEIGHT: 650,
      
      // 当前设备屏幕信息
      screenWidth: 375,
      scaleRatio: 1, // 缩放比例
      mapHeight: 0, // 计算后的地图高度
      
      // 导航相关数据
      poiList: [], // 可选地点列表
      startIndex: 0, // 默认选中入口
      endIndex: -1, // 终点
      
      isNavigating: false,
      pathFound: false,
      pathDistance: 0,
      
      // Canvas上下文
      ctx: null
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
    // 1. 初始化地图配置，计算缩放比
    initMapConfig() {
      const sysInfo = uni.getSystemInfoSync()
      this.screenWidth = sysInfo.windowWidth
      // 计算缩放比例：当前屏幕宽度 / 设计图宽度
      this.scaleRatio = this.screenWidth / this.ORIGIN_WIDTH
      // 计算自适应后的地图高度
      this.mapHeight = this.ORIGIN_HEIGHT * this.scaleRatio
    },
    
    // 2. 初始化POI列表 (过滤掉不需要展示的路口节点)
    initPoiList() {
      const list = []
      for (let key in mapNodes) {
        // 通过key的前缀判断是否为展示节点
        // 这里简单规则：所有N_开头的都是具体房间，或者NODE_ENTRY入口
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
      
      // 默认设置入口为起点
      const entryIdx = list.findIndex(item => item.id === 'NODE_ENTRY')
      if (entryIdx >= 0) this.startIndex = entryIdx
    },
    
    // 起点变更
    onStartChange(e) {
      this.startIndex = Number(e.detail.value)
    },
    
    // 终点变更
    onEndChange(e) {
      this.endIndex = Number(e.detail.value)
      // 如果选了终点，自动触发导航
      // this.startNavigation() 
    },
    
    // 3. 开始导航
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
      
      this.isNavigating = true
      
      // 计算最短路径
      const result = this.dijkstra(startNodeId, endNodeId)
      
      if (result.path.length > 0) {
        this.pathFound = true
        this.pathDistance = result.distance
        this.drawPath(result.path)
      } else {
        uni.showToast({ title: '未找到路径', icon: 'none' })
      }
      
      this.isNavigating = false
    },
    
    // 4. Dijkstra 最短路径算法 implementation
    dijkstra(startId, endId) {
      const distances = {} // 各节点到起点的距离
      const previous = {} // 前驱节点，用于回溯路径
      const pq = new PriorityQueue() // 优先队列
      
      // 初始化
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
        
        if (currentId === endId) break // 找到终点，提前退出
        if (distances[currentId] === Infinity) break // 剩下的不可达
        
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
      
      // 回溯路径
      const path = []
      let current = endId
      
      // 如果终点不可达
      if (distances[endId] === Infinity) {
        return { path: [], distance: 0 }
      }
      
      while (current !== null) {
        path.unshift(current)
        current = previous[current]
      }
      
      return { path, distance: distances[endId] }
    },
    
    // 5. 绘制路径
    drawPath(nodeIdList) {
      if (!this.ctx) return
      
      // 清空画布
      this.ctx.clearRect(0, 0, this.screenWidth, this.mapHeight)
      
      if (nodeIdList.length < 2) return
      
      const ratio = this.scaleRatio
      
      // 绘制路径线
      this.ctx.beginPath()
      this.ctx.setStrokeStyle('#FF3333') // 红色路径
      this.ctx.setLineWidth(4)
      this.ctx.setLineCap('round')
      this.ctx.setLineJoin('round')
      
      // 移动到起点
      const startNode = mapNodes[nodeIdList[0]]
      this.ctx.moveTo(startNode.x * ratio, startNode.y * ratio)
      
      // 连接后续节点
      for (let i = 1; i < nodeIdList.length; i++) {
        const node = mapNodes[nodeIdList[i]]
        this.ctx.lineTo(node.x * ratio, node.y * ratio)
      }
      this.ctx.stroke()
      
      // 绘制起点小圆点 (绿色)
      const start = mapNodes[nodeIdList[0]]
      this.drawPoint(start.x * ratio, start.y * ratio, '#09BB07')
      
      // 绘制终点小圆点 (红色)
      const end = mapNodes[nodeIdList[nodeIdList.length - 1]]
      this.drawPoint(end.x * ratio, end.y * ratio, '#FF3333')
      
      this.ctx.draw()
    },
    
    // 辅助绘制点
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

// 简单的优先队列实现
class PriorityQueue {
  constructor() {
    this.items = []
  }
  
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
    if (!added) {
      this.items.push(queueElement)
    }
  }
  
  dequeue() {
    return this.items.shift()
  }
  
  isEmpty() {
    return this.items.length === 0
  }
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

.picker-group {
  margin-bottom: 30rpx;
}

.picker-item {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  font-size: 28rpx;
}

.picker-item:last-child {
  margin-bottom: 0;
}

.label {
  width: 100rpx;
  color: #333;
  font-weight: bold;
}

.picker-view {
  flex: 1;
  height: 80rpx;
  background: #f5f7fb;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  color: #333;
}

.arrow {
  color: #999;
  font-size: 24rpx;
}

.nav-btn {
  background: #479fff;
  color: #ffffff;
  font-size: 32rpx;
  border-radius: 44rpx;
  height: 88rpx;
  line-height: 88rpx;
  margin-top: 20rpx;
}

.nav-btn:active {
  opacity: 0.9;
}

.map-wrapper {
  position: relative;
  width: 100%;
  margin-top: 20rpx;
  background: #fff;
}

.map-image {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

.nav-canvas {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  pointer-events: none; /* 让事件穿透到下一层，如果需要地图交互的话 */
}

.nav-tips {
  padding: 24rpx 30rpx;
  background: #e6f3ff;
  color: #479fff;
  font-size: 26rpx;
  text-align: center;
}
</style>
