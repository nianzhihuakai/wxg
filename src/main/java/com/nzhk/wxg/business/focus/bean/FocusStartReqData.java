package com.nzhk.wxg.business.focus.bean;

import lombok.Data;

@Data
public class FocusStartReqData {
    private String habitId;
    private Integer plannedMinutes;
    private Boolean remindSound;
    private Boolean remindVibrate;
    private Boolean remindSubscribe;
}

