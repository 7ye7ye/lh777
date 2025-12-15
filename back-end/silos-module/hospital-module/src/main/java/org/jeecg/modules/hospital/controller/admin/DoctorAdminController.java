package org.jeecg.modules.hospital.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.jeecg.modules.hospital.service.HosUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医生管理控制器（管理员端）
 */
@DS("hospital")
@Slf4j
@RestController
@RequestMapping("/admin/doctor")
@Tag(name = "管理员-医生管理")
public class DoctorAdminController {

    @Resource
    private DoctorService doctorService;
    
    @Resource
    private DepartmentService departmentService;
    
    @Resource
    private HosUserService hosUserService;

    /**
     * 分页获取医生列表
     */
    @Operation(summary = "分页获取医生列表")
    @GetMapping("/list")
    public Result<IPage<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer isActive) {
        
        Page<Doctor> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            // 使用and包装or条件，确保or只影响姓名和专长的查询，不影响其他条件
            queryWrapper.and(wrapper -> wrapper
                .like(Doctor::getDoctorName, keyword)
                .or()
                .like(Doctor::getSpecialty, keyword)
            );
        }
        
        if (deptId != null) {
            // 检查是否为一级科室
            Department department = departmentService.getById(deptId);
            if (department != null && department.getDeptLevel() == 1) {
                // 如果是一级科室，查询该科室下所有二级科室
                List<Department> subDepartments = departmentService.getSecondLevelByParentId(deptId);
                if (subDepartments != null && !subDepartments.isEmpty()) {
                    // 提取所有二级科室ID
                    List<Long> subDeptIds = new ArrayList<>();
                    for (Department subDept : subDepartments) {
                        subDeptIds.add(subDept.getDeptId());
                    }
                    // 查询所有二级科室的医生
                    queryWrapper.in(Doctor::getDeptId, subDeptIds);
                } else {
                    // 重要修复：如果一级科室没有子科室，设置一个不可能匹配的条件，确保不返回任何医生
                    queryWrapper.eq(Doctor::getDeptId, -1);
                }
            } else {
                // 如果是二级科室或找不到科室信息，使用原有的精确查询
                queryWrapper.eq(Doctor::getDeptId, deptId);
            }
        }
        
        if (StringUtils.hasText(title)) {
            queryWrapper.eq(Doctor::getTitle, title);
        }
        
        if (isActive != null) {
            queryWrapper.eq(Doctor::getIsActive, isActive);
        }
        
        queryWrapper.orderByDesc(Doctor::getDoctorId);

        
        IPage<Doctor> pageResult = doctorService.page(page, queryWrapper);
        
        // 转换为带科室名称的Map列表
        IPage<Map<String, Object>> resultPage = new Page<>(pageNo, pageSize);
        resultPage.setTotal(pageResult.getTotal());
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (Doctor doctor : pageResult.getRecords()) {
            Map<String, Object> doctorMap = convertDoctorToMap(doctor);
            records.add(doctorMap);
        }
        
        resultPage.setRecords(records);
        return Result.OK(resultPage);
    }
    
    /**
     * 获取医生详情
     */
    @Operation(summary = "获取医生详情")
    @GetMapping("/detail/{doctorId}")
    public Result<Map<String, Object>> detail(@PathVariable Long doctorId) {
        Doctor doctor = doctorService.getById(doctorId);
        if (doctor == null) {
            return Result.error("医生不存在");
        }
        Map<String, Object> doctorMap = convertDoctorToMap(doctor);
        return Result.OK(doctorMap);
    }
    
    /**
     * 将Doctor对象转换为带科室名称的Map
     */
    private Map<String, Object> convertDoctorToMap(Doctor doctor) {
        Map<String, Object> map = new HashMap<>();
        map.put("doctorId", doctor.getDoctorId());
        map.put("userId", doctor.getUserId());
        map.put("deptId", doctor.getDeptId());
        map.put("title", doctor.getTitle());
        map.put("specialty", doctor.getSpecialty());
        map.put("doctorDesc", doctor.getDoctorDesc());
        map.put("avatar", doctor.getAvatar());
        map.put("isActive", doctor.getIsActive());
        map.put("updateVerify", doctor.getUpdateVerify());
        map.put("doctorName", doctor.getDoctorName());
        
        // 添加科室名称信息
        if (doctor.getDeptId() != null) {
            Department department = departmentService.getById(doctor.getDeptId());
            if (department != null) {
                map.put("deptName", department.getDeptName());
            }
        }
        
        return map;
    }



    /**
     * 新增医生
     */
    @Operation(summary = "新增医生")
    @PostMapping("/add")
    public Result<?> add(@RequestBody Doctor doctor) {
        // 验证必要字段
        if (!StringUtils.hasText(doctor.getDoctorName())) {
            return Result.error("医生姓名不能为空");
        }
        
        if (doctor.getUserId() != null) {
            // 检查用户是否存在
            HosUser user = hosUserService.getById(doctor.getUserId());
            if (user == null) {
                return Result.error("关联用户不存在");
            }
        }
        
        // 设置默认值
        if (doctor.getIsActive() == null) {
            doctor.setIsActive(1); // 默认启用
        }
        
        boolean success = doctorService.save(doctor);
        if (success) {
            return Result.OK("添加成功");
        } else {
            return Result.error("添加失败");
        }
    }

    /**
     * 更新医生信息
     */
    @Operation(summary = "更新医生信息")
    @PutMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> update(@RequestBody Map<String, Object> data) {
        log.info("开始更新医生信息: {}", data);
        try {
            // 获取医生ID
            Long doctorId = null;
            try {
                doctorId = Long.valueOf(data.get("doctorId").toString());
            } catch (Exception e) {
                log.error("医生ID格式错误: {}", e.getMessage());
            Result<HashMap<String, Object>> result = new Result<>();
            result.setSuccess(false);
            result.setCode(400); // SC_BAD_REQUEST_400
            result.setMessage("医生ID不能为空且必须为数字");
            result.setResult(new HashMap<String, Object>());
            return result;
            }
            
            if (doctorId == null) {
                Result<HashMap<String, Object>> result = new Result<>();
            result.setSuccess(false);
            result.setCode(400); // SC_BAD_REQUEST_400
            result.setMessage("医生ID不能为空");
            result.setResult(new HashMap<String, Object>());
            return result;
            }
            
            // 检查医生是否存在
            Doctor existing = doctorService.getById(doctorId);
            if (existing == null) {
                log.warn("医生不存在, doctorId: {}", doctorId);
            Result<HashMap<String, Object>> result = new Result<>();
            result.setSuccess(false);
            result.setCode(CommonConstant.SC_INTERNAL_NOT_FOUND_404);
            result.setMessage("医生不存在");
            result.setResult(new HashMap<String, Object>());
            return result;
            }
            
            // 更新Doctor表信息
            Doctor doctor = new Doctor();
            doctor.setDoctorId(doctorId);
            
            // 设置Doctor相关字段
            if (data.containsKey("doctorName")) {
                doctor.setDoctorName(data.get("doctorName").toString());
            }
            if (data.containsKey("deptId")) {
                doctor.setDeptId(Long.valueOf(data.get("deptId").toString()));
            }
            if (data.containsKey("title")) {
                doctor.setTitle(data.get("title").toString());
            }
            if (data.containsKey("specialty")) {
                doctor.setSpecialty(data.get("specialty").toString());
            }
            if (data.containsKey("doctorDesc")) {
                doctor.setDoctorDesc(data.get("doctorDesc").toString());
            }
            if (data.containsKey("avatar")) {
                doctor.setAvatar(data.get("avatar").toString());
            }
            if (data.containsKey("isActive")) {
                doctor.setIsActive(Integer.valueOf(data.get("isActive").toString()));
            }
            if (data.containsKey("updateVerify")) {
                doctor.setUpdateVerify(Integer.valueOf(data.get("updateVerify").toString()));
            }
            
            // 处理用户ID变更
            Long userId = null;
            if (data.containsKey("userId")) {
                userId = Long.valueOf(data.get("userId").toString());
                doctor.setUserId(userId);
            } else {
                userId = existing.getUserId();
            }
            
            // 检查用户是否存在
            if (userId != null) {
                HosUser user = hosUserService.getById(userId);
                if (user == null) {
                    log.warn("关联用户不存在, userId: {}", userId);
                Result<HashMap<String, Object>> result = new Result<>();
                result.setSuccess(false);
                result.setCode(CommonConstant.SC_INTERNAL_NOT_FOUND_404);
                result.setMessage("关联用户不存在");
                result.setResult(new HashMap<String, Object>());
                return result;
                }
                
                // 更新HosUser表信息（如果有相关字段）
                if (data.containsKey("userAccount") || data.containsKey("email")) {
                    boolean hasUserUpdate = false;
                    
                    if (data.containsKey("userAccount")) {
                        user.setUserAccount(data.get("userAccount").toString());
                        hasUserUpdate = true;
                    }
                    if (data.containsKey("email")) {
                        user.setEmail(data.get("email").toString());
                        hasUserUpdate = true;
                    }
                    
                    if (hasUserUpdate) {
                        user.setUpdateTime(LocalDateTime.now());
                        hosUserService.updateById(user);
                        log.info("更新用户信息成功, userId: {}", userId);
                    }
                }
            }
            
            // 执行Doctor表更新
            boolean success = doctorService.updateById(doctor);
            if (success) {
                log.info("更新医生信息成功, doctorId: {}", doctorId);
                Result<HashMap<String, Object>> result = new Result<>();
                result.setSuccess(true);
                result.setCode(CommonConstant.SC_OK_200);
                result.setMessage("更新成功");
                result.setResult(new HashMap<String, Object>());
                return result;
            } else {
                log.error("更新医生信息失败, doctorId: {}", doctorId);
                Result<HashMap<String, Object>> result = new Result<>();
                result.setSuccess(false);
                result.setCode(CommonConstant.SC_INTERNAL_SERVER_ERROR_500);
                result.setMessage("更新失败");
                result.setResult(new HashMap<String, Object>());
                return result;
            }
        } catch (Exception e) {
            log.error("更新医生信息异常: {}", e.getMessage(), e);
            // 创建并返回一个标准格式的错误响应对象
            Result<HashMap<String, Object>> result = new Result<>();
            result.setSuccess(false);
            result.setCode(CommonConstant.SC_INTERNAL_SERVER_ERROR_500);
            result.setMessage("系统异常，请稍后重试");
            // 避免返回空数据，提供一个空对象
            result.setResult(new HashMap<String, Object>());
            return result;
        }
    }

    /**
     * 删除医生
     */
    @Operation(summary = "删除医生")
    @DeleteMapping("/delete/{doctorId}")
    public Result<?> delete(@PathVariable Long doctorId) {
        // 检查医生是否存在
        Doctor doctor = doctorService.getById(doctorId);
        if (doctor == null) {
            return Result.error("医生不存在");
        }
        
        boolean success = doctorService.removeById(doctorId);
        if (success) {
            return Result.OK("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 批量删除医生
     */
    @Operation(summary = "批量删除医生")
    @DeleteMapping("/delete/batch")
    public Result<?> batchDelete(@RequestBody List<Long> doctorIds) {
        if (doctorIds == null || doctorIds.isEmpty()) {
            return Result.error("请选择要删除的医生");
        }
        
        boolean success = doctorService.removeByIds(doctorIds);
        if (success) {
            return Result.OK("批量删除成功");
        } else {
            return Result.error("批量删除失败");
        }
    }

    /**
     * 切换医生状态（启用/禁用）
     */
    @Operation(summary = "切换医生状态")
    @PutMapping("/status/{doctorId}")
    public Result<?> changeStatus(@PathVariable Long doctorId) {
        Doctor doctor = doctorService.getById(doctorId);
        if (doctor == null) {
            return Result.error("医生不存在");
        }
        
        // 切换状态
        doctor.setIsActive(doctor.getIsActive() == 1 ? 0 : 1);
        boolean success = doctorService.updateById(doctor);
        
        if (success) {
            return Result.OK("状态更新成功");
        } else {
            return Result.error("状态更新失败");
        }
    }

    /**
     * 获取医生统计信息
     */
    @Operation(summary = "获取医生统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总医生数
        stats.put("totalCount", doctorService.count());
        
        // 启用的医生数
        stats.put("activeCount", doctorService.count(new LambdaQueryWrapper<Doctor>().eq(Doctor::getIsActive, 1)));
        
        // 禁用的医生数
        stats.put("inactiveCount", doctorService.count(new LambdaQueryWrapper<Doctor>().eq(Doctor::getIsActive, 0)));
        
        return Result.OK(stats);
    }

    @Operation(summary = "按科室查询医生")
    @GetMapping("/list/by-department")
    public Result<List<Doctor>> listByDepartment(@RequestParam("departmentId") Long departmentId) {
        List<Doctor> doctors = doctorService.getDoctorsByDeptId(departmentId);
        return Result.OK(doctors);
    }
}