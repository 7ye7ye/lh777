<template>
  <PageWrapper>
    <!-- 头部：标题 + 筛选栏，放在原 PageWrapper 标题区域 -->
    <template #headerContent>
      <div class="page-header">
        <div class="header-left">
          <div class="page-title">数据大屏</div>
          <p class="subtitle">医院运营概览 · 挂号 / 就诊 / 号源 / 收入</p>
        </div>
        <div class="header-filters">
          <a-space :size="12" wrap>
            <a-radio-group v-model="timeType" size="middle">
              <a-radio-button value="today">今日</a-radio-button>
              <a-radio-button value="week">本周</a-radio-button>
              <a-radio-button value="month">本月</a-radio-button>
            </a-radio-group>

            <a-range-picker v-model="dateRange" />

            <a-select
              v-model="dept"
              placeholder="选择科室"
              style="width: 160px"
              :options="deptOptions"
              allow-clear
            />

            <a-select
              v-model="doctor"
              placeholder="选择医生"
              style="width: 160px"
              :options="doctorOptions"
              allow-clear
            />

            <a-button type="primary" @click="handleQuery" :loading="loading">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </div>
      </div>
              </template>

    <div class="data-screen">
      <!-- 一、顶部 KPI 区 -->
      <a-row :gutter="16" class="kpi-row">
        <a-col :xs="24" :sm="12" :md="6" v-for="item in kpiCards" :key="item.key">
          <a-card :bordered="false" class="kpi-card">
            <div class="kpi-title">{{ item.title }}</div>
            <div class="kpi-value" :class="{ 'loading-value': loading }">
              <a-spin v-if="loading" size="small" />
              <span v-else>{{ item.value }}</span>
            </div>
            <div class="kpi-desc">{{ item.desc }}</div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 二、主体图表区：两列布局 -->
      <a-row :gutter="16" class="main-row">
        <!-- 左侧 -->
        <a-col :xs="24" :lg="16">
          <a-card title="就诊量趋势（按日 / 周 / 月）" :bordered="false" class="block-card">
            <div class="chart-placeholder">
              <div ref="outpatientChartRef" class="chart-box"></div>
            </div>
          </a-card>

          <a-card title="就诊时段分布" :bordered="false" class="block-card">
            <div class="chart-placeholder">
              柱状图 / 热力图占位：早 / 中 / 晚高峰时段就诊分布
            </div>
          </a-card>

          <a-card title="收入趋势" :bordered="false" class="block-card">
            <div class="chart-placeholder">
              折线图占位：挂号收入、检查收入、体检收入等按时间变化
            </div>
          </a-card>
        </a-col>

        <!-- 右侧 -->
        <a-col :xs="24" :lg="8">
          <a-card title="科室负荷统计" :bordered="false" class="block-card">
            <div class="chart-placeholder">
              <div ref="deptLoadChartRef" class="chart-box"></div>
            </div>
          </a-card>

          <a-card title="医生工作量 Top N" :bordered="false" class="block-card">
            <div class="chart-placeholder">
              条形图占位：就诊人次最多的医生排名
            </div>
          </a-card>

          <a-card title="系统运行与消息发送情况" :bordered="false" class="block-card">
            <ul class="status-list">
              <li>关键接口成功率：——</li>
              <li>今日系统错误数：——</li>
              <li>预约 / 就诊提醒发送成功率：——</li>
            </ul>
          </a-card>
        </a-col>
      </a-row>

      <!-- 三、底部明细表格区 -->
      <a-card title="科室 / 医生明细列表" :bordered="false" class="block-card bottom-table-card">
        <a-tabs v-model="activeTab">
          <a-tab-pane key="dept" tab="按科室统计">
            <div class="table-placeholder">
              表格占位：科室、挂号数、就诊数、爽约数、收入等明细
            </div>
          </a-tab-pane>
          <a-tab-pane key="doctor" tab="按医生统计">
            <div class="table-placeholder">
              表格占位：医生、科室、就诊人次、平均就诊时长等明细
            </div>
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import {
  getStatisticsSummary,
  getRegistrationStatistics,
  getDepartmentLoadStatistics,
  type StatisticsQuery,
  type RegistrationStatisticsItem,
  type DepartmentLoadItem,
} from '/@/api/hospital/statistics';
import * as echarts from 'echarts';

type TimeType = 'today' | 'week' | 'month';
type TabKey = 'dept' | 'doctor';

