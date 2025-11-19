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
          <a-card :bordered="false">
            <template #title>
              <div class="card-title">
                <span>调班申请列表</span>
                <a-space>
                  <a-button size="small" @click="loadAdjustmentRequests">刷新</a-button>
                </a-space>
              </div>
            </template>

            <a-table
              :data-source="adjustmentRequests"
              :columns="requestColumns"
              row-key="requestId"
              :pagination="{ pageSize: 10 }"
              class="mt-4"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'status'">
                  <a-tag :color="getStatusColor(record.status)">
                    {{ getStatusText(record.status) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space>
                    <a-button
                      v-if="record.status === 0"
                      type="primary"
                      size="small"
                      @click="handleApprove(record)"
                    >
                      同意
                    </a-button>
                    <a-button
                      v-if="record.status === 0"
                      danger
                      size="small"
                      @click="showRejectModal(record)"
                    >
                      拒绝
                    </a-button>
                    <a-button
                      v-if="record.status !== 0"
                      type="link"
                      size="small"
                      @click="handleViewDetail(record)"
                    >
                      查看详情
                    </a-button>
                  </a-space>
                </template>
                <template v-else-if="column.key === 'requestTime'">
                  {{ formatTime(record.requestTime) }}
                </template>
                <template v-else-if="column.key === 'currentSchedule'">
                  <div>
                    <div>日期: {{ record.currentDate }}</div>
                    <div>时段: {{ slotLabel(record.currentTimeSlot) }}</div>
                    <div>诊室: {{ record.currentRoom }}</div>
                  </div>
                </template>
                <template v-else-if="column.key === 'targetSchedule'">
                  <div v-if="record.targetDate">
                    <div>日期: {{ record.targetDate }}</div>
                    <div>时段: {{ slotLabel(record.targetTimeSlot) }}</div>
                  </div>
                  <span v-else>-</span>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-tab-pane>

        <a-tab-pane key="today" tab="今日排班">
          <div class="mb-2">
            <a-row :gutter="16" class="mb-2">
              <a-col :xs="24" :md="6">
                <a-card size="small">
                  <a-statistic title="今日排班总数" :value="stats.total" />
                </a-card>
              </a-col>
              <a-col :xs="24" :md="6">
                <a-card size="small">
                  <a-statistic title="上午" :value="stats.am" value-style="color: #52c41a" />
                </a-card>
              </a-col>
              <a-col :xs="24" :md="6">
                <a-card size="small">
                  <a-statistic title="下午" :value="stats.pm" value-style="color: #1677ff" />
                </a-card>
              </a-col>
              <a-col :xs="24" :md="6">
                <a-card size="small">
                  <a-statistic title="晚上" :value="stats.night" value-style="color: #722ed1" />
                </a-card>
              </a-col>
            </a-row>

            <a-card :bordered="false">
              <template #title>
                <div class="card-title">
                  <span>筛选条件</span>
                  <a-space>
                    <a-button size="small" @click="handleSearch">刷新</a-button>
                  </a-space>
                </div>
              </template>

              <div class="toolbar">
                <div class="field">
                  <span class="field-label">日期：</span>
                  <a-date-picker v-model:value="filters.date" style="width: 180px" />
                </div>
                <div class="field">
                  <span class="field-label">科室：</span>
                  <a-select v-model:value="filters.deptId" :options="deptOptions" style="width: 220px" allow-clear show-search placeholder="选择科室" />
                </div>
                <div class="field">
                  <span class="field-label">医生：</span>
                  <a-select
                    v-model:value="filters.doctorId"
                    :options="doctorOptions"
                    style="width: 220px"
                    allow-clear
                    show-search
                    placeholder="选择医生"
                  />
                </div>
                <div class="field">
                  <span class="field-label">关键词：</span>
                  <a-input v-model:value="filters.keyword" style="width: 220px" placeholder="输入科室或医生姓名" allow-clear />
                </div>
                <div class="field">
                  <span class="field-label">时段：</span>
                  <a-select
                    v-model:value="filters.timeSlot"
                    :options="timeOptions"
                    style="width: 160px"
                    allow-clear
                    placeholder="选择时段"
                  />
                </div>
                <a-space>
                  <a-button type="primary" @click="handleSearch">查询</a-button>
                  <a-button @click="handleViewCalendar">查看月排班</a-button>
                </a-space>
              </div>

              <a-divider class="mt-2" />

              <a-table
                :data-source="rows"
                :columns="columns"
                row-key="scheduleId"
                bordered
                :pagination="{ pageSize: 10 }"
                class="mt-4"
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
                </template>
              </a-table>
            </a-card>
          </div>
        </a-tab-pane>
      </a-tabs>

      <a-modal
        v-model:visible="rejectModal.visible"
        title="拒绝调班申请"
        @ok="handleReject"
        @cancel="handleCancelReject"
        :confirmLoading="rejectModal.loading"
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
import { defineComponent, reactive, ref, onMounted, computed, watch, h } from 'vue'; // **【已修改】引入 h**
import { PageWrapper } from '/@/components/Page';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import { getDepartmentList } from '/@/api/hospital/department';
import { listSchedulesByDate, type TodayScheduleItem } from '/@/api/hospital/scheduleView';
import { useGo } from '/@/hooks/web/usePage';
import { getDoctorList } from '/@/api/hospital/doctor';

// 模拟调班申请数据接口
interface AdjustmentRequest {
  requestId: string;
  doctorId: number;
  doctorName: string;
  deptId: number;
  deptName: string;
  currentDate: string;
  currentTimeSlot: number;
  currentRoom: string;
  targetDate?: string;
  targetTimeSlot?: number;
  targetRoom?: string;
  reason: string;
  status: number; // 0: 待处理, 1: 已同意, 2: 已拒绝
  requestTime: string;
  responseTime?: string;
  responseReason?: string;
}

export default defineComponent({
  name: 'AdminScheduleAdjustment',
  components: { PageWrapper },
  setup() {
    const go = useGo();
    const activeTab = ref('requests');

    // 筛选条件
    const filters = reactive({
      date: dayjs(),
      deptId: undefined as number | undefined,
      doctorId: undefined as number | undefined,
      timeSlot: undefined as number | undefined,
      keyword: '' as string | undefined,
    });

    // 调班申请相关
    const adjustmentRequests = ref<AdjustmentRequest[]>([]);
    const rejectModal = reactive({
      visible: false,
      loading: false,
      reason: '',
      currentRequest: null as AdjustmentRequest | null
    });

    const timeOptions = [
      { label: '上午', value: 1 },
      { label: '下午', value: 2 },
      { label: '晚上', value: 3 },
    ];
    const deptOptions = ref<{ label: string; value: number }[]>([]);
    const doctorOptions = ref<{ label: string; value: number }[]>([]);
    const rows = ref<TodayScheduleItem[]>([]);

    // 调班申请表格列
    const requestColumns = [
      { title: '申请医生', dataIndex: 'doctorName', key: 'doctorName', width: 120 },
      { title: '科室', dataIndex: 'deptName', key: 'deptName', width: 120 },
      { title: '当前排班', key: 'currentSchedule', width: 180 },
      { title: '目标排班', key: 'targetSchedule', width: 180 },
      { title: '申请原因', dataIndex: 'reason', key: 'reason', width: 200, ellipsis: true },
      { title: '申请时间', key: 'requestTime', width: 150 },
      { title: '状态', key: 'status', width: 100 },
      { title: '操作', key: 'action', width: 150 },
    ];

    // 今日排班表格列 - 【已修改】在科室后面添加诊室列
    const columns = [
      { title: '日期', dataIndex: 'scheduleDate', key: 'scheduleDate', width: 120 },
      { title: '医生', dataIndex: 'doctorName', key: 'doctorName', width: 140 },
      { title: '科室', dataIndex: 'deptName', key: 'deptName', width: 160 },
      { title: '诊室', dataIndex: 'roomNumber', key: 'roomNumber', width: 120 }, // 【新增】诊室列
      {
        title: '时段',
        dataIndex: 'timeSlot',
        key: 'timeSlot',
        width: 110,
      },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 100,
      },
    ];

    // 计算属性
    const pendingRequests = computed(() => {
      return adjustmentRequests.value.filter(req => req.status === 0);
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
      const colors: any = { 0: 'blue', 1: 'green', 2: 'red' };
      return colors[status] || 'default';
    }

    function getStatusText(status: number) {
      const texts: any = { 0: '待处理', 1: '已同意', 2: '已拒绝' };
      return texts[status] || '未知';
    }

    function formatTime(timeStr: string) {
      return dayjs(timeStr).format('MM-DD HH:mm');
    }

    // 加载调班申请数据
    async function loadAdjustmentRequests() {
      try {
        // 模拟数据 - 实际项目中应该调用API
        const mockData: AdjustmentRequest[] = [
          {
            requestId: '1',
            doctorId: 101,
            doctorName: '张医生',
            deptId: 1,
            deptName: '内科',
            currentDate: '2024-01-20',
            currentTimeSlot: 1,
            currentRoom: 'A101',
            targetDate: '2024-01-21',
            targetTimeSlot: 2,
            targetRoom: 'A102',
            reason: '家庭事务需要调整班次',
            status: 0,
            requestTime: '2024-01-15 10:30:00'
          },
          {
            requestId: '2',
            doctorId: 102,
            doctorName: '李医生',
            deptId: 2,
            deptName: '外科',
            currentDate: '2024-01-22',
            currentTimeSlot: 2,
            currentRoom: 'B201',
            reason: '身体不适需要休息',
            status: 0,
            requestTime: '2024-01-15 14:20:00'
          }
        ];
        adjustmentRequests.value = mockData;
      } catch (error) {
        message.error('加载调班申请失败');
      }
    }

    // 同意调班申请
    async function handleApprove(request: AdjustmentRequest) {
      Modal.confirm({
        title: '确认同意',
        content: `确定同意 ${request.doctorName} 医生的调班申请吗？`,
        onOk: async () => {
          try {
            // 调用API同意申请
            // await approveAdjustmentRequest(request.requestId);

            // 模拟API调用
            await new Promise(resolve => setTimeout(resolve, 1000));

            // 更新状态
            const index = adjustmentRequests.value.findIndex(req => req.requestId === request.requestId);
            if (index !== -1) {
              adjustmentRequests.value[index].status = 1;
              adjustmentRequests.value[index].responseTime = new Date().toISOString();
            }

            message.success('已同意调班申请');

            // 【已修改】路由路径
            go(`/admin/schedule-modify?requestId=${request.requestId}`);
          } catch (error) {
            message.error('操作失败');
          }
        }
      });
    }

    // 显示拒绝弹窗
    function showRejectModal(request: AdjustmentRequest) {
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
        // 调用API拒绝申请
        // await rejectAdjustmentRequest(rejectModal.currentRequest.requestId, rejectModal.reason);

        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000));

        // 更新状态
        const index = adjustmentRequests.value.findIndex(
          req => req.requestId === rejectModal.currentRequest!.requestId
        );
        if (index !== -1) {
          adjustmentRequests.value[index].status = 2;
          adjustmentRequests.value[index].responseTime = new Date().toISOString();
          adjustmentRequests.value[index].responseReason = rejectModal.reason;
        }

        message.success('已拒绝调班申请');
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

    // **【已修改】使用 h 函数代替 JSX/TSX 渲染 Modal 内容**
    function handleViewDetail(request: AdjustmentRequest) {
      Modal.info({
        title: '调班申请详情',
        width: 600,
        content: h('div', {}, [
          h('p', null, [h('strong', null, '申请医生：'), request.doctorName]),
          h('p', null, [h('strong', null, '科室：'), request.deptName]),
          h('p', null, [h('strong', null, '当前排班：'), `${request.currentDate} ${slotLabel(request.currentTimeSlot)} ${request.currentRoom}`]),
          request.targetDate
            ? h('p', null, [h('strong', null, '目标排班：'), `${request.targetDate} ${slotLabel(request.targetTimeSlot!)} ${request.targetRoom}`])
            : null,
          h('p', null, [h('strong', null, '申请原因：'), request.reason]),
          h('p', null, [h('strong', null, '申请时间：'), formatTime(request.requestTime)]),
          request.responseTime
            ? h('p', null, [h('strong', null, '处理时间：'), formatTime(request.responseTime)])
            : null,
          request.responseReason
            ? h('p', null, [h('strong', null, '处理原因：'), request.responseReason])
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
      const list = await getDoctorList(
        filters.deptId != null ? { deptId: Number(filters.deptId) } : undefined
      );
      doctorOptions.value = (list || []).map((d: any) => ({ label: d.doctorName, value: d.doctorId }));
    }

    async function loadList() {
      const data = await listSchedulesByDate({
        date: filters.date ? filters.date.format('YYYY-MM-DD') : undefined,
        deptId: filters.deptId != null ? Number(filters.deptId) : undefined,
        doctorId: filters.doctorId != null ? Number(filters.doctorId) : undefined,
        timeSlot: filters.timeSlot != null ? Number(filters.timeSlot) : undefined,
        keyword: filters.keyword && filters.keyword.trim() ? filters.keyword.trim() : undefined,
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
      await loadAdjustmentRequests();
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
.p-4 { padding: 16px; }
.mb-2 { margin-bottom: 8px; }
.mb-4 { margin-bottom: 16px; }
.card-title { display: flex; justify-content: space-between; align-items: center; }
.toolbar { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; flex-wrap: wrap; }
.field { display: flex; align-items: center; }
.field-label { margin-right: 8px; color: rgba(0, 0, 0, 0.88); white-space: nowrap; }
.mt-2 { margin-top: 8px; }
.mt-4 { margin-top: 16px; }
.slot-tag { display: inline-block; padding: 2px 8px; border-radius: 12px; color: #fff; font-size: 12px; }
.slot-am { background: #52c41a; }
.slot-pm { background: #1677ff; }
.slot-night { background: #722ed1; }
.status-ok { color: #52c41a; font-weight: 500; }
.status-ban { color: #ff4d4f; font-weight: 500; }
</style>
