package org.jeecg.modules.hospital.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "获取所有科室列表")
    @GetMapping("/list")
    public Result<List<Department>> getAllDepartments() {
        List<Department> list = departmentService.list();
        return Result.OK(list);
    }
//
//    @Operation(summary = "创建科室")
//    @PostMapping("/create")
//    public Result<Boolean> createDepartment(@RequestBody Department department) {
//        boolean result = departmentService.createDepartment(department);
//        return Result.OK(result);
//    }
//
//    @Operation(summary = "更新科室信息")
//    @PutMapping("/update")
//    public Result<Boolean> updateDepartment(@RequestBody Department department) {
//        boolean result = departmentService.updateDepartment(department);
//        return Result.OK(result);
//    }
//
//    @Operation(summary = "删除科室")
//    @DeleteMapping("/{deptId}")
//    public Result<Boolean> deleteDepartment(@PathVariable Long deptId) {
//        boolean result = departmentService.deleteDepartment(deptId);
//        return Result.OK(result);
//    }
}
