import { defHttp } from '/@/utils/http/axios';

enum Api {
  Pending = '/admin/shift-change/pending',
  Detail = '/admin/shift-change/detail',
  Approve = '/admin/shift-change/approve',
  Reject = '/admin/shift-change/reject',
}

export interface ShiftChangeRequest {
  adjustmentId: number;
  doctorId: number;
  doctorName: string;
  originalScheduleId: number;
  targetDate: string;
  targetTimeSlot: number; // 1=上午 2=下午 3=晚上
  targetDeptId?: number;
  reason?: string;
  applyTime?: string;
  status?: number; // 0=待审批 1=已通过 2=已驳回
}

export const getPendingShiftChanges = (params?: { keyword?: string; date?: string }) =>
  defHttp.get<ShiftChangeRequest[]>({ url: Api.Pending, params });

export const getShiftChangeDetail = (id: number) =>
  defHttp.get<ShiftChangeRequest>({ url: `${Api.Detail}/${id}` });

export const approveShiftChange = (data: {
  id: number;
  scheduleDate: string;
  timeSlot: number;
  deptId?: number;
}) => defHttp.post<boolean>({ url: Api.Approve, data });

export const rejectShiftChange = (data: { id: number; reason: string }) =>
  defHttp.post<boolean>({ url: Api.Reject, data });