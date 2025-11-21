import { defHttp } from '/@/utils/http/axios';

enum Api {
  DoctorList = '/admin/doctor/list',
  DoctorDetail = '/admin/doctor',
  DoctorProfile = '/doctor/profile',
  DoctorRegister = '/admin/doctor/register',
}

export interface Doctor {
  doctorId: number;
  doctorName: string;
  userId: number;
  deptId: number;
  deptName?: string;
  title: string;
  specialty: string;
  doctorDesc: string;
  avatar: string;
  isActive: number;
  // 追加：HosUser字段
  userAccount?: string;
  userPassword?: string;
  userType?: number;
  email?: string;
}

// 方法：规范化服务端返回，空数组→空对象，数组→首元素，undefined/null→默认对象
function normalizeOne<T>(data: T | T[] | undefined | null): T {
  // 处理undefined或null的情况
  if (data === undefined || data === null) {
    return {} as T;
  }
  // 处理数组的情况
  if (Array.isArray(data)) {
    return (data[0] ?? ({} as T)) as T;
  }
  // 返回原始数据
  return data as T;
}

export async function getMyDoctorProfile() {
  // 原实现：直接返回 result
  // return defHttp.get<Doctor>({ url: `${Api.DoctorProfile}/me` });
  const res = await defHttp.get<Doctor | Doctor[]>({ url: `${Api.DoctorProfile}/me` });
  return normalizeOne<Doctor>(res);
}

export async function getDoctorProfile(params: { doctorId: number }) {
  // 原实现：直接返回 result
  // return defHttp.get<Doctor>({ url: Api.DoctorProfile, params });
  const res = await defHttp.get<Doctor | Doctor[]>({ url: Api.DoctorProfile, params });
  return normalizeOne<Doctor>(res);
}

// 新增：按 userId 查询医生
export async function getDoctorByUserId(userId: number) {
  // 原实现：直接返回 result
  // return defHttp.get<Doctor>({ url: `${Api.DoctorProfile}/byUserId`, params: { userId } });
  const res = await defHttp.get<Doctor | Doctor[]>({ url: `${Api.DoctorProfile}/byUserId`, params: { userId } });
  return normalizeOne<Doctor>(res);
}

export async function getDoctorByAccount(account: string) {
  // 原实现：直接返回 result
  // return defHttp.get<Doctor>({ url: `${Api.DoctorProfile}/byAccount`, params: { account } });
  const res = await defHttp.get<Doctor | Doctor[]>({ url: `${Api.DoctorProfile}/byAccount`, params: { account } });
  return normalizeOne<Doctor>(res);
}
export function updateDoctorProfile(data: Partial<Doctor> & { doctorId: number }) {
  // 后端允许同时更新 doctor + hos_user 的字段
  return defHttp.put<boolean>({ url: Api.DoctorProfile, data });
}

export interface DoctorListParams {
  pageNo?: number;
  pageSize?: number;
  keyword?: string;
  deptId?: number;
  isActive?: number;
}

/**
 * 获取医生列表（管理员）
 */
export const getDoctorList = (params?: DoctorListParams) => {
  return defHttp.get<Record<string, any>>({ url: Api.DoctorList, params });
}

/**
 * 获取医生详情
 */
export const getDoctorDetail = (doctorId: number) =>
  defHttp.get<Doctor>({ url: `${Api.DoctorDetail}/${doctorId}` });

/**
 * 添加医生
 */
export const addDoctor = async (data: Omit<Doctor, 'doctorId' | 'userId' | 'avatar'>) => {
  try {
    console.log('添加医生请求数据:', data);
    // 修改请求URL为/add，与后端API路径对应
    // 关键修改：添加 { isTransformResponse: false } 选项，获取完整的HTTP响应
    const res = await defHttp.post<Record<string, any>>(
      { url: `${Api.DoctorDetail}/add`, data },
      { isTransformResponse: false }
    );
    console.log('添加医生原始响应:', res);
    
    // 检查响应是否存在
    if (!res) {
      console.error('添加医生：响应为空');
      return { success: false, message: '添加医生失败：服务器返回空数据' };
    }
    
    // 处理完整的HTTP响应对象
    if (res.data) {
      console.log('添加医生响应数据(data):', res.data);
      return res.data;
    }
    
    return { success: false, message: '添加医生失败：服务器返回空数据' };
  } catch (error) {
    console.error('添加医生API调用异常:', error);
    return {
      success: false,
      message: error instanceof Error ? error.message : '添加医生失败：网络错误'
    };
  }
}

/**
 * 更新医生
 */
export const updateDoctor = async (data: Partial<Doctor> & { doctorId: number }) => {
  try {
    console.log('更新医生请求数据:', JSON.stringify(data));
    console.log('更新医生请求URL:', `${Api.DoctorDetail}/update`);
    
    // 关键修改：添加 { isTransformResponse: false } 选项，获取完整的HTTP响应
    const res = await defHttp.put<Record<string, any>>(
      { url: `${Api.DoctorDetail}/update`, data },
      { isTransformResponse: false }
    );
    
    console.log('更新医生原始响应类型:', typeof res);
    console.log('更新医生原始响应存在性:', res !== null && res !== undefined);
    console.log('更新医生原始响应完整内容:', JSON.stringify(res));
    
    // 检查响应是否存在
    if (!res) {
      console.error('更新医生：响应对象为空');
      return { success: false, message: '更新医生失败：服务器返回空数据' };
    }
    
    // 检查响应对象结构
    console.log('响应对象是否有data属性:', 'data' in res);
    console.log('响应对象的data属性类型:', typeof res.data);
    
    // 处理完整的HTTP响应对象
    if (res.data) {
      console.log('更新医生响应data属性:', JSON.stringify(res.data));
      
      // 检查data是否为对象
      if (typeof res.data === 'object' && res.data !== null) {
        // 确保返回的对象有success、message字段
        const result = {
          success: res.data.success !== undefined ? res.data.success : false,
          message: res.data.message || '更新医生成功',
          ...res.data
        };
        console.log('最终返回的结果:', JSON.stringify(result));
        return result;
      }
      
      return res.data;
    }
    
    // 如果没有data属性，但有其他内容，尝试返回整个响应
    console.warn('响应没有data属性，但响应对象存在');
    return {
      success: false,
      message: '更新医生失败：无法获取响应数据',
      rawResponse: res
    };
  } catch (error) {
    console.error('更新医生API调用异常:', error);
    console.error('异常类型:', error instanceof Error ? error.name : typeof error);
    console.error('异常堆栈:', error instanceof Error ? error.stack : null);
    
    return {
      success: false,
      message: error instanceof Error ? error.message : '更新医生失败：网络错误',
      errorDetails: error instanceof Error ? error.toString() : String(error)
    };
  }
}

/**
 * 删除医生
 */
export const deleteDoctor = (doctorId: number) => {
  return defHttp.delete<Record<string, any>>({ url: `${Api.DoctorDetail}/delete/${doctorId}` });
}

// 新增的医生注册相关接口
export interface RegisterDoctorPayload {
  userAccount: string;
  userPassword: string;
  userType?: number;
  doctorName: string;
  deptId: number;
  title: string;
  specialty: string;
  doctorDesc?: string;
  email?: string;
  isActive?: number;
}

export const registerDoctorAccount = (data: RegisterDoctorPayload) => {
  return defHttp.post<any>(
    {
      url: Api.DoctorRegister,
      data: {
        userType: 2,
        isActive: 1,
        ...data,
      },
    },
    {
      isTransformResponse: false,
      errorMessageMode: 'none',
    }
  );
};
