package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatisticsInfoResData {

    private String firstCheckInDateTime;

    private String lastCheckInDateTime;

    private String mostHabitName;

    private String lessHabitName;

    private Integer totalCheckInDays;

    private Integer checkInNumDays;

    private BigDecimal checkInRate;
}
