import { defHttp } from '/@/utils/http/axios';

export interface TodayScheduleItem {
  scheduleId: number;
  doctorId: number;
  deptId: number;
  typeId?: number;
  scheduleDate: string; // YYYY-MM-DD
  timeSlot: number; // 1=上午 2=下午 3=晚上
  usedQuota?: number;
  maxQuota?: number;
  status?: number; // 1有效 0停用
  roomNumber?: string;
  createTime?: string;
  updateTime?: string;
  doctorName?: string;
  deptName?: string;
}

// key=YYYY-MM-DD, value=当日排班时段列表
export type MonthScheduleMap = Record<string, Array<{ timeSlot: number }>>;

export const listSchedulesByDate = (params?: { date?: string; deptId?: number; doctorId?: number; timeSlot?: number; keyword?: string }) =>
  defHttp.get<TodayScheduleItem[]>({ url: '/hospital/scheduleView/listByDate', params });

export const listMonthlyScheduleByDoctor = (params: { doctorId: number; year: number; month: number }) =>
  defHttp.get<MonthScheduleMap>({ url: '/admin/schedule/month-by-doctor', params });

export const listMonthlyScheduleByDept = (params: { deptId: number; year: number; month: number }) =>
  defHttp.get<MonthScheduleMap>({ url: '/admin/schedule/month-by-dept', params });

// 根据科室从doctor_schedule表获取医生列表
export const getDoctorsByDeptFromSchedule = (deptId: number) =>
  defHttp.get<Array<{ doctorId: number; doctorName: string; deptId: number }>>({
    url: '/admin/schedule/doctors-by-dept',
    params: { deptId }
  });
