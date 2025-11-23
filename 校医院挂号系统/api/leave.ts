import { http } from '../utils/request';

const PREFIX = '/doctor/leave';

const d = {
    get: (path: string, params?: any, options?: any) => http.get(`${PREFIX}${path}`, params, options),
    post: (path: string, data?: any, options?: any) => http.post(`${PREFIX}${path}`, data, options),
    put: (path: string, data?: any, options?: any) => http.put(`${PREFIX}${path}`, data, options),
    delete: (path: string, params?: any, options?: any) => http.delete(`${PREFIX}${path}`, params, options),
};

// 请假申请接口
export interface LeaveApplyRequest {
    doctorId: number;
    doctorName: string;
    deptId: number;
    deptName: string;
    leaveType: string;
    startDate: string;
    endDate: string;
    reason: string;
}

export const leaveApi = {
    // 提交请假申请
    applyLeave: (data: LeaveApplyRequest) =>
        d.post('/apply', data),

    // 查询我的请假申请列表
    getMyLeaveList: (doctorId: number, status?: number) =>
        d.get('/list', { doctorId, status }),

    // 撤销请假申请
    cancelLeave: (leaveId: number) =>
        d.post('/cancel', { leaveId }),
};

