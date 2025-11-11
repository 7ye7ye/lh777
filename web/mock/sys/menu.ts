import { resultSuccess, resultError, getRequestToken, requestParams,baseUrl} from '../_util';
import { MockMethod } from 'vite-plugin-mock';
import { createFakeUserList } from './user';

// single
const dashboardRoute = {
  path: '/dashboard',
  name: 'Dashboard',
  component: 'LAYOUT',
  redirect: '/dashboard/analysis',
  meta: {
    title: 'routes.dashboard.dashboard',
    hideChildrenInMenu: true,
    icon: 'bx:bx-home',
  },
  children: [
    {
      path: 'analysis',
      name: 'Analysis',
      component: '/dashboard/Analysis/index',
      meta: {
        hideMenu: true,
        hideBreadcrumb: true,
        title: 'routes.dashboard.analysis',
        currentActiveMenu: '/dashboard',
        icon: 'bx:bx-home',
      },
    },
    {
      path: 'workbench',
      name: 'Workbench',
      component: '/dashboard/workbench/index',
      meta: {
        hideMenu: true,
        hideBreadcrumb: true,
        title: 'routes.dashboard.workbench',
        currentActiveMenu: '/dashboard',
        icon: 'bx:bx-home',
      },
    },
  ],
};

const backRoute = {
  path: 'back',
  name: 'PermissionBackDemo',
  meta: {
    title: 'routes.demo.permission.back',
  },

  children: [
    {
      path: 'page',
      name: 'BackAuthPage',
      component: '/demo/permission/back/index',
      meta: {
        title: 'routes.demo.permission.backPage',
      },
    },
    {
      path: 'btn',
      name: 'BackAuthBtn',
      component: '/demo/permission/back/Btn',
      meta: {
        title: 'routes.demo.permission.backBtn',
      },
    },
  ],
};

const authRoute = {
  path: '/permission',
  name: 'Permission',
  component: 'LAYOUT',
  redirect: '/permission/front/page',
  meta: {
    icon: 'carbon:user-role',
    title: 'routes.demo.permission.permission',
  },
  children: [backRoute],
};

const levelRoute = {
  path: '/level',
  name: 'Level',
  component: 'LAYOUT',
  redirect: '/level/menu1/menu1-1',
  meta: {
    icon: 'carbon:user-role',
    title: 'routes.demo.level.level',
  },

  children: [
    {
      path: 'menu1',
      name: 'Menu1Demo',
      meta: {
        title: 'Menu1',
      },
      children: [
        {
          path: 'menu1-1',
          name: 'Menu11Demo',
          meta: {
            title: 'Menu1-1',
          },
          children: [
            {
              path: 'menu1-1-1',
              name: 'Menu111Demo',
              component: '/demo/level/Menu111',
              meta: {
                title: 'Menu111',
              },
            },
          ],
        },
        {
          path: 'menu1-2',
          name: 'Menu12Demo',
          component: '/demo/level/Menu12',
          meta: {
            title: 'Menu1-2',
          },
        },
      ],
    },
    {
      path: 'menu2',
      name: 'Menu2Demo',
      component: '/demo/level/Menu2',
      meta: {
        title: 'Menu2',
      },
    },
  ],
};

const sysRoute = {
  path: '/system',
  name: 'System',
  component: 'LAYOUT',
  redirect: '/system/account',
  meta: {
    icon: 'ion:settings-outline',
    title: 'routes.demo.system.moduleName',
  },
  children: [
    {
      path: 'account',
      name: 'AccountManagement',
      meta: {
        title: 'routes.demo.system.account',
        ignoreKeepAlive: true,
      },
      component: '/demo/system/account/index',
    },
    {
      path: 'account_detail/:id',
      name: 'AccountDetail',
      meta: {
        hideMenu: true,
        title: 'routes.demo.system.account_detail',
        ignoreKeepAlive: true,
        showMenu: false,
        currentActiveMenu: '/system/account',
      },
      component: '/demo/system/account/AccountDetail',
    },
    {
      path: 'role',
      name: 'RoleManagement',
      meta: {
        title: 'routes.demo.system.role',
        ignoreKeepAlive: true,
      },
      component: '/demo/system/role/index',
    },

    {
      path: 'menu',
      name: 'MenuManagement',
      meta: {
        title: 'routes.demo.system.menu',
        ignoreKeepAlive: true,
      },
      component: '/demo/system/menu/index',
    },
    {
      path: 'dept',
      name: 'DeptManagement',
      meta: {
        title: 'routes.demo.system.dept',
        ignoreKeepAlive: true,
      },
      component: '/demo/system/dept/index',
    },
    {
      path: 'changePassword',
      name: 'ChangePassword',
      meta: {
        title: 'routes.demo.system.password',
        ignoreKeepAlive: true,
      },
      component: '/demo/system/password/index',
    },
  ],
};

