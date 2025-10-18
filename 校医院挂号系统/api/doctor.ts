import { http } from '../utils/request';

// 虚拟数据：医生列表（与后端数据库一致）
const mockDoctors = [
  // 心内科医生 (dept_id: 11)
  {
    doctorId: 101,
    doctorName: '张建国',
    deptId: 11,
    title: '主任医师',
    specialty: '高血压、冠心病、心律失常',
    doctorDesc: '心血管内科专家，从事心血管疾病诊疗工作25年，擅长高血压、冠心病、心律失常、心力衰竭的诊断和治疗。发表学术论文30余篇，多次参加国家级学术会议。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 102,
    doctorName: '李慧敏',
    deptId: 11,
    title: '副主任医师',
    specialty: '心血管疾病、心电图诊断',
    doctorDesc: '心内科副主任医师，从事心血管疾病诊疗15年，擅长心电图解读、动态心电图分析。对心血管疾病的预防和康复有独到见解。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 神经内科医生 (dept_id: 12)
  {
    doctorId: 103,
    doctorName: '王晓峰',
    deptId: 12,
    title: '主任医师',
    specialty: '脑血管病、头痛、失眠',
    doctorDesc: '神经内科专家，从事神经系统疾病诊疗工作20年，擅长脑血管病、头痛、眩晕、失眠等疾病的诊断和治疗。曾在北京协和医院进修学习。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 104,
    doctorName: '刘晓丽',
    deptId: 12,
    title: '主治医师',
    specialty: '神经痛、面瘫、神经衰弱',
    doctorDesc: '神经内科主治医师，从事神经系统疾病诊疗10年，擅长神经痛、面神经麻痹、神经衰弱的治疗。对学生神经衰弱、焦虑症有丰富经验。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 消化内科医生 (dept_id: 13)
  {
    doctorId: 105,
    doctorName: '陈国强',
    deptId: 13,
    title: '主任医师',
    specialty: '胃肠疾病、消化系统疾病',
    doctorDesc: '消化内科专家，从事消化系统疾病诊疗25年，擅长胃炎、胃溃疡、肠胃炎、便秘等疾病的诊断和治疗。提供幽门螺杆菌检测和治疗方案。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 106,
    doctorName: '赵敏',
    deptId: 13,
    title: '副主任医师',
    specialty: '功能性消化不良、肠易激综合征',
    doctorDesc: '消化内科副主任医师，从事消化系统疾病诊疗15年，擅长功能性消化不良、肠易激综合征的诊治。对学生群体消化系统疾病有深入研究。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 呼吸内科医生 (dept_id: 14)
  {
    doctorId: 107,
    doctorName: '吴建华',
    deptId: 14,
    title: '主任医师',
    specialty: '呼吸系统疾病、哮喘、肺部感染',
    doctorDesc: '呼吸内科专家，从事呼吸系统疾病诊疗工作30年，擅长感冒、支气管炎、肺炎、哮喘等疾病的诊断和治疗。配备肺功能检测，提供专业呼吸康复指导。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 108,
    doctorName: '孙晓燕',
    deptId: 14,
    title: '副主任医师',
    specialty: '慢性咳嗽、上呼吸道感染',
    doctorDesc: '呼吸内科副主任医师，从事呼吸系统疾病诊疗18年，擅长慢性咳嗽、上呼吸道感染、慢阻肺的诊治。对学生常见呼吸道疾病有丰富经验。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 内分泌科医生 (dept_id: 15)
  {
    doctorId: 109,
    doctorName: '周明辉',
    deptId: 15,
    title: '主任医师',
    specialty: '糖尿病、甲状腺疾病',
    doctorDesc: '内分泌科专家，从事内分泌代谢疾病诊疗20年，擅长糖尿病、甲状腺疾病、肥胖症的诊断和治疗。提供血糖监测、饮食指导、运动方案。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 110,
    doctorName: '马丽萍',
    deptId: 15,
    title: '主治医师',
    specialty: '代谢综合征、营养指导',
    doctorDesc: '内分泌科主治医师，从事内分泌疾病诊疗12年，擅长代谢综合征、多囊卵巢综合征的诊治。持有营养师资格，提供专业营养指导。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 骨科医生 (dept_id: 21)
  {
    doctorId: 111,
    doctorName: '李强',
    deptId: 21,
    title: '主任医师',
    specialty: '骨折、关节炎、脊柱疾病',
    doctorDesc: '骨科专家，从事骨科临床工作25年，擅长骨折、关节炎、颈椎病、腰椎间盘突出的诊断和治疗。提供康复指导和物理治疗方案。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 112,
    doctorName: '张宏伟',
    deptId: 21,
    title: '副主任医师',
    specialty: '运动损伤、软组织损伤',
    doctorDesc: '骨科副主任医师，从事骨科临床工作15年，擅长运动损伤、软组织损伤的诊治。对学生群体运动损伤有丰富经验，提供康复训练指导。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 皮肤科医生 (dept_id: 22)
  {
    doctorId: 113,
    doctorName: '刘晓霞',
    deptId: 22,
    title: '主任医师',
    specialty: '皮炎、湿疹、真菌感染',
    doctorDesc: '皮肤科专家，从事皮肤病诊疗工作20年，擅长湿疹、皮炎、荨麻疹、痤疮、真菌感染等疾病的诊断和治疗。对学生皮肤问题有专业见解。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 114,
    doctorName: '王芳',
    deptId: 22,
    title: '主治医师',
    specialty: '痤疮、过敏性皮肤病',
    doctorDesc: '皮肤科主治医师，从事皮肤病诊疗10年，擅长痤疮、过敏性皮肤病的治疗。对青少年痤疮治疗有丰富经验，提供个性化治疗方案。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 普通外科医生 (dept_id: 23)
  {
    doctorId: 115,
    doctorName: '陈建军',
    deptId: 23,
    title: '主任医师',
    specialty: '痔疮、胆囊炎、阑尾炎',
    doctorDesc: '普通外科专家，从事外科临床工作25年，擅长痔疮、胆囊炎、阑尾炎、疝气等疾病的诊断和治疗。提供小型外科手术服务。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 116,
    doctorName: '杨明',
    deptId: 23,
    title: '副主任医师',
    specialty: '外科常见病、小手术',
    doctorDesc: '普通外科副主任医师，从事外科临床工作18年，擅长外科常见病的诊治和小型手术。服务细致周到，深受患者好评。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 外伤处理医生 (dept_id: 24)
  {
    doctorId: 117,
    doctorName: '赵勇',
    deptId: 24,
    title: '主治医师',
    specialty: '外伤急救、伤口处理',
    doctorDesc: '急诊外科主治医师，从事外伤急救工作12年，擅长各类外伤、切割伤、扭伤、挫伤的处理。提供专业的伤口清创、缝合、包扎服务。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 118,
    doctorName: '李静',
    deptId: 24,
    title: '主治医师',
    specialty: '烧烫伤、运动损伤',
    doctorDesc: '急诊外科主治医师，从事外伤处理工作10年，擅长烧烫伤、运动损伤的处理。对学生常见外伤有丰富经验，提供及时有效的治疗。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 预防保健科医生
  {
    doctorId: 119,
    doctorName: '张春华',
    deptId: 31,
    title: '主任医师',
    specialty: '流感预防与治疗、呼吸系统疾病',
    doctorDesc: '从事临床工作30余年，擅长老年人流感预防与治疗，对老年慢性病管理有丰富经验。获得多项医疗奖项，发表学术论文20余篇。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 121,
    doctorName: '李明',
    deptId: 32,
    title: '副主任医师',
    specialty: '儿童疫苗接种、儿童保健',
    doctorDesc: '儿科专家，从事儿童疫苗接种工作15年，对各类儿童疫苗接种有深入研究，曾在北京儿童医院进修学习。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 123,
    doctorName: '马强',
    deptId: 33,
    title: '主治医师',
    specialty: '流感预防、疫苗接种',
    doctorDesc: '公共卫生医师，从事疫苗接种工作10年，对流感病毒研究和预防有专业知识。耐心细致，严格执行接种规范。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 体检科医生
  {
    doctorId: 129,
    doctorName: '孙丽华',
    deptId: 41,
    title: '副主任医师',
    specialty: '健康体检、慢性病管理',
    doctorDesc: '预防医学专家，从事体检工作10余年，擅长体检报告解读、健康风险评估、慢性病预防指导。为多家单位提供健康管理服务。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 131,
    doctorName: '陈静雅',
    deptId: 43,
    title: '副主任医师',
    specialty: '健康管理、营养指导',
    doctorDesc: '健康管理专家，从事体检和健康管理工作15年，擅长体检报告解读、健康风险评估、慢病管理、营养咨询。持有健康管理师资格证书。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 133,
    doctorName: '张国良',
    deptId: 44,
    title: '副主任医师',
    specialty: '老年医学、慢性病管理',
    doctorDesc: '老年医学专家，从事老年保健工作18年，擅长老年人健康体检、慢性病筛查、健康指导。对老年常见病如高血压、糖尿病、心脑血管疾病有丰富的诊治经验。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 135,
    doctorName: '李雪梅',
    deptId: 45,
    title: '主治医师',
    specialty: '儿童青少年健康、生长发育评估',
    doctorDesc: '儿科主治医师，从事学生健康体检工作12年，擅长儿童青少年生长发育评估、视力保健、营养指导、心理健康咨询。对学生常见健康问题有深入研究。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 137,
    doctorName: '徐建军',
    deptId: 46,
    title: '主任医师',
    specialty: '健康体检、职业健康、慢性病管理',
    doctorDesc: '体检科主任，从事健康体检工作20余年，擅长教职工健康体检、职业健康评估、慢性病筛查。熟悉学校卫生工作，为学校教职工提供专业的健康管理服务。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 139,
    doctorName: '刘芳芳',
    deptId: 47,
    title: '主治医师',
    specialty: '学生健康、传染病筛查',
    doctorDesc: '从事学校卫生工作10年，擅长学生入学体检、传染病筛查、心理健康评估。熟悉大学生常见健康问题，为新生建立健康档案提供专业服务。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 口腔科医生
  {
    doctorId: 141,
    doctorName: '赵雪梅',
    deptId: 51,
    title: '副主任医师',
    specialty: '口腔常见病、口腔健康管理',
    doctorDesc: '口腔医学专家，从事口腔临床工作20年，擅长龋齿治疗、牙周病防治、口腔健康指导。服务态度温和，深受患者好评。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 143,
    doctorName: '陈伟',
    deptId: 52,
    title: '主治医师',
    specialty: '牙齿修复、牙齿美容',
    doctorDesc: '口腔修复专家，擅长各类牙齿修复、牙齿美容、烤瓷牙制作等。引进先进口腔诊疗技术，为患者提供高质量口腔医疗服务。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // B超室医生
  {
    doctorId: 145,
    doctorName: '王芳',
    deptId: 61,
    title: '主治医师',
    specialty: '心脏超声、血管超声',
    doctorDesc: '超声医学专家，擅长心脏超声检查和诊断，对心血管疾病超声诊断有丰富经验。多次参加国内超声医学学术交流。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 147,
    doctorName: '刘建国',
    deptId: 62,
    title: '主任医师',
    specialty: '腹部超声、消化系统疾病诊断',
    doctorDesc: '从事超声诊断工作25年，擅长肝胆胰脾等腹部器官超声检查，对消化系统疾病诊断准确率高。曾获得市级优秀医师称号。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 149,
    doctorName: '李晓红',
    deptId: 63,
    title: '主治医师',
    specialty: '甲状腺超声、乳腺超声、妇科超声',
    doctorDesc: '超声医学专家，从事超声诊断工作12年。擅长甲状腺、乳腺、妇科等部位超声检查，诊断准确，服务专业。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 护理科医生
  {
    doctorId: 151,
    doctorName: '周敏',
    deptId: 71,
    title: '主管护师',
    specialty: '骨密度检测、骨质疏松预防',
    doctorDesc: '专业护理人员，从事骨密度检测工作8年，对骨质疏松预防和健康指导有深入研究。服务细致周到，善于沟通。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 153,
    doctorName: '吴静',
    deptId: 72,
    title: '主管护师',
    specialty: 'PICC导管维护、静脉输液治疗',
    doctorDesc: 'PICC专科护士，获得PICC置管及维护资质认证，从事静脉治疗护理工作12年，操作技术娴熟，护理质量高。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 公疗报销人员
  {
    doctorId: 155,
    doctorName: '周文静',
    deptId: 81,
    title: '财务主管',
    specialty: '公费医疗报销、毕业生医保政策',
    doctorDesc: '从事医保报销工作10年，熟悉各类医保政策和报销流程。为毕业生提供专业的医保报销指导，服务热情周到。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 157,
    doctorName: '张丽萍',
    deptId: 82,
    title: '财务专员',
    specialty: '学生医保报销、医保政策咨询',
    doctorDesc: '学生医保报销专员，工作认真负责。熟悉在校学生公费医疗政策，为学生提供便捷的报销服务和政策解读。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  {
    doctorId: 159,
    doctorName: '王国强',
    deptId: 83,
    title: '财务主任',
    specialty: '教职工医保报销、医保政策管理',
    doctorDesc: '资深医保管理专家，从事医保财务工作20余年。负责教职工公费医疗报销管理，熟悉各项医保政策和报销规定。',
    avatar: '/static/doctor.svg',
    isActive: 1
  }
];

/**
 * 获取所有在职医生列表
 * 使用虚拟数据
 */
export const getAllDoctors = async () => {
  await new Promise(resolve => setTimeout(resolve, 300));
  
  return {
    success: true,
    result: mockDoctors.filter(d => d.isActive === 1),
    message: '获取成功'
  };
};

/**
 * 根据科室ID获取医生列表
 * 使用虚拟数据
 */
export const getDoctorsByDeptId = async (deptId: number | string) => {
  await new Promise(resolve => setTimeout(resolve, 200));
  
  // 转换为数字进行比较
  const targetId = typeof deptId === 'string' ? parseInt(deptId) : deptId;
  
  const doctors = mockDoctors.filter(d => d.deptId === targetId && d.isActive === 1);
  
  return {
    success: true,
    result: doctors,
    message: '获取成功'
  };
};

/**
 * 获取医生详情
 * 使用虚拟数据
 */
export const getDoctorDetail = async (doctorId: number | string) => {
  await new Promise(resolve => setTimeout(resolve, 200));
  
  // 转换为数字进行比较
  const targetId = typeof doctorId === 'string' ? parseInt(doctorId) : doctorId;
  
  const doctor = mockDoctors.find(d => d.doctorId === targetId);
  
  if (doctor) {
    return {
      success: true,
      result: doctor,
      message: '获取成功'
    };
  } else {
    return {
      success: false,
      result: null,
      message: '医生不存在'
    };
  }
};

/**
 * 搜索医生
 * 使用虚拟数据
 */
export const searchDoctors = async (keyword: string) => {
  await new Promise(resolve => setTimeout(resolve, 200));
  
  const results = mockDoctors.filter(d => 
    d.isActive === 1 && (
      d.doctorName.includes(keyword) ||
      d.specialty.includes(keyword) ||
      d.title.includes(keyword)
    )
  );
  
  return {
    success: true,
    result: results,
    message: '搜索成功'
  };
};
