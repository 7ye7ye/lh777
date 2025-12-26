<template>
  <PageWrapper>
    <template #headerContent>
      <div class="page-header">
        <div class="header-left">
          <div class="page-title">数据分析</div>
          <p class="subtitle">医院运营数据分析与概览</p>
        </div>
        <div class="header-actions">
          <a-button type="primary" @click="goToStatistics">
            <template #icon><BarChartOutlined /></template>
            查看详细统计
          </a-button>
        </div>
      </div>
    </template>

    <div class="dashboard-container">
      <!-- 快速概览卡片 -->
      <a-row :gutter="16" class="overview-row">
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-card :bordered="false" class="overview-card">
            <a-statistic
              title="今日就诊量"
              :value="loading ? 0 : overviewData.todayVisit"
              :precision="0"
              suffix="人"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-card :bordered="false" class="overview-card">
            <a-statistic
              title="今日收入"
              :value="loading ? 0 : overviewData.todayIncome"
              :precision="2"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <DollarOutlined style="margin-right: 4px" />
                <span>¥</span>
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-card :bordered="false" class="overview-card">
            <a-statistic
              title="在岗医生"
              :value="loading ? 0 : overviewData.activeDoctors"
              :precision="0"
              suffix="人"
              :value-style="{ color: '#faad14' }"
            >
              <template #prefix>
                <TeamOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-card :bordered="false" class="overview-card">
            <a-statistic
              title="号源使用率"
              :value="loading ? 0 : overviewData.quotaUsage"
              :precision="1"
              suffix="%"
              :value-style="{ color: '#722ed1' }"
            >
              <template #prefix>
                <PieChartOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <!-- 功能导航卡片 -->
      <a-row :gutter="16" class="nav-row">
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-card
            :bordered="false"
            class="nav-card"
            hoverable
            @click="goToStatistics"
          >
            <div class="nav-card-content">
              <BarChartOutlined class="nav-icon" />
              <div class="nav-title">数据统计</div>
              <div class="nav-desc">查看详细的统计数据和分析图表</div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-card
            :bordered="false"
            class="nav-card"
            hoverable
            @click="goToReportExport"
          >
            <div class="nav-card-content">
              <FileTextOutlined class="nav-icon" />
              <div class="nav-title">报表生成</div>
              <div class="nav-desc">生成和导出各类统计报表</div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-card
            :bordered="false"
            class="nav-card"
            hoverable
            @click="goToScheduleAdjustment"
          >
            <div class="nav-card-content">
              <CalendarOutlined class="nav-icon" />
              <div class="nav-title">排班调整</div>
              <div class="nav-desc">管理和调整医生排班安排</div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6">
          <a-card
            :bordered="false"
            class="nav-card"
            hoverable
            @click="goToDepartmentManagement"
          >
            <div class="nav-card-content">
              <BankOutlined class="nav-icon" />
              <div class="nav-title">科室管理</div>
              <div class="nav-desc">管理医院科室信息</div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 系统信息卡片 -->
      <a-card title="系统概览" :bordered="false" class="info-card">
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <h3>系统信息</h3>
            <a-descriptions :column="1" bordered size="small">
              <a-descriptions-item label="系统名称">
                北京交通大学校医院管理系统
              </a-descriptions-item>
              <a-descriptions-item label="当前时间">
                {{ currentTime }}
              </a-descriptions-item>
              <a-descriptions-item label="数据更新时间">
                {{ lastUpdateTime }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :xs="24" :md="12">
            <h3>快速操作</h3>
            <a-space direction="vertical" style="width: 100%">
              <a-button type="primary" block @click="goToStatistics">
                查看数据统计
              </a-button>
              <a-button @click="goToReportExport">生成报表</a-button>
              <a-button @click="goToScheduleAdjustment">排班管理</a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { PageWrapper } from '/@/components/Page';
import {
  UserOutlined,
  DollarOutlined,
  TeamOutlined,
  PieChartOutlined,
  BarChartOutlined,
  FileTextOutlined,
  CalendarOutlined,
  BankOutlined,
} from '@ant-design/icons-vue';
import { getStatisticsSummary } from '/@/api/hospital/statistics';
import dayjs from 'dayjs';

const router = useRouter();
const loading = ref(false);
const currentTime = ref('');
const lastUpdateTime = ref('');
let timeInterval: NodeJS.Timeout | null = null;

const overviewData = ref({
  todayVisit: 0,
  todayIncome: 0,
  activeDoctors: 0,
  quotaUsage: 0,
});

// 加载概览数据
const loadOverviewData = async () => {
  loading.value = true;
  try {
    const today = dayjs().format('YYYY-MM-DD');
    const summary = await getStatisticsSummary({
      periodType: 'day',
      startDate: today,
      endDate: today,
    });

    if (summary) {
      overviewData.value = {
        todayVisit: summary.totalVisitCount || summary.totalRegistration || 0,
        todayIncome: summary.totalIncome || 0,
        activeDoctors: summary.activeStaffCount || 0,
        quotaUsage: summary.avgDeptLoad || 0,
      };
    }
    lastUpdateTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
  } catch (error) {
    console.error('加载概览数据失败:', error);
    // 如果加载失败，使用默认值
    overviewData.value = {
      todayVisit: 0,
      todayIncome: 0,
      activeDoctors: 0,
      quotaUsage: 0,
    };
  } finally {
    loading.value = false;
  }
};

// 更新当前时间
const updateCurrentTime = () => {
  currentTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
};

// 导航到数据统计页面
const goToStatistics = () => {
  router.push('/admin/statistics');
};

// 导航到报表生成页面
const goToReportExport = () => {
  router.push('/admin/report-export');
};

// 导航到排班调整页面
const goToScheduleAdjustment = () => {
  router.push('/admin/schedule-today');
};

// 导航到科室管理页面
const goToDepartmentManagement = () => {
  router.push('/admin/management/department');
};

onMounted(() => {
  updateCurrentTime();
  timeInterval = setInterval(updateCurrentTime, 1000);
  loadOverviewData();
});

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval);
  }
});
</script>

<style scoped lang="less">
.dashboard-container {
  padding: 16px;
  background: #f5f7fa;
  min-height: calc(100vh - 200px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.header-left {
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #333;
  }

  .subtitle {
    margin: 4px 0 0;
    color: #999;
    font-size: 12px;
  }
}

.overview-row {
  margin-bottom: 16px;
}

.overview-card {
  text-align: center;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  :deep(.ant-statistic-title) {
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
  }

  :deep(.ant-statistic-content) {
    font-size: 24px;
    font-weight: 600;
  }
}

.nav-row {
  margin-bottom: 16px;
}

.nav-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 140px;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transform: translateY(-4px);
  }
}

.nav-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.nav-icon {
  font-size: 32px;
  color: #1890ff;
  margin-bottom: 12px;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.nav-desc {
  font-size: 12px;
  color: #999;
  line-height: 1.5;
}

.info-card {
  margin-bottom: 16px;

  h3 {
    margin-bottom: 16px;
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
}
</style>
