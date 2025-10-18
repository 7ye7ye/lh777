package org.jeecg.modules.hospital.controller.applet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小程序端-科室查询控制器
 */
@RestController
@RequestMapping("/applet/department")
@Tag(name = "小程序-科室查询")
public class DepartmentAppletController {

    @Autowired
    private DepartmentService departmentService;

    @Operation(summary = "获取科室树形结构（一级+二级）")
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getDepartmentTree() {
        List<Map<String, Object>> tree = departmentService.getDepartmentTree();
        return Result.OK(tree);
    }

    @Operation(summary = "根据父科室ID获取二级科室")
    @GetMapping("/second-level")
    public Result<List<Department>> getSecondLevelDepartments(@RequestParam Long parentDeptId) {
        List<Department> departments = departmentService.getSecondLevelByParentId(parentDeptId);
        return Result.OK(departments);
    }

    @Operation(summary = "搜索科室")
    @GetMapping("/search")
    public Result<List<Department>> searchDepartments(@RequestParam String keyword) {
        List<Department> departments = departmentService.searchDepartments(keyword);
        return Result.OK(departments);
    }

    @Operation(summary = "获取科室详情")
    @GetMapping("/{deptId}")
    public Result<Department> getDepartmentDetail(@PathVariable Long deptId) {
        Department department = departmentService.getById(deptId);
        return Result.OK(department);
    }
}
