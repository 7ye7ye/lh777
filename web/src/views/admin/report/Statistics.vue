<template>
  <PageWrapper title="数据统计">
    <div class="statistics-container">
      <!-- 查询条件 -->
      <a-card :bordered="false" class="query-card">
        <a-form :model="queryParams" layout="inline">
          <a-form-item label="统计周期">
            <a-radio-group v-model:value="queryParams.periodType">
              <a-radio-button value="day">按日</a-radio-button>
              <a-radio-button value="week">按周</a-radio-button>
              <a-radio-button value="month">按月</a-radio-button>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="日期范围">
            <a-range-picker
              v-model:value="dateRange"
              format="YYYY-MM-DD"
              :placeholder="['开始日期', '结束日期']"
              @change="onDateRangeChange"
            />
          </a-form-item>
          <a-form-item label="科室">
            <a-tree-select
              v-model:value="queryParams.deptId"
              :tree-data="departmentTreeData"
              placeholder="选择科室"
              style="width: 200px"
              allow-clear
              tree-default-expand-all
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="loadStatistics" :loading="loading">
              <template #icon><SearchOutlined /></template>
              统计
            </a-button>
            <a-button style="margin-left: 8px" @click="resetQuery">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-form-item>
        </a-form>
      </a-card>

      <!-- 统计卡片 -->
      <a-row :gutter="16" class="stats-cards">
        <a-col :xs="24" :sm="12" :md="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="总门诊量"
              :value="summary.totalVisitCount"
              :prefix="loading ? '' : ''"
              :loading="loading"
            >
              <template #suffix>
                <span class="stat-unit">人次</span>
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="平均科室负荷"
              :value="summary.avgDeptLoad"
              :precision="1"
              :loading="loading"
            >
              <template #suffix>%</template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="平均退号率"
              :value="summary.avgCancelRate"
              :precision="2"
              :loading="loading"
            >
              <template #suffix>%</template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="总挂号量"
              :value="summary.totalRegistration"
              :loading="loading"
            >
              <template #suffix>
                <span class="stat-unit">人次</span>
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <!-- 图表区域 -->
      <a-row :gutter="16" class="charts-row">
        <!-- 门诊量统计图表 -->
        <a-col :xs="24" :lg="12">
          <a-card :bordered="false" title="门诊量统计">
            <div ref="outpatientChartRef" style="width: 100%; height: 400px"></div>
          </a-card>
        </a-col>
        <!-- 科室负荷统计图表 -->
        <a-col :xs="24" :lg="12">
          <a-card :bordered="false" title="科室负荷统计">
            <div ref="deptLoadChartRef" style="width: 100%; height: 400px"></div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" class="charts-row">
        <!-- 退号率统计图表 -->
        <a-col :xs="24" :lg="12">
          <a-card :bordered="false" title="退号率统计">
            <div ref="cancelRateChartRef" style="width: 100%; height: 400px"></div>
          </a-card>
        </a-col>
        <!-- 挂号量统计图表 -->
        <a-col :xs="24" :lg="12">
          <a-card :bordered="false" title="挂号量统计">
            <div ref="registrationChartRef" style="width: 100%; height: 400px"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 详细数据表格 -->
      <a-card :bordered="false" title="详细数据" class="table-card">
        <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
          <a-tab-pane key="outpatient" tab="门诊量">
            <a-table
              :columns="outpatientColumns"
              :data-source="outpatientData"
              :loading="loading"
              :pagination="false"
              size="middle"
            />
          </a-tab-pane>
          <a-tab-pane key="deptLoad" tab="科室负荷">
            <a-table
              :columns="deptLoadColumns"
              :data-source="deptLoadData"
              :loading="loading"
              :pagination="false"
              size="middle"
            />
          </a-tab-pane>
          <a-tab-pane key="cancelRate" tab="退号率">
            <a-table
              :columns="cancelRateColumns"
              :data-source="cancelRateData"
              :loading="loading"
              :pagination="false"
              size="middle"
            />
          </a-tab-pane>
          <a-tab-pane key="registration" tab="挂号量">
            <a-table
              :columns="registrationColumns"
              :data-source="registrationData"
              :loading="loading"
              :pagination="false"
              size="middle"
            />
          </a-tab-pane>
          <a-tab-pane key="referral" tab="转诊情况">
            <a-table
              :columns="referralColumns"
              :data-source="referralData"
              :loading="loading"
              :pagination="false"
              size="middle"
            />
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import * as echarts from 'echarts';
import {
  getOutpatientStatistics,
  getDepartmentLoadStatistics,
  getCancelRateStatistics,
  getRegistrationStatistics,
  getReferralStatistics,
  getStatisticsSummary,
  type StatisticsQuery,
} from '/@/api/hospital/statistics';
import { getDepartmentList } from '/@/api/hospital/department';
import type { Department } from '/@/api/hospital/department';
import { convertDepartmentsToTree, type DepartmentTreeNode } from '/@/utils/departmentHelper';
import type {
  OutpatientStatisticsItem,
  DepartmentLoadItem,
  CancelRateItem,
  RegistrationStatisticsItem,
  ReferralStatisticsItem,
} from '/@/api/hospital/statistics';

