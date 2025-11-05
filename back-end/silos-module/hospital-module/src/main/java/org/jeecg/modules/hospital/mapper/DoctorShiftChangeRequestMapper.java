package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.hospital.entity.DoctorShiftChangeRequest;

/**
 * 医生调班申请Mapper
 */
@DS("hospital")
@Mapper
public interface DoctorShiftChangeRequestMapper extends BaseMapper<DoctorShiftChangeRequest> {
    @org.apache.ibatis.annotations.Update({
        "CREATE TABLE IF NOT EXISTS doctor_schedule_adjustment (",
        "  adjustment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '调整申请ID',",
        "  doctor_id BIGINT NOT NULL COMMENT '医生ID',",
        "  original_schedule_id BIGINT NOT NULL COMMENT '原排班ID',",
        "  target_date DATE NULL COMMENT '目标日期',",
        "  target_time_slot TINYINT NULL COMMENT '目标时段(1-上午,2-下午,3-晚上)',",
        "  target_dept_id BIGINT NULL COMMENT '目标科室ID',",
        "  reason VARCHAR(255) NULL COMMENT '申请原因',",
        "  apply_time DATETIME NOT NULL COMMENT '申请时间',",
        "  status TINYINT NOT NULL COMMENT '状态(1-待审批,2-已通过,3-已驳回,4-已撤销)',",
        "  admin_id BIGINT NULL COMMENT '审批管理员ID',",
        "  approve_time DATETIME NULL COMMENT '审批时间',",
        "  reject_reason VARCHAR(255) NULL COMMENT '驳回原因',",
        "  new_schedule_id BIGINT NULL COMMENT '生成的新排班ID',",
        "  PRIMARY KEY (adjustment_id)",
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生排班调整申请表'"
    })
    void ensureTable();
}