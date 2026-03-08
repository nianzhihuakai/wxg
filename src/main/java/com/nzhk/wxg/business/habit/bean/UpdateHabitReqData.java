package com.nzhk.wxg.business.habit.bean;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateHabitReqData {

    private String habitId;

    private String name;

    private String habitTypeId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean remindFlag;

    private String remindTime;

    /** 打卡频次类型：fixed/weekly/monthly */
    private String checkInFrequencyType;

    /** 打卡频次 */
    private String checkInFrequency;
}
