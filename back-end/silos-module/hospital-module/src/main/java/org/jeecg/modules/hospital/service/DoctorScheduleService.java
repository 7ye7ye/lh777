package org.jeecg.modules.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService; // 核心修改：导入 IService
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
@Service
/**
 * 医生排班服务接口
 * 继承 IService 后，将自动拥有 updateById, getById, save 等基础 CRUD 方法。
 */
public interface DoctorScheduleService extends IService<DoctorSchedule> {

    /**
     * 自定义查询方法：按医生、科室或日期查询排班
     */
    List<DoctorSchedule> list(Long doctorId, Long deptId, LocalDate date, LocalDate startDate, LocalDate endDate);

    DoctorSchedule create(DoctorSchedule schedule);

    boolean update(DoctorSchedule schedule);

    boolean delete(Long scheduleId);

    DoctorSchedule getById(Long scheduleId);

    // 注意：
    // 继承 IService<DoctorSchedule> 后，以下自定义方法不再需要手动声明：
    // DoctorSchedule create(DoctorSchedule schedule);  // 对应 IService.save(T entity)
    // boolean update(DoctorSchedule schedule);        // 对应 IService.updateById(T entity)
    // boolean delete(Long scheduleId);                // 对应 IService.removeById(Serializable id)
    // DoctorSchedule getById(Long scheduleId);        // 对应 IService.getById(Serializable id)

    // 如果你坚持要保留这些方法名以兼容旧代码，则它们将成为自定义方法，需要在 Impl 中手动实现。
    // 在使用 Mybatis-Plus 时，强烈建议直接使用 IService 提供的标准方法 (save, updateById, removeById, getById)。

    // 新增：按医生与单日查询
    List<DoctorSchedule> listByDoctorAndDate(Long doctorId, LocalDate date);

    // 新增：按医生与日期范围查询
    List<DoctorSchedule> listByDoctorAndDateRange(Long doctorId, LocalDate startDate, LocalDate endDate);

    // 新增：更新已使用号源
    boolean updateUsedQuota(Long scheduleId, Integer usedQuota);

    /**
     * 增加号源，并自动处理候补队列前 n 名
     * @param scheduleId 排班id+排班max_quota
     * @param addCount 新增号源数量
     * @return 是否成功
     */
    boolean addQuotaAndFillQueue(Long scheduleId, int addCount);
}