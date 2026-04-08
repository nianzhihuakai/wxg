package com.nzhk.wxg.business.focus.bean;

import lombok.Data;

@Data
public class FocusAdjustReqData {
    private String sessionId;
    private Integer deltaMinutes;
}

