package com.nzhk.wxg.scheduler;

import com.nzhk.wxg.business.habitcheckin.service.IHabitCheckInService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserRankSnapshotScheduler {

    @Resource
    private IHabitCheckInService habitCheckInService;

    /**
     * 每天凌晨 00:05 校准用户排行预计算表
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void recalibrateUserRankSnapshots() {
        log.info("user rank snapshot scheduler start");
        try {
            habitCheckInService.recalibrateUserRankSnapshots();
            log.info("user rank snapshot scheduler finish");
        } catch (Exception e) {
            log.error("user rank snapshot scheduler error", e);
        }
    }
}
