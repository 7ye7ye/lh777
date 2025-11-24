<template>
  <view class="search-page">
    <view class="search-row">
      <view class="search-box">
        <input
          class="search-input"
          type="text"
          v-model="keyword"
          placeholder="请输入目的地或服务设施"
          confirm-type="search"
          @confirm="handleSearch"
        />
        <view class="voice-btn">
          <image class="voice-icon" src="/static/inhos_navi/voice.svg" mode="aspectFit" />
        </view>
      </view>
      <view class="search-btn" @tap="handleSearch">搜索</view>
    </view>

    <view v-if="!hasSearched" class="recent-section">
      <view class="section-title">
        <text>最近搜索</text>
        <view class="clear-btn" @tap="clearHistory">
          <text class="clear-icon">🗑</text>
        </view>
      </view>
      <view v-if="searchHistory.length" class="history-list">
        <view
          class="history-item"
          v-for="item in searchHistory"
          :key="item"
          @tap="applyHistory(item)"
        >
          {{ item }}
        </view>
      </view>
      <view v-else class="empty-history">
        暂无搜索记录
      </view>
    </view>

    <view v-else class="result-section">
      <view class="result-title">导航指南</view>
      <view v-if="guideResult" class="guide-card">
        <view class="guide-header">{{ guideResult.title }}路线</view>
        <view class="guide-step" v-for="(step, index) in guideResult.steps" :key="index">
          第{{ index + 1 }}步：{{ step }}
        </view>
        <view v-if="guideResult.note" class="guide-note">
          注意：{{ guideResult.note }}
        </view>
      </view>
      <view v-else class="empty-result">
        没有查到相关路线，请尝试其他关键词
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const HISTORY_KEY = 'NAVIGATION_SEARCH_HISTORY'

const keyword = ref('')
const hasSearched = ref(false)
const searchHistory = ref([])
const guideResult = ref(null)

const navigationGuides = [
  {
    title: '口腔科',
    keywords: ['口腔科', '牙科'],
    steps: [
      '在一层挂口腔科号并完成缴费',
      '前往电梯或扶梯，上至二层 (2F)',
      '沿指示牌步行至【口腔科】候诊区'
    ],
    note: '口腔治疗耗时较长，建议提前取号并耐心等待叫号'
  },
  {
    title: '外科',
    keywords: ['外科', '普外'],
    steps: [
      '进入门诊楼后前往一层挂号收费室取外科号',
      '保持在一层 (1F)，经过换药室左转',
      '抵达【外科】诊室，向护士站报到'
    ],
    note: '如需换药或拆线，可直接在外科旁换药室处理'
  },
  {
    title: '西药房',
    keywords: ['西药房', '药房', '取药'],
    steps: [
      '完成就诊后在自助机或收费窗口缴费',
      '返回一层大厅，沿指示牌找到收费处右侧通道',
      '排队至【西药房】窗口取药并核对药品信息'
    ],
    note: '请核对姓名与药品用法，离开前检查药品数量是否正确'
  },
  {
    title: '妇科',
    keywords: ['妇科', '孕期保健'],
    steps: [
      '先在一层挂号收费室办理妇科挂号',
      '乘电梯直达四层 (4F)，右侧即为妇科区域',
      '按指示找到【妇科/孕期保健室】候诊区排队'
    ],
    note: '孕期检查请提前准备母子健康档案及既往检验结果'
  },
  {
    title: '中医理疗科',
    keywords: ['中医理疗', '理疗', '中医'],
    steps: [
      '在一层挂号窗口选择“中医理疗科”并缴费',
      '乘电梯或楼梯上至三层 (3F)',
      '沿导诊指引前往会议室旁的【中医理疗科】办理治疗'
    ],
    note: '部分理疗项目需提前预约，请与护士站确认排期'
  },
  {
    title: '服务台',
    keywords: ['服务台', '导诊台'],
    steps: [
      '进入门诊大厅后直行至中央区域',
      '在左侧可看到醒目的【服务台】标识',
      '向导诊护士咨询科室位置或排队情况'
    ],
    note: '服务台可提供打印导诊单、咨询分诊等基础服务'
  },
  {
    title: '收费处',
    keywords: ['收费处', '缴费', '付款'],
    steps: [
      '在门诊楼一层找到挂号收费区',
      '根据叫号信息排队到【收费处】窗口办理缴费',
      '完成缴费后妥善保存票据或凭条'
    ],
    note: '高峰时段建议使用自助机或微信缴费，减少排队时间'
  },
  {
    title: '挂号收费室',
    keywords: ['挂号', '挂号收费室'],
    steps: [
      '门诊大厅一层右侧即是“挂号收费室”',
      '按屏幕指引选择科室并支付挂号费',
      '领取挂号回执，以便在对应科室候诊'
    ],
    note: '如已在小程序预约，可直接扫码取号，省去排队时间'
  },
  {
    title: '取药窗口',
    keywords: ['取药', '西药房', '药房'],
    steps: [
      '完成就诊和缴费后，按指示前往一层西药房',
      '凭处方单或取药码排队等待叫号',
      '到【取药窗口】核对药品信息并领取药品'
    ],
    note: '请务必核对药品名称、剂量与使用方法'
  },
  {
    title: '电梯间',
    keywords: ['电梯', '上楼', '扶梯'],
    steps: [
      '在门诊大厅中部即可看到电梯组团',
      '根据指示牌选择对应楼层，按下目的楼层按钮',
      '到达后顺着导向系统快速抵达科室'
    ],
    note: '高峰期请排队候梯，可选择旁边楼梯或扶梯分流'
  },
  {
    title: '卫生间',
    keywords: ['卫生间', '洗手间', '厕所'],
    steps: [
      '每层科室区域两端都设有卫生间指示牌',
      '按指引前往最近的【卫生间】，保持通道畅通',
      '如需无障碍卫生间，可在服务台咨询位置'
    ],
    note: '请注意随身物品，保持卫生'
  }
]

