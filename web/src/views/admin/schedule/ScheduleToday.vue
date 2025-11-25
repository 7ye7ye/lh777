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
                    <a-upload
                      name="file"
                      accept=".xlsx,.xls"
                      :showUploadList="false"
                      :beforeUpload="handleExcelUpload"
                    >
                      <a-button type="primary" size="small" preIcon="ant-design:upload-outlined">
                        上传Excel排班
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
import * as XLSX from 'xlsx';
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
    const rows = ref<(TodayScheduleItem & { doctorName?: string; deptName?: string })[]>([]);

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

    // 处理Excel文件上传
    async function handleExcelUpload(file: File) {
      const fileExtension = file.name.split('.').pop()?.toLowerCase();
      if (fileExtension !== 'xlsx' && fileExtension !== 'xls') {
        message.error('请上传Excel文件（.xlsx或.xls格式）');
        return false;
      }

      try {
        // 读取Excel文件
        const data = await file.arrayBuffer();
        const workbook = XLSX.read(data, { type: 'array' });
        
        // 获取第一个工作表
        const firstSheetName = workbook.SheetNames[0];
        const worksheet = workbook.Sheets[firstSheetName];
        
        // 将工作表转换为JSON数组
        const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 }) as any[][];
        
        if (jsonData.length < 2) {
          message.error('Excel文件至少需要包含表头和数据行');
          return false;
        }

        // 解析表头（第一行）
        const headers = jsonData[0].map((h: any) => String(h || '').trim().toLowerCase());
        
        // 查找列索引
        const dateIndex = findColumnIndex(headers, ['日期', 'date', '排班日期', 'scheduleDate']);
        const doctorNameIndex = findColumnIndex(headers, ['医生', 'doctor', '医生姓名', 'doctorName', '姓名']);
        const deptNameIndex = findColumnIndex(headers, ['科室', 'dept', '科室名称', 'deptName', '部门']);
        const timeSlotIndex = findColumnIndex(headers, ['时段', 'timeslot', 'timeSlot', '时间段', '班次']);
        const roomIndex = findColumnIndex(headers, ['诊室', 'room', 'roomNumber', '诊室号', '房间']);

        if (dateIndex === -1 || doctorNameIndex === -1) {
          message.error('Excel文件必须包含"日期"和"医生"列');
          return false;
        }

        // 解析数据行
        const parsedSchedules: TodayScheduleItem[] = [];
        const errors: string[] = [];

        for (let i = 1; i < jsonData.length; i++) {
          const row = jsonData[i];
          if (!row || row.length === 0) continue;

          try {
            // 解析日期
            let scheduleDate = '';
            const dateValue = row[dateIndex];
            if (dateValue) {
              if (typeof dateValue === 'string') {
                scheduleDate = dayjs(dateValue).format('YYYY-MM-DD');
              } else if (dateValue instanceof Date) {
                scheduleDate = dayjs(dateValue).format('YYYY-MM-DD');
              } else if (typeof dateValue === 'number') {
                // Excel日期序列号
                const excelDate = XLSX.SSF.parse_date_code(dateValue);
                scheduleDate = dayjs(`${excelDate.y}-${excelDate.m}-${excelDate.d}`).format('YYYY-MM-DD');
              }
            }

            if (!scheduleDate || scheduleDate === 'Invalid Date') {
              errors.push(`第${i + 1}行：日期格式错误`);
              continue;
            }

            // 解析医生姓名
            const doctorName = String(row[doctorNameIndex] || '').trim();
            if (!doctorName) {
              errors.push(`第${i + 1}行：医生姓名不能为空`);
              continue;
            }

            // 解析科室名称
            const deptName = deptNameIndex !== -1 ? String(row[deptNameIndex] || '').trim() : '';

            // 解析时段（1-上午，2-下午，3-晚上）
            let timeSlot = 1;
            if (timeSlotIndex !== -1) {
              const timeSlotValue = String(row[timeSlotIndex] || '').trim().toLowerCase();
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
            const roomNumber = roomIndex !== -1 ? String(row[roomIndex] || '').trim() : '';

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
          const key = `${s.scheduleDate}-${s.doctorName}-${s.timeSlot}`;
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
        console.error('Excel解析失败：', error);
        message.error('Excel文件解析失败：' + (error.message || '未知错误'));
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
      handleExcelUpload,
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
