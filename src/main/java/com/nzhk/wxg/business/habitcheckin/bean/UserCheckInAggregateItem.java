package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

@Data
public class UserCheckInAggregateItem {

    private String userId;

    private Long checkInDays;

    private Long checkInCount;
}
