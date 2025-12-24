import type { UserInfo, LoginInfo } from '/#/store';
import type { ErrorMessageMode } from '/#/axios';
import { defineStore } from 'pinia';
import { store } from '/@/store';
import { RoleEnum } from '/@/enums/roleEnum';
import { PageEnum } from '/@/enums/pageEnum';
import { ROLES_KEY, TOKEN_KEY, USER_INFO_KEY, LOGIN_INFO_KEY, DB_DICT_DATA_KEY, TENANT_ID } from '/@/enums/cacheEnum';
import { getAuthCache, setAuthCache } from '/@/utils/auth';
import { GetUserInfoModel, LoginParams, ThirdLoginParams } from '/@/api/sys/model/userModel';
import { doLogout, loginApi, phoneLoginApi } from '/@/api/sys/user';
import { useI18n } from '/@/hooks/web/useI18n';
import { useMessage } from '/@/hooks/web/useMessage';
import { router } from '/@/router';
import { useGlobSetting } from '/@/hooks/setting';
import { JDragConfigEnum } from '/@/enums/jeecgEnum';
interface dictType {
  [key: string]: any;
}
interface UserState {
  userInfo: Nullable<UserInfo>;
  token?: string;
  roleList: RoleEnum[];
  dictItems?: dictType | null;
  sessionTimeout?: boolean;
  lastUpdateTime: number;
  tenantid?: string | number;
  shareTenantId?: Nullable<string | number>;
  loginInfo?: Nullable<LoginInfo>;
}

