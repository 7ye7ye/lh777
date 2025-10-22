<template>
  <view class="home-bg">
    <view class="home-header">
      <text class="title">北京交通大学校医院</text>
      <view class="header-icons">
        <view class="header-icon">⚙️</view>
      </view>
    </view>
    <view class="banner">
      <image src="/static/hospitalpicture.png" mode="aspectFill" style="width: 100%; height: 100%; border-radius: 12rpx;" />
    </view>
    <view class="visit-card card" @click="onVisitCardClick">
      <view class="visit-left">
        <view class="weather">周晴晴  女  20岁</view>
        <view class="ecard">电子就诊卡</view>
        <view class="visit-no">门诊号：M01078965</view>
      </view>
      <view class="visit-right">
        <view class="qrcode">📱</view>
        <view class="enter">出示就诊码</view>
      </view>
    </view>
    <view class="bind-tip card">
      <text class="plus">+</text>
      <text>首次使用，请绑定就诊人</text>
    </view>
    <view class="night-banner">
      <text>“ 晚间门诊 ” 专栏</text>
      <button class="night-btn" size="mini">点击进入</button>
    </view>
    <view class="quick card">
      <view class="quick-grid">
        <view class="quick-item" @click="onQuickItemClick('disease')">
          <view class="quick-icon">📝</view>
          <text>按疾病挂号</text>
        </view>
        <view class="quick-item" @click="onQuickItemClick('department')">
          <view class="quick-icon">🏥</view>
          <text>按科室挂号</text>
        </view>
        <view class="quick-item" @click="onQuickItemClick('report')">
          <view class="quick-icon">📊</view>
          <text>报告查询</text>
        </view>
        <view class="quick-item" @click="onQuickItemClick('internet')">
          <view class="quick-icon">🌐</view>
          <text>互联网诊疗</text>
        </view>
      </view>
    </view>
    <view class="home-tabs card">
      <view 
        v-for="(tab, idx) in tabs" 
        :key="tab" 
        class="tab" 
        :class="{ active: idx === activeIndex }"
        @click="activeIndex = idx"
      >{{ tab }}</view>
    </view>
    <view class="home-section card">
      <view class="home-grid">
        <view 
          v-for="item in currentItems" 
          :key="item.text" 
          class="home-item" 
          @click="onItemClick(item)"
        >
          <view class="icon">{{ item.icon }}</view>
          <text>{{ item.text }}</text>
        </view>
      </view>
    </view>
    <!-- 未登录内联提示 -->
    <LoginPrompt ref="loginPromptRef" mode="inline" message="登录后可出示电子就诊码" login-text="去登录" />
    <view class="tabbar-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import LoginPrompt from '@/components/LoginPrompt.vue'
import { AUTH_REQUIRED_FEATURES, createAuthHandler } from '@/utils/auth'

const tabs = ['门诊', '住院', '体检', '其他']
const activeIndex = ref(0)
const loginPromptRef = ref(null)

const itemsMap = {
  门诊: [
    { icon: '🌙', text: '晚间门诊' },
    { icon: '📅', text: '周末门诊' },
    { icon: '📋', text: '门诊签到' },
    { icon: '🧠', text: '心理筛查门诊' },
    { icon: '🗓️', text: '超声签到' },
    { icon: '🧾', text: '看结果K号' },
    { icon: '💴', text: '门诊缴费' },
    { icon: '🔎', text: '检查预约' },
    { icon: '🧾', text: '电子发票' },
    { icon: '📂', text: '电子票夹' },
    { icon: '🧭', text: '院内导航' },
    { icon: '📘', text: '门诊服务指南' },
    { icon: '📝', text: '预约记录' },
    { icon: '💬', text: '护理咨询' },
    { icon: '💳', text: '就诊卡余额退款' },
    { icon: '📚', text: '病史采集' },
    { icon: '🤖', text: '智能导诊' },
  ],
  住院: [
    { icon: '💳', text: '住院预交' },
    { icon: '🧾', text: '在院费用查询' },
    { icon: '🪪', text: '电子陪护证' },
    { icon: '📄', text: '病案复印' },
    { icon: '🧾', text: '住院发票清单' },
    { icon: '📘', text: '住院服务指南' },
    { icon: '🍱', text: '住院订餐' },
    { icon: '🧾', text: '订单清单' },
    { icon: '🍼', text: '出生证预约' },
    { icon: '🧠', text: '心理筛查住院' },
    { icon: '📊', text: '满意度调查' },
  ],
  体检: [
    { icon: '👤', text: '个检预约', url: '/pages/physical-exam/physical-exam' },
    { icon: '👥', text: '团检预约', url: '/subpkg/physical-exam/group-booking' },
    { icon: '🗂️', text: '体检报告', url: '/subpkg/physical-exam/exam-report' },
    { icon: '🧾', text: '体检订单', url: '/subpkg/physical-exam/exam-orders' },
    { icon: '🏥', text: '体检中心', url: '/subpkg/physical-exam/exam-center' },
  ],
  其他: [
    { icon: '📚', text: '健康百科' },
    { icon: '📣', text: '科普宣教' },
    { icon: '🆘', text: '帮助与反馈' },
    { icon: '💴', text: '价目公示' },
    { icon: '➕', text: '移动随访' },
    { icon: '🚑', text: '院前急救' },
    { icon: '💉', text: '惠民复诊' },
  ],
}