const loading = ref(false);
const timeType = ref<TimeType>('today');
const dateRange = ref<[Dayjs, Dayjs] | null>(null);
const dept = ref<string | undefined>();
const doctor = ref<string | undefined>();
const activeTab = ref<TabKey>('dept');
const outpatientChartRef = ref<HTMLDivElement>();
let outpatientChart: echarts.ECharts | null = null;
const registrationData = ref<RegistrationStatisticsItem[]>([]);
const deptLoadChartRef = ref<HTMLDivElement>();
let deptLoadChart: echarts.ECharts | null = null;
const deptLoadData = ref<DepartmentLoadItem[]>([]);

const deptOptions = [
  { label: '全部科室', value: 'all' },
  { label: '口腔内科', value: 'kouqiang' },
  { label: '全科门诊', value: 'quanke' },
];

const doctorOptions = [
  { label: '全部医生', value: 'all' },
  { label: '张三', value: 'zhangsan' },
  { label: '李四', value: 'lisi' },
];

const kpiCards = reactive([
  { key: 'visit', title: '今日就诊量', value: '——', desc: '含已挂号与已就诊人数' },
  { key: 'income', title: '今日收入', value: '——', desc: '挂号费 + 检查费等' },
  { key: 'usage', title: '号源使用率', value: '——', desc: '已预约 / 可预约' },
  { key: 'noShow', title: '爽约 / 退号率', value: '——', desc: '爽约人数占比' },
]);

// 初始化今日日期范围
const initTodayDateRange = () => {
  const today = dayjs();
  dateRange.value = [today, today];
};

// 根据时间类型更新日期范围
const updateDateRangeByTimeType = () => {
  const today = dayjs();
  switch (timeType.value) {
    case 'today':
      dateRange.value = [today, today];
      break;
    case 'week':
      dateRange.value = [today.startOf('week'), today.endOf('week')];
      break;
    case 'month':
      dateRange.value = [today.startOf('month'), today.endOf('month')];
      break;
  }
};

// 监听时间类型变化
watch(
  () => timeType.value,
  () => {
    updateDateRangeByTimeType();
    loadStatisticsData();
  }
);

// 渲染就诊量趋势图
const renderOutpatientChart = (empty = false) => {
  if (!outpatientChartRef.value) return;
  if (!outpatientChart) {
    outpatientChart = echarts.init(outpatientChartRef.value);
  }

  if (empty || registrationData.value.length === 0) {
    outpatientChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#999', fontSize: 14 } },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [],
      grid: { left: 40, right: 20, bottom: 40, top: 20 },
    });
    return;
  }

  // 按日期汇总挂号量
  const dateMap = new Map<string, number>();
  registrationData.value.forEach((item) => {
    const count = dateMap.get(item.date) || 0;
    // 优先用各号别挂号量 typeRegistration，否则用 totalRegistration
    const val = item.typeRegistration ?? item.totalRegistration ?? 0;
    dateMap.set(item.date, count + val);
  });
  const dates = Array.from(dateMap.keys()).sort();
  const values = dates.map((d) => dateMap.get(d) || 0);

  outpatientChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, bottom: 40, top: 30 },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', name: '人次' },
    series: [
      {
        type: 'line',
        smooth: true,
        data: values,
        showSymbol: true,
        symbolSize: 8,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.35)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.05)' },
          ]),
        },
        lineStyle: { color: '#1890ff', width: 2 },
        itemStyle: { color: '#1890ff' },
      },
    ],
  });
};

// 渲染科室负荷统计
const renderDeptLoadChart = (empty = false) => {
  if (!deptLoadChartRef.value) return;
  if (!deptLoadChart) {
    deptLoadChart = echarts.init(deptLoadChartRef.value);
  }

  if (empty || deptLoadData.value.length === 0) {
    deptLoadChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#999', fontSize: 14 } },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: [] },
      series: [],
      grid: { left: 60, right: 20, bottom: 40, top: 20 },
    });
    return;
  }

  const names = deptLoadData.value.map((item) => item.deptName || '未知科室');
  const usage = deptLoadData.value.map((item) => Number(item.quotaUsageRate || 0));

  deptLoadChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 70, right: 20, bottom: 40, top: 20 },
    xAxis: { type: 'value', name: '使用率(%)', max: 100 },
    yAxis: { type: 'category', data: names },
    series: [
      {
        type: 'bar',
        data: usage,
        label: { show: true, position: 'right', formatter: '{c}%' },
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#6ec6ff' },
            { offset: 1, color: '#1890ff' },
          ]),
        },
      },
    ],
  });
};

