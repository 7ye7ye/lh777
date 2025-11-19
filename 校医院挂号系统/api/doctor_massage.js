import { http } from '../utils/request';

const PREFIX = '/applet/doctor';

const d = {
  get: (path, params, options) => http.get(`${PREFIX}${path}`, params, options),
  post: (path, data, options) => http.post(`${PREFIX}${path}`, data, options),
  put: (path, data, options) => http.put(`${PREFIX}${path}`, data, options),
  delete: (path, params, options) => http.delete(`${PREFIX}${path}`, params, options),
};

export const doctorApi = {
  // 获取未来 N 天排班
  getSchedules: (doctorId, startDate, days) =>
    d.get('/schedules', { doctorId, startDate, days }),

  // 获取今日排班
  getTodaySchedule: (doctorId) =>
    d.get('/schedule/today', { doctorId }),

  // 申请调班
  applyShiftChange: (data) =>
    d.post('/schedule/shift/apply', data),

  // 按日期获取患者列表
  getPatientsByDate: (doctorId, date) =>
    d.get('/patients/by-date', { doctorId, date }),

  // 患者详情
  getPatientDetail: (patientId) =>
    d.get(`/patient/${patientId}`),

  // 更新就诊状态（已接诊/已完成）
  updatePatientStatus: (appointmentId, status) =>
    d.post('/patient/status', { appointmentId, status }),
    
  // 获取医生详情
  getDoctorDetail: (doctorId) =>
    d.get(`/${doctorId}`),
};

// 获取医生详情
export const getDoctorDetail = async (doctorId) => {
  return d.get(`/${doctorId}`);
};

// 获取所有医生列表
export const getAllDoctors = async () => {
  return d.get('/list');
};

// 搜索医生
export const searchDoctors = async (keyword) => {
  return d.get('/search', { keyword });
};

// 兼容旧代码导出的工具函数：按科室ID获取医生列表
export const getDoctorsByDeptId = async (deptId) => {
  return d.get(`/by-dept/${deptId}`);
};

