import { http } from '../utils/request';

// 虚拟数据：科室列表（与后端数据库一致）
const mockDepartments = [
  {
    deptId: 1,
    deptName: '内科',
    parentDeptId: null,
    deptLevel: 1,
    deptDesc: '内科常见疾病诊疗',
    location: '内科门诊',
    children: [
      {
        deptId: 11,
        deptName: '心内科',
        parentDeptId: 1,
        deptLevel: 2,
        deptDesc: '心脏疾病、高血压、心律失常诊疗',
        location: '内科门诊'
      },
      {
        deptId: 12,
        deptName: '神经内科',
        parentDeptId: 1,
        deptLevel: 2,
        deptDesc: '头痛、失眠、神经痛、脑血管疾病诊疗',
        location: '内科门诊'
      },
      {
        deptId: 13,
        deptName: '消化内科',
        parentDeptId: 1,
        deptLevel: 2,
        deptDesc: '胃痛、腹泻、肠胃炎等消化系统疾病',
        location: '内科门诊'
      },
      {
        deptId: 14,
        deptName: '呼吸内科',
        parentDeptId: 1,
        deptLevel: 2,
        deptDesc: '咳嗽、哮喘、肺部疾病诊疗',
        location: '内科门诊'
      },
      {
        deptId: 15,
        deptName: '内分泌科',
        parentDeptId: 1,
        deptLevel: 2,
        deptDesc: '糖尿病、甲状腺疾病、代谢性疾病',
        location: '内科门诊'
      }
    ]
  },
  {
    deptId: 2,
    deptName: '外科',
    parentDeptId: null,
    deptLevel: 1,
    deptDesc: '外科常见疾病诊疗',
    location: '外科门诊',
    children: [
      {
        deptId: 21,
        deptName: '骨科',
        parentDeptId: 2,
        deptLevel: 2,
        deptDesc: '骨折、关节炎、颈腰椎病诊疗',
        location: '外科门诊'
      },
      {
        deptId: 22,
        deptName: '皮肤科',
        parentDeptId: 2,
        deptLevel: 2,
        deptDesc: '皮炎、湿疹、皮肤病诊疗',
        location: '外科门诊'
      },
      {
        deptId: 23,
        deptName: '普通外科',
        parentDeptId: 2,
        deptLevel: 2,
        deptDesc: '痔疮、胆囊炎、阑尾炎等外科疾病',
        location: '外科门诊'
      },
      {
        deptId: 24,
        deptName: '外伤处理',
        parentDeptId: 2,
        deptLevel: 2,
        deptDesc: '外伤、扭伤、创伤处理',
        location: '外科门诊'
      }
    ]
  },
  {
    deptId: 3,
    deptName: '预防保健科',
    parentDeptId: null,
    deptLevel: 1,
    deptDesc: '疫苗接种与健康咨询',
    location: '预防保健科',
    children: [
      {
        deptId: 31,
        deptName: '老年人免费流感疫苗预约',
        parentDeptId: 3,
        deptLevel: 2,
        deptDesc: '65岁以上老年人免费流感疫苗接种',
        location: '预防保健科'
      },
      {
        deptId: 32,
        deptName: '儿童接种疫苗预约',
        parentDeptId: 3,
        deptLevel: 2,
        deptDesc: '儿童常规疫苗接种服务',
        location: '预防保健科'
      },
      {
        deptId: 33,
        deptName: '自费流感疫苗预约',
        parentDeptId: 3,
        deptLevel: 2,
        deptDesc: '自费流感疫苗接种服务',
        location: '预防保健科'
      },
      {
        deptId: 34,
        deptName: '麻风腮疫苗',
        parentDeptId: 3,
        deptLevel: 2,
        deptDesc: '麻疹、风疹、腮腺炎联合疫苗',
        location: '预防保健科'
      },
      {
        deptId: 35,
        deptName: '甲肝、乙肝疫苗接种',
        parentDeptId: 3,
        deptLevel: 2,
        deptDesc: '甲型肝炎、乙型肝炎疫苗接种',
        location: '预防保健科'
      }
    ]
  },
  {
    deptId: 4,
    deptName: '体检科',
    parentDeptId: null,
    deptLevel: 1,
    deptDesc: '健康体检服务',
    location: '体检中心',
    children: [
      {
        deptId: 41,
        deptName: '体检复查预约',
        parentDeptId: 4,
        deptLevel: 2,
        deptDesc: '体检结果复查服务',
        location: '体检中心2楼'
      },
      {
        deptId: 43,
        deptName: '常规体检预约',
        parentDeptId: 4,
        deptLevel: 2,
        deptDesc: '教职工及家属健康体检服务',
        location: '体检中心1楼'
      },
      {
        deptId: 44,
        deptName: '老年人体检预约',
        parentDeptId: 4,
        deptLevel: 2,
        deptDesc: '65岁以上老年人免费体检',
        location: '体检中心2楼'
      },
      {
        deptId: 45,
        deptName: '学生体检预约',
        parentDeptId: 4,
        deptLevel: 2,
        deptDesc: '在校学生健康体检服务',
        location: '体检中心1楼'
      },
      {
        deptId: 46,
        deptName: '教职工体检预约',
        parentDeptId: 4,
        deptLevel: 2,
        deptDesc: '教职工年度健康体检',
        location: '体检中心1楼'
      },
      {
        deptId: 47,
        deptName: '新生入学体检',
        parentDeptId: 4,
        deptLevel: 2,
        deptDesc: '新生入学健康检查',
        location: '体检中心1楼'
      }
    ]
  },
  {
    deptId: 5,
    deptName: '口腔科',
    parentDeptId: null,
    deptLevel: 1,
    deptDesc: '口腔疾病诊疗',
    location: '口腔科',
    children: [
      {
        deptId: 51,
        deptName: '口腔咨询门诊',
        parentDeptId: 5,
        deptLevel: 2,
        deptDesc: '口腔健康咨询和初步检查',
        location: '口腔科'
      },
      {
        deptId: 52,
        deptName: '口腔治疗',
        parentDeptId: 5,
        deptLevel: 2,
        deptDesc: '龋齿治疗、牙周病、口腔修复',
        location: '口腔科'
      }
    ]
  },
  {
    deptId: 6,
    deptName: 'B超室',
    parentDeptId: null,
    deptLevel: 1,
    deptDesc: '超声检查服务',
    location: 'B超室',
    children: [
      {
        deptId: 61,
        deptName: '超声心动图',
        parentDeptId: 6,
        deptLevel: 2,
        deptDesc: '心脏超声检查',
        location: 'B超室'
      },
      {
        deptId: 62,
        deptName: '肝胆胰脾彩超',
        parentDeptId: 6,
        deptLevel: 2,
        deptDesc: '腹部器官彩色超声检查',
        location: 'B超室'
      },
      {
        deptId: 63,
        deptName: '其他项目彩超',
        parentDeptId: 6,
        deptLevel: 2,
        deptDesc: '其他部位彩色超声检查',
        location: 'B超室'
      }
    ]
  },
  {
    deptId: 7,
    deptName: '护理科',
    parentDeptId: null,
    deptLevel: 1,
    deptDesc: '护理服务',
    location: '护理科',
    children: [
      {
        deptId: 71,
        deptName: '骨密度检测',
        parentDeptId: 7,
        deptLevel: 2,
        deptDesc: '骨密度检查服务',
        location: '护理科'
      },
      {
        deptId: 72,
        deptName: 'PICC换药',
        parentDeptId: 7,
        deptLevel: 2,
        deptDesc: 'PICC导管维护服务',
        location: '护理科'
      }
    ]
  },
  {
    deptId: 8,
    deptName: '公疗报销',
    parentDeptId: null,
    deptLevel: 1,
    deptDesc: '公费医疗报销服务',
    location: '财务科',
    children: [
      {
        deptId: 81,
        deptName: '毕业生公疗报销预约',
        parentDeptId: 8,
        deptLevel: 2,
        deptDesc: '毕业生公费医疗报销服务',
        location: '财务科'
      },
      {
        deptId: 82,
        deptName: '学生专属预约',
        parentDeptId: 8,
        deptLevel: 2,
        deptDesc: '在校学生公费医疗报销',
        location: '财务科'
      },
      {
        deptId: 83,
        deptName: '教职工专属预约',
        parentDeptId: 8,
        deptLevel: 2,
        deptDesc: '教职工公费医疗报销',
        location: '财务科'
      }
    ]
  }
];