const currentItems = computed(() => itemsMap[tabs[activeIndex.value]] || [])

// 快捷入口点击处理
const onQuickItemClick = (type) => {
  switch (type) {
    case 'disease':
      // 按疾病挂号 - 跳转到疾病导航页面
      uni.navigateTo({ url: '/subpkg/hospital/disease-guide' })
      break
    case 'department':
      // 按科室挂号 - 跳转到科室预约页面
      uni.navigateTo({ url: '/subpkg/hospital/department-booking' })
      break
    case 'report':
      // 报告查询
      uni.showToast({ title: '报告查询功能开发中', icon: 'none' })
      break
    case 'internet':
      // 互联网诊疗
      uni.showToast({ title: '互联网诊疗功能开发中', icon: 'none' })
      break
  }
}

// 咨询问题内容
const consultContent = {
  before: {
    title: '体检前注意事项',
    content: `1. 体检前一天\n• 晚餐清淡，避免油腻食物\n• 晚上8点后禁食\n• 不要饮酒，保证充足睡眠\n\n2. 体检当天\n• 空腹（禁食8-12小时）\n• 可少量饮水\n• 穿宽松衣服\n• 携带有效证件\n\n3. 女性注意\n• 避开生理期\n• 怀孕或备孕请提前告知\n\n4. 慢性病患者\n• 高血压、糖尿病患者可少量饮水服药\n• 携带近期病历和处方`
  },
  report: {
    title: '体检报告解读',
    content: `1. 体检报告领取\n• 一般3-5个工作日\n• 可在线查看或现场领取\n\n2. 报告内容\n• 各项检查结果\n• 异常指标标注\n• 医生总结和建议\n\n3. 异常指标处理\n• 轻度异常：注意复查\n• 中度异常：门诊咨询\n• 重度异常：及时就医\n\n4. 免费服务\n• 报告解读咨询\n• 健康管理建议\n• 异常指标复查指导`
  },
  package: {
    title: '如何选择体检套餐',
    content: `1. 基础套餐（280元）\n适合：学生、青年教职工\n包含：15项常规检查\n\n2. 教职工套餐（480元）★推荐\n适合：在职教职工\n特色：学校报销、职业病筛查\n包含：25项全面检查\n\n3. 全面套餐（880元）\n适合：50岁以上、有基础疾病\n特色：深度筛查、跟踪服务\n包含：35项全面检查\n\n提示：\n• 学生体检免费\n• 教职工基础套餐学校报销\n• 老年人基础套餐免费`
  },
  booking: {
    title: '预约流程',
    content: `1. 在线预约（推荐）\n• 打开校医院挂号系统小程序\n• 选择体检科→选择体检类型\n• 选择日期和时间段\n• 填写个人信息并确认\n\n2. 电话预约\n• 拨打：010-51682525转体检科\n• 提供身份信息\n• 选择体检日期\n\n3. 现场预约\n• 前往体检中心1楼服务台\n• 出示有效证件\n• 填写预约表\n\n4. 集体预约\n• 新生：随录取通知书说明\n• 学生：学生处统一安排\n• 教职工：人事处统一组织`
  },
  time: {
    title: '体检时间安排',
    content: `1. 常规体检时间\n• 周一至周五 7:30-11:00\n• 周六 8:00-11:00（预约）\n• 采血时间：7:30-10:00\n\n2. 特殊时间安排\n• 新生入学体检：8月25日-9月5日\n  每日7:00-17:00\n• 学生年度体检：9-11月\n  集体：周一至周五 7:00-11:00\n  补检：周一、三、五 13:00-16:00\n• 教职工体检：3-6月\n  周一至周五 7:30-11:00\n\n3. 建议\n• 尽量选择工作日早晨\n• 避免月初、周一高峰\n• 提前预约可节省时间`
  },
  price: {
    title: '收费政策',
    content: `1. 免费项目\n• 学生常规体检（学校承担）\n• 新生入学体检（学校承担）\n• 教职工基础套餐（学校报销）\n• 老年人基础套餐（国家项目）\n\n2. 收费项目\n• 基础套餐：280元\n• 教职工套餐：480元（报销后0元）\n• 全面套餐：880元\n• 升级项目：按项目收费\n\n3. 优惠政策\n• 教职工家属：9折优惠\n• 校友：9.5折优惠\n• 团体预约（10人以上）：9折\n\n4. 支付方式\n• 微信/支付宝\n• 校园一卡通\n• 医保卡（部分项目）`
  }
}

