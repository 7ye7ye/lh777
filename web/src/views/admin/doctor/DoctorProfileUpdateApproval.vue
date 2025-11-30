<template>
  <PageWrapper title="医生资料修改审批">
    <a-card>
      <a-space class="mb-4">
        <a-select
          v-model:value="status"
          style="width: 160px"
          allow-clear
          placeholder="全部状态"
          @change="onStatusChange"
        >
          <a-select-option :value="1">待审核</a-select-option>
          <a-select-option :value="2">已通过</a-select-option>
          <a-select-option :value="3">已驳回</a-select-option>
        </a-select>
        <a-button type="primary" @click="fetchData">查询</a-button>
      </a-space>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record, text }">
          <!-- 头像列 -->
          <template v-if="column.dataIndex === 'avatar'">
            <img 
              v-if="record.avatar" 
              :src="record.avatar"
              style="width: 60px; height: 60px; object-fit: cover; cursor: pointer; border-radius: 4px;"
              @click="previewImage(record.avatar)"
            />
            <span v-else>-</span>
          </template>
          
          <!-- 状态列 -->
          <template v-else-if="column.dataIndex === 'status'">
            <a-tag v-if="text === 1" color="blue">待审核</a-tag>
            <a-tag v-else-if="text === 2" color="green">已通过</a-tag>
            <a-tag v-else-if="text === 3" color="red">已驳回</a-tag>
            <span v-else>-</span>
          </template>
          
          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" type="link" :disabled="record.status !== 1" @click="openApprove(record)">
                通过
              </a-button>
              <a-button size="small" type="link" danger :disabled="record.status !== 1" @click="openReject(record)">
                驳回
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="modalVisible" :title="modalMode === 'approve' ? '通过申请' : '驳回申请'" @ok="handleConfirm">
      <p class="mb-2">医生ID：{{ currentRecord?.doctorId }}</p>
      <p class="mb-2">医生姓名：{{ currentRecord?.doctorName }}</p>
      <p class="mb-2">擅长领域：{{ currentRecord?.specialty }}</p>
      <p class="mb-2">医生简介：{{ currentRecord?.doctorDesc }}</p>
      <a-textarea v-model:value="reason" :rows="3" placeholder="可填写审批备注" />
    </a-modal>

    <!-- 图片预览模态框 -->
    <a-modal
      v-model:open="previewVisible"
      title="医生头像预览"
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
import { ref, reactive, onMounted, onActivated } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { useMessage } from '/@/hooks/web/useMessage';
import { getDoctorProfileUpdateRequests, approveDoctorProfileUpdate, rejectDoctorProfileUpdate } from '/@/api/hospital/doctor';


const { createMessage } = useMessage();

const buildImageUrl = (relativePath: string) => {
  if (!relativePath) return '';
  // 已经是完整 URL 的情况，直接返回
  if (/^https?:\/\//.test(relativePath)) {
    return relativePath;
  }

  const baseURL = import.meta.env.VITE_GLOB_API_URL || '';
  // 如果 baseURL 中已经包含 jeecg-boot，则不再重复拼接
  const hasJeecgBoot = baseURL.includes('jeecg-boot');
  const apiPrefix = hasJeecgBoot ? '' : '/jeecg-boot';
  const cleanPrefix = apiPrefix.endsWith('/') ? apiPrefix.slice(0, -1) : apiPrefix;
  const cleanPath = relativePath.replace(/^\/+/, '');

  return `${baseURL}${cleanPrefix}/sys/common/static/${encodeURI(cleanPath)}`;
};

const loading = ref(false);
const status = ref<number | undefined>(1);
const previewVisible = ref(false);
const previewImageUrl = ref('');

interface UpdateRequestRecord {
  id: number;
  doctorId: number;
  doctorName?: string;
  avatar?: string;
  specialty: string;
  doctorDesc?: string;
  status: number;
  reason?: string;
  createTime?: string;
}

const dataSource = ref<UpdateRequestRecord[]>([]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total: number) => `共 ${total} 条`,
});

const columns = [
  { title: '申请ID', dataIndex: 'id', width: 80 },
  { title: '医生姓名', dataIndex: 'doctorName', width: 120 },
  { title: '头像', dataIndex: 'avatar', width: 80 },
  { title: '擅长领域', dataIndex: 'specialty' },
  { title: '医生简介', dataIndex: 'doctorDesc' },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '提交时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 160 },
];

async function fetchData() {
  loading.value = true;
  try {
    const res = await getDoctorProfileUpdateRequests({
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
      status: status.value,
    });
    console.log('updateRequest response:', res);
    // 在 DoctorProfileUpdateApproval.vue 里加一行调试
    console.log(
      'doctor avatar url:',
      buildImageUrl('doctor-avatar/yAJC3yt3DLmm099312ae84ff483152c07708f5172d25_1764430082732.jpg'),
    );

    const records = res?.records || [];
    dataSource.value = records;
    pagination.total = res?.total || 0;
  } catch (e) {
    console.error(e);
    createMessage.error('获取资料修改申请列表失败');
  } finally {
    loading.value = false;
  }
}

function onStatusChange() {
  // 状态切换时重置页码并重新加载
  pagination.current = 1;
  fetchData();
}

function handleTableChange(pag: any) {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
}

const modalVisible = ref(false);
const modalMode = ref<'approve' | 'reject'>('approve');
const currentRecord = ref<UpdateRequestRecord | null>(null);
const reason = ref('');

function openApprove(record: UpdateRequestRecord) {
  modalMode.value = 'approve';
  currentRecord.value = record;
  reason.value = '';
  modalVisible.value = true;
}

function openReject(record: UpdateRequestRecord) {
  modalMode.value = 'reject';
  currentRecord.value = record;
  reason.value = '';
  modalVisible.value = true;
}

async function handleConfirm() {
  if (!currentRecord.value) return;
  try {
    if (modalMode.value === 'approve') {
      await approveDoctorProfileUpdate({ requestId: currentRecord.value.id, reason: reason.value });
      createMessage.success('已通过该申请');
    } else {
      await rejectDoctorProfileUpdate({ requestId: currentRecord.value.id, reason: reason.value });
      createMessage.success('已驳回该申请');
    }
    modalVisible.value = false;
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

// 使用 keep-alive 标签切换时自动刷新当前页
onActivated(() => {
  fetchData();
});
</script>

<style scoped>
.mb-4 {
  margin-bottom: 16px;
}
</style>
