-- ===================================================
-- 校医院挂号系统 - 科室和医生数据初始化脚本
-- 生成时间: 2024-01-01
-- 说明: 包含所有一级科室、二级科室和医生信息
-- ===================================================

-- 清空科室和医生数据
SET FOREIGN_KEY_CHECKS = 0;  -- 暂时关闭外键检查

TRUNCATE TABLE doctor;       -- 清空医生表
TRUNCATE TABLE department;   -- 清空科室表
DELETE FROM hos_user WHERE user_type = 2;  -- 删除医生账号

SET FOREIGN_KEY_CHECKS = 1;  -- 恢复外键检查
USE hospital;

-- 清空现有数据（谨慎操作）
-- TRUNCATE TABLE doctor;
-- TRUNCATE TABLE department;

-- ===================================================
-- 插入科室数据
-- ===================================================

-- 一级科室：内科
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) 
VALUES (1, '内科', '内科常见疾病诊疗', NULL, 1, '内科门诊', NOW(), NOW());

-- 内科二级科室
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) VALUES
(11, '心内科', '心脏疾病、高血压、心律失常诊疗', 1, 2, '内科门诊', NOW(), NOW()),
(12, '神经内科', '头痛、失眠、神经痛、脑血管疾病诊疗', 1, 2, '内科门诊', NOW(), NOW()),
(13, '消化内科', '胃痛、腹泻、肠胃炎等消化系统疾病', 1, 2, '内科门诊', NOW(), NOW()),
(14, '呼吸内科', '咳嗽、哮喘、肺部疾病诊疗', 1, 2, '内科门诊', NOW(), NOW()),
(15, '内分泌科', '糖尿病、甲状腺疾病、代谢性疾病', 1, 2, '内科门诊', NOW(), NOW());

-- 一级科室：外科
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) 
VALUES (2, '外科', '外科常见疾病诊疗', NULL, 1, '外科门诊', NOW(), NOW());

-- 外科二级科室
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) VALUES
(21, '骨科', '骨折、关节炎、颈腰椎病诊疗', 2, 2, '外科门诊', NOW(), NOW()),
(22, '皮肤科', '皮炎、湿疹、皮肤病诊疗', 2, 2, '外科门诊', NOW(), NOW()),
(23, '普通外科', '痔疮、胆囊炎、阑尾炎等外科疾病', 2, 2, '外科门诊', NOW(), NOW()),
(24, '外伤处理', '外伤、扭伤、创伤处理', 2, 2, '外科门诊', NOW(), NOW());

-- 一级科室：预防保健科
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) 
VALUES (3, '预防保健科', '疫苗接种与健康咨询', NULL, 1, '预防保健科', NOW(), NOW());

-- 预防保健科二级科室
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) VALUES
(31, '老年人免费流感疫苗预约', '65岁以上老年人免费流感疫苗接种', 3, 2, '预防保健科', NOW(), NOW()),
(32, '儿童接种疫苗预约', '儿童常规疫苗接种服务', 3, 2, '预防保健科', NOW(), NOW()),
(33, '自费流感疫苗预约', '自费流感疫苗接种服务', 3, 2, '预防保健科', NOW(), NOW()),
(34, '麻风腮疫苗', '麻疹、风疹、腮腺炎联合疫苗', 3, 2, '预防保健科', NOW(), NOW()),
(35, '甲肝、乙肝疫苗接种', '甲型肝炎、乙型肝炎疫苗接种', 3, 2, '预防保健科', NOW(), NOW());

-- 一级科室：体检科
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) 
VALUES (4, '体检科', '健康体检服务', NULL, 1, '体检中心', NOW(), NOW());

