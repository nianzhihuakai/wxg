package com.nzhk.wxg.business.journal.bean;

import lombok.Data;

import java.util.List;

@Data
public class JournalListResData {

    private Integer pageNo;

    private Integer pageSize;

    private Long total;

    private Boolean hasMore;

    private List<JournalHistoryItemResData> records;
}
