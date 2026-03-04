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
}
