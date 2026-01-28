package com.nzhk.wxg.business.habit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wxg.business.habit.entity.Habit;
import com.nzhk.wxg.business.habit.service.IHabitService;
import com.nzhk.wxg.mapper.HabitMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 习惯表 服务实现类
 * </p>
 *
 * @author lxy
 * @since 2026-01-28
 */
@Service
public class HabitServiceImpl extends ServiceImpl<HabitMapper, Habit> implements IHabitService {

}
