<template>
  <view class="page-bg">
    <!-- 顶部横幅 -->
    <view class="header-banner">
      <view class="banner-title">校医院体检套餐</view>
      <view class="banner-subtitle">专注师生健康 · 校园特色服务 · 优惠价格</view>
    </view>

    <!-- 套餐列表 -->
    <view class="packages-container">
      <view 
        v-for="(pkg, index) in packages" 
        :key="pkg.id" 
        class="package-card"
        :class="{ 'recommended': pkg.recommended }"
        @click="selectPackage(pkg)"
      >
        <!-- 推荐标签 -->
        <view class="recommend-tag" v-if="pkg.recommended">
          <text>推荐</text>
        </view>

        <!-- 套餐头部 -->
        <view class="package-header">
          <view class="package-name">{{ pkg.name }}</view>
          <view class="package-price">
            <text class="price-symbol">¥</text>
            <text class="price-value">{{ pkg.price }}</text>
          </view>
          <view class="package-subtitle">{{ pkg.subtitle }}</view>
        </view>

        <!-- 套餐内容 -->
        <view class="package-content">
          <view class="content-title">
            <text class="icon">📋</text>
            <text>检查项目（{{ pkg.items.length }}项）</text>
          </view>
          <view class="items-list">
            <view 
              v-for="(item, idx) in pkg.items" 
              :key="idx" 
              class="item"
            >
              <text class="dot">•</text>
              <text>{{ item }}</text>
            </view>
          </view>
        </view>

        <!-- 适用人群 -->
        <view class="suitable-for">
          <text class="label">适用人群：</text>
          <text class="value">{{ pkg.suitableFor }}</text>
        </view>

        <!-- 预约按钮 -->
        <view class="package-footer">
          <button class="reserve-btn" :class="{ 'recommend-btn': pkg.recommended }">
            立即预约
          </button>
        </view>
      </view>
    </view>

    <!-- 体检须知 -->
    <view class="notice-card">
      <view class="notice-title">
        <text class="icon">📢</text>
        <text>体检须知</text>
      </view>
      <view class="notice-content">
        <view class="notice-item">
          <text class="num">1.</text>
          <text>体检当日需空腹（禁食8-12小时），可少量饮水</text>
        </view>
        <view class="notice-item">
          <text class="num">2.</text>
          <text>请携带身份证或学生证、工作证</text>
        </view>
        <view class="notice-item">
          <text class="num">3.</text>
          <text>体检时间：周一至周六 7:30-11:00（采血时间：7:30-10:00）</text>
        </view>
        <view class="notice-item">
          <text class="num">4.</text>
          <text>体检报告一般3-5个工作日可取，可在线查询</text>
        </view>
        <view class="notice-item">
          <text class="num">5.</text>
          <text>如有特殊情况请提前联系：010-51682525转体检科</text>
        </view>
      </view>
    </view>

    <!-- 联系咨询 -->
    <view class="contact-card">
      <view class="contact-item">
        <text class="icon">📞</text>
        <text class="label">咨询电话：</text>
        <text class="value">010-51682525转体检科</text>
      </view>
      <view class="contact-item">
        <text class="icon">📍</text>
        <text class="label">体检地址：</text>
        <text class="value">体检中心1-2楼</text>
      </view>
    </view>

    <view class="bottom-placeholder"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

// 体检套餐数据
const packages = ref([
  {
    id: 1,
    name: '基础套餐',
    price: 280,
    subtitle: '含15项常规检查',
    recommended: false,
    suitableFor: '学生、青年教职工、家属',
    items: [
      '一般检查（身高、体重、血压、BMI）',
      '内科检查（心肺听诊、腹部触诊）',
      '外科检查（淋巴结、甲状腺、脊柱四肢）',
      '眼科检查（视力、辨色力、眼底）',
      '耳鼻喉科检查',
      '血常规（18项）',
      '尿常规（11项）',
      '肝功能（ALT、AST）',
      '肾功能（尿素氮、肌酐）',
      '空腹血糖',
      '血脂两项（总胆固醇、甘油三酯）',
      '心电图',
      '胸部X光',
      '腹部B超（肝胆胰脾）',
      '总检报告及健康指导'
    ]
  },
  {
    id: 2,
    name: '教职工套餐',
    price: 480,
    subtitle: '含25项检查项目',
    recommended: true,
    suitableFor: '在职教职工、中年人群（学校报销基础费用）',
    items: [
      '基础套餐全部项目',
      '肝功能全套（ALT、AST、GGT、ALP、总蛋白等8项）',
      '肾功能全套（尿素氮、肌酐、尿酸）',
      '血脂四项（总胆固醇、甘油三酯、高密度脂蛋白、低密度脂蛋白）',
      '糖化血红蛋白',
      '甲状腺功能三项（TSH、FT3、FT4）',
      '肿瘤标志物筛查（AFP、CEA）',
      '幽门螺杆菌检测（C13呼气试验）',
      '颈椎X光（教师职业病筛查）',
      '心脏彩超',
      '心理压力评估',
      '总检报告及职业健康指导'
    ]
  },
  {
    id: 3,
    name: '全面套餐',
    price: 880,
    subtitle: '含35项全面检查',
    recommended: false,
    suitableFor: '50岁以上教职工、退休教职工、有基础疾病者',
    items: [
      '教职工套餐全部项目',
      '肿瘤标志物全套（AFP、CEA、CA199、CA125、PSA等）',
      '风湿免疫检查（类风湿因子、抗O、C反应蛋白）',
      '甲状腺功能全套（TSH、FT3、FT4、甲状腺抗体）',
      '骨密度检测',
      '肺功能检测',
      '胸部CT（低剂量螺旋CT）',
      '全腹部彩超（肝胆胰脾肾输尿管膀胱）',
      '甲状腺彩超',
      '颈动脉彩超',
      '乳腺彩超（女性）/前列腺彩超（男性）',
      '认知功能评估（老年人）',
      '营养状况评估',
      '慢病风险评估',
      '个性化健康管理方案',
      '健康管理师一年跟踪服务'
    ]
  }
])

