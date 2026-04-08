package com.nzhk.wxg.business.focus.bean;

import lombok.Data;

import java.util.List;

@Data
public class FocusListResData {
    private List<FocusSessionResData> list;
    private Long total;
}

