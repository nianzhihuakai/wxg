package com.nzhk.wxg.business.habit.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HabitListResData {

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

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime archiveDateTime;

    private String habitTypeId;

    private String habitTypeName;

    private String checkInId;

    private boolean alreadyCheckedInToday;

    private Integer totalCheckInNum = 0;

    private Integer checkInNum = 0;

    private BigDecimal checkInRate = BigDecimal.ZERO;

    private String checkInFrequencyType;

    private String checkInFrequency;

    /**
     * 今日是否展示为可打卡（可操作打卡按钮）
     * true-今日可打卡或已打卡，false-今日休息或本周/月已达标
     */
    @ApiModelProperty("今日是否展示为可打卡")
    private Boolean showToday;

    /**
     * 不展示打卡时的原因：rest-今日休息日，weekly_done-本周已达标，monthly_done-本月已达标，null-正常可打卡
     */
    @ApiModelProperty("不展示打卡原因: rest/weekly_done/monthly_done")
    private String showReason;

    /** 本周打卡次数（用于 weekly 类型展示 1/3） */
    @ApiModelProperty("本周打卡次数")
    private Integer weekCheckInCount;

    /** 本月打卡次数（用于 monthly 类型展示 5/15） */
    @ApiModelProperty("本月打卡次数")
    private Integer monthCheckInCount;

    @ApiModelProperty("打卡成功后是否弹出写感悟")
    private Boolean promptCheckinReflection;
}
