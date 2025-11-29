import type { AppRouteModule } from '/@/router/types';

const patient: AppRouteModule = {
  path: '/admin/patient',
  name: 'AdminPatient',
  component: 'LAYOUT',
  meta: {
    orderNo: 40,
    icon: 'ion:people-outline',
    title: '患者管理',
  },
  children: [
    {
      path: 'identity-approval',
      name: 'PatientIdentityApproval',
      component: '/admin/patient/PatientIdentityApproval',
      meta: {
        title: '患者身份认证',
        ignoreKeepAlive: false,
      },
    },
  ],
};

export default patient;