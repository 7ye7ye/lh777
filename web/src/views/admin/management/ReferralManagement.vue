<template>
  <PageWrapper title="转诊管理">
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
              <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
              <a-button 
                v-if="record.status === 'PENDING' || (record.status && String(record.status).toUpperCase() === 'PENDING')" 
                type="primary" 
                size="small"
                @click="(e) => openReview(record, e)"
              >
                审核
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer v-model:open="detailVisible" width="600px" title="转诊详情">
      <a-descriptions v-if="currentDetail" bordered column="1" size="small">
        <a-descriptions-item label="转诊单号">{{ currentDetail.referralCode }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor(currentDetail.status)">{{ statusText(currentDetail.status) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="患者信息">
          {{ currentDetail.patientName }} 
          <span v-if="currentDetail.gender">（{{ currentDetail.gender === 'MALE' ? '男' : currentDetail.gender === 'FEMALE' ? '女' : currentDetail.gender }}）</span>
          <span v-if="currentDetail.age">，{{ currentDetail.age }}岁</span>
        </a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ currentDetail.phone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="转诊目标">
          <div>
            <div>{{ currentDetail.targetType === 'EXTERNAL' ? (currentDetail.targetHospitalName || '外部医院') : '院内转诊' }}</div>
            <div v-if="currentDetail.targetDeptName" style="color: #999; font-size: 12px; margin-top: 4px;">
              {{ currentDetail.targetDeptName }}
            </div>
          </div>
        </a-descriptions-item>
        <a-descriptions-item label="症状描述">{{ currentDetail.symptoms || '-' }}</a-descriptions-item>
        <a-descriptions-item label="既往病史">{{ currentDetail.medicalHistory || '-' }}</a-descriptions-item>
        <a-descriptions-item label="转诊原因">{{ currentDetail.reason || '-' }}</a-descriptions-item>
        <a-descriptions-item label="号源策略">
          {{
            currentDetail.quotaAction === 'DIRECT_ASSIGN'
              ? '直接安排'
              : currentDetail.quotaAction === 'WAITLIST'
              ? `候补#${currentDetail.waitNumber || '-'}`
              : '外部医院'
          }}
        </a-descriptions-item>
        <a-descriptions-item label="申请来源">
          {{ currentDetail.sourceType === 'DOCTOR_DIRECT' ? '医生发起' : '患者申请' }}
        </a-descriptions-item>
        <a-descriptions-item label="申请时间">{{ formatDateTime(currentDetail.applyTime) }}</a-descriptions-item>
        <a-descriptions-item v-if="currentDetail.reviewTime" label="审核时间">
          {{ formatDateTime(currentDetail.reviewTime) }}
        </a-descriptions-item>
        <a-descriptions-item v-if="currentDetail.reviewDoctor" label="审核医生">
          {{ currentDetail.reviewDoctor }}
        </a-descriptions-item>
        <a-descriptions-item v-if="currentDetail.reviewComments" label="审核意见">
          {{ currentDetail.reviewComments }}
        </a-descriptions-item>
        <a-descriptions-item v-if="currentDetail.rejectReason" label="驳回原因">
          <span style="color: #ff4d4f;">{{ currentDetail.rejectReason }}</span>
        </a-descriptions-item>
        <a-descriptions-item v-if="currentDetail.attachments && currentDetail.attachments.length > 0" label="上传资料">
          <div style="display: flex; flex-wrap: wrap; gap: 8px;">
            <img
              v-for="(item, index) in currentDetail.attachments"
              :key="index"
              :src="item.url || item"
              style="width: 80px; height: 80px; object-fit: cover; border-radius: 4px; cursor: pointer; border: 1px solid #e8e8e8;"
              @click="previewImage(item.url || item, currentDetail.attachments)"
              alt="附件图片"
            />
          </div>
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>

    <a-modal 
      v-model:open="reviewVisible" 
      title="审核转诊申请" 
      @ok="submitReview" 
      @cancel="handleReviewCancel"
      :confirm-loading="reviewLoading"
      :mask-closable="false"
      destroy-on-close
      width="600px"
    >
      <a-form :model="reviewForm" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="审核结果" :required="true">
          <a-radio-group v-model:value="reviewForm.decision">
            <a-radio value="APPROVE">通过</a-radio>
            <a-radio value="REJECT">驳回</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="审核医生">
          <a-input 
            v-model:value="reviewForm.reviewDoctor" 
            placeholder="可填写审核医生姓名（选填）" 
            :maxlength="50"
          />
        </a-form-item>
        <!-- 审核意见 / 驳回原因，使用 key 区分两种状态，避免渲染异常 -->
        <template v-if="reviewForm.decision === 'APPROVE'">
          <a-form-item
            key="approveComment"
            label="审核意见"
          >
            <a-textarea
              v-model:value="reviewForm.reviewComments"
              :rows="3"
              placeholder="填写通过意见，如：同意转诊（选填）"
              :maxlength="500"
              show-count
            />
          </a-form-item>
        </template>
        <template v-else>
          <a-form-item
            key="rejectReason"
            label="驳回原因"
            :required="true"
          >
            <a-textarea
              v-model:value="reviewForm.rejectReason"
              :rows="3"
              placeholder="请详细填写驳回原因"
              :maxlength="500"
              show-count
            />
          </a-form-item>
        </template>
      </a-form>
    </a-modal>
  </div>
  </PageWrapper>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { fetchReferralPage, getReferralDetail, reviewReferral } from '/@/api/hospital/referral';
import { PageWrapper } from '/@/components/Page';
import { createImgPreview } from '/@/components/Preview';

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
  { 
    title: '申请时间', 
    dataIndex: 'applyTime', 
    key: 'applyTime', 
    width: 180,
    customRender: ({ text }: { text: string }) => formatDateTime(text),
  },
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

const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '-';
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
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
  } catch (error: any) {
    console.error('加载转诊申请失败:', error);
    message.error(error?.message || '加载转诊申请失败');
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
  try {
    const detail = await getReferralDetail(record.id);
    currentDetail.value = detail;
    detailVisible.value = true;
  } catch (error: any) {
    console.error('加载转诊详情失败:', error);
    message.error(error?.message || '加载转诊详情失败');
  }
};