// 显示咨询内容
const showConsultDialog = (question) => {
  const content = consultContent[question]
  if (content) {
    uni.showModal({
      title: content.title,
      content: content.content,
      showCancel: true,
      cancelText: '关闭',
      confirmText: '电话咨询',
      success: (res) => {
        if (res.confirm) {
          // 点击电话咨询，拨打电话
          uni.makePhoneCall({
            phoneNumber: '010-51682525'
          })
        }
      }
    })
  }
}

// 服务项点击处理
const onItemClick = (item) => {
  // 如果是咨询类型，显示咨询对话框
  if (item.type === 'consult') {
    showConsultDialog(item.question)
    return
  }
  
  // 如果有URL，跳转页面
  if (item.url) {
    uni.navigateTo({ 
      url: item.url,
      fail: (err) => {
        console.error('页面跳转失败:', err)
        uni.showToast({ title: '页面跳转失败', icon: 'none' })
      }
    })
  } else {
    uni.showToast({ title: `${item.text}功能开发中`, icon: 'none' })
  }
}

// 使用统一的权限控制（需要就诊卡）
const onVisitCardClick = createAuthHandler(
  AUTH_REQUIRED_FEATURES.HOME.VISIT_CARD,
  '/subpkg/profile/personal/mycard',
  { requireCard: true }
)
</script>

<style scoped>
.home-bg {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 120rpx;
}
.home-header {
  background: #3a9cff;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}
.title {
  color: #fff;
  font-size: 36rpx;
  font-weight: bold;
}
.header-icons {
  display: flex;
  align-items: center;
}
.header-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
}
.banner {
  width: 100%;
  height: 180rpx;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80rpx;
  background: #f0f0f0;
  border-radius: 12rpx;
}
.bind-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(90deg, #3a9cff 0%, #1de9b6 100%);
  color: #fff;
  font-size: 28rpx;
  border-radius: 16rpx;
  margin: 16rpx 24rpx 0 24rpx;
  padding: 24rpx 0;
  font-weight: bold;
}
.plus {
  font-size: 36rpx;
  margin-right: 16rpx;
}
.special-banner {
  width: 92%;
  margin: 24rpx 4% 0 4%;
  border-radius: 16rpx;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60rpx;
  background: #f0f0f0;
}
.visit-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 16rpx 24rpx 0 24rpx;
  padding: 16rpx 24rpx;
}
.visit-left .weather { font-size: 26rpx; color: #fff; }
.visit-left .ecard { margin-top: 8rpx; background: #fff; color: #3a9cff; display: inline-block; padding: 6rpx 12rpx; border-radius: 8rpx; font-size: 24rpx; }
.visit-left .visit-no { margin-top: 8rpx; color: #fff; font-size: 26rpx; }
.visit-right { display:flex; flex-direction: column; align-items: center; color:#fff; }
.visit-right .qrcode { font-size: 48rpx; }
.visit-right .enter { font-size: 22rpx; margin-top: 6rpx; }

.night-banner {
  margin: 16rpx 24rpx 0 24rpx;
  height: 120rpx;
  border-radius: 16rpx;
  background: linear-gradient(90deg, #6a00ff 0%, #8a2eff 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  color: #fff;
  font-weight: bold;
}
.night-btn { background: #fff; color: #6a00ff; border-radius: 999rpx; padding: 8rpx 16rpx; }

.quick .quick-grid { display: flex; }
.quick-item { width: 25%; display: flex; flex-direction: column; align-items: center; }
.quick-icon { width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; font-size: 32rpx; margin-bottom: 8rpx; }
.card {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx 24rpx 0 24rpx;
  padding: 24rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
}
.home-section {
  margin-top: 24rpx;
}
.home-row {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  margin: 0 0 16rpx 0;
}
.home-grid {
  display: flex;
  flex-wrap: wrap;
}
.home-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 16rpx 0;
}
.icon {
  width: 56rpx;
  height: 56rpx;
  margin-bottom: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}
.home-tabs {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx 24rpx 0 24rpx;
  overflow: hidden;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #888;
}
.tab.active {
  color: #3a9cff;
  font-weight: bold;
  border-bottom: 4rpx solid #3a9cff;
  background: #f0f8ff;
}
.tabbar-placeholder {
  height: 120rpx;
}
</style>
