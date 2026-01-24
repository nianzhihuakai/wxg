package com.nzhk.wxg.business.wxuser.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class WxLoginResVO {

    private String sessionKey;

    private String openId;
}
