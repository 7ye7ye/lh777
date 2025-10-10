"use strict";
const utils_request = require("../utils/request.js");
const PREFIX = "/user";
const u = {
  get: (path, params, options) => utils_request.http.get(`${PREFIX}${path}`, params, options),
  post: (path, data, options) => utils_request.http.post(`${PREFIX}${path}`, data, options),
  put: (path, data, options) => utils_request.http.put(`${PREFIX}${path}`, data, options),
  delete: (path, params, options) => utils_request.http.delete(`${PREFIX}${path}`, params, options)
};
const userApi = {
  // 测试：与后端 @PostMapping("/test") 保持一致
  test: () => u.get("/test"),
  // 登录
  login: (data) => u.post("/login", data),
  // 注册
  register: (data) => u.post("/register", data),
  // 获取当前用户信息
  getCurrentUser: () => u.get("/current"),
  // 退出登录
  logout: () => u.post("/logout"),
  // 就诊卡相关
  getCard: () => u.get("/card"),
  rechargeCard: (data) => u.post("/card/recharge", data),
  getCardHistory: () => u.get("/card/history"),
  // 就诊人相关
  getPatientList: () => u.get("/patients"),
  addPatient: (data) => u.post("/patients", data),
  updatePatient: (id, data) => u.put(`/patients/${id}`, data),
  deletePatient: (id) => u.delete(`/patients/${id}`),
  // 医生相关
  getDoctorList: () => u.get("/doctors"),
  getDoctorDetail: (id) => u.get(`/doctors/${id}`),
  // 记录相关
  getRegisterRecord: () => u.get("/records/register"),
  getOutpatientRecord: () => u.get("/records/outpatient"),
  getHospitalRecord: () => u.get("/records/hospital"),
  getConsultRecord: () => u.get("/records/consult"),
  getRevisitRecord: () => u.get("/records/revisit"),
  getCheckRecord: () => u.get("/records/check"),
  // 评价相关
  submitEvaluate: (data) => u.post("/evaluate", data),
  getEvaluateList: () => u.get("/evaluates"),
  // 反馈相关
  submitHelp: (data) => u.post("/help", data),
  submitComplain: (data) => u.post("/complain", data),
  // 账户解绑
  unbindAccount: (data) => u.post("/unbind", data)
};
exports.userApi = userApi;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/user.js.map
