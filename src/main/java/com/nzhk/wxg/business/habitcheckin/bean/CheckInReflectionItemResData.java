package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckInReflectionItemResData {

    private LocalDate checkInDate;

    private String reflection;

    private String reflectionImageUrl;
}
