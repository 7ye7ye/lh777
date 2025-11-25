package org.jeecg.modules.hospital.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web端-管理员科室管理控制器
 */
@RestController
@RequestMapping("/admin/department")
@Tag(name = "管理员-科室管理")
public class DepartmentAdminController {

    @Autowired
    private DepartmentService departmentService;

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
        boolean result = departmentService.save(department);
        return Result.ok(result);
    }

    @Operation(summary = "更新科室信息")
    @PutMapping("/update")
    public Result<Boolean> updateDepartment(@RequestBody Department department) {
        boolean result = departmentService.updateById(department);
        return Result.ok(result);
    }

    @Operation(summary = "删除科室")
    @DeleteMapping("/{deptId}")
    public Result<Boolean> deleteDepartment(@PathVariable Long deptId) {
        boolean result = departmentService.removeById(deptId);
        return Result.ok(result);
    }
}
