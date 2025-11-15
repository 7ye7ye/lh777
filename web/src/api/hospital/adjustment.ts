import { defHttp } from '/@/utils/http/axios';

export interface AdjustmentRecord {
  adjustmentId: number;
  doctorId: number;
  doctorName?: string;
  deptName?: string;
  originalScheduleId: number;
  originalDate?: string;
  originalTimeSlot?: number;
  targetDate: string;
  targetTimeSlot: number;
  targetDeptId: number;
  reason: string;
  applyTime: string;
  status: number; // 1-待审批,2-已通过,3-已驳回,4-已撤销
  adminId?: number;
  approveTime?: string;
  rejectReason?: string;
  newScheduleId?: number;
}

export interface AdjustmentApprovalRequest {
  adjustmentId: number;
  status: number; // 2-通过, 3-驳回
  rejectReason?: string;
}

export interface AdjustmentListParams {
  status?: number;
  doctorName?: string;
  deptId?: number;
  startDate?: string;
  endDate?: string;
  current?: number;
  size?: number;
}

// 获取调班申请列表
export const getAdjustmentList = (params: AdjustmentListParams) =>
  defHttp.get<{
    records: AdjustmentRecord[];
    total: number;
    current: number;
    size: number;
  }>({
    url: '/admin/adjustment/list',
    params,
  });

// 审批调班申请
export const approveAdjustment = (data: AdjustmentApprovalRequest) =>
  defHttp.post<boolean>({
    url: '/admin/adjustment/approve',
    data,
  });

// 获取调班申请详情
export const getAdjustmentDetail = (adjustmentId: number) =>
  defHttp.get<AdjustmentRecord>({
    url: `/admin/adjustment/${adjustmentId}`,
  });

// 医生提交调班申请
export interface DoctorShiftChangeApplyDTO {
  originalScheduleId: number;
  targetDate: string;
  targetTimeSlot: number;
  targetDeptId: number;
  reason: string;
}

export const applyShiftChange = (data: DoctorShiftChangeApplyDTO) =>
  defHttp.post<boolean>({
    url: '/doctor/shift-change/apply',
    data,
  });