const loading = ref(false);
const dateRange = ref<[Dayjs, Dayjs] | null>(null);
const departmentList = ref<Department[]>([]);
const departmentTreeData = ref<DepartmentTreeNode[]>([]);
const activeTab = ref('outpatient');

const queryParams = reactive<StatisticsQuery>({
  periodType: 'day',
  startDate: dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  endDate: dayjs().format('YYYY-MM-DD'),
  deptId: undefined,
});

const summary = reactive({
  totalVisitCount: 0,
  avgDeptLoad: 0,
  avgCancelRate: 0,
  totalRegistration: 0,
});

const outpatientData = ref<OutpatientStatisticsItem[]>([]);
const deptLoadData = ref<DepartmentLoadItem[]>([]);
const cancelRateData = ref<CancelRateItem[]>([]);
const registrationData = ref<RegistrationStatisticsItem[]>([]);
const referralData = ref<ReferralStatisticsItem[]>([]);

// 图表引用
const outpatientChartRef = ref<HTMLDivElement>();
const deptLoadChartRef = ref<HTMLDivElement>();
const cancelRateChartRef = ref<HTMLDivElement>();
const registrationChartRef = ref<HTMLDivElement>();

let outpatientChart: echarts.ECharts | null = null;
let deptLoadChart: echarts.ECharts | null = null;
let cancelRateChart: echarts.ECharts | null = null;
let registrationChart: echarts.ECharts | null = null;

// 表格列定义
const outpatientColumns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 120 },
  { title: '科室', dataIndex: 'deptName', key: 'deptName' },
  { title: '门诊量', dataIndex: 'visitCount', key: 'visitCount', align: 'right' },
  { title: '总门诊量', dataIndex: 'totalVisitCount', key: 'totalVisitCount', align: 'right' },
  {
    title: '增长率',
    dataIndex: 'growthRate',
    key: 'growthRate',
    align: 'right',
    customRender: ({ text }: { text: number }) =>
      text ? `${text > 0 ? '+' : ''}${text.toFixed(2)}%` : '-',
  },
];

const deptLoadColumns = [
  { title: '科室', dataIndex: 'deptName', key: 'deptName' },
  { title: '医生', dataIndex: 'doctorName', key: 'doctorName' },
  {
    title: '出诊时长',
    dataIndex: 'visitDurationHours',
    key: 'visitDurationHours',
    align: 'right',
    customRender: ({ text }: { text: number }) => `${text ? text.toFixed(1) : 0}小时`,
  },
  {
    title: '号源使用率',
    dataIndex: 'quotaUsageRate',
    key: 'quotaUsageRate',
    align: 'right',
    customRender: ({ text }: { text: number }) => `${text.toFixed(2)}%`,
  },
];

const cancelRateColumns = [
  { title: '科室', dataIndex: 'deptName', key: 'deptName' },
  { title: '医生', dataIndex: 'doctorName', key: 'doctorName' },
  { title: '号别', dataIndex: 'typeName', key: 'typeName' },
  { title: '总挂号数', dataIndex: 'totalCount', key: 'totalCount', align: 'right' },
  { title: '退号数', dataIndex: 'cancelCount', key: 'cancelCount', align: 'right' },
  {
    title: '退号率',
    dataIndex: 'cancelRate',
    key: 'cancelRate',
    align: 'right',
    customRender: ({ text }: { text: number }) => `${text.toFixed(2)}%`,
  },
];

const registrationColumns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 120 },
  { title: '号别', dataIndex: 'typeName', key: 'typeName' },
  { title: '挂号量', dataIndex: 'typeRegistration', key: 'typeRegistration', align: 'right' },
  { title: '总挂号量', dataIndex: 'totalRegistration', key: 'totalRegistration', align: 'right' },
  {
    title: '增长率',
    dataIndex: 'growthRate',
    key: 'growthRate',
    align: 'right',
    customRender: ({ text }: { text: number }) =>
      text ? `${text > 0 ? '+' : ''}${text.toFixed(2)}%` : '-',
  },
];

