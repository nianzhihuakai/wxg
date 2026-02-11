package com.nzhk.wxg.feedback.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 建议与问题反馈表
 * </p>
 *
 * @author lxy
 * @since 2026-02-11
 */
@Data
@ApiModel(value = "Feedback对象", description = "建议与问题反馈表")
public class Feedback implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty("主键")
    private String id;

    /**
     * 提交用户ID，与登录 token 对应用户
     */
    @ApiModelProperty("提交用户ID，与登录 token 对应用户")
    private String userId;

    /**
     * 反馈内容，前端限制 500 字
     */
    @ApiModelProperty("反馈内容，前端限制 500 字")
    private String content;

    /**
     * 状态：0-待处理，1-已处理
     */
    @ApiModelProperty("状态：0-待处理，1-已处理")
    private Integer status;

    /**
     * 提交时间
     */
    @ApiModelProperty("提交时间")
    private LocalDateTime createdAt;

}
