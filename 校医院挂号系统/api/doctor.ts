import { http } from '../utils/request';
import { mockDoctorList, getDoctorsByDeptId as getMockDoctorsByDeptId, getMockDoctorDetail } from './doctor-mock';

// 是否使用模拟数据（当后端服务器不可用时）
const USE_MOCK_DATA = true;

/**
 * 获取所有在职医生列表
 */
export const getAllDoctors = async () => {
  if (USE_MOCK_DATA) {
    await new Promise(resolve => setTimeout(resolve, 300));
    return mockDoctorList.filter(d => d.isActive === 1);
  }
  
  try {
    return await http.get('/applet/doctor/list');
  } catch (error) {
    console.warn('后端接口不可用，使用模拟数据:', error);
    return mockDoctorList.filter(d => d.isActive === 1);
  }
};

/**
 * 根据科室ID获取医生列表
 */
export const getDoctorsByDeptId = async (deptId: number) => {
  if (USE_MOCK_DATA) {
    await new Promise(resolve => setTimeout(resolve, 300));
    return getMockDoctorsByDeptId(deptId);
  }
  
  try {
    const res = await http.get('/applet/doctor/listByDeptId', { deptId });
    return res.result || res.data || res;
  } catch (error) {
    console.warn('后端接口不可用，使用模拟数据:', error);
    return getMockDoctorsByDeptId(deptId);
  }
};

/**
 * 获取医生详情
 */
export const getDoctorDetail = async (doctorId: number) => {
  if (USE_MOCK_DATA) {
    await new Promise(resolve => setTimeout(resolve, 300));
    return getMockDoctorDetail(doctorId);
  }
  
  try {
    return await http.get(`/applet/doctor/${doctorId}`);
  } catch (error) {
    console.warn('后端接口不可用，使用模拟数据:', error);
    return getMockDoctorDetail(doctorId);
  }
};

/**
 * 搜索医生
 */
export const searchDoctors = async (keyword: string) => {
  if (USE_MOCK_DATA) {
    await new Promise(resolve => setTimeout(resolve, 300));
    return mockDoctorList.filter(doctor => 
      doctor.isActive === 1 && (
        doctor.doctorName.includes(keyword) || 
        doctor.specialty.includes(keyword) ||
        (doctor.doctorDesc && doctor.doctorDesc.includes(keyword))
      )
    );
  }
  
  try {
    return await http.get('/applet/doctor/search', { keyword });
  } catch (error) {
    console.warn('后端接口不可用，使用模拟数据:', error);
    return mockDoctorList.filter(doctor => 
      doctor.isActive === 1 && (
        doctor.doctorName.includes(keyword) || 
        doctor.specialty.includes(keyword) ||
        (doctor.doctorDesc && doctor.doctorDesc.includes(keyword))
      )
    );
  }
};

