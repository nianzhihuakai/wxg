package com.nzhk.wxg.business.focus.bean;

import lombok.Data;

@Data
public class FocusFinishReqData {
    private String sessionId;
    /** normal/manual/cancel */
    private String finishType;
}

