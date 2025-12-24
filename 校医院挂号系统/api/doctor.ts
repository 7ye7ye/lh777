import { http } from '../utils/request';
const PREFIX = '/doctor';

const d = {
    get: (path: string, params?: any, options?: any) => http.get(`${PREFIX}${path}`, params, options),
    post: (path: string, data?: any, options?: any) => http.post(`${PREFIX}${path}`, data, options),
    put: (path: string, data?: any, options?: any) => http.put(`${PREFIX}${path}`, data, options),
    delete: (path: string, params?: any, options?: any) => http.delete(`${PREFIX}${path}`, params, options),
};

// 方法：规范化服务端返回，空数组 -> 空对象，数组 -> 首元素
function normalizeOne<T>(data: T | T[]): T {
    if (Array.isArray(data)) {
        return (data[0] ?? ({} as T)) as T;
    }
    return data as T;
}

export interface Schedule {
    id: number;
    date: string;
    timeRange: string;
    roomNo: string;
    totalSlots: number;
    bookedCount: number;
}

export interface ShiftApply {
    doctorId: number
    originalScheduleId: number
    targetDate: string
    targetTimeSlot: number
    targetDeptId?: number
    reason: string
}

// 添加医生信息接口
export interface DoctorInfo {
    id: number;
    name: string;
    title: string;
    departmentId: number;
    departmentName?: string;
    avatar?: string;
    description?: string;
    specialty?: string;
}

// 获取所有医生列表
export const getAllDoctors = (params?: { page?: number; size?: number }) =>
    d.get('/list', params);

// 搜索医生
export const searchDoctors = (params: { keyword: string; departmentId?: number }) =>
    d.get('/search', params);

// 根据科室ID获取医生列表
export const getDoctorsByDeptId = (deptId: string | number) =>
    d.get('/list/by-department', { departmentId: deptId });

// 获取医生详情
export const getDoctorDetail = (doctorId: string | number) =>
    d.get(`/detail/${doctorId}`);

export const doctorApi = {
    // 获取所有医生列表
    getAllDoctors: (params?: { page?: number; size?: number }) =>
        d.get('/list', params),

    // 搜索医生
    searchDoctors: (params: { keyword: string; departmentId?: number }) =>
        d.get('/search', params),

    // 获取医生详情
    getDoctorDetail: (doctorId: string | number) =>
        d.get(`/detail/${doctorId}`),

    // 获取未来 N 天排班
    getSchedules: (doctorId: number, startDate: string, days: number) =>
        d.get('/schedules', { doctorId, startDate, days }),

    // 获取今日排班
    getTodaySchedule: (doctorId: number) =>
        d.get('/schedule/today', { doctorId }),

    // 申请调班（后端：/doctor/shift-change/apply）
    applyShiftChange: (data: ShiftApply) =>
        d.post('/shift-change/apply', data),

    // 查询我的调班申请（后端：/doctor/shift-change/list）
    listShiftChange: (doctorId: number, status?: number) =>
        d.get('/shift-change/list', { doctorId, status }),

    // 按日期获取患者列表
    getPatientsByDate: (doctorId: number, date: string) =>
        d.get('/patients/by-date', { doctorId, date }),

    // 基础患者列表（直接查 patient 表）
    getPatientsBasic: (params?: { keyword?: string; startDate?: string; endDate?: string }) =>
        d.get('/patient/list-basic', params),

    // 患者详情
    getPatientDetail: (patientId: number) => d.get(`/patient/${patientId}`),

    // 预约详情（根据 record_id）使用新前缀 /appointment/detail 避免与 /{patientId} 冲突
    getAppointmentDetail: (appointmentId: number) => d.get('/patient/appointment/detail', { appointmentId }),

    // 更新就诊状态（开始接诊/完成接诊）, 支持 record_id 或 registration_no 二选一
    updatePatientStatus: (payload: {
        appointmentId?: number;
        registrationNo?: string;
        action: 'start' | 'finish' | 'referral' | '开始接诊' | '完成接诊' | '转诊' | '开始' | '完成';
    }) => d.post('/patient/status', payload),

    // 按 userId 查询医生资料（依赖后端 /doctor/profile/byUserId）
    getProfileByUserId: (userId: number) =>
        d.get('/profile/byUserId', { userId }).then((res) => normalizeOne<any>(res)),

    // 会话接口（依赖后端 /doctor/profile/me）
    getMyProfile: () =>
        d.get('/profile/me').then((res) => normalizeOne<any>(res)),

    // 更新医生个人资料（依赖后端 /doctor/profile/update）
    updateProfile: (data: any) =>
        d.put('/profile/update', data),

    // 医生资料变更申请（依赖后端 /doctor/profile/update-request）
    applyProfileUpdate: (data: any) =>
        d.post('/profile/update-request', data),

    // 医生端：查看本人资料变更申请记录（依赖后端 /doctor/profile/update-request/my）
    listProfileUpdateRequests: (params: { doctorId: number; status?: number; pageNo?: number; pageSize?: number }) =>
        d.get('/profile/update-request/my', params),

    // 根据科室ID获取医生列表
    getDoctorsByDeptId: (deptId: string | number) =>
        d.get('/list/by-department', { departmentId: deptId }),
};