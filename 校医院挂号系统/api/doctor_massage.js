import { http } from '../utils/request';

const PREFIX_APPLET = '/applet/doctor';
const PREFIX_DOCTOR = '/doctor';

const dApplet = {
  get: (path, params, options) => http.get(`${PREFIX_APPLET}${path}`, params, options),
  post: (path, data, options) => http.post(`${PREFIX_APPLET}${path}`, data, options),
  put: (path, data, options) => http.put(`${PREFIX_APPLET}${path}`, data, options),
  delete: (path, params, options) => http.delete(`${PREFIX_APPLET}${path}`, params, options),
};

const dDoctor = {
  get: (path, params, options) => http.get(`${PREFIX_DOCTOR}${path}`, params, options),
  post: (path, data, options) => http.post(`${PREFIX_DOCTOR}${path}`, data, options),
  put: (path, data, options) => http.put(`${PREFIX_DOCTOR}${path}`, data, options),
  delete: (path, params, options) => http.delete(`${PREFIX_DOCTOR}${path}`, params, options),
};

export const doctorApi = {
  // 获取未来 N 天排班
  getSchedules: (doctorId, startDate, days) =>
    dDoctor.get('/schedules', { doctorId, startDate, days }),

  // 获取今日排班
  getTodaySchedule: (doctorId) =>
    dDoctor.get('/schedule/today', { doctorId }),

  // 申请调班
  applyShiftChange: (data) =>
    dDoctor.post('/schedule/shift/apply', data),

  // 按日期获取患者列表
  getPatientsByDate: (doctorId, date) =>
    dDoctor.get('/patients/by-date', { doctorId, date }),

  // 患者详情
  getPatientDetail: (patientId) =>
    dDoctor.get(`/patient/${patientId}`),

  // 更新就诊状态（已接诊/已完成）
  updatePatientStatus: (appointmentId, status) =>
    dDoctor.post('/patient/status', { appointmentId, status }),
    
  // 获取医生详情
  getDoctorDetail: (doctorId) =>
    dApplet.get(`/${doctorId}`),
};

// 获取医生详情
export const getDoctorDetail = async (doctorId) => {
  return dApplet.get(`/${doctorId}`);
};

// 获取所有医生列表
export const getAllDoctors = async () => {
  return dApplet.get('/list');
};

// 搜索医生
export const searchDoctors = async (keyword) => {
  return dApplet.get('/search', { keyword });
};

// 兼容旧代码导出的工具函数：按科室ID获取医生列表
export const getDoctorsByDeptId = async (deptId) => {
  return dApplet.get(`/by-dept/${deptId}`);
};

