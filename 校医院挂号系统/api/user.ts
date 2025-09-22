// src/api/userApi.ts
import { http } from '@/utils/request';

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
  test: () => http.get<any>('/jeecg-boot/test'),

  // 登录
  login: (data: LoginData) => http.post<UserInfo>('/jeecg-boot/user/login2', data),
  
  // 注册
  register: (data: RegisterData ) => 
    http.post<{ id: number }>('/jeecg-boot/user/register2', data),
  
  // 获取当前用户信息
  getCurrentUser: () => http.get<UserInfo>('/user/current'),
  
  // 退出登录
  logout: () => http.post<{ success: boolean }>('/user/logout'),

  // 就诊卡相关
  getCard: () => http.get<CardInfo>('/user/card'),
  rechargeCard: (data: { amount: number }) => http.post<{ success: boolean }>('/user/card/recharge', data),
  getCardHistory: () => http.get<RecordInfo[]>('/user/card/history'),

  // 就诊人相关
  getPatientList: () => http.get<PatientInfo[]>('/user/patients'),
  addPatient: (data: PatientInfo) => http.post<{ id: number }>('/user/patients', data),
  updatePatient: (id: number, data: Partial<PatientInfo>) => http.put<{ success: boolean }>(`/user/patients/${id}`, data),
  deletePatient: (id: number) => http.delete<{ success: boolean }>(`/user/patients/${id}`),

  // 医生相关
  getDoctorList: () => http.get<DoctorInfo[]>('/user/doctors'),
  getDoctorDetail: (id: number) => http.get<DoctorInfo>(`/user/doctors/${id}`),

  // 记录相关
  getRegisterRecord: () => http.get<RecordInfo[]>('/user/records/register'),
  getOutpatientRecord: () => http.get<RecordInfo[]>('/user/records/outpatient'),
  getHospitalRecord: () => http.get<RecordInfo[]>('/user/records/hospital'),
  getConsultRecord: () => http.get<RecordInfo[]>('/user/records/consult'),
  getRevisitRecord: () => http.get<RecordInfo[]>('/user/records/revisit'),
  getCheckRecord: () => http.get<RecordInfo[]>('/user/records/check'),

  // 评价相关
  submitEvaluate: (data: EvaluateData) => http.post<{ success: boolean }>('/user/evaluate', data),
  getEvaluateList: () => http.get<any[]>('/user/evaluates'),

  // 反馈相关
  submitHelp: (data: { content: string }) => http.post<{ success: boolean }>('/user/help', data),
  submitComplain: (data: { content: string }) => http.post<{ success: boolean }>('/user/complain', data),

  // 账户解绑
  unbindAccount: (data?: { reason: string }) => http.post<{ success: boolean }>('/user/unbind', data),
};