-- 体检科二级科室
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) VALUES
(41, '体检复查预约', '体检结果复查服务', 4, 2, '体检中心2楼', NOW(), NOW()),
(43, '常规体检预约', '教职工及家属健康体检服务', 4, 2, '体检中心1楼', NOW(), NOW()),
(44, '老年人体检预约', '65岁以上老年人免费体检', 4, 2, '体检中心2楼', NOW(), NOW()),
(45, '学生体检预约', '在校学生健康体检服务', 4, 2, '体检中心1楼', NOW(), NOW()),
(46, '教职工体检预约', '教职工年度健康体检', 4, 2, '体检中心1楼', NOW(), NOW()),
(47, '新生入学体检', '新生入学健康检查', 4, 2, '体检中心1楼', NOW(), NOW());

-- 一级科室：口腔科
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) 
VALUES (5, '口腔科', '口腔疾病诊疗', NULL, 1, '口腔科', NOW(), NOW());

-- 口腔科二级科室
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) VALUES
(51, '口腔咨询门诊', '口腔健康咨询和初步检查', 5, 2, '口腔科', NOW(), NOW()),
(52, '口腔治疗', '龋齿治疗、牙周病、口腔修复', 5, 2, '口腔科', NOW(), NOW());

-- 一级科室：B超室
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) 
VALUES (6, 'B超室', '超声检查服务', NULL, 1, 'B超室', NOW(), NOW());

-- B超室二级科室
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) VALUES
(61, '超声心动图', '心脏超声检查', 6, 2, 'B超室', NOW(), NOW()),
(62, '肝胆胰脾彩超', '腹部器官彩色超声检查', 6, 2, 'B超室', NOW(), NOW()),
(63, '其他项目彩超', '其他部位彩色超声检查', 6, 2, 'B超室', NOW(), NOW());

-- 一级科室：护理科
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) 
VALUES (7, '护理科', '护理服务', NULL, 1, '护理科', NOW(), NOW());

-- 护理科二级科室
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) VALUES
(71, '骨密度检测', '骨密度检查服务', 7, 2, '护理科', NOW(), NOW()),
(72, 'PICC换药', 'PICC导管维护服务', 7, 2, '护理科', NOW(), NOW());

-- 一级科室：公疗报销
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) 
VALUES (8, '公疗报销', '公费医疗报销服务', NULL, 1, '财务科', NOW(), NOW());

-- 公疗报销二级科室
INSERT INTO department (dept_id, dept_name, dept_desc, parent_dept_id, dept_level, location, create_time, update_time) VALUES
(81, '毕业生公疗报销预约', '毕业生公费医疗报销服务', 8, 2, '财务科', NOW(), NOW()),
(82, '学生专属预约', '在校学生公费医疗报销', 8, 2, '财务科', NOW(), NOW()),
(83, '教职工专属预约', '教职工公费医疗报销', 8, 2, '财务科', NOW(), NOW());

