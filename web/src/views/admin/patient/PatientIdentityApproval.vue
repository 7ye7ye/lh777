<template>
  <PageWrapper title="患者身份认证审核">
    <a-card>
      <a-space class="mb-4">
        <a-select
          v-model:value="status"
          style="width: 160px"
          allow-clear
          placeholder="全部状态"
          @change="onStatusChange"
        >
          <a-select-option :value="0">未审核</a-select-option>
          <a-select-option :value="1">已通过</a-select-option>
          <a-select-option :value="2">未通过</a-select-option>
        </a-select>
        <a-button type="primary" @click="fetchData">查询</a-button>
      </a-space>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        row-key="patientId"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record, text }">
          <!-- 证件照片列 -->
          <template v-if="column.dataIndex === 'identityPhoto'">
            <img 
              v-if="record.identityPhoto" 
              :src="record.identityPhoto"
              style="width: 80px; height: 80px; object-fit: cover; cursor: pointer; border-radius: 4px;"
              @click="previewImage(record.identityPhoto)"
            />
            <span v-else>-</span>
          </template>

          <!-- 手持证件照片列 -->
          <template v-if="column.dataIndex === 'handheldIdentityPhoto'">
            <img
              v-if="record.handheldIdentityPhoto"
              :src="record.handheldIdentityPhoto"
              style="width: 80px; height: 80px; object-fit: cover; cursor: pointer; border-radius: 4px;"
              @click="previewImage(record.handheldIdentityPhoto)"
            />
            <span v-else>-</span>
          </template>
          
          <!-- 状态列 -->
          <template v-else-if="column.dataIndex === 'identityVerify'">
            <a-tag v-if="text === 0" color="blue">未审核</a-tag>
            <a-tag v-else-if="text === 1" color="green">已通过</a-tag>
            <a-tag v-else-if="text === 2" color="red">未通过</a-tag>
            <span v-else>-</span>
          </template>
          
          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" type="link" :disabled="record.identityVerify !== 0" @click="handleApprove(record, true)">
                通过
              </a-button>
              <a-button size="small" type="link" danger :disabled="record.identityVerify !== 0" @click="showRejectModal(record)">
                驳回
              </a-button>
              <a-tooltip v-if="record.identityVerify === 2 && record.rejectReason">
                <template #title>{{ record.rejectReason }}</template>
                <info-circle-outlined style="color: #ff4d4f; cursor: pointer;" />
              </a-tooltip>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 图片预览模态框 -->
    <a-modal
      v-model:open="previewVisible"
      title="证件照片预览"
      :footer="null"
      :width="800"
      centered
    >
      <div style="text-align: center;">
        <img :src="previewImageUrl" style="max-width: 100%; max-height: 70vh;" />
      </div>
    </a-modal>
    
    <!-- 驳回原因模态框 -->
    <a-modal
      v-model:open="rejectModalVisible"
      title="驳回原因"
      @ok="confirmReject"
      :confirmLoading="rejectLoading"
    >
      <a-form>
        <a-form-item label="驳回原因" required>
          <a-textarea 
            v-model:value="rejectReason" 
            :rows="4" 
            placeholder="请输入驳回原因，例如：证件照片模糊、信息不匹配等"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageWrapper>
</template>

<script lang="ts" setup>
console.log('PatientIdentityApproval 页面脚本已执行');
import { ref, reactive, onMounted } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { useMessage } from '/@/hooks/web/useMessage';
import { getPatientIdentityApprovals, approvePatientIdentity } from '/@/api/hospital/patient';
import { InfoCircleOutlined } from '@ant-design/icons-vue';

const { createMessage, createConfirm } = useMessage();

const loading = ref(false);
const status = ref<number | undefined>(0);
const previewVisible = ref(false);
const previewImageUrl = ref('');

// 驳回相关状态
 const rejectModalVisible = ref(false);
 const rejectLoading = ref(false);
 const rejectReason = ref('');
 const currentRecord = ref<PatientRecord | null>(null);

interface PatientRecord {
  patientId: number;
  patientName: string;
  phone?: string;
  patientType?: number;
  studentId?: string;
  staffId?: string;
  identityVerify?: number;
  identityPhoto?: string;
  handheldIdentityPhoto?: string;
  rejectReason?: string;
}

const dataSource = ref<PatientRecord[]>([]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total: number) => `共 ${total} 条`,
});

const columns = [
  { title: '患者ID', dataIndex: 'patientId', width: 80 },
  { title: '姓名', dataIndex: 'patientName', width: 120 },
  { title: '手机号', dataIndex: 'phone', width: 120 },
  {
    title: '身份',
    dataIndex: 'patientType',
    customRender: ({ text }: any) => {
      if (text === 1) return '学生';
      if (text === 2) return '教职工';
      return '其他';
    },
    width: 100,
  },
  { title: '学号', dataIndex: 'studentId', width: 120 },
  { title: '工号', dataIndex: 'staffId', width: 120 },
  { title: '证件照片', dataIndex: 'identityPhoto', width: 120 },
  { title: '手持证件照片', dataIndex: 'handheldIdentityPhoto', width: 120 },
  { title: '状态', dataIndex: 'identityVerify', width: 100 },
  { title: '操作', key: 'action', width: 160 },
];

async function fetchData() {
  console.log('fetchData called, status = ', status.value);
  loading.value = true;
  try {
    const res = await getPatientIdentityApprovals({
      status: status.value,
    });
    console.log('adminList api response:', res);
    const result: any = res || {};
    const records: PatientRecord[] = (result.records as PatientRecord[]) || [];
    dataSource.value = records;
    pagination.total = (result.total as number) || records.length || 0;
  } catch (e) {
    console.error('adminList error:', e);
    createMessage.error('获取认证申请列表失败');
  } finally {
    loading.value = false;
  }
}

function onStatusChange() {
  pagination.current = 1;
  fetchData();
}

function handleTableChange(pag: any) {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
}


function showRejectModal(record: PatientRecord) {
  currentRecord.value = record;
  rejectReason.value = '';
  rejectModalVisible.value = true;
}

// 确认驳回
async function confirmReject() {
  if (!rejectReason.value || rejectReason.value.trim() === '') {
    createMessage.warning('请输入驳回原因');
    return;
  }
  
  if (!currentRecord.value) return;
  
  rejectLoading.value = true;
  try {
    await approvePatientIdentity({
      patientId: currentRecord.value.patientId,
      approve: false,
      rejectReason: rejectReason.value
    });
    createMessage.success('已驳回该申请');
    rejectModalVisible.value = false;
    fetchData();
  } catch (e) {
    console.error(e);
    createMessage.error('操作失败，请稍后重试');
  } finally {
    rejectLoading.value = false;
  }
}

async function handleApprove(record: PatientRecord, approve: boolean) {
  if (!approve) {
    showRejectModal(record);
    return;
  }
  
  try {
    await approvePatientIdentity({
      patientId: record.patientId,
      approve: true,
    });
    createMessage.success('已通过该申请');
    fetchData();
  } catch (e) {
    console.error(e);
    createMessage.error('操作失败，请稍后重试');
  }
}

function previewImage(url: string) {
  if (url) {
    previewImageUrl.value = url;
    previewVisible.value = true;
  }
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.mb-4 {
  margin-bottom: 16px;
}
</style>
