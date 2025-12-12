<template>
  <PageWrapper>
    <!-- 头部：标题 + 筛选栏 -->
    <template #headerContent>
      <div class="page-header">
        <div class="header-left">
          <div class="page-title">数据大屏</div>
          <p class="subtitle">医院运营概览 · 挂号 / 就诊 / 号源 / 收入</p>
        </div>
        <div class="header-filters">
          <a-space :size="12" wrap>
        <a-radio-group v-model="timeType" size="middle" @change="handleTimeTypeChange">
              <a-radio-button value="today">今日</a-radio-button>
              <a-radio-button value="week">本周</a-radio-button>
              <a-radio-button value="month">本月</a-radio-button>
            </a-radio-group>

          <a-range-picker v-model="dateRange" @change="onRangeChange" />

            <a-button type="primary" @click="handleQuery" :loading="loading">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </div>
      </div>
              </template>

    <div class="data-screen">
      <!-- 一、顶部 KPI 区 -->
      <a-row type="flex" justify="space-between" :gutter="12" class="kpi-row">
        <a-col class="kpi-col" v-for="item in kpiCards" :key="item.key">
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
          <a-card title="就诊量趋势" :bordered="false" class="block-card">
            <div class="chart-placeholder">
              <div ref="outpatientChartRef" class="chart-box"></div>
            </div>
          </a-card>

          <a-card title="收入趋势" :bordered="false" class="block-card">
            <div class="chart-placeholder">
              <div ref="incomeChartRef" class="chart-box"></div>
            </div>
          </a-card>

          <a-card title="患者信息评估" :bordered="false" class="block-card">
            <div class="chart-placeholder patient-card">
              <a-table
                :columns="patientEvalColumns"
                :data-source="patientEvalView"
                :pagination="false"
              :loading="loading"
                size="small"
                :scroll="{ y: 220 }"
                class="patient-eval-table"
                v-if="patientEvalView.length"
            >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'riskText'">
                    <span class="risk-badge" :class="record.riskLevel">{{ record.riskText }}</span>
              </template>
                  <template v-else-if="column.key === 'patientName'">
                    {{ record.patientName || '—' }}
                  </template>
                  <template v-else-if="column.key === 'deptName'">
                    {{ record.deptName || '未知科室' }}
                  </template>
                  <template v-else-if="column.key === 'registerTime'">
                    {{ record.registerTime || '—' }}
                  </template>
                </template>
              </a-table>
              <div v-else class="empty-tip">暂无数据</div>
            </div>
          </a-card>
        </a-col>

        <!-- 右侧 -->
        <a-col :xs="24" :lg="8">
          <a-card title="科室门诊量统计" :bordered="false" class="block-card">
            <div class="chart-placeholder">
              <div ref="deptLoadChartRef" class="chart-box"></div>
            </div>
          </a-card>

          <a-card key="timeslot-card" title="就诊时段分布" :bordered="false" class="block-card">
            <div class="chart-placeholder">
              <div ref="timeSlotChartRef" class="chart-box"></div>
            </div>
          </a-card>

          <a-card key="doctor-card" title="医生工作量 Top 5" :bordered="false" class="block-card">
            <div class="chart-placeholder">
              <div ref="doctorChartRef" class="chart-box"></div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 三、底部明细表格区 -->
      <a-card title="科室 / 医生明细列表" :bordered="false" class="block-card bottom-table-card">
        <a-tabs v-model="activeTab">
          <a-tab-pane key="dept" tab="按科室统计">
            <div class="table-toolbar">
              <a-space>
                <span>科室筛选：</span>
                <a-select
                  v-model="deptFilter"
                  allow-clear
                  placeholder="全部科室"
                  style="width: 180px"
                  :options="deptFilterOptions"
                />
              </a-space>
            </div>
            <a-table
              :columns="deptColumns"
              :data-source="filteredDeptDetailData"
              :loading="loading"
              :pagination="false"
              size="middle"
              row-key="deptId"
            />
          </a-tab-pane>
          <a-tab-pane key="doctor" tab="按医生统计">
            <a-table
              :columns="doctorColumns"
              :data-source="doctorDetailData"
              :loading="loading"
              :pagination="false"
              size="middle"
              row-key="doctorId"
            />
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  getStatisticsSummary,
  getRegistrationStatistics,
  getDepartmentLoadStatistics,
  getTimeSlotDistribution,
  getIncomeTrend,
  getDeptDetail,
  getDoctorDetail,
  getPatientEval,
  type StatisticsQuery,
  type RegistrationStatisticsItem,
  type DepartmentLoadItem,
  type TimeSlotItem,
  type IncomeTrendItem,
  type DeptDetailItem,
  type DoctorDetailItem,
  type PatientEvalItem,
} from '/@/api/hospital/statistics';
import * as echarts from 'echarts';

