import { defHttp } from '/@/utils/http/axios';

export interface LeaveRecord {
  leaveId: number;
  doctorId: number;
  doctorName: string;
  deptId: number;
  deptName: string;
  leaveType: string;
  startDate: string;
  endDate: string;
  reason: string;
  applyTime: string;
  status: number; // 1-待审批,2-已通过,3-已驳回,4-已撤销
  adminId?: number;
  approveTime?: string;
  rejectReason?: string;
}

export interface LeaveApprovalRequest {
  leaveId: number;
  status: number; // 2-通过, 3-驳回
  rejectReason?: string;
}

export interface LeaveListParams {
  status?: number;
  doctorName?: string;
  deptId?: number;
  startDate?: string;
  endDate?: string;
  current?: number;
  size?: number;
}

// 获取请假申请列表
export const getLeaveList = (params: LeaveListParams) =>
  defHttp.get<{
    records: LeaveRecord[];
    total: number;
    current: number;
    size: number;
  }>({
    url: '/admin/leave/list',
    params,
  });

// 审批请假申请
export const approveLeave = (data: LeaveApprovalRequest) =>
  defHttp.post<boolean>({
    url: '/admin/leave/approve',
    data,
  });

// 获取请假申请详情
export const getLeaveDetail = (leaveId: number) =>
  defHttp.get<LeaveRecord>({
    url: `/admin/leave/${leaveId}`,
  });

