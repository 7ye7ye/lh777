<template>
  <PageWrapper :title="'号别设置'">
    <div class="p-4">
      <a-space direction="vertical" size="large" class="w-full">
        <a-card :bordered="false">
          <a-form layout="inline" class="mb-3">
            <a-form-item label="新增号别">
              <a-input v-model:value="newType.name" placeholder="如：普通号/专家号" style="width: 220px" />
            </a-form-item>
            <a-form-item label="挂号费(元)">
              <a-input-number v-model:value="newType.fee" :min="0" />
            </a-form-item>
            <a-form-item label="单日上限">
              <a-input-number v-model:value="newType.dailyQuota" :min="0" />
            </a-form-item>
            <a-button type="primary" @click="addType">添加</a-button>
          </a-form>
          <a-table :data-source="types" :columns="columns" row-key="name" bordered size="middle" :pagination="{ pageSize: 10 }" />
        </a-card>
      </a-space>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';
import { PageWrapper } from '/@/components/Page';

export default defineComponent({
  name: 'AdminNumberTypeSetting',
  components: { PageWrapper },
  setup() {
    const newType = reactive({ name: '', fee: 0, dailyQuota: 0 });
    const types = ref([{ name: '普通号', fee: 10, dailyQuota: 50 }]);
    const columns = [
      { title: '号别名称', dataIndex: 'name' },
      { title: '挂号费(元)', dataIndex: 'fee' },
      { title: '日上限', dataIndex: 'dailyQuota' },
    ];
    function addType() {
      if (!newType.name) return;
      types.value = [...types.value, { ...newType }];
      newType.name = '';
      newType.fee = 0;
      newType.dailyQuota = 0;
    }
    return { newType, types, columns, addType };
  },
});
</script>

<style scoped>
.p-4 { padding: 16px; }
.mb-3 { margin-bottom: 12px; }
.w-full { width: 100%; }
</style>


