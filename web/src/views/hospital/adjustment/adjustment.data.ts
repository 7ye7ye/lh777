import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

export const columns: BasicColumn[] = [
  {
    title: '申请ID',
    dataIndex: 'adjustmentId',
    width: 80,
  },
  {
    title: '医生姓名',
    dataIndex: 'doctorName',
    width: 100,
  },
  {
    title: '原排班日期',
    dataIndex: 'originalDate',
    width: 120,
  },
  {
    title: '目标日期',
    dataIndex: 'targetDate',
    width: 120,
  },
  {
    title: '目标时段',
    dataIndex: 'targetTimeSlot',
    width: 80,
    customRender: ({ record }) => {
      const timeSlotMap = { 1: '上午', 2: '下午', 3: '晚上' };
      const colorMap = { 1: 'blue', 2: 'green', 3: 'orange' };
      return h(Tag, { color: colorMap[record.targetTimeSlot] }, () => timeSlotMap[record.targetTimeSlot]);
    },
  },
  {
    title: '目标科室',
    dataIndex: 'targetDeptName',
    width: 120,
  },
  {
    title: '申请原因',
    dataIndex: 'reason',
    width: 200,
    ellipsis: true,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80,
    customRender: ({ record }) => {
      const statusMap = { 1: '待审批', 2: '已通过', 3: '已驳回', 4: '已取消' };
      const colorMap = { 1: 'orange', 2: 'green', 3: 'red', 4: 'gray' };
      return h(Tag, { color: colorMap[record.status] }, () => statusMap[record.status]);
    },
  },
  {
    title: '申请时间',
    dataIndex: 'applyTime',
    width: 180,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'status',
    label: '状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '全部', value: '' },
        { label: '待审批', value: 1 },
        { label: '已通过', value: 2 },
        { label: '已驳回', value: 3 },
        { label: '已取消', value: 4 },
      ],
    },
    colProps: { span: 8 },
  },
  {
    field: 'doctorName',
    label: '医生姓名',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: '[startDate, endDate]',
    label: '申请日期',
    component: 'RangePicker',
    colProps: { span: 8 },
  },
];