const referralColumns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 120 },
  { title: '科室', dataIndex: 'deptName', key: 'deptName', width: 150 },
  { title: '转诊类型', dataIndex: 'targetTypeName', key: 'targetTypeName', width: 100 },
  { title: '申请数量', dataIndex: 'applicationCount', key: 'applicationCount', align: 'right', width: 100 },
  { title: '已批准', dataIndex: 'approvedCount', key: 'approvedCount', align: 'right', width: 100 },
  { title: '已拒绝', dataIndex: 'rejectedCount', key: 'rejectedCount', align: 'right', width: 100 },
  { title: '已取消', dataIndex: 'cancelledCount', key: 'cancelledCount', align: 'right', width: 100 },
  { title: '已完成', dataIndex: 'completedCount', key: 'completedCount', align: 'right', width: 100 },
  { title: '总数量', dataIndex: 'totalCount', key: 'totalCount', align: 'right', width: 100 },
  {
    title: '批准率',
    dataIndex: 'approvalRate',
    key: 'approvalRate',
    align: 'right',
    width: 100,
    customRender: ({ text }: { text: number }) => `${text?.toFixed(2) || 0}%`,
  },
  {
    title: '完成率',
    dataIndex: 'completionRate',
    key: 'completionRate',
    align: 'right',
    width: 100,
    customRender: ({ text }: { text: number }) => `${text?.toFixed(2) || 0}%`,
  },
];

// 初始化日期范围
const initDateRange = () => {
  const end = dayjs();
  const start = end.subtract(7, 'day');
  dateRange.value = [start, end];
  queryParams.startDate = start.format('YYYY-MM-DD');
  queryParams.endDate = end.format('YYYY-MM-DD');
};

// 日期范围变化
const onDateRangeChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates && dates[0] && dates[1]) {
    queryParams.startDate = dates[0].format('YYYY-MM-DD');
    queryParams.endDate = dates[1].format('YYYY-MM-DD');
  }
};

// 加载科室列表
const loadDepartments = async () => {
  try {
    const list = await getDepartmentList();
    departmentList.value = Array.isArray(list) ? list : [];
    // 转换为树形结构
    departmentTreeData.value = convertDepartmentsToTree(departmentList.value);
  } catch (error) {
    console.error('加载科室列表失败:', error);
  }
};

// 加载统计数据
const loadStatistics = async () => {
  if (!queryParams.startDate || !queryParams.endDate) {
    message.warning('请选择日期范围');
    return;
  }

  loading.value = true;
  try {
    // 并行加载所有统计数据
    const [summaryRes, outpatientRes, deptLoadRes, cancelRateRes, registrationRes, referralRes] =
      await Promise.all([
        getStatisticsSummary(queryParams),
        getOutpatientStatistics(queryParams),
        getDepartmentLoadStatistics(queryParams),
        getCancelRateStatistics(queryParams),
        getRegistrationStatistics(queryParams),
        getReferralStatistics(queryParams),
      ]);

    // 更新汇总数据
    Object.assign(summary, {
      totalVisitCount: summaryRes.totalVisitCount || 0,
      avgDeptLoad: summaryRes.avgDeptLoad || 0,
      avgCancelRate: summaryRes.avgCancelRate || 0,
      totalRegistration: summaryRes.totalRegistration || 0,
    });

    // 更新表格数据
    outpatientData.value = Array.isArray(outpatientRes) ? outpatientRes : [];
    deptLoadData.value = Array.isArray(deptLoadRes) ? deptLoadRes : [];
    cancelRateData.value = Array.isArray(cancelRateRes) ? cancelRateRes : [];
    registrationData.value = Array.isArray(registrationRes) ? registrationRes : [];
    referralData.value = Array.isArray(referralRes) ? referralRes : [];

    // 更新图表
    await nextTick();
    updateCharts();
  } catch (error: any) {
    console.error('加载统计数据失败:', error);
    message.error(error?.message || '加载统计数据失败');
  } finally {
    loading.value = false;
  }
};

// 更新图表
const updateCharts = () => {
  updateOutpatientChart();
  updateDeptLoadChart();
  updateCancelRateChart();
  updateRegistrationChart();
};

