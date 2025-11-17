<template>
  <div>
    <PageWrapper dense contentFullHeight fixedHeight contentClass="flex" :title="'欢迎来到校医院管理系统'">
      <BasicTable @register="registerTable" class="w-4/4 xl:w-5/5" :searchInfo="searchInfo">
        <template #toolbar>
          <a-button type="primary" @click="handleCreate">
            <Icon icon="ant-design:plus-outlined" /> 新增排班
          </a-button>
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
                  icon: 'clarity:note-edit-line',
                  tooltip: '编辑',
                  onClick: handleEdit.bind(null, record),
                },
                {
                  icon: 'ant-design:delete-outlined',
                  color: 'error',
                  tooltip: '删除',
                  popConfirm: {
                    title: '是否确认删除',
                    placement: 'left',
                    confirm: handleDelete.bind(null, record),
                  },
                },
              ]"
            />
          </template>
          <template v-else-if="column.key === 'timeSlot'">
            <Tag :color="getTimeSlotColor(record.timeSlot)">
              {{ getTimeSlotText(record.timeSlot) }}
            </Tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <Tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '有效' : '停用' }}
            </Tag>
          </template>
        </template>
      </BasicTable>
      <ScheduleModal @register="registerModal" @success="handleSuccess" />
      <ScheduleDetailModal @register="registerDetailModal" />
    </PageWrapper>
  </div>
</template>
<script lang="ts">
  import { defineComponent, reactive, ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { Icon } from '/@/components/Icon';
  import { Tag } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';

  import { columns, searchFormSchema } from './schedule.data';
  import { getScheduleList, deleteSchedule } from '/@/api/hospital/schedule';
  import ScheduleModal from './ScheduleModal.vue';
  import ScheduleDetailModal from './ScheduleDetailModal.vue';

  export default defineComponent({
    name: 'ScheduleManage',
    components: { BasicTable, PageWrapper, TableAction, ScheduleModal, ScheduleDetailModal, Icon, Tag },
    setup() {
      const { createMessage } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const [registerDetailModal, { openModal: openDetailModal }] = useModal();
      const searchInfo = reactive<Recordable>({});
      const refreshLoading = ref(false);

      const [registerTable, { reload, getForm }] = useTable({
        title: '医生排班列表',
        api: getScheduleList,
        rowKey: 'scheduleId',
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
          width: 120,
          title: '操作',
          dataIndex: 'action',
          fixed: undefined,
        },
      });

      function handleCreate() {
        openModal(true, {
          isUpdate: false,
        });
      }

      function handleEdit(record: Recordable) {
        openModal(true, {
          record,
          isUpdate: true,
        });
      }

      function handleDelete(record: Recordable) {
        deleteSchedule(record.scheduleId).then(() => {
          createMessage.success('删除成功');
          reload();
        });
      }

      function handleSuccess() {
        reload();
      }

      function handleView(record: Recordable) {
        openDetailModal(true, { record });
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
        registerModal,
        registerDetailModal,
        searchInfo,
        refreshLoading,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        handleView,
        handleRefresh,
        getTimeSlotText,
        getTimeSlotColor,
      };
    },
  });
</script>