/**
 * 获取科室树形结构（一级+二级）
 * 使用虚拟数据
 */
export const getDepartmentTree = async () => {
  // 模拟网络延迟
  await new Promise(resolve => setTimeout(resolve, 300));
  
  return {
    success: true,
    result: mockDepartments,
    message: '获取成功'
  };
};

/**
 * 根据父科室ID获取二级科室
 * 使用虚拟数据
 */
export const getSecondLevelDepartments = async (parentDeptId: number) => {
  await new Promise(resolve => setTimeout(resolve, 200));
  
  const parent = mockDepartments.find(d => d.deptId === parentDeptId);
  const children = parent?.children || [];
  
  return {
    success: true,
    result: children,
    message: '获取成功'
  };
};

/**
 * 搜索科室
 * 使用虚拟数据
 */
export const searchDepartments = async (keyword: string) => {
  await new Promise(resolve => setTimeout(resolve, 200));
  
  const results: any[] = [];
  mockDepartments.forEach(dept => {
    if (dept.deptName.includes(keyword)) {
      results.push(dept);
    }
    dept.children?.forEach(child => {
      if (child.deptName.includes(keyword)) {
        results.push(child);
      }
    });
  });
  
  return {
    success: true,
    result: results,
    message: '搜索成功'
  };
};

/**
 * 获取科室详情
 * 使用虚拟数据
 */
export const getDepartmentDetail = async (deptId: number | string) => {
  await new Promise(resolve => setTimeout(resolve, 200));
  
  // 转换为数字进行比较
  const targetId = typeof deptId === 'string' ? parseInt(deptId) : deptId;
  
  let department = null;
  
  // 先在一级科室中查找
  department = mockDepartments.find(d => d.deptId === targetId);
  
  // 如果没找到，在二级科室中查找
  if (!department) {
    for (const dept of mockDepartments) {
      const child = dept.children?.find(c => c.deptId === targetId);
      if (child) {
        department = child;
        break;
      }
    }
  }
  
  if (department) {
    return {
      success: true,
      result: department,
      message: '获取成功'
    };
  } else {
    return {
      success: false,
      result: null,
      message: '科室不存在'
    };
  }
};
