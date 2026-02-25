package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckInReqData {

    private String habitId;

    private LocalDate checkInDate;

    private Integer fillCheckInStatus;
}
