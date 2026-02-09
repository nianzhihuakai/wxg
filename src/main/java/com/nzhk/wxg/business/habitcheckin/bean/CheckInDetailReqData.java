package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckInDetailReqData {

    private String habitId;

    private LocalDate weekStart;

    private LocalDate weekEnd;

    private LocalDate monthDate;

    private LocalDate yearDate;
}
