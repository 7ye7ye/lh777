package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.*;
import org.jeecg.modules.hospital.mapper.PatientMapper;
import org.jeecg.modules.hospital.mapper.RegistrationMapper;
import org.jeecg.modules.hospital.mapper.WaitingQueueMapper;
import org.jeecg.modules.hospital.service.RegistrationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 挂号业务实现类
 */
@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Resource
    private RegistrationMapper registrationMapper;
    @Resource
    private WaitingQueueMapper waitingQueueMapper;
    @Resource
    private PatientMapper patientMapper;

    @Override
    public List<RegistrationType> getAllRegistrationTypes() {
        return registrationMapper.selectAllTypes();
    }

    @Override
    public List<Map<String, Object>> getDoctorSchedules(Long doctorId, String startDate, Integer days) {
        // 调用 Mapper 查询医生排班（简单示例，可根据 schedule 表设计修改）
        return registrationMapper.selectSchedulesByDoctor(doctorId, startDate, days);
    }

    @Override
    public Result<String> createRegistration(RegistrationRecord record, Long patientId, boolean joinWaitingQueue) {
        try {
            // 1. 检查必填字段
            if (record.getDoctorId() == null || record.getTypeId() == null || record.getScheduleId() == null) {
                return Result.error("缺少必要的挂号信息");
            }
            if (patientId == null) {
                return Result.error("未获取到患者ID，请登录后再挂号");
            }
            record.setPatientId(patientId);

            // 2. 获取排班信息
            DoctorSchedule schedule = registrationMapper.selectScheduleById(record.getScheduleId());
            if (schedule == null) {
                return Result.error("未找到对应排班信息");
            }
            // 3. 重复挂号检测
            boolean isDuplicate = checkDuplicateBySchedule(patientId, schedule.getScheduleId());
            if (isDuplicate) {
                return Result.error("您已预约过该医生的该时段，请勿重复挂号！");
            }
            // 3. 检查每日上限
            RegistrationType type = registrationMapper.selectTypeById(record.getTypeId());
            if (type == null) {
                return Result.error("未找到对应挂号类型");
            }

            int dailyQuota = type.getDailyQuota();
            int usedQuota = schedule.getUsedQuota() != null ? schedule.getUsedQuota() : 0;

            if (usedQuota >= dailyQuota) {
                // 已满
                if (!joinWaitingQueue) {
                    return Result.error("该号源已满，是否加入候补？");
                }

                // 生成候补记录
                WaitingQueue queue = new WaitingQueue();
                queue.setScheduleId(schedule.getScheduleId());
                queue.setPatientId(patientId);
                queue.setQueueTime(LocalDateTime.now());
                // 查询当前候补最大排名
                Integer maxRank = registrationMapper.selectMaxQueueRank(schedule.getScheduleId());
                queue.setQueueRank(maxRank != null ? maxRank + 1 : 1);
                queue.setStatus(0); // 等待状态
                waitingQueueMapper.insert(queue);

                return Result.OK("已加入候补队列");
            }

            // 4. 挂号未满，生成挂号记录
            record.setRegisterTime(LocalDateTime.now());
            record.setStatus(1); // 已预约
            record.setVisitTime(null); // 实际就诊时间先留空

            // 根据患者类型设置价格
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) {
                return Result.error("患者信息未找到");
            }
            switch (patient.getPatientType()) {
                case 1 -> record.setActualPrice(type.getStudentPrice());
                case 2, 3 -> record.setActualPrice(type.getStaffPrice());
                default -> record.setActualPrice(type.getPriceOriginal());
            }
            record.setPriceOriginal(type.getPriceOriginal());

            // 生成流水号
            String serialNumber = System.currentTimeMillis() + "" + (int) (Math.random() * 1000);
            record.setRegistrationNo(serialNumber);

            // 5. 插入挂号记录
            registrationMapper.insertRegistration(record);

            // 6. 更新排班已用数量
            schedule.setUsedQuota(usedQuota + 1);
            registrationMapper.updateScheduleUsedQuota(schedule);

            return Result.OK("挂号成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("挂号失败：" + e.getMessage());
        }
    }



    @Override
    public List<RegistrationRecord> getRecordsByPatientId(Long patientId) {
        QueryWrapper<RegistrationRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("patient_id", patientId);
        wrapper.orderByDesc("register_time");
        return registrationMapper.selectList(wrapper);
    }

    @Override
    public boolean checkDuplicateBySchedule(Long patientId, Long scheduleId) {
        Integer count = registrationMapper.checkDuplicateBySchedule(patientId, scheduleId);
        return count != null && count > 0;
    }

    @Override
    public Result<String> addWaitingQueue(WaitingQueue queue) {
        if (queue == null || queue.getPatientId() == null || queue.getScheduleId() == null) {
            return Result.error("缺少必要信息");
        }

        try {
            // 检查是否已在候补队列
            int existing = waitingQueueMapper.countExistingQueue(queue.getScheduleId(), queue.getPatientId());
            if (existing > 0) {
                return Result.error("您已在该排班的候补队列中");
            }

            // 查询最大排名
            Integer maxRank = waitingQueueMapper.selectMaxQueueRank(queue.getScheduleId());
            queue.setQueueRank(maxRank != null ? maxRank + 1 : 1);
            queue.setQueueTime(LocalDateTime.now());
            queue.setStatus(0);
            if (queue.getRecordId() == null) queue.setRecordId(null);

            int rows = waitingQueueMapper.insert(queue);
            if (rows == 1) {
                return Result.OK("已加入候补队列");
            } else {
                return Result.error("加入候补失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("加入候补异常：" + e.getMessage());
        }
    }

    @Override
    public boolean cancelRegistration(Long recordId, String cancelReason) {

        // 1. 校验记录是否存在
        RegistrationRecord record = registrationMapper.selectById(recordId);
        if (record == null) {
            return false;
        }

        // 若状态不是已预约(1)，不允许重复取消
        if (record.getStatus() != 1) {
            return false;
        }

        // 2. 设置取消状态与信息（status = 3）
        record.setStatus(3); // 3 = 已退号
        record.setCancelTime(LocalDateTime.now());
        record.setCancelReason(cancelReason);

        // 3. 更新挂号记录
        int updated = registrationMapper.updateById(record);
        if (updated <= 0) {
            return false;
        }

        // 4. 退号成功 → 排班号源 +1（即 usedQuota -1）
        DoctorSchedule schedule = registrationMapper.selectScheduleById(record.getScheduleId());
        if (schedule != null) {
            Integer used = schedule.getUsedQuota() != null ? schedule.getUsedQuota() : 0;

            // 防止出现负数
            schedule.setUsedQuota(Math.max(0, used - 1));
            registrationMapper.updateScheduleUsedQuota(schedule);
        }
// ⭐⭐⭐ 4. 自动候补补位
        autoFillFromQueue(record.getScheduleId());

        return true;
    }
    /**
     * 若有人退号 → 自动补候补队列
     */
    private void autoFillFromQueue(Long scheduleId) {

        // 1. 读取候补队列中排第一的人
        WaitingQueue first = waitingQueueMapper.selectFirstWaiting(scheduleId); // 需要写 mapper
        if (first == null) {
            return; // 没有人候补
        }

        // 2. 获取排班
        DoctorSchedule schedule = registrationMapper.selectScheduleById(scheduleId);
        RegistrationType type = registrationMapper.selectTypeById(schedule.getTypeId().longValue());


        // 3. 构建新的挂号记录
        RegistrationRecord newRecord = new RegistrationRecord();
        newRecord.setScheduleId(scheduleId);
        newRecord.setPatientId(first.getPatientId());
        newRecord.setDoctorId(schedule.getDoctorId());
        newRecord.setTypeId(type.getTypeId());
        newRecord.setRegisterTime(LocalDateTime.now());
        newRecord.setStatus(0); // 自动补位 = 已预约
        newRecord.setPriceOriginal(type.getPriceOriginal());
        newRecord.setActualPrice(type.getPriceOriginal());
        newRecord.setIsAdd(0);

        // 生成新挂号单号
        newRecord.setRegistrationNo(System.currentTimeMillis() + "" + (int)(Math.random()*1000));

        // 4. 插入挂号记录
        registrationMapper.insertRegistration(newRecord);

        // 5. 更新排班 usedQuota +1
        schedule.setUsedQuota(schedule.getUsedQuota() + 1);
        registrationMapper.updateScheduleUsedQuota(schedule);

        first.setStatus(1); // 已转正
        waitingQueueMapper.updateById(first); // 注意这里是 WaitingQueueMapper

    }

}
