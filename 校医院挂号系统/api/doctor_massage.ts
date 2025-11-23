import { http } from '../utils/request';
const PREFIX_APPLET = '/applet/doctor';
const PREFIX_DOCTOR = '/doctor';

const dApplet = {
  get: (path: string, params?: any, options?: any) => http.get(`${PREFIX_APPLET}${path}`, params, options),
  post: (path: string, data?: any, options?: any) => http.post(`${PREFIX_APPLET}${path}`, data, options),
  put: (path: string, data?: any, options?: any) => http.put(`${PREFIX_APPLET}${path}`, data, options),
  delete: (path: string, params?: any, options?: any) => http.delete(`${PREFIX_APPLET}${path}`, params, options),
};

const dDoctor = {
  get: (path: string, params?: any, options?: any) => http.get(`${PREFIX_DOCTOR}${path}`, params, options),
  post: (path: string, data?: any, options?: any) => http.post(`${PREFIX_DOCTOR}${path}`, data, options),
  put: (path: string, data?: any, options?: any) => http.put(`${PREFIX_DOCTOR}${path}`, data, options),
  delete: (path: string, params?: any, options?: any) => http.delete(`${PREFIX_DOCTOR}${path}`, params, options),
};

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
  scheduleId: number;
  newDate: string;
  newTimeRange: string;
  reason: string;
}

export interface PatientSummary {
  appointmentId: number;
  patientId: number;
  name: string;
  identity: string;
  appointmentTimeRange: string;
  statusText?: string;
  statusClass?: string;
}

export interface PatientDetail {
  name: string;
  identity: string;
  age: number;
  gender: string;
  history: string;
  visitedBefore: boolean;
  lastVisit?: {
    time: string;
    dept: string;
    doctor: string;
  }
}

export const doctorApi = {
  // 获取未来 N 天排班
  getSchedules: (doctorId: number, startDate: string, days: number) =>
    dDoctor.get('/schedules', { doctorId, startDate, days }),

  // 获取今日排班
  getTodaySchedule: (doctorId: number) =>
    dDoctor.get('/schedule/today', { doctorId }),

  // 申请调班
  applyShiftChange: (data: ShiftApply) =>
    dDoctor.post('/schedule/shift/apply', data),

  // 按日期获取患者列表
  getPatientsByDate: (doctorId: number, date: string) =>
    dDoctor.get('/patients/by-date', { doctorId, date }),

  // 患者详情
  getPatientDetail: (patientId: number) =>
    dDoctor.get(`/patient/${patientId}`),

  // 更新就诊状态（已接诊/已完成）
  updatePatientStatus: (appointmentId: number, status: 'RECEIVED' | 'DONE') =>
    dDoctor.post('/patient/status', { appointmentId, status }),
    
  // 获取医生详情
  getDoctorDetail: (doctorId: number | string) =>
    dApplet.get(`/${doctorId}`),

  getMyProfile: () =>
    dDoctor.get('/profile/me').then((res: any) => normalizeOne<any>(res)),

  getProfileByUserId: (userId: number) =>
    dDoctor.get('/profile/byUserId', { userId }).then((res: any) => normalizeOne<any>(res)),
};

// 获取医生详情
export const getDoctorDetail = async (doctorId: number | string) => {
  return dApplet.get(`/${doctorId}`);
};

// 获取所有医生列表
export const getAllDoctors = async () => {
  return dApplet.get('/list');
};

// 搜索医生
export const searchDoctors = async (keyword: string) => {
  return dApplet.get('/search', { keyword });
};

// 兼容旧代码导出的工具函数：按科室ID获取医生列表
export const getDoctorsByDeptId = async (deptId: number | string) => {
  return dApplet.get(`/by-dept/${deptId}`);
};