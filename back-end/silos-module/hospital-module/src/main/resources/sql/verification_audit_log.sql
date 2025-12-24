-- 创建身份认证审核日志表
CREATE TABLE `verification_audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` bigint(20) NOT NULL COMMENT '患者ID',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `action` varchar(20) NOT NULL COMMENT '操作类型：SUBMIT-提交申请, RESUBMIT-重新提交, APPROVE-审核通过, REJECT-审核驳回',
  `description` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='身份认证审核日志表';
