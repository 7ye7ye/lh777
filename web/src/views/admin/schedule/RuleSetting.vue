<template>
  <PageWrapper :title="'排班规则制定'">
    <div class="rule-setting-page">
      <!-- 月份选择器 -->
      <a-card :bordered="false" class="mb-4">
        <a-space>
          <span>选择月份：</span>
          <a-date-picker
            v-model:value="currentMonth"
            picker="month"
            format="YYYY年MM月"
            @change="handleMonthChange"
          />
          <a-button type="primary" @click="loadMonthSchedules">刷新</a-button>
        </a-space>
      </a-card>

      <!-- 排班日历 -->
      <a-card title="排班日历" :bordered="false" class="mb-4">
        <a-calendar v-model:value="calendarValue" :fullscreen="false">
          <template #dateCellRender="{ current }">
            <div class="calendar-cell" @click="handleDateClick(current)">
              <div class="cell-date">{{ current.date() }}</div>
              <div class="cell-schedules">
                <template v-for="slot in getDateSchedules(current)" :key="slot">
                  <a-tag :color="getSlotColor(slot)" size="small">{{ getSlotLabel(slot) }}</a-tag>
                </template>
              </div>
            </div>
          </template>
        </a-calendar>
      </a-card>

      <!-- 排班详情弹窗 -->
      <a-modal
        v-model:visible="detailModalVisible"
        :title="`${selectedDate ? selectedDate.format('YYYY年MM月DD日') : ''} 排班详情`"
        width="900px"
        :footer="null"
      >
        <div class="schedule-detail">
          <div class="detail-header">
            <a-space>
              <a-button type="primary" @click="handleAddSchedule">
                <template #icon><PlusOutlined /></template>
                添加排班
              </a-button>
              <a-button @click="loadDateSchedules">刷新</a-button>
            </a-space>
          </div>

          <a-table
            :data-source="dateSchedules"
            :columns="scheduleColumns"
            :pagination="{ pageSize: 10 }"
            row-key="scheduleId"
            class="mt-4"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'doctorName'">
                {{ record.doctorName || '未知医生' }}
              </template>
              <template v-else-if="column.key === 'deptName'">
                {{ record.deptName || '未知科室' }}
              </template>
              <template v-else-if="column.key === 'timeSlot'">
                <a-tag :color="getSlotColor(record.timeSlot)">
                  {{ getSlotLabel(record.timeSlot) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'status'">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">
                  {{ record.status === 1 ? '有效' : '停用' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="handleEditSchedule(record)">编辑</a-button>
                  <a-button type="link" size="small" danger @click="handleDeleteSchedule(record)">删除</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </div>
      </a-modal>

      <!-- 添加/编辑排班弹窗 -->
      <a-modal
        v-model:visible="scheduleFormVisible"
        :title="editingSchedule ? '编辑排班' : '添加排班'"
        width="600px"
        @ok="handleSaveSchedule"
        @cancel="handleCancelSchedule"
      >
        <a-form :model="scheduleForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
          <a-form-item label="医生" required>
            <a-select
              v-model:value="scheduleForm.doctorId"
              :options="doctorOptions"
              placeholder="选择医生"
              show-search
              :filter-option="filterOption"
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
          <a-form-item label="日期" required>
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
          <a-form-item label="最大号源">
            <a-input-number
              v-model:value="scheduleForm.maxQuota"
              :min="1"
              :max="100"
              style="width: 100%"
            />
          </a-form-item>
          <a-form-item label="诊室">
            <a-input v-model:value="scheduleForm.roomNumber" placeholder="请输入诊室号" />
          </a-form-item>
          <a-form-item label="状态">
            <a-radio-group v-model:value="scheduleForm.status">
              <a-radio :value="1">有效</a-radio>
              <a-radio :value="0">停用</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-form>
      </a-modal>

      <!-- 文件导入 -->
      <a-card title="排班文件导入" :bordered="false">
        <a-alert
          message="文件格式说明"
          description="请按照以下格式准备Excel或CSV文件：第一行为表头（医生姓名、科室名称、排班日期、时段、号源数量），从第二行开始为数据。排班日期格式：yyyy-MM-dd，时段：上午/下午/晚上，号源数量可选（不填则根据规则自动计算）。支持的文件格式：.xlsx、.xls、.csv"
          type="info"
          show-icon
          class="mb-4"
        />
        <a-upload
          :before-upload="handleBeforeUpload"
          :file-list="fileList"
          accept=".xlsx,.xls,.csv"
          :max-count="1"
        >
          <a-button type="primary">
            <template #icon><UploadOutlined /></template>
            选择文件（Excel/CSV）
          </a-button>
        </a-upload>
        <div class="mt-4">
          <a-button
            type="primary"
            :loading="uploading"
            :disabled="!fileList || fileList.length === 0"
            @click="handleUpload"
          >
            开始导入
          </a-button>
        </div>
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, onMounted } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { UploadOutlined, PlusOutlined } from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import type { UploadFile, UploadProps } from 'ant-design-vue';
import {
  getScheduleList,
  createSchedule,
  updateSchedule,
  deleteSchedule,
  importScheduleExcel,
  type DoctorSchedule,
} from '/@/api/hospital/schedule';
import { getDoctorList } from '/@/api/hospital/doctor';
import { getDepartmentList } from '/@/api/hospital/department';

export default defineComponent({
  name: 'AdminScheduleRules',
  components: { PageWrapper, UploadOutlined, PlusOutlined },
  setup() {
    const currentMonth = ref<Dayjs>(dayjs());
    const calendarValue = ref<Dayjs>(dayjs());
    const selectedDate = ref<Dayjs | null>(null);
    const detailModalVisible = ref(false);
    const scheduleFormVisible = ref(false);
    const editingSchedule = ref<DoctorSchedule | null>(null);

    // 排班数据
    const monthSchedules = ref<Map<string, DoctorSchedule[]>>(new Map());
    const dateSchedules = ref<DoctorSchedule[]>([]);

    // 选项数据
    const doctorOptions = ref<{ label: string; value: number }[]>([]);
    const deptOptions = ref<{ label: string; value: number }[]>([]);

    // 文件上传
    const fileList = ref<UploadFile[]>([]);
    const uploading = ref(false);
    let currentFile: File | null = null;

    // 排班表单
    const scheduleForm = reactive({
      scheduleId: undefined as number | undefined,
      doctorId: undefined as number | undefined,
      deptId: undefined as number | undefined,
      scheduleDate: null as Dayjs | null,
      timeSlot: undefined as number | undefined,
      maxQuota: 50 as number,
      roomNumber: '' as string,
      status: 1 as number,
    });

    // 表格列
    const scheduleColumns = [
      { title: '医生', key: 'doctorName', dataIndex: 'doctorName', width: 120 },
      { title: '科室', key: 'deptName', dataIndex: 'deptName', width: 120 },
      { title: '时段', key: 'timeSlot', dataIndex: 'timeSlot', width: 100 },
      { title: '最大号源', dataIndex: 'maxQuota', width: 100 },
      { title: '已用号源', dataIndex: 'usedQuota', width: 100 },
      { title: '诊室', dataIndex: 'roomNumber', width: 100 },
      { title: '状态', key: 'status', dataIndex: 'status', width: 80 },
      { title: '操作', key: 'action', width: 150 },
    ];

    // 加载选项数据
    async function loadOptions() {
      try {
        // 加载医生列表
        const doctorResponse = await getDoctorList({});
        let doctorList: any[] = [];
        if (Array.isArray(doctorResponse)) {
          doctorList = doctorResponse;
        } else if (doctorResponse?.records && Array.isArray(doctorResponse.records)) {
          doctorList = doctorResponse.records;
        }
        doctorOptions.value = doctorList.map((d: any) => ({
          label: d.doctorName || d.name || '',
          value: d.doctorId || d.id || 0,
        }));

        // 加载科室列表
        const deptList = await getDepartmentList();
        deptOptions.value = (deptList || []).map((d: any) => ({
          label: d.deptName || d.name || '',
          value: d.deptId || d.id || 0,
        }));
      } catch (error: any) {
        console.error('加载选项数据失败:', error);
        message.error('加载选项数据失败');
      }
    }

    // 加载月份排班数据
    async function loadMonthSchedules() {
      try {
        const startDate = currentMonth.value.startOf('month').format('YYYY-MM-DD');
        const endDate = currentMonth.value.endOf('month').format('YYYY-MM-DD');
        const response = await getScheduleList({
          startDate,
          endDate,
        });

        // 后端直接返回数组
        const schedules = Array.isArray(response) ? response : [];
        const scheduleMap = new Map<string, DoctorSchedule[]>();

        schedules.forEach((schedule: DoctorSchedule) => {
          const date = schedule.scheduleDate;
          if (!scheduleMap.has(date)) {
            scheduleMap.set(date, []);
          }
          scheduleMap.get(date)!.push(schedule);
        });

        monthSchedules.value = scheduleMap;
      } catch (error: any) {
        console.error('加载月份排班失败:', error);
        message.error('加载月份排班失败');
      }
    }

    // 加载指定日期的排班
    async function loadDateSchedules() {
      if (!selectedDate.value) return;

      try {
        const dateStr = selectedDate.value.format('YYYY-MM-DD');
        
        // 使用date参数进行精确日期查询，确保只查询指定日期的数据
        const response = await getScheduleList({
          date: dateStr,  // 使用date参数进行精确查询
        });
        
        // 后端直接返回数组，且已包含医生和科室名称
        let schedules = Array.isArray(response) ? response : [];
        
        // 双重保险：确保只显示指定日期的数据（防止后端返回其他日期的数据）
        schedules = schedules.filter((schedule: DoctorSchedule) => {
          return schedule.scheduleDate === dateStr;
        });
        
        // 如果后端没有返回名称，则从选项列表中查找
        const schedulesWithNames = schedules.map((schedule: DoctorSchedule) => {
          if (!schedule.doctorName && schedule.doctorId) {
            const doctor = doctorOptions.value.find((d) => d.value === schedule.doctorId);
            if (doctor) schedule.doctorName = doctor.label;
          }
          if (!schedule.deptName && schedule.deptId) {
            const dept = deptOptions.value.find((d) => d.value === schedule.deptId);
            if (dept) schedule.deptName = dept.label;
          }
          return schedule;
        });

        dateSchedules.value = schedulesWithNames;
      } catch (error: any) {
        console.error('加载日期排班失败:', error);
        message.error('加载日期排班失败：' + (error?.message || '未知错误'));
      }
    }

    // 获取日期对应的排班时段
    function getDateSchedules(current: Dayjs): number[] {
      const dateStr = current.format('YYYY-MM-DD');
      const schedules = monthSchedules.value.get(dateStr) || [];
      return [...new Set(schedules.map((s) => s.timeSlot))];
    }

    // 时段标签
    function getSlotLabel(slot: number): string {
      const map: any = { 1: '上午', 2: '下午', 3: '晚上' };
      return map[slot] || slot.toString();
    }

    // 时段颜色
    function getSlotColor(slot: number): string {
      const map: any = { 1: 'green', 2: 'blue', 3: 'purple' };
      return map[slot] || 'default';
    }

    // 点击日期
    function handleDateClick(date: Dayjs) {
      selectedDate.value = date;
      detailModalVisible.value = true;
      loadDateSchedules();
    }

    // 月份变化
    function handleMonthChange() {
      calendarValue.value = currentMonth.value;
      loadMonthSchedules();
    }

    // 添加排班
    function handleAddSchedule() {
      editingSchedule.value = null;
      scheduleForm.scheduleId = undefined;
      scheduleForm.doctorId = undefined;
      scheduleForm.deptId = undefined;
      scheduleForm.scheduleDate = selectedDate.value ? selectedDate.value : null;
      scheduleForm.timeSlot = undefined;
      scheduleForm.maxQuota = 50;
      scheduleForm.roomNumber = '';
      scheduleForm.status = 1;
      scheduleFormVisible.value = true;
    }

    // 编辑排班
    function handleEditSchedule(record: DoctorSchedule) {
      editingSchedule.value = record;
      scheduleForm.scheduleId = record.scheduleId;
      scheduleForm.doctorId = record.doctorId;
      scheduleForm.deptId = record.deptId;
      scheduleForm.scheduleDate = dayjs(record.scheduleDate);
      scheduleForm.timeSlot = record.timeSlot;
      scheduleForm.maxQuota = record.maxQuota || 50;
      scheduleForm.roomNumber = record.roomNumber || '';
      scheduleForm.status = record.status;
      scheduleFormVisible.value = true;
    }

    // 删除排班
    function handleDeleteSchedule(record: DoctorSchedule) {
      Modal.confirm({
        title: '确认删除',
        content: `确定要删除 ${record.doctorName || '该'} 医生的排班吗？`,
        onOk: async () => {
          try {
            await deleteSchedule(record.scheduleId);
            message.success('删除成功');
            loadDateSchedules();
            loadMonthSchedules();
          } catch (error: any) {
            message.error('删除失败：' + (error?.message || '未知错误'));
          }
        },
      });
    }

    // 将时段转换为后端需要的格式
    function getShiftString(timeSlot: number): string {
      const map: any = { 1: 'morning', 2: 'afternoon', 3: 'evening' };
      return map[timeSlot] || 'morning';
    }

    // 保存排班
    async function handleSaveSchedule() {
      if (!scheduleForm.doctorId || !scheduleForm.deptId || !scheduleForm.scheduleDate || !scheduleForm.timeSlot) {
        message.warning('请填写完整信息');
        return;
      }

      try {
        const dateStr = scheduleForm.scheduleDate.format('YYYY-MM-DD');
        const shiftStr = getShiftString(scheduleForm.timeSlot);
        
        if (editingSchedule.value) {
          // 更新
          await updateSchedule({
            scheduleId: scheduleForm.scheduleId!,
            doctorId: scheduleForm.doctorId,
            deptId: scheduleForm.deptId,
            date: dateStr,
            shift: shiftStr,
            slots: scheduleForm.maxQuota,
            bookedSlots: editingSchedule.value.usedQuota || 0,
            status: scheduleForm.status,
            remark: scheduleForm.roomNumber,
            roomNumber: scheduleForm.roomNumber,
            maxQuota: scheduleForm.maxQuota,
          });
          message.success('更新成功');
        } else {
          // 创建
          await createSchedule({
            doctorId: scheduleForm.doctorId,
            deptId: scheduleForm.deptId,
            date: dateStr,
            shift: shiftStr,
            slots: scheduleForm.maxQuota,
            remark: scheduleForm.roomNumber,
            roomNumber: scheduleForm.roomNumber,
            maxQuota: scheduleForm.maxQuota,
          });
          message.success('添加成功');
        }

        scheduleFormVisible.value = false;
        loadDateSchedules();
        loadMonthSchedules();
      } catch (error: any) {
        message.error('保存失败：' + (error?.message || '未知错误'));
      }
    }

    // 取消保存
    function handleCancelSchedule() {
      scheduleFormVisible.value = false;
      editingSchedule.value = null;
    }

    // 文件上传前
    const handleBeforeUpload: UploadProps['beforeUpload'] = (file) => {
      const isExcel =
        file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
        file.type === 'application/vnd.ms-excel' ||
        file.name.endsWith('.xlsx') ||
        file.name.endsWith('.xls');
      const isCsv =
        file.type === 'text/csv' ||
        file.type === 'text/plain' ||
        file.type === 'application/csv' ||
        file.name.endsWith('.csv');
      if (!isExcel && !isCsv) {
        message.error('只能上传Excel或CSV文件（.xlsx、.xls或.csv格式）');
        return false;
      }
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        message.error('文件大小不能超过10MB');
        return false;
      }
      currentFile = file;
      fileList.value = [file as UploadFile];
      return false; // 阻止自动上传
    };

    // 文件上传
    async function handleUpload() {
      if (!currentFile) {
        message.warning('请先选择文件');
        return;
      }

      uploading.value = true;
      try {
        const result = await importScheduleExcel(currentFile, 30, 'am-pm-night', 1);
        if (result.success) {
          message.success(result.message || '导入成功');
          fileList.value = [];
          currentFile = null;
          loadMonthSchedules();
        } else {
          message.error(result.message || '导入失败');
        }
      } catch (error: any) {
        console.error('导入失败', error);
        message.error(error.message || '导入失败，请检查文件格式');
      } finally {
        uploading.value = false;
      }
    }

    // 筛选选项
    function filterOption(input: string, option: any) {
      return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    }

    // 初始化
    onMounted(async () => {
      await loadOptions();
      await loadMonthSchedules();
    });

    return {
      currentMonth,
      calendarValue,
      selectedDate,
      detailModalVisible,
      scheduleFormVisible,
      editingSchedule,
      dateSchedules,
      doctorOptions,
      deptOptions,
      fileList,
      uploading,
      scheduleForm,
      scheduleColumns,
      getDateSchedules,
      getSlotLabel,
      getSlotColor,
      handleDateClick,
      handleMonthChange,
      loadMonthSchedules,
      loadDateSchedules,
      handleAddSchedule,
      handleEditSchedule,
      handleDeleteSchedule,
      handleSaveSchedule,
      handleCancelSchedule,
      handleBeforeUpload,
      handleUpload,
      filterOption,
    };
  },
});
</script>

<style scoped>
.rule-setting-page {
  padding: 16px;
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}

.calendar-cell {
  min-height: 60px;
  padding: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.calendar-cell:hover {
  background-color: #f0f0f0;
}

.cell-date {
  font-weight: 500;
  margin-bottom: 4px;
}

.cell-schedules {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

.schedule-detail {
  min-height: 400px;
}

.detail-header {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
