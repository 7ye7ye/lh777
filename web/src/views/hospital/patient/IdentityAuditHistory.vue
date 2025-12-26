<template>
  <PageWrapper title="身份认证审核历史">
    <a-card>
      <a-timeline>
        <a-timeline-item v-for="(log, index) in auditLogs" :key="index" :color="getActionColor(log.action)">
          <template #dot>
            <component :is="getActionIcon(log.action)" />
          </template>
          <div class="history-item">
            <div class="history-header">
              <span class="history-action">{{ getActionText(log.action) }}</span>
              <span class="history-time">{{ formatDateTime(log.createTime) }}</span>
            </div>
            <div class="history-content">
              {{ log.description }}
            </div>
            <div class="history-operator" v-if="log.operatorName">
              操作人: {{ log.operatorName }}
            </div>
          </div>
        </a-timeline-item>
        <a-timeline-item v-if="auditLogs.length === 0">
          <div class="empty-history">暂无审核历史记录</div>
        </a-timeline-item>
      </a-timeline>
      
      <div class="action-buttons">
        <a-button type="primary" @click="goBack">返回</a-button>
      </div>
    </a-card>
  </PageWrapper>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { useMessage } from '/@/hooks/web/useMessage';
import { useRoute, useRouter } from 'vue-router';
import { getPatientIdentityAuditHistory } from '/@/api/hospital/patient';
import dayjs from 'dayjs';
import { 
  CheckCircleOutlined, 
  CloseCircleOutlined, 
  ExclamationCircleOutlined, 
  LoadingOutlined,
  FileAddOutlined
} from '@ant-design/icons-vue';

const route = useRoute();
const router = useRouter();
const { createMessage } = useMessage();

// 审核日志列表
const auditLogs = ref<any[]>([]);
const loading = ref(false);

// 获取患者ID
const patientId = ref<number | null>(null);

// 初始化
onMounted(() => {
  const id = route.params.id;
  if (id) {
    patientId.value = Number(id);
    fetchAuditHistory();
  } else {
    createMessage.error('患者ID不能为空');
  }
});

// 获取审核历史
async function fetchAuditHistory() {
  if (!patientId.value) return;
  
  loading.value = true;
  try {
    const res = await getPatientIdentityAuditHistory(patientId.value);
    if (res && res.code === 200 && res.data) {
      auditLogs.value = res.data.auditLogs || [];
    } else {
      createMessage.error(res?.message || '获取审核历史失败');
    }
  } catch (error) {
    console.error('获取审核历史失败', error);
    createMessage.error('获取审核历史失败');
  } finally {
    loading.value = false;
  }
}

// 格式化日期时间
function formatDateTime(dateTime: string | null) {
  if (!dateTime) return '-';
  try {
    return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
  } catch (error) {
    console.error('Date formatting error:', error);
    return dateTime; // 如果无法格式化，返回原始字符串
  }
}

// 获取操作类型对应的颜色
function getActionColor(action: string) {
  switch (action) {
    case 'SUBMIT':
    case 'RESUBMIT':
      return 'blue';
    case 'APPROVE':
      return 'green';
    case 'REJECT':
      return 'red';
    default:
      return 'gray';
  }
}

// 获取操作类型对应的图标
function getActionIcon(action: string) {
  switch (action) {
    case 'SUBMIT':
      return FileAddOutlined;
    case 'RESUBMIT':
      return LoadingOutlined;
    case 'APPROVE':
      return CheckCircleOutlined;
    case 'REJECT':
      return CloseCircleOutlined;
    default:
      return ExclamationCircleOutlined;
  }
}

// 获取操作类型对应的文本
function getActionText(action: string) {
  switch (action) {
    case 'SUBMIT':
      return '提交认证申请';
    case 'RESUBMIT':
      return '重新提交认证申请';
    case 'APPROVE':
      return '审核通过';
    case 'REJECT':
      return '审核驳回';
    default:
      return '未知操作';
  }
}

// 返回上一页
function goBack() {
  router.back();
}
</script>

<style scoped>
.history-item {
  padding: 8px 0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.history-action {
  font-weight: bold;
}

.history-time {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.history-content {
  margin-bottom: 4px;
}

.history-operator {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.empty-history {
  color: rgba(0, 0, 0, 0.45);
  text-align: center;
  padding: 16px 0;
}

.action-buttons {
  margin-top: 24px;
  text-align: center;
}
</style>
