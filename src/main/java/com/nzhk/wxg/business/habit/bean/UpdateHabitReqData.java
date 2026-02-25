package com.nzhk.wxg.business.habit.bean;

import lombok.Data;

@Data
public class UpdateHabitReqData {

    private String habitId;

    private String name;

    private String habitTypeId;
}
