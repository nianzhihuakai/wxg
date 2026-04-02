package com.nzhk.wxg.business.habitcheckin.bean;

import lombok.Data;

@Data
public class CheckInReflectionListReqData {

    private String habitId;

    private Integer pageNo = 1;

    private Integer pageSize = 20;
}
