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
                  icon: 'ant-design:swap-outlined',
                  tooltip: '申请调班',
                  onClick: handleApplyShift.bind(null, record),
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
      <BasicModal @register="registerApplyModal" title="申请调班" @ok="handleApplySubmit">
        <BasicForm @register="registerApplyForm" />
      </BasicModal>
    </PageWrapper>
  </div>
</template>
<script lang="ts">
  import { defineComponent, reactive, ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { BasicModal } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { Icon } from '/@/components/Icon';
  import { Tag } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';

  import { columns, searchFormSchema } from './schedule.data';
  import { getScheduleList, deleteSchedule } from '/@/api/hospital/schedule';
  import { getDepartmentList } from '/@/api/hospital/department';
  import { applyShiftChange } from '/@/api/hospital/adjustment';
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
      const applyScheduleId = ref<number | null>(null);
      const deptOptions = ref<{ label: string; value: number }[]>([]);

      const applyFormSchema = [
        {
          field: 'originalScheduleId',
          label: '原排班ID',
          component: 'Input',
          componentProps: { disabled: true },
        },
        {
          field: 'targetDate',
          label: '目标日期',
          component: 'DatePicker',
          required: true,
          componentProps: {
            placeholder: '选择日期',
            style: { width: '100%' },
          },
        },
        {
          field: 'targetTimeSlot',
          label: '目标时段',
          component: 'Select',
          required: true,
          componentProps: {
            options: [
              { label: '上午', value: 1 },
              { label: '下午', value: 2 },
              { label: '晚上', value: 3 },
            ],
            placeholder: '选择时段',
          },
        },
        {
          field: 'targetDeptId',
          label: '目标科室',
          component: 'Select',
          required: true,
          componentProps: {
            options: deptOptions,
            showSearch: true,
            allowClear: true,
            placeholder: '选择科室',
          },
        },
        {
          field: 'reason',
          label: '申请原因',
          component: 'InputTextArea',
          required: true,
          componentProps: {
            rows: 4,
            placeholder: '请输入申请原因',
          },
        },
      ];

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

      const [registerApplyForm, { setFieldsValue, resetFields, validate }] = useForm({
        labelWidth: 100,
        baseColProps: { span: 24 },
        schemas: applyFormSchema,
        showActionButtonGroup: false,
      });

      const [registerApplyModal, { openModal: openApplyModal, setModalProps: setApplyModalProps, closeModal: closeApplyModal }] = useModal();

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

      async function handleApplyShift(record: Recordable) {
        applyScheduleId.value = record.scheduleId as number;
        const list = await getDepartmentList();
        deptOptions.value = (list || []).map((d: any) => ({ label: d.deptName, value: d.deptId }));
        resetFields();
        setFieldsValue({ originalScheduleId: applyScheduleId.value });
        openApplyModal(true);
      }

      async function handleApplySubmit() {
        try {
          const values = await validate();
          setApplyModalProps({ confirmLoading: true });
          const payload = {
            originalScheduleId: Number(values.originalScheduleId),
            targetDate: values.targetDate && values.targetDate.format ? values.targetDate.format('YYYY-MM-DD') : values.targetDate,
            targetTimeSlot: Number(values.targetTimeSlot),
            targetDeptId: Number(values.targetDeptId),
            reason: String(values.reason || '').trim(),
          };
          await applyShiftChange(payload as any);
          closeApplyModal();
          createMessage.success('申请已提交');
        } finally {
          setApplyModalProps({ confirmLoading: false });
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
        registerApplyModal,
        registerApplyForm,
        searchInfo,
        refreshLoading,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        handleView,
        handleRefresh,
        handleApplyShift,
        handleApplySubmit,
        getTimeSlotText,
        getTimeSlotColor,
      };
    },
  });
</script>