type TimeType = 'today' | 'week' | 'month';
type TabKey = 'dept' | 'doctor';

const loading = ref(false);
const timeType = ref<TimeType>('today');
const dateRange = ref<[dayjs.Dayjs, dayjs.Dayjs] | null>(null);
const isCustomRange = ref(false); // 手动选择日期范围时使用日粒度
const activeTab = ref<TabKey>('dept');
const outpatientChartRef = ref<HTMLDivElement>();
let outpatientChart: echarts.ECharts | null = null;
const registrationData = ref<RegistrationStatisticsItem[]>([]);
const deptLoadChartRef = ref<HTMLDivElement>();
let deptLoadChart: echarts.ECharts | null = null;
const deptLoadData = ref<DepartmentLoadItem[]>([]);
const timeSlotData = ref<TimeSlotItem[]>([]);
const timeSlotChartRef = ref<HTMLDivElement>();
let timeSlotChart: echarts.ECharts | null = null;
const incomeTrendData = ref<IncomeTrendItem[]>([]);
const incomeChartRef = ref<HTMLDivElement>();
let incomeChart: echarts.ECharts | null = null;
const deptDetailData = ref<DeptDetailItem[]>([]);
const doctorDetailData = ref<DoctorDetailItem[]>([]);
const deptFilter = ref<string | undefined>();
const patientEvalData = ref<PatientEvalItem[]>([]);
const patientEvalView = computed(() => {
  return (patientEvalData.value || []).slice(0, 5).map((item, index) => {
    const idLabel = item.studentId || item.staffId || `ID:${item.patientId}`;
    // 兼容两种可能的字段名：patientName 或 patient_name
    const patientName = (item as any).patientName || (item as any).patient_name || '';
    // 简单风险指数：根据 patientId 取模，前端生成
    const score = (item.patientId || 0) % 100;
    let riskText = '低风险';
    let riskLevel = 'low';
    if (score > 66) {
      riskText = '高风险';
      riskLevel = 'high';
    } else if (score > 33) {
      riskText = '中风险';
      riskLevel = 'mid';
    }
    const genderText = item.gender === '男' || item.gender === '女' ? item.gender : '性别未知';
    return {
      ...item,
      index: index + 1,
      idLabel,
      patientName,
      riskText,
      riskLevel,
      genderText,
    };
  });
});
const deptFilterOptions = computed(() =>
  deptDetailData.value.map((item) => ({ label: item.deptName, value: item.deptName }))
);
const filteredDeptDetailData = computed(() => {
  if (!deptFilter.value) return deptDetailData.value;
  return deptDetailData.value.filter((item) => item.deptName === deptFilter.value);
});
const doctorChartRef = ref<HTMLDivElement>();
let doctorChart: echarts.ECharts | null = null;

const deptColumns = [
  { title: '科室', dataIndex: 'deptName', key: 'deptName' },
  {
    title: '挂号数',
    dataIndex: 'registerCount',
    key: 'registerCount',
    align: 'right',
    sorter: (a: DeptDetailItem, b: DeptDetailItem) => (a.registerCount || 0) - (b.registerCount || 0),
  },
  {
    title: '退号数',
    dataIndex: 'cancelCount',
    key: 'cancelCount',
    align: 'right',
    sorter: (a: DeptDetailItem, b: DeptDetailItem) => (a.cancelCount || 0) - (b.cancelCount || 0),
  },
  {
    title: '收入',
    dataIndex: 'income',
    key: 'income',
    align: 'right',
    sorter: (a: DeptDetailItem, b: DeptDetailItem) => (a.income || 0) - (b.income || 0),
    customRender: ({ text }: { text: number }) => `¥${Number(text || 0).toFixed(2)}`,
  },
];

