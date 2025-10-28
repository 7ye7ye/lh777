import { defHttp } from '/@/utils/http/axios';

export interface DoctorSchedule {
  scheduleId: number;
  doctorId: number;
  doctorName?: string;
  deptId: number;
  deptName?: string;
  scheduleDate: string;
  timeSlot: number; // 1-上午, 2-下午, 3-晚上
  usedQuota?: number;
  status: number;
  createTime?: string;
  updateTime?: string;
}

export interface ScheduleListParams {
  doctorId?: number;
  doctorName?: string;
  deptId?: number;
  startDate?: string;
  endDate?: string;
  current?: number;
  size?: number;
}

export interface ScheduleCreateRequest {
  doctorId: number;
  deptId: number;
  date: string;
  shift: string;
  slots: number;
  remark?: string;
}

export interface ScheduleUpdateRequest {
  scheduleId: number;
  doctorId?: number;
  deptId?: number;
  date?: string;
  shift?: string;
  slots?: number;
  bookedSlots?: number;
  status?: number;
  remark?: string;
}

// 获取医生排班列表
export const getScheduleList = (params: ScheduleListParams) =>
  defHttp.get<{
    records: DoctorSchedule[];
    total: number;
    current: number;
    size: number;
  }>({
    url: '/admin/schedule/list',
    params,
  });

// 获取排班详情
export const getScheduleDetail = (scheduleId: number) =>
  defHttp.get<DoctorSchedule>({
    url: `/admin/schedule/${scheduleId}`,
  });

// 创建排班
export const createSchedule = (data: ScheduleCreateRequest) =>
  defHttp.post<DoctorSchedule>({
    url: '/admin/schedule/create',
    data,
  });

// 更新排班
export const updateSchedule = (data: ScheduleUpdateRequest) =>
  defHttp.put<boolean>({
    url: '/admin/schedule/update',
    data,
  });

// 删除排班
export const deleteSchedule = (scheduleId: number) =>
  defHttp.delete<boolean>({
    url: `/admin/schedule/${scheduleId}`,
  });