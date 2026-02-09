package com.nzhk.wxg.business.wxuser.bean;

import com.nzhk.wxg.common.cache.UserInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WxUserLoginResData {

    private String token;

    private UserInfo userInfo;
}
