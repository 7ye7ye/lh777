<template>
  <PageWrapper :title="'请假审批'">
    <div class="p-4">
      <a-alert
        v-if="pendingRequests.length > 0"
        message="有待处理的请假申请"
        :description="`当前有 ${pendingRequests.length} 个请假申请需要处理`"
        type="info"
        show-icon
        class="mb-4"
        closable
      />

      <a-card :bordered="false">
        <template #title>
          <div class="card-title">
            <span>请假申请列表</span>
            <a-space>
              <a-select
                v-model:value="statusFilter"
                placeholder="筛选状态"
                style="width: 120px"
                size="small"
                @change="loadLeaveRequests"
              >
                <a-select-option :value="undefined">全部</a-select-option>
                <a-select-option :value="1">待审批</a-select-option>
                <a-select-option :value="2">已同意</a-select-option>
                <a-select-option :value="3">已拒绝</a-select-option>
                <a-select-option :value="4">已撤销</a-select-option>
              </a-select>
              <a-button size="small" @click="loadLeaveRequests">刷新</a-button>
            </a-space>
          </div>
        </template>

        <div class="table-container">
          <a-table
            :data-source="leaveRequests"
            :columns="columns"
            row-key="leaveId"
            :pagination="{ pageSize: 10 }"
            :loading="loading"
            class="compact-table"
            :scroll="{ x: 1200 }"
            size="small"
            bordered
          >
            <template #emptyText>
              <a-empty description="暂无请假申请" />
            </template>
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
              <template v-else-if="column.key === 'leaveDate'">
                <div class="date-range">
                  {{ record.startDate }} 至 {{ record.endDate }}
                </div>
              </template>
              <template v-else-if="column.key === 'leaveType'">
                <a-tag :color="getLeaveTypeColor(record.leaveType)">
                  {{ getLeaveTypeText(record.leaveType) }}
                </a-tag>
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

      <!-- 拒绝请假弹窗 -->
      <a-modal
        v-model:visible="rejectModal.visible"
        title="拒绝请假申请"
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

      <!-- 查看详情弹窗 -->
      <a-modal
        v-model:visible="detailModal.visible"
        title="请假申请详情"
        :footer="null"
        width="600px"
      >
        <a-descriptions :column="2" bordered v-if="detailModal.currentRequest">
          <a-descriptions-item label="医生姓名">
            {{ detailModal.currentRequest.doctorName }}
          </a-descriptions-item>
          <a-descriptions-item label="科室">
            {{ detailModal.currentRequest.deptName }}
          </a-descriptions-item>
          <a-descriptions-item label="请假类型">
            <a-tag :color="getLeaveTypeColor(detailModal.currentRequest.leaveType)">
              {{ getLeaveTypeText(detailModal.currentRequest.leaveType) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="请假日期">
            {{ detailModal.currentRequest.startDate }} 至 {{ detailModal.currentRequest.endDate }}
          </a-descriptions-item>
          <a-descriptions-item label="请假事由" :span="2">
            {{ detailModal.currentRequest.reason }}
          </a-descriptions-item>
          <a-descriptions-item label="申请时间">
            {{ formatTime(detailModal.currentRequest.applyTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(detailModal.currentRequest.status)">
              {{ getStatusText(detailModal.currentRequest.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item
            v-if="detailModal.currentRequest.rejectReason"
            label="拒绝原因"
            :span="2"
          >
            {{ detailModal.currentRequest.rejectReason }}
          </a-descriptions-item>
          <a-descriptions-item
            v-if="detailModal.currentRequest.approveTime"
            label="审批时间"
            :span="2"
          >
            {{ formatTime(detailModal.currentRequest.approveTime) }}
          </a-descriptions-item>
        </a-descriptions>
      </a-modal>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, onMounted, computed } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  getLeaveList,
  approveLeave,
  type LeaveRecord,
  type LeaveApprovalRequest,
} from '/@/api/hospital/leave';

export default defineComponent({
  name: 'AdminLeaveApproval',
  components: { PageWrapper },
  setup() {
    const leaveRequests = ref<LeaveRecord[]>([]);
    const loading = ref(false);
    const statusFilter = ref<number | undefined>(1); // 默认显示待审批

    // 拒绝请假弹窗
    const rejectModal = reactive({
      visible: false,
      loading: false,
      reason: '',
      currentRequest: null as LeaveRecord | null,
    });

    // 查看详情弹窗
    const detailModal = reactive({
      visible: false,
      currentRequest: null as LeaveRecord | null,
    });

    // 表格列
    const columns = [
      {
        title: '申请医生',
        dataIndex: 'doctorName',
        key: 'doctorName',
        width: 100,
        ellipsis: true,
      },
      {
        title: '科室',
        dataIndex: 'deptName',
        key: 'deptName',
        width: 100,
        ellipsis: true,
      },
      {
        title: '请假类型',
        key: 'leaveType',
        width: 100,
      },
      {
        title: '请假日期',
        key: 'leaveDate',
        width: 200,
      },
      {
        title: '请假事由',
        dataIndex: 'reason',
        key: 'reason',
        width: 200,
        ellipsis: true,
      },
      {
        title: '申请时间',
        key: 'applyTime',
        width: 120,
      },
      {
        title: '状态',
        key: 'status',
        width: 80,
      },
      {
        title: '操作',
        key: 'action',
        width: 150,
        fixed: 'right' as const,
      },
    ];

    // 计算属性
    const pendingRequests = computed(() => {
      return leaveRequests.value.filter((req) => req.status === 1);
    });

    // 方法
    function getStatusColor(status: number) {
      const colors: any = { 1: 'blue', 2: 'green', 3: 'red', 4: 'orange' };
      return colors[status] || 'default';
    }

    function getStatusText(status: number) {
      const texts: any = { 1: '待审批', 2: '已同意', 3: '已拒绝', 4: '已撤销' };
      return texts[status] || '未知';
    }

    function getLeaveTypeText(leaveType: string) {
      const map: any = {
        sick: '病假',
        personal: '事假',
        annual: '年假',
        maternity: '产假',
        marriage: '婚假',
        bereavement: '丧假',
        other: '其他',
      };
      return map[leaveType] || leaveType;
    }

    function getLeaveTypeColor(leaveType: string) {
      const colors: any = {
        sick: 'red',
        personal: 'orange',
        annual: 'blue',
        maternity: 'pink',
        marriage: 'purple',
        bereavement: 'gray',
        other: 'default',
      };
      return colors[leaveType] || 'default';
    }

    function formatTime(timeStr: string) {
      return dayjs(timeStr).format('YYYY-MM-DD HH:mm');
    }

    // 加载请假申请数据
    async function loadLeaveRequests() {
      loading.value = true;
      try {
        const response = await getLeaveList({ 
          status: statusFilter.value, 
          current: 1, 
          size: 50 
        });
        console.log('请假申请列表响应:', response);
        
        // 处理不同的响应格式
        let records: LeaveRecord[] = [];
        if (response) {
          if (Array.isArray(response)) {
            // 如果直接返回数组
            records = response;
          } else if (response.records && Array.isArray(response.records)) {
            // 如果是分页对象
            records = response.records;
          } else if (response.list && Array.isArray(response.list)) {
            // 如果是list格式
            records = response.list;
          }
        }
        
        leaveRequests.value = records;
        console.log('解析后的请假申请列表:', leaveRequests.value);
        
        if (records.length === 0) {
          console.warn('未获取到请假申请数据，请检查后端接口是否正常');
        }
      } catch (error: any) {
        console.error('加载请假申请失败:', error);
        message.error('加载请假申请失败: ' + (error.message || '未知错误'));
        leaveRequests.value = [];
      } finally {
        loading.value = false;
      }
    }

    // 同意请假申请
    async function handleApprove(request: LeaveRecord) {
      Modal.confirm({
        title: '确认同意',
        content: `确定同意该请假申请吗？系统将自动禁用请假期间的排班。`,
        onOk: async () => {
          try {
            const approvalRequest: LeaveApprovalRequest = {
              leaveId: request.leaveId,
              status: 2,
            };

            await approveLeave(approvalRequest);
            message.success('已同意请假申请');
            await loadLeaveRequests();
          } catch (error: any) {
            console.error('同意请假失败:', error);
            message.error('操作失败: ' + (error.message || '未知错误'));
          }
        },
      });
    }

    // 显示拒绝弹窗
    function showRejectModal(request: LeaveRecord) {
      rejectModal.currentRequest = request;
      rejectModal.reason = '';
      rejectModal.visible = true;
    }

    // 拒绝请假申请
    async function handleReject() {
      if (!rejectModal.currentRequest) return;

      if (!rejectModal.reason.trim()) {
        message.warning('请输入拒绝原因');
        return;
      }

      rejectModal.loading = true;
      try {
        const approvalRequest: LeaveApprovalRequest = {
          leaveId: rejectModal.currentRequest.leaveId,
          status: 3,
          rejectReason: rejectModal.reason,
        };

        await approveLeave(approvalRequest);
        message.success('已拒绝请假申请');
        rejectModal.visible = false;
        rejectModal.reason = '';
        await loadLeaveRequests();
      } catch (error: any) {
        console.error('拒绝请假失败:', error);
        message.error('操作失败: ' + (error.message || '未知错误'));
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

    // 查看详情
    function handleViewDetail(record: LeaveRecord) {
      detailModal.currentRequest = record;
      detailModal.visible = true;
    }

    // 初始化
    onMounted(() => {
      loadLeaveRequests();
    });

    return {
      leaveRequests,
      pendingRequests,
      columns,
      loading,
      statusFilter,
      rejectModal,
      detailModal,
      getStatusColor,
      getStatusText,
      getLeaveTypeText,
      getLeaveTypeColor,
      formatTime,
      loadLeaveRequests,
      handleApprove,
      showRejectModal,
      handleReject,
      handleCancelReject,
      handleViewDetail,
    };
  },
});
</script>

<style scoped>
.p-4 {
  padding: 16px;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-container {
  margin-top: 16px;
}

.compact-table {
  font-size: 12px;
}

.status-tag {
  font-size: 12px;
}

.time-text {
  font-size: 12px;
  color: #666;
}

.date-range {
  font-size: 12px;
}

.reason-text {
  font-size: 12px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
