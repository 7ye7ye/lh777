// 虚拟数据：医生列表（与后端数据库一致）
export const mockDoctors = [
  // 心内科医生 (dept_id: 11)
  {
    doctorId: 101,
    doctorName: '张建国',
    deptId: 11,
    deptName: '心内科',
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
    deptName: '心内科',
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
    deptName: '神经内科',
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
    deptName: '神经内科',
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
    deptName: '消化内科',
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
    deptName: '消化内科',
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
    deptName: '呼吸内科',
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
    deptName: '呼吸内科',
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
    deptName: '内分泌科',
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
    deptName: '内分泌科',
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
    deptName: '骨科',
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
    deptName: '骨科',
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
    deptName: '皮肤科',
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
    deptName: '皮肤科',
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
    deptName: '普通外科',
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
    deptName: '普通外科',
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
    deptName: '外伤处理',
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
    deptName: '外伤处理',
    title: '主治医师',
    specialty: '烧烫伤、运动损伤',
    doctorDesc: '急诊外科主治医师，从事外伤处理工作10年，擅长烧烫伤、运动损伤的处理。对学生常见外伤有丰富经验，提供及时有效的治疗。',
    avatar: '/static/doctor.svg',
    isActive: 1
  },
  // 口腔科医生
  {
    doctorId: 141,
    doctorName: '赵雪梅',
    deptId: 51,
    deptName: '口腔咨询',
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
    deptName: '口腔治疗',
    title: '主治医师',
    specialty: '牙齿修复、牙齿美容',
    doctorDesc: '口腔修复专家，擅长各类牙齿修复、牙齿美容、烤瓷牙制作等。引进先进口腔诊疗技术，为患者提供高质量口腔医疗服务。',
    avatar: '/static/doctor.svg',
    isActive: 1
  }
];



