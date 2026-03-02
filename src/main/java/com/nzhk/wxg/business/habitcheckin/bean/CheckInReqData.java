package com.nzhk.wxg.business.habitcheckin.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckInReqData {

    private String habitId;

    private LocalDate checkInDate;

    private Integer fillCheckInStatus;

    // 打卡类型 1-普通打卡 2-补卡
    private Integer checkInType;
}
