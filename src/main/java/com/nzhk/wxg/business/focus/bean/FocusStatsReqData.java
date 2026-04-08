package com.nzhk.wxg.business.focus.bean;

import lombok.Data;

@Data
public class FocusStatsReqData {
    private String habitId;
    /** day|week|month|all */
    private String period;
}

