import { defHttp } from '/@/utils/http/axios';

enum Api {
  ReferralPage = '/admin/referral/page',
  ReferralDetail = '/admin/referral',
  ReferralReview = '/admin/referral/review',
  ReferralOptions = '/admin/referral/options',
}

export const fetchReferralPage = (params: Recordable) =>
  defHttp.get({ url: Api.ReferralPage, params });

export const getReferralDetail = (id: number | string) =>
  defHttp.get({ url: `${Api.ReferralDetail}/${id}` });

export const reviewReferral = (data: Recordable) =>
  defHttp.post({ url: Api.ReferralReview, params: data, data });

export const getReferralOptions = () =>
  defHttp.get({ url: Api.ReferralOptions });

