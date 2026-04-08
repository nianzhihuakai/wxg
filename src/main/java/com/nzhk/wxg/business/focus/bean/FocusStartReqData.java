package com.nzhk.wxg.business.focus.bean;

import lombok.Data;

@Data
public class FocusStartReqData {
    private String habitId;
    /** COUNTDOWN | STOPWATCH */
    private String focusMode;
    private Integer plannedMinutes;
    private Integer targetMinutes;
    private Boolean remindSound;
    private Boolean remindVibrate;
    private Boolean remindSubscribe;
}