const handleSearch = () => {
  const term = keyword.value.trim()
  if (!term) {
    uni.showToast({ title: '请输入搜索内容', icon: 'none' })
    return
  }
  hasSearched.value = true
  const lowerTerm = term.toLowerCase()
  guideResult.value = navigationGuides.find(guide =>
    guide.keywords.some(k => lowerTerm.includes(k.toLowerCase()) || k.toLowerCase().includes(lowerTerm))
  ) || null
  updateHistory(term)
}

const updateHistory = (term) => {
  const list = searchHistory.value.filter(item => item !== term)
  list.unshift(term)
  searchHistory.value = list.slice(0, 8)
  uni.setStorageSync(HISTORY_KEY, searchHistory.value)
}

const clearHistory = () => {
  searchHistory.value = []
  uni.removeStorageSync(HISTORY_KEY)
}

const applyHistory = (term) => {
  keyword.value = term
  handleSearch()
}

onLoad((options = {}) => {
  const history = uni.getStorageSync(HISTORY_KEY)
  if (Array.isArray(history)) {
    searchHistory.value = history
    if (history.length) {
      keyword.value = history[0]
    }
  }
  if (options.keyword) {
    keyword.value = decodeURIComponent(options.keyword)
    handleSearch()
  }
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: #ffffff;
}

.search-row {
  display: flex;
  align-items: center;
  padding: 12rpx 24rpx 16rpx;
  background: #479fff;
  gap: 16rpx;
}

.search-box {
  flex: 1;
  background: #ffffff;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  padding: 0 16rpx;
}

.search-input {
  flex: 1;
  height: 64rpx;
  font-size: 26rpx;
  padding: 0 12rpx;
}

.voice-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-left: 12rpx;
}

.voice-icon {
  width: 36rpx;
  height: 36rpx;
}

.search-btn {
  color: #fff;
  font-size: 28rpx;
  padding-right: 8rpx;
}

.recent-section,
.result-section {
  padding: 32rpx;
}

.section-title {
  display: flex;
  justify-content: space-between;
  font-size: 28rpx;
  color: #333;
}

.clear-btn {
  font-size: 24rpx;
  color: #666;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 24rpx;
}

.history-item {
  padding: 12rpx 24rpx;
  background: #f5f7fb;
  border-radius: 999rpx;
  font-size: 24rpx;
  color: #333;
}

.empty-history,
.empty-result {
  margin-top: 40rpx;
  text-align: center;
  color: #999;
  font-size: 26rpx;
}

.result-title {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
}

.guide-card {
  padding: 32rpx;
  border-radius: 20rpx;
  background: #f8f9fd;
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.04);
}

.guide-header {
  font-size: 30rpx;
  color: #2f4c80;
  font-weight: 600;
  margin-bottom: 20rpx;
}

.guide-step {
  font-size: 26rpx;
  color: #333;
  margin-bottom: 16rpx;
  line-height: 1.5;
}

.guide-note {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #d9534f;
  background: #fff0f0;
  padding: 16rpx;
  border-radius: 12rpx;
}
</style>

