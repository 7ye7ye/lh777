-- 添加默认就诊卡字段
ALTER TABLE `patient` ADD COLUMN `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认就诊卡（0-否；1-是，默认0）' AFTER `is_deleted`;

