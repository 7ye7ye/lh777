package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.hospital.entity.DoctorLeaveRequest;

/**
 * 医生请假申请Mapper
 */
@DS("hospital")
@Mapper
public interface DoctorLeaveRequestMapper extends BaseMapper<DoctorLeaveRequest> {
    @org.apache.ibatis.annotations.Update({
        "CREATE TABLE IF NOT EXISTS doctor_leave_request (",
        "  leave_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '请假申请ID',",
        "  doctor_id BIGINT NOT NULL COMMENT '医生ID',",
        "  doctor_name VARCHAR(100) NOT NULL COMMENT '医生姓名',",
        "  dept_id BIGINT NOT NULL COMMENT '科室ID',",
        "  dept_name VARCHAR(100) NOT NULL COMMENT '科室名称',",
        "  leave_type VARCHAR(50) NOT NULL COMMENT '请假类型(病假/事假/年假/产假/婚假/丧假/其他)',",
        "  start_date DATE NOT NULL COMMENT '请假开始日期',",
        "  end_date DATE NOT NULL COMMENT '请假结束日期',",
        "  reason TEXT NULL COMMENT '请假事由',",
        "  apply_time DATETIME NOT NULL COMMENT '申请时间',",
        "  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-待审批,2-已通过,3-已驳回,4-已撤销)',",
        "  admin_id BIGINT NULL COMMENT '审批管理员ID',",
        "  approve_time DATETIME NULL COMMENT '审批时间',",
        "  reject_reason VARCHAR(255) NULL COMMENT '驳回原因',",
        "  PRIMARY KEY (leave_id),",
        "  INDEX idx_doctor_id (doctor_id),",
        "  INDEX idx_status (status),",
        "  INDEX idx_apply_time (apply_time)",
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生请假申请表'"
    })
    void ensureTable();
}

