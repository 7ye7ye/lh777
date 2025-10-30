/*
 Navicat Premium Data Transfer

 Source Server         : hospital
 Source Server Type    : MySQL
 Source Server Version : 90300 (9.3.0)
 Source Host           : localhost:3306
 Source Schema         : hospital

 Target Server Type    : MySQL
 Target Server Version : 90300 (9.3.0)
 File Encoding         : 65001

 Date: 29/10/2025 21:22:01
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES
                        (1, 7, 'admin_a', 'hashed_password_1', '张管理员', '系统总管理员，负责用户管理和系统维护', '2025-10-28 16:10:00', '2025-10-28 16:10:00'),
                        (2, 8, 'admin_b', 'hashed_password_2', '李管理员', '排班管理员，负责医生排班审核', '2025-10-29 17:25:00', '2025-10-29 17:25:00'),
                        (3, 9, 'admin_c', 'hashed_password_3', '王管理员', '患者信息管理员，负责患者身份审核', '2025-10-30 18:40:00', '2025-10-30 18:40:00'),
                        (4, 11, 'admin_d', 'hashed_password_4', '赵管理员', '财务管理员，负责挂号费用管理', '2025-10-31 09:15:00', '2025-10-31 09:15:00'),
                        (5, 12, 'admin_e', 'hashed_password_5', '钱管理员', '科室管理员，负责科室信息维护', '2025-11-01 10:20:00', '2025-11-01 10:20:00'),
                        (6, 13, 'admin_f', 'hashed_password_6', '孙管理员', '系统监控员，负责系统运行监控', '2025-11-02 11:25:00', '2025-11-02 11:25:00'),
                        (7, 14, 'admin_g', 'hashed_password_7', '周管理员', '数据备份员，负责数据备份恢复', '2025-11-03 12:30:00', '2025-11-03 12:30:00'),
                        (8, 15, 'admin_h', 'hashed_password_8', '吴管理员', '权限管理员，负责用户权限分配', '2025-11-04 13:35:00', '2025-11-04 13:35:00'),
                        (9, 16, 'admin_i', 'hashed_password_9', '郑管理员', '报表管理员，负责统计报表生成', '2025-11-05 14:40:00', '2025-11-05 14:40:00'),
                        (10, 17, 'admin_j', 'hashed_password_10', '冯管理员', '客服管理员，负责用户问题处理', '2025-11-06 15:45:00', '2025-11-06 15:45:00'),
                        (11, 18, 'admin_k', 'hashed_password_11', '陈管理员', '设备管理员，负责医疗设备管理', '2025-11-07 16:50:00', '2025-11-07 16:50:00'),
                        (12, 19, 'admin_l', 'hashed_password_12', '楚管理员', '药品管理员，负责药品库存管理', '2025-11-08 17:55:00', '2025-11-08 17:55:00');

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
                               `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '科室唯一标识',
                               `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '科室名称（如"内科""消化内科"）',
                               `parent_dept_id` bigint NULL DEFAULT NULL COMMENT '父科室ID（顶级科室为0，如"内科"的父科室为0，"消化内科"父科室为内科ID）',
                               `dept_level` tinyint NOT NULL COMMENT '科室级别（1-一级科室；2-二级科室）',
                               `dept_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '科室简介（如"负责消化系统疾病诊疗"）',
                               `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '科室位置（如"门诊楼3层东侧"，用于导航扩展功能）',
                               `create_time` datetime NOT NULL COMMENT '创建时间',
                               `update_time` datetime NOT NULL COMMENT '更新时间',
                               PRIMARY KEY (`dept_id`) USING BTREE,
                               UNIQUE INDEX `dept_name`(`dept_name` ASC) USING BTREE,
                               INDEX `parent_dept_id`(`parent_dept_id` ASC) USING BTREE,
                               CONSTRAINT `department_ibfk_1` FOREIGN KEY (`parent_dept_id`) REFERENCES `department` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 201 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '科室表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES
                             (100, '内科', NULL, 1, '综合内科诊疗服务', '门诊楼2层', '2025-10-22 14:31:03', '2025-10-22 14:31:03'),
                             (101, '消化内科', 100, 2, '负责消化系统疾病的诊疗，如胃炎、胃溃疡等', '门诊楼2层东侧', '2025-10-22 14:31:03', '2025-10-22 14:31:03'),
                             (102, '心血管内科', 100, 2, '负责心血管系统疾病的诊疗', '门诊楼2层西侧', '2025-10-22 14:32:00', '2025-10-22 14:32:00'),
                             (103, '呼吸内科', 100, 2, '负责呼吸系统疾病的诊疗', '门诊楼2层南侧', '2025-10-22 14:33:00', '2025-10-22 14:33:00'),
                             (104, '神经内科', 100, 2, '负责神经系统疾病的诊疗', '门诊楼2层北侧', '2025-10-22 14:34:00', '2025-10-22 14:34:00'),
                             (200, '外科', NULL, 1, '综合外科诊疗服务', '门诊楼3层', '2025-10-22 14:31:03', '2025-10-22 14:31:03'),
                             (201, '普外科', 200, 2, '负责普通外科疾病的诊疗', '门诊楼3层东侧', '2025-10-22 14:35:00', '2025-10-22 14:35:00'),
                             (202, '骨科', 200, 2, '负责骨科疾病的诊疗', '门诊楼3层西侧', '2025-10-22 14:36:00', '2025-10-22 14:36:00'),
                             (203, '神经外科', 200, 2, '负责神经外科疾病的诊疗', '门诊楼3层南侧', '2025-10-22 14:37:00', '2025-10-22 14:37:00'),
                             (204, '泌尿外科', 200, 2, '负责泌尿系统外科疾病的诊疗', '门诊楼3层北侧', '2025-10-22 14:38:00', '2025-10-22 14:38:00'),
                             (300, '妇产科', NULL, 1, '妇产科诊疗服务', '门诊楼4层', '2025-10-22 14:39:00', '2025-10-22 14:39:00'),
                             (301, '妇科', 300, 2, '负责妇科疾病的诊疗', '门诊楼4层东侧', '2025-10-22 14:40:00', '2025-10-22 14:40:00'),
                             (302, '产科', 300, 2, '负责产科相关服务', '门诊楼4层西侧', '2025-10-22 14:41:00', '2025-10-22 14:41:00'),
                             (400, '儿科', NULL, 1, '儿科诊疗服务', '门诊楼5层', '2025-10-22 14:42:00', '2025-10-22 14:42:00'),
                             (401, '小儿内科', 400, 2, '负责小儿内科疾病的诊疗', '门诊楼5层东侧', '2025-10-22 14:43:00', '2025-10-22 14:43:00'),
                             (402, '小儿外科', 400, 2, '负责小儿外科疾病的诊疗', '门诊楼5层西侧', '2025-10-22 14:44:00', '2025-10-22 14:44:00'),
                             (500, '眼科', NULL, 1, '眼科诊疗服务', '门诊楼6层', '2025-10-22 14:45:00', '2025-10-22 14:45:00'),
                             (501, '眼科门诊', 500, 2, '负责眼科疾病的诊疗', '门诊楼6层东侧', '2025-10-22 14:46:00', '2025-10-22 14:46:00');

-- ----------------------------
-- Table structure for doctor
-- ----------------------------
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor`  (
                           `doctor_id` bigint NOT NULL AUTO_INCREMENT COMMENT '医生唯一标识',
                           `user_id` bigint NOT NULL COMMENT '关联用户表',
                           `dept_id` bigint NOT NULL COMMENT '所属科室ID（二级科室，如"消化内科"）',
                           `title` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '职称（如"主治医师""主任医师"）',
                           `specialty` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '擅长领域（如"胃炎、胃溃疡诊疗"）',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1001 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of doctor
-- ----------------------------
INSERT INTO `doctor` VALUES
                         (1, 10, 101, '主治医师', '胃炎、胃溃疡诊疗', '擅长胃肠道常见病、多发病的诊治。', NULL, 1, 2, '王医生'),
                         (2, 5, 101, '主任医师', '肝病、胰腺疾病诊疗', '资深专家，享受国务院特殊津贴。', NULL, 1, 2, '张医生'),
                         (3, 20, 102, '副主任医师', '高血压、冠心病诊疗', '心血管疾病专家，从事临床工作20年。', NULL, 1, 2, '李医生'),
                         (4, 21, 102, '主治医师', '心律失常、心力衰竭', '擅长心血管介入治疗。', NULL, 1, 2, '刘医生'),
                         (5, 22, 103, '主任医师', '哮喘、慢性阻塞性肺病', '呼吸科资深专家。', NULL, 1, 2, '陈医生'),
                         (6, 23, 103, '副主任医师', '肺炎、支气管炎', '擅长呼吸系统感染性疾病诊治。', NULL, 1, 2, '杨医生'),
                         (7, 24, 104, '主任医师', '脑卒中、癫痫', '神经内科专家。', NULL, 1, 2, '赵医生'),
                         (8, 25, 104, '主治医师', '头痛、眩晕', '擅长神经系统常见病诊治。', NULL, 1, 2, '周医生'),
                         (9, 26, 201, '副主任医师', '甲状腺疾病、乳腺疾病', '普外科专家。', NULL, 1, 2, '吴医生'),
                         (10, 27, 201, '主治医师', '阑尾炎、疝气', '擅长普外科常见手术。', NULL, 1, 2, '黄医生'),
                         (11, 28, 202, '主任医师', '骨折、关节置换', '骨科资深专家。', NULL, 1, 2, '徐医生'),
                         (12, 29, 202, '副主任医师', '脊柱疾病、骨质疏松', '擅长骨科微创手术。', NULL, 1, 2, '孙医生'),
                         (13, 30, 203, '主任医师', '脑肿瘤、脑血管病', '神经外科专家。', NULL, 1, 2, '朱医生'),
                         (14, 31, 203, '主治医师', '颅脑损伤、脑出血', '擅长神经外科急诊。', NULL, 1, 2, '马医生'),
                         (15, 32, 204, '副主任医师', '前列腺疾病、泌尿系结石', '泌尿外科专家。', NULL, 1, 2, '胡医生');

-- ----------------------------
-- Table structure for doctor_schedule
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
                                    CONSTRAINT `doctor_schedule_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                    CONSTRAINT `doctor_schedule_ibfk_2` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生排班表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of doctor_schedule
-- ----------------------------
INSERT INTO `doctor_schedule` VALUES
                                  (1, 1, 101, 1, '2025-10-29', 1, 2, 1, '2025-10-22 14:34:02', '2025-10-22 14:34:02'),
                                  (2, 2, 101, 1, '2025-10-25', 2, 0, 1, '2025-10-22 14:34:02', '2025-10-22 14:34:02'),
                                  (3, 1, 101, 2, '2025-10-30', 3, 5, 1, '2025-10-22 14:34:02', '2025-10-22 14:34:02'),
                                  (4, 3, 102, 1, '2025-10-30', 1, 3, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
                                  (5, 3, 102, 1, '2025-10-31', 2, 2, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
                                  (6, 4, 102, 2, '2025-10-30', 2, 1, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
                                  (7, 5, 103, 1, '2025-10-30', 1, 4, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
                                  (8, 6, 103, 1, '2025-10-31', 1, 2, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
                                  (9, 7, 104, 2, '2025-10-30', 1, 3, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
                                  (10, 8, 104, 1, '2025-10-31', 2, 1, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
                                  (11, 9, 201, 1, '2025-10-30', 1, 2, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
                                  (12, 10, 201, 1, '2025-10-31', 2, 3, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
                                  (13, 11, 202, 2, '2025-10-30', 1, 4, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
                                  (14, 12, 202, 1, '2025-10-31', 2, 2, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
                                  (15, 13, 203, 1, '2025-10-30', 1, 1, 1, '2025-10-23 09:00:00', '2025-10-23 09:00:00');

-- ----------------------------
-- Table structure for doctor_schedule_adjustment
-- ----------------------------
DROP TABLE IF EXISTS `doctor_schedule_adjustment`;
CREATE TABLE `doctor_schedule_adjustment`  (
                                               `adjustment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '调班申请记录唯一标识',
                                               `doctor_id` bigint NOT NULL COMMENT '申请调班的医生ID',
                                               `original_schedule_id` bigint NULL DEFAULT NULL COMMENT '原排班记录ID(被调整的排班)',
                                               `target_date` date NOT NULL COMMENT '目标调班日期',
                                               `target_time_slot` int NOT NULL COMMENT '目标调班时段(1-上午; 2-下午; 3-晚上; ...)',
                                               `target_dept_id` bigint NOT NULL COMMENT '目标出诊科室ID(可能跨科室调班)',
                                               `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '医生申请调班的原因',
                                               `apply_time` datetime NOT NULL COMMENT '申请提交时间',
                                               `status` tinyint NOT NULL DEFAULT 1 COMMENT '审批状态(1-待审批; 2-已通过; 3-已驳回; 4-已撤销)',
                                               `admin_id` bigint NULL DEFAULT NULL COMMENT '负责审批的管理员ID(状态为2/3时填写)',
                                               `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
                                               `reject_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '驳回原因(状态为3时填写)',
                                               `new_schedule_id` bigint NULL DEFAULT NULL COMMENT '新排班记录ID(若审批通过, 系统创建的新排班记录)',
                                               PRIMARY KEY (`adjustment_id`) USING BTREE,
                                               INDEX `idx_doctor`(`doctor_id` ASC) USING BTREE,
                                               INDEX `idx_original_schedule`(`original_schedule_id` ASC) USING BTREE,
                                               INDEX `idx_new_schedule`(`new_schedule_id` ASC) USING BTREE,
                                               INDEX `idx_target_date`(`target_date` ASC) USING BTREE,
                                               INDEX `fk_adjustment_target_dept`(`target_dept_id` ASC) USING BTREE,
                                               INDEX `fk_adjustment_admin`(`admin_id` ASC) USING BTREE,
                                               CONSTRAINT `fk_adjustment_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
                                               CONSTRAINT `fk_adjustment_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                               CONSTRAINT `fk_adjustment_new_schedule` FOREIGN KEY (`new_schedule_id`) REFERENCES `doctor_schedule` (`schedule_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
                                               CONSTRAINT `fk_adjustment_original_schedule` FOREIGN KEY (`original_schedule_id`) REFERENCES `doctor_schedule` (`schedule_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                               CONSTRAINT `fk_adjustment_target_dept` FOREIGN KEY (`target_dept_id`) REFERENCES `department` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生申请排班(调班)表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctor_schedule_adjustment
-- ----------------------------
INSERT INTO `doctor_schedule_adjustment` VALUES
                                             (1, 1, 1, '2025-10-25', 1, 100, '俺要回家', '2025-10-24 15:05:10', 1, NULL, NULL, NULL, NULL),
                                             (2, 1, 1, '2025-10-24', 3, 100, '俺要回家', '2025-10-24 15:09:42', 1, NULL, NULL, NULL, NULL),
                                             (3, 3, 4, '2025-11-01', 1, 102, '参加学术会议', '2025-10-25 09:00:00', 2, 2, '2025-10-25 14:00:00', NULL, 16),
                                             (4, 5, 7, '2025-11-02', 2, 103, '病假休息', '2025-10-25 10:00:00', 2, 2, '2025-10-25 15:00:00', NULL, 17),
                                             (5, 7, 9, '2025-11-03', 1, 104, '外出培训', '2025-10-26 09:00:00', 3, 2, '2025-10-26 11:00:00', '该时段已有其他医生调班', NULL),
                                             (6, 9, 11, '2025-11-04', 2, 201, '家庭事务', '2025-10-26 10:00:00', 1, NULL, NULL, NULL, NULL),
                                             (7, 11, 13, '2025-11-05', 1, 202, '参加手术', '2025-10-27 09:00:00', 2, 2, '2025-10-27 16:00:00', NULL, 18),
                                             (8, 13, 15, '2025-11-06', 2, 203, '学术交流', '2025-10-27 10:00:00', 1, NULL, NULL, NULL, NULL),
                                             (9, 2, NULL, '2025-11-07', 1, 101, '临时加诊', '2025-10-28 09:00:00', 2, 2, '2025-10-28 14:00:00', NULL, 19),
                                             (10, 4, 6, '2025-11-08', 2, 102, '个人原因', '2025-10-28 10:00:00', 3, 2, '2025-10-28 15:00:00', '该时段号源已满', NULL),
                                             (11, 6, 8, '2025-11-09', 1, 103, '体检', '2025-10-29 09:00:00', 4, NULL, NULL, NULL, NULL),
                                             (12, 8, 10, '2025-11-10', 2, 104, '休假', '2025-10-29 10:00:00', 1, NULL, NULL, NULL, NULL),
                                             (13, 10, 12, '2025-11-11', 1, 201, '学术报告', '2025-10-30 09:00:00', 2, 2, '2025-10-30 14:00:00', NULL, 20),
                                             (14, 12, 14, '2025-11-12', 2, 202, '教学任务', '2025-10-30 10:00:00', 1, NULL, NULL, NULL, NULL),
                                             (15, 14, NULL, '2025-11-13', 1, 203, '急诊支援', '2025-10-31 09:00:00', 2, 2, '2025-10-31 14:00:00', NULL, 21);

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of hos_user
-- ----------------------------
INSERT INTO `hos_user` VALUES
                           (1, 'zhangxiaoming_std', 'hashed_password', 1, '110105200010221234', '13900000011', 'zxm@example.com', 1, '2025-10-22 10:00:00', '2025-10-22 10:00:00'),
                           (2, '11111111', 'b090a8e2dd67f03005adeff82c6b80e7', 1, NULL, NULL, NULL, 1, '2025-09-23 19:53:32', '2025-09-23 19:53:32'),
                           (3, '23301172', '71df02cfe7dd9606db2ef65b6d048f5b', 1, NULL, NULL, NULL, 1, '2025-10-20 11:30:24', '2025-10-20 11:30:24'),
                           (4, '123456bjtu', 'hashed_password', 2, '210102197510223456', '13600000044', 'chen@hospital.com', 1, '2025-10-25 13:45:00', '2025-10-25 13:45:00'),
                           (5, '234567bjtu', 'hashed_password', 2, '500101198010267890', '13500000055', 'zhao@hospital.com', 1, '2025-10-26 14:50:00', '2025-10-26 14:50:00'),
                           (6, 'doctor_sun', 'hashed_password', 2, '610101199510292345', '13400000066', 'sun@hospital.com', 1, '2025-10-27 15:55:00', '2025-10-27 15:55:00'),
                           (7, 'admin_a', 'hashed_password', 3, '440101197010226789', '13300000077', 'admin_a@hospital.com', 1, '2025-10-28 16:10:00', '2025-10-28 16:10:00'),
                           (8, 'admin_b', 'hashed_password', 3, '320101198010231234', '13200000088', 'admin_b@hospital.com', 1, '2025-10-29 17:25:00', '2025-10-29 17:25:00'),
                           (9, 'admin_c', 'hashed_password', 3, '510101199010305678', '13100000099', 'admin_c@hospital.com', 1, '2025-10-30 18:40:00', '2025-10-30 18:40:00'),
                           (10, '345678bjtu', 'f5cce500f5ffb83ecfcc4fd19b8b1377', 2, NULL, '', NULL, 1, '2025-10-26 12:03:20', '2025-10-26 12:03:20'),
                           (11, 'admin_d', 'hashed_password', 3, '330101198510221234', '13000000100', 'admin_d@hospital.com', 1, '2025-10-31 09:15:00', '2025-10-31 09:15:00'),
                           (12, 'admin_e', 'hashed_password', 3, '340101197510231234', '13000000101', 'admin_e@hospital.com', 1, '2025-11-01 10:20:00', '2025-11-01 10:20:00'),
                           (13, 'admin_f', 'hashed_password', 3, '350101198510241234', '13000000102', 'admin_f@hospital.com', 1, '2025-11-02 11:25:00', '2025-11-02 11:25:00'),
                           (14, 'admin_g', 'hashed_password', 3, '360101197510251234', '13000000103', 'admin_g@hospital.com', 1, '2025-11-03 12:30:00', '2025-11-03 12:30:00'),
                           (15, 'admin_h', 'hashed_password', 3, '370101198510261234', '13000000104', 'admin_h@hospital.com', 1, '2025-11-04 13:35:00', '2025-11-04 13:35:00'),
                           (16, 'admin_i', 'hashed_password', 3, '380101197510271234', '13000000105', 'admin_i@hospital.com', 1, '2025-11-05 14:40:00', '2025-11-05 14:40:00'),
                           (17, 'admin_j', 'hashed_password', 3, '390101198510281234', '13000000106', 'admin_j@hospital.com', 1, '2025-11-06 15:45:00', '2025-11-06 15:45:00'),
                           (18, 'admin_k', 'hashed_password', 3, '400101197510291234', '13000000107', 'admin_k@hospital.com', 1, '2025-11-07 16:50:00', '2025-11-07 16:50:00'),
                           (19, 'admin_l', 'hashed_password', 3, '410101198510301234', '13000000108', 'admin_l@hospital.com', 1, '2025-11-08 17:55:00', '2025-11-08 17:55:00'),
                           (20, 'doctor_li', 'hashed_password', 2, '420101197510311234', '13000000109', 'li@hospital.com', 1, '2025-11-09 09:00:00', '2025-11-09 09:00:00'),
                           (21, 'doctor_liu', 'hashed_password', 2, '430101198511011234', '13000000110', 'liu@hospital.com', 1, '2025-11-09 10:00:00', '2025-11-09 10:00:00'),
                           (22, 'doctor_chen', 'hashed_password', 2, '440101197511021234', '13000000111', 'chen@hospital.com', 1, '2025-11-09 11:00:00', '2025-11-09 11:00:00'),
                           (23, 'doctor_yang', 'hashed_password', 2, '450101198511031234', '13000000112', 'yang@hospital.com', 1, '2025-11-09 12:00:00', '2025-11-09 12:00:00'),
                           (24, 'doctor_zhao', 'hashed_password', 2, '460101197511041234', '13000000113', 'zhao@hospital.com', 1, '2025-11-09 13:00:00', '2025-11-09 13:00:00'),
                           (25, 'doctor_zhou', 'hashed_password', 2, '470101198511051234', '13000000114', 'zhou@hospital.com', 1, '2025-11-09 14:00:00', '2025-11-09 14:00:00'),
                           (26, 'doctor_wu', 'hashed_password', 2, '480101197511061234', '13000000115', 'wu@hospital.com', 1, '2025-11-09 15:00:00', '2025-11-09 15:00:00'),
                           (27, 'doctor_huang', 'hashed_password', 2, '490101198511071234', '13000000116', 'huang@hospital.com', 1, '2025-11-09 16:00:00', '2025-11-09 16:00:00'),
                           (28, 'doctor_xu', 'hashed_password', 2, '500101197511081234', '13000000117', 'xu@hospital.com', 1, '2025-11-09 17:00:00', '2025-11-09 17:00:00'),
                           (30, 'doctor_zhu', 'hashed_password', 2, '520101197511101234', '13000000119', 'zhu@hospital.com', 1, '2025-11-09 19:00:00', '2025-11-09 19:00:00'),
                           (31, 'doctor_ma', 'hashed_password', 2, '530101198511111234', '13000000120', 'ma@hospital.com', 1, '2025-11-09 20:00:00', '2025-11-09 20:00:00'),
                           (32, 'doctor_hu', 'hashed_password', 2, '540101197511121234', '13000000121', 'hu@hospital.com', 1, '2025-11-09 21:00:00', '2025-11-09 21:00:00'),
                           (33, 'patient_li', 'hashed_password', 1, '550101200011131234', '13000000122', 'patient_li@example.com', 1, '2025-11-10 09:00:00', '2025-11-10 09:00:00'),
                           (34, 'patient_wang', 'hashed_password', 1, '560101200111141234', '13000000123', 'patient_wang@example.com', 1, '2025-11-10 10:00:00', '2025-11-10 10:00:00'),
                           (35, 'patient_zhang', 'hashed_password', 1, '570101200211151234', '13000000124', 'patient_zhang@example.com', 1, '2025-11-10 11:00:00', '2025-11-10 11:00:00'),
                           (36, 'patient_liu', 'hashed_password', 1, '580101200311161234', '13000000125', 'patient_liu@example.com', 1, '2025-11-10 12:00:00', '2025-11-10 12:00:00'),
                           (37, 'patient_chen', 'hashed_password', 1, '590101200411171234', '13000000126', 'patient_chen@example.com', 1, '2025-11-10 13:00:00', '2025-11-10 13:00:00'),
                           (38, 'patient_yang', 'hashed_password', 1, '600101200511181234', '13000000127', 'patient_yang@example.com', 1, '2025-11-10 14:00:00', '2025-11-10 14:00:00'),
                           (39, 'patient_zhao', 'hashed_password', 1, '610101200611191234', '13000000128', 'patient_zhao@example.com', 1, '2025-11-10 15:00:00', '2025-11-10 15:00:00'),
                           (40, 'patient_zhou', 'hashed_password', 1, '620101200711201234', '13000000129', 'patient_zhou@example.com', 1, '2025-11-10 16:00:00', '2025-11-10 16:00:00'),
                           (41, 'patient_wu', 'hashed_password', 1, '630101200811211234', '13000000130', 'patient_wu@example.com', 1, '2025-11-10 17:00:00', '2025-11-10 17:00:00'),
                           (42, 'patient_zheng', 'hashed_password', 1, '640101200911221234', '13000000131', 'patient_zheng@example.com', 1, '2025-11-10 18:00:00', '2025-11-10 18:00:00');

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of patient
-- ----------------------------
INSERT INTO `patient` VALUES
                          (1, 2, 1, '23301172', NULL, '2005-03-14', '女', 170.50, 55.00, 'O型', '未婚', NULL, '近三天腹泻', '无特殊病史', '无', '青霉素', '身份证', '440301200010105678', '汉族', '中国', '北京市', 'XX大学22宿舍楼214', '13000130003', '北京市海淀区北下关', '郑女士', '13912345678', NULL, 1, '2025-10-22 14:37:09', 'P20250001', NULL, NULL, NULL),
                          (2, 1, 1, '20230001', NULL, '2002-05-15', '男', 175.00, 65.00, 'A型', '未婚', NULL, '感冒发烧', '无', '无', '无', '身份证', '110105200010221234', '汉族', '中国', '北京市', 'XX大学1宿舍楼101', '13900000011', '北京市朝阳区', '张先生', '13900000012', '无', 1, '2025-10-23 10:00:00', 'P20250002', NULL, NULL, NULL),
                          (3, 33, 1, '20230002', NULL, '2003-06-20', '女', 165.00, 50.00, 'B型', '未婚', NULL, '头痛头晕', '无', '高血压', '无', '身份证', '550101200011131234', '汉族', '中国', '北京市', 'XX大学3宿舍楼305', '13000000122', '北京市海淀区', '李先生', '13000000123', '无', 1, '2025-10-23 11:00:00', 'P20250003', NULL, NULL, NULL),
                          (4, 34, 2, NULL, 'T2023001', '1980-08-25', '男', 180.00, 75.00, 'AB型', '已婚', '已生育', '胃痛', '胃炎', '糖尿病', '无', '身份证', '560101200111141234', '汉族', '中国', '北京市', '教职工公寓A栋101', '13000000123', '北京市西城区', '王先生', '13000000124', '胃炎病史', 1, '2025-10-23 12:00:00', 'P20250004', NULL, NULL, NULL),
                          (5, 35, 1, '20230003', NULL, '2004-07-30', '女', 168.00, 52.00, 'O型', '未婚', NULL, '咳嗽', '无', '无', '青霉素', '身份证', '570101200211151234', '汉族', '中国', '北京市', 'XX大学5宿舍楼408', '13000000124', '北京市东城区', '张女士', '13000000125', '无', 1, '2025-10-23 13:00:00', 'P20250005', NULL, NULL, NULL),
                          (6, 36, 3, NULL, 'S2023001', '1975-09-10', '男', 172.00, 70.00, 'A型', '已婚', '已生育', '腰痛', '腰椎间盘突出', '心脏病', '无', '身份证', '580101200311161234', '汉族', '中国', '北京市', '职工宿舍B栋202', '13000000125', '北京市丰台区', '刘先生', '13000000126', '腰椎病史', 1, '2025-10-23 14:00:00', 'P20250006', NULL, NULL, NULL),
                          (7, 37, 1, '20230004', NULL, '2003-10-15', '女', 162.00, 48.00, 'B型', '未婚', NULL, '腹痛', '无', '无', '海鲜', '身份证', '590101200411171234', '汉族', '中国', '北京市', 'XX大学7宿舍楼512', '13000000126', '北京市石景山区', '陈女士', '13000000127', '无', 1, '2025-10-23 15:00:00', 'P20250007', NULL, NULL, NULL),
                          (8, 38, 2, NULL, 'T2023002', '1982-11-20', '男', 178.00, 72.00, 'O型', '已婚', '未生育', '失眠', '无', '无', '无', '身份证', '600101200511181234', '汉族', '中国', '北京市', '教职工公寓C栋303', '13000000127', '北京市通州区', '杨先生', '13000000128', '无', 1, '2025-10-23 16:00:00', 'P20250008', NULL, NULL, NULL),
                          (9, 39, 1, '20230005', NULL, '2004-12-25', '女', 166.00, 51.00, 'A型', '未婚', NULL, '喉咙痛', '无', '无', '无', '身份证', '610101200611191234', '汉族', '中国', '北京市', 'XX大学9宿舍楼615', '13000000128', '北京市昌平区', '赵女士', '13000000129', '无', 1, '2025-10-23 17:00:00', 'P20250009', NULL, NULL, NULL),
                          (10, 40, 3, NULL, 'S2023002', '1978-01-30', '男', 175.00, 68.00, 'AB型', '已婚', '已生育', '关节痛', '关节炎', '无', '无', '身份证', '620101200711201234', '汉族', '中国', '北京市', '职工宿舍D栋104', '13000000129', '北京市大兴区', '周先生', '13000000130', '关节炎病史', 1, '2025-10-23 18:00:00', 'P20250010', NULL, NULL, NULL),
                          (11, 41, 1, '20230006', NULL, '2003-02-14', '男', 177.00, 63.00, 'O型', '未婚', NULL, '发热', '无', '无', '无', '身份证', '630101200811211234', '汉族', '中国', '北京市', 'XX大学11宿舍楼721', '13000000130', '北京市房山区', '吴先生', '13000000131', '无', 1, '2025-10-23 19:00:00', 'P20250011', NULL, NULL, NULL),
                          (12, 42, 1, '20230007', NULL, '2004-03-18', '女', 164.00, 49.00, 'B型', '未婚', NULL, '胸闷', '无', '心脏病', '无', '身份证', '640101200911221234', '汉族', '中国', '北京市', 'XX大学13宿舍楼819', '13000000131', '北京市顺义区', '郑女士', '13000000132', '无', 1, '2025-10-23 20:00:00', 'P20250012', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for registration_record
-- ----------------------------
DROP TABLE IF EXISTS `registration_record`;
CREATE TABLE `registration_record`  (
                                        `patient_name` varchar(20) NOT NULL COMMENT '用户姓名',
                                        `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '挂号记录唯一标识',
                                        `schedule_id` bigint NOT NULL COMMENT '排班ID',
                                        `patient_id` bigint NOT NULL COMMENT '患者ID',
                                        `doctor_id` bigint NOT NULL COMMENT '医生ID',
                                        `type_id` bigint NOT NULL COMMENT '号源类型ID',
                                        `registration_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '挂号单号（格式：YYYYMMDD+科室ID+序号，如20241001001001）',
                                        `register_time` datetime NOT NULL COMMENT '挂号时间',
                                        `status` tinyint NOT NULL COMMENT '挂号状态（0-候补；1-已预约；2-已就诊；3-已退号；4-已取消（医生/系统取消））',
                                        `price_original` decimal(10, 2) NOT NULL COMMENT '原价',
                                        `actual_price` decimal(10, 2) NOT NULL COMMENT '实付价（根据患者类型计算）',
                                        `waiting_rank` int NULL DEFAULT NULL COMMENT '候补排名（仅状态为0时有效）',
                                        `consult_room` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '诊室号（如"301诊室"，就诊前分配）',
                                        `visit_time` datetime NULL DEFAULT NULL COMMENT '实际就诊时间（状态为2时填写）',
                                        `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消/退号时间（状态为3/4时填写）',
                                        `cancel_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消/退号原因',
                                        `is_add` tinyint NOT NULL COMMENT '是否为加号（0-正常号；1-加号）',
                                        `add_remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '加号备注（如"急诊优先"，仅is_add=1时填写）',
                                        PRIMARY KEY (`record_id`) USING BTREE,
                                        UNIQUE INDEX `registration_no`(`registration_no` ASC) USING BTREE,
                                        INDEX `schedule_id`(`schedule_id` ASC) USING BTREE,
                                        INDEX `patient_id`(`patient_id` ASC) USING BTREE,
                                        INDEX `doctor_id`(`doctor_id` ASC) USING BTREE,
                                        INDEX `type_id`(`type_id` ASC) USING BTREE,
                                        CONSTRAINT `registration_record_ibfk_1` FOREIGN KEY (`schedule_id`) REFERENCES `doctor_schedule` (`schedule_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                        CONSTRAINT `registration_record_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                        CONSTRAINT `registration_record_ibfk_3` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                        CONSTRAINT `registration_record_ibfk_4` FOREIGN KEY (`type_id`) REFERENCES `registration_type` (`type_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '挂号记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of registration_record
-- ----------------------------
INSERT INTO `registration_record` VALUES
                                      (一,1, 1, 1, 1, 1, '202510291010001', '2025-10-29 08:30:00', 2, 50.00, 2.50, NULL, '301诊室', '2025-10-29 09:15:00', NULL, NULL, 0, NULL),
                                      (二,2, 1, 2, 1, 1, '202510291010002', '2025-10-29 08:35:00', 2, 50.00, 2.50, NULL, '301诊室', '2025-10-29 09:30:00', NULL, NULL, 0, NULL),
                                      (三,3, 3, 3, 1, 2, '202510251010003', '2025-10-25 14:00:00', 2, 100.00, 5.00, NULL, '302诊室', '2025-10-25 15:00:00', NULL, NULL, 0, NULL),
                                      (四,4, 4, 4, 3, 1, '202510301020001', '2025-10-30 08:00:00', 1, 50.00, 5.00, NULL, '201诊室', NULL, NULL, NULL, 0, NULL),
                                      (五,5, 4, 5, 3, 1, '202510301020002', '2025-10-30 08:05:00', 1, 50.00, 2.50, NULL, '201诊室', NULL, NULL, NULL, 0, NULL),
                                      (六,6, 4, 6, 3, 1, '202510301020003', '2025-10-30 08:10:00', 1, 50.00, 5.00, NULL, '201诊室', NULL, NULL, NULL, 0, NULL),
                                      (七,7, 5, 7, 3, 1, '202510311020004', '2025-10-31 13:00:00', 1, 50.00, 2.50, NULL, '201诊室', NULL, NULL, NULL, 0, NULL),
                                      (八,8, 5, 8, 3, 1, '202510311020005', '2025-10-31 13:05:00', 1, 50.00, 5.00, NULL, '201诊室', NULL, NULL, NULL, 0, NULL),
                                      (九,9, 6, 9, 4, 2, '202510301020006', '2025-10-30 13:00:00', 1, 100.00, 5.00, NULL, '202诊室', NULL, NULL, NULL, 0, NULL),
                                      (十,10, 7, 10, 5, 1, '202510301030001', '2025-10-30 08:00:00', 1, 50.00, 5.00, NULL, '101诊室', NULL, NULL, NULL, 0, NULL),
                                      (十一,11, 7, 11, 5, 1, '202510301030002', '2025-10-30 08:05:00', 1, 50.00, 2.50, NULL, '101诊室', NULL, NULL, NULL, 0, NULL),
                                      (十二,12, 7, 12, 5, 1, '202510301030003', '2025-10-30 08:10:00', 1, 50.00, 2.50, NULL, '101诊室', NULL, NULL, NULL, 0, NULL),
                                      (十三,13, 7, 1, 5, 1, '202510301030004', '2025-10-30 08:15:00', 1, 50.00, 2.50, NULL, '101诊室', NULL, NULL, NULL, 0, NULL),
                                      (十四,14, 8, 2, 6, 1, '202510311030005', '2025-10-31 08:00:00', 1, 50.00, 2.50, NULL, '102诊室', NULL, NULL, NULL, 0, NULL),
                                      (十五,15, 8, 3, 6, 1, '202510311030006', '2025-10-31 08:05:00', 1, 50.00, 2.50, NULL, '102诊室', NULL, NULL, NULL, 0, NULL),
                                      (十六,16, 9, 4, 7, 2, '202510301040001', '2025-10-30 08:00:00', 1, 100.00, 10.00, NULL, '401诊室', NULL, NULL, NULL, 0, NULL),
                                      (十七,17, 9, 5, 7, 2, '202510301040002', '2025-10-30 08:05:00', 1, 100.00, 5.00, NULL, '401诊室', NULL, NULL, NULL, 0, NULL),
                                      (十八,18, 9, 6, 7, 2, '202510301040003', '2025-10-30 08:10:00', 1, 100.00, 10.00, NULL, '401诊室', NULL, NULL, NULL, 0, NULL),
                                      (十九,19, 10, 7, 8, 1, '202510311040004', '2025-10-31 13:00:00', 1, 50.00, 2.50, NULL, '402诊室', NULL, NULL, NULL, 0, NULL),
                                      (二十,20, 11, 8, 9, 1, '202510301050001', '2025-10-30 08:00:00', 1, 50.00, 5.00, NULL, '501诊室', NULL, NULL, NULL, 0, NULL),
                                      (二十一,21, 11, 9, 9, 1, '202510301050002', '2025-10-30 08:05:00', 1, 50.00, 2.50, NULL, '501诊室', NULL, NULL, NULL, 0, NULL),
                                      (二十二,22, 12, 10, 10, 1, '202510311050003', '2025-10-31 13:00:00', 1, 50.00, 5.00, NULL, '502诊室', NULL, NULL, NULL, 0, NULL),
                                      (二十三,23, 12, 11, 10, 1, '202510311050004', '2025-10-31 13:05:00', 1, 50.00, 2.50, NULL, '502诊室', NULL, NULL, NULL, 0, NULL),
                                      (二十四,24, 12, 12, 10, 1, '202510311050005', '2025-10-31 13:10:00', 1, 50.00, 2.50, NULL, '502诊室', NULL, NULL, NULL, 0, NULL),
                                      (二十五,25, 13, 1, 11, 2, '202510301060001', '2025-10-30 08:00:00', 1, 100.00, 5.00, NULL, '601诊室', NULL, NULL, NULL, 0, NULL);

-- ----------------------------
-- Table structure for registration_type
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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '号源类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of registration_type
-- ----------------------------
INSERT INTO `registration_type` VALUES
                                    (1, '普通号', 50.00, 2.50, 5.00, 50, 1, '2025-10-17 10:54:17', '2025-10-17 10:54:17'),
                                    (2, '专家号', 100.00, 5.00, 10.00, 20, 1, '2025-10-17 10:54:17', '2025-10-17 10:54:17'),
                                    (3, '特需号', 200.00, 10.00, 20.00, 10, 1, '2025-10-17 10:54:17', '2025-10-17 10:54:17'),
                                    (4, '急诊号', 80.00, 4.00, 8.00, 30, 1, '2025-10-24 09:00:00', '2025-10-24 09:00:00'),
                                    (5, '专科号', 60.00, 3.00, 6.00, 40, 1, '2025-10-24 10:00:00', '2025-10-24 10:00:00'),
                                    (6, '名医号', 150.00, 7.50, 15.00, 15, 1, '2025-10-24 11:00:00', '2025-10-24 11:00:00'),
                                    (7, '会诊号', 300.00, 15.00, 30.00, 5, 1, '2025-10-24 12:00:00', '2025-10-24 12:00:00'),
                                    (8, '复查号', 40.00, 2.00, 4.00, 60, 1, '2025-10-24 13:00:00', '2025-10-24 13:00:00'),
                                    (9, '体检号', 120.00, 6.00, 12.00, 25, 1, '2025-10-24 14:00:00', '2025-10-24 14:00:00'),
                                    (10, '咨询号', 30.00, 1.50, 3.00, 80, 1, '2025-10-24 15:00:00', '2025-10-24 15:00:00'),
                                    (11, '中医号', 70.00, 3.50, 7.00, 35, 1, '2025-10-24 16:00:00', '2025-10-24 16:00:00'),
                                    (12, '康复号', 45.00, 2.25, 4.50, 45, 1, '2025-10-24 17:00:00', '2025-10-24 17:00:00'),
                                    (13, '心理号', 90.00, 4.50, 9.00, 20, 1, '2025-10-24 18:00:00', '2025-10-24 18:00:00'),
                                    (14, '营养号', 55.00, 2.75, 5.50, 30, 1, '2025-10-24 19:00:00', '2025-10-24 19:00:00'),
                                    (15, '护理号', 25.00, 1.25, 2.50, 100, 1, '2025-10-24 20:00:00', '2025-10-24 20:00:00');

-- ----------------------------
-- Table structure for waiting_queue
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '候补队列表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of waiting_queue
-- ----------------------------
INSERT INTO `waiting_queue` VALUES
                                (1, 1, 3, 3, 1, '2025-10-29 08:40:00', 1, '2025-10-29 09:00:00'),
                                (2, 4, 6, 6, 1, '2025-10-30 08:15:00', 0, NULL),
                                (3, 7, 12, 12, 1, '2025-10-30 08:20:00', 0, NULL),
                                (4, 7, 1, 13, 2, '2025-10-30 08:25:00', 0, NULL),
                                (5, 8, 3, 15, 1, '2025-10-31 08:10:00', 0, NULL),
                                (6, 9, 6, 18, 1, '2025-10-30 08:15:00', 0, NULL),
                                (7, 10, 7, 19, 1, '2025-10-31 13:05:00', 0, NULL),
                                (8, 11, 9, 21, 1, '2025-10-30 08:10:00', 0, NULL),
                                (9, 12, 11, 23, 1, '2025-10-31 13:10:00', 0, NULL),
                                (10, 12, 12, 24, 2, '2025-10-31 13:15:00', 0, NULL),
                                (11, 13, 1, 25, 1, '2025-10-30 08:05:00', 0, NULL),
                                (12, 4, 7, 26, 2, '2025-10-30 08:20:00', 0, NULL),
                                (13, 4, 8, 27, 3, '2025-10-30 08:25:00', 0, NULL),
                                (14, 5, 9, 28, 1, '2025-10-31 13:10:00', 0, NULL),
                                (15, 5, 10, 29, 2, '2025-10-31 13:15:00', 0, NULL),
                                (16, 6, 11, 30, 1, '2025-10-30 13:05:00', 0, NULL),
                                (17, 6, 12, 31, 2, '2025-10-30 13:10:00', 0, NULL),
                                (18, 7, 2, 32, 3, '2025-10-30 08:30:00', 0, NULL),
                                (19, 7, 3, 33, 4, '2025-10-30 08:35:00', 0, NULL),
                                (20, 8, 4, 34, 2, '2025-10-31 08:15:00', 0, NULL);

SET FOREIGN_KEY_CHECKS = 1;