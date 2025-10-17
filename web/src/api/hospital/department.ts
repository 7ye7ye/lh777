import { defHttp } from '/@/utils/http/axios';

enum Api {
  // 小程序端接口
  AppletDepartmentTree = '/applet/department/tree',
  AppletDepartmentSecondLevel = '/applet/department/second-level',
  AppletDepartmentSearch = '/applet/department/search',
  AppletDepartmentDetail = '/applet/department',
  
  // 管理员端接口
  AdminDepartmentList = '/admin/department/list',
  AdminDepartmentCreate = '/admin/department/create',
  AdminDepartmentUpdate = '/admin/department/update',
  AdminDepartmentDelete = '/admin/department',
}

/**
 * 小程序端-获取科室树形结构
 */
export const getDepartmentTree = () => {
  return defHttp.get({ url: Api.AppletDepartmentTree });
};

/**
 * 小程序端-根据父科室ID获取二级科室
 */
export const getSecondLevelDepartments = (parentDeptId: number) => {
  return defHttp.get({ 
    url: Api.AppletDepartmentSecondLevel,
    params: { parentDeptId }
  });
};

/**
 * 小程序端-搜索科室
 */
export const searchDepartments = (keyword: string) => {
  return defHttp.get({ 
    url: Api.AppletDepartmentSearch,
    params: { keyword }
  });
};

/**
 * 小程序端-获取科室详情
 */
export const getDepartmentDetail = (deptId: number) => {
  return defHttp.get({ url: `${Api.AppletDepartmentDetail}/${deptId}` });
};

/**
 * 管理员端-获取所有科室列表
 */
export const getAllDepartments = () => {
  return defHttp.get({ url: Api.AdminDepartmentList });
};

/**
 * 管理员端-创建科室
 */
export const createDepartment = (data: any) => {
  return defHttp.post({ url: Api.AdminDepartmentCreate, data });
};

/**
 * 管理员端-更新科室
 */
export const updateDepartment = (data: any) => {
  return defHttp.put({ url: Api.AdminDepartmentUpdate, data });
};

/**
 * 管理员端-删除科室
 */
export const deleteDepartment = (deptId: number) => {
  return defHttp.delete({ url: `${Api.AdminDepartmentDelete}/${deptId}` });
};

/**
 * 管理员端-获取一级科室列表（用于二级科室选择上级）
 */
export const getFirstLevelDepartments = () => {
  return defHttp.get({ url: Api.AppletDepartmentTree });
};

