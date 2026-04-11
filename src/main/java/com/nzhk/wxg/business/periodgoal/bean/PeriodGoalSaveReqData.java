package com.nzhk.wxg.business.periodgoal.bean;

import lombok.Data;

import java.util.List;

@Data
public class PeriodGoalSaveReqData {

    /** 新建可空 */
    private String id;

    private String title;

    private String periodType;

    /** yyyy-MM-dd */
    private String periodStart;

    /** yyyy-MM-dd */
    private String periodEnd;

    private String metricType;

    private Integer targetValue;

    private List<String> habitIds;

    private String harvestHtml;
}
