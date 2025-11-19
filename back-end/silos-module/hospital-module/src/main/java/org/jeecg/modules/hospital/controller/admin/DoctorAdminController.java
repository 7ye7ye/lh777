package org.jeecg.modules.hospital.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.HosUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医生管理控制器（管理员端）
 */
@RestController
@RequestMapping("/admin/doctor")
@Tag(name = "管理员-医生管理")
public class DoctorAdminController {

    @Resource
    private DoctorService doctorService;
    
    @Resource
    private HosUserService hosUserService;

    /**
     * 分页获取医生列表
     */
    @Operation(summary = "分页获取医生列表")
    @GetMapping("/list")
    public Result<IPage<Doctor>> list(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId) {
        
        Page<Doctor> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Doctor::getDoctorName, keyword).or().like(Doctor::getSpecialty, keyword);
        }
        
        if (deptId != null) {
            queryWrapper.eq(Doctor::getDeptId, deptId);
        }
        
        queryWrapper.orderByDesc(Doctor::getDoctorId);
        
        IPage<Doctor> pageResult = doctorService.page(page, queryWrapper);
        return Result.OK(pageResult);
    }

    /**
     * 获取医生详情
     */
    @Operation(summary = "获取医生详情")
    @GetMapping("/detail/{doctorId}")
    public Result<Doctor> detail(@PathVariable Long doctorId) {
        Doctor doctor = doctorService.getById(doctorId);
        if (doctor == null) {
            return Result.error("医生不存在");
        }
        return Result.OK(doctor);
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
    public Result<?> update(@RequestBody Doctor doctor) {
        if (doctor.getDoctorId() == null) {
            return Result.error("医生ID不能为空");
        }
        
        // 检查医生是否存在
        Doctor existing = doctorService.getById(doctor.getDoctorId());
        if (existing == null) {
            return Result.error("医生不存在");
        }
        
        if (doctor.getUserId() != null) {
            // 检查用户是否存在
            HosUser user = hosUserService.getById(doctor.getUserId());
            if (user == null) {
                return Result.error("关联用户不存在");
            }
        }
        
        boolean success = doctorService.updateById(doctor);
        if (success) {
            return Result.OK("更新成功");
        } else {
            return Result.error("更新失败");
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
}