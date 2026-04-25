package com.nzhk.wxg.business.periodgoal.bean;

import lombok.Data;

import java.time.LocalDate;

/**
 * Mapper 聚合查询行：按周期起止分组计数
 */
@Data
public class PeriodGoalPeriodCountRow {

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private Long cnt;
}
