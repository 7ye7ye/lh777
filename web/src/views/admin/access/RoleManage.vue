<template>
  <PageWrapper :title="'角色管理'">
    <div class="p-4">
      <a-card :bordered="false">
        <a-form layout="inline" class="mb-3">
          <a-form-item label="角色名称">
            <a-input v-model:value="roleName" style="width: 220px" />
          </a-form-item>
          <a-button type="primary" @click="add">新增角色</a-button>
        </a-form>
        <a-table :data-source="roles" :columns="columns" row-key="id" bordered size="middle" :pagination="{ pageSize: 10 }" />
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { PageWrapper } from '/@/components/Page';

export default defineComponent({
  name: 'AdminRoleManage',
  components: { PageWrapper },
  setup() {
    const roleName = ref('');
    const roles = ref([{ id: 1, name: '管理员', permissionCount: 12 }]);
    const columns = [
      { title: '角色名称', dataIndex: 'name' },
      { title: '权限点数量', dataIndex: 'permissionCount' },
    ];
    function add() {
      if (!roleName.value) return;
      roles.value = [...roles.value, { id: Date.now(), name: roleName.value, permissionCount: 0 }];
      roleName.value = '';
    }
    return { roleName, roles, columns, add };
  },
});
</script>

<style scoped>
.p-4 { padding: 16px; }
.mb-3 { margin-bottom: 12px; }
</style>


