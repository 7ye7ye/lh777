import { ErrorTypeEnum } from '/@/enums/exceptionEnum';
import { MenuModeEnum, MenuTypeEnum } from '/@/enums/menuEnum';
import { RoleInfo } from '/@/api/sys/model/userModel';

// Lock screen information
export interface LockInfo {
  // Password required
  pwd?: string | undefined;
  // Is it locked?
  isLock?: boolean;
}

// Error-log information
export interface ErrorLogInfo {
  // Type of error
  type: ErrorTypeEnum;
  // Error file
  file: string;
  // Error name
  name?: string;
  // Error message
  message: string;
  // Error stack
  stack?: string;
  // Error detail
  detail: string;
  // Error url
  url: string;
  // Error time
  time?: string;
}

export interface UserInfo {
  id: string | number;
  userId: string | number;
  userAccount: string; // HosUser 类型的主要字段
  username: string;
  realname: string;
  avatar: string;
  desc?: string;
  homePath?: string;
  tenantid?: string | number;
  roles: RoleInfo[];
  orgCode?: string;
  // HosUser 类型的其他字段
  userPassword?: string | null;
  userType?: number;
  idCard?: string | null;
  phone?: string | null;
  email?: string | null;
  status?: number | null;
  createTime?: string | null;
  updateTime?: string | null;
  workNo?: string;
  orgCodeTxt?: string;
  postText?: string;
}

export interface LoginInfo {
  multi_depart?: string | number;
  userInfo?: object;
  departs?: [];
  tenantList?: [];
  isLogin?: boolean;
}

export interface BeforeMiniState {
  menuCollapsed?: boolean;
  menuSplit?: boolean;
  menuMode?: MenuModeEnum;
  menuType?: MenuTypeEnum;
}
