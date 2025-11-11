<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="调班申请详情" :footer="null">
    <Description @register="registerDesc" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Description, useDescription } from '/@/components/Description/index';

  const descSchema = [
    {
      field: 'adjustmentId',
      label: '申请ID',
    },
    {
      field: 'doctorName',
      label: '医生姓名',
    },
    {
      field: 'originalScheduleId',
      label: '原排班ID',
    },
    {
      field: 'originalDate',
      label: '原排班日期',
    },
    {
      field: 'targetDate',
      label: '目标日期',
    },
    {
      field: 'targetTimeSlot',
      label: '目标时段',
      render: (val) => {
        const map = { 1: '上午', 2: '下午', 3: '晚上' };
        return map[val] || '未知';
      },
    },
    {
      field: 'targetDeptName',
      label: '目标科室',
    },
    {
      field: 'reason',
      label: '申请原因',
      span: 2,
    },
    {
      field: 'status',
      label: '状态',
      render: (val) => {
        const map = { 1: '待审批', 2: '已通过', 3: '已驳回', 4: '已取消' };
        return map[val] || '未知';
      },
    },
    {
      field: 'applyTime',
      label: '申请时间',
    },
    {
      field: 'approveTime',
      label: '审批时间',
    },
    {
      field: 'rejectReason',
      label: '驳回原因',
      span: 2,
      ifShow: (data) => data.status === 3 && data.rejectReason,
    },
  ];

  export default defineComponent({
    name: 'AdjustmentDetailModal',
    components: { BasicModal, Description },
    emits: ['register'],
    setup() {
      const [registerDesc, { setDescProps }] = useDescription({
        schema: descSchema,
        column: 2,
      });

      const [registerModal] = useModalInner(async (data) => {
        const record = data.record;
        setDescProps({ data: record });
      });

      return { registerModal, registerDesc };
    },
  });
</script>