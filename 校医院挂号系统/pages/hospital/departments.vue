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
      <view 
        v-for="(firstLevel, index) in departmentTree" 
        :key="index" 
        class="first-level"
      >
        <!-- 一级科室标题 -->
        <view class="first-level-title" @click="toggleFirstLevel(index)">
          <text class="dept-name">{{ firstLevel.deptName }}</text>
          <image 
            src="/static/icons/arrow-down.png" 
            mode="widthFix" 
            class="arrow-icon"
            :class="{ 'rotate': firstLevel.expanded }"
          ></image>
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
    
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDepartmentTree, searchDepartments } from '../../api/department'

const keyword = ref('')
const departmentTree = ref([])
const originalTree = ref([]) // 用于搜索备份

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
    departmentTree.value = [...originalTree.value]
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
      // 简单处理搜索结果（实际项目需按树形结构重组）
      departmentTree.value = data.map(item => ({
        deptId: item.deptId,
        deptName: item.deptName,
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
    url: `/pages/hospital/department-detail?deptId=${dept.deptId}`
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
  width: 36rpx;
  height: 36rpx;
  transition: transform 0.3s;
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
</style>


