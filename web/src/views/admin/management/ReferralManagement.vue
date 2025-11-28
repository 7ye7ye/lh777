<template>
  <PageWrapper title="转诊审核管理">
    <div class="referral-management">
    <a-card class="filter-card" bordered>
      <a-form layout="inline" :model="filters">
        <a-form-item label="患者姓名">
          <a-input v-model:value="filters.patientName" allow-clear placeholder="输入患者姓名" />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="filters.phone" allow-clear placeholder="输入手机号" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="filters.status" allow-clear placeholder="全部状态" style="width: 150px">
            <a-select-option v-for="item in statusOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="目标类型">
          <a-select v-model:value="filters.targetType" allow-clear placeholder="全部类型" style="width: 150px">
            <a-select-option value="INTERNAL">院内科室</a-select-option>
            <a-select-option value="EXTERNAL">外部医院</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData(true)">查询</a-button>
            <a-button @click="resetFilters">重置</a-button>
            <a-button :loading="loading" @click="fetchData()">刷新</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered>
      <a-table
        row-key="id"
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'target'">
            <div class="target-cell">
              <div>{{ record.targetType === 'EXTERNAL' ? record.targetHospitalName : '院内转诊' }}</div>
              <div class="sub">{{ record.targetDeptName || '-' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'quotaAction'">
            <a-tag>
              {{
                record.quotaAction === 'DIRECT_ASSIGN'
                  ? '直接安排'
                  : record.quotaAction === 'WAITLIST'
                  ? `候补#${record.waitNumber || '-'}`
                  : '外部医院'
              }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="openDetail(record)">详情</a>
              <a v-if="record.status === 'PENDING'" @click="openReview(record)">审核</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer v-model:open="detailVisible" width="520px" title="转诊详情">
      <a-descriptions v-if="currentDetail" bordered column="1" size="small">
        <a-descriptions-item label="转诊单号">{{ currentDetail.referralCode }}</a-descriptions-item>
        <a-descriptions-item label="患者">{{ currentDetail.patientName }}</a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ currentDetail.phone }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusText(currentDetail.status) }}</a-descriptions-item>
        <a-descriptions-item label="目标">
          {{ currentDetail.targetType === 'EXTERNAL' ? currentDetail.targetHospitalName : '院内转诊' }}
          · {{ currentDetail.targetDeptName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="症状描述">{{ currentDetail.symptoms || '-' }}</a-descriptions-item>
        <a-descriptions-item label="转诊原因">{{ currentDetail.reason || '-' }}</a-descriptions-item>
        <a-descriptions-item label="号源策略">
          {{
            currentDetail.quotaAction === 'DIRECT_ASSIGN'
              ? '直接安排'
              : currentDetail.quotaAction === 'WAITLIST'
              ? `候补#${currentDetail.waitNumber || '-'}`
              : '外院转诊'
          }}
        </a-descriptions-item>
        <a-descriptions-item label="申请时间">{{ currentDetail.applyTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="审核时间">{{ currentDetail.reviewTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="审核意见">{{ currentDetail.reviewComments || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>

    <a-modal v-model:open="reviewVisible" title="审核转诊" @ok="submitReview" :confirm-loading="reviewLoading">
      <a-form :model="reviewForm" label-col="{ span: 5 }" wrapper-col="{ span: 18 }">
        <a-form-item label="审核结果" required>
          <a-radio-group v-model:value="reviewForm.decision">
            <a-radio value="APPROVE">通过</a-radio>
            <a-radio value="REJECT">驳回</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="审核医生">
          <a-input v-model:value="reviewForm.reviewDoctor" placeholder="可填写审核医生" />
        </a-form-item>
        <a-form-item v-if="reviewForm.decision === 'APPROVE'" label="审核意见">
          <a-textarea v-model:value="reviewForm.reviewComments" rows="3" placeholder="填写通过意见" />
        </a-form-item>
        <a-form-item v-else label="驳回原因" required>
          <a-textarea v-model:value="reviewForm.rejectReason" rows="3" placeholder="请填写驳回原因" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
  </PageWrapper>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { fetchReferralPage, getReferralDetail, reviewReferral } from '/@/api/hospital/referral';
import { PageWrapper } from '/@/components/Page';

const filters = reactive({
  patientName: '',
  phone: '',
  status: undefined as undefined | string,
  targetType: undefined as undefined | string,
});

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '待审核', value: 'PENDING' },
  { label: '已审核', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已取消', value: 'CANCELLED' },
];

const columns = [
  { title: '转诊编号', dataIndex: 'referralCode', key: 'code', width: 160 },
  { title: '患者', dataIndex: 'patientName', key: 'patientName', width: 100 },
  { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '目标', key: 'target', width: 200 },
  { title: '号源', key: 'quotaAction', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '申请时间', dataIndex: 'applyTime', key: 'applyTime', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 120 },
];

const tableData = ref<any[]>([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const detailVisible = ref(false);
const currentDetail = ref<any | null>(null);

const reviewVisible = ref(false);
const reviewLoading = ref(false);
const reviewForm = reactive({
  id: undefined as undefined | number,
  decision: 'APPROVE',
  reviewDoctor: '',
  reviewComments: '',
  rejectReason: '',
});

const statusText = (status?: string) => {
  switch (status) {
    case 'PENDING':
      return '待审核';
    case 'APPROVED':
      return '已审核';
    case 'REJECTED':
      return '已拒绝';
    case 'CANCELLED':
      return '已取消';
    default:
      return '待审核';
  }
};

const statusColor = (status?: string) => {
  switch (status) {
    case 'APPROVED':
      return 'green';
    case 'REJECTED':
      return 'red';
    case 'CANCELLED':
      return 'gold';
    default:
      return 'blue';
  }
};

const fetchData = async (reset = false) => {
  if (reset) pagination.current = 1;
  loading.value = true;
  try {
    const res = await fetchReferralPage({
      ...filters,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    });
    tableData.value = res.records || [];
    pagination.total = res.total || 0;
  } finally {
    loading.value = false;
  }
};

const handleTableChange = (pager: any) => {
  pagination.current = pager.current;
  pagination.pageSize = pager.pageSize;
  fetchData();
};

const resetFilters = () => {
  filters.patientName = '';
  filters.phone = '';
  filters.status = undefined;
  filters.targetType = undefined;
  fetchData(true);
};

const openDetail = async (record: any) => {
  const detail = await getReferralDetail(record.id);
  currentDetail.value = detail;
  detailVisible.value = true;
};

const openReview = (record: any) => {
  reviewForm.id = record.id;
  reviewForm.decision = 'APPROVE';
  reviewForm.reviewDoctor = '';
  reviewForm.reviewComments = '';
  reviewForm.rejectReason = '';
  reviewVisible.value = true;
};

const submitReview = async () => {
  if (!reviewForm.id) return;
  if (reviewForm.decision === 'REJECT' && !reviewForm.rejectReason) {
    return message.warning('请填写驳回原因');
  }
  reviewLoading.value = true;
  try {
    await reviewReferral(reviewForm);
    reviewVisible.value = false;
    fetchData();
  } finally {
    reviewLoading.value = false;
  }
};

onMounted(() => fetchData(true));
</script>

<style scoped>
.referral-management {
  padding: 16px;
}

.filter-card {
  margin-bottom: 16px;
}

.ml-8 {
  margin-left: 8px;
}

.target-cell .sub {
  font-size: 12px;
  color: #999;
}
</style>

