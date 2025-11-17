// api/registration.ts
import { http } from '../utils/request';

const PREFIX = '/applet/registration';

const request = {
  get: (path: string, params?: any, options?: any) => http.get(`${PREFIX}${path}`, params, options),
  post: (path: string, data?: any, options?: any) => http.post(`${PREFIX}${path}`, data, options),
};

/**
 * 获取所有挂号类型
 */
export const getRegistrationTypes = async () => {
  return request.get('/types');
};

/**
 * 根据医生ID获取排班信息
 * @param doctorId 医生ID
 * @param startDate 查询起始日期 yyyy-MM-dd
 * @param days 查询天数，默认7
 */
export const getDoctorSchedules = async (doctorId: number, startDate: string, days = 7) => {
  return request.get('/schedules', { doctorId, startDate, days });
};

/**
 * 创建挂号预约记录
 * @param record 挂号记录对象
 * @param patientId 患者ID（从登录信息获取）
 * @param joinWaitingQueue 是否加入候补队列，默认 false
 */
export const createRegistration = async (
  record: Record<string, any>,
  patientId: number,
  joinWaitingQueue = false
) => {
  // POST 请求的查询参数需要通过 URL 拼接
  const url = `/create?patientId=${patientId}&joinWaitingQueue=${joinWaitingQueue}`
  return request.post(url, record);
};

/**
 * 根据患者ID获取挂号记录
 * @param patientId 患者ID
 */
export const getRegistrationRecords = async (patientId: number) => {
  return request.get('/records', { patientId });
};

/**
 * ✅ 检查该患者是否已预约过同一排班（scheduleId）
 * @param patientId 患者ID
 * @param scheduleId 排班ID
 */
export const checkDuplicateBySchedule = async (patientId: number, scheduleId: number) => {
  return request.get(
      `/checkDuplicateBySchedule?patientId=${patientId}&scheduleId=${scheduleId}`
    );
};
