<template>
  <PageWrapper :title="'报表生成'">
    <div class="p-4">
      <a-card :bordered="false">
        <a-form layout="inline" class="mb-3">
          <a-form-item label="报表类型">
            <a-select v-model:value="type" :options="types" style="width: 200px" />
          </a-form-item>
          <a-button type="primary" @click="exportExcel">导出Excel</a-button>
        </a-form>
        <a-alert type="info" show-icon message="将生成含统计数据与图表的Excel文件" />
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { PageWrapper } from '/@/components/Page';

export default defineComponent({
  name: 'AdminReportExport',
  components: { PageWrapper },
  setup() {
    const type = ref('clinic');
    const types = [
      { label: '门诊量', value: 'clinic' },
      { label: '科室统计', value: 'dept' },
      { label: '挂号量', value: 'registration' },
    ];
    function exportExcel() {
      console.log('export report', type.value);
    }
    return { type, types, exportExcel };
  },
});
</script>

<style scoped>
.p-4 { padding: 16px; }
.mb-3 { margin-bottom: 12px; }
</style>


