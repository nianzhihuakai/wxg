package com.nzhk.wxg.wechat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订阅消息发送记录表
 */
@Data
@TableName("subscribe_message_log")
@ApiModel(value = "SubscribeMessageLog对象", description = "订阅消息发送记录表")
public class SubscribeMessageLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("用户表 id (wx_user.id)")
    private String userId;

    @ApiModelProperty("用户 openid")
    private String openid;

    @ApiModelProperty("发送的模板数据 JSON")
    private String sendContent;

    @ApiModelProperty("微信模板 ID")
    private String templateId;

    @ApiModelProperty("业务类型：habit_remind-习惯提醒等")
    private String bizType;

    @ApiModelProperty("业务ID，如 habit_id")
    private String bizId;

    @ApiModelProperty("扩展数据，如 habitName")
    private String extraData;

    @ApiModelProperty("发送状态：0-失败，1-成功")
    private Integer sendStatus;

    @ApiModelProperty("微信返回错误码，成功时为 null")
    private Integer errcode;

    @ApiModelProperty("微信返回错误信息，成功时为 null")
    private String errmsg;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
}
