package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

@Data
public class UserCheckInRankItemResData {

    private Integer rankNo;

    private String userId;

    private String nickName;

    private String avatarUrl;

    /**
     * 排行值：天数/次数/连续天数
     */
    private Long rankValue;

    /**
     * 当前连续天数（坚持势头展示）
     */
    private Integer currentStreakDays;

    /**
     * 历史最高连续天数（坚持势头展示）
     */
    private Integer maxStreakDays;
}
