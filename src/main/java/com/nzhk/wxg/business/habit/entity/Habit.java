package com.nzhk.wxg.business.habit.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 习惯表
 * </p>
 *
 * @author lxy
 * @since 2026-01-28
 */
@Data
@ApiModel(value = "Habit对象", description = "习惯表")
public class Habit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 习惯ID
     */
    @ApiModelProperty("习惯ID")
    private String id;

    /**
     * 用户ID
     */
    @ApiModelProperty("用户ID")
    private String userId;

    /**
     * 习惯名称
     */
    @ApiModelProperty("习惯名称")
    private String name;

    /**
     * 目标描述
     */
    @ApiModelProperty("目标描述")
    private String description;

    /**
     * 图标(emoji)
     */
    @ApiModelProperty("图标(emoji)")
    private String icon;

    /**
     * 图标索引
     */
    @ApiModelProperty("图标索引")
    private Integer iconIndex;

    /**
     * 主题颜色
     */
    @ApiModelProperty("主题颜色")
    private String color;

    /**
     * 颜色索引
     */
    @ApiModelProperty("颜色索引")
    private Integer colorIndex;

    /**
     * 连续天数
     */
    @ApiModelProperty("连续天数")
    private Integer streakDays;

    /**
     * 总打卡天数
     */
    @ApiModelProperty("总打卡天数")
    private Integer totalDays;

    /**
     * 状态: 0-已删除, 1-启用, 2-暂停
     */
    @ApiModelProperty("状态: 0-已删除, 1-启用, 2-暂停")
    private Integer status;

    /**
     * 创建时间(时间戳)
     */
    @ApiModelProperty("创建时间(时间戳)")
    private LocalDateTime createTime;

    /**
     * 更新时间(时间戳)
     */
    @ApiModelProperty("更新时间(时间戳)")
    private LocalDateTime updateTime;

}
