<template>
  <PageWrapper title="报表生成">
    <div class="export-container">
      <a-card :bordered="false" class="export-card">
        <a-form :model="exportParams" layout="vertical">
          <a-row :gutter="24">
            <a-col :xs="24" :md="12">
              <a-form-item label="报表类型" required>
                <a-select v-model:value="exportParams.reportType" placeholder="选择报表类型">
                  <a-select-option value="outpatient">门诊量报表</a-select-option>
                  <a-select-option value="department-load">科室负荷报表</a-select-option>
                  <a-select-option value="cancel-rate">退号率报表</a-select-option>
                  <a-select-option value="registration">挂号量报表</a-select-option>
                  <a-select-option value="referral">转诊情况报表</a-select-option>
                  <a-select-option value="comprehensive">综合报表</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="12">
              <a-form-item label="统计周期" required>
                <a-radio-group v-model:value="exportParams.periodType">
                  <a-radio-button value="day">按日</a-radio-button>
                  <a-radio-button value="week">按周</a-radio-button>
                  <a-radio-button value="month">按月</a-radio-button>
                </a-radio-group>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="24">
            <a-col :xs="24" :md="12">
              <a-form-item label="日期范围" required>
                <a-range-picker
                  v-model:value="dateRange"
                  format="YYYY-MM-DD"
                  :placeholder="['开始日期', '结束日期']"
                  @change="onDateRangeChange"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="12">
              <a-form-item label="科室筛选">
                <a-tree-select
                  v-model:value="exportParams.deptId"
                  :tree-data="departmentTreeData"
                  placeholder="选择科室（可选）"
                  allow-clear
                  tree-default-expand-all
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="导出选项">
            <a-checkbox-group v-model:value="exportOptions">
              <a-checkbox value="data">包含统计数据</a-checkbox>
              <a-checkbox value="chart">包含图表</a-checkbox>
              <a-checkbox value="summary">包含汇总信息</a-checkbox>
              <a-checkbox value="compare">包含历史对比</a-checkbox>
            </a-checkbox-group>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" size="large" @click="handleExport" :loading="exporting">
                <template #icon><DownloadOutlined /></template>
                导出Excel
              </a-button>
              <a-button @click="handlePreview" :loading="previewLoading">
                <template #icon><EyeOutlined /></template>
                预览数据
              </a-button>
              <a-button @click="resetForm">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>

        <a-alert
          type="info"
          show-icon
          message="报表说明"
          description="导出的Excel文件将包含您选择的统计数据和图表。如需包含历史对比数据，请勾选'包含历史对比'选项。"
          class="mt-4"
        />
      </a-card>

      <!-- 预览区域 -->
      <a-card v-if="previewData && previewData.length > 0" :bordered="false" title="数据预览" class="preview-card">
        <a-table
          :columns="previewColumns"
          :data-source="previewData"
          :pagination="{ pageSize: 10, showSizeChanger: true, showTotal: (total) => `共 ${total} 条` }"
          :scroll="{ x: 'max-content' }"
          size="middle"
          bordered
        />
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { DownloadOutlined, EyeOutlined, ReloadOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import * as XLSX from 'xlsx';
import {
  getOutpatientStatistics,
  getDepartmentLoadStatistics,
  getCancelRateStatistics,
  getRegistrationStatistics,
  getReferralStatistics,
  getStatisticsSummary,
  type StatisticsQuery,
} from '/@/api/hospital/statistics';
import { getDepartmentList, type Department } from '/@/api/hospital/department';
import { convertDepartmentsToTree, type DepartmentTreeNode } from '/@/utils/departmentHelper';

const exporting = ref(false);
const previewLoading = ref(false);
const dateRange = ref<[Dayjs, Dayjs] | null>(null);
const departmentList = ref<Department[]>([]);
const departmentTreeData = ref<DepartmentTreeNode[]>([]);
const exportOptions = ref<string[]>(['data', 'chart', 'summary']);

const exportParams = reactive<StatisticsQuery & { reportType: string }>({
  reportType: 'outpatient',
  periodType: 'day',
  startDate: dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  endDate: dayjs().format('YYYY-MM-DD'),
  deptId: undefined,
});

const previewData = ref<any[]>([]);
const previewColumns = ref<any[]>([]);

// 二级科室映射
const deptMap = computed(() => {
  const map = new Map<number, Department>();
  (departmentList.value || []).forEach((d) => map.set(d.deptId, d));
  return map;
});
const getParentDeptName = (deptId?: number, fallback?: string, selfName?: string) => {
  if (!deptId) return fallback || selfName || '';
  const map = deptMap.value;
  const cur = map.get(deptId);
  if (cur?.parentDeptId) {
    const parent = map.get(cur.parentDeptId);
    return parent?.deptName || fallback || selfName || '';
  }
  return fallback || selfName || '';
};
const getLevel2DeptName = (deptId?: number, fallback?: string) => {
  if (!deptId) return fallback || '';
  const map = deptMap.value;
  let cur = map.get(deptId);
  while (cur) {
    if (cur.deptLevel === 2) return cur.deptName;
    if (!cur.parentDeptId) break;
    cur = map.get(cur.parentDeptId);
  }
  return fallback || '';
};

// 初始化日期范围
const initDateRange = () => {
  const end = dayjs();
  const start = end.subtract(7, 'day');
  dateRange.value = [start, end];
  exportParams.startDate = start.format('YYYY-MM-DD');
  exportParams.endDate = end.format('YYYY-MM-DD');
};

// 日期范围变化
const onDateRangeChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates && dates[0] && dates[1]) {
    exportParams.startDate = dates[0].format('YYYY-MM-DD');
    exportParams.endDate = dates[1].format('YYYY-MM-DD');
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

// 导出Excel
const handleExport = async () => {
  if (!exportParams.reportType) {
    message.warning('请选择报表类型');
    return;
  }

  if (!exportParams.startDate || !exportParams.endDate) {
    message.warning('请选择日期范围');
    return;
  }

  exporting.value = true;
  try {
    const workbook = XLSX.utils.book_new();

    // 根据报表类型加载数据并生成Excel
    switch (exportParams.reportType) {
      case 'outpatient':
        await exportOutpatientReport(workbook);
        break;
      case 'department-load':
        await exportDepartmentLoadReport(workbook);
        break;
      case 'cancel-rate':
        await exportCancelRateReport(workbook);
        break;
      case 'registration':
        await exportRegistrationReport(workbook);
        break;
      case 'referral':
        await exportReferralReport(workbook);
        break;
      case 'comprehensive':
        await exportComprehensiveReport(workbook);
        break;
    }

    // 添加汇总信息
    if (exportOptions.value.includes('summary')) {
      await addSummarySheet(workbook);
    }

    // 生成文件名
    const fileName = generateFileName();

    // 导出文件
    XLSX.writeFile(workbook, fileName);
    message.success('报表导出成功！');
  } catch (error: any) {
    console.error('导出报表失败:', error);
    message.error(error?.message || '导出报表失败');
  } finally {
    exporting.value = false;
  }
};

// 导出门诊量报表
const exportOutpatientReport = async (workbook: XLSX.WorkBook) => {
  const data = await getOutpatientStatistics(exportParams);
  const worksheetData = [
    ['日期', '二级科室', '科室', '门诊量', '总门诊量', '对比历史', '增长率(%)'],
    ...data.map((item: any) => [
      item.date,
      getLevel2DeptName(item.deptId, item.deptName || '-'),
      getParentDeptName(item.deptId, undefined, item.deptName || '-') || item.deptName || '-',
      item.visitCount || 0,
      item.totalVisitCount || 0,
      item.compareVisitCount || '-',
      item.growthRate ? `${item.growthRate.toFixed(2)}%` : '-',
    ]),
  ];

  const ws = XLSX.utils.aoa_to_sheet(worksheetData);
  XLSX.utils.book_append_sheet(workbook, ws, '门诊量统计');
};

// 导出科室负荷报表
const exportDepartmentLoadReport = async (workbook: XLSX.WorkBook) => {
  const data = await getDepartmentLoadStatistics(exportParams);
  const worksheetData = [
    ['科室', '二级科室', '医生', '出诊时长(小时)', '号源使用率(%)'],
    ...data.map((item: any) => [
      item.parentDeptName || item.parent_dept_name || getParentDeptName(item.deptId, undefined, item.deptName || '-') || '-',
      item.deptName || getLevel2DeptName(item.deptId, item.deptName || '-'),
      item.doctorName || '-',
      item.visitDurationHours ? item.visitDurationHours.toFixed(1) : 0,
      item.quotaUsageRate ? `${item.quotaUsageRate.toFixed(2)}%` : '0%',
    ]),
  ];

  const ws = XLSX.utils.aoa_to_sheet(worksheetData);
  XLSX.utils.book_append_sheet(workbook, ws, '科室负荷统计');
};

// 导出退号率报表
const exportCancelRateReport = async (workbook: XLSX.WorkBook) => {
  const data = await getCancelRateStatistics(exportParams);
  const worksheetData = [
    ['科室', '二级科室', '医生', '号别', '总挂号数', '退号数', '退号率(%)'],
    ...data.map((item: any) => [
      item.parentDeptName || item.parent_dept_name || getParentDeptName(item.deptId, undefined, item.deptName || '-') || '-',
      item.deptName || getLevel2DeptName(item.deptId, item.deptName || '-'),
      item.doctorName ||
        item.doctor_name ||
        item.doctorFullName ||
        item.doctor_full_name ||
        item.doctorRealName ||
        item.doctor_real_name ||
        item.doctor ||
        (item.doctorId ? `医生${item.doctorId}` : '-') ||
        '-',
      item.typeName ||
        item.type_name ||
        item.typeLabel ||
        item.type_label ||
        item.type ||
        item.registerTypeName ||
        item.register_type_name ||
        (item.typeId ? `号别${item.typeId}` : '-') ||
        '-',
      item.totalCount || item.totalRegistration || 0,
      item.cancelCount || 0,
      item.cancelRate ? `${item.cancelRate.toFixed(2)}%` : '0%',
    ]),
  ];

  const ws = XLSX.utils.aoa_to_sheet(worksheetData);
  XLSX.utils.book_append_sheet(workbook, ws, '退号率统计');
};

// 导出挂号量报表
const exportRegistrationReport = async (workbook: XLSX.WorkBook) => {
  const data = await getRegistrationStatistics(exportParams);
  const worksheetData = [
    ['日期', '科室', '二级科室', '医生', '号别', '挂号量', '总挂号量', '对比历史', '增长率(%)'],
    ...data.map((item: any) => [
      item.date,
      getParentDeptName(item.deptId, undefined, item.deptName || item.dept_name || '-') ||
        item.deptName ||
        item.dept_name ||
        '-',
      getLevel2DeptName(item.deptId, item.deptName || item.dept_name || '-'),
      item.doctorName || item.doctor_name || '-',
      item.typeName || item.type_name || '-',
      item.typeRegistration || 0,
      item.totalRegistration || 0,
      item.compareRegistration || '-',
      item.growthRate != null ? `${Number(item.growthRate).toFixed(2)}%` : '-',
    ]),
  ];

  const ws = XLSX.utils.aoa_to_sheet(worksheetData);
  XLSX.utils.book_append_sheet(workbook, ws, '挂号量统计');
};

// 导出转诊情况报表
const exportReferralReport = async (workbook: XLSX.WorkBook) => {
  const data = await getReferralStatistics(exportParams);
  const worksheetData = [
    ['日期', '二级科室', '科室', '转诊类型', '申请数量', '已批准', '已拒绝', '已取消', '已完成', '总数量', '批准率(%)', '完成率(%)'],
    ...data.map((item: any) => [
      item.date,
      getLevel2DeptName(item.deptId, item.deptName || '-'),
      item.deptName || '-',
      item.targetTypeName || '-',
      item.applicationCount || 0,
      item.approvedCount || 0,
      item.rejectedCount || 0,
      item.cancelledCount || 0,
      item.completedCount || 0,
      item.totalCount || 0,
      item.approvalRate ? `${item.approvalRate.toFixed(2)}%` : '0%',
      item.completionRate ? `${item.completionRate.toFixed(2)}%` : '0%',
    ]),
  ];

  const ws = XLSX.utils.aoa_to_sheet(worksheetData);
  XLSX.utils.book_append_sheet(workbook, ws, '转诊情况统计');
};

// 导出综合报表
const exportComprehensiveReport = async (workbook: XLSX.WorkBook) => {
  await exportOutpatientReport(workbook);
  await exportDepartmentLoadReport(workbook);
  await exportCancelRateReport(workbook);
  await exportRegistrationReport(workbook);
  await exportReferralReport(workbook);
};

// 添加汇总信息
const addSummarySheet = async (workbook: XLSX.WorkBook) => {
  const summary = await getStatisticsSummary(exportParams);
  const worksheetData = [
    ['统计项', '数值'],
    ['总门诊量', summary.totalVisitCount || 0],
    ['平均科室负荷', summary.avgDeptLoad ? `${summary.avgDeptLoad.toFixed(2)}%` : '0%'],
    ['平均退号率', summary.avgCancelRate ? `${summary.avgCancelRate.toFixed(2)}%` : '0%'],
    ['总挂号量', summary.totalRegistration || 0],
    ['总退号率', summary.avgCancelRate ? `${summary.avgCancelRate.toFixed(2)}%` : '0%'],
    ['总收入(挂号费)', summary.totalIncome != null ? summary.totalIncome : 0],
    ['在岗医护人数', summary.activeStaffCount != null ? summary.activeStaffCount : 0],
    ['统计周期', exportParams.periodType === 'day' ? '按日' : exportParams.periodType === 'week' ? '按周' : '按月'],
    ['开始日期', exportParams.startDate],
    ['结束日期', exportParams.endDate],
  ];

  const ws = XLSX.utils.aoa_to_sheet(worksheetData);
  XLSX.utils.book_append_sheet(workbook, ws, '汇总信息');
};

// 生成文件名
const generateFileName = () => {
  const typeMap: Record<string, string> = {
    outpatient: '门诊量',
    'department-load': '科室负荷',
    'cancel-rate': '退号率',
    registration: '挂号量',
    referral: '转诊情况',
    comprehensive: '综合',
  };

  const typeName = typeMap[exportParams.reportType] || '报表';
  const dateStr = `${exportParams.startDate}_${exportParams.endDate}`;
  return `${typeName}报表_${dateStr}.xlsx`;
};

// 预览数据
const handlePreview = async () => {
  if (!exportParams.reportType) {
    message.warning('请选择报表类型');
    return;
  }

  previewLoading.value = true;
  try {
    let data: any[] = [];
    let columns: any[] = [];
    const withLevel2 = (list: any[]) =>
      list.map((item) => ({
        ...item,
        level2DeptName: item.deptName || item.dept_name || getLevel2DeptName(item.deptId, item.deptName || '-'),
        deptName:
          item.parentDeptName ||
          item.parent_dept_name ||
          getParentDeptName(item.deptId, undefined, item.deptName || item.dept_name || '-') ||
          '-',
      }));
    const fillDoctorDept = (list: any[]) =>
      list.map((item) => {
        const doctorName =
          item.doctorName ||
          item.doctor_name ||
          item.doctorFullName ||
          item.doctor_full_name ||
          item.doctorRealName ||
          item.doctor_real_name ||
          item.doctor ||
          (item.doctorId ? `医生${item.doctorId}` : '-');
        const deptName =
          item.parentDeptName ||
          item.parent_dept_name ||
          getParentDeptName(item.deptId, undefined, item.deptName || item.dept_name || '-') ||
          '-';
        const level2DeptName = item.deptName || item.dept_name || getLevel2DeptName(item.deptId, deptName);
        const typeName =
          item.typeName ||
          item.type_name ||
          item.typeLabel ||
          item.type_label ||
          item.type ||
          item.registerTypeName ||
          item.register_type_name ||
          (item.typeId ? `号别${item.typeId}` : '-');
        return {
          ...item,
          level2DeptName,
          deptName,
          doctorName,
          typeName,
          totalCount: item.totalCount ?? item.totalRegistration ?? 0,
          cancelCount: item.cancelCount ?? 0,
          cancelRate: item.cancelRate ?? 0,
          visitDurationHours: item.visitDurationHours ?? 0,
          quotaUsageRate: item.quotaUsageRate ?? 0,
        };
      });

    switch (exportParams.reportType) {
      case 'outpatient': {
        data = await getOutpatientStatistics(exportParams);
        columns = [
          { title: '日期', dataIndex: 'date', key: 'date', width: 120, fixed: 'left' },
          { title: '科室', dataIndex: 'deptName', key: 'deptName', width: 150 },
          { title: '二级科室', dataIndex: 'level2DeptName', key: 'level2DeptName', width: 150 },
          { title: '门诊量', dataIndex: 'visitCount', key: 'visitCount', align: 'right', width: 100 },
          { title: '总门诊量', dataIndex: 'totalVisitCount', key: 'totalVisitCount', align: 'right', width: 120 },
        ];
        data = withLevel2(data).map((item: any) => ({
          ...item,
          deptName: item.deptName || '-',
          visitCount: item.visitCount ?? 0,
          totalVisitCount: item.totalVisitCount ?? 0,
          compareVisitCount: item.compareVisitCount ?? '-',
          growthRate: item.growthRate ?? null,
        }));
        break;
      }
      case 'department-load': {
        data = await getDepartmentLoadStatistics(exportParams);
        columns = [
          { title: '科室', dataIndex: 'deptName', key: 'deptName', width: 150 },
          { title: '二级科室', dataIndex: 'level2DeptName', key: 'level2DeptName', width: 150 },
          { title: '医生', dataIndex: 'doctorName', key: 'doctorName' },
          {
            title: '出诊时长(小时)',
            dataIndex: 'visitDurationHours',
            key: 'visitDurationHours',
            align: 'right',
            customRender: ({ text }: { text: number }) => `${text ? text.toFixed(1) : 0}`,
          },
          {
            title: '号源使用率(%)',
            dataIndex: 'quotaUsageRate',
            key: 'quotaUsageRate',
            align: 'right',
            customRender: ({ text }: { text: number }) => `${text?.toFixed(2) || 0}%`,
          },
        ];
        data = fillDoctorDept(data);
        break;
      }
      case 'cancel-rate': {
        data = await getCancelRateStatistics(exportParams);
        columns = [
          { title: '科室', dataIndex: 'deptName', key: 'deptName', width: 150 },
          { title: '二级科室', dataIndex: 'level2DeptName', key: 'level2DeptName', width: 150 },
          { title: '医生', dataIndex: 'doctorName', key: 'doctorName' },
          { title: '号别', dataIndex: 'typeName', key: 'typeName' },
          { title: '总挂号数', dataIndex: 'totalCount', key: 'totalCount', align: 'right' },
          { title: '退号数', dataIndex: 'cancelCount', key: 'cancelCount', align: 'right' },
          {
            title: '退号率(%)',
            dataIndex: 'cancelRate',
            key: 'cancelRate',
            align: 'right',
            customRender: ({ text }: { text: number }) => `${text?.toFixed(2) || 0}%`,
          },
        ];
        data = fillDoctorDept(data);
        break;
      }
      case 'registration': {
        data = await getRegistrationStatistics(exportParams);
        columns = [
          { title: '日期', dataIndex: 'date', key: 'date', width: 120, fixed: 'left' },
          { title: '科室', dataIndex: 'deptName', key: 'deptName', width: 150 },
          { title: '二级科室', dataIndex: 'level2DeptName', key: 'level2DeptName', width: 150 },
          { title: '医生', dataIndex: 'doctorName', key: 'doctorName', width: 120 },
          { title: '号别', dataIndex: 'typeName', key: 'typeName', width: 120 },
          { title: '挂号量', dataIndex: 'typeRegistration', key: 'typeRegistration', align: 'right', width: 100 },
          { title: '总挂号量', dataIndex: 'totalRegistration', key: 'totalRegistration', align: 'right', width: 120 },
        ];
        data = fillDoctorDept(data).map((item: any) => ({
          ...item,
          typeRegistration: item.typeRegistration ?? 0,
          totalRegistration: item.totalRegistration ?? 0,
          compareRegistration: item.compareRegistration ?? '-',
          growthRate: item.growthRate ?? null,
        }));
        break;
      }
      case 'referral': {
        data = await getReferralStatistics(exportParams);
        columns = [
          { title: '日期', dataIndex: 'date', key: 'date', width: 120, fixed: 'left' },
          { title: '科室', dataIndex: 'deptName', key: 'deptName', width: 150 },
          { title: '转诊类型', dataIndex: 'targetTypeName', key: 'targetTypeName', width: 100 },
          { title: '申请数量', dataIndex: 'applicationCount', key: 'applicationCount', align: 'right', width: 100 },
          { title: '已批准', dataIndex: 'approvedCount', key: 'approvedCount', align: 'right', width: 100 },
          { title: '已拒绝', dataIndex: 'rejectedCount', key: 'rejectedCount', align: 'right', width: 100 },
          { title: '已取消', dataIndex: 'cancelledCount', key: 'cancelledCount', align: 'right', width: 100 },
          { title: '已完成', dataIndex: 'completedCount', key: 'completedCount', align: 'right', width: 100 },
          { title: '总数量', dataIndex: 'totalCount', key: 'totalCount', align: 'right', width: 100 },
          { title: '批准率(%)', dataIndex: 'approvalRate', key: 'approvalRate', align: 'right', width: 100 },
          { title: '完成率(%)', dataIndex: 'completionRate', key: 'completionRate', align: 'right', width: 100 },
        ];
        break;
      }
      case 'comprehensive': {
        // 综合报表显示汇总统计信息
        const summary = await getStatisticsSummary(exportParams);
        data = [
          {
            key: '1',
            statType: '总门诊量',
            value: summary.totalVisitCount || 0,
            unit: '人次',
          },
          {
            key: '2',
            statType: '平均科室负荷',
            value: summary.avgDeptLoad ? summary.avgDeptLoad.toFixed(2) : '0.00',
            unit: '%',
          },
          {
            key: '3',
            statType: '平均退号率',
            value: summary.avgCancelRate ? summary.avgCancelRate.toFixed(2) : '0.00',
            unit: '%',
          },
          {
            key: '4',
            statType: '总挂号量',
            value: summary.totalRegistration || 0,
            unit: '人次',
          },
        ];
        columns = [
          { title: '统计项', dataIndex: 'statType', key: 'statType', width: 200 },
          { title: '数值', dataIndex: 'value', key: 'value', align: 'right', width: 150 },
          { title: '单位', dataIndex: 'unit', key: 'unit', width: 100 },
        ];
        break;
      }
    }

    previewData.value = Array.isArray(data) ? data : [];
    previewColumns.value = columns;
    message.success('预览数据加载成功');
  } catch (error: any) {
    console.error('预览数据失败:', error);
    message.error(error?.message || '预览数据失败');
  } finally {
    previewLoading.value = false;
  }
};

// 重置表单
const resetForm = () => {
  exportParams.reportType = 'outpatient';
  exportParams.periodType = 'day';
  exportParams.deptId = undefined;
  exportOptions.value = ['data', 'chart', 'summary'];
  initDateRange();
  previewData.value = [];
};

// 初始化
onMounted(async () => {
  initDateRange();
  await loadDepartments();
});
</script>

<style scoped lang="less">
.export-container {
  padding: 16px;
}

.export-card {
  margin-bottom: 16px;
}

.preview-card {
  margin-top: 16px;
  
  :deep(.ant-table-wrapper) {
    .ant-table {
      min-width: 100%;
    }
    
    .ant-table-container {
      overflow-x: auto;
    }
    
    .ant-table-thead > tr > th {
      background-color: #fafafa;
      font-weight: 600;
      white-space: nowrap;
    }
    
    .ant-table-tbody > tr > td {
      white-space: nowrap;
    }
    
    .ant-table-pagination {
      margin-top: 16px;
    }
  }
}

.mt-4 {
  margin-top: 16px;
}
</style>