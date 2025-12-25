import type { AppRouteModule } from '/@/router/types';

import { LAYOUT } from '/@/router/constant';
import { t } from '/@/hooks/web/useI18n';

const dashboard: AppRouteModule = {
  path: '/dashboard',
  name: 'Dashboard',
  component: LAYOUT,
  redirect: '/admin/statistics',
  meta: {
    orderNo: 10,
    icon: 'ion:grid-outline',
    title: t('routes.dashboard.dashboard'),
    hideMenu: true, // 隐藏菜单，因为已经重定向到 admin/statistics
  },
  children: [
    {
      path: 'analysis',
      name: 'Analysis',
      redirect: '/admin/statistics',
      meta: {
        affix: false,
        title: t('routes.dashboard.analysis'),
        hideMenu: true, // 隐藏菜单
      },
    },
    {
      path: 'workbench',
      name: 'Workbench',
      component: () => import('/@/views/dashboard/workbench/index.vue'),
      meta: {
        title: t('routes.dashboard.workbench'),
        hideMenu: true, // 隐藏菜单
      },
    },
  ],
};

export default dashboard;
