package com.nzhk.wxg.business.wxuser.bean;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoResData {

    private String id;

    private LocalDateTime createTime;

    private String versionNo = "1.1.0";
}
