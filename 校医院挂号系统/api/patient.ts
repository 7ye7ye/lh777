import { http } from '../utils/request';
const PREFIX = '/patient';

// 统一带 /patient 前缀的便捷方法，避免每个接口手写 PREFIX
const u = {
  get: (path: string, params?: any, options?: any) => http.get(`${PREFIX}${path}`, params, options),
  post: (path: string, data?: any, options?: any) => http.post(`${PREFIX}${path}`, data, options),
  put: (path: string, data?: any, options?: any) => http.put(`${PREFIX}${path}`, data, options),
  delete: (path: string, params?: any, options?: any) => http.delete(`${PREFIX}${path}`, params, options),
};

interface CardInfo {
    name: string;
    cardNumber: string;
    balance: string;
    status: string;
}

// 创建就诊卡请求参数
interface CreateCardRequest {
    userId: number;
    patientName: string;
    idType: string;
    idCard: string;
    gender: string;
    birthDate: string;
    nation: string;
    nationality: string;
    region: string;
    detailedAddress: string;
    phone: string;
    phoneNumber?: string;
    patientType: number; // 1-学生；2-教师；3-职工
    studentId?: string;
    staffId?: string;
    emergencyContact?: string;
    emergencyPhone?: string;
}


export const patientApi = {

// 就诊卡相关
getCard: (data: { userId: number }) => u.post('/cardInfo', data),
createCard: (data: CreateCardRequest) => u.post('/create', data),
rechargeCard: (data: { amount: number }) => u.post('/card/recharge', data),
getCardHistory: () => u.post('/card/history'),

// 就诊人相关
getPatientList: () => u.get('/patients'),
// addPatient: (data: PatientInfo) => u.post('/patients', data),
// updatePatient: (id: number, data: Partial<PatientInfo>) => u.put(`/patients/${id}`, data),
deletePatient: (id: number) => u.delete(`/patients/${id}`)

}