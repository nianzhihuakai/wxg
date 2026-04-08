package com.nzhk.wxg.business.focus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@TableName("focus_session")
public class FocusSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String habitId;
    /** COUNTDOWN | STOPWATCH */
    private String focusMode;
    private Integer plannedMinutes;
    private Integer targetMinutes;
    private Integer actualSeconds;
    /** 1-running 2-paused 3-finished 4-cancelled */
    private Integer status;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private OffsetDateTime expectedEndTime;
    private OffsetDateTime pauseStartTime;
    private Integer pauseTotalSeconds;
    private Boolean remindSound;
    private Boolean remindVibrate;
    private Boolean remindSubscribe;
    private Boolean remindSentFlag;
    private OffsetDateTime remindSentTime;
    private String finishType;
    private String note;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}

