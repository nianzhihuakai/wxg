package com.nzhk.wxg.business.focus.bean;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FocusSessionResData {
    private String sessionId;
    private String habitId;
    private String focusMode;
    private Integer plannedMinutes;
    private Integer targetMinutes;
    private Integer actualSeconds;
    private Integer status;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private OffsetDateTime expectedEndTime;
    private Integer pauseTotalSeconds;
    private String finishType;
}

