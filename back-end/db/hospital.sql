SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for hos_user (用户表)
-- ----------------------------
DROP TABLE IF EXISTS `hos_user`;
CREATE TABLE `hos_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一标识',
  `user_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录账号（患者：学号/工号/手机号；医生：管理员分配账号；管理员：固定账号）',
  `user_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '加密存储的密码（如MD5+盐值）',
  `user_type` tinyint NOT NULL COMMENT '用户类型（1-患者；2-医生；3-管理员）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱（可选通知渠道）',
  `status` tinyint NOT NULL COMMENT '账号状态（0-未激活；1-正常；2-禁用）',
  `create_time` datetime NOT NULL COMMENT '账号创建时间',
  `update_time` datetime NOT NULL COMMENT '账号更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `user_account`(`user_account` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of hos_user
-- ----------------------------
INSERT INTO `hos_user` VALUES (1, 'zhangxiaoming_std', 'hashed_password', 1, 'zxm@example.com', 1, '2025-10-22 10:00:00', '2025-10-22 10:00:00');
INSERT INTO `hos_user` VALUES (2, '11111111', 'b090a8e2dd67f03005adeff82c6b80e7', 1, NULL, 1, '2025-09-23 19:53:32', '2025-09-23 19:53:32');
INSERT INTO `hos_user` VALUES (3, '23301172', '71df02cfe7dd9606db2ef65b6d048f5b', 1, NULL, 1, '2025-10-20 11:30:24', '2025-10-20 11:30:24');
INSERT INTO `hos_user` VALUES (4, 'doctor_chen', 'hashed_password', 2, 'chen@hospital.com', 1, '2025-10-25 13:45:00', '2025-10-25 13:45:00');
INSERT INTO `hos_user` VALUES (5, 'doctor_zhao', 'hashed_password', 2, 'zhao@hospital.com', 1, '2025-10-26 14:50:00', '2025-10-26 14:50:00');
INSERT INTO `hos_user` VALUES (6, 'doctor_sun', 'hashed_password', 2, 'sun@hospital.com', 1, '2025-10-27 15:55:00', '2025-10-27 15:55:00');
INSERT INTO `hos_user` VALUES (7, 'admin_a', 'hashed_password', 3, 'admin_a@hospital.com', 1, '2025-10-28 16:10:00', '2025-10-28 16:10:00');
INSERT INTO `hos_user` VALUES (8, 'admin_b', 'hashed_password', 3, 'admin_b@hospital.com', 1, '2025-10-29 17:25:00', '2025-10-29 17:25:00');
INSERT INTO `hos_user` VALUES (9, 'admin_c', 'hashed_password', 3, 'admin_c@hospital.com', 1, '2025-10-30 18:40:00', '2025-10-30 18:40:00');

-- ----------------------------
-- Table structure for patient (患者表)
-- ----------------------------
DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient`  (
  `patient_id` bigint NOT NULL AUTO_INCREMENT COMMENT '患者唯一标识',
  `patient_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '患者姓名（真实姓名，用于就诊记录）',
  `user_id` bigint NOT NULL COMMENT '关联用户表（外键关联hos_user.user_id，唯一）',
  `patient_type` tinyint NOT NULL COMMENT '患者身份（1-学生；2-教师；3-职工）',
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学号（学生用户必填，唯一）',
  `staff_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工号（教师/职工用户必填，唯一）',
  `birth_date` date NOT NULL COMMENT '出生日期',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '性别（男/女/未知）',
  `height` decimal(5, 2) NULL DEFAULT NULL COMMENT '身高（单位：cm）',
  `weight` decimal(5, 2) NULL DEFAULT NULL COMMENT '体重（单位：kg）',
  `blood_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '血型',
  `marital_status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '未婚' COMMENT '婚姻状况（默认"未婚"）',
  `fertility_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生育情况',
  `present_illness` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '现病史',
  `past_illness` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '既往史',
  `family_illness` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '家族史',
  `allergy_history` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '无' COMMENT '过敏史（默认"无"）',
  `id_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '身份证' COMMENT '证件类型（默认"身份证"）',
  `nation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '汉族' COMMENT '民族（默认"汉族"）',
  `nationality` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '中国' COMMENT '国籍（默认"中国"）',
  `region` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所在地区',
  `detailed_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '详细住址',
  `home_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '家庭地址',
  `emergency_contact` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '紧急联系人姓名',
  `emergency_phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '紧急联系人电话',
  `medical_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '既往病史（可修改）',
  `identity_verify` tinyint NOT NULL DEFAULT 0 COMMENT '身份认证状态（0-未审核；1-已通过；2-未通过）',
  `verify_time` datetime NULL DEFAULT NULL COMMENT '审核通过时间',
  `outpatient_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '门诊号',
  `hospitalization_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '住院号',
  `barcode_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条形码信息',
  `qr_code_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '二维码信息',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '身份证号（敏感信息，加密存储，唯一）',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号（用于接收就诊提醒，唯一）',
  PRIMARY KEY (`patient_id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `student_id`(`student_id` ASC) USING BTREE,
  UNIQUE INDEX `staff_id`(`staff_id` ASC) USING BTREE,
  UNIQUE INDEX `id_card`(`id_card` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  CONSTRAINT `patient_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `hos_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '患者表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of patient
-- ----------------------------
INSERT INTO `patient` VALUES (1, '张三', 2, 1, '23301172', NULL, '2005-03-14', '女', 170.50, 55.00, 'O型', '未婚', NULL, '近三天腹泻', '无特殊病史', '无', '青霉素', '身份证', '汉族', '中国', '北京市', 'XX大学22宿舍楼214', '北京市海淀区北下关', '郑女士', '13912345678', NULL, 1, '2025-10-22 14:37:09', 'P20250001', NULL, NULL, NULL, '440301200010105678', '13000130003');

-- ----------------------------
-- Table structure for department (科室表)
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '科室唯一标识',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '科室名称（如“内科”“消化内科”）',
  `parent_dept_id` bigint NULL DEFAULT NULL COMMENT '父科室ID（顶级科室为0，如“内科”的父科室为0，“消化内科”父科室为内科ID）',
  `dept_level` tinyint NOT NULL COMMENT '科室级别（1-一级科室；2-二级科室）',
  `dept_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科室简介（如“负责消化系统疾病诊疗”）',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科室位置（如“门诊楼3层东侧”，用于导航扩展功能）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE,
  UNIQUE INDEX `dept_name`(`dept_name` ASC) USING BTREE,
  INDEX `parent_dept_id`(`parent_dept_id` ASC) USING BTREE,
  CONSTRAINT `department_ibfk_1` FOREIGN KEY (`parent_dept_id`) REFERENCES `department` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 201 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '科室表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES (100, '内科', NULL, 1, '综合内科诊疗服务', '门诊楼2层', '2025-10-22 14:31:03', '2025-10-22 14:31:03');
INSERT INTO `department` VALUES (101, '消化内科', 100, 2, '负责消化系统疾病的诊疗，如胃炎、胃溃疡等', '门诊楼2层东侧', '2025-10-22 14:31:03', '2025-10-22 14:31:03');
INSERT INTO `department` VALUES (200, '外科', NULL, 1, '综合外科诊疗服务', '门诊楼3层', '2025-10-22 14:31:03', '2025-10-22 14:31:03');

-- ----------------------------
-- Table structure for doctor_info (医生信息表)
-- ----------------------------
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor_info`  (
  `doctor_id` bigint NOT NULL AUTO_INCREMENT COMMENT '医生唯一标识',
  `user_id` bigint NOT NULL COMMENT '关联用户表(外键关联hos_user.user_id，唯一)',
  `dept_id` bigint NOT NULL COMMENT '所属科室ID（二级科室，外键关联department.dept_id）',
  `title` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '职称（如“主治医师”“主任医师”）',
  `specialty` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '擅长领域（如“胃炎、胃溃疡诊疗”）',
  `doctor_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '医生简介（可选）',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '医生头像URL（可选）',
  `is_active` tinyint NOT NULL COMMENT '出诊状态（0-暂停出诊；1-正常出诊）',
  `update_verify` tinyint NOT NULL COMMENT '信息修改审核状态（0-未提交修改；1-待审核；2-已通过；3-已驳回）',
  `doctor_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '医生姓名',
  PRIMARY KEY (`doctor_id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `dept_id`(`dept_id` ASC) USING BTREE,
  CONSTRAINT `doctor_info_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `hos_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `doctor_info_ibfk_2` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医生信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of doctor_info
-- ----------------------------
INSERT INTO `doctor_info` VALUES (1, 4, 101, '主治医师', '胃炎、胃溃疡诊疗', '擅长胃肠道常见病、多发病的诊治。', NULL, 1, 2, '王医生');
INSERT INTO `doctor_info` VALUES (2, 5, 101, '主任医师', '肝病、胰腺疾病诊疗', '资深专家，享受国务院特殊津贴。', NULL, 1, 2, '张医生');

-- ----------------------------
-- Table structure for admin (管理员表)
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `admin_id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员唯一标识',
  `user_id` bigint NOT NULL COMMENT '关联用户表的用户ID (外键关联hos_user.user_id)',
  `admin_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '管理员登录账号（唯一）',
  `admin_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '加密存储的密码',
  `admin_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '管理员姓名',
  `admin_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '管理员简介（可选）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`admin_id`) USING BTREE,
  UNIQUE INDEX `admin_account`(`admin_account` ASC) USING BTREE,
  INDEX `admin_ibfk_1`(`user_id` ASC) USING BTREE,
  CONSTRAINT `admin_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `hos_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin
-- ----------------------------

-- ----------------------------
-- Table structure for doctor_schedule (医生排班表)
-- ----------------------------
DROP TABLE IF EXISTS `doctor_schedule`;
CREATE TABLE `doctor_schedule`  (
  `schedule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `type_id` int NULL DEFAULT NULL COMMENT '排班类型ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `time_slot` tinyint NOT NULL COMMENT '时段(1-上午,2-下午,3-晚上)',
  `used_quota` int NOT NULL DEFAULT 0 COMMENT '已使用号源',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(1-有效,0-停用)',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`schedule_id`) USING BTREE,
  INDEX `idx_doctor_date`(`doctor_id` ASC, `schedule_date` ASC) USING BTREE,
  INDEX `idx_dept_date`(`dept_id` ASC, `schedule_date` ASC) USING BTREE,
  CONSTRAINT `doctor_schedule_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_info` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `doctor_schedule_ibfk_2` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医生排班表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of doctor_schedule
-- ----------------------------
INSERT INTO `doctor_schedule` VALUES (1, 1, 101, 1, '2025-10-22', 1, 2, 1, '2025-10-22 14:34:02', '2025-10-22 14:34:02');
INSERT INTO `doctor_schedule` VALUES (2, 2, 101, 1, '2025-10-23', 2, 0, 1, '2025-10-22 14:34:02', '2025-10-22 14:34:02');
INSERT INTO `doctor_schedule` VALUES (3, 1, 101, 2, '2025-10-22', 3, 5, 1, '2025-10-22 14:34:02', '2025-10-22 14:34:02');

-- ----------------------------
-- Table structure for registration_type (号源类型表)
-- ----------------------------
DROP TABLE IF EXISTS `registration_type`;
CREATE TABLE `registration_type`  (
  `type_id` bigint NOT NULL AUTO_INCREMENT COMMENT '号源类型唯一标识',
  `type_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型名称（如"普通号""专家号""特需号"）',
  `price_original` decimal(10, 2) NOT NULL COMMENT '原价（未报销前费用）',
  `student_price` decimal(10, 2) NOT NULL COMMENT '学生支付价（原价×5%）',
  `staff_price` decimal(10, 2) NOT NULL COMMENT '教师/职工支付价（原价×10%）',
  `daily_quota` int NOT NULL COMMENT '每日号源上限（如普通号50个，专家号20个）',
  `is_active` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用（0-禁用；1-启用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`type_id`) USING BTREE,
  UNIQUE INDEX `type_name`(`type_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '号源类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of registration_type
-- ----------------------------
INSERT INTO `registration_type` VALUES (1, '普通号', 50.00, 2.50, 5.00, 50, 1, '2025-10-17 10:54:17', '2025-10-17 10:54:17');
INSERT INTO `registration_type` VALUES (2, '专家号', 100.00, 5.00, 10.00, 20, 1, '2025-10-17 10:54:17', '2025-10-17 10:54:17');
INSERT INTO `registration_type` VALUES (3, '特需号', 200.00, 10.00, 20.00, 10, 1, '2025-10-17 10:54:17', '2025-10-17 10:54:17');

-- ----------------------------
-- Table structure for registration_record (挂号记录表)
-- ----------------------------
DROP TABLE IF EXISTS `registration_record`;
CREATE TABLE `registration_record`  (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '挂号记录唯一标识',
  `schedule_id` bigint NOT NULL COMMENT '排班ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `type_id` bigint NOT NULL COMMENT '号源类型ID',
  `registration_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '挂号单号（格式：YYYYMMDD+科室ID+序号，如20241001001001）',
  `register_time` datetime NOT NULL COMMENT '挂号时间',
  `status` tinyint NOT NULL COMMENT '挂号状态（0-候补；1-已预约；2-已就诊；3-已退号；4-已取消（医生/系统取消））',
  `price_original` decimal(10, 2) NOT NULL COMMENT '原价',
  `actual_price` decimal(10, 2) NOT NULL COMMENT '实付价（根据患者类型计算）',
  `waiting_rank` int NULL DEFAULT NULL COMMENT '候补排名（仅状态为0时有效）',
  `consult_room` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '诊室号（如"301诊室"，就诊前分配）',
  `visit_time` datetime NULL DEFAULT NULL COMMENT '实际就诊时间（状态为2时填写）',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消/退号时间（状态为3/4时填写）',
  `cancel_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '取消/退号原因',
  `is_add` tinyint NOT NULL COMMENT '是否为加号（0-正常号；1-加号）',
  `add_remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '加号备注（如"急诊优先"，仅is_add=1时填写）',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `registration_no`(`registration_no` ASC) USING BTREE,
  INDEX `schedule_id`(`schedule_id` ASC) USING BTREE,
  INDEX `patient_id`(`patient_id` ASC) USING BTREE,
  INDEX `doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `type_id`(`type_id` ASC) USING BTREE,
  CONSTRAINT `registration_record_ibfk_1` FOREIGN KEY (`schedule_id`) REFERENCES `doctor_schedule` (`schedule_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `registration_record_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `registration_record_ibfk_3` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_info` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `registration_record_ibfk_4` FOREIGN KEY (`type_id`) REFERENCES `registration_type` (`type_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '挂号记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of registration_record
-- ----------------------------

-- ----------------------------
-- Table structure for waiting_queue (候补队列表)
-- ----------------------------
DROP TABLE IF EXISTS `waiting_queue`;
CREATE TABLE `waiting_queue`  (
  `queue_id` bigint NOT NULL AUTO_INCREMENT COMMENT '队列唯一标识',
  `schedule_id` bigint NOT NULL COMMENT '排班ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `record_id` bigint NOT NULL COMMENT '关联挂号记录（状态为0）',
  `queue_rank` int NOT NULL COMMENT '候补排名（按挂号时间排序）',
  `queue_time` datetime NOT NULL COMMENT '加入队列时间',
  `status` tinyint NOT NULL COMMENT '队列状态（0-等待中；1-已结束；2-已放弃；3-已过期）',
  `transfer_time` datetime NULL DEFAULT NULL COMMENT '转正时间（状态为1时填写）',
  PRIMARY KEY (`queue_id`) USING BTREE,
  UNIQUE INDEX `record_id`(`record_id` ASC) USING BTREE,
  INDEX `schedule_id`(`schedule_id` ASC) USING BTREE,
  INDEX `patient_id`(`patient_id` ASC) USING BTREE,
  CONSTRAINT `waiting_queue_ibfk_1` FOREIGN KEY (`schedule_id`) REFERENCES `doctor_schedule` (`schedule_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `waiting_queue_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `waiting_queue_ibfk_3` FOREIGN KEY (`record_id`) REFERENCES `registration_record` (`record_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '候补队列表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of waiting_queue
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