-- ============================================
-- 医生数据初始化脚本
-- 用于在hospital数据库中插入医生数据
-- 每个科室至少包含3个医生
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 重要说明：处理hospital.sql中已有医生数据
-- ============================================
-- hospital.sql中已有2个医生（user_id 4和5），使用dept_id=101（消化内科）
-- init_department.sql中消化内科的dept_id=13
-- 如果已经执行了init_department.sql重建科室表，需要更新这些医生的dept_id
-- ============================================

-- 更新hospital.sql中已有医生的科室ID（从101改为13，匹配init_department.sql）
UPDATE `doctor` SET `dept_id` = 13 WHERE `dept_id` = 101 AND `user_id` IN (4, 5);

-- 为已有医生设置头像
UPDATE `doctor` SET `avatar` = '/static/doctor.svg' 
WHERE `user_id` IN (4, 5) AND (`avatar` IS NULL OR `avatar` = '');

-- 更新doctor_schedule表中相关排班记录的科室ID
UPDATE `doctor_schedule` SET `dept_id` = 13 WHERE `dept_id` = 101;

-- ============================================
-- 注意：
-- 1. 此脚本假设hos_user表的AUTO_INCREMENT从10开始
-- 2. 如果已有其他数据，请根据实际情况调整user_id的起始值
-- 3. hospital.sql中已有医生用户：user_id 4, 5（其中4,5已用于doctor表）
-- 4. user_id 6也是医生用户，但可能尚未在doctor表中使用
-- ============================================

-- ============================================
-- 插入医生用户到hos_user表
-- ============================================

-- 消化内科医生 (dept_id = 13，hospital.sql中已有2个医生user_id 4和5，已通过UPDATE更新其dept_id，现在添加第3个)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_li_digest', 'hashed_password', 2, 'li_digest@hospital.com', 1, NOW(), NOW());

