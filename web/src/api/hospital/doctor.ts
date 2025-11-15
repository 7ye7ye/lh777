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

// 方法：规范化服务端返回，空数组→空对象，数组→首元素
function normalizeOne<T>(data: T | T[]): T {
  if (Array.isArray(data)) {
    return (data[0] ?? ({} as T)) as T;
  }
  return data as T;
}

export async function getMyDoctorProfile() {
  // 原实现：直接返回 result
  // return defHttp.get<Doctor>({ url: `${Api.DoctorProfile}/me` });
  const res = await defHttp.get<Doctor | Doctor[]>({ url: `${Api.DoctorProfile}/me` });
  return normalizeOne<Doctor>(res);
}

export async function getDoctorProfile(params: { doctorId: number }) {
  // 原实现：直接返回 result
  // return defHttp.get<Doctor>({ url: Api.DoctorProfile, params });
  const res = await defHttp.get<Doctor | Doctor[]>({ url: Api.DoctorProfile, params });
  return normalizeOne<Doctor>(res);
}

// 新增：按 userId 查询医生
export async function getDoctorByUserId(userId: number) {
  // 原实现：直接返回 result
  // return defHttp.get<Doctor>({ url: `${Api.DoctorProfile}/byUserId`, params: { userId } });
  const res = await defHttp.get<Doctor | Doctor[]>({ url: `${Api.DoctorProfile}/byUserId`, params: { userId } });
  return normalizeOne<Doctor>(res);
}

export async function getDoctorByAccount(account: string) {
  // 原实现：直接返回 result
  // return defHttp.get<Doctor>({ url: `${Api.DoctorProfile}/byAccount`, params: { account } });
  const res = await defHttp.get<Doctor | Doctor[]>({ url: `${Api.DoctorProfile}/byAccount`, params: { account } });
  return normalizeOne<Doctor>(res);
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
export const getDoctorList = (params?: DoctorListParams) => {
  if (params?.deptId) {
    return defHttp.get<Doctor[]>({ url: `/applet/doctor/by-dept/${params.deptId}` });
  }
  return defHttp.get<Doctor[]>({ url: `/applet/doctor/list` });
}

/**
 * 获取医生详情
 */
export const getDoctorDetail = (doctorId: number) =>
  defHttp.get<Doctor>({ url: `${Api.DoctorDetail}/${doctorId}` });