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
  maxQuota?: number;
}

export interface Schedule {
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
  maxQuota?: number;
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

export interface GetSchedulesParams {
  doctorId: number;
  startDate: string;
  days?: number; // 可选，默认 7
}

// 获取指定医生排班信息
export const getDoctorSchedules = (params: GetSchedulesParams) =>
  defHttp.get<DoctorSchedule[]>({
    url: '/admin/schedule/add',
    params,
  });

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

// 更新排班且候补成功
export const addQuotaAndFillQueue = (scheduleId: number, addCount: number) =>
  defHttp.put<boolean>({
    url: '/admin/schedule/addQuota',
    data: {
      scheduleId,
      addCount,
    },
  });



// 删除排班
export const deleteSchedule = (scheduleId: number) =>
  defHttp.delete<boolean>({
    url: `/admin/schedule/${scheduleId}`,
  });

// Excel导入排班数据
export const importScheduleExcel = (file: File, durationMinutes: number, timeSlotType?: string, maxWorkDays?: number) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('durationMinutes', durationMinutes.toString());
  if (timeSlotType) {
    formData.append('timeSlotType', timeSlotType);
  }
  if (maxWorkDays !== undefined) {
    formData.append('maxWorkDays', maxWorkDays.toString());
  }
  return defHttp.post<{
    success: boolean;
    message: string;
  }>({
    url: '/admin/schedule/importExcel',
    data: formData,
    // 不要手动设置Content-Type，让axios自动添加boundary参数
  }, {
    isTransformResponse: false,
  }).then((res: any) => {
    // 处理后端返回格式
    if (res.success) {
      return { success: true, message: res.message || '导入成功' };
    } else {
      return { success: false, message: res.message || '导入失败' };
    }
  });
};
