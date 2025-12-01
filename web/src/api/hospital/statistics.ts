import { defHttp } from '/@/utils/http/axios';

enum Api {
  OutpatientStatistics = '/admin/statistics/outpatient',
  DepartmentLoad = '/admin/statistics/department-load',
  CancelRate = '/admin/statistics/cancel-rate',
  RegistrationStatistics = '/admin/statistics/registration',
  ReferralStatistics = '/admin/statistics/referral',
  StatisticsSummary = '/admin/statistics/summary',
  ExportReport = '/admin/statistics/export',
}

export interface StatisticsQuery {
  periodType?: 'day' | 'week' | 'month';
  startDate?: string;
  endDate?: string;
  deptId?: number;
  doctorId?: number;
  typeId?: number;
  compareHistory?: boolean;
  comparePeriods?: number;
}

export interface OutpatientStatisticsItem {
  date: string;
  deptId: number;
  deptName: string;
  visitCount: number;
  totalVisitCount: number;
  compareVisitCount?: number;
  growthRate?: number;
}

export interface DepartmentLoadItem {
  deptId: number;
  deptName: string;
  doctorId?: number;
  doctorName?: string;
  visitDurationHours: number;
  totalQuota?: number;
  usedQuota?: number;
  quotaUsageRate: number;
}

export interface CancelRateItem {
  deptId?: number;
  deptName?: string;
  doctorId?: number;
  doctorName?: string;
  typeId?: number;
  typeName?: string;
  totalCount: number;
  cancelCount: number;
  cancelRate: number;
}

export interface RegistrationStatisticsItem {
  date: string;
  totalRegistration: number;
  typeId?: number;
  typeName?: string;
  typeRegistration?: number;
  compareRegistration?: number;
  growthRate?: number;
}

export interface ReferralStatisticsItem {
  date: string;
  deptId: number;
  deptName: string;
  targetType: string;
  targetTypeName: string;
  applicationCount: number;
  approvedCount: number;
  rejectedCount: number;
  cancelledCount: number;
  completedCount: number;
  totalCount: number;
  approvalRate: number;
  completionRate: number;
}

/**
 * 获取门诊量统计
 */
export const getOutpatientStatistics = (params: StatisticsQuery) =>
  defHttp.get<OutpatientStatisticsItem[]>({ url: Api.OutpatientStatistics, params });

/**
 * 获取科室负荷统计
 */
export const getDepartmentLoadStatistics = (params: StatisticsQuery) =>
  defHttp.get<DepartmentLoadItem[]>({ url: Api.DepartmentLoad, params });

/**
 * 获取退号率统计
 */
export const getCancelRateStatistics = (params: StatisticsQuery) =>
  defHttp.get<CancelRateItem[]>({ url: Api.CancelRate, params });

/**
 * 获取挂号量统计
 */
export const getRegistrationStatistics = (params: StatisticsQuery) =>
  defHttp.get<RegistrationStatisticsItem[]>({ url: Api.RegistrationStatistics, params });

/**
 * 获取转诊情况统计
 */
export const getReferralStatistics = (params: StatisticsQuery) =>
  defHttp.get<ReferralStatisticsItem[]>({ url: Api.ReferralStatistics, params });

/**
 * 获取统计数据汇总
 */
export const getStatisticsSummary = (params: StatisticsQuery) =>
  defHttp.get<Record<string, any>>({ url: Api.StatisticsSummary, params });

/**
 * 导出报表
 */
export const exportReport = (params: { type: string; query: StatisticsQuery }) =>
  defHttp.get<Blob>(
    {
      url: Api.ExportReport,
      params: { ...params.query, reportType: params.type },
      responseType: 'blob',
    },
    { isReturnNativeResponse: true }
  );


