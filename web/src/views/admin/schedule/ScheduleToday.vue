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
                <a-button type="primary" style="margin-left: 10px" @click="handleAutoGenerate">
                  <template #icon><PlusOutlined /></template>
                  自动生成排班
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

      <!-- 自动生成排班弹窗 -->
      <a-modal
        v-model:visible="autoGenerateModal.visible"
        title="自动生成排班"
        width="800px"
        @ok="handleGenerateSchedules"
        @cancel="handleCancelAutoGenerate"
        :confirmLoading="autoGenerateModal.loading"
        okText="生成排班"
        cancelText="取消"
      >
        <a-form :model="autoGenerateForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
          <a-form-item label="选择科室" required>
            <a-select
              v-model:value="autoGenerateForm.deptIds"
              :options="deptOptions"
              mode="multiple"
              placeholder="请选择科室（可多选）"
              show-search
              :filter-option="filterOption"
              style="width: 100%"
            />
          </a-form-item>
          <a-form-item label="排班数量" required>
            <a-input-number
              v-model:value="autoGenerateForm.scheduleCount"
              :min="1"
              :max="100"
              style="width: 100%"
              placeholder="请输入要生成的排班数量"
            />
          </a-form-item>
          <a-form-item label="排班时间" required>
            <a-select
              v-model:value="autoGenerateForm.timeSlots"
              mode="multiple"
              placeholder="请选择时段（可多选）"
              style="width: 100%"
            >
              <a-select-option :value="1">上午</a-select-option>
              <a-select-option :value="2">下午</a-select-option>
              <a-select-option :value="3">晚上</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="最大号源数" required>
            <a-input-number
              v-model:value="autoGenerateForm.maxQuota"
              :min="1"
              :max="200"
              style="width: 100%"
              placeholder="请输入最大号源数"
            />
          </a-form-item>
          <a-form-item label="起始日期" required>
            <a-date-picker
              v-model:value="autoGenerateForm.startDate"
              style="width: 100%"
              format="YYYY-MM-DD"
              placeholder="选择起始日期"
            />
          </a-form-item>
        </a-form>
      </a-modal>

      <!-- 生成结果列表弹窗 -->
      <a-modal
        v-model:visible="generatedSchedulesModal.visible"
        title="生成的排班列表"
        width="1000px"
        @ok="handleConfirmGeneratedSchedules"
        @cancel="handleCancelGeneratedSchedules"
        :confirmLoading="generatedSchedulesModal.loading"
        okText="确认保存"
        cancelText="取消"
      >
        <a-table
          :data-source="generatedSchedulesModal.schedules"
          :columns="generatedSchedulesColumns"
          row-key="tempId"
          :pagination="{ pageSize: 10 }"
          bordered
          size="small"
        >
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.key === 'index'">
              {{ index + 1 }}
            </template>
            <template v-else-if="column.key === 'timeSlot'">
              <span :class="['slot-tag', slotClass(record.timeSlot)]">
                {{ slotLabel(record.timeSlot) }}
              </span>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="handleEditGeneratedSchedule(record)">编辑</a-button>
                <a-button type="link" size="small" danger @click="handleDeleteGeneratedSchedule(record)">删除</a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-modal>

      <!-- 编辑生成排班弹窗 -->
      <a-modal
        v-model:visible="editGeneratedScheduleModal.visible"
        title="编辑排班"
        width="700px"
        @ok="handleSaveGeneratedSchedule"
        @cancel="handleCancelEditGeneratedSchedule"
      >
        <a-form :model="editGeneratedScheduleModal.form" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
          <a-form-item label="医生">
            <a-input v-model:value="editGeneratedScheduleModal.form.doctorName" disabled />
          </a-form-item>
          <a-form-item label="科室">
            <a-input v-model:value="editGeneratedScheduleModal.form.deptName" disabled />
          </a-form-item>
          <a-form-item label="排班日期" required>
            <a-date-picker
              v-model:value="editGeneratedScheduleModal.form.scheduleDate"
              style="width: 100%"
              format="YYYY-MM-DD"
            />
          </a-form-item>
          <a-form-item label="时段" required>
            <a-select v-model:value="editGeneratedScheduleModal.form.timeSlot" placeholder="选择时段">
              <a-select-option :value="1">上午</a-select-option>
              <a-select-option :value="2">下午</a-select-option>
              <a-select-option :value="3">晚上</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="最大号源数" required>
            <a-input-number
              v-model:value="editGeneratedScheduleModal.form.maxQuota"
              :min="1"
              :max="200"
              style="width: 100%"
              placeholder="请输入最大号源数"
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
          <a-form-item label="医生姓名" required>
            <a-input
              v-model:value="scheduleForm.doctorName"
              placeholder="请输入医生姓名"
              @blur="handleDoctorNameBlur"
              @input="handleDoctorNameInput"
              allow-clear
            />
            <div v-if="scheduleForm.selectedDoctorInfo" style="color: #52c41a; font-size: 12px; margin-top: 4px;">
              已选择：{{ scheduleForm.selectedDoctorInfo.doctorName }} (ID: {{ scheduleForm.selectedDoctorInfo.doctorId }}, 科室: {{ scheduleForm.selectedDoctorInfo.deptName }})
            </div>
          </a-form-item>
          <a-form-item label="科室" required>
            <a-select
              v-model:value="scheduleForm.deptId"
              :options="deptOptions"
              placeholder="选择科室（选择医生后会自动填充，可修改）"
              show-search
              :filter-option="filterOption"
            />
            <div v-if="scheduleForm.selectedDoctorInfo && scheduleForm.deptId" style="color: #52c41a; font-size: 12px; margin-top: 4px;">
              已自动填充：{{ scheduleForm.selectedDoctorInfo.deptName || '科室ID: ' + scheduleForm.deptId }}
            </div>
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
              placeholder="留空则系统自动分配（保持原楼层）"
              allow-clear
            />
            <div style="color: #999; font-size: 12px; margin-top: 4px;">
              {{ editingSchedule ? '留空则系统自动分配，保持原楼层不变（如：门诊101 → 门诊104）' : '系统将自动随机分配一个可用诊室' }}
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

      <!-- 医生选择弹窗（当医生姓名重复时显示） -->
      <a-modal
        v-model:visible="doctorSelectModal.visible"
        title="选择医生"
        width="800px"
        @ok="handleSelectDoctor"
        @cancel="handleCancelSelectDoctor"
        okText="确认选择"
        cancelText="取消"
      >
        <div style="margin-bottom: 16px;">
          <a-alert
            message="检测到多个同名医生，请选择其中一个"
            type="warning"
            show-icon
            style="margin-bottom: 16px;"
          />
          <a-table
            :data-source="doctorSelectModal.doctors"
            :columns="doctorSelectColumns"
            row-key="doctorId"
            :pagination="{ pageSize: 5 }"
            :row-selection="{
              type: 'radio',
              selectedRowKeys: doctorSelectModal.selectedDoctorId ? [doctorSelectModal.selectedDoctorId] : [],
              onChange: (selectedRowKeys) => {
                doctorSelectModal.selectedDoctorId = selectedRowKeys[0] as number;
              }
            }"
            bordered
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'deptName'">
                {{ record.deptName || '未知科室' }}
              </template>
              <template v-else-if="column.key === 'title'">
                {{ record.title || '-' }}
              </template>
              <template v-else-if="column.key === 'specialty'">
                {{ record.specialty || '-' }}
              </template>
            </template>
          </a-table>
        </div>
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
import { listSchedulesByDate, type TodayScheduleItem, getDoctorsByDeptFromSchedule } from '/@/api/hospital/scheduleView';
import { useGo } from '/@/hooks/web/usePage';
import { createSchedule, updateSchedule, deleteSchedule, getAvailableRoom, generateSchedules, batchCreateSchedules } from '/@/api/hospital/schedule';
import { getAdjustmentList, approveAdjustment, type AdjustmentRecord, type AdjustmentApprovalRequest } from '/@/api/hospital/adjustment';
import { getDoctorList, type Doctor } from '/@/api/hospital/doctor';

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
      selectedDoctorInfo: null as { doctorId: number; doctorName: string; deptId: number; deptName: string } | null,
      deptId: undefined as number | undefined,
      scheduleDate: null as Dayjs | null,
      timeSlot: undefined as number | undefined,
      maxQuota: 50 as number,
      roomNumber: '' as string,
      status: 1 as number,
    });

    // 医生选择弹窗相关
    const doctorSelectModal = reactive({
      visible: false,
      doctors: [] as Doctor[],
      selectedDoctorId: undefined as number | undefined,
      currentDoctorName: '' as string,
    });

    // 医生选择表格列
    const doctorSelectColumns = [
      { title: '医生ID', dataIndex: 'doctorId', key: 'doctorId', width: 100 },
      { title: '医生姓名', dataIndex: 'doctorName', key: 'doctorName', width: 120 },
      { title: '科室', key: 'deptName', width: 150 },
      { title: '职称', key: 'title', width: 120 },
      { title: '专业', key: 'specialty', width: 150 },
    ];

    // 自动生成排班相关
    const autoGenerateModal = reactive({
      visible: false,
      loading: false,
    });
    const autoGenerateForm = reactive({
      deptIds: [] as number[],
      scheduleCount: 10 as number,
      timeSlots: [] as number[],
      maxQuota: 50 as number,
      startDate: null as Dayjs | null,
    });
    const generatedSchedulesModal = reactive({
      visible: false,
      loading: false,
      schedules: [] as Array<{
        tempId: string;
        doctorId: number;
        doctorName: string;
        deptId: number;
        deptName: string;
        scheduleDate: string;
        timeSlot: number;
        maxQuota: number;
        roomNumber?: string;
      }>,
    });
    const editGeneratedScheduleModal = reactive({
      visible: false,
      form: {
        tempId: '',
        doctorId: 0,
        doctorName: '',
        deptId: 0,
        deptName: '',
        scheduleDate: null as Dayjs | null,
        timeSlot: undefined as number | undefined,
        maxQuota: 50 as number,
      },
    });
    const generatedSchedulesColumns = [
      { title: '序号', key: 'index', width: 80 },
      { title: '医生', dataIndex: 'doctorName', key: 'doctorName', width: 120 },
      { title: '科室', dataIndex: 'deptName', key: 'deptName', width: 120 },
      { title: '日期', dataIndex: 'scheduleDate', key: 'scheduleDate', width: 120 },
      { title: '时段', dataIndex: 'timeSlot', key: 'timeSlot', width: 100 },
      { title: '最大号源数', dataIndex: 'maxQuota', key: 'maxQuota', width: 120 },
      { title: '诊室', dataIndex: 'roomNumber', key: 'roomNumber', width: 120 },
      { title: '操作', key: 'action', width: 150, fixed: 'right' },
    ];

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
        // 从doctor_schedule表中获取该科室有排班记录的医生列表
        const list = await getDoctorsByDeptFromSchedule(Number(filters.deptId));

        // 处理返回的医生列表
        doctorOptions.value = (Array.isArray(list) ? list : [])
          .filter((d: any) => {
            // 过滤掉没有姓名的数据，并确保有有效的doctorId
            return d && d.doctorName && d.doctorId;
          })
          .map((d: any) => ({
            label: d.doctorName || '未知医生',
            value: d.doctorId || 0
          }))
          .filter((item: any) => item.value > 0) // 再次过滤掉无效的ID
          .sort((a, b) => a.label.localeCompare(b.label, 'zh-CN')); // 按姓名排序（支持中文）

        console.log('从doctor_schedule表加载医生列表成功，科室ID:', filters.deptId, '医生数量:', doctorOptions.value.length);
        if (doctorOptions.value.length === 0) {
          message.info('该科室在排班表中暂无医生');
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
      scheduleForm.selectedDoctorInfo = null;
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
      scheduleForm.selectedDoctorInfo = record.doctorId && record.doctorName ? {
        doctorId: record.doctorId,
        doctorName: record.doctorName,
        deptId: record.deptId,
        deptName: record.deptName || '',
      } : null;
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

    // 处理医生姓名输入
    function handleDoctorNameInput() {
      // 输入时清空已选择的医生信息
      scheduleForm.selectedDoctorInfo = null;
      scheduleForm.doctorId = undefined;
    }

    // 处理医生姓名失焦，查询医生
    async function handleDoctorNameBlur() {
      const doctorName = scheduleForm.doctorName?.trim();
      if (!doctorName) {
        scheduleForm.selectedDoctorInfo = null;
        scheduleForm.doctorId = undefined;
        return;
      }

      try {
        console.log('开始查询医生:', { doctorName });
        
        // 查询医生列表 - 后端API使用keyword参数，且支持模糊查询
        // 设置较大的pageSize以确保能获取到所有匹配的医生
        // 只查询激活的医生（isActive=1）
        const response = await getDoctorList({ 
          keyword: doctorName,  // 使用keyword参数，后端会进行模糊查询
          isActive: 1,           // 只查询激活的医生
          pageNum: 1,
          pageSize: 100  // 设置较大的页面大小以获取所有匹配结果
        } as any);
        
        console.log('API请求参数:', { keyword: doctorName, isActive: 1, pageNum: 1, pageSize: 100 });
        
        console.log('API原始返回:', response);
        console.log('API返回类型:', typeof response);
        console.log('API返回键:', response ? Object.keys(response) : []);
        
        let doctorList: Doctor[] = [];
        
        // 处理API返回的数据结构
        // defHttp的transformRequestHook会提取result字段，所以response已经是分页对象
        // 后端返回格式: { code: 200, success: true, result: { records: [...], total: ... } }
        // 经过transformRequestHook处理后，返回的是result部分: { records: [...], total: ... }
        if (response) {
          // 如果response本身就是数组（直接返回）
          if (Array.isArray(response)) {
            doctorList = response;
            console.log('✓ 数据是数组格式，数量:', doctorList.length);
          } 
          // 如果response直接有records字段（分页对象，这是最常见的情况）
          else if (response.records && Array.isArray(response.records)) {
            doctorList = response.records;
            console.log('✓ 数据在records中，数量:', doctorList.length);
          }
          // 如果response有result字段（可能是未经过transformRequestHook处理的原始响应）
          else if (response.result) {
            // result可能是分页对象，有records字段
            if (response.result.records && Array.isArray(response.result.records)) {
              doctorList = response.result.records;
              console.log('✓ 数据在result.records中，数量:', doctorList.length);
            }
            // result本身可能是数组
            else if (Array.isArray(response.result)) {
              doctorList = response.result;
              console.log('✓ 数据在result中（数组），数量:', doctorList.length);
            }
            // result可能是IPage对象，需要检查其他字段
            else if (response.result.list && Array.isArray(response.result.list)) {
              doctorList = response.result.list;
              console.log('✓ 数据在result.list中，数量:', doctorList.length);
            }
          }
          // 其他可能的数据结构
          else if (response.list && Array.isArray(response.list)) {
            doctorList = response.list;
            console.log('✓ 数据在list中，数量:', doctorList.length);
          } 
          else if (response.data && Array.isArray(response.data)) {
            doctorList = response.data;
            console.log('✓ 数据在data中，数量:', doctorList.length);
          }
          
          // 如果还是没有找到数据，打印完整的response结构用于调试
          if (doctorList.length === 0) {
            console.error('❌ 未找到医生数据！');
            console.error('完整响应结构:', JSON.stringify(response, null, 2));
            console.error('响应类型:', typeof response);
            console.error('响应键:', response ? Object.keys(response) : []);
            if (response && typeof response === 'object') {
              console.error('响应值:', response);
              if ((response as any).result) {
                console.error('result类型:', typeof (response as any).result);
                console.error('result键:', Object.keys((response as any).result));
                console.error('result值:', (response as any).result);
              }
            }
          }
        } else {
          console.error('❌ API返回为空！');
        }

        console.log('解析后的医生列表:', doctorList);
        console.log('医生列表数量:', doctorList.length);

        // 过滤出姓名完全匹配的医生（后端是模糊查询，需要前端精确匹配）
        // 使用更宽松的匹配逻辑，处理可能的空格、全角半角等问题
        const matchedDoctors = doctorList.filter(d => {
          // 处理可能的字段名差异（doctorName 或 doctor_name）
          const name = (d.doctorName || (d as any).doctor_name || (d as any).name)?.toString().trim();
          // 移除所有空格后比较（处理可能的空格问题）
          const normalizedName = name?.replace(/\s+/g, '');
          const normalizedInput = doctorName.replace(/\s+/g, '');
          
          // 精确匹配
          const exactMatch = name && name === doctorName;
          // 去除空格后匹配
          const normalizedMatch = normalizedName && normalizedName === normalizedInput;
          
          const match = exactMatch || normalizedMatch;
          
          if (match) {
            console.log('✓ 匹配到医生:', { 
              原始姓名: name, 
              输入姓名: doctorName,
              医生ID: d.doctorId || (d as any).doctor_id,
              完整数据: d 
            });
          } else if (name) {
            // 打印不匹配的医生信息，用于调试
            console.log('✗ 不匹配:', { 
              数据库姓名: name, 
              输入姓名: doctorName,
              是否包含: name.includes(doctorName) || doctorName.includes(name)
            });
          }
          
          return match;
        });

        console.log('最终匹配的医生数量:', matchedDoctors.length);
        if (matchedDoctors.length > 0) {
          console.log('匹配的医生列表:', matchedDoctors);
        }

        if (matchedDoctors.length === 0) {
          message.warning('未找到该医生，请检查姓名是否正确');
          scheduleForm.selectedDoctorInfo = null;
          scheduleForm.doctorId = undefined;
        } else if (matchedDoctors.length === 1) {
          // 只有一个匹配的医生，直接选择
          const doctor = matchedDoctors[0];
          // 如果医生信息中没有科室名称，从deptOptions中查找
          let deptName = doctor.deptName || '';
          if (!deptName && doctor.deptId) {
            const dept = deptOptions.value.find(d => d.value === doctor.deptId);
            deptName = dept ? dept.label : '';
          }
          
          scheduleForm.selectedDoctorInfo = {
            doctorId: doctor.doctorId,
            doctorName: doctor.doctorName,
            deptId: doctor.deptId,
            deptName: deptName,
          };
          scheduleForm.doctorId = doctor.doctorId;
          scheduleForm.deptId = doctor.deptId;
          message.success(`已选择医生：${doctor.doctorName}（${deptName || '科室ID: ' + doctor.deptId}）`);
        } else {
          // 有多个同名医生，显示选择弹窗
          doctorSelectModal.doctors = matchedDoctors;
          doctorSelectModal.currentDoctorName = doctorName;
          doctorSelectModal.selectedDoctorId = undefined;
          doctorSelectModal.visible = true;
        }
      } catch (error: any) {
        console.error('查询医生失败:', error);
        message.error('查询医生失败：' + (error?.message || '未知错误'));
        scheduleForm.selectedDoctorInfo = null;
        scheduleForm.doctorId = undefined;
      }
    }

    // 处理选择医生
    function handleSelectDoctor() {
      if (!doctorSelectModal.selectedDoctorId) {
        message.warning('请选择一个医生');
        return;
      }

      const selectedDoctor = doctorSelectModal.doctors.find(
        d => d.doctorId === doctorSelectModal.selectedDoctorId
      );

      if (selectedDoctor) {
        // 如果医生信息中没有科室名称，从deptOptions中查找
        let deptName = selectedDoctor.deptName || '';
        if (!deptName && selectedDoctor.deptId) {
          const dept = deptOptions.value.find(d => d.value === selectedDoctor.deptId);
          deptName = dept ? dept.label : '';
        }
        
        scheduleForm.selectedDoctorInfo = {
          doctorId: selectedDoctor.doctorId,
          doctorName: selectedDoctor.doctorName,
          deptId: selectedDoctor.deptId,
          deptName: deptName,
        };
        scheduleForm.doctorId = selectedDoctor.doctorId;
        scheduleForm.deptId = selectedDoctor.deptId;
        doctorSelectModal.visible = false;
        message.success(`已选择医生：${selectedDoctor.doctorName}（${deptName || '科室ID: ' + selectedDoctor.deptId}）`);
      }
    }

    // 取消选择医生
    function handleCancelSelectDoctor() {
      doctorSelectModal.visible = false;
      doctorSelectModal.selectedDoctorId = undefined;
      // 清空表单中的医生信息
      scheduleForm.doctorName = '';
      scheduleForm.selectedDoctorInfo = null;
      scheduleForm.doctorId = undefined;
    }

    function filterOption(input: string, option: any) {
      return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    }

    async function handleSaveSchedule() {
      if (!scheduleForm.doctorName || !scheduleForm.doctorName.trim()) {
        message.error('请输入医生姓名');
        return;
      }
      if (!scheduleForm.doctorId) {
        message.error('请先通过医生姓名选择医生');
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
            // 调用后端接口获取可用诊室（创建新排班时，没有原诊室号）
            try {
              roomNumber = await getAvailableRoom({
                date: dateStr,
                timeSlot: scheduleForm.timeSlot,
              });
            } catch (error) {
              console.error('获取可用诊室失败，使用默认值:', error);
              // 如果后端接口不存在，使用默认值
              roomNumber = '门诊101';
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

    // 自动生成排班相关方法
    function handleAutoGenerate() {
      autoGenerateForm.deptIds = [];
      autoGenerateForm.scheduleCount = 10;
      autoGenerateForm.timeSlots = [];
      autoGenerateForm.maxQuota = 50;
      autoGenerateForm.startDate = filters.date || dayjs();
      autoGenerateModal.visible = true;
    }

    function handleCancelAutoGenerate() {
      autoGenerateModal.visible = false;
    }

    async function handleGenerateSchedules() {
      if (!autoGenerateForm.deptIds || autoGenerateForm.deptIds.length === 0) {
        message.error('请选择至少一个科室');
        return;
      }
      if (!autoGenerateForm.scheduleCount || autoGenerateForm.scheduleCount < 1) {
        message.error('请输入有效的排班数量');
        return;
      }
      if (!autoGenerateForm.timeSlots || autoGenerateForm.timeSlots.length === 0) {
        message.error('请选择至少一个时段');
        return;
      }
      if (!autoGenerateForm.maxQuota || autoGenerateForm.maxQuota < 1) {
        message.error('请输入有效的最大号源数');
        return;
      }
      if (!autoGenerateForm.startDate) {
        message.error('请选择起始日期');
        return;
      }

      autoGenerateModal.loading = true;
      try {
        const result = await generateSchedules({
          deptIds: autoGenerateForm.deptIds,
          scheduleCount: autoGenerateForm.scheduleCount,
          timeSlots: autoGenerateForm.timeSlots,
          maxQuota: autoGenerateForm.maxQuota,
          startDate: autoGenerateForm.startDate.format('YYYY-MM-DD'),
        });
        
        if (result && Array.isArray(result)) {
          generatedSchedulesModal.schedules = result.map((item: any, index: number) => ({
            ...item,
            tempId: `temp_${Date.now()}_${index}`,
          }));
          autoGenerateModal.visible = false;
          generatedSchedulesModal.visible = true;
        } else {
          message.error('生成排班失败：返回数据格式错误');
        }
      } catch (error: any) {
        console.error('生成排班失败:', error);
        message.error('生成排班失败：' + (error?.message || '未知错误'));
      } finally {
        autoGenerateModal.loading = false;
      }
    }

    function handleEditGeneratedSchedule(record: any) {
      editGeneratedScheduleModal.form = {
        tempId: record.tempId,
        doctorId: record.doctorId,
        doctorName: record.doctorName,
        deptId: record.deptId,
        deptName: record.deptName,
        scheduleDate: dayjs(record.scheduleDate),
        timeSlot: record.timeSlot,
        maxQuota: record.maxQuota,
      };
      editGeneratedScheduleModal.visible = true;
    }

    function handleDeleteGeneratedSchedule(record: any) {
      const index = generatedSchedulesModal.schedules.findIndex(s => s.tempId === record.tempId);
      if (index !== -1) {
        generatedSchedulesModal.schedules.splice(index, 1);
        message.success('已删除');
      }
    }

    function handleSaveGeneratedSchedule() {
      if (!editGeneratedScheduleModal.form.scheduleDate) {
        message.error('请选择排班日期');
        return;
      }
      if (!editGeneratedScheduleModal.form.timeSlot) {
        message.error('请选择时段');
        return;
      }
      if (!editGeneratedScheduleModal.form.maxQuota || editGeneratedScheduleModal.form.maxQuota < 1) {
        message.error('请输入有效的最大号源数');
        return;
      }

      const index = generatedSchedulesModal.schedules.findIndex(
        s => s.tempId === editGeneratedScheduleModal.form.tempId
      );
      if (index !== -1) {
        generatedSchedulesModal.schedules[index] = {
          ...generatedSchedulesModal.schedules[index],
          scheduleDate: editGeneratedScheduleModal.form.scheduleDate.format('YYYY-MM-DD'),
          timeSlot: editGeneratedScheduleModal.form.timeSlot,
          maxQuota: editGeneratedScheduleModal.form.maxQuota,
        };
        message.success('修改成功');
        editGeneratedScheduleModal.visible = false;
      }
    }

    function handleCancelEditGeneratedSchedule() {
      editGeneratedScheduleModal.visible = false;
    }

    function handleCancelGeneratedSchedules() {
      generatedSchedulesModal.visible = false;
      generatedSchedulesModal.schedules = [];
    }

    async function handleConfirmGeneratedSchedules() {
      if (generatedSchedulesModal.schedules.length === 0) {
        message.warning('没有可保存的排班');
        return;
      }

      generatedSchedulesModal.loading = true;
      try {
        await batchCreateSchedules(generatedSchedulesModal.schedules.map(s => ({
          doctorId: s.doctorId,
          deptId: s.deptId,
          scheduleDate: s.scheduleDate,
          timeSlot: s.timeSlot,
          maxQuota: s.maxQuota,
          roomNumber: s.roomNumber,
        })));
        message.success(`成功保存 ${generatedSchedulesModal.schedules.length} 条排班记录`);
        generatedSchedulesModal.visible = false;
        generatedSchedulesModal.schedules = [];
        await loadList();
      } catch (error: any) {
        console.error('保存排班失败:', error);
        message.error('保存排班失败：' + (error?.message || '未知错误'));
      } finally {
        generatedSchedulesModal.loading = false;
      }
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
      handleDoctorNameBlur,
      handleDoctorNameInput,
      handleSelectDoctor,
      handleCancelSelectDoctor,
      doctorSelectModal,
      doctorSelectColumns,
      filterOption,
      handleDeptChange,
      autoGenerateModal,
      autoGenerateForm,
      generatedSchedulesModal,
      editGeneratedScheduleModal,
      generatedSchedulesColumns,
      handleAutoGenerate,
      handleCancelAutoGenerate,
      handleGenerateSchedules,
      handleEditGeneratedSchedule,
      handleDeleteGeneratedSchedule,
      handleSaveGeneratedSchedule,
      handleCancelEditGeneratedSchedule,
      handleCancelGeneratedSchedules,
      handleConfirmGeneratedSchedules,
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
