package com.nzhk.wxg.business.habitcheckin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_rank_snapshot")
public class UserRankSnapshot {

    private String userId;

    private Integer checkInDays;

    private Integer checkInCount;

    private Integer currentStreakDays;

    private Integer maxStreakDays;

    private BigDecimal momentumScore;

    private LocalDate lastCheckInDate;

    private LocalDateTime updatedAt;
}