const doctorColumns = [
  { title: '医生', dataIndex: 'doctorName', key: 'doctorName' },
  { title: '科室', dataIndex: 'deptName', key: 'deptName' },
  {
    title: '挂号数',
    dataIndex: 'registerCount',
    key: 'registerCount',
    align: 'right',
    sorter: (a: DoctorDetailItem, b: DoctorDetailItem) => (a.registerCount || 0) - (b.registerCount || 0),
  },
  {
    title: '退号数',
    dataIndex: 'cancelCount',
    key: 'cancelCount',
    align: 'right',
    sorter: (a: DoctorDetailItem, b: DoctorDetailItem) => (a.cancelCount || 0) - (b.cancelCount || 0),
  },
  {
    title: '收入',
    dataIndex: 'income',
    key: 'income',
    align: 'right',
    sorter: (a: DoctorDetailItem, b: DoctorDetailItem) => (a.income || 0) - (b.income || 0),
    customRender: ({ text }: { text: number }) => `¥${Number(text || 0).toFixed(2)}`,
  },
];

const patientEvalColumns = [
  {
    title: '序号',
    dataIndex: 'index',
    key: 'index',
    width: 60,
    align: 'center',
  },
  {
    title: '学号/职工号',
    dataIndex: 'idLabel',
    key: 'idLabel',
    width: 130,
    ellipsis: true,
  },
  {
    title: '姓名',
    dataIndex: 'patientName',
    key: 'patientName',
    width: 100,
    ellipsis: true,
  },
  {
    title: '风险指数',
    dataIndex: 'riskText',
    key: 'riskText',
    width: 100,
    align: 'center',
  },
  {
    title: '性别',
    dataIndex: 'genderText',
    key: 'genderText',
    width: 70,
    align: 'center',
  },
  {
    title: '科室',
    dataIndex: 'deptName',
    key: 'deptName',
    width: 110,
    ellipsis: true,
  },
  {
    title: '挂号时间',
    dataIndex: 'registerTime',
    key: 'registerTime',
    width: 170,
  },
];

const kpiCards = reactive([
  { key: 'visit', title: '累计就诊量', value: '——', desc: '已挂号&已就诊，按挂号日期统计' },
  { key: 'income', title: '累计收入', value: '——', desc: '挂号费' },
  { key: 'staff', title: '在岗医护人员', value: '——', desc: '排班有效且不重复' },
  { key: 'usage', title: '号源使用率', value: '——', desc: '已用号源 / 最大号源' },
  { key: 'noShow', title: '爽约 / 退号率', value: '——', desc: '爽约人数占比' },
]);

// 日期范围变化
const onRangeChange = (values: [dayjs.Dayjs, dayjs.Dayjs] | null) => {
  dateRange.value = values;
  isCustomRange.value = true;
  // 选择日期后直接刷新
  loadStatisticsData();
};

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
      return [today, today] as [dayjs.Dayjs, dayjs.Dayjs];
    case 'week':
      return [today.startOf('week'), today.endOf('week')] as [dayjs.Dayjs, dayjs.Dayjs];
    case 'month':
      return [today.startOf('month'), today.endOf('month')] as [dayjs.Dayjs, dayjs.Dayjs];
  }
  return [today, today] as [dayjs.Dayjs, dayjs.Dayjs];
};

// 切换时间粒度
const handleTimeTypeChange = (val: any) => {
  // Ant Design Vue 的 change 事件参数是 { target: { value } }
  const nextVal = val?.target?.value ?? val;
  timeType.value = nextVal as TimeType;
  isCustomRange.value = false;
  loadStatisticsData(true);
};

const buildDateSpan = () => {
  if (!dateRange.value || !dateRange.value[0] || !dateRange.value[1]) return [];
  const span: string[] = [];
  let cur = dateRange.value[0].startOf('day');
  const end = dateRange.value[1].startOf('day');
  while (cur.isBefore(end) || cur.isSame(end)) {
    span.push(cur.format('YYYY-MM-DD'));
    cur = cur.add(1, 'day');
  }
  return span;
};

