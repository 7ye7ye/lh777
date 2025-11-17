<template>
  <PageWrapper :title="'数据统计'">
    <div class="p-4">
      <a-card :bordered="false" class="mb-3">
        <a-form layout="inline">
          <a-form-item label="日期范围">
            <a-range-picker v-model:value="range" />
          </a-form-item>
          <a-button type="primary" @click="load">统计</a-button>
        </a-form>
      </a-card>

      <a-row :gutter="16">
        <a-col :xs="24" :md="8">
          <a-card :bordered="false">
            <a-statistic title="门诊量" :value="stats.clinicCount" />
          </a-card>
        </a-col>
        <a-col :xs="24" :md="8">
          <a-card :bordered="false">
            <a-statistic title="科室负荷(%)" :value="stats.deptLoad" suffix="%" />
          </a-card>
        </a-col>
        <a-col :xs="24" :md="8">
          <a-card :bordered="false">
            <a-statistic title="退号率(%)" :value="stats.refundRate" suffix="%" />
          </a-card>
        </a-col>
      </a-row>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';
import { PageWrapper } from '/@/components/Page';

export default defineComponent({
  name: 'AdminStatistics',
  components: { PageWrapper },
  setup() {
    const range = ref<any>();
    const stats = reactive({ clinicCount: 0, deptLoad: 0, refundRate: 0 });
    function load() {
      stats.clinicCount = 1234; stats.deptLoad = 67; stats.refundRate = 4.3 as any;
    }
    return { range, stats, load };
  },
});
</script>

<style scoped>
.p-4 { padding: 16px; }
.mb-3 { margin-bottom: 12px; }
</style>