// 更新门诊量图表
const updateOutpatientChart = () => {
  if (!outpatientChartRef.value || outpatientData.value.length === 0) return;

  if (!outpatientChart) {
    outpatientChart = echarts.init(outpatientChartRef.value);
  }

  // 按日期分组
  const dateMap = new Map<string, number>();
  outpatientData.value.forEach((item) => {
    const count = dateMap.get(item.date) || 0;
    dateMap.set(item.date, count + item.visitCount);
  });

  const dates = Array.from(dateMap.keys()).sort();
  const values = dates.map((date) => dateMap.get(date) || 0);

  outpatientChart.setOption({
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['门诊量'],
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
    },
    yAxis: {
      type: 'value',
      name: '人次',
    },
    series: [
      {
        name: '门诊量',
        type: 'line',
        smooth: true,
        data: values,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(74, 144, 226, 0.3)' },
            { offset: 1, color: 'rgba(74, 144, 226, 0.1)' },
          ]),
        },
        itemStyle: {
          color: '#4a90e2',
        },
      },
    ],
  });
};

// 更新科室负荷图表
const updateDeptLoadChart = () => {
  if (!deptLoadChartRef.value || deptLoadData.value.length === 0) return;

  if (!deptLoadChart) {
    deptLoadChart = echarts.init(deptLoadChartRef.value);
  }

  const deptNames = deptLoadData.value.map((item) => item.deptName);
  const usageRates = deptLoadData.value.map((item) => item.quotaUsageRate);

  deptLoadChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'value',
      name: '使用率(%)',
      max: 100,
    },
    yAxis: {
      type: 'category',
      data: deptNames,
    },
    series: [
      {
        name: '号源使用率',
        type: 'bar',
        data: usageRates,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#6ec6ff' },
            { offset: 1, color: '#4a90e2' },
          ]),
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c}%',
        },
      },
    ],
  });
};

// 更新退号率图表
const updateCancelRateChart = () => {
  if (!cancelRateChartRef.value || cancelRateData.value.length === 0) return;

  if (!cancelRateChart) {
    cancelRateChart = echarts.init(cancelRateChartRef.value);
  }

  const labels = cancelRateData.value.map(
    (item) => item.deptName || item.doctorName || item.typeName || '未知'
  );
  const rates = cancelRateData.value.map((item) => item.cancelRate);

  cancelRateChart.setOption({
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
    },
    series: [
      {
        name: '退号率',
        type: 'pie',
        radius: '60%',
        data: labels.map((label, index) => ({
          value: rates[index],
          name: label,
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  });
};

// 更新挂号量图表
const updateRegistrationChart = () => {
  if (!registrationChartRef.value || registrationData.value.length === 0) return;

  if (!registrationChart) {
    registrationChart = echarts.init(registrationChartRef.value);
  }

  // 按日期分组
  const dateMap = new Map<string, number>();
  registrationData.value.forEach((item) => {
    const count = dateMap.get(item.date) || 0;
    dateMap.set(item.date, count + (item.typeRegistration || item.totalRegistration || 0));
  });

  const dates = Array.from(dateMap.keys()).sort();
  const values = dates.map((date) => dateMap.get(date) || 0);

  registrationChart.setOption({
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['挂号量'],
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: dates,
    },
    yAxis: {
      type: 'value',
      name: '人次',
    },
    series: [
      {
        name: '挂号量',
        type: 'bar',
        data: values,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4a90e2' },
            { offset: 1, color: '#6ec6ff' },
          ]),
        },
      },
    ],
  });
};

// 重置查询
const resetQuery = () => {
  queryParams.deptId = undefined;
  initDateRange();
  loadStatistics();
};

// Tab切换
const onTabChange = (key: string) => {
  activeTab.value = key;
};

// 初始化
onMounted(async () => {
  initDateRange();
  await loadDepartments();
  await loadStatistics();

  // 窗口大小改变时重新调整图表
  window.addEventListener('resize', () => {
    outpatientChart?.resize();
    deptLoadChart?.resize();
    cancelRateChart?.resize();
    registrationChart?.resize();
  });
});
</script>

<style scoped lang="less">
.statistics-container {
  padding: 16px;
}

.query-card {
  margin-bottom: 16px;
}

.stats-cards {
  margin-bottom: 16px;

  .stat-card {
    text-align: center;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }
  }

  .stat-unit {
    font-size: 14px;
    color: #999;
    margin-left: 4px;
  }
}

.charts-row {
  margin-bottom: 16px;
}

.table-card {
  :deep(.ant-card-body) {
    padding-top: 16px;
  }
}
</style>