export const useUserStore = defineStore({
  id: 'app-user',
  state: (): UserState => ({
    // 用户信息
    userInfo: null,
    // token
    token: undefined,
    // 角色列表
    roleList: [],
    // 字典
    dictItems: null,
    // session过期时间
    sessionTimeout: false,
    // Last fetch time
    lastUpdateTime: 0,
    //租户id
    tenantid: '',
    // 分享租户ID
    // 用于分享页面所属租户与当前用户登录租户不一致的情况
    shareTenantId: null,
    //登录返回信息
    loginInfo: null,
  }),
  getters: {
    getUserInfo(): UserInfo {
      if(this.userInfo == null){
        this.userInfo = getAuthCache<UserInfo>(USER_INFO_KEY)!=null ? getAuthCache<UserInfo>(USER_INFO_KEY) : null;
      }
      return this.userInfo || getAuthCache<UserInfo>(USER_INFO_KEY) || {};
    },
    getLoginInfo(): LoginInfo {
      return this.loginInfo || getAuthCache<LoginInfo>(LOGIN_INFO_KEY) || {};
    },
    getToken(): string {
      return this.token || getAuthCache<string>(TOKEN_KEY);
    },
    getAllDictItems(): any {
      return this.dictItems || getAuthCache(DB_DICT_DATA_KEY);
    },
    getRoleList(): RoleEnum[] {
      return this.roleList.length > 0 ? this.roleList : getAuthCache<RoleEnum[]>(ROLES_KEY);
    },
    getSessionTimeout(): boolean {
      return !!this.sessionTimeout;
    },
    getLastUpdateTime(): number {
      return this.lastUpdateTime;
    },
    getTenant(): string | number {
      return this.tenantid || getAuthCache<string | number>(TENANT_ID);
    },
    // 是否有分享租户id
    hasShareTenantId(): boolean {
      return this.shareTenantId != null && this.shareTenantId !== '';
    },
  },
  actions: {
    setToken(info: string | undefined) {
      this.token = info ? info : ''; // for null or undefined value
      setAuthCache(TOKEN_KEY, info);
    },
    setRoleList(roleList: RoleEnum[]) {
      this.roleList = roleList;
      setAuthCache(ROLES_KEY, roleList);
    },
    setUserInfo(info: UserInfo | null) {
      this.userInfo = info;
      this.lastUpdateTime = new Date().getTime();
      setAuthCache(USER_INFO_KEY, info);
    },
    setLoginInfo(info: LoginInfo | null) {
      this.loginInfo = info;
      setAuthCache(LOGIN_INFO_KEY, info);
    },
    setAllDictItems(dictItems) {
      this.dictItems = dictItems;
      setAuthCache(DB_DICT_DATA_KEY, dictItems);
    },
    setAllDictItemsByLocal() {
      // update-begin--author:liaozhiyang---date:20240321---for：【QQYUN-8572】表格行选择卡顿问题（customRender中字典引起的）
      if (!this.dictItems) {
        const allDictItems = getAuthCache(DB_DICT_DATA_KEY);
        if (allDictItems) {
          this.dictItems = allDictItems;
        }
      }
      // update-end--author:liaozhiyang---date:20240321---for：【QQYUN-8572】表格行选择卡顿问题（customRender中字典引起的）
    },
    setTenant(id) {
      this.tenantid = id;
      setAuthCache(TENANT_ID, id);
    },
    setShareTenantId(id: NonNullable<typeof this.shareTenantId>) {
      this.shareTenantId = id;
    },
    setSessionTimeout(flag: boolean) {
      this.sessionTimeout = flag;
    },
    resetState() {
      this.userInfo = null;
      this.dictItems = null;
      this.token = '';
      this.roleList = [];
      this.sessionTimeout = false;
    },
    /**
     * 登录事件
     */
    async login(
      params: LoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<GetUserInfoModel | null> {
      try {
        const { goHome = true, mode, ...loginParams } = params;
        const res = await loginApi(loginParams, mode);
        const payload: any = res?.data ?? res?.result ?? res;
        const token = payload?.token;
        const user = payload?.user;
        if (!token || !user) {
          throw new Error('登录响应缺少必要字段');
        }
        this.setToken(token);
        const userInfo = {
          username: user.userAccount,
          realname: user.userAccount,
          avatar: '',
          desc: '',
          roles: [],
          homePath: '/dashboard/analysis',
          ...user,
        } as any;
        this.setUserInfo(userInfo as any);
        return this.afterLoginAction(goHome, { token, userInfo });
      } catch (error) {
        // 直接抛出原始错误，不要包装，让前端能够获取到后端的详细错误信息
        throw error;
      }
    },


    /**
     * 扫码登录事件
     */
    async qrCodeLogin(token): Promise<GetUserInfoModel | null> {
      try {
        // save token
        this.setToken(token);
        return this.afterLoginAction(true, {});
      } catch (error) {
        return Promise.reject(error);
      }
    },
    /**
     * 登录完成处理 - 简化版本，避免路由构建问题
     * @param goHome
     */
    async afterLoginAction(goHome?: boolean, data?: any): Promise<any | null> {
      if (!this.getToken) return null;
      try {
        const passed = data?.userInfo;
        const userInfo = passed ? passed : await this.getUserInfoAction();
        if (userInfo) this.setUserInfo(userInfo);
        await this.setLoginInfo({ ...data, isLogin: true });
        localStorage.setItem(JDragConfigEnum.DRAG_BASE_URL, useGlobSetting().domainUrl);
        const sessionTimeout = this.sessionTimeout;
        if (sessionTimeout) {
          this.setSessionTimeout(false);
        }
        let redirect = router.currentRoute.value?.query?.redirect as string;
        if (redirect && goHome) {
          window.open(`${router.options.history.base}${redirect}`, '_self');
          return data;
        }
        if (goHome) {
          const homePath = userInfo?.homePath || PageEnum.BASE_HOME;
          await router.replace(homePath);
        }
        return data;
      } catch (error) {
        if (goHome) {
          await router.replace(PageEnum.BASE_HOME);
        }
        return data;
      }
    },
    /**
     * 手机号登录
     * @param params
     */
    async phoneLogin(
      params: LoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<GetUserInfoModel | null> {
      try {
        const { goHome = true, mode, ...loginParams } = params;
        const response = await phoneLoginApi(loginParams, mode);
        
        // 解构token和user信息（适配HosUser类型）
        const { token, user } = response; // 直接解构响应数据
        
        // 验证关键数据是否存在
        if (!token) {
          throw new Error('手机号登录失败：后端未返回token');
        }
        if (!user) {
          throw new Error('手机号登录失败：后端未返回用户信息');
        }
        
        // 构造用户信息对象，适配HosUser类型
        const userInfo = {
          username: user.userAccount,
          realname: user.userAccount, // 如果后端没有返回realname，使用userAccount
          avatar: '',
          desc: '',
          roles: [],
          homePath: '/dashboard/analysis', // 默认首页
          ...user // 保留所有HosUser的原始字段
        };
        
        // save token
        this.setToken(token);
        return this.afterLoginAction(goHome, { token, userInfo });
      } catch (error) {
        // 直接抛出原始错误，不要包装，让前端能够获取到后端的详细错误信息
        throw error;
      }
    },
    /**
     * 获取用户信息 - 简化版本，避免后端类型转换问题
     */
    async getUserInfoAction(): Promise<UserInfo | null> {
      if (!this.getToken) {
        return null;
      }
      
      // 直接使用登录时保存的用户信息，避免调用可能有问题的后端接口
      const cachedUserInfo = this.getUserInfo;
      if (cachedUserInfo && cachedUserInfo.userId !== 'unknown') {
        return cachedUserInfo;
      }
      
      // 如果没有缓存的用户信息，创建一个基本的HosUser类型用户信息
      const basicUserInfo: UserInfo = {
        id: '1',
        userId: '1',
        userAccount: 'user',
        username: 'user',
        realname: '用户',
        avatar: '',
        desc: '',
        roles: [],
        homePath: '/dashboard/analysis',
        userType: 1,
        status: 1
      } as UserInfo;
      
      this.setUserInfo(basicUserInfo);
      this.setRoleList([]);
      return basicUserInfo;
    },
    /**
     * 退出登录 - 使用hospital模块的退出接口
     */
    async logout(goLogin = false) {
      try {
        // 调用hospital模块的退出登录接口
        if (this.getToken) {
          await doLogout();
          console.log('退出登录成功');
        }
      } catch (error) {
        console.log('退出登录接口调用失败，继续执行本地清理:', error);
      }

      // 清除所有用户相关数据
      this.setToken('');
      setAuthCache(TOKEN_KEY, null);
      this.setSessionTimeout(false);
      this.setUserInfo(null);
      this.setLoginInfo(null);
      this.setTenant(null);
      this.setRoleList([]);
      this.setAllDictItems(null);
      
      // 清除拖拽模块的接口前缀
      localStorage.removeItem(JDragConfigEnum.DRAG_BASE_URL);

      // 跳转到登录页
      if (goLogin) {
        await router.push({
          path: PageEnum.BASE_LOGIN,
          query: {
            // 传入当前的路由，登录成功后跳转到当前路由
            redirect: router.currentRoute.value.fullPath,
          }
        });
      }
    },
    /**
     * 第三方登录 - 已弃用，使用新的登录逻辑
     */
    async ThirdLogin(
      params: ThirdLoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<any | null> {
      console.warn('第三方登录已弃用，请使用新的登录逻辑');
      return Promise.reject(new Error('第三方登录已弃用'));
    },
    /**
     * 退出询问
     */
    confirmLoginOut() {
      const { createConfirm } = useMessage();
      const { t } = useI18n();
      createConfirm({
        iconType: 'warning',
        title: t('sys.app.logoutTip'),
        content: t('sys.app.logoutMessage'),
        onOk: async () => {
          await this.logout(true);
        },
      });
    },
  },
});

// Need to be used outside the setup
export function useUserStoreWithOut() {
  return useUserStore(store);
}
