package com.nzhk.wxg.business.journal.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ApiModel(value = "Journal对象", description = "日记表")
public class Journal implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("日记日期")
    private LocalDate journalDate;

    @ApiModelProperty("心情编码")
    private String moodValue;

    @ApiModelProperty("心情文案")
    private String moodLabel;

    @ApiModelProperty("主题")
    private String subject;

    @ApiModelProperty("正文")
    private String content;

    @ApiModelProperty("状态 0-删除 1-有效")
    private Integer status;

    @ApiModelProperty("幂等请求号")
    private String clientRequestId;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
