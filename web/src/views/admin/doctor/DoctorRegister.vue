<template>
  <PageWrapper title="医生注册">
    <a-row :gutter="16">
      <a-col :span="16">
        <a-card title="医生基础信息" :loading="loading">
          <a-alert type="info" show-icon class="mb-4">
            <template #message>
              请填写医生账号信息及基本资料，提交后系统将自动在
              <strong>hos_user</strong> 与 <strong>doctor</strong> 表中同时创建记录。
            </template>
          </a-alert>

          <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="登录账号" name="userAccount" required>
                  <a-input v-model:value="formState.userAccount" placeholder="4-20位，支持字母、数字、下划线" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="医生姓名" name="doctorName" required>
                  <a-input v-model:value="formState.doctorName" placeholder="2-10位，仅支持中文和英文字母" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="所属科室" name="deptId" required>
                  <a-select
                    v-model:value="formState.deptId"
                    show-search
                    placeholder="请选择科室"
                    :options="deptOptions"
                    :filter-option="filterDept"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="职称" name="title" required>
                  <a-select v-model:value="formState.title" :options="titleOptions" placeholder="2-10位" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="联系邮箱" name="email">
                  <a-input v-model:value="formState.email" placeholder="可选" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="出诊状态" name="isActive">
                  <a-switch
                    v-model:checked="formState.isActive"
                    checked-children="启用"
                    un-checked-children="停诊"
                  />
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item label="擅长领域" name="specialty" required>
              <a-textarea
                v-model:value="formState.specialty"
                :rows="3"
                placeholder="5-200位，请输入医生擅长的疾病或手术领域"
              />
            </a-form-item>

            <a-form-item label="医生简介" name="doctorDesc">
              <a-textarea
                v-model:value="formState.doctorDesc"
                :rows="3"
                placeholder="10-500位（可选），简要介绍医生背景与成就"
              />
            </a-form-item>
          </a-form>
        </a-card>

        <div class="mt-4 text-right">
          <a-space>
            <a-button @click="resetForm">重置</a-button>
            <a-button type="primary" :loading="submitting" @click="handleSubmit">提交注册</a-button>
          </a-space>
        </div>
      </a-col>

      <a-col :span="8">
        <a-card title="初始密码" class="doctor-register__side-card">
          <p class="text-muted">系统自动生成医生初始密码，可在提交前重新生成或复制给医生。</p>
          <a-input :value="password" readonly class="mb-2" />
          <a-space>
            <a-button size="small" @click="regeneratePassword">重新生成</a-button>
            <a-button size="small" @click="copyPassword">复制密码</a-button>
          </a-space>
          <a-alert type="warning" show-icon class="mt-3">
            <template #message>请提醒医生首次登录后修改密码。</template>
          </a-alert>
        </a-card>
      </a-col>
    </a-row>
  </PageWrapper>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { PageWrapper } from '/@/components/Page';
import type { FormInstance } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { useMessage } from '/@/hooks/web/useMessage';
import { getDepartmentList } from '/@/api/hospital/department';
import { registerDoctorAccount } from '/@/api/hospital/doctor';

interface DoctorFormState {
  userAccount: string;
  doctorName: string;
  deptId?: number;
  title: string;
  specialty: string;
  doctorDesc?: string;
  email?: string;
  isActive: boolean;
}

const formRef = ref<FormInstance>();
const loading = ref(false);
const submitting = ref(false);
const { createMessage } = useMessage();

const formState = reactive<DoctorFormState>({
  userAccount: '',
  doctorName: '',
  deptId: undefined,
  title: '主治医师',
  specialty: '',
  doctorDesc: '',
  email: '',
  isActive: true,
});

const password = ref(generatePassword());
const deptOptions = ref<{ label: string; value: number }[]>([]);
const titleOptions = [
  { label: '主任医师', value: '主任医师', id: 1 },
  { label: '副主任医师', value: '副主任医师', id: 2 },
  { label: '主治医师', value: '主治医师', id: 3 },
  { label: '住院医师', value: '住院医师', id: 4 },
  { label: '主任护师', value: '主任护师', id: 5 },
  { label: '副主任护师', value: '副主任护师', id: 6 },
  { label: '主管护师', value: '主管护师', id: 7 },
  { label: '报销专员', value: '报销专员', id: 8 },
];

const rules: Record<string, Rule[]> = {
  userAccount: [{ required: true, message: '请输入登录账号' }],
  doctorName: [{ required: true, message: '请输入医生姓名' }],
  deptId: [{ required: true, type: 'number', message: '请选择所属科室' }],
  title: [{ required: true, message: '请选择职称' }],
  specialty: [{ required: true, message: '请输入擅长领域' }],
};

function generatePassword(length = 10) {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789';
  return Array.from({ length }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
}

function regeneratePassword() {
  password.value = generatePassword();
  createMessage.success('已生成新的初始密码');
}

async function copyPassword() {
  try {
    await navigator.clipboard.writeText(password.value);
    createMessage.success('密码已复制到剪贴板');
  } catch {
    createMessage.error('复制失败，请手动选择复制');
  }
}

function resetForm() {
  formRef.value?.resetFields();
  password.value = generatePassword();
}

function filterDept(input: string, option?: { label: string; value: number }) {
  return (option?.label ?? '').toLowerCase().includes(input.toLowerCase());
}

async function fetchDepartments() {
  loading.value = true;
  try {
    const list = await getDepartmentList({ size: 200, current: 1 });
    deptOptions.value = (list || []).map((item) => ({
      label: item.deptName,
      value: item.deptId,
    }));
  } catch (error) {
    console.error(error);
    createMessage.error('获取科室列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate();
    if (!formState.deptId) {
      createMessage.warning('请选择科室');
      return;
    }
    
    const selectedTitle = titleOptions.find(t => t.value === formState.title);
    if (!selectedTitle) {
      createMessage.error('请选择有效的职称');
      return;
    }

    submitting.value = true;
    
    const res = await registerDoctorAccount({
      userAccount: formState.userAccount.trim(),
      userPassword: password.value,
      doctorName: formState.doctorName.trim(),
      deptId: formState.deptId,
      title: formState.title,
      titleId: selectedTitle.id,
      specialty: formState.specialty,
      doctorDesc: formState.doctorDesc,
      email: formState.email,
      isActive: formState.isActive,
      userType: 2,
    });

    // 注册成功，显示包含密码的提示信息
    createMessage.success(`医生账户注册成功，请将密码通知医生`);
    resetForm();
  } catch (error: any) {
    // 统一处理错误信息
    let errorMessage = '医生注册失败';
    
    // 如果是业务异常，使用后端返回的错误信息
    if (error?.response?.data) {
      const responseData = error.response.data;
      errorMessage = responseData.description || responseData.message || errorMessage;
    } else if (error?.message) {
      // 其他错误信息
      errorMessage = error.message;
    }
    
    // 只显示一次错误提示
    createMessage.error(errorMessage);
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  fetchDepartments();
});
</script>

<style scoped>
.mb-4 {
  margin-bottom: 16px;
}

.mt-3 {
  margin-top: 12px;
}

.mt-4 {
  margin-top: 16px;
}

.text-right {
  text-align: right;
}

.doctor-register__side-card + .doctor-register__side-card {
  margin-top: 16px;
}

.side-list {
  padding-left: 18px;
}

.side-list li {
  margin-bottom: 8px;
  color: var(--text-color-secondary);
}

.text-muted {
  color: var(--text-color-secondary);
}
</style>

