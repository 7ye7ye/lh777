import type { AppRouteModule } from '/@/router/types';

import { LAYOUT } from '/@/router/constant';
import { t } from '/@/hooks/web/useI18n';

const hospital: AppRouteModule = {
  path: '/hospital',
  name: 'Hospital',
  component: LAYOUT,
  redirect: '/hospital/adjustment/approve',
  meta: {
    orderNo: 10,
    icon: 'ion:medical-outline',
    title: t('routes.hospital.moduleName'),
    // 仅医生可访问，且必须登录
    requiredUserType: 2,
  },
  children: [
    {
      path: 'department',
      name: 'DepartmentManage',
      component: () => import('/@/views/hospital/department/DepartmentManage.vue'),
      meta: {
        title: t('routes.hospital.department'),
        icon: 'ion:business-outline',
        requiredUserType: 2,
      },
    },
    {
      path: 'adjustment/approve',
      name: 'AdjustmentApprove',
      component: () => import('/@/views/hospital/adjustment/AdjustmentApprove.vue'),
      meta: {
        title: t('routes.hospital.adjustmentApprove'),
        icon: 'ion:checkmark-circle-outline',
        requiredUserType: 2,
      },
    },
    {
      path: 'schedule/manage',
      name: 'ScheduleManage',
      component: () => import('/@/views/hospital/schedule/ScheduleManage.vue'),
      meta: {
        title: t('routes.hospital.scheduleManage'),
        icon: 'ion:list-outline',
        requiredUserType: 2,
      },
    },
    {
      path: 'doctor/profile',
      name: 'DoctorProfile',
      component: () => import('/@/views/hospital/doctor/Profile.vue'),
      meta: {
        title: '医生个人信息',
        icon: 'ion:person-circle-outline',
        requiredUserType: 2,
      },
    },
  ],
};

export default hospital;