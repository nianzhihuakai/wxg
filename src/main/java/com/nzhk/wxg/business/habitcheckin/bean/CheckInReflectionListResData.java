package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

import java.util.List;

@Data
public class CheckInReflectionListResData {

    private Long total;

    private List<CheckInReflectionItemResData> records;
}
