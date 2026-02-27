package com.nzhk.wxg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nzhk.wxg.business.habit.bean.HabitListResData;
import com.nzhk.wxg.business.habit.entity.Habit;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 习惯表 Mapper 接口
 * </p>
 *
 * @author lxy
 * @since 2026-01-28
 */
public interface HabitMapper extends BaseMapper<Habit> {

    List<HabitListResData> selectHabitList(@Param("userId") String userId, @Param("nowDate") LocalDate nowDate, @Param("habitTypeId") String habitTypeId);

    List<HabitListResData> selectArchiveHabitList(String userId, LocalDate now, String habitTypeId);
}
