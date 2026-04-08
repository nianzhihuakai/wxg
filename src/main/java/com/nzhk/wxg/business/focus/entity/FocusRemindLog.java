package com.nzhk.wxg.business.focus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@TableName("focus_remind_log")
public class FocusRemindLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String habitId;
    private String sessionId;
    private String remindChannel;
    private Integer sendStatus;
    private String errCode;
    private String errMsg;
    private OffsetDateTime createTime;
}

