package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CheckInDetailResData {

    private Integer totalCheckInNum;

    private Integer checkInNum;

    private BigDecimal checkInRate;

    private List<LocalDate> checkInDate;
}
