<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="排班详情" :footer="null">
    <Description @register="registerDesc" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Description, useDescription } from '/@/components/Description/index';
  import { getScheduleDetail } from '/@/api/hospital/schedule';

  const descSchema = [
    {
      field: 'scheduleId',
      label: '排班ID',
    },
    {
      field: 'doctorName',
      label: '医生姓名',
    },
    {
      field: 'deptName',
      label: '科室',
    },
    {
      field: 'scheduleDate',
      label: '排班日期',
    },
    {
      field: 'timeSlot',
      label: '时段',
      render: (val) => {
        const map = { 1: '上午', 2: '下午', 3: '晚上' };
        return map[val] || '未知';
      },
    },
    {
      field: 'usedQuota',
      label: '已用号源',
    },
    {
      field: 'status',
      label: '状态',
      render: (val) => (val === 1 ? '有效' : '停用'),
    },
    {
      field: 'createTime',
      label: '创建时间',
    },
    {
      field: 'updateTime',
      label: '更新时间',
    },
  ];

  export default defineComponent({
    name: 'ScheduleDetailModal',
    components: { BasicModal, Description },
    emits: ['register'],
    setup() {
      const [registerDesc, { setDescProps }] = useDescription({
        schema: descSchema,
        column: 2,
      });

      const [registerModal] = useModalInner(async (data) => {
        const record = data.record;
        
        // 如果需要从后端获取详细信息
        // const detail = await getScheduleDetail(record.scheduleId);
        // setDescProps({ data: detail });
        
        setDescProps({ data: record });
      });

      return { registerModal, registerDesc };
    },
  });
</script>