// src/api/userApi.ts
import { http } from '../utils/request';
const PREFIX = '/user';

// 统一带 /user 前缀的便捷方法，避免每个接口手写 PREFIX
const u = {
  get: (path: string, params?: any, options?: any) => http.get(`${PREFIX}${path}`, params, options),
  post: (path: string, data?: any, options?: any) => http.post(`${PREFIX}${path}`, data, options),
  put: (path: string, data?: any, options?: any) => http.put(`${PREFIX}${path}`, data, options),
  delete: (path: string, params?: any, options?: any) => http.delete(`${PREFIX}${path}`, params, options),
};

// 定义类型（保持 TypeScript 类型安全）
interface LoginData {
  userAccount: string;
  userPassword: string;
}

interface RegisterData {
  userAccount: string;
  userPassword: string;
  checkPassword: string;
}
interface UserInfo {
  id: number;
  username: string;
  name: string;
  phone: string;
  role: string;
  bindTime?: string;
}

interface CardInfo {
  name: string;
  cardNumber: string;
  balance: string;
  status: string;
}

interface PatientInfo {
  id: number;
  name: string;
  gender: string;
  age: number;
  idType: string;
  idNumber: string;
  phone: string;
  birthday: string;
}

interface DoctorInfo {
  id: number;
  name: string;
  dept: string;
  avatar: string;
  title?: string;
  introduction?: string;
}

interface RecordInfo {
  id: number;
  dept?: string;
  doctor?: string;
  time: string;
  project?: string;
  amount?: string;
  status?: string;
}

interface EvaluateData {
  type: string;
  patientId: number;
  doctorRating: {
    attitude: number;
    professional: number;
    communication: number;
  };
  hospitalRating: {
    environment: number;
    waiting: number;
  };
  text: string;
}

// 统一响应类型（后端返回的外层结构）
interface ApiResponse<T = any> {
    code: number;    // 后端状态码（如 200 成功）
    message: string; // 提示信息
    data: T;         // 实际响应数据（这里 T 指定为 number）
}

// 用户相关 API
export const userApi = {
  // 测试：与后端 @PostMapping("/test") 保持一致
  test: () => u.get('/test'),

  // 登录
  login: (data: LoginData) => u.post('/login2', data, { skipAuth: true }),
  
  // 注册
  register: (data: RegisterData ) => 
    u.post('/register2', data, { skipAuth: true }),
  
  // 获取当前用户信息
  getCurrentUser: () => u.get('/current'),
  
  // 退出登录
  logout: () => u.post('/logout'),

  // 就诊卡相关
  getCard: () => u.get('/card'),
  rechargeCard: (data: { amount: number }) => u.post('/card/recharge', data),
  getCardHistory: () => u.get('/card/history'),

  // 就诊人相关
  getPatientList: () => u.get('/patients'),
  addPatient: (data: PatientInfo) => u.post('/patients', data),
  updatePatient: (id: number, data: Partial<PatientInfo>) => u.put(`/patients/${id}`, data),
  deletePatient: (id: number) => u.delete(`/patients/${id}`),

  // 医生相关
  getDoctorList: () => u.get('/doctors'),
  getDoctorDetail: (id: number) => u.get(`/doctors/${id}`),

  // 记录相关
  getRegisterRecord: () => u.get('/records/register'),
  getOutpatientRecord: () => u.get('/records/outpatient'),
  getHospitalRecord: () => u.get('/records/hospital'),
  getConsultRecord: () => u.get('/records/consult'),
  getRevisitRecord: () => u.get('/records/revisit'),
  getCheckRecord: () => u.get('/records/check'),

  // 评价相关
  submitEvaluate: (data: EvaluateData) => u.post('/evaluate', data),
  getEvaluateList: () => u.get('/evaluates'),

  // 反馈相关
  submitHelp: (data: { content: string }) => u.post('/help', data),
  submitComplain: (data: { content: string }) => u.post('/complain', data),

  // 账户解绑
  unbindAccount: (data?: { reason: string }) => u.post('/unbind', data),
};