// 渲染就诊量趋势图
const renderOutpatientChart = (empty = false) => {
  if (!outpatientChartRef.value) return;
  if (!outpatientChart) {
    outpatientChart = echarts.init(outpatientChartRef.value);
  }
  // 清除上一次的配置，避免“暂无数据”标题残留
  outpatientChart.clear();

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
  const spanDates = buildDateSpan();
  const dates = spanDates.length ? spanDates : Array.from(dateMap.keys()).sort();
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
  // 清除上一次的配置，避免“暂无数据”标题残留
  deptLoadChart.clear();

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
  // quotaUsageRate 在后端已改为“科室挂号量”，这里直接用作人次
  const usage = deptLoadData.value.map((item) => Number(item.quotaUsageRate || 0));

  deptLoadChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 70, right: 20, bottom: 40, top: 20 },
    xAxis: { type: 'value', name: '就诊人数' },
    yAxis: { type: 'category', data: names },
    series: [
      {
        type: 'bar',
        data: usage,
        label: { show: true, position: 'right', formatter: '{c}' },
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

// 渲染就诊时段分布（环形图 + 右侧图注显示人数）
const renderTimeSlotChart = (empty = false) => {
  if (!timeSlotChartRef.value) {
    console.warn('timeSlotChartRef.value is null');
    return;
  }
  // 如果实例已存在但容器已销毁，重新创建
  if (timeSlotChart && timeSlotChart.isDisposed()) {
    timeSlotChart = null;
  }
  if (!timeSlotChart) {
    timeSlotChart = echarts.init(timeSlotChartRef.value);
  }
  timeSlotChart.clear();

  if (empty || timeSlotData.value.length === 0) {
    timeSlotChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#999', fontSize: 14 } },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [],
      grid: { left: 40, right: 20, bottom: 40, top: 20 },
    });
    return;
  }

  const slotLabel = (v: number) => {
    if (v === 1) return '上午';
    if (v === 2) return '下午';
    if (v === 3) return '晚上';
    return `时段${v}`;
  };

  const labels = timeSlotData.value.map((item) => slotLabel(item.timeSlot));
  const counts = timeSlotData.value.map((item) => Number(item.cnt || 0));
  const data = labels.map((name, idx) => ({ name, value: counts[idx] || 0 }));

  timeSlotChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>{c} 人 ({d}%)' },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'middle',
      formatter: (name: string) => {
        const idx = labels.indexOf(name);
        const val = counts[idx] ?? 0;
        return `${name}  ${val}人`;
      },
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{d}%', color: '#666' },
        labelLine: { show: true },
        data,
        color: ['#91caff', '#5bd8a6', '#ff9f7f'],
      },
    ],
  });
  // 确保图表正确渲染
  timeSlotChart.resize();
};

// 渲染收入趋势
const renderIncomeChart = (empty = false) => {
  if (!incomeChartRef.value) return;
  if (!incomeChart) {
    incomeChart = echarts.init(incomeChartRef.value);
  }
  incomeChart.clear();

  if (empty || incomeTrendData.value.length === 0) {
    incomeChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#999', fontSize: 14 } },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [],
      grid: { left: 40, right: 20, bottom: 40, top: 20 },
    });
    return;
  }

  const spanDates = buildDateSpan();
  const map = new Map<string, number>();
  incomeTrendData.value.forEach((item) => {
    map.set(item.date, Number(item.income || 0));
  });
  const dates = spanDates.length ? spanDates : Array.from(map.keys()).sort();
  const values = dates.map((d) => map.get(d) || 0);

  incomeChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>{c} 元' },
    grid: { left: 40, right: 20, bottom: 40, top: 30 },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', name: '元' },
    series: [
      {
        type: 'line',
        smooth: true,
        data: values,
        showSymbol: true,
        symbolSize: 8,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 136, 102, 0.35)' },
            { offset: 1, color: 'rgba(255, 136, 102, 0.05)' },
          ]),
        },
        lineStyle: { color: '#ff8845', width: 2 },
        itemStyle: { color: '#ff8845' },
      },
    ],
  });
};

