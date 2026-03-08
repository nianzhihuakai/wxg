package com.nzhk.wxg.business.habit.bean;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AddHabitReqData {

    private String name;

    private String habitTypeId;

    private LocalDate startDate;

    private LocalDate endDate;

    /** 是否开启提醒，默认 false */
    private Boolean remindFlag;

    /** 提醒时间，格式 HH:mm，如 09:30 */
    private String remindTime;

    /** 打卡频次类型：fixed/weekly/monthly，默认 fixed */
    private String checkInFrequencyType;

    /** 打卡频次：fixed时为"1,2,3,4,5,6,7"；weekly时为1-7；monthly时为1-31 */
    private String checkInFrequency;
}
