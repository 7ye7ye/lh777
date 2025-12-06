<template>
  <div class="add-number-source-container">
    <div class="page-header">
      <h2>{{ t('routes.admin.addNumberSource') }}</h2>
    </div>

    <!-- 表单选择科室/医生/查询天数 -->
    <a-card :bordered="false" class="form-card">
      <a-form :model="form" :rules="rules" ref="formRef" layout="horizontal">
        <a-row :gutter="16">
          <a-col :xs="24" :md="6">
            <a-form-item
              name="deptId"
              label="所属科室"
              :label-col="{ span: 8 }"
              :wrapper-col="{ span: 16 }"
            >
              <a-select
                v-model:value="form.deptId"
                placeholder="请选择科室"
                allow-clear
                :loading="isDeptLoading"
                @change="onDeptChange"
              >
                <a-select-option
                  v-for="dept in departmentList"
                  :key="dept.deptId"
                  :value="dept.deptId"
                >
                  {{ dept.deptName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :xs="24" :md="6">
            <a-form-item
              name="doctorId"
              label="医生"
              :label-col="{ span: 8 }"
              :wrapper-col="{ span: 16 }"
            >
              <a-select
                v-model:value="form.doctorId"
                placeholder="请选择医生"
                allow-clear
                :loading="isDoctorLoading"
              >
                <a-select-option
                  v-for="doctor in doctorList"
                  :key="doctor.doctorId"
                  :value="doctor.doctorId"
                >
                  {{ doctor.doctorName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :xs="24" :md="6">
            <a-form-item
              name="days"
              label="查询天数"
              :label-col="{ span: 8 }"
              :wrapper-col="{ span: 16 }"
            >
              <a-input-number v-model:value="form.days" :min="1" :max="30" />
            </a-form-item>
          </a-col>

          <a-col :xs="24" :md="6">
            <a-button type="primary" @click="loadSchedule">查询排班</a-button>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- 排班信息表格 -->
    <a-card :bordered="false" class="form-card" style="margin-top: 16px;">
      <a-table
        :columns="columns"
        :data-source="scheduleList"
        :rowKey="record => record.scheduleId"
        :loading="isScheduleLoading"
        :pagination="false"
      >
        <template #action="{ record }">
          <a-button type="primary" size="small" @click="addNumber(record)">增加号源</a-button>
        </template>
        <template #numberType="{ record }">
          {{ getNumberType(record.timeSlot) }}
        </template>
        <template #remainCount="{ record }">
          {{ getRemainCount(record) }}
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, h} from 'vue';
import { useI18n } from '/@/hooks/web/useI18n';
import type { FormInstance } from 'ant-design-vue';
import dayjs from 'dayjs';
import { getDepartmentList } from '/@/api/hospital/department';
import { getDoctorsByDepartment } from '/@/api/hospital/doctor';
import {addQuotaAndFillQueue, getDoctorSchedules } from '/@/api/hospital/schedule';
import { Modal, InputNumber, message } from 'ant-design-vue';
import type { Department } from '/@/api/hospital/department';
import type { Doctor } from '/@/api/hospital/doctor';
import type { Schedule } from '/@/api/hospital/schedule';


const { t } = useI18n();
const formRef = ref<FormInstance | null>(null);

// 表单数据
const form = ref({
  deptId: undefined as number | undefined,
  doctorId: undefined as number | undefined,
  days: 7,
});

// 表单校验规则
const rules = ref({
  deptId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  days: [{ required: true, message: '请输入查询天数', trigger: 'change' }],
});

// 科室和医生
const departmentList = ref<Department[]>([]);
const isDeptLoading = ref(false);
const doctorList = ref<Doctor[]>([]);
const isDoctorLoading = ref(false);

// 排班数据
const scheduleList = ref<Schedule[]>([]);
const isScheduleLoading = ref(false);

// 表格列
const columns = [
  { title: '号源日期', dataIndex: 'scheduleDate', key: 'scheduleDate' },
  { title: '号源类型', key: 'numberType', slots: { customRender: 'numberType' } },
  { title: '号源总数', dataIndex: 'maxQuota', key: 'maxQuota' },
  { title: '已用号源', dataIndex: 'usedQuota', key: 'usedQuota' },
  { title: '剩余号源', key: 'remainCount', slots: { customRender: 'remainCount' } },
  { title: '操作', key: 'action', slots: { customRender: 'action' } },
];

// 号源余量
const getRemainCount = (record: Schedule) => {
  return (record.maxQuota ?? 0) - (record.usedQuota ?? 0);
};

// 加载科室
const loadDepartments = async () => {
  try {
    isDeptLoading.value = true;
    const data = await getDepartmentList();
    departmentList.value = Array.isArray(data) ? data : [];
  } finally {
    isDeptLoading.value = false;
  }
};

// 选择科室
const onDeptChange = async (deptId: number) => {
  form.value.doctorId = undefined;
  doctorList.value = [];
  scheduleList.value = [];
  if (!deptId) return;
  try {
    isDoctorLoading.value = true;
    const data = await getDoctorsByDepartment(deptId);
    doctorList.value = data || [];
  } finally {
    isDoctorLoading.value = false;
  }
};

// 加载排班
const loadSchedule = async () => {
  scheduleList.value = [];
  if (!form.value.doctorId || !form.value.days) return;

  try {
    isScheduleLoading.value = true;
    const startDate = dayjs().format('YYYY-MM-DD');

    const res = await getDoctorSchedules({
      doctorId: form.value.doctorId,
      startDate,
      days: form.value.days,
    });

    console.log('接口原始返回:', res);

    scheduleList.value = (res || []).map((item: any): Schedule => ({
      scheduleId: item.schedule_id,
      scheduleDate: item.schedule_date,
      timeSlot: item.time_slot,
      maxQuota: item.max_quota,
      usedQuota: item.used_quota,
      status: item.status,
      doctorId: form.value.doctorId || 0,
      deptId: form.value.deptId || 0,
      doctorName: undefined,
      deptName: undefined,
      createTime: undefined,
      updateTime: undefined,
    }));

    if (scheduleList.value.length === 0) {
      console.info(`该医生未来 ${form.value.days} 天没有排班`);
    }
  } finally {
    isScheduleLoading.value = false;
  }
};

// 点击新增号源
const addNumber = (record: Schedule) => {
  const inputValue = ref<number>(1);

  Modal.confirm({
    title: '新增号源',
    content: () =>
      h('div', { style: 'display:flex; flex-direction:column; gap:8px;' }, [
        h('span', '请输入新增号源数量：'),
        h(InputNumber, {
          min: 1,
          value: inputValue.value,
          style: 'width: 100%',
          onChange: (val: number) => {
            inputValue.value = val;
          },
        }),
      ]),
    okText: '新增',
    cancelText: '取消',
    onOk: () => {
      return new Promise<void>(async (resolve, reject) => {
        if (inputValue.value <= 0) {
          message.warning('请输入有效数量');
          reject(); // 阻止关闭
          return;
        }
        try {
          const ok = await addQuotaAndFillQueue(record.scheduleId, inputValue.value);
          if (ok) {
            message.success(`号源已增加 ${inputValue.value} 个`);
            // 重新加载排班列表，获取最新的 used_quota 和 max_quota
            await loadSchedule();
            resolve();
          } else {
            message.error('增加号源失败');
            reject();
          }
        } catch (err) {
          message.error('增加号源失败');
          reject();
        }
      });
    },
  });
};

// 号源类型
const getNumberType = (timeSlot: number) => {
  return timeSlot === 1 ? '上午号源' : timeSlot === 2 ? '下午号源' : '晚上号源';
};

onMounted(() => {
  loadDepartments();
});
</script>

<style lang="less" scoped>
.add-number-source-container {
  padding: 16px;
}
.page-header {
  margin-bottom: 24px;
  h2 {
    font-size: 18px;
    font-weight: 500;
  }
}
.form-card {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 16px;
}
</style>
