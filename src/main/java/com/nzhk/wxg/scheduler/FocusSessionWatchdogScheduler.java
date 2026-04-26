package com.nzhk.wxg.scheduler;

import com.nzhk.wxg.business.focus.service.IFocusService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FocusSessionWatchdogScheduler {

    private static final int INACTIVE_HOURS = 8;

    @Resource
    private IFocusService focusService;

    /** 每小时执行一次，清理超过 8 小时未活跃的 paused 会话 */
    @Scheduled(cron = "0 0 * * * ?")
    public void cancelInactivePausedSessions() {
        try {
            int affected = focusService.cancelInactivePausedSessions(INACTIVE_HOURS);
            if (affected > 0) {
                log.info("focus watchdog cancelled inactive paused sessions: {}", affected);
            }
        } catch (Exception e) {
            log.error("focus watchdog failed", e);
        }
    }
}
