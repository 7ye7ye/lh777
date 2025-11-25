<template>
  <PageWrapper title="转诊申请">
    <a-card>
      <a-form-model ref="formRef" :model="formData" :rules="rules" layout="vertical">
        <!-- 基本信息 -->
        <a-divider orientation="left">基本信息</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-model-item label="患者姓名" prop="patientName">
              <a-input v-model:value="formData.patientName" placeholder="请输入患者姓名" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="性别" prop="gender">
              <a-radio-group v-model:value="formData.gender">
                <a-radio value="MALE">男</a-radio>
                <a-radio value="FEMALE">女</a-radio>
              </a-radio-group>
            </a-form-model-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-model-item label="年龄" prop="age">
              <a-input-number v-model:value="formData.age" :min="0" :max="150" placeholder="请输入年龄" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="联系电话" prop="phone">
              <a-input v-model:value="formData.phone" placeholder="请输入联系电话" />
            </a-form-model-item>
          </a-col>
        </a-row>

        <!-- 转诊信息 -->
        <a-divider orientation="left">转诊信息</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-model-item label="转诊类型" prop="targetType">
              <a-radio-group v-model:value="formData.targetType" @change="handleTargetTypeChange">
                <a-radio value="INTERNAL">院内转诊</a-radio>
                <a-radio value="EXTERNAL">院外转诊</a-radio>
              </a-radio-group>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="目标医院" v-if="formData.targetType === 'EXTERNAL'" prop="targetHospitalId">
              <a-select v-model:value="formData.targetHospitalId" placeholder="请选择目标医院">
                <a-select-option v-for="hospital in hospitalOptions" :key="hospital.id" :value="hospital.id">
                  {{ hospital.name }}
                </a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-model-item label="目标科室" prop="targetDeptId">
              <a-select v-model:value="formData.targetDeptId" placeholder="请选择目标科室">
                <a-select-option v-for="dept in departmentOptions" :key="dept.id" :value="dept.id">
                  {{ dept.name }}
                </a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
        </a-row>

        <!-- 病情信息 -->
        <a-divider orientation="left">病情信息</a-divider>
        <a-form-model-item label="症状描述" prop="symptoms">
          <a-textarea v-model:value="formData.symptoms" rows="3" placeholder="请详细描述患者的症状" />
        </a-form-model-item>
        <a-form-model-item label="病史" prop="medicalHistory">
          <a-textarea v-model:value="formData.medicalHistory" rows="3" placeholder="请填写患者的既往病史，如无则填写无" />
        </a-form-model-item>
        <a-form-model-item label="转诊原因" prop="reason">
          <a-textarea v-model:value="formData.reason" rows="3" placeholder="请说明转诊原因" />
        </a-form-model-item>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <a-button type="primary" @click="handleSubmit" :loading="submitting">提交申请</a-button>
          <a-button style="margin-left: 12px;" @click="handleReset">重置</a-button>
        </div>
      </a-form-model>
    </a-card>

    <!-- 提交成功提示 -->
    <a-modal
      v-model:open="successModalVisible"
      title="申请提交成功"
      footer="null"
      closable="false"
      :maskClosable="false"
    >
      <div class="success-content">
        <a-result
          status="success"
          title="转诊申请已成功提交"
          sub-title="您的申请将在1-3个工作日内完成审核，请耐心等待"
        >
          <template #extra>
            <a-space>
              <a-button @click="handleCloseSuccess">返回</a-button>
              <a-button type="primary" @click="handleViewHistory">查看申请记录</a-button>
            </a-space>
          </template>
        </a-result>
      </div>
    </a-modal>
  </PageWrapper>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { PageWrapper } from '/@/components/Page';
import { submitReferralApplication, getReferralOptions } from '/@/api/hospital/referral';

const router = useRouter();

// 表单引用
const formRef = ref<{ validate: () => Promise<boolean> }>();

