import type { AppRouteModule } from '/@/router/types';

import { LAYOUT } from '/@/router/constant';
import { t } from '/@/hooks/web/useI18n';

const admin: AppRouteModule = {
  path: '/admin',
  name: 'Admin',
  component: LAYOUT,
  redirect: '/admin/schedule-rules',
  meta: {
    orderNo: 9,
    icon: 'ion:settings-outline',
    title: t('routes.admin.moduleName'),
    ignoreAuth: true,
  },
  children: [
    {
      path: 'schedule-rules',
      name: 'AdminScheduleRules',
      component: () => import('/@/views/admin/schedule/RuleSetting.vue'),
      meta: {
        title: t('routes.admin.scheduleRule'),
        ignoreAuth: true,
      },
    },
    {
      path: 'schedule-adjustment',
      name: 'AdminScheduleAdjustment',
      component: () => import('/@/views/admin/schedule/Adjustment.vue'),
      meta: {
        title: t('routes.admin.scheduleAdjustment'),
        ignoreAuth: true,
      },
    },
    {
      path: 'number-type',
      name: 'AdminNumberType',
      component: () => import('/@/views/admin/schedule/NumberTypeSetting.vue'),
      meta: {
        title: t('routes.admin.numberType'),
        ignoreAuth: true,
      },
    },
    {
      path: 'reservation-rule',
      name: 'AdminReservationRule',
      component: () => import('/@/views/admin/schedule/ReservationRule.vue'),
      meta: {
        title: t('routes.admin.reservationRule'),
        ignoreAuth: true,
      },
    },
    {
      path: 'reg-rule',
      name: 'AdminRegistrationRule',
      component: () => import('/@/views/admin/business/RegistrationRule.vue'),
      meta: {
        title: t('routes.admin.registrationRule'),
        ignoreAuth: true,
      },
    },
    {
      path: 'refund-rule',
      name: 'AdminRefundRule',
      component: () => import('/@/views/admin/business/RefundRule.vue'),
      meta: {
        title: t('routes.admin.refundRule'),
        ignoreAuth: true,
      },
    },
    {
      path: 'statistics',
      name: 'AdminStatistics',
      component: () => import('/@/views/admin/report/Statistics.vue'),
      meta: {
        title: t('routes.admin.statistics'),
        ignoreAuth: true,
      },
    },
    {
      path: 'report-export',
      name: 'AdminReportExport',
      component: () => import('/@/views/admin/report/ReportExport.vue'),
      meta: {
        title: t('routes.admin.reportExport'),
        ignoreAuth: true,
      },
    },
    {
      path: 'role',
      name: 'AdminRole',
      component: () => import('/@/views/admin/access/RoleManage.vue'),
      meta: {
        title: t('routes.admin.role'),
        ignoreAuth: true,
      },
    },
    {
      path: 'account-permission',
      name: 'AdminAccountPermission',
      component: () => import('/@/views/admin/access/AccountPermission.vue'),
      meta: {
        title: t('routes.admin.accountPermission'),
        ignoreAuth: true,
      },
    },
    {
      path: 'leave-approval',
      name: 'AdminLeaveApproval',
      component: () => import('/@/views/admin/access/LeaveApproval.vue'),
      meta: {
        title: t('routes.admin.leaveApproval'),
        ignoreAuth: true,
      },
    },
  ],
};

export default admin;
