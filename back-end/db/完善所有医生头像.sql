-- ============================================
-- 完善所有医生头像和数据脚本
-- 确保每个二级科室都有3名医生，并设置合适的头像
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 第一部分：为所有现有医生更新头像
-- ============================================

-- 内科系统医生（呼吸内科11、心内科12、消化内科13、神经内科14、内分泌科15）
UPDATE `doctor` SET `avatar` = '/static/doctor.svg' 
WHERE `dept_id` IN (11, 12, 13, 14, 15) AND (`avatar` IS NULL OR `avatar` = '');

-- 外科系统医生（骨科21、皮肤科22、普通外科23、外伤处理24）
UPDATE `doctor` SET `avatar` = '/static/doctor.svg' 
WHERE `dept_id` IN (21, 22, 23, 24) AND (`avatar` IS NULL OR `avatar` = '');

-- 预防保健科医生（儿童保健31、妇女保健32、老年人保健33）
UPDATE `doctor` SET `avatar` = '/static/images/health-bg.png' 
WHERE `dept_id` IN (31, 32, 33) AND (`avatar` IS NULL OR `avatar` = '');

-- 体检科医生（常规体检41、入职体检42、专项体检43）
UPDATE `doctor` SET `avatar` = '/static/check.svg' 
WHERE `dept_id` IN (41, 42, 43) AND (`avatar` IS NULL OR `avatar` = '');

-- 口腔科医生（口腔内科51、口腔外科52、口腔修复53）
UPDATE `doctor` SET `avatar` = '/static/doctor.svg' 
WHERE `dept_id` IN (51, 52, 53) AND (`avatar` IS NULL OR `avatar` = '');

-- B超室医生（腹部B超61、妇科B超62、心脏彩超63）
UPDATE `doctor` SET `avatar` = '/static/check.svg' 
WHERE `dept_id` IN (61, 62, 63) AND (`avatar` IS NULL OR `avatar` = '');

-- 护理科医生（门诊护理71、住院护理72、社区护理73）
UPDATE `doctor` SET `avatar` = '/static/doctor.svg' 
WHERE `dept_id` IN (71, 72, 73) AND (`avatar` IS NULL OR `avatar` = '');

-- 公疗报销医生（门诊报销81、住院报销82、慢病报销83）
UPDATE `doctor` SET `avatar` = '/static/info_message.png' 
WHERE `dept_id` IN (81, 82, 83) AND (`avatar` IS NULL OR `avatar` = '');

-- ============================================
-- 第二部分：确保所有科室都有3名医生（如果需要补充）
-- ============================================
-- 注意：此部分需要在完善科室和医生数据.sql执行后运行
-- 这里只检查并标记需要补充的科室

-- 查找医生数量不足3人的科室
SELECT 
    d.dept_id AS '科室ID',
    d.dept_name AS '科室名称',
    COUNT(do.doctor_id) AS '当前医生数量',
    CASE 
        WHEN COUNT(do.doctor_id) < 3 THEN '需要补充'
        ELSE '已满足'
    END AS '状态',
    3 - COUNT(do.doctor_id) AS '需要补充数量'
FROM department d
LEFT JOIN doctor do ON d.dept_id = do.dept_id AND do.is_active = 1
WHERE d.dept_level = 2
GROUP BY d.dept_id, d.dept_name
HAVING COUNT(do.doctor_id) < 3
ORDER BY d.dept_id;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 最终验证：检查所有科室的医生和头像
-- ============================================
SELECT 
    d.dept_id AS '科室ID',
    d.dept_name AS '科室名称',
    COUNT(do.doctor_id) AS '医生总数',
    SUM(CASE WHEN do.avatar IS NULL OR do.avatar = '' THEN 1 ELSE 0 END) AS '无头像数量',
    SUM(CASE WHEN do.avatar IS NOT NULL AND do.avatar != '' THEN 1 ELSE 0 END) AS '有头像数量'
FROM department d
LEFT JOIN doctor do ON d.dept_id = do.dept_id AND do.is_active = 1
WHERE d.dept_level = 2
GROUP BY d.dept_id, d.dept_name
ORDER BY d.dept_id;



