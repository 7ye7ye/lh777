<template>
  <PageWrapper :title="'排班规则制定'">
    <div class="page">
      <a-row :gutter="16">
        <a-col :xs="24" :lg="12">
          <a-card title="基础规则设置" :bordered="false">
            <a-form layout="vertical">
              <a-form-item label="医生出诊频率(次/周)">
                <a-input-number v-model:value="form.basic.frequencyPerWeek" :min="0" />
              </a-form-item>
              <a-form-item label="单次时长(分钟)">
                <a-input-number v-model:value="form.basic.durationMinutes" :min="0" />
              </a-form-item>
              <a-form-item label="可预约时段划分">
                <a-select v-model:value="form.basic.timeSlot" :options="timeSlots" style="width: 240px" />
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="12">
          <a-card title="科室差异化规则" :bordered="false">
            <a-form layout="vertical">
              <a-form-item label="选择科室">
                <a-select v-model:value="form.deptRule.department" :options="departments" style="width: 240px" />
              </a-form-item>
              <a-form-item label="是否允许加班出诊">
                <a-switch v-model:checked="form.deptRule.allowOvertime" />
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>
      </a-row>

      <div class="section">
        <a-space>
          <a-button type="primary" @click="handleSave">保存</a-button>
          <a-button @click="handleReset">重置</a-button>
        </a-space>
      </div>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { PageWrapper } from '/@/components/Page';

export default defineComponent({
  name: 'AdminScheduleRules',
  components: { PageWrapper },
  setup() {
    const timeSlots = [
      { label: '按上午/下午/晚上', value: 'am-pm-night' },
      { label: '按小时', value: 'hourly' },
    ];
    const departments = [
      { label: '内科', value: 'internal' },
      { label: '外科', value: 'surgery' },
    ];
    const form = reactive({
      basic: { frequencyPerWeek: 0, durationMinutes: 30, timeSlot: 'am-pm-night' },
      deptRule: { department: 'internal', allowOvertime: false },
    });

    function handleSave() {
      console.log('save schedule rule', JSON.stringify(form));
    }
    function handleReset() {
      form.basic.frequencyPerWeek = 0;
      form.basic.durationMinutes = 30;
      form.basic.timeSlot = 'am-pm-night';
      form.deptRule.department = 'internal';
      form.deptRule.allowOvertime = false;
    }
    return { form, timeSlots, departments, handleSave, handleReset };
  },
});
</script>

<style scoped>
.page { padding: 16px; }
.section { margin-top: 16px; }
</style>


