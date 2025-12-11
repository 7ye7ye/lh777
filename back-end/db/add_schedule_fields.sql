-- 为doctor_schedule表添加max_quota和room_number字段
-- 如果字段已存在，则不会报错（使用IF NOT EXISTS）

ALTER TABLE `doctor_schedule` 
ADD COLUMN IF NOT EXISTS `max_quota` int NULL DEFAULT 50 COMMENT '最大号源数' AFTER `used_quota`,
ADD COLUMN IF NOT EXISTS `room_number` varchar(20) NULL DEFAULT NULL COMMENT '诊室号' AFTER `max_quota`;

-- 如果数据库不支持IF NOT EXISTS，可以使用以下方式（先检查再添加）：
-- 注意：MySQL 8.0.19+ 支持 IF NOT EXISTS，旧版本需要手动检查

-- 更新现有记录的max_quota默认值（如果为NULL）
UPDATE `doctor_schedule` SET `max_quota` = 50 WHERE `max_quota` IS NULL;

