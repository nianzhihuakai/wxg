package com.nzhk.wxg.business.habit.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
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
     * 历史最高连续打卡天数
     */
    @ApiModelProperty("历史最高连续打卡天数")
    private Integer maxStreakDays;

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

    private String habitTypeId;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime archiveDateTime;

    /**
     * 是否开启提醒：0-关闭，1-开启
     */
    @ApiModelProperty("是否开启提醒")
    private Boolean remindFlag;

    /**
     * 提醒时间，格式 HH:mm，如 09:30
     */
    @ApiModelProperty("提醒时间 HH:mm")
    private String remindTime;

    /**
     * 打卡频次类型：fixed-固定周几，weekly-每周N次，monthly-每月N次
     */
    @ApiModelProperty("打卡频次类型")
    private String checkInFrequencyType;

    /**
     * 打卡频次：fixed时为"1,2,3,4,5,6,7"；weekly时为"3"；monthly时为"15"
     */
    @ApiModelProperty("打卡频次")
    private String checkInFrequency;

}
