"use strict";
const utils_request = require("../utils/request.js");
const PREFIX = "/doctor";
const d = {
  get: (path, params, options) => utils_request.http.get(`${PREFIX}${path}`, params, options),
  post: (path, data, options) => utils_request.http.post(`${PREFIX}${path}`, data, options),
  put: (path, data, options) => utils_request.http.put(`${PREFIX}${path}`, data, options),
  delete: (path, params, options) => utils_request.http.delete(`${PREFIX}${path}`, params, options)
};
const doctorApi = {
  // 获取未来 N 天排班
  getSchedules: (doctorId, startDate, days) => d.get("/schedules", { doctorId, startDate, days }),
  // 获取今日排班
  getTodaySchedule: (doctorId) => d.get("/schedule/today", { doctorId }),
  // 申请调班（后端：/doctor/shift-change/apply）
  applyShiftChange: (data) => d.post("/shift-change/apply", data),
  // 查询我的调班申请（后端：/doctor/shift-change/list）
  listShiftChange: (doctorId, status) => d.get("/shift-change/list", { doctorId, status }),
  // 按日期获取患者列表
  getPatientsByDate: (doctorId, date) => d.get("/patients/by-date", { doctorId, date }),
  // 患者详情
  getPatientDetail: (patientId) => d.get(`/patient/${patientId}`),
  // 更新就诊状态（已接诊/已完成）
  updatePatientStatus: (appointmentId, status) => d.post("/patient/status", { appointmentId, status }),
  // 按 userId 查询医生资料（依赖后端 /doctor/profile/byUserId）
  getProfileByUserId: (userId) => d.get("/profile/byUserId", { userId }),
  // 会话接口（如保留可用作备选）
  getMyProfile: () => d.get("/profile/me")
};
exports.doctorApi = doctorApi;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/doctor.js.map