-- 呼吸内科医生 (dept_id = 11，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_wang_resp', 'hashed_password', 2, 'wang_resp@hospital.com', 1, NOW(), NOW()),
('doctor_li_resp', 'hashed_password', 2, 'li_resp@hospital.com', 1, NOW(), NOW()),
('doctor_zhang_resp', 'hashed_password', 2, 'zhang_resp@hospital.com', 1, NOW(), NOW());

-- 心内科医生 (dept_id = 12，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_chen_card', 'hashed_password', 2, 'chen_card@hospital.com', 1, NOW(), NOW()),
('doctor_wu_card', 'hashed_password', 2, 'wu_card@hospital.com', 1, NOW(), NOW()),
('doctor_zhao_card', 'hashed_password', 2, 'zhao_card@hospital.com', 1, NOW(), NOW());

-- 神经内科医生 (dept_id = 14，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_sun_neuro', 'hashed_password', 2, 'sun_neuro@hospital.com', 1, NOW(), NOW()),
('doctor_zhou_neuro', 'hashed_password', 2, 'zhou_neuro@hospital.com', 1, NOW(), NOW()),
('doctor_qian_neuro', 'hashed_password', 2, 'qian_neuro@hospital.com', 1, NOW(), NOW());

-- 内分泌科医生 (dept_id = 15，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_zheng_endo', 'hashed_password', 2, 'zheng_endo@hospital.com', 1, NOW(), NOW()),
('doctor_feng_endo', 'hashed_password', 2, 'feng_endo@hospital.com', 1, NOW(), NOW()),
('doctor_chen_endo', 'hashed_password', 2, 'chen_endo@hospital.com', 1, NOW(), NOW());

-- 骨科医生 (dept_id = 21，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_chu_ortho', 'hashed_password', 2, 'chu_ortho@hospital.com', 1, NOW(), NOW()),
('doctor_wei_ortho', 'hashed_password', 2, 'wei_ortho@hospital.com', 1, NOW(), NOW()),
('doctor_jiang_ortho', 'hashed_password', 2, 'jiang_ortho@hospital.com', 1, NOW(), NOW());

-- 皮肤科医生 (dept_id = 22，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_shen_derma', 'hashed_password', 2, 'shen_derma@hospital.com', 1, NOW(), NOW()),
('doctor_han_derma', 'hashed_password', 2, 'han_derma@hospital.com', 1, NOW(), NOW()),
('doctor_yang_derma', 'hashed_password', 2, 'yang_derma@hospital.com', 1, NOW(), NOW());

-- 普通外科医生 (dept_id = 23，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_han_surg', 'hashed_password', 2, 'han_surg@hospital.com', 1, NOW(), NOW()),
('doctor_dong_surg', 'hashed_password', 2, 'dong_surg@hospital.com', 1, NOW(), NOW()),
('doctor_cao_surg', 'hashed_password', 2, 'cao_surg@hospital.com', 1, NOW(), NOW());

-- 外伤处理医生 (dept_id = 24，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_xu_trauma', 'hashed_password', 2, 'xu_trauma@hospital.com', 1, NOW(), NOW()),
('doctor_he_trauma', 'hashed_password', 2, 'he_trauma@hospital.com', 1, NOW(), NOW()),
('doctor_luo_trauma', 'hashed_password', 2, 'luo_trauma@hospital.com', 1, NOW(), NOW());

-- 常规体检医生 (dept_id = 41，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_guan_exam', 'hashed_password', 2, 'guan_exam@hospital.com', 1, NOW(), NOW()),
('doctor_xie_exam', 'hashed_password', 2, 'xie_exam@hospital.com', 1, NOW(), NOW()),
('doctor_yan_exam', 'hashed_password', 2, 'yan_exam@hospital.com', 1, NOW(), NOW());

-- 预防保健科 - 儿童保健医生 (dept_id = 31，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_wang_child', 'hashed_password', 2, 'wang_child@hospital.com', 1, NOW(), NOW()),
('doctor_li_child', 'hashed_password', 2, 'li_child@hospital.com', 1, NOW(), NOW()),
('doctor_zhang_child', 'hashed_password', 2, 'zhang_child@hospital.com', 1, NOW(), NOW());

-- 预防保健科 - 妇女保健医生 (dept_id = 32，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_chen_women', 'hashed_password', 2, 'chen_women@hospital.com', 1, NOW(), NOW()),
('doctor_wu_women', 'hashed_password', 2, 'wu_women@hospital.com', 1, NOW(), NOW()),
('doctor_zhao_women', 'hashed_password', 2, 'zhao_women@hospital.com', 1, NOW(), NOW());

-- 预防保健科 - 老年人保健医生 (dept_id = 33，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_sun_elder', 'hashed_password', 2, 'sun_elder@hospital.com', 1, NOW(), NOW()),
('doctor_zhou_elder', 'hashed_password', 2, 'zhou_elder@hospital.com', 1, NOW(), NOW()),
('doctor_qian_elder', 'hashed_password', 2, 'qian_elder@hospital.com', 1, NOW(), NOW());

-- 体检科 - 入职体检医生 (dept_id = 42，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_zheng_join', 'hashed_password', 2, 'zheng_join@hospital.com', 1, NOW(), NOW()),
('doctor_feng_join', 'hashed_password', 2, 'feng_join@hospital.com', 1, NOW(), NOW()),
('doctor_chen2_join', 'hashed_password', 2, 'chen2_join@hospital.com', 1, NOW(), NOW());

-- 体检科 - 专项体检医生 (dept_id = 43，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_chu_special', 'hashed_password', 2, 'chu_special@hospital.com', 1, NOW(), NOW()),
('doctor_wei_special', 'hashed_password', 2, 'wei_special@hospital.com', 1, NOW(), NOW()),
('doctor_jiang_special', 'hashed_password', 2, 'jiang_special@hospital.com', 1, NOW(), NOW());

-- 口腔科 - 口腔内科医生 (dept_id = 51，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_shen_dent1', 'hashed_password', 2, 'shen_dent1@hospital.com', 1, NOW(), NOW()),
('doctor_han_dent1', 'hashed_password', 2, 'han_dent1@hospital.com', 1, NOW(), NOW()),
('doctor_yang_dent1', 'hashed_password', 2, 'yang_dent1@hospital.com', 1, NOW(), NOW());

-- 口腔科 - 口腔外科医生 (dept_id = 52，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_han_dent2', 'hashed_password', 2, 'han_dent2@hospital.com', 1, NOW(), NOW()),
('doctor_dong_dent2', 'hashed_password', 2, 'dong_dent2@hospital.com', 1, NOW(), NOW()),
('doctor_cao_dent2', 'hashed_password', 2, 'cao_dent2@hospital.com', 1, NOW(), NOW());

-- 口腔科 - 口腔修复医生 (dept_id = 53，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_xu_dent3', 'hashed_password', 2, 'xu_dent3@hospital.com', 1, NOW(), NOW()),
('doctor_he_dent3', 'hashed_password', 2, 'he_dent3@hospital.com', 1, NOW(), NOW()),
('doctor_luo_dent3', 'hashed_password', 2, 'luo_dent3@hospital.com', 1, NOW(), NOW());

-- B超室 - 腹部B超医生 (dept_id = 61，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_guan_abd', 'hashed_password', 2, 'guan_abd@hospital.com', 1, NOW(), NOW()),
('doctor_xie_abd', 'hashed_password', 2, 'xie_abd@hospital.com', 1, NOW(), NOW()),
('doctor_yan_abd', 'hashed_password', 2, 'yan_abd@hospital.com', 1, NOW(), NOW());

-- B超室 - 妇科B超医生 (dept_id = 62，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_lu_gynec', 'hashed_password', 2, 'lu_gynec@hospital.com', 1, NOW(), NOW()),
('doctor_shi_gynec', 'hashed_password', 2, 'shi_gynec@hospital.com', 1, NOW(), NOW()),
('doctor_gao_gynec', 'hashed_password', 2, 'gao_gynec@hospital.com', 1, NOW(), NOW());

-- B超室 - 心脏彩超医生 (dept_id = 63，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_lin_cardio', 'hashed_password', 2, 'lin_cardio@hospital.com', 1, NOW(), NOW()),
('doctor_huang_cardio', 'hashed_password', 2, 'huang_cardio@hospital.com', 1, NOW(), NOW()),
('doctor_xu2_cardio', 'hashed_password', 2, 'xu2_cardio@hospital.com', 1, NOW(), NOW());

-- 护理科 - 门诊护理医生 (dept_id = 71，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_ma_nurse1', 'hashed_password', 2, 'ma_nurse1@hospital.com', 1, NOW(), NOW()),
('doctor_kong_nurse1', 'hashed_password', 2, 'kong_nurse1@hospital.com', 1, NOW(), NOW()),
('doctor_cao2_nurse1', 'hashed_password', 2, 'cao2_nurse1@hospital.com', 1, NOW(), NOW());

-- 护理科 - 住院护理医生 (dept_id = 72，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_yan_nurse2', 'hashed_password', 2, 'yan_nurse2@hospital.com', 1, NOW(), NOW()),
('doctor_wang2_nurse2', 'hashed_password', 2, 'wang2_nurse2@hospital.com', 1, NOW(), NOW()),
('doctor_feng2_nurse2', 'hashed_password', 2, 'feng2_nurse2@hospital.com', 1, NOW(), NOW());

-- 护理科 - 社区护理医生 (dept_id = 73，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_zhang2_nurse3', 'hashed_password', 2, 'zhang2_nurse3@hospital.com', 1, NOW(), NOW()),
('doctor_chen3_nurse3', 'hashed_password', 2, 'chen3_nurse3@hospital.com', 1, NOW(), NOW()),
('doctor_wu2_nurse3', 'hashed_password', 2, 'wu2_nurse3@hospital.com', 1, NOW(), NOW());

-- 公疗报销 - 门诊报销医生 (dept_id = 81，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_liu_reimb1', 'hashed_password', 2, 'liu_reimb1@hospital.com', 1, NOW(), NOW()),
('doctor_chen4_reimb1', 'hashed_password', 2, 'chen4_reimb1@hospital.com', 1, NOW(), NOW()),
('doctor_wu3_reimb1', 'hashed_password', 2, 'wu3_reimb1@hospital.com', 1, NOW(), NOW());

-- 公疗报销 - 住院报销医生 (dept_id = 82，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_zhao2_reimb2', 'hashed_password', 2, 'zhao2_reimb2@hospital.com', 1, NOW(), NOW()),
('doctor_qian2_reimb2', 'hashed_password', 2, 'qian2_reimb2@hospital.com', 1, NOW(), NOW()),
('doctor_sun2_reimb2', 'hashed_password', 2, 'sun2_reimb2@hospital.com', 1, NOW(), NOW());

-- 公疗报销 - 慢病报销医生 (dept_id = 83，需要3个医生)
INSERT IGNORE INTO `hos_user` (`user_account`, `user_password`, `user_type`, `email`, `status`, `create_time`, `update_time`) VALUES
('doctor_zhou2_reimb3', 'hashed_password', 2, 'zhou2_reimb3@hospital.com', 1, NOW(), NOW()),
('doctor_wu4_reimb3', 'hashed_password', 2, 'wu4_reimb3@hospital.com', 1, NOW(), NOW()),
('doctor_zheng2_reimb3', 'hashed_password', 2, 'zheng2_reimb3@hospital.com', 1, NOW(), NOW());

-- ============================================
-- 插入医生信息到doctor表
-- ============================================
-- 注意：使用SELECT子查询通过user_account动态查找user_id，避免硬编码导致的ID冲突
-- 这样即使hos_user表的AUTO_INCREMENT不是预期值，也能正确插入数据

-- 消化内科医生 (dept_id = 13，hospital.sql中已有2个医生user_id 4和5，已通过UPDATE更新其dept_id，现在添加第3个)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 13, '副主任医师', '慢性胃炎、功能性消化不良', '从事消化内科临床工作多年，擅长慢性胃炎、功能性消化不良等疾病的诊治。', '/static/doctor.svg', 1, 2, '李医生'
FROM `hos_user` WHERE `user_account` = 'doctor_li_digest' LIMIT 1;

-- 呼吸内科医生 (dept_id = 11)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 11, '主任医师', '慢性咳嗽、支气管哮喘', '资深呼吸内科专家，擅长慢性咳嗽、支气管哮喘、慢性阻塞性肺疾病的诊治。', '/static/doctor.svg', 1, 2, '王医生'
FROM `hos_user` WHERE `user_account` = 'doctor_wang_resp' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 11, '主治医师', '上呼吸道感染、肺炎', '擅长上呼吸道感染、肺炎等常见呼吸系统疾病的诊疗。', '/static/doctor.svg', 1, 2, '李医生'
FROM `hos_user` WHERE `user_account` = 'doctor_li_resp' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 11, '副主任医师', '肺结节、胸腔积液', '擅长肺部影像学诊断，对肺结节、胸腔积液的诊治有丰富经验。', '/static/doctor.svg', 1, 2, '张医生'
FROM `hos_user` WHERE `user_account` = 'doctor_zhang_resp' LIMIT 1;

-- 心内科医生 (dept_id = 12)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 12, '主任医师', '冠心病、心律失常', '心内科资深专家，擅长冠心病、心律失常、心力衰竭的诊治。', '/static/doctor.svg', 1, 2, '陈医生'
FROM `hos_user` WHERE `user_account` = 'doctor_chen_card' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 12, '主治医师', '高血压、高血脂', '擅长高血压、高血脂等心血管常见疾病的诊疗和健康管理。', '/static/doctor.svg', 1, 2, '吴医生'
FROM `hos_user` WHERE `user_account` = 'doctor_wu_card' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 12, '副主任医师', '心悸、胸闷', '擅长心悸、胸闷等心血管疾病的诊断和治疗。', '/static/doctor.svg', 1, 2, '赵医生'
FROM `hos_user` WHERE `user_account` = 'doctor_zhao_card' LIMIT 1;

-- 神经内科医生 (dept_id = 14)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 14, '主任医师', '脑血管病、头痛', '神经内科专家，擅长脑血管病、头痛、头晕等疾病的诊治。', '/static/doctor.svg', 1, 2, '孙医生'
FROM `hos_user` WHERE `user_account` = 'doctor_sun_neuro' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 14, '主治医师', '失眠、焦虑', '擅长失眠、焦虑、抑郁等神经心理疾病的诊疗。', '/static/doctor.svg', 1, 2, '周医生'
FROM `hos_user` WHERE `user_account` = 'doctor_zhou_neuro' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 14, '副主任医师', '癫痫、帕金森', '对癫痫、帕金森病等神经系统疾病的诊治有丰富经验。', '/static/doctor.svg', 1, 2, '钱医生'
FROM `hos_user` WHERE `user_account` = 'doctor_qian_neuro' LIMIT 1;

-- 内分泌科医生 (dept_id = 15)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 15, '主任医师', '糖尿病、甲状腺疾病', '内分泌科资深专家，擅长糖尿病、甲状腺疾病的诊疗。', '/static/doctor.svg', 1, 2, '郑医生'
FROM `hos_user` WHERE `user_account` = 'doctor_zheng_endo' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 15, '主治医师', '肥胖症、代谢综合征', '擅长肥胖症、代谢综合征等代谢性疾病的诊治。', '/static/doctor.svg', 1, 2, '冯医生'
FROM `hos_user` WHERE `user_account` = 'doctor_feng_endo' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 15, '副主任医师', '骨质疏松、痛风', '擅长骨质疏松、痛风等内分泌相关疾病的诊疗。', '/static/doctor.svg', 1, 2, '陈医生'
FROM `hos_user` WHERE `user_account` = 'doctor_chen_endo' LIMIT 1;

-- 骨科医生 (dept_id = 21)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 21, '主任医师', '骨折、关节损伤', '骨科专家，擅长各种骨折、关节损伤的诊治。', '/static/doctor.svg', 1, 2, '褚医生'
FROM `hos_user` WHERE `user_account` = 'doctor_chu_ortho' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 21, '主治医师', '颈椎病、腰椎病', '擅长颈椎病、腰椎间盘突出等脊柱疾病的诊疗。', '/static/doctor.svg', 1, 2, '卫医生'
FROM `hos_user` WHERE `user_account` = 'doctor_wei_ortho' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 21, '副主任医师', '关节炎、运动损伤', '擅长关节炎、运动损伤等骨科疾病的诊治。', '/static/doctor.svg', 1, 2, '蒋医生'
FROM `hos_user` WHERE `user_account` = 'doctor_jiang_ortho' LIMIT 1;

-- 皮肤科医生 (dept_id = 22)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 22, '主任医师', '皮炎、湿疹', '皮肤科专家，擅长各类皮炎、湿疹等过敏性皮肤病的诊治。', '/static/doctor.svg', 1, 2, '沈医生'
FROM `hos_user` WHERE `user_account` = 'doctor_shen_derma' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 22, '主治医师', '痤疮、青春痘', '擅长痤疮、青春痘等常见皮肤病的诊疗。', '/static/doctor.svg', 1, 2, '韩医生'
FROM `hos_user` WHERE `user_account` = 'doctor_han_derma' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 22, '副主任医师', '银屑病、白癜风', '对银屑病、白癜风等顽固性皮肤病的诊治有丰富经验。', '/static/doctor.svg', 1, 2, '杨医生'
FROM `hos_user` WHERE `user_account` = 'doctor_yang_derma' LIMIT 1;

-- 普通外科医生 (dept_id = 23)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 23, '主任医师', '阑尾炎、胆囊炎', '普外科专家，擅长阑尾炎、胆囊炎等急腹症的诊治。', '/static/doctor.svg', 1, 2, '韩医生'
FROM `hos_user` WHERE `user_account` = 'doctor_han_surg' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 23, '主治医师', '痔疮、肛肠疾病', '擅长痔疮、肛肠疾病等普外科常见病的诊疗。', '/static/doctor.svg', 1, 2, '董医生'
FROM `hos_user` WHERE `user_account` = 'doctor_dong_surg' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 23, '副主任医师', '疝气、腹部包块', '擅长疝气、腹部包块等疾病的诊断和治疗。', '/static/doctor.svg', 1, 2, '曹医生'
FROM `hos_user` WHERE `user_account` = 'doctor_cao_surg' LIMIT 1;

-- 外伤处理医生 (dept_id = 24)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 24, '主任医师', '外伤急救、创伤处理', '外伤处理专家，擅长各种外伤、创伤的急救和处理。', '/static/doctor.svg', 1, 2, '徐医生'
FROM `hos_user` WHERE `user_account` = 'doctor_xu_trauma' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 24, '主治医师', '扭伤、拉伤', '擅长运动损伤、扭伤、拉伤等常见外伤的处理。', '/static/doctor.svg', 1, 2, '何医生'
FROM `hos_user` WHERE `user_account` = 'doctor_he_trauma' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 24, '副主任医师', '伤口处理、缝合', '擅长各种伤口的清创、缝合等处理。', '/static/doctor.svg', 1, 2, '罗医生'
FROM `hos_user` WHERE `user_account` = 'doctor_luo_trauma' LIMIT 1;

-- 常规体检医生 (dept_id = 41)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 41, '主任医师', '健康体检、健康管理', '体检科专家，擅长健康体检和健康管理。', '/static/check.svg', 1, 2, '管医生'
FROM `hos_user` WHERE `user_account` = 'doctor_guan_exam' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 41, '主治医师', '常规体检、体检报告解读', '擅长常规体检项目的实施和体检报告的解读。', '/static/check.svg', 1, 2, '谢医生'
FROM `hos_user` WHERE `user_account` = 'doctor_xie_exam' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`) 
SELECT `user_id`, 41, '副主任医师', '职业体检、健康咨询', '擅长职业健康体检和健康咨询。', '/static/check.svg', 1, 2, '严医生'
FROM `hos_user` WHERE `user_account` = 'doctor_yan_exam' LIMIT 1;

-- 预防保健科 - 儿童保健医生 (dept_id = 31)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 31, '主任医师', '儿童生长发育、疫苗接种', '从事儿童保健工作多年，擅长儿童生长发育评估、疫苗接种指导等。', '/static/images/health-bg.png', 1, 2, '王医生'
FROM `hos_user` WHERE `user_account` = 'doctor_wang_child' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 31, '主治医师', '儿童健康管理、预防接种', '擅长儿童健康管理、预防接种和常见病防治。', '/static/images/health-bg.png', 1, 2, '李医生'
FROM `hos_user` WHERE `user_account` = 'doctor_li_child' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 31, '副主任医师', '儿童营养、发育监测', '擅长儿童营养指导、生长发育监测和评估。', '/static/images/health-bg.png', 1, 2, '张医生'
FROM `hos_user` WHERE `user_account` = 'doctor_zhang_child' LIMIT 1;

-- 预防保健科 - 妇女保健医生 (dept_id = 32)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 32, '主任医师', '妇科检查、妇女健康管理', '从事妇女保健工作多年，擅长妇科常见病检查、妇女健康管理。', '/static/images/health-bg.png', 1, 2, '陈医生'
FROM `hos_user` WHERE `user_account` = 'doctor_chen_women' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 32, '主治医师', '孕前检查、围产期保健', '擅长孕前检查、围产期保健和产后康复指导。', '/static/images/health-bg.png', 1, 2, '吴医生'
FROM `hos_user` WHERE `user_account` = 'doctor_wu_women' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 32, '副主任医师', '乳腺检查、更年期保健', '擅长乳腺疾病筛查、更年期保健和激素调节。', '/static/images/health-bg.png', 1, 2, '赵医生'
FROM `hos_user` WHERE `user_account` = 'doctor_zhao_women' LIMIT 1;

-- 预防保健科 - 老年人保健医生 (dept_id = 33)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 33, '主任医师', '老年慢病管理、健康评估', '从事老年保健工作多年，擅长老年人慢性病管理和健康评估。', '/static/images/health-bg.png', 1, 2, '孙医生'
FROM `hos_user` WHERE `user_account` = 'doctor_sun_elder' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 33, '主治医师', '老年营养、用药指导', '擅长老年人营养指导和合理用药指导。', '/static/images/health-bg.png', 1, 2, '周医生'
FROM `hos_user` WHERE `user_account` = 'doctor_zhou_elder' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 33, '副主任医师', '老年康复、居家护理', '擅长老年人康复训练和居家护理指导。', '/static/images/health-bg.png', 1, 2, '钱医生'
FROM `hos_user` WHERE `user_account` = 'doctor_qian_elder' LIMIT 1;

-- 体检科 - 入职体检医生 (dept_id = 42)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 42, '主任医师', '入职体检、职业健康评估', '从事入职体检工作多年，擅长职业健康评估和体检报告解读。', '/static/check.svg', 1, 2, '郑医生'
FROM `hos_user` WHERE `user_account` = 'doctor_zheng_join' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 42, '主治医师', '体检咨询、健康指导', '擅长体检项目选择和健康指导。', '/static/check.svg', 1, 2, '冯医生'
FROM `hos_user` WHERE `user_account` = 'doctor_feng_join' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 42, '副主任医师', '体检报告解读、风险评估', '擅长体检报告详细解读和健康风险评估。', '/static/check.svg', 1, 2, '陈医生'
FROM `hos_user` WHERE `user_account` = 'doctor_chen2_join' LIMIT 1;

-- 体检科 - 专项体检医生 (dept_id = 43)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 43, '主任医师', '专项体检、疾病筛查', '从事专项体检工作多年，擅长各类疾病筛查和早期发现。', '/static/check.svg', 1, 2, '褚医生'
FROM `hos_user` WHERE `user_account` = 'doctor_chu_special' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 43, '主治医师', '体检套餐设计、健康管理', '擅长根据个人情况设计体检套餐和制定健康管理方案。', '/static/check.svg', 1, 2, '卫医生'
FROM `hos_user` WHERE `user_account` = 'doctor_wei_special' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 43, '副主任医师', '体检数据分析、健康建议', '擅长体检数据分析和个性化健康建议。', '/static/check.svg', 1, 2, '蒋医生'
FROM `hos_user` WHERE `user_account` = 'doctor_jiang_special' LIMIT 1;

-- 口腔科 - 口腔内科医生 (dept_id = 51)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 51, '主任医师', '牙体牙髓病、根管治疗', '从事口腔内科工作多年，擅长牙体牙髓病诊治和根管治疗。', '/static/doctor.svg', 1, 2, '沈医生'
FROM `hos_user` WHERE `user_account` = 'doctor_shen_dent1' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 51, '主治医师', '龋齿治疗、牙周病', '擅长龋齿充填治疗和牙周病基础治疗。', '/static/doctor.svg', 1, 2, '韩医生'
FROM `hos_user` WHERE `user_account` = 'doctor_han_dent1' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 51, '副主任医师', '儿童牙病、口腔预防', '擅长儿童龋齿防治和口腔预防保健。', '/static/doctor.svg', 1, 2, '杨医生'
FROM `hos_user` WHERE `user_account` = 'doctor_yang_dent1' LIMIT 1;

-- 口腔科 - 口腔外科医生 (dept_id = 52)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 52, '主任医师', '拔牙、口腔小手术', '从事口腔外科工作多年，擅长各类牙齿拔除和口腔小手术。', '/static/doctor.svg', 1, 2, '韩医生'
FROM `hos_user` WHERE `user_account` = 'doctor_han_dent2' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 52, '主治医师', '智齿拔除、外伤处理', '擅长智齿拔除和口腔外伤处理。', '/static/doctor.svg', 1, 2, '董医生'
FROM `hos_user` WHERE `user_account` = 'doctor_dong_dent2' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 52, '副主任医师', '口腔肿瘤、颌面外科', '擅长口腔肿瘤早期发现和颌面外科手术。', '/static/doctor.svg', 1, 2, '曹医生'
FROM `hos_user` WHERE `user_account` = 'doctor_cao_dent2' LIMIT 1;

-- 口腔科 - 口腔修复医生 (dept_id = 53)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 53, '主任医师', '烤瓷牙、全瓷牙', '从事口腔修复工作多年，擅长烤瓷牙、全瓷牙修复。', '/static/doctor.svg', 1, 2, '徐医生'
FROM `hos_user` WHERE `user_account` = 'doctor_xu_dent3' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 53, '主治医师', '活动义齿、固定义齿', '擅长活动义齿和固定义齿制作。', '/static/doctor.svg', 1, 2, '何医生'
FROM `hos_user` WHERE `user_account` = 'doctor_he_dent3' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 53, '副主任医师', '种植牙、美学修复', '擅长种植牙修复和美学修复。', '/static/doctor.svg', 1, 2, '罗医生'
FROM `hos_user` WHERE `user_account` = 'doctor_luo_dent3' LIMIT 1;

-- B超室 - 腹部B超医生 (dept_id = 61)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 61, '主任医师', '腹部脏器B超诊断', '从事B超诊断工作多年，擅长肝、胆、胰、脾、肾等腹部脏器疾病诊断。', '/static/check.svg', 1, 2, '管医生'
FROM `hos_user` WHERE `user_account` = 'doctor_guan_abd' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 61, '主治医师', '腹部肿瘤筛查', '擅长腹部肿瘤的B超筛查和早期发现。', '/static/check.svg', 1, 2, '谢医生'
FROM `hos_user` WHERE `user_account` = 'doctor_xie_abd' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 61, '副主任医师', '腹部急症诊断', '擅长腹部急症的B超快速诊断。', '/static/check.svg', 1, 2, '严医生'
FROM `hos_user` WHERE `user_account` = 'doctor_yan_abd' LIMIT 1;

-- B超室 - 妇科B超医生 (dept_id = 62)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 62, '主任医师', '妇科疾病B超诊断', '从事妇科B超诊断工作多年，擅长妇科疾病的B超诊断和鉴别。', '/static/check.svg', 1, 2, '卢医生'
FROM `hos_user` WHERE `user_account` = 'doctor_lu_gynec' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 62, '主治医师', '孕检B超、胎儿监测', '擅长孕检B超和胎儿生长发育监测。', '/static/check.svg', 1, 2, '石医生'
FROM `hos_user` WHERE `user_account` = 'doctor_shi_gynec' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 62, '副主任医师', '妇科肿瘤筛查', '擅长妇科肿瘤的B超筛查和早期诊断。', '/static/check.svg', 1, 2, '高医生'
FROM `hos_user` WHERE `user_account` = 'doctor_gao_gynec' LIMIT 1;

-- B超室 - 心脏彩超医生 (dept_id = 63)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 63, '主任医师', '心脏功能评估', '从事心脏彩超诊断工作多年，擅长心脏结构和功能的评估。', '/static/check.svg', 1, 2, '林医生'
FROM `hos_user` WHERE `user_account` = 'doctor_lin_cardio' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 63, '主治医师', '先天性心脏病诊断', '擅长先天性心脏病的彩超诊断。', '/static/check.svg', 1, 2, '黄医生'
FROM `hos_user` WHERE `user_account` = 'doctor_huang_cardio' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 63, '副主任医师', '心脏瓣膜病诊断', '擅长心脏瓣膜疾病的彩超诊断和评估。', '/static/check.svg', 1, 2, '徐医生'
FROM `hos_user` WHERE `user_account` = 'doctor_xu2_cardio' LIMIT 1;

-- 护理科 - 门诊护理医生 (dept_id = 71)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 71, '主任护师', '门诊护理、输液管理', '从事门诊护理工作多年，擅长门诊输液和护理管理。', '/static/doctor.svg', 1, 2, '马护士'
FROM `hos_user` WHERE `user_account` = 'doctor_ma_nurse1' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 71, '主管护师', '换药护理、伤口处理', '擅长各类伤口的换药和护理。', '/static/doctor.svg', 1, 2, '孔护士'
FROM `hos_user` WHERE `user_account` = 'doctor_kong_nurse1' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 71, '副主任护师', '护理咨询、健康教育', '擅长护理知识咨询和健康宣教。', '/static/doctor.svg', 1, 2, '曹护士'
FROM `hos_user` WHERE `user_account` = 'doctor_cao2_nurse1' LIMIT 1;

-- 护理科 - 住院护理医生 (dept_id = 72)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 72, '主任护师', '住院患者护理管理', '从事住院护理工作多年，擅长住院患者的整体护理管理。', '/static/doctor.svg', 1, 2, '严护士'
FROM `hos_user` WHERE `user_account` = 'doctor_yan_nurse2' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 72, '主管护师', '危重症护理', '擅长危重症患者的专业护理。', '/static/doctor.svg', 1, 2, '王护士'
FROM `hos_user` WHERE `user_account` = 'doctor_wang2_nurse2' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 72, '副主任护师', '康复护理、功能训练', '擅长患者康复护理和功能训练指导。', '/static/doctor.svg', 1, 2, '冯护士'
FROM `hos_user` WHERE `user_account` = 'doctor_feng2_nurse2' LIMIT 1;

-- 护理科 - 社区护理医生 (dept_id = 73)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 73, '主任护师', '家庭病床、上门护理', '从事社区护理工作多年，擅长家庭病床管理和上门护理服务。', '/static/doctor.svg', 1, 2, '张护士'
FROM `hos_user` WHERE `user_account` = 'doctor_zhang2_nurse3' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 73, '主管护师', '慢病随访、健康管理', '擅长慢性病患者的随访和健康管理。', '/static/doctor.svg', 1, 2, '陈护士'
FROM `hos_user` WHERE `user_account` = 'doctor_chen3_nurse3' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 73, '副主任护师', '护理培训、技术指导', '擅长社区护理人员的培训和技术指导。', '/static/doctor.svg', 1, 2, '吴护士'
FROM `hos_user` WHERE `user_account` = 'doctor_wu2_nurse3' LIMIT 1;

-- 公疗报销 - 门诊报销医生 (dept_id = 81)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 81, '报销专员', '门诊费用报销办理', '从事门诊报销工作多年，熟悉门诊费用报销流程和政策。', '/static/info_message.png', 1, 2, '刘专员'
FROM `hos_user` WHERE `user_account` = 'doctor_liu_reimb1' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 81, '报销专员', '报销材料审核', '擅长报销材料的审核和咨询指导。', '/static/info_message.png', 1, 2, '陈专员'
FROM `hos_user` WHERE `user_account` = 'doctor_chen4_reimb1' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 81, '报销专员', '报销政策咨询', '擅长报销政策解读和咨询解答。', '/static/info_message.png', 1, 2, '吴专员'
FROM `hos_user` WHERE `user_account` = 'doctor_wu3_reimb1' LIMIT 1;

-- 公疗报销 - 住院报销医生 (dept_id = 82)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 82, '报销专员', '住院费用报销办理', '从事住院报销工作多年，熟悉住院费用报销流程。', '/static/info_message.png', 1, 2, '赵专员'
FROM `hos_user` WHERE `user_account` = 'doctor_zhao2_reimb2' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 82, '报销专员', '大额医疗报销', '擅长大额医疗费用的报销办理。', '/static/info_message.png', 1, 2, '钱专员'
FROM `hos_user` WHERE `user_account` = 'doctor_qian2_reimb2' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 82, '报销专员', '报销材料整理', '擅长报销材料的整理和归档。', '/static/info_message.png', 1, 2, '孙专员'
FROM `hos_user` WHERE `user_account` = 'doctor_sun2_reimb2' LIMIT 1;

-- 公疗报销 - 慢病报销医生 (dept_id = 83)
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 83, '报销专员', '慢病费用报销办理', '从事慢病报销工作多年，熟悉慢性病报销政策和流程。', '/static/info_message.png', 1, 2, '周专员'
FROM `hos_user` WHERE `user_account` = 'doctor_zhou2_reimb3' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 83, '报销专员', '慢病认定办理', '擅长慢性病认定和报销办理。', '/static/info_message.png', 1, 2, '吴专员'
FROM `hos_user` WHERE `user_account` = 'doctor_wu4_reimb3' LIMIT 1;
INSERT IGNORE INTO `doctor` (`user_id`, `dept_id`, `title`, `specialty`, `doctor_desc`, `avatar`, `is_active`, `update_verify`, `doctor_name`)
SELECT `user_id`, 83, '报销专员', '慢病随访报销', '擅长慢性病患者的随访和报销服务。', '/static/info_message.png', 1, 2, '郑专员'
FROM `hos_user` WHERE `user_account` = 'doctor_zheng2_reimb3' LIMIT 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 说明：
-- 1. 本脚本会自动更新hospital.sql中已有医生的dept_id（从101改为13），确保与init_department.sql的科室ID匹配
-- 2. 本脚本为每个二级科室插入了至少3个医生
-- 3. 每个医生都需要先在hos_user表中创建用户记录（user_type=2）
-- 4. 然后在doctor表中创建医生信息记录
-- 5. 使用INSERT IGNORE和SELECT子查询，即使账号或医生数据已存在也不会报错，支持重复执行
--    通过user_account动态查找user_id，无需担心AUTO_INCREMENT值，避免了ID冲突问题
-- 6. 密码字段使用了占位符'hashed_password'，实际使用时请替换为加密后的密码
-- 7. 执行顺序：先执行init_department.sql初始化科室，再执行本脚本
-- 8. 根据init_department.sql，共有25个二级科室，本脚本为所有25个科室各插入3名医生：
--    内科系统：
--    - 呼吸内科 (11) - 3名
--    - 心内科 (12) - 3名
--    - 消化内科 (13) - hospital.sql中已有2个医生（user_id 4和5），已通过UPDATE更新其dept_id为13，本脚本新增1个，共3名
--    - 神经内科 (14) - 3名
--    - 内分泌科 (15) - 3名
--    外科系统：
--    - 骨科 (21) - 3名
--    - 皮肤科 (22) - 3名
--    - 普通外科 (23) - 3名
--    - 外伤处理 (24) - 3名
--    预防保健科：
--    - 儿童保健 (31) - 3名
--    - 妇女保健 (32) - 3名
--    - 老年人保健 (33) - 3名
--    体检科：
--    - 常规体检 (41) - 3名
--    - 入职体检 (42) - 3名
--    - 专项体检 (43) - 3名
--    口腔科：
--    - 口腔内科 (51) - 3名
--    - 口腔外科 (52) - 3名
--    - 口腔修复 (53) - 3名
--    B超室：
--    - 腹部B超 (61) - 3名
--    - 妇科B超 (62) - 3名
--    - 心脏彩超 (63) - 3名
--    护理科：
--    - 门诊护理 (71) - 3名
--    - 住院护理 (72) - 3名
--    - 社区护理 (73) - 3名
--    公疗报销：
--    - 门诊报销 (81) - 3名
--    - 住院报销 (82) - 3名
--    - 慢病报销 (83) - 3名
--    总计：25个科室 × 3名医生 = 75名医生
-- ============================================
