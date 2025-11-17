import { http } from '../utils/request';

const PREFIX = '/applet/schedule';

export const scheduleApi = {
  getByDate: (doctorId: number, date: string) =>
    http.get(`${PREFIX}/by-date`, null, { params: { doctorId, date } }),

  getWeekly: (doctorId: number, startDate: string, days = 7) =>
    http.get(`${PREFIX}/weekly`, null, { params: { doctorId, startDate, days } }),

  create: (data: any) =>
    http.post(`${PREFIX}/create`, data, {}),

  updateQuota: (scheduleId: number, usedQuota: number) =>
    http.put(`${PREFIX}/quota`, { scheduleId, usedQuota }, {}),
};