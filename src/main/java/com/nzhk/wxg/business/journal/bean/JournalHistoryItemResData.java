package com.nzhk.wxg.business.journal.bean;

import lombok.Data;

import java.util.List;

@Data
public class JournalHistoryItemResData {

    private String journalId;

    private String date;

    private String moodValue;

    private String moodLabel;

    private String subject;

    private String content;

    private String updatedAt;

    private List<JournalImageResData> images;
}
