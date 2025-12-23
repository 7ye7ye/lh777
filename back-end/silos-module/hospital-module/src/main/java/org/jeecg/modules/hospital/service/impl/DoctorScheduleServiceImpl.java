package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 导入 Mybatis-Plus 的 ServiceImpl
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hospital.controller.request.DoctorScheduleUpdateRequest;
import org.jeecg.modules.hospital.dto.RegistrationDetailDTO;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.entity.RegistrationRecord;
import org.jeecg.modules.hospital.entity.WaitingQueue;
import org.jeecg.modules.hospital.mapper.DoctorScheduleMapper;
import org.jeecg.modules.hospital.mapper.RegistrationMapper;
import org.jeecg.modules.hospital.mapper.WaitingQueueMapper;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.jeecg.modules.hospital.service.RegistrationService;
import org.jeecg.modules.hospital.service.WaitingQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 医生排班服务实现类
 * 继承 ServiceImpl<Mapper, Entity>，自动获得 CRUD 方法
 */
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@DS("hospital")
public class DoctorScheduleServiceImpl
        extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule>
        implements DoctorScheduleService {

    // 【注意】继承 ServiceImpl 后，不再需要 @Resource 注入 Mapper，可以直接使用 this.baseMapper。
    // private DoctorScheduleMapper mapper; // 此行可以删除或注释掉

    @Autowired
    private WaitingQueueMapper waitingQueueMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private WaitingQueueService waitingQueueService;
    // ------------------- 自定义方法 (保留) -------------------

    @Override
    public List<DoctorSchedule> list(Long doctorId, Long deptId, LocalDate date, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DoctorSchedule> qw = new LambdaQueryWrapper<>();
        qw.eq(doctorId != null, DoctorSchedule::getDoctorId, doctorId);
        qw.eq(deptId != null, DoctorSchedule::getDeptId, deptId);

        // 优先使用单个日期精确查询
        if (date != null) {
            // 如果指定了单个日期，使用精确匹配
            qw.eq(DoctorSchedule::getScheduleDate, date);
        } else if (startDate != null && endDate != null) {
            // 如果开始日期和结束日期相同，使用精确匹配
            if (startDate.equals(endDate)) {
                qw.eq(DoctorSchedule::getScheduleDate, startDate);
            } else {
                // 日期范围查询
                qw.ge(DoctorSchedule::getScheduleDate, startDate);
                qw.le(DoctorSchedule::getScheduleDate, endDate);
            }
        } else if (startDate != null) {
            // 只有开始日期
            qw.ge(DoctorSchedule::getScheduleDate, startDate);
        } else if (endDate != null) {
            // 只有结束日期
            qw.le(DoctorSchedule::getScheduleDate, endDate);
        }

        // 按日期和时段排序
        qw.orderByAsc(DoctorSchedule::getScheduleDate);
        qw.orderByAsc(DoctorSchedule::getTimeSlot);

        return this.baseMapper.selectList(qw);
    }

    // ------------------- 新增方法 -------------------

    /**
     * 根据医生ID和日期查询排班列表
     */
    @Override
    public List<DoctorSchedule> listByDoctorAndDate(Long doctorId, java.time.LocalDate date) {
        if (doctorId == null || date == null) {
            return java.util.Collections.emptyList();
        }
        return this.lambdaQuery()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .eq(DoctorSchedule::getScheduleDate, date)
                .eq(DoctorSchedule::getStatus, 1)
                .list();
    }

    @Override
    public List<DoctorSchedule> listByDoctorAndDateRange(Long doctorId, LocalDate start, LocalDate end) {
        if (doctorId == null || start == null || end == null) {
            return List.of();
        }
        return this.lambdaQuery()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .ge(DoctorSchedule::getScheduleDate, start)
                .le(DoctorSchedule::getScheduleDate, end)
                .eq(DoctorSchedule::getStatus, 1)
                .list();
    }

    @Override
    public boolean updateUsedQuota(Long scheduleId, Integer usedQuota) {
        if (scheduleId == null || usedQuota == null) {
            return false;
        }
        DoctorSchedule s = super.getById(scheduleId);
        if (s == null) {
            return false;
        }
        s.setUsedQuota(usedQuota);
        s.setUpdateTime(java.time.LocalDateTime.now());
        return super.updateById(s);
    }

    // ------------------- 冗余方法已移除 -------------------
    // create, update, delete, getById 方法已删除，因为它们由 ServiceImpl 自动提供。

    @Override
    public DoctorSchedule create(DoctorSchedule schedule) {
        if (schedule == null) return null;
        if (schedule.getUsedQuota() == null) schedule.setUsedQuota(0);
        if (schedule.getStatus() == null) schedule.setStatus(1);
        if (schedule.getMaxQuota() == null) schedule.setMaxQuota(50);
        // 确保timeSlot有值 - 必须在保存前设置
        if (schedule.getTimeSlot() == null || schedule.getTimeSlot() < 1 || schedule.getTimeSlot() > 3) {
            log.warn("timeSlot值为null或无效: {}, 设置为默认值1", schedule.getTimeSlot());
            schedule.setTimeSlot(1); // 默认上午
        }
        schedule.setCreateTime(java.time.LocalDateTime.now());
        schedule.setUpdateTime(java.time.LocalDateTime.now());

        // 添加日志确认字段值 - 保存前再次确认
        log.info("保存排班前 - scheduleId: {}, doctorId: {}, deptId: {}, date: {}, timeSlot: {}, maxQuota: {}, roomNumber: {}, status: {}",
                schedule.getScheduleId(), schedule.getDoctorId(), schedule.getDeptId(),
                schedule.getScheduleDate(), schedule.getTimeSlot(), schedule.getMaxQuota(),
                schedule.getRoomNumber(), schedule.getStatus());

        // 再次确保timeSlot不为null
        if (schedule.getTimeSlot() == null) {
            log.error("timeSlot仍然为null，强制设置为1");
            schedule.setTimeSlot(1);
        }

        // 直接使用自定义的insert方法，确保time_slot字段被包含
        log.info("准备调用自定义insert方法，timeSlot值: {}", schedule.getTimeSlot());
        try {
            int result = this.baseMapper.insertSchedule(schedule);
            if (result > 0) {
                log.info("使用自定义insert方法保存成功，保存后的scheduleId: {}", schedule.getScheduleId());
                return schedule;
            } else {
                log.error("自定义insert方法返回0，插入失败");
                throw new RuntimeException("插入排班失败");
            }
        } catch (Exception e) {
            log.error("自定义insert方法调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存排班失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(DoctorSchedule schedule) {
        Long id = (schedule != null ? schedule.getScheduleId() : null);
        if (id == null) return false;
        DoctorSchedule origin = super.getById(id);
        if (origin == null) return false;

        if (schedule.getDoctorId() != null) origin.setDoctorId(schedule.getDoctorId());
        if (schedule.getDeptId() != null) origin.setDeptId(schedule.getDeptId());
        if (schedule.getTypeId() != null) origin.setTypeId(schedule.getTypeId());
        if (schedule.getScheduleDate() != null) origin.setScheduleDate(schedule.getScheduleDate());
        if (schedule.getTimeSlot() != null) origin.setTimeSlot(schedule.getTimeSlot());
        if (schedule.getUsedQuota() != null) origin.setUsedQuota(schedule.getUsedQuota());
        if (schedule.getStatus() != null) origin.setStatus(schedule.getStatus());
        if (schedule.getMaxQuota() != null) origin.setMaxQuota(schedule.getMaxQuota());
        if (schedule.getRoomNumber() != null) origin.setRoomNumber(schedule.getRoomNumber());
        origin.setUpdateTime(java.time.LocalDateTime.now());

        return super.updateById(origin);
    }

    @Override
    public boolean delete(Long scheduleId) {
        if (scheduleId == null) return false;
        return super.removeById(scheduleId);
    }

    @Override
    public DoctorSchedule getById(Long scheduleId) {
        if (scheduleId == null) return null;
        return super.getById(scheduleId);
    }

    @Override
    @Transactional
    public boolean addQuotaAndFillQueue(Long scheduleId, int addCount) {
        // 获取排班信息
        DoctorSchedule schedule = this.getById(scheduleId);
        if (schedule == null) return false;

        // 更新 maxQuota
        int newMaxQuota = (schedule.getMaxQuota() != null ? schedule.getMaxQuota() : 0) + addCount;
        schedule.setMaxQuota(newMaxQuota);

        // 调用 update 方法更新排班
        boolean updated = update(schedule);  // 调用 update 方法传递排班数据
        if (!updated) return false;

        // 调用 autoFillFromQueue 实现候补成功，传递 addCount 作为候补人数
        // ⭐ 标识为加号场景，消息会显示"加号成功提醒"而不是"候补挂号成功提醒"
        waitingQueueService.autoFillFromQueue(scheduleId, addCount, true);

        return true;
    }
}