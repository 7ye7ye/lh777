<template>
  <PageWrapper :title="'账号权限设置'">
    <div class="p-4">
      <a-card :bordered="false">
        <a-form layout="inline" class="mb-3">
          <a-form-item label="账号">
            <a-input v-model:value="account" style="width: 220px" />
          </a-form-item>
          <a-form-item label="角色">
            <a-select v-model:value="role" :options="roleOptions" style="width: 200px" />
          </a-form-item>
          <a-button type="primary" @click="bind">绑定角色</a-button>
        </a-form>

        <a-alert type="info" show-icon message="可在此修改账号关联角色与权限" />
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { PageWrapper } from '/@/components/Page';

export default defineComponent({
  name: 'AdminAccountPermission',
  components: { PageWrapper },
  setup() {
    const account = ref('');
    const role = ref('');
    const roleOptions = [
      { label: '管理员', value: 'admin' },
      { label: '运营', value: 'ops' },
    ];
    function bind() { console.log('bind role', account.value, role.value); }
    return { account, role, roleOptions, bind };
  },
});
</script>

<style scoped>
.p-4 { padding: 16px; }
.mb-3 { margin-bottom: 12px; }
</style>


