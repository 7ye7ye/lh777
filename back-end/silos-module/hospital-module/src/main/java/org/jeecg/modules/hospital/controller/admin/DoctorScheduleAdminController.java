package org.jeecg.modules.hospital.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.controller.request.DoctorScheduleCreateRequest;
import org.jeecg.modules.hospital.controller.request.DoctorScheduleExcelImportDTO;
import org.jeecg.modules.hospital.controller.request.DoctorScheduleDirectImportDTO;
import org.jeecg.modules.hospital.controller.request.DoctorScheduleUpdateRequest;
import org.jeecg.modules.hospital.entity.Doctor;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.entity.Department;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.jeecg.modules.hospital.service.DoctorService;
import org.jeecg.modules.hospital.service.DepartmentService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 管理员-医生排班管理控制器
 */
@DS("hospital")
@Slf4j
@RestController
@RequestMapping("/admin/schedule")
@Tag(name = "管理员-医生排班管理")
public class DoctorScheduleAdminController {

    @Resource
    private DoctorScheduleService scheduleService;

    @Resource
    private DoctorService doctorService;

    @Resource
    private DepartmentService departmentService;

    @Operation(summary = "查询排班列表（可按医生/科室/日期筛选）")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("查询排班列表 - doctorId: {}, deptId: {}, date: {}, startDate: {}, endDate: {}", 
                doctorId, deptId, date, startDate, endDate);
        List<DoctorSchedule> list = scheduleService.list(doctorId, deptId, date, startDate, endDate);
        log.info("查询结果数量: {}", list.size());
        // 转换为包含医生和科室名称的Map列表
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (DoctorSchedule schedule : list) {
            log.debug("排班记录 - scheduleId: {}, date: {}, doctorId: {}, deptId: {}", 
                    schedule.getScheduleId(), schedule.getScheduleDate(), schedule.getDoctorId(), schedule.getDeptId());
            Map<String, Object> map = new HashMap<>();
            map.put("scheduleId", schedule.getScheduleId());
            map.put("doctorId", schedule.getDoctorId());
            map.put("deptId", schedule.getDeptId());
            map.put("scheduleDate", schedule.getScheduleDate() != null ? schedule.getScheduleDate().toString() : null);
            map.put("timeSlot", schedule.getTimeSlot());
            map.put("usedQuota", schedule.getUsedQuota());
            map.put("maxQuota", schedule.getMaxQuota());
            map.put("status", schedule.getStatus());
            map.put("roomNumber", schedule.getRoomNumber());
            map.put("createTime", schedule.getCreateTime());
            map.put("updateTime", schedule.getUpdateTime());
            
            // 获取医生名称
            if (schedule.getDoctorId() != null) {
                Doctor doctor = doctorService.getById(schedule.getDoctorId());
                if (doctor != null) {
                    map.put("doctorName", doctor.getDoctorName());
                }
            }
            
            // 获取科室名称
            if (schedule.getDeptId() != null) {
                Department dept = departmentService.getById(schedule.getDeptId());
                if (dept != null) {
                    map.put("deptName", dept.getDeptName());
                }
            }
            
            resultList.add(map);
        }
        return Result.OK(resultList);
    }

    @Operation(summary = "根据排班ID获取详情")
    @GetMapping("/{scheduleId:\\d+}")
    public Result<DoctorSchedule> detail(@PathVariable Long scheduleId) {
        DoctorSchedule s = scheduleService.getById(scheduleId);
        return Result.OK(s);
    }

    @Operation(summary = "创建排班")
    @PostMapping("/create")
    public Result<DoctorSchedule> create(@RequestBody DoctorScheduleCreateRequest req) {
        DoctorSchedule s = new DoctorSchedule();
        s.setDoctorId(req.getDoctorId());
        s.setDeptId(req.getDeptId());
        s.setDate(LocalDate.parse(req.getDate()));
        
        // 解析时段：优先从shift字符串解析，支持中文和英文
        Integer timeSlot = parseTimeSlotFromShift(req.getShift());
        if (timeSlot == null || timeSlot < 1 || timeSlot > 3) {
            // 如果shift解析失败，使用默认值
            log.warn("无法从shift解析timeSlot: {}, 使用默认值1", req.getShift());
            timeSlot = 1; // 默认上午
        }
        s.setTimeSlot(timeSlot);
        log.info("创建排班 - doctorId: {}, deptId: {}, date: {}, timeSlot: {}, shift: {}", 
                req.getDoctorId(), req.getDeptId(), req.getDate(), timeSlot, req.getShift());
        
        // 设置maxQuota
        if (req.getMaxQuota() != null) {
            s.setMaxQuota(req.getMaxQuota());
        } else if (req.getSlots() != null) {
            // 如果没有maxQuota，使用slots作为maxQuota
            s.setMaxQuota(req.getSlots());
        }
        
        // 设置roomNumber：如果没有指定，则随机分配一个可用诊室
        if (req.getRoomNumber() != null && !req.getRoomNumber().trim().isEmpty()) {
            s.setRoomNumber(req.getRoomNumber());
        } else {
            // 随机分配诊室（创建新排班时，没有原诊室号）
            LocalDate scheduleDate = LocalDate.parse(req.getDate());
            String availableRoom = getAvailableRoom(scheduleDate, timeSlot, null);
            s.setRoomNumber(availableRoom);
        }
        
        // 设置默认值
        s.setUsedQuota(0);
        s.setStatus(1);
        s.setCreateTime(LocalDateTime.now());
        s.setUpdateTime(LocalDateTime.now());
        
        // 再次确认timeSlot不为null
        if (s.getTimeSlot() == null) {
            log.error("timeSlot在Controller中为null，强制设置为1");
            s.setTimeSlot(1);
        }
        
        log.info("调用create前 - timeSlot: {}, 所有字段: doctorId={}, deptId={}, date={}, timeSlot={}, maxQuota={}, roomNumber={}", 
                s.getTimeSlot(), s.getDoctorId(), s.getDeptId(), s.getScheduleDate(), 
                s.getTimeSlot(), s.getMaxQuota(), s.getRoomNumber());
        
        DoctorSchedule created = scheduleService.create(s);
        return Result.OK(created);
    }

    @Operation(summary = "更新排班")
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody DoctorScheduleUpdateRequest req) {
        DoctorSchedule s = new DoctorSchedule();
        s.setScheduleId(req.getScheduleId());
        s.setDoctorId(req.getDoctorId());
        s.setDeptId(req.getDeptId());
        if (req.getDate() != null && !req.getDate().isEmpty()) {
            s.setDate(LocalDate.parse(req.getDate()));
        }
        
        // 解析时段：优先使用timeSlot，如果没有则从shift字符串转换
        Integer timeSlot = null;
        if (req.getTimeSlot() != null && req.getTimeSlot() >= 1 && req.getTimeSlot() <= 3) {
            // 直接使用timeSlot
            timeSlot = req.getTimeSlot();
            log.info("更新排班 - scheduleId: {}, 使用timeSlot: {}", req.getScheduleId(), timeSlot);
        } else if (req.getShift() != null && !req.getShift().trim().isEmpty()) {
            // 从shift字符串转换
            timeSlot = parseTimeSlotFromShift(req.getShift());
            if (timeSlot != null && timeSlot >= 1 && timeSlot <= 3) {
                log.info("更新排班 - scheduleId: {}, shift: {}, 转换后timeSlot: {}", 
                        req.getScheduleId(), req.getShift(), timeSlot);
            } else {
                log.warn("无法从shift解析timeSlot: {}, 保持原值", req.getShift());
            }
        }
        
        // 如果timeSlot有效，则设置
        if (timeSlot != null && timeSlot >= 1 && timeSlot <= 3) {
            s.setTimeSlot(timeSlot);
        } else {
            log.warn("更新排班 - scheduleId: {}, timeSlot无效或未提供，保持原值", req.getScheduleId());
        }
        
        s.setStatus(req.getStatus());
        // 设置maxQuota（如果Request中有这些字段）
        if (req.getMaxQuota() != null) {
            s.setMaxQuota(req.getMaxQuota());
        } else if (req.getSlots() != null) {
            // 如果没有maxQuota，使用slots作为maxQuota
            s.setMaxQuota(req.getSlots());
        }
        
        // 设置roomNumber：如果没有指定，则根据原排班的诊室号保持楼层不变
        if (req.getRoomNumber() != null && !req.getRoomNumber().trim().isEmpty()) {
            s.setRoomNumber(req.getRoomNumber());
        } else {
            // 获取原排班记录，提取原诊室号
            DoctorSchedule original = scheduleService.getById(req.getScheduleId());
            String originalRoomNumber = (original != null && original.getRoomNumber() != null) 
                ? original.getRoomNumber() 
                : null;
            
            // 获取新的日期和时段
            LocalDate scheduleDate = original != null && original.getScheduleDate() != null
                ? original.getScheduleDate()
                : (req.getDate() != null && !req.getDate().isEmpty() 
                    ? LocalDate.parse(req.getDate()) 
                    : LocalDate.now());
            Integer finalTimeSlot = s.getTimeSlot() != null ? s.getTimeSlot() 
                : (original != null ? original.getTimeSlot() : timeSlot);
            
            // 随机分配诊室，保持原楼层不变
            String availableRoom = getAvailableRoom(scheduleDate, finalTimeSlot, originalRoomNumber);
            s.setRoomNumber(availableRoom);
            log.info("更新排班时自动分配诊室 - scheduleId: {}, 原诊室: {}, 新诊室: {}", 
                    req.getScheduleId(), originalRoomNumber, availableRoom);
        }
        
        boolean ok = scheduleService.update(s);
        return Result.OK(ok);
    }

    @Operation(summary = "删除排班")
    @DeleteMapping("/{scheduleId:\\d+}")
    public Result<Boolean> delete(@PathVariable Long scheduleId) {
        boolean ok = scheduleService.delete(scheduleId);
        return Result.OK(ok);
    }

    @Operation(summary = "获取可用诊室（随机分配）")
    @GetMapping("/available-room")
    public Result<String> getAvailableRoom(
            @RequestParam String date,
            @RequestParam Integer timeSlot,
            @RequestParam(required = false) String originalRoomNumber) {
        try {
            LocalDate scheduleDate = LocalDate.parse(date);
            String room = getAvailableRoom(scheduleDate, timeSlot, originalRoomNumber);
            return Result.OK(room);
        } catch (Exception e) {
            log.error("获取可用诊室失败", e);
            return Result.error("获取可用诊室失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用诊室（内部方法）
     * 根据日期和时段，查找未被占用的诊室，随机返回一个
     * 格式：门诊+楼层+门牌号，如"门诊101"
     * 如果提供了原诊室号，保持楼层不变
     */
    private String getAvailableRoom(LocalDate date, Integer timeSlot, String originalRoomNumber) {
        // 提取楼层信息（如果提供了原诊室号）
        Integer targetFloor = null;
        if (originalRoomNumber != null && !originalRoomNumber.trim().isEmpty()) {
            targetFloor = extractFloorFromRoomNumber(originalRoomNumber.trim());
            log.info("从原诊室号 {} 提取楼层: {}", originalRoomNumber, targetFloor);
        }
        
        // 生成该楼层的所有诊室号（格式：门诊+楼层+门牌号）
        List<String> candidateRooms = new ArrayList<>();
        if (targetFloor != null) {
            // 保持原楼层，生成该楼层的诊室号（101-109, 201-209等）
            for (int i = 1; i <= 9; i++) {
                candidateRooms.add(String.format("门诊%d%02d", targetFloor, i));
            }
            log.info("保持楼层 {} 不变，候选诊室: {}", targetFloor, candidateRooms);
        } else {
            // 没有原诊室号，生成所有楼层的诊室号（1-5层，每层9个诊室）
            for (int floor = 1; floor <= 5; floor++) {
                for (int i = 1; i <= 9; i++) {
                    candidateRooms.add(String.format("门诊%d%02d", floor, i));
                }
            }
            log.info("未指定楼层，生成所有楼层候选诊室，总数: {}", candidateRooms.size());
        }
        
        // 查询该日期和时段已占用的诊室
        LambdaQueryWrapper<DoctorSchedule> query = new LambdaQueryWrapper<>();
        query.eq(DoctorSchedule::getScheduleDate, date)
             .eq(DoctorSchedule::getTimeSlot, timeSlot)
             .eq(DoctorSchedule::getStatus, 1)
             .isNotNull(DoctorSchedule::getRoomNumber)
             .ne(DoctorSchedule::getRoomNumber, "");
        List<DoctorSchedule> occupiedSchedules = scheduleService.list(query);
        
        Set<String> occupiedRooms = new HashSet<>();
        for (DoctorSchedule schedule : occupiedSchedules) {
            if (schedule.getRoomNumber() != null && !schedule.getRoomNumber().trim().isEmpty()) {
                occupiedRooms.add(schedule.getRoomNumber().trim());
            }
        }
        log.info("已占用诊室: {}", occupiedRooms);
        
        // 找出未占用的诊室
        List<String> availableRooms = new ArrayList<>();
        for (String room : candidateRooms) {
            if (!occupiedRooms.contains(room)) {
                availableRooms.add(room);
            }
        }
        
        // 如果有可用诊室，随机返回一个；否则返回该楼层的第一个诊室
        if (!availableRooms.isEmpty()) {
            java.util.Random random = new java.util.Random();
            String selectedRoom = availableRooms.get(random.nextInt(availableRooms.size()));
            log.info("随机选择可用诊室: {}", selectedRoom);
            return selectedRoom;
        } else {
            // 所有诊室都被占用，返回该楼层的第一个诊室
            String fallbackRoom = candidateRooms.isEmpty() ? "门诊101" : candidateRooms.get(0);
            log.warn("该楼层所有诊室都被占用，返回默认诊室: {}", fallbackRoom);
            return fallbackRoom;
        }
    }
    
    /**
     * 从诊室号中提取楼层信息
     * 格式：门诊+楼层+门牌号，如"门诊101" -> 1, "门诊201" -> 2
     */
    private Integer extractFloorFromRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = roomNumber.trim();
        // 匹配格式：门诊+楼层+门牌号，如"门诊101"、"门诊201"
        // 提取楼层（百位数字）
        if (trimmed.startsWith("门诊") && trimmed.length() >= 4) {
            try {
                // 获取"门诊"后面的数字，取百位
                String numberPart = trimmed.substring(2); // 去掉"门诊"前缀
                if (numberPart.length() >= 3) {
                    // 格式：XXX，取第一位作为楼层
                    int floor = Character.getNumericValue(numberPart.charAt(0));
                    if (floor >= 1 && floor <= 9) {
                        return floor;
                    }
                }
            } catch (Exception e) {
                log.warn("无法从诊室号 {} 提取楼层", roomNumber);
            }
        }
        
        // 兼容旧格式：A-101, B-201等
        if (trimmed.contains("-")) {
            String[] parts = trimmed.split("-");
            if (parts.length >= 2) {
                try {
                    String numberPart = parts[1];
                    if (numberPart.length() >= 3) {
                        int floor = Character.getNumericValue(numberPart.charAt(0));
                        if (floor >= 1 && floor <= 9) {
                            return floor;
                        }
                    }
                } catch (Exception e) {
                    log.warn("无法从诊室号 {} 提取楼层", roomNumber);
                }
            }
        }
        
        return null;
    }

    /**
     * 将shift字符串转换为timeSlot数字
     */
    private Integer parseTimeSlotFromShift(String shift) {
        if (shift == null) return 1;
        String s = shift.toLowerCase();
        if (s.contains("morning") || s.contains("上午") || s.equals("1")) {
            return 1;
        } else if (s.contains("afternoon") || s.contains("下午") || s.equals("2")) {
            return 2;
        } else if (s.contains("evening") || s.contains("晚上") || s.equals("3")) {
            return 3;
        }
        return 1; // 默认上午
    }


    @Operation(summary = "根据科室从doctor_schedule表获取医生列表")
    @GetMapping("/doctors-by-dept")
    public Result<List<Map<String, Object>>> getDoctorsByDeptFromSchedule(
            @RequestParam Long deptId
    ) {
        log.info("根据科室从doctor_schedule表获取医生列表 - deptId: {}", deptId);
        
        // 从doctor_schedule表中查询指定科室的所有排班记录
        List<DoctorSchedule> schedules = scheduleService.lambdaQuery()
                .eq(DoctorSchedule::getDeptId, deptId)
                .eq(DoctorSchedule::getStatus, 1)
                .list();
        
        log.info("查询到 {} 条排班记录", schedules.size());
        
        // 获取所有唯一的医生ID
        Set<Long> doctorIds = new HashSet<>();
        for (DoctorSchedule schedule : schedules) {
            if (schedule.getDoctorId() != null) {
                doctorIds.add(schedule.getDoctorId());
            }
        }
        
        log.info("去重后得到 {} 个医生ID", doctorIds.size());
        
        // 获取医生详细信息
        List<Map<String, Object>> doctorList = new ArrayList<>();
        for (Long doctorId : doctorIds) {
            Doctor doctor = doctorService.getById(doctorId);
            if (doctor != null) {
                Map<String, Object> doctorMap = new HashMap<>();
                doctorMap.put("doctorId", doctor.getDoctorId());
                doctorMap.put("doctorName", doctor.getDoctorName());
                doctorMap.put("deptId", doctor.getDeptId());
                doctorList.add(doctorMap);
            }
        }
        
        // 按医生姓名排序
        doctorList.sort((a, b) -> {
            String nameA = (String) a.get("doctorName");
            String nameB = (String) b.get("doctorName");
            return nameA != null && nameB != null ? nameA.compareTo(nameB) : 0;
        });
        
        log.info("返回 {} 个医生的信息", doctorList.size());
        return Result.OK(doctorList);
    }

    @Operation(summary = "按科室查询指定年月排班映射")
    @GetMapping("/month-by-dept")
    public Result<Map<String, List<Map<String, Integer>>>> monthByDept(
            @RequestParam Long deptId,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        log.info("按科室查询排班日历 - deptId: {}, year: {}, month: {}, start: {}, end: {}", 
                deptId, year, month, start, end);
        
        // 从doctor_schedule表查询指定科室的排班记录
        List<DoctorSchedule> list = scheduleService.lambdaQuery()
                .eq(DoctorSchedule::getDeptId, deptId)
                .ge(DoctorSchedule::getScheduleDate, start)
                .le(DoctorSchedule::getScheduleDate, end)
                .eq(DoctorSchedule::getStatus, 1)
                .orderByAsc(DoctorSchedule::getScheduleDate)
                .orderByAsc(DoctorSchedule::getTimeSlot)
                .list();
        
        log.info("查询到 {} 条排班记录", list.size());
        if (list.size() > 0) {
            log.debug("排班记录详情: {}", list.stream()
                    .map(ds -> String.format("scheduleId=%d, doctorId=%d, date=%s, timeSlot=%d", 
                            ds.getScheduleId(), ds.getDoctorId(), ds.getScheduleDate(), ds.getTimeSlot()))
                    .collect(java.util.stream.Collectors.joining(", ")));
        }
        
        Map<String, List<Map<String, Integer>>> resp = new HashMap<>();
        for (DoctorSchedule ds : list) {
            String key = ds.getScheduleDate().toString();
            List<Map<String, Integer>> arr = resp.computeIfAbsent(key, k -> new ArrayList<>());
            Map<String, Integer> obj = new HashMap<>();
            obj.put("timeSlot", ds.getTimeSlot());
            arr.add(obj);
        }
        
        log.info("返回排班映射，包含 {} 个日期", resp.size());
        return Result.OK(resp);
    }

    @Operation(summary = "按医生查询指定年月排班映射")
    @GetMapping("/month-by-doctor")
    public Result<Map<String, List<Map<String, Integer>>>> monthByDoctor(
            @RequestParam Long doctorId,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        log.info("按医生查询排班日历 - doctorId: {}, year: {}, month: {}, start: {}, end: {}", 
                doctorId, year, month, start, end);
        
        // 从doctor_schedule表查询指定医生的排班记录
        List<DoctorSchedule> list = scheduleService.lambdaQuery()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .ge(DoctorSchedule::getScheduleDate, start)
                .le(DoctorSchedule::getScheduleDate, end)
                .eq(DoctorSchedule::getStatus, 1)
                .orderByAsc(DoctorSchedule::getScheduleDate)
                .orderByAsc(DoctorSchedule::getTimeSlot)
                .list();
        
        log.info("查询到 {} 条排班记录", list.size());
        if (list.size() > 0) {
            log.debug("排班记录详情: {}", list.stream()
                    .map(ds -> String.format("scheduleId=%d, date=%s, timeSlot=%d", 
                            ds.getScheduleId(), ds.getScheduleDate(), ds.getTimeSlot()))
                    .collect(java.util.stream.Collectors.joining(", ")));
        }
        
        Map<String, List<Map<String, Integer>>> resp = new HashMap<>();
        for (DoctorSchedule ds : list) {
            String key = ds.getScheduleDate().toString();
            List<Map<String, Integer>> arr = resp.computeIfAbsent(key, k -> new ArrayList<>());
            Map<String, Integer> obj = new HashMap<>();
            obj.put("timeSlot", ds.getTimeSlot());
            arr.add(obj);
        }
        
        log.info("返回排班映射，包含 {} 个日期", resp.size());
        return Result.OK(resp);
    }

    @Operation(summary = "通过Excel或CSV导入排班数据")
    @PostMapping("/importExcel")
    public Result<?> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "durationMinutes", required = false, defaultValue = "30") Integer durationMinutes,
            @RequestParam(value = "timeSlotType", required = false) String timeSlotType,
            @RequestParam(value = "maxWorkDays", required = false, defaultValue = "1") Integer maxWorkDays) {
        if (file == null || file.isEmpty()) {
            log.warn("上传的文件为空");
            return Result.error("未找到上传的文件，请选择文件后重试");
        }

        // 使用默认值
        if (durationMinutes == null) {
            durationMinutes = 30;
        }
        if (maxWorkDays == null) {
            maxWorkDays = 1; // 默认1天，即不能连续工作两天
        }

        List<String> errorMessages = new ArrayList<>();
        List<String> adjustMessages = new ArrayList<>(); // 记录调整信息

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            return Result.error("文件名不能为空");
        }

        boolean isCsv = fileName.toLowerCase().endsWith(".csv");

        java.io.InputStream fileInputStream = null;
        try {
            ImportParams params = new ImportParams();
            params.setTitleRows(1); // 标题行数
            params.setHeadRows(1);  // 表头行数
            params.setNeedSave(false);

            // 先读取文件检测格式（通过表头判断）
            fileInputStream = file.getInputStream();
            if (fileInputStream == null) {
                return Result.error("无法读取文件，请检查文件是否损坏");
            }

            // 检测文件格式：尝试解析为直接导入格式（包含doctor_id等字段）
            // 将输入流转换为字节数组，以便多次读取
            byte[] fileBytes = toByteArray(fileInputStream);
            fileInputStream.close();

            boolean isDirectFormat = false;
            try {
                // 先尝试用直接导入格式解析
                java.io.InputStream testStream = new java.io.ByteArrayInputStream(fileBytes);
                List<DoctorScheduleDirectImportDTO> testList = ExcelImportUtil.importExcel(
                        testStream,
                        DoctorScheduleDirectImportDTO.class,
                        params
                );
                if (testList != null && !testList.isEmpty()) {
                    DoctorScheduleDirectImportDTO first = testList.get(0);
                    // 如果第一个记录有doctor_id字段，说明是直接导入格式
                    if (first.getDoctorId() != null) {
                        isDirectFormat = true;
                    }
                }
                testStream.close();
            } catch (Exception e) {
                // 解析失败，可能是旧格式，继续使用旧格式
                log.debug("检测到旧格式文件，使用名称导入方式: {}", e.getMessage());
            }

            // 重新创建输入流
            fileInputStream = new java.io.ByteArrayInputStream(fileBytes);

            // 根据格式选择不同的处理方式
            if (isDirectFormat) {
                // 直接导入格式（使用ID）
                return importDirectFormat(fileInputStream, params, fileName, errorMessages, adjustMessages);
            } else {
                // 名称导入格式（使用医生姓名、科室名称）
                return importByNameFormat(fileInputStream, isCsv, params, fileName, durationMinutes, maxWorkDays, errorMessages, adjustMessages);
            }
        } catch (Exception e) {
            log.error("导入文件失败，文件名：{}", fileName, e);
            String errorMsg = "文件导入失败：" + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += "，原因：" + e.getCause().getMessage();
            }
            e.printStackTrace();
            return Result.error(errorMsg);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.error("关闭文件流失败", e);
                }
            }
        }
    }

    /**
     * 直接导入格式（使用ID字段）
     */
    private Result<?> importDirectFormat(
            java.io.InputStream fileInputStream,
            ImportParams params,
            String fileName,
            List<String> errorMessages,
            List<String> adjustMessages) {
        try {
            List<DoctorScheduleDirectImportDTO> importList = ExcelImportUtil.importExcel(
                    fileInputStream,
                    DoctorScheduleDirectImportDTO.class,
                    params
            );

            if (importList == null || importList.isEmpty()) {
                return Result.error("文件内容为空，请检查文件格式是否正确");
            }

            int successCount = 0;
            int errorCount = 0;

            for (int i = 0; i < importList.size(); i++) {
                DoctorScheduleDirectImportDTO dto = importList.get(i);
                if (dto == null) {
                    errorMessages.add(String.format("第%d行：数据为空", i + 2));
                    errorCount++;
                    continue;
                }

                try {
                    // 验证必要字段
                    if (dto.getDoctorId() == null) {
                        errorMessages.add(String.format("第%d行：医生ID不能为空", i + 2));
                        errorCount++;
                        continue;
                    }
                    if (dto.getDeptId() == null) {
                        errorMessages.add(String.format("第%d行：科室ID不能为空", i + 2));
                        errorCount++;
                        continue;
                    }
                    if (dto.getScheduleDate() == null || dto.getScheduleDate().trim().isEmpty()) {
                        errorMessages.add(String.format("第%d行：排班日期不能为空", i + 2));
                        errorCount++;
                        continue;
                    }
                    if (dto.getTimeSlot() == null) {
                        errorMessages.add(String.format("第%d行：时段不能为空", i + 2));
                        errorCount++;
                        continue;
                    }

                    // 解析日期（支持 yyyy/MM/dd 和 yyyy-MM-dd 格式）
                    LocalDate scheduleDate;
                    try {
                        String dateStr = dto.getScheduleDate().trim();
                        if (dateStr.contains("/")) {
                            scheduleDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                        } else {
                            scheduleDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        }
                    } catch (Exception e) {
                        errorMessages.add(String.format("第%d行：日期格式错误'%s'，应为yyyy/MM/dd或yyyy-MM-dd", i + 2, dto.getScheduleDate()));
                        errorCount++;
                        continue;
                    }

                    // 验证医生和科室是否存在
                    Doctor doctor = doctorService.getById(dto.getDoctorId());
                    if (doctor == null) {
                        errorMessages.add(String.format("第%d行：医生ID'%d'不存在", i + 2, dto.getDoctorId()));
                        errorCount++;
                        continue;
                    }

                    Department dept = departmentService.getById(dto.getDeptId());
                    if (dept == null) {
                        errorMessages.add(String.format("第%d行：科室ID'%d'不存在", i + 2, dto.getDeptId()));
                        errorCount++;
                        continue;
                    }

                    // 创建或更新排班记录
                    DoctorSchedule schedule;
                    if (dto.getScheduleId() != null) {
                        // 更新现有记录
                        schedule = scheduleService.getById(dto.getScheduleId());
                        if (schedule == null) {
                            errorMessages.add(String.format("第%d行：排班ID'%d'不存在", i + 2, dto.getScheduleId()));
                            errorCount++;
                            continue;
                        }
                    } else {
                        // 创建新记录
                        schedule = new DoctorSchedule();
                        schedule.setCreateTime(LocalDateTime.now());
                    }

                    schedule.setDoctorId(dto.getDoctorId());
                    schedule.setDeptId(dto.getDeptId());
                    schedule.setScheduleDate(scheduleDate);
                    schedule.setTimeSlot(dto.getTimeSlot());
                    schedule.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
                    schedule.setTypeId(dto.getTypeId() != null ? dto.getTypeId() : 1);
                    schedule.setUsedQuota(dto.getUsedQuota() != null ? dto.getUsedQuota() : 0);
                    schedule.setMaxQuota(dto.getMaxQuota() != null ? dto.getMaxQuota() : 50);
                    schedule.setRoomNumber(dto.getRoomNumber());
                    schedule.setUpdateTime(LocalDateTime.now());

                    if (dto.getScheduleId() != null) {
                        scheduleService.updateById(schedule);
                    } else {
                        scheduleService.save(schedule);
                    }

                    successCount++;
                } catch (Exception e) {
                    log.error("处理第{}行数据时出错", i + 2, e);
                    errorMessages.add(String.format("第%d行：%s", i + 2, e.getMessage()));
                    errorCount++;
                }
            }

            StringBuilder resultMessage = new StringBuilder();
            resultMessage.append(String.format("导入完成：成功%d条，失败%d条", successCount, errorCount));
            if (errorCount > 0 && !errorMessages.isEmpty()) {
                resultMessage.append("\n错误信息：").append(String.join("; ", errorMessages));
            }

            if (errorCount > 0) {
                return Result.error(resultMessage.toString());
            } else {
                return Result.ok(resultMessage.toString());
            }
        } catch (Exception e) {
            log.error("直接导入格式处理失败", e);
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 名称导入格式（使用医生姓名、科室名称）
     */
    private Result<?> importByNameFormat(
            java.io.InputStream fileInputStream,
            boolean isCsv,
            ImportParams params,
            String fileName,
            Integer durationMinutes,
            Integer maxWorkDays,
            List<String> errorMessages,
            List<String> adjustMessages) {
        int successCount = 0;
        int errorCount = 0;

        try {
            List<DoctorScheduleExcelImportDTO> importList;
            if (isCsv) {
                importList = parseCsvFile(fileInputStream);
            } else {
                importList = ExcelImportUtil.importExcel(
                        fileInputStream,
                        DoctorScheduleExcelImportDTO.class,
                        params
                );
            }

            if (importList == null || importList.isEmpty()) {
                return Result.error("文件内容为空，请检查文件格式是否正确（至少需要表头和数据行）");
            }

            // 第一步：收集所有有效的排班数据（不直接保存）
            List<ScheduleData> scheduleDataList = new ArrayList<>();

            // 处理每条导入数据，先验证并收集
            for (int i = 0; i < importList.size(); i++) {
                DoctorScheduleExcelImportDTO dto = importList.get(i);
                if (dto == null) {
                    errorMessages.add(String.format("第%d行：数据为空", i + 2));
                    errorCount++;
                    continue;
                }
                try {
                    // 检查必要字段
                    if (dto.getDoctorName() == null || dto.getDoctorName().trim().isEmpty()) {
                        errorMessages.add(String.format("第%d行：医生姓名不能为空", i + 2));
                        errorCount++;
                        continue;
                    }
                    if (dto.getDeptName() == null || dto.getDeptName().trim().isEmpty()) {
                        errorMessages.add(String.format("第%d行：科室名称不能为空", i + 2));
                        errorCount++;
                        continue;
                    }
                    if (dto.getScheduleDate() == null || dto.getScheduleDate().trim().isEmpty()) {
                        errorMessages.add(String.format("第%d行：排班日期不能为空", i + 2));
                        errorCount++;
                        continue;
                    }
                    if (dto.getTimeSlotStr() == null || dto.getTimeSlotStr().trim().isEmpty()) {
                        errorMessages.add(String.format("第%d行：时段不能为空", i + 2));
                        errorCount++;
                        continue;
                    }

                    // 1. 根据医生姓名查找医生
                    LambdaQueryWrapper<Doctor> doctorQuery = new LambdaQueryWrapper<>();
                    doctorQuery.eq(Doctor::getDoctorName, dto.getDoctorName().trim());
                    List<Doctor> doctors = doctorService.list(doctorQuery);
                    if (doctors.isEmpty()) {
                        errorMessages.add(String.format("第%d行：医生'%s'不存在", i + 2, dto.getDoctorName()));
                        errorCount++;
                        continue;
                    }
                    Doctor doctor = doctors.get(0);

                    // 2. 根据科室名称查找科室
                    LambdaQueryWrapper<Department> deptQuery = new LambdaQueryWrapper<>();
                    deptQuery.eq(Department::getDeptName, dto.getDeptName().trim());
                    List<Department> departments = departmentService.list(deptQuery);
                    if (departments.isEmpty()) {
                        errorMessages.add(String.format("第%d行：科室'%s'不存在", i + 2, dto.getDeptName()));
                        errorCount++;
                        continue;
                    }
                    Department department = departments.get(0);

                    // 3. 解析日期
                    LocalDate scheduleDate;
                    try {
                        scheduleDate = LocalDate.parse(dto.getScheduleDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } catch (Exception e) {
                        errorMessages.add(String.format("第%d行：日期格式错误'%s'，应为yyyy-MM-dd", i + 2, dto.getScheduleDate()));
                        errorCount++;
                        continue;
                    }

                    // 4. 解析时段（上午/下午/晚上 -> 1/2/3）
                    Integer timeSlot = parseTimeSlot(dto.getTimeSlotStr());
                    if (timeSlot == null) {
                        errorMessages.add(String.format("第%d行：时段'%s'格式错误，应为'上午'、'下午'或'晚上'", i + 2, dto.getTimeSlotStr()));
                        errorCount++;
                        continue;
                    }

                    // 5. 计算号源数量（根据规则）
                    Integer maxQuota = dto.getQuota();
                    if (maxQuota == null || maxQuota <= 0) {
                        // 根据规则计算：假设每个时段默认号源数 = 240分钟 / 单次时长
                        maxQuota = 240 / durationMinutes; // 假设一个时段4小时=240分钟
                    }

                    // 收集排班数据，稍后统一处理
                    ScheduleData scheduleData = new ScheduleData();
                    scheduleData.doctorId = doctor.getDoctorId();
                    scheduleData.deptId = department.getDeptId();
                    scheduleData.scheduleDate = scheduleDate;
                    scheduleData.timeSlot = timeSlot;
                    scheduleData.maxQuota = maxQuota;
                    scheduleData.rowIndex = i + 2; // Excel行号（从2开始，因为第1行是表头）
                    scheduleDataList.add(scheduleData);
                } catch (Exception e) {
                    log.error("处理第{}行数据时出错", i + 2, e);
                    errorMessages.add(String.format("第%d行：%s", i + 2, e.getMessage()));
                    errorCount++;
                }
            }

            // 第二步：应用规则检查和调整
            // 规则1：检查医生连续工作天数，如果超过maxWorkDays，自动调整
            adjustConsecutiveWorkDays(scheduleDataList, maxWorkDays, adjustMessages);

            // 规则2：检查每个科室每天是否至少有一个医生在诊
            ensureDeptDailyCoverage(scheduleDataList, durationMinutes, adjustMessages);

            // 第三步：保存所有排班数据
            for (ScheduleData data : scheduleDataList) {
                try {
                    // 检查是否已存在相同的排班记录
                    LambdaQueryWrapper<DoctorSchedule> scheduleQuery = new LambdaQueryWrapper<>();
                    scheduleQuery.eq(DoctorSchedule::getDoctorId, data.doctorId)
                            .eq(DoctorSchedule::getDeptId, data.deptId)
                            .eq(DoctorSchedule::getScheduleDate, data.scheduleDate)
                            .eq(DoctorSchedule::getTimeSlot, data.timeSlot);
                    DoctorSchedule existing = scheduleService.getOne(scheduleQuery);

                    if (existing != null) {
                        // 更新现有记录
                        existing.setMaxQuota(data.maxQuota);
                        existing.setUpdateTime(LocalDateTime.now());
                        scheduleService.updateById(existing);
                    } else {
                        // 创建新记录
                        DoctorSchedule schedule = new DoctorSchedule();
                        schedule.setDoctorId(data.doctorId);
                        schedule.setDeptId(data.deptId);
                        schedule.setScheduleDate(data.scheduleDate);
                        schedule.setTimeSlot(data.timeSlot);
                        schedule.setMaxQuota(data.maxQuota);
                        schedule.setUsedQuota(0);
                        schedule.setStatus(1);
                        schedule.setCreateTime(LocalDateTime.now());
                        schedule.setUpdateTime(LocalDateTime.now());
                        scheduleService.save(schedule);
                    }

                    successCount++;
                } catch (Exception e) {
                    log.error("保存排班数据失败", e);
                    errorMessages.add(String.format("保存排班失败：%s", e.getMessage()));
                    errorCount++;
                }
            }

        } catch (Exception e) {
            log.error("导入文件失败，文件名：{}", fileName, e);
            String errorMsg = "文件导入失败：" + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += "，原因：" + e.getCause().getMessage();
            }
            // 打印完整的堆栈跟踪以便调试
            e.printStackTrace();
            return Result.error(errorMsg);
        }

        StringBuilder resultMessage = new StringBuilder();
        resultMessage.append(String.format("导入完成：成功%d条，失败%d条", successCount, errorCount));
        if (!adjustMessages.isEmpty()) {
            resultMessage.append("\n自动调整：").append(String.join("; ", adjustMessages));
        }
        if (errorCount > 0 && !errorMessages.isEmpty()) {
            resultMessage.append("\n错误信息：").append(String.join("; ", errorMessages));
        }

        if (errorCount > 0) {
            return Result.error(resultMessage.toString());
        } else {
            return Result.ok(resultMessage.toString());
        }
    }

    /**
     * 解析CSV文件
     * @param inputStream CSV文件输入流
     * @return 解析后的DTO列表
     */
    private List<DoctorScheduleExcelImportDTO> parseCsvFile(java.io.InputStream inputStream) throws IOException {
        List<DoctorScheduleExcelImportDTO> importList = new ArrayList<>();

        // 读取文件内容到字节数组，以便检测BOM（兼容Java 8）
        byte[] bytes = toByteArray(inputStream);

        // 检测并移除UTF-8 BOM
        if (bytes.length >= 3 && bytes[0] == (byte)0xEF && bytes[1] == (byte)0xBB && bytes[2] == (byte)0xBF) {
            // 移除BOM
            byte[] newBytes = new byte[bytes.length - 3];
            System.arraycopy(bytes, 3, newBytes, 0, newBytes.length);
            bytes = newBytes;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new java.io.ByteArrayInputStream(bytes), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    // 跳过表头
                    isFirstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                // 解析CSV行（处理引号和逗号）
                String[] fields = parseCsvLine(line);
                if (fields.length < 4) {
                    log.warn("CSV行数据不完整，跳过：{}", line);
                    continue; // 跳过不完整的行
                }

                DoctorScheduleExcelImportDTO dto = new DoctorScheduleExcelImportDTO();
                dto.setDoctorName(fields.length > 0 && fields[0] != null ? fields[0].trim() : "");
                dto.setDeptName(fields.length > 1 && fields[1] != null ? fields[1].trim() : "");
                dto.setScheduleDate(fields.length > 2 && fields[2] != null ? fields[2].trim() : "");
                dto.setTimeSlotStr(fields.length > 3 && fields[3] != null ? fields[3].trim() : "");
                if (fields.length > 4 && fields[4] != null && !fields[4].trim().isEmpty()) {
                    try {
                        dto.setQuota(Integer.parseInt(fields[4].trim()));
                    } catch (NumberFormatException e) {
                        log.warn("号源数量格式错误，忽略：{}", fields[4]);
                        // 忽略无效的数字
                    }
                }

                // 只添加非空的数据
                if (dto.getDoctorName() != null && !dto.getDoctorName().isEmpty() &&
                        dto.getDeptName() != null && !dto.getDeptName().isEmpty() &&
                        dto.getScheduleDate() != null && !dto.getScheduleDate().isEmpty() &&
                        dto.getTimeSlotStr() != null && !dto.getTimeSlotStr().isEmpty()) {
                    importList.add(dto);
                } else {
                    log.warn("CSV行数据不完整，跳过：{}", line);
                }
            }
        }
        return importList;
    }

    /**
     * 将InputStream转换为字节数组（兼容Java 8）
     * @param inputStream 输入流
     * @return 字节数组
     */
    private byte[] toByteArray(java.io.InputStream inputStream) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    /**
     * 解析CSV行，处理引号和逗号
     * @param line CSV行
     * @return 字段数组
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // 转义的双引号
                    currentField.append('"');
                    i++;
                } else {
                    // 切换引号状态
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // 字段分隔符
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // 添加最后一个字段
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    /**
     * 解析时段字符串为数字
     * @param timeSlotStr 时段字符串（上午/下午/晚上）
     * @return 时段数字（1-上午, 2-下午, 3-晚上）
     */
    private Integer parseTimeSlot(String timeSlotStr) {
        if (timeSlotStr == null) {
            return null;
        }
        String slot = timeSlotStr.trim();
        if (slot.contains("上午") || slot.contains("早") || slot.equalsIgnoreCase("morning")) {
            return 1;
        } else if (slot.contains("下午") || slot.equalsIgnoreCase("afternoon")) {
            return 2;
        } else if (slot.contains("晚上") || slot.contains("晚") || slot.equalsIgnoreCase("evening")) {
            return 3;
        }
        return null;
    }

    /**
     * 内部类：用于临时存储排班数据
     */
    private static class ScheduleData {
        Long doctorId;
        Long deptId;
        LocalDate scheduleDate;
        Integer timeSlot;
        Integer maxQuota;
        int rowIndex; // Excel行号
    }

    /**
     * 规则1：检查并调整医生连续工作天数
     * 如果医生连续工作超过maxWorkDays天，自动将后续的排班日期延后
     */
    private void adjustConsecutiveWorkDays(List<ScheduleData> scheduleDataList, Integer maxWorkDays, List<String> adjustMessages) {
        // 按医生ID和日期分组
        Map<Long, List<ScheduleData>> doctorSchedules = new HashMap<>();
        for (ScheduleData data : scheduleDataList) {
            doctorSchedules.computeIfAbsent(data.doctorId, k -> new ArrayList<>()).add(data);
        }

        for (Map.Entry<Long, List<ScheduleData>> entry : doctorSchedules.entrySet()) {
            Long doctorId = entry.getKey();
            List<ScheduleData> schedules = entry.getValue();

            // 按日期排序
            schedules.sort((a, b) -> a.scheduleDate.compareTo(b.scheduleDate));

            // 检查连续工作天数，如果超过限制，调整后续日期
            int consecutiveDays = 0;
            LocalDate lastDate = null;
            LocalDate consecutiveStartDate = null;

            for (ScheduleData data : schedules) {
                if (lastDate != null && data.scheduleDate.equals(lastDate.plusDays(1))) {
                    // 连续工作
                    consecutiveDays++;
                    if (consecutiveStartDate == null) {
                        consecutiveStartDate = lastDate;
                    }
                } else {
                    // 不连续，重置
                    consecutiveDays = 1;
                    consecutiveStartDate = data.scheduleDate;
                }

                if (consecutiveDays > maxWorkDays) {
                    // 超过最大连续工作天数，将当前排班日期延后1天
                    LocalDate newDate = data.scheduleDate.plusDays(1);
                    adjustMessages.add(String.format("医生ID %d 从 %s 开始连续工作超过%d天，已将 %s 的排班调整到 %s",
                            doctorId, consecutiveStartDate, maxWorkDays, data.scheduleDate, newDate));
                    data.scheduleDate = newDate;
                    // 重置连续天数，因为已经延后了
                    consecutiveDays = 1;
                    consecutiveStartDate = newDate;
                }

                lastDate = data.scheduleDate;
            }
        }
    }

    /**
     * 规则2：确保每个科室每天至少有一个医生在诊
     * 如果某个科室某天没有医生，自动添加一个排班
     */
    private void ensureDeptDailyCoverage(List<ScheduleData> scheduleDataList, Integer durationMinutes, List<String> adjustMessages) {
        // 按科室和日期分组，找出所有有排班的日期
        Map<Long, Set<LocalDate>> deptDates = new HashMap<>();
        for (ScheduleData data : scheduleDataList) {
            deptDates.computeIfAbsent(data.deptId, k -> new HashSet<>()).add(data.scheduleDate);
        }

        // 找出所有科室的所有日期范围
        Map<Long, LocalDate> deptMinDate = new HashMap<>();
        Map<Long, LocalDate> deptMaxDate = new HashMap<>();
        for (ScheduleData data : scheduleDataList) {
            deptMinDate.put(data.deptId, deptMinDate.getOrDefault(data.deptId, data.scheduleDate).isBefore(data.scheduleDate)
                    ? deptMinDate.get(data.deptId) : data.scheduleDate);
            deptMaxDate.put(data.deptId, deptMaxDate.getOrDefault(data.deptId, data.scheduleDate).isAfter(data.scheduleDate)
                    ? deptMaxDate.get(data.deptId) : data.scheduleDate);
        }

        // 检查每个科室的每一天
        for (Map.Entry<Long, Set<LocalDate>> entry : deptDates.entrySet()) {
            Long deptId = entry.getKey();
            Set<LocalDate> dates = entry.getValue();
            LocalDate minDate = deptMinDate.get(deptId);
            LocalDate maxDate = deptMaxDate.get(deptId);

            if (minDate == null || maxDate == null) {
                continue;
            }

            // 遍历日期范围内的每一天
            LocalDate currentDate = minDate;
            while (!currentDate.isAfter(maxDate)) {
                if (!dates.contains(currentDate)) {
                    // 该科室这一天没有医生，需要添加一个排班
                    // 获取该科室的医生列表
                    List<Doctor> doctors = doctorService.getDoctorsByDeptId(deptId);
                    if (doctors.isEmpty()) {
                        adjustMessages.add(String.format("科室ID %d 在 %s 没有医生在诊，且该科室没有可用医生", deptId, currentDate));
                    } else {
                        // 选择第一个医生，添加上午时段的排班
                        Doctor doctor = doctors.get(0);
                        ScheduleData newSchedule = new ScheduleData();
                        newSchedule.doctorId = doctor.getDoctorId();
                        newSchedule.deptId = deptId;
                        newSchedule.scheduleDate = currentDate;
                        newSchedule.timeSlot = 1; // 默认上午
                        newSchedule.maxQuota = 240 / durationMinutes; // 根据规则计算
                        newSchedule.rowIndex = -1; // 自动添加的，没有行号
                        scheduleDataList.add(newSchedule);
                        adjustMessages.add(String.format("科室ID %d 在 %s 没有医生在诊，已自动添加医生 %s 的排班",
                                deptId, currentDate, doctor.getDoctorName()));
                    }
                }
                currentDate = currentDate.plusDays(1);
            }
        }
    }
}