const openReview = (record: any, event?: Event) => {
  if (event) {
    event.stopPropagation();
    event.preventDefault();
  }
  console.log('openReview called with record:', record);
  console.log('record.status:', record?.status);
  if (!record || !record.id) {
    message.warning('转诊记录信息不完整');
    return;
  }
  // 重置表单
  reviewForm.id = record.id;
  reviewForm.decision = 'APPROVE';
  reviewForm.reviewDoctor = '';
  reviewForm.reviewComments = '';
  reviewForm.rejectReason = '';
  
  // 直接设置，不使用 nextTick
  reviewVisible.value = true;
  console.log('reviewVisible set to:', reviewVisible.value);
};

const handleReviewCancel = () => {
  reviewForm.id = undefined;
  reviewForm.decision = 'APPROVE';
  reviewForm.reviewDoctor = '';
  reviewForm.reviewComments = '';
  reviewForm.rejectReason = '';
  reviewVisible.value = false;
};

const previewImage = (current: string | any, attachments?: any[]) => {
  if (!attachments || attachments.length === 0) {
    const url = typeof current === 'string' ? current : (current?.url || current);
    createImgPreview({ imageList: [url] });
    return;
  }
  const imageList = attachments.map((item: any) => item?.url || item);
  createImgPreview({ 
    imageList,
  });
};

const submitReview = async () => {
  if (!reviewForm.id) {
    message.warning('转诊申请ID不能为空');
    return;
  }
  
  if (reviewForm.decision === 'REJECT' && !reviewForm.rejectReason?.trim()) {
    message.warning('请填写驳回原因');
    return;
  }
  
  reviewLoading.value = true;
  try {
    await reviewReferral(reviewForm);
    message.success('审核成功');
    reviewVisible.value = false;
    // 重置表单
    reviewForm.id = undefined;
    reviewForm.decision = 'APPROVE';
    reviewForm.reviewDoctor = '';
    reviewForm.reviewComments = '';
    reviewForm.rejectReason = '';
    // 刷新列表
    fetchData();
  } catch (error: any) {
    console.error('审核失败:', error);
    message.error(error?.message || '审核失败，请重试');
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

