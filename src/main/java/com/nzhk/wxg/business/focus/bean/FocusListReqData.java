package com.nzhk.wxg.business.focus.bean;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FocusListReqData {
    private String habitId;
    private LocalDate date;
    private Integer pageNo;
    private Integer pageSize;
}

