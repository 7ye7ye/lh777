
import type { AppRouteModule } from '/@/router/types';

import { LAYOUT } from '/@/router/constant';
import { t } from '/@/hooks/web/useI18n';

const admin: AppRouteModule = {
  path: '/admin',
  name: 'Admin',
  component: LAYOUT,
  redirect: '/admin/rules/schedule-rules',
  meta: {
    orderNo: 9,
    icon: 'ion:settings-outline',
    title: t('routes.admin.moduleName'),
    ignoreAuth: true,
  },
  children: [
    {
      path: 'management',
      name: 'AdminManagement',
      component: LAYOUT,
      redirect: '/admin/management/department',
      meta: {
        orderNo: 8,
        title: '管理中心',
        ignoreAuth: true,
      },
      children: [
        {
          path: 'department',
          name: 'AdminDepartmentManagement',
          component: () => import('/@/views/admin/management/DepartmentManagement.vue'),
          meta: {
            title: '科室管理',
            ignoreAuth: true,
          },
        },
        {
          path: 'department/detail/:deptId',
          name: 'AdminDepartmentDetail',
          component: () => import('/@/views/admin/management/DepartmentDetail.vue'),
          meta: {
            title: '科室详情',
            ignoreAuth: true,
            hideMenu: true,
          },
        },
        {
          path: 'doctor',
          name: 'AdminDoctorManagement',
          component: () => import('/@/views/admin/management/DoctorManagement.vue'),
          meta: {
            title: '医生管理',
            ignoreAuth: true,
          },
        },
        {
          path: 'referral/review',
          name: 'AdminReferralManagement',
          component: () => import('/@/views/admin/management/ReferralManagement.vue'),
          meta: {
            title: t('routes.admin.referralReview'),
            ignoreAuth: true,
          },
        },
        {
          path: 'referral/history',
          name: 'AdminReferralHistory',
          component: () => import('/@/views/admin/management/referral/ReferralHistory.vue'),
          meta: {
            title: t('routes.admin.referralHistory'),
            ignoreAuth: true,
          },
        },
      ],
    },
    {
      path: 'schedule-today',
      name: 'AdminScheduleToday',
      component: () => import('/@/views/admin/schedule/ScheduleToday.vue'),
      meta: {
        title: t('routes.admin.scheduleToday'),
        ignoreAuth: true,
      },
    },
    {
      path: 'schedule-calendar',
      name: 'AdminScheduleCalendar',
      component: () => import('/@/views/admin/schedule/ScheduleCalendar.vue'),
      meta: {
        title: t('routes.admin.scheduleCalendar'),
        ignoreAuth: true,
        hideMenu: true,
      },
    },
    {
      path: 'rules',
      name: 'AdminRules',
      component: LAYOUT,
      redirect: '/admin/rules/schedule-rules',
      meta: {
        orderNo: 10,
        title: t('routes.admin.ruleGroup'),
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
      ],
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
      path: 'schedule-adjustment/:id/change',
      name: 'AdminScheduleAdjustmentChange',
      component: () => import('/@/views/admin/schedule/AdjustmentChange.vue'),
      meta: {
        title: '更改排班',
        ignoreAuth: true,
        hideMenu: true,
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
      path: 'doctor-register',
      name: 'AdminDoctorRegister',
      component: () => import('/@/views/admin/doctor/DoctorRegister.vue'),
      meta: {
        title: t('routes.admin.doctorRegister'),
        ignoreAuth: true,
      },
    },
    {
      path: 'doctor-profile-update-approval',
      name: 'AdminDoctorProfileUpdateApproval',
      component: () => import('/@/views/admin/doctor/DoctorProfileUpdateApproval.vue'),
      meta: {
        title: t('routes.admin.doctorProfileUpdateApproval'),
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
