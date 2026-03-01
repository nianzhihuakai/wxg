package com.nzhk.wxg.business.journal.bean;

import lombok.Data;

@Data
public class JournalImageReqData {

    private String fileId;

    private String url;

    private Integer sort;

    private Integer width;

    private Integer height;

    private Long fileSize;

    private String mimeType;
}
