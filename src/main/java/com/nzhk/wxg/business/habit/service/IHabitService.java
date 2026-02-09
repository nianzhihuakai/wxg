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

    HabitDetailResData getHabitById(HabitDetailReqData data);

    void addHabit(AddHabitReqData data);
}
