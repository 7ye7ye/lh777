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
 * @description: Login interface return value
 */
export interface LoginResultModel {
  user: {
    userId: string | number;
    userAccount: string;
    userPassword?: string;
    userType?: number;
    idCard?: string;
    phone?: string;
    email?: string;
    status?: number;
    createTime?: string;
    updateTime?: string;
  };
  token: string;
  tokenExpireTime?: string | null;
}

/**
 * @description: Get user information return value
 */
export interface GetUserInfoModel {
  roles: RoleInfo[];
  // 用户id
  userId: string | number;
  // 用户名
  username: string;
  // 真实名字
  realname: string;
  // 头像
  avatar: string;
  // 介绍
  desc?: string;
  // 用户信息
  userInfo?: any;
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
