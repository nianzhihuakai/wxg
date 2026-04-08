package com.nzhk.wxg.scheduler;

import com.nzhk.wxg.business.focus.entity.FocusSession;
import com.nzhk.wxg.business.focus.service.IFocusService;
import com.nzhk.wxg.business.habit.entity.Habit;
import com.nzhk.wxg.business.wxuser.entity.WxUser;
import com.nzhk.wxg.mapper.HabitMapper;
import com.nzhk.wxg.mapper.WxUserMapper;
import com.nzhk.wxg.wechat.service.SubscribeMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class FocusRemindScheduler {
    @Resource
    private IFocusService focusService;
    @Resource
    private WxUserMapper wxUserMapper;
    @Resource
    private HabitMapper habitMapper;
    @Resource
    private SubscribeMessageService subscribeMessageService;

    @Scheduled(cron = "10 * * * * ?")
    public void sendFocusFinishReminders() {
        List<FocusSession> sessions = focusService.listNeedRemind();
        if (sessions.isEmpty()) return;
        for (FocusSession s : sessions) {
            try {
                WxUser user = wxUserMapper.selectById(s.getUserId());
                if (user == null || user.getOpenid() == null || user.getOpenid().isEmpty()) continue;
                Habit habit = habitMapper.selectById(s.getHabitId());
                String habitName = habit != null && habit.getName() != null ? habit.getName() : "习惯专注";
                boolean sent = subscribeMessageService.sendFocusFinish(
                        user.getId(), user.getOpenid(), s.getHabitId(), habitName
                );
                if (sent) {
                    focusService.markRemindSent(s.getId());
                }
            } catch (Exception e) {
                log.error("focus remind send error, sessionId=" + s.getId(), e);
            }
        }
    }
}

