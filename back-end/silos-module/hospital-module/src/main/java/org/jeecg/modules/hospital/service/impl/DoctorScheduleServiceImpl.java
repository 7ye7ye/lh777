package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 导入 Mybatis-Plus 的 ServiceImpl
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.mapper.DoctorScheduleMapper;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生排班服务实现类
 * 继承 ServiceImpl<Mapper, Entity>，自动获得 CRUD 方法
 */
// class DoctorScheduleServiceImpl
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
@DS("hospital")
public class DoctorScheduleServiceImpl
        extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule>
        implements DoctorScheduleService {

    // 【注意】继承 ServiceImpl 后，不再需要 @Resource 注入 Mapper，可以直接使用 this.baseMapper。
    // private DoctorScheduleMapper mapper; // 此行可以删除或注释掉

    // ------------------- 自定义方法 (保留) -------------------

    @Override
    public List<DoctorSchedule> list(Long doctorId, Long deptId, LocalDate date) {
        LambdaQueryWrapper<DoctorSchedule> qw = new LambdaQueryWrapper<>();
        qw.eq(doctorId != null, DoctorSchedule::getDoctorId, doctorId);
        qw.eq(deptId != null, DoctorSchedule::getDeptId, deptId);
        qw.eq(date != null, DoctorSchedule::getScheduleDate, date);
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
        schedule.setCreateTime(java.time.LocalDateTime.now());
        schedule.setUpdateTime(java.time.LocalDateTime.now());
        super.save(schedule);
        return schedule;
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
}