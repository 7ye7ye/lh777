<template>
  <PageWrapper :title="'号源预约规则'">
    <div class="p-4">
      <a-card :bordered="false">
        <a-form layout="vertical">
          <a-row :gutter="16">
            <a-col :xs="24" :sm="12" :xl="8">
              <a-form-item label="可预约天数范围">
                <a-input-number v-model:value="form.daysAhead" :min="0" class="w-full" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :xl="8">
              <a-form-item label="每日开放时间">
                <a-time-range-picker v-model:value="form.openRange" class="w-full" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :xl="8">
              <a-form-item label="每日早于几点不放号">
                <a-time-picker v-model:value="form.earlyCutoff" class="w-full" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
        <div class="page-actions">
          <a-space>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="save">保存</a-button>
          </a-space>
        </div>
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { PageWrapper } from '/@/components/Page';

export default defineComponent({
  name: 'AdminReservationRule',
  components: { PageWrapper },
  setup() {
    const form = reactive({ daysAhead: 8, openRange: [] as any, earlyCutoff: null as any });
    function save() { console.log('save reservation rule', form); }
    function reset() { form.daysAhead = 8; form.openRange = []; form.earlyCutoff = null; }
    return { form, save, reset };
  },
});
</script>

<style scoped>
.p-4 { padding: 16px; }
.w-full { width: 100%; }
.page-actions { text-align: right; }
</style>


