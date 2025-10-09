/**
 * @description: Login interface parameters
 */
export interface LoginParams {
  userAccount: string;
  userPassword: string;
  captcha?: string;
  checkKey?: string;
}

export interface ThirdLoginParams {
  token: string;
  thirdType: string;
}

export interface RoleInfo {
  roleName: string;
  value: string;
}

/**
 * @description: Login interface return value - 适配 HosUser 类型
 */
export interface LoginResultModel {
  user: HosUser;
  token: string;
  tokenExpireTime?: string | null;
}

/**
 * @description: HosUser 类型定义 - 适配后端 HosUser 类型
 */
export interface HosUser {
  userId: string | number;
  userAccount: string;
  userPassword?: string | null;
  userType?: number;
  idCard?: string | null;
  phone?: string | null;
  email?: string | null;
  status?: number | null;
  createTime?: string | null;
  updateTime?: string | null;
  // 添加其他可能需要的字段
  realname?: string;
  avatar?: string;
  homePath?: string;
  roles?: RoleInfo[];
  id?: string | number;
  username?: string;
  desc?: string;
  orgCode?: string;
  workNo?: string;
  orgCodeTxt?: string;
  postText?: string;
}

/**
 * @description: Get user information return value - 适配 HosUser 类型
 */
export interface GetUserInfoModel {
  userInfo: HosUser;
  // 缓存字典项
  sysAllDictItems?: any;
}

/**
 * @description: Get user information return value
 */
export interface GetResultModel {
  code: number;
  message: string;
  result: object;
  success: Boolean;
}
