<template>
  <PageWrapper :title="'欢迎来到校医院管理系统'">
    <div>
      <PageWrapper dense contentFullHeight fixedHeight contentClass="flex">
        <BasicTable @register="registerTable" class="w-4/4 xl:w-5/5" :searchInfo="searchInfo">
          <template #toolbar>
            <a-button @click="handleRefresh" :loading="refreshLoading">
              <Icon icon="ant-design:reload-outlined" /> 刷新
            </a-button>
          </template>
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <TableAction
                :actions="[
                  {
                    icon: 'clarity:info-standard-line',
                    tooltip: '查看详情',
                    onClick: handleView.bind(null, record),
                  },
                  {
                    icon: 'ant-design:check-outlined',
                    color: 'success',
                    tooltip: '通过',
                    ifShow: record.status === 1,
                    onClick: handleApprove.bind(null, record),
                  },
                  {
                    icon: 'ant-design:close-outlined',
                    color: 'error',
                    tooltip: '驳回',
                    ifShow: record.status === 1,
                    onClick: handleReject.bind(null, record),
                  },
                ]"
              />
            </template>
            <template v-else-if="column.key === 'status'">
              <Tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </Tag>
            </template>
            <template v-else-if="column.key === 'timeSlot'">
              <Tag :color="getTimeSlotColor(record.timeSlot)">
                {{ getTimeSlotText(record.timeSlot) }}
              </Tag>
            </template>
          </template>
        </BasicTable>
        <AdjustmentDetailModal @register="registerDetailModal" />
        <RejectModal @register="registerRejectModal" @success="handleSuccess" />
      </PageWrapper>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, reactive, ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { Icon } from '/@/components/Icon';
  import { Tag } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';

  import { columns, searchFormSchema } from './adjustment.data';
  import { getAdjustmentList, approveAdjustment } from '/@/api/hospital/adjustment';
  import AdjustmentDetailModal from './AdjustmentDetailModal.vue';
  import RejectModal from './RejectModal.vue';

  export default defineComponent({
    name: 'AdjustmentApprove',
    components: { BasicTable, PageWrapper, TableAction, AdjustmentDetailModal, RejectModal, Icon, Tag },
    setup() {
      const { createMessage } = useMessage();
      const [registerDetailModal, { openModal: openDetailModal }] = useModal();
      const [registerRejectModal, { openModal: openRejectModal }] = useModal();
      const searchInfo = reactive<Recordable>({});
      const refreshLoading = ref(false);

      const [registerTable, { reload }] = useTable({
        title: '调班申请列表',
        api: getAdjustmentList,
        rowKey: 'adjustmentId',
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
          autoSubmitOnEnter: true,
        },
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        actionColumn: {
          width: 150,
          title: '操作',
          dataIndex: 'action',
          fixed: undefined,
        },
      });

      function handleView(record: Recordable) {
        openDetailModal(true, { record });
      }

      function handleApprove(record: Recordable) {
        approveAdjustment({
          adjustmentId: record.adjustmentId,
          status: 2, // 通过
        }).then(() => {
          createMessage.success('审批通过');
          reload();
        });
      }

      function handleReject(record: Recordable) {
        openRejectModal(true, { record });
      }

      function handleSuccess() {
        reload();
      }

      async function handleRefresh() {
        refreshLoading.value = true;
        try {
          await reload();
          createMessage.success('刷新成功');
        } catch (error) {
          createMessage.error('刷新失败');
        } finally {
          refreshLoading.value = false;
        }
      }

      function getStatusText(status: number) {
        const map = { 1: '待审批', 2: '已通过', 3: '已驳回', 4: '已取消' };
        return map[status] || '未知';
      }

      function getStatusColor(status: number) {
        const map = { 1: 'orange', 2: 'green', 3: 'red', 4: 'gray' };
        return map[status] || 'default';
      }

      function getTimeSlotText(timeSlot: number) {
        const map = { 1: '上午', 2: '下午', 3: '晚上' };
        return map[timeSlot] || '未知';
      }

      function getTimeSlotColor(timeSlot: number) {
        const map = { 1: 'blue', 2: 'green', 3: 'orange' };
        return map[timeSlot] || 'default';
      }

      return {
        registerTable,
        registerDetailModal,
        registerRejectModal,
        searchInfo,
        refreshLoading,
        handleView,
        handleApprove,
        handleReject,
        handleSuccess,
        handleRefresh,
        getStatusText,
        getStatusColor,
        getTimeSlotText,
        getTimeSlotColor,
      };
    },
  });
</script>