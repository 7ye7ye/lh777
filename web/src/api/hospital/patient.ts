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

// 获取患者身份认证驳回原因
export function getPatientIdentityRejectReason(patientId: number) {
  return defHttp.get<any>({
    url: `/patient/identity/rejectReason/${patientId}`,
  });
}

// 获取患者身份认证审核历史
export function getPatientIdentityAuditHistory(patientId: number) {
  return defHttp.get<any>({
    url: `/patient/identity/auditHistory/${patientId}`,
  });
}
