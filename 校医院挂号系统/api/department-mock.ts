// 模拟数据，用于测试
export const mockDepartmentTree = [
  {
    deptId: 1,
    deptName: '疫苗预约',
    deptDesc: '疫苗接种服务',
    children: [
      {
        deptId: 11,
        deptName: '老年人免费流感疫苗预约',
        deptDesc: '65岁以上老年人免费流感疫苗接种',
        location: '预防保健科'
      },
      {
        deptId: 12,
        deptName: '儿童接种疫苗预约',
        deptDesc: '儿童常规疫苗接种服务',
        location: '预防保健科'
      },
      {
        deptId: 13,
        deptName: '自费流感疫苗预约',
        deptDesc: '自费流感疫苗接种服务',
        location: '预防保健科'
      },
      {
        deptId: 14,
        deptName: '麻风腮疫苗',
        deptDesc: '麻疹、风疹、腮腺炎联合疫苗',
        location: '预防保健科'
      },
      {
        deptId: 15,
        deptName: '甲肝、乙肝疫苗接种',
        deptDesc: '甲型肝炎、乙型肝炎疫苗接种',
        location: '预防保健科'
      }
    ]
  },
  {
    deptId: 2,
    deptName: 'B超室',
    deptDesc: '超声检查服务',
    children: [
      {
        deptId: 21,
        deptName: '超声心动图',
        deptDesc: '心脏超声检查',
        location: 'B超室'
      },
      {
        deptId: 22,
        deptName: '肝胆胰脾彩超',
        deptDesc: '腹部器官彩色超声检查',
        location: 'B超室'
      },
      {
        deptId: 23,
        deptName: '其他项目彩超',
        deptDesc: '其他部位彩色超声检查',
        location: 'B超室'
      }
    ]
  },
  {
    deptId: 3,
    deptName: '公疗报销',
    deptDesc: '公费医疗报销服务',
    children: [
      {
        deptId: 31,
        deptName: '毕业生公疗报销预约',
        deptDesc: '毕业生公费医疗报销服务',
        location: '财务科'
      },
      {
        deptId: 32,
        deptName: '学生专属预约',
        deptDesc: '在校学生公费医疗报销',
        location: '财务科'
      },
      {
        deptId: 33,
        deptName: '教职工专属预约',
        deptDesc: '教职工公费医疗报销',
        location: '财务科'
      }
    ]
  },
  {
    deptId: 4,
    deptName: '体检科',
    deptDesc: '健康体检服务',
    children: [
      {
        deptId: 41,
        deptName: '体检复查预约',
        deptDesc: '体检结果复查服务',
        location: '体检科'
      }
    ]
  },
  {
    deptId: 5,
    deptName: '口腔科',
    deptDesc: '口腔疾病诊疗',
    children: [
      {
        deptId: 51,
        deptName: '口腔咨询门诊',
        deptDesc: '口腔健康咨询和初步检查',
        location: '口腔科'
      },
      {
        deptId: 52,
        deptName: '口腔科',
        deptDesc: '口腔疾病诊疗服务',
        location: '口腔科'
      }
    ]
  },
  {
    deptId: 6,
    deptName: '护理科',
    deptDesc: '护理服务',
    children: [
      {
        deptId: 61,
        deptName: '骨密度检测',
        deptDesc: '骨密度检查服务',
        location: '护理科'
      },
      {
        deptId: 62,
        deptName: 'PICC换药',
        deptDesc: 'PICC导管维护服务',
        location: '护理科'
      }
    ]
  }
];

