/*
 Navicat Premium Dump SQL

 Source Server         : ku
 Source Server Type    : MySQL
 Source Server Version : 90001 (9.0.1)
 Source Host           : localhost:3306
 Source Schema         : hospital

 Target Server Type    : MySQL
 Target Server Version : 90001 (9.0.1)
 File Encoding         : 65001

 Date: 26/09/2025 11:10:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `admin_id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员唯一标识',
  `user_id` bigint NOT NULL COMMENT '关联用户表的用户ID',
  `admin_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员登录账号',
  `admin_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密存储的密码',
  `admin_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员姓名',
  `admin_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员简介',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`admin_id`) USING BTREE,
  UNIQUE INDEX `admin_account`(`admin_account` ASC) USING BTREE,
  INDEX `admin_ibfk_1`(`user_id` ASC) USING BTREE,
  CONSTRAINT `admin_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `hos_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '科室唯一标识',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '科室名称（如“内科”“消化内科”）',
  `parent_dept_id` bigint NULL DEFAULT NULL COMMENT '父科室ID（顶级科室为0，如“内科”的父科室为0，“消化内科”父科室为内科ID）',
  `dept_level` tinyint NOT NULL COMMENT '科室级别（1-一级科室；2-二级科室）',
  `dept_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '科室简介（如“负责消化系统疾病诊疗”）',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '科室位置（如“门诊楼3层东侧”，用于导航扩展功能）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE,
  UNIQUE INDEX `dept_name`(`dept_name` ASC) USING BTREE,
  INDEX `parent_dept_id`(`parent_dept_id` ASC) USING BTREE,
  CONSTRAINT `department_ibfk_1` FOREIGN KEY (`parent_dept_id`) REFERENCES `department` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '科室表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of department
-- ----------------------------
-- 一级科室数据
INSERT INTO `department` VALUES (1, '疫苗预约', 0, 1, '疫苗接种服务', '预防保健科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (2, 'B超室', 0, 1, '超声检查服务', 'B超室', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (3, '公疗报销', 0, 1, '公费医疗报销服务', '财务科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (4, '体检科', 0, 1, '健康体检服务', '体检科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (5, '口腔科', 0, 1, '口腔疾病诊疗', '口腔科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (6, '护理科', 0, 1, '护理服务', '护理科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');

-- 二级科室数据
-- 疫苗预约子科室
INSERT INTO `department` VALUES (11, '老年人免费流感疫苗预约', 1, 2, '65岁以上老年人免费流感疫苗接种', '预防保健科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (12, '儿童接种疫苗预约', 1, 2, '儿童常规疫苗接种服务', '预防保健科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (13, '自费流感疫苗预约', 1, 2, '自费流感疫苗接种服务', '预防保健科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (14, '麻风腮疫苗', 1, 2, '麻疹、风疹、腮腺炎联合疫苗', '预防保健科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (15, '甲肝、乙肝疫苗接种', 1, 2, '甲型肝炎、乙型肝炎疫苗接种', '预防保健科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');

-- B超室子科室
INSERT INTO `department` VALUES (21, '超声心动图', 2, 2, '心脏超声检查', 'B超室', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (22, '肝胆胰脾彩超', 2, 2, '腹部器官彩色超声检查', 'B超室', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (23, '其他项目彩超', 2, 2, '其他部位彩色超声检查', 'B超室', '2024-01-01 10:00:00', '2024-01-01 10:00:00');

-- 公疗报销子科室
INSERT INTO `department` VALUES (31, '毕业生公疗报销预约', 3, 2, '毕业生公费医疗报销服务', '财务科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (32, '学生专属预约', 3, 2, '在校学生公费医疗报销', '财务科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (33, '教职工专属预约', 3, 2, '教职工公费医疗报销', '财务科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');

-- 体检科子科室
INSERT INTO `department` VALUES (41, '体检复查预约', 4, 2, '体检结果复查服务', '体检科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');

-- 口腔科子科室
INSERT INTO `department` VALUES (51, '口腔咨询门诊', 5, 2, '口腔健康咨询和初步检查', '口腔科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (52, '口腔科', 5, 2, '口腔疾病诊疗服务', '口腔科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');

-- 护理科子科室
INSERT INTO `department` VALUES (61, '骨密度检测', 6, 2, '骨密度检查服务', '护理科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO `department` VALUES (62, 'PICC换药', 6, 2, 'PICC导管维护服务', '护理科', '2024-01-01 10:00:00', '2024-01-01 10:00:00');

-- ----------------------------
-- Table structure for doctor
-- ----------------------------
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor`  (
  `doctor_id` bigint NOT NULL AUTO_INCREMENT COMMENT '医生唯一标识',
  `user_id` bigint NOT NULL COMMENT '关联用户表',
  `dept_id` bigint NOT NULL COMMENT '所属科室ID（二级科室，如“消化内科”）',
  `title` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '职称（如“主治医师”“主任医师”）',
  `specialty` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '擅长领域（如“胃炎、胃溃疡诊疗”）',
  `doctor_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '医生简介',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '医生头像URL',
  `is_active` tinyint NOT NULL COMMENT '出诊状态（0-暂停出诊；1-正常出诊）',
  `update_verify` tinyint NOT NULL COMMENT '信息修改审核状态（0-未提交修改；1-待审核；2-已通过；3-已驳回）',
  `doctor_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '医生姓名',
  PRIMARY KEY (`doctor_id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `dept_id`(`dept_id` ASC) USING BTREE,
  CONSTRAINT `doctor_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `hos_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `doctor_ibfk_2` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctor
-- ----------------------------

-- ----------------------------
-- Table structure for hos_user
-- ----------------------------
DROP TABLE IF EXISTS `hos_user`;
CREATE TABLE `hos_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一标识',
  `user_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号（患者：学号/工号/手机号；医生：管理员分配账号；管理员：固定账号）',
  `user_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密存储的密码（如MD5+盐值）',
  `user_type` tinyint NOT NULL COMMENT '用户类型（1-患者；2-医生；3-管理员）',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号（敏感信息，加密存储）',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号（用于接收就诊提醒）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱（可选通知渠道）',
  `status` tinyint NOT NULL COMMENT '账号状态（0-未激活；1-正常；2-禁用）',
  `create_time` datetime NOT NULL COMMENT '账号创建时间',
  `update_time` datetime NOT NULL COMMENT '账号更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `user_account`(`user_account` ASC) USING BTREE,
  UNIQUE INDEX `id_card`(`id_card` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hos_user
-- ----------------------------
INSERT INTO `hos_user` VALUES (2, '11111111', 'b090a8e2dd67f03005adeff82c6b80e7', 1, NULL, NULL, NULL, 1, '2025-09-23 19:53:32', '2025-09-23 19:53:32');

-- ----------------------------
-- Table structure for patient
-- ----------------------------
DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient`  (
  `patient_id` bigint NOT NULL AUTO_INCREMENT COMMENT '患者唯一标识',
  `user_id` bigint NOT NULL COMMENT '关联用户表',
  `patient_type` tinyint NOT NULL COMMENT '患者身份（1-学生；2-教师；3-职工）',
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号（学生用户必填）',
  `staff_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工号（教师/职工用户必填）',
  `birth_date` date NOT NULL COMMENT '出生日期',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '性别（男/女/未知）',
  `height` decimal(5, 2) NULL DEFAULT NULL COMMENT '身高（单位：cm）',
  `weight` decimal(5, 2) NULL DEFAULT NULL COMMENT '体重（单位：kg）',
  `blood_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '血型',
  `marital_status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '未婚' COMMENT '婚姻状况',
  `fertility_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生育情况',
  `present_illness` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '现病史',
  `past_illness` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '既往史',
  `family_illness` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '家族史',
  `allergy_history` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '无' COMMENT '过敏史',
  `id_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '身份证' COMMENT '证件类型',
  `id_number` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证件号码',
  `nation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '汉族' COMMENT '民族',
  `nationality` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '中国' COMMENT '国籍',
  `region` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在地区',
  `detailed_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细住址',
  `phone_number` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话号码',
  `home_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '家庭地址',
  `emergency_contact` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '紧急联系人姓名',
  `emergency_phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '紧急联系人电话',
  `medical_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '既往病史（可修改）',
  `identity_verify` tinyint NOT NULL COMMENT '身份认证状态（0-未审核；1-已通过；2-未通过）',
  `verify_time` datetime NULL DEFAULT NULL COMMENT '审核通过时间',
  `outpatient_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '门诊号',
  `hospitalization_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '住院号',
  `barcode_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '条形码信息',
  `qr_code_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '二维码信息',
  PRIMARY KEY (`patient_id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `student_id`(`student_id` ASC) USING BTREE,
  UNIQUE INDEX `staff_id`(`staff_id` ASC) USING BTREE,
  CONSTRAINT `patient_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `hos_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of patient
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for message_log
-- ----------------------------
DROP TABLE IF EXISTS `message_log`;
CREATE TABLE `message_log` (
  `message_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '消息主键ID',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接收消息的用户ID',
  `appointment_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关联的预约ID',
  `message_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息类型 (e.g. APPOINTMENT_SUCCESS)',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息卡片标题',
  `content` json NULL COMMENT '消息核心内容 (JSON格式)',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息创建时间',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读 (0-未读, 1-已读)',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_user_appointment`(`user_id`, `appointment_id`) USING BTREE COMMENT '用户和预约ID的联合索引，用于快速查询'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户消息记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message_log (测试数据)
-- ----------------------------

-- 场景一：用户 "武芷竹" (user_id: 'wuzhizhu_001') 的一次完整预约流程 (先成功，后取消)
-- 这两条消息共享同一个 appointment_id: 'APPOINTMENT_001'

-- 1. 预约成功消息
INSERT INTO `message_log` (`user_id`, `appointment_id`, `message_type`, `title`, `content`, `created_time`, `is_read`) VALUES (
'wuzhizhu_001', 
'APPOINTMENT_001', 
'APPOINTMENT_SUCCESS', 
'预约挂号成功提醒', 
'{
    "patient_card_no": "M017092997", 
    "patient_name": "武芷竹", 
    "doctor_name": "于泳", 
    "department_name": "眼科门诊(滑翔)", 
    "appointment_time": "2025-10-15 09:28", 
    "hospital_remark": "盛京医院南湖院区停车场10月1日凌晨0点开始阶梯式收费"
}', 
'2025-10-09 16:31:00', 
1
);

-- 2. 预约取消消息 (注意时间在预约成功之后)
INSERT INTO `message_log` (`user_id`, `appointment_id`, `message_type`, `title`, `content`, `created_time`, `is_read`) VALUES (
'wuzhizhu_001', 
'APPOINTMENT_001', 
'APPOINTMENT_CANCEL', 
'预约挂号取消通知', 
'{
    "doctor_name": "于泳", 
    "department_name": "眼科门诊(滑翔)", 
    "appointment_time": "2025-10-15 09:28", 
    "hospital_remark": "您已成功退号"
}', 
'2025-10-09 16:34:00', 
0
);


-- 场景二：用户 "李明" (user_id: 'li_ming_002') 的一次简单预约
-- 这条消息使用一个新的 appointment_id: 'APPOINTMENT_002'

-- 1. 预约成功消息
INSERT INTO `message_log` (`user_id`, `appointment_id`, `message_type`, `title`, `content`, `created_time`, `is_read`) VALUES (
'li_ming_002', 
'APPOINTMENT_002', 
'APPOINTMENT_SUCCESS', 
'预约挂号成功提醒', 
'{
    "patient_card_no": "M018334882", 
    "patient_name": "李明", 
    "doctor_name": "刘医生", 
    "department_name": "内科门诊", 
    "appointment_time": "2025-10-18 10:00", 
    "hospital_remark": "请携带有效身份证件就诊"
}', 
'2025-10-10 11:20:00', 
0
);

-- ----------------------------
-- Table structure for appointments
-- ----------------------------
DROP TABLE IF EXISTS `appointments`;
CREATE TABLE `appointments` (
                                `id` varchar(255) NOT NULL COMMENT '预约ID',
                                `qr_code_data` varchar(255) NULL DEFAULT NULL COMMENT '用于生成二维码的数据',
                                `serial_number` varchar(255) NULL DEFAULT NULL COMMENT '预约流水',
                                `patient_name` varchar(255) NULL DEFAULT NULL COMMENT '就诊人',
                                `hospital_address` varchar(255) NULL DEFAULT NULL COMMENT '医院地址',
                                `department_name` varchar(255) NULL DEFAULT NULL COMMENT '就诊科室',
                                `visit_location` varchar(255) NULL DEFAULT NULL COMMENT '就诊地点',
                                `doctor_name` varchar(255) NULL DEFAULT NULL COMMENT '预约医生',
                                `appointment_date` date NULL DEFAULT NULL COMMENT '预约日期',
                                `appointment_time` time NULL DEFAULT NULL COMMENT '预约时间',
                                `consultation_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '诊查费',
                                `status` varchar(50) NULL DEFAULT NULL COMMENT '业务状态',
                                `order_number` varchar(255) NULL DEFAULT NULL COMMENT '商户订单号',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of appointments
-- ----------------------------
INSERT INTO `appointments` VALUES (
                                      'APPOINTMENT_001',
                                      'M017092997',
                                      '流水号20251015001',
                                      '武芷竹',
                                      '辽宁省沈阳市铁西区滑翔路39号',
                                      '眼科门诊(滑翔)',
                                      '滑翔1号楼4楼 D区眼科2诊室',
                                      '于泳(副主任医师)',
                                      '2025-10-15',
                                      '09:28:00',
                                      14.40,
                                      '已取消',
                                      'SJ1010009251009026955'
                                  );