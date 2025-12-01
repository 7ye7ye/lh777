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
                    <a-upload
                      name="file"
                      accept=".csv"
                      :showUploadList="false"
                      :beforeUpload="handleCsvUpload"
                    >
                      <a-button type="primary" size="small" preIcon="ant-design:upload-outlined">
                        上传CSV排班
                      </a-button>
                    </a-upload>
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
                  <a-select
                    v-model:value="filters.deptId"
                    :options="deptOptions"
                    style="width: 220px"
                    allow-clear
                    show-search
                    placeholder="选择科室"
                    @change="handleDeptChange"
                  />
                </div>
                <div class="field">
                  <span class="field-label">医生：</span>
                  <a-select
                    v-model:value="filters.doctorId"
                    :options="doctorOptions"
                    style="width: 220px"
                    allow-clear
                    show-search
                    :placeholder="filters.deptId ? '选择该科室的医生' : '请先选择科室'"
                    :disabled="!filters.deptId"
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

              <div class="mb-4">
                <a-button type="primary" @click="handleAddSchedule">
                  <template #icon><PlusOutlined /></template>
                  添加排班
                </a-button>
                <span style="margin-left: 10px; color: #999; font-size: 12px;">(v2.0 - 已更新)</span>
              </div>

              <a-table
                :data-source="rows"
                :columns="columns"
                row-key="scheduleId"
                bordered
                :pagination="{ pageSize: 10 }"
                :scroll="{ x: 1200 }"
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
                  <template v-else-if="column.key === 'maxQuota'">
                    {{ record.maxQuota || '-' }}
                  </template>
                  <template v-else-if="column.key === 'roomNumber'">
                    {{ record.roomNumber || '-' }}
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button type="link" size="small" @click="handleEditSchedule(record)">编辑</a-button>
                      <a-button type="link" size="small" danger @click="handleDeleteSchedule(record)">删除</a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
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

      <!-- 添加/编辑排班弹窗 -->
      <a-modal
        v-model:visible="scheduleFormVisible"
        :title="editingSchedule ? '编辑排班' : '添加排班'"
        width="700px"
        @ok="handleSaveSchedule"
        @cancel="handleCancelSchedule"
      >
        <a-form :model="scheduleForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
          <a-form-item label="医生ID" required>
            <a-input-number
              v-model:value="scheduleForm.doctorId"
              :min="1"
              style="width: 100%"
              placeholder="请输入医生ID"
              @change="handleDoctorIdChange"
            />
          </a-form-item>
          <a-form-item label="医生姓名">
            <a-input
              v-model:value="scheduleForm.doctorName"
              placeholder="请输入医生姓名（可选，系统会自动查找）"
              :disabled="!!scheduleForm.doctorId"
            />
          </a-form-item>
          <a-form-item label="科室" required>
            <a-select
              v-model:value="scheduleForm.deptId"
              :options="deptOptions"
              placeholder="选择科室"
              show-search
              :filter-option="filterOption"
            />
          </a-form-item>
          <a-form-item label="排班日期" required>
            <a-date-picker
              v-model:value="scheduleForm.scheduleDate"
              style="width: 100%"
              format="YYYY-MM-DD"
            />
          </a-form-item>
          <a-form-item label="时段" required>
            <a-select v-model:value="scheduleForm.timeSlot" placeholder="选择时段">
              <a-select-option :value="1">上午</a-select-option>
              <a-select-option :value="2">下午</a-select-option>
              <a-select-option :value="3">晚上</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="最大号源数" required>
            <a-input-number
              v-model:value="scheduleForm.maxQuota"
              :min="1"
              :max="100"
              style="width: 100%"
              placeholder="请输入最大号源数"
            />
          </a-form-item>
          <a-form-item label="诊室">
            <a-input
              v-model:value="scheduleForm.roomNumber"
              placeholder="留空则系统随机分配"
              :disabled="!editingSchedule"
            />
            <div v-if="!editingSchedule" style="color: #999; font-size: 12px; margin-top: 4px;">
              系统将自动随机分配一个可用诊室
            </div>
          </a-form-item>
          <a-form-item label="状态">
            <a-radio-group v-model:value="scheduleForm.status">
              <a-radio :value="1">有效</a-radio>
              <a-radio :value="0">停用</a-radio>
            </a-radio-group>
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
import { PlusOutlined } from '@ant-design/icons-vue';
import dayjs, { Dayjs } from 'dayjs';
import { getDepartmentList } from '/@/api/hospital/department';
import { listSchedulesByDate, type TodayScheduleItem } from '/@/api/hospital/scheduleView';
import { useGo } from '/@/hooks/web/usePage';
import { getDoctorList } from '/@/api/hospital/doctor';
import { createSchedule, updateSchedule, deleteSchedule, getAvailableRoom } from '/@/api/hospital/schedule';
import { getAdjustmentList, approveAdjustment, type AdjustmentRecord, type AdjustmentApprovalRequest } from '/@/api/hospital/adjustment';

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
    const adjustmentRequests = ref<AdjustmentRecord[]>([]);
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
    const rows = ref<(TodayScheduleItem & { doctorName?: string; deptName?: string; maxQuota?: number; roomNumber?: string })[]>([]);

    // 排班表单相关
    const scheduleFormVisible = ref(false);
    const editingSchedule = ref<(TodayScheduleItem & { doctorName?: string; deptName?: string; maxQuota?: number; roomNumber?: string }) | null>(null);
    const scheduleForm = reactive({
      scheduleId: undefined as number | undefined,
      doctorId: undefined as number | undefined,
      doctorName: '' as string,
      deptId: undefined as number | undefined,
      scheduleDate: null as Dayjs | null,
      timeSlot: undefined as number | undefined,
      maxQuota: 50 as number,
      roomNumber: '' as string,
      status: 1 as number,
    });

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

    // 今日排班表格列 - 已更新v2.0
    const columns = [
      { title: '日期', dataIndex: 'scheduleDate', key: 'scheduleDate', width: 120 },
      { title: '医生', dataIndex: 'doctorName', key: 'doctorName', width: 140 },
      { title: '科室', dataIndex: 'deptName', key: 'deptName', width: 160 },
      { title: '最大号源数', dataIndex: 'maxQuota', key: 'maxQuota', width: 120 },
      { title: '诊室', dataIndex: 'roomNumber', key: 'roomNumber', width: 120 },
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
      { title: '操作', key: 'action', width: 150, fixed: 'right' },
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
      try {
        const list = await getDepartmentList();
        deptOptions.value = (list || []).map((d: any) => ({ label: d.deptName, value: d.deptId }));
      } catch (error: any) {
        console.error('加载科室列表失败:', error);
        message.error('加载科室列表失败：' + (error?.message || '未知错误'));
        deptOptions.value = [];
        throw error; // 重新抛出错误，让调用者知道失败
      }
    }

    async function loadDoctorOptions() {
      // 如果没有选择科室，清空医生列表
      if (filters.deptId == null) {
        doctorOptions.value = [];
        return;
      }

      try {
        // 调用API获取该科室的医生列表，设置较大的pageSize以获取所有医生
        const response = await getDoctorList({
          deptId: Number(filters.deptId),
          isActive: 1, // 只获取启用的医生
          pageNum: 1,
          pageSize: 1000 // 设置较大的值以获取所有医生
        });

        // 处理API返回的数据结构：后端返回的是 IPage<Map<String, Object>>，即 { records: [...], total: ... }
        let list: any[] = [];
        if (response) {
          if (Array.isArray(response)) {
            // 直接是数组
            list = response;
          } else if (response.records && Array.isArray(response.records)) {
            // 分页数据格式：{ records: [...], total: ... }
            list = response.records;
          } else if (response.list && Array.isArray(response.list)) {
            list = response.list;
          } else if (response.data && Array.isArray(response.data)) {
            list = response.data;
          } else if (response.result) {
            // Result格式：{ result: { records: [...] } } 或 { result: [...] }
            if (Array.isArray(response.result)) {
              list = response.result;
            } else if (response.result.records && Array.isArray(response.result.records)) {
              list = response.result.records;
            }
          }
        }

        // 从doctor表中提取医生姓名，确保显示doctorName字段
        doctorOptions.value = list
          .filter((d: any) => {
            // 过滤掉没有姓名的数据，并确保有有效的doctorId
            return d && (d.doctorName || d.name) && (d.doctorId || d.id);
          })
          .map((d: any) => ({
            label: d.doctorName || d.name || '未知医生',
            value: d.doctorId || d.id || 0
          }))
          .filter((item: any) => item.value > 0) // 再次过滤掉无效的ID
          .sort((a, b) => a.label.localeCompare(b.label, 'zh-CN')); // 按姓名排序（支持中文）

        console.log('加载医生列表成功，科室ID:', filters.deptId, '医生数量:', doctorOptions.value.length);
        if (doctorOptions.value.length === 0) {
          message.info('该科室暂无医生');
        }
      } catch (error: any) {
        console.error('加载医生列表失败:', error);
        message.error('加载医生列表失败：' + (error?.message || '未知错误'));
        doctorOptions.value = [];
      }
    }

    async function loadList() {
      try {
        const data = await listSchedulesByDate({
          date: filters.date ? filters.date.format('YYYY-MM-DD') : undefined,
          deptId: filters.deptId != null ? Number(filters.deptId) : undefined,
          doctorId: filters.doctorId != null ? Number(filters.doctorId) : undefined,
          timeSlot: filters.timeSlot != null ? Number(filters.timeSlot) : undefined,
          keyword: filters.keyword && filters.keyword.trim() ? filters.keyword.trim() : undefined,
        });
        // 确保数据包含所有必要字段
        rows.value = (Array.isArray(data) ? data : []).map((item: any) => ({
          ...item,
          maxQuota: item.maxQuota ?? null,
          roomNumber: item.roomNumber ?? null,
          doctorName: item.doctorName ?? '',
          deptName: item.deptName ?? '',
        }));
        console.log('排班列表数据:', rows.value);
      } catch (error: any) {
        console.error('加载排班列表失败:', error);
        message.error('加载排班列表失败：' + (error?.message || '未知错误'));
        rows.value = [];
        throw error; // 重新抛出错误，让调用者知道失败
      }
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

    // 处理CSV文件上传
    async function handleCsvUpload(file: File) {
      const fileExtension = file.name.split('.').pop()?.toLowerCase();
      if (fileExtension !== 'csv') {
        message.error('请上传CSV文件（.csv格式）');
        return false;
      }

      try {
        // 读取CSV文件
        const text = await file.text();
        const lines = text.split('\n').filter(line => line.trim());

        if (lines.length < 2) {
          message.error('CSV文件至少需要包含表头和数据行');
          return false;
        }

        // 解析CSV（简单处理，支持逗号分隔）
        function parseCSVLine(line: string): string[] {
          const result: string[] = [];
          let current = '';
          let inQuotes = false;

          for (let i = 0; i < line.length; i++) {
            const char = line[i];
            if (char === '"') {
              inQuotes = !inQuotes;
            } else if (char === ',' && !inQuotes) {
              result.push(current.trim());
              current = '';
            } else {
              current += char;
            }
          }
          result.push(current.trim());
          return result;
        }

        // 解析表头（第一行）
        const headers = parseCSVLine(lines[0]).map((h: string) => h.toLowerCase().replace(/"/g, ''));

        // 查找列索引
        const dateIndex = findColumnIndex(headers, ['日期', 'date', '排班日期', 'scheduledate']);
        const doctorNameIndex = findColumnIndex(headers, ['医生', 'doctor', '医生姓名', 'doctorname', '姓名']);
        const deptNameIndex = findColumnIndex(headers, ['科室', 'dept', '科室名称', 'deptname', '部门']);
        const timeSlotIndex = findColumnIndex(headers, ['时段', 'timeslot', '时间段', '班次']);
        const roomIndex = findColumnIndex(headers, ['诊室', 'room', 'roomnumber', '诊室号', '房间']);

        if (dateIndex === -1 || doctorNameIndex === -1) {
          message.error('CSV文件必须包含"日期"和"医生"列');
          return false;
        }

        // 解析数据行
        const parsedSchedules: TodayScheduleItem[] = [];
        const errors: string[] = [];

        for (let i = 1; i < lines.length; i++) {
          const row = parseCSVLine(lines[i]);
          if (!row || row.length === 0) continue;

          try {
            // 解析日期
            let scheduleDate = '';
            const dateValue = row[dateIndex]?.replace(/"/g, '').trim();
            if (dateValue) {
              scheduleDate = dayjs(dateValue).format('YYYY-MM-DD');
            }

            if (!scheduleDate || scheduleDate === 'Invalid Date') {
              errors.push(`第${i + 1}行：日期格式错误`);
              continue;
            }

            // 解析医生姓名
            const doctorName = (row[doctorNameIndex] || '').replace(/"/g, '').trim();
            if (!doctorName) {
              errors.push(`第${i + 1}行：医生姓名不能为空`);
              continue;
            }

            // 解析科室名称
            const deptName = deptNameIndex !== -1 ? (row[deptNameIndex] || '').replace(/"/g, '').trim() : '';

            // 解析时段（1-上午，2-下午，3-晚上）
            let timeSlot = 1;
            if (timeSlotIndex !== -1) {
              const timeSlotValue = (row[timeSlotIndex] || '').replace(/"/g, '').trim().toLowerCase();
              if (timeSlotValue.includes('上午') || timeSlotValue.includes('am') || timeSlotValue === '1') {
                timeSlot = 1;
              } else if (timeSlotValue.includes('下午') || timeSlotValue.includes('pm') || timeSlotValue === '2') {
                timeSlot = 2;
              } else if (timeSlotValue.includes('晚上') || timeSlotValue.includes('night') || timeSlotValue === '3') {
                timeSlot = 3;
              } else if (!isNaN(Number(timeSlotValue))) {
                timeSlot = Number(timeSlotValue);
              }
            }

            // 解析诊室
            const roomNumber = roomIndex !== -1 ? (row[roomIndex] || '').replace(/"/g, '').trim() : '';

            // 查找医生ID和科室ID
            let doctorId = 0;
            let deptId = 0;

            // 从医生列表中找到匹配的医生
            const doctor = doctorOptions.value.find(d => d.label === doctorName);
            if (doctor) {
              doctorId = doctor.value;
            }

            // 从科室列表中找到匹配的科室
            if (deptName) {
              const dept = deptOptions.value.find(d => d.label === deptName);
              if (dept) {
                deptId = dept.value;
              }
            }

            // 创建排班记录（扩展TodayScheduleItem以包含显示字段）
            const schedule: TodayScheduleItem & { doctorName?: string; deptName?: string } = {
              scheduleId: Date.now() + i, // 临时ID
              doctorId: doctorId || 0,
              deptId: deptId || 0,
              scheduleDate: scheduleDate,
              timeSlot: timeSlot,
              roomNumber: roomNumber || undefined,
              status: 1, // 默认有效
              doctorName: doctorName,
              deptName: deptName || '未知科室',
            };

            parsedSchedules.push(schedule);
          } catch (error: any) {
            errors.push(`第${i + 1}行：${error.message || '解析失败'}`);
          }
        }

        if (parsedSchedules.length === 0) {
          message.error('未能解析出有效的排班数据');
          if (errors.length > 0) {
            console.error('解析错误：', errors);
          }
          return false;
        }

        // 合并到现有排班列表（去重：基于日期+医生+时段）
        const existingKeys = new Set(
          rows.value.map(r => `${r.scheduleDate}-${r.doctorName}-${r.timeSlot}`)
        );

        const newSchedules = parsedSchedules.filter(s => {
          const key = `${s.scheduleDate}-${(s as any).doctorName || ''}-${s.timeSlot}`;
          return !existingKeys.has(key);
        });

        // 添加到列表
        rows.value = [...rows.value, ...newSchedules];

        message.success(`成功导入 ${newSchedules.length} 条排班记录${parsedSchedules.length - newSchedules.length > 0 ? `，${parsedSchedules.length - newSchedules.length} 条重复记录已跳过` : ''}`);

        if (errors.length > 0) {
          console.warn('部分数据解析失败：', errors);
          message.warning(`有 ${errors.length} 行数据解析失败，请查看控制台`);
        }

        return false; // 阻止默认上传行为
      } catch (error: any) {
        console.error('CSV解析失败：', error);
        message.error('CSV文件解析失败：' + (error.message || '未知错误'));
        return false;
      }
    }

    // 查找列索引（支持多个可能的列名）
    function findColumnIndex(headers: string[], possibleNames: string[]): number {
      for (const name of possibleNames) {
        const index = headers.findIndex(h => h.includes(name.toLowerCase()));
        if (index !== -1) return index;
      }
      return -1;
    }

    onMounted(async () => {
      try {
        // 加载科室选项（内部已有错误处理）
        await loadDeptOptions().catch((error) => {
          // 错误已在函数内部处理，这里只记录日志
          console.error('onMounted: 加载科室列表失败', error);
        });

        // 加载医生选项（内部已有错误处理）
        await loadDoctorOptions().catch((error) => {
          // 错误已在函数内部处理，这里只记录日志
          console.error('onMounted: 加载医生列表失败', error);
        });

        // 加载排班列表（内部已有错误处理）
        await loadList().catch((error) => {
          // 错误已在函数内部处理，这里只记录日志
          console.error('onMounted: 加载排班列表失败', error);
        });

        // 加载调班申请（内部已有错误处理）
        await loadAdjustmentRequests().catch((error) => {
          // 错误已在函数内部处理，这里只记录日志
          console.error('onMounted: 加载调班申请失败', error);
        });
      } catch (error: any) {
        // 捕获未预期的错误
        console.error('页面初始化发生未预期的错误:', error);
        message.error('页面初始化失败：' + (error?.message || '未知错误'));
      }
    });

    // 处理科室变化
    async function handleDeptChange() {
      // 清空已选择的医生
      filters.doctorId = undefined;
      // 重新加载该科室的医生列表
      await loadDoctorOptions();
    }

    // 监听科室变化，自动更新医生列表
    watch(() => filters.deptId, async (newDeptId) => {
      // 清空已选择的医生
      filters.doctorId = undefined;
      // 如果选择了新科室，加载该科室的医生列表
      if (newDeptId != null) {
        await loadDoctorOptions();
      } else {
        // 如果清空了科室选择，清空医生列表
        doctorOptions.value = [];
      }
    });

    // 排班增删改功能
    function handleAddSchedule() {
      editingSchedule.value = null;
      scheduleForm.scheduleId = undefined;
      scheduleForm.doctorId = undefined;
      scheduleForm.doctorName = '';
      scheduleForm.deptId = undefined;
      scheduleForm.scheduleDate = filters.date || dayjs();
      scheduleForm.timeSlot = undefined;
      scheduleForm.maxQuota = 50;
      scheduleForm.roomNumber = '';
      scheduleForm.status = 1;
      scheduleFormVisible.value = true;
    }

    function handleEditSchedule(record: TodayScheduleItem & { doctorName?: string; deptName?: string; maxQuota?: number; roomNumber?: string }) {
      editingSchedule.value = record;
      scheduleForm.scheduleId = record.scheduleId;
      scheduleForm.doctorId = record.doctorId;
      scheduleForm.doctorName = record.doctorName || '';
      scheduleForm.deptId = record.deptId;
      scheduleForm.scheduleDate = dayjs(record.scheduleDate);
      scheduleForm.timeSlot = record.timeSlot;
      scheduleForm.maxQuota = record.maxQuota || 50;
      scheduleForm.roomNumber = record.roomNumber || '';
      scheduleForm.status = record.status || 1;
      scheduleFormVisible.value = true;
    }

    async function handleDeleteSchedule(record: TodayScheduleItem & { doctorName?: string; deptName?: string; maxQuota?: number; roomNumber?: string }) {
      Modal.confirm({
        title: '确认删除',
        content: `确定要删除 ${record.doctorName || '该医生'} 在 ${record.scheduleDate} ${slotLabel(record.timeSlot)} 的排班吗？`,
        onOk: async () => {
          try {
            await deleteSchedule(record.scheduleId);
            message.success('删除成功');
            await loadList();
          } catch (error: any) {
            message.error('删除失败：' + (error?.message || '未知错误'));
          }
        },
      });
    }

    async function handleDoctorIdChange() {
      if (scheduleForm.doctorId) {
        try {
          // 尝试从医生列表中查找医生信息
          const doctor = doctorOptions.value.find(d => d.value === scheduleForm.doctorId);
          if (doctor) {
            scheduleForm.doctorName = doctor.label;
          }
        } catch (error) {
          console.error('获取医生信息失败:', error);
        }
      }
    }

    function filterOption(input: string, option: any) {
      return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    }

    async function handleSaveSchedule() {
      if (!scheduleForm.doctorId) {
        message.error('请选择医生ID');
        return;
      }
      if (!scheduleForm.deptId) {
        message.error('请选择科室');
        return;
      }
      if (!scheduleForm.scheduleDate) {
        message.error('请选择排班日期');
        return;
      }
      if (!scheduleForm.timeSlot) {
        message.error('请选择时段');
        return;
      }
      if (!scheduleForm.maxQuota || scheduleForm.maxQuota < 1) {
        message.error('请输入有效的最大号源数');
        return;
      }

      try {
        const dateStr = scheduleForm.scheduleDate.format('YYYY-MM-DD');
        const shiftMap: { [key: number]: string } = { 1: '上午', 2: '下午', 3: '晚上' };
        const shiftStr = shiftMap[scheduleForm.timeSlot] || '上午';

        if (editingSchedule.value && scheduleForm.scheduleId) {
          // 更新排班
          await updateSchedule({
            scheduleId: scheduleForm.scheduleId,
            doctorId: scheduleForm.doctorId,
            deptId: scheduleForm.deptId,
            date: dateStr,
            timeSlot: scheduleForm.timeSlot, // 直接传递timeSlot数字
            shift: shiftStr, // 保留shift作为备用
            slots: scheduleForm.maxQuota,
            maxQuota: scheduleForm.maxQuota,
            roomNumber: scheduleForm.roomNumber || undefined,
            status: scheduleForm.status,
          });
          message.success('更新成功');
        } else {
          // 创建排班 - 需要随机分配诊室
          let roomNumber = scheduleForm.roomNumber;
          if (!roomNumber) {
            // 调用后端接口获取可用诊室
            try {
              roomNumber = await getAvailableRoom({
                date: dateStr,
                timeSlot: scheduleForm.timeSlot,
              });
            } catch (error) {
              console.error('获取可用诊室失败，使用默认值:', error);
              // 如果后端接口不存在，使用简单的随机分配
              const rooms = ['A-101', 'A-102', 'A-103', 'A-104', 'A-105', 'B-201', 'B-202'];
              roomNumber = rooms[Math.floor(Math.random() * rooms.length)];
            }
          }

          await createSchedule({
            doctorId: scheduleForm.doctorId,
            deptId: scheduleForm.deptId,
            date: dateStr,
            shift: shiftStr,
            slots: scheduleForm.maxQuota,
            maxQuota: scheduleForm.maxQuota,
            roomNumber: roomNumber,
          });
          message.success('添加成功');
        }

        scheduleFormVisible.value = false;
        await loadList();
      } catch (error: any) {
        message.error((editingSchedule.value ? '更新' : '添加') + '失败：' + (error?.message || '未知错误'));
      }
    }

    function handleCancelSchedule() {
      scheduleFormVisible.value = false;
      editingSchedule.value = null;
    }

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
      handleCsvUpload,
      slotLabel,
      slotClass,
      getStatusColor,
      getStatusText,
      formatTime,
      PlusOutlined,
      scheduleFormVisible,
      editingSchedule,
      scheduleForm,
      handleAddSchedule,
      handleEditSchedule,
      handleDeleteSchedule,
      handleSaveSchedule,
      handleCancelSchedule,
      handleDoctorIdChange,
      filterOption,
      handleDeptChange,
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

.mb-2 { margin-bottom: 8px; }
.mb-4 { margin-bottom: 16px; }

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

.time-text {
  font-size: 11px;
  white-space: nowrap;
}

.status-tag {
  font-size: 11px;
  padding: 0 4px;
  line-height: 18px;
}
</style>
