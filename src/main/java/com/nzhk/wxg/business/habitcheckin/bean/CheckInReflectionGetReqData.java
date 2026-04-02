package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckInReflectionGetReqData {

    private String habitId;

    private LocalDate checkInDate;
}
