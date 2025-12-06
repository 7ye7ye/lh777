
-- ----------------------------
-- Table structure for hospital_referral (转诊申请)
-- ----------------------------
DROP TABLE IF EXISTS `hospital_referral`;
CREATE TABLE `hospital_referral` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `referral_code` VARCHAR(40) NOT NULL COMMENT '转诊单编号',
  `patient_name` VARCHAR(50) NOT NULL COMMENT '患者姓名',
  `gender` VARCHAR(5) DEFAULT NULL COMMENT '性别',
  `age` INT DEFAULT NULL COMMENT '年龄',
  `phone` VARCHAR(30) NOT NULL COMMENT '联系电话',
  `symptoms` TEXT COMMENT '症状描述',
  `medical_history` TEXT COMMENT '既往病史',
  `reason` TEXT COMMENT '转诊原因',
  `source_type` VARCHAR(32) DEFAULT 'PATIENT_AFTER' COMMENT '申请来源(医生/患者)',
  `target_type` VARCHAR(32) DEFAULT 'INTERNAL' COMMENT '转诊目标类型(INTERNAL/EXTERNAL)',
  `target_dept_id` BIGINT DEFAULT NULL COMMENT '目标科室ID(校内)',
  `target_dept_name` VARCHAR(120) DEFAULT NULL COMMENT '目标科室名称',
  `target_hospital_name` VARCHAR(120) DEFAULT NULL COMMENT '目标医院名称',
  `quota_action` VARCHAR(32) DEFAULT NULL COMMENT '名额处理策略(DIRECT/WAITLIST/EXTERNAL)',
  `assigned_schedule_id` BIGINT DEFAULT NULL COMMENT '占用的排班ID',
  `assigned_date` DATE DEFAULT NULL COMMENT '排班日期',
  `assigned_time_slot` TINYINT DEFAULT NULL COMMENT '时间段(1-上午2-下午3-晚上)',
  `wait_number` INT DEFAULT NULL COMMENT '候补序号',
  `status` VARCHAR(32) DEFAULT 'PENDING' COMMENT '状态',
  `review_doctor` VARCHAR(60) DEFAULT NULL COMMENT '审核医生',
  `review_comments` TEXT COMMENT '审核意见',
  `reject_reason` TEXT COMMENT '拒绝原因',
  `cancel_reason` TEXT COMMENT '取消原因',
  `attachments` LONGTEXT COMMENT '附件(JSON)',
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_referral_code` (`referral_code`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转诊申请';