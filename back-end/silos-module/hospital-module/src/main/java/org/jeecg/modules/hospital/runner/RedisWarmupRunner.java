package org.jeecg.modules.hospital.runner;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hospital.entity.DoctorSchedule;
import org.jeecg.modules.hospital.service.DoctorScheduleService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Redis预热启动器 + 定时刷新
 * 1. 项目启动时自动执行
 * 2. 每隔 5 分钟自动刷新一次
 */
@Slf4j
@Component
@EnableScheduling
public class RedisWarmupRunner implements ApplicationRunner {

    @Resource
    private DoctorScheduleService doctorScheduleService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动时立即执行一次
        warmup();
    }

    /**
     * 每 5 分钟执行一次刷新 (单位：毫秒)
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    @DS("hospital") // 明确指定数据源
    public void warmup() {
        log.info("====== 开始预热挂号库存到 Redis (定时/启动) ======");

        try {
            // 1. 查询未来7天内有效排班
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);

            LambdaQueryWrapper<DoctorSchedule> query = new LambdaQueryWrapper<>();
            query.ge(DoctorSchedule::getScheduleDate, today)
                    .le(DoctorSchedule::getScheduleDate, nextWeek)
                    .eq(DoctorSchedule::getStatus, 1); // 1-正常

            List<DoctorSchedule> schedules = doctorScheduleService.list(query);

            if (schedules == null || schedules.isEmpty()) {
                log.info("当前没有可用的排班数据，跳过预热。");
                return;
            }

            // 2. 遍历并同步
            int successCount = 0;
            for (DoctorSchedule schedule : schedules) {
                String redisKey = "doctor_schedule:quota:" + schedule.getScheduleId();

                // 计算剩余库存
                int remaining = (schedule.getMaxQuota() != null ? schedule.getMaxQuota() : 0)
                        - (schedule.getUsedQuota() != null ? schedule.getUsedQuota() : 0);
                if (remaining < 0)
                    remaining = 0;

                // 写入 Redis
                stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(remaining));

                // 设置过期时间
                stringRedisTemplate.expire(redisKey, 24, TimeUnit.HOURS);

                successCount++;
            }
            log.info("====== 库存预热结束，共同步 {} 条排班数据 ======", successCount);

        } catch (Exception e) {
            log.error("Redis预热失败", e);
        }
    }
}
