package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StatisticsInfoResData {

    private String firstCheckInDateTime;

    private String lastCheckInDateTime;

    private String mostHabitName;

    private String lessHabitName;

    private Integer totalCheckInDays;

    private Integer checkInNumDays;

    private BigDecimal checkInRate;

    /** 当前连续打卡天数（全量统计：有任意习惯打卡即算） */
    private Integer currentStreakDays;

    /** 最长连续打卡天数 */
    private Integer maxStreakDays;

    /** 已达成的打卡里程碑，如 ["7天", "30天", "100天"] */
    private List<String> achievedMilestones;

    /** 下一个未达成的里程碑，如 "365天"；若已全部达成则为 null */
    private String nextMilestone;

    /** 各习惯打卡排行 Top5 */
    private List<HabitRankItem> habitRanking;

    /** 打卡总次数（所有习惯的打卡记录数之和） */
    private Integer totalCheckInCount;

    /** 习惯总数 */
    private Integer habitCount;
}
