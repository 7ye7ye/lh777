package org.jeecg.modules.hospital.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.hospital.entity.Doctor;

import java.util.List;

/**
 * 医生表数据库操作Mapper
 * @author Administrator
 * @createDate 2025-09-22 20:15:21
 */
@Mapper
@DS("hospital")
public interface DoctorMapper extends BaseMapper<Doctor> {

    /**
     * 根据科室ID查询医生列表
     */
    List<Doctor> selectByDeptId(Long deptId);

    /**
     * 查询所有在职医生
     */
    List<Doctor> selectActiveDoctors();

    /**
     * 根据医生姓名模糊查询
     */
    List<Doctor> selectByDoctorName(String doctorName);
}




