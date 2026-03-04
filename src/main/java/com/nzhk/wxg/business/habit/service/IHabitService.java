package com.nzhk.wxg.business.habit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nzhk.wxg.business.habit.bean.*;
import com.nzhk.wxg.business.habit.entity.Habit;

import java.util.List;

/**
 * <p>
 * 习惯表 服务类
 * </p>
 *
 * @author lxy
 * @since 2026-01-28
 */
public interface IHabitService extends IService<Habit> {

    List<HabitListResData> getHabits(HabitListReqData data);

    List<HabitListResData> getArchiveHabits(HabitListReqData data);

    HabitDetailResData getHabitById(HabitDetailReqData data);

    void addHabit(AddHabitReqData data);

    void updateHabit(UpdateHabitReqData data);

    void archiveHabit(UpdateHabitReqData data);

    void deleteHabit(UpdateHabitReqData data);

    /**
     * 定时任务：将 end_date 为昨天且 status=1 的习惯自动归档（status=2）
     */
    void autoArchiveEndedHabits();

    /**
     * 查询需要提醒的习惯：remind_flag=true, remind_time 匹配, status=1, 在日期范围内
     *
     * @param remindTime HH:mm 格式
     * @return 需要提醒的习惯列表
     */
    List<Habit> listHabitsNeedRemind(String remindTime);
}
