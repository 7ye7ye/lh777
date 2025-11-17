import { defHttp } from '/@/utils/http/axios';

enum Api {
  DepartmentList = '/admin/department/list',
  DepartmentDetail = '/admin/department',
  DepartmentCreate = '/admin/department/create',
  DepartmentUpdate = '/admin/department/update',
}

export interface Department {
  deptId: number;
  deptName: string;
  parentDeptId?: number;
  deptLevel: number;
  deptDesc?: string;
  location?: string;
}

export interface DepartmentListParams {
  current?: number;
  size?: number;
  deptName?: string;
  deptLevel?: number;
}

/**
 * 获取科室列表
 */
export const getDepartmentList = (params?: DepartmentListParams) =>
  defHttp.get<Department[]>({ url: Api.DepartmentList, params });

/**
 * 获取科室详情
 */
export const getDepartmentDetail = (deptId: number) =>
  defHttp.get<Department>({ url: `${Api.DepartmentDetail}/${deptId}` });

export const createDepartment = (data: Partial<Department>) =>
  defHttp.post<boolean>({ url: Api.DepartmentCreate, data });

export const updateDepartment = (data: Department) =>
  defHttp.put<boolean>({ url: Api.DepartmentUpdate, data });

export const deleteDepartment = (deptId: number) =>
  defHttp.delete<boolean>({ url: `${Api.DepartmentDetail}/${deptId}` });

export const getAllDepartments = (params?: DepartmentListParams) =>
  defHttp.get<Department[]>({ url: Api.DepartmentList, params });

export const getFirstLevelDepartments = async () => {
  const list = await getAllDepartments();
  return Array.isArray(list) ? list.filter((d) => d.deptLevel === 1) : [];
};

