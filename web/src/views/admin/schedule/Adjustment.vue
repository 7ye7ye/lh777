<template>
  <PageWrapper :title="'排班调整'">
    <div class="p-4">
      <a-alert
        v-if="pendingRequests.length > 0"
        message="有待处理的调班申请"
        :description="`当前有 ${pendingRequests.length} 个调班申请需要处理`"
        type="info"
        show-icon
        class="mb-4"
        closable
      />

      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="requests" tab="调班申请">
          <a-card :bordered="false" class="card-container">
            <template #title>
              <div class="card-title">
                <span>调班申请列表</span>
                <a-space>
                  <a-button size="small" @click="loadAdjustmentRequests">刷新</a-button>
                </a-space>
              </div>
            </template>

            <div class="table-container">
              <a-table
                :data-source="adjustmentRequests"
                :columns="requestColumns"
                row-key="adjustmentId"
                :pagination="{ pageSize: 10 }"
                class="compact-table"
                :scroll="{ x: 1000 }"
                size="small"
                bordered
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-tag :color="getStatusColor(record.status)" class="status-tag">
                      {{ getStatusText(record.status) }}
                    </a-tag>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space :size="4" wrap>
                      <a-button
                        v-if="record.status === 1"
                        type="primary"
                        size="small"
                        @click="handleApprove(record)"
                      >
                        同意
                      </a-button>
                      <a-button
                        v-if="record.status === 1"
                        danger
                        size="small"
                        @click="showRejectModal(record)"
                      >
                        拒绝
                      </a-button>
                      <a-button
                        v-if="record.status !== 1"
                        type="link"
                        size="small"
                        @click="handleViewDetail(record)"
                      >
                        查看
                      </a-button>
                    </a-space>
                  </template>
                  <template v-else-if="column.key === 'applyTime'">
                    <div class="time-text">
                      {{ formatTime(record.applyTime) }}
                    </div>
                  </template>
                  <template v-else-if="column.key === 'currentSchedule'">
                    <div class="schedule-info">
                      <div class="schedule-item"><span class="label">日期:</span> {{ record.originalDate }}</div>
                      <div class="schedule-item"><span class="label">时段:</span> {{ slotLabel(record.originalTimeSlot) }}</div>
                      <div class="schedule-item"><span class="label">诊室:</span> {{ record.currentRoom || '-' }}</div>
                    </div>
                  </template>
                  <template v-else-if="column.key === 'targetSchedule'">
                    <div v-if="record.targetDate" class="schedule-info">
                      <div class="schedule-item"><span class="label">日期:</span> {{ record.targetDate }}</div>
                      <div class="schedule-item"><span class="label">时段:</span> {{ slotLabel(record.targetTimeSlot) }}</div>
                    </div>
                    <span v-else class="empty-text">-</span>
                  </template>
                  <template v-else-if="column.key === 'reason'">
                    <div class="reason-text" :title="record.reason">
                      {{ record.reason }}
                    </div>
                  </template>
                </template>
              </a-table>
            </div>
          </a-card>
        </a-tab-pane>

        <a-tab-pane key="today" tab="今日排班">
          <div class="today-schedule-container">
            <a-row :gutter="16" class="stats-row">
              <a-col :xs="24" :sm="12" :md="6" class="stats-col">
                <a-card size="small" class="stats-card">
                  <a-statistic title="今日排班总数" :value="stats.total" />
                </a-card>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6" class="stats-col">
                <a-card size="small" class="stats-card">
                  <a-statistic title="上午" :value="stats.am" value-style="color: #52c41a" />
                </a-card>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6" class="stats-col">
                <a-card size="small" class="stats-card">
                  <a-statistic title="下午" :value="stats.pm" value-style="color: #1677ff" />
                </a-card>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6" class="stats-col">
                <a-card size="small" class="stats-card">
                  <a-statistic title="晚上" :value="stats.night" value-style="color: #722ed1" />
                </a-card>
              </a-col>
            </a-row>

            <a-card :bordered="false" class="card-container">
              <template #title>
                <div class="card-title">
                  <span>筛选条件</span>
                  <a-space>
                    <a-button size="small" @click="handleSearch">刷新</a-button>
                  </a-space>
                </div>
              </template>

              <div class="filter-toolbar">
                <a-row :gutter="[16, 16]" class="filter-row">
                  <a-col :xs="24" :sm="12" :md="6">
                    <div class="filter-field">
                      <span class="filter-label">日期：</span>
                      <a-date-picker
                        v-model:value="filters.date"
                        class="filter-control"
                        placeholder="选择日期"
                        size="small"
                      />
                    </div>
                  </a-col>
                  <a-col :xs="24" :sm="12" :md="6">
                    <div class="filter-field">
                      <span class="filter-label">科室：</span>
                      <a-select
                        v-model:value="filters.deptId"
                        :options="deptOptions"
                        class="filter-control"
                        allow-clear
                        show-search
                        placeholder="选择科室"
                        size="small"
                      />
                    </div>
                  </a-col>
                  <a-col :xs="24" :sm="12" :md="6">
                    <div class="filter-field">
                      <span class="filter-label">医生：</span>
                      <a-select
                        v-model:value="filters.doctorId"
                        :options="doctorOptions"
                        class="filter-control"
                        allow-clear
                        show-search
                        placeholder="选择医生"
                        size="small"
                      />
                    </div>
                  </a-col>
                  <a-col :xs="24" :sm="12" :md="6">
                    <div class="filter-field">
                      <span class="filter-label">时段：</span>
                      <a-select
                        v-model:value="filters.timeSlot"
                        :options="timeOptions"
                        class="filter-control"
                        allow-clear
                        placeholder="选择时段"
                        size="small"
                      />
                    </div>
                  </a-col>
                </a-row>

                <div class="filter-actions">
                  <a-space :size="12">
                    <a-button type="primary" size="small" @click="handleSearch">查询</a-button>
                    <a-button size="small" @click="handleViewCalendar">查看月排班</a-button>
                  </a-space>
                </div>
              </div>

              <a-divider class="compact-divider" />

              <div class="table-container">
                <a-table
                  :data-source="rows"
                  :columns="columns"
                  row-key="scheduleId"
                  bordered
                  :pagination="{ pageSize: 10 }"
                  class="compact-table"
                  :scroll="{ x: 600 }"
                  size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'timeSlot'">
                      <span :class="['slot-tag', slotClass(record.timeSlot)]">
                        {{ slotLabel(record.timeSlot) }}
                      </span>
                    </template>
                    <template v-else-if="column.key === 'status'">
                      <span :class="record.status === 1 ? 'status-ok' : 'status-ban'">
                        {{ record.status === 1 ? '有效' : '停用' }}
                      </span>
                    </template>
                    <template v-else-if="column.key === 'scheduleDate'">
                      <div class="date-text">
                        {{ record.scheduleDate }}
                      </div>
                    </template>
                    <template v-else-if="column.key === 'doctorName' || column.key === 'deptName'">
                      <div class="text-ellipsis" :title="record[column.key]">
                        {{ record[column.key] }}
                      </div>
                    </template>
                  </template>
                </a-table>
              </div>
            </a-card>
          </div>
        </a-tab-pane>
      </a-tabs>

      <!-- 拒绝调班弹窗 -->
      <a-modal
        v-model:visible="rejectModal.visible"
        title="拒绝调班申请"
        @ok="handleReject"
        @cancel="handleCancelReject"
        :confirmLoading="rejectModal.loading"
        okText="确认拒绝"
        cancelText="取消"
        width="400px"
      >
        <a-form layout="vertical">
          <a-form-item label="拒绝原因">
            <a-textarea
              v-model:value="rejectModal.reason"
              placeholder="请输入拒绝原因"
              :rows="4"
              :maxlength="200"
              show-count
            />
          </a-form-item>
        </a-form>
      </a-modal>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, onMounted, computed, watch, h } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import { getDepartmentList } from '/@/api/hospital/department';
