// 模拟数据，用于测试
export const mockDepartmentTree = [
  {
    deptId: 1,
    deptName: '内科',
    deptDesc: '内科疾病诊疗',
    children: [
      {
        deptId: 11,
        deptName: '心血管内科',
        deptDesc: '心血管疾病诊疗',
        location: '门诊楼2层东侧'
      },
      {
        deptId: 12,
        deptName: '消化内科',
        deptDesc: '消化系统疾病诊疗',
        location: '门诊楼2层西侧'
      }
    ]
  },
  {
    deptId: 2,
    deptName: '外科',
    deptDesc: '外科疾病诊疗',
    children: [
      {
        deptId: 21,
        deptName: '普外科',
        deptDesc: '普通外科疾病诊疗',
        location: '门诊楼3层东侧'
      },
      {
        deptId: 22,
        deptName: '骨科',
        deptDesc: '骨科疾病诊疗',
        location: '门诊楼3层西侧'
      }
    ]
  }
];

export const mockDepartmentDetail = {
  deptId: 11,
  deptName: '心血管内科',
  deptDesc: '心血管内科是医院的重点科室，专门负责心血管疾病的诊断和治疗。科室拥有先进的医疗设备和专业的医师团队，能够为患者提供高质量的医疗服务。',
  location: '门诊楼2层东侧',
  createTime: '2024-01-01 10:00:00',
  updateTime: '2024-01-01 10:00:00'
};
