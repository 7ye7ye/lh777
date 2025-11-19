<template>
  <PageWrapper :title="'请假审批'">
    <div class="p-4">
      <a-card :bordered="false">
        <a-table :data-source="rows" :columns="columns" row-key="id" bordered size="middle" :pagination="{ pageSize: 10 }" />
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { PageWrapper } from '/@/components/Page';

export default defineComponent({
  name: 'AdminLeaveApproval',
  components: { PageWrapper },
  setup() {
    const rows = ref([
      { id: 1, doctor: '李四', reason: '外出进修', date: '2025-11-09', status: '待审批' },
    ]);
    const columns = [
      { title: '医生', dataIndex: 'doctor' },
      { title: '原因', dataIndex: 'reason' },
      { title: '日期', dataIndex: 'date' },
      { title: '状态', dataIndex: 'status' },
    ];
    return { rows, columns };
  },
});
</script>

<style scoped>
.p-4 { padding: 16px; }
</style>


