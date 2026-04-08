package com.nzhk.wxg.business.focus.bean;

import lombok.Data;

@Data
public class FocusStatsResData {
    private Long totalSessions;
    private Long totalSeconds;
    private Long totalMinutes;
    private Long avgMinutes;
}

