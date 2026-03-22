package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 习惯打卡排行单项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitRankItem {

    /** 习惯名称 */
    private String habitName;

    /** 打卡次数 */
    private Long count;
}
