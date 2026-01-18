package com.nzhk.wxg.business.wxuser.bean;

import lombok.Data;

@Data
public class WxUserLoginReqData {

    private String code;

    private String nickName;

    private String avatarUrl;
}
