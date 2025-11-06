import { http } from '../utils/request'

const BASE_URL = '/hospital/examBooking'
const PACKAGE_URL = '/hospital/examPackage'
const REPORT_URL = '/hospital/examReport'

// 体检预约相关API（若后端未实现，则返回模拟数据）
export const addExamBooking = (data: any) => http.post(`${BASE_URL}/add`, data)
export const addGroupBooking = (data: any) => http.post(`${BASE_URL}/add`, data)
export const getExamBookings = async (params?: any) => {
  try {
    const res: any = await http.get(`${BASE_URL}/list`, params)
    return res
  } catch (e) {
    // mock 数据兜底
    return {
      records: [
        { id: 1, packageName: '基础体检套餐', examineeName: '张三', phone: '13800000000', expectDate: '2025-10-30', status: 1, remark: '' },
        { id: 2, packageName: '教职工套餐', examineeName: '李四', phone: '13900000000', expectDate: '2025-11-02', status: 2, remark: '已完成' }
      ]
    }
  }
}
export const getExamBookingDetail = (id: number | string) => http.get(`${BASE_URL}/queryById`, { id })
export const cancelExamBooking = (id: number | string) => http.put(`${BASE_URL}/cancel`, null, { params: { id } })

// 体检套餐相关API
export const getExamPackages = async () => {
  try {
    return await http.get(`${PACKAGE_URL}/listActive`)
  } catch (e) {
    return [
      { id: 101, name: '基础体检套餐', price: 280 },
      { id: 102, name: '教职工体检套餐', price: 480 },
      { id: 103, name: '全面体检套餐', price: 880 }
    ]
  }
}

// 体检报告相关API
export const getExamReports = (params?: any) => http.get(`${REPORT_URL}/userReports`, params)
export const getExamReportDetail = (id: number | string) => http.get(`${REPORT_URL}/queryById`, { id })
export const markReportAsViewed = (id: number | string) => http.put(`${REPORT_URL}/markViewed`, null, { params: { id } })






