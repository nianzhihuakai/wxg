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
}
