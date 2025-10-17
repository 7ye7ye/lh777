package org.jeecg.modules.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.hospital.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息记录表数据访问接口
 */
@Mapper // 声明这是一个MyBatis的Mapper接口
public interface MessageMapper extends BaseMapper<Message> {
    // 继承了 BaseMapper 之后，常见的增删改查方法就自动有了，无需手写
    // 例如 selectById, insert, delete, update 等
}