package com.nzhk.wxg.business.habit.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HabitDetailResData {

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

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime archiveDateTime;

    private String habitTypeId;

    private Boolean remindFlag;

    private String remindTime;

    /** 打卡频次类型：fixed/weekly/monthly */
    private String checkInFrequencyType;

    /** 打卡频次 */
    private String checkInFrequency;

    private Integer totalCheckInNum = 0;

    private Integer checkInNum = 0;

    private BigDecimal checkInRate = BigDecimal.ZERO;

    /** 本周打卡次数（用于 weekly 类型展示 1/3） */
    @ApiModelProperty("本周打卡次数")
    private Integer weekCheckInCount;

    /** 本月打卡次数（用于 monthly 类型展示 5/15） */
    @ApiModelProperty("本月打卡次数")
    private Integer monthCheckInCount;
}