// 选择套餐
const selectPackage = (pkg) => {
  uni.showModal({
    title: '预约确认',
    content: `您选择的是${pkg.name}（¥${pkg.price}），是否立即预约？`,
    confirmText: '立即预约',
    cancelText: '再看看',
    success: (res) => {
      if (res.confirm) {
        // 跳转到预约页面（可以携带套餐ID）
        uni.navigateTo({
          url: `/subpkg/hospital/department-detail?deptId=43&packageId=${pkg.id}`
        })
      }
    }
  })
}
</script>

<style scoped>
.page-bg {
  background: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 40rpx;
}

/* 顶部横幅 */
.header-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx 32rpx 40rpx;
  color: #fff;
  text-align: center;
}

.banner-title {
  font-size: 40rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
}

.banner-subtitle {
  font-size: 26rpx;
  opacity: 0.9;
}

/* 套餐容器 */
.packages-container {
  padding: 32rpx 24rpx;
}

/* 套餐卡片 */
.package-card {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 32rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.08);
  position: relative;
  transition: all 0.3s;
}

.package-card.recommended {
  border: 3rpx solid #667eea;
  box-shadow: 0 12rpx 32rpx rgba(102, 126, 234, 0.15);
}

.package-card:active {
  transform: scale(0.98);
}

/* 推荐标签 */
.recommend-tag {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
  padding: 8rpx 24rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: bold;
  z-index: 1;
}

/* 套餐头部 */
.package-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 32rpx 32rpx;
  color: #fff;
  text-align: center;
}

.package-card.recommended .package-header {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.package-name {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
}

.package-price {
  margin-bottom: 12rpx;
}

.price-symbol {
  font-size: 32rpx;
  font-weight: bold;
}

.price-value {
  font-size: 56rpx;
  font-weight: bold;
}

.package-subtitle {
  font-size: 26rpx;
  opacity: 0.9;
}

/* 套餐内容 */
.package-content {
  padding: 32rpx;
}

.content-title {
  display: flex;
  align-items: center;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
}

.content-title .icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.items-list {
  background: #f8f9fc;
  border-radius: 16rpx;
  padding: 24rpx;
}

.item {
  display: flex;
  align-items: flex-start;
  font-size: 26rpx;
  color: #666;
  line-height: 40rpx;
  margin-bottom: 12rpx;
}

.item:last-child {
  margin-bottom: 0;
}

.item .dot {
  color: #667eea;
  margin-right: 12rpx;
  font-weight: bold;
}

/* 适用人群 */
.suitable-for {
  padding: 0 32rpx 24rpx;
  font-size: 26rpx;
  color: #666;
}

.suitable-for .label {
  color: #999;
}

.suitable-for .value {
  color: #667eea;
  font-weight: bold;
}

/* 套餐底部 */
.package-footer {
  padding: 0 32rpx 32rpx;
}

.reserve-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reserve-btn.recommend-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.reserve-btn::after {
  border: none;
}

/* 体检须知 */
.notice-card {
  background: #fff;
  border-radius: 24rpx;
  margin: 0 24rpx 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.notice-title {
  display: flex;
  align-items: center;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
}

.notice-title .icon {
  font-size: 36rpx;
  margin-right: 12rpx;
}

.notice-content {
  background: #fffbf0;
  border-radius: 16rpx;
  padding: 24rpx;
  border-left: 6rpx solid #faad14;
}

.notice-item {
  display: flex;
  font-size: 26rpx;
  color: #666;
  line-height: 40rpx;
  margin-bottom: 16rpx;
}

.notice-item:last-child {
  margin-bottom: 0;
}

.notice-item .num {
  color: #faad14;
  font-weight: bold;
  margin-right: 12rpx;
}

/* 联系咨询 */
.contact-card {
  background: #fff;
  border-radius: 24rpx;
  margin: 0 24rpx 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.contact-item {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 20rpx;
}

.contact-item:last-child {
  margin-bottom: 0;
}

.contact-item .icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.contact-item .label {
  color: #999;
}

.contact-item .value {
  color: #333;
  font-weight: bold;
}

.bottom-placeholder {
  height: 40rpx;
}
</style>

