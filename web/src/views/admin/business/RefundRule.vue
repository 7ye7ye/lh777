<template>
  <PageWrapper :title="'退号规则制定'">
    <div class="p-4">
      <a-card :bordered="false">
        <a-form layout="vertical">
          <a-row :gutter="16">
            <a-col :xs="24" :sm="12" :xl="8">
              <a-form-item label="退号截止时间(就诊前 x 小时)">
                <a-input-number v-model:value="form.deadlineHours" :min="0" class="w-full" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :xl="8">
              <a-form-item label="每月最多退号次数">
                <a-input-number v-model:value="form.monthlyMax" :min="0" class="w-full" />
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
  name: 'AdminRefundRule',
  components: { PageWrapper },
  setup() {
    const form = reactive({ deadlineHours: 24, monthlyMax: 3 });
    function save() { console.log('save refund rule', form); }
    function reset() { form.deadlineHours = 24; form.monthlyMax = 3; }
    return { form, save, reset };
  },
});
</script>

<style scoped>
.p-4 { padding: 16px; }
.w-full { width: 100%; }
.page-actions { text-align: right; }
</style>