// 获取就诊量趋势 + 顶部汇总
const loadStatisticsData = async () => {
  // 如果没有选择日期范围，默认使用今日
  let startDate = dayjs().format('YYYY-MM-DD');
  let endDate = dayjs().format('YYYY-MM-DD');

  if (dateRange.value && dateRange.value[0] && dateRange.value[1]) {
    startDate = dateRange.value[0].format('YYYY-MM-DD');
    endDate = dateRange.value[1].format('YYYY-MM-DD');
  }

  const periodType: StatisticsQuery['periodType'] =
    timeType.value === 'today' ? 'day' : timeType.value;

  const queryParams: StatisticsQuery = {
    periodType,
    startDate,
    endDate,
    deptId: dept.value && dept.value !== 'all' ? Number(dept.value) : undefined,
    doctorId: doctor.value && doctor.value !== 'all' ? Number(doctor.value) : undefined,
  };

  loading.value = true;
  try {
    const [summary, registrationStats, deptLoadStats] = await Promise.all([
      getStatisticsSummary(queryParams),
      getRegistrationStatistics(queryParams),
      getDepartmentLoadStatistics(queryParams),
    ]);
    registrationData.value = Array.isArray(registrationStats) ? registrationStats : [];
    deptLoadData.value = Array.isArray(deptLoadStats) ? deptLoadStats : [];
    
    // 更新今日就诊量卡片
    const visitCount = summary.totalVisitCount || summary.totalRegistration || 0;
    const visitCard = kpiCards.find((card) => card.key === 'visit');
    if (visitCard) {
      visitCard.value = visitCount.toString();
    }
    
    // 更新今日收入卡片
    const totalIncome = summary.totalIncome || 0;
    const incomeCard = kpiCards.find((card) => card.key === 'income');
    if (incomeCard) {
      // 格式化金额，保留两位小数
      incomeCard.value = `¥${Number(totalIncome).toFixed(2)}`;
    }
    
    // 更新号源使用率卡片
    const avgDeptLoad = summary.avgDeptLoad || 0;
    const usageCard = kpiCards.find((card) => card.key === 'usage');
    if (usageCard) {
      usageCard.value = `${Number(avgDeptLoad).toFixed(2)}%`;
    }
    
    // 更新爽约/退号率卡片
    const avgCancelRate = summary.avgCancelRate || 0;
    const noShowCard = kpiCards.find((card) => card.key === 'noShow');
    if (noShowCard) {
      noShowCard.value = `${Number(avgCancelRate).toFixed(2)}%`;
    }
    // 渲染就诊量趋势图
    await nextTick();
    renderOutpatientChart();
    renderDeptLoadChart();
  } catch (error: any) {
    console.error('加载统计数据失败:', error);
    message.error(error?.message || '加载统计数据失败');
    // 发生错误时重置所有卡片
    kpiCards.forEach((card) => {
      card.value = '——';
    });
    deptLoadData.value = [];
    registrationData.value = [];
    await nextTick();
    renderOutpatientChart(true);
    renderDeptLoadChart(true);
  } finally {
    loading.value = false;
  }
};

// 查询按钮处理
const handleQuery = () => {
  loadStatisticsData();
};

// 重置按钮处理
const handleReset = () => {
  timeType.value = 'today';
  initTodayDateRange();
  dept.value = undefined;
  doctor.value = undefined;
  loadStatisticsData();
};

// 组件挂载时加载数据
onMounted(() => {
  initTodayDateRange();
  loadStatisticsData();
});

// 窗口尺寸变化时自适应
  window.addEventListener('resize', () => {
    outpatientChart?.resize();
    deptLoadChart?.resize();
});
</script>

<style scoped lang="less">
.data-screen {
  padding: 0 16px 16px;
  background: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.subtitle {
  margin: 4px 0 0;
  color: #999;
  font-size: 12px;
}

.kpi-row {
  margin-bottom: 16px;
}

.kpi-card {
  min-height: 100px;
}

.kpi-title {
  font-size: 13px;
  color: #999;
}

.kpi-value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 600;
  color: #1890ff;
  display: flex;
  align-items: center;
  gap: 8px;

  &.loading-value {
    color: #999;
  }
}

.kpi-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #aaa;
}

.main-row {
  margin-bottom: 16px;
}

.block-card {
  margin-bottom: 16px;
}

.chart-placeholder,
.table-placeholder {
  height: 260px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  text-align: center;
  padding: 0 12px;
}

.chart-box {
  width: 100%;
  height: 100%;
}

.bottom-table-card .table-placeholder {
  height: 260px;
}

.status-list {
  padding-left: 18px;
  margin: 0;
  color: #666;
  line-height: 1.8;
}
</style>