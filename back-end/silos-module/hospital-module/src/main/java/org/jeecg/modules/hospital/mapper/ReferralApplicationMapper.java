package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.hospital.entity.ReferralApplication;

@Mapper
@DS("hospital")
public interface ReferralApplicationMapper extends BaseMapper<ReferralApplication> {
}

