package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckInReflectionSaveReqData {

    private String habitId;

    private LocalDate checkInDate;

    /**
     * 感悟正文；为 null 时不修改已有正文；非 null 时写入（trim 后空串则清空）
     */
    private String reflection;

    /**
     * 是否更新配图：true 时 imageFileId 为空则清除配图，非空则绑定已上传文件的 fileId
     */
    private Boolean updateImage;

    private String imageFileId;
}