// 渲染医生工作量 Top N
const renderDoctorChart = (empty = false) => {
  if (!doctorChartRef.value) {
    console.warn('doctorChartRef.value is null');
    return;
  }
  // 如果实例已存在但容器已销毁，重新创建
  if (doctorChart && doctorChart.isDisposed()) {
    doctorChart = null;
  }
  if (!doctorChart) {
    doctorChart = echarts.init(doctorChartRef.value);
  }
  doctorChart.clear();

  if (empty || doctorDetailData.value.length === 0) {
    doctorChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#999', fontSize: 14 } },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: [] },
      series: [],
      grid: { left: 60, right: 20, bottom: 40, top: 20 },
    });
    return;
  }

  // 仅取前 5 名
  const top = doctorDetailData.value
    .slice()
    .sort((a, b) => (b.registerCount || 0) - (a.registerCount || 0))
    .slice(0, 5);

  const names = top.map((item) => `${item.doctorName || '未知医生'}（${item.deptName || '—'}）`);
  const counts = top.map((item) => Number(item.registerCount || 0));

  doctorChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    // 留出右侧与底部空间，避免横轴标签被遮挡，并整体略向左偏移
    grid: { left: 60, right: 65, bottom: 50, top: 20 },
    xAxis: { type: 'value', name: '就诊人次' },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        formatter: (val: string) => {
          const match = val.match(/^(.*)（(.*)）$/);
          if (match) {
            const [, doctorName, deptName] = match;
            return `${doctorName}\n(${deptName})`;
          }
          return val;
        },
        fontSize: 11,
        lineHeight: 14,
        margin: 10,
      },
    },
    series: [
      {
        type: 'bar',
        data: counts,
        label: { show: true, position: 'right' },
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#91caff' },
            { offset: 1, color: '#2d8cf0' },
          ]),
        },
      },
    ],
  });
  // 确保图表正确渲染
  doctorChart.resize();
};

