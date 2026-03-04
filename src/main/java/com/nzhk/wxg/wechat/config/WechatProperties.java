package com.nzhk.wxg.wechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wxg.wechat")
public class WechatProperties {

    private String appid;
    private String secret;
    private String subscribeTemplateId;
}
