package com.nzhk.wxg.scheduler;

import com.nzhk.wxg.business.habit.service.IHabitService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 习惯相关定时任务
 * 每天凌晨 00:01 自动归档 end_date 为昨天的习惯
 */
@Slf4j
@Component
public class HabitArchiveScheduler {

    @Resource
    private IHabitService habitService;

    /**
     * 每天凌晨 00:01 执行：将 end_date=昨天 且 status=1 的习惯更新为 status=2（归档）
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void autoArchiveEndedHabits() {
        log.info("habit archive scheduler start: auto archive ended habits");
        try {
            habitService.autoArchiveEndedHabits();
            log.info("habit archive scheduler finish");
        } catch (Exception e) {
            log.error("habit archive scheduler error", e);
        }
    }
}
