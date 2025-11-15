import { MockMethod } from 'vite-plugin-mock';
import dayjs from 'dayjs';
import { resultSuccess } from '../_util';

function pick<T>(arr: T[], id?: number) {
  if (!arr.length) return undefined;
  if (id) {
    return arr.find((it: any) => it.value === id) || arr[0];
  }
  return arr[Math.floor(Math.random() * arr.length)];
}

const depts = [
  { label: '内科', value: 1 },
  { label: '外科', value: 2 },
  { label: '儿科', value: 3 },
  { label: '急诊科', value: 4 },
  { label: '骨科', value: 5 },
];

const doctors = [
  { label: '张医生', value: 101, deptId: 1 },
  { label: '李医生', value: 102, deptId: 2 },
  { label: '王医生', value: 103, deptId: 1 },
  { label: '赵医生', value: 104, deptId: 3 },
  { label: '孙医生', value: 105, deptId: 4 },
];

const rooms = ['A101', 'A102', 'B201', 'B202', 'C301', 'D401'];

function genList(params: any) {
  const date = params?.date || dayjs().format('YYYY-MM-DD');
  const size = 20;
  const list: any[] = [];
  for (let i = 0; i < size; i++) {
    const deptPick = pick(depts, params?.deptId);
    const docPool = doctors.filter((d) => !deptPick || d.deptId === deptPick.value);
    const docPick = pick(docPool, params?.doctorId);
    const timeSlot = params?.timeSlot || ((i % 3) + 1);
    const maxQuota = [20, 30, 40][timeSlot - 1];
    const usedQuota = Math.floor(Math.random() * (maxQuota + 1));
    list.push({
      scheduleId: Number(`${dayjs(date).format('YYYYMMDD')}${i + 1}`),
      doctorId: docPick?.value || 101,
      doctorName: docPick?.label || '张医生',
      deptId: deptPick?.value || 1,
      deptName: deptPick?.label || '内科',
      typeId: 1,
      scheduleDate: date,
      timeSlot,
      usedQuota,
      maxQuota,
      status: 1,
      createTime: dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
      updateTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      roomNumber: rooms[i % rooms.length],
    });
  }
  return list;
}

export default [
  {
    url: '/jeecgboot/admin/schedule/list',
    method: 'get',
    timeout: 100,
    response: ({ query }) => {
      const date = query?.date || dayjs().format('YYYY-MM-DD');
      const deptId = query?.deptId ? Number(query.deptId) : undefined;
      const doctorId = query?.doctorId ? Number(query.doctorId) : undefined;
      const timeSlot = query?.timeSlot ? Number(query.timeSlot) : undefined;
      const list = genList({ date, deptId, doctorId, timeSlot });
      return resultSuccess(list);
    },
  },
] as MockMethod[];