package com.nzhk.wxg.business.periodgoal.bean;

import lombok.Data;

import java.util.List;

@Data
public class PeriodGoalItemResData {

    private String id;

    private String title;

    private String periodType;

    private String periodStart;

    private String periodEnd;

    private String metricType;

    private Integer targetValue;

    private List<String> habitIds;

    private String harvestHtml;
}
