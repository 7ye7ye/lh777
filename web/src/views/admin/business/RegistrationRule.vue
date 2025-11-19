<template>
  <PageWrapper :title="'挂号规则制定'">
    <div class="p-4">
      <a-card :bordered="false">
        <a-form layout="vertical">
          <a-row :gutter="16">
            <a-col :xs="24" :sm="12" :xl="8">
              <a-form-item label="同一用户-同一科室短期内最大预约次数">
                <a-input-number v-model:value="form.maxPerDeptShort" :min="0" class="w-full" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :xl="8">
              <a-form-item label="同一用户-同一时间段可预约多个号源">
                <a-switch v-model:checked="form.allowMultiSlot" />
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
  name: 'AdminRegistrationRule',
  components: { PageWrapper },
  setup() {
    const form = reactive({ maxPerDeptShort: 2, allowMultiSlot: false });
    function save() { console.log('save reg rule', form); }
    function reset() { form.maxPerDeptShort = 2; form.allowMultiSlot = false; }
    return { form, save, reset };
  },
});
</script>

<style scoped>
.p-4 { padding: 16px; }
.w-full { width: 100%; }
.page-actions { text-align: right; }
</style>


