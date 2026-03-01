package com.nzhk.wxg.business.journal.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ApiModel(value = "JournalImage对象", description = "日记图片关联表")
public class JournalImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("日记ID")
    private String journalId;

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("文件ID")
    private String fileId;

    @ApiModelProperty("图片地址")
    private String imageUrl;

    @ApiModelProperty("排序")
    private Integer sortOrder;

    @ApiModelProperty("宽")
    private Integer width;

    @ApiModelProperty("高")
    private Integer height;

    @ApiModelProperty("文件大小")
    private Long fileSize;

    @ApiModelProperty("MIME 类型")
    private String mimeType;

    @ApiModelProperty("状态 0-删除 1-有效")
    private Integer status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
}
