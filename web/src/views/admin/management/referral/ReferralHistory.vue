<template>
  <PageWrapper title="转诊记录管理">
    <a-card>
      <!-- 搜索栏 -->
      <div class="search-bar mb-4">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-select v-model:value="filters.status" placeholder="选择状态" style="width: 100%">
              <a-select-option value="">全部</a-select-option>
              <a-select-option value="PENDING">待审核</a-select-option>
              <a-select-option value="APPROVED">已通过</a-select-option>
              <a-select-option value="REJECTED">已拒绝</a-select-option>
              <a-select-option value="CANCELLED">已取消</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-input v-model:value="filters.patientName" placeholder="患者姓名" />
          </a-col>
          <a-col :span="6">
            <a-input v-model:value="filters.phone" placeholder="联系电话" />
          </a-col>
          <a-col :span="6">
            <a-space>
              <a-button type="primary" @click="handleSearch">搜索</a-button>
              <a-button @click="handleReset">重置</a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>

      <!-- 转诊记录表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="id"
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
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="handleViewDetail(record)">查看详情</a>
              <a v-if="record.status === 'PENDING'" @click="handleCancel(record)" type="link">取消申请</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情抽屉 -->
    <a-drawer
      v-model:open="detailVisible"
      :title="'转诊详情 - ' + (currentDetail?.referralCode || '')"
      width="520px"
    >
      <a-descriptions v-if="currentDetail" bordered column="1" size="small">
        <a-descriptions-item label="转诊单号">{{ currentDetail.referralCode }}</a-descriptions-item>
        <a-descriptions-item label="患者姓名">{{ currentDetail.patientName }}</a-descriptions-item>
        <a-descriptions-item label="性别">{{ currentDetail.gender === 'MALE' ? '男' : '女' }}</a-descriptions-item>
        <a-descriptions-item label="年龄">{{ currentDetail.age }}岁</a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ currentDetail.phone }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusText(currentDetail.status) }}</a-descriptions-item>
        <a-descriptions-item label="转诊目标">
          {{ currentDetail.targetType === 'EXTERNAL' ? currentDetail.targetHospitalName : '院内转诊' }}
          {{ currentDetail.targetDeptName ? ' · ' + currentDetail.targetDeptName : '' }}
        </a-descriptions-item>
        <a-descriptions-item label="症状描述">{{ currentDetail.symptoms || '-' }}</a-descriptions-item>
        <a-descriptions-item label="病史">{{ currentDetail.medicalHistory || '-' }}</a-descriptions-item>
        <a-descriptions-item label="转诊原因">{{ currentDetail.reason || '-' }}</a-descriptions-item>
        <a-descriptions-item label="申请时间">{{ formatDateTime(currentDetail.applyTime) }}</a-descriptions-item>
        <a-descriptions-item v-if="currentDetail.reviewTime" label="审核时间">{{ formatDateTime(currentDetail.reviewTime) }}</a-descriptions-item>
        <a-descriptions-item v-if="currentDetail.reviewComments" label="审核意见">{{ currentDetail.reviewComments }}</a-descriptions-item>
        <a-descriptions-item v-if="currentDetail.rejectReason" label="驳回原因">{{ currentDetail.rejectReason }}</a-descriptions-item>
        <a-descriptions-item v-if="currentDetail.cancelTime" label="取消时间">{{ formatDateTime(currentDetail.cancelTime) }}</a-descriptions-item>
        <a-descriptions-item v-if="currentDetail.cancelReason" label="取消原因">{{ currentDetail.cancelReason }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>

    <!-- 取消确认弹窗 -->
    <a-modal
      v-model:open="cancelVisible"
      title="取消转诊申请"
      @ok="handleConfirmCancel"
      :confirm-loading="cancelLoading"
    >
      <p>确定要取消该转诊申请吗？</p>
      <a-form-model v-model="cancelForm" layout="vertical" class="mt-4">
        <a-form-model-item label="取消原因" prop="reason">
          <a-textarea v-model:value="cancelForm.reason" rows="3" placeholder="请填写取消原因" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </PageWrapper>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { PageWrapper } from '/@/components/Page';
import dayjs from 'dayjs';
import { getPatientReferralList, getPatientReferralDetail, cancelPatientReferral } from '/@/api/hospital/referral';

// 搜索条件
const filters = reactive({
  status: '',
  patientName: '',
  phone: '',
});

// 表格数据
const tableData = ref<any[]>([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50', '100'],
});

// 详情抽屉
const detailVisible = ref(false);
const currentDetail = ref<any>(null);

// 取消弹窗
const cancelVisible = ref(false);
const cancelLoading = ref(false);
const cancelForm = reactive({
  reason: '',
});
const currentCancelId = ref<number>();

// 表格列配置
const columns = [
  {
    title: '转诊单号',
    dataIndex: 'referralCode',
    key: 'referralCode',
    width: 160,
  },
  {
    title: '患者姓名',
    dataIndex: 'patientName',
    key: 'patientName',
    width: 120,
  },
  {
    title: '联系电话',
    dataIndex: 'phone',
    key: 'phone',
    width: 140,
  },
  {
    title: '转诊目标',
    key: 'target',
    width: 200,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '申请时间',
    dataIndex: 'applyTime',
    key: 'applyTime',
    width: 180,
    customRender: ({ text }: { text: string }) => formatDateTime(text),
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right' as const,
  },
];

// 状态文本映射
const statusText = (status?: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消',
  };
  return statusMap[status || ''] || '-';
};

