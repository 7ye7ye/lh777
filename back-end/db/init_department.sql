-- ============================================
-- 科室表初始化脚本
-- 用于在hospital数据库中创建department表并插入完整的科室数据
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for department (科室表)
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '科室唯一标识',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '科室名称（如"内科""消化内科"）',
  `parent_dept_id` bigint NULL DEFAULT NULL COMMENT '父科室ID（顶级科室为NULL，如"内科"的父科室为NULL，"消化内科"父科室为内科ID）',
  `dept_level` tinyint NOT NULL COMMENT '科室级别（1-一级科室；2-二级科室）',
  `dept_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科室简介（如"负责消化系统疾病诊疗"）',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科室位置（如"门诊楼3层东侧"，用于导航扩展功能）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE,
  UNIQUE INDEX `dept_name`(`dept_name` ASC) USING BTREE,
  INDEX `parent_dept_id`(`parent_dept_id` ASC) USING BTREE,
  CONSTRAINT `department_ibfk_1` FOREIGN KEY (`parent_dept_id`) REFERENCES `department` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 300 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '科室表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of department
-- ----------------------------

-- 一级科室
INSERT INTO `department` VALUES (1, '内科', NULL, 1, '综合内科诊疗服务', '门诊楼2层', NOW(), NOW());
INSERT INTO `department` VALUES (2, '外科', NULL, 1, '综合外科诊疗服务', '门诊楼3层', NOW(), NOW());
INSERT INTO `department` VALUES (3, '预防保健科', NULL, 1, '疾病预防与健康保健服务', '门诊楼1层', NOW(), NOW());
INSERT INTO `department` VALUES (4, '体检科', NULL, 1, '健康体检服务', '体检中心', NOW(), NOW());
INSERT INTO `department` VALUES (5, '口腔科', NULL, 1, '口腔疾病诊疗服务', '门诊楼2层', NOW(), NOW());
INSERT INTO `department` VALUES (6, 'B超室', NULL, 1, 'B超检查服务', '门诊楼2层', NOW(), NOW());
INSERT INTO `department` VALUES (7, '护理科', NULL, 1, '护理服务', '门诊楼各层', NOW(), NOW());
INSERT INTO `department` VALUES (8, '公疗报销', NULL, 1, '公费医疗报销服务', '门诊楼1层', NOW(), NOW());

-- 二级科室 - 内科系统
INSERT INTO `department` VALUES (11, '呼吸内科', 1, 2, '咳嗽、哮喘、肺部疾病诊疗', '内科门诊', NOW(), NOW());
INSERT INTO `department` VALUES (12, '心内科', 1, 2, '心脏疾病、高血压诊疗', '内科门诊', NOW(), NOW());
INSERT INTO `department` VALUES (13, '消化内科', 1, 2, '胃痛、腹泻、肠胃炎等消化系统疾病', '内科门诊', NOW(), NOW());
INSERT INTO `department` VALUES (14, '神经内科', 1, 2, '脑血管病、头痛、失眠诊疗', '内科门诊', NOW(), NOW());
INSERT INTO `department` VALUES (15, '内分泌科', 1, 2, '糖尿病、甲状腺疾病等内分泌疾病诊疗', '内科门诊', NOW(), NOW());

-- 二级科室 - 外科系统
INSERT INTO `department` VALUES (21, '骨科', 2, 2, '骨折、关节炎、颈腰椎病诊疗', '外科门诊', NOW(), NOW());
INSERT INTO `department` VALUES (22, '皮肤科', 2, 2, '皮炎、湿疹、皮肤病诊疗', '外科门诊', NOW(), NOW());
INSERT INTO `department` VALUES (23, '普通外科', 2, 2, '痔疮、胆囊炎、阑尾炎等外科疾病', '外科门诊', NOW(), NOW());
INSERT INTO `department` VALUES (24, '外伤处理', 2, 2, '外伤、扭伤、创伤处理', '外科门诊', NOW(), NOW());

-- 二级科室 - 体检科
INSERT INTO `department` VALUES (41, '常规体检', 4, 2, '健康体检服务', '体检中心', NOW(), NOW());
INSERT INTO `department` VALUES (42, '入职体检', 4, 2, '入职健康体检服务', '体检中心', NOW(), NOW());
INSERT INTO `department` VALUES (43, '专项体检', 4, 2, '专项健康检查服务', '体检中心', NOW(), NOW());

-- 二级科室 - 预防保健科
INSERT INTO `department` VALUES (31, '儿童保健', 3, 2, '儿童健康管理、疫苗接种', '预防保健科', NOW(), NOW());
INSERT INTO `department` VALUES (32, '妇女保健', 3, 2, '妇女健康管理、妇科检查', '预防保健科', NOW(), NOW());
INSERT INTO `department` VALUES (33, '老年人保健', 3, 2, '老年人健康管理、慢病随访', '预防保健科', NOW(), NOW());

-- 二级科室 - 口腔科
INSERT INTO `department` VALUES (51, '口腔内科', 5, 2, '龋齿、牙髓炎、根管治疗', '口腔科门诊', NOW(), NOW());
INSERT INTO `department` VALUES (52, '口腔外科', 5, 2, '拔牙、口腔小手术', '口腔科门诊', NOW(), NOW());
INSERT INTO `department` VALUES (53, '口腔修复', 5, 2, '补牙、烤瓷牙、义齿', '口腔科门诊', NOW(), NOW());

-- 二级科室 - B超室
INSERT INTO `department` VALUES (61, '腹部B超', 6, 2, '肝、胆、胰、脾、肾等腹部脏器检查', 'B超室', NOW(), NOW());
INSERT INTO `department` VALUES (62, '妇科B超', 6, 2, '妇科疾病、孕检B超', 'B超室', NOW(), NOW());
INSERT INTO `department` VALUES (63, '心脏彩超', 6, 2, '心脏功能、心脏疾病检查', 'B超室', NOW(), NOW());

-- 二级科室 - 护理科
INSERT INTO `department` VALUES (71, '门诊护理', 7, 2, '门诊输液、换药、护理服务', '门诊各层', NOW(), NOW());
INSERT INTO `department` VALUES (72, '住院护理', 7, 2, '住院患者护理服务', '住院部', NOW(), NOW());
INSERT INTO `department` VALUES (73, '社区护理', 7, 2, '上门护理、家庭病床', '社区服务', NOW(), NOW());

-- 二级科室 - 公疗报销
INSERT INTO `department` VALUES (81, '门诊报销', 8, 2, '门诊费用报销办理', '公疗报销窗口', NOW(), NOW());
INSERT INTO `department` VALUES (82, '住院报销', 8, 2, '住院费用报销办理', '公疗报销窗口', NOW(), NOW());
INSERT INTO `department` VALUES (83, '慢病报销', 8, 2, '慢性病费用报销办理', '公疗报销窗口', NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;



