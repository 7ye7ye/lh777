import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

export const columns: BasicColumn[] = [
  {
    title: '排班ID',
    dataIndex: 'scheduleId',
    width: 80,
  },
  {
    title: '医生姓名',
    dataIndex: 'doctorName',
    width: 100,
  },
  {
    title: '科室',
    dataIndex: 'deptName',
    width: 120,
  },
  {
    title: '排班日期',
    dataIndex: 'scheduleDate',
    width: 120,
  },
  {
    title: '时段',
    dataIndex: 'timeSlot',
    width: 80,
    customRender: ({ record }) => {
      const timeSlotMap = { 1: '上午', 2: '下午', 3: '晚上' };
      const colorMap = { 1: 'blue', 2: 'green', 3: 'orange' };
      return h(Tag, { color: colorMap[record.timeSlot] }, () => timeSlotMap[record.timeSlot]);
    },
  },
  {
    title: '已用号源',
    dataIndex: 'usedQuota',
    width: 80,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80,
    customRender: ({ record }) => {
      const status = record.status;
      const color = status === 1 ? 'green' : 'red';
      const text = status === 1 ? '有效' : '停用';
      return h(Tag, { color }, () => text);
    },
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'doctorName',
    label: '医生姓名',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'deptId',
    label: '科室',
    component: 'ApiSelect',
    componentProps: {
      api: () => import('/@/api/hospital/department').then(m => m.getDepartmentList()),
      labelField: 'deptName',
      valueField: 'deptId',
    },
    colProps: { span: 8 },
  },
  {
    field: '[startDate, endDate]',
    label: '排班日期',
    component: 'RangePicker',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'scheduleId',
    label: '排班ID',
    component: 'Input',
    show: false,
  },
  {
    field: 'doctorId',
    label: '医生',
    component: 'ApiSelect',
    componentProps: {
      api: () => import('/@/api/hospital/doctor').then(m => m.getDoctorList()),
      labelField: 'doctorName',
      valueField: 'doctorId',
    },
    required: true,
  },
  {
    field: 'deptId',
    label: '科室',
    component: 'ApiSelect',
    componentProps: {
      api: () => import('/@/api/hospital/department').then(m => m.getDepartmentList()),
      labelField: 'deptName',
      valueField: 'deptId',
    },
    required: true,
  },
  {
    field: 'scheduleDate',
    label: '排班日期',
    component: 'DatePicker',
    required: true,
  },
  {
    field: 'timeSlot',
    label: '时段',
    component: 'Select',
    componentProps: {
      options: [
        { label: '上午', value: 1 },
        { label: '下午', value: 2 },
        { label: '晚上', value: 3 },
      ],
    },
    required: true,
  },
  {
    field: 'status',
    label: '状态',
    component: 'RadioButtonGroup',
    defaultValue: 1,
    componentProps: {
      options: [
        { label: '有效', value: 1 },
        { label: '停用', value: 0 },
      ],
    },
  },
];