// 表单数据
const formData = reactive({
  patientName: '',
  gender: 'MALE' as 'MALE' | 'FEMALE',
  age: 0,
  phone: '',
  targetType: 'INTERNAL' as 'INTERNAL' | 'EXTERNAL',
  targetHospitalId: undefined as number | undefined,
  targetDeptId: undefined as number | undefined,
  symptoms: '',
  medicalHistory: '',
  reason: '',
});

// 表单验证规则
const rules = {
  patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 0, max: 150, message: '年龄必须在0-150之间', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { 
      pattern: /^1[3-9]\d{9}$/, 
      message: '请输入正确的手机号码格式', 
      trigger: 'blur' 
    },
  ],
  targetType: [{ required: true, message: '请选择转诊类型', trigger: 'change' }],
  targetHospitalId: [
    { 
      required: () => formData.targetType === 'EXTERNAL', 
      message: '请选择目标医院', 
      trigger: 'change' 
    },
  ],
  targetDeptId: [{ required: true, message: '请选择目标科室', trigger: 'change' }],
  symptoms: [{ required: true, message: '请描述症状', trigger: 'blur' }],
  reason: [{ required: true, message: '请说明转诊原因', trigger: 'blur' }],
};

// 选项数据
const hospitalOptions = ref<Array<{ id: number; name: string }>>([]);
const departmentOptions = ref<Array<{ id: number; name: string }>>([]);

// 状态
const submitting = ref(false);
const successModalVisible = ref(false);

// 处理转诊类型变更
const handleTargetTypeChange = () => {
  if (formData.targetType === 'INTERNAL') {
    formData.targetHospitalId = undefined;
  }
  // 可以根据转诊类型动态加载科室选项
};

// 加载选项数据
const loadOptions = async () => {
  try {
    const res = await getReferralOptions();
    
    // 处理医院选项
    if (res.data?.hospitals) {
      hospitalOptions.value = res.data.hospitals.map((h: any) => ({
        id: h.id,
        name: h.name,
      }));
    }
    
    // 处理科室选项
    if (res.data?.departments) {
      departmentOptions.value = res.data.departments.map((d: any) => ({
        id: d.id,
        name: d.name,
      }));
    }
  } catch (error) {
    console.error('加载选项失败:', error);
    message.error('加载选项失败，请刷新页面重试');
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    // 验证表单
    await formRef.value.validate();
    
    submitting.value = true;
    
    // 提交申请
    const response = await submitReferralApplication({
      ...formData,
      // 转换为后端需要的格式
      patientName: formData.patientName,
      gender: formData.gender,
      age: formData.age,
      phone: formData.phone,
      targetType: formData.targetType,
      targetHospitalId: formData.targetHospitalId,
      targetDeptId: formData.targetDeptId,
      symptoms: formData.symptoms,
      medicalHistory: formData.medicalHistory || '无',
      reason: formData.reason,
    });
    
    // 显示成功提示
    successModalVisible.value = true;
  } catch (error: any) {
    console.error('提交申请失败:', error);
    
    // 处理验证错误
    if (error.name === 'ValidateError') {
      return;
    }
    
    // 处理其他错误
    const errorMessage = error.response?.data?.message || '提交申请失败，请稍后重试';
    message.error(errorMessage);
  } finally {
    submitting.value = false;
  }
};

// 重置表单
const handleReset = () => {
  if (formRef.value) {
    formRef.value.validate();
  }
  Object.assign(formData, {
    patientName: '',
    gender: 'MALE',
    age: 0,
    phone: '',
    targetType: 'INTERNAL',
    targetHospitalId: undefined,
    targetDeptId: undefined,
    symptoms: '',
    medicalHistory: '',
    reason: '',
  });
};

// 关闭成功提示
const handleCloseSuccess = () => {
  successModalVisible.value = false;
};

// 查看申请记录
const handleViewHistory = () => {
  successModalVisible.value = false;
  // 跳转到转诊记录历史页面
  router.push('/admin/management/referral/history');
};

// 组件挂载时加载选项数据
onMounted(() => {
  loadOptions();
});
</script>

<style scoped>
.submit-section {
  margin-top: 24px;
  text-align: center;
}

.success-content {
  text-align: center;
}
</style>