// Admin management portal routes (backend-permission mock)
const adminRoute = {
  path: '/admin',
  name: 'Admin',
  component: 'LAYOUT',
  redirect: '/admin/schedule-rules',
  meta: {
    icon: 'ion:settings-outline',
    title: 'routes.admin.moduleName',
  },
  children: [
    {
      path: 'schedule-rules',
      name: 'AdminScheduleRules',
      component: '/admin/schedule/RuleSetting',
      meta: {
        title: 'routes.admin.scheduleRule',
        ignoreKeepAlive: true,
      },
    },
    {
      path: 'schedule-adjustment',
      name: 'AdminScheduleAdjustment',
      component: '/admin/schedule/Adjustment',
      meta: {
        title: 'routes.admin.scheduleAdjustment',
        ignoreKeepAlive: true,
      },
    },
    {
      path: 'number-type',
      name: 'AdminNumberType',
      component: '/admin/schedule/NumberTypeSetting',
      meta: {
        title: 'routes.admin.numberType',
        ignoreKeepAlive: true,
      },
    },
    {
      path: 'reservation-rule',
      name: 'AdminReservationRule',
      component: '/admin/schedule/ReservationRule',
      meta: {
        title: 'routes.admin.reservationRule',
        ignoreKeepAlive: true,
      },
    },
    {
      path: 'reg-rule',
      name: 'AdminRegistrationRule',
      component: '/admin/business/RegistrationRule',
      meta: {
        title: 'routes.admin.registrationRule',
        ignoreKeepAlive: true,
      },
    },
    {
      path: 'refund-rule',
      name: 'AdminRefundRule',
      component: '/admin/business/RefundRule',
      meta: {
        title: 'routes.admin.refundRule',
        ignoreKeepAlive: true,
      },
    },
    {
      path: 'statistics',
      name: 'AdminStatistics',
      component: '/admin/report/Statistics',
      meta: {
        title: 'routes.admin.statistics',
        ignoreKeepAlive: true,
      },
    },
    {
      path: 'report-export',
      name: 'AdminReportExport',
      component: '/admin/report/ReportExport',
      meta: {
        title: 'routes.admin.reportExport',
        ignoreKeepAlive: true,
      },
    },
    {
      path: 'role',
      name: 'AdminRole',
      component: '/admin/access/RoleManage',
      meta: {
        title: 'routes.admin.role',
        ignoreKeepAlive: true,
      },
    },
    {
      path: 'account-permission',
      name: 'AdminAccountPermission',
      component: '/admin/access/AccountPermission',
      meta: {
        title: 'routes.admin.accountPermission',
        ignoreKeepAlive: true,
      },
    },
    {
      path: 'leave-approval',
      name: 'AdminLeaveApproval',
      component: '/admin/access/LeaveApproval',
      meta: {
        title: 'routes.admin.leaveApproval',
        ignoreKeepAlive: true,
      },
    },
  ],
};

const linkRoute = {
  path: '/link',
  name: 'Link',
  component: 'LAYOUT',
  meta: {
    icon: 'ion:tv-outline',
    title: 'routes.demo.iframe.frame',
  },
  children: [
    {
      path: 'doc',
      name: 'Doc',
      meta: {
        title: 'routes.demo.iframe.doc',
        frameSrc: 'https://vvbin.cn/doc-next/',
      },
    },
    {
      path: 'https://vvbin.cn/doc-next/',
      name: 'DocExternal',
      component: 'LAYOUT',
      meta: {
        title: 'routes.demo.iframe.docExternal',
      },
    },
  ],
};

export default [
  {
    url: `${baseUrl}/sys/permission/getUserPermissionByToken`,
    timeout: 1000,
    method: 'get',
    response: (request: requestParams) => {
      const token = getRequestToken(request);
      if (!token) {
        return resultError('Invalid token!');
      }
      const checkUser = createFakeUserList().find((item) => item.token === token);
      if (!checkUser) {
        return resultError('Invalid user token!');
      }
      const id = checkUser.userId;
      let menu: Object[];
      switch (id) {
        case '1':
          dashboardRoute.redirect = dashboardRoute.path + '/' + dashboardRoute.children[0].path;
          menu = [dashboardRoute, adminRoute, authRoute, levelRoute, sysRoute, linkRoute];
          break;
        case '2':
          dashboardRoute.redirect = dashboardRoute.path + '/' + dashboardRoute.children[1].path;
          menu = [dashboardRoute, authRoute, levelRoute, linkRoute];
          break;
        default:
          menu = [];
      }

      return resultSuccess(menu);
    },
  },
] as MockMethod[];
