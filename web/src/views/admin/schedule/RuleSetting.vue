<template>
  <PageWrapper :title="'排班规则制定'">
    <div class="page">
      <a-row :gutter="16">
        <a-col :xs="24" :lg="12">
          <a-card title="基础规则设置" :bordered="false">
            <a-form layout="vertical">
              <a-form-item label="最长工作时间(天)">
                <a-input-number v-model:value="form.basic.maxWorkDays" :min="1" :max="7" />
                <div style="color: #999; font-size: 12px; margin-top: 4px;">
                  设置医生连续工作的最大天数，超过此天数将自动调整排班（默认：1天，即不能连续工作两天）
                </div>
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :xs="24" :lg="24">
          <a-card title="排班文件导入" :bordered="false">
            <a-alert
              message="文件格式说明"
              description="请按照以下格式准备Excel或CSV文件：第一行为表头（医生姓名、科室名称、排班日期、时段、号源数量），从第二行开始为数据。排班日期格式：yyyy-MM-dd，时段：上午/下午/晚上，号源数量可选（不填则根据规则自动计算）。支持的文件格式：.xlsx、.xls、.csv"
              type="info"
              show-icon
              style="margin-bottom: 16px"
            />
            <a-upload
              :before-upload="handleBeforeUpload"
              :file-list="fileList"
              accept=".xlsx,.xls,.csv"
              :max-count="1"
            >
              <a-button type="primary">
                <template #icon><UploadOutlined /></template>
                选择文件（Excel/CSV）
              </a-button>
            </a-upload>
            <div style="margin-top: 16px">
              <a-button 
                type="primary" 
                :loading="uploading" 
                :disabled="!fileList || fileList.length === 0"
                @click="handleUpload"
              >
                开始导入
              </a-button>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <div class="section">
        <a-space>
          <a-button type="primary" @click="handleSave">保存</a-button>
          <a-button @click="handleReset">重置</a-button>
        </a-space>
      </div>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { UploadOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { importScheduleExcel } from '/@/api/hospital/schedule';
import type { UploadFile, UploadProps } from 'ant-design-vue';

export default defineComponent({
  name: 'AdminScheduleRules',
  components: { PageWrapper, UploadOutlined },
  setup() {
    const form = reactive({
      basic: { maxWorkDays: 1 },
    });

    const fileList = ref<UploadFile[]>([]);
    const uploading = ref(false);
    let currentFile: File | null = null;

    function handleSave() {
      console.log('save schedule rule', JSON.stringify(form));
    }
    function handleReset() {
      form.basic.maxWorkDays = 1;
    }

    const handleBeforeUpload: UploadProps['beforeUpload'] = (file) => {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        || file.type === 'application/vnd.ms-excel'
        || file.name.endsWith('.xlsx')
        || file.name.endsWith('.xls');
      const isCsv = file.type === 'text/csv' 
        || file.type === 'text/plain'
        || file.type === 'application/csv'
        || file.name.endsWith('.csv');
      if (!isExcel && !isCsv) {
        message.error('只能上传Excel或CSV文件（.xlsx、.xls或.csv格式）');
        return false;
      }
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        message.error('文件大小不能超过10MB');
        return false;
      }
      currentFile = file;
      fileList.value = [file as UploadFile];
      return false; // 阻止自动上传
    };

    async function handleUpload() {
      if (!currentFile) {
        message.warning('请先选择文件');
        return;
      }

      uploading.value = true;
      try {
        // 使用默认值：单次时长30分钟，时段类型为上午/下午/晚上
        const result = await importScheduleExcel(
          currentFile,
          30, // 默认单次时长30分钟
          'am-pm-night', // 默认时段类型
          form.basic.maxWorkDays || 1
        );
        if (result.success) {
          message.success(result.message || '导入成功');
          fileList.value = [];
          currentFile = null;
        } else {
          message.error(result.message || '导入失败');
        }
      } catch (error: any) {
        console.error('导入失败', error);
        message.error(error.message || '导入失败，请检查文件格式');
      } finally {
        uploading.value = false;
      }
    }

    return { 
      form, 
      handleSave, 
      handleReset,
      fileList,
      uploading,
      handleBeforeUpload,
      handleUpload,
    };
  },
});
</script>

<style scoped>
.page { padding: 16px; }
.section { margin-top: 16px; }
</style>


