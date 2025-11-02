import { http } from '../utils/request';

const PREFIX = '/applet/department';

const d = {
  get: (path: string, params?: any, options?: any) => http.get(`${PREFIX}${path}`, params, options),
  post: (path: string, data?: any, options?: any) => http.post(`${PREFIX}${path}`, data, options),
  put: (path: string, data?: any, options?: any) => http.put(`${PREFIX}${path}`, data, options),
  delete: (path: string, params?: any, options?: any) => http.delete(`${PREFIX}${path}`, params, options),
};

/**
 * 获取科室树形结构（一级+二级）
 */
export const getDepartmentTree = async () => {
  return d.get('/tree');
};

/**
 * 根据父科室ID获取二级科室
 */
export const getSecondLevelDepartments = async (parentDeptId: number) => {
  return d.get('/second-level', { parentDeptId });
};

/**
 * 搜索科室
 */
export const searchDepartments = async (keyword: string) => {
  return d.get('/search', { keyword });
};

/**
 * 获取科室详情
 */
export const getDepartmentDetail = async (deptId: number | string) => {
  return d.get(`/${deptId}`);
};
