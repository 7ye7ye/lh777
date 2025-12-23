import { defHttp } from '/@/utils/http/axios';

// 医生类型定义
export type Doctor = {
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
  userAccount: string;
  email: string;
  createTime?: string;
  updateTime?: string;
};

// 创建/更新医生参数接口
export interface DoctorForm {
  doctorId?: number;
  doctorName: string;
  deptId: number;
  title: string;
  specialty: string;
  doctorDesc?: string;
  avatar?: string;
}

// 注册医生账号参数接口
export interface RegisterDoctorParams {
  doctorName: string;
  userAccount: string;
  userPassword: string; // 与后端API保持一致
  deptId: number;
  title: string;
  specialty: string;
  isActive?: boolean; // 修改为boolean类型以匹配DoctorRegister.vue中的使用方式
  email?: string;
  doctorDesc?: string;
  avatar?: string;
}

// 医生相关API接口
export const getDoctorProfile = (doctorId: number) =>
  defHttp.get<Doctor>({ url: `${Api.DoctorDetail}/profile/${doctorId}` });

// 提交医生资料修改申请
export const applyDoctorProfileUpdate = (params: {
  id: number;
  avatar?: string;
  specialty: string;
  doctorDesc?: string;
}) =>
  defHttp.post<boolean>({ url: '/doctor/profile/update-request', data: params });

export const updateDoctorProfile = (doctor: Partial<Doctor>) =>
  defHttp.put<boolean>({ url: `${Api.DoctorDetail}/profile`, data: doctor });

export const getMyDoctorProfile = () =>
  defHttp.get<Doctor>({ url: `${Api.DoctorDetail}/my-profile` });

export const getDoctorByAccount = (account: string) =>
  defHttp.get<Doctor | null>({ url: `${Api.DoctorDetail}/by-account/${account}` });

export const getDoctorByUserId = (userId: number) =>
  defHttp.get<Doctor | null>({ url: `${Api.DoctorDetail}/by-user/${userId}` });

// 注册医生账号函数
export const registerDoctorAccount = (params: RegisterDoctorParams) =>
  defHttp.post<{ success: boolean; message: string; data: { userId: number; doctorId: number } }>({
    url: '/admin/doctor/register',
    data: params,
  });

// 医生资料修改申请相关接口（管理员端）
export const getDoctorProfileUpdateRequests = (params: {
  pageNo?: number;
  pageSize?: number;
  status?: number;
}) =>
  defHttp.get<any>({
    url: '/doctor/profile/update-request/list',
    params,
  });

export const approveDoctorProfileUpdate = (params: { requestId: number; reason?: string }) =>
  defHttp.post<boolean>({
    url: '/doctor/profile/update-request/approve',
    data: params,
  });

export const rejectDoctorProfileUpdate = (params: { requestId: number; reason?: string }) =>
  defHttp.post<boolean>({
    url: '/doctor/profile/update-request/reject',
    data: params,
  });

// 创建医生
export const createDoctor = (params: DoctorForm) =>
  // 根据后端API实现，正确的添加路径是/add
  defHttp.post<boolean>({ url: `${Api.DoctorDetail}/add`, data: params });

// 更新医生
export const updateDoctor = (params: DoctorForm) => {
  if (!params.doctorId) {
    throw new Error('doctorId is required for update');
  }
  // 修改为不将doctorId添加到URL路径中，而是包含在请求体中
  return defHttp.put<boolean>({ url: Api.DoctorUpdate, data: params });
};

// 删除医生
export const deleteDoctor = (doctorId: number) =>
  defHttp.delete<boolean>({ url: `${Api.DoctorDelete}/${doctorId}` });

// 批量删除医生
export const batchDeleteDoctors = (doctorIds: number[]) =>
  defHttp.delete<boolean>({ url: Api.BatchDeleteDoctors, data: doctorIds });

// 获取医生详情
export const getDoctorDetail = (doctorId: number) =>
  defHttp.get<Doctor>({ url: `${Api.DoctorDetail}/detail/${doctorId}` });

// 按科室获取医生列表
export const getDoctorsByDepartment = (departmentId: number) =>
  defHttp.get<Doctor[]>({
    url: `${Api.DoctorDetail}/list/by-department`,
    params: { departmentId },
  });


// 定义医生相关API路径
enum Api {
  DoctorList = '/admin/doctor/list',
  DoctorDetail = '/admin/doctor',
  DoctorCreate = '/admin/doctor/create',
  DoctorUpdate = '/admin/doctor/update',
  DoctorDelete = '/admin/doctor/delete',
  BatchDeleteDoctors = '/admin/doctor/batch-delete',
}

// 获取医生列表函数
export const getDoctorList = (params?: {
  doctorName?: string;
  deptId?: number | undefined;
  title?: string | undefined;
  isActive?: number | undefined;
  pageNum?: number;
  pageSize?: number;
}) => {
  return defHttp.get<any>({
    url: Api.DoctorList,
    params
  });
};
