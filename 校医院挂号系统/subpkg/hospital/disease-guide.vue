<template>
  <view class="disease-guide-page">
    <!-- 顶部搜索栏 -->
    <view class="search-header">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          placeholder="搜索症状或疾病..." 
          v-model="searchKeyword"
          @input="onSearch"
        />
      </view>
    </view>

    <!-- 疾病分类标签 -->
    <view class="category-tabs">
      <scroll-view class="tabs-scroll" scroll-x>
        <view 
          v-for="(category, index) in categories" 
          :key="index"
          class="tab-item"
          :class="{ active: currentCategory === category.name }"
          @click="selectCategory(category.name)"
        >
          <text class="tab-icon">{{ category.icon }}</text>
          <text class="tab-text">{{ category.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 疾病列表 -->
    <scroll-view class="disease-list" scroll-y>
      <view v-if="!searchKeyword">
        <!-- 按分类显示 -->
        <view v-for="(group, index) in filteredDiseases" :key="index" class="disease-group">
          <view class="group-header">
            <text class="group-icon">{{ group.icon }}</text>
            <text class="group-title">{{ group.title }}</text>
            <text class="group-count">{{ group.items.length }}种</text>
          </view>
          <view class="group-items">
            <view 
              v-for="(item, idx) in group.items" 
              :key="idx"
              class="disease-item"
              @click="selectDisease(item)"
            >
              <view class="item-left">
                <text class="item-name">{{ item.name }}</text>
                <text class="item-desc">{{ item.desc }}</text>
              </view>
              <view class="item-right">
                <text class="dept-tag">{{ item.dept }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 搜索结果 -->
      <view v-else class="search-results">
        <view v-if="searchResults.length > 0">
          <view class="result-header">
            <text>找到 {{ searchResults.length }} 个相关结果</text>
          </view>
          <view 
            v-for="(item, index) in searchResults" 
            :key="index"
            class="disease-item"
            @click="selectDisease(item)"
          >
            <view class="item-left">
              <text class="item-name">{{ item.name }}</text>
              <text class="item-desc">{{ item.desc }}</text>
            </view>
            <view class="item-right">
              <text class="dept-tag">{{ item.dept }}</text>
              <text class="arrow">›</text>
            </view>
          </view>
        </view>
        <view v-else class="empty-result">
          <text class="empty-icon">🔍</text>
          <text class="empty-text">未找到相关疾病</text>
          <text class="empty-tip">试试其他关键词或直接选择科室挂号</text>
        </view>
      </view>

      <!-- 温馨提示 -->
      <view class="tips-card">
        <view class="tips-header">
          <text class="tips-icon">💡</text>
          <text class="tips-title">温馨提示</text>
        </view>
        <view class="tips-content">
          <text class="tip-item">• 请根据主要症状选择就诊科室</text>
          <text class="tip-item">• 如不确定，可先咨询导诊台：010-51682525</text>
          <text class="tip-item">• 急重症患者请直接前往急诊科</text>
          <text class="tip-item">• 传染病患者请前往发热门诊</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 搜索关键词
const searchKeyword = ref('')

// 当前选中的分类
const currentCategory = ref('全部')

// 分类列表
const categories = ref([
  { name: '全部', icon: '🏥' },
  { name: '常见症状', icon: '😷' },
  { name: '呼吸系统', icon: '🫁' },
  { name: '消化系统', icon: '🫄' },
  { name: '心血管', icon: '❤️' },
  { name: '神经系统', icon: '🧠' },
  { name: '骨骼肌肉', icon: '🦴' },
  { name: '皮肤科', icon: '🩹' },
  { name: '眼耳鼻喉', icon: '👁️' },
  { name: '其他', icon: '➕' }
])

// 疾病症状数据
const diseaseData = ref([
  {
    category: '常见症状',
    icon: '😷',
    title: '常见症状',
    items: [
      { name: '发热、发烧', desc: '体温超过37.3℃', dept: '神经内科', deptId: 14, keywords: ['发热', '发烧', '高烧', '体温'] },
      { name: '头痛、头晕', desc: '头部疼痛或眩晕感', dept: '神经内科', deptId: 14, keywords: ['头痛', '头晕', '眩晕', '偏头痛'] },
      { name: '咳嗽、咳痰', desc: '持续咳嗽或有痰', dept: '呼吸内科', deptId: 11, keywords: ['咳嗽', '咳痰', '干咳', '咳血'] },
      { name: '腹痛、腹泻', desc: '腹部疼痛或腹泻', dept: '消化内科', deptId: 13, keywords: ['腹痛', '腹泻', '肚子疼', '拉肚子'] },
      { name: '胸痛、胸闷', desc: '胸部疼痛或闷胀', dept: '心内科', deptId: 12, keywords: ['胸痛', '胸闷', '心慌', '心悸'] },
      { name: '恶心、呕吐', desc: '恶心或呕吐症状', dept: '消化内科', deptId: 13, keywords: ['恶心', '呕吐', '想吐', '反胃'] },
      { name: '乏力、疲劳', desc: '全身无力、疲劳', dept: '内分泌科', deptId: 15, keywords: ['乏力', '疲劳', '无力', '困倦'] },
      { name: '失眠、多梦', desc: '睡眠质量差', dept: '神经内科', deptId: 14, keywords: ['失眠', '多梦', '睡不着', '睡眠障碍'] }
    ]
  },
  {
    category: '呼吸系统',
    icon: '🫁',
    title: '呼吸系统疾病',
    items: [
      { name: '感冒、流感', desc: '上呼吸道感染', dept: '呼吸内科', deptId: 11, keywords: ['感冒', '流感', '鼻塞', '流鼻涕'] },
      { name: '支气管炎', desc: '支气管黏膜炎症', dept: '呼吸内科', deptId: 11, keywords: ['支气管炎', '气管炎', '慢性咳嗽'] },
      { name: '肺炎', desc: '肺部感染性疾病', dept: '呼吸内科', deptId: 11, keywords: ['肺炎', '肺部感染', '呼吸困难'] },
      { name: '哮喘', desc: '慢性气道炎症', dept: '呼吸内科', deptId: 11, keywords: ['哮喘', '气喘', '喘息', '呼吸急促'] },
      { name: '咽喉炎', desc: '咽喉部炎症', dept: '口腔科', deptId: 51, keywords: ['咽喉炎', '嗓子疼', '咽痛', '喉咙痛'] },
      { name: '扁桃体炎', desc: '扁桃体发炎', dept: '口腔科', deptId: 51, keywords: ['扁桃体炎', '扁桃体发炎', '咽扁桃体'] }
    ]
  },
  {
    category: '消化系统',
    icon: '🫄',
    title: '消化系统疾病',
    items: [
      { name: '胃炎、胃溃疡', desc: '胃黏膜炎症或溃疡', dept: '消化内科', deptId: 13, keywords: ['胃炎', '胃溃疡', '胃痛', '胃疼'] },
      { name: '肠胃炎', desc: '急性胃肠道炎症', dept: '消化内科', deptId: 13, keywords: ['肠胃炎', '急性肠炎', '拉肚子'] },
      { name: '便秘', desc: '排便困难或次数减少', dept: '消化内科', deptId: 13, keywords: ['便秘', '大便困难', '排便不畅'] },
      { name: '痔疮', desc: '肛门直肠疾病', dept: '普通外科', deptId: 23, keywords: ['痔疮', '痔', '便血', '肛门疼痛'] },
      { name: '胆囊炎', desc: '胆囊炎症', dept: '普通外科', deptId: 23, keywords: ['胆囊炎', '右上腹痛', '胆结石'] },
      { name: '阑尾炎', desc: '阑尾发炎', dept: '普通外科', deptId: 23, keywords: ['阑尾炎', '右下腹痛', '转移性腹痛'] }
    ]
  },
  {
    category: '心血管',
    icon: '❤️',
    title: '心血管疾病',
    items: [
      { name: '高血压', desc: '血压持续升高', dept: '心内科', deptId: 11, keywords: ['高血压', '血压高', '头晕头痛'] },
      { name: '心律失常', desc: '心跳节律异常', dept: '心内科', deptId: 11, keywords: ['心律失常', '心跳快', '心跳慢', '心悸'] },
      { name: '冠心病', desc: '冠状动脉疾病', dept: '心内科', deptId: 11, keywords: ['冠心病', '心绞痛', '胸痛'] },
      { name: '心肌炎', desc: '心肌炎症', dept: '心内科', deptId: 11, keywords: ['心肌炎', '心慌', '胸闷'] },
      { name: '低血压', desc: '血压偏低', dept: '心内科', deptId: 11, keywords: ['低血压', '血压低', '头晕乏力'] }
    ]
  },
  {
    category: '神经系统',
    icon: '🧠',
    title: '神经系统疾病',
    items: [
      { name: '偏头痛', desc: '反复发作的头痛', dept: '神经内科', deptId: 14, keywords: ['偏头痛', '头痛', '太阳穴痛'] },
      { name: '神经衰弱', desc: '精神易兴奋易疲劳', dept: '神经内科', deptId: 14, keywords: ['神经衰弱', '失眠', '健忘'] },
      { name: '面神经麻痹', desc: '面部表情肌瘫痪', dept: '神经内科', deptId: 14, keywords: ['面瘫', '面神经麻痹', '口眼歪斜'] },
      { name: '焦虑症', desc: '焦虑情绪障碍', dept: '神经内科', deptId: 14, keywords: ['焦虑', '紧张', '心慌', '恐惧'] },
      { name: '抑郁症', desc: '情绪低落症候群', dept: '神经内科', deptId: 14, keywords: ['抑郁', '情绪低落', '兴趣减退'] }
    ]
  },
  {
    category: '骨骼肌肉',
    icon: '🦴',
    title: '骨骼肌肉疾病',
    items: [
      { name: '颈椎病', desc: '颈椎退行性病变', dept: '骨科', deptId: 21, keywords: ['颈椎病', '脖子疼', '颈部疼痛'] },
      { name: '腰椎间盘突出', desc: '腰椎间盘病变', dept: '骨科', deptId: 21, keywords: ['腰椎间盘突出', '腰痛', '腿麻'] },
      { name: '关节炎', desc: '关节炎症', dept: '骨科', deptId: 21, keywords: ['关节炎', '关节痛', '关节肿胀'] },
      { name: '肩周炎', desc: '肩关节周围炎', dept: '骨科', deptId: 21, keywords: ['肩周炎', '肩膀疼', '五十肩'] },
      { name: '骨折、扭伤', desc: '骨骼或软组织损伤', dept: '外伤处理', deptId: 24, keywords: ['骨折', '扭伤', '摔伤', '外伤'] },
      { name: '腱鞘炎', desc: '肌腱鞘炎症', dept: '骨科', deptId: 21, keywords: ['腱鞘炎', '鼠标手', '妈妈手'] }
    ]
  },
  {
    category: '皮肤科',
    icon: '🩹',
    title: '皮肤疾病',
    items: [
      { name: '湿疹、皮炎', desc: '皮肤炎症性疾病', dept: '皮肤科', deptId: 22, keywords: ['湿疹', '皮炎', '皮肤瘙痒', '红疹'] },
      { name: '荨麻疹', desc: '皮肤过敏反应', dept: '皮肤科', deptId: 22, keywords: ['荨麻疹', '风疹块', '过敏'] },
      { name: '痤疮、粉刺', desc: '毛囊皮脂腺炎症', dept: '皮肤科', deptId: 22, keywords: ['痤疮', '粉刺', '青春痘', '痘痘'] },
      { name: '真菌感染', desc: '皮肤真菌病', dept: '皮肤科', deptId: 22, keywords: ['真菌感染', '脚气', '灰指甲', '癣'] },
      { name: '带状疱疹', desc: '病毒性皮肤病', dept: '皮肤科', deptId: 22, keywords: ['带状疱疹', '蛇盘疮', '水泡'] }
    ]
  },
  {
    category: '眼耳鼻喉',
    icon: '👁️',
    title: '眼耳鼻喉疾病',
    items: [
      { name: '近视、远视', desc: '视力问题', dept: '口腔科', deptId: 51, keywords: ['近视', '远视', '视力下降', '看不清'] },
      { name: '结膜炎', desc: '结膜发炎', dept: '口腔科', deptId: 51, keywords: ['结膜炎', '红眼病', '眼睛红'] },
      { name: '中耳炎', desc: '中耳发炎', dept: '口腔科', deptId: 51, keywords: ['中耳炎', '耳朵疼', '流脓'] },
      { name: '鼻炎、鼻窦炎', desc: '鼻部炎症', dept: '口腔科', deptId: 51, keywords: ['鼻炎', '鼻窦炎', '鼻塞', '流鼻涕'] },
      { name: '耳鸣', desc: '耳内有声音', dept: '口腔科', deptId: 51, keywords: ['耳鸣', '耳朵响', '听力下降'] },
      { name: '口腔溃疡', desc: '口腔黏膜溃疡', dept: '口腔科', deptId: 52, keywords: ['口腔溃疡', '口腔溃疡', '嘴巴疼'] }
    ]
  },
  {
    category: '其他',
    icon: '➕',
    title: '其他常见问题',
    items: [
      { name: '糖尿病', desc: '血糖代谢异常', dept: '内分泌科', deptId: 15, keywords: ['糖尿病', '血糖高', '多饮多尿'] },
      { name: '甲状腺疾病', desc: '甲状腺功能异常', dept: '内分泌科', deptId: 15, keywords: ['甲状腺', '甲亢', '甲减'] },
      { name: '贫血', desc: '血红蛋白减少', dept: '心内科', deptId: 11, keywords: ['贫血', '头晕', '乏力', '面色苍白'] },
      { name: '过敏反应', desc: '免疫系统过度反应', dept: '皮肤科', deptId: 22, keywords: ['过敏', '过敏反应', '皮疹'] },
      { name: '疫苗接种', desc: '预防接种服务', dept: '预防保健科', deptId: 33, keywords: ['疫苗', '接种', '打针', '预防针'] },
      { name: '健康体检', desc: '定期健康检查', dept: '体检科', deptId: 43, keywords: ['体检', '健康检查', '身体检查'] }
    ]
  }
])

// 筛选后的疾病列表
const filteredDiseases = computed(() => {
  if (currentCategory.value === '全部') {
    return diseaseData.value
  }
  return diseaseData.value.filter(group => group.category === currentCategory.value)
})

// 搜索结果
const searchResults = computed(() => {
  if (!searchKeyword.value) return []
  
  const keyword = searchKeyword.value.toLowerCase()
  const results = []
  
  diseaseData.value.forEach(group => {
    group.items.forEach(item => {
      // 搜索名称、描述和关键词
      const matchName = item.name.toLowerCase().includes(keyword)
      const matchDesc = item.desc.toLowerCase().includes(keyword)
      const matchKeywords = item.keywords?.some(k => k.includes(keyword))
      
      if (matchName || matchDesc || matchKeywords) {
        results.push(item)
      }
    })
  })
  
  return results
})

// 选择分类
const selectCategory = (category) => {
  currentCategory.value = category
}

// 搜索
const onSearch = () => {
  // 搜索逻辑已在computed中处理
}

// 选择疾病
const selectDisease = (item) => {
  uni.showModal({
    title: item.name,
    content: `建议您挂号：${item.dept}\n\n${item.desc}\n\n是否前往该科室？`,
    confirmText: '去挂号',
    cancelText: '再看看',
    success: (res) => {
      if (res.confirm) {
        // 跳转到科室详情页
        uni.navigateTo({
          url: `/subpkg/hospital/department-detail?deptId=${item.deptId}`
        })
      }
    }
  })
}
</script>

<style scoped>
.disease-guide-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f7ff 0%, #ffffff 30%);
}

.search-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  padding: 24rpx 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.search-box {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 48rpx;
  padding: 20rpx 32rpx;
}

.search-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.category-tabs {
  background: #fff;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.tabs-scroll {
  white-space: nowrap;
  padding: 0 32rpx;
}

.tab-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 48rpx;
  padding: 16rpx 24rpx;
  border-radius: 16rpx;
  transition: all 0.3s;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.tab-icon {
  font-size: 48rpx;
  margin-bottom: 8rpx;
}

.tab-text {
  font-size: 24rpx;
  color: #666;
  white-space: nowrap;
}

.tab-item.active .tab-text {
  color: #fff;
  font-weight: bold;
}

.disease-list {
  height: calc(100vh - 280rpx);
  padding: 24rpx 32rpx;
}

.disease-group {
  margin-bottom: 48rpx;
}

.group-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  padding: 0 8rpx;
}

.group-icon {
  font-size: 48rpx;
  margin-right: 16rpx;
}

.group-title {
  flex: 1;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.group-count {
  font-size: 24rpx;
  color: #999;
}

.group-items {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.disease-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
  transition: all 0.2s;
  cursor: pointer;
}

.disease-item:last-child {
  border-bottom: none;
}

.disease-item:active {
  background: #f0f7ff;
  transform: scale(0.98);
}

.item-left {
  flex: 1;
  margin-right: 16rpx;
}

.item-name {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 8rpx;
}

.item-desc {
  display: block;
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
}

.item-right {
  display: flex;
  align-items: center;
}

.dept-tag {
  font-size: 24rpx;
  color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
  font-weight: 500;
  border: 1rpx solid rgba(102, 126, 234, 0.2);
}

.search-results {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
}

.result-header {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 24rpx;
  padding: 0 8rpx;
}

.empty-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
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
  margin-bottom: 16rpx;
}

.empty-tip {
  font-size: 24rpx;
  color: #999;
}

.tips-card {
  background: linear-gradient(135deg, #fef5e7 0%, #fff9e6 100%);
  border-radius: 16rpx;
  padding: 24rpx;
  margin-top: 32rpx;
  margin-bottom: 32rpx;
}

.tips-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.tips-icon {
  font-size: 36rpx;
  margin-right: 12rpx;
}

.tips-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #e67e22;
}

.tips-content {
  padding-left: 48rpx;
}

.tip-item {
  display: block;
  font-size: 24rpx;
  color: #8b5a2b;
  line-height: 40rpx;
  margin-bottom: 8rpx;
}
</style>

