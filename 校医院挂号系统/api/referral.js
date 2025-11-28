// api/referral.js
import { request } from '../utils/request';

/**
 * 获取患者就诊记录列表（用于转诊选择）
 * @param {Object} params 查询参数
 * @returns {Promise} 接口返回结果
 */
export function getPatientVisitRecords(params) {
  return request({
    url: '/patient/registration/history',
    method: 'get',
    params
  });
}

/**
 * 获取患者转诊记录列表
 * @param {Object} params 查询参数
 * @param {number} params.pageNo 页码
 * @param {number} params.pageSize 每页大小
 * @param {string} params.keyword 搜索关键词
 * @param {string} params.status 状态筛选
 * @returns {Promise} 接口返回结果
 */
export function getPatientReferralList(params) {
  return request({
    url: '/patient/referral/list',
    method: 'get',
    params
  });
}

/**
 * 获取患者转诊详情
 * @param {number} id 转诊申请ID
 * @returns {Promise} 接口返回结果
 */
export function getPatientReferralDetail(id) {
  return request({
    url: `/patient/referral/${id}`,
    method: 'get'
  });
}

/**
 * 取消患者转诊申请
 * @param {Object} params 参数
 * @param {number} params.id 转诊申请ID
 * @param {string} params.reason 取消原因
 * @returns {Promise} 接口返回结果
 */
export function cancelPatientReferral(params) {
  return request({
    url: '/patient/referral/cancel',
    method: 'post',
    data: params
  });
}

/**
 * 获取转诊选项（院内科室和外部医院）
 * @returns {Promise} 接口返回结果
 */
export function getReferralOptions() {
  return request({
    url: '/patient/referral/options',
    method: 'get'
  });
}

/**
 * 获取合作医院列表（院外转诊）
 * 单独暴露，便于仅需要医院数据的页面引用。
 * @returns {Promise} 接口返回结果
 */
export function getReferralHospitals() {
  return request({
    url: '/patient/referral/options',
    method: 'get'
  }).then((res) => {
    const data = res?.data || res?.result || res || {}
    if (Array.isArray(data.hospitals)) {
      return { ...res, data: data.hospitals }
    }
    // 若后端直接返回数组
    if (Array.isArray(data)) {
      return { ...res, data }
    }
    return res
  })
}

/**
 * 提交转诊申请
 * @param {Object} data 转诊申请数据
 * @returns {Promise} 接口返回结果
 */
export function submitReferralApplication(data) {
  return request({
    url: '/patient/referral/apply',
    method: 'post',
    data: data
  });
}

/**
 * 医生生成转诊意见
 * @param {Object} data 转诊意见数据
 * @returns {Promise} 接口返回结果
 */
export function doctorGenerateReferralAdvice(data) {
  return request({
    url: '/api/referral/doctor/create',
    method: 'post',
    data: data
  });
}

/**
 * 院内转诊自动挂号
 * @param {number|string} referralId 转诊申请ID
 * @returns {Promise} 接口返回结果
 */
export function autoRegisterInternalReferral(referralId) {
  return request({
    url: `/patient/referral/autoRegister/${referralId}`,
    method: 'post',
    data: {}
  });
}