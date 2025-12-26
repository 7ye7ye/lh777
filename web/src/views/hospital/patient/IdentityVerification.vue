<template>
  <PageWrapper title="身份认证">
    <a-card>
      <a-alert
        v-if="verificationStatus === 0"
        message="您的身份认证申请正在审核中，请耐心等待"
        type="info"
        show-icon
        class="mb-4"
      />
      <a-alert
        v-else-if="verificationStatus === 1"
        message="您的身份已完成认证"
        type="success"
        show-icon
        class="mb-4"
      />
      <a-alert
        v-else-if="verificationStatus === 2"
        :message="`您的身份认证申请被驳回：${rejectReason || '未提供原因'}`"
        type="error"
        show-icon
        class="mb-4"
      />

      <a-form
        :model="formState"
        :rules="rules"
        ref="formRef"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 14 }"
      >
        <a-form-item label="姓名" name="patientName">
          <a-input v-model:value="formState.patientName" disabled />
        </a-form-item>

        <a-form-item label="身份证号" name="idCard">
          <a-input v-model:value="formState.idCard" disabled />
        </a-form-item>

        <a-form-item v-if="formState.patientType === 1" label="学号" name="studentId">
          <a-input v-model:value="formState.studentId" placeholder="请输入学号" />
        </a-form-item>

        <a-form-item v-if="formState.patientType === 2" label="工号" name="staffId">
          <a-input v-model:value="formState.staffId" placeholder="请输入工号" />
        </a-form-item>

        <a-form-item label="身份证照片" name="identityPhoto" required>
          <div class="upload-wrapper">
            <div v-if="formState.identityPhoto" class="preview-container">
              <img :src="formState.identityPhoto" class="preview-image" @click="previewImage(formState.identityPhoto)" />
              <a-button type="primary" size="small" @click="uploadIdentityPhoto" class="mt-2">重新上传</a-button>
            </div>
            <div v-else>
              <a-upload
                name="file"
                list-type="picture-card"
                :show-upload-list="false"
                :before-upload="beforeUpload"
                :customRequest="handleIdentityPhotoUpload"
                :accept="'.jpg,.jpeg,.png'"
              >
                <div v-if="uploading">
                  <loading-outlined />
                </div>
                <div v-else>
                  <plus-outlined />
                  <div style="margin-top: 8px">上传身份证照片</div>
                </div>
              </a-upload>
              <div class="upload-tip">请上传清晰的身份证正面照片，支持JPG、PNG格式，大小不超过5MB</div>
            </div>
          </div>
        </a-form-item>

        <a-form-item label="手持身份证照片" name="handheldIdentityPhoto" required>
          <div class="upload-wrapper">
            <div v-if="formState.handheldIdentityPhoto" class="preview-container">
              <img :src="formState.handheldIdentityPhoto" class="preview-image" @click="previewImage(formState.handheldIdentityPhoto)" />
              <a-button type="primary" size="small" @click="uploadHandheldPhoto" class="mt-2">重新上传</a-button>
            </div>
            <div v-else>
              <a-upload
                name="file"
                list-type="picture-card"
                :show-upload-list="false"
                :before-upload="beforeUpload"
                :customRequest="handleHandheldPhotoUpload"
                :accept="'.jpg,.jpeg,.png'"
              >
                <div v-if="uploading">
                  <loading-outlined />
                </div>
                <div v-else>
                  <plus-outlined />
                  <div style="margin-top: 8px">上传手持身份证照片</div>
                </div>
              </a-upload>
              <div class="upload-tip">请上传本人手持身份证的照片，确保面部和身份证信息清晰可见</div>
            </div>
          </div>
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 6, span: 14 }">
          <a-button
            type="primary"
            :loading="submitting"
            :disabled="verificationStatus === 0 || verificationStatus === 1"
            @click="submitForm"
          >
            {{ verificationStatus === 2 ? '重新提交认证' : '提交认证' }}
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 图片预览模态框 -->
    <a-modal
      v-model:open="previewVisible"
      title="照片预览"
      :footer="null"
      :width="800"
      centered
    >
      <div style="text-align: center;">
        <img :src="previewImageUrl" style="max-width: 100%; max-height: 70vh;" />
      </div>
    </a-modal>
  </PageWrapper>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { useMessage } from '/@/hooks/web/useMessage';
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue';
import { useRoute } from 'vue-router';
import { defHttp } from '/@/utils/http/axios';

const route = useRoute();
const { createMessage } = useMessage();
const formRef = ref();

// 定义表单状态类型
interface FormState {
  patientId: number | null;
  patientName: string;
  idCard: string;
  patientType: number | null;
  studentId: string;
  staffId: string;
  identityPhoto: string;
  handheldIdentityPhoto: string;
  [key: string]: any; // 允许动态属性访问
}

// 表单状态
const formState = reactive<FormState>({
  patientId: null,
  patientName: '',
  idCard: '',
  patientType: null,
  studentId: '',
  staffId: '',
  identityPhoto: '',
  handheldIdentityPhoto: '',
});

