<template>
  <div class="department-management">
    <div class="page-header">
      <h2>科室管理</h2>
    </div>

    <a-card class="search-card" bordered>
      <a-form layout="inline" :model="searchFormData">
        <a-form-item label="科室名称">
          <a-input v-model:value="searchFormData.deptName" allow-clear placeholder="请输入科室名称" />
        </a-form-item>
        <a-form-item label="科室级别">
          <a-select
            v-model:value="searchFormData.deptLevel"
            allow-clear
            placeholder="请选择科室级别"
            style="width: 160px"
          >
            <a-select-option :value="1">一级科室</a-select-option>
            <a-select-option :value="2">二级科室</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered>
      <div class="toolbar">
        <a-button type="primary" @click="handleAdd">
          添加科室
        </a-button>
      </div>

      <a-table
      :columns="columns"
      :data-source="tableData"
      :loading="loading"
      :pagination="pagination"
      row-key="deptId"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'deptLevel'">
          <span>{{ formatDeptLevel(record.deptLevel) }}</span>
        </template>
        <template v-else-if="column.key === 'parentDeptName'">
          <span>{{ getParentDeptName(record.parentDeptId) }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space size="middle">
            <a @click="handleEdit(record)">编辑</a>
            <a class="danger-link" @click="handleDelete(record)">删除</a>
          </a-space>
        </template>
      </template>
      <template #deptName="{ record }">
        <a class="dept-name-link" @click="handleViewDetail(record.deptId)">
          {{ record.deptName }}
        </a>
      </template>
    </a-table>
    </a-card>

    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      destroy-on-close
      width="520px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form ref="formRef" layout="vertical" :model="formData" :rules="formRules">
        <a-form-item label="科室名称" name="deptName">
          <a-input v-model:value="formData.deptName" placeholder="请输入科室名称" />
        </a-form-item>
        <a-form-item label="科室级别" name="deptLevel">
          <a-select 
    v-model:value="formData.deptLevel" 
    placeholder="请选择科室级别"
    @change="handleDeptLevelChange"
  >
            <a-select-option :value="1">一级科室</a-select-option>
            <a-select-option :value="2">二级科室</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="上级科室" name="parentDeptId">
          <a-select
            style="width: 100%"
            v-model:value="formData.parentDeptId"
            allow-clear
            :disabled="formData.deptLevel === 1"
            placeholder="请选择上级科室"
          >
            <a-select-option 
              v-for="dept in parentDepartmentList.filter(item => !isEdit.value || item.deptId !== formData.deptId)" 
              :key="dept.deptId" 
              :value="dept.deptId"
            >
              {{ dept.deptName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="科室位置" name="location">
          <a-input v-model:value="formData.location" placeholder="请输入科室位置" />
        </a-form-item>
        <a-form-item label="科室简介" name="deptDesc">
          <a-textarea v-model:value="formData.deptDesc" :rows="3" placeholder="请输入科室简介" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue';
import { Modal, message } from 'ant-design-vue';
import type { FormInstance } from 'ant-design-vue';
import { getDepartmentList, createDepartment, updateDepartment, deleteDepartment, type Department } from '/@/api/hospital/department';
import { useRouter } from 'vue-router';

type DepartmentRecord = Department & { createTime?: string; updateTime?: string };

interface PaginationState {
  current: number;
  pageSize: number;
  total: number;
  showSizeChanger: boolean;
  showQuickJumper: boolean;
}

interface DepartmentFormModel {
  deptId?: number;
  deptName: string;
  deptLevel: number;
  parentDeptId?: number;
  deptDesc?: string;
  location?: string;
}

const loading = ref(false);
const tableData = ref<DepartmentRecord[]>([]);
const parentDepartmentList = ref<DepartmentRecord[]>([]);
const pagination = reactive<PaginationState>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
});

const searchFormData = reactive({
  deptName: '',
  deptLevel: undefined as number | undefined,
});

const router = useRouter();

const columns = [
  { title: '科室ID', dataIndex: 'deptId', key: 'deptId', width: 100 },
  { title: '科室名称',
      dataIndex: 'deptName',
      key: 'deptName',
      width: 180,
      slots: {
        customRender: 'deptName'
      },
  },
  { title: '科室级别', dataIndex: 'deptLevel', key: 'deptLevel', width: 100 },
  { title: '上一级科室', dataIndex: 'parentDeptId', key: 'parentDeptName', width: 160 },
  { title: '科室简介', dataIndex: 'deptDesc', key: 'deptDesc', width: 240 },
  { title: '位置', dataIndex: 'location', key: 'location', width: 160 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '操作', key: 'action', width: 160 },
];

const modalVisible = ref(false);
const modalTitle = ref('添加科室');
const isEdit = ref(false);
const formRef = ref<FormInstance | null>(null);

const formData = reactive<DepartmentFormModel>({
  deptId: undefined,
  deptName: '',
  deptLevel: 1,
  parentDeptId: undefined,
  deptDesc: '',
  location: '',
});

const formRules = {
  deptName: [{ required: true, message: '请输入科室名称' }],
  deptLevel: [{ required: true, message: '请选择科室级别' }],
};

function formatDeptLevel(level?: number) {
  if (!level) {
    return '-';
  }
  return `第${level}级`;
}

function getParentDeptName(parentDeptId?: number): string {
  if (!parentDeptId) {
    return '-';
  }
  const dept = tableData.value.find(item => item.deptId === parentDeptId);
  return dept ? dept.deptName : '-';
}

function handleViewDetail(deptId: string) {
  router.push(`/admin/management/department/detail/${deptId}`);
}

function normalizeList(res: unknown): { items: DepartmentRecord[]; total: number } {
  if (Array.isArray(res)) {
    return { items: res, total: res.length };
  }
  if (res && typeof res === 'object') {
    const records = (res as any).records ?? [];
    const total = (res as any).total ?? records.length;
    return { items: records, total };
  }
  return { items: [], total: 0 };
}

async function fetchParentDepartmentList() {
  try {
    const res = await getDepartmentList({ deptLevel: 1 } as any);
    const { items } = normalizeList(res);
    parentDepartmentList.value = items.filter(item => item.deptLevel === 1);
  } catch (error) {
    console.error('获取一级科室列表失败:', error);
    parentDepartmentList.value = [];
  }
}

async function fetchDepartmentList() {
  loading.value = true;
  try {
    const res = await getDepartmentList({
      current: pagination.current,
      size: pagination.pageSize,
      deptName: searchFormData.deptName || undefined,
      deptLevel: searchFormData.deptLevel,
    } as any);
    const { items, total } = normalizeList(res);
    tableData.value = items;
    pagination.total = total;
  } catch (error) {
    console.error('获取科室列表失败:', error);
    message.error('获取科室列表失败');
    tableData.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchDepartmentList();
}

function handleSearch() {
  pagination.current = 1;
  fetchDepartmentList();
}

function handleReset() {
  searchFormData.deptName = '';
  searchFormData.deptLevel = undefined;
  pagination.current = 1;
  fetchDepartmentList();
}

function resetFormData() {
  formData.deptId = undefined;
  formData.deptName = '';
  formData.deptLevel = 1;
  formData.parentDeptId = undefined;
  formData.deptDesc = '';
  formData.location = '';
}

function handleAdd() {
  isEdit.value = false;
  modalTitle.value = '添加科室';
  resetFormData();
  fetchParentDepartmentList();
  modalVisible.value = true;
  nextTick(() => formRef.value?.clearValidate());
}

function handleDeptLevelChange(value: number) {
  if (value === 1) {
    formData.parentDeptId = undefined;
  }
}

function handleEdit(record: DepartmentRecord) {
  isEdit.value = true;
  modalTitle.value = '编辑科室';
  formData.deptId = record.deptId;
  formData.deptName = record.deptName;
  formData.deptLevel = record.deptLevel;
  formData.parentDeptId = record.parentDeptId;
  formData.deptDesc = record.deptDesc;
  formData.location = record.location;
  fetchParentDepartmentList();
  modalVisible.value = true;
  nextTick(() => formRef.value?.clearValidate());
}

function handleDelete(record: DepartmentRecord) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除科室「${record.deptName}」吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteDepartment(record.deptId);
        message.success('删除科室成功');
        fetchDepartmentList();
      } catch (error) {
        console.error('删除科室失败:', error);
        message.error('删除科室失败');
      }
    },
  });
}

async function handleModalOk() {
  try {
    await formRef.value?.validate();
    const payload: Department = {
      deptId: formData.deptId ?? 0,
      deptName: formData.deptName,
      deptLevel: formData.deptLevel,
      parentDeptId: formData.parentDeptId,
      deptDesc: formData.deptDesc,
      location: formData.location,
    };

    if (isEdit.value && formData.deptId) {
      await updateDepartment(payload);
      message.success('编辑科室成功');
    } else {
      const { deptId, ...rest } = payload;
      await createDepartment(rest);
      message.success('添加科室成功');
    }
    modalVisible.value = false;
    fetchDepartmentList();
  } catch (error) {
    if (error) {
      console.error('保存科室信息失败:', error);
      message.error('保存科室信息失败');
    }
  }
}

function handleModalCancel() {
  modalVisible.value = false;
}

onMounted(() => {
  fetchDepartmentList();
});
</script>

<style scoped>
.department-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.danger-link {
  color: #ff4d4f;
}

.dept-name-link {
  color: #1890ff;
  text-decoration: none;
}

.dept-name-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}
</style>