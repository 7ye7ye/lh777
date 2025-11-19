<template>
  <PageWrapper :title="'更改排班'">
    <div class="p-4">
      <a-card :bordered="false">
        <a-descriptions title="申请信息" bordered :column="1" size="small" class="mb-4" v-if="detail">
          <a-descriptions-item label="医生">{{ detail.doctorName }}</a-descriptions-item>
          <a-descriptions-item label="原排班ID">{{ detail.originalScheduleId }}</a-descriptions-item>
          <a-descriptions-item label="目标日期(申请)">{{ detail.targetDate }}</a-descriptions-item>
          <a-descriptions-item label="目标时段(申请)">{{ slotLabel(detail.targetTimeSlot) }}</a-descriptions-item>
          <a-descriptions-item label="申请理由">{{ detail.reason }}</a-descriptions-item>
        </a-descriptions>

        <a-form layout="vertical" :model="form" class="mt-2" style="max-width: 520px">
          <a-form-item label="调整日期" required>
            <a-date-picker v-model:value="form.scheduleDate" style="width: 100%" />
          </a-form-item>
          <a-form-item label="调整时段" required>
            <a-select v-model:value="form.timeSlot" :options="timeOptions" />
          </a-form-item>
          <a-form-item label="调整科室">
            <a-select v-model:value="form.deptId" :options="deptOptions" show-search allowClear />
          </a-form-item>

          <a-space>
            <a-button type="primary" :loading="saving" @click="handleSubmit">提交审批并保存</a-button>
            <a-button @click="handleBack">返回</a-button>
          </a-space>
        </a-form>
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, onMounted, reactive, ref } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { useRoute } from 'vue-router';
import { useGo } from '/@/hooks/web/usePage';
import { approveShiftChange, getShiftChangeDetail, type ShiftChangeRequest } from '/@/api/hospital/shiftChange';
import { message } from 'ant-design-vue';
import { getDepartmentList } from '/@/api/hospital/department';
import type { Dayjs } from 'dayjs';

export default defineComponent({
  name: 'AdminScheduleAdjustmentChange',
  components: { PageWrapper },
  setup() {
    const route = useRoute();
    const go = useGo();
    const id = Number(route.params.id);
    const detail = ref<ShiftChangeRequest | null>(null);

    const form = reactive({
      scheduleDate: '' as string | Dayjs,
      timeSlot: 1,
      deptId: undefined as number | undefined,
    });
    const timeOptions = [
      { label: '上午', value: 1 },
      { label: '下午', value: 2 },
      { label: '晚上', value: 3 },
    ];
    const deptOptions = ref<{ label: string; value: number }[]>([]);
    const saving = ref(false);

    function slotLabel(v: number) {
      const map: any = { 1: '上午', 2: '下午', 3: '晚上' };
      return map[v] || v;
    }

    async function loadDetail() {
      try {
        const data = await getShiftChangeDetail(id);
        if (!data) {
          message.error('获取排班调整详情失败');
          return;
        }

        detail.value = data;
        // 默认将表单初始化为申请的目标，添加空值检查
        form.scheduleDate = data?.targetDate || '';
        form.timeSlot = data?.targetTimeSlot || 1;
        if (data?.targetDeptId) {
          form.deptId = data.targetDeptId;
        }
      } catch (error) {
        console.error('加载排班调整详情失败:', error);
        message.error('加载排班调整详情失败');
      }
    }

    async function loadDeptOptions() {
      try {
        const list = await getDepartmentList();
        deptOptions.value = (list || []).map((d: any) => ({ label: d.deptName, value: d.deptId }));
      } catch (error) {
        console.error('加载科室列表失败:', error);
        deptOptions.value = [];
      }
    }

    function formatDate(date: string | Dayjs): string {
      if (typeof date === 'string') {
        return date;
      }
      // 如果是 Dayjs 对象，调用 format 方法
      return date.format('YYYY-MM-DD');
    }

    async function handleSubmit() {
      if (!form.scheduleDate || !form.timeSlot) {
        message.warning('请完善日期与时段');
        return;
      }
      try {
        saving.value = true;
        await approveShiftChange({
          id,
          scheduleDate: formatDate(form.scheduleDate),
          timeSlot: form.timeSlot,
          deptId: form.deptId,
        });
        message.success('审批通过并已保存更改');
        go('/admin/schedule-adjustment');
      } catch (error) {
        console.error('提交审批失败:', error);
        message.error('提交审批失败');
      } finally {
        saving.value = false;
      }
    }

    function handleBack() {
      go('/admin/schedule-adjustment');
    }

    onMounted(async () => {
      try {
        await Promise.all([loadDetail(), loadDeptOptions()]);
      } catch (error) {
        console.error('初始化页面失败:', error);
        message.error('页面初始化失败');
      }
    });

    return { detail, form, timeOptions, deptOptions, saving, slotLabel, handleSubmit, handleBack };
  },
});
</script>

<style scoped>
.p-4 { padding: 16px; }
.mb-4 { margin-bottom: 16px; }
.mt-2 { margin-top: 8px; }
</style>
