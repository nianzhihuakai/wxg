package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserCheckInDateItem {

    private String userId;

    private String nickName;

    private String avatarUrl;

    private LocalDate checkInDate;
}
