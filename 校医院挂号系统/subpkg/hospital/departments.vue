<template>
  <view class="dept-bg">
    <!-- 搜索框 -->
    <view class="search-bar">
      <input 
        type="text" 
        placeholder="搜索科室..." 
        v-model="keyword"
        @input="handleSearch"
        class="search-input"
      />
    </view>

    <!-- 科室树形列表 -->
    <view class="department-tree">
      <!-- 搜索结果模式 -->
      <view v-if="isSearchMode" class="search-results">
        <view 
          v-for="(item, index) in departmentTree" 
          :key="index" 
          class="search-result-item"
          @click="navigateToDetail(item)"
        >
          <view class="dot"></view>
          <view class="dept-info">
            <text class="dept-name">{{ item.deptName }}</text>
            <text class="dept-desc">{{ item.deptDesc || '暂无介绍' }}</text>
            <text class="location" v-if="item.location">
              位置：{{ item.location }}
            </text>
          </view>
        </view>
      </view>
      
      <!-- 正常树形结构模式 -->
      <view v-else>
        <view 
          v-for="(firstLevel, index) in departmentTree" 
          :key="index" 
          class="first-level"
        >
          <!-- 一级科室标题 -->
          <view class="first-level-title" @click="toggleFirstLevel(index)">
            <text class="dept-name">{{ firstLevel.deptName }}</text>
            <text 
              class="arrow-icon"
              :class="{ 'rotate': firstLevel.expanded }"
            >▼</text>
          </view>

          <!-- 二级科室列表 -->
          <view class="second-level-list" v-if="firstLevel.expanded">
            <view 
              v-for="(secondLevel, sIndex) in firstLevel.children" 
              :key="sIndex" 
              class="second-level-item"
              @click="navigateToDetail(secondLevel)"
            >
              <view class="dot"></view>
              <view class="dept-info">
                <text class="dept-name">{{ secondLevel.deptName }}</text>
                <text class="dept-desc">{{ secondLevel.deptDesc || '暂无介绍' }}</text>
                <text class="location" v-if="secondLevel.location">
                  位置：{{ secondLevel.location }}
                </text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDepartmentTree, searchDepartments } from '../../api/department'

const keyword = ref('')
const departmentTree = ref([])
const originalTree = ref([]) // 用于搜索备份
const isSearchMode = ref(false) // 是否为搜索模式

// 加载科室树形结构
const loadDepartmentTree = async () => {
  try {
    const res = await getDepartmentTree()
    console.log('科室树数据:', res)
    
    // 处理不同的响应格式
    let data = res
    if (res && res.data) {
      data = res.data
    } else if (res && res.result) {
      data = res.result
    }
    
    if (data && Array.isArray(data)) {
      departmentTree.value = data.map(item => ({
        ...item,
        expanded: false // 默认折叠
      }))
      originalTree.value = [...departmentTree.value]
    } else {
      console.warn('科室树数据格式异常:', res)
      uni.showToast({
        title: '数据格式异常',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('加载科室树失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

// 切换一级科室展开/折叠
const toggleFirstLevel = (index) => {
  departmentTree.value[index].expanded = !departmentTree.value[index].expanded
}

// 搜索科室
const handleSearch = async () => {
  if (!keyword.value.trim()) {
    // 清空搜索，恢复原始树形结构
    departmentTree.value = [...originalTree.value]
    isSearchMode.value = false
    return
  }
  
  try {
    const res = await searchDepartments(keyword.value)
    console.log('搜索结果:', res)
    
    // 处理不同的响应格式
    let data = res
    if (res && res.data) {
      data = res.data
    } else if (res && res.result) {
      data = res.result
    }
    
    if (data && Array.isArray(data)) {
      // 进入搜索模式，显示扁平化的搜索结果
      isSearchMode.value = true
      departmentTree.value = data.map(item => ({
        deptId: item.deptId,
        deptName: item.deptName,
        deptDesc: item.deptDesc,
        location: item.location,
        expanded: true,
        children: []
      }))
    } else {
      console.warn('搜索结果格式异常:', res)
      uni.showToast({
        title: '搜索数据格式异常',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({
      title: '搜索失败',
      icon: 'none'
    })
  }
}

// 跳转到科室详情
const navigateToDetail = (dept) => {
  uni.navigateTo({
    url: `/subpkg/hospital/department-detail?deptId=${dept.deptId}`
  })
}

onMounted(() => {
  loadDepartmentTree()
})
</script>

<style scoped>
.dept-bg {
  background: #f8faff;
  min-height: 100vh;
  padding: 16rpx 0 120rpx 0;
}

.search-bar {
  background: #fff;
  padding: 16rpx;
  border-radius: 8rpx;
  margin: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}

.search-input {
  height: 60rpx;
  background-color: #f0f0f0;
  padding: 0 20rpx;
  border-radius: 30rpx;
  font-size: 28rpx;
  width: 100%;
  box-sizing: border-box;
}

.department-tree {
  background: #fff;
  border-radius: 16rpx;
  margin: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
  overflow: hidden;
}

.first-level {
  border-bottom: 1rpx solid #eee;
}

.first-level:last-child {
  border-bottom: none;
}

.first-level-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 24rpx;
  font-size: 32rpx;
  font-weight: bold;
  background: #f8faff;
}

.dept-name {
  color: #222;
}

.arrow-icon {
  font-size: 24rpx;
  color: #999;
  transition: transform 0.3s;
  display: inline-block;
}

.rotate {
  transform: rotate(180deg);
}

.second-level-list {
  padding-left: 32rpx;
  background-color: #fafafa;
}

.second-level-item {
  display: flex;
  align-items: flex-start;
  padding: 20rpx 16rpx;
  border-bottom: 1rpx solid #eee;
}

.second-level-item:last-child {
  border-bottom: none;
}

.dot {
  width: 8rpx;
  height: 8rpx;
  background: #d0021b;
  border-radius: 50%;
  margin-right: 16rpx;
  margin-top: 12rpx;
}

.dept-info {
  flex: 1;
}

.dept-info .dept-name {
  display: block;
  font-size: 30rpx;
  color: #222;
  margin-bottom: 8rpx;
}

.dept-desc {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
  line-height: 1.4;
}

.location {
  font-size: 24rpx;
  color: #999;
}

.tabbar-placeholder { 
  height: 120rpx; 
}

/* 搜索结果样式 */
.search-results {
  padding: 0;
}

.search-result-item {
  display: flex;
  align-items: flex-start;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #eee;
  background: #fff;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:active {
  background-color: #f5f5f5;
}
</style>


