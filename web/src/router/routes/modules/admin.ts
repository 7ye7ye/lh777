
import type { AppRouteModule } from '/@/router/types';

import { LAYOUT } from '/@/router/constant';

const admin: AppRouteModule = {
  path: '/admin',
  name: 'Admin',
  component: LAYOUT,
  redirect: '/admin/rules/schedule-rules',
  meta: {
    orderNo: 9,
    icon: 'ion:settings-outline',
    title: '管理员端',
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
          path: 'referral/manage',
          name: 'AdminReferralManagement',
          component: () => import('/@/views/admin/management/ReferralManagement.vue'),
          meta: {
            // 统一为“转诊管理”页面，包含列表与审核
            title: '转诊管理',
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
        title: '排班调整',
        ignoreAuth: true,
      },
    },
    {
      path: 'schedule-calendar',
      name: 'AdminScheduleCalendar',
      component: () => import('/@/views/admin/schedule/ScheduleCalendar.vue'),
      meta: {
        title: '排班日历',
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
        title: '规则制定',
        ignoreAuth: true,
        hideMenu: true,
      },
      children: [
        {
          path: 'schedule-rules',
          name: 'AdminScheduleRules',
          component: () => import('/@/views/admin/schedule/RuleSetting.vue'),
          meta: {
            title: '排班规则制定',
            ignoreAuth: true,
          },
        },
        {
          path: 'number-type',
          name: 'AdminNumberType',
          component: () => import('/@/views/admin/schedule/NumberTypeSetting.vue'),
          meta: {
            title: '号别设置',
            ignoreAuth: true,
          },
        },
        {
          path: 'reservation-rule',
          name: 'AdminReservationRule',
          component: () => import('/@/views/admin/schedule/ReservationRule.vue'),
          meta: {
            title: '号源预约规则',
            ignoreAuth: true,
          },
        },
        {
          path: 'reg-rule',
          name: 'AdminRegistrationRule',
          component: () => import('/@/views/admin/business/RegistrationRule.vue'),
          meta: {
            title: '挂号规则制定',
            ignoreAuth: true,
          },
        },
        {
          path: 'refund-rule',
          name: 'AdminRefundRule',
          component: () => import('/@/views/admin/business/RefundRule.vue'),
          meta: {
            title: '退号规则制定',
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
        title: '排班调整',
        ignoreAuth: true,
        hideMenu: true,
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
      path: 'add-number-source', // 路由路径（唯一，建议语义化）
      name: 'AdminAddNumberSource', // 路由名称（唯一，格式与现有一致）
      component: () => import('/@/views/admin/schedule/AddNumberSource.vue'), // 新增的页面组件
      meta: {
        title: '新增号源', // 左侧栏显示的菜单名称
        ignoreAuth: true,
        // 可选配置：图标（与现有风格一致，用 ionicons 图标）
        icon: 'ion:add-circle-outline',
        // 可选配置：排序（orderNo 控制左侧栏显示顺序，比 schedule-adjustment 大则在后面）
        orderNo: 11,
      },
    },
    {
      path: 'statistics',
      name: 'AdminStatistics',
      component: () => import('/@/views/admin/report/Statistics.vue'),
      meta: {
        title: '数据统计',
        ignoreAuth: true,
      },
    },
    {
      path: 'report-export',
      name: 'AdminReportExport',
      component: () => import('/@/views/admin/report/ReportExport.vue'),
      meta: {
        title: '报表生成',
        ignoreAuth: true,
      },
    },
    {
      path: 'role',
      name: 'AdminRole',
      component: () => import('/@/views/admin/access/RoleManage.vue'),
      meta: {
        title: '角色管理',
        ignoreAuth: true,
      },
    },
    {
      path: 'doctor-register',
      name: 'AdminDoctorRegister',
      component: () => import('/@/views/admin/doctor/DoctorRegister.vue'),
      meta: {
        title: '医生注册',
        ignoreAuth: true,
      },
    },
    {
      path: 'doctor-profile-update-approval',
      name: 'AdminDoctorProfileUpdateApproval',
      component: () => import('/@/views/admin/doctor/DoctorProfileUpdateApproval.vue'),
      meta: {
        title: '医生资料修改审批',
        ignoreAuth: true,
      },
    },
    {
      path: 'account-permission',
      name: 'AdminAccountPermission',
      component: () => import('/@/views/admin/access/AccountPermission.vue'),
      meta: {
        title: '账号权限设置',
        ignoreAuth: true,
      },
    },
    {
      path: 'leave-approval',
      name: 'AdminLeaveApproval',
      component: () => import('/@/views/admin/access/LeaveApproval.vue'),
      meta: {
        title: '请假审批',
        ignoreAuth: true,
      },
    },
    {
      path: 'patient-identity-approval',
      name: 'AdminPatientIdentityApproval',
      component: () => import('/@/views/admin/patient/PatientIdentityApproval.vue'),
      meta: {
        title: '患者身份认证',
        ignoreAuth: true,
      },
    },
  ],
};

export default admin;
