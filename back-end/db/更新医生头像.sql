-- ============================================
-- 更新医生头像脚本
-- 为所有医生添加合适的头像图标路径
-- 根据科室类型选择不同的图标
-- ============================================

SET NAMES utf8mb4;

-- ============================================
-- 头像路径说明：
-- 使用前端 static 目录下的图标
-- 根据科室特点选择不同的医生图标
-- ============================================

-- 内科系统医生头像（使用通用医生图标）
UPDATE `doctor` SET `avatar` = '/static/doctor.svg' WHERE `dept_id` IN (11, 12, 13, 14, 15) AND `avatar` IS NULL;

-- 外科系统医生头像（使用通用医生图标）
UPDATE `doctor` SET `avatar` = '/static/doctor.svg' WHERE `dept_id` IN (21, 22, 23, 24) AND `avatar` IS NULL;

-- 预防保健科医生头像（使用健康相关图标）
UPDATE `doctor` SET `avatar` = '/static/images/health-bg.png' WHERE `dept_id` IN (31, 32, 33) AND `avatar` IS NULL;

-- 体检科医生头像（使用检查相关图标）
UPDATE `doctor` SET `avatar` = '/static/check.svg' WHERE `dept_id` IN (41, 42, 43) AND `avatar` IS NULL;

-- 口腔科医生头像（使用通用医生图标）
UPDATE `doctor` SET `avatar` = '/static/doctor.svg' WHERE `dept_id` IN (51, 52, 53) AND `avatar` IS NULL;

-- B超室医生头像（使用检查相关图标）
UPDATE `doctor` SET `avatar` = '/static/check.svg' WHERE `dept_id` IN (61, 62, 63) AND `avatar` IS NULL;

-- 护理科医生头像（使用通用医生图标，可区分护士）
UPDATE `doctor` SET `avatar` = '/static/doctor.svg' WHERE `dept_id` IN (71, 72, 73) AND `avatar` IS NULL;

-- 公疗报销医生头像（使用信息相关图标）
UPDATE `doctor` SET `avatar` = '/static/info_message.png' WHERE `dept_id` IN (81, 82, 83) AND `avatar` IS NULL;

-- ============================================
-- 验证更新结果
-- ============================================
-- 检查每个科室的医生数量和头像设置情况
SELECT 
    d.dept_id AS '科室ID',
    d.dept_name AS '科室名称',
    COUNT(do.doctor_id) AS '医生数量',
    SUM(CASE WHEN do.avatar IS NULL THEN 1 ELSE 0 END) AS '未设置头像数量',
    SUM(CASE WHEN do.avatar IS NOT NULL THEN 1 ELSE 0 END) AS '已设置头像数量'
FROM department d
LEFT JOIN doctor do ON d.dept_id = do.dept_id AND do.is_active = 1
WHERE d.dept_level = 2
GROUP BY d.dept_id, d.dept_name
ORDER BY d.dept_id;

-- 查询所有医生及其头像
SELECT 
    do.doctor_id,
    do.doctor_name AS '医生姓名',
    d.dept_name AS '科室',
    do.title AS '职称',
    do.avatar AS '头像路径'
FROM doctor do
LEFT JOIN department d ON do.dept_id = d.dept_id
WHERE do.is_active = 1
ORDER BY d.dept_id, do.doctor_id;