// 所有科室的详细信息
export const mockDepartmentDetails = {
  11: {
    deptId: 11,
    deptName: '老年人免费流感疫苗预约',
    deptDesc: '为65岁以上老年人提供免费流感疫苗接种服务。流感疫苗是预防流感最有效的方法，特别对老年人等高风险人群具有重要意义。我们提供专业的疫苗接种服务，确保接种安全有效。',
    location: '预防保健科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  12: {
    deptId: 12,
    deptName: '儿童接种疫苗预约',
    deptDesc: '为儿童提供常规疫苗接种服务。包括国家免疫规划疫苗和自费疫苗，严格按照国家免疫程序进行接种，保障儿童健康成长。',
    location: '预防保健科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  13: {
    deptId: 13,
    deptName: '自费流感疫苗预约',
    deptDesc: '为有需求的人群提供自费流感疫苗接种服务。接种流感疫苗可有效预防流感病毒感染，降低感染风险，特别适合学生、上班族等需要提高免疫力的人群。',
    location: '预防保健科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  14: {
    deptId: 14,
    deptName: '麻风腮疫苗',
    deptDesc: '提供麻疹、风疹、腮腺炎联合疫苗接种服务。这是一种联合疫苗，可同时预防三种传染病，适用于儿童和成人补种。接种后可有效保护机体免受这些病毒感染。',
    location: '预防保健科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  15: {
    deptId: 15,
    deptName: '甲肝、乙肝疫苗接种',
    deptDesc: '提供甲型肝炎和乙型肝炎疫苗接种服务。肝炎疫苗可有效预防肝炎病毒感染，保护肝脏健康。我们提供单独接种和联合接种方案，根据个人情况制定最佳接种计划。',
    location: '预防保健科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  21: {
    deptId: 21,
    deptName: '超声心动图',
    deptDesc: '提供专业的心脏超声检查服务。可清晰显示心脏结构、心室心房大小、心脏瓣膜功能、心肌运动等，是诊断心脏疾病的重要检查手段。配备先进的彩色多普勒超声设备和经验丰富的医师。',
    location: 'B超室',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  22: {
    deptId: 22,
    deptName: '肝胆胰脾彩超',
    deptDesc: '提供肝脏、胆囊、胰腺、脾脏等腹部器官的彩色超声检查服务。可以发现肝脏病变、胆囊结石、胆囊炎、胰腺炎等疾病，是腹部疾病诊断的首选检查方法。检查无创、无辐射、准确率高。',
    location: 'B超室',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  23: {
    deptId: 23,
    deptName: '其他项目彩超',
    deptDesc: '提供全身各部位的彩色超声检查服务，包括甲状腺、乳腺、泌尿系统、妇科、浅表淋巴结等。采用高清晰度彩超设备，由经验丰富的超声医师进行诊断，为临床提供准确的影像学依据。',
    location: 'B超室',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  31: {
    deptId: 31,
    deptName: '毕业生公疗报销预约',
    deptDesc: '为即将毕业或已毕业的学生提供公费医疗报销服务。办理毕业生医疗费用报销需提前预约，携带相关就医凭证、发票、病历等材料。我们将按照学校公费医疗管理规定为您办理报销手续。',
    location: '财务科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  32: {
    deptId: 32,
    deptName: '学生专属预约',
    deptDesc: '为在校学生提供公费医疗报销服务。学生在校期间享受公费医疗待遇，可报销门诊、急诊、住院等医疗费用。请提前预约并准备好相关材料，包括学生证、就诊病历、医疗费用发票等。',
    location: '财务科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  33: {
    deptId: 33,
    deptName: '教职工专属预约',
    deptDesc: '为学校教职工提供公费医疗报销服务。教职工享受公费医疗待遇，可报销本人及符合条件的家属医疗费用。办理报销需提前预约，携带工作证、就医凭证、医疗费用发票、处方等相关材料。',
    location: '财务科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  41: {
    deptId: 41,
    deptName: '体检复查预约',
    deptDesc: '为体检结果异常的人员提供复查服务。如果在常规体检中发现指标异常，可预约进行针对性复查。我们将根据体检报告安排相应的检查项目，并由专业医师进行解读和健康指导。',
    location: '体检科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  51: {
    deptId: 51,
    deptName: '口腔咨询门诊',
    deptDesc: '提供口腔健康咨询和初步检查服务。由经验丰富的口腔医师为您进行口腔健康检查，解答口腔疾病相关问题，提供口腔保健建议。适合想了解自己口腔健康状况或有口腔问题咨询需求的人群。',
    location: '口腔科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  52: {
    deptId: 52,
    deptName: '口腔科',
    deptDesc: '提供全面的口腔疾病诊疗服务，包括龋齿治疗、牙周病治疗、牙齿美容、牙齿矫正、拔牙、镶牙等。配备先进的口腔诊疗设备，由专业的口腔医师团队提供优质的医疗服务。',
    location: '口腔科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  61: {
    deptId: 61,
    deptName: '骨密度检测',
    deptDesc: '提供骨密度检测服务，用于评估骨骼健康状况和骨质疏松风险。采用先进的骨密度测定仪，检测快速、准确、无创。特别适合中老年人、绝经后女性、长期服用激素类药物人群进行骨质疏松筛查。',
    location: '护理科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  62: {
    deptId: 62,
    deptName: 'PICC换药',
    deptDesc: '提供PICC（经外周静脉置入中心静脉导管）维护服务。由专业的PICC护理团队进行导管维护、换药、冲管等操作，确保导管通畅和使用安全。为需要长期输液治疗的患者提供专业的PICC护理。',
    location: '护理科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  // 一级科室的详情
  1: {
    deptId: 1,
    deptName: '疫苗预约',
    deptDesc: '本科室提供各类疫苗接种服务，包括老年人免费流感疫苗、儿童常规疫苗、自费流感疫苗、麻风腮疫苗、甲肝乙肝疫苗等。我们拥有专业的接种团队和规范的接种流程，确保疫苗接种的安全性和有效性。',
    location: '预防保健科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  2: {
    deptId: 2,
    deptName: 'B超室',
    deptDesc: '本科室配备先进的彩色多普勒超声诊断仪，提供全身各部位超声检查服务。主要检查项目包括超声心动图、肝胆胰脾彩超、泌尿系统、妇科、甲状腺、乳腺等。由资深超声医师提供准确的影像诊断。',
    location: 'B超室',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  3: {
    deptId: 3,
    deptName: '公疗报销',
    deptDesc: '本科室负责办理学校师生员工的公费医疗报销业务。包括毕业生、在校学生、教职工的门诊、急诊、住院医疗费用报销。我们严格按照学校公费医疗管理规定，为广大师生提供便捷、高效的报销服务。',
    location: '财务科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  4: {
    deptId: 4,
    deptName: '体检科',
    deptDesc: '本科室提供全面的健康体检服务，包括入职体检、常规体检、专项体检等。配备先进的体检设备和专业的体检团队。对体检发现的异常指标，我们提供复查和健康指导服务，帮助您全面了解和管理自己的健康状况。',
    location: '体检科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  5: {
    deptId: 5,
    deptName: '口腔科',
    deptDesc: '本科室提供全方位的口腔医疗服务，包括口腔检查、龋齿治疗、牙周病治疗、口腔修复、口腔美容等。拥有先进的口腔诊疗设备和经验丰富的口腔医师团队，为您提供专业、安全、舒适的口腔医疗服务。',
    location: '口腔科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  6: {
    deptId: 6,
    deptName: '护理科',
    deptDesc: '本科室提供专业的护理服务和健康检查项目，包括骨密度检测、PICC导管维护等。我们拥有专业的护理团队，严格执行护理操作规范，为患者提供安全、优质、人性化的护理服务。',
    location: '护理科',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  }
};

// 根据科室ID获取详情
export const getMockDepartmentDetail = (deptId) => {
  return mockDepartmentDetails[deptId] || mockDepartmentDetails[11]; // 默认返回第一个
};

