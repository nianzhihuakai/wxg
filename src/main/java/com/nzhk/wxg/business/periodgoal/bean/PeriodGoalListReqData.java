package com.nzhk.wxg.business.periodgoal.bean;

import lombok.Data;

@Data
public class PeriodGoalListReqData {

    private String periodType;

    /** yyyy-MM-dd */
    private String periodStart;

    /** yyyy-MM-dd */
    private String periodEnd;
}
