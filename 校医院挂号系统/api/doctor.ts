import { http } from '../utils/request';

const PREFIX = '/applet/doctor';

const d = {
  get: (path: string, params?: any, options?: any) => http.get(`${PREFIX}${path}`, params, options),
  post: (path: string, data?: any, options?: any) => http.post(`${PREFIX}${path}`, data, options),
  put: (path: string, data?: any, options?: any) => http.put(`${PREFIX}${path}`, data, options),
  delete: (path: string, params?: any, options?: any) => http.delete(`${PREFIX}${path}`, params, options),
};

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
    d.get('/schedules', { doctorId, startDate, days }),

  // 获取今日排班
  getTodaySchedule: (doctorId: number) =>
    d.get('/schedule/today', { doctorId }),

  // 申请调班
  applyShiftChange: (data: ShiftApply) =>
    d.post('/schedule/shift/apply', data),

  // 按日期获取患者列表
  getPatientsByDate: (doctorId: number, date: string) =>
    d.get('/patients/by-date', { doctorId, date }),

  // 患者详情
  getPatientDetail: (patientId: number) =>
    d.get(`/patient/${patientId}`),

  // 更新就诊状态（已接诊/已完成）
  updatePatientStatus: (appointmentId: number, status: 'RECEIVED' | 'DONE') =>
    d.post('/patient/status', { appointmentId, status }),
};

// 获取医生详情
export const getDoctorDetail = async (doctorId: number | string) => {
  return d.get(`/${doctorId}`);
};

// 获取所有医生列表
export const getAllDoctors = async () => {
  return d.get('/list');
};

// 搜索医生
export const searchDoctors = async (keyword: string) => {
  return d.get('/search', { keyword });
};

// 兼容旧代码导出的工具函数：按科室ID获取医生列表
export const getDoctorsByDeptId = async (deptId: number | string) => {
  return d.get(`/by-dept/${deptId}`);
};