package org.jeecg.modules.hospital.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Web端-管理员科室管理控制器
 */
@RestController
@RequestMapping("/admin/department")
@Tag(name = "管理员-科室管理")
public class DepartmentAdminController {

    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private DoctorScheduleService doctorScheduleService;

    @Operation(summary = "获取科室列表（支持筛选）")
    @GetMapping("/list")
    public Result<List<Department>> getDepartmentList(
            @RequestParam(required = false) Integer current,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) Integer deptLevel) {
        // 创建查询条件
        LambdaQueryWrapper<Department> queryWrapper = new LambdaQueryWrapper<>();
        if (deptName != null && !deptName.isEmpty()) {
            queryWrapper.like(Department::getDeptName, deptName);
        }
        if (deptLevel != null) {
            queryWrapper.eq(Department::getDeptLevel, deptLevel);
        }
        
        // 执行查询
        List<Department> list = departmentService.list(queryWrapper);
        
        // 直接返回科室列表数组，适配前端期望的数据结构
        return Result.ok(list);
    }

    @Operation(summary = "获取科室详情")
    @GetMapping("/{deptId}")
    public Result<Department> getDepartmentDetail(@PathVariable Long deptId) {
        Department department = departmentService.getById(deptId);
        return department != null ? Result.ok(department) : Result.error(500, "科室不存在");
    }

    @Operation(summary = "创建科室")
    @PostMapping("/create")
    public Result<Boolean> createDepartment(@RequestBody Department department) {
        try {
            boolean result = departmentService.save(department);
            return Result.ok(result);
        } catch (Exception e) {
            String errorMsg = getFriendlyErrorMessage(e);
            return Result.error(errorMsg);
        }
    }

    @Operation(summary = "更新科室信息")
    @PutMapping("/update")
    public Result<Boolean> updateDepartment(@RequestBody Department department) {
        try {
            boolean result = departmentService.updateById(department);
            return Result.ok(result);
        } catch (Exception e) {
            String errorMsg = getFriendlyErrorMessage(e);
            return Result.error(errorMsg);
        }
    }

    @Operation(summary = "删除科室")
    @DeleteMapping("/{deptId}")
    public Result<Boolean> deleteDepartment(@PathVariable Long deptId) {
        try {
            // 先检查科室是否存在
            Department department = departmentService.getById(deptId);
            if (department == null) {
                return Result.error("科室不存在");
            }
            
            // 如果是一级科室，检查是否有二级子科室
            if (department.getDeptLevel() != null && department.getDeptLevel() == 1) {
                List<Department> subDepartments = departmentService.getSecondLevelByParentId(deptId);
                if (subDepartments != null && !subDepartments.isEmpty()) {
                    return Result.error("该一级科室下存在" + subDepartments.size() + "个二级科室，无法删除。请先删除所有二级科室后再删除该科室");
                }
            }
            
            // 检查是否有医生关联该科室
            List<Doctor> doctors = doctorService.getDoctorsByDeptId(deptId);
            if (doctors != null && !doctors.isEmpty()) {
                return Result.error("该科室下存在" + doctors.size() + "位医生，无法删除。请先移除该科室下的所有医生后再删除");
            }
            
            // 检查是否有排班记录关联该科室
            LambdaQueryWrapper<DoctorSchedule> scheduleQuery = new LambdaQueryWrapper<>();
            scheduleQuery.eq(DoctorSchedule::getDeptId, deptId);
            long scheduleCount = doctorScheduleService.count(scheduleQuery);
            if (scheduleCount > 0) {
                return Result.error("该科室下存在" + scheduleCount + "条排班记录，无法删除。请先删除所有排班记录后再删除该科室");
            }
            
            // 执行删除
            boolean result = departmentService.removeById(deptId);
            if (result) {
                return Result.ok("删除成功");
            } else {
                return Result.error("删除失败，请稍后重试");
            }
        } catch (Exception e) {
            String errorMsg = getFriendlyErrorMessage(e, deptId);
            return Result.error(errorMsg);
        }
    }

    /**
     * 将SQL异常转换为用户友好的错误提示
     */
    private String getFriendlyErrorMessage(Exception e) {
        return getFriendlyErrorMessage(e, null);
    }
    
    /**
     * 将SQL异常转换为用户友好的错误提示
     * @param e 异常
     * @param deptId 科室ID（用于进一步检查）
     */
    private String getFriendlyErrorMessage(Exception e, Long deptId) {
        String message = e.getMessage();
        if (message == null) {
            return "操作失败，请稍后重试";
        }

        // 检查是否是SQL完整性约束违反异常
        Throwable cause = e.getCause();
        while (cause != null && !(cause instanceof SQLIntegrityConstraintViolationException)) {
            cause = cause.getCause();
        }

        if (cause instanceof SQLIntegrityConstraintViolationException) {
            SQLIntegrityConstraintViolationException sqlException = (SQLIntegrityConstraintViolationException) cause;
            String sqlMessage = sqlException.getMessage();
            
            if (sqlMessage != null) {
                // 检查是否是重复的科室名称
                if (sqlMessage.contains("Duplicate entry") && sqlMessage.contains("dept_name")) {
                    // 提取科室名称
                    String deptName = extractDeptNameFromError(sqlMessage);
                    if (deptName != null) {
                        return "科室名称 \"" + deptName + "\" 已存在，请使用其他名称";
                    }
                    return "科室名称已存在，请使用其他名称";
                }
                
                // 检查是否是外键约束
                if (sqlMessage.contains("foreign key constraint") || 
                    sqlMessage.contains("Cannot delete or update") ||
                    sqlMessage.contains("a foreign key constraint fails")) {
                    // 尝试进一步判断是医生还是子科室
                    if (deptId != null) {
                        try {
                            Department dept = departmentService.getById(deptId);
                            if (dept != null && dept.getDeptLevel() != null && dept.getDeptLevel() == 1) {
                                // 如果是一级科室，检查是否有子科室
                                List<Department> subDepartments = departmentService.getSecondLevelByParentId(deptId);
                                if (subDepartments != null && !subDepartments.isEmpty()) {
                                    return "该一级科室下存在" + subDepartments.size() + "个二级科室，无法删除。请先删除所有二级科室后再删除该科室";
                                }
                            }
                            // 检查是否有医生
                            List<Doctor> doctors = doctorService.getDoctorsByDeptId(deptId);
                            if (doctors != null && !doctors.isEmpty()) {
                                return "该科室下存在" + doctors.size() + "位医生，无法删除。请先移除该科室下的所有医生后再删除";
                            }
                            // 检查是否有排班记录
                            LambdaQueryWrapper<DoctorSchedule> scheduleQuery = new LambdaQueryWrapper<>();
                            scheduleQuery.eq(DoctorSchedule::getDeptId, deptId);
                            long scheduleCount = doctorScheduleService.count(scheduleQuery);
                            if (scheduleCount > 0) {
                                return "该科室下存在" + scheduleCount + "条排班记录，无法删除。请先删除所有排班记录后再删除该科室";
                            }
                        } catch (Exception ex) {
                            // 如果检查失败，使用通用提示
                        }
                    }
                    // 通用外键约束错误提示
                    return "该科室存在关联数据，无法删除。请先删除所有关联的二级科室、医生和排班记录后再删除";
                }
            }
        }

        // 检查错误消息中是否包含重复条目信息
        if (message.contains("Duplicate entry") && message.contains("dept_name")) {
            String deptName = extractDeptNameFromError(message);
            if (deptName != null) {
                return "科室名称 \"" + deptName + "\" 已存在，请使用其他名称";
            }
            return "科室名称已存在，请使用其他名称";
        }

        // 检查是否是外键约束错误
        if (message.contains("foreign key constraint") || 
            message.contains("Cannot delete or update") ||
            message.contains("a foreign key constraint fails")) {
            // 尝试进一步判断是医生还是子科室
            if (deptId != null) {
                try {
                    Department dept = departmentService.getById(deptId);
                    if (dept != null && dept.getDeptLevel() != null && dept.getDeptLevel() == 1) {
                        // 如果是一级科室，检查是否有子科室
                        List<Department> subDepartments = departmentService.getSecondLevelByParentId(deptId);
                        if (subDepartments != null && !subDepartments.isEmpty()) {
                            return "该一级科室下存在" + subDepartments.size() + "个二级科室，无法删除。请先删除所有二级科室后再删除该科室";
                        }
                    }
                    // 检查是否有医生
                    List<Doctor> doctors = doctorService.getDoctorsByDeptId(deptId);
                    if (doctors != null && !doctors.isEmpty()) {
                        return "该科室下存在" + doctors.size() + "位医生，无法删除。请先移除该科室下的所有医生后再删除";
                    }
                    // 检查是否有排班记录
                    LambdaQueryWrapper<DoctorSchedule> scheduleQuery = new LambdaQueryWrapper<>();
                    scheduleQuery.eq(DoctorSchedule::getDeptId, deptId);
                    long scheduleCount = doctorScheduleService.count(scheduleQuery);
                    if (scheduleCount > 0) {
                        return "该科室下存在" + scheduleCount + "条排班记录，无法删除。请先删除所有排班记录后再删除该科室";
                    }
                } catch (Exception ex) {
                    // 如果检查失败，使用通用提示
                }
            }
            // 通用外键约束错误提示
            return "该科室存在关联数据，无法删除。请先删除所有关联的二级科室、医生和排班记录后再删除";
        }

        // 默认错误提示
        return "操作失败，请稍后重试";
    }

    /**
     * 从错误消息中提取科室名称
     */
    private String extractDeptNameFromError(String errorMessage) {
        try {
            // 错误消息格式类似：Duplicate entry '眼科' for key 'dept_name'
            int startIndex = errorMessage.indexOf("'");
            if (startIndex >= 0) {
                int endIndex = errorMessage.indexOf("'", startIndex + 1);
                if (endIndex > startIndex) {
                    return errorMessage.substring(startIndex + 1, endIndex);
                }
            }
        } catch (Exception e) {
            // 忽略提取失败
        }
        return null;
    }
}
