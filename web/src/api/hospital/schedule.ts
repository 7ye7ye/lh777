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
  maxQuota?: number;
  roomNumber?: string;
  status: number;
  createTime?: string;
  updateTime?: string;
}

export interface ScheduleListParams {
  doctorId?: number;
  doctorName?: string;
  deptId?: number;
  date?: string;  // 单个日期查询
  startDate?: string;  // 日期范围查询开始日期
  endDate?: string;  // 日期范围查询结束日期
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
  roomNumber?: string;
  maxQuota?: number;
}

export interface ScheduleUpdateRequest {
  scheduleId: number;
  doctorId?: number;
  deptId?: number;
  date?: string;
  shift?: string;
  timeSlot?: number; // 1-上午, 2-下午, 3-晚上
  slots?: number;
  bookedSlots?: number;
  status?: number;
  remark?: string;
  roomNumber?: string;
  maxQuota?: number;
}

// 获取医生排班列表
export const getScheduleList = (params: ScheduleListParams) =>
  defHttp.get<DoctorSchedule[]>({
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

// 获取可用诊室（随机分配）
export const getAvailableRoom = (params: { date: string; timeSlot: number; originalRoomNumber?: string }) =>
  defHttp.get<string>({
    url: '/admin/schedule/available-room',
    params,
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

// 自动生成排班接口
export interface GenerateSchedulesRequest {
  deptIds: number[];
  scheduleCount: number;
  timeSlots: number[];
  maxQuota: number;
  startDate: string;
}

export interface GeneratedScheduleItem {
  doctorId: number;
  doctorName: string;
  deptId: number;
  deptName: string;
  scheduleDate: string;
  timeSlot: number;
  maxQuota: number;
  roomNumber?: string;
}

export const generateSchedules = (data: GenerateSchedulesRequest) =>
  defHttp.post<GeneratedScheduleItem[]>({
    url: '/admin/schedule/generate',
    data,
  });

// 批量创建排班接口
export interface BatchCreateScheduleItem {
  doctorId: number;
  deptId: number;
  scheduleDate: string;
  timeSlot: number;
  maxQuota: number;
  roomNumber?: string;
}

export const batchCreateSchedules = (data: BatchCreateScheduleItem[]) =>
  defHttp.post<{
    success: boolean;
    message: string;
    successCount: number;
    failCount: number;
    errors?: string[];
  }>({
    url: '/admin/schedule/batch-create',
    data,
  });