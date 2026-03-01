package com.nzhk.wxg.business.file.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ApiModel(value = "UploadedFile对象", description = "上传文件记录表")
public class UploadedFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("文件业务ID")
    private String fileId;

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("业务类型")
    private String bizType;

    @ApiModelProperty("原文件名")
    private String originalName;

    @ApiModelProperty("存储路径")
    private String storagePath;

    @ApiModelProperty("访问地址")
    private String url;

    @ApiModelProperty("MIME 类型")
    private String mimeType;

    @ApiModelProperty("文件大小")
    private Long fileSize;

    @ApiModelProperty("图片宽")
    private Integer width;

    @ApiModelProperty("图片高")
    private Integer height;

    @ApiModelProperty("状态 0-删除 1-有效")
    private Integer status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
}