// 状态颜色映射
const statusColor = (status?: string) => {
  const colorMap: Record<string, string> = {
    'PENDING': 'blue',
    'APPROVED': 'green',
    'REJECTED': 'red',
    'CANCELLED': 'gold',
  };
  return colorMap[status || ''] || 'default';
};

// 格式化日期时间
const formatDateTime = (dateTime?: string) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-';
};

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      ...filters,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };
    const res = await getPatientReferralList(params);
    tableData.value = res.data?.records || [];
    pagination.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载转诊记录失败:', error);
    message.error('加载转诊记录失败');
  } finally {
    loading.value = false;
  }
};

// 处理表格分页变化
const handleTableChange = (pagination: any) => {
  Object.assign(pagination, pagination);
  loadData();
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadData();
};

// 重置
const handleReset = () => {
  filters.status = '';
  filters.patientName = '';
  filters.phone = '';
  pagination.current = 1;
  loadData();
};

// 查看详情
const handleViewDetail = async (record: any) => {
  try {
    const res = await getPatientReferralDetail(record.id);
    currentDetail.value = res.data;
    detailVisible.value = true;
  } catch (error) {
    console.error('获取详情失败:', error);
    message.error('获取详情失败');
  }
};

// 取消申请
const handleCancel = (record: any) => {
  currentCancelId.value = record.id;
  cancelForm.reason = '';
  cancelVisible.value = true;
};

// 确认取消
const handleConfirmCancel = async () => {
  if (!currentCancelId.value || !cancelForm.reason) {
    message.warning('请填写取消原因');
    return;
  }
  
  cancelLoading.value = true;
  try {
    await cancelPatientReferral({
      id: currentCancelId.value,
      reason: cancelForm.reason,
    });
    message.success('取消成功');
    cancelVisible.value = false;
    loadData(); // 重新加载数据
  } catch (error) {
    console.error('取消失败:', error);
    message.error('取消失败');
  } finally {
    cancelLoading.value = false;
  }
};

// 组件挂载时加载数据
onMounted(() => {
  loadData();
});
</script>

<style scoped>
.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}

.target-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.target-cell .sub {
  color: var(--text-color-secondary);
  font-size: 12px;
}

.search-bar {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 6px;
}
</style>