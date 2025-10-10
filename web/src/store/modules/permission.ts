import type { AppRouteRecordRaw, Menu } from '/@/router/types';

import { defineStore } from 'pinia';
import { store } from '/@/store';
import { useI18n } from '/@/hooks/web/useI18n';
import { useUserStore } from './user';
import { useAppStoreWithOut } from './app';
import { toRaw } from 'vue';
import { transformObjToRoute, flatMultiLevelRoutes, addSlashToRouteComponent } from '/@/router/helper/routeHelper';
import { transformRouteToMenu } from '/@/router/helper/menuHelper';

import projectSetting from '/@/settings/projectSetting';

import { PermissionModeEnum } from '/@/enums/appEnum';

import { asyncRoutes } from '/@/router/routes';
import { ERROR_LOG_ROUTE, PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';
import { staticRoutesList } from '../../router/routes/staticRouter';

import { filter } from '/@/utils/helper/treeHelper';

import { getBackMenuAndPerms } from '/@/api/sys/menu';

import { useMessage } from '/@/hooks/web/useMessage';
import { PageEnum } from '/@/enums/pageEnum';

// 系统权限
interface AuthItem {
  // 菜单权限编码，例如：“sys:schedule:list,sys:schedule:info”,多个逗号隔开
  action: string;
  // 权限策略1显示2禁用
  type: string | number;
  // 权限状态(0无效1有效)
  status: string | number;
  // 权限名称
  describe?: string;
  isAuth?: boolean;
}

interface PermissionState {
  // Permission code list
  permCodeList: string[] | number[];
  // Whether the route has been dynamically added
  isDynamicAddedRoute: boolean;
  // To trigger a menu update
  lastBuildMenuTime: number;
  // Backstage menu list
  backMenuList: Menu[];
  frontMenuList: Menu[];
  // 用户所拥有的权限
  authList: AuthItem[];
  // 全部权限配置
  allAuthList: AuthItem[];
  // 系统安全模式
  sysSafeMode: boolean;
  // online子表按钮权限
  onlineSubTableAuthMap: object;
}
export const usePermissionStore = defineStore({
  id: 'app-permission',
  state: (): PermissionState => ({
    permCodeList: [],
    // Whether the route has been dynamically added
    isDynamicAddedRoute: false,
    // To trigger a menu update
    lastBuildMenuTime: 0,
    // Backstage menu list
    backMenuList: [],
    // menu List
    frontMenuList: [],
    authList: [],
    allAuthList: [],
    sysSafeMode: false,
    onlineSubTableAuthMap: {},
  }),
  getters: {
    getPermCodeList(): string[] | number[] {
      return this.permCodeList;
    },
    getBackMenuList(): Menu[] {
      return this.backMenuList;
    },
    getFrontMenuList(): Menu[] {
      return this.frontMenuList;
    },
    getLastBuildMenuTime(): number {
      return this.lastBuildMenuTime;
    },
    getIsDynamicAddedRoute(): boolean {
      return this.isDynamicAddedRoute;
    },

    //update-begin-author:taoyan date:2022-6-1 for: VUEN-1162 子表按钮没控制
    getOnlineSubTableAuth: (state) => {
      return (code) => state.onlineSubTableAuthMap[code];
    },
    //update-end-author:taoyan date:2022-6-1 for: VUEN-1162 子表按钮没控制
  },
  actions: {
    setPermCodeList(codeList: string[]) {
      this.permCodeList = codeList;
    },

    setBackMenuList(list: Menu[]) {
      this.backMenuList = list;
      list?.length > 0 && this.setLastBuildMenuTime();
    },

    setFrontMenuList(list: Menu[]) {
      this.frontMenuList = list;
    },

    setLastBuildMenuTime() {
      this.lastBuildMenuTime = new Date().getTime();
    },

    setDynamicAddedRoute(added: boolean) {
      this.isDynamicAddedRoute = added;
    },
    resetState(): void {
      this.isDynamicAddedRoute = false;
      this.permCodeList = [];
      this.backMenuList = [];
      this.lastBuildMenuTime = 0;
    },
    async changePermissionCode() {
      try {
        const systemPermission = await getBackMenuAndPerms();
        const codeList = systemPermission.codeList;
        this.setPermCodeList(codeList);
        this.setAuthData(systemPermission);
        
        //菜单路由
        const routeList = systemPermission.menu;
        return routeList;
      } catch (error) {
        console.error('获取菜单权限失败，可能是后端类型转换错误:', error);
        // 返回空菜单列表，避免页面崩溃
        this.setPermCodeList([]);
        this.setAuthData({ codeList: [], menu: [] });
        return [];
      }
    },
    async buildRoutesAction(): Promise<AppRouteRecordRaw[]> {
      const { t } = useI18n();
      const userStore = useUserStore();
      const appStore = useAppStoreWithOut();

      let routes: AppRouteRecordRaw[] = [];
      const roleList = toRaw(userStore.getRoleList) || [];
      const { permissionMode = projectSetting.permissionMode } = appStore.getProjectConfig;

      const routeFilter = (route: AppRouteRecordRaw) => {
        const { meta } = route;
        const { roles } = meta || {};
        if (!roles) return true;
        return roleList.some((role) => roles.includes(role));
      };

      const routeRemoveIgnoreFilter = (route: AppRouteRecordRaw) => {
        const { meta } = route;
        const { ignoreRoute } = meta || {};
        return !ignoreRoute;
      };

      /**
       * @description 根据设置的首页path，修正routes中的affix标记（固定首页）
       * */
      const patchHomeAffix = (routes: AppRouteRecordRaw[]) => {
        if (!routes || routes.length === 0) return;
        let homePath: string = userStore.getUserInfo.homePath || PageEnum.BASE_HOME;
        function patcher(routes: AppRouteRecordRaw[], parentPath = '') {
          if (parentPath) parentPath = parentPath + '/';
          routes.forEach((route: AppRouteRecordRaw) => {
            const { path, children, redirect } = route;
            const currentPath = path.startsWith('/') ? path : parentPath + path;
            if (currentPath === homePath) {
              if (redirect) {
                homePath = route.redirect! as string;
              } else {
                route.meta = Object.assign({}, route.meta, { affix: true });
                throw new Error('end');
              }
            }
            children && children.length > 0 && patcher(children, currentPath);
          });
        }
        try {
          patcher(routes);
        } catch (e) {
          // 已处理完毕跳出循环
        }
        return;
      };

      switch (permissionMode) {
        case PermissionModeEnum.ROLE:
          routes = filter(asyncRoutes, routeFilter);
          routes = routes.filter(routeFilter);
          //  将多级路由转换为二级
          routes = flatMultiLevelRoutes(routes);
          break;

        case PermissionModeEnum.ROUTE_MAPPING:
          routes = filter(asyncRoutes, routeFilter);
          routes = routes.filter(routeFilter);
          const menuList = transformRouteToMenu(routes, true);
          routes = filter(routes, routeRemoveIgnoreFilter);
          routes = routes.filter(routeRemoveIgnoreFilter);
          menuList.sort((a, b) => {
            return (a.meta?.orderNo || 0) - (b.meta?.orderNo || 0);
          });

          this.setFrontMenuList(menuList);
          // 将多级路由转换为二级
          routes = flatMultiLevelRoutes(routes);
          break;

        // 后台菜单构建
        case PermissionModeEnum.BACK:
          const { createMessage, createWarningModal } = useMessage();
          console.log(" --- 构建路由菜单（简化版，使用前端路由，不依赖后端权限）--- ")
          
          // 简化版：不调用后端接口，直接使用前端定义的asyncRoutes
          let routeList: AppRouteRecordRaw[] = [];
          try {
            console.log('使用前端asyncRoutes，跳过后端权限检查');
            
            // 设置空的权限列表，表示有所有权限
            this.setPermCodeList([]);
            this.setAuthData({ codeList: [], menu: [], auth: [], allAuth: [], sysSafeMode: false });
            
            // 使用前端定义的asyncRoutes作为路由列表
            routeList = [...asyncRoutes];
            console.log('加载的路由数量:', routeList.length);
          } catch (error) {
            console.error('构建路由出错:', error);
            routeList = [];
          }

          // 构建菜单
          const backMenuList = transformRouteToMenu(routeList);
          this.setBackMenuList(backMenuList);

          // 删除meta.ignoreRoute项
          routeList = filter(routeList, routeRemoveIgnoreFilter);
          routeList = routeList.filter(routeRemoveIgnoreFilter);

          routeList = flatMultiLevelRoutes(routeList);
          // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-522】ai助手路由写死在前端
          routes = [PAGE_NOT_FOUND_ROUTE, ...routeList, ...staticRoutesList];
          // update-end--author:liaozhiyang---date:20240529---for：【TV360X-522】ai助手路由写死在前端
          break;
      }

      routes.push(ERROR_LOG_ROUTE);
      patchHomeAffix(routes);
      return routes;
    },
    setAuthData(systemPermission) {
      this.authList = systemPermission.auth;
      this.allAuthList = systemPermission.allAuth;
      this.sysSafeMode = systemPermission.sysSafeMode;
    },
    setAuthList(authList: AuthItem[]) {
      this.authList = authList;
    },
    setAllAuthList(authList: AuthItem[]) {
      this.allAuthList = authList;
    },

    //update-begin-author:taoyan date:2022-6-1 for: VUEN-1162 子表按钮没控制
    setOnlineSubTableAuth(code, hideBtnList) {
      this.onlineSubTableAuthMap[code] = hideBtnList;
    },
    //update-end-author:taoyan date:2022-6-1 for: VUEN-1162 子表按钮没控制
  },
});

// 需要在设置之外使用
export function usePermissionStoreWithOut() {
  return usePermissionStore(store);
}