-- ===================================================
-- 插入用户数据（医生账号）
-- ===================================================
-- 注意：密码统一使用 MD5('123456') = 'e10adc3949ba59abbe56e057f20f883e'
-- status: 1-正常
INSERT INTO hos_user (user_id, user_account, user_password, user_type, id_card, phone, email, status, create_time, update_time) VALUES
(201, 'doctor_001', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000001', NULL, 1, NOW(), NOW()),
(202, 'doctor_002', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000002', NULL, 1, NOW(), NOW()),
(203, 'doctor_003', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000003', NULL, 1, NOW(), NOW()),
(204, 'doctor_004', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000004', NULL, 1, NOW(), NOW()),
(205, 'doctor_005', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000005', NULL, 1, NOW(), NOW()),
(206, 'doctor_006', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000006', NULL, 1, NOW(), NOW()),
(207, 'doctor_007', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000007', NULL, 1, NOW(), NOW()),
(208, 'doctor_008', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000008', NULL, 1, NOW(), NOW()),
(209, 'doctor_009', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000009', NULL, 1, NOW(), NOW()),
(210, 'doctor_010', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000010', NULL, 1, NOW(), NOW()),
(211, 'doctor_011', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000011', NULL, 1, NOW(), NOW()),
(212, 'doctor_012', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000012', NULL, 1, NOW(), NOW()),
(213, 'doctor_013', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000013', NULL, 1, NOW(), NOW()),
(214, 'doctor_014', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000014', NULL, 1, NOW(), NOW()),
(215, 'doctor_015', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000015', NULL, 1, NOW(), NOW()),
(216, 'doctor_016', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000016', NULL, 1, NOW(), NOW()),
(217, 'doctor_017', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000017', NULL, 1, NOW(), NOW()),
(218, 'doctor_018', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000018', NULL, 1, NOW(), NOW()),
(219, 'doctor_019', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000019', NULL, 1, NOW(), NOW()),
(220, 'doctor_020', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000020', NULL, 1, NOW(), NOW()),
(221, 'doctor_021', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000021', NULL, 1, NOW(), NOW()),
(222, 'doctor_022', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000022', NULL, 1, NOW(), NOW()),
(223, 'doctor_023', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000023', NULL, 1, NOW(), NOW()),
(224, 'doctor_024', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000024', NULL, 1, NOW(), NOW()),
(225, 'doctor_025', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000025', NULL, 1, NOW(), NOW()),
(226, 'doctor_026', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000026', NULL, 1, NOW(), NOW()),
(227, 'doctor_027', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000027', NULL, 1, NOW(), NOW()),
(228, 'doctor_028', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000028', NULL, 1, NOW(), NOW()),
(229, 'doctor_029', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000029', NULL, 1, NOW(), NOW()),
(230, 'doctor_030', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000030', NULL, 1, NOW(), NOW()),
(231, 'doctor_031', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000031', NULL, 1, NOW(), NOW()),
(232, 'doctor_032', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000032', NULL, 1, NOW(), NOW()),
(233, 'doctor_033', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000033', NULL, 1, NOW(), NOW()),
(234, 'doctor_034', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000034', NULL, 1, NOW(), NOW()),
(235, 'doctor_035', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000035', NULL, 1, NOW(), NOW()),
(236, 'doctor_036', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000036', NULL, 1, NOW(), NOW()),
(237, 'doctor_037', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000037', NULL, 1, NOW(), NOW()),
(238, 'doctor_038', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000038', NULL, 1, NOW(), NOW()),
(239, 'doctor_039', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000039', NULL, 1, NOW(), NOW()),
(240, 'doctor_040', 'e10adc3949ba59abbe56e057f20f883e', 2, NULL, '13800000040', NULL, 1, NOW(), NOW());

-- ===================================================
-- 插入医生数据
-- ===================================================

-- 心内科医生 (dept_id: 11)
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(101, 201, 11, '张建国', '主任医师', '高血压、冠心病、心律失常', '心血管内科专家，从事心血管疾病诊疗工作25年，擅长高血压、冠心病、心律失常、心力衰竭的诊断和治疗。发表学术论文30余篇，多次参加国家级学术会议。', '/static/doctor.svg', 1, 2),
(102, 202, 11, '李慧敏', '副主任医师', '心血管疾病、心电图诊断', '心内科副主任医师，从事心血管疾病诊疗15年，擅长心电图解读、动态心电图分析。对心血管疾病的预防和康复有独到见解。', '/static/doctor.svg', 1, 2);

-- 神经内科医生 (dept_id: 12)
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(103, 203, 12, '王晓峰', '主任医师', '脑血管病、头痛、失眠', '神经内科专家，从事神经系统疾病诊疗工作20年，擅长脑血管病、头痛、眩晕、失眠等疾病的诊断和治疗。曾在北京协和医院进修学习。', '/static/doctor.svg', 1, 2),
(104, 204, 12, '刘晓丽', '主治医师', '神经痛、面瘫、神经衰弱', '神经内科主治医师，从事神经系统疾病诊疗10年，擅长神经痛、面神经麻痹、神经衰弱的治疗。对学生神经衰弱、焦虑症有丰富经验。', '/static/doctor.svg', 1, 2);

-- 消化内科医生 (dept_id: 13)
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(105, 205, 13, '陈国强', '主任医师', '胃肠疾病、消化系统疾病', '消化内科专家，从事消化系统疾病诊疗25年，擅长胃炎、胃溃疡、肠胃炎、便秘等疾病的诊断和治疗。提供幽门螺杆菌检测和治疗方案。', '/static/doctor.svg', 1, 2),
(106, 206, 13, '赵敏', '副主任医师', '功能性消化不良、肠易激综合征', '消化内科副主任医师，从事消化系统疾病诊疗15年，擅长功能性消化不良、肠易激综合征的诊治。对学生群体消化系统疾病有深入研究。', '/static/doctor.svg', 1, 2);

-- 呼吸内科医生 (dept_id: 14)
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(107, 207, 14, '吴建华', '主任医师', '呼吸系统疾病、哮喘、肺部感染', '呼吸内科专家，从事呼吸系统疾病诊疗工作30年，擅长感冒、支气管炎、肺炎、哮喘等疾病的诊断和治疗。配备肺功能检测，提供专业呼吸康复指导。', '/static/doctor.svg', 1, 2),
(108, 208, 14, '孙晓燕', '副主任医师', '慢性咳嗽、上呼吸道感染', '呼吸内科副主任医师，从事呼吸系统疾病诊疗18年，擅长慢性咳嗽、上呼吸道感染、慢阻肺的诊治。对学生常见呼吸道疾病有丰富经验。', '/static/doctor.svg', 1, 2);

-- 内分泌科医生 (dept_id: 15)
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(109, 209, 15, '周明辉', '主任医师', '糖尿病、甲状腺疾病', '内分泌科专家，从事内分泌代谢疾病诊疗20年，擅长糖尿病、甲状腺疾病、肥胖症的诊断和治疗。提供血糖监测、饮食指导、运动方案。', '/static/doctor.svg', 1, 2),
(110, 210, 15, '马丽萍', '主治医师', '代谢综合征、营养指导', '内分泌科主治医师，从事内分泌疾病诊疗12年，擅长代谢综合征、多囊卵巢综合征的诊治。持有营养师资格，提供专业营养指导。', '/static/doctor.svg', 1, 2);

-- 骨科医生 (dept_id: 21)
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(111, 211, 21, '李强', '主任医师', '骨折、关节炎、脊柱疾病', '骨科专家，从事骨科临床工作25年，擅长骨折、关节炎、颈椎病、腰椎间盘突出的诊断和治疗。提供康复指导和物理治疗方案。', '/static/doctor.svg', 1, 2),
(112, 212, 21, '张宏伟', '副主任医师', '运动损伤、软组织损伤', '骨科副主任医师，从事骨科临床工作15年，擅长运动损伤、软组织损伤的诊治。对学生群体运动损伤有丰富经验，提供康复训练指导。', '/static/doctor.svg', 1, 2);

-- 皮肤科医生 (dept_id: 22)
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(113, 213, 22, '刘晓霞', '主任医师', '皮炎、湿疹、真菌感染', '皮肤科专家，从事皮肤病诊疗工作20年，擅长湿疹、皮炎、荨麻疹、痤疮、真菌感染等疾病的诊断和治疗。对学生皮肤问题有专业见解。', '/static/doctor.svg', 1, 2),
(114, 214, 22, '王芳', '主治医师', '痤疮、过敏性皮肤病', '皮肤科主治医师，从事皮肤病诊疗10年，擅长痤疮、过敏性皮肤病的治疗。对青少年痤疮治疗有丰富经验，提供个性化治疗方案。', '/static/doctor.svg', 1, 2);

-- 普通外科医生 (dept_id: 23)
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(115, 215, 23, '陈建军', '主任医师', '痔疮、胆囊炎、阑尾炎', '普通外科专家，从事外科临床工作25年，擅长痔疮、胆囊炎、阑尾炎、疝气等疾病的诊断和治疗。提供小型外科手术服务。', '/static/doctor.svg', 1, 2),
(116, 216, 23, '杨明', '副主任医师', '外科常见病、小手术', '普通外科副主任医师，从事外科临床工作18年，擅长外科常见病的诊治和小型手术。服务细致周到，深受患者好评。', '/static/doctor.svg', 1, 2);

-- 外伤处理医生 (dept_id: 24)
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(117, 217, 24, '赵勇', '主治医师', '外伤急救、伤口处理', '急诊外科主治医师，从事外伤急救工作12年，擅长各类外伤、切割伤、扭伤、挫伤的处理。提供专业的伤口清创、缝合、包扎服务。', '/static/doctor.svg', 1, 2),
(118, 218, 24, '李静', '主治医师', '烧烫伤、运动损伤', '急诊外科主治医师，从事外伤处理工作10年，擅长烧烫伤、运动损伤的处理。对学生常见外伤有丰富经验，提供及时有效的治疗。', '/static/doctor.svg', 1, 2);

-- 预防保健科医生（部分代表性医生）
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(119, 219, 31, '张春华', '主任医师', '流感预防与治疗、呼吸系统疾病', '从事临床工作30余年，擅长老年人流感预防与治疗，对老年慢性病管理有丰富经验。获得多项医疗奖项，发表学术论文20余篇。', '/static/doctor.svg', 1, 2),
(121, 221, 32, '李明', '副主任医师', '儿童疫苗接种、儿童保健', '儿科专家，从事儿童疫苗接种工作15年，对各类儿童疫苗接种有深入研究，曾在北京儿童医院进修学习。', '/static/doctor.svg', 1, 2),
(123, 223, 33, '马强', '主治医师', '流感预防、疫苗接种', '公共卫生医师，从事疫苗接种工作10年，对流感病毒研究和预防有专业知识。耐心细致，严格执行接种规范。', '/static/doctor.svg', 1, 2);

-- 体检科医生
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(129, 229, 41, '孙丽华', '副主任医师', '健康体检、慢性病管理', '预防医学专家，从事体检工作10余年，擅长体检报告解读、健康风险评估、慢性病预防指导。为多家单位提供健康管理服务。', '/static/doctor.svg', 1, 2),
(131, 231, 43, '陈静雅', '副主任医师', '健康管理、营养指导', '健康管理专家，从事体检和健康管理工作15年，擅长体检报告解读、健康风险评估、慢病管理、营养咨询。持有健康管理师资格证书。', '/static/doctor.svg', 1, 2),
(133, 233, 44, '张国良', '副主任医师', '老年医学、慢性病管理', '老年医学专家，从事老年保健工作18年，擅长老年人健康体检、慢性病筛查、健康指导。对老年常见病如高血压、糖尿病、心脑血管疾病有丰富的诊治经验。', '/static/doctor.svg', 1, 2),
(135, 235, 45, '李雪梅', '主治医师', '儿童青少年健康、生长发育评估', '儿科主治医师，从事学生健康体检工作12年，擅长儿童青少年生长发育评估、视力保健、营养指导、心理健康咨询。对学生常见健康问题有深入研究。', '/static/doctor.svg', 1, 2),
(137, 237, 46, '徐建军', '主任医师', '健康体检、职业健康、慢性病管理', '体检科主任，从事健康体检工作20余年，擅长教职工健康体检、职业健康评估、慢性病筛查。熟悉学校卫生工作，为学校教职工提供专业的健康管理服务。', '/static/doctor.svg', 1, 2),
(139, 239, 47, '刘芳芳', '主治医师', '学生健康、传染病筛查', '从事学校卫生工作10年，擅长学生入学体检、传染病筛查、心理健康评估。熟悉大学生常见健康问题，为新生建立健康档案提供专业服务。', '/static/doctor.svg', 1, 2);

-- 口腔科医生
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(141, 241, 51, '赵雪梅', '副主任医师', '口腔常见病、口腔健康管理', '口腔医学专家，从事口腔临床工作20年，擅长龋齿治疗、牙周病防治、口腔健康指导。服务态度温和，深受患者好评。', '/static/doctor.svg', 1, 2),
(143, 243, 52, '陈伟', '主治医师', '牙齿修复、牙齿美容', '口腔修复专家，擅长各类牙齿修复、牙齿美容、烤瓷牙制作等。引进先进口腔诊疗技术，为患者提供高质量口腔医疗服务。', '/static/doctor.svg', 1, 2);

-- B超室医生
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(145, 245, 61, '王芳', '主治医师', '心脏超声、血管超声', '超声医学专家，擅长心脏超声检查和诊断，对心血管疾病超声诊断有丰富经验。多次参加国内超声医学学术交流。', '/static/doctor.svg', 1, 2),
(147, 247, 62, '刘建国', '主任医师', '腹部超声、消化系统疾病诊断', '从事超声诊断工作25年，擅长肝胆胰脾等腹部器官超声检查，对消化系统疾病诊断准确率高。曾获得市级优秀医师称号。', '/static/doctor.svg', 1, 2),
(149, 249, 63, '李晓红', '主治医师', '甲状腺超声、乳腺超声、妇科超声', '超声医学专家，从事超声诊断工作12年。擅长甲状腺、乳腺、妇科等部位超声检查，诊断准确，服务专业。', '/static/doctor.svg', 1, 2);

-- 护理科医生
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(151, 251, 71, '周敏', '主管护师', '骨密度检测、骨质疏松预防', '专业护理人员，从事骨密度检测工作8年，对骨质疏松预防和健康指导有深入研究。服务细致周到，善于沟通。', '/static/doctor.svg', 1, 2),
(153, 253, 72, '吴静', '主管护师', 'PICC导管维护、静脉输液治疗', 'PICC专科护士，获得PICC置管及维护资质认证，从事静脉治疗护理工作12年，操作技术娴熟，护理质量高。', '/static/doctor.svg', 1, 2);

-- 公疗报销人员
INSERT INTO doctor (doctor_id, user_id, dept_id, doctor_name, title, specialty, doctor_desc, avatar, is_active, update_verify) VALUES
(155, 255, 81, '周文静', '财务主管', '公费医疗报销、毕业生医保政策', '从事医保报销工作10年，熟悉各类医保政策和报销流程。为毕业生提供专业的医保报销指导，服务热情周到。', '/static/doctor.svg', 1, 2),
(157, 257, 82, '张丽萍', '财务专员', '学生医保报销、医保政策咨询', '学生医保报销专员，工作认真负责。熟悉在校学生公费医疗政策，为学生提供便捷的报销服务和政策解读。', '/static/doctor.svg', 1, 2),
(159, 259, 83, '王国强', '财务主任', '教职工医保报销、医保政策管理', '资深医保管理专家，从事医保财务工作20余年。负责教职工公费医疗报销管理，熟悉各项医保政策和报销规定。', '/static/doctor.svg', 1, 2);

-- ===================================================
-- 数据插入完成
-- ===================================================

-- 查看插入结果
SELECT '科室数据插入完成' AS status;
SELECT COUNT(*) AS total_departments FROM department;
SELECT COUNT(*) AS total_doctors FROM doctor;

-- 查看一级科室
SELECT dept_id, dept_name, dept_desc FROM department WHERE dept_level = 1;

-- 查看二级科室数量
SELECT parent_dept_id, COUNT(*) AS count FROM department WHERE dept_level = 2 GROUP BY parent_dept_id;

-- 查看各科室医生数量
SELECT d.dept_name, COUNT(doc.doctor_id) AS doctor_count 
FROM department d 
LEFT JOIN doctor doc ON d.dept_id = doc.dept_id 
GROUP BY d.dept_id, d.dept_name
HAVING doctor_count > 0
ORDER BY d.dept_id;

