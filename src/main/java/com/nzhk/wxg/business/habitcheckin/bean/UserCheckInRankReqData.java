package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

@Data
public class UserCheckInRankReqData {

    /**
     * 排行类型：days(天数) / count(次数) / streak(连续)
     */
    private String rankType;
}
