package com.nzhk.wxg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalPeriodCountRow;
import com.nzhk.wxg.business.periodgoal.entity.PeriodGoal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

public interface PeriodGoalMapper extends BaseMapper<PeriodGoal> {

    /**
     * 与 [rangeStart, rangeEnd] 有交集的有效目标，按周期起止分组计数
     */
    @Select("SELECT period_start AS periodStart, period_end AS periodEnd, COUNT(*) AS cnt "
            + "FROM period_goal WHERE user_id = #{userId} AND status = 1 AND period_type = #{periodType} "
            + "AND period_start <= #{rangeEnd} AND period_end >= #{rangeStart} "
            + "GROUP BY period_start, period_end")
    List<PeriodGoalPeriodCountRow> selectCountsGroupedByPeriod(
            @Param("userId") String userId,
            @Param("periodType") String periodType,
            @Param("rangeStart") LocalDate rangeStart,
            @Param("rangeEnd") LocalDate rangeEnd);
}
