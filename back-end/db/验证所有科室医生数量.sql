-- ============================================
-- 验证所有科室的医生数量
-- 检查哪些科室缺少医生
-- ============================================

SELECT 
    d.dept_id AS '科室ID',
    d.dept_name AS '科室名称',
    COUNT(do.doctor_id) AS '医生数量',
    CASE 
        WHEN COUNT(do.doctor_id) = 0 THEN '❌ 没有医生'
        WHEN COUNT(do.doctor_id) < 3 THEN '⚠️ 医生不足3名'
        WHEN COUNT(do.doctor_id) = 3 THEN '✅ 医生数量正常'
        ELSE '❓ 医生超过3名'
    END AS '状态'
FROM department d
LEFT JOIN doctor do ON d.dept_id = do.dept_id AND do.is_active = 1
WHERE d.dept_level = 2
GROUP BY d.dept_id, d.dept_name
ORDER BY 
    CASE 
        WHEN COUNT(do.doctor_id) = 0 THEN 1
        WHEN COUNT(do.doctor_id) < 3 THEN 2
        ELSE 3
    END,
    d.dept_id;



