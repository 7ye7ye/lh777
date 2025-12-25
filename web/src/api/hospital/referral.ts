import { defHttp } from '/@/utils/http/axios';

enum Api {
  ReferralPage = '/admin/referral/page',
  ReferralDetail = '/admin/referral',
  ReferralReview = '/admin/referral/review',
  ReferralOptions = '/admin/referral/options',
  ReferralApplication = '/admin/referral/application',
  PatientReferralList = '/patient/referral/list',
  PatientReferralDetail = '/patient/referral',
  PatientReferralCancel = '/patient/referral/cancel',
}

export const fetchReferralPage = (params: Recordable) =>
  defHttp.get({ url: Api.ReferralPage, params });

export const getReferralDetail = (id: number | string) =>
  defHttp.get({ url: `${Api.ReferralDetail}/${id}` });

export const reviewReferral = (data: Recordable) =>
  defHttp.post({ url: Api.ReferralReview, data });

export const getReferralOptions = () =>
  defHttp.get({ url: Api.ReferralOptions });

export const submitReferralApplication = (data: Recordable) =>
  defHttp.post({ url: Api.ReferralApplication, data });

export const getPatientReferralList = (params: Recordable) =>
  defHttp.get({ url: Api.PatientReferralList, params });

export const getPatientReferralDetail = (id: number | string) =>
  defHttp.get({ url: `${Api.PatientReferralDetail}/${id}` });

export const cancelPatientReferral = (data: Recordable) =>
  defHttp.post({ url: Api.PatientReferralCancel, data });

