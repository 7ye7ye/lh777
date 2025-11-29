import { defHttp } from '/@/utils/http/axios';

// 管理端获取患者身份认证申请列表（后端直接返回数组）
export const getPatientIdentityApprovals = (params?: { status?: number }) =>
  defHttp.post<any>({
    url: '/patient/identity/adminList',
    params,
  });

// 管理员审核患者身份认证
export function approvePatientIdentity(data: { patientId: number; approve: boolean; rejectReason?: string }) {
  return defHttp.post<any>({
    url: '/patient/identity/approve',
    params: data,
  });
}
