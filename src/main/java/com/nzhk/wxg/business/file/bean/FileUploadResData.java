package com.nzhk.wxg.business.file.bean;

import lombok.Data;

@Data
public class FileUploadResData {

    private String fileId;

    private String url;

    private Integer width;

    private Integer height;

    private Long size;

    private String mimeType;
}
