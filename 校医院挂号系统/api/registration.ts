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

// 定义接口
interface Result<T = any> {
  code: number;
  message: string;
  result?: T;
}

/**
 * 将患者加入候补队列
 */
export const addWaitingQueue = async (data: {
  scheduleId: number;
  patientId: number;
  recordId?: number;
}): Promise<{ success: boolean; message: string }> => {
  try {
    // res 就是拦截器返回的 data
    const res = await request.post('/addWaitingQueue', data) as Result<string> & { success: boolean };

    return {
      success: res.success,
      message: res.message || res.result || '成功加入候补'
    };
  } catch (error: any) {
    return {
      success: false,
      message: error?.message || '加入候补队列失败'
    };
  }
};

/**
 * 取消挂号
 * @param recordId 挂号记录ID
 */
export const cancelRegistration = async (recordId: number) => {
  return request.post(`/cancel?recordId=${recordId}`);
};

/**
 * 根据排班ID获取科室ID
 * @param scheduleId 排班ID
 */
export const getDepartmentIdBySchedule = async (scheduleId: number) => {
  return request.get('/schedule/department', { scheduleId });
};

interface ScheduleDetail {
  schedule_id: number;
  doctor_id: number;
  dept_id: number;
  type_id?: number;
  schedule_date: string;
  time_slot: number;
  room_number: string;
}

interface Result<T = any> {
  code: number;
  message: string;
  result?: T;
}

const getTimeSlotLabel = (slot?: number) => {
  if (slot === 1) return '上午';
  if (slot === 2) return '下午';
  if (slot === 3) return '晚上';
  return '';
};

export const getScheduleDetailById = async (scheduleId: number) => {
  try {
    const res = await request.get('/schedule/detail', { scheduleId }) as Result<ScheduleDetail> | ScheduleDetail;

    // 处理不同的响应格式
    let schedule: any = null;
    
    // 如果响应拦截器已经解包，res 直接就是 result 对象
    if (res && typeof res === 'object') {
      // 检查是否有 schedule_date 或 scheduleDate 字段（说明已经是排班对象）
      if ('schedule_date' in res || 'scheduleDate' in res || 'scheduleId' in res) {
        schedule = res;
      }
      // 如果有 result 字段，说明响应拦截器没有解包
      else if ('result' in res && res.result) {
        schedule = res.result;
      }
      // 如果有 data 字段
      else if ('data' in res && res.data) {
        schedule = res.data;
      }
    }

    if (!schedule) {
      console.warn('排班详情为空，scheduleId:', scheduleId, '响应:', res);
      return null;
    }

    const slotLabel = getTimeSlotLabel(schedule.time_slot || schedule.timeSlot);

    return {
      ...schedule,
      scheduleDate: schedule.schedule_date || schedule.scheduleDate,
      timeSlot: schedule.time_slot || schedule.timeSlot,
      timeSlotValue: schedule.time_slot || schedule.timeSlot,
      timeSlotText: slotLabel,
      timeSlotLabel: slotLabel,
      roomNumber: schedule.room_number || schedule.roomNumber,
      deptId: schedule.dept_id || schedule.deptId,
      doctorId: schedule.doctor_id || schedule.doctorId,
      typeId: schedule.type_id || schedule.typeId
    };
  } catch (error) {
    console.error('获取排班详情失败:', error);
    return null;
  }
};

/**
 * 根据患者ID获取患者详情
 * @param patientId 患者ID
 */
export const getPatientDetailById = async (patientId: number) => {
  try {
    const res = await request.get('/patient/detail', { patientId }) as Result<any>;
    return res?.result ?? null;
  } catch (error) {
    console.error('获取患者详情失败:', error);
    return null;
  }
};











