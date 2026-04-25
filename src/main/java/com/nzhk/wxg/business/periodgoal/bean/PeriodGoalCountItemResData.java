package com.nzhk.wxg.business.periodgoal.bean;

import lombok.Data;

@Data
public class PeriodGoalCountItemResData {

    private String periodStart;

    private String periodEnd;

    /** 该自然周期下有效目标条数 */
    private int count;
}
