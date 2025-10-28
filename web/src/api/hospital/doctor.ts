import { defHttp } from '/@/utils/http/axios';

enum Api {
  DoctorList = '/admin/doctor/list',
  DoctorDetail = '/admin/doctor',
  DoctorProfile = '/doctor/profile',
}

export interface Doctor {
  doctorId: number;
  doctorName: string;
  userId: number;
  deptId: number;
  deptName?: string;
  title: string;
  specialty: string;
  doctorDesc: string;
  avatar: string;
  isActive: number;
  updateVerify: number;
  // 追加：HosUser字段
  userAccount?: string;
  email?: string;
}

export function getMyDoctorProfile() {
  return defHttp.get<Doctor>({ url: `${Api.DoctorProfile}/me` });
}

export function getDoctorProfile(params: { doctorId: number }) {
  return defHttp.get<Doctor>({ url: Api.DoctorProfile, params });
}

export function updateDoctorProfile(data: Partial<Doctor> & { doctorId: number }) {
  // 后端允许同时更新 doctor + hos_user 的字段
  return defHttp.put<boolean>({ url: Api.DoctorProfile, data });
}

export interface DoctorListParams {
  current?: number;
  size?: number;
  doctorName?: string;
  deptId?: number;
  isActive?: number;
}

/**
 * 获取医生列表
 */
export const getDoctorList = (params?: DoctorListParams) =>
  defHttp.get<Doctor[]>({ url: Api.DoctorList, params });

/**
 * 获取医生详情
 */
export const getDoctorDetail = (doctorId: number) =>
  defHttp.get<Doctor>({ url: `${Api.DoctorDetail}/${doctorId}` });

// 新增：按 userId 查询医生
export function getDoctorByUserId(userId: number) {
  return defHttp.get<Doctor>({ url: `${Api.DoctorProfile}/byUserId`, params: { userId } });
}

export function getDoctorByAccount(account: string) {
  return defHttp.get<Doctor>({ url: `${Api.DoctorProfile}/byAccount`, params: { account } });
}