// 验证规则
const rules = {
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  staffId: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  identityPhoto: [{ required: true, message: '请上传身份证照片', trigger: 'change' }],
  handheldIdentityPhoto: [{ required: true, message: '请上传手持身份证照片', trigger: 'change' }],
};

// 状态变量
const uploading = ref(false);
const submitting = ref(false);
const previewVisible = ref(false);
const previewImageUrl = ref('');
const verificationStatus = ref<number | null>(null);
const rejectReason = ref('');

// 计算属性
const patientId = computed(() => {
  return route.params.id ? Number(route.params.id) : null;
});

// 获取患者信息
async function fetchPatientInfo() {
  if (!patientId.value) {
    createMessage.error('患者ID不能为空');
    return;
  }

  try {
    const res = await defHttp.get({
      url: `/patient/detail/${patientId.value}`,
    });

    if (res && res.code === 200) {
      const data = res.data || {};
      formState.patientId = data.patientId;
      formState.patientName = data.patientName || '';
      formState.idCard = data.idCard || '';
      formState.patientType = data.patientType;
      formState.studentId = data.studentId || '';
      formState.staffId = data.staffId || '';
      formState.identityPhoto = data.identityPhoto || '';
      formState.handheldIdentityPhoto = data.handheldIdentityPhoto || '';
      verificationStatus.value = data.identityVerify;
      
      // 获取驳回原因（如果有）
      if (data.identityVerify === 2) {
        fetchRejectReason();
      }
    }
  } catch (error) {
    console.error('获取患者信息失败', error);
    createMessage.error('获取患者信息失败');
  }
}

// 获取驳回原因
async function fetchRejectReason() {
  try {
    const res = await defHttp.get({
      url: `/patient/identity/rejectReason/${patientId.value}`,
    });
    
    if (res && res.code === 200 && res.data) {
      rejectReason.value = res.data.rejectReason || '';
    }
  } catch (error) {
    console.error('获取驳回原因失败', error);
  }
}

// 上传前检查
function beforeUpload(file: File) {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    createMessage.error('只能上传JPG或PNG格式的图片!');
    return false;
  }
  
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    createMessage.error('图片大小不能超过5MB!');
    return false;
  }
  
  return true;
}

// 上传身份证照片
function handleIdentityPhotoUpload({ file }: any) {
  uploadFile(file, 'identityPhoto');
}

// 上传手持身份证照片
function handleHandheldPhotoUpload({ file }: any) {
  uploadFile(file, 'handheldIdentityPhoto');
}

// 通用上传文件方法
async function uploadFile(file: File, fieldName: string) {
  uploading.value = true;
  
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    const res = await defHttp.uploadFile(
      {
        url: '/common/upload',
      },
      {
        file: file,
      }
    );
    
    if (res && res.code === 200) {
      formState[fieldName as keyof typeof formState] = res.data.url;
      createMessage.success('上传成功');
    } else {
      createMessage.error('上传失败');
    }
  } catch (error) {
    console.error('上传失败', error);
    createMessage.error('上传失败');
  } finally {
    uploading.value = false;
  }
}

// 重新上传身份证照片
function uploadIdentityPhoto() {
  formState.identityPhoto = '';
}

// 重新上传手持身份证照片
function uploadHandheldPhoto() {
  formState.handheldIdentityPhoto = '';
}

// 预览图片
function previewImage(url: string) {
  previewImageUrl.value = url;
  previewVisible.value = true;
}

// 提交表单
async function submitForm() {
  try {
    await formRef.value.validate();
    
    submitting.value = true;
    
    // 验证必填字段
    if (!formState.identityPhoto) {
      createMessage.error('请上传身份证照片');
      submitting.value = false;
      return;
    }
    
    if (!formState.handheldIdentityPhoto) {
      createMessage.error('请上传手持身份证照片');
      submitting.value = false;
      return;
    }
    
    // 提交认证申请
    const res = await defHttp.post({
      url: '/patient/identity/apply',
      params: {
        patientId: formState.patientId,
        studentId: formState.studentId,
        staffId: formState.staffId,
        identityPhoto: formState.identityPhoto,
        handheldIdentityPhoto: formState.handheldIdentityPhoto,
      },
    });
    
    if (res && res.code === 200) {
      createMessage.success('身份认证申请已提交，请等待管理员审核');
      verificationStatus.value = 0; // 设置为待审核状态
    } else {
      createMessage.error(res?.message || '提交失败');
    }
  } catch (error) {
    console.error('提交失败', error);
    createMessage.error('提交失败，请检查表单');
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  fetchPatientInfo();
});
</script>

<style scoped>
.mb-4 {
  margin-bottom: 16px;
}

.mt-2 {
  margin-top: 8px;
}

.upload-wrapper {
  width: 100%;
}

.preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 200px;
}

.preview-image {
  width: 200px;
  height: auto;
  max-height: 200px;
  object-fit: contain;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
}

.upload-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
  margin-top: 8px;
}
</style>
