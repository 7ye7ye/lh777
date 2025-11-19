<template>
  <div class="doctor-management">
    <div class="page-header">
      <h2>医生管理</h2>
    </div>

    <a-card class="search-card" bordered>
      <a-form layout="inline" :model="searchFormData">
        <a-form-item label="医生姓名">
          <a-input v-model:value="searchFormData.doctorName" allow-clear placeholder="请输入医生姓名" />
        </a-form-item>
        <a-form-item label="所属科室">
          <a-select
            v-model:value="searchFormData.deptId"
            allow-clear
            placeholder="请选择科室"
            style="width: 200px"
          >
            <a-select-option v-for="dept in departmentOptions" :key="dept.value" :value="dept.value">
              {{ dept.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="职称">
          <a-input v-model:value="searchFormData.title" allow-clear placeholder="请输入职称" />
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
        <a-button type="primary" @click="handleAdd">添加医生</a-button>
      </div>
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        row-key="doctorId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.isActive === 1 ? 'green' : 'red'">
              {{ record.isActive === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="middle">
              <a @click="handleEdit(record)">编辑</a>
              <a class="danger-link" @click="handleDelete(record)">删除</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      destroy-on-close
      width="640px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form ref="formRef" layout="vertical" :model="formData" :rules="formRules">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="医生姓名" name="doctorName">
              <a-input v-model:value="formData.doctorName" placeholder="请输入医生姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="所属科室" name="deptId">
              <a-select v-model:value="formData.deptId" placeholder="请选择科室">
                <a-select-option
                  v-for="dept in departmentOptions"
                  :key="dept.value"
                  :value="dept.value"
                >
                  {{ dept.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="医生职称" name="title">
              <a-input v-model:value="formData.title" placeholder="请输入职称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="联系方式" name="contact">
              <a-input v-model:value="formData.contact" placeholder="请输入联系方式" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="专长" name="specialty">
          <a-input v-model:value="formData.specialty" placeholder="请输入医生专长" />
        </a-form-item>
        <a-form-item label="医生简介" name="doctorDesc">
          <a-textarea v-model:value="formData.doctorDesc" :rows="4" placeholder="请输入医生简介" />
        </a-form-item>
        <a-form-item label="是否启用" name="isActive">
          <a-radio-group v-model:value="formData.isActive">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue';
import { Modal, message } from 'ant-design-vue';
import type { FormInstance } from 'ant-design-vue';
import { getDoctorList, addDoctor, updateDoctor, deleteDoctor, type Doctor } from '/@/api/hospital/doctor';
import { getAllDepartments } from '/@/api/hospital/department';
import { getToken } from '/@/utils/auth';

interface PaginationState {
  current: number;
  pageSize: number;
  total: number;
  showSizeChanger: boolean;
  showQuickJumper: boolean;
}

interface DoctorSearchForm {
  doctorName: string;
  deptId?: number;
  title: string;
}

interface DoctorFormModel {
  doctorId?: number;
  doctorName: string;
  deptId?: number;
  title: string;
  contact?: string;
  specialty: string;
  doctorDesc: string;
  isActive: number;
}

type DepartmentOption = { label: string; value: number };

const loading = ref(false);
const tableData = ref<Doctor[]>([]);
const pagination = reactive<PaginationState>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
});

const departmentOptions = ref<DepartmentOption[]>([]);

// 加载科室选项
async function loadDepartmentOptions() {
  try {
    const res = await getAllDepartments();
    if (Array.isArray(res)) {
      departmentOptions.value = res.map(dept => ({
        label: dept.deptName,
        value: dept.deptId
      }));
    } else if (res?.success && Array.isArray(res.result)) {
      departmentOptions.value = res.result.map(dept => ({
        label: dept.deptName,
        value: dept.deptId
      }));
    } else {
      // 降级为备用数据
      departmentOptions.value = guestDepartmentOptions;
    }
  } catch (error) {
    console.error('加载科室选项失败:', error);
    departmentOptions.value = guestDepartmentOptions;
  }
}

const searchFormData = reactive<DoctorSearchForm>({
  doctorName: '',
  deptId: undefined,
  title: '',
});

const fallbackDoctors: Doctor[] = [
  {
    doctorId: 1001,
    doctorName: '李医生',
    userId: 1,
    deptId: 1,
    deptName: '内科',
    title: '主任医师',
    specialty: '心血管',
    doctorDesc: '从业 15 年，擅长心血管疾病诊疗',
    avatar: '',
    isActive: 1,
  },
  {
    doctorId: 1002,
    doctorName: '王医生',
    userId: 2,
    deptId: 2,
    deptName: '外科',
    title: '副主任医师',
    specialty: '微创外科',
    doctorDesc: '擅长腹腔镜微创手术',
    avatar: '',
    isActive: 1,
  },
];

const guestDoctors = ref<Doctor[]>([...fallbackDoctors]);
const guestDepartmentOptions: DepartmentOption[] = [
  { label: '内科', value: 1 },
  { label: '外科', value: 2 },
  { label: '儿科', value: 3 },
];

const columns = [
  { title: '医生ID', dataIndex: 'doctorId', key: 'doctorId', width: 90 },
  { title: '医生姓名', dataIndex: 'doctorName', key: 'doctorName', width: 120 },
  { title: '所属科室', dataIndex: 'deptName', key: 'deptName', width: 160 },
  { title: '职称', dataIndex: 'title', key: 'title', width: 120 },
  { title: '专长', dataIndex: 'specialty', key: 'specialty' },
  { title: '简介', dataIndex: 'doctorDesc', key: 'doctorDesc' },
  { title: '状态', dataIndex: 'isActive', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 160 },
];

const modalVisible = ref(false);
const modalTitle = ref('添加医生');
const isEdit = ref(false);
const formRef = ref<FormInstance | null>(null);

const formData = reactive<DoctorFormModel>({
  doctorId: undefined,
  doctorName: '',
  deptId: undefined,
  title: '',
  contact: '',
  specialty: '',
  doctorDesc: '',
  isActive: 1,
});

const formRules = {
  doctorName: [{ required: true, message: '请输入医生姓名' }],
  deptId: [{ required: true, message: '请选择所属科室' }],
  title: [{ required: true, message: '请输入医生职称' }],
  specialty: [{ required: true, message: '请输入医生专长' }],
};

function normalizeDoctorList(res: unknown): { items: Doctor[]; total: number } {
  if (res && typeof res === 'object') {
    // 处理后端返回的Result格式
    if ('result' in res && res.result && typeof res.result === 'object') {
      const result = res.result;
      const records = result.records ?? [];
      const total = result.total ?? records.length;
      return { 
        items: records.map((item: any) => ({
          ...item,
          deptName: item.deptName || departmentOptions.value.find(d => d.value === item.deptId)?.label || '未分配'
        })), 
        total 
      };
    }
    // 处理直接返回的分页对象
    const records = (res as any).records ?? [];
    const total = (res as any).total ?? records.length;
    return { 
      items: records.map((item: any) => ({
        ...item,
        deptName: item.deptName || departmentOptions.value.find(d => d.value === item.deptId)?.label || '未分配'
      })), 
      total 
    };
  }
  if (Array.isArray(res)) {
    return { 
      items: res.map((item: any) => ({
        ...item,
        deptName: item.deptName || departmentOptions.value.find(d => d.value === item.deptId)?.label || '未分配'
      })), 
      total: res.length 
    };
  }
  return { items: [], total: 0 };
}

function applyGuestDoctorView() {
  const filtered = guestDoctors.value.filter((doctor) => {
    const matchName = searchFormData.doctorName ? doctor.doctorName.includes(searchFormData.doctorName) : true;
    const matchDept = searchFormData.deptId ? doctor.deptId === searchFormData.deptId : true;
    return matchName && matchDept;
  });
  pagination.total = filtered.length;
  const start = (pagination.current - 1) * pagination.pageSize;
  tableData.value = filtered.slice(start, start + pagination.pageSize);
}

async function fetchDoctorList() {
  loading.value = true;
  const token = getToken();
  if (!token) {
    applyGuestDoctorView();
    loading.value = false;
    return;
  }
  try {
    const res = await getDoctorList({
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
      keyword: searchFormData.doctorName || undefined,
      deptId: searchFormData.deptId,
    } as any);
    const { items, total } = normalizeDoctorList(res);
    tableData.value = items;
    pagination.total = total;
  } catch (error) {
    console.error('获取医生列表失败:', error);
    message.error('获取医生列表失败');
    tableData.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchDoctorList();
}

onMounted(() => {
  loadDepartmentOptions();
  fetchDoctorList();
});

function handleSearch() {
  pagination.current = 1;
  fetchDoctorList();
}

function handleReset() {
  searchFormData.doctorName = '';
  searchFormData.deptId = undefined;
  searchFormData.title = '';
  pagination.current = 1;
  fetchDoctorList();
}

function resetFormData() {
  formData.doctorId = undefined;
  formData.doctorName = '';
  formData.deptId = undefined;
  formData.title = '';
  formData.contact = '';
  formData.specialty = '';
  formData.doctorDesc = '';
  formData.isActive = 1;
}

function handleAdd() {
  isEdit.value = false;
  modalTitle.value = '添加医生';
  resetFormData();
  modalVisible.value = true;
  nextTick(() => formRef.value?.clearValidate());
}

function handleEdit(record: Doctor) {
  isEdit.value = true;
  modalTitle.value = '编辑医生';
  formData.doctorId = record.doctorId;
  formData.doctorName = record.doctorName;
  formData.deptId = record.deptId;
  formData.title = record.title;
  formData.specialty = record.specialty;
  formData.doctorDesc = record.doctorDesc;
  formData.isActive = record.isActive;
  modalVisible.value = true;
  nextTick(() => formRef.value?.clearValidate());
}

function handleDelete(record: Doctor) {
  const token = getToken();
  if (!token) {
    guestDoctors.value = guestDoctors.value.filter((doctor) => doctor.doctorId !== record.doctorId);
    applyGuestDoctorView();
    message.success('删除医生成功（演示）');
    return;
  }
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除医生「${record.doctorName}」吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteDoctor(record.doctorId!);
        if (res?.success || res?.code === 200) {
          message.success('删除医生成功');
          fetchDoctorList();
        } else {
          message.error(res?.message || '删除医生失败');
        }
      } catch (error) {
        console.error('删除医生失败:', error);
        message.error('删除医生失败');
      }
    },
  });
}

async function handleModalOk() {
  try {
    await formRef.value?.validate();
    const token = getToken();
    if (!token) {
      if (isEdit.value && formData.doctorId) {
        guestDoctors.value = guestDoctors.value.map((doctor) =>
          doctor.doctorId === formData.doctorId ? { ...doctor, ...formData } : doctor
        );
        message.success('编辑医生成功（演示）');
      } else {
        const newRecord: Doctor = {
          doctorId: Date.now(),
          doctorName: formData.doctorName,
          deptId: formData.deptId || 0,
          deptName: departmentOptions.value.find((opt) => opt.value === formData.deptId)?.label || '未分配',
          title: formData.title,
          specialty: formData.specialty,
          doctorDesc: formData.doctorDesc,
          userId: 0,
          avatar: '',
          isActive: formData.isActive,
        };
        guestDoctors.value.unshift(newRecord);
        message.success('添加医生成功（演示）');
      }
      modalVisible.value = false;
      applyGuestDoctorView();
      return;
    }

    // 准备提交数据，确保数字字段类型正确
    const submitData = {
      doctorId: formData.doctorId ? Number(formData.doctorId) : undefined,
      doctorName: formData.doctorName,
      deptId: formData.deptId ? Number(formData.deptId) : undefined,
      title: formData.title,
      specialty: formData.specialty,
      doctorDesc: formData.doctorDesc,
      isActive: formData.isActive ? Number(formData.isActive) : 1
    };

    let res;
    if (isEdit.value) {
      res = await updateDoctor(submitData);
    } else {
      res = await addDoctor(submitData);
    }

    if (res?.success || res?.code === 200) {
      message.success(isEdit.value ? '编辑医生成功' : '添加医生成功');
      modalVisible.value = false;
      fetchDoctorList();
    } else {
      message.error(res?.message || (isEdit.value ? '编辑医生失败' : '添加医生失败'));
    }
  } catch (error) {
    console.error('保存医生信息失败:', error);
    message.error('保存医生信息失败');
  }
}

function handleModalCancel() {
  modalVisible.value = false;
}

// 保留loadDepartmentOptions函数，删除重复的loadDepartments函数
onMounted(() => {
  loadDepartmentOptions();
  fetchDoctorList();
});
</script>

<style scoped>
.doctor-management {
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
</style>