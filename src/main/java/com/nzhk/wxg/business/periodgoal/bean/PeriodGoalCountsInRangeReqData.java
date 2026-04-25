package com.nzhk.wxg.business.periodgoal.bean;

import lombok.Data;

@Data
public class PeriodGoalCountsInRangeReqData {

    /** week / month / year */
    private String periodType;

    /** yyyy-MM-dd 时间窗起点（含） */
    private String rangeStart;

    /** yyyy-MM-dd 时间窗终点（含） */
    private String rangeEnd;
}