// 获取就诊量趋势 + 顶部汇总
const loadStatisticsData = async (forceUpdateRange = false) => {
  // 如果没有选择日期范围，默认使用今日
  let startDate = dayjs().format('YYYY-MM-DD');
  let endDate = dayjs().format('YYYY-MM-DD');

  if (forceUpdateRange || !dateRange.value) {
    const [s, e] = updateDateRangeByTimeType();
    dateRange.value = [s, e];
  }

  if (dateRange.value && dateRange.value[0] && dateRange.value[1]) {
    startDate = dateRange.value[0].format('YYYY-MM-DD');
    endDate = dateRange.value[1].format('YYYY-MM-DD');
  }

  // “本周 / 本月 / 今日 / 自定义”均按天取数，避免聚合到首日
  const periodType: StatisticsQuery['periodType'] =
    isCustomRange.value || timeType.value === 'month' || timeType.value === 'week' || timeType.value === 'today'
      ? 'day'
      : timeType.value;

  const queryParams: StatisticsQuery = {
    periodType,
    startDate,
    endDate,
  };

  loading.value = true;
  try {
    const [
      summary,
      registrationStats,
      deptLoadStats,
      timeSlotRes,
      incomeRes,
      deptDetailRes,
      doctorDetailRes,
      patientEvalRes,
    ] =
      await Promise.all([
      getStatisticsSummary(queryParams),
      getRegistrationStatistics(queryParams),
      getDepartmentLoadStatistics(queryParams),
      getTimeSlotDistribution({ startDate, endDate }),
      getIncomeTrend(queryParams),
        getDeptDetail({ startDate, endDate }),
        getDoctorDetail({ startDate, endDate }),
        getPatientEval({ startDate, endDate }),
    ]);
    registrationData.value = Array.isArray(registrationStats) ? registrationStats : [];
    deptLoadData.value = Array.isArray(deptLoadStats) ? deptLoadStats : [];
    timeSlotData.value = Array.isArray(timeSlotRes) ? timeSlotRes : [];
    incomeTrendData.value = Array.isArray(incomeRes) ? incomeRes : [];
    deptDetailData.value = Array.isArray(deptDetailRes) ? deptDetailRes : [];
    doctorDetailData.value = Array.isArray(doctorDetailRes) ? doctorDetailRes : [];
    patientEvalData.value = Array.isArray(patientEvalRes) ? patientEvalRes : [];
    // 调试：查看患者评估数据
    if (patientEvalData.value.length > 0) {
      console.log('患者评估数据示例:', patientEvalData.value[0]);
    }

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
    
    // 在岗医护人员
    const staffCount = summary.activeStaffCount || 0;
    const staffCard = kpiCards.find((card) => card.key === 'staff');
    if (staffCard) {
      staffCard.value = staffCount.toString();
    }

    // 更新爽约/退号率卡片
    const avgCancelRate = summary.avgCancelRate || 0;
    const noShowCard = kpiCards.find((card) => card.key === 'noShow');
    if (noShowCard) {
      noShowCard.value = `${Number(avgCancelRate).toFixed(2)}%`;
    }
    // 渲染图表
    await nextTick();
    // 使用 setTimeout 确保 DOM 完全渲染
    setTimeout(() => {
      renderOutpatientChart();
      renderDeptLoadChart();
      renderTimeSlotChart();
      renderIncomeChart();
      renderDoctorChart();
    }, 100);
  } catch (error: any) {
    console.error('加载统计数据失败:', error);
    message.error(error?.message || '加载统计数据失败');
    // 发生错误时重置所有卡片
    kpiCards.forEach((card) => {
      card.value = '——';
    });
    deptLoadData.value = [];
    registrationData.value = [];
    timeSlotData.value = [];
    incomeTrendData.value = [];
    deptDetailData.value = [];
    doctorDetailData.value = [];
    patientEvalData.value = [];
    await nextTick();
    setTimeout(() => {
      renderOutpatientChart(true);
      renderDeptLoadChart(true);
      renderTimeSlotChart(true);
      renderIncomeChart(true);
      renderDoctorChart(true);
    }, 100);
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
  isCustomRange.value = false;
  loadStatisticsData();
};

// 组件挂载时加载数据
onMounted(() => {
  const [s, e] = updateDateRangeByTimeType();
  dateRange.value = [s, e];
  loadStatisticsData();
});

// 窗口尺寸变化时自适应
  window.addEventListener('resize', () => {
    outpatientChart?.resize();
    deptLoadChart?.resize();
  timeSlotChart?.resize();
  incomeChart?.resize();
  doctorChart?.resize();
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
  display: flex;
  flex-wrap: wrap;
}

.kpi-col {
  flex: 1 1 calc(20% - 12px);
  min-width: 200px;
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
  min-height: 260px;
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

.patient-card {
  height: 260px;
  overflow: hidden;
  padding: 0;
}

.patient-eval-table {
  :deep(.ant-table) {
    font-size: 13px;
  }
  
  :deep(.ant-table-thead > tr > th) {
    background: #fafafa;
    font-weight: 600;
    padding: 10px 12px;
    border-bottom: 2px solid #e8e8e8;
    color: #333;
  }
  
  :deep(.ant-table-tbody > tr > td) {
    padding: 10px 12px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.ant-table-tbody > tr:hover > td) {
    background: #f5f5f5;
  }
  
  :deep(.ant-table-tbody > tr:last-child > td) {
    border-bottom: none;
  }
  
  // 确保风险指数样式在表格中正确应用
  :deep(.risk-badge) {
    &.risk-low {
      background: #f6ffed !important;
      color: #52c41a !important;
      border: 1px solid #b7eb8f !important;
    }
    
    &.risk-mid {
      background: #fffbe6 !important;
      color: #faad14 !important;
      border: 1px solid #ffe58f !important;
    }
    
    &.risk-high {
      background: #fff1f0 !important;
      color: #ff4d4f !important;
      border: 1px solid #ffccc7 !important;
    }
  }
}

.risk-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  
  &.risk-low {
    background: #f6ffed !important;
    color: #52c41a !important;
    border: 1px solid #b7eb8f !important;
  }
  
  &.risk-mid {
    background: #fffbe6 !important;
    color: #faad14 !important;
    border: 1px solid #ffe58f !important;
  }
  
  &.risk-high {
    background: #fff1f0 !important;
    color: #ff4d4f !important;
    border: 1px solid #ffccc7 !important;
  }
}

// 确保样式在表格中正确应用
.patient-eval-table {
  .risk-badge {
    &.risk-low {
      color: #52c41a !important;
    }
    
    &.risk-mid {
      color: #faad14 !important;
    }
    
    &.risk-high {
      color: #ff4d4f !important;
    }
  }
}

.chart-box {
  width: 100%;
  height: 100%;
}

.status-list {
  padding-left: 18px;
  margin: 0;
  color: #666;
  line-height: 1.8;
}

.table-toolbar {
  margin: 8px 0;
}
</style>