import { listSchedulesByDate, type TodayScheduleItem } from '/@/api/hospital/scheduleView';
import { useGo } from '/@/hooks/web/usePage';
import { getDoctorList } from '/@/api/hospital/doctor';
import { getAdjustmentList, approveAdjustment, type AdjustmentRecord, type AdjustmentApprovalRequest } from '/@/api/hospital/adjustment';

// 修正接口定义，与实际的 AdjustmentRecord 保持一致
export default defineComponent({
  name: 'AdminScheduleAdjustment',
  components: { PageWrapper },
  setup() {
    const go = useGo();
    const activeTab = ref('today');

    // 筛选条件
    const filters = reactive({
      date: dayjs(),
      deptId: undefined as number | undefined,
      doctorId: undefined as number | undefined,
      timeSlot: undefined as number | undefined,
    });

    // 调班申请相关
    const adjustmentRequests = ref<AdjustmentRecord[]>([]);

    // 拒绝调班弹窗
    const rejectModal = reactive({
      visible: false,
      loading: false,
      reason: '',
      currentRequest: null as AdjustmentRecord | null
    });

    const timeOptions = [
      { label: '上午', value: 1 },
      { label: '下午', value: 2 },
      { label: '晚上', value: 3 },
    ];
    const deptOptions = ref<{ label: string; value: number }[]>([]);
    const doctorOptions = ref<{ label: string; value: number }[]>([]);
    const rows = ref<TodayScheduleItem[]>([]);

    // 调班申请表格列 - 调整列宽确保不超出
    const requestColumns = [
      {
        title: '申请医生',
        dataIndex: 'doctorName',
        key: 'doctorName',
        width: 80,
        ellipsis: true
      },
      {
        title: '科室',
        dataIndex: 'deptName',
        key: 'deptName',
        width: 80,
        ellipsis: true
      },
      {
        title: '当前排班',
        key: 'currentSchedule',
        width: 120
      },
      {
        title: '目标排班',
        key: 'targetSchedule',
        width: 120
      },
      {
        title: '申请原因',
        dataIndex: 'reason',
        key: 'reason',
        width: 120,
        ellipsis: true
      },
      {
        title: '申请时间',
        key: 'applyTime',
        width: 100
      },
      {
        title: '状态',
        key: 'status',
        width: 70
      },
      {
        title: '操作',
        key: 'action',
        width: 120,
        fixed: 'right' as const
      },
    ];

    // 今日排班表格列
    const columns = [
      {
        title: '日期',
        dataIndex: 'scheduleDate',
        key: 'scheduleDate',
        width: 90,
        ellipsis: true
      },
      {
        title: '医生',
        dataIndex: 'doctorName',
        key: 'doctorName',
        width: 80,
        ellipsis: true
      },
      {
        title: '科室',
        dataIndex: 'deptName',
        key: 'deptName',
        width: 100,
        ellipsis: true
      },
      {
        title: '时段',
        dataIndex: 'timeSlot',
        key: 'timeSlot',
        width: 60,
      },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 60,
      },
    ];

    // 计算属性
    const pendingRequests = computed(() => {
      return adjustmentRequests.value.filter((req) => req.status === 1);
    });

    const stats = computed(() => {
      const total = rows.value.length;
      const am = rows.value.filter((r) => r.timeSlot === 1).length;
      const pm = rows.value.filter((r) => r.timeSlot === 2).length;
      const night = rows.value.filter((r) => r.timeSlot === 3).length;
      return { total, am, pm, night };
    });

    // 方法
    function slotLabel(v: number) {
      const map: any = { 1: '上午', 2: '下午', 3: '晚上' };
      return map[v] || v;
    }

    function slotClass(v: number) {
      const map: any = { 1: 'slot-am', 2: 'slot-pm', 3: 'slot-night' };
      return map[v] || '';
    }

    function getStatusColor(status: number) {
      const colors: any = { 1: 'blue', 2: 'green', 3: 'red', 4: 'orange' };
      return colors[status] || 'default';
    }

    function getStatusText(status: number) {
      const texts: any = { 1: '待审批', 2: '已同意', 3: '已拒绝', 4: '已撤销' };
      return texts[status] || '未知';
    }

    function formatTime(timeStr: string) {
      return dayjs(timeStr).format('MM-DD HH:mm');
    }

    // 加载调班申请数据
    async function loadAdjustmentRequests() {
      try {
        const { records } = await getAdjustmentList({ status: 1, current: 1, size: 50 });
        adjustmentRequests.value = Array.isArray(records) ? records : [];
      } catch (error) {
        message.error('加载调班申请失败');
      }
    }

    // 同意调班申请
    async function handleApprove(request: AdjustmentRecord) {
      Modal.confirm({
        title: '确认同意',
        content: `确定同意该调班申请吗？系统将自动创建新的排班记录。`,
        onOk: async () => {
          try {
            // 根据 AdjustmentApprovalRequest 的实际定义传递参数
            const approvalRequest: AdjustmentApprovalRequest = {
              adjustmentId: request.adjustmentId,
              status: 2
            };

            await approveAdjustment(approvalRequest);
            message.success('已同意调班申请');
            await loadAdjustmentRequests();
          } catch (error: any) {
            console.error('同意调班失败:', error);
            // 如果还是因为 max_quota 错误，建议修改后端接口或数据库表结构
            if (error.message?.includes('max_quota')) {
              message.error('操作失败：请检查排班表结构，max_quota 字段需要默认值');
            } else {
              message.error('操作失败: ' + (error.message || '未知错误'));
            }
          }
        }
      });
    }

    // 显示拒绝弹窗
    function showRejectModal(request: AdjustmentRecord) {
      rejectModal.currentRequest = request;
      rejectModal.reason = '';
      rejectModal.visible = true;
    }

    // 拒绝调班申请
    async function handleReject() {
      if (!rejectModal.currentRequest) return;

      if (!rejectModal.reason.trim()) {
        message.warning('请输入拒绝原因');
        return;
      }

      rejectModal.loading = true;
      try {
        const approvalRequest: AdjustmentApprovalRequest = {
          adjustmentId: rejectModal.currentRequest.adjustmentId,
          status: 3,
          rejectReason: rejectModal.reason
        };

        await approveAdjustment(approvalRequest);
        message.success('已拒绝调班申请');
        await loadAdjustmentRequests();
        rejectModal.visible = false;
        rejectModal.reason = '';
      } catch (error) {
        message.error('操作失败');
      } finally {
        rejectModal.loading = false;
      }
    }

    // 取消拒绝
    function handleCancelReject() {
      rejectModal.visible = false;
      rejectModal.reason = '';
      rejectModal.currentRequest = null;
    }

    // 使用 h 函数渲染 Modal 内容
    function handleViewDetail(request: AdjustmentRecord) {
      Modal.info({
        title: '调班申请详情',
        width: 500,
        content: h('div', { class: 'detail-content' }, [
          h('p', null, [h('strong', null, '申请医生：'), request.doctorName || request.doctorId]),
          h('p', null, [h('strong', null, '当前排班：'), `${request.originalDate || ''} ${slotLabel(request.originalTimeSlot || 0)}`]),
          request.targetDate
            ? h('p', null, [h('strong', null, '目标排班：'), `${request.targetDate} ${slotLabel(request.targetTimeSlot)}`])
            : null,
          h('p', null, [h('strong', null, '申请原因：'), request.reason]),
          h('p', null, [h('strong', null, '申请时间：'), formatTime(request.applyTime)]),
          request.approveTime
            ? h('p', null, [h('strong', null, '处理时间：'), formatTime(request.approveTime)])
            : null,
          request.rejectReason
            ? h('p', null, [h('strong', null, '处理原因：'), request.rejectReason])
            : null,
        ]),
      });
    }

    // 标签页切换
    function handleTabChange(key: string) {
      if (key === 'requests') {
        loadAdjustmentRequests();
      }
    }

    // 原有方法
    async function loadDeptOptions() {
      const list = await getDepartmentList();
      deptOptions.value = (list || []).map((d: any) => ({ label: d.deptName, value: d.deptId }));
    }

    async function loadDoctorOptions() {
      try {
        const response = await getDoctorList(filters.deptId ? { deptId: filters.deptId } : undefined);
        // 处理API返回的数据结构：可能是 { records: [...], total: ... } 或直接是数组
        let list: any[] = [];
        if (response) {
          if (Array.isArray(response)) {
            list = response;
          } else if (response.records && Array.isArray(response.records)) {
            list = response.records;
          } else if (response.list && Array.isArray(response.list)) {
            list = response.list;
          } else if (response.data && Array.isArray(response.data)) {
            list = response.data;
          }
        }
        doctorOptions.value = list.map((d: any) => ({ 
          label: d.doctorName || d.name || '', 
          value: d.doctorId || d.id || 0 
        }));
      } catch (error: any) {
        console.error('加载医生列表失败:', error);
        message.error('加载医生列表失败：' + (error?.message || '未知错误'));
        doctorOptions.value = [];
      }
    }

    async function loadList() {
      const data = await listSchedulesByDate({
        date: filters.date ? filters.date.format('YYYY-MM-DD') : undefined,
        deptId: filters.deptId,
        doctorId: filters.doctorId,
        timeSlot: filters.timeSlot,
      });
      rows.value = Array.isArray(data) ? data : [];
    }

    function handleSearch() {
      loadList();
    }

    function handleViewCalendar() {
      if (filters.doctorId) {
        go(`/admin/schedule-calendar?type=doctor&doctorId=${filters.doctorId}`);
        return;
      }
      if (filters.deptId) {
        go(`/admin/schedule-calendar?type=dept&deptId=${filters.deptId}`);
        return;
      }
      message.warning('请先选择医生或科室后再查看月排班');
    }

    onMounted(async () => {
      await loadDeptOptions();
      await loadDoctorOptions();
      await loadList();
    });

    watch(() => filters.deptId, async () => {
      filters.doctorId = undefined;
      await loadDoctorOptions();
    });

    return {
      activeTab,
      filters,
      timeOptions,
      deptOptions,
      doctorOptions,
      rows,
      columns,
      adjustmentRequests,
      requestColumns,
      rejectModal,
      pendingRequests,
      stats,
      loadAdjustmentRequests,
      handleSearch,
      handleViewCalendar,
      handleApprove,
      handleReject,
      handleCancelReject,
      showRejectModal,
      handleViewDetail,
      handleTabChange,
      slotLabel,
      slotClass,
      getStatusColor,
      getStatusText,
      formatTime
    };
  },
});
</script>

<style scoped>
.p-4 {
  padding: 16px;
  box-sizing: border-box;
  max-width: 100%;
  overflow: hidden;
}

.mb-4 {
  margin-bottom: 16px;
}

.card-container {
  width: 100%;
  box-sizing: border-box;
  max-width: 100%;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.today-schedule-container {
  width: 100%;
  box-sizing: border-box;
  max-width: 100%;
}

.stats-row {
  margin-bottom: 16px;
}

.stats-col {
  margin-bottom: 8px;
}

.stats-card {
  height: 100%;
}

.filter-toolbar {
  width: 100%;
  box-sizing: border-box;
  max-width: 100%;
}

.filter-row {
  margin-bottom: 16px;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-label {
  color: rgba(0, 0, 0, 0.88);
  font-size: 14px;
  white-space: nowrap;
  margin-bottom: 4px;
}

.filter-control {
  width: 100%;
}

.filter-actions {
  display: flex;
  justify-content: flex-start;
}

.compact-divider {
  margin: 16px 0;
}

/* 表格容器关键修复 */
.table-container {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}

.table-container::-webkit-scrollbar {
  height: 8px;
}

.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.table-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.table-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.compact-table {
  min-width: 100%;
  table-layout: fixed;
}

.compact-table :deep(.ant-table) {
  font-size: 12px;
}

.compact-table :deep(.ant-table-thead > tr > th) {
  padding: 6px 4px;
  background-color: #fafafa;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.compact-table :deep(.ant-table-tbody > tr > td) {
  padding: 6px 4px;
  white-space: normal;
  word-break: break-word;
}

.schedule-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 11px;
}

.schedule-item {
  line-height: 1.2;
  color: #666;
  display: flex;
  align-items: center;
}

.schedule-item .label {
  color: #999;
  margin-right: 2px;
  min-width: 28px;
}

.empty-text {
  color: #999;
  font-style: italic;
  font-size: 11px;
}

.reason-text {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11px;
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11px;
}

.date-text {
  font-size: 11px;
}

.time-text {
  font-size: 11px;
  white-space: nowrap;
}

.status-tag {
  font-size: 11px;
  padding: 0 4px;
  line-height: 18px;
}

.slot-tag {
  display: inline-block;
  padding: 1px 4px;
  border-radius: 6px;
  color: #fff;
  font-size: 10px;
  white-space: nowrap;
}

.slot-am { background: #52c41a; }
.slot-pm { background: #1677ff; }
.slot-night { background: #722ed1; }

.status-ok {
  color: #52c41a;
  font-weight: 500;
  font-size: 11px;
}

.status-ban {
  color: #ff4d4f;
  font-weight: 500;
  font-size: 11px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .p-4 {
    padding: 8px;
  }

  .card-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .filter-field {
    flex-direction: column;
  }

  .filter-label {
    margin-bottom: 4px;
  }

  .table-container {
    margin: 0 -8px;
    width: calc(100% + 16px);
  }
}

@media (max-width: 576px) {
  .stats-col {
    margin-bottom: 8px;
  }

  .filter-actions {
    justify-content: center;
  }

  .filter-actions .ant-space {
    width: 100%;
    justify-content: center;
  }

  .compact-table :deep(.ant-table-thead > tr > th),
  .compact-table :deep(.ant-table-tbody > tr > td) {
    padding: 4px 2px;
    font-size: 11px;
  }
}

/* 详情弹窗样式 */
.detail-content p {
  margin-bottom: 8px;
  line-height: 1.5;
  font-size: 14px;
}

.detail-content strong {
  color: #333;
  margin-right: 8px;
  min-width: 80px;
  display: inline-block;
}
</style>
