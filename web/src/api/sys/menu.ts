import { defHttp } from '/@/utils/http/axios';
import { getMenuListResultModel } from './model/menuModel';

enum Api {
  GetMenuList = '/sys/permission/getUserPermissionByToken',
  // 【QQYUN-8487】
  // SwitchVue3Menu = '/sys/switchVue3Menu',
}

/**
 * @description: Get user menu based on id
 */

export const getMenuList = () => {
  return new Promise((resolve) => {
    //为了兼容mock和接口数据
    defHttp.get<getMenuListResultModel>({ url: Api.GetMenuList }).then((res) => {
      if (Array.isArray(res)) {
        resolve(res);
      } else {
        resolve(res['menu']);
      }
    });
  });
};

/**
 * @description: 获取后台菜单权限和按钮权限
 */
export function getBackMenuAndPerms() {
  return defHttp.get({ url: Api.GetMenuList }, { 
    isTransformResponse: false, // 不进行数据转换，避免后端类型转换错误
    errorMessageMode: 'none' // 不显示错误提示，由调用方处理
  }).then((res) => {
    // 确保返回正确的数据结构
    if (res && res.data) {
      return res.data;
    }
    if (res && res.result) {
      return res.result;
    }
    // 如果数据结构不对，返回原始数据
    return res || { codeList: [], menu: [] };
  }).catch((error) => {
    console.error('获取菜单权限接口出错:', error);
    // 返回默认的空菜单结构
    return {
      codeList: [],
      menu: []
    };
  });
}

/**
 * 切换成vue3菜单
 */
 // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8487】注释掉判断菜单是否vue2版本逻辑代码
// export const switchVue3Menu = () => {
//   return new Promise((resolve) => {
//     defHttp.get({ url: Api.SwitchVue3Menu });
//   });
// };
// update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8487】注释掉判断菜单是否vue2版本逻辑代码
