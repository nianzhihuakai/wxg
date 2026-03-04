package com.nzhk.wxg.scheduler;

import com.nzhk.wxg.business.habit.entity.Habit;
import com.nzhk.wxg.business.habit.service.IHabitService;
import com.nzhk.wxg.business.wxuser.entity.WxUser;
import com.nzhk.wxg.mapper.WxUserMapper;
import com.nzhk.wxg.wechat.service.SubscribeMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 习惯提醒订阅消息定时任务
 * 每分钟执行一次，在 remind_time 为当前时间（HH:mm）时向用户发送订阅消息
 */
@Slf4j
@Component
public class HabitRemindScheduler {

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    @Resource
    private IHabitService habitService;
    @Resource
    private WxUserMapper wxUserMapper;
    @Resource
    private SubscribeMessageService subscribeMessageService;

    @Scheduled(cron = "0 * * * * ?")
    public void sendHabitReminders() {
        String remindTime = LocalTime.now().format(HHMM);
        List<Habit> habits = habitService.listHabitsNeedRemind(remindTime);
        if (habits.isEmpty()) {
            return;
        }
        log.info("habit remind scheduler: found {} habits to remind at {}", habits.size(), remindTime);
        for (Habit habit : habits) {
            try {
                WxUser user = wxUserMapper.selectById(habit.getUserId());
                if (user == null || user.getOpenid() == null || user.getOpenid().isEmpty()) {
                    log.warn("habit remind skip: user openid not found, habitId={}, userId={}", habit.getId(), habit.getUserId());
                    continue;
                }
                boolean sent = subscribeMessageService.sendHabitRemind(
                        user.getOpenid(),
                        habit.getName() != null ? habit.getName() : "习惯打卡"
                );
                if (sent) {
                    log.info("habit remind sent: habitId={}, openid={}", habit.getId(), user.getOpenid());
                }
            } catch (Exception e) {
                log.error("habit remind error, habitId=" + habit.getId(), e);
            }
        }
    }
}
