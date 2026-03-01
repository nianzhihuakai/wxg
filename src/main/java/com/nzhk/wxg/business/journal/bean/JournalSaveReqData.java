package com.nzhk.wxg.business.journal.bean;

import lombok.Data;

import java.util.List;

@Data
public class JournalSaveReqData {

    private String journalId;

    private String date;

    private String moodValue;

    private String moodLabel;

    private String subject;

    private String content;

    private List<JournalImageReqData> images;

    